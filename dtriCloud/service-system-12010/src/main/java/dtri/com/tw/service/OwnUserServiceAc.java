package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemLanguageCell;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.pgsql.dao.SystemGroupDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.SystemUserDao;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class OwnUserServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private SystemUserDao userDao;

	@Autowired
	private SystemGroupDao groupDao;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		// int total = pageSetJson.get("total").getAsInt();
		// int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.DESC, "sysmdate"));
		orders.add(new Order(Direction.DESC, "suaccount"));
		// 一般模式
		// PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<SystemUser> entitys = userDao.findAllBySystemUserCheck(packageBean.getUserAccount(), null, null, null);

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
			pList.forEach(t -> {
				pListArr.add(t.getSgname() + "_" + t.getSggid());
			});
			sugid.setSlcmtype("select");
			sugid.setSlcmfixed(1);
			sugid.setSlcmselect(pListArr.toString());
			mapLanguages.put("sugid", sugid);

			SystemLanguageCell sysstatus = mapLanguages.get("sysstatus");
			sysstatus.setSlcmfixed(1);
			mapLanguages.put("sysstatus", sysstatus);

			SystemLanguageCell suposition = mapLanguages.get("suposition");
			suposition.setSlcmfixed(1);
			mapLanguages.put("suposition", suposition);

			SystemLanguageCell suaccount = mapLanguages.get("suaccount");
			suaccount.setSlcmfixed(1);
			mapLanguages.put("suaccount", suaccount);

			SystemLanguageCell suaaccount = mapLanguages.get("suaaccount");
			suaaccount.setSlcmfixed(1);
			mapLanguages.put("suaaccount", suaaccount);

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

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ArrayList<SystemUser> entitys = userDao.findAllBySystemUserCheck(packageBean.getUserAccount(), null, null, null);

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
			packageBean.setEntityDetailJson("");

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new SystemUser());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("suid_");
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
		ArrayList<SystemUser> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<SystemUser>>() {
			});

			// Step2.資料檢查
			for (SystemUser entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<SystemUser> checkDatas = userDao.findAllBySystemUserCheck(null, entityData.getSuemail(), null, null);
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
				entityDataOld.setSuemail(x.getSuemail());
				entityDataOld.setSuname(x.getSuname());
				entityDataOld.setSuename(x.getSuename());
				entityDataOld.setSulanguage(x.getSulanguage());

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
}
