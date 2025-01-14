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
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.ScheduleProductionHistoryDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BomProductManagement;
import dtri.com.tw.pgsql.entity.ScheduleProductionHistory;
import dtri.com.tw.pgsql.entity.SystemGroup;
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
public class ScheduleProductionNotesServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private ScheduleProductionHistoryDao historyDao;

	@Autowired
	private BomProductManagementDao managementDao;

	@Autowired
	private BasicCommandListDao commandListDao;

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
		orders.add(new Order(Direction.DESC, "syscdate"));// 建立時間
		orders.add(new Order(Direction.ASC, "sphbpmnb"));// 產品號
		List<Order> ordersD = new ArrayList<>();
		ordersD.add(new Order(Direction.ASC, "bpmnb"));// 產品號
		ordersD.add(new Order(Direction.ASC, "bpmmodel"));// 型號
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));
		PageRequest pageableDetail = PageRequest.of(batch, total, Sort.by(ordersD));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<ScheduleProductionHistory> entitys = historyDao.findAllBySearch(null, null, null, null, null,
					null, pageable);

			ArrayList<BomProductManagement> entityDetails = managementDao.findAllBySearch(null, null, null, null,
					pageableDetail);

			// Step3-2.資料區分(一般/細節)

			// 類別(一般模式)
			// 資料包裝
			String entityJson = packageService.beanToJson(entitys);
			packageBean.setEntityJson(entityJson);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("ScheduleProductionHistory",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("BomProductManagement", null, 2);
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonArray searchDetailJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = ScheduleProductionHistory.class.getDeclaredFields();
			Field[] fieldDetails = BomProductManagement.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			resultDetailTJsons = packageService.resultSet(fieldDetails, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sphpon", "Ex:製令單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目(工單紀錄)
			searchJsons = packageService.searchSet(searchJsons, null, "sphbpmnb", "Ex:BOM號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("searchDetailSet", searchDetailJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);//
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ScheduleProductionHistory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ScheduleProductionHistory.class);

			ArrayList<ScheduleProductionHistory> entitys = historyDao.findAllBySearch(searchData.getSphbpmnb(), null,
					searchData.getSphonb(), null, null, null, pageable);

			ArrayList<BomProductManagement> entityDetails = managementDao.findAllBySearch(searchData.getSphbpmnb(),
					null, null, null, pageableDetail);
			// Step4-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			String entityDteailJson = packageService.beanToJson(entityDetails);

			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson(entityDteailJson);

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]") && packageBean.getEntityDetailJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new ScheduleProductionHistory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("sphid_");
		packageBean.setEntityDetailIKeyGKey("bpmid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 取得資料Order */
	public PackageBean getSearchOrder(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.DESC, "syscdate"));// 建立時間
		orders.add(new Order(Direction.ASC, "sphbpmnb"));// 產品號
		List<Order> ordersD = new ArrayList<>();
		ordersD.add(new Order(Direction.ASC, "bpmnb"));// 產品號
		ordersD.add(new Order(Direction.ASC, "bpmmodel"));// 型號
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));
		PageRequest pageableDetail = PageRequest.of(batch, total, Sort.by(ordersD));

		// ========================區分:訪問/查詢========================

		// Step4-1. 取得資料(一般/細節)

		ScheduleProductionHistory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
				ScheduleProductionHistory.class);
		ArrayList<ScheduleProductionHistory> entitys = new ArrayList<ScheduleProductionHistory>();

		ArrayList<BomProductManagement> entityDetails = new ArrayList<BomProductManagement>();
		// 必須切兩段->要有資料->只抓一筆
		String sphpon[] = searchData.getSphpon().split("-");
		if (sphpon.length == 2) {
			String bclclass = sphpon[0];
			String bclsn = sphpon[1];
			ArrayList<BasicCommandList> commandLists = commandListDao.findAllByComList(bclclass, bclsn, null, null);
			if (commandLists.size() > 0) {
				BasicCommandList commandList = commandLists.get(0);
				ScheduleProductionHistory entity = new ScheduleProductionHistory();
				// 客戶/國家/訂單
				String sysnot[] = commandList.getSyshnote().split("\\");
				//
				entity.setSyscdate(commandList.getSyscdate());
				entity.setSysmdate(commandList.getSysmdate());
				entity.setSyscuser(commandList.getSyscuser());
				entity.setSysmuser(commandList.getSysmuser());
				entity.setSphid(null);
				entity.setSphbpmnb(commandList.getBclproduct());// BOM 的產品號
				entity.setSphbpmmodel("");// BOM 的產品型號
				entity.setSphbpmtype("2");// 產品歸類:0 = 開發BOM/1 = 產品BOM/2 = 配件BOM/3 = 半成品BOM/3 = 板階BOM
				entity.setSphbpmtypename("產品BOM");// 產品歸類名稱
				entity.setSphbisitem("{}");// 產品-物料結構
				entity.setSphbpsnv("[]");// 產品-參數設置
				entity.setSphprnv("[]");// 製造-參數設置
				entity.setSphscnv("[]");// 生管-參數設置
				entity.setSphbpsuser("");// BOM負責人
				entity.setSphpon(searchData.getSphpon());// 製令單號
				entity.setSphonb(sysnot[2] != null ? sysnot[0] : "");// 訂單號
				entity.setSphoname(sysnot[0] != null ? sysnot[0] : "");// 訂單客戶
				entity.setSphocountry(sysnot[1] != null ? sysnot[0] : "");// 訂單國家
				entity.setSphobpmnb("");// 訂單 BOM的產品號
				entity.setSphhdate(Fm_T.to_y_M_d(commandList.getBclsdate()));// 預計出貨日
				entity.setSphfrom("");// 規格來源:生管自訂/產品經理
				entity.setSphstatus(1);// 狀態類型 0=作廢單 1=有效單 2=自訂紀錄(不具備生產能力)
				entity.setSphprogress(0);// 進度:完成ERP工單(準備物料)=1/完成注意事項(預約生產)=2/完成->流程卡(準備生產)=3/(生產中)=4/(生產結束)=5
				entity.setSphssn("");// SN開始
				entity.setSphesn("");// SN結束
				entity.setSphpmnote("");// 產品經理事項
				entity.setSphscnote("");// 生管備註事項
				entity.setSphprnote1("");// 製造事項1
				entity.setSphprnote2("");// 製造事項2

				// BOM清單
				ArrayList<BomProductManagement> managements = managementDao.findAllBySearch(commandList.getBclproduct(),
						null, null, null, pageableDetail);
				entitys.add(entity);
				// 如果有匹配到BOM->
				if (managements.size() > 0) {
					entityDetails = managements;
					// 補充
					entity.setSphbpmmodel(managements.get(0).getBpmmodel());// BOM 的產品型號
					entity.setSphbisitem(managements.get(0).getBpmbisitem());// 產品-物料結構
					entity.setSphbpsnv(managements.get(0).getBpmbisitem());// 產品-參數設置
					entity.setSphbpsuser(managements.get(0).getSyscuser());// 最後更改人
					// 處裡格式化
					StringBuilder noteAll = new StringBuilder();
					try {
						JsonArray notes = JsonParser.parseString(managements.get(0).getBpmbisitem()).getAsJsonArray();
						String s = "「";
						String e = "」";
						notes.forEach(n -> {
							noteAll.append(s);
							noteAll.append(n.getAsString().replace("_", ":"));
							noteAll.append(e);
						});

					} catch (Exception e) {
						// not do anything->不加入note
					}
					entity.setSphpmnote(managements.get(0).getSysnote() + noteAll.toString());

				}
			}
		}

		// Step4-2.資料區分(一般/細節)

		// 類別(一般模式)
		String entityJson = packageService.beanToJson(entitys);
		String entityDteailJson = packageService.beanToJson(entityDetails);

		// 資料包裝
		packageBean.setEntityJson(entityJson);
		packageBean.setEntityDetailJson(entityDteailJson);

		// 查不到資料
		if (packageBean.getEntityJson().equals("[]") && packageBean.getEntityDetailJson().equals("[]")) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}

		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new ScheduleProductionHistory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("sphid_");
		packageBean.setEntityDetailIKeyGKey("bpmid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		// =======================資料檢查=======================
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		// =======================資料檢查=======================
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		// =======================資料檢查=======================
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<ScheduleProductionHistory> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM schedule_production_history e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("sph", "sph_");
			//
			cellName = cellName.replace("sph_bpmnb", "sph_bpm_nb");
			cellName = cellName.replace("sph_bpmmodel", "sph_bpm_model");
			cellName = cellName.replace("sph_bpmtype", "sph_bpm_type");
			cellName = cellName.replace("sph_bpmtypename", "sph_bpm_type_name");
			//
			cellName = cellName.replace("sph_bisitem", "sph_bis_item");
			cellName = cellName.replace("sph_bpsnv", "sph_bps_nv");
			cellName = cellName.replace("sph_bpsuser", "sph_bps_user");
			cellName = cellName.replace("sph_onb", "sph_o_nb");
			//
			cellName = cellName.replace("sph_oname", "sph_o_name");
			cellName = cellName.replace("sph_ocountry", "sph_o_country");
			cellName = cellName.replace("sph_hdate", "sph_h_date");
			cellName = cellName.replace("sph_onb", "sph_o_nb");
			//
			cellName = cellName.replace("sph_ssn", "sph_s_sn");
			cellName = cellName.replace("sph_esn", "sph_e_sn");
			cellName = cellName.replace("sph_pmnote", "sph_pm_note");
			cellName = cellName.replace("sph_scnote", "sph_sc_note");
			cellName = cellName.replace("sph_prnote1", "sph_pr_note1");
			cellName = cellName.replace("sph_prnote2", "sph_pr_note2");

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
		nativeQuery += " order by e.sys_c_date desc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, ScheduleProductionHistory.class);
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
