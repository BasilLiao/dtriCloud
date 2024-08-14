package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import dtri.com.tw.pgsql.dao.BomItemSpecificationsDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.BomProductRuleDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomProductManagement;
import dtri.com.tw.pgsql.entity.BomProductManagementDetailFront;
import dtri.com.tw.pgsql.entity.BomProductRule;
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
		orders.add(new Order(Direction.ASC, "bpmid"));// 群組

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<BomProductManagement> entitys = managementDao.findAllBySearch(null, null, null, pageable);
			ArrayList<BomProductManagementDetailFront> entityDetails = new ArrayList<BomProductManagementDetailFront>();

			// Step3-2.資料區分(一般/細節)
			// 類別(一般模式)->群組拆開
			entitys.forEach(e -> {
				// 取得細節
				if (!e.getBpmbisitem().equals("")) {
					JsonArray dJsons = (JsonArray) JsonParser.parseString(e.getBpmbisitem());
					dJsons.forEach(dJs -> {
						BomProductManagementDetailFront detailFront = new BomProductManagementDetailFront();
						JsonObject dJ = dJs.getAsJsonObject();
						detailFront.setBpmid(e.getBpmid());// ID
						detailFront.setBisgid(dJ.get("bisid").getAsLong());
						detailFront.setBisgid(dJ.get("bisgid").getAsLong());
						detailFront.setBisqty(dJ.get("bisqty").getAsInt());
						detailFront.setBisgname(dJ.get("bisgname").getAsString());
						detailFront.setBisgfname(dJ.get("bisgfname").getAsString());
						detailFront.setBisfname(dJ.get("bisfname").getAsString());
						detailFront.setBissdescripion(dJ.get("bissdescripion").getAsString());
						entityDetails.add(detailFront);
					});
				}
			});

			// 其他資料格式(同步-BOM成品組成/BOM產品規則/BOM可選擇性項目)
			List<Order> ordersBBI = new ArrayList<>();
			ordersBBI.add(new Order(Direction.ASC, "bbisn"));//
			List<Order> ordersBPR = new ArrayList<>();
			ordersBPR.add(new Order(Direction.ASC, "bprname"));//
			List<Order> ordersBIS = new ArrayList<>();
			ordersBIS.add(new Order(Direction.ASC, "syssort"));//
			ordersBIS.add(new Order(Direction.ASC, "bisgname"));//
			ordersBIS.add(new Order(Direction.ASC, "bisfname"));//

			// 其他模式
			PageRequest pageableBIS = PageRequest.of(0, 20000, Sort.by(ordersBIS));
			ArrayList<BomItemSpecifications> entityBIS = specificationsDao.findAllBySearch(null, null, null,
					pageableBIS);
			//
			PageRequest pageableBPR = PageRequest.of(0, 200, Sort.by(ordersBPR));
			ArrayList<BomProductRule> entityBPR = productRuleDao.findAllBySearch(null, null, pageableBPR);
			//
			PageRequest pageableBBI = PageRequest.of(0, 30000, Sort.by(ordersBBI));
			ArrayList<BasicBomIngredients> entityBBI = ingredientsDao.findAllBySearch("90-", null, null, null, null,
					pageableBBI);
			Set<BasicBomIngredients> entityBBISet = new HashSet<>(entityBBI);
			Iterator<BasicBomIngredients> iterator = (Iterator<BasicBomIngredients>) entityBBISet.iterator();
			// 資料整理(BBI-限制200筆)
			Map<String, ArrayList<BasicBomIngredients>> entityBBIMap = new TreeMap<String, ArrayList<BasicBomIngredients>>();
			while (iterator.hasNext() && entityBBIMap.size() <= 100) {
				ArrayList<BasicBomIngredients> ingredients = new ArrayList<BasicBomIngredients>();
				BasicBomIngredients bbi = iterator.next();
				if (entityBBIMap.containsKey(bbi.getBbisn())) {
					ingredients = entityBBIMap.get(bbi.getBbisn());
					ingredients.add(bbi);
					entityBBIMap.put(bbi.getBbisn(), ingredients);
				} else {
					ingredients.add(bbi);
					entityBBIMap.put(bbi.getBbisn(), ingredients);
				}
			}
			ArrayList<BasicBomIngredients> entityBBIh = new ArrayList<BasicBomIngredients>();
			ArrayList<BasicBomIngredients> entityBBId = new ArrayList<BasicBomIngredients>();
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

			String entityJsonBBI = packageService.beanToJson(entityBBIh);
			String entityDetailJsonBBI = packageService.beanToJson(entityBBId);
			String entityJsonBPR = packageService.beanToJson(entityBPR);
			String entityJsonBIS = packageService.beanToJson(entityBIS);
			JsonObject other = new JsonObject();
			other.add("BBI", packageService.StringToAJson(entityJsonBBI));
			other.add("DetailBBI", packageService.StringToAJson(entityDetailJsonBBI));
			other.add("BPR", packageService.StringToAJson(entityJsonBPR));
			other.add("BIS", packageService.StringToAJson(entityJsonBIS));
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
			Map<String, SystemLanguageCell> mapLanguagesBPR = new HashMap<>();// Cloud BOM rules list(BOM產品規則)
			Map<String, SystemLanguageCell> mapLanguagesBIS = new HashMap<>();// Cloud BOM detailed material(BOM物料項目規範)

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
			// 動態->覆蓋寫入->修改UI選項

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

			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = BomProductManagement.class.getDeclaredFields();
			Field[] fieldsDetail = BomProductManagementDetailFront.class.getDeclaredFields();
			// 而外資料
			Field[] fieldsBBI = BasicBomIngredients.class.getDeclaredFields();
			Field[] fieldsBPR = BomProductRule.class.getDeclaredFields();
			Field[] fieldsBIS = BomItemSpecifications.class.getDeclaredFields();

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

			// Step3-5. 建立查詢項目
			// ERP 料BOM
			searchJsonsBBI = packageService.searchSet(searchJsonsBBI, null, "bbisn", "Ex:90BOM號(物料)?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_6);
			// Cloud rules
			searchJsonsBPR = packageService.searchSet(searchJsonsBPR, null, "bprname", "Ex:規則名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_6);
			// BOM規格
			searchJsonsBPM = packageService.searchSet(searchJsonsBPM, null, "bpmnb", "Ex:90BOM號(規格)?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_6);
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

			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			BomItemSpecifications searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					BomItemSpecifications.class);
			// 群組
			ArrayList<BomItemSpecifications> entityGroups = new ArrayList<BomItemSpecifications>();
			Map<String, BomItemSpecifications> mapGroups = new HashMap<String, BomItemSpecifications>();

			ArrayList<BomItemSpecifications> entitys = specificationsDao.findAllBySearch(searchData.getBisgname(),
					searchData.getBisname(), searchData.getBisnb(), pageable);
			// Step4-2.資料區分(一般/細節)
			entitys.forEach(e -> {
				if (!mapGroups.containsKey(e.getBisgname())) {
					// 群組資料
					mapGroups.put(e.getBisgname(), e);
					entityGroups.add(e);
				}
			});

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			String entityGJson = packageService.beanToJson(entityGroups);
			// 資料包裝
			packageBean.setEntityJson(entityGJson);
			packageBean.setEntityDetailJson(entityJson);
			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new BomItemSpecifications());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("bisid_bisgid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 取得測試條件資料 */
	public PackageBean getSearchTest(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<BomItemSpecifications> entitys = new ArrayList<>();
		List<BomItemSpecifications> entityNews = new ArrayList<>();
		List<WarehouseMaterial> materials = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		Map<String, BomItemSpecifications> sqlQueryEntitys = new HashMap<>();
		String entityJsonDatas = "";
		String statusGid = packageBean.getCallBackValue();

		// =======================舊有資料?=======================
		if (statusGid.split("_").length == 2 && !statusGid.split("_")[1].equals("")) {
			entitys = specificationsDao.findAllByBisgid(Long.parseLong(statusGid.split("_")[1]));
			entitys.forEach(r -> {
				r.setSysstatus(2);// 無效狀態
			});
		}

		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM warehouse_material e Where ";
		int n = 1;
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[1];
			cellName = cellName.replace("wm", "wm_");
			cellName = cellName.replace("wm_pnb", "wm_p_nb");

			String andOR = x.getAsString().split("<_>")[0].replaceAll("<", "").replaceAll(">", "");
			String where = x.getAsString().split("<_>")[2];
			String value = x.getAsString().split("<_>")[3];// 有可能空白

			switch (where) {
			case "AllSame":
				nativeQuery += andOR + " (e." + cellName + " = :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, value);
				break;
			case "NotSame":
				nativeQuery += andOR + " (e." + cellName + " != :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, value);
				break;
			case "Like":
				nativeQuery += andOR + " (e." + cellName + " LIKE :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "%" + value + "%");
				break;
			case "LikeS":
				nativeQuery += andOR + " (e." + cellName + " LIKE :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "" + value + "%");
				break;
			case "LikeE":
				nativeQuery += andOR + " (e." + cellName + " LIKE :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "%" + value + "");
				break;
			case "NotLike":
				nativeQuery += andOR + " (e." + cellName + " NOT LIKE :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "%" + value + "%");
				break;
			case "NotLikeS":
				nativeQuery += andOR + " (e." + cellName + " NOT LIKE :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "" + value + "%");
				break;
			case "NotLikeE":
				nativeQuery += andOR + " (e." + cellName + " NOT LIKE :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "%" + value + "");
				break;
			}
			n++;
		}

		nativeQuery += " order by e.wm_p_nb asc,e.wm_name asc";
		nativeQuery += " LIMIT 2500 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, WarehouseMaterial.class);
		// =======================查詢參數=======================
		sqlQuery.forEach((key, valAndType) -> {
			String val = valAndType;
			// 文字?
			query.setParameter(key, val);
		});
		try {
			materials = query.getResultList();
			// 資料轉換
			materials.forEach(m -> {
				BomItemSpecifications itemSp = new BomItemSpecifications();
				itemSp.setSyscdate(m.getSyscdate());
				itemSp.setSysmdate(m.getSysmdate());
				itemSp.setSyscuser(m.getSyscuser());
				itemSp.setSysmuser(m.getSysmuser());
				//
				itemSp.setBisname(m.getWmname());
				itemSp.setBisnb(m.getWmpnb());
				itemSp.setBisspecifications(m.getWmspecification());
				itemSp.setBisdescription(m.getWmdescription());
				itemSp.setSysstatus(0);
				// 如果舊有資料->添加群組ID
				if (statusGid.split("_").length == 2 && !statusGid.split("_")[1].equals("")) {
					itemSp.setBisgid(Long.parseLong(statusGid.split("_")[1]));
				}
				sqlQueryEntitys.put(itemSp.getBisnb(), itemSp);// 物料號
				// entitys.add(itemSp);
			});
		} catch (PersistenceException e) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, Lan.zh_TW, null);
		}
		// 資料比對整理(新舊整合)
		entitys.forEach(s -> {
			// 有比對到->更新
			BomItemSpecifications itemSp = new BomItemSpecifications();
			if (sqlQueryEntitys.containsKey(s.getBisnb())) {
				itemSp = sqlQueryEntitys.get(s.getBisnb());
				s.setBisname(itemSp.getBisname());
				s.setBisnb(itemSp.getBisnb());
				s.setBisspecifications(itemSp.getBisspecifications());
				s.setBisdescription(itemSp.getBisdescription());
				entityNews.add(s);
				sqlQueryEntitys.get(s.getBisnb()).setSysstatus(2);// 無效狀態
			} else {
				// 沒比對到->移除(除了customize例外)
				if (s.getBisnb().equals("customize")) {
					entityNews.add(s);
				}
			}
		});

		// 可能有新的?
		sqlQueryEntitys.forEach((k, v) -> {
			if (v.getSysstatus() == 0) {
				entityNews.add(v);
			}
		});
		// 物料排序
		entityNews.sort((o1, o2) -> o1.getBisnb().compareTo(o2.getBisnb()));

		// 資料包裝
		entityJsonDatas = packageService.beanToJson(entityNews);
		packageBean.setEntityDetailJson(entityJsonDatas);
		// 查不到資料
		if (packageBean.getEntityDetailJson().equals("[]")) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}

		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomItemSpecifications> entityDatas = new ArrayList<>();
		Map<String, Boolean> entitySame = new HashMap<String, Boolean>();
		// =======================資料檢查=======================
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<BomItemSpecifications>>() {
					});

			// Step2.資料檢查
			for (BomItemSpecifications entityData : entityDatas) {
				// 檢查-舊資料-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<BomItemSpecifications> checkDatas = specificationsDao.findAllByCheck(null,
						entityData.getBisfname(), null);
				for (BomItemSpecifications checkData : checkDatas) {
					// 排除自己
					if (entityData.getBisid() != null && checkData.getBisid().compareTo(entityData.getBisid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getBisfname() });
					}
				}
				// 檢查-新資料-名稱重複
				if (entitySame.containsKey(entityData.getBisfname())) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getBisfname() });
				}
				entitySame.put(entityData.getBisfname(), true);
				// 檢查資料-正規化值?
				if (entityData.getBisfname().equals("[\"\"]")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getBisnb() });
				}
			}
		}
		// 一定要有群組
		if (entityDatas.get(0).getBisgid() == null) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
					new String[] { entityDatas.get(0).getBisname() });
		}

		// =======================資料整理=======================
		// Step3.一般資料->寫入
		Long bisgid = entityDatas.get(0).getBisgid();
		ArrayList<BomItemSpecifications> oldDatas = specificationsDao.findAllByBisgid(entityDatas.get(0).getBisgid());
		ArrayList<BomItemSpecifications> saveDatasUpdate = new ArrayList<>();
		ArrayList<BomItemSpecifications> saveDatasRemove = new ArrayList<>();
		Map<Long, BomItemSpecifications> entityMapDatas = new HashMap<Long, BomItemSpecifications>();
		entityDatas.forEach(c -> {// 要更新的資料
			if (c.getBisid() == null) {
				// 可能->新的?
				c.setBisid(null);
				c.setBisgid(bisgid);
				c.setSysmdate(new Date());
				c.setSysmuser(packageBean.getUserAccount());
				c.setSysodate(new Date());
				c.setSysouser(packageBean.getUserAccount());
				c.setSyscdate(new Date());
				c.setSyscuser(packageBean.getUserAccount());
				c.setSysheader(false);
				c.setSysstatus(0);
				saveDatasUpdate.add(c);
			} else {
				entityMapDatas.put(c.getBisid(), c);
			}
		});

		// 更新
		oldDatas.forEach(entityDataOld -> {
			// 比對到->更新
			Long bisId = entityDataOld.getBisid();
			if (entityMapDatas.containsKey(bisId)) {
				BomItemSpecifications x = new BomItemSpecifications();
				x = entityMapDatas.get(bisId);
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());

				entityDataOld.setBisnb(x.getBisnb());
				entityDataOld.setBisname(x.getBisname());
				entityDataOld.setBisspecifications(x.getBisspecifications());
				entityDataOld.setBisdescription(x.getBisdescription());
				entityDataOld.setBissdescripion(x.getBissdescripion());
				entityDataOld.setBisfname(x.getBisfname());
				entityDataOld.setBisgfname(x.getBisgfname());
				entityDataOld.setBisgffield(x.getBisgffield());
				entityDataOld.setBisgname(x.getBisgname());
				entityDataOld.setBisgsplit(x.getBisgsplit());
				entityDataOld.setBisgcondition(x.getBisgcondition());
				// 勾選
				entityDataOld.setBisproduct(x.getBisproduct());
				entityDataOld.setBisaccessories(x.getBisaccessories());
				entityDataOld.setBissfproduct(x.getBissfproduct());
				entityDataOld.setBisdevelopment(x.getBisdevelopment());
				entityDataOld.setBispcb(x.getBispcb());
				entityDataOld.setBisiauto(x.getBisiauto());
				//
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysodate(new Date());
				saveDatasUpdate.add(entityDataOld);
			} else {
				// 沒比對到->移除
				saveDatasRemove.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		specificationsDao.saveAll(saveDatasUpdate);
		specificationsDao.deleteAll(saveDatasRemove);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomItemSpecifications> entityDatas = new ArrayList<>();
		ArrayList<BomItemSpecifications> entitySave = new ArrayList<>();
		Map<String, BomItemSpecifications> checkSame = new HashMap<String, BomItemSpecifications>();
		// =======================資料檢查=======================
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<BomItemSpecifications>>() {
					});
			// Step2-1.資料檢查
			for (BomItemSpecifications entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<BomItemSpecifications> checkDatas = specificationsDao.findAllByCheck(null,
						entityData.getBisfname(), null);
				if (checkDatas.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { checkDatas.get(0).getBisnb() + " -> " + checkDatas.get(0).getBisname() });
				}

				// Step2-2.資料檢查-檢查基本重複?
				if (!checkSame.containsKey(entityData.getBisfname())) {
					checkSame.put(entityData.getBisfname(), entityData);
				} else {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { checkSame.get(entityData.getBisfname()).getBisnb() + " : "
									+ entityData.getBisnb() + "->" + entityData.getBisfname() });
				}
				// Step2-3.資料檢查-缺少值?
				if (entityData.getBisgcondition() == null || entityData.getBisgcondition().equals("") || //
						entityData.getBisgname() == null || entityData.getBisgname().equals("") || //
						entityData.getBisgffield() == null || entityData.getBisgffield().equals("") || //
						entityData.getBisgfname() == null || entityData.getBisgfname().equals("") || //
						entityData.getBisgsplit() == null || entityData.getBisgsplit().equals("")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getBisnb() });
				}

			}
		}
		// =======================資料整理=======================

		Long bisgid = specificationsDao.getBomItemSpecificationsGroupGSeq();
		for (BomItemSpecifications x : entityDatas) {
			// 新增
			x.setBisid(null);
			x.setBisgid(bisgid);
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSysheader(false);
			entitySave.add(x);
		}
		// =======================資料儲存=======================
		// 資料Data
		specificationsDao.saveAll(entitySave);

		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomItemSpecifications> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<BomItemSpecifications>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BomItemSpecifications> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBisid() != null) {
				BomItemSpecifications entityDataOld = specificationsDao.findById(x.getBisid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		specificationsDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomItemSpecifications> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<BomItemSpecifications>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BomItemSpecifications> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBisid() != null) {
				BomItemSpecifications entityDataOld = specificationsDao.getReferenceById(x.getBisid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		specificationsDao.deleteAll(saveDatas);
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
		String nativeQuery = "SELECT e.* FROM bom_item_specifications e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("bis", "bis_");

			cellName = cellName.replace("bis_gid", "bis_g_id");
			cellName = cellName.replace("bis_sdescripion", "bis_s_descripion");
			cellName = cellName.replace("bis_fname", "bis_f_name");
			cellName = cellName.replace("bis_gname", "bis_g_name");
			cellName = cellName.replace("bis_gsplit", "bis_g_split");

			cellName = cellName.replace("bis_gcondition", "bis_g_condition");
			cellName = cellName.replace("bis_sfproduct", "bis_sf_product");
			cellName = cellName.replace("bis_iauto", "bis_i_auto");

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
		nativeQuery += " order by e.bis_g_name asc,e.bis_name asc";
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
