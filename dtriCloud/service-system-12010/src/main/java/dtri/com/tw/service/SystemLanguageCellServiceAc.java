package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.SystemPermissionDao;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.SystemPermission;
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
import jakarta.persistence.metamodel.EntityType;

@Service
public class SystemLanguageCellServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;
	@Autowired
	private SystemPermissionDao permissionDao;

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
		orders.add(new Order(Direction.ASC, "slclass"));
		orders.add(new Order(Direction.DESC, "slspcontrol"));
		orders.add(new Order(Direction.ASC, "syssort"));
		orders.add(new Order(Direction.ASC, "sltarget"));
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			// Step3-0. 事先檢查-Entity List ->取得package位置->取出Class->取出每個屬性名稱
			languageCellCheckAll();

			// Step3-1.取得資料(一般/細節)
			ArrayList<SystemLanguageCell> entitys = languageDao.findAllBySysLCell(null, null, 0, pageable);
			entitys.forEach(x -> {
				if (!x.getSllanguage().equals("")) {
					// {"zh-TW":"建立時間","zh-CN":"建立时间","en-US":"Creation time","vi-VN":"xây dựng thời
					// gian"}
					JsonObject language = packageService.StringToJson(x.getSllanguage());
					x.setSl_zhTW(language.get("zh-TW").getAsString());
					x.setSl_zhCN(language.get("zh-CN").getAsString());
					x.setSl_enUS(language.get("en-US").getAsString());
					x.setSl_viVN(language.get("vi-VN").getAsString());
				}
			});

			// Step3-2.資料區分(一般/細節)
			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllBySysLCell("SystemLanguageCell", null, 0, null);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemLanguageCell.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("sllanguage");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "slspcontrol", "Ex:DB_system_config", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sltarget", "Ex:slid", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// 查詢項目-狀態(選項_值)
			JsonArray selectArr = new JsonArray();
			selectArr.add("All(全部)_0");
			selectArr.add("Menu Item(選單)_1");
			selectArr.add("Table Cell(欄位)_2");
			selectArr.add("Message(訊息)_3");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "slclass", "", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			SystemLanguageCell searchData = packageService.jsonToBean(packageBean.getEntityJson(), SystemLanguageCell.class);
			ArrayList<SystemLanguageCell> entitys = languageDao.findAllBySysLCell(searchData.getSlspcontrol(), searchData.getSltarget(),
					searchData.getSlclass(), pageable);
			entitys.forEach(x -> {
				if (!x.getSllanguage().equals("")) {
					// {"zh-TW":"建立時間","zh-CN":"建立时间","en-US":"Creation time","vi-VN":"xây dựng thời
					// gian"}
					JsonObject language = packageService.StringToJson(x.getSllanguage());
					x.setSl_zhTW(language.get("zh-TW").getAsString());
					x.setSl_zhCN(language.get("zh-CN").getAsString());
					x.setSl_enUS(language.get("en-US").getAsString());
					x.setSl_viVN(language.get("vi-VN").getAsString());
				}
			});
			// Step3-2.資料區分(一般/細節)
			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new SystemLanguageCell());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("slid_");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemLanguageCell> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemLanguageCell>>() {
			});

			// Step2.資料檢查
			for (SystemLanguageCell entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemLanguageCell> checkDatas = languageDao.findAllByLanguageCellSame(entityData.getSlspcontrol(),
						entityData.getSltarget(), null);
				for (SystemLanguageCell checkData : checkDatas) {
					if (checkData.getSlid().compareTo(entityData.getSlid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSltarget() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemLanguageCell> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSlid() != null) {
				SystemLanguageCell entityDataOld = languageDao.findAllBySlid(x.getSlid()).get(0);
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setSysstatus(x.getSysstatus());
				entityDataOld.setSyssort(x.getSyssort());
				JsonObject language = new JsonObject();
				language.addProperty("zh-TW", x.getSl_zhTW());
				language.addProperty("zh-CN", x.getSl_zhCN());
				language.addProperty("en-US", x.getSl_enUS());
				language.addProperty("vi-VN", x.getSl_viVN());
				entityDataOld.setSllanguage(language.toString());

				// 修改
				entityDataOld.setSlcmdefval(x.getSlcmdefval());
				entityDataOld.setSlcmfixed(x.getSlcmfixed());
				entityDataOld.setSlcmmust(x.getSlcmmust());
				entityDataOld.setSlcmplaceholder(x.getSlcmplaceholder());
				entityDataOld.setSlcmselect(x.getSlcmselect());
				entityDataOld.setSlcmshow(x.getSlcmshow());
				entityDataOld.setSlcmtype(x.getSlcmtype());
				// 查詢
				entityDataOld.setSlclass(x.getSlclass());
				entityDataOld.setSlcshow(x.getSlcshow());
				entityDataOld.setSlcwidth(x.getSlcwidth());
				entityDataOld.setSlspcontrol(x.getSlspcontrol());
				entityDataOld.setSltarget(x.getSltarget());

				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		languageDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<SystemLanguageCell> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemLanguageCell>>() {
			});

			// Step2.資料檢查
			for (SystemLanguageCell entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemLanguageCell> checkDatas = languageDao.findAllByLanguageCellSame(entityData.getSlspcontrol(),
						entityData.getSltarget(), null);
				if (checkDatas.size() > 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW, new String[] { entityData.getSltarget() });
				}
			}
		}
		// =======================資料整理=======================
		// 資料Data
		entityDatas.forEach(x -> {
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSysheader(false);
			x.setSlid(null);
			JsonObject language = new JsonObject();
			language.addProperty("zh-TW", x.getSl_zhTW());
			language.addProperty("zh-CN", x.getSl_zhCN());
			language.addProperty("en-US", x.getSl_enUS());
			language.addProperty("vi-VN", x.getSl_viVN());
			x.setSllanguage(language.toString());
		});
		// =======================資料儲存=======================
		// 資料Detail
		languageDao.saveAll(entityDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemLanguageCell> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemLanguageCell>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemLanguageCell> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSlid() != null) {
				SystemLanguageCell entityDataOld = languageDao.findAllBySlid(x.getSlid()).get(0);
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		languageDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemLanguageCell> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemLanguageCell>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemLanguageCell> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSlid() != null) {
				SystemLanguageCell entityDataOld = languageDao.findAllBySlid(x.getSlid()).get(0);
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		languageDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<SystemLanguageCell> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM system_language_cell e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("sl", "sl_");
			cellName = cellName.replace("sl_c", "sl_c_");
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
		nativeQuery += " order by e.sl_sp_control asc , e.sys_sort asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, SystemLanguageCell.class);
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
			entitys.forEach(x -> {
				JsonObject language = packageService.StringToJson(x.getSllanguage());
				x.setSl_zhTW(language.get("zh-TW").getAsString());
				x.setSl_zhCN(language.get("zh-CN").getAsString());
				x.setSl_enUS(language.get("en-US").getAsString());
				x.setSl_viVN(language.get("vi-VN").getAsString());
			});
		} catch (PersistenceException e) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, Lan.zh_TW, null);
		}

		// 資料包裝
		String entityJsonDatas = packageService.beanToJson(entitys);
		packageBean.setEntityJson(entityJsonDatas);

		return packageBean;
	}

	@Transactional
	private void languageCellCheckAll() throws ClassNotFoundException {
		// Prepare.
		// 只能取得SRC內
		String packageName = "dtri.com.tw.pgsql.entity";
		URL root = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", "/"));
		System.out.println(root);
		// 取得實際物件
		EntityManager entityManager = em;
		Set<EntityType<?>> entitySet = entityManager.getMetamodel().getEntities();

		// Filter .class files.
//		File[] files = new File(root.getFile()).listFiles(new FilenameFilter() {
//			public boolean accept(File dir, String name) {
//				System.out.println(name);
//				return name.endsWith(".class");
//			}
//		});
		// 基礎屬性
		JsonArray sysstatusArr = new JsonArray();
		sysstatusArr.add("normal(正常)_0");
		sysstatusArr.add("completed(完成)_1");
		sysstatusArr.add("disabled(禁用)_2");
		sysstatusArr.add("onlyAdmin(特權)_3");
		// Find classes implementing ICommand.
		ArrayList<SystemLanguageCell> languageCells = new ArrayList<>();
		for (EntityType<?> file : entitySet) {
			String className = file.getName().replaceAll(".class", "");
			// String className = file.getName();
			Class<?> cls = Class.forName(packageName + "." + className);
			Field[] fields = cls.getDeclaredFields();
			System.out.println(className);
			// 每個欄位
			for (Field fieldOne : fields) {
				if("WarehouseSynchronize".equals(className)) {
					System.out.println(className);					
				}
				if (languageDao.findAllByLanguageCellSame(className, fieldOne.getName(), 0).size() == 0) {
					// 查詢比對後->自動建置->預設值
					// System.out.println(fieldOne.getName());
					SystemLanguageCell languageCell = new SystemLanguageCell();
					JsonObject languageJson = new JsonObject();
					//
					languageCell.setSlclass(2);// 類型?2=Table Cell 欄位
					// 編輯
					languageCell.setSlcmfixed(0);// 編輯?0=可編輯 1=固定值
					languageCell.setSlcmmust(0);// 必填?0=不必填 1=必填
					languageCell.setSlcmshow(1);// 顯示?0=不顯示 1=顯示
					languageCell.setSlcmtype("text");// 編輯屬性
					languageCell.setSlcmselect("[]");
					// 查詢
					languageCell.setSlcshow(1);// 顯示?0=不顯示 1=顯示
					languageCell.setSlcwidth(100);// 顯示寬度?
					languageCell.setSlspcontrol(className);// 對應Class
					languageCell.setSltarget(fieldOne.getName());// 對應欄位
					// 系統固定
					switch (fieldOne.getName()) {
					case "syssort":
						languageJson.addProperty("zh-TW", "排序");
						languageJson.addProperty("zh-CN", "排序");
						languageJson.addProperty("en-US", "sort");
						languageJson.addProperty("vi-VN", "xắp xếp");
						languageCell.setSlcmtype("number");// 編輯屬性
						languageCell.setSlcmdefval("0");
						languageCell.setSlcwidth(80);// 顯示寬度?
						languageCell.setSyssort(990);
						break;
					case "sysstatus":
						languageJson.addProperty("zh-TW", "狀態");
						languageJson.addProperty("zh-CN", "状态");
						languageJson.addProperty("en-US", "status");
						languageJson.addProperty("vi-VN", "tình trạng");
						languageCell.setSlcmtype("select");// 編輯屬性
						languageCell.setSlcmselect(sysstatusArr.toString());
						languageCell.setSlcmdefval("0");
						languageCell.setSyssort(991);
						break;
					case "sysheader":
						languageJson.addProperty("zh-TW", "群組標記");
						languageJson.addProperty("zh-CN", "群组标记");
						languageJson.addProperty("en-US", "Group tag");
						languageJson.addProperty("vi-VN", "thẻ nhóm");
						languageCell.setSlcmfixed(1);
						languageCell.setSlcmdefval("false");
						languageCell.setSyssort(992);
						break;
					case "sysodate":
						languageJson.addProperty("zh-TW", "擁有時間");
						languageJson.addProperty("zh-CN", "拥有时间");
						languageJson.addProperty("en-US", "Own time");
						languageJson.addProperty("vi-VN", "có thời gian");
						languageCell.setSlcmtype("datetime");// 編輯屬性
						languageCell.setSlcwidth(200);// 顯示寬度?
						languageCell.setSlcmfixed(1);
						languageCell.setSyssort(993);
						break;
					case "sysouser":
						languageJson.addProperty("zh-TW", "擁有用戶");
						languageJson.addProperty("zh-CN", "拥有用户");
						languageJson.addProperty("en-US", "Own user");
						languageJson.addProperty("vi-VN", "có người dùng");
						languageCell.setSlcmfixed(1);
						languageCell.setSyssort(994);
						break;
					case "syscdate":
						languageJson.addProperty("zh-TW", "建立時間");
						languageJson.addProperty("zh-CN", "建立时间");
						languageJson.addProperty("en-US", "Creation time");
						languageJson.addProperty("vi-VN", "xây dựng thời gian");
						languageCell.setSlcmtype("datetime");// 編輯屬性
						languageCell.setSlcwidth(200);// 顯示寬度?
						languageCell.setSlcmfixed(1);
						languageCell.setSyssort(995);
						break;
					case "syscuser":
						languageJson.addProperty("zh-TW", "建立用戶");
						languageJson.addProperty("zh-CN", "建立用户");
						languageJson.addProperty("en-US", "Creation user");
						languageJson.addProperty("vi-VN", "Tạo người dùng");
						languageCell.setSlcmfixed(1);
						languageCell.setSyssort(996);
						break;
					case "sysmdate":
						languageJson.addProperty("zh-TW", "修改時間");
						languageJson.addProperty("zh-CN", "修改时间");
						languageJson.addProperty("en-US", "Modified time");
						languageJson.addProperty("vi-VN", "Thay đổi thời gian");
						languageCell.setSlcmtype("datetime");// 編輯屬性
						languageCell.setSlcwidth(200);// 顯示寬度?
						languageCell.setSlcmfixed(1);
						languageCell.setSyssort(997);
						break;
					case "sysmuser":
						languageJson.addProperty("zh-TW", "修改用戶");
						languageJson.addProperty("zh-CN", "修改用户");
						languageJson.addProperty("en-US", "Modified user");
						languageJson.addProperty("vi-VN", "sửa đổi người dùng");
						languageCell.setSlcmfixed(1);
						languageCell.setSyssort(998);
						break;
					case "sysnote":
						languageJson.addProperty("zh-TW", "備註");
						languageJson.addProperty("zh-CN", "备注");
						languageJson.addProperty("en-US", "Note");
						languageJson.addProperty("vi-VN", "ghi chú");
						languageCell.setSlcmtype("textarea");// 編輯屬性
						languageCell.setSlcwidth(200);// 顯示寬度?
						languageCell.setSyssort(999);
						break;

					default:
						languageJson.addProperty("zh-TW", "");
						languageJson.addProperty("zh-CN", "");
						languageJson.addProperty("en-US", "");
						languageJson.addProperty("vi-VN", "");
						break;
					}

					languageCell.setSllanguage(languageJson.toString());
					languageCells.add(languageCell);
				}
			}
		}
		// Step3-0. 事先檢查-Menu->清單->取出每個屬性名稱
		List<SystemPermission> allPers = new ArrayList<>();
		allPers = permissionDao.findAll();
		allPers.forEach(p -> {
			if (p.getSpid() != 0L) {
				ArrayList<SystemLanguageCell> cells = new ArrayList<>();
				cells = languageDao.findAllByLanguageCellSame(p.getSpcontrol(), null, 1);
				// 查不到資料?新建 但是必須要有控制端
				if (cells.size() == 0 && !p.getSpcontrol().equals("")) {
					SystemLanguageCell languageCell = new SystemLanguageCell();
					JsonObject languageJson = new JsonObject();
					//
					languageCell.setSlclass(1);// 類型?1=Menu 欄位
					// 編輯
					languageCell.setSlcmfixed(0);// 編輯?0=可編輯 1=固定值
					languageCell.setSlcmmust(0);// 必填?0=不必填 1=必填
					languageCell.setSlcmshow(1);// 顯示?0=不顯示 1=顯示
					languageCell.setSlcmtype("");// 編輯屬性
					languageCell.setSlcmselect("[]");
					// 查詢
					languageCell.setSyssort(0);
					languageCell.setSlcshow(1);// 顯示?0=不顯示 1=顯示
					languageCell.setSlcwidth(100);// 顯示寬度?
					languageCell.setSlspcontrol(p.getSpcontrol());// 對應Class
					languageCell.setSltarget(p.getSpcontrol());// 對應欄位
					languageJson.addProperty("zh-TW", "");
					languageJson.addProperty("zh-CN", "");
					languageJson.addProperty("en-US", "");
					languageJson.addProperty("vi-VN", "");
					languageCell.setSllanguage(languageJson.toString());
					languageCells.add(languageCell);
				}
			}
		});
		languageDao.saveAll(languageCells);
	}
}
