package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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

import dtri.com.tw.bean.BasicLine;
import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BomItemSpecificationsDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomItemSpecificationsDetailFront;
import dtri.com.tw.pgsql.entity.BomProductManagement;
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
public class BomItemSpecificationsServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private BomItemSpecificationsDao specificationsDao;

	@Autowired
	private BomProductManagementDao managementDao;

	@Autowired
	private BasicBomIngredientsDao bomIngredientsDao;

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
		orders.add(new Order(Direction.ASC, "syssort"));// 排序
		orders.add(new Order(Direction.ASC, "bisgid"));// 群組
		orders.add(new Order(Direction.ASC, "bisnb"));// 物料號

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, 10000, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<BomItemSpecifications> entitys = specificationsDao.findAllBySearch(null, null, null, pageable);
			ArrayList<BomItemSpecifications> entityGroups = new ArrayList<BomItemSpecifications>();
			Map<String, BomItemSpecifications> mapGroups = new HashMap<String, BomItemSpecifications>();

			// Step3-2.資料區分(一般/細節)

			// 類別(一般模式)
			entitys.forEach(e -> {
				if (!mapGroups.containsKey(e.getBisgname())) {
					// 群組資料
					mapGroups.put(e.getBisgname(), e);
					entityGroups.add(e);
				}
			});

			String entityJson = packageService.beanToJson(entityGroups);
			String entityDetailJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDetailJson);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("BomItemSpecifications",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("BomItemSpecificationsDetailFront", null, 2);
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = BomItemSpecifications.class.getDeclaredFields();
			Field[] fieldsDetail = BomItemSpecificationsDetailFront.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// 欄位翻譯(細節)
			resultDetailTJsons = packageService.resultSet(fieldsDetail, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "bisgname", "Ex:項目組名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "bisfname", "Ex:正規化內容?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "bisnb", "Ex:50-123-456789?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			BomItemSpecifications searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					BomItemSpecifications.class);
			// 群組
			ArrayList<BomItemSpecifications> entityGroups = new ArrayList<BomItemSpecifications>();
			Map<String, BomItemSpecifications> mapGroups = new HashMap<String, BomItemSpecifications>();

			ArrayList<BomItemSpecifications> entitys = specificationsDao.findAllBySearch(searchData.getBisgname(),
					searchData.getBisfname(), searchData.getBisnb(), pageable);
			// Step4-2.資料區分(一般/細節)
			entitys.forEach(e -> {
				if (!mapGroups.containsKey(e.getBisgname())) {
					// 群組資料
					mapGroups.put(e.getBisgname(), e);
					entityGroups.add(e);
				}
			});

			// 如果有查詢另外資料->再次查詢 群組
			ArrayList<BomItemSpecifications> entityAll = new ArrayList<BomItemSpecifications>();
			if (searchData.getBisfname() != null || searchData.getBisnb() != null) {
				entityGroups.forEach(g -> {
					ArrayList<BomItemSpecifications> entityOne = specificationsDao.findAllBySearch(g.getBisgname(),
							null, null, pageable);
					entityAll.addAll(entityOne);
				});
			}

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entityAll.size() > 0 ? entityAll : entitys);
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
		Map<String, String> entityNewsRep = new HashMap<>();// 避免重複
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
				nativeQuery += andOR + " (e." + cellName + " ~ :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "" + value + "");
				break;
			case "LikeS":
				nativeQuery += andOR + " (e." + cellName + " ~ :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "^" + value + "");
				break;
			case "LikeE":
				nativeQuery += andOR + " (e." + cellName + " ~ :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "" + value + "^");
				break;
			case "NotLike":
				nativeQuery += andOR + " (e." + cellName + " !~ :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "" + value + "");
				break;
			case "NotLikeS":
				nativeQuery += andOR + " (e." + cellName + " !~ :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "^" + value + "");
				break;
			case "NotLikeE":
				nativeQuery += andOR + " (e." + cellName + " !~ :" + cellName + n + ")  ";
				sqlQuery.put(cellName + n, "" + value + "^");
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
				itemSp.setBislevel(0);
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
				// 避免重複
				if (!entityNewsRep.containsKey(itemSp.getBisnb())) {
					entityNewsRep.put(itemSp.getBisnb(), "");
					entityNews.add(s);
				}
				sqlQueryEntitys.get(s.getBisnb()).setSysstatus(2);// 無效狀態
			} else {
				// 沒比對到->移除(除了customize例外)
				if (s.getBisnb().contains("customize")) {
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

	// 自動測試與更新
	public PackageBean getAutoSearchTestAndUpdate(PackageBean packageBean) {
		Map<Long, List<BomItemSpecifications>> entitysMap = new HashMap<Long, List<BomItemSpecifications>>();
		// =======================舊有資料?=======================
		List<BomItemSpecifications> entitysData = specificationsDao.findAll();
		// 過濾->只取得GID
		entitysData.forEach(s -> {
			// 去除自訂義
			if (!s.getBisnb().contains("customize")) {
				if (entitysMap.containsKey(s.getBisgid())) {
					List<BomItemSpecifications> list = entitysMap.get(s.getBisgid());
					list.add(s);
					entitysMap.put(s.getBisgid(), list);
				} else {
					List<BomItemSpecifications> list = new ArrayList<BomItemSpecifications>();
					list.add(s);
					entitysMap.put(s.getBisgid(), list);
				}
			}
		});

		entitysMap.forEach((ks, vs) -> {
			// =======================查詢語法=======================
			// 拼湊SQL語法
			List<WarehouseMaterial> materials = new ArrayList<>();
			Map<String, String> sqlQuery = new HashMap<>();
			List<BomItemSpecifications> entityNews = new ArrayList<>();
			Map<String, BomItemSpecifications> sqlQueryEntitys = new HashMap<>();
			String nativeQuery = "SELECT e.* FROM warehouse_material e Where ";
			List<BomItemSpecifications> entitys = vs;
			BomItemSpecifications entityOne = vs.get(0);

			// 共用參數
			Long bisgid = ks;// GID
			Boolean bisiauto = entityOne.getBisiauto();// 自動?
			Boolean bisdselect = entityOne.getBisdselect();// 預設選擇?
			// 勾選
			Boolean bispcb = entityOne.getBispcb();
			Boolean bisproduct = entityOne.getBisproduct();
			Boolean bissfproduct = entityOne.getBissfproduct();
			Boolean bisaccessories = entityOne.getBisaccessories();
			Boolean bisdevelopment = entityOne.getBisdevelopment();
			// 勾選-必選
			Boolean bismpcb = entityOne.getBismpcb();
			Boolean bismproduct = entityOne.getBismproduct();
			Boolean bismsfproduct = entityOne.getBismsfproduct();
			Boolean bismaccessories = entityOne.getBismaccessories();
			Boolean bismdevelopment = entityOne.getBismdevelopment();
			//
			String bisgfname = entityOne.getBisgfname();// 正規畫-群組名稱
			Integer bisgfnameSize = bisgfname.split(" ").length;
			String bisprocess = entityOne.getBisprocess();
			String bisgname = entityOne.getBisgname();
			String bisgffield = entityOne.getBisgffield();
			String bisgsplit = entityOne.getBisgsplit();// 排序
			Integer syssort = entityOne.getSyssort();// 排序

			String bisgcondition = entityOne.getBisgcondition().replaceAll("(<AND>|<OR>)", "<@@>$1");
			String bisgconditions[] = bisgcondition.split("<@@>");// 條件
			//
			int n = 1;// 第幾參數
			for (String x : bisgconditions) {
				// entity 需要轉換SQL與句 && 欄位
				String cellName = x.split("<_>")[1];
				cellName = cellName.replace("wm", "wm_");
				cellName = cellName.replace("wm_pnb", "wm_p_nb");

				String andOR = x.split("<_>")[0].replaceAll("<", "").replaceAll(">", "");
				String where = x.split("<_>")[2];
				String value = x.split("<_>")[3];// 有可能空白

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
					nativeQuery += andOR + " (e." + cellName + " ~ :" + cellName + n + ")  ";
					sqlQuery.put(cellName + n, "" + value + "");
					break;
				case "LikeS":
					nativeQuery += andOR + " (e." + cellName + " ~ :" + cellName + n + ")  ";
					sqlQuery.put(cellName + n, "^" + value + "");
					break;
				case "LikeE":
					nativeQuery += andOR + " (e." + cellName + " ~ :" + cellName + n + ")  ";
					sqlQuery.put(cellName + n, "" + value + "^");
					break;
				case "NotLike":
					nativeQuery += andOR + " (e." + cellName + " !~ :" + cellName + n + ")  ";
					sqlQuery.put(cellName + n, "" + value + "");
					break;
				case "NotLikeS":
					nativeQuery += andOR + " (e." + cellName + " !~ :" + cellName + n + ")  ";
					sqlQuery.put(cellName + n, "^" + value + "");
					break;
				case "NotLikeE":
					nativeQuery += andOR + " (e." + cellName + " !~ :" + cellName + n + ")  ";
					sqlQuery.put(cellName + n, "" + value + "^");
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

			materials = query.getResultList();
			// 資料轉換
			Map<String, String> sameBisfname = new HashMap<String, String>();
			materials.forEach(m -> {
				BomItemSpecifications itemSp = new BomItemSpecifications();
				itemSp.setSyscdate(m.getSyscdate());
				itemSp.setSysmdate(m.getSysmdate());
				itemSp.setSyscuser(m.getSyscuser());
				itemSp.setSysmuser(m.getSysmuser());
				//
				itemSp.setBisnb(m.getWmpnb());
				itemSp.setBisname(m.getWmname());
				itemSp.setBisspecifications(m.getWmspecification());
				itemSp.setBisdescription(m.getWmdescription());
				// 舊有資料->新資料內
				itemSp.setBisid(null);
				itemSp.setSysstatus(0);
				itemSp.setBisgid(bisgid);
				itemSp.setBisiauto(bisiauto);
				itemSp.setBisdselect(bisdselect);
				itemSp.setBisgfname(bisgfname);
				// 勾選
				itemSp.setBispcb(bispcb);// PCBA主板
				itemSp.setBisproduct(bisproduct);// 產品
				itemSp.setBissfproduct(bissfproduct);//
				itemSp.setBisaccessories(bisaccessories);//
				itemSp.setBisdevelopment(bisdevelopment);//
				//
				itemSp.setBismpcb(bismpcb);// PCBA主板
				itemSp.setBismproduct(bismproduct);// 產品
				itemSp.setBismsfproduct(bismsfproduct);//
				itemSp.setBismaccessories(bismaccessories);//
				itemSp.setBismdevelopment(bismdevelopment);//
				//
				itemSp.setBisprocess(bisprocess);//
				itemSp.setBisgname(bisgname);//
				itemSp.setBisgffield(bisgffield);//
				itemSp.setBisgsplit(bisgsplit);
				itemSp.setBisgcondition(bisgcondition);// 區分
				itemSp.setBislevel(0);

				// 正規畫名稱轉換(物料?物料名?規格?敘述?)
				String[] array = new String[0];
				if (bisgffield.equals("bisnb")) {
					array = m.getWmpnb().split(bisgsplit);
				} else if (bisgffield.equals("bisname")) {
					array = m.getWmname().split(bisgsplit);
				} else if (bisgffield.equals("bisspecifications")) {
					array = m.getWmspecification().split(bisgsplit);
				} else if (bisgffield.equals("bisdescription")) {
					array = m.getWmdescription().split(bisgsplit);
				}
				array = Arrays.copyOfRange(array, 0, Math.min(array.length, bisgfnameSize)); // 限制為最多 5 個元素
				Gson gson = new Gson();
				JsonArray jsonArray = gson.toJsonTree(array).getAsJsonArray();
				// 將 JsonArray 轉成字串並存入 itemSp\\n: 移除換行符號。 \\r: 移除回車符號。 \\t: 移除制表符號。
				String setBisfname = jsonArray.toString();
				setBisfname = setBisfname.replaceAll("[\\n\\r\\t]", "");
				setBisfname = setBisfname.replaceAll("\\\\[nrt]", "");
				itemSp.setBisfname(setBisfname);
				itemSp.setSyssort(syssort);

				// 如果重複?
				if (!sameBisfname.containsKey(setBisfname)) {
					sameBisfname.put(setBisfname, "");
					sqlQueryEntitys.put(itemSp.getBisnb(), itemSp);// 物料號
				} else {
					System.out.println("重複:" + itemSp.getBisfname());
				}
			});

			// 資料比對整理(舊(entitys)->新(sqlQueryEntitys) 整合)
			entitys.forEach(s -> {
				// 有比對到->更新
				BomItemSpecifications itemSp = new BomItemSpecifications();
				if (sqlQueryEntitys.containsKey(s.getBisnb())) {
					itemSp = sqlQueryEntitys.get(s.getBisnb());
					s.setBisname(itemSp.getBisname());
					s.setBisnb(itemSp.getBisnb());
					s.setBisspecifications(itemSp.getBisspecifications());
					s.setBisdescription(itemSp.getBisdescription());
					// 如果有標記自動?
					if (s.getBisiauto()) {
						entityNews.add(s);
					}
					sqlQueryEntitys.get(s.getBisnb()).setSysstatus(2);// 無效狀態
				} else {
					// 沒比對到->移除(除了customize例外)
					if (s.getBisnb().contains("customize")) {
						// entityNews.add(s);
					} else {
						// 如果有標記自動?
						if (s.getBisiauto()) {
							specificationsDao.delete(s);
						}
					}
				}
			});

			// 可能有新的?
			sqlQueryEntitys.forEach((k, v) -> {
				if (v.getSysstatus() == 0) {
					// 如果有標記自動?
					if (v.getBisiauto()) {
						specificationsDao.save(v);
					}
				}
			});
			// 物料排序->修改更新
			if (entityNews.size() > 0) {
				entityNews.sort((o1, o2) -> o1.getBisnb().compareTo(o2.getBisnb()));
				specificationsDao.saveAll(entityNews);
			}
		});

		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomItemSpecifications> entityDatas = new ArrayList<>();
		Map<String, String> entitySame = new HashMap<String, String>();// 敘述+物料號
		// =======================資料檢查=======================
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<BomItemSpecifications>>() {
					});

			// Step2.資料檢查
			String getBisgname = "";
			for (BomItemSpecifications entityData : entityDatas) {

				// 檢查-新資料-名稱重複
				if (entitySame.containsKey(entityData.getBisfname())) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entitySame.get(entityData.getBisfname()) + ":" + entityData.getBisnb() });
				}
				entitySame.put(entityData.getBisfname(), entityData.getBisnb());
				// 檢查資料-正規化值?
				if (entityData.getBisfname().equals("[\"\"]")) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getBisnb() });
				}
				// 檢查 資料完整性
				// Step2-4.資料檢查-缺少值?
				if (entityData.getBisgcondition() == null || entityData.getBisgcondition().equals("") || //
						entityData.getBisgname() == null || entityData.getBisgname().equals("") || //
						entityData.getBisgffield() == null || entityData.getBisgffield().equals("") || //
						entityData.getBisgfname() == null || entityData.getBisgfname().equals("") || //
						entityData.getBisgsplit() == null || entityData.getBisgsplit().equals("") || //
						entityData.getBisfname() == null || entityData.getBisfname().equals("[\"\"]") || //
						entityData.getSyssort() == null) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { "Please check again Or Use copy Data!" });
				}
				// 檢查群組名稱是否重複?
				if (getBisgname.equals("") || !getBisgname.equals(entityData.getBisgname())) {
					ArrayList<BomItemSpecifications> checkBIS = specificationsDao
							.findAllByCheck(entityData.getBisgname(), null, null);
					getBisgname = entityData.getBisgname();
					for (BomItemSpecifications checkOneBIS : checkBIS) {
						// 如果不同群組 卻相同GID 則不可更新
						Long checkBisgid = checkOneBIS.getBisgid();
						Long entityBisgid = entityData.getBisgid();
						if (!checkBisgid.equals(entityBisgid)) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
									new String[] {
											"Item group name has been used already. : " + checkOneBIS.getBisgname() });
						} else {
							break;
						}
					}
				}
			}
			// 自動狀態 須關閉才能修改
			ArrayList<BomItemSpecifications> oldDatasCheck = specificationsDao
					.findAllByBisgid(entityDatas.get(0).getBisgid());
			if (oldDatasCheck.size() > 0 && oldDatasCheck.get(0).getBisiauto() && entityDatas.get(0).getBisiauto()) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
						new String[] { "Please turn off Auto : " + entityDatas.get(0).getBisiauto() });
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
		ArrayList<BomItemSpecifications> saveDatasNewUpdate = new ArrayList<>();
		ArrayList<BomItemSpecifications> saveDatasUpdate = new ArrayList<>();
		ArrayList<BomItemSpecifications> saveDatasRemove = new ArrayList<>();
		Map<Long, BomItemSpecifications> entityMapDatas = new HashMap<Long, BomItemSpecifications>();
		entityDatas.forEach(c -> {// 要更新的資料
			if (c.getBisid() == null) {
				// 可能->新的?
				c.setBisid(null);
				c.setBisgid(bisgid);
				c.setBislevel(0);
				c.setSysmdate(new Date());
				c.setSysmuser(packageBean.getUserAccount());
				c.setSysodate(new Date());
				c.setSysouser(packageBean.getUserAccount());
				c.setSyscdate(new Date());
				c.setSyscuser(packageBean.getUserAccount());
				c.setSysheader(false);
				c.setSysstatus(0);
				saveDatasNewUpdate.add(c);
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
				entityDataOld.setBisfname(x.getBisfname().replaceAll("[\\n\\r\\t]", "").replaceAll("\\\\[nrt]", ""));
				entityDataOld.setBisgfname(x.getBisgfname());
				entityDataOld.setBisgffield(x.getBisgffield());
				entityDataOld.setBisgname(x.getBisgname());
				entityDataOld.setBisgsplit(x.getBisgsplit());
				entityDataOld.setBisgcondition(x.getBisgcondition());
				entityDataOld.setBislevel(x.getBislevel() == null ? 0 : x.getBislevel());
				// 勾選
				entityDataOld.setBisproduct(x.getBisproduct());
				entityDataOld.setBisaccessories(x.getBisaccessories());
				entityDataOld.setBissfproduct(x.getBissfproduct());
				entityDataOld.setBisdevelopment(x.getBisdevelopment());
				entityDataOld.setBispcb(x.getBispcb());
				//
				entityDataOld.setBismproduct(x.getBismproduct());
				entityDataOld.setBismaccessories(x.getBismaccessories());
				entityDataOld.setBismsfproduct(x.getBismsfproduct());
				entityDataOld.setBismdevelopment(x.getBismdevelopment());
				entityDataOld.setBismpcb(x.getBismpcb());
				//
				entityDataOld.setBisiauto(x.getBisiauto());
				entityDataOld.setBisdselect(x.getBisdselect());
				//
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysodate(new Date());
				entityDataOld.setSyssort(x.getSyssort());
				saveDatasUpdate.add(entityDataOld);
			} else {
				// 沒比對到->移除
				saveDatasRemove.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		specificationsDao.deleteAll(saveDatasRemove);
		specificationsDao.flush(); // <- 強制立即同步刪除到資料庫
		specificationsDao.saveAll(saveDatasUpdate);
		specificationsDao.flush(); // <- 強制立即同步刪除到資料庫
		specificationsDao.saveAll(saveDatasNewUpdate);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomItemSpecifications> entityDatas = new ArrayList<>();
		ArrayList<BomItemSpecifications> entitySave = new ArrayList<>();
		Map<String, String> entitySame = new HashMap<String, String>();// 敘述+物料號
		// =======================資料檢查=======================
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityDetailJson(),
					new TypeReference<ArrayList<BomItemSpecifications>>() {
					});
			// Step2-1.資料檢查
			for (BomItemSpecifications entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<BomItemSpecifications> checkDatas = specificationsDao.findAllByCheck(entityData.getBisgname(),
						entityData.getBisfname(), null);
				if (checkDatas.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getBisgname() + " -> " + checkDatas.get(0).getBisname() });
				}

				// Step2-2.檢查-新資料-群組名稱重複
				ArrayList<BomItemSpecifications> checkDatasGname = specificationsDao
						.findAllByCheck(entityData.getBisgname(), null, null);
				if (checkDatasGname.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getBisgname() });
				}

				// Step2-3.資料檢查-檢查基本重複?-> 檢查-新資料-名稱重複
				if (entitySame.containsKey(entityData.getBisfname())) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entitySame.get(entityData.getBisfname()) + ":" + entityData.getBisnb() });
				}
				entitySame.put(entityData.getBisfname(), entityData.getBisnb());

				// Step2-4.資料檢查-缺少值?
				if (entityData.getBisgcondition() == null || entityData.getBisgcondition().equals("") || //
						entityData.getBisgname() == null || entityData.getBisgname().equals("") || //
						entityData.getBisgffield() == null || entityData.getBisgffield().equals("") || //
						entityData.getBisgfname() == null || entityData.getBisgfname().equals("") || //
						entityData.getBisgsplit() == null || entityData.getBisgsplit().equals("") || //
						entityData.getBisfname() == null || entityData.getBisfname().equals("[\"\"]") || //
						entityData.getSyssort() == null) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { "Please check again" });
				}
				// 自動狀態 須關閉才能修改
				ArrayList<BomItemSpecifications> oldDatasCheck = specificationsDao
						.findAllByBisgid(entityDatas.get(0).getBisgid());
				if (oldDatasCheck.size() > 0 && oldDatasCheck.get(0).getBisiauto()
						&& entityDatas.get(0).getBisiauto()) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "Please turn off Auto : " + entityDatas.get(0).getBisiauto() });
				}
			}
		}
		// =======================資料整理=======================

		Long bisgid = specificationsDao.getBomItemSpecificationsGroupGSeq();
		for (BomItemSpecifications x : entityDatas) {
			// 新增
			x.setBisid(null);
			x.setBisgid(bisgid);
			x.setSysstatus(0);
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSysheader(false);
			x.setBislevel(x.getBislevel() == null ? 0 : x.getBislevel());
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
			// 自動狀態 須關閉才能修改
			ArrayList<BomItemSpecifications> oldDatasCheck = specificationsDao
					.findAllByBisgid(entityDatas.get(0).getBisgid());
			if (oldDatasCheck.size() > 0 && oldDatasCheck.get(0).getBisiauto() && entityDatas.get(0).getBisiauto()) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
						new String[] { "Please turn off Auto : " + entityDatas.get(0).getBisiauto() });
			}
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
			// 自動狀態 須關閉才能修改
			ArrayList<BomItemSpecifications> oldDatasCheck = specificationsDao
					.findAllByBisgid(entityDatas.get(0).getBisgid());
			if (oldDatasCheck.size() > 0 && oldDatasCheck.get(0).getBisiauto() && entityDatas.get(0).getBisiauto()) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
						new String[] { "Please turn off Auto : " + entityDatas.get(0).getBisiauto() });
			}
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
		nativeQuery += " LIMIT 10000 OFFSET 0 ";
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

	public boolean getSynAllBom() throws Exception {
		boolean check = true;
		// Step3-1.取得資料(一般/細節)
		ArrayList<BomItemSpecifications> entityItems = specificationsDao.findAllBySearch(null, null, null, null);
		Map<String, HashMap<String, BomItemSpecifications>> itemsMap = new HashMap<String, HashMap<String, BomItemSpecifications>>();
		// GID,<物料號orID_XXXXX,物件>
		for (BomItemSpecifications itemsOne : entityItems) {
			HashMap<String, BomItemSpecifications> newOne = new HashMap<String, BomItemSpecifications>();
			String bisid_bisnb = "";
			// 非物料 or 是物料
			if (itemsOne.getBisnb().equals("customize")) {
				bisid_bisnb = "id_" + itemsOne.getBisid();
			} else {
				bisid_bisnb = itemsOne.getBisnb();
			}
			// 有沒有紀錄?
			if (itemsMap.containsKey(itemsOne.getBisgid() + "")) {
				// 有
				newOne = itemsMap.get(itemsOne.getBisgid() + "");
				newOne.put(bisid_bisnb, itemsOne);
				itemsMap.put(itemsOne.getBisgid() + "", newOne);
			} else {
				// 無
				newOne.put(bisid_bisnb, itemsOne);
				itemsMap.put(itemsOne.getBisgid() + "", newOne);
			}
		}

		// Step3-2.資料區分(一般/細節)
		ArrayList<BomProductManagement> entityBoms = managementDao.findAllBySearch("90-", null, null, null, null, null);

		// 收集「真的有變更」的資料列，最後批次 saveAll
		List<BomProductManagement> dirty = new ArrayList<>();
		List<BomProductManagement> bomSynErp = new ArrayList<>();

		// Gson：若 JSON 內含 HTML 內容，建議用 disableHtmlEscaping 避免被轉義
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();

		for (BomProductManagement b : entityBoms) {
			String raw = b.getBpmbisitem();
			if (raw == null || raw.isBlank()) {
				// 無 JSON 可處理 → 略過
				continue;
			}

			// === 1) 解析 JSON ===
			JsonObject root;
			try {
				root = JsonParser.parseString(raw).getAsJsonObject();
			} catch (Exception ex) {
				// 若遇壞資料：可記錄警示或另行補救
				// log.warn("bpmbisitem 解析失敗, id={}, raw={}", b.getId(), raw, ex);
				continue;
			}

			// 取得 items 陣列（若不存在或型別錯誤則略過）
			if (!root.has("items") || !root.get("items").isJsonArray()) {
				continue;
			}
			JsonArray items = root.getAsJsonArray("items");

			boolean changed = false; // 本筆是否有任何改動

			// === 2) 逐筆合併 ===
			for (JsonElement el : items) {
				if (!el.isJsonObject()) {
					// 非物件（例如字串/數字），不處理
					continue;
				}
				JsonObject iiObj = el.getAsJsonObject();

				// 取三個關鍵欄位（防 null）
				String bisgid = getAsString(iiObj, "bisgid");
				String bisnb = getAsString(iiObj, "bisnb");
				String bisid = getAsString(iiObj, "bisid");

				if (bisgid == null) {
					// 群組不存在 → 無法匹配 → 略過
					continue;
				}

				// 取出群組
				Map<String, BomItemSpecifications> group = itemsMap.get(bisgid);
				if (group == null || group.isEmpty()) {
					continue;
				}

				// 依規則決定 key（優先 bisnb，且不可為 "customize"）
				String key = null;
				if (bisnb != null && !"customize".equals(bisnb) && group.containsKey(bisnb)) {
					key = bisnb;
				} else if (bisid != null && group.containsKey("id_" + bisid)) {
					key = "id_" + bisid;
				}
				if (key == null) {
					continue; // 找不到對應規則 → 略過
				}

				BomItemSpecifications spec = group.get(key);
				if (spec == null) {
					continue;
				}

				// === 3) 僅在不同時寫入（避免多餘 UPDATE） ===
				// 你目前要求的三個欄位，後續可自由增減
				changed |= addOrOverwriteIfDifferent(iiObj, "bisgfname", spec.getBisgfname()); // 格式:"產品 版次"
				changed |= addOrOverwriteIfDifferent(iiObj, "bisgname", spec.getBisgname()); // 項目組名稱:"PCBA"
				changed |= addOrOverwriteIfDifferent(iiObj, "bisfname", spec.getBisfname()); // 項目格式化:"[\"DT340TR
																								// MB...\",\"1.2\"]"
			}

			// === 4) 有改動才寫回欄位 ===
			if (changed) {
				String updatedJson = gson.toJson(root); // 將 root 轉回字串
				b.setBpmbisitem(updatedJson);
				dirty.add(b);
			}
		}

		// === 5) 批次儲存（只有髒資料） ===
		if (!dirty.isEmpty()) {
			managementDao.saveAll(dirty);
			// 視需求：若後續馬上要讀一致資料，可立刻 flush
			// managementDao.flush();
		}
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "bbisnnb"));// BOM+序號
		// 一般模式
		PageRequest pageable = PageRequest.of(0, 1000, Sort.by(orders));
		// 同步BOM 與 規格BOM一致性
		for (BomProductManagement b : entityBoms) {
			// 取得產品-物料
			ArrayList<BasicBomIngredients> bomIngredients = bomIngredientsDao.findAllBySearch(b.getBpmnb(), null, null,
					null, 1, null, pageable);

			// ===== 新 BOM：ERP 抓回的 BOM，格式 mat_qty_process_level =====
			JsonArray bomNewArray = new JsonArray();
			bomIngredients.forEach(e -> {
				// 組成物料_數量_製成_階層(預設都是1)
				bomNewArray.add(e.getBbiisn() + "_" + e.getBbiiqty() + "_" + e.getBbiiprocess() + "_1");
			});

			// ===== 讀取 Cloud JSON 欄位(避免各種異常) =====
			String raw = b.getBpmbisitem();
			if (raw == null || raw.isBlank()) {
				continue;
			}

			JsonObject root;
			try {
				root = JsonParser.parseString(raw).getAsJsonObject();
			} catch (Exception ex) {
				continue;
			}

			if (!root.has("items") || !root.get("items").isJsonArray() || !root.has("basic")
					|| !root.get("basic").isJsonArray()) {
				continue;
			}

			// 格式化內容
			JsonArray items = root.getAsJsonArray("items");// Cloud 規格:KeyPart
			JsonArray basic = root.getAsJsonArray("basic");// Cloud 規格:物料
			// ===== 基本 BOM 舊結構（Cloud basic + Cloud items 中的 Key Parts）=====
			JsonArray basicItems = basic.deepCopy(); // 舊的BOM
			// ===== groupChange：蒐集 Key Part 的料號 -> spec =====
			// key: (GID)
			// val: Map<物料號, BomItemSpecifications>
			Map<Long, Map<String, BomItemSpecifications>> groupChange = new HashMap<>();
			// 為了快速判斷某料號是否曾是 Key Part（用在 basic 重建時排除）
			// key: 料號
			Set<String> keyPartMaterials = new HashSet<>();
			// ===== 掃描 items，找 Key Part（舊 Cloud 結構）=====
			for (JsonElement el : items) {

				if (!el.isJsonObject())
					continue;

				JsonObject ii = el.getAsJsonObject();
				String bisgidStr = getAsString(ii, "bisgid");
				String bisnb = getAsString(ii, "bisnb");
				String qty = getAsString(ii, "bisqty");
				String process = getAsString(ii, "bisprocess");
				String level = getAsString(ii, "bislevel");

				if (bisgidStr == null)
					continue;
				if (bisnb == null || "customize".equals(bisnb))
					continue;
				if (!"1".equals(level))
					continue; // 不是第一階層的略過-> Key Part 條件

				// 找到此群組的規格表 (假設 itemsMap 的 key 是 String 型別的 bisgid)
				Map<String, BomItemSpecifications> group = itemsMap.get(bisgidStr);
				if (group == null || group.isEmpty())
					continue;

				// 轉成 Long 型別 GID
				Long gid = null;
				try {
					gid = Long.valueOf(bisgidStr);
				} catch (NumberFormatException ex) {
					// 若資料有問題，這一筆略過
					continue;
				}

				// 取得或建立此群組底下的「料號 -> spec」map
				Map<String, BomItemSpecifications> byMaterialMap = groupChange.computeIfAbsent(gid,
						k -> new HashMap<>());

				// 將此群組底下的所有 Key Part 規格都掃描一次，建立完整索引
				for (BomItemSpecifications spec : group.values()) {
					String m = spec.getBisnb();
					if (m == null)
						continue;

					byMaterialMap.put(m, spec); // 建立 (gid -> {mat -> spec})
					keyPartMaterials.add(m); // 記錄：此料號是 Key Part 候選
				}

				// 加入舊 basic 額外資訊（舊 items 也視為 basic）
				basicItems.add(bisnb + "_" + qty + "_" + process + "_" + level + "_" + bisgidStr);
			}

			// ============================
			// oldMap / newMap 建構->為了更好比對 BOM物料差異<組成物料:組成物料_數量_製成_階層_使用的KeyPart項目群組GID>
			// ============================

			Map<String, BasicLine> oldMap = new HashMap<>();
			for (JsonElement e : basicItems) {
				if (e == null || e.isJsonNull())
					continue;
				BasicLine line = BasicLine.parseOld(e.getAsString());
				if (line.getMaterial() != null && !line.getMaterial().isBlank()) {
					oldMap.put(line.getMaterial(), line);
				}
			}

			Map<String, BasicLine> newMap = new HashMap<>();
			for (JsonElement e : bomNewArray) {
				if (e == null || e.isJsonNull())
					continue;
				BasicLine line = BasicLine.parseNew(e.getAsString());
				if (line.getMaterial() != null && !line.getMaterial().isBlank()) {
					newMap.put(line.getMaterial(), line);
				}
			}
			System.out.println(basicItems.toString());
			System.out.println(bomNewArray.toString());
			// ============================
			// 3. 計算差異（新增／移除／修改）
			// ============================
			// removedMaterials：舊有有、但新 BOM 沒有 → 視為「移除」
			Set<String> removedMaterials = new HashSet<>();
			// addedMaterials：新 BOM 有、但舊有沒有 → 視為「新增」
			Set<String> addedMaterials = new HashSet<>();
			// modifiedMaterials：舊、新都有同料號，但 qty / process / level 任一不同 → 視為「修改」
			Set<String> modifiedMaterials = new HashSet<>();

			// 舊有 -> 新沒有
			for (String mat : oldMap.keySet()) {
				if (!newMap.containsKey(mat))
					removedMaterials.add(mat);
			}

			// 新有 -> 舊沒有 或 修改數量/製程/階層
			for (String mat : newMap.keySet()) {
				BasicLine n = newMap.get(mat);
				BasicLine o = oldMap.get(mat);

				if (o == null) {// 舊沒有 → 純新增
					addedMaterials.add(mat);
					continue;
				}
				// 比較 qty / process / level 任一欄位是否不同
				boolean changed = !o.getQty().equals(n.getQty()) || !o.getProcess().equals(n.getProcess())
						|| !o.getLevel().equals(n.getLevel());

				if (changed)
					modifiedMaterials.add(mat);
			}

			// ============================
			// 4. 以群組 bisgid 為單位進行 Items 操作
			// 說明：
			// - 此設計是「group 為單位」而非「單一料號為單位」
			// - 一旦某料號 (Key Part) 移除或修改，就以其群組 bisgid 決定整組 items 的刪除或替換
			// ============================
			// groupsToRemove：需整組刪除的群組 ID 集合
			Set<Long> groupsToRemove = new HashSet<>();
			// groupsToAdd：需新增的群組 ID 集合
			Map<Long, BomItemSpecifications> groupsToAdd = new HashMap<>();
			// groupsToReplace：需整組更新的群組 (bisgid → 新的 BomItemSpecifications)
			Map<Long, BomItemSpecifications> groupsToReplace = new HashMap<>();

			// 移除：舊有有但新 BOM 沒有的料號 -> 此料號出現在哪些群組，這些群組就要移除
			for (String mat : removedMaterials) {
				for (Map.Entry<Long, Map<String, BomItemSpecifications>> entry : groupChange.entrySet()) {
					Long gid = entry.getKey();
					Map<String, BomItemSpecifications> byMat = entry.getValue();

					if (byMat.containsKey(mat)) {
						groupsToRemove.add(gid);
					}
				}
			}
			// 新增：新有有但舊 BOM 沒有的料號 -> 此料號出現在哪些群組，這些群組就要新增
			for (String mat : addedMaterials) {
				for (Map.Entry<Long, Map<String, BomItemSpecifications>> entry : groupChange.entrySet()) {
					Long gid = entry.getKey();
					Map<String, BomItemSpecifications> byMat = entry.getValue();

					if (byMat.containsKey(mat)) {
						BomItemSpecifications spec = byMat.get(mat);
						groupsToAdd.put(gid, spec);
					}
				}
			}

			// 修改：舊/新都有此料號，但 qty/process/level 有變化 -> 出現此料號的群組都要更新
			for (String mat : modifiedMaterials) {
				for (Map.Entry<Long, Map<String, BomItemSpecifications>> entry : groupChange.entrySet()) {
					Long gid = entry.getKey();
					Map<String, BomItemSpecifications> byMat = entry.getValue();

					if (byMat.containsKey(mat)) {
						BomItemSpecifications spec = byMat.get(mat);
						groupsToReplace.put(gid, spec);
					}
				}
			}

			// ===== 生成新的 items =====
			JsonArray newItems = new JsonArray();

			for (JsonElement el : items) {
				if (!el.isJsonObject()) {// 異常項目直接略過
					continue;
				}

				JsonObject obj = el.getAsJsonObject();
				Long bisgid = obj.has("bisgid") ? obj.get("bisgid").getAsLong() : null;

				if (bisgid != null) {

					// 整組移除:若此群組在 groupsToRemove 中 & 不在 groupsToAdd 中 → 整組移除，不再加入 newItems
					if (groupsToRemove.contains(bisgid) && !groupsToAdd.containsKey(bisgid)) {
						continue;
					}

					// 新增 : groupsToAdd 中
					if (groupsToAdd.containsKey(bisgid)) {
						BomItemSpecifications spec = groupsToAdd.get(bisgid);
						BasicLine n = newMap.get(spec.getBisnb());
						if (n == null) {
							// 理論上不應發生，若發生代表 spec 與新 BOM 不一致，略過這筆群組更新
							continue;
						}
						JsonObject newItem = new JsonObject();
						newItem.addProperty("bisid", spec.getBisid() + "");
						newItem.addProperty("bisgid", spec.getBisgid() + "");
						newItem.addProperty("bisnb", spec.getBisnb());
						newItem.addProperty("bisname", spec.getBisname());
						newItem.addProperty("bisqty", n.getQty());
						newItem.addProperty("bisgname", spec.getBisgname());
						newItem.addProperty("bisgfname", spec.getBisgfname());
						newItem.addProperty("bisfname", spec.getBisfname());
						newItem.addProperty("bissdescripion", spec.getBissdescripion());
						newItem.addProperty("bisprocess", n.getProcess());
						newItem.addProperty("bislevel", n.getLevel());
						// 將更新後的項目加入 newItems
						newItems.add(newItem);
						// 注意：這裡是「整組替換」，所以舊的同群組項目全部略過
						continue;
					}

					// 整組更新:若此群組在 groupsToReplace 中 → 整組改為最新 spec
					if (groupsToReplace.containsKey(bisgid)) {
						BomItemSpecifications spec = groupsToReplace.get(bisgid);
						BasicLine n = newMap.get(spec.getBisnb());
						if (n == null) {
							// 理論上不應發生，若發生代表 spec 與新 BOM 不一致，略過這筆群組更新
							continue;
						}
						JsonObject newItem = new JsonObject();
						newItem.addProperty("bisid", spec.getBisid() + "");
						newItem.addProperty("bisgid", spec.getBisgid() + "");
						newItem.addProperty("bisnb", spec.getBisnb());
						newItem.addProperty("bisname", spec.getBisname());
						newItem.addProperty("bisqty", n.getQty());
						newItem.addProperty("bisgname", spec.getBisgname());
						newItem.addProperty("bisgfname", spec.getBisgfname());
						newItem.addProperty("bisfname", spec.getBisfname());
						newItem.addProperty("bissdescripion", spec.getBissdescripion());
						newItem.addProperty("bisprocess", n.getProcess());
						newItem.addProperty("bislevel", n.getLevel());
						// 將更新後的項目加入 newItems
						newItems.add(newItem);
						// 注意：這裡是「整組替換」，所以舊的同群組項目全部略過
						continue;
					}
				}

				// 沒被處理的保留:不在移除或更新清單的項目 → 原封不動加入 newItems
				newItems.add(obj);
			}
			// 新增
			// ============================
			// 4-1. 針對「新增的 Key Part」補上對應的 items
			// ============================

			for (String mat : addedMaterials) {

				// 若此料號沒有出現在任何 Key Part 群組 → 不需建立 items，由 basic 管即可
				if (!keyPartMaterials.contains(mat)) {
					continue;
				}

				// 在所有群組裡找出哪些群組有這個料號
				for (Map.Entry<Long, Map<String, BomItemSpecifications>> entry : groupChange.entrySet()) {
					Long gid = entry.getKey();
					Map<String, BomItemSpecifications> byMat = entry.getValue();

					if (!byMat.containsKey(mat)) {
						continue;
					}

					// 若該群組已被標記為整組移除，就不用再為這個群組新增
					if (groupsToRemove.contains(gid)) {
						continue;
					}

					BomItemSpecifications spec = byMat.get(mat);
					BasicLine n = newMap.get(mat);
					if (n == null) {
						// 理論上不會發生，防護一下
						continue;
					}

					JsonObject newItem = new JsonObject();
					newItem.addProperty("bisid", String.valueOf(spec.getBisid()));
					newItem.addProperty("bisgid", String.valueOf(spec.getBisgid()));
					newItem.addProperty("bisnb", spec.getBisnb());
					newItem.addProperty("bisname", spec.getBisname());
					newItem.addProperty("bisqty", n.getQty());
					newItem.addProperty("bisgname", spec.getBisgname());
					newItem.addProperty("bisgfname", spec.getBisgfname());
					newItem.addProperty("bisfname", spec.getBisfname());
					newItem.addProperty("bissdescripion", spec.getBissdescripion());
					newItem.addProperty("bisprocess", n.getProcess());
					newItem.addProperty("bislevel", n.getLevel());

					newItems.add(newItem);
				}
			}

			// 替換原本 items
			items = newItems;

			// ============================
			// 5. 重建 basic（非 Key Part 才會放 basic）
			// 原則：
			// - 在 newMap 中的所有料號逐一檢查：
			// * 若料號出現在 groupChange (代表是 Key Part) → 由 items 管，不放 basic
			// * 其它料號 → 寫入 basicNewFin
			// ============================

			JsonArray basicNewFin = new JsonArray();
			for (String mat : newMap.keySet()) {
				BasicLine n = newMap.get(mat);
				// 若料號曾出現在任何 Key Part 群組，代表由 items 管，不放 basic
				boolean isKeyPart = keyPartMaterials.contains(mat);

				if (!isKeyPart) {
					basicNewFin.add(n.toBasicString());
				}
			}

			// ============================
			// 6. 寫回 JSON
			// ============================

			boolean changed = !removedMaterials.isEmpty() || !addedMaterials.isEmpty() || !modifiedMaterials.isEmpty();

			if (changed) {
				// 排序(basicNewFin)
				// basicNewFin 排序
				List<String> list = new ArrayList<>();
				for (JsonElement el : basicNewFin) {
					if (!el.isJsonNull()) {
						list.add(el.getAsString());
					}
				}
				// 字串排序：從小排到大
				Collections.sort(list);
				JsonArray sortedBasic = new JsonArray();
				list.forEach(sortedBasic::add);
				basicNewFin = sortedBasic;
				root.add("basic", basicNewFin);
				root.add("items", items);
				//
				String updatedJson = gson.toJson(root);
				b.setBpmbisitem(updatedJson);
				bomSynErp.add(b);
			}
		}
		if (!bomSynErp.isEmpty()) {
			String reBom = new String();
			for (BomProductManagement o : bomSynErp) {
				reBom += o.getBpmnb() + "_";
			}
			System.out.println("update:" + reBom);
			managementDao.saveAll(bomSynErp);
			// 視需求：若後續馬上要讀一致資料，可立刻 flush
			managementDao.flush();
		}

		return check;
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
