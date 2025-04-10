package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseHistoryDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.ManufactureActionDetailFront;
import dtri.com.tw.pgsql.entity.ManufactureActionFront;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseHistory;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class ManufactureActionServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private WarehouseAreaDao areaDao;

	@Autowired
	private BasicShippingListDao shippingListDao;

	@Autowired
	private WarehouseTypeFilterDao filterDao;

	@Autowired
	private WarehouseHistoryDao historyDao;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = 9999;
		int batch = 0;

		// Step2.排序

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.DESC, "bslstatus"));// 急迫性(越大越急)
		shOrders.add(new Order(Direction.ASC, "bsledate"));// 時間
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslfromwho"));// 供應來源
		shOrders.add(new Order(Direction.ASC, "sysnote"));// 備註
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號

		// 一般模式
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<ManufactureActionFront> entitys = new ArrayList<ManufactureActionFront>();
		ArrayList<ManufactureActionDetailFront> entityDetails = new ArrayList<ManufactureActionDetailFront>();
		Map<String, Integer> entityChecks = new HashMap<>();
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

			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllByManufactureSearchAction(null, null,
					null, null, null, shPageable);

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn() + "-領料類";
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb() + "-領料類";

				ManufactureActionDetailFront e = new ManufactureActionDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setMasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setMasclasssn(headerKey);// 單據+單據號
				e.setMasnb(sh.getBslnb());// 序號
				e.setMastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setMassmuser(sh.getBslsmuser());// : 產線配料人
				e.setMasfuser(sh.getBslfuser());// 完成人
				e.setMaspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setMaspname(sh.getBslpname());// : 品名<br>
				e.setMaspnqty(sh.getBslpnqty());// : 數量<br>
				e.setMaspngqty(sh.getBslpngqty());// : 已數量<br>
				e.setMasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				e.setMasedate(sh.getBsledate());// 預計領料/預計入料
				e.setMastocommand(sh.getBsltocommand());// 指令(對象)
				e.setMasfromcommand(sh.getBslfromcommand());// 指令(來源)
				e.setMastowho(sh.getBsltowho());// 物件(對象)
				e.setMasfromwho(sh.getBslfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setMastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setMaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						e.setMasqcqty(0);// 待驗量
						e.setMasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());

				// 測試用
