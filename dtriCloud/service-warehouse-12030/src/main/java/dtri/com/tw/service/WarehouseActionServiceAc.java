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

import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.ScheduleShortageListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseHistoryDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.ScheduleShortageList;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseActionFront;
import dtri.com.tw.pgsql.entity.WarehouseActionDetailFront;
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
public class WarehouseActionServiceAc {

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

	@Autowired
	private WarehouseHistoryDao historyDao;

	@Autowired
	private ScheduleShortageListDao shortageListDao;

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
		inOrders.add(new Order(Direction.DESC, "bilstatus"));// 急迫性(越大越急)
		inOrders.add(new Order(Direction.ASC, "biledate"));// 時間
		inOrders.add(new Order(Direction.ASC, "bilclass"));// 單別
		inOrders.add(new Order(Direction.ASC, "bilsn"));// 單號
		inOrders.add(new Order(Direction.ASC, "biltowho"));// 供應對象
		inOrders.add(new Order(Direction.ASC, "bilnb"));// 流水號

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.DESC, "bslstatus"));// 急迫性(越大越急)
		shOrders.add(new Order(Direction.ASC, "bsledate"));// 時間
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslfromwho"));// 供應來源
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號

		// 一般模式
		PageRequest inPageable = PageRequest.of(batch, total, Sort.by(inOrders));
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<WarehouseActionFront> entitys = new ArrayList<WarehouseActionFront>();
		ArrayList<WarehouseActionDetailFront> entityDetails = new ArrayList<WarehouseActionDetailFront>();
		Map<String, String> entityChecks = new HashMap<>();
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
			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchAction(null, null, null,
					packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(), "", inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchAction(null, null, null,
					packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(), "", shPageable);

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn() + "-領料類";
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb() + "-領料類";

				WarehouseActionDetailFront e = new WarehouseActionDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setWasclasssn(headerKey);// 單據+單據號
				e.setWasnb(sh.getBslnb());// 序號
				e.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
				e.setWasfuser(sh.getBslfuser());// 完成人
				e.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setWaspname(sh.getBslpname());// : 品名<br>
				e.setWaspnqty(sh.getBslpnqty());// : 數量<br>
				e.setWaspngqty(sh.getBslpngqty());// : 已數量<br>
				e.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				e.setWasedate(sh.getBsledate());// 預計領料/預計入料
				e.setWastocommand(sh.getBsltocommand());// 指令(對象)
				e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
				e.setWastowho(sh.getBsltowho());// 物件(對象)
				e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						e.setWasqcqty(0);// 待驗量
						e.setWasaliaswmpnb(areaKey);// 倉儲_物料號
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
				if (entityChecks.size() < 50) {
					if (!entityChecks.containsKey(headerKey)) {
						entityChecks.put(headerKey, headerKey);
						// 限制大小50張單
						WarehouseActionFront eh = new WarehouseActionFront();
						eh.setId(Key);
						eh.setGid(headerKey);
						eh.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
						eh.setWasclasssn(headerKey);// 單據+單據號
						eh.setWasnb(sh.getBslnb());// 序號
						eh.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
						eh.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
						eh.setWasfuser(sh.getBslfuser());// 完成人
						eh.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
						eh.setWaspname(sh.getBslpname());// : 品名<br>
						eh.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						eh.setWasedate(sh.getBsledate());// 預計領料/預計入料
						eh.setWastocommand(sh.getBsltocommand());// 指令(對象)
						eh.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
						eh.setWastowho(sh.getBsltowho());// 物件(對象)
						eh.setWasfromwho(sh.getBslfromwho());// 物件(來源)
						//
						eh.setWasaliaswmpnb(e.getWasaliaswmpnb());// 倉儲_物料號
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
					// body
					entityDetails.add(e);
				}
			});

			// 進料
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn() + "-入料類";
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb() + "-入料類";
				WarehouseActionDetailFront e = new WarehouseActionDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				e.setWasclasssn(headerKey);// 單據+單據號
				e.setWasnb(in.getBilnb());// 序號
				e.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				e.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
				e.setWasfuser(in.getBilfuser());// 完成人
				e.setWaspnumber(in.getBilpnumber());// : 物料號<br>
				e.setWaspname(in.getBilpname());// : 品名<br>
				e.setWaspnqty(in.getBilpnqty());// : 數量<br>
				e.setWaspngqty(in.getBilpngqty());// : 已數量<br>
				e.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				e.setWasedate(in.getBiledate());// 預計領料/預計入料
				e.setWastocommand(in.getBiltocommand());// 指令(對象)
				e.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
				e.setWastowho(in.getBiltowho());// 物件(對象)
				e.setWasfromwho(in.getBilfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						e.setWasqcqty(0);// 待驗量
						e.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				e.setSyscdate(in.getSyscdate());
				e.setSyscuser(in.getSyscuser());
				e.setSysmdate(in.getSysmdate());
				e.setSysmuser(in.getSysmuser());
				e.setSysnote(in.getSysnote());
				// 限制大小50張單
				if (entityChecks.size() < 100) {
					// header
					if (!entityChecks.containsKey(headerKey)) {
						entityChecks.put(headerKey, headerKey);
						// 限制大小50張單
						WarehouseActionFront eh = new WarehouseActionFront();
						eh.setId(Key);
						eh.setGid(headerKey);
						eh.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
						eh.setWasclasssn(headerKey);// 單據+單據號
						eh.setWasnb(in.getBilnb());// 序號
						eh.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
						eh.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
						eh.setWasfuser(in.getBilfuser());// 完成人
						eh.setWaspnumber(in.getBilpnumber());// : 物料號<br>
						eh.setWaspname(in.getBilpname());// : 品名<br>
						eh.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						eh.setWasedate(in.getBiledate());// 預計領料/預計入料
						eh.setWastocommand(in.getBiltocommand());// 指令(對象)
						eh.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
						eh.setWastowho(in.getBiltowho());// 物件(對象)
						eh.setWasfromwho(in.getBilfromwho());// 物件(來源)
						//
						eh.setWasaliaswmpnb(e.getWasaliaswmpnb());// 倉儲_物料號
						//
						eh.setSyscdate(in.getSyscdate());
						eh.setSyscuser(in.getSyscuser());
						eh.setSysmdate(in.getSysmdate());
						eh.setSysmuser(in.getSysmuser());
						eh.setSysnote(in.getSyshnote());
						entitys.add(eh);
					}
				}
				if (entityChecks.containsKey(headerKey)) {
					// body
					entityDetails.add(e);
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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseActionFront",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("WarehouseActionDetailFront", null, 2);
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
			Field[] fields = WarehouseActionFront.class.getDeclaredFields();
			Field[] fieldDteails = WarehouseActionDetailFront.class.getDeclaredFields();

			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			// 欄位翻譯(細節)
			resultDetailTJsons = packageService.resultSet(fieldDteails, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wasclasssn", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectArr = new JsonArray();
			selectArr.add("領料類_領料類");
			selectArr.add("入料類_入料類");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "wastype", "Ex:單據類型?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseActionFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					WarehouseActionFront.class);
			String wasclass = null;
			String wassn = null;
			String wastype = searchData.getWastype();
			if (searchData.getWasclasssn() != null && searchData.getWasclasssn().split("-").length == 2) {
				wasclass = searchData.getWasclasssn().split("-")[0];
				wassn = searchData.getWasclasssn().split("-")[1];
			} else {
				wasclass = searchData.getWasclasssn();
			}
			ArrayList<BasicIncomingList> incomingLists = new ArrayList<>();
			ArrayList<BasicShippingList> shippingLists = new ArrayList<>();

			if (wastype != null && wastype.equals("領料類")) {
				shippingLists = shippingListDao.findAllBySearchAction(wasclass, wassn, null,
						packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(), "",
						shPageable);
			} else if (wastype != null && wastype.equals("入料類")) {
				incomingLists = incomingListDao.findAllBySearchAction(wasclass, wassn, null,
						packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(), "",
						inPageable);
			} else {
				incomingLists = incomingListDao.findAllBySearchAction(wasclass, wassn, null,
						packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(), "",
						inPageable);

				shippingLists = shippingListDao.findAllBySearchAction(wasclass, wassn, null,
						packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(), "",
						shPageable);
			}
			// Step4-2.資料區分(一般/細節)

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn() + "-領料類";
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb() + "-領料類";

				WarehouseActionDetailFront e = new WarehouseActionDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setWasclasssn(headerKey);// 單據+單據號
				e.setWasnb(sh.getBslnb());// 序號
				e.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
				e.setWasfuser(sh.getBslfuser());// 完成人
				e.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setWaspname(sh.getBslpname());// : 品名<br>
				e.setWaspnqty(sh.getBslpnqty());// : 數量<br>
				e.setWaspngqty(sh.getBslpngqty());// : 已數量<br>
				e.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				e.setWasedate(sh.getBsledate());// 預計領料/預計入料
				e.setWastocommand(sh.getBsltocommand());// 指令(對象)
				e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
				e.setWastowho(sh.getBsltowho());// 物件(對象)
				e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						e.setWasqcqty(0);// 待驗量
						e.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());
				if (entityChecks.size() < 50) {
					// header
					if (!entityChecks.containsKey(headerKey)) {
						entityChecks.put(headerKey, headerKey);
						// 限制大小50張單
						WarehouseActionFront eh = new WarehouseActionFront();
						eh.setId(Key);
						eh.setGid(headerKey);
						eh.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
						eh.setWasclasssn(headerKey);// 單據+單據號
						eh.setWasnb(sh.getBslnb());// 序號
						eh.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
						eh.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
						eh.setWasfuser(sh.getBslfuser());// 完成人
						eh.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
						eh.setWaspname(sh.getBslpname());// : 品名<br>
						eh.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						eh.setWasedate(sh.getBsledate());// 預計領料/預計入料
						eh.setWastocommand(sh.getBsltocommand());// 指令(對象)
						eh.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
						eh.setWastowho(sh.getBsltowho());// 物件(對象)
						eh.setWasfromwho(sh.getBslfromwho());// 物件(來源)
						//
						eh.setWasaliaswmpnb(e.getWasaliaswmpnb());// 倉儲_物料號
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
					// body
					entityDetails.add(e);
				}
			});
			// 進料
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn() + "-入料類";
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb() + "-入料類";

				WarehouseActionDetailFront e = new WarehouseActionDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				e.setWasclasssn(headerKey);// 單據+單據號
				e.setWasnb(in.getBilnb());// 序號
				e.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				e.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
				e.setWasfuser(in.getBilfuser());// 完成人
				e.setWaspnumber(in.getBilpnumber());// : 物料號<br>
				e.setWaspname(in.getBilpname());// : 品名<br>
				e.setWaspnqty(in.getBilpnqty());// : 數量<br>
				e.setWaspngqty(in.getBilpngqty());// : 已數量<br>
				e.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				e.setWasedate(in.getBiledate());// 預計領料/預計入料
				e.setWastocommand(in.getBiltocommand());// 指令(對象)
				e.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
				e.setWastowho(in.getBiltowho());// 物件(對象)
				e.setWasfromwho(in.getBilfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						e.setWasqcqty(0);// 待驗量
						e.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				e.setSyscdate(in.getSyscdate());
				e.setSyscuser(in.getSyscuser());
				e.setSysmdate(in.getSysmdate());
				e.setSysmuser(in.getSysmuser());
				e.setSysnote(in.getSysnote());
				if (entityChecks.size() < 100) {
					// header
					if (!entityChecks.containsKey(headerKey)) {
						entityChecks.put(headerKey, headerKey);
						// 限制大小50張單
						WarehouseActionFront eh = new WarehouseActionFront();
						eh.setId(Key);
						eh.setGid(headerKey);
						eh.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
						eh.setWasclasssn(headerKey);// 單據+單據號
						eh.setWasnb(in.getBilnb());// 序號
						eh.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
						eh.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
						eh.setWasfuser(in.getBilfuser());// 完成人
						eh.setWaspnumber(in.getBilpnumber());// : 物料號<br>
						eh.setWaspname(in.getBilpname());// : 品名<br>
						eh.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						eh.setWasedate(in.getBiledate());// 預計領料/預計入料
						eh.setWastocommand(in.getBiltocommand());// 指令(對象)
						eh.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
						eh.setWastowho(in.getBiltowho());// 物件(對象)
						eh.setWasfromwho(in.getBilfromwho());// 物件(來源)
						//
						eh.setWasaliaswmpnb(e.getWasaliaswmpnb());// 倉儲_物料號
						//
						eh.setSyscdate(in.getSyscdate());
						eh.setSyscuser(in.getSyscuser());
						eh.setSysmdate(in.getSysmdate());
						eh.setSysmuser(in.getSysmuser());
						eh.setSysnote(in.getSyshnote());
						entitys.add(eh);
					}
				}
				if (entityChecks.containsKey(headerKey)) {
					// body
					entityDetails.add(e);
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
		String entityFormatJson = packageService.beanToJson(new WarehouseActionFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_wasedate");
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
		List<Order> inOrders = new ArrayList<>();
		inOrders.add(new Order(Direction.DESC, "bilstatus"));// 急迫性(越大越急)
		inOrders.add(new Order(Direction.ASC, "bilclass"));// 單別
		inOrders.add(new Order(Direction.ASC, "bilsn"));// 單號
		inOrders.add(new Order(Direction.ASC, "biltowho"));// 供應對象
		inOrders.add(new Order(Direction.ASC, "bilnb"));// 流水號

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.DESC, "bslstatus"));// 急迫性(越大越急)
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslfromwho"));// 供應來源
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號

		// 一般模式
		PageRequest inPageable = PageRequest.of(batch, total, Sort.by(inOrders));
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<WarehouseActionFront> entitys = new ArrayList<WarehouseActionFront>();
		ArrayList<WarehouseActionDetailFront> entityDetails = new ArrayList<WarehouseActionDetailFront>();
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
		WarehouseActionFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
				WarehouseActionFront.class);

		if (searchData.getWasclasssn() != null) {
			List<String> wasclasssn = Arrays.asList(searchData.getWasclasssn().split("_"));
			wasclasssn.forEach(r -> {
				if (r.split("-").length >= 2) {
					String wasclass = r.split("-")[0];
					String wassn = r.split("-")[1];
					ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchAction(wasclass, wassn,
							null, packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(),
							null, inPageable);
					ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchAction(wasclass, wassn,
							null, packageBean.getUserAccount().equals("admin") ? null : packageBean.getUserAccount(),
							null, shPageable);
					// Step4-2.資料區分(一般/細節)
					// 進料
					incomingLists.forEach(in -> {
						String headerKey = in.getBilclass() + "-" + in.getBilsn() + "-入料類";
						String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb() + "-入料類";

						WarehouseActionDetailFront e = new WarehouseActionDetailFront();
						e.setId(Key);
						e.setGid(headerKey);
						// 進料單
						e.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
						e.setWasclasssn(headerKey);// 單據+單據號
						e.setWasnb(in.getBilnb());// 序號
						e.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
						e.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
						e.setWasfuser(in.getBilfuser());// 完成人
						e.setWaspnumber(in.getBilpnumber());// : 物料號<br>
						e.setWaspname(in.getBilpname());// : 品名<br>
						e.setWaspnqty(in.getBilpnqty());// : 數量<br>
						e.setWaspngqty(in.getBilpngqty());// : 已數量<br>
						e.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						e.setWasedate(in.getBiledate());// 預計領料/預計入料
						e.setWastocommand(in.getBiltocommand());// 指令(對象)
						e.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
						e.setWastowho(in.getBiltowho());// 物件(對象)
						e.setWasfromwho(in.getBilfromwho());// 物件(來源)
						// 倉儲(必須符合格式)
						if (in.getBiltowho().split("_").length > 1) {
							String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
							if (areaMaps.containsKey(areaKey)) {
								e.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
								e.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
								e.setWasqcqty(0);// 待驗量
								e.setWasaliaswmpnb(areaKey);// 倉儲_物料號
							}
						}
						// System
						e.setSyscdate(in.getSyscdate());
						e.setSyscuser(in.getSyscuser());
						e.setSysmdate(in.getSysmdate());
						e.setSysmuser(in.getSysmuser());
						e.setSysnote(in.getSysnote());
						// body
						entityDetails.add(e);
					});

					// 領料
					shippingLists.forEach(sh -> {
						String headerKey = sh.getBslclass() + "-" + sh.getBslsn() + "-領料類";
						String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb() + "-領料類";

						WarehouseActionDetailFront e = new WarehouseActionDetailFront();
						e.setId(Key);
						e.setGid(headerKey);
						// 進料單
						e.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
						e.setWasclasssn(headerKey);// 單據+單據號
						e.setWasnb(sh.getBslnb());// 序號
						e.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
						e.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
						e.setWasfuser(sh.getBslfuser());// 完成人
						e.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
						e.setWaspname(sh.getBslpname());// : 品名<br>
						e.setWaspnqty(sh.getBslpnqty());// : 數量<br>
						e.setWaspngqty(sh.getBslpngqty());// : 已數量<br>
						e.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
						e.setWasedate(sh.getBsledate());// 預計領料/預計入料
						e.setWastocommand(sh.getBsltocommand());// 指令(對象)
						e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
						e.setWastowho(sh.getBsltowho());// 物件(對象)
						e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
						// 倉儲(必須符合格式)
						if (sh.getBslfromwho().split("_").length > 1) {
							String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_"
									+ sh.getBslpnumber();
							if (areaMaps.containsKey(areaKey)) {
								e.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
								e.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
								e.setWasqcqty(0);// 待驗量
								e.setWasaliaswmpnb(areaKey);// 倉儲_物料號
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
		String entityFormatJson = packageService.beanToJson(new WarehouseActionFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_wasedate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseHistory> entityHistories = new ArrayList<>();
		ArrayList<WarehouseActionDetailFront> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<WarehouseActionDetailFront>>() {
					});
			// Step2.資料檢查
			for (WarehouseActionDetailFront entityData : entityDatas) {
				// 檢查-名稱重複(沒資料 已經被登記過)
				// 入料
				ArrayList<BasicIncomingList> checkIncomingDatas = incomingListDao.findAllByCheckUser(//
						entityData.getWasclasssn().split("-")[0], //
						entityData.getWasclasssn().split("-")[1], //
						entityData.getWasnb());
				if (checkIncomingDatas.size() == 0 && entityData.getWastype().equals("入料類")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getWasclasssn() + "-" + entityData.getWasnb() });
				}
				// 領料
				ArrayList<BasicShippingList> checkShippingDatas = shippingListDao.findAllByCheckUser(//
						entityData.getWasclasssn().split("-")[0], //
						entityData.getWasclasssn().split("-")[1], //
						entityData.getWasnb());
				if (checkShippingDatas.size() == 0 && entityData.getWastype().equals("領料類")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getWasclasssn() + "-" + entityData.getWasnb() });
				}
				// 檢查數量與備註 是否匹配
				if (checkShippingDatas.size() > 0) {

					// 庫存數
					List<WarehouseArea> areaLists = areaDao.findAllByWaaliasawmpnb(entityData.getWasaliaswmpnb());
					if (areaLists.size() > 0) {
						WarehouseArea area = areaLists.get(0);
						// 超領:只能備品轉(庫存量<實領量)
						Boolean checkOK = false;
						if (area.getWatqty() < entityData.getWaspngqty()
								&& entityData.getWaspnqty().equals(entityData.getWaspngqty())
								&& entityData.getSysnote().contains("備品轉")) {
							// 超領登記: 庫存數量 < 已取數量 &&需領數量 = 已領取數量 && 選:備品轉
							checkOK = true;
						} else if (area.getWatqty() >= entityData.getWaspngqty()
								&& entityData.getWaspnqty().equals(entityData.getWaspngqty())) {
							// 正常:庫存數量 >= 已取數量 && 需領數量 = 已領取數量
							checkOK = true;
						} else if (entityData.getWaspnqty() > entityData.getWaspngqty()
								&& entityData.getSysnote().contains("部分領料")) {
							// 假缺少:需領數量 > 已領數量 && 只能有(部分領料)
							checkOK = true;
						} else if (area.getWatqty() <= entityData.getWaspngqty()
								&& entityData.getWaspnqty() > entityData.getWaspngqty()//
								&& entityData.getSysnote().contains("庫存量不足")) {
							// 真缺少: 庫存數量 <= 已取數量 && 需領數量 > 已領取數量 && 只能有(庫存量不足)
							checkOK = true;
						}
						if (!checkOK) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
									new String[] { entityData.getWasclasssn() + "-" + entityData.getWasnb() });
						}
					}
				}

				if (areaDao.findAllByWaaliasawmpnb(entityData.getWasaliaswmpnb()).size() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getWasaliaswmpnb() + "-配對不上" });
				}
			}
			// Step2.資料檢查(PASS)
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BasicShippingList> shippingLists = new ArrayList<>();
		ArrayList<BasicIncomingList> incomingLists = new ArrayList<>();
		entityDatas.forEach(x -> {
			String wasClass = x.getId().split("-")[0];
			String wasSn = x.getId().split("-")[1];
			String wasNb = x.getId().split("-")[2];
			String wasType = x.getWastype();

			// 庫存數
			List<WarehouseArea> areaLists = areaDao.findAllByWaaliasawmpnb(x.getWasaliaswmpnb());
			WarehouseArea area = new WarehouseArea();
			area = areaLists.get(0);

			if (wasType.equals("入料類")) {
				ArrayList<BasicIncomingList> arrayList = incomingListDao.findAllByCheckUser(wasClass, wasSn, wasNb);
				// 有資料?
				if (arrayList.size() > 0) {
					BasicIncomingList incomingList = arrayList.get(0);
					// 如果 有同步數量/或已經撿了
					if (incomingList.getBilpngqty().equals(incomingList.getBilpnqty())) {
						System.out.println(incomingList.getBilpngqty() + ":" + incomingList.getBilpnqty());
						// 只登記人->不做數量修正
					} else {
						if (x.getWaspnqty() < x.getWaspngqty()) {
							// 來料多(須入量<實入量)
							incomingList.setBilpnoqty(x.getWaspngqty() - x.getWaspnqty());// 超入登記
							incomingList.setSysnote(incomingList.getSysnote() + "[異常:進貨料多]");

						} else if (x.getWaspnqty() > x.getWaspngqty() && x.getSysnote().indexOf("進貨料短少") < 0) {
							// 來料缺(須入量>實入量)+沒做標記
							incomingList.setSysnote(incomingList.getSysnote() + "[異常:進貨料短少]");
						}
						incomingList.setBilpngqty(x.getWaspngqty());

						// 庫存更新
						area.setWatqty(area.getWatqty() + x.getWaspngqty());
						areaDao.save(area);
					}
					// 單據更新
					incomingList.setBilfuser(x.getWasfuser());
					incomingList.setSysmuser(x.getWasfuser());
					incomingList.setSysnote(x.getSysnote());
					incomingList.setSysmdate(new Date());
					incomingLists.add(incomingList);

					// 紀錄更新
					WarehouseHistory history = new WarehouseHistory();
					history.setWhtype("入料");
					history.setWhwmslocation(incomingList.getBiltowho());
					history.setWhcontent(//
							incomingList.getBilclass() + "-" + incomingList.getBilsn() + "-" + //
									incomingList.getBilnb() + "*" + incomingList.getBilpnqty());
					history.setWhwmpnb(incomingList.getBilpnumber());
					history.setWhfuser(incomingList.getBilfuser());
					history.setWheqty(area.getWaerptqty());
					history.setWhcqty(area.getWatqty());
					history.setWhcheckin(incomingList.getBilcheckin() == 0 ? "未核單" : "已核單");
					entityHistories.add(history);
				}
			} else {
				// 領料類
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheckUser(wasClass, wasSn, wasNb);
				// 有資料?
				if (arrayList.size() > 0) {
					BasicShippingList shippingList = arrayList.get(0);
					if (area.getWatqty() < x.getWaspngqty() && x.getWaspnqty().equals(x.getWaspngqty())
							&& x.getSysnote().contains("備品轉")) {
						// 超領登記: 庫存數量 < 已取數量 &&需領數量 = 已領取數量 && 選:備品轉
						shippingList.setBslpnoqty(x.getWaspngqty() - area.getWatqty());
					} else if (area.getWatqty() >= x.getWaspngqty() && x.getWaspnqty().equals(x.getWaspngqty())) {
						// 正常:庫存數量 >= 已取數量 && 需領數量 = 已領取數量
					} else if (x.getWaspnqty() > x.getWaspngqty() && x.getSysnote().contains("部分領料")) {
						// 假缺少:需領數量 > 已領數量 && 只能有(部分領料)

					} else if (area.getWatqty() <= x.getWaspngqty() && x.getWaspnqty() > x.getWaspngqty()
							&& x.getSysnote().contains("庫存量不足")) {
						// 真缺少: 庫存數量 <= 已取數量 && 需領數量 > 已領取數量 && 只能有(部分領料/庫存量不足)
					}
					// 單據更新
					shippingList.setBslfuser(x.getWasfuser());
					shippingList.setSysmuser(x.getWasfuser());
					shippingList.setSysnote(x.getSysnote());
					shippingList.setSysmdate(new Date());
					shippingList.setBslpngqty(x.getWaspngqty());
					shippingLists.add(shippingList);

					// 庫存更新(夠扣?)
					if (area.getWatqty() - x.getWaspngqty() < 0) {
						area.setWatqty(0);
					} else {
						area.setWatqty(area.getWatqty() - x.getWaspngqty());
					}
					areaDao.save(area);

					// 紀錄更新
					WarehouseHistory history = new WarehouseHistory();
					history.setWhtype("領料");
					history.setWhwmslocation(shippingList.getBslfromwho());
					history.setWhcontent(x.getWasfromcommand() + " " + // 製令單
							shippingList.getBslclass() + "-" + shippingList.getBslsn() + "-" + // 領料單
							shippingList.getBslnb() + "*" + shippingList.getBslpnqty());
					history.setWhwmpnb(shippingList.getBslpnumber());
					history.setWhfuser(shippingList.getBslfuser());
					history.setWheqty(area.getWaerptqty());
					history.setWhcqty(area.getWatqty());
					history.setWhcheckin(shippingList.getBslcheckin() == 0 ? "未核單" : "已核單");
					entityHistories.add(history);
				}
			}

		});
		// =======================資料儲存=======================
		shippingListDao.saveAll(shippingLists);
		incomingListDao.saveAll(incomingLists);
		historyDao.saveAll(entityHistories);

		return packageBean;
	}

	// 檢查單號->缺料?
	@Transactional
	public void setModifyCheck(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		List<ScheduleShortageList> shortageLists = new ArrayList<>();
		ArrayList<WarehouseActionDetailFront> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<WarehouseActionDetailFront>>() {
					});
			// =======================資料整理=======================
			// Step3.一般資料->寫入
			Map<String, Boolean> wasClassSn = new HashMap<>();
			entityDatas.forEach(x -> {
				if (!wasClassSn.containsKey(x.getWasclasssn())) {
					// 只能找已經全部完成的
					ArrayList<BasicShippingList> checkSize = shippingListDao
							.findAllByCheckUser(x.getWasclasssn().split("-")[0], x.getWasclasssn().split("-")[1], null);
					if (checkSize.size() == 0 && x.getWastype().equals("領料類")) {
						wasClassSn.put(x.getWasclasssn(), true);
					}
				}
			});
			wasClassSn.forEach((x, y) -> {
				List<Order> shOrders = new ArrayList<>();
				shOrders.add(new Order(Direction.DESC, "bslstatus"));// 急迫性(越大越急)
				shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
				shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
				shOrders.add(new Order(Direction.ASC, "bslfromwho"));// 供應來源
				shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號
				PageRequest shPageable = PageRequest.of(0, 9999, Sort.by(shOrders));
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheckShortageList(x.split("-")[0],
						x.split("-")[1], null, shPageable);
				// 缺料登記(取量 小於 需求量)
				arrayList.forEach(z -> {
					ScheduleShortageList shortageList = new ScheduleShortageList();
					shortageList.setSslbslsnnb(z.getBslclass() + "-" + z.getBslsn() + "-" + z.getBslnb());
					shortageList.setSslfuser(z.getBslfuser());
					shortageList.setSslpnumber(z.getBslpnumber());
					shortageList.setSslpname(z.getBslpname());
					shortageList.setSslpngqty(z.getBslpngqty());
					shortageList.setSslpnqty(z.getBslpnqty());
					shortageList.setSslpnlqty(z.getBslpnqty() - z.getBslpngqty());
					shortageList.setSysnote(z.getSysnote());
					shortageList.setSyshnote(z.getSyshnote());// 單據備註
					shortageList.setSslfromcommand(z.getBslfromcommand());// 指令來源
					shortageList.setSslerpcuser(z.getBslerpcuser());// 開單人
					shortageLists.add(shortageList);
				});
			});
			// =======================資料儲存=======================
			shortageListDao.saveAll(shortageLists);
		}
	}
}
