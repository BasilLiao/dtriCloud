package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseSynchronize;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class WarehouseSynchronizeServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private WarehouseAreaDao areaDao;

	@Autowired
	private BasicIncomingListDao incomingListDao;

	@Autowired
	private BasicShippingListDao shippingListDao;

	@Autowired
	private WarehouseTypeFilterDao filterDao;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = 9999;
		int batch = 0;

		// Step2.排序
		List<Order> inOrders = new ArrayList<>();
		inOrders.add(new Order(Direction.ASC, "bilclass"));// 單別
		inOrders.add(new Order(Direction.ASC, "bilsn"));// 單號
		inOrders.add(new Order(Direction.ASC, "bilnb"));// 流水號

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號

		// 一般模式
		PageRequest inPageable = PageRequest.of(batch, total, Sort.by(inOrders));
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<WarehouseSynchronize> entitys = new ArrayList<WarehouseSynchronize>();
		//
		List<WarehouseArea> areaLists = areaDao.findAll();
		Map<String, WarehouseArea> areaMaps = new HashMap<>();
		//
		List<WarehouseTypeFilter> typeFilters = filterDao.findAll();
		Map<String, String> typeFilterMaps = new HashMap<>();
		// Step3-2.資料區分(一般/細節)
		areaLists.forEach(a -> {
			String key = a.getWaaliasawmpnb();// 倉儲+物料號
			areaMaps.put(key, a);
		});
		typeFilters.forEach(t -> {
			typeFilterMaps.put(t.getWtfcode(), t.getWtfname());
		});

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			//
			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchSynchronize(null, null, null, inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchSynchronize(null, null, null, shPageable);
			// 進料
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();

				WarehouseSynchronize e = new WarehouseSynchronize();
				e.setId(Key);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(in.getBilnb());// 序號
				e.setWsstype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(in.getBilfuser());// 完成人
				e.setWsscuser(in.getBilcuser());// 核准人
				e.setWsspnumber(in.getBilpnumber());// : 物料號<br>
				e.setWsspnqty(in.getBilpnqty());// : 數量<br>
				e.setWsspngqty(in.getBilpngqty());// 已取數量<br>
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(in.getSyscdate());
				e.setSyscuser(in.getSyscuser());
				e.setSysmdate(in.getSysmdate());
				e.setSysmuser(in.getSysmuser());
				e.setSysnote(in.getSysnote());
				e.setSysstatus(in.getSysstatus());
				// header
				entitys.add(e);
			});

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();

				WarehouseSynchronize e = new WarehouseSynchronize();
				e.setId(Key);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(sh.getBslnb());// 序號
				e.setWsstype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(sh.getBslfuser());// 完成人
				e.setWsscuser(sh.getBslcuser());// 核准人
				e.setWsspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setWsspnqty(sh.getBslpnqty());// : 數量<br>
				e.setWsspngqty(sh.getBslpngqty());// 已取數量<br>
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());
				e.setSysstatus(sh.getSysstatus());
				entitys.add(e);
			});

			// 類別(一般模式)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJsonDatas);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseSynchronize", null, 2);
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
			Field[] fields = WarehouseSynchronize.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wssclasssn", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectArr = new JsonArray();
			selectArr.add("領料類_領料類");
			selectArr.add("入料類_入料類");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "wsstype", "Ex:單據類型?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectStatusArr = new JsonArray();
			selectStatusArr.add("未結單_0");
			selectStatusArr.add("已結單_1");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sysstatus", "Ex:狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseSynchronize searchData = packageService.jsonToBean(packageBean.getEntityJson(), WarehouseSynchronize.class);
			String wasclass = null;
			String wassn = null;
			if (searchData.getWssclasssn() != null && searchData.getWssclasssn().split("-").length == 2) {
				wasclass = searchData.getWssclasssn().split("-")[0];
				wassn = searchData.getWssclasssn().split("-")[1];
			} else {
				wasclass = searchData.getWssclasssn();
			}

			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchSynchronize(wasclass, wassn, searchData.getWsstype(),
					inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchSynchronize(wasclass, wassn, searchData.getWsstype(),
					shPageable);
			// Step4-2.資料區分(一般/細節)
			// 進料
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();

				WarehouseSynchronize e = new WarehouseSynchronize();
				e.setId(Key);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(in.getBilnb());// 序號
				e.setWsstype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(in.getBilfuser());// 完成人
				e.setWsscuser(in.getBilcuser());// 核准人
				e.setWsspnumber(in.getBilpnumber());// : 物料號<br>
				e.setWsspnqty(in.getBilpnqty());// : 數量<br>
				e.setWsspngqty(in.getBilpngqty());// 已取數量<br>
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(in.getSyscdate());
				e.setSyscuser(in.getSyscuser());
				e.setSysmdate(in.getSysmdate());
				e.setSysmuser(in.getSysmuser());
				e.setSysnote(in.getSysnote());
				e.setSysstatus(in.getSysstatus());
				// header
				entitys.add(e);

			});

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();

				WarehouseSynchronize e = new WarehouseSynchronize();
				e.setId(Key);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(sh.getBslnb());// 序號
				e.setWsstype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(sh.getBslfuser());// 完成人
				e.setWsscuser(sh.getBslcuser());// 核准人
				e.setWsspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setWsspnqty(sh.getBslpnqty());// : 數量<br>
				e.setWsspngqty(sh.getBslpngqty());// 已取數量<br>
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量

					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());
				e.setSysstatus(sh.getSysstatus());
				// header
				entitys.add(e);
			});

			// 類別(一般模式)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJsonDatas);
			packageBean.setEntityDetailJson("{}");

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new WarehouseSynchronize());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_wasedate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean, String action) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllByStatus(0);
		List<WarehouseArea> areas = areaDao.findAll();

		// =======================資料檢查=======================

		// =======================資料整理=======================
		// Step3.一般資料->寫入
		if (action.equals("Item")) {
			incomingLists.forEach(x -> {
				x.setBilpnqty(x.getBilpngqty());
				x.setSysmuser(packageBean.getUserAccount());
				x.setSysmdate(new Date());
			});
			shippingLists.forEach(x -> {
				x.setBslpnqty(x.getBslpngqty());
				x.setSysmuser(packageBean.getUserAccount());
				x.setSysmdate(new Date());
			});
			incomingListDao.saveAll(incomingLists);
			shippingListDao.saveAll(shippingLists);
		}
		if (action.equals("Qty")) {
			areas.forEach(x -> {
				x.setWatqty(x.getWaerptqty());
				x.setSysmuser(packageBean.getUserAccount());
				x.setSysmdate(new Date());
			});
			areaDao.saveAll(areas);
		}
		if (action.equals("Remove")) {
			incomingListDao.deleteAll();
			shippingListDao.deleteAll();
		}

		return packageBean;
	}
}
