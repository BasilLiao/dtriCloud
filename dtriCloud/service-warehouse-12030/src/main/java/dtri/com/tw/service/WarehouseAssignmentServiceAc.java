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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseHistoryDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseAssignment;
import dtri.com.tw.pgsql.entity.WarehouseAssignmentDetail;
import dtri.com.tw.pgsql.entity.WarehouseHistory;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class WarehouseAssignmentServiceAc {

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
		inOrders.add(new Order(Direction.ASC, "biledate"));// 預計時間

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號
		shOrders.add(new Order(Direction.ASC, "bsledate"));// 預計時間

		// 一般模式
		PageRequest inPageable = PageRequest.of(batch, total, Sort.by(inOrders));
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<WarehouseAssignment> entitys = new ArrayList<WarehouseAssignment>();
		ArrayList<WarehouseAssignmentDetail> entityDetails = new ArrayList<WarehouseAssignmentDetail>();
		Map<String, String> entityChecks = new HashMap<>();
		Map<String, Integer> entitySchedulTotail = new HashMap<>();
		Map<String, Integer> entitySchedulFinish = new HashMap<>();
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
			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchStatus(null, null, null, null,
					"false", 0, inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchStatus(null, null, null, null,
					"false", 0, shPageable);
			// 進料
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();
				WarehouseAssignmentDetail ed = new WarehouseAssignmentDetail();
				ed.setId(Key);
				ed.setGid(headerKey);
				// 進料單
				ed.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				ed.setWasclasssn(headerKey);// 單據+單據號
				ed.setWasnb(in.getBilnb());// 序號
				ed.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				ed.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
				ed.setWasfuser(in.getBilfuser());// 完成人
				ed.setWascuser(in.getBilcuser());// 核准人
				ed.setWasacceptance(in.getBilacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
				ed.setWaspnumber(in.getBilpnumber());// : 物料號<br>
				ed.setWaspname(in.getBilpname());// : 品名<br>
				ed.setWaspnqty(in.getBilpnqty());// : 數量<br>
				ed.setWaspngqty(in.getBilpngqty());// : (已)數量<br>
				ed.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				ed.setWaspalready(in.getBilpalready() == 0 ? "未打印" : "已打印");

				switch (in.getBilstatus()) {
				case 0:
					ed.setWasstatusname("預設(3天)");
					break;
				case 1:
					ed.setWasstatusname("手動標示急迫");
					break;
				case 2:
					ed.setWasstatusname("立即");
					break;
				case 3:
					ed.setWasstatusname("取消");
					break;
				case 4:
					ed.setWasstatusname("暫停");
					break;
				default:
					break;
				}
				ed.setWasedate(in.getBiledate());// 預計領料/預計入料
				ed.setWastocommand(in.getBiltocommand());// 指令(對象)
				ed.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
				ed.setWastowho(in.getBiltowho());// 物件(對象)
				ed.setWasfromwho(in.getBilfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						ed.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						ed.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						ed.setWasqcqty(0);// 待驗量
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				ed.setSyscdate(in.getSyscdate());
				ed.setSyscuser(in.getSyscuser());
				ed.setSysmdate(in.getSysmdate());
				ed.setSysmuser(in.getSysmuser());
				ed.setSysnote(in.getSysnote());
				ed.setSysstatus(in.getSysstatus());
				// header
				if (!entityChecks.containsKey(headerKey)) {
					WarehouseAssignment e = new WarehouseAssignment();
					entityChecks.put(headerKey, headerKey);
					entitySchedulTotail.put(headerKey, 0);
					entitySchedulFinish.put(headerKey, 0);
					//
					e.setId(Key);
					e.setGid(headerKey);
					// 進料單
					e.setWasclassname(ed.getWasclassname());// 單據名稱
					e.setWasclasssn(headerKey);// 單據+單據號
					e.setWasnb(in.getBilnb());// 序號
					e.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
					e.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
					e.setWasfuser(in.getBilfuser());// 完成人
					e.setWascuser(in.getBilcuser());// 核准人
					e.setWasacceptance(in.getBilacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
					e.setWaspnumber(in.getBilpnumber());// : 物料號<br>
					e.setWaspname(in.getBilpname());// : 品名<br>
					e.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
					//
					e.setWaspalready(in.getBilpalready() == 0 ? "未打印" : "已打印");
					e.setWasstatusname(ed.getWasstatusname());
					e.setWasedate(in.getBiledate());// 預計領料/預計入料
					e.setWastocommand(in.getBiltocommand());// 指令(對象)
					e.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
					e.setWastowho(in.getBiltowho());// 物件(對象)
					e.setWasfromwho(in.getBilfromwho());// 物件(來源)
					//
					e.setSyscdate(in.getSyscdate());
					e.setSyscuser(in.getSyscuser());
					e.setSysmdate(in.getSysmdate());
					e.setSysmuser(in.getSysmuser());
					e.setSysnote(in.getSysnote());
					e.setSyshnote(in.getSyshnote());// 表單頭 備註
					e.setWaserpcuser(in.getBilerpcuser());// 開單人
					e.setSysstatus(in.getSysstatus());

					entitys.add(e);
				}
				// body
				entityDetails.add(ed);
				// 進度判別
				entitySchedulTotail.put(headerKey, entitySchedulTotail.get(headerKey) + 1);
				if (!ed.getWasfuser().equals("")) {
					entitySchedulFinish.put(headerKey, entitySchedulFinish.get(headerKey) + 1);
				}
			});

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();
				WarehouseAssignmentDetail ed = new WarehouseAssignmentDetail();

				ed.setId(Key);
				ed.setGid(headerKey);
				// 進料單
				ed.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				ed.setWasclasssn(headerKey);// 單據+單據號
				ed.setWasnb(sh.getBslnb());// 序號
				ed.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				ed.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
				ed.setWasfuser(sh.getBslfuser());// 完成人
				ed.setWascuser(sh.getBslcuser());// 核准人
				ed.setWasacceptance(sh.getBslacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
				ed.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
				ed.setWaspname(sh.getBslpname());// : 品名<br>
				ed.setWaspnqty(sh.getBslpnqty());// : 數量<br>
				ed.setWaspngqty(sh.getBslpngqty());// : (已)數量<br>
				ed.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				ed.setWaspalready(sh.getBslpalready() == 0 ? "未打印" : "已打印");
				switch (sh.getBslstatus()) {
				case 0:
					ed.setWasstatusname("預設(3天)");
					break;
				case 1:
					ed.setWasstatusname("手動標示急迫");
					break;
				case 2:
					ed.setWasstatusname("立即");
					break;
				case 3:
					ed.setWasstatusname("取消");
					break;
				case 4:
					ed.setWasstatusname("暫停");
					break;
				default:
					break;
				}
				ed.setWasedate(sh.getBsledate());// 預計領料/預計入料
				ed.setWastocommand(sh.getBsltocommand());// 指令(對象)
				ed.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
				ed.setWastowho(sh.getBsltowho());// 物件(對象)
				ed.setWasfromwho(sh.getBslfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						ed.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						ed.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						ed.setWasqcqty(0);// 待驗量
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				ed.setSyscdate(sh.getSyscdate());
				ed.setSyscuser(sh.getSyscuser());
				ed.setSysmdate(sh.getSysmdate());
				ed.setSysmuser(sh.getSysmuser());
				ed.setSysnote(sh.getSysnote());
				ed.setSysstatus(sh.getSysstatus());
				// header
				if (!entityChecks.containsKey(headerKey)) {
					WarehouseAssignment e = new WarehouseAssignment();
					entityChecks.put(headerKey, headerKey);
					entitySchedulTotail.put(headerKey, 0);
					entitySchedulFinish.put(headerKey, 0);
					//
					e.setId(Key);
					e.setGid(headerKey);
					// 領料單
					e.setWasclassname(ed.getWasclassname());// 單據名稱
					e.setWasclasssn(headerKey);// 單據+單據號
					e.setWasnb(sh.getBslnb());// 序號
					e.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
					e.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
					e.setWasfuser(sh.getBslfuser());// 完成人
					e.setWascuser(sh.getBslcuser());// 核准人
					e.setWassmuser(sh.getBslsmuser());// 產線配置清點人員
					e.setWasacceptance(sh.getBslacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
					e.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
					e.setWaspname(sh.getBslpname());// : 品名<br>
					e.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
					//
					e.setWaspalready(sh.getBslpalready() == 0 ? "未打印" : "已打印");
					e.setWasstatusname(ed.getWasstatusname());
					e.setWasedate(sh.getBsledate());// 預計領料/預計入料
					e.setWastocommand(sh.getBsltocommand());// 指令(對象)
					e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
					e.setWastowho(sh.getBsltowho());// 物件(對象)
					e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
					//
					e.setSyscdate(sh.getSyscdate());
					e.setSyscuser(sh.getSyscuser());
					e.setSysmdate(sh.getSysmdate());
					e.setSysmuser(sh.getSysmuser());
					e.setSysnote(sh.getSysnote());
					e.setSyshnote(sh.getSyshnote());// 表單頭 備註
					e.setWaserpcuser(sh.getBslerpcuser());// 開單人
					e.setSysstatus(sh.getSysstatus());
					entitys.add(e);
				}
				// body
				entityDetails.add(ed);
				// 進度判別
				entitySchedulTotail.put(headerKey, entitySchedulTotail.get(headerKey) + 1);
				if (!ed.getWasfuser().equals("")) {
					entitySchedulFinish.put(headerKey, entitySchedulFinish.get(headerKey) + 1);
				}

			});
			// 進度添加
			entitys.forEach(h -> {
				if (entitySchedulTotail.containsKey(h.getWasclasssn())) {
					h.setWasschedule(entitySchedulFinish.get(h.getWasclasssn()) + " / "
							+ entitySchedulTotail.get(h.getWasclasssn()));
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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseAssignment", null,
					2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("WarehouseAssignmentDetail", null, 2);
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
			Field[] fields = WarehouseAssignment.class.getDeclaredFields();
			Field[] fieldDetails = WarehouseAssignmentDetail.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			// 欄位翻譯(細節)
			resultDetailTJsons = packageService.resultSet(fieldDetails, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			JsonArray selectArrStat = new JsonArray();
			selectArrStat.add("未核准_false");
			selectArrStat.add("已核准_true");
			searchJsons = packageService.searchSet(searchJsons, selectArrStat, "wascuser", "Ex:核准?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wasclasssn", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wasfromcommand", "Ex:指示來源?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectArr = new JsonArray();
			selectArr.add("領料類_領料類");
			selectArr.add("入料類_入料類");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "wastype", "Ex:單據類型?", true, //
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
			WarehouseAssignment searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					WarehouseAssignment.class);
			String wasclass = null;
			String wassn = null;
			if (searchData.getWasclasssn() != null && searchData.getWasclasssn().split("-").length == 2) {
				wasclass = searchData.getWasclasssn().split("-")[0];
				wassn = searchData.getWasclasssn().split("-")[1];
			} else {
				wasclass = searchData.getWasclasssn();
			}
			if (searchData.getWascuser() == null) {
				searchData.setWascuser("false");
			}
			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchStatus(wasclass, wassn,
					searchData.getWasfromcommand(), searchData.getWastype(), searchData.getWascuser(),
					searchData.getSysstatus(), inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchStatus(wasclass, wassn,
					searchData.getWasfromcommand(), searchData.getWastype(), searchData.getWascuser(),
					searchData.getSysstatus(), shPageable);
			// Step4-2.資料區分(一般/細節)
			// 進料
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();
				WarehouseAssignmentDetail ed = new WarehouseAssignmentDetail();
				ed.setId(Key);
				ed.setGid(headerKey);
				// 進料單
				ed.setWasclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				ed.setWasclasssn(headerKey);// 單據+單據號
				ed.setWasnb(in.getBilnb());// 序號
				ed.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				ed.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
				ed.setWasfuser(in.getBilfuser());// 完成人
				ed.setWascuser(in.getBilcuser());// 核准人
				ed.setWasacceptance(in.getBilacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
				ed.setWaspnumber(in.getBilpnumber());// : 物料號<br>
				ed.setWaspname(in.getBilpname());// : 品名<br>
				ed.setWaspnqty(in.getBilpnqty());// : 數量<br>
				ed.setWaspngqty(in.getBilpngqty());// : (已)數量<br>
				ed.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				ed.setWaspalready(in.getBilpalready() == 0 ? "未打印" : "已打印");

				switch (in.getBilstatus()) {
				case 0:
					ed.setWasstatusname("預設(3天)");
					break;
				case 1:
					ed.setWasstatusname("手動標示急迫");
					break;
				case 2:
					ed.setWasstatusname("立即");
					break;
				case 3:
					ed.setWasstatusname("取消");
					break;
				case 4:
					ed.setWasstatusname("暫停");
					break;
				default:
					break;
				}
				ed.setWasedate(in.getBiledate());// 預計領料/預計入料
				ed.setWastocommand(in.getBiltocommand());// 指令(對象)
				ed.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
				ed.setWastowho(in.getBiltowho());// 物件(對象)
				ed.setWasfromwho(in.getBilfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						ed.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						ed.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						ed.setWasqcqty(0);// 待驗量
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				ed.setSyscdate(in.getSyscdate());
				ed.setSyscuser(in.getSyscuser());
				ed.setSysmdate(in.getSysmdate());
				ed.setSysmuser(in.getSysmuser());
				ed.setSysnote(in.getSysnote());
				ed.setSysstatus(in.getSysstatus());
				// header
				if (!entityChecks.containsKey(headerKey)) {
					WarehouseAssignment e = new WarehouseAssignment();
					entityChecks.put(headerKey, headerKey);
					entitySchedulTotail.put(headerKey, 0);
					entitySchedulFinish.put(headerKey, 0);
					//
					e.setId(Key);
					e.setGid(headerKey);
					// 進料單
					e.setWasclassname(ed.getWasclassname());// 單據名稱
					e.setWasclasssn(headerKey);// 單據+單據號
					e.setWasnb(in.getBilnb());// 序號
					e.setWastype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
					e.setWasmuser(in.getBilmuser());// : 可分配-負責人<br>
					e.setWasfuser(in.getBilfuser());// 完成人
					e.setWascuser(in.getBilcuser());// 核准人
					e.setWasacceptance(in.getBilacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
					e.setWaspnumber(in.getBilpnumber());// : 物料號<br>
					e.setWaspname(in.getBilpname());// : 品名<br>
					e.setWasstatus(in.getBilstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
					//
					e.setWaspalready(in.getBilpalready() == 0 ? "未打印" : "已打印");
					e.setWasstatusname(ed.getWasstatusname());
					e.setWasedate(in.getBiledate());// 預計領料/預計入料
					e.setWastocommand(in.getBiltocommand());// 指令(對象)
					e.setWasfromcommand(in.getBilfromcommand());// 指令(來源)
					e.setWastowho(in.getBiltowho());// 物件(對象)
					e.setWasfromwho(in.getBilfromwho());// 物件(來源)
					//
					e.setSyscdate(in.getSyscdate());
					e.setSyscuser(in.getSyscuser());
					e.setSysmdate(in.getSysmdate());
					e.setSysmuser(in.getSysmuser());
					e.setSysnote(in.getSysnote());
					e.setSyshnote(in.getSyshnote());// 表單頭 備註
					e.setWaserpcuser(in.getBilerpcuser());// 開單人
					e.setSysstatus(in.getSysstatus());

					entitys.add(e);
				}
				// body
				entityDetails.add(ed);
				// 進度判別
				entitySchedulTotail.put(headerKey, entitySchedulTotail.get(headerKey) + 1);
				if (!ed.getWasfuser().equals("")) {
					entitySchedulFinish.put(headerKey, entitySchedulFinish.get(headerKey) + 1);
				}
			});

			// 領料
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();
				WarehouseAssignmentDetail ed = new WarehouseAssignmentDetail();

				ed.setId(Key);
				ed.setGid(headerKey);
				// 進料單
				ed.setWasclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				ed.setWasclasssn(headerKey);// 單據+單據號
				ed.setWasnb(sh.getBslnb());// 序號
				ed.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				ed.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
				ed.setWasfuser(sh.getBslfuser());// 完成人
				ed.setWascuser(sh.getBslcuser());// 核准人

				ed.setWasacceptance(sh.getBslacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
				ed.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
				ed.setWaspname(sh.getBslpname());// : 品名<br>
				ed.setWaspnqty(sh.getBslpnqty());// : 數量<br>
				ed.setWaspngqty(sh.getBslpngqty());// : (已)數量<br>
				ed.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
				ed.setWaspalready(sh.getBslpalready() == 0 ? "未打印" : "已打印");
				switch (sh.getBslstatus()) {
				case 0:
					ed.setWasstatusname("預設(3天)");
					break;
				case 1:
					ed.setWasstatusname("手動標示急迫");
					break;
				case 2:
					ed.setWasstatusname("立即");
					break;
				case 3:
					ed.setWasstatusname("取消");
					break;
				case 4:
					ed.setWasstatusname("暫停");
					break;
				default:
					break;
				}
				ed.setWasedate(sh.getBsledate());// 預計領料/預計入料
				ed.setWastocommand(sh.getBsltocommand());// 指令(對象)
				ed.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
				ed.setWastowho(sh.getBsltowho());// 物件(對象)
				ed.setWasfromwho(sh.getBslfromwho());// 物件(來源)
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						ed.setWastqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						ed.setWaserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
						ed.setWasqcqty(0);// 待驗量
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
					}
				}
				// System
				ed.setSyscdate(sh.getSyscdate());
				ed.setSyscuser(sh.getSyscuser());
				ed.setSysmdate(sh.getSysmdate());
				ed.setSysmuser(sh.getSysmuser());
				ed.setSysnote(sh.getSysnote());
				ed.setSysstatus(sh.getSysstatus());
				// header
				if (!entityChecks.containsKey(headerKey)) {
					WarehouseAssignment e = new WarehouseAssignment();
					entityChecks.put(headerKey, headerKey);
					entitySchedulTotail.put(headerKey, 0);
					entitySchedulFinish.put(headerKey, 0);
					//
					e.setId(Key);
					e.setGid(headerKey);
					// 領料單
					e.setWasclassname(ed.getWasclassname());// 單據名稱
					e.setWasclasssn(headerKey);// 單據+單據號
					e.setWasnb(sh.getBslnb());// 序號
					e.setWastype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
					e.setWasmuser(sh.getBslmuser());// : 可分配-負責人<br>
					e.setWasfuser(sh.getBslfuser());// 完成人
					e.setWascuser(sh.getBslcuser());// 核准人
					e.setWassmuser(sh.getBslsmuser());// 產線配置清點人員
					e.setWasacceptance(sh.getBslacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
					e.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
					e.setWaspname(sh.getBslpname());// : 品名<br>
					e.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
					//
					e.setWaspalready(sh.getBslpalready() == 0 ? "未打印" : "已打印");
					e.setWasstatusname(ed.getWasstatusname());
					e.setWasedate(sh.getBsledate());// 預計領料/預計入料
					e.setWastocommand(sh.getBsltocommand());// 指令(對象)
					e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
					e.setWastowho(sh.getBsltowho());// 物件(對象)
					e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
					//
					e.setSyscdate(sh.getSyscdate());
					e.setSyscuser(sh.getSyscuser());
					e.setSysmdate(sh.getSysmdate());
					e.setSysmuser(sh.getSysmuser());
					e.setSysnote(sh.getSysnote());
					e.setSyshnote(sh.getSyshnote());// 表單頭 備註
					e.setWaserpcuser(sh.getBslerpcuser());// 開單人
					e.setSysstatus(sh.getSysstatus());
					entitys.add(e);
				}
				// body
				entityDetails.add(ed);
				// 進度判別
				entitySchedulTotail.put(headerKey, entitySchedulTotail.get(headerKey) + 1);
				if (!ed.getWasfuser().equals("")) {
					entitySchedulFinish.put(headerKey, entitySchedulFinish.get(headerKey) + 1);
				}
			});
			// 進度添加
			entitys.forEach(h -> {
				if (entitySchedulTotail.containsKey(h.getWasclasssn())) {
					h.setWasschedule(entitySchedulFinish.get(h.getWasclasssn()) + " / "
							+ entitySchedulTotail.get(h.getWasclasssn()));
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
		String entityFormatJson = packageService.beanToJson(new WarehouseAssignment());
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
		ArrayList<WarehouseHistory> entityHistories = new ArrayList<>();
		ArrayList<WarehouseAssignment> entityDatas = new ArrayList<>();
		ArrayList<WarehouseAssignmentDetail> entityDetailDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			if (action.equals("ReturnSelect")) {
				entityDetailDatas = packageService.jsonToBean(packageBean.getEntityJson(),
						new TypeReference<ArrayList<WarehouseAssignmentDetail>>() {
						});
			} else {
				entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
						new TypeReference<ArrayList<WarehouseAssignment>>() {
						});
			}

			// Step2.資料檢查(PASS)
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		String snNew = System.currentTimeMillis() + "";
		entityDatas.forEach(x -> {
			String wasClass = x.getWasclasssn().split("-")[0];
			String wasSn = x.getWasclasssn().split("-")[1];
			// String wasNb = x.getWasnb();
			String wasType = x.getWastype();
			if (wasType.equals("入料類")) {
				ArrayList<BasicIncomingList> arrayList = incomingListDao.findAllByCheck(wasClass, wasSn, null);
				ArrayList<BasicShippingList> arrayListNew = new ArrayList<>();
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						//
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "Agree":
							t.setBilcuser(x.getWascuser());
							break;
						case "Print":
							t.setBilpalready(1);
							break;
						case "PassAll":
							if (t.getBilcuser().equals("")) {
								t.setBilcuser(x.getWascuser());
							}
							if (t.getBilfuser().equals("")) {
								t.setBilfuser(x.getWasfuser());
							}
							t.setBilpngqty(t.getBilpnqty());
							if (t.getBiltowho().split("_").length > 1) {
								String areaKey = t.getBiltowho().split("_")[0].replace("[", "") + "_"
										+ t.getBilpnumber();
								ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
								// 倉庫更新數量
								if (areas.size() > 0) {
									int qty = areas.get(0).getWatqty();
									areas.get(0).setWatqty(qty + t.getBilpnqty());
									areaDao.save(areas.get(0));
								}
							}
							break;
						case "Urgency":
							t.setBilstatus(x.getWasstatus());
							break;
						default:
							break;
						}
						// 記錄用
						WarehouseHistory history = new WarehouseHistory();
						history.setWhtype("指令:入料:(" + action + ")");
						history.setWhwmslocation(t.getBiltowho());
						history.setWhcontent(t.getBilclass() + "-" + //
								t.getBilsn() + "-" + //
								t.getBilnb() + "*" + t.getBilpnqty());
						history.setWhwmpnb(t.getBilpnumber());
						history.setWhfuser(t.getBilfuser());
						history.setWhcheckin(t.getBilcheckin() == 0 ? "未核單" : "已核單");
						entityHistories.add(history);
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				shippingListDao.saveAll(arrayListNew);
				incomingListDao.saveAll(arrayList);
			} else {
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheck(wasClass, wasSn, null);
				ArrayList<BasicIncomingList> arrayListNew = new ArrayList<>();
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						//
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "Agree":
							if (t.getBslcuser().equals("")) {
								t.setBslcuser(x.getWascuser());
							}
							break;
						case "Print":
							t.setBslpalready(1);
							break;
						case "PassAll":
							if (t.getBslcuser().equals("")) {
								t.setBslcuser(x.getWascuser());
							}
							if (t.getBslfuser().equals("")) {
								t.setBslfuser(x.getWasfuser());
							}
							t.setBslpngqty(t.getBslpnqty());
							if (t.getBslfromwho().split("_").length > 1) {
								String areaKey = t.getBslfromwho().split("_")[0].replace("[", "") + "_"
										+ t.getBslpnumber();
								ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
								// 倉庫更新數量
								if (areas.size() > 0) {
									int qty = areas.get(0).getWatqty();
									qty = qty - t.getBslpnqty() > 0 ? qty - t.getBslpnqty() : 0;
									areas.get(0).setWatqty(qty);
									areaDao.save(areas.get(0));
								}
							}
							break;
						case "Urgency":
							t.setBslstatus(x.getWasstatus());
							break;
						case "ManufacturePass":
							if (t.getBslsmuser().equals("")) {
								t.setBslsmuser(x.getWassmuser());
							}
							break;
						default:
							break;
						}
						// 記錄用
						WarehouseHistory history = new WarehouseHistory();
						history.setWhtype("指令:領料:(" + action + ")");
						history.setWhwmslocation(t.getBslfromwho());
						history.setWhcontent(t.getBslclass() + "-" + //
								t.getBslsn() + "-" + //
								t.getBslnb() + "*" + t.getBslpnqty());
						history.setWhwmpnb(t.getBslpnumber());
						history.setWhfuser(t.getBslfuser());
						history.setWhcheckin(t.getBslcheckin() == 0 ? "未核單" : "已核單");
						entityHistories.add(history);
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				incomingListDao.saveAll(arrayListNew);
				shippingListDao.saveAll(arrayList);
			}
		});
		// 細節
		entityDetailDatas.forEach(x -> {
			String wasClass = x.getWasclasssn().split("-")[0];
			String wasSn = x.getWasclasssn().split("-")[1];
			String wasNb = x.getWasnb();
			String wasType = x.getWastype();
			if (wasType.equals("入料類")) {
				ArrayList<BasicIncomingList> arrayList = incomingListDao.findAllByCheck(wasClass, wasSn, wasNb);
				ArrayList<BasicShippingList> arrayListNew = new ArrayList<>();

				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						//
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "ReturnSelect":
							if (t.getBilcuser().equals("")) {
								t.setBilcuser(packageBean.getUserAccount());
							}
							if (t.getBilfuser().equals("")) {
								t.setBilfuser(packageBean.getUserAccount());
							}

							// 歸還單?
							if (t.getBilpngqty() > 0) {
								BasicShippingList o = new BasicShippingList();
								o.setChecksum("");
								o.setBslclass("AAAA");// 入庫單[別]
								o.setBslsn(snNew);// 入庫單[號]
								o.setBslnb(t.getBilnb());// 序號
								o.setBsltype("領料類");// 入庫單
								o.setBslcheckin(1);// 0=未核單 1=已核單
								o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
								o.setBslpnumber(t.getBilpnumber());// 物料號品號
								o.setBslpname(t.getBilpname());// 品名
								o.setBslpspecification(t.getBilpspecification());// 規格
								o.setBslpnqty(t.getBilpngqty());// 需入庫量
								o.setBsledate(new Date());// 預計入料日(今天)
								o.setSysstatus(0);// 未完成
								o.setSysmdate(new Date());
								// 而外匹配 [單別]
								o.setBslfromcommand(
										"[" + t.getBilclass() + "-" + t.getBilsn() + "-" + t.getBilnb() + "]");// 製令單
								o.setBsltocommand("[_]");
								// 而外匹配 [倉別代號+倉別名稱+位置]
								o.setBsltowho(t.getBilfromwho());// 目的[_生產線]
								o.setBslfromwho(t.getBiltowho());// 目的來源[_倉庫]
								// 而外匹配 [儲位負責]
								o.setBslmuser(t.getBilmuser());
								arrayListNew.add(o);
							}
							break;
						default:
							break;
						}
						// 記錄用
						WarehouseHistory history = new WarehouseHistory();
						history.setWhtype("指令:入料:(" + action + ")");
						history.setWhwmslocation(t.getBiltowho());
						history.setWhcontent(t.getBilclass() + "-" + //
								t.getBilsn() + "-" + //
								t.getBilnb() + "*" + t.getBilpnqty());
						history.setWhwmpnb(t.getBilpnumber());
						history.setWhfuser(t.getBilfuser());
						history.setWhcheckin(t.getBilcheckin() == 0 ? "未核單" : "已核單");
						entityHistories.add(history);
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				shippingListDao.saveAll(arrayListNew);
				incomingListDao.saveAll(arrayList);
			} else {
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheck(wasClass, wasSn, wasNb);
				ArrayList<BasicIncomingList> arrayListNew = new ArrayList<>();
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						// 記錄用
						//
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "ReturnSelect":
							if (t.getBslcuser().equals("")) {
								t.setBslcuser(packageBean.getUserAccount());
							}
							if (t.getBslfuser().equals("")) {
								t.setBslfuser(packageBean.getUserAccount());
							}
							// 歸還單?
							if (t.getBslpngqty() > 0) {
								BasicIncomingList o = new BasicIncomingList();
								o.setChecksum("");
								o.setBilclass("AAAA");// 入庫單[別]
								o.setBilsn(snNew);// 入庫單[號]
								o.setBilnb(t.getBslnb());// 序號
								o.setBiltype("入料類");// 入庫單
								o.setBilcheckin(1);// 0=未核單 1=已核單
								o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
								o.setBilpnumber(t.getBslpnumber());// 物料號品號
								o.setBilpname(t.getBslpname());// 品名
								o.setBilpspecification(t.getBslpspecification());// 規格
								o.setBilpnqty(t.getBslpngqty());// 需入庫量
								o.setBiledate(new Date());// 預計入料日(今天)
								o.setSysstatus(0);// 未完成
								o.setSysmdate(new Date());
								// 而外匹配 [單別]
								o.setBilfromcommand(
										"[" + t.getBslclass() + "-" + t.getBslsn() + "-" + t.getBslnb() + "]");// 製令單
								o.setBiltocommand("[_]");
								// 而外匹配 [倉別代號+倉別名稱+位置]
								o.setBiltowho(t.getBslfromwho());// 目的[_生產線]
								o.setBilfromwho(t.getBsltowho());// 目的來源[_倉庫]
								// 而外匹配 [儲位負責]
								o.setBilmuser(t.getBslmuser());
								arrayListNew.add(o);
							}
							break;
						default:
							break;
						}
						// 記錄用
						WarehouseHistory history = new WarehouseHistory();
						history.setWhtype("指令:領料:(" + action + ")");
						history.setWhwmslocation(t.getBslfromwho());
						history.setWhcontent(t.getBslclass() + "-" + //
								t.getBslsn() + "-" + //
								t.getBslnb() + "*" + t.getBslpnqty());
						history.setWhwmpnb(t.getBslpnumber());
						history.setWhfuser(t.getBslfuser());
						history.setWhcheckin(t.getBslcheckin() == 0 ? "未核單" : "已核單");
						entityHistories.add(history);
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				incomingListDao.saveAll(arrayListNew);
				shippingListDao.saveAll(arrayList);
			}
		});

		return packageBean;
	}
}
