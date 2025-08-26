package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BomHistoryDao;
import dtri.com.tw.pgsql.dao.BomItemSpecificationsDao;
import dtri.com.tw.pgsql.dao.BomKeeperDao;
import dtri.com.tw.pgsql.dao.BomParameterSettingsDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.BomProductRuleDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BomHistory;
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomItemSpecificationsDetailFront;
import dtri.com.tw.pgsql.entity.BomKeeper;
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
	private WarehouseMaterialDao materialDao;

	@Autowired
	private BomKeeperDao bomKeeperDao;

	@Autowired
	private BomHistoryDao historyDao;

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
			ArrayList<BomProductManagement> entitys = managementDao.findAllBySearch(null, null, null, null, pageable);
			ArrayList<BomProductManagementDetailFront> entityDetails = new ArrayList<BomProductManagementDetailFront>();

			// Step3-2.資料區分(一般/細節)
			// 正規化BOM
			PageRequest pageableBIS = PageRequest.of(0, 20000, Sort.by(ordersBIS));
			ArrayList<BomItemSpecifications> entityBIS = specificationsDao.findAllBySearch(null, null, null,
					pageableBIS);
			// 規則BOM
			PageRequest pageableBPR = PageRequest.of(0, 200, Sort.by(ordersBPR));
			ArrayList<BomProductRule> entityBPR = productRuleDao.findAllBySearch(null, null, null, pageableBPR);
			// ERP_料BOM
			PageRequest pageableBBI = PageRequest.of(0, 5000, Sort.by(ordersBBI));
			ArrayList<BasicBomIngredients> entityBBI = ingredientsDao.findAllBySearch("90-504", null, null, null, null,
					pageableBBI);
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
					case "bisname":
						s.setSyssort(4);
						break;
					case "bissdescripion":
						s.setSyssort(5);
						break;
					case "bisprocess":
						s.setSyssort(6);
						break;
					case "biswhere":
						s.setSyssort(7);
						break;
					case "bislevel":
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
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "bpmmodel", "Ex:產品型號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "bpmbisitem", "Ex:規格內容?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_4);
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
				PageRequest pageableBBI = PageRequest.of(0, 50000, Sort.by(ordersBBI));
				// 先查詢有哪些BOM->每一個查詢展BOM(因為JPA 與 原生SQL 有技術上的匹配不到)
				ArrayList<BasicBomIngredients> bbisnList = ingredientsDao.findAllBySearch(bbisn, bbiname, null, null,
						null, pageableBBI);
				Map<String, BasicBomIngredients> bbisnMap = new TreeMap<String, BasicBomIngredients>();
				for (BasicBomIngredients bbis : bbisnList) {
					// 沒有?最多100筆
					if (!bbisnMap.containsKey(bbis.getBbisn()) && bbisnMap.size() <= 100) {
						bbisnMap.put(bbis.getBbisn(), bbis);
					}
					// 100筆資料後 跳出
					if (bbisnMap.size() == 100) {
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
				entitys = managementDao.findAllBySearch(searchData.getBpmnb(), searchData.getBpmmodel(), null,
						searchData.getBpmbisitem(), pageable);
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

		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductManagement>>() {
					});

			// Step2.資料檢查
			for (BomProductManagement entityData : entityDatas) {
				// 檢查-舊資料-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<BomProductManagement> checkDatas = managementDao.findAllByCheck(entityData.getBpmnb(), null,
						null);
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
				// Step2-3.檢查匹配權限?
				BomProductManagement checkDataOne = new BomProductManagement();
				// 有可能直接覆蓋?
				if (entityData.getBpmid() == null) {
					ArrayList<BomProductManagement> managements = managementDao.findAllByCheck(entityData.getBpmnb(),
							null, null);
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

		// Step3.一般資料->寫入
		entityDatas.forEach(c -> {// 要更新的資料
			if (c.getBpmid() == null) {
				// 如果是有BOM號嘗試比對看看
				ArrayList<BomProductManagement> oldDatas = managementDao.findAllByCheck(c.getBpmnb(), null, null);
				if (oldDatas.size() == 1) {
					c.setBpmid(oldDatas.get(0).getBpmid());
				}
			}
			if (c.getBpmid() != null) {
				BomProductManagement oldData = managementDao.getReferenceById(c.getBpmid());
				// 內容不同?->比對差異->登記異動紀錄->待發信件通知
				if (!oldData.getBpmbisitem().equals(c.getBpmbisitem())) {
					newBPM.put(c.getBpmnb() + "_" + c.getBpmmodel(),
							JsonParser.parseString(c.getBpmbisitem()).getAsJsonObject());
					oldBPM.put(c.getBpmnb() + "_" + c.getBpmmodel(),
							JsonParser.parseString(oldData.getBpmbisitem()).getAsJsonObject());
				}
				// 可能->新的?

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
		Map<String, HashMap<String, String>> oldBom = new HashMap<String, HashMap<String, String>>();
		Map<String, HashMap<String, String>> newBom = new HashMap<String, HashMap<String, String>>();
		// 舊資料整理->一個BOM 內多個項目
		oldBPM.forEach((oldK, oldV) -> {
			// 物料號_數量_製成別(items>basic 如果有一樣的物料的話)
			HashMap<String, String> oldVs = new HashMap<String, String>();
			JsonArray items = oldV.getAsJsonArray("items");
			JsonArray basic = oldV.getAsJsonArray("basic");
			items.forEach(i -> {
				String bisnb = i.getAsJsonObject().getAsJsonPrimitive("bisnb").getAsString();
				String bisqty = i.getAsJsonObject().getAsJsonPrimitive("bisqty").getAsString();
				String bisprocess = i.getAsJsonObject().getAsJsonPrimitive("bisprocess").getAsString();
				String bislevel = i.getAsJsonObject().getAsJsonPrimitive("bislevel").getAsString();
				if (!bisnb.equals("") && !oldVs.containsKey(bisnb)) {
					// 若不重複則加入
					oldVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
				}
			});
			basic.forEach(i -> {
				String bisnb_bisqty_bisprocess = i.getAsString();
				String bisnb = bisnb_bisqty_bisprocess.split("_")[0];
				String bisqty = bisnb_bisqty_bisprocess.split("_")[1];
				String bisprocess = bisnb_bisqty_bisprocess.split("_")[2];
				String bislevel = bisnb_bisqty_bisprocess.split("_")[3];
				if (!bisnb.equals("") && !oldVs.containsKey(bisnb)) {
					// 若不重複則加入
					oldVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
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
				String bisnb = i.getAsJsonObject().getAsJsonPrimitive("bisnb").getAsString();
				String bisqty = i.getAsJsonObject().getAsJsonPrimitive("bisqty").getAsString();
				String bisprocess = i.getAsJsonObject().getAsJsonPrimitive("bisprocess").getAsString();
				String bislevel = i.getAsJsonObject().getAsJsonPrimitive("bislevel").getAsString();
				if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
					// 若不重複則加入
					newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
				}
			});
			basic.forEach(i -> {
				String bisnb_bisqty_bisprocess = i.getAsString();
				String bisnb = bisnb_bisqty_bisprocess.split("_")[0];
				String bisqty = bisnb_bisqty_bisprocess.split("_")[1];
				String bisprocess = bisnb_bisqty_bisprocess.split("_")[2];
				String bislevel = bisnb_bisqty_bisprocess.split("_")[3];
				if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
					// 若不重複則加入
					newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
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
				// 沒有變化(物料)
				if (oldBomv.containsKey(newItemK)) {
					bomHistory.setBhpnb(newItemV.split("_")[0]);
					bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
					bomHistory.setBhpprocess(newItemV.split("_")[2]);
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
						bomHistoryOld.setBhpprocess(oldItemV.split("_")[2]);
						bomHistoryOld.setBhlevel(Integer.parseInt(oldItemV.split("_")[3]));
						bomHistoryOld.setBhatype("Old");
						changeBom.add(bomHistoryOld);
					}
					changeBom.add(bomHistory);
					// 複寫標記null
					oldBomv.put(newItemK, null);
				} else {
					// 沒比對到?新的?
					bomHistory.setBhpnb(newItemV.split("_")[0]);
					bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
					bomHistory.setBhpprocess(newItemV.split("_")[2]);
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
					bomHistoryRemove.setBhpnb(oldItemV.split("_")[0]);
					bomHistoryRemove.setBhpqty(Integer.parseInt(oldItemV.split("_")[1]));
					bomHistoryRemove.setBhpprocess(oldItemV.split("_")[2]);
					bomHistoryRemove.setBhlevel(Integer.parseInt(oldItemV.split("_")[3]));
					bomHistoryRemove.setBhatype("Delete");
					changeBom.add(bomHistoryRemove);

				}
			});
			// 比對同一張BOM->的物料
			historyDao.saveAll(changeBom);

		});

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
							null, null);
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
					// Step2-3.檢查匹配權限?
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
				}
			}
			// =======================資料整理=======================
			// 準備比對資料<BOM號,內容新舊>
			Map<String, JsonObject> newBPM = new HashMap<String, JsonObject>();
			for (BomProductManagement x : entityDatas) {
				// 登記異動紀錄->待發信件通知
				newBPM.put(x.getBpmnb() + "_" + x.getBpmmodel(),
						JsonParser.parseString(x.getBpmbisitem()).getAsJsonObject());
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
					String bisnb = i.getAsJsonObject().getAsJsonPrimitive("bisnb").getAsString();
					String bisqty = i.getAsJsonObject().getAsJsonPrimitive("bisqty").getAsString();
					String bisprocess = i.getAsJsonObject().getAsJsonPrimitive("bisprocess").getAsString();
					String bislevel = i.getAsJsonObject().getAsJsonPrimitive("bislevel").getAsString();
					if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
						// 若不重複則加入
						newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
					}
				});
				basic.forEach(i -> {
					String bisnb_bisqty_bisprocess = i.getAsString();
					String bisnb = bisnb_bisqty_bisprocess.split("_")[0];
					String bisqty = bisnb_bisqty_bisprocess.split("_")[1];
					String bisprocess = bisnb_bisqty_bisprocess.split("_")[2];
					String bislevel = bisnb_bisqty_bisprocess.split("_")[3];
					if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
						// 若不重複則加入
						newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
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
					bomHistory.setBhpnb(newItemV.split("_")[0]);
					bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
					bomHistory.setBhpprocess(newItemV.split("_")[2]);
					bomHistory.setBhlevel(Integer.parseInt(newItemV.split("_")[3]));
					bomHistory.setBhatype("All New");
					changeBom.add(bomHistory);
				});
				// 比對同一張BOM->的物料
				historyDao.saveAll(changeBom);
			});
			// =======================資料儲存=======================
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
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBpmid() != null) {
				// 登記異動紀錄->待發信件通知
				newBPM.put(x.getBpmnb() + "_" + x.getBpmmodel(),
						JsonParser.parseString(x.getBpmbisitem()).getAsJsonObject());
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
				String bisnb = i.getAsJsonObject().getAsJsonPrimitive("bisnb").getAsString();
				String bisqty = i.getAsJsonObject().getAsJsonPrimitive("bisqty").getAsString();
				String bisprocess = i.getAsJsonObject().getAsJsonPrimitive("bisprocess").getAsString();
				String bislevel = i.getAsJsonObject().getAsJsonPrimitive("bislevel").getAsString();
				if (!bisnb.equals("") && !newVs.containsKey(bisnb)) {
					// 若不重複則加入
					newVs.put(bisnb, bisnb + "_" + bisqty + "_" + bisprocess + "_" + bislevel);
				}
			});
			basic.forEach(i -> {
				String bisnb_bisqty_bisprocess = i.getAsString();
				// 排除項目是空直
				if (!bisnb_bisqty_bisprocess.equals("")) {
					String bisnb = bisnb_bisqty_bisprocess.split("_")[0];
					String bisqty = bisnb_bisqty_bisprocess.split("_")[1];
					String bisprocess = bisnb_bisqty_bisprocess.split("_")[2];
					String bislevel = bisnb_bisqty_bisprocess.split("_")[3];
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
				bomHistory.setBhpnb(newItemV.split("_")[0]);
				bomHistory.setBhpqty(Integer.parseInt(newItemV.split("_")[1]));
				bomHistory.setBhpprocess(newItemV.split("_")[2]);
				bomHistory.setBhlevel(Integer.parseInt(newItemV.split("_")[3]));
				bomHistory.setBhatype("All Delete");
				changeBom.add(bomHistory);
			});
			// 比對同一張BOM->的物料
			historyDao.saveAll(changeBom);
		});

		// =======================資料儲存=======================
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
}
