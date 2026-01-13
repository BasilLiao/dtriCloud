package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import dtri.com.tw.pgsql.entity.WarehouseAssignmentDetailFront;
import dtri.com.tw.pgsql.entity.WarehouseAssignmentFront;
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
		int total = 12500;
		int batch = 0;

		// Step2.排序
		List<Order> inOrders = new ArrayList<>();
		inOrders.add(new Order(Direction.ASC, "bilclass"));// 單別
		inOrders.add(new Order(Direction.DESC, "bilsn"));// 單號
		inOrders.add(new Order(Direction.ASC, "bilnb"));// 流水號
		inOrders.add(new Order(Direction.ASC, "biledate"));// 預計時間

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.DESC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號
		shOrders.add(new Order(Direction.ASC, "bsledate"));// 預計時間

		// 一般模式
		PageRequest inPageable = PageRequest.of(batch, total, Sort.by(inOrders));
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<WarehouseAssignmentFront> entitys = new ArrayList<WarehouseAssignmentFront>();
		ArrayList<WarehouseAssignmentDetailFront> entityDetails = new ArrayList<WarehouseAssignmentDetailFront>();
		Map<String, String> entityChecks = new HashMap<>();
		Map<String, Integer> entitySchedulTotail = new HashMap<>();
		Map<String, Integer> entitySchedulFinish = new HashMap<>();
		Map<String, Integer> entitySchedulCheckInFinish = new HashMap<>();
		List<WarehouseTypeFilter> typeFilters = filterDao.findAll();
		// Step3-2.資料區分(一般/細節)
		Map<String, String> typeFilterMaps = typeFilters.stream()
				.collect(Collectors.toMap(WarehouseTypeFilter::getWtfcode, WarehouseTypeFilter::getWtfname));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			//
			ArrayList<BasicIncomingList> incomingLists = new ArrayList<BasicIncomingList>();
			// 第一次載入不帶入 進料類 ->第一次A541
			/*
			 * ArrayList<BasicIncomingList> incomingLists =
			 * incomingListDao.findAllBySearchStatus(null, null, null, null, null, "false",
			 * 0, null, inPageable);
			 */
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchStatus("A541", null, null, null,
					"true", null, null, null, null, shPageable);
			LinkedHashSet<String> areaListShIn = new LinkedHashSet<String>();
			// 進料
			for (BasicIncomingList in : incomingLists) {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();
				WarehouseAssignmentDetailFront ed = new WarehouseAssignmentDetailFront();
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
				ed.setWasfromcustomer("");//客戶
				ed.setWasfucheckin(false);// 集結

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
					areaKey = areaKey.replaceAll(" ", "");
					areaListShIn.add(areaKey);
					ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
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
					WarehouseAssignmentFront e = new WarehouseAssignmentFront();
					entityChecks.put(headerKey, headerKey);
					entitySchedulTotail.put(headerKey, 0);
					entitySchedulFinish.put(headerKey, 0);
					entitySchedulCheckInFinish.put(headerKey, 0);
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
					e.setWasfromcustomer("");
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
					entitySchedulCheckInFinish.put(headerKey, entitySchedulFinish.get(headerKey) + 1);
				}
			}

			// 領料
			for (BasicShippingList sh : shippingLists) {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				if ((entityChecks.size() > 100 && entityChecks.containsKey(headerKey))
						|| (entityChecks.size() <= 100)) {
					String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();
					WarehouseAssignmentDetailFront ed = new WarehouseAssignmentDetailFront();

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
					ed.setWasfucheckin(sh.getBslfucheckin());// 集結
					ed.setWasfromcustomer(sh.getBslfromcustomer());//客戶
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
					String[] fromWhoParts = sh.getBslfromwho().split("_");
					if (fromWhoParts.length > 1) {
						String areaKey = (sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber());
						areaKey = areaKey.replaceAll(" ", "");
						areaListShIn.add(areaKey);
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
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
						WarehouseAssignmentFront e = new WarehouseAssignmentFront();
						entityChecks.put(headerKey, headerKey);
						entitySchedulTotail.put(headerKey, 0);
						entitySchedulFinish.put(headerKey, 0);
						entitySchedulCheckInFinish.put(headerKey, 0);
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
						e.setWassdate(sh.getBslsdate());// 預計出貨
						e.setWastocommand(sh.getBsltocommand());// 指令(對象)
						e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
						e.setWastowho(sh.getBsltowho());// 物件(對象)
						e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
						e.setWasfromcustomer(sh.getBslfromcustomer());//客戶
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
					if (sh.getBslfucheckin()) {// 集結進度
						entitySchedulCheckInFinish.put(headerKey, entitySchedulCheckInFinish.get(headerKey) + 1);
					}
				}
			}
			// 進度添加
			entitys.forEach(h -> {
				if (entitySchedulTotail.containsKey(h.getWasclasssn())) {
					h.setWasschedule(entitySchedulFinish.get(h.getWasclasssn()) + " / "
							+ entitySchedulTotail.get(h.getWasclasssn()));
					h.setWascischedule(entitySchedulCheckInFinish.get(h.getWasclasssn()) + " / "
							+ entitySchedulTotail.get(h.getWasclasssn()));
				}
			});
			// 配對倉儲物料狀況
			final int BATCH_SIZE = 1500;
			List<String> areaListShInArr = new ArrayList<>(areaListShIn);
			List<WarehouseArea> areaLists = new ArrayList<>();
			for (int i = 0; i < areaListShIn.size(); i += BATCH_SIZE) {
				// 取得當前批次的子清單
				List<String> batchShIns = areaListShInArr.subList(i, Math.min(i + BATCH_SIZE, areaListShIn.size()));
				// 執行查詢
				List<WarehouseArea> batchResult = areaDao.findAllByWaaliasawmpnb(new ArrayList<>(batchShIns));
				// 合併結果
				areaLists.addAll(batchResult);
			}
			Map<String, WarehouseArea> areaMaps = areaLists.stream()
					.collect(Collectors.toMap(WarehouseArea::getWaaliasawmpnb, Function.identity()));
			entityDetails.forEach(ed -> {
				if (areaMaps.containsKey(ed.getWasaliaswmpnb())) {
					ed.setWastqty(areaMaps.get(ed.getWasaliaswmpnb()).getWatqty());// 實際數量
					ed.setWaserptqty(areaMaps.get(ed.getWasaliaswmpnb()).getWaerptqty());// 帳務數量
					ed.setWasqcqty(0);// 待驗量
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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseAssignmentFront",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("WarehouseAssignmentDetailFront", null, 2);
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
			Field[] fields = WarehouseAssignmentFront.class.getDeclaredFields();
			Field[] fieldDetails = WarehouseAssignmentDetailFront.class.getDeclaredFields();
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
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			JsonArray selectWasfuserArr = new JsonArray();
			selectWasfuserArr.add("未完成_false");
			selectWasfuserArr.add("已完成_true");
			searchJsons = packageService.searchSet(searchJsons, selectWasfuserArr, "wasfuser", "Ex:完成人?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			selectWasfuserArr = new JsonArray();
			selectWasfuserArr.add("4F_4F");
			selectWasfuserArr.add("6F_6F");
			searchJsons = packageService.searchSet(searchJsons, selectWasfuserArr, "syshnote", "Ex:單據備註(樓層)?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			selectWasfuserArr = new JsonArray();
			selectWasfuserArr.add("未完成_false");
			selectWasfuserArr.add("已完成_true");
			searchJsons = packageService.searchSet(searchJsons, selectWasfuserArr, "wassmuser", "Ex:配料員?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);
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
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			JsonArray selectStatusArr = new JsonArray();
			selectStatusArr.add("未結單_0");
			selectStatusArr.add("已結單_1");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sysstatus", "Ex:狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseAssignmentFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					WarehouseAssignmentFront.class);
			// 單別_單號
			String wasclass = null;
			String wassn = null;
			if (searchData.getWasclasssn() != null && searchData.getWasclasssn().split("-").length == 2) {
				wasclass = searchData.getWasclasssn().split("-")[0];
				wassn = searchData.getWasclasssn().split("-")[1];
			} else {
				wasclass = searchData.getWasclasssn();
				if (wasclass == null) {
					wasclass = "A541";
				}
			}
			// 核准人?
			if (searchData.getWascuser() == null) {
				searchData.setWascuser("true");
			}

			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchStatus(wasclass, wassn,
					searchData.getWasfromcommand(), searchData.getWastype(), searchData.getWascuser(),
					searchData.getWasfuser(), searchData.getSysstatus(), searchData.getSyshnote(), inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchStatus(wasclass, wassn,
					searchData.getWasfromcommand(), searchData.getWastype(), searchData.getWascuser(),
					searchData.getWasfuser(), searchData.getWassmuser(), searchData.getSysstatus(),
					searchData.getSyshnote(), shPageable);
			// Step4-2.資料區分(一般/細節)
			LinkedHashSet<String> areaListShIn = new LinkedHashSet<String>();
			// 進料
			for (BasicIncomingList in : incomingLists) {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				if ((entityChecks.size() > 100 && entityChecks.containsKey(headerKey))
						|| (entityChecks.size() <= 100)) {
					String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();
					WarehouseAssignmentDetailFront ed = new WarehouseAssignmentDetailFront();
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
					ed.setWasfucheckin(false);// 集結
					ed.setWasfromcustomer("");//客戶
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
						areaKey = areaKey.replaceAll(" ", "");
						areaListShIn.add(areaKey);
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
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
						WarehouseAssignmentFront e = new WarehouseAssignmentFront();
						entityChecks.put(headerKey, headerKey);
						entitySchedulTotail.put(headerKey, 0);
						entitySchedulFinish.put(headerKey, 0);
						entitySchedulCheckInFinish.put(headerKey, 0);
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
						e.setWasfromcustomer("");//客戶
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
						entitySchedulCheckInFinish.put(headerKey, entitySchedulCheckInFinish.get(headerKey) + 1);
					}
				} else {
					break;
				}
			}

			// 領料

			for (BasicShippingList sh : shippingLists) {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				if ((entityChecks.size() > 100 && entityChecks.containsKey(headerKey))
						|| (entityChecks.size() <= 100)) {
					String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();
					WarehouseAssignmentDetailFront ed = new WarehouseAssignmentDetailFront();

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
					ed.setWassmuser(sh.getBslsmuser());// 產線人

					ed.setWasacceptance(sh.getBslacceptance() == 0 ? "未檢驗" : "已檢驗");// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
					ed.setWaspnumber(sh.getBslpnumber());// : 物料號<br>
					ed.setWaspname(sh.getBslpname());// : 品名<br>
					ed.setWaspnqty(sh.getBslpnqty());// : 數量<br>
					ed.setWaspngqty(sh.getBslpngqty());// : (已)數量<br>
					ed.setWasstatus(sh.getBslstatus());// 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
					ed.setWaspalready(sh.getBslpalready() == 0 ? "未打印" : "已打印");
					ed.setWasfucheckin(sh.getBslfucheckin());// 集結
					ed.setWasfromcustomer(sh.getBslfromcustomer());//客戶
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
						areaKey = areaKey.replaceAll(" ", "");
						areaListShIn.add(areaKey);
						ed.setWasaliaswmpnb(areaKey);// 倉儲_物料號
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
						WarehouseAssignmentFront e = new WarehouseAssignmentFront();
						entityChecks.put(headerKey, headerKey);
						entitySchedulTotail.put(headerKey, 0);
						entitySchedulFinish.put(headerKey, 0);
						entitySchedulCheckInFinish.put(headerKey, 0);
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
						e.setWassdate(sh.getBslsdate());// 預計出貨
						e.setWastocommand(sh.getBsltocommand());// 指令(對象)
						e.setWasfromcommand(sh.getBslfromcommand());// 指令(來源)
						e.setWastowho(sh.getBsltowho());// 物件(對象)
						e.setWasfromwho(sh.getBslfromwho());// 物件(來源)
						e.setWasfromcustomer(sh.getBslfromcustomer());//客戶
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
					if (sh.getBslfucheckin()) {// 集結進度
						entitySchedulCheckInFinish.put(headerKey, entitySchedulCheckInFinish.get(headerKey) + 1);
					}
				} else {
					break;
				}
			}
			// 進度添加
			entitys.forEach(h -> {
				if (entitySchedulTotail.containsKey(h.getWasclasssn())) {
					h.setWasschedule(entitySchedulFinish.get(h.getWasclasssn()) + " / "
							+ entitySchedulTotail.get(h.getWasclasssn()));
					h.setWascischedule(entitySchedulCheckInFinish.get(h.getWasclasssn()) + " / "
							+ entitySchedulTotail.get(h.getWasclasssn()));
				}
			});
			// 配對倉儲物料狀況
			final int BATCH_SIZE = 1500;
			List<String> areaListShInArr = new ArrayList<>(areaListShIn);
			List<WarehouseArea> areaLists = new ArrayList<>();
			for (int i = 0; i < areaListShIn.size(); i += BATCH_SIZE) {
				// 取得當前批次的子清單
				List<String> batchShIns = areaListShInArr.subList(i, Math.min(i + BATCH_SIZE, areaListShIn.size()));
				// 執行查詢
				List<WarehouseArea> batchResult = areaDao.findAllByWaaliasawmpnb(new ArrayList<>(batchShIns));
				// 合併結果
				areaLists.addAll(batchResult);
			}
			Map<String, WarehouseArea> areaMaps = areaLists.stream()
					.collect(Collectors.toMap(WarehouseArea::getWaaliasawmpnb, Function.identity()));
			entityDetails.forEach(ed -> {
				if (areaMaps.containsKey(ed.getWasaliaswmpnb())) {
					ed.setWastqty(areaMaps.get(ed.getWasaliaswmpnb()).getWatqty());// 實際數量
					ed.setWaserptqty(areaMaps.get(ed.getWasaliaswmpnb()).getWaerptqty());// 帳務數量
					ed.setWasqcqty(0);// 待驗量
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
		String entityFormatJson = packageService.beanToJson(new WarehouseAssignmentFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_wasedate_wassdate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean, String action) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseHistory> entityHistories = new ArrayList<>();
		ArrayList<WarehouseAssignmentFront> entityDatas = new ArrayList<>();
		ArrayList<WarehouseAssignmentDetailFront> entityDetailDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			if (action.equals("ReturnSelect") || action.equals("PassAll")) {
				entityDetailDatas = packageService.jsonToBean(packageBean.getEntityJson(),
						new TypeReference<ArrayList<WarehouseAssignmentDetailFront>>() {
						});
			} else {
				entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
						new TypeReference<ArrayList<WarehouseAssignmentFront>>() {
						});
			}

			// Step2.資料檢查(PASS)
			if (action.equals("PassAll")) {
				// Step2.資料檢查
				for (WarehouseAssignmentDetailFront entityData : entityDetailDatas) {
					// 檢查-數量充足(有資料)
					String wasClass = entityData.getWasclasssn().split("-")[0];
					String wasSn = entityData.getWasclasssn().split("-")[1];
					String wasnb = entityData.getWasnb();
					ArrayList<BasicIncomingList> inCheckDatas = new ArrayList<BasicIncomingList>();
					ArrayList<BasicShippingList> shCheckDatas = new ArrayList<BasicShippingList>();
					if ("A581".equals(wasClass)) {
						inCheckDatas = incomingListDao.findAllByCheck(wasClass, wasSn, wasnb);
					} else {
						shCheckDatas = shippingListDao.findAllByCheck(wasClass, wasSn, wasnb);
					}
					if (shCheckDatas.size() > 0) {
						BasicShippingList checkData = shCheckDatas.get(0);
						String areaKey = checkData.getBslfromwho().split("_")[0].replace("[", "") + "_"
								+ checkData.getBslpnumber();
						areaKey = areaKey.replaceAll(" ", "");
						//
						ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
						// 倉庫不夠?(已經有取?/未取?)
						int qty = 0;
						if (checkData.getBslpngqty() - checkData.getBslpnqty() >= 0) {
							// 已經取
						} else {
							// 未取完整+完全未取
							qty = areas.get(0).getWatqty() - checkData.getBslpnqty() + checkData.getBslpngqty();
							if (areas.size() > 0 && qty < 0) {
								throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
										new String[] { checkData.getBslpnumber() + " Qty is:" + qty });
							}
						}
					} else if (inCheckDatas.size() > 0) {
						// 必須以核單
						if (inCheckDatas.get(0).getBilcheckin() == 0) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
									new String[] { " Not yet verified : " + entityData.getWasclasssn() + "-"
											+ entityData.getWasnb() });
						}

						// 入料~無須檢測
					} else {
						// 找不到
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
								new String[] { entityData.getWasclasssn() + "-" + entityData.getWasnb() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		entityDatas.forEach(x -> {
			String wasClass = x.getWasclasssn().split("-")[0];
			String wasSn = x.getWasclasssn().split("-")[1];
			// String wasNb = x.getWasnb();
			String wasType = x.getWastype();
			if (wasType.equals("入料類")) {
				ArrayList<BasicIncomingList> arrayList = incomingListDao.findAllByCheck(wasClass, wasSn, null);
				ArrayList<BasicIncomingList> arrayListNew = new ArrayList<>();
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						//
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "Agree":
							t.setBilcuser(t.getBilcuser().equals("") ? x.getWascuser() : t.getBilcuser());
							break;
						case "Print":
							t.setBilpalready(1);
							break;

						case "Urgency":
							t.setBilstatus(x.getWasstatus());
							break;
						default:
							break;
						}
						arrayListNew.add(t);
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				incomingListDao.saveAll(arrayListNew);
			} else {
				// 領料類
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheck(wasClass, wasSn, null);
				ArrayList<BasicShippingList> arrayListNew = new ArrayList<>();
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						//
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "Agree":// 同意派送
							t.setBslcuser(t.getBslcuser().equals("") ? x.getWascuser() : t.getBslcuser());
							break;
						case "Print":// 打印
							t.setBslpalready(1);
							break;

						case "Urgency":// 急迫
							t.setBslstatus(x.getWasstatus());
							break;
						case "ManufacturePass"://
							t.setBslsmuser(t.getBslsmuser().equals("") ? x.getWassmuser() : t.getBslsmuser());
							break;
						default:
							break;
						}
						arrayListNew.add(t);
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				shippingListDao.saveAll(arrayListNew);
				historyDao.saveAll(entityHistories);
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
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						switch (action) {
						case "ReturnSelect":
							// 要有"已入數量"
							if (t.getBilpngqty() >= 0) {
								// 更新 儲位物料->有該儲位?
								Boolean checkOK = false;
								WarehouseArea area = new WarehouseArea();
								if (t.getBiltowho().split("_").length > 1) {
									String areaKey = t.getBiltowho().split("_")[0];
									areaKey = areaKey.replace("[", "") + "_" + t.getBilpnumber();
									areaKey = areaKey.replaceAll(" ", "");
									ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
									// 倉庫更新數量
									if (areas.size() > 0) {
										area = areas.get(0);
										int qty = area.getWatqty() - t.getBilpngqty();
										// 檢查 已經取多少?未取?已取?
										if (qty >= 0) {
											checkOK = true;
											area.setWatqty(qty);
											areaDao.save(area);
										}
									}
								}
								// 如果正常還原
								if (checkOK) {
									// 記錄用
									WarehouseHistory history = new WarehouseHistory();
									history.setWhtype("入料(指令:" + action + ")");
									history.setWhwmslocation(t.getBiltowho());
									history.setWhcontent(t.getBilclass() + "-" + //
											t.getBilsn() + "-" + //
											t.getBilnb() + "*" + t.getBilpngqty());
									history.setWhwmpnb(t.getBilpnumber());
									history.setWhfuser(packageBean.getUserAccount());
									history.setWheqty(area.getWaerptqty());
									history.setWhcqty(area.getWatqty());
									history.setWhpomqty("-" + t.getBilpnqty());
									history.setWhcheckin(t.getBilcheckin() == 0 ? "未核單" : "已核單");
									entityHistories.add(history);
									//
									t.setSysmdate(new Date());
									t.setSysmuser(packageBean.getUserAccount());
									t.setBilfuser("");
									t.setSysnote(t.getSysnote().replaceAll("\\[異常:.*?\\]", ""));// [異常:進貨料短少][異常:進貨料多][異常:備品轉][異常:部分領料][異常:庫存量不足]
									t.setBilpngqty(0);
								}
							}
							break;
						case "PassAll":
							Boolean checkOK = false;
							WarehouseArea area = new WarehouseArea();
							// 更新 儲位物料->有該儲位?
							if (t.getBiltowho().split("_").length > 1) {
								String areaKey = t.getBiltowho().split("_")[0];
								areaKey = areaKey.replace("[", "") + "_" + t.getBilpnumber();
								areaKey = areaKey.replaceAll(" ", "");
								ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
								// 倉庫更新數量+必須完成人空
								if (areas.size() > 0 && t.getBilfuser().equals("")) {
									area = areas.get(0);
									int qty = area.getWatqty();
									area.setWatqty(qty + (t.getBilpnqty() - t.getBilpngqty()));
									areaDao.save(area);
									checkOK = true;
								}
								// 更新單據+紀錄
								if (checkOK) {
									t.setBilcuser(t.getBilcuser().equals("") ? x.getWascuser() : t.getBilcuser());
									t.setBilpngqty(t.getBilpnqty());
									if (!t.getBilfuser().contains("System")) {// 已經登記自動化了記錄內:則不需要紀錄
										t.setBilfuser(t.getBilfuser().equals("")
												? "Pass(" + packageBean.getUserAccount() + ")"
												: t.getBilfuser());
										// 記錄用
										WarehouseHistory history = new WarehouseHistory();
										history.setWhtype("入料(指令:" + action + ")");
										history.setWhwmslocation(t.getBiltowho());
										history.setWhcontent(t.getBilclass() + "-" + t.getBilsn() + "-" + t.getBilnb()
												+ "*" + t.getBilpnqty());// 入料單
										history.setWhwmpnb(t.getBilpnumber());
										history.setWhfuser(packageBean.getUserAccount());
										history.setWheqty(area.getWaerptqty());
										history.setWhcqty(area.getWatqty());
										history.setWhpomqty("+" + t.getBilpnqty());
										history.setWhcheckin(t.getBilcheckin() == 0 ? "未核單" : "已核單");
										entityHistories.add(history);
									}
								}
							}
							break;
						default:
							break;
						}
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				incomingListDao.saveAll(arrayList);
			} else {
				ArrayList<BasicShippingList> arrayList = shippingListDao.findAllByCheck(wasClass, wasSn, wasNb);
				// 有資料?
				if (arrayList.size() > 0) {
					arrayList.forEach(t -> {
						// 記錄用
						t.setSysmdate(new Date());
						t.setSysmuser(packageBean.getUserAccount());
						switch (action) {
						case "ReturnSelect":
							// 要有"已入數量"
							if (t.getBslpngqty() >= 0) {
								// 更新 儲位物料->有該儲位?
								Boolean checkOK = false;
								WarehouseArea area = new WarehouseArea();
								if (t.getBslfromwho().split("_").length > 1) {
									String areaKey = t.getBslfromwho().split("_")[0];
									areaKey = areaKey.replace("[", "") + "_" + t.getBslpnumber();
									areaKey = areaKey.replaceAll(" ", "");
									ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
									// 倉庫更新數量
									if (areas.size() > 0) {
										checkOK = true;
										area = areas.get(0);
										int qty = area.getWatqty();
										area.setWatqty(qty + t.getBslpngqty());
										areaDao.save(area);
									}
								}
								// 如果正常還原
								if (checkOK) {
									// 記錄用
									WarehouseHistory history = new WarehouseHistory();
									history.setWhtype("領料(指令:" + action + ")");
									history.setWhwmslocation(t.getBslfromwho());
									history.setWhcontent(t.getBslclass() + "-" + //
											t.getBslsn() + "-" + //
											t.getBslnb() + "*" + t.getBslpngqty());
									history.setWhwmpnb(t.getBslpnumber());
									history.setWhfuser(packageBean.getUserAccount());
									history.setWheqty(area.getWaerptqty());
									history.setWhcqty(area.getWatqty());
									history.setWhpomqty("" + t.getBslpnqty());
									history.setWhcheckin(t.getBslcheckin() == 0 ? "未核單" : "已核單");
									entityHistories.add(history);
									//
									t.setSysmdate(new Date());
									t.setSysmuser(packageBean.getUserAccount());
									t.setBslfuser("");
									t.setSysnote(t.getSysnote().replaceAll("\\[異常:.*?\\]", ""));// [異常:進貨料短少][異常:進貨料多][異常:備品轉][異常:部分領料][異常:庫存量不足]
									t.setBslpngqty(0);
									t.setBslfucheckin(false);
								}
							}
							break;
						case "PassAll":
							Boolean checkOK = false;
							WarehouseArea area = new WarehouseArea();
							// 更新 儲位物料->有該儲位?
							if (t.getBslfromwho().split("_").length > 1) {
								String areaKey = t.getBslfromwho().split("_")[0];
								areaKey = areaKey.replace("[", "") + "_" + t.getBslpnumber();
								areaKey = areaKey.replaceAll(" ", "");
								ArrayList<WarehouseArea> areas = areaDao.findAllByWaaliasawmpnb(areaKey);
								// 倉庫更新數量+完成人必續空
								if (areas.size() > 0 && t.getBslfuser().equals("")) {
									area = areas.get(0);
									int qty = area.getWatqty() - (t.getBslpnqty() - t.getBslpngqty());// 未取完整?
									// 檢查:是否足夠扣除[庫存-需領用量+已領用量]
									if (qty >= 0) {
										area.setWatqty(qty);
										areaDao.save(area);
										checkOK = true;
									}
								}
								// 更新單據+紀錄
								if (checkOK) {
									t.setBslcuser(t.getBslcuser().equals("") ? x.getWascuser() : t.getBslcuser());
									t.setBslpngqty(t.getBslpnqty());
									t.setBslfucheckin(true);// 已集結
									if (!t.getBslfuser().contains("System")) {// 已經登記自動化了記錄內:則不需要紀錄
										t.setBslfuser(t.getBslfuser().equals("")
												? "Pass(" + packageBean.getUserAccount() + ")"
												: t.getBslfuser());
										// 記錄用
										WarehouseHistory history = new WarehouseHistory();
										history.setWhtype("領料(指令:" + action + ")");
										history.setWhwmslocation(t.getBslfromwho());
										history.setWhcontent(x.getWasfromcommand() + " " + // 製令單
												t.getBslclass() + "-" + t.getBslsn() + "-" + t.getBslnb() + "*"
												+ t.getBslpnqty());// 領料單
										history.setWhwmpnb(t.getBslpnumber());
										history.setWhfuser(packageBean.getUserAccount());
										history.setWheqty(area.getWaerptqty());
										history.setWhcqty(area.getWatqty());
										history.setWhpomqty("-" + t.getBslpnqty());
										history.setWhcheckin(t.getBslcheckin() == 0 ? "未核單" : "已核單");
										entityHistories.add(history);
									}
								}
							}
							break;
						default:
							break;
						}
					});
				}
				// =======================資料儲存=======================
				// 資料Data
				historyDao.saveAll(entityHistories);
				shippingListDao.saveAll(arrayList);
			}
		});

		return packageBean;
	}
}
