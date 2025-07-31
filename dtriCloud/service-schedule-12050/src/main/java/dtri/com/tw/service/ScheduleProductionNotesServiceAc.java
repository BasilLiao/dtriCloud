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
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.dao.BomItemSpecificationsDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.ScheduleProductionHistoryDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomProductManagement;
import dtri.com.tw.pgsql.entity.ScheduleProductionHistory;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class ScheduleProductionNotesServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private ScheduleProductionHistoryDao historyDao;

	@Autowired
	private BomProductManagementDao managementDao;

	@Autowired
	private BasicCommandListDao commandListDao;

	@Autowired
	private BomItemSpecificationsDao specificationsDao;

	@Autowired
	private BasicBomIngredientsDao bomIngredientsDao;

	// @Autowired
	// private EntityManager em;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.DESC, "syscdate"));// 建立時間
		orders.add(new Order(Direction.ASC, "sphbpmnb"));// 產品號
		List<Order> ordersD = new ArrayList<>();
		ordersD.add(new Order(Direction.ASC, "bpmnb"));// 產品號
		ordersD.add(new Order(Direction.ASC, "bpmmodel"));// 型號

		List<Order> ordersI = new ArrayList<>();
		ordersI.add(new Order(Direction.ASC, "bisgname"));// 項目組名稱
		ordersI.add(new Order(Direction.ASC, "bisfname"));// 正規化-項目內容
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));
		PageRequest pageableDetail = PageRequest.of(batch, total, Sort.by(ordersD));
		PageRequest pageableI = PageRequest.of(0, 99999, Sort.by(ordersI));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<ScheduleProductionHistory> entitys = historyDao.findAllBySearch(null, null, null, null, null,
					null, null, pageable);

			ArrayList<BomProductManagement> entityDetails = managementDao.findAllBySearch(null, null, null, null,
					pageableDetail);

			ArrayList<BomItemSpecifications> specifications = specificationsDao.findAllBySearch(null, null, null,
					pageableI);

			// Step3-2.資料區分(一般/細節)
			entityDetails.forEach(ed -> {
				// 處裡格式化
				StringBuilder noteAll = new StringBuilder();
				try {
					JsonArray notes = JsonParser.parseString(ed.getBpmbpsnv()).getAsJsonArray();
					String s = "「";
					String e = "」";
					notes.forEach(n -> {
						noteAll.append(s);
						noteAll.append(n.getAsString().replace("_", ":"));
						noteAll.append(e);
					});
					ed.setSysnote(ed.getSysnote() + noteAll);
				} catch (Exception e) {
					System.out.println(e);
					// not do anything->不加入note
				}
			});

			// 類別(一般模式)
			// 資料包裝
			String entityJson = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJson);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("ScheduleProductionHistory",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("BomProductManagement", null, 2);
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonArray searchDetailJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			JsonObject resultSettingJsons = new JsonObject();// 自動化參數
			JsonArray resultItemsJsons = new JsonArray();// 自選規格項目
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = ScheduleProductionHistory.class.getDeclaredFields();
			Field[] fieldDetails = BomProductManagement.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			resultDetailTJsons = packageService.resultSet(fieldDetails, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sphpon", "Ex:製令單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目(工單紀錄)
			searchJsons = packageService.searchSet(searchJsons, null, "sphbpmnb", "Ex:BOM號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立其他(自動化參數)

			// Step3-5. 建立其他(自選規格項目)
			String lastBisgname = "";
			for (BomItemSpecifications ii : specifications) {
				if (!lastBisgname.equals(ii.getBisgname())) {
					resultItemsJsons.add(">==============<" + ii.getBisgname() + ">==============<");
					lastBisgname = ii.getBisgname();
				}
				resultItemsJsons.add(ii.getBisgname() + "<_>" + ii.getBisfname() + "<_>" + ii.getBisnb());
			}

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("searchDetailSet", searchDetailJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);//
			// 自動化參數
			searchSetJsonAll.add("resultSetting", resultSettingJsons);//
			// 自選規格項目
			searchSetJsonAll.add("resultItems", resultItemsJsons);//

			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ScheduleProductionHistory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ScheduleProductionHistory.class);

			ArrayList<ScheduleProductionHistory> entitys = historyDao.findAllBySearch(searchData.getSphpon(),
					searchData.getSphbpmnb(), null, null, null, null, null, pageable);

			ArrayList<BomProductManagement> entityDetails = managementDao.findAllBySearch(searchData.getSphbpmnb(),
					null, null, null, pageableDetail);
			// Step4-2.資料區分(一般/細節)
			entityDetails.forEach(ed -> {
				// 處裡格式化
				StringBuilder noteAll = new StringBuilder();
				try {
					JsonArray notes = JsonParser.parseString(ed.getBpmbpsnv()).getAsJsonArray();
					String s = "「";
					String e = "」";
					notes.forEach(n -> {
						noteAll.append(s);
						noteAll.append(n.getAsString().replace("_", ":"));
						noteAll.append(e);
					});
					ed.setSysnote(ed.getSysnote() + noteAll);
				} catch (Exception e) {
					System.out.println(e);
					// not do anything->不加入note
				}
			});

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			String entityDteailJson = packageService.beanToJson(entityDetails);

			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDteailJson);

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]") && packageBean.getEntityDetailJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new ScheduleProductionHistory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("sphid_");
		packageBean.setEntityDetailIKeyGKey("bpmid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_sphhdate_sphsdate_sphindate");
		return packageBean;
	}

	/** 取得資料Order */
	public PackageBean getSearchOrder(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.DESC, "syscdate"));// 建立時間
		orders.add(new Order(Direction.ASC, "sphbpmnb"));// 產品號
		List<Order> ordersD = new ArrayList<>();
		ordersD.add(new Order(Direction.ASC, "bpmnb"));// 產品號
		ordersD.add(new Order(Direction.ASC, "bpmmodel"));// 型號
		// 一般模式
		// PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));
		PageRequest pageableDetail = PageRequest.of(batch, total, Sort.by(ordersD));

		// ========================區分:訪問/查詢========================

		// Step4-1. 取得資料(一般/細節)

		ScheduleProductionHistory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
				ScheduleProductionHistory.class);
		ArrayList<ScheduleProductionHistory> entitys = new ArrayList<ScheduleProductionHistory>();

		ArrayList<BomProductManagement> entityDetails = new ArrayList<BomProductManagement>();
		// 必須切兩段->要有資料->只抓一筆
		if (searchData.getSphpon() == null || searchData.getSphpon().equals("")) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
					new String[] { "check the fields again." });
		}
		String sphpon[] = searchData.getSphpon().split("-");
		String sphbpmnb = searchData.getSphbpmnb();// 特定BOM號
		if (sphpon.length == 2) {
			String bclclass = sphpon[0];
			String bclsn = sphpon[1];
			ArrayList<BasicCommandList> commandLists = commandListDao.findAllByComList(bclclass, bclsn, null, null);
			if (commandLists.size() > 0) {
				BasicCommandList commandList = commandLists.get(0);
				ScheduleProductionHistory entity = new ScheduleProductionHistory();
				// 客戶/國家/訂單
				String[] sysnot = null;
				String syshnote = commandList.getSyshnote();
				if (syshnote != null && !syshnote.trim().isEmpty()) {
					// 去除所有空格並進行分割
					sysnot = syshnote.replaceAll("\\s+", "").split("/");
				}
				//
				entity.setSyscdate(commandList.getSyscdate());
				entity.setSysmdate(commandList.getSysmdate());
				entity.setSyscuser(commandList.getSyscuser());
				entity.setSysmuser(commandList.getSysmuser());

				entity.setSphid(null);
				entity.setSphbpmnb(commandList.getBclproduct());// BOM 的產品號
				entity.setSphname(commandList.getBclpname());// 產品名稱
				entity.setSphspecification(commandList.getBclpspecification());// 產品規格
				entity.setSphbpmmodel("");// BOM 的產品型號
				entity.setSphbpmtype("2");// 產品歸類:0 = 開發BOM/1 = 產品BOM/2 = 配件BOM/3 = 半成品BOM/3 = 板階BOM
				entity.setSphbpmtypename("產品BOM");// 產品歸類名稱
				entity.setSphbisitem("{}");// 產品-物料結構
				entity.setSphbpsnv("[]");// 產品-參數設置
				entity.setSphprnv("[]");// 製造-參數設置
				entity.setSphscnv("[]");// 生管-參數設置
				entity.setSphbpsuser("");// BOM負責人
				entity.setSphpon(searchData.getSphpon());// 製令單號
				entity.setSphoqty(commandList.getBclpnqty());// 需生產數
				// 預設值
				String defaultSphonb = "";
				String defaultSphoname = "";
				String defaultSphocountry = "";

				// 檢查陣列長度與元素是否為 null，避免發生例外
				if (sysnot != null) {
				    entity.setSphoname(sysnot.length > 0 && sysnot[0] != null ? sysnot[0] : defaultSphoname);     // 客戶
				    entity.setSphocountry(sysnot.length > 1 && sysnot[1] != null ? sysnot[1] : defaultSphocountry); // 國家
				    entity.setSphonb(sysnot.length > 2 && sysnot[2] != null ? sysnot[2] : defaultSphonb);         // 訂單號
				} else {
				    // 若 sysnot 本身就是 null
				    entity.setSphoname(defaultSphoname);
				    entity.setSphocountry(defaultSphocountry);
				    entity.setSphonb(defaultSphonb);
				}
				entity.setSphobpmnb("");// 訂單 BOM的產品號
				entity.setSphhdate(commandList.getBclsdate());// 預計出貨日
				entity.setSphfrom("");// 規格來源:生管自訂/產品經理
				entity.setSphstatus(1);// 狀態類型 0=作廢單 1=有效單 2=自訂紀錄(不具備生產能力)
				entity.setSphprogress(0);// 進度:完成ERP工單(準備物料)=1/完成注意事項(預約生產)=2/完成->流程卡(準備生產)=3/(生產中)=4/(生產結束)=5
				entity.setSphssn("");// SN開始
				entity.setSphesn("");// SN結束
				entity.setSphpmnote("");// 產品經理事項
				entity.setSphscnote(commandList.getSyshonote());// 生管備註事項
				entity.setSphprnote1("");// 製造事項1
				entity.setSphprnote2("");// 製造事項2

				// BOM清單
				sphbpmnb = sphbpmnb != null ? sphbpmnb : commandList.getBclproduct();
				ArrayList<BomProductManagement> managements = managementDao.findAllBySearch(sphbpmnb, null, null, null,
						pageableDetail);
				entitys.add(entity);
				// 如果有匹配到BOM->
				if (managements.size() > 0) {
					entityDetails = managements;
					// 補充
					entity.setSphbpmmodel(managements.get(0).getBpmmodel());// BOM 的產品型號
					entity.setSphbisitem(managements.get(0).getBpmbisitem());// 產品-物料結構
					entity.setSphbpsnv(managements.get(0).getBpmbpsnv());// 產品-參數設置
					entity.setSphbpsuser(managements.get(0).getSyscuser());// 最後更改人
					// 處裡格式化
					StringBuilder noteAll = new StringBuilder();
					try {
						JsonArray notes = JsonParser.parseString(managements.get(0).getBpmbpsnv()).getAsJsonArray();
						String s = "「";
						String e = "」";
						notes.forEach(n -> {
							noteAll.append(s);
							noteAll.append(n.getAsString().replace("_", ":"));
							noteAll.append(e);
						});

					} catch (Exception e) {
						System.out.println(e);
						// not do anything->不加入note
					}
					entity.setSphpmnote(managements.get(0).getSysnote() + noteAll);
				}
			}
		}
		// Step4-2.資料區分(一般/細節)

		// 類別(一般模式)
		String entityJson = packageService.beanToJson(entitys);
		String entityDteailJson = packageService.beanToJson(entityDetails);

		// 資料包裝
		packageBean.setEntityJson(entityJson);
		packageBean.setEntityDetailJson(entityDteailJson);

		// 查不到資料
		if (packageBean.getEntityJson().equals("[]") && packageBean.getEntityDetailJson().equals("[]")) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}

		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new ScheduleProductionHistory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("sphid_");
		packageBean.setEntityDetailIKeyGKey("bpmid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_sphhdate");

		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		System.out.println("setModify");
		// =======================資料檢查=======================
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		System.out.println("setAdd");
		ArrayList<ScheduleProductionHistory> entityDatas = new ArrayList<>();
		ScheduleProductionHistory oldData = null;
		// =======================資料檢查=======================

		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleProductionHistory>>() {
					});

			// Step2.資料檢查
			for (ScheduleProductionHistory entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)->如果重複?->檢查狀態 是否已經打印->
				// 沒流程卡->舊資料 標記 stop
				// 有流成卡->不可動
				ArrayList<ScheduleProductionHistory> checkDatas = historyDao.findAllByCheck(entityData.getSphpon(),
						null, null, null);
				// 有資料?
				if (checkDatas.size() > 0 && checkDatas.get(0).getSphprogress() >= 3) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { entityData.getSphpon() });
				} else if (checkDatas.size() > 0 && checkDatas.get(0).getSphprogress() < 3) {
					// 可修改?
					oldData = new ScheduleProductionHistory();
					oldData = checkDatas.get(0);
					int n = historyDao.findAllBySearch(entityData.getSphpon(), null, null, null, null, null, null, null)
							.size();
					oldData.setSphpon(oldData.getSphpon() + "_STOP" + n);
					oldData.setSysmuser(packageBean.getUserAccount());

				}
			}
		}
		// =======================資料整理=======================
		if (oldData != null) {
			historyDao.save(oldData);
		}
		entityDatas.forEach(e -> {
			e.setSyscuser(packageBean.getUserAccount());
			e.setSyscdate(new Date());
			e.setSphprogress(2);
			e.setSphid(null);
			// 是否沒有產品品號?
			if (e.getSphname() == null || e.getSphname().equals("")) {
				ArrayList<BasicBomIngredients> ingredients = bomIngredientsDao.findAllBySearch(e.getSphbpmnb(), null,
						null, null, null, null);
				if (ingredients.size() > 0) {
					e.setSphname(ingredients.get(0).getBbiname());
					e.setSphspecification(ingredients.get(0).getBbispecification());
				}
			}

		});
		historyDao.saveAll(entityDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		System.out.println("setInvalid");
		// =======================資料檢查=======================
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		return packageBean;
	}
}
