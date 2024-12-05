package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseArea;
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
public class BasicChangeItemsServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private BasicCommandListDao commandListDao;

	@Autowired
	private BasicBomIngredientsDao bomIngredientsDao;

	@Autowired
	private WarehouseAreaDao areaDao;

	@Autowired
	private EntityManager em;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = 9999;
		int batch = 0;

		// Step2.排序
		List<Order> ordersBC = new ArrayList<>();
		ordersBC.add(new Order(Direction.DESC, "bclclass"));// 單別
		ordersBC.add(new Order(Direction.DESC, "bclsn"));// 單號
		ordersBC.add(new Order(Direction.ASC, "bclnb"));// 物料號
		//
		List<Order> ordersWA = new ArrayList<>();
		ordersWA.add(new Order(Direction.DESC, "wawmpnb"));// 物料號

		// 一般模式
		PageRequest pageableBC = PageRequest.of(batch, total, Sort.by(ordersBC));
		PageRequest pageableWA = PageRequest.of(batch, total, Sort.by(ordersWA));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			// 製令單
			ArrayList<BasicCommandList> entitysNew = new ArrayList<BasicCommandList>();
			ArrayList<BasicCommandList> entitys = commandListDao.findAllBySearch(null, null, "90-320", pageableBC);
			// Step4-2.資料區分(一般/細節)- 排除重複單號
			Map<String, Boolean> check = new HashMap<String, Boolean>();
			entitys.forEach(o -> {
				String k = o.getBclclass() + "-" + o.getBclsn();
				if (!check.containsKey(k)) {
					check.put(k, true);
					entitysNew.add(o);
				}
			});
			// BOM組成結構
			/*
			 * ArrayList<BasicBomIngredients> ingredients =
			 * bomIngredientsDao.findAllBySearch(null, null, null, null, null, null);
			 */
			// 倉庫數量儲位
			ArrayList<WarehouseArea> entityDetails = areaDao.findAllByWawmpnbNot0("90-320", pageableWA);
			// Step3-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitysNew);
			String entityDetailJson = packageService.beanToJson(entityDetails);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDetailJson);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("BasicCommandList", null,
					2);
			ArrayList<SystemLanguageCell> languagesDetail = languageDao.findAllByLanguageCellSame("WarehouseArea", null,
					2);

			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項
			mapLanguages.forEach((k, v) -> {
				if (k.equals("bclproduct")) {// 成品號
					v.setSyssort(0);
				} else if (k.equals("bclclass")) {// 工單別
					v.setSyssort(1);
				} else if (k.equals("bclsn")) {// 工單號
					v.setSyssort(2);
				} else if (k.equals("bclnb")) {// 序號
					v.setSyssort(3);
				} else if (k.equals("bclpnumber")) {// 物料號
					v.setSyssort(4);
				} else if (k.equals("bclpname")) {// 物料品名
					v.setSyssort(5);
				} else if (k.equals("bclpspecification")) {// 物料規格
					v.setSyssort(6);
				} else if (k.equals("bclsdate")) {// 預計出貨日
					v.setSyssort(7);
				} else if (k.equals("bclerpcuser")) {// 開單人
					v.setSyssort(8);
				} else {
					v.setSlcshow(0);
				}
			});
			mapLanguagesDetail.forEach((k, v) -> {
				
			});
			

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fieldsBC = BasicCommandList.class.getDeclaredFields();
			Field[] fieldsWA = WarehouseArea.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fieldsBC, exceptionCell, mapLanguages);
			resultDetailTJsons = packageService.resultSet(fieldsWA, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "bclpnumber", "Ex:物料1_物料2/成品號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			BasicCommandList searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					BasicCommandList.class);
			ArrayList<BasicCommandList> entitysNew = new ArrayList<BasicCommandList>();
			ArrayList<WarehouseArea> entityDetails = new ArrayList<WarehouseArea>();
			// 複數?避免為空
			String bclpns[] = searchData.getBclpnumber() != null && !searchData.getBclpnumber().isEmpty()
					? searchData.getBclpnumber().split("_")
					: new String[] { "90-320" };
			for (String bclpn : bclpns) {
				// 製令單
				ArrayList<BasicCommandList> entitys = commandListDao.findAllBySearch(null, null, bclpn, pageableBC);
				// BOM組成結構
				ArrayList<BasicBomIngredients> ingredients = bomIngredientsDao.findAllBySearch(null, null, bclpn, null,
						null, null);

				// BOM而外庫存量儲位
				ArrayList<WarehouseArea> entityDetailsForBom = new ArrayList<WarehouseArea>();
				ingredients.forEach(bom -> {
					ArrayList<WarehouseArea> forBom = areaDao.findAllByWawmpnbNot0(bom.getBbisn(), pageableWA);
					// 加入到 庫存清單內
					if (forBom.size() > 0) {
						entityDetailsForBom.addAll(forBom);
					}
				});

				// 倉庫數量儲位
				ArrayList<WarehouseArea> entityDetail = areaDao.findAllByWawmpnbNot0(bclpn, pageableWA);
				entityDetail.addAll(entityDetailsForBom);

				// 合併一起+標記
				WarehouseArea newW = new WarehouseArea();
				newW.setWawmpnb("==" + bclpn + "==");
				entityDetails.add(newW);
				// 合併-與多筆資料
				entityDetails.addAll(entityDetail);

				// Step4-2.資料區分(一般/細節)- 排除重複單號
				Map<String, Boolean> check = new HashMap<String, Boolean>();
				entitys.forEach(o -> {
					String k = o.getBclclass() + "-" + o.getBclsn();
					if (!check.containsKey(k)) {
						check.put(k, true);
						entitysNew.add(o);
					}
				});
			}

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitysNew);
			String entityDetailJson = packageService.beanToJson(entityDetails);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDetailJson);
			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new BasicCommandList());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("bclid_");
		packageBean.setEntityDetailIKeyGKey("waid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_bcledate_bclfdate_bclsdate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================

		// =======================資料檢查=======================

		// =======================資料整理=======================
		// Step3.一般資料->寫入

		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================

		// =======================資料檢查=======================

		// =======================資料整理=======================

		// =======================資料儲存=======================

		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================

		// =======================資料檢查=======================

		// =======================資料整理=======================
		// Step3.一般資料->寫入

		// =======================資料儲存=======================
		// 資料Data

		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================

		// =======================資料檢查=======================

		// =======================資料整理=======================

		// =======================資料儲存=======================
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<BasicCommandList> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM basic_command_list e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("bcl", "bcl_");

			cellName = cellName.replace("bcl_cuser", "bcl_c_user");
			cellName = cellName.replace("bcl_fuser", "bcl_f_user");
			cellName = cellName.replace("bcl_pnumber", "bcl_p_number");
			cellName = cellName.replace("bcl_pname", "bcl_p_name");
			cellName = cellName.replace("bcl_pspecification", "bcl_p_specification");
			cellName = cellName.replace("bcl_pnqty", "bcl_pn_qty");
			cellName = cellName.replace("bcl_tocommand", "bcl_to_command");
			cellName = cellName.replace("bcl_fromcommand", "bcl_from_command");
			cellName = cellName.replace("bcl_towho", "bcl_to_who");
			cellName = cellName.replace("bcl_fromwho", "bcl_from_who");
			cellName = cellName.replace("bcl_edate", "bcl_e_date");
			cellName = cellName.replace("bcl_fdate", "bcl_f_date");
			cellName = cellName.replace("bcl_sdate", "bcl_s_date");

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
		nativeQuery += " order by e.bcl_class asc";
		nativeQuery += " , e.bcl_sn asc";
		nativeQuery += " , e.bcl_p_number asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, BasicCommandList.class);
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