//				if (headerKey.equals("A541-231003009")) {
//					System.out.println(headerKey);
//				}

				// header
				if (entityChecks.size() < 100) {
					if (!entityChecks.containsKey(headerKey)) {
						entityChecks.put(headerKey, 0);
						// 限制大小50張單
						ManufactureActionFront eh = new ManufactureActionFront();
						eh.setId(Key);
						eh.setGid(headerKey);
						eh.setMasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
						eh.setMasclasssn(headerKey);// 單據+單據號
						eh.setMasnb(sh.getBslnb());// 序號
						eh.setMastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
						eh.setMasmuser(sh.getBslsmuser());// : 產線配料人<br>
						eh.setMasfuser(sh.getBslfuser());// 完成人
						eh.setMaspnumber(sh.getBslpnumber());// : 物料號<br>
						eh.setMaspname(sh.getBslpname());// : 品名<br>
						eh.setMasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						eh.setMasedate(sh.getBsledate());// 預計領料/預計入料
						eh.setMastocommand(sh.getBsltocommand());// 指令(對象)
						eh.setMasfromcommand(sh.getBslfromcommand());// 指令(來源)
						eh.setMastowho(sh.getBsltowho());// 物件(對象)
						eh.setMasfromwho(sh.getBslfromwho());// 物件(來源)
						//
						eh.setMasaliaswmpnb(e.getMasaliaswmpnb());// 倉儲_物料號
						//
						eh.setSyscdate(sh.getSyscdate());
						eh.setSyscuser(sh.getSyscuser());
						eh.setSysmdate(sh.getSysmdate());
						eh.setSysmuser(sh.getSysmuser());
						eh.setSysnote(sh.getSyshnote());
						entitys.add(eh);
					}
				}
				if (entityChecks.containsKey(headerKey)) {
					entityChecks.put(headerKey, entityChecks.get(headerKey) + 1);
					// body
					entityDetails.add(e);
				}
			});
			// 顯示數量
			entitys.forEach(enh -> {
				if (entityChecks.containsKey(enh.getGid())) {
					enh.setMasclassname(enh.getMasclassname() + "(" + entityChecks.get(enh.getGid()) + ")");
				}
			});

			// 類別(一般模式)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJsonDatas);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("ManufactureActionFront",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("ManufactureActionDetailFront", null, 2);
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});

			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = ManufactureActionFront.class.getDeclaredFields();
			Field[] fieldDteails = ManufactureActionDetailFront.class.getDeclaredFields();

			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			// 欄位翻譯(細節)
			resultDetailTJsons = packageService.resultSet(fieldDteails, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "masclasssn", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectArr = new JsonArray();
			selectArr.add("已點料_已點料");
			selectArr.add("未點料_未點料");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "mastype", "Ex:已點料狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			selectArr = new JsonArray();
			selectArr.add("含有(6F)_6F");
			selectArr.add("含有(4F)_4F");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "sysnote", "Ex:含有樓層?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ManufactureActionFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ManufactureActionFront.class);
			String masclass = null;
			String massn = null;
			String massmuser = searchData.getMastype();
			// 單據查詢
			if (searchData.getMasclasssn() != null && searchData.getMasclasssn().split("-").length == 2) {
				masclass = searchData.getMasclasssn().split("-")[0];
				massn = searchData.getMasclasssn().split("-")[1];
			} else {
				masclass = searchData.getMasclasssn();
			}
			//
			ArrayList<BasicShippingList> shippingLists = new ArrayList<>();
			shippingLists = shippingListDao.findAllByManufactureSearchAction(masclass, massn, null, massmuser,
					searchData.getSysnote(), shPageable);

			// Step4-2.資料區分(一般/細節)
			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn() + "-領料類";
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb() + "-領料類";

				ManufactureActionDetailFront e = new ManufactureActionDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setMasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setMasclasssn(headerKey);// 單據+單據號
				e.setMasnb(sh.getBslnb());// 序號
				e.setMastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setMassmuser(sh.getBslsmuser());// : 產線配料人<br>
				e.setMasfuser(sh.getBslfuser());// 完成人
				e.setMaspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setMaspname(sh.getBslpname());// : 品名<br>
				e.setMaspnqty(sh.getBslpnqty());// : 數量<br>
				e.setMaspngqty(sh.getBslpngqty());// : 已數量<br>
				e.setMasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				e.setMasedate(sh.getBsledate());// 預計領料/預計入料
				e.setMastocommand(sh.getBsltocommand());// 指令(對象)
				e.setMasfromcommand(sh.getBslfromcommand());// 指令(來源)
				e.setMastowho(sh.getBsltowho());// 物件(對象)
				e.setMasfromwho(sh.getBslfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setMastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setMaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						e.setMasqcqty(0);// 待驗量
						e.setMasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());
				if (entityChecks.size() < 100) {
					// header
					if (!entityChecks.containsKey(headerKey)) {
						entityChecks.put(headerKey, 0);
						// 限制大小50張單
						ManufactureActionFront eh = new ManufactureActionFront();
						eh.setId(Key);
						eh.setGid(headerKey);
						eh.setMasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
						eh.setMasclasssn(headerKey);// 單據+單據號
						eh.setMasnb(sh.getBslnb());// 序號
						eh.setMastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
						eh.setMasmuser(sh.getBslsmuser());// : 產線配料人<br>
						eh.setMasfuser(sh.getBslfuser());// 完成人
						eh.setMaspnumber(sh.getBslpnumber());// : 物料號<br>
						eh.setMaspname(sh.getBslpname());// : 品名<br>
						eh.setMasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						eh.setMasedate(sh.getBsledate());// 預計領料/預計入料
						eh.setMastocommand(sh.getBsltocommand());// 指令(對象)
						eh.setMasfromcommand(sh.getBslfromcommand());// 指令(來源)
						eh.setMastowho(sh.getBsltowho());// 物件(對象)
						eh.setMasfromwho(sh.getBslfromwho());// 物件(來源)
						//
						eh.setMasaliaswmpnb(e.getMasaliaswmpnb());// 倉儲_物料號
						//
						eh.setSyscdate(sh.getSyscdate());
						eh.setSyscuser(sh.getSyscuser());
						eh.setSysmdate(sh.getSysmdate());
						eh.setSysmuser(sh.getSysmuser());
						eh.setSysnote(sh.getSyshnote());
						entitys.add(eh);
					}
				}
				if (entityChecks.containsKey(headerKey)) {
					entityChecks.put(headerKey, entityChecks.get(headerKey) + 1);
					// body
					entityDetails.add(e);
				}
			});
			// 顯示數量
			entitys.forEach(enh -> {
				if (entityChecks.containsKey(enh.getGid())) {
					enh.setMasclassname(enh.getMasclassname() + "(" + entityChecks.get(enh.getGid()) + ")");
				}
			});
			// 類別(一般模式)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJsonDatas);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new ManufactureActionFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_masedate");
		return packageBean;
	}

	/** 取得資料(Detail) */
	public PackageBean getSearchDetail(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = 9999;
		int batch = 0;

		// Step2.排序
		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.DESC, "bslstatus"));// 急迫性(越大越急)
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslfromwho"));// 供應來源
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號

		// 一般模式
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<ManufactureActionFront> entitys = new ArrayList<ManufactureActionFront>();
		ArrayList<ManufactureActionDetailFront> entityDetails = new ArrayList<ManufactureActionDetailFront>();
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

		// Step4-1. 取得資料(一般/細節)
		ManufactureActionFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
				ManufactureActionFront.class);

		if (searchData.getMasclasssn() != null) {
			List<String> masclasssn = Arrays.asList(searchData.getMasclasssn().split("_"));
			masclasssn.forEach(r -> {
				if (r.split("-").length >= 2) {
					String masclass = r.split("-")[0];
					String massn = r.split("-")[1];

					ArrayList<BasicShippingList> shippingLists = shippingListDao
							.findAllByManufactureDetailSearchAction(masclass, massn, null, shPageable);
					// Step4-2.資料區分(一般/細節)

					// 領料
					shippingLists.forEach(sh -> {
						String headerKey = sh.getBslclass() + "-" + sh.getBslsn() + "-領料類";
						String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb() + "-領料類";

						ManufactureActionDetailFront e = new ManufactureActionDetailFront();
						e.setId(Key);
						e.setGid(headerKey);
						// 進料單
						e.setMasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
						e.setMasclasssn(headerKey);// 單據+單據號
						e.setMasnb(sh.getBslnb());// 序號
						e.setMastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
						e.setMassmuser(sh.getBslsmuser());// : 產線配料人<br>
						e.setMasfuser(sh.getBslfuser());// 完成人
						e.setMaspnumber(sh.getBslpnumber());// : 物料號<br>
						e.setMaspname(sh.getBslpname());// : 品名<br>
						e.setMaspnqty(sh.getBslpnqty());// : 數量<br>
						e.setMaspngqty(sh.getBslpngqty());// : 已數量<br>
						e.setMasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						e.setMasedate(sh.getBsledate());// 預計領料/預計入料
						e.setMastocommand(sh.getBsltocommand());// 指令(對象)
						e.setMasfromcommand(sh.getBslfromcommand());// 指令(來源)
						e.setMastowho(sh.getBsltowho());// 物件(對象)
						e.setMasfromwho(sh.getBslfromwho());// 物件(來源)
						// 倉儲(必須符合格式)
						if (sh.getBslfromwho().split("_").length > 1) {
							String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_"
									+ sh.getBslpnumber();
							if (areaMaps.containsKey(areaKey)) {
								e.setMastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
								e.setMaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
								e.setMasqcqty(0);// 待驗量
								e.setMasaliaswmpnb(areaKey);// 倉儲_物料號
							}
						}
						// System
						e.setSyscdate(sh.getSyscdate());
						e.setSyscuser(sh.getSyscuser());
						e.setSysmdate(sh.getSysmdate());
						e.setSysmuser(sh.getSysmuser());
						e.setSysnote(sh.getSysnote());
						// body
						entityDetails.add(e);
					});
				}
			});
		}
		// 類別(一般模式)
		// 資料包裝
		String entityJsonDatas = packageService.beanToJson(entitys);
		packageBean.setEntityJson(entityJsonDatas);
		String entityJsonDetails = packageService.beanToJson(entityDetails);
		packageBean.setEntityDetailJson(entityJsonDetails);

		// 查不到資料
		if (packageBean.getEntityDetailJson().equals("[]")) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new ManufactureActionFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_masedate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ManufactureActionDetailFront> entityDatas = new ArrayList<>();
		ArrayList<WarehouseHistory> entityHistories = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<ManufactureActionDetailFront>>() {
					});
			// Step2.資料檢查
			for (ManufactureActionDetailFront entityData : entityDatas) {
				// 檢查-名稱重複(沒資料 已經被登記過)

				ArrayList<BasicShippingList> checkShippingDatas = shippingListDao.findAllByCheckUser(//
						entityData.getMasclasssn().split("-")[0], //
						entityData.getMasclasssn().split("-")[1], //
						entityData.getMasnb());
				if (checkShippingDatas.size() == 0 && !entityData.getMastype().equals("入料類")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getMasclasssn() + "-" + entityData.getMasnb() });
				}

				if (areaDao.findAllByWaaliasawmpnb(entityData.getMasaliaswmpnb()).size() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getMasaliaswmpnb() + "-配對不上" });
				}
			}

			// Step2.資料檢查(PASS)
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BasicShippingList> shippingLists = new ArrayList<>();
		entityDatas.forEach(x -> {
			String masClass = x.getId().split("-")[0];
			String masSn = x.getId().split("-")[1];
			String masNb = x.getId().split("-")[2];
			String masType = x.getMastype();

			if (masType.equals("領料類")) {
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheckUser(masClass, masSn, masNb);
				// 有資料?
				if (arrayList.size() > 0) {
					BasicShippingList shippingList = arrayList.get(0);
					// 單據更新
					shippingList.setBslsmuser(x.getMassmuser());
					shippingList.setSysmuser(x.getMassmuser());
					shippingList.setSysmdate(new Date());
					shippingLists.add(shippingList);
					// 紀錄更新
					WarehouseHistory history = new WarehouseHistory();
					history.setWhtype("領料(製造點料)");
					history.setWhwmslocation(shippingList.getBslfromwho());
					history.setWhcontent(x.getMasfromcommand() + " " + // 製令單
							shippingList.getBslclass() + "-" + shippingList.getBslsn() + "-" + // 領料單
							shippingList.getBslnb() + "*" + shippingList.getBslpnqty());
					history.setWhwmpnb(shippingList.getBslpnumber());
					history.setWhfuser(shippingList.getBslfuser());
					history.setWheqty(x.getMaserptqty());
					history.setWhcqty(x.getMastqty());
					history.setWhpomqty("");
					history.setWhcheckin(shippingList.getBslcheckin() == 0 ? "未核單" : "已核單");
					entityHistories.add(history);
				}
			}
		});
		// =======================資料儲存=======================
		historyDao.saveAll(entityHistories);
		shippingListDao.saveAll(shippingLists);

		return packageBean;
	}
}
