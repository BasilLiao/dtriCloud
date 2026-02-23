package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseHistoryDao;
import dtri.com.tw.pgsql.dao.WarehouseInventoryDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseHistory;
import dtri.com.tw.pgsql.entity.WarehouseInventory;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;

@Service
public class WarehouseInventoryServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private WarehouseInventoryDao inventoryDao;

	@Autowired
	private WarehouseAreaDao areaDao;

	@Autowired
	private BasicIncomingListDao incomingListDao;

	@Autowired
	private BasicShippingListDao shippingListDao;

	@Autowired
	private WarehouseHistoryDao historyDao;

	@Autowired
	private EntityManager em;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "widate"));// 盤點時間
		orders.add(new Order(Direction.ASC, "wiwaalias"));// 倉別
		orders.add(new Order(Direction.ASC, "wiwaslocation"));// 儲位
		orders.add(new Order(Direction.ASC, "wiwmpnb"));// 物料號
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<WarehouseInventory> entitys = inventoryDao.findAllBySearch(null, null, null, null, null, null,
					null, null, null, null);
			ArrayList<WarehouseArea> warehouseAreas = (ArrayList<WarehouseArea>) areaDao.findAll();
			ArrayList<BasicIncomingList> basicIncomingLists = incomingListDao.findAllByCheckInventory(null, null, null);// 未入+已入數量->不等同於->須入數量
			ArrayList<BasicShippingList> basicShippingLists = shippingListDao.findAllByCheckBslpngqty();// 已領+未核單

			// Step3-2.資料區分(一般/細節)
			// 取得物料位置
			ArrayList<WarehouseInventory> entityNews = new ArrayList<WarehouseInventory>();
			ArrayList<WarehouseInventory> entityRemoves = new ArrayList<WarehouseInventory>();
			Map<String, WarehouseArea> mapWarehouseAreas = new TreeMap<String, WarehouseArea>();
			Map<String, TreeMap<String, Integer>> mapBasicIncomings = new HashMap<>();// 倉別_物料號:<工單號-流水號:數量>
			Map<String, TreeMap<String, Integer>> mapBasicShippings = new HashMap<>();// 倉別_物料號:<工單號-流水號:數量>
			warehouseAreas.forEach(w -> {
				// 帳上+實際數量 不可為0
				//if (!w.getWatqty().equals(0) || !w.getWaerptqty().equals(0)) {
					w.setSysstatus(0);
					mapWarehouseAreas.put(w.getWaaliasawmpnb(), w);// 倉儲_物料:
				//}
			});
			basicIncomingLists.forEach(i -> {// 入料
				String docKey = i.getBilclass() + "-" + i.getBilsn() + "-" + i.getBilnb();
				Integer qty = i.getBilpnqty();
				String pnumber = i.getBilpnumber(); // 物料號
				String towho = i.getBiltowho().replaceAll("[\\[\\]]", "").split("_")[0] + "_";// 倉別
				// 物料號:<工單號-流水號:數量>
				// 如果 mapBasicIncomings 裡沒有這個物料號，就新建一個 TreeMap 給它
				// 特出 A431/ A581 / A561 排除->因為本身單據同步需要未核單的->此處需要已核單的
				if ((i.getBilclass().equals("A431") || i.getBilclass().equals("A581") || i.getBilclass().equals("A561"))
						&& i.getBilstatus() != 1) {
					// 不可納入計算
				} else {
					mapBasicIncomings.computeIfAbsent(towho + pnumber, k -> new TreeMap<>()).put(docKey, qty);
				}
			});
			basicShippingLists.forEach(o -> {// 領料
				String docKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
				Integer qty = o.getBslpngqty();
				String pnumber = o.getBslpnumber(); // 物料號
				String fromwho = o.getBslfromwho().replaceAll("[\\[\\]]", "").split("_")[0] + "_";// 倉別
				//特別標註 
				if(o.getBslcheckin()==1) {
					docKey+="*>"+docKey;
				};
				// 物料號:<工單號-流水號:數量>
				// 如果 mapBasicIncomings 裡沒有這個物料號，就新建一個 TreeMap 給它
				mapBasicShippings.computeIfAbsent(fromwho + pnumber, k -> new TreeMap<>()).put(docKey, qty);
			});

			// 檢查看看是否有->更新的資料?
			entitys.forEach(e -> {
				String waaliasawmpnb = e.getWiwaaliasnb();// 倉儲_物料

				if (mapWarehouseAreas.containsKey(waaliasawmpnb)) {// 倉儲_物料
					// WarehouseArea->資料轉換->WarehouseInventory;
					// 標記已使用過
					WarehouseArea oneArea = mapWarehouseAreas.get(waaliasawmpnb);
					oneArea.setSysstatus(1);
					mapWarehouseAreas.put(oneArea.getWaaliasawmpnb(), oneArea);

					// === 依物料號填入 入料 / 領料 JSON ===
					// String pnumber = oneArea.getWawmpnb(); // 物料號（關鍵）
					TreeMap<String, Integer> incomingMap = mapBasicIncomings.get(e.getWiwaaliasnb());
					TreeMap<String, Integer> shippingMap = mapBasicShippings.get(e.getWiwaaliasnb());
					JsonArray incomingString = new JsonArray();
					JsonArray shippingString = new JsonArray();
					Integer incomingTotal = 0;// 在途入_總數
					Integer shippingTotal = 0;// 在途出_總數
					// 入料清單
					if (incomingMap != null && incomingMap.size() > 0) {
						for (Map.Entry<String, Integer> entry : incomingMap.entrySet()) {
							String key = entry.getKey();
							Integer val = entry.getValue();
							incomingTotal += val;
							incomingString.add(key + "_" + val);// 單據_數量
						}
						incomingString.add("All_" + incomingTotal);
					}
					// 領料清單
					if (shippingMap != null && shippingMap.size() > 0) {
						for (Map.Entry<String, Integer> entry : shippingMap.entrySet()) {
							String key = entry.getKey();
							Integer val = entry.getValue();
							shippingTotal += val;
							shippingString.add(key + "_" + val);// 單據_數量
						}
						shippingString.add("All_" + shippingTotal);
					}
					// 在途數量+實際數量+帳務數 = 不同在更新
					boolean witqtyCheck = e.getWitqty() != (incomingTotal + shippingTotal);
					boolean wirqtyCheck = !e.getWirqty().equals(oneArea.getWatqty());
					boolean winqtyCheck = !e.getWinqty().equals(oneArea.getWaerptqty());
					boolean wiincoming = !e.getWiincoming().equals(incomingString.toString());
					boolean wishipping = !e.getWishipping().equals(shippingString.toString());

					if (witqtyCheck || wirqtyCheck || winqtyCheck || wiincoming || wishipping) {
						e.setWitqty(incomingTotal + shippingTotal);// 在途數量
						e.setWiincoming(incomingString.toString());// 入料單類型 JSON格式['單據_數量']
						e.setWishipping(shippingString.toString());// 領料單類型 JSON格式['單據_數量']
						//
						e.setWiwmpnb(oneArea.getWawmpnb());// 物料號
						e.setWiwmname("");// 物料名
						e.setWiwaalias(oneArea.getWaalias());// 倉別
						e.setWiwaaliasname(oneArea.getWaaname() == null ? "" : oneArea.getWaaname());// 庫別名稱
						e.setWiwaslocation(oneArea.getWaslocation());// 物料儲位
						e.setWirqty(oneArea.getWatqty());// 實際數量
						e.setWinqty(oneArea.getWaerptqty());// 帳務數
						e.setSysmdate(new Date());
						e.setSysmuser("system");

						// 標記已更新
						entityNews.add(e);
					}
				} else {
					// 沒有比對到?->標記移除?
					entityRemoves.add(e);
				}

			});
			// 新增?
			mapWarehouseAreas.forEach((k, w) -> {
				if (w.getSysstatus() == 0) {
					WarehouseInventory oneNew = new WarehouseInventory();
					oneNew.setWiwmpnb(w.getWawmpnb());// 物料號
					oneNew.setWiwmname("");// 物料名
					oneNew.setWiwaaliasname(w.getWaaname() == null ? "" : w.getWaaname());// 庫別名稱
					oneNew.setWiwaalias(w.getWaalias());// 倉別
					oneNew.setWiwaslocation(w.getWaslocation());// 物料儲位
					oneNew.setWirqty(w.getWatqty());// 實際數量
					oneNew.setWinqty(w.getWaerptqty());// 帳務數
					oneNew.setWiwaaliasnb(k);// 倉別_物料
					// === 依物料號填入 入料 / 領料 JSON ===
					TreeMap<String, Integer> incomingMap = mapBasicIncomings.get(k);
					TreeMap<String, Integer> shippingMap = mapBasicShippings.get(k);
					JsonArray incomingString = new JsonArray();
					JsonArray shippingString = new JsonArray();
					Integer incomingTotal = 0;// 在途入_總數
					Integer shippingTotal = 0;// 在途出_總數
					// 入料清單
					if (incomingMap != null && incomingMap.size() > 0) {
						for (Map.Entry<String, Integer> entry : incomingMap.entrySet()) {
							String key = entry.getKey();
							Integer val = entry.getValue();
							incomingTotal += val;
							incomingString.add(key + "_" + val);// 單據_數量
						}
						incomingString.add("All_" + incomingTotal);
					}
					// 領料清單
					if (shippingMap != null && shippingMap.size() > 0) {
						for (Map.Entry<String, Integer> entry : shippingMap.entrySet()) {
							String key = entry.getKey();
							Integer val = entry.getValue();
							shippingTotal += val;
							shippingString.add(key + "_" + val);// 單據_數量
						}
						shippingString.add("All_" + shippingTotal);
					}

					oneNew.setWitqty(incomingTotal - shippingTotal);// 在途數量
					oneNew.setWiincoming(incomingString.toString());// 入料單類型 JSON格式['單據_數量']
					oneNew.setWishipping(shippingString.toString());// 領料單類型 JSON格式['單據_數量']
					entityNews.add(oneNew);
				}
				w.setSysstatus(0);
			});

			// 移除
			inventoryDao.deleteAll(entityRemoves);
			// 須更新+新增
			inventoryDao.saveAll(entityNews);
			// 重新查
			entitys = inventoryDao.findAllBySearch(null, null, null, null, null, null, null, null, null, pageable);
			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseInventory", null,
					2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = WarehouseInventory.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wiwaaliasnb", "Ex:倉別_品號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wiwaslocation", "Ex:物料位置?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "ssyscdate", "Ex:開始時間?", true, //
					PackageService.SearchType.date, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "esyscdate", "Ex:結束時間?", true, //
					PackageService.SearchType.date, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectWicheckArr = new JsonArray();
			selectWicheckArr.add("未確認_false");
			selectWicheckArr.add("確認盤點_true");
			searchJsons = packageService.searchSet(searchJsons, selectWicheckArr, "wicheck", "Ex:確認?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);
			JsonArray selectWignowqty = new JsonArray();
			selectWignowqty.add("盤點差異_1");
			searchJsons = packageService.searchSet(searchJsons, selectWignowqty, "wignowqty", "Ex:盤點差距?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseInventory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					WarehouseInventory.class);

			ArrayList<WarehouseInventory> entitys = inventoryDao.findAllBySearch(searchData.getWiwmpnb(),
					searchData.getWiwaslocation(), searchData.getWiwaalias(), searchData.getWiwaaliasnb(),
					searchData.getSsyscdate(), searchData.getEsyscdate(), null, searchData.getWicheck(), searchData.getWignowqty(),
					pageable);
			// Step4-2.資料區分(一般/細節)
			// 如果只有一筆資料->可進行同步->
			if (entitys.size() == 1) {
				ArrayList<WarehouseArea> warehouseAreas = (ArrayList<WarehouseArea>) areaDao
						.findAllByWaaliasawmpnbOnlyOne(searchData.getWiwaaliasnb());// 單一個物料
				ArrayList<BasicIncomingList> basicIncomingLists = incomingListDao.findAllByCheckInventory(null, null,
						null);// 未入+已入數量->不等同於->須入數量
				ArrayList<BasicShippingList> basicShippingLists = shippingListDao.findAllByCheckBslpngqtyAndCheckin();// 已領+未核單
				Map<String, WarehouseArea> mapWarehouseAreas = new TreeMap<String, WarehouseArea>();
				Map<String, TreeMap<String, Integer>> mapBasicIncomings = new HashMap<>();// 倉別_物料號:<工單號-流水號:數量>
				Map<String, TreeMap<String, Integer>> mapBasicShippings = new HashMap<>();// 倉別_物料號:<工單號-流水號:數量>
				warehouseAreas.forEach(w -> {
					// 帳上+實際數量 不可為0
					
						w.setSysstatus(0);
						mapWarehouseAreas.put(w.getWaaliasawmpnb(), w);// 倉儲_物料:
					
				});
				basicIncomingLists.forEach(i -> {// 入料
					String docKey = i.getBilclass() + "-" + i.getBilsn() + "-" + i.getBilnb();
					Integer qty = i.getBilpnqty();
					String pnumber = i.getBilpnumber(); // 物料號
					String towho = i.getBiltowho().replaceAll("[\\[\\]]", "").split("_")[0] + "_";// 倉別
					// 物料號:<工單號-流水號:數量>
					// 如果 mapBasicIncomings 裡沒有這個物料號，就新建一個 TreeMap 給它
					// 特出 A581/ A561 排除->因為本身單據同步需要未核單的->此處需要已核單的
					if ((i.getBilclass().equals("A431") || i.getBilclass().equals("A581")
							|| i.getBilclass().equals("A561")) && i.getBilstatus() != 1) {
						// 不可納入計算
					} else {
						mapBasicIncomings.computeIfAbsent(towho + pnumber, k -> new TreeMap<>()).put(docKey, qty);
					}

				});
				basicShippingLists.forEach(o -> {// 領料
					String docKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
					Integer qty = o.getBslpngqty();
					String pnumber = o.getBslpnumber(); // 物料號
					String fromwho = o.getBslfromwho().replaceAll("[\\[\\]]", "").split("_")[0] + "_";// 倉別
					//特別標註 
					if(o.getBslcheckin()==1) {
						docKey+="*>"+docKey;
					};
					// 物料號:<工單號-流水號:數量>
					// 如果 basicShippingLists 裡沒有這個物料號，就新建一個 TreeMap 給它
					mapBasicShippings.computeIfAbsent(fromwho + pnumber, k -> new TreeMap<>()).put(docKey, qty);
					
					
				});
				// 檢查看看是否有->更新的資料?
				entitys.forEach(e -> {
					String waaliasawmpnb = e.getWiwaaliasnb();// 倉儲_物料
					if (mapWarehouseAreas.containsKey(waaliasawmpnb)) {// 倉儲_物料
						// WarehouseArea->資料轉換->WarehouseInventory;
						// 標記已使用過
						WarehouseArea oneArea = mapWarehouseAreas.get(waaliasawmpnb);
						oneArea.setSysstatus(1);
						mapWarehouseAreas.put(oneArea.getWaaliasawmpnb(), oneArea);

						// === 依物料號填入 入料 / 領料 JSON ===
						// String pnumber = oneArea.getWawmpnb(); // 物料號（關鍵）
						TreeMap<String, Integer> incomingMap = mapBasicIncomings.get(e.getWiwaaliasnb());
						TreeMap<String, Integer> shippingMap = mapBasicShippings.get(e.getWiwaaliasnb());
						JsonArray incomingString = new JsonArray();
						JsonArray shippingString = new JsonArray();
						Integer incomingTotal = 0;// 在途入_總數
						Integer shippingTotal = 0;// 在途出_總數
						// 入料清單
						if (incomingMap != null && incomingMap.size() > 0) {
							for (Map.Entry<String, Integer> entry : incomingMap.entrySet()) {
								String key = entry.getKey();
								Integer val = entry.getValue();
								incomingTotal += val;
								incomingString.add(key + "_" + val);// 單據_數量
							}
							incomingString.add("All_" + incomingTotal);
						}
						// 領料清單
						if (shippingMap != null && shippingMap.size() > 0) {
							for (Map.Entry<String, Integer> entry : shippingMap.entrySet()) {
								String key = entry.getKey();
								Integer val = entry.getValue();
								shippingTotal += val;
								shippingString.add(key + "_" + val);// 單據_數量
							}
							shippingString.add("All_" + shippingTotal);
						}
						// 在途數量+實際數量+帳務數 = 不同在更新
						boolean witqtyCheck = e.getWitqty() != (incomingTotal + shippingTotal);
						boolean wirqtyCheck = !e.getWirqty().equals(oneArea.getWatqty());
						boolean winqtyCheck = !e.getWinqty().equals(oneArea.getWaerptqty());
						boolean wiincoming = !e.getWiincoming().equals(incomingString.toString());
						boolean wishipping = !e.getWishipping().equals(shippingString.toString());

						if (witqtyCheck || wirqtyCheck || winqtyCheck || wiincoming || wishipping) {
							e.setWitqty(incomingTotal + shippingTotal);// 在途數量
							e.setWiincoming(incomingString.toString());// 入料單類型 JSON格式['單據_數量']
							e.setWishipping(shippingString.toString());// 領料單類型 JSON格式['單據_數量']
							//
							e.setWiwmpnb(oneArea.getWawmpnb());// 物料號
							e.setWiwmname("");// 物料名
							e.setWiwaalias(oneArea.getWaalias());// 倉別
							e.setWiwaaliasname(oneArea.getWaaname() == null ? "" : oneArea.getWaaname());// 庫別名稱
							e.setWiwaslocation(oneArea.getWaslocation());// 物料儲位
							e.setWirqty(oneArea.getWatqty());// 實際數量
							e.setWinqty(oneArea.getWaerptqty());// 帳務數
							e.setSysmdate(new Date());
							e.setSysmuser("system");
						}
					}
				});
				// 更新
				inventoryDao.saveAll(entitys);
			}

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("");

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new WarehouseInventory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("wiid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_widate_ssyscdate_esyscdate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseInventory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<WarehouseInventory>>() {
					});

			// Step2.資料檢查
			for (WarehouseInventory entityData : entityDatas) {
				// 檢查-名稱使否有盤點人
				if (entityData.getWiuser().equals("") || !entityData.getWicuser().equals("")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "無盤點人 or 已確認!" });

				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<WarehouseInventory> saveDatas = new ArrayList<>();
		ArrayList<BasicIncomingList> incomingLists = new ArrayList<>();
		ArrayList<BasicShippingList> shippingLists = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getWiid() != null) {
				WarehouseInventory entityDataOld = new WarehouseInventory();
				entityDataOld = inventoryDao.getReferenceById(x.getWiid());

				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				// 修改
				entityDataOld.setWignowqty(0);// 盤點差異
				entityDataOld.setWiuser("");// 盤點人
				entityDataOld.setWicuser(x.getWiuser() + "_(" + packageBean.getUserAccount() + ")");// 盤點人+確認人
				entityDataOld.setWicheck(true);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		incomingListDao.saveAll(incomingLists);
		shippingListDao.saveAll(shippingLists);
		inventoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 修改資料(限定盤點) */
	@Transactional
	public PackageBean setModifyLimit(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseInventory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<WarehouseInventory>>() {
					});

			// Step2.資料檢查
			for (WarehouseInventory warehouseInventory : entityDatas) {
				// 盤點不可有負數
				if (warehouseInventory.getWinowqty() < 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1008, Lan.zh_TW,
							new String[] { "Qty < 0 ?!" });
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<WarehouseInventory> saveDatas = new ArrayList<>();

		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getWiid() != null) {
				WarehouseInventory entityDataOld = inventoryDao.getReferenceById(x.getWiid());
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				// 盤點數 - 實際數量 = 差異數量
				int whpomqty = x.getWinowqty() - x.getWirqty();
				// 盤點差異 =>盤點數-(帳上數-在途數)
				int wignowqty = x.getWinowqty() - (x.getWinqty() - x.getWitqty());
				// 修改
				entityDataOld.setWinowqty(x.getWinowqty());// 盤點數
				entityDataOld.setWignowqty(wignowqty);// 盤點差異
				entityDataOld.setWirqty(x.getWinowqty());// 實際數量
				entityDataOld.setWidate(new Date());// 盤點時間
				entityDataOld.setWiuser(packageBean.getUserAccount());// 盤點人
				entityDataOld.setWicuser("");// 確認人->盤點人+確認人
				entityDataOld.setWicheck(false);
				saveDatas.add(entityDataOld);
				//
				ArrayList<WarehouseArea> arrayList = areaDao.findAllByWaaliasawmpnbOnlyOne(x.getWiwaaliasnb());
				if (arrayList.size() == 1) {
					// 倉庫更新
					WarehouseArea wa = arrayList.get(0);
					wa.setWatqty(x.getWinowqty());
					areaDao.save(wa);

					// 紀錄更新
					WarehouseHistory history = new WarehouseHistory();
					history.setWhtype("盤點(User)");
					history.setWhwmslocation("-----");
					history.setWhcontent("-----");
					history.setWhwmpnb(x.getWiwmpnb());
					history.setWhfuser(packageBean.getUserAccount());
					history.setWheqty(x.getWinqty());
					history.setWhcqty(x.getWinowqty());
					history.setWhpomqty("" + whpomqty);
					history.setWhcheckin("已核單");
					historyDao.save(history);

				}
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		inventoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
//		// =======================資料準備=======================
//		ArrayList<WarehouseInventory> entityDatas = new ArrayList<>();
//		// =======================資料檢查=======================
//		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
//			// Step1.資料轉譯(一般)
//			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
//					new TypeReference<ArrayList<WarehouseInventory>>() {
//					});

//			// Step2.資料檢查
//			for (WarehouseInventory entityData : entityDatas) {
//				// 檢查-名稱重複(有資料 && 不是同一筆資料)
//				ArrayList<WarehouseInventory> checkDatas = inventoryDao.findAllByCheck(entityData.getWawmpnb(), null,
//						entityData.getWaalias());
//				if (checkDatas.size() > 0) {
//					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
//							new String[] { entityData.getWawmpnb() });
//				}
//			}
//		}

		// =======================資料整理=======================
		// 資料Data
//		ArrayList<WarehouseInventory> saveDatas = new ArrayList<>();
//		entityDatas.forEach(x -> {
//			x.setSysmdate(new Date());
//			x.setSysmuser(packageBean.getUserAccount());
//			x.setSysodate(new Date());
//			x.setSysouser(packageBean.getUserAccount());
//			x.setSyscdate(new Date());
//			x.setSyscuser(packageBean.getUserAccount());
//			// 新增
//			x.setWaaliasawmpnb(x.getWaalias() + "_" + x.getWawmpnb());
//			x.setWaid(null);
//			saveDatas.add(x);
//		});
		// =======================資料儲存=======================
		// 資料Detail
//		inventoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
//		// =======================資料準備 =======================
//		ArrayList<WarehouseInventory> entityDatas = new ArrayList<>();
//		// =======================資料檢查=======================
//		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
//			// Step1.資料轉譯(一般)
//			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
//					new TypeReference<ArrayList<WarehouseInventory>>() {
//					});
//			// Step2.資料檢查
//		}
//		// =======================資料整理=======================
//		// Step3.一般資料->寫入
//		ArrayList<WarehouseInventory> saveDatas = new ArrayList<>();
//		entityDatas.forEach(x -> {
//			// 排除 沒有ID
//			if (x.getWiid() != null) {
//				WarehouseInventory entityDataOld = inventoryDao.findById(x.getWiid()).get();
//				entityDataOld.setSysmdate(new Date());
//				entityDataOld.setSysmuser(packageBean.getUserAccount());
//				entityDataOld.setSysstatus(2);
//				saveDatas.add(entityDataOld);
//			}
//		});
//		// =======================資料儲存=======================
//		// 資料Data
//		inventoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
//		// =======================資料準備 =======================
//		ArrayList<WarehouseInventory> entityDatas = new ArrayList<>();
//		// =======================資料檢查=======================
//		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
//			// Step1.資料轉譯(一般)
//			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
//					new TypeReference<ArrayList<WarehouseInventory>>() {
//					});
//			// Step2.資料檢查
//		}
//		// =======================資料整理=======================
//		// Step3.一般資料->寫入
//		ArrayList<WarehouseInventory> saveDatas = new ArrayList<>();
//		// 一般-移除內容
//		entityDatas.forEach(x -> {
//			// 排除 沒有ID
//			if (x.getWiid() != null) {
//				WarehouseInventory entityDataOld = inventoryDao.getReferenceById(x.getWiid());
//				saveDatas.add(entityDataOld);
//			}
//		});
//
//		// =======================資料儲存=======================
//		// 資料Data
//		inventoryDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<WarehouseInventory> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM warehouse_inventory e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("wi", "wi_");
			cellName = cellName.replace("wi_gnowqty", "wi_g_now_qty");
			cellName = cellName.replace("wi_nowqty", "wi_now_qty");
			cellName = cellName.replace("wi_nqty", "wi_n_qty");
			cellName = cellName.replace("wi_rqty", "wi_r_qty");
			cellName = cellName.replace("wi_tqty", "wi_t_qty");
			cellName = cellName.replace("wi_waalias", "wi_wa_alias");
			cellName = cellName.replace("wi_waslocation", "wi_wa_s_location");
			cellName = cellName.replace("wi_wmname", "wi_wm_name");
			cellName = cellName.replace("wi_wmpnb", "wi_wm_p_nb");
			cellName = cellName.replace("wi_waaliasnb", "wi_wa_alias_nb");
			cellName = cellName.replace("wi_waaliasname", "wi_wa_alias_name");

			String where = x.getAsString().split("<_>")[1];
			String value = x.getAsString().split("<_>")[2];// 有可能空白
			String valueType = x.getAsString().split("<_>")[3];

			switch (where) {
			case "AllSame":
				nativeQuery += "(e." + cellName + " = :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			case "NotSame":
				nativeQuery += "(e." + cellName + " != :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			case "Like":
				nativeQuery += "(e." + cellName + " LIKE :" + cellName + ") AND ";
				sqlQuery.put(cellName, "%" + value + "%<_>" + valueType);
				break;
			case "NotLike":
				nativeQuery += "(e." + cellName + "NOT LIKE :" + cellName + ") AND ";
				sqlQuery.put(cellName, "%" + value + "%<_>" + valueType);
				break;
			case "MoreThan":
				nativeQuery += "(e." + cellName + " >= :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			case "LessThan":
				nativeQuery += "(e." + cellName + " <= :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			}
		}

		nativeQuery = StringUtils.removeEnd(nativeQuery, "AND ");
		nativeQuery += " order by e.wi_wm_p_nb asc,e.wi_wa_s_location asc";
		nativeQuery += " LIMIT 10000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, WarehouseInventory.class);
		// =======================查詢參數=======================
		sqlQuery.forEach((key, valAndType) -> {
			String val = valAndType.split("<_>")[0];
			String tp = valAndType.split("<_>")[1];
			if (tp.equals("dateTime")) {
				// 時間格式?
				query.setParameter(key, Fm_T.toDate(val));
			} else if (tp.equals("number")) {
				// 數字?
				query.setParameter(key, Integer.parseInt(val));
			} else {
				// 文字?
				query.setParameter(key, val);
			}
		});
		try {
			entitys = query.getResultList();
		} catch (PersistenceException e) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, Lan.zh_TW, null);
		}

		// 資料包裝
		String entityJsonDatas = packageService.beanToJson(entitys);
		packageBean.setEntityJson(entityJsonDatas);

		return packageBean;
	}
}
