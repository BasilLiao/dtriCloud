package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BomSoftwareHardwareDao;
import dtri.com.tw.pgsql.dao.ManufactureRuleNumberDao;
import dtri.com.tw.pgsql.dao.ManufactureSerialNumberDao;
import dtri.com.tw.pgsql.dao.ScheduleProductionHistoryDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.ScheduleProductionHistory;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.persistence.EntityManager;

@Service
public class ManufactureProcessCardServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private ManufactureRuleNumberDao ruleNumberDao;

	@Autowired
	private ManufactureSerialNumberDao serialNumberDao;

	@Autowired
	private ScheduleProductionHistoryDao productionHistoryDao;

	@Autowired
	private BomSoftwareHardwareDao bomSoftwareHardwareDao;

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
		orders.add(new Order(Direction.ASC, "sphpon"));// 工單號

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<ScheduleProductionHistory> entitys = productionHistoryDao.findAllBySearch(null, null, null, null,
					null, null, null, pageable);

			// Step3-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages_sph = new HashMap<>();// 歷史紀錄
			Map<String, SystemLanguageCell> mapLanguages_mrn = new HashMap<>();// SN規則
			Map<String, SystemLanguageCell> mapLanguages_bsh = new HashMap<>();// 硬體軟體版本規格

			// 一般翻譯
			ArrayList<SystemLanguageCell> languages_sph = languageDao
					.findAllByLanguageCellSame("ScheduleProductionHistory", null, 2);
			languages_sph.forEach(x -> {
				mapLanguages_sph.put(x.getSltarget(), x);
			});
			ArrayList<SystemLanguageCell> languages_mrn = languageDao.findAllByLanguageCellSame("ManufactureRuleNumber",
					null, 2);
			languages_mrn.forEach(x -> {
				mapLanguages_mrn.put(x.getSltarget(), x);
			});
			ArrayList<SystemLanguageCell> languages_bsh = languageDao.findAllByLanguageCellSame("BomSoftwareHardware",
					null, 2);
			languages_bsh.forEach(x -> {
				mapLanguages_bsh.put(x.getSltarget(), x);
			});

			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons_sph = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons_mrn = new JsonObject();// 回傳欄位-細節名稱
			JsonObject resultDetailTJsons_bsh = new JsonObject();// 回傳欄位-細節名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-一般名稱

			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = ScheduleProductionHistory.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons_sph = packageService.resultSet(fields, exceptionCell, mapLanguages_sph);
			resultDetailTJsons_mrn = packageService.resultSet(fields, exceptionCell, mapLanguages_mrn);
			resultDetailTJsons_bsh = packageService.resultSet(fields, exceptionCell, mapLanguages_bsh);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sphpon", "Ex:製令單?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sphbpmnb", "Ex:產品號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectArr = new JsonArray();
			selectArr.add("取消生產_0");
			selectArr.add("準備物料_1");
			selectArr.add("預約生產_2");
			selectArr.add("準備生產_3");
			selectArr.add("生產中_4");
			selectArr.add("生產結束_5");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "sphprogress", "Ex:單據狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons_sph);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			searchSetJsonAll.add("resultDetailThead_mrn", resultDetailTJsons_mrn);
			searchSetJsonAll.add("resultDetailThead_bsh", resultDetailTJsons_bsh);

			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ScheduleProductionHistory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ScheduleProductionHistory.class);

			// Step4-2.資料區分(一般/細節)
			ArrayList<ScheduleProductionHistory> entitys = productionHistoryDao.findAllBySearch(searchData.getSphpon(),
					null, null, null, null, null, null, pageable);

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
		String entityFormatJson = packageService.beanToJson(new ScheduleProductionHistory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("sphid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ScheduleProductionHistory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleProductionHistory>>() {
					});

			// Step2.資料檢查
			for (ScheduleProductionHistory entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<ScheduleProductionHistory> checkDatas = productionHistoryDao
						.findAllByCheck(entityData.getSphpon(), null, null, null);
				if (checkDatas.size() >= 1) {
					ScheduleProductionHistory checkOne = checkDatas.get(0);
					// *******尚未完成

					// 檢查 SN是否重複?

					// 檢查 軟硬體版本是否填寫好?

					// 檢查

				} else {
					// 沒有此號?
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getSphpon() });
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ScheduleProductionHistory> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSphid() != null) {
				ScheduleProductionHistory entityDataOld = productionHistoryDao.getReferenceById(x.getSphid());
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSphssn(x.getSphssn());// 開始SN
				entityDataOld.setSphesn(x.getSphesn());// 結束SN
				entityDataOld.setSphprnote1(x.getSphprnote1());// 製造1
				entityDataOld.setSphprnote2(x.getSphprnote2());// 製造2
				entityDataOld.setSphbsh(x.getSphbsh());// 產品軟硬體
				entityDataOld.setSphprogress(3);// 進度 完成ERP工單(準備物料)=1 完成注意事項(預約生產)=2 完成->流程卡(準備生產)=3 (生產中)=4 (生產結束)=5

				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		productionHistoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
//		ArrayList<ScheduleProductionHistory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
//		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
//			// Step1.資料轉譯(一般)
//			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
//					new TypeReference<ArrayList<ScheduleProductionHistory>>() {
//					});
//
//			// Step2.資料檢查
//			for (ScheduleProductionHistory entityData : entityDatas) {
//				// 檢查-名稱重複(有資料 && 不是同一筆資料)
//				ArrayList<ScheduleProductionHistory> checkDatas = productionHistoryDao
//						.findAllByCheck(entityData.getSphpon(), null, null, null);
//
//			}
//		}

		// =======================資料整理=======================
		// 資料Data
//		ArrayList<ScheduleProductionHistory> saveDatas = new ArrayList<>();

		// =======================資料儲存=======================
		// 資料Detail

		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ScheduleProductionHistory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleProductionHistory>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ScheduleProductionHistory> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSphid() != null) {
				ScheduleProductionHistory entityDataOld = productionHistoryDao.findById(x.getSphid()).get();
				int stop = productionHistoryDao
						.findAllBySearch(entityDataOld.getSphpon(), null, null, null, null, null, null, null).size();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				entityDataOld.setSphpon(entityDataOld.getSphpon() + "Stop" + stop);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		productionHistoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
//		ArrayList<ScheduleProductionHistory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
//		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
//			// Step1.資料轉譯(一般)
//			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
//					new TypeReference<ArrayList<ScheduleProductionHistory>>() {
//					});
//			// Step2.資料檢查
//		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入

		// 一般-移除內容

		// =======================資料儲存=======================
		// 資料Data

		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	public PackageBean getReport(PackageBean packageBean) throws Exception {
//		String entityReport = packageBean.getEntityReportJson();
//		JsonArray reportAry = packageService.StringToAJson(entityReport);
//		List<ScheduleProductionHistory> entitys = new ArrayList<>();
//		Map<String, String> sqlQuery = new HashMap<>();
//		// =======================查詢語法=======================
//		// 拼湊SQL語法
//		
//		
//
//		// 資料包裝
//		String entityJsonDatas = packageService.beanToJson(entitys);
//		packageBean.setEntityJson(entityJsonDatas);

		return packageBean;
	}
}
