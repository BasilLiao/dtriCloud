package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BomItemSpecificationsDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomItemSpecificationsDetailFront;
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
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

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

	// 自動測試與更新
	public PackageBean getAutoSearchTestAndUpdate(PackageBean packageBean) {
		Map<Long, List<BomItemSpecifications>> entitysMap = new HashMap<Long, List<BomItemSpecifications>>();
		// =======================舊有資料?=======================
		List<BomItemSpecifications> entitysData = specificationsDao.findAll();
		// 過濾->只取得GID
		entitysData.forEach(s -> {
			if (entitysMap.containsKey(s.getBisgid())) {
				List<BomItemSpecifications> list = entitysMap.get(s.getBisgid());
				list.add(s);
				entitysMap.put(s.getBisgid(), list);
			} else {
				List<BomItemSpecifications> list = new ArrayList<BomItemSpecifications>();
				list.add(s);
				entitysMap.put(s.getBisgid(), list);
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
			// 共用參數
			Long bisgid = ks;// GID
			Boolean bisiauto = vs.get(0).getBisiauto();// 自動?
			Boolean bisdselect = vs.get(0).getBisdselect();// 預設選擇?
			Boolean bispcb = vs.get(0).getBispcb();
			Boolean bisproduct = vs.get(0).getBisproduct();
			Boolean bissfproduct = vs.get(0).getBissfproduct();
			Boolean bisaccessories = vs.get(0).getBisaccessories();
			Boolean bisdevelopment = vs.get(0).getBisdevelopment();
			String bisgfname = vs.get(0).getBisgfname();// 正規畫-群組名稱
			Integer bisgfnameSize = bisgfname.split(" ").length;
			String bisprocess = vs.get(0).getBisprocess();
			String bisgname = vs.get(0).getBisgname();
			String bisgffield = vs.get(0).getBisgffield();
			String bisgsplit = vs.get(0).getBisgsplit();// 排序
			Integer syssort = vs.get(0).getSyssort();// 排序

			String bisgcondition = vs.get(0).getBisgcondition().replaceAll("(<AND>|<OR>)", "<@@>$1");
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
				itemSp.setBispcb(bispcb);// PCBA主板
				itemSp.setBisproduct(bisproduct);// 產品
				itemSp.setBisaccessories(bisaccessories);//
				itemSp.setBisdevelopment(bisdevelopment);//
				itemSp.setBisprocess(bisprocess);//
				itemSp.setBisgname(bisgname);//
				itemSp.setBisgffield(bisgffield);//
				itemSp.setBissfproduct(bissfproduct);//
				itemSp.setBisgsplit(bisgsplit);
				itemSp.setBisgcondition(bisgcondition);// 區分

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
				// 將 JsonArray 轉成字串並存入 itemSp
				itemSp.setBisfname(jsonArray.toString());

				itemSp.setSyssort(syssort);
				sqlQueryEntitys.put(itemSp.getBisnb(), itemSp);// 物料號
				// entitys.add(itemSp);
			});

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
					// 如果有標記自動?
					if (s.getBisiauto()) {
						entityNews.add(s);
					}
					sqlQueryEntitys.get(s.getBisnb()).setSysstatus(2);// 無效狀態
				} else {
					// 沒比對到->移除(除了customize例外)
					if (s.getBisnb().equals("customize")) {
						entityNews.add(s);
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
					// entityNews.add(v);
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
						entityData.getBisgsplit() == null || entityData.getBisgsplit().equals("") || //
						entityData.getBisfname() == null || entityData.getBisfname().equals("[\"\"]") || //
						entityData.getSyssort() == null) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { "Please check again" });
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
