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

import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.SystemPermissionDao;
import dtri.com.tw.pgsql.entity.SystemConfig;
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

@Service
public class SystemPermissionServiceAc {

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
		orders.add(new Order(Direction.ASC, "spgid"));
		orders.add(new Order(Direction.DESC, "sysheader"));
		orders.add(new Order(Direction.ASC, "syssort"));
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			String user = null;
			if (packageBean.getUserAccount().equals("admin")) {
				user = packageBean.getUserAccount();
			}
			// Step3-1.取得資料(一般/細節)
			ArrayList<SystemPermission> entitys = permissionDao.findAllByPermission(null, null, null, user, pageable);

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("SystemPermission", null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemPermission.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("language");
			exceptionCell.add("systemGroup");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "spname", "Ex:單元名稱", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "spgname", "Ex:單元權限名稱", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
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
			SystemPermission searchData = packageService.jsonToBean(packageBean.getEntityJson(), SystemPermission.class);
			String user = null;
			if (packageBean.getUserAccount().equals("admin")) {
				user = packageBean.getUserAccount();
			}

			ArrayList<SystemPermission> entitys = permissionDao.findAllByPermission(searchData.getSpname(), searchData.getSpgname(),
					searchData.getSysstatus(), user, pageable);

			// Step3-2.資料區分(一般/細節)
			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("");

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new SystemPermission());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("spid_");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemPermission> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemPermission>>() {
			});

			// Step2.資料檢查
			for (SystemPermission entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemPermission> checkDatas = permissionDao.findAllByPCheck(entityData.getSpgname(), entityData.getSpname(), null);
				for (SystemPermission checkData : checkDatas) {
					if (checkData.getSpid().compareTo(entityData.getSpid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSpname() });
					}
				}
				// 檢查-控制單位 重複(有資料 && 不是同一筆資料)
				checkDatas = permissionDao.findAllByPCheck(null, null, entityData.getSpcontrol());
				for (SystemPermission checkData : checkDatas) {
					if (checkData.getSpid().compareTo(entityData.getSpid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSpcontrol() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemPermission> saveDatas = new ArrayList<>();
		Map<String, Long> spGroupCheck = new HashMap<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSpid() != null) {
				SystemPermission entityDataOld = permissionDao.findBySpid(x.getSpid()).get(0);
				// GID 不同:取新GID/ 相同:取舊有 GID
				if (!x.getSpgname().equals(entityDataOld.getSpgname())) {
					if (spGroupCheck.containsKey(x.getSpgname())) {
						Long newGid = spGroupCheck.get(x.getSpgname());
						entityDataOld.setSpgid(newGid);
					} else {
						// 可能跟隨其他單元組
						ArrayList<SystemPermission> entityGOld = permissionDao.findAllByPCheck(x.getSpgname(), null, null);
						if (entityGOld.size() > 0) {
							// 跟隨舊群組
							entityDataOld.setSpgid(entityGOld.get(0).getSpgid());
						} else {
							// 全新
							Long newGid = permissionDao.getSystemConfigGseq();
							entityDataOld.setSpgid(newGid);
						}
					}
				}
				// 權限碼(12)
				if (x.getSppermission().length() != 12) {
					x.setSppermission("000001111111");
				}

				// Header true:清單組/false=功能項目
				if (x.getSysheader()) {
					x.setSppermission("000000000000");
					x.setSpgname(x.getSpname());
					x.setSptype(0);
					// 檢查是否有其他子目錄
					ArrayList<SystemPermission> otherItem = permissionDao.findBySpgid(x.getSpgid());
					otherItem.forEach(o -> {
						if (!o.getSysheader()) {
							o.setSpgname(x.getSpgname());
							saveDatas.add(o);
						}
					});
				}

				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setSysstatus(x.getSysstatus());
				entityDataOld.setSyssort(x.getSyssort());
				// 修改
				entityDataOld.setSysheader(x.getSysheader());
				entityDataOld.setSpgname(x.getSpgname());
				entityDataOld.setSpname(x.getSpname());
				entityDataOld.setSpcontrol(x.getSpcontrol());
				entityDataOld.setSppermission(x.getSppermission());
				entityDataOld.setSptype(x.getSptype());
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		permissionDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<SystemPermission> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemPermission>>() {
			});

			// Step2.資料檢查
			for (SystemPermission entityData : entityDatas) {
				// 群組?
				ArrayList<SystemPermission> checkDatas = new ArrayList<>();
				ArrayList<SystemPermission> checkDataGroups = new ArrayList<>();
				if (entityData.getSysheader()) {
					entityData.setSpgname(entityData.getSpname());
					checkDatas = permissionDao.findAllByPCheck(entityData.getSpgname(), null, null);
					if (checkDatas.size() > 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSpname() });
					}
				} else {
					// 一般
					checkDatas = permissionDao.findAllByPCheck(null, entityData.getSpname(), null);
					// 檢查-名稱重複(有資料 && 不是同一筆資料)
					if (checkDatas.size() > 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSpname() });
					}
					// 是否有跟隨群組?
					checkDataGroups = permissionDao.findAllByPCheck(entityData.getSpgname(), null, null);
					if (checkDataGroups.size() == 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1002, Lan.zh_TW, null);
					}
				}
				// 檢查-控制單位 重複(有資料 && 不是同一筆資料)
				checkDatas = permissionDao.findAllByPCheck(null, null, entityData.getSpcontrol());
				if (checkDatas.size() > 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getSpcontrol() });
				}
			}
		}
		// =======================資料整理=======================
		// 資料Data
		Map<String, Long> spGroupCheck = new HashMap<>();
		ArrayList<SystemPermission> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			ArrayList<SystemPermission> entityDataOld = permissionDao.findAllByPermission(null, x.getSpgname(), null, "admin", null);

			// 1.無功能組+已有建立:比對名稱相同->帶入GID
			// 2.無功能組:取新群組->登記GID
			// 3.有功能組:取就有GID
			if (spGroupCheck.containsKey(x.getSpgname())) {
				Long newGid = spGroupCheck.get(x.getSpgname());
				x.setSpgid(newGid);
			} else if (entityDataOld.size() == 0) {
				Long newGid = permissionDao.getSystemConfigGseq();
				spGroupCheck.put(x.getSpgname(), newGid);
				x.setSpgid(newGid);
				x.setSppermission("000000000000");
				x.setSpname(x.getSpgname());
				x.setSptype(0);
				x.setSysheader(true);
			} else if (entityDataOld.size() > 0) {
				x.setSpgid(entityDataOld.get(0).getSpgid());
				x.setSpgname(entityDataOld.get(0).getSpgname());
			}

			// Header true:清單組/false=功能項目
			if (x.getSysheader()) {
				x.setSppermission("000000000000");
				x.setSpname(x.getSpgname());
				x.setSptype(0);
			}

			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSpid(null);
			saveDatas.add(x);
		});
		// =======================資料儲存=======================
		// 資料Detail
		permissionDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemPermission> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemPermission>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemPermission> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSpid() != null) {
				SystemPermission entityDataOld = permissionDao.findBySpid(x.getSpid()).get(0);
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		permissionDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemPermission> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemPermission>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemPermission> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSpid() != null) {
				SystemPermission entityDataOld = permissionDao.findBySpid(x.getSpid()).get(0);
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		permissionDao.deleteAll(saveDatas);
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
		String nativeQuery = "SELECT e.* FROM system_permission e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("sp", "sp_");
			cellName = cellName.replace("sp_g", "sp_g_");
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
		nativeQuery += " order by e.sp_g_id asc,e.sys_header desc , e.sys_sort asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, SystemPermission.class);
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
