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

import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemLanguageCell;
import dtri.com.tw.db.entity.SystemPermission;
import dtri.com.tw.pgsql.dao.SystemGroupDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.SystemPermissionDao;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Service
public class SystemGroupServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemGroupDao groupDao;

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
		orders.add(new Order(Direction.ASC, "sgname"));
		orders.add(new Order(Direction.DESC, "sysheader"));
		orders.add(new Order(Direction.DESC, "syssort"));
		// 一般模式/細節模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			// Step3-1.取得資料(一般/細節)
			List<SystemGroup> entitys = new ArrayList<>();
			entitys = groupDao.findAllByGroupHeader(null, null, true, pageable);

			// Step3-2.資料區分(一般/細節)
			// 類別(細節模式)
			ArrayList<SystemGroup> entityDatas = new ArrayList<>();
			ArrayList<SystemGroup> entityDetails = new ArrayList<>();
			// 數量控管(細節模式)
			for (int s = 0; s < entitys.size(); s++) {
				SystemGroup entityOne = entitys.get(s);
				// 父類別
				entityOne.setSystemusers(null);
				entityDatas.add(entityOne);
				// 子類別
				groupDao.findAllByGroupHeader(entityOne.getSgname(), null, false, null).forEach(sd -> {
					sd.setSpname(sd.getSystemPermission().getSpname());
					sd.setSpid(sd.getSystemPermission().getSpid());
					char[] ch = sd.getSgpermission().toCharArray();
					sd.setpAA(ch[0] == '1' ? true : false);
					sd.setpAR(ch[1] == '1' ? true : false);
					sd.setpAU(ch[2] == '1' ? true : false);
					sd.setpAC(ch[3] == '1' ? true : false);
					sd.setpAD(ch[4] == '1' ? true : false);
					sd.setpDD(ch[5] == '1' ? true : false);
					sd.setpS1(ch[6] == '1' ? true : false);
					sd.setpS2(ch[7] == '1' ? true : false);
					sd.setpS3(ch[8] == '1' ? true : false);
					sd.setpS4(ch[9] == '1' ? true : false);
					sd.setpS5(ch[10] == '1' ? true : false);
					sd.setpS6(ch[11] == '1' ? true : false);
					sd.setSystemusers(null);
					entityDetails.add(sd);
				});
			}
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entityDatas);
			packageBean.setEntityJson(entityJsonDatas);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("SystemGroup", null,  2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao.findAllByLanguageCellSame("SystemGroup_Detail", null, 2);
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemGroup.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("systemPermission");
			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			// 欄位翻譯(細節)
			if (packageBean.getDetailMode()) {
				resultDetailTJsons = packageService.resultSet(fields, exceptionCell, mapLanguagesDetail);
			}
			// Step3-5. 建立查詢(修改)項目
			searchJsons = packageService.searchSet(searchJsons, null, "sgname", "Ex:DB_NAME", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			searchJsons = packageService.searchSet(searchJsons, null, "spname", "Ex:DATA_BKUP", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// 修改項目-群組權限選項
			JsonArray selectArr = new JsonArray();
			List<SystemPermission> permissions = permissionDao.findAll();
			permissions.forEach(p -> {
				String pArr = p.getSpname() + "_" + p.getSpid();
				selectArr.add(pArr);
			});
			searchJsons = packageService.searchSet(searchJsons, selectArr, "spid", "", false, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			SystemGroup searchData = packageService.jsonToBean(packageBean.getEntityJson(), SystemGroup.class);
			ArrayList<SystemGroup> entitys = groupDao.findAllByGroupHeader(searchData.getSgname(), null, true, pageable);
			// Step3-2.資料區分(一般/細節)
			// 類別(細節模式)
			ArrayList<SystemGroup> entityDatas = new ArrayList<>();
			ArrayList<SystemGroup> entityDetails = new ArrayList<>();
			// 數量控管(細節模式)
			for (int s = 0; s < entitys.size(); s++) {
				SystemGroup entityOne = entitys.get(s);
				// 父類別
				entityOne.setSystemusers(null);
				entityDatas.add(entityOne);
				// 子類別
				groupDao.findAllByGroupHeader(entityOne.getSgname(), entityOne.getSpname(), false, null).forEach(sd -> {
					sd.setSpname(sd.getSystemPermission().getSpname());
					sd.setSpid(sd.getSystemPermission().getSpid());
					char[] ch = sd.getSgpermission().toCharArray();
					sd.setpAA(ch[0] == 1 ? true : false);
					sd.setpAR(ch[1] == 1 ? true : false);
					sd.setpAU(ch[2] == 1 ? true : false);
					sd.setpAC(ch[3] == 1 ? true : false);
					sd.setpAD(ch[4] == 1 ? true : false);
					sd.setpDD(ch[5] == 1 ? true : false);
					sd.setpS1(ch[6] == 1 ? true : false);
					sd.setpS2(ch[7] == 1 ? true : false);
					sd.setpS3(ch[8] == 1 ? true : false);
					sd.setpS4(ch[9] == 1 ? true : false);
					sd.setpS5(ch[10] == 1 ? true : false);
					sd.setpS6(ch[10] == 1 ? true : false);
					sd.setSystemusers(null);
					entityDetails.add(sd);
				});
			}
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entityDatas);
			packageBean.setEntityJson(entityJsonDatas);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new SystemGroup());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("sgid_sggid");
		packageBean.setEntityDetailIKeyGKey("sgid_sggid");
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
		ArrayList<SystemGroup> entityDatas = new ArrayList<>();
		ArrayList<SystemGroup> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});

			// Step2.資料檢查
			for (SystemGroup entityData : entityDatas) {
				// 檢查-群組名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemGroup> checkDatas = groupDao.findAllByGroupHeader(entityData.getSgname(), null, true, null);
				for (SystemGroup checkData : checkDatas) {
					if (checkData.getSggid() != entityData.getSggid()) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getSpname() });
					}
				}
			}
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1. 資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});
			// Step2.資料檢查
			for (SystemGroup entityDetail : entityDetails) {
				// 檢查-名稱重複(有數量 && 不同資料有重疊)
				List<SystemPermission> permissions = permissionDao.findBySpid(entityDetail.getSpid());
				ArrayList<SystemGroup> checkDetails = groupDao.findAllByGroupHeader(entityDetail.getSgname(), permissions.get(0).getSpname(), false,
						null);
				for (SystemGroup checkDetail : checkDetails) {
					if (checkDetail.getSggid() != entityDetail.getSggid()) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityDetail.getSpname() });
					}
				}

				// 檢查-群組ID(沒有跟隨到群組)
				checkDetails = groupDao.findBySggidOrderBySggid(entityDetail.getSggid());
				if (checkDetails.size() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1002, Lan.zh_TW, new String[] { entityDetail.getSpname() });
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemGroup> details = entityDetails;
		ArrayList<SystemGroup> saveDatas = new ArrayList<>();
		ArrayList<SystemGroup> saveDetails = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSgid() != null) {
				SystemGroup entityDataOld = groupDao.findBySggidOrderBySggid(x.getSgid()).get(0);
				entityDataOld.setSyscdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setSysstatus(x.getSysstatus());
				entityDataOld.setSyssort(x.getSyssort());
				entityDataOld.setSgname(x.getSgname());
				saveDatas.add(entityDataOld);
			}
			// 細節-更新匹配內容
			details.forEach(y -> {
				// 添加
				if (y.getSggid() != null && y.getSgid() == null) {
					SystemGroup entityDetailOld = groupDao.findBySggidOrderBySggid(y.getSggid()).get(0);
					y.setSysmdate(new Date());
					y.setSysmuser(packageBean.getUserAccount());
					y.setSysodate(new Date());
					y.setSysouser(packageBean.getUserAccount());
					y.setSyscdate(new Date());
					y.setSyscuser(packageBean.getUserAccount());
					y.setSysheader(false);
					SystemPermission permissions = permissionDao.findBySpid(y.getSpid()).get(0);
					y.setSystemPermission(permissions);
					y.setSgname(entityDetailOld.getSgname());
					y.setSggid(entityDetailOld.getSggid());
					saveDetails.add(y);
				} else if (y.getSgid() != null) {
					// 修改
					SystemGroup entityDetailOld = groupDao.findBySgidOrderBySgidAscSyssortAsc(y.getSgid()).get(0);
					entityDetailOld.setSysmdate(new Date());
					entityDetailOld.setSysmuser(packageBean.getUserAccount());
					entityDetailOld.setSysstatus(y.getSysstatus());
					entityDetailOld.setSysnote(y.getSysnote());
					entityDetailOld.setSyssort(y.getSyssort());
					SystemPermission permissions = permissionDao.findBySpid(y.getSpid()).get(0);
					entityDetailOld.setSystemPermission(permissions);
					entityDetailOld.setSgpermission(y.getSgpermission());
					saveDetails.add(entityDetailOld);
				}
			});
		});
		// =======================資料儲存=======================
		// 資料Data
		groupDao.saveAll(saveDatas);
		// 資料Detail
		groupDao.saveAll(saveDetails);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<SystemGroup> entityDatas = new ArrayList<>();
		ArrayList<SystemGroup> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});
			// Step2.資料檢查
			for (SystemGroup entityData : entityDatas) {
				// 檢查-群組名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemGroup> checkDatas = groupDao.findAllByGroupHeader(entityData.getSgname(), null, true, null);
				if (checkDatas.size() > 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW, new String[] { entityData.getSpname() });
				}
			}
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1.資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});
			// Step2.資料檢查(要有跟到群組名稱)
			for (SystemGroup entityDetail : entityDetails) {
				ArrayList<SystemGroup> checkDetails = groupDao.findAllByGroupHeader(entityDetail.getSgname(), null, true, null);
				if (checkDetails.size() < 1) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW, new String[] { entityDetail.getSpname() });
				}
				entityDetail.setSggid(checkDetails.get(0).getSggid());
			}
		}
		// =======================資料整理=======================
		// 資料Data
		ArrayList<SystemGroup> saveDatas = new ArrayList<>();
		ArrayList<SystemGroup> saveDetails = new ArrayList<>();
		entityDatas.forEach(x -> {
			SystemPermission permissions = permissionDao.findBySpid(1L).get(0);
			Long sggid = groupDao.getSystemGroupGSeq();
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSysheader(true);
			x.setSystemPermission(permissions);
			x.setSgpermission("000000000000");
			x.setSggid(sggid);
			saveDatas.add(x);
		});
		// 資料細節-群組配對
		entityDetails.forEach(y -> {
			if (y.getSpid() != null) {
				SystemPermission permissions = permissionDao.findBySpid(y.getSpid()).get(0);
				y.setSysmdate(new Date());
				y.setSysmuser(packageBean.getUserAccount());
				y.setSysodate(new Date());
				y.setSysouser(packageBean.getUserAccount());
				y.setSyscdate(new Date());
				y.setSyscuser(packageBean.getUserAccount());
				y.setSysheader(false);
				y.setSgid(null);
				y.setSystemPermission(permissions);
				saveDetails.add(y);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		groupDao.saveAll(saveDatas);
		// 資料Detail
		groupDao.saveAll(saveDetails);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemGroup> entityDatas = new ArrayList<>();
		ArrayList<SystemGroup> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});
			// Step2.資料檢查
		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1. 資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemGroup> details = entityDetails;
		ArrayList<SystemGroup> saveDatas = new ArrayList<>();
		ArrayList<SystemGroup> saveDetails = new ArrayList<>();
		// 一般
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSgid() != null) {
				SystemGroup entityDataOld = groupDao.findBySgidOrderBySgidAscSyssortAsc(x.getSgid()).get(0);
				entityDataOld.setSyscdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});

		// 細節-更新匹配內容
		details.forEach(y -> {
			if (y.getSgid() != null) {
				// 修改
				SystemGroup entityDataOld = groupDao.findBySgidOrderBySgidAscSyssortAsc(y.getSgid()).get(0);
				entityDataOld.setSyscdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDetails.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		groupDao.saveAll(saveDatas);
		// 資料Detail
		groupDao.saveAll(saveDetails);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<SystemGroup> entityDatas = new ArrayList<>();
		ArrayList<SystemGroup> entityDetails = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});

			// Step2.資料檢查

		}
		if (packageBean.getEntityDetailJson() != null && !packageBean.getEntityDetailJson().equals("")) {
			// Step1. 資料轉譯(細節)
			entityDetails = packageService.jsonToBean(packageBean.getEntityDetailJson(), new TypeReference<ArrayList<SystemGroup>>() {
			});
			// Step2.資料檢查

		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<SystemGroup> saveDatas = new ArrayList<>();
		ArrayList<SystemGroup> saveDetails = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSgid() != null) {
				SystemGroup entityDataOld = groupDao.findBySgidOrderBySgidAscSyssortAsc(x.getSgid()).get(0);
				saveDatas.add(entityDataOld);
			}
		});
		// 細節-移除內容
		entityDetails.forEach(y -> {
			// 排除 沒有ID
			if (y.getSgid() != null) {
				SystemGroup entityDetailOld = groupDao.findBySgidOrderBySgidAscSyssortAsc(y.getSgid()).get(0);
				saveDetails.add(entityDetailOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Detail
		groupDao.deleteAll(saveDetails);
		// 資料Data
		groupDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<SystemGroup> entitys = new ArrayList<>();
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
			cellName = cellName.replace("sg", "sg_");
			cellName = cellName.replace("sg_g", "sg_g_");
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
		nativeQuery += " order by e.sg_g_id desc,e.sys_sort asc ";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, SystemGroup.class);
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
		entitys = query.getResultList();
		// 類別(細節模式)
		ArrayList<SystemGroup> entityDatas = new ArrayList<>();
		// 修正資料
		for (int s = 0; s < entitys.size(); s++) {
			SystemGroup entityOne = entitys.get(s);
			SystemPermission permissions = permissionDao.findBySpid(entityOne.getSpid()).get(0);
			entityOne.setSpid(permissions.getSpid());
			entityOne.setSpname(permissions.getSpname());
			entityDatas.add(entityOne);// 父類別
		}
		// 資料包裝
		String entityJsonDatas = packageService.beanToJson(entityDatas);
		packageBean.setEntityJson(entityJsonDatas);

		return packageBean;
	}
}
