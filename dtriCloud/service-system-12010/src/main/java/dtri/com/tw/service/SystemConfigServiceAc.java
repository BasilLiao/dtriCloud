package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.SystemConfigDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.SystemConfig;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
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
public class SystemConfigServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemConfigDao configDao;

	@Autowired
	private SystemLanguageCellDao languageDao;

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
		orders.add(new Order(Direction.ASC, "scgid"));
		orders.add(new Order(Direction.ASC, "scname"));
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));
		// 細節模式
		PageRequest pageableDetail = PageRequest.of(0, 100000, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			// Step3-1.取得資料(一般/細節)
			ArrayList<SystemConfig> entitys = configDao.findAllByConfig(null, null, null, null, 0, true, pageable);
			ArrayList<SystemConfig> entityDetails = configDao.findAllByConfig(null, null, null, null, 0, false, pageableDetail);
			// Step3-2.資料區分(一般/細節)

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("SystemConfig", null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao.findAllByLanguageCellSame("SystemConfig_Detail", null, 2);
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemConfig.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("systemConfig");
			exceptionCell.add("systemConfigs");
			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			// 欄位翻譯(細節)
			resultDetailTJsons = packageService.resultSet(fields, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "scname", "Ex:DB_NAME", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			searchJsons = packageService.searchSet(searchJsons, null, "scgname", "Ex:DATA_BKUP", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// 查詢項目-時間開始
			searchJsons = packageService.searchSet(searchJsons, null, "sysmdatestart", "Ex:2011-01-02 12:12:00", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_2);
			// 查詢項目-時間結束
			searchJsons = packageService.searchSet(searchJsons, null, "sysmdateend", "Ex:2011-01-02 12:12:00", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_2);
			// 查詢項目-狀態
			JsonArray selectArr = new JsonArray();
			selectArr.add("normal(正常)_0");
			selectArr.add("completed(完成)_1");
			selectArr.add("disabled(禁用)_2");
			selectArr.add("onlyAdmin(特權)_3");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "sysstatus", "", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			SystemConfig searchData = packageService.jsonToBean(packageBean.getEntityJson(), SystemConfig.class);
			ArrayList<SystemConfig> entitys = configDao.findAllByConfig(//
					null, searchData.getScgname(), searchData.getSysmdatestart(), searchData.getSysmdateend(), searchData.getSysstatus(), true,
					pageable);
			ArrayList<SystemConfig> entityDetails = configDao.findAllByConfig(//
					searchData.getScname(), null, null, null, null, false, pageableDetail);
			// Step3-2.資料區分(一般/細節)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJsonDatas);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new SystemConfig());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("scid_scgid");
		// 查不到資料
		if (packageBean.getEntityJson().equals("[]")) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}

		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemConfig> entityDatas = new ArrayList<>();
		ArrayList<SystemConfig> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});

			// Step2.資料檢查
			for (SystemConfig entityData : entityDatas) {
				// 檢查-群組名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemConfig> checkDatas = configDao.findAllByConfigCheck(null, entityData.getScgname(), true);
				for (SystemConfig checkData : checkDatas) {
					if (checkData.getScid().compareTo(entityData.getScid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getScgname() });
					}
				}
			}
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1. 資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});
			// Step2.資料檢查
			for (SystemConfig entityDetail : entityDetails) {
				// 檢查-名稱重複(有數量 && 不同資料有重疊)
				ArrayList<SystemConfig> checkDatas = configDao.findAllByConfigCheck(entityDetail.getScname(), entityDetail.getScgname(), false);
				for (SystemConfig checkData : checkDatas) {
					if (entityDetail.getScid() != null && checkData.getScid().compareTo(entityDetail.getScid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityDetail.getScname() });
					}
					// 如果是 複製添加(全新的會沒有ID)
					if (entityDetail.getScid() == null && checkDatas.size() > 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityDetail.getScname() });
					}
				}
				// 檢查-群組ID(沒有跟隨到群組)
				checkDatas = configDao.findAllByConfigByScgid(entityDetail.getScgid());
				if (checkDatas.size() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1002, Lan.zh_TW, new String[] { entityDetail.getScname() });
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemConfig> details = entityDetails;
		ArrayList<SystemConfig> saveDatas = new ArrayList<>();
		ArrayList<SystemConfig> saveDetails = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getScid() != null) {
				SystemConfig entityDataOld = configDao.findAllByConfigByScid(x.getScid()).get(0);
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setScgname(x.getScgname());
				entityDataOld.setScname(x.getScgname());
				entityDataOld.setSysstatus(x.getSysstatus());
				saveDatas.add(entityDataOld);
				// 細節-更新匹配內容
				details.forEach(y -> {
					// 添加
					if (y.getScgid() != null && y.getScid() == null) {
						y.setSysmdate(new Date());
						y.setSysmuser(packageBean.getUserAccount());
						y.setSysodate(new Date());
						y.setSysouser(packageBean.getUserAccount());
						y.setSyscdate(new Date());
						y.setSyscuser(packageBean.getUserAccount());
						y.setSysheader(false);
						y.setSyssort(0);
						y.setScgname(x.getScgname());
						saveDetails.add(y);
					} else if (y.getScid() != null && y.getScgid().compareTo(x.getScid()) == 0) {
						// 同一群組
						SystemConfig oldy = configDao.findAllByConfigByScid(y.getScid()).get(0);
						oldy.setSysmdate(new Date());
						oldy.setSysmuser(packageBean.getUserAccount());
						oldy.setSysstatus(y.getSysstatus());
						oldy.setSysnote(y.getSysnote());
						oldy.setScgname(x.getScgname());
						oldy.setScname(y.getScname());
						oldy.setScvalue(y.getScvalue());
						saveDetails.add(oldy);
					}
				});
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		configDao.saveAll(saveDatas);
		// 資料Detail
		configDao.saveAll(saveDetails);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<SystemConfig> entityDatas = new ArrayList<>();
		ArrayList<SystemConfig> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});

			// Step2.資料檢查
			for (SystemConfig entityData : entityDatas) {
				// 檢查-群組名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemConfig> entityDataOlds = configDao.findAllByConfigCheck(null, entityData.getScgname(), true);
				if (entityDataOlds.size() > 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW, new String[] { entityData.getScgname() });
				}
			}
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});
			// Step2.資料檢查
			for (SystemConfig entityDetail : entityDetails) {
				// 檢查-名稱重複(有數量 && 不同資料有重疊)
				ArrayList<SystemConfig> configs = configDao.findAllByConfigCheck(entityDetail.getScname(), entityDetail.getScgname(), false);
				if (configs.size() > 0 && configs.get(0).getScid() != entityDetail.getScid()) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW, new String[] { entityDetail.getScname() });
				}
			}
		}
		// =======================資料整理=======================
		// 資料Data
		ArrayList<SystemConfig> details = entityDetails;
		ArrayList<SystemConfig> entitySave = new ArrayList<>();
		entityDatas.forEach(x -> {
			Long gID = configDao.getSystemNextConfigGSeq();
			Long oldGID = x.getScgid();
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setScname(x.getScgname());
			x.setSysheader(true);
			x.setSyssort(0);
			x.setScvalue("");
			x.setScid(null);
			x.setScgid(gID);
			// 資料Data
			entitySave.add(x);
			// 資料細節-群組配對
			details.forEach(y -> {
				if (oldGID != null && oldGID == y.getScgid()) {
					// 複製的
					y.setScgname(x.getScgname());
					y.setScgid(x.getScgid());
					y.setSysmdate(new Date());
					y.setSysmuser(packageBean.getUserAccount());
					y.setSysodate(new Date());
					y.setSysouser(packageBean.getUserAccount());
					y.setSyscdate(new Date());
					y.setSyscuser(packageBean.getUserAccount());
					y.setSysheader(false);
					y.setSyssort(0);
					y.setScid(null);
					entitySave.add(y);
				} else if (oldGID == null) {
					// 新增的
					y.setScgname(x.getScgname());
					y.setScgid(x.getScgid());
					y.setSysmdate(new Date());
					y.setSysmuser(packageBean.getUserAccount());
					y.setSysodate(new Date());
					y.setSysouser(packageBean.getUserAccount());
					y.setSyscdate(new Date());
					y.setSyscuser(packageBean.getUserAccount());
					y.setSysheader(false);
					y.setSyssort(0);
					y.setScid(null);
					entitySave.add(y);
				}
			});
		});
		// =======================資料儲存=======================
		// 資料Detail
		configDao.saveAll(entitySave);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemConfig> entityDatas = new ArrayList<>();
		ArrayList<SystemConfig> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});
			// Step2.資料檢查
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1. 資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemConfig> saveDatas = new ArrayList<>();
		ArrayList<SystemConfig> saveDetails = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getScid() != null) {
				ArrayList<SystemConfig> entityDataOld = configDao.findAllByConfigByScgid(x.getScgid());
				entityDataOld.forEach(t -> {
					t.setSysmdate(new Date());
					t.setSysmuser(packageBean.getUserAccount());
					t.setSysstatus(2);
					saveDatas.add(t);
				});
			}
		});
		entityDetails.forEach(x -> {
			// 排除 沒有ID
			if (x.getScid() != null) {
				SystemConfig entityDataOld = configDao.findAllByConfigByScgid(x.getScid()).get(0);
				entityDataOld.setSyscdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDetails.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		configDao.saveAll(saveDatas);
		// 資料Detail
		configDao.saveAll(saveDetails);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemConfig> entityDatas = new ArrayList<>();
		ArrayList<SystemConfig> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});

			// Step2.資料檢查
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1. 資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemConfig>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemConfig> saveDatas = new ArrayList<>();
		ArrayList<SystemConfig> saveDetails = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getScid() != null) {
				ArrayList<SystemConfig> entityDataOld = configDao.findAllByConfigByScgid(x.getScgid());
				entityDataOld.forEach(t -> {
					saveDetails.add(t);
				});
			}
		});
		// 細節-移除內容
		entityDetails.forEach(y -> {
			// 排除 沒有ID
			if (y.getScid() != null) {
				SystemConfig entityDetailOld = configDao.findAllByConfigByScid(y.getScid()).get(0);
				saveDetails.add(entityDetailOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Detail
		configDao.deleteAll(saveDetails);
		// 資料Data
		configDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<SystemConfig> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM system_config e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("sc", "sc_");
			cellName = cellName.replace("sc_g", "sc_g_");
			String where = x.getAsString().split("<_>")[1];
			String value = x.getAsString().split("<_>").length == 2 ? "" : x.getAsString().split("<_>")[2];// 有可能空白
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
		nativeQuery += " order by e.sc_g_id desc ";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, SystemConfig.class);
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

		// 類別(細節模式)
		ArrayList<SystemConfig> entityDatas = new ArrayList<>();
		// 修正資料
		for (int s = 0; s < entitys.size(); s++) {
			SystemConfig entityOne = entitys.get(s);
			if (!entityOne.getSysheader()) {
				entityOne.setScgid(entityOne.getScid());
				entityDatas.add(entityOne);// 父類別
			}
		}
		// 資料包裝
		String entityJsonDatas = packageService.beanToJson(entityDatas);
		packageBean.setEntityJson(entityJsonDatas);

		return packageBean;
	}
}
