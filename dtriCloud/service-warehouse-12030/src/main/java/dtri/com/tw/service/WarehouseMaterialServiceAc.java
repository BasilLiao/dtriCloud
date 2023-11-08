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
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
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
public class WarehouseMaterialServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private WarehouseMaterialDao materialDao;

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
		orders.add(new Order(Direction.ASC, "wmpnb"));// 物料號
		orders.add(new Order(Direction.DESC, "wmname"));// 物料名稱
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<WarehouseMaterial> entitys = materialDao.findAllBySearch(null, null, null, pageable);

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseMaterial", null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = WarehouseMaterial.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("warehouseAreas");
			exceptionCell.add("wmidate");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wmpnb", "Ex:物料號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wmname", "Ex:物料名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wmspecification", "Ex:物料規格?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseMaterial searchData = packageService.jsonToBean(packageBean.getEntityJson(), WarehouseMaterial.class);

			ArrayList<WarehouseMaterial> entitys = materialDao.findAllBySearch(searchData.getWmpnb(), searchData.getWmname(),
					searchData.getWmspecification(), pageable);
			// Step4-2.資料區分(一般/細節)

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
		String entityFormatJson = packageService.beanToJson(new WarehouseMaterial());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("wmid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseMaterial> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<WarehouseMaterial>>() {
			});

			// Step2.資料檢查
			for (WarehouseMaterial entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<WarehouseMaterial> checkDatas = materialDao.findAllByCheck(entityData.getWmpnb(), null, null, null);
				for (WarehouseMaterial checkData : checkDatas) {
					if (checkData.getWmid().compareTo(entityData.getWmid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getWmpnb() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<WarehouseMaterial> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getWmid() != null) {
				WarehouseMaterial entityDataOld = new WarehouseMaterial();
				entityDataOld = materialDao.getReferenceById(x.getWmid());

				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setSysstatus(x.getSysstatus());
				entityDataOld.setSyssort(x.getSyssort());
				entityDataOld.setSysheader(false);
				// 修改
				entityDataOld.setWmname(x.getWmname());
				entityDataOld.setWmspecification(x.getWmspecification());
				entityDataOld.setWmadqty(x.getWmadqty());
				entityDataOld.setWmaiqty(x.getWmaiqty());
				entityDataOld.setWmmrcheck(x.getWmmrcheck());
				entityDataOld.setWmsepncheck(x.getWmsepncheck());
				entityDataOld.setChecksum(x.getChecksum());

				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		materialDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<WarehouseMaterial> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<WarehouseMaterial>>() {
			});

			// Step2.資料檢查
			for (WarehouseMaterial entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<WarehouseMaterial> checkDatas = materialDao.findAllByCheck(entityData.getWmpnb(), null, null, null);
				if (checkDatas.size() > 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW, new String[] { entityData.getWmpnb() });
				}
			}
		}

		// =======================資料整理=======================
		// 資料Data
		ArrayList<WarehouseMaterial> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			// 新增
			x.setWmid(null);
			saveDatas.add(x);
		});
		// =======================資料儲存=======================
		// 資料Detail
		materialDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseMaterial> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<WarehouseMaterial>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<WarehouseMaterial> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getWmid() != null) {
				WarehouseMaterial entityDataOld = materialDao.findById(x.getWmid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		materialDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<WarehouseMaterial> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(), new TypeReference<ArrayList<WarehouseMaterial>>() {
			});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<WarehouseMaterial> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getWmid() != null) {
				WarehouseMaterial entityDataOld = materialDao.getReferenceById(x.getWmid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		materialDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<WarehouseMaterial> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM warehouse_material e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("wm", "wm_");
			cellName = cellName.replace("wm_adqty", "wm_a_d_qty");
			cellName = cellName.replace("wm_aiqty", "wm_a_i_qty");
			cellName = cellName.replace("wm_idate", "wm_i_date");
			cellName = cellName.replace("wm_pnb", "wm_p_nb");
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
		nativeQuery += " order by e.wm_p_nb asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, WarehouseMaterial.class);
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
