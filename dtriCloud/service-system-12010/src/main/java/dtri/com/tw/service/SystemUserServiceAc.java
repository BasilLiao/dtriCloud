package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.SystemGroupDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.SystemPermissionDao;
import dtri.com.tw.pgsql.dao.SystemUserDao;
import dtri.com.tw.pgsql.entity.SystemConfig;
import dtri.com.tw.pgsql.entity.SystemGroup;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.SystemPermission;
import dtri.com.tw.pgsql.entity.SystemUser;
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
public class SystemUserServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private SystemUserDao userDao;

	@Autowired
	private SystemGroupDao groupDao;

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
		orders.add(new Order(Direction.DESC, "sugname"));
		orders.add(new Order(Direction.DESC, "sysmdate"));
		orders.add(new Order(Direction.DESC, "suaccount"));
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			String user = null;
			if (packageBean.getUserAccount().equals("admin")) {
				user = packageBean.getUserAccount();
				// 順便更新
				updateAdmin();
			}
			// Step3-1.取得資料(一般/細節)
			ArrayList<SystemUser> entitys = userDao.findAllBySystemUser(null, null, null, user, null, pageable);

			// Step3-2.資料區分(一般/細節)
			entitys.forEach(u -> {
				u.setSugid(u.getSystemgroups().iterator().next().getSggid());
				u.setSystemgroups(null);
				u.setSupassword("");
			});

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("SystemUser", null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項
			SystemLanguageCell sugid = mapLanguages.get("sugid");
			List<SystemGroup> pList = new ArrayList<>();
			JsonArray pListArr = new JsonArray();
			pList = groupDao.findAllByGroupHeader(null, null, true, null);
			String adminGroupCheck = user;
			pList.forEach(t -> {
				if (t.getSggid() != 1L) {
					pListArr.add(t.getSgname() + "_" + t.getSggid());
				} else if (adminGroupCheck != null) {// 有權限?
					pListArr.add(t.getSgname() + "_" + t.getSggid());
				}
			});
			sugid.setSlcmtype("select");
			sugid.setSlcmselect(pListArr.toString());
			mapLanguages.put("sugid", sugid);

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemUser.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "suaccount", "Ex:帳號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "suname", "Ex:使用者名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "suposition", "Ex:職位?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, pListArr, "sugid", "Ex:用戶組?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			SystemUser searchData = packageService.jsonToBean(packageBean.getEntityJson(), SystemUser.class);
			String user = null;
			if (packageBean.getUserAccount().equals("admin")) {
				user = packageBean.getUserAccount();
			}

			ArrayList<SystemUser> entitys = userDao.findAllBySystemUser(searchData.getSuname(),
					searchData.getSuaccount(), searchData.getSuposition(), user, null, pageable);
			ArrayList<SystemUser> entityResp = new ArrayList<>();
			// Step4-2.資料區分(一般/細節)
			entitys.forEach(u -> {
				Long sugid = u.getSystemgroups().iterator().next().getSggid();
				if (searchData.getSugid() != null && searchData.getSugid().compareTo(sugid) == 0) {
					u.setSugid(u.getSystemgroups().iterator().next().getSggid());
					u.setSystemgroups(null);
					u.setSupassword("");
					entityResp.add(u);
				} else if (searchData.getSugid() == null) {
					u.setSugid(sugid);
					u.setSystemgroups(null);
					u.setSupassword("");
					entityResp.add(u);
				}
			});

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entityResp);
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
		String entityFormatJson = packageService.beanToJson(new SystemUser());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("suid_");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemUser> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<SystemUser>>() {
					});

			// Step2.資料檢查
			for (SystemUser entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemUser> checkDatas = userDao.findAllBySystemUserCheck(entityData.getSuaccount(), null,
						null, null);
				for (SystemUser checkData : checkDatas) {
					if (checkData.getSuid().compareTo(entityData.getSuid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSuaccount() });
					}
				}
				checkDatas = userDao.findAllBySystemUserCheck(null, entityData.getSuemail(), null, null);
				for (SystemUser checkData : checkDatas) {
					if (checkData.getSuid().compareTo(entityData.getSuid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSuemail() });
					}
				}
				checkDatas = userDao.findAllBySystemUserCheck(null, null, entityData.getSuname(), null);
				for (SystemUser checkData : checkDatas) {
					if (checkData.getSuid().compareTo(entityData.getSuid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSuname() });
					}
				}
				checkDatas = userDao.findAllBySystemUserCheck(null, null, null, entityData.getSuename());
				for (SystemUser checkData : checkDatas) {
					if (checkData.getSuid().compareTo(entityData.getSuid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSuename() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemUser> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSuid() != null) {
				SystemUser entityDataOld = userDao.findAllBySuid(x.getSuid()).get(0);

				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setSysstatus(x.getSysstatus());
				entityDataOld.setSyssort(x.getSyssort());
				entityDataOld.setSysheader(false);
				// 修改
				entityDataOld.setSuaccount(x.getSuaccount().replaceAll("_", ""));
				entityDataOld.setSuemail(x.getSuemail());
				entityDataOld.setSuname(x.getSuname());
				entityDataOld.setSuename(x.getSuename());
				entityDataOld.setSulanguage(x.getSulanguage());
				entityDataOld.setSuposition(x.getSuposition());
				// 群組更換
				ArrayList<SystemGroup> glist = groupDao.findBySggidOrderBySggid(x.getSugid());
				entityDataOld.setSystemgroups(new HashSet<>(glist));
				entityDataOld.setSugname(glist.get(0).getSgname());

				// 密碼加密
				BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
				String pwd = x.getSupassword();
				pwd = pwd.equals("") ? entityDataOld.getSupassword() : pwdEncoder.encode(pwd);
				entityDataOld.setSupassword(pwd);

				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		userDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<SystemUser> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<SystemUser>>() {
					});

			// Step2.資料檢查
			for (SystemUser entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemUser> checkDatas = userDao.findAllBySystemUserCheck(entityData.getSuaccount(), null,
						null, null);
				if (checkDatas.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getSuaccount() });
				}
				checkDatas = userDao.findAllBySystemUserCheck(null, entityData.getSuemail(), null, null);
				if (checkDatas.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getSuemail() });
				}
				checkDatas = userDao.findAllBySystemUserCheck(null, null, entityData.getSuname(), null);
				if (checkDatas.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getSuname() });
				}
				checkDatas = userDao.findAllBySystemUserCheck(null, null, null, entityData.getSuename());
				if (checkDatas.size() != 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getSuename() });
				}
				// 檢查-密碼
				if (entityData.getSupassword().length() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { "password" });
				}
			}
		}

		// =======================資料整理=======================
		// 資料Data
		ArrayList<SystemUser> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			ArrayList<SystemGroup> groups = groupDao.findBySggidOrderBySggid(x.getSugid());
			x.setSystemgroups(new HashSet<SystemGroup>(groups));
			x.setSugname(groups.get(0).getSgname());
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			// 新增
			x.setSuid(null);
			// 密碼加密
			BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
			x.setSupassword(pwdEncoder.encode(x.getSupassword()));
			x.setSuaccount(x.getSuaccount().replaceAll("_", ""));
			saveDatas.add(x);
		});
		// =======================資料儲存=======================
		// 資料Detail
		userDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemUser> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<SystemUser>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemUser> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSuid() != null) {
				SystemUser entityDataOld = userDao.findById(x.getSuid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		userDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemUser> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<SystemUser>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemUser> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSuid() != null) {
				SystemUser entityDataOld = userDao.findById(x.getSuid()).get();
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		userDao.deleteAll(saveDatas);
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
		String nativeQuery = "SELECT e.* FROM system_user e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("su", "su_");
			cellName = cellName.replace("su_g", "su_g_");
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
		nativeQuery += " order by e.su_name asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, SystemUser.class);
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

	// 自動更新Admin 資料內容物
	public void updateAdmin() throws Exception {
		// 取得Admin User
		ArrayList<SystemUser> users = userDao.findAllBySuid(1L);
		// 取得Admin Group
		Set<SystemGroup> groups = users.get(0).getSystemgroups();
		// 取得Admin Permission
		List<SystemPermission> permissions = permissionDao.findAll();
		// 檢查
		permissions.forEach(p -> {
			boolean checkHave = false;
			for (SystemGroup g : groups) {
				Long checkGID = g.getSystemPermission().getSpid();
				// 排除無作用 + 已經有的PASS
				if (p.getSpid() != 0L && checkGID.compareTo(p.getSpid()) == 0) {
					checkHave = true;
					continue;
				}
			}
			// 如果還是沒有資料->新增
			if (!checkHave) {
				SystemGroup y = new SystemGroup();
				y.setSysmdate(new Date());
				y.setSysmuser("system");
				y.setSysodate(new Date());
				y.setSysouser("system");
				y.setSyscdate(new Date());
				y.setSyscuser("system");
				y.setSysheader(false);
				y.setSysstatus(3);
				y.setSystemPermission(p);
				y.setSgname(groups.iterator().next().getSgname());
				y.setSggid(groups.iterator().next().getSggid());
				//
				String sgpermission = new String("111111111111");
				y.setSgpermission(sgpermission);
				y.setSystemPermission(p);
				groupDao.save(y);
				groups.add(y);
			}
		});
		users.get(0).setSystemgroups(groups);
		userDao.save(users.get(0));
	}
}
