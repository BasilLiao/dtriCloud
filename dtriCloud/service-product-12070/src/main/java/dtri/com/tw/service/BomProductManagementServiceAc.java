package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.diff.StringsComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.BomHistoryDao;
import dtri.com.tw.pgsql.dao.BomItemSpecificationsDao;
import dtri.com.tw.pgsql.dao.BomKeeperDao;
import dtri.com.tw.pgsql.dao.BomNotificationDao;
import dtri.com.tw.pgsql.dao.BomParameterSettingsDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.BomProductRuleDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.BomHistory;
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomItemSpecificationsDetailFront;
import dtri.com.tw.pgsql.entity.BomKeeper;
import dtri.com.tw.pgsql.entity.BomNotification;
import dtri.com.tw.pgsql.entity.BomParameterSettings;
import dtri.com.tw.pgsql.entity.BomProductManagement;
import dtri.com.tw.pgsql.entity.BomProductManagementDetailFront;
import dtri.com.tw.pgsql.entity.BomProductRule;
import dtri.com.tw.pgsql.entity.BomSoftwareHardware;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
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
public class BomProductManagementServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private BomItemSpecificationsDao specificationsDao;

	@Autowired
	private BomProductManagementDao managementDao;

	@Autowired
	private BomProductRuleDao productRuleDao;

	@Autowired
	private BasicBomIngredientsDao ingredientsDao;

	@Autowired
	private BomParameterSettingsDao settingsDao;

	@Autowired
	private BomNotificationDao notificationDao;

	@Autowired
	private WarehouseMaterialDao materialDao;

	@Autowired
	private BomKeeperDao bomKeeperDao;

	@Autowired
	private BomHistoryDao historyDao;

	@Autowired
	private BasicNotificationMailDao notificationMailDao;

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
		orders.add(new Order(Direction.ASC, "bpmnb"));// 群組
		// 其他資料格式(同步-BOM成品組成/BOM產品規則/BOM可選擇性項目)
		List<Order> ordersBBI = new ArrayList<>();
		ordersBBI.add(new Order(Direction.ASC, "bbisn"));//
		ordersBBI.add(new Order(Direction.ASC, "bbiisn"));//
		List<Order> ordersBPR = new ArrayList<>();
		ordersBPR.add(new Order(Direction.ASC, "bprname"));//
		List<Order> ordersBIS = new ArrayList<>();
		ordersBIS.add(new Order(Direction.ASC, "syssort"));//
		ordersBIS.add(new Order(Direction.ASC, "bisgname"));//
		ordersBIS.add(new Order(Direction.ASC, "bisfname"));//
		List<Order> ordersBPS = new ArrayList<>();
		ordersBPS.add(new Order(Direction.ASC, "syssort"));//
		ordersBPS.add(new Order(Direction.ASC, "bpsname"));//
		List<Order> ordersWM = new ArrayList<>();
		ordersWM.add(new Order(Direction.ASC, "wmpnb"));//

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<BomProductManagement> entitys = managementDao.findAllBySearch(null, null, null, null, null,
					pageable);
			ArrayList<BomProductManagementDetailFront> entityDetails = new ArrayList<BomProductManagementDetailFront>();

			// Step3-2.資料區分(一般/細節)
			// 正規化BOM
			PageRequest pageableBIS = PageRequest.of(0, 20000, Sort.by(ordersBIS));
			ArrayList<BomItemSpecifications> entityBIS = specificationsDao.findAllBySearch(null, null, null,
					pageableBIS);
			// 測試用
			entityBIS.forEach(x -> {
				if (x.getBisnb().equals("50-117-270001")) {
					System.out.println(x.getBisfname());
				}
			});

			// 規則BOM
			PageRequest pageableBPR = PageRequest.of(0, 200, Sort.by(ordersBPR));
			ArrayList<BomProductRule> entityBPR = productRuleDao.findAllBySearch(null, null, null, pageableBPR);
			// ERP_料BOM
			PageRequest pageableBBI = PageRequest.of(0, 5000, Sort.by(ordersBBI));
			ArrayList<BasicBomIngredients> entityBBI = ingredientsDao.findAllBySearch("90-504", null, null, null, null,
					null, pageableBBI);
			// 參數BOM
			PageRequest pageableBPS = PageRequest.of(0, 200, Sort.by(ordersBPS));
			ArrayList<BomParameterSettings> entityBPS = settingsDao.findAllBySearch(null, null, null, pageableBPS);
			// 物料清單
			PageRequest pageableWM = PageRequest.of(0, 1000, Sort.by(ordersWM));
			ArrayList<WarehouseMaterial> entityWM = materialDao.findAllBySearch(null, "停", null, pageableWM);

			// 資料整理(BBI-限制200筆)
			Map<String, ArrayList<BasicBomIngredients>> entityBBIMap = new TreeMap<String, ArrayList<BasicBomIngredients>>();
			int indexBBI = 0;
			while (entityBBI.size() > indexBBI && entityBBIMap.size() <= 200) {
				ArrayList<BasicBomIngredients> ingredients = new ArrayList<BasicBomIngredients>();
				BasicBomIngredients bbi = entityBBI.get(indexBBI);
				if (entityBBIMap.containsKey(bbi.getBbisn())) {
					ingredients = entityBBIMap.get(bbi.getBbisn());
					ingredients.add(bbi);
					entityBBIMap.put(bbi.getBbisn(), ingredients);
				} else {
					ingredients.add(bbi);
					entityBBIMap.put(bbi.getBbisn(), ingredients);
				}
				indexBBI++;
			}
			ArrayList<BasicBomIngredients> entityBBIh = new ArrayList<BasicBomIngredients>();
			ArrayList<BasicBomIngredients> entityBBId = new ArrayList<BasicBomIngredients>();
			// 資料整理(BBI-限制200筆)
			entityBBIMap.forEach((k, v) -> {
				v.forEach(d -> {
					entityBBId.add(d);// d
				});
				BasicBomIngredients h = new BasicBomIngredients();
				h.setBbiname(v.get(0).getBbiname());
				h.setBbispecification(v.get(0).getBbispecification());
				h.setBbidescription(v.get(0).getBbidescription());
				h.setBbisn(v.get(0).getBbisn());
				entityBBIh.add(h);// h
			});
			// 資料放入
			String entityJsonBBI = packageService.beanToJson(entityBBIh);
			String entityDetailJsonBBI = packageService.beanToJson(entityBBId);
			String entityJsonBPR = packageService.beanToJson(entityBPR);
			String entityJsonBIS = packageService.beanToJson(entityBIS);
			String entityJsonBPS = packageService.beanToJson(entityBPS);
			String entityJsonWM = packageService.beanToJson(entityWM);

			JsonObject other = new JsonObject();
			other.add("BBI", packageService.StringToAJson(entityJsonBBI));
			other.add("BBIDetail", packageService.StringToAJson(entityDetailJsonBBI));
			other.add("BPR", packageService.StringToAJson(entityJsonBPR));
			other.add("BIS", packageService.StringToAJson(entityJsonBIS));
			other.add("BPS", packageService.StringToAJson(entityJsonBPS));
			other.add("WM", packageService.StringToAJson(entityJsonWM));
			//
			String entityJson = packageService.beanToJson(entitys);
			String entityDetailJson = packageService.beanToJson(entityDetails);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDetailJson);
			packageBean.setOtherSet(other.toString());

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguagesBPM = new HashMap<>();// 規格BOM(BOM產品管理)
			Map<String, SystemLanguageCell> mapLanguagesDetailBPM = new HashMap<>();// 規格BOM 細節(BOM產品管理)
			// 而外資料
			Map<String, SystemLanguageCell> mapLanguagesBBI = new HashMap<>();// From ERP BOM list(同步-BOM成品組成)
			Map<String, SystemLanguageCell> mapLanguagesBPR = new HashMap<>();// BOM rules list(BOM產品規則)
			Map<String, SystemLanguageCell> mapLanguagesBIS = new HashMap<>();// BOM detailed material(BOM物料項目規範)
			Map<String, SystemLanguageCell> mapLanguagesBSH = new HashMap<>();// BOM Software Hardware(用途:BOM軟硬體配置)
			Map<String, SystemLanguageCell> mapLanguagesBPS = new HashMap<>();// BOM Parameter Settings(用途:BOM產品參數)

			// 一般翻譯
			ArrayList<SystemLanguageCell> languagesBPM = languageDao.findAllByLanguageCellSame("BomProductManagement",
					null, 2);
			languagesBPM.forEach(x -> {
				mapLanguagesBPM.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetailBPM = languageDao
					.findAllByLanguageCellSame("BomProductManagementDetailFront", null, 2);
			languagesDetailBPM.forEach(x -> {
				mapLanguagesDetailBPM.put(x.getSltarget(), x);
			});

			// 同步-BOM成品組成
			ArrayList<SystemLanguageCell> languagesBBI = languageDao.findAllByLanguageCellSame("BasicBomIngredients",
					null, 2);
			languagesBBI.forEach(x -> {
				mapLanguagesBBI.put(x.getSltarget(), x);
			});
			// BOM產品規則
			ArrayList<SystemLanguageCell> languagesBPR = languageDao.findAllByLanguageCellSame("BomProductRule", null,
					2);
			languagesBPR.forEach(x -> {
				mapLanguagesBPR.put(x.getSltarget(), x);
			});
			// BOM物料項目規範
			ArrayList<SystemLanguageCell> languagesBIS = languageDao
					.findAllByLanguageCellSame("BomItemSpecificationsDetailFront", null, 2);
			languagesBIS.forEach(x -> {
				mapLanguagesBIS.put(x.getSltarget(), x);
			});
			// BOM軟硬體配置
			ArrayList<SystemLanguageCell> languagesBSH = languageDao.findAllByLanguageCellSame("BomSoftwareHardware",
					null, 2);
			languagesBSH.forEach(x -> {
				mapLanguagesBSH.put(x.getSltarget(), x);
			});
			// BOM產品參數
			ArrayList<SystemLanguageCell> languagesBPS = languageDao.findAllByLanguageCellSame("BomParameterSettings",
					null, 2);
			languagesBPS.forEach(x -> {
				mapLanguagesBPS.put(x.getSltarget(), x);
			});

			// 動態->覆蓋寫入->修改UI選項
			// BBI
			mapLanguagesBBI.forEach((k, s) -> {
				if (k.equals("bbisn") || k.equals("bbiname") || k.equals("bbispecification")
						|| k.equals("bbidescription") || k.indexOf("sysnote") >= 0) {
					s.setSlcshow(1);
				} else {
					s.setSlcshow(0);
				}
			});
			// BIS
			mapLanguagesBIS.forEach((k, s) -> {
				if (k.equals("bisnb") || k.equals("bisname") || k.equals("bisqty") || k.equals("bisprocess")
						|| k.equals("sysnote") || k.equals("bislevel")) {
					s.setSlcshow(1);
					if (k.equals("sysnote")) {
						// s.setSlcwidth(400);
					}
				} else {
					s.setSlcshow(0);
				}
			});

			mapLanguagesDetailBPM.forEach((k, s) -> {
				// 只顯示這幾項目
				if (k.equals("bisnb") || k.equals("bisname") || k.equals("bisgname") || k.equals("bisqty")
						|| k.equals("bissdescripion") || k.equals("bisfname") || k.equals("bisprocess")
						|| k.equals("bislevel")) {
					s.setSlcshow(1);
					switch (k) {
					case "bisgname":
						s.setSyssort(0);
						break;
					case "bisfname":
						s.setSyssort(1);
						break;
					case "bisqty":
						s.setSyssort(2);
						break;
					case "bisnb":
						s.setSyssort(3);
						break;
					case "bislevel":
						s.setSyssort(4);
						break;
					case "bisname":
						s.setSyssort(5);
						break;
					case "bisprocess":
						s.setSyssort(6);
						break;
					case "bissdescripion":
						s.setSyssort(7);
						break;
					case "biswhere":
						s.setSyssort(8);
						break;
					}
				} else {
					s.setSlcshow(0);
				}
			});
			// 產品參數只要:參數名稱 & 參數值(Def)
			mapLanguagesBPS.forEach((k, s) -> {
				// 只顯示這幾項目
				if (k.equals("bpsname") || k.equals("bpsval")) {
					s.setSlcshow(1);
				} else {
					s.setSlcshow(0);
				}
			});

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsonsBPM = new JsonArray();// 查詢設定(BOM產品管理)
			JsonArray searchJsonsBBI = new JsonArray();// 查詢設定(同步-BOM成品組成)
			JsonArray searchJsonsBPR = new JsonArray();// 查詢設定(BOM產品規則)
			//
			JsonObject resultDataTJsonsBPM = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsonsBPM = new JsonObject();// 回傳欄位-細節名稱
			// 而外資料
			JsonObject resultDataTJsonsBBI = new JsonObject();// 回傳欄位-(同步-BOM成品組成)
			JsonObject resultDataTJsonsBPR = new JsonObject();// 回傳欄位-(BOM產品規則)
			JsonObject resultDataTJsonsBIS = new JsonObject();// 回傳欄位-(BOM物料項目規範)
			JsonObject resultDataTJsonsBSH = new JsonObject();// 回傳欄位-(BOM軟硬體配置)
			JsonObject resultDataTJsonsBPS = new JsonObject();// 回傳欄位-(BOM產品參數)

			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = BomProductManagement.class.getDeclaredFields();
			Field[] fieldsDetail = BomProductManagementDetailFront.class.getDeclaredFields();
			// 而外資料
			Field[] fieldsBBI = BasicBomIngredients.class.getDeclaredFields();
			Field[] fieldsBPR = BomProductRule.class.getDeclaredFields();
			Field[] fieldsBIS = BomItemSpecificationsDetailFront.class.getDeclaredFields();
			Field[] fieldsBSH = BomSoftwareHardware.class.getDeclaredFields();
			Field[] fieldsBPS = BomParameterSettings.class.getDeclaredFields();

			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsonsBPM = packageService.resultSet(fields, exceptionCell, mapLanguagesBPM);
			// 欄位翻譯(細節)
			resultDetailTJsonsBPM = packageService.resultSet(fieldsDetail, exceptionCell, mapLanguagesDetailBPM);
			// 而外資料
			resultDataTJsonsBBI = packageService.resultSet(fieldsBBI, exceptionCell, mapLanguagesBBI);
			resultDataTJsonsBPR = packageService.resultSet(fieldsBPR, exceptionCell, mapLanguagesBPR);
			resultDataTJsonsBIS = packageService.resultSet(fieldsBIS, exceptionCell, mapLanguagesBIS);
			resultDataTJsonsBSH = packageService.resultSet(fieldsBSH, exceptionCell, mapLanguagesBSH);
			resultDataTJsonsBPS = packageService.resultSet(fieldsBPS, exceptionCell, mapLanguagesBPS);

			// Step3-5. 建立查詢項目
			// ERP 料BOM
			searchJsonsBBI = packageService.searchSet(searchJsonsBBI, null, "bbisn", "Ex:成品-物料號(物料)?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);

			searchJsonsBBI = packageService.searchSet(searchJsonsBBI, null, "bbiname", "Ex:成品-物料名稱(物料)?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);
			// Cloud rules
			searchJsonsBPR = packageService.searchSet(searchJsonsBPR, null, "bprname", "Ex:規則名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);
			searchJsonsBPR = packageService.searchSet(searchJsonsBPR, null, "bprbisitem", "Ex:規則內容?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);
			// BOM規格
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "bpmnb", "Ex:成品號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_3);
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "bpmmodel", "Ex:產品型號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_3);
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "bpmbisitem", "Ex:規格內容?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_3);
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "syscuser", "Ex:user?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_3);
			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsonsBPM);
			searchSetJsonAll.add("searchSetBBI", searchJsonsBBI);
			searchSetJsonAll.add("searchSetBPR", searchJsonsBPR);
			// Result欄位
			searchSetJsonAll.add("resultThead", resultDataTJsonsBPM);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsonsBPM);
			searchSetJsonAll.add("resultTheadBIS", resultDataTJsonsBIS);
			searchSetJsonAll.add("resultTheadBPR", resultDataTJsonsBPR);
			searchSetJsonAll.add("resultTheadBBI", resultDataTJsonsBBI);
			searchSetJsonAll.add("resultTheadBSH", resultDataTJsonsBSH);
			searchSetJsonAll.add("resultTheadBPS", resultDataTJsonsBPS);

			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-0.哪一種查詢other_BBI/other_BPR/other_BPM
			String otherType = packageBean.getOtherSet();
			JsonObject other = new JsonObject();
			// Step3-1.取得資料(一般/細節)
			ArrayList<BomProductManagement> entitys = new ArrayList<BomProductManagement>();
			ArrayList<BomProductManagementDetailFront> entityDetails = new ArrayList<BomProductManagementDetailFront>();

			if (otherType.equals("BBI")) {
				// Step4-1. 取得資料(一般/細節)
				BasicBomIngredients searchData = packageService.jsonToBean(packageBean.getEntityJson(),
						BasicBomIngredients.class);
				// ERP_料BOM

				String bbisn = searchData.getBbisn();
				String bbiname = searchData.getBbiname();
				if (bbisn == null && bbiname == null) {
					bbisn = "90-504-T10";
				}

				PageRequest pageableBBI = PageRequest.of(0, 50000, Sort.by(ordersBBI));
				// 先查詢有哪些BOM->每一個查詢展BOM(因為JPA 與 原生SQL 有技術上的匹配不到)
				ArrayList<BasicBomIngredients> bbisnList = ingredientsDao.findAllBySearch(bbisn, bbiname, null, null,
						null, null, pageableBBI);
				Map<String, BasicBomIngredients> bbisnMap = new TreeMap<String, BasicBomIngredients>();
				for (BasicBomIngredients bbis : bbisnList) {
					// 沒有?最多50筆
					if (!bbisnMap.containsKey(bbis.getBbisn()) && bbisnMap.size() <= 50) {
						bbisnMap.put(bbis.getBbisn(), bbis);
					}
					// 50筆資料後 跳出
					if (bbisnMap.size() == 50) {
						break;
					}
				}
				// 每個查詢
				Map<String, ArrayList<BasicBomIngredients>> entityBBIMap = new HashMap<String, ArrayList<BasicBomIngredients>>();

				for (Entry<String, BasicBomIngredients> entry : bbisnMap.entrySet()) {
					String k = entry.getKey();
					// BasicBomIngredients v = entry.getValue();
					ArrayList<BasicBomIngredients> entityOneBBI = ingredientsDao.findFlattenedBomLevel(k, null);
					if (entityOneBBI != null && !entityOneBBI.isEmpty()) {
						// 對應異常(主BOM 會因多筆查詢導致 前BOM項目蓋去後BOM項目)
						ArrayList<BasicBomIngredients> correctionNew = new ArrayList<BasicBomIngredients>();
						for (int i = 0; i < entityOneBBI.size(); i++) {
							BasicBomIngredients corrected = new BasicBomIngredients();
							BasicBomIngredients original = entityOneBBI.get(i);
							corrected.setBbiid(original.getBbiid());
							corrected.setBbinb(original.getBbinb());
							corrected.setBbisn(k); // <-- 在這裡，為新物件設定正確的 bbi_sn
							corrected.setBbisnnb(original.getBbisnnb());
							corrected.setBbiname(original.getBbiname());
							corrected.setSysnote(original.getSysnote());
							corrected.setBbiisn(original.getBbiisn());
							corrected.setBbiiqty(original.getBbiiqty());
							corrected.setBbiidescription(original.getBbiidescription());
							corrected.setBbiiname(original.getBbiiname());
							corrected.setBbiiserp(original.getBbiiserp());
							corrected.setBbiiprocess(original.getBbiiprocess());
							corrected.setBbiiqty(original.getBbiiqty());
							corrected.setBbiilevel(original.getBbiilevel() == 0 ? 1 : original.getBbiilevel());
							corrected.setBbiispecification(original.getBbiispecification());
							System.out.println((i + 1) + " : " + corrected.getBbisn() + " : " + corrected.getBbiisn()
									+ " : " + corrected.getBbiilevel() + " : " + corrected.getSysnote());
							correctionNew.add(corrected);
						}
						// 彙整->分類->把每次查到的結果加到總集合
						entityBBIMap.put(k, correctionNew);
					}
				}

//				//會異常-> 每個查詢
//				Map<String, ArrayList<BasicBomIngredients>> entityBBIMap = new HashMap<String, ArrayList<BasicBomIngredients>>();
//				ArrayList<BasicBomIngredients> entityBBIs = ingredientsDao.findFlattenedBomLevel(bbisn, bbiname);
//				int i = 0;
//				for (BasicBomIngredients entityOneBBI : entityBBIs) {
//					ArrayList<BasicBomIngredients> correctionNew = new ArrayList<BasicBomIngredients>();
//					BasicBomIngredients corrected = new BasicBomIngredients();
//					BasicBomIngredients original = entityOneBBI;
//					corrected.setBbiid(original.getBbiid());
//					corrected.setBbinb(original.getBbinb());
//					corrected.setBbisn(original.getBbisn()); // <-- 在這裡，為新物件設定正確的 bbi_sn
//					corrected.setBbisnnb(original.getBbisnnb());
//					corrected.setBbiname(original.getBbiname());
//					corrected.setSysnote(original.getSysnote());
//					corrected.setBbiisn(original.getBbiisn());
//					corrected.setBbiiqty(original.getBbiiqty());
//					corrected.setBbiidescription(original.getBbiidescription());
//					corrected.setBbiiname(original.getBbiiname());
//					corrected.setBbiiserp(original.getBbiiserp());
//					corrected.setBbiiprocess(original.getBbiiprocess());
//					corrected.setBbiiqty(original.getBbiiqty());
//					corrected.setBbiispecification(original.getBbiispecification());
//					// 顯示
//					System.out.println((i += 1) + " : " + corrected.getBbisn() + " : " + corrected.getBbiisn() + " : "
//							+ corrected.getSysnote());
//					// 有重複?
//					if (entityBBIMap.containsKey(entityOneBBI.getBbisn())) {
//						correctionNew = entityBBIMap.get(entityOneBBI.getBbisn());
//						correctionNew.add(corrected);
//						entityBBIMap.put(corrected.getBbisn(), correctionNew);
//					} else {
//						correctionNew.add(corrected);
//						entityBBIMap.put(corrected.getBbisn(), correctionNew);
//					}
//					// 數量上限100
//					if (entityBBIMap.size() >= 100) {
//						break;
//					}
//				}

				// 資料整理(BBI-限制100筆)
				ArrayList<BasicBomIngredients> entityBBIh = new ArrayList<BasicBomIngredients>();
				ArrayList<BasicBomIngredients> entityBBId = new ArrayList<BasicBomIngredients>();
				// 分類整粒
				bbisnMap.forEach((k, v) -> {
					// ERP Header
					entityBBIh.add(v);// h
					// ERP Body
					entityBBId.addAll(entityBBIMap.get(k));// b

				});
				// 資料放入
				String entityJsonBBI = packageService.beanToJson(entityBBIh);
				String entityDetailJsonBBI = packageService.beanToJson(entityBBId);
				other.add("BBI", packageService.StringToAJson(entityJsonBBI));
				other.add("BBIDetail", packageService.StringToAJson(entityDetailJsonBBI));
			} else if (otherType.equals("BPM")) {
				// Step4-1. 取得資料(一般/細節)
				BomProductManagement searchData = packageService.jsonToBean(packageBean.getEntityJson(),
						BomProductManagement.class);
				// Step3-1.取得資料(一般/細節)
				if (searchData.getBpmnb() != null && searchData.getBpmnb().split(" ").length > 0) {
					// 多筆查詢
					String bpmnb[] = searchData.getBpmnb().split(" ");
					String checkSame = "";
					for (String bpmnbOne : bpmnb) {
						if (!bpmnbOne.equals("")) {
							ArrayList<BomProductManagement> someBom = managementDao.findAllBySearch(bpmnbOne,
									searchData.getBpmmodel(), null, searchData.getBpmbisitem(),
									searchData.getSyscuser(), pageable);
							if (someBom.size() > 0) {
								for (BomProductManagement aBom : someBom) {
									// 避免重複
									if (!checkSame.contains(aBom.getBpmnb())) {
										checkSame += aBom.getBpmnb() + "_";
										entitys.add(aBom);
									}
								}
							}
						}
					}
				} else {
					// 單筆資料查詢
					entitys = managementDao.findAllBySearch(searchData.getBpmnb(), searchData.getBpmmodel(), null,
							searchData.getBpmbisitem(), searchData.getSyscuser(), pageable);
				}

				ArrayList<BomProductManagementDetailFront> entityNewDetails = new ArrayList<BomProductManagementDetailFront>();

				// Step3-2.資料區分(一般/細節)
				entityDetails = entityNewDetails;
			}

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			String entityDetailJson = packageService.beanToJson(entityDetails);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDetailJson);
			packageBean.setOtherSet(other.toString());
			packageBean.setCallBackValue(otherType);
			// 查不到資料
			if (!(packageBean.getEntityJson().equals("[]") || packageBean.getOtherSet().equals("{}"))) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}

		}

		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatBBI = packageService.beanToJson(new BasicBomIngredients());
		String entityFormatBPR = packageService.beanToJson(new BomProductRule());
		String entityFormatBPM = packageService.beanToJson(new BomProductManagement());
		JsonObject entityFormats = new JsonObject();
		entityFormats.addProperty("BBI", entityFormatBBI);
		entityFormats.addProperty("BPR", entityFormatBPR);
		entityFormats.addProperty("BPM", entityFormatBPM);
		packageBean.setEntityFormatJson(entityFormats.toString());
		// KEY名稱Ikey_Gkey
		JsonObject entityIKeyGKeys = new JsonObject();
		entityIKeyGKeys.addProperty("BBI", "bbiid_bbigid");
		entityIKeyGKeys.addProperty("BPR", "bprid_");
		entityIKeyGKeys.addProperty("BPM", "bpmid_bpmgid");
		packageBean.setEntityIKeyGKey(entityIKeyGKeys.toString());
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 取得物料清單資料 */
	public PackageBean getSearchWM(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		List<Order> ordersWM = new ArrayList<>();
		ordersWM.add(new Order(Direction.ASC, "wmpnb"));//
		// 物料清單
		PageRequest pageableWM = PageRequest.of(0, 60000, Sort.by(ordersWM));
		ArrayList<WarehouseMaterial> entityWM = materialDao.findAllBySearch(null, "停", null, pageableWM);
		// 資料放入
		String entityJsonWM = packageService.beanToJson(entityWM);
		JsonObject other = new JsonObject();
		other.add("WM", packageService.StringToAJson(entityJsonWM));
		// 資料包裝
		packageBean.setOtherSet(other.toString());
		packageBean.setCallBackValue("BPMWM");

		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductManagement> saveDatasUpdate = new ArrayList<BomProductManagement>();
		ArrayList<BomProductManagement> entityDatas = new ArrayList<>();
		ArrayList<BomKeeper> bomKeepers = bomKeeperDao.findAllBySearch(packageBean.getUserAccount(), null, null, null);
		ArrayList<BomItemSpecifications> entityBIS = specificationsDao.findAllBySearch(null, null, null, null);// 選擇清單項目

		Map<String, ArrayList<BomItemSpecifications>> entityCheckSame = new HashMap<String, ArrayList<BomItemSpecifications>>();// 為了:去除同類型物料()
		Map<String, Boolean> entityCheckBismproduct = new HashMap<String, Boolean>();// GID 必填?成品
		Map<String, Boolean> entityCheckBismaccessories = new HashMap<String, Boolean>();// GID 必填?配件

		entityBIS.forEach(b -> {
			// 有必填?
			System.out.println(b.getBisgid() + " 成品:" + b.getBismproduct() + " 配件:" + b.getBismaccessories());
			// GID 必填?成品
			if (b.getBismproduct() && !entityCheckBismproduct.containsKey(b.getBisgid() + "")) {
				entityCheckBismproduct.put(b.getBisgid() + "", true);
			}
			// GID 必填?配件
			if (b.getBismaccessories() && !entityCheckBismaccessories.containsKey(b.getBisgid() + "")) {
				entityCheckBismaccessories.put(b.getBisgid() + "", true);
			}
			// 為了:去除同類型資料item->basic
			ArrayList<BomItemSpecifications> newCheckSame = new ArrayList<BomItemSpecifications>();
			if (entityCheckSame.containsKey(b.getBisgid() + "")) {
				newCheckSame = entityCheckSame.get(b.getBisgid() + "");
				newCheckSame.add(b);
				entityCheckSame.put(b.getBisgid() + "", newCheckSame);
			} else {
				newCheckSame.add(b);
				entityCheckSame.put(b.getBisgid() + "", newCheckSame);
			}
		});

		Boolean in_Production = false;
		Boolean auto_Item = false;
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductManagement>>() {
					});
			// 急單檢查?
			JsonObject sn_checkJson = new JsonObject();
			sn_checkJson = JsonParser.parseString(packageBean.getCallBackValue()).getAsJsonObject();
			// 避免沒資料-急單
			if (sn_checkJson.has("In_Production") && !sn_checkJson.get("In_Production").isJsonNull()) {
				in_Production = sn_checkJson.get("In_Production").getAsBoolean();
			}
			// 避免沒資料-檢查連動物料 局限於 92 / 81
			if (sn_checkJson.has("Auto_item") && !sn_checkJson.get("Auto_item").isJsonNull()) {
				auto_Item = sn_checkJson.get("Auto_item").getAsBoolean();
			}

			// Step2.資料檢查
			for (BomProductManagement entityData : entityDatas) {
				// 檢查-舊資料-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<BomProductManagement> checkDatas = managementDao.findAllByCheck(entityData.getBpmnb(), null,
						null, null, null);
				for (BomProductManagement checkData : checkDatas) {
					// 排除自己
					if (entityData.getBpmid() != null && checkData.getBpmid().compareTo(entityData.getBpmid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getBpmnb() });
					}
				}
				// Step2-1.檢查-新資料-資料檢查-缺少值?
				if (entityData.getBpmmodel() == null || entityData.getBpmmodel().equals("") || // 型號
						entityData.getBpmnb() == null || entityData.getBpmnb().equals("") || // 成品號
						entityData.getBpmtype() == null || entityData.getBpmtype() < 0 || // 成品類
						entityData.getBpmbpsnv() == null || entityData.getBpmbpsnv().equals("") || // 參數
						entityData.getBpmbisitem() == null || entityData.getBpmbisitem().equals("")) {// 物料匹配規格
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getBpmmodel() });
				}

				// Step2-2.備註檢查是否一樣 ?
				ArrayList<BomProductManagement> checkDatas2 = managementDao.findAllByCheck(null, null, null,
						entityData.getSysnote(), null);
				if (checkDatas2.size() > 0) {
					for (BomProductManagement one : checkDatas2) {
						// 如果跟本產品不同在警報
						if (!one.getBpmnb().equals(entityData.getBpmnb())
								&& !one.getBpmid().equals(entityData.getBpmid())) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
									new String[] { ":" + one.getBpmnb() + " 備註 : " + entityData.getSysnote() });
						}
					}
				}

				// Step2-3.檢查BOM所有項目 內容 是否一樣 ?
				ArrayList<BomProductManagement> checkDatas3 = managementDao.findAllByCheck(null, null, null, null,
						entityData.getBpmbisitem());
				if (checkDatas3.size() > 0) {
					for (BomProductManagement one : checkDatas3) {
						// 如果跟本產品不同在警報
						if (!one.getBpmnb().equals(entityData.getBpmnb())
								&& !one.getBpmid().equals(entityData.getBpmid())) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
									new String[] { one.getBpmnb() + " 規格內容 : " + checkDatas3.get(0).getBpmnb() + " : "
											+ entityData.getBpmnb() });
						}
					}
				}

				// Step2-4.檢查BOM所有項目 是否有重複 ?
				Map<String, Boolean> checkGorupItems = new HashMap<String, Boolean>();
				JsonObject bpmbisitem = new JsonObject();
				JsonArray bpmbisitems = new JsonArray();
				bpmbisitem = JsonParser.parseString(entityData.getBpmbisitem()).getAsJsonObject();
				bpmbisitems = bpmbisitem.get("items").getAsJsonArray();
				for (JsonElement bomKeeper : bpmbisitems) {
					String key = bomKeeper.getAsJsonObject().get("bisgname").getAsString();
					if (checkGorupItems.containsKey(key)) {
						// 有重複跳出訊號
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getBpmnb() + " : " + key });
					}
					checkGorupItems.put(key, true);
				}
				// Step2-5.檢查 項目是否缺少
				if (entityData.getBpmtypename().equals("產品BOM")) {
					// 成品類 ?
					JsonObject entityV = JsonParser.parseString(entityData.getBpmbisitem()).getAsJsonObject();
					JsonArray items = entityV.getAsJsonArray("items");
					for (JsonElement itemCheck : items) {
						String bisgid = itemCheck.getAsJsonObject().get("bisgid").getAsString();
						if (entityCheckBismproduct.containsKey(bisgid)) {
							entityCheckBismproduct.put(bisgid, false);
						}
					}
					for (Map.Entry<String, Boolean> entry : entityCheckBismproduct.entrySet()) {
						if (entry.getValue()) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
									new String[] { "Some BOM items are missing. Please check again. !!" });
						}
					}
					// 檢查階層是否正確
					for (JsonElement itemCheck : items) {
						String bisgname = itemCheck.getAsJsonObject().get("bisgname").getAsString();
						String bisnb = itemCheck.getAsJsonObject().get("bisnb").getAsString();// 物料號
						Integer bislevel = itemCheck.getAsJsonObject().get("bislevel").getAsInt();// 物料階層
						// 不包含customize
						if (!bisnb.equals("") && !bisnb.contains("customize") && bislevel == 0) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
									new String[] { "Please select a BOM item 'level' : " + bisgname
											+ ". Please check again. !!" });
						}
					}

				} else if (entityData.getBpmtypename().equals("配件BOM")) {
					// 配件類 ?
					JsonObject entityV = JsonParser.parseString(entityData.getBpmbisitem()).getAsJsonObject();
					JsonArray items = entityV.getAsJsonArray("items");
					for (JsonElement itemCheck : items) {
						String bisgid = itemCheck.getAsJsonObject().get("bisgid").getAsString();
						if (entityCheckBismaccessories.containsKey(bisgid)) {
							entityCheckBismaccessories.put(bisgid, false);
						}
					}
					for (Map.Entry<String, Boolean> entry : entityCheckBismaccessories.entrySet()) {
						if (entry.getValue()) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
									new String[] { "Some BOM items are missing. Please check again. !!" });
						}
					}
					// 檢查階層是否正確
					for (JsonElement itemCheck : items) {
						String bisgfname = itemCheck.getAsJsonObject().get("bisgfname").getAsString();
						String bisnb = itemCheck.getAsJsonObject().get("bisnb").getAsString();// 物料號
						Integer bislevel = itemCheck.getAsJsonObject().get("bislevel").getAsInt();// 物料階層
						// 不包含customize
						if (!bisnb.equals("") && !bisnb.contains("customize") && bislevel == 0) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
									new String[] { "Please select a BOM item 'level' : " + bisgfname
											+ ". Please check again. !!" });
						}
					}
				}

				// Step2-6.檢查匹配權限?
				BomProductManagement checkDataOne = new BomProductManagement();
				// 有可能直接覆蓋?
				if (entityData.getBpmid() == null) {
					ArrayList<BomProductManagement> managements = managementDao.findAllByCheck(entityData.getBpmnb(),
							null, null, null, null);
					if (managements.size() == 1) {
						checkDataOne = managements.get(0);
					} else {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW,
								new String[] {});
					}

				} else {
					checkDataOne = managementDao.getReferenceById(entityData.getBpmid());
				}

				Boolean throwCheck = true;
				String info = checkDataOne.getBpmnb() + " / " + checkDataOne.getBpmmodel();
				for (BomKeeper keeper : bomKeepers) {
					String bkmodel = keeper.getBkmodel();
					String bknb = keeper.getBknb();
					// 2=normal(增改刪)/1=limited(改)/0=disabled(禁用)
					if (keeper.getBktype() == 0) {
						// 沒權限?
						if (!bkmodel.equals("") && (checkDataOne.getBpmmodel().contains(bkmodel)
								|| entityData.getBpmmodel().contains(bkmodel))) {
							throwCheck = true;
							info = bkmodel;
							break;
						}
						if (!bknb.equals("")
								&& (checkDataOne.getBpmnb().contains(bknb) || entityData.getBpmnb().contains(bknb))) {
							throwCheck = true;
							info = bknb;
							break;
						}
					} else if (keeper.getBktype() == 1 || keeper.getBktype() == 2) {
						// 有權限?
						info = bknb + " / " + bkmodel;
						if (!bkmodel.equals("") && (checkDataOne.getBpmmodel().contains(bkmodel)
								&& entityData.getBpmmodel().contains(bkmodel))) {
							// 有權限
							throwCheck = false;
						} else if (!bknb.equals("")
								&& (checkDataOne.getBpmnb().contains(bknb) && entityData.getBpmnb().contains(bknb))) {
							// 有權限
							throwCheck = false;
						}
					}
				}
				// 沒權限?
				if (throwCheck) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "This account has no permissions : " + packageBean.getUserAccount() + " : "
									+ info });
				}
			}
		}

		// =======================資料整理=======================
		// 準備比對資料<BOM號,內容新舊>
		Map<String, JsonObject> newBPM = new HashMap<String, JsonObject>();
		Map<String, JsonObject> oldBPM = new HashMap<String, JsonObject>();
		Map<String, String> changeBpmnb = new HashMap<String, String>();// 修改了產品品號->則全新,舊,
		Map<String, String> oldBomNote = new HashMap<String, String>();// 舊BOM Note
		Boolean all_auto_Item = auto_Item;
		// Step3.一般資料->寫入
		entityDatas.forEach(c -> {// 要更新的資料
			if (c.getBpmid() == null) {
				// 如果是有BOM號嘗試比對看看
				ArrayList<BomProductManagement> oldDatas = managementDao.findAllByCheck(c.getBpmnb(), null, null, null,
						null);
				if (oldDatas.size() == 1) {
					c.setBpmid(oldDatas.get(0).getBpmid());
				}
			}
			if (c.getBpmid() != null) {
				String jsonStr = c.getBpmbisitem();
				// 解析 JSON-> 沒有 JSON 資料就不處理，
				if (jsonStr != null) {
					// 取出Basil vs Item 裡面項目 比對->如有類似 則去除Basic項目
					Set<String> removeBisnbSet = new HashSet<>();// 有比對到的移除清單
					JsonObject entityV = JsonParser.parseString(c.getBpmbisitem()).getAsJsonObject();
					JsonArray items = entityV.getAsJsonArray("items");
					String basic = entityV.getAsJsonArray("basic").toString();
					JsonArray filteredBasic = new JsonArray();// 過濾後的Basic
					// 結構不完整，不處理或記錄
					if (items != null && basic != null) {
						for (JsonElement itemCheck : items) {
							String bisgid = itemCheck.getAsJsonObject().get("bisgid").getAsString();
							String bisnb = itemCheck.getAsJsonObject().get("bisnb").getAsString();
							String bislevel = itemCheck.getAsJsonObject().get("bislevel").getAsString();
							// 有比對到此物料群組 && 有物料號 && 此物料放置第一階層
							if (entityCheckSame.containsKey(bisgid) && !bisnb.equals("") && bislevel.equals("1")) {
								// 取得清單
								for (BomItemSpecifications checkSame : entityCheckSame.get(bisgid)) {
									String bisnbCheck = checkSame.getBisnb();// 物料號
									// 有比對到物料?
									if (basic.contains(bisnbCheck)) {
										removeBisnbSet.add(bisnbCheck);
									}
								}
							}
						}
						// 有要移除Basic 內容?
						for (JsonElement oneBasic : entityV.getAsJsonArray("basic")) {
							String checkOneBasic = oneBasic.getAsString().split("_")[0];
							Boolean isSame = false;
							for (String removeOne : removeBisnbSet) {
								if (checkOneBasic.contains(removeOne)) {
									isSame = true;
									break;
								}
							}
							// 如果不同則OK
							if (!isSame) {
								filteredBasic.add(oneBasic.getAsString());
							}
						}
						// ===========================
						// ★ 把新的 basic 放回 entityV
						// ★ 重要：把更新後 JSON 回寫回 c
						// ===========================
						entityV.add("basic", filteredBasic);
						c.setBpmbisitem(entityV.toString());
					}
				}

				BomProductManagement oldData = managementDao.getReferenceById(c.getBpmid());
				// 內容不同?->比對差異->登記異動紀錄->待發信件通知
				if (!oldData.getBpmbisitem().equals(c.getBpmbisitem())) {
					newBPM.put(c.getBpmnb() + "_" + c.getBpmmodel(),
							JsonParser.parseString(c.getBpmbisitem()).getAsJsonObject());
					oldBPM.put(c.getBpmnb() + "_" + c.getBpmmodel(),
							JsonParser.parseString(oldData.getBpmbisitem()).getAsJsonObject());
					// 登記舊的備註資訊 之後比對使用
					oldBomNote.put(c.getBpmnb() + "_" + c.getBpmmodel(), oldData.getSysnote());
				} else if (!oldData.getSysnote().equals(c.getSysnote())) {
					// 不同才通知
					mailSend(oldData, c);
				}

				// 可能是新的 -> <新的,舊的>?
				if (!oldData.getBpmnb().equals(c.getBpmnb())) {
					changeBpmnb.put(c.getBpmnb(), oldData.getBpmnb());
				}

				//
				oldData.setSysmdate(new Date());
				oldData.setSysmuser(packageBean.getUserAccount());
				oldData.setSysodate(new Date());
				oldData.setSysouser(packageBean.getUserAccount());
				oldData.setSysheader(false);
				oldData.setSyssort(0);
				oldData.setSysstatus(0);
				oldData.setSysnote(c.getSysnote());
				oldData.setBpmbisitem(c.getBpmbisitem());
				oldData.setBpmbpsnv(c.getBpmbpsnv());
				oldData.setBpmmodel(c.getBpmmodel().replaceAll("\\s|/", ""));
				oldData.setBpmnb(c.getBpmnb().replaceAll("\\s|/", ""));
				oldData.setBpmtype(c.getBpmtype());
				oldData.setBpmtypename(c.getBpmtypename().replaceAll("\\s|/", ""));
				saveDatasUpdate.add(oldData);
			}
		});
		// Step4.彙整更新的資料->物料排序->標記更新 or 移除
		ArrayList<BomHistory> changeBom = new ArrayList<BomHistory>();
		Map<String, HashMap<String, String>> oldBom = new HashMap<String, HashMap<String, String>>();// 舊BOM
		Map<String, HashMap<String, String>> newBom = new HashMap<String, HashMap<String, String>>();// 新BOM

		// 舊資料整理->一個BOM 內多個項目
		oldBPM.forEach((oldK, oldV) -> {
			// 物料號_數量_製成別(items>basic 如果有一樣的物料的話)
			HashMap<String, String> oldVs = new HashMap<String, String>();
			JsonArray items = oldV.getAsJsonArray("items");
			JsonArray basic = oldV.getAsJsonArray("basic");
			items.forEach(i -> {
				JsonObject obj = i.getAsJsonObject();
				String bisnb = obj.getAsJsonPrimitive("bisnb").getAsString();
				String bisqty = obj.getAsJsonPrimitive("bisqty").getAsString();
				String bisprocess = obj.getAsJsonPrimitive("bisprocess").getAsString();
				// 防呆
				String bislevel = Optional.ofNullable(obj.get("bislevel")).filter(e -> !e.isJsonNull())
						.map(JsonElement::getAsString).orElse("0");
				if (!bisnb.equals("") && !oldVs.containsKey(bisnb)) {
					// 若不重複則加入
					oldVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
				}
			});
			basic.forEach(i -> {
				String bisnb_bisqty_bisprocess = i.getAsString();
				// 排除項目是空直
				if (!bisnb_bisqty_bisprocess.equals("")) {
					String[] parts = bisnb_bisqty_bisprocess.split("_");
					String bisnb = parts[0];
					String bisqty = parts[1];
					String bisprocess = parts[2];
					// 防呆
					String bislevel = parts.length > 3 ? parts[3] : "0";
					if (!bisnb.equals("") && !oldVs.containsKey(bisnb)) {
						// 若不重複則加入
						oldVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
					}
				}
			});
			// 新的BOM_型號 , 物料_數量_製成別
			oldBom.put(oldK, oldVs);
		});
		// 新料整理->一個BOM 內多個項目
		newBPM.forEach((newK, newV) -> {
			// 物料號_數量_製成別(items>basic 如果有一樣的物料的話)
			HashMap<String, String> newVs = new HashMap<String, String>();
			JsonArray items = newV.getAsJsonArray("items");
			JsonArray basic = newV.getAsJsonArray("basic");
			items.forEach(i -> {
				JsonObject obj = i.getAsJsonObject();
				String bisnb = obj.getAsJsonPrimitive("bisnb").getAsString();
				String bisqty = obj.getAsJsonPrimitive("bisqty").getAsString();
				String bisprocess = obj.getAsJsonPrimitive("bisprocess").getAsString();
				// 防呆
				String bislevel = Optional.ofNullable(obj.get("bislevel")).filter(e -> !e.isJsonNull())
						.map(JsonElement::getAsString).orElse("0");
				if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
					// 若不重複則加入
					newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
				}
			});
			basic.forEach(i -> {
				String bisnb_bisqty_bisprocess = i.getAsString();
				// 排除項目是空直
				if (!bisnb_bisqty_bisprocess.equals("")) {
					String[] parts = bisnb_bisqty_bisprocess.split("_");
					String bisnb = parts[0];
					String bisqty = parts[1];
					String bisprocess = parts[2];
					// 防呆
					String bislevel = parts.length > 3 ? parts[3] : "0";
					if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
						// 若不重複則加入
						newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
					}
				}
			});
			// 新的BOM_型號 , 物料_數量_製成別
			newBom.put(newK, newVs);
		});

		// 資料整理入 BomHistory 內
		newBom.forEach((newBomK, newBomV) -> {
			// 匹配的舊BOM
			HashMap<String, String> oldBomv = oldBom.get(newBomK);
			// 每個新BOM的Item
			newBomV.forEach((newItemK, newItemV) -> {
				BomHistory bomHistory = new BomHistory();
				bomHistory.setBhnb(newBomK.split("_")[0]);
				bomHistory.setBhmodel(newBomK.split("_")[1]);
				bomHistory.setBhatype("");
				// 舊的NOTE
				if (oldBomNote.containsKey(newBomK)) {
					bomHistory.setSysnote(oldBomNote.get(newBomK));
				}
				// 沒有變化(物料)
				if (oldBomv.containsKey(newItemK)) {
					bomHistory.setBhpnb(newItemV.split("_")[0].trim());
					bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
					bomHistory.setBhpprocess(newItemV.split("_")[2].trim());
					bomHistory.setBhlevel(Integer.parseInt(newItemV.split("_")[3]));

					// 可能更新?數量?製成?
					String oldItemV = oldBomv.get(newItemK);
					if (!newItemV.split("_")[1].equals(oldItemV.split("_")[1])
							|| !newItemV.split("_")[2].equals(oldItemV.split("_")[2])) {
						bomHistory.setBhatype("Update");
						// 舊的資料標記
						BomHistory bomHistoryOld = new BomHistory();
						bomHistoryOld.setBhnb(newBomK.split("_")[0]);
						bomHistoryOld.setBhmodel(newBomK.split("_")[1]);
						bomHistoryOld.setBhpnb(oldItemV.split("_")[0]);
						bomHistoryOld.setBhpqty(Integer.parseInt(oldItemV.split("_")[1]));
						bomHistoryOld.setBhpprocess(oldItemV.split("_")[2].trim());
						bomHistoryOld.setBhlevel(Integer.parseInt(oldItemV.split("_")[3]));
						bomHistoryOld.setBhatype("Old");
						changeBom.add(bomHistoryOld);
					}
					changeBom.add(bomHistory);
					// 複寫標記null
					oldBomv.put(newItemK, null);
				} else {
					// 沒比對到?新的?
					bomHistory.setBhpnb(newItemV.split("_")[0].trim());
					bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
					bomHistory.setBhpprocess(newItemV.split("_")[2].trim());
					bomHistory.setBhlevel(Integer.parseInt(newItemV.split("_")[3]));
					bomHistory.setBhatype("New");
					changeBom.add(bomHistory);
				}
			});

			// 舊資料
			oldBomv.forEach((oldItemK, oldItemV) -> {
				// 表示被移除了
				if (oldItemV != null) {
					BomHistory bomHistoryRemove = new BomHistory();
					bomHistoryRemove.setBhnb(newBomK.split("_")[0]);
					bomHistoryRemove.setBhmodel(newBomK.split("_")[1]);
					bomHistoryRemove.setBhpnb(oldItemV.split("_")[0].trim());
					bomHistoryRemove.setBhpqty(Integer.parseInt(oldItemV.split("_")[1]));
					bomHistoryRemove.setBhpprocess(oldItemV.split("_")[2]);
					bomHistoryRemove.setBhlevel(Integer.parseInt(oldItemV.split("_")[3]));
					bomHistoryRemove.setBhatype("Delete");
					// 舊的NOTE
					if (oldBomNote.containsKey(newBomK)) {
						bomHistoryRemove.setSysnote(oldBomNote.get(newBomK));
					}
					changeBom.add(bomHistoryRemove);

				}
			});
			// 檢查是否為新BOM(因為有改產品BOM號則為全新)
			Iterator<BomHistory> it = changeBom.iterator();// it將會連動changeBom
			while (it.hasNext()) {
				BomHistory b = it.next();
				// 僅處理「BOM號有變更的品號」項目
				if (changeBpmnb.containsKey(b.getBhnb())) {
					String oldType = b.getBhatype();
					if (oldType == null || oldType.equals("New") || oldType.equals("Update") || oldType.equals("")) {
						// 舊值為 空/ New/ Update -> 視為全新
						b.setBhatype("All New");
					} else {
						// 其他舊值 -> 移除
						it.remove(); // 只能用 iterator.remove() 才不會 ConcurrentModificationException
					}
				}
			}
		});
		// 需要檢查是否連動?
		Map<String, BomItemSpecifications> entityMapBIS = new HashMap<String, BomItemSpecifications>();// 物料號
		// 哪個 90BOM<哪個選項>
		Map<String, HashMap<String, BomItemSpecifications>> entityMatchBIS = new HashMap<String, HashMap<String, BomItemSpecifications>>();
		if (all_auto_Item) {
			entityBIS.forEach(bis -> {
				entityMapBIS.put(bis.getBisnb(), bis);
			});
		}

		// 統一時間 不然會導致BOM被切割
		Date sameTime = new Date();
		Boolean setBhinproduction = in_Production;
		changeBom.forEach(his -> {
			// 紀錄時間
			his.setSyscdate(sameTime);
			his.setBhinproduction(setBhinproduction);

			// 需要檢查是否連動?
			if (all_auto_Item && (his.getBhpnb().startsWith("92") || his.getBhpnb().startsWith("81"))) {
				ArrayList<BasicBomIngredients> entityOneBBI = ingredientsDao.findFlattenedBomLevel(his.getBhpnb(),
						null);
				entityOneBBI.forEach(kp -> {
					// 如果有對應到標記90BOM + 此項目是 新的 or 更新
					if (entityMapBIS.containsKey(kp.getBbiisn())
							&& (his.getBhatype().equals("New") || his.getBhatype().equals("Update"))) {
						HashMap<String, BomItemSpecifications> kpMap = new HashMap<String, BomItemSpecifications>();
						// 已經有了?
						if (entityMatchBIS.containsKey(his.getBhnb())) {
							kpMap = entityMatchBIS.get(his.getBhnb());
						}
						kpMap.put(entityMapBIS.get(kp.getBbiisn()).getBisgid() + "", entityMapBIS.get(kp.getBbiisn()));
						entityMatchBIS.put(his.getBhnb(), kpMap);
					}
				});
			}
		});

		// 修正其他Key Part
		if (all_auto_Item) {
			saveDatasUpdate.forEach(bpmOne -> {
				// Gson：若 JSON 內含 HTML 內容，建議用 disableHtmlEscaping 避免被轉義
				Gson gson = new GsonBuilder().disableHtmlEscaping().create();
				// 比對修正資料-90BOM
				if (entityMatchBIS.containsKey(bpmOne.getBpmnb())) {
					// === 1) 解析 JSON ===
					String raw = bpmOne.getBpmbisitem();
					JsonObject root = JsonParser.parseString(raw).getAsJsonObject();
					JsonArray items = root.getAsJsonArray("items");// BOM 的物件
					HashMap<String, BomItemSpecifications> entityMatchKe = entityMatchBIS.get(bpmOne.getBpmnb());// 要匹配的
																													// 正規化清單
					// BOM內容-每個Item
					for (JsonElement el : items) {
						if (!el.isJsonObject()) {
							// 非物件（例如字串/數字），不處理
							continue;
						}
						//
						boolean changed = false; // 本筆是否有任何改動
						JsonObject iiObj = el.getAsJsonObject();
						// 取三個關鍵欄位（防 null）
						// String bisgname = getAsString(iiObj, "bisgname");// 項目組名稱
						String bisgid = getAsString(iiObj, "bisgid");// 項目組ID
						// String bisid = getAsString(iiObj, "bisid");// 項目ID_V
						String bisnb = getAsString(iiObj, "bisnb");// 項目物料號_V
						// String bisname = getAsString(iiObj, "bisname");// 物料品名_V
						String bisqty = getAsString(iiObj, "bisqty");// 物料數量_V
						// String bisfname = getAsString(iiObj, "bisfname");// 格式化_V
						//
						if (bisgid == null) {
							// 群組不存在 → 無法匹配 → 略過
							continue;
						}
						// 排除customize
						if (bisnb == null || "customize".equals(bisnb)) {
							continue;
						}
						// 如果沒配對到
						if (!entityMatchKe.containsKey(bisgid)) {
							continue;
						}
						// === 3) 僅在不同時寫入（避免多餘 UPDATE） ===
						// 你目前要求的三個欄位，後續可自由增減
						// 取得KeyPart
						BomItemSpecifications bisOne = entityMatchKe.get(bisgid);
						//
						changed |= addOrOverwriteIfDifferent(iiObj, "bisid", bisOne.getBisid() + ""); // ID
						changed |= addOrOverwriteIfDifferent(iiObj, "bisnb", bisOne.getBisnb()); // 物料號"
						changed |= addOrOverwriteIfDifferent(iiObj, "bisname", bisOne.getBisname()); // 物料名稱"
						changed |= addOrOverwriteIfDifferent(iiObj, "bisqty", bisqty); // 物料數量"
						changed |= addOrOverwriteIfDifferent(iiObj, "bisgfname", bisOne.getBisgfname()); // 格式:"產品 版次"
						changed |= addOrOverwriteIfDifferent(iiObj, "bisgname", bisOne.getBisgname()); // 項目組名稱:"PCBA"
						changed |= addOrOverwriteIfDifferent(iiObj, "bisfname", bisOne.getBisfname()); // 項目格式化:"[\"DT340TR
																										// MB...\",\"1.2\"]
						// === 4) 有改動才寫回欄位 ===
						if (changed) {
							String updatedJson = gson.toJson(root); // 將 root 轉回字串
							bpmOne.setBpmbisitem(updatedJson);
						}
					}
				}
			});
		}

		// 比對同一張BOM->的物料
		historyDao.saveAll(changeBom);

		// =======================資料儲存=======================
		// 資料Data
		managementDao.saveAll(saveDatasUpdate);
		packageBean.setCallBackValue("BPM");
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductManagement> entityDatas = new ArrayList<>();
		ArrayList<BomProductManagement> entitySave = new ArrayList<>();
		ArrayList<BomKeeper> bomKeepers = bomKeeperDao.findAllBySearch(packageBean.getUserAccount(), null, null, null);
		ArrayList<BomItemSpecifications> entityBIS = specificationsDao.findAllBySearch(null, null, null, null);// 選擇清單項目
		Map<String, ArrayList<BomItemSpecifications>> entityCheckSame = new HashMap<String, ArrayList<BomItemSpecifications>>();// 為了:去除同類型物料()
		Map<String, Boolean> entityCheckBismproduct = new HashMap<String, Boolean>();// GID 必填?成品
		Map<String, Boolean> entityCheckBismaccessories = new HashMap<String, Boolean>();// GID 必填?配件

		entityBIS.forEach(b -> {
			// 有必填?
			System.out.println(b.getBisgid() + " 成品:" + b.getBismproduct() + " 配件:" + b.getBismaccessories());
			// GID 必填?成品
			if (b.getBismproduct() && !entityCheckBismproduct.containsKey(b.getBisgid() + "")) {
				entityCheckBismproduct.put(b.getBisgid() + "", true);
			}
			// GID 必填?配件
			if (b.getBismaccessories() && !entityCheckBismaccessories.containsKey(b.getBisgid() + "")) {
				entityCheckBismaccessories.put(b.getBisgid() + "", true);
			}
			// 為了:去除同類型資料item->basic
			ArrayList<BomItemSpecifications> newCheckSame = new ArrayList<BomItemSpecifications>();
			if (entityCheckSame.containsKey(b.getBisgid() + "")) {
				newCheckSame = entityCheckSame.get(b.getBisgid() + "");
				newCheckSame.add(b);
				entityCheckSame.put(b.getBisgid() + "", newCheckSame);
			} else {
				newCheckSame.add(b);
				entityCheckSame.put(b.getBisgid() + "", newCheckSame);
			}
		});
		// 連續號檢查?(末3碼)SN_check
		JsonObject sn_checkJson = new JsonObject();
		sn_checkJson = JsonParser.parseString(packageBean.getCallBackValue()).getAsJsonObject();
		Boolean sn_check = sn_checkJson.get("SN_check").getAsBoolean();

		// =======================資料檢查=======================
		// 一般BOM規格
		if (packageBean.getOtherSet().equals("BPM")) {
			if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
				// Step1.資料轉譯(一般)
				entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
						new TypeReference<ArrayList<BomProductManagement>>() {
						});
				// Step1-1.負責人配置?
				if (bomKeepers.size() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "This account has no permissions :" + packageBean.getUserAccount() });
				}
				// Step2-1.資料檢查
				for (BomProductManagement entityData : entityDatas) {
					// 檢查-名稱重複(有資料 && 不是同一筆資料)
					ArrayList<BomProductManagement> checkDatas = managementDao.findAllByCheck(entityData.getBpmnb(),
							null, null, null, null);
					if (checkDatas.size() != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getBpmnb() });
					}
					// Step2-2.資料檢查-缺少值?
					if (entityData.getBpmmodel() == null || entityData.getBpmmodel().equals("") || // 型號
							entityData.getBpmnb() == null || entityData.getBpmnb().equals("") || // 成品號
							entityData.getBpmtype() == null || entityData.getBpmtype() < 0 || // 成品類
							entityData.getBpmbpsnv() == null || entityData.getBpmbpsnv().equals("") || // 參數
							entityData.getBpmbisitem() == null || entityData.getBpmbisitem().equals("")) {// 物料匹配規格
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
								new String[] { entityData.getBpmmodel() });
					}
					// Step2-2.備註檢查是否一樣 ?
					ArrayList<BomProductManagement> checkDatas2 = managementDao.findAllByCheck(null, null, null,
							entityData.getSysnote(), null);
					if (checkDatas2.size() > 0) {
						for (BomProductManagement one : checkDatas2) {
							// 如果跟本產品不同在警報
							if (!one.getBpmnb().equals(entityData.getBpmnb())) {
								throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
										new String[] { ":" + one.getBpmnb() + " 備註 : " + entityData.getSysnote() });
							}
						}
					}

					// Step2-3.檢查BOM所有項目 內容 是否一樣 ?
					ArrayList<BomProductManagement> checkDatas3 = managementDao.findAllByCheck(null, null, null, null,
							entityData.getBpmbisitem());
					if (checkDatas3.size() > 0) {
						for (BomProductManagement one : checkDatas3) {
							// 如果跟本產品不同在警報
							if (!one.getBpmnb().equals(entityData.getBpmnb())) {
								throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
										new String[] { ":" + one.getBpmnb() + " 規格內容 : " + checkDatas3.get(0).getBpmnb()
												+ " : " + entityData.getBpmnb() });
							}
						}
					}
					// Step2-4.檢查BOM所有項目 是否有重複 ?
					Map<String, Boolean> checkGorupItems = new HashMap<String, Boolean>();
					JsonObject bpmbisitem = new JsonObject();
					JsonArray bpmbisitems = new JsonArray();
					bpmbisitem = JsonParser.parseString(entityData.getBpmbisitem()).getAsJsonObject();
					bpmbisitems = bpmbisitem.get("items").getAsJsonArray();
					for (JsonElement bomKeeper : bpmbisitems) {
						String key = bomKeeper.getAsJsonObject().get("bisgname").getAsString();
						if (checkGorupItems.containsKey(key)) {
							// 有重複跳出訊號
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
									new String[] { entityData.getBpmnb() + " : " + key });
						}
						checkGorupItems.put(key, true);
					}

					// Step2-5.檢查 項目是否缺少
					if (entityData.getBpmtypename().equals("產品BOM")) {
						// 成品類 ?
						JsonObject entityV = JsonParser.parseString(entityData.getBpmbisitem()).getAsJsonObject();
						JsonArray items = entityV.getAsJsonArray("items");
						for (JsonElement itemCheck : items) {
							String bisgid = itemCheck.getAsJsonObject().get("bisgid").getAsString();
							if (entityCheckBismproduct.containsKey(bisgid)) {
								entityCheckBismproduct.put(bisgid, false);
							}
						}
						for (Map.Entry<String, Boolean> entry : entityCheckBismproduct.entrySet()) {
							if (entry.getValue()) {
								throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
										new String[] { "Some BOM items are missing. Please check again. !!" });
							}
						}
					} else if (entityData.getBpmtypename().equals("配件BOM")) {
						// 配件類 ?
						JsonObject entityV = JsonParser.parseString(entityData.getBpmbisitem()).getAsJsonObject();
						JsonArray items = entityV.getAsJsonArray("items");
						for (JsonElement itemCheck : items) {
							String bisgid = itemCheck.getAsJsonObject().get("bisgid").getAsString();
							if (entityCheckBismaccessories.containsKey(bisgid)) {
								entityCheckBismaccessories.put(bisgid, false);
							}
						}
						for (Map.Entry<String, Boolean> entry : entityCheckBismaccessories.entrySet()) {
							if (entry.getValue()) {
								throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
										new String[] { "Some BOM items are missing. Please check again. !!" });
							}
						}
					}
					// Step2-6.檢查匹配權限?
					Boolean throwCheck = true;
					String info = entityData.getBpmnb() + " / " + entityData.getBpmmodel();
					for (BomKeeper keeper : bomKeepers) {
						String bkmodel = keeper.getBkmodel();
						String bknb = keeper.getBknb();
						// 2=normal(增改刪)/1=limited(改)/0=disabled(禁用)
						if (keeper.getBktype() == 0 || keeper.getBktype() == 1) {
							// 沒權限?
							if (!bkmodel.equals("") && entityData.getBpmmodel().contains(bkmodel)) {
								throwCheck = true;
								info = bkmodel;
								break;
							}
							if (!bknb.equals("") && entityData.getBpmnb().contains(bknb)) {
								throwCheck = true;
								info = bknb;
								break;
							}
						} else {
							// 有權限?
							info = bknb + " / " + bkmodel;
							if (!bknb.equals("") && entityData.getBpmnb().contains(bknb)) {
								// 有權限
								throwCheck = false;
								break;
							} else if (!bkmodel.equals("") && entityData.getBpmmodel().contains(bkmodel)) {
								// 有權限
								throwCheck = false;
								break;
							}
						}
					}

					if (throwCheck) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
								new String[] { "This account has no permissions : " + packageBean.getUserAccount()
										+ " : " + info });
					}

					// 檢查連續碼
					if (sn_check) {
						String bpmnbNew = entityData.getBpmnb();
						// 先確保字串長度 >= 3
						if (bpmnbNew != null && bpmnbNew.length() >= 3) {
							// 末3碼
							String bpmnbNewLast3 = bpmnbNew.substring(bpmnbNew.length() - 3);
							// 其餘碼
							String bpmnbNewPrefix = bpmnbNew.substring(0, bpmnbNew.length() - 3);
							//
							List<Order> orders = new ArrayList<>();
							orders.add(new Order(Direction.DESC, "bpmnb"));// BOM號
							PageRequest pageable = PageRequest.of(0, 999, Sort.by(orders));
							ArrayList<BomProductManagement> checkDataSn = managementDao.findAllBySearch(bpmnbNewPrefix,
									null, null, null, null, pageable);
							if (checkDataSn.size() > 0) {
								// 檢查連序號
								String bpmnbOld = checkDataSn.get(0).getBpmnb();
								// 末3碼
								String bpmnbOldLast3 = bpmnbOld.substring(bpmnbOld.length() - 3);
								// 其餘碼
								// String bpmnbOldPrefix = bpmnbOld.substring(0, bpmnbOld.length() - 3);
								String expectedNext = nextSN(bpmnbOldLast3);

								if (bpmnbNewLast3.equals(expectedNext)) {
									System.out.println("✅ 序號連貫");
								} else {
									System.out.println("❌ 序號不連貫，上一碼是 " + bpmnbOldLast3 + "，理應是 " + expectedNext
											+ "，但收到 " + bpmnbNewLast3);
									throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1008,
											Lan.zh_TW, new String[] { "Product serial numbers are not continuous:"
													+ bpmnbOld + "->" + entityData.getBpmnb() });
								}
							}
						} else {
							System.out.println("字串長度不足3碼");
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1008, Lan.zh_TW,
									new String[] {
											"The string length is less than 3 characters:" + entityData.getBpmnb() });
						}
					}

				}
			}
			// =======================資料整理=======================
			// 準備比對資料<BOM號,內容新舊>
			Map<String, JsonObject> newBPM = new HashMap<String, JsonObject>();
			for (BomProductManagement x : entityDatas) {
				// 登記異動紀錄->待發信件通知
				newBPM.put(x.getBpmnb() + "_" + x.getBpmmodel(),
						JsonParser.parseString(x.getBpmbisitem()).getAsJsonObject());

				String jsonStr = x.getBpmbisitem();
				// 解析 JSON-> 沒有 JSON 資料就不處理，
				if (jsonStr != null) {
					// 取出Basil vs Item 裡面項目 比對->如有類似 則去除Basic項目
					Set<String> removeBisnbSet = new HashSet<>();// 有比對到的移除清單
					JsonObject entityV = JsonParser.parseString(x.getBpmbisitem()).getAsJsonObject();
					JsonArray items = entityV.getAsJsonArray("items");
					String basic = entityV.getAsJsonArray("basic").toString();
					JsonArray filteredBasic = new JsonArray();// 過濾後的Basic
					// 結構不完整，不處理或記錄
					if (items != null && basic != null) {
						for (JsonElement itemCheck : items) {
							String bisgid = itemCheck.getAsJsonObject().get("bisgid").getAsString();
							String bisnb = itemCheck.getAsJsonObject().get("bisnb").getAsString();
							String bislevel = itemCheck.getAsJsonObject().get("bislevel").getAsString();
							// 有比對到此物料群組 && 有物料號 && 此物料放置第一階層
							if (entityCheckSame.containsKey(bisgid) && !bisnb.equals("") && bislevel.equals("1")) {
								// 取得清單
								for (BomItemSpecifications checkSame : entityCheckSame.get(bisgid)) {
									String bisnbCheck = checkSame.getBisnb();// 物料號
									// 有比對到物料?
									if (basic.contains(bisnbCheck)) {
										removeBisnbSet.add(bisnbCheck);
									}
								}
							}
						}
						// 有要移除Basic 內容?
						for (JsonElement oneBasic : entityV.getAsJsonArray("basic")) {
							String checkOneBasic = oneBasic.getAsString().split("_")[0];
							Boolean isSame = false;
							for (String removeOne : removeBisnbSet) {
								if (checkOneBasic.contains(removeOne)) {
									isSame = true;
									break;
								}
							}
							// 如果不同則OK
							if (!isSame) {
								filteredBasic.add(oneBasic.getAsString());
							}
						}
						// ===========================
						// ★ 把新的 basic 放回 entityV
						// ★ 重要：把更新後 JSON 回寫回 c
						// ===========================
						entityV.add("basic", filteredBasic);
						x.setBpmbisitem(entityV.toString());
					}
				}

				// 新增
				x.setBpmid(null);
				x.setSysmdate(new Date());
				x.setSysmuser(packageBean.getUserAccount());
				x.setSysodate(new Date());
				x.setSysouser(packageBean.getUserAccount());
				x.setSyscdate(new Date());
				x.setSyscuser(packageBean.getUserAccount());
				x.setSysheader(false);
				x.setSyssort(0);
				x.setSysstatus(0);
				x.setBpmmodel(x.getBpmmodel().replaceAll("\\s|/", ""));
				x.setBpmnb(x.getBpmnb().replaceAll("\\s|/", ""));
				x.setBpmtypename(x.getBpmtypename().replaceAll("\\s|/", ""));
				entitySave.add(x);
			}
			// 異動資料
			ArrayList<BomHistory> changeBom = new ArrayList<BomHistory>();
			Map<String, HashMap<String, String>> newBom = new HashMap<String, HashMap<String, String>>();
			// 新料整理->一個BOM 內多個項目
			newBPM.forEach((newK, newV) -> {
				// 物料號_數量_製成別(items>basic 如果有一樣的物料的話)
				HashMap<String, String> newVs = new HashMap<String, String>();
				JsonArray items = newV.getAsJsonArray("items");
				JsonArray basic = newV.getAsJsonArray("basic");
				items.forEach(i -> {
					JsonObject obj = i.getAsJsonObject();
					String bisnb = obj.getAsJsonPrimitive("bisnb").getAsString();
					String bisqty = obj.getAsJsonPrimitive("bisqty").getAsString();
					String bisprocess = obj.getAsJsonPrimitive("bisprocess").getAsString();
					// 防呆
					String bislevel = Optional.ofNullable(obj.get("bislevel")).filter(e -> !e.isJsonNull())
							.map(JsonElement::getAsString).orElse("0");
					if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
						// 若不重複則加入
						newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
					}
				});
				basic.forEach(i -> {
					String bisnb_bisqty_bisprocess = i.getAsString();
					// 排除項目是空直
					if (!bisnb_bisqty_bisprocess.equals("")) {
						String[] parts = bisnb_bisqty_bisprocess.split("_");
						String bisnb = parts[0];
						String bisqty = parts[1];
						String bisprocess = parts[2];
						// 防呆
						String bislevel = parts.length > 3 ? parts[3] : "0";
						if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
							// 若不重複則加入
							newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
						}
					}
				});
				// 新的BOM_型號 , 物料_數量_製成別
				newBom.put(newK, newVs);
			});
			// 資料整理入 BomHistory 內
			newBom.forEach((newBomK, newBomV) -> {
				// 每個新BOM的Item
				newBomV.forEach((newItemK, newItemV) -> {
					// 新的?
					BomHistory bomHistory = new BomHistory();
					bomHistory.setBhnb(newBomK.split("_")[0]);
					bomHistory.setBhmodel(newBomK.split("_")[1]);
					bomHistory.setBhpnb(newItemV.split("_")[0].trim());
					bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
					bomHistory.setBhpprocess(newItemV.split("_")[2].trim());
					bomHistory.setBhlevel(Integer.parseInt(newItemV.split("_")[3]));
					bomHistory.setBhatype("All New");
					changeBom.add(bomHistory);
				});

				// 比對同一張BOM->的物料
			});
			// =======================資料儲存=======================
			// 統一時間 不然會導致BOM被切割
			Date sameTime = new Date();
			changeBom.forEach(his -> his.setSyscdate(sameTime));
			historyDao.saveAll(changeBom);
			// 資料Data
			managementDao.saveAll(entitySave);
			packageBean.setCallBackValue("BPM");
		}

		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductManagement> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductManagement>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BomProductManagement> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBpmid() != null) {
				BomProductManagement entityDataOld = managementDao.findById(x.getBpmid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		managementDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomKeeper> bomKeepers = bomKeeperDao.findAllBySearch(packageBean.getUserAccount(), null, null, null);
		ArrayList<BomProductManagement> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductManagement>>() {
					});

			// Step2.資料檢查
			for (BomProductManagement entityData : entityDatas) {
				// Step2-3.檢查匹配權限?
				BomProductManagement checkDataOne = managementDao.getReferenceById(entityData.getBpmid());
				Boolean throwCheck = true;
				String info = checkDataOne.getBpmnb() + " / " + checkDataOne.getBpmmodel();
				for (BomKeeper keeper : bomKeepers) {
					String bkmodel = keeper.getBkmodel();
					String bknb = keeper.getBknb();
					// 2=normal(增改刪)/1=limited(改)/0=disabled(禁用)
					if (keeper.getBktype() == 0) {
						// 沒權限?
						if (!bkmodel.equals("") && (checkDataOne.getBpmmodel().contains(bkmodel)
								|| entityData.getBpmmodel().contains(bkmodel))) {
							throwCheck = true;
							info = bkmodel;
							break;
						}
						if (!bknb.equals("")
								&& (checkDataOne.getBpmnb().contains(bknb) || entityData.getBpmnb().contains(bknb))) {
							throwCheck = true;
							info = bknb;
							break;
						}
					} else if (keeper.getBktype() == 2) {
						// 有權限?
						info = bknb + " / " + bkmodel;
						if (!bkmodel.equals("") && (checkDataOne.getBpmmodel().contains(bkmodel)
								&& entityData.getBpmmodel().contains(bkmodel))) {
							// 有權限
							throwCheck = false;
						} else if (!bknb.equals("")
								&& (checkDataOne.getBpmnb().contains(bknb) && entityData.getBpmnb().contains(bknb))) {
							// 有權限
							throwCheck = false;
						}
					}
				}
				// 沒權限?
				if (throwCheck) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "This account has no permissions : " + packageBean.getUserAccount() + " : "
									+ info });
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BomProductManagement> saveDatas = new ArrayList<>();
		// 準備比對資料<BOM號,內容新舊>
		Map<String, JsonObject> newBPM = new HashMap<String, JsonObject>();
		Map<String, String> oldBomNote = new HashMap<String, String>();// 舊BOM Note
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBpmid() != null) {
				// 登記異動紀錄->待發信件通知
				newBPM.put(x.getBpmnb() + "_" + x.getBpmmodel(),
						JsonParser.parseString(x.getBpmbisitem()).getAsJsonObject());
				// 登記舊的備註資訊 之後比對使用
				oldBomNote.put(x.getBpmnb() + "_" + x.getBpmmodel(), x.getSysnote());
				//
				BomProductManagement entityDataOld = managementDao.getReferenceById(x.getBpmid());
				saveDatas.add(entityDataOld);
			}
		});
		// 異動資料
		ArrayList<BomHistory> changeBom = new ArrayList<BomHistory>();
		Map<String, HashMap<String, String>> newBom = new HashMap<String, HashMap<String, String>>();
		// 新料整理->一個BOM 內多個項目
		newBPM.forEach((newK, newV) -> {
			// 物料號_數量_製成別(items>basic 如果有一樣的物料的話)
			HashMap<String, String> newVs = new HashMap<String, String>();
			JsonArray items = newV.getAsJsonArray("items");
			JsonArray basic = newV.getAsJsonArray("basic");
			items.forEach(i -> {
				JsonObject obj = i.getAsJsonObject();
				String bisnb = obj.getAsJsonPrimitive("bisnb").getAsString();
				String bisqty = obj.getAsJsonPrimitive("bisqty").getAsString();
				String bisprocess = obj.getAsJsonPrimitive("bisprocess").getAsString();
				// 防呆
				String bislevel = Optional.ofNullable(obj.get("bislevel")).filter(e -> !e.isJsonNull())
						.map(JsonElement::getAsString).orElse("0");
				if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
					// 若不重複則加入
					newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
				}
			});
			basic.forEach(i -> {
				String bisnb_bisqty_bisprocess = i.getAsString();
				// 排除項目是空直
				if (!bisnb_bisqty_bisprocess.equals("")) {
					String[] parts = bisnb_bisqty_bisprocess.split("_");
					String bisnb = parts[0];
					String bisqty = parts[1];
					String bisprocess = parts[2];
					// 防呆
					String bislevel = parts.length > 3 ? parts[3] : "0";
					if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
						// 若不重複則加入
						newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
					}
				}
			});
			// 新的BOM_型號 , 物料_數量_製成別
			newBom.put(newK, newVs);
		});

		// 資料整理入 BomHistory 內
		newBom.forEach((newBomK, newBomV) -> {
			// 每個新BOM的Item
			newBomV.forEach((newItemK, newItemV) -> {
				// 新的?
				BomHistory bomHistory = new BomHistory();
				// 舊的NOTE
				if (oldBomNote.containsKey(newBomK)) {
					bomHistory.setSysnote(oldBomNote.get(newBomK));
				}
				bomHistory.setBhnb(newBomK.split("_")[0]);
				bomHistory.setBhmodel(newBomK.split("_")[1]);
				bomHistory.setBhpnb(newItemV.split("_")[0].trim());
				bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
				bomHistory.setBhpprocess(newItemV.split("_")[2].trim());
				bomHistory.setBhlevel(Integer.parseInt(newItemV.split("_")[3]));
				bomHistory.setBhatype("All Delete");
				changeBom.add(bomHistory);
			});
			// 比對同一張BOM->的物料
		});
		// 統一時間 不然會導致BOM被切割
		Date sameTime = new Date();
		changeBom.forEach(his -> his.setSyscdate(sameTime));
		// =======================資料儲存=======================
		historyDao.saveAll(changeBom);
		// 資料Data
		managementDao.deleteAll(saveDatas);
		packageBean.setCallBackValue("BPM");
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<BomItemSpecifications> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM bom_product_management e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("bpm", "bpm_");

			cellName = cellName.replace("bpm_bisitem", "bpm_bis_item");
			cellName = cellName.replace("bpm_bpsnv", "bpm_bps_nv");
			cellName = cellName.replace("bpm_typename", "bpm_type_name");

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
		nativeQuery += " order by e.bpm_model asc,e.bpm_nb asc";
		nativeQuery += " LIMIT 2500 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, BomItemSpecifications.class);
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

	// 先定義合法字元集：A-Z + 0-9
	private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	// 連續號檢測
	public static String nextSN(String sn) {
		char[] chars = sn.toCharArray();
		int i = chars.length - 1;

		while (i >= 0) {
			int index = CHARSET.indexOf(chars[i]);
			if (index < 0) {
				throw new IllegalArgumentException("不合法字元: " + chars[i]);
			}

			if (index == CHARSET.length() - 1) {
				// 進位 (Z → A, 9 → A)
				chars[i] = CHARSET.charAt(0);
				i--;
			} else {
				// 當前位數 +1
				chars[i] = CHARSET.charAt(index + 1);
				return new String(chars);
			}
		}

		// 全部滿了 (ZZZ → overflow)
		throw new IllegalStateException("序號已達最大值: " + sn);
	}

	private void mailSend(BomProductManagement oldData, BomProductManagement c) {

		// Step1. 取得寄信人
		List<Order> nf_orders = new ArrayList<>();
		nf_orders.add(new Order(Direction.ASC, "bnsuname"));// 關聯帳號名稱
		PageRequest nf_pageable = PageRequest.of(0, 9999, Sort.by(nf_orders));
		ArrayList<BomNotification> notificationsUpdate = notificationDao.findAllBySearch(null, null, true, null, null,
				0, nf_pageable);// 必須要有勾一個(更新)
		// 寄信件對象
		ArrayList<String> mainUsers = new ArrayList<String>();
		ArrayList<String> secondaryUsers = new ArrayList<String>();
		// 寄信對象條件
		notificationsUpdate.forEach(r -> {// 沒有設置=全寄信
			// 如果有機型?
			if (!r.getBnmodel().equals("") && c.getBpmmodel().contains(r.getBnmodel())) {
				// 主要?次要?
				if (r.getBnprimary() == 0) {
					mainUsers.add(r.getBnsumail());
				} else {
					secondaryUsers.add(r.getBnsumail());
				}
			} // 如果有成品號?
			else if (!r.getBnnb().equals("") && c.getBpmnb().contains(r.getBnnb())) {
				// 主要?次要?
				if (r.getBnprimary() == 0) {
					mainUsers.add(r.getBnsumail());
				} else {
					secondaryUsers.add(r.getBnsumail());
				}
			} else if (r.getBnnb().equals("") && r.getBnmodel().equals("")) {
				// 如果都沒有過濾(留空白)-> 主要?次要?
				if (r.getBnprimary() == 0) {
					mainUsers.add(r.getBnsumail());
				} else {
					secondaryUsers.add(r.getBnsumail());
				}
			}
		});

		//
		BasicNotificationMail readyNeedMail = new BasicNotificationMail();
		readyNeedMail.setBnmkind("BOM");
		readyNeedMail.setBnmmail(mainUsers + "");
		readyNeedMail.setBnmmailcc(secondaryUsers + "");// 標題
		readyNeedMail.setBnmtitle("[Update][" + c.getBpmnb() + "][" + Fm_T.to_yMd_Hms(new Date()) + "]"//
				+ " Cloud system BOM Note notification!");

		// 如果BOM規格內資料沒有異動?只改備註?
		// 取得BOM資訊(PM備註)
		String sysnote = "";
		sysnote += "☑Product Model : " + c.getBpmmodel();
		sysnote += c.getSysnote();
		sysnote += "(" + c.getSysmuser() + ")";
		sysnote = sysnote.replaceAll("\n", "<br>");
		//
		String sysnoteOld = "";
		sysnoteOld += "☑Product Model : " + oldData.getBpmmodel();
		sysnoteOld += oldData.getSysnote();
		sysnoteOld += "(" + oldData.getSysmuser() + ")";
		sysnoteOld = sysnoteOld.replaceAll("\n", "<br>");

		String newDiff = highlightDiff(sysnoteOld, sysnote);

		// 內容
		String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
				+ "<thead><tr style= 'background-color: aliceblue;'>"//
				+ "<th>新舊</th>"//
				+ "<th>品規修改</th>"//
				+ "</tr></thead>"//
				+ "<tbody>"// 模擬12筆資料
				+ "<tr><td>New</td><td>" + newDiff + "</td><tr>"// 新備註
				+ "<tr><td>Old</td><td>" + sysnoteOld + "</td><tr>"// 舊備註
				+ "</tbody></table>";//
		readyNeedMail.setBnmcontent(bnmcontent);
		notificationMailDao.save(readyNeedMail);

	}

	/**
	 * 取得兩段文字差異，並在不同處以黃色背景標記
	 * 
	 * @param oldText 舊文字
	 * @param newText 新文字
	 * @return 含HTML標記的比對結果
	 */
	public static String highlightDiff(String oldText, String newText) {
		StringsComparator comparator = new StringsComparator(oldText, newText);
		StringBuilder result = new StringBuilder();
		comparator.getScript().visit(new org.apache.commons.text.diff.CommandVisitor<Character>() {
			@Override
			public void visitInsertCommand(Character c) {
				// 🔸【新增字元】：
				// 表示這個字元 c 是新字串中出現、但在舊字串中沒有的內容
				// 通常代表「新增的部分」。
				//
				// 這裡我們用 <span> 包住該字元，
				// 並以黃色背景（background-color:yellow）凸顯它是新加入的。
				result.append("<span style='background-color:yellow;'>").append(c).append("</span>");
			}

			@Override
			public void visitDeleteCommand(Character c) {
				// 可選：顯示刪除內容（例如紅色刪除線）
				// 🔸【刪除字元】：
				// 表示這個字元 c 是舊字串中有、但在新字串中被刪除的內容
				// 通常代表「被移除的部分」。
				//
				// 為了讓視覺上更清楚，我們使用粉紅底（#ffc0cb）
				// 並加上刪除線（text-decoration:line-through）
				// 以提示這個部分是舊內容、已被移除。
				result.append("<span style='background-color:#ffc0cb;text-decoration:line-through;'>").append(c)
						.append("</span>");
			}

			@Override
			public void visitKeepCommand(Character c) {
				// 🔸【保留字元】：
				// 表示這個字元 c 在舊字串與新字串中都存在，
				// 沒有變化，因此不需要上色或刪除線。
				//
				// 直接將字元原樣加入結果即可。
				result.append(c);
			}
		});
		return result.toString();
	}
	/* -------------------- 輔助方法區 -------------------- */

	/** 安全取得 JsonObject 欄位字串值；若不是字串(例如數字/布林)也做容錯 */
	private static String getAsString(JsonObject obj, String key) {
		if (obj == null || key == null || !obj.has(key))
			return null;
		JsonElement e = obj.get(key);
		if (e == null || e.isJsonNull())
			return null;
		try {
			return e.getAsString();
		} catch (Exception ignore) {
			// 非字串（可能是數字或布林），退回 toString 並去除外層引號
			return e.toString().replaceAll("^\"|\"$", "");
		}
	}

	/**
	 * 僅在舊值與新值不同時才寫入；可避免無謂 UPDATE。 newVal == null → 寫入 JsonNull（或改為不動，視規格調整）。
	 * 
	 * @return true 表示有變更
	 */
	private static boolean addOrOverwriteIfDifferent(JsonObject target, String key, String newVal) {
		if (target == null || key == null)
			return false;

		String oldVal = null;
		if (target.has(key) && !target.get(key).isJsonNull()) {
			JsonElement oldEl = target.get(key);
			try {
				oldVal = oldEl.getAsString();
			} catch (Exception ignore) {
				oldVal = oldEl.toString().replaceAll("^\"|\"$", "");
			}
		}

		if (Objects.equals(oldVal, newVal)) {
			return false; // 值相同 → 不動
		}

		if (newVal == null) {
			target.add(key, JsonNull.INSTANCE); // 或者：return false; (若規格不允許清空)
		} else {
			target.addProperty(key, newVal);
		}
		return true;
	}
}
