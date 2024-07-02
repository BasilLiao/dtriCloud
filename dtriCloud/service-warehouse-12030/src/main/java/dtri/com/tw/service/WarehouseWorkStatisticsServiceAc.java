package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseHistoryDao;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseHistory;
import dtri.com.tw.pgsql.entity.WarehouseWorkStatisticsFront;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class WarehouseWorkStatisticsServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private WarehouseHistoryDao historyDao;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.DESC, "syscdate"));// 時間
		orders.add(new Order(Direction.ASC, "whfuser"));// 完成人
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			Date ssyscdate = Fm_T.toDateTime(Fm_T.to_y_M_d(new Date()) + " 00:00:00");
			Date esyscdate = Fm_T.toDateTime(Fm_T.to_y_M_d(new Date()) + " 23:59:59");
			ArrayList<WarehouseHistory> entitys = historyDao.findAllBySearch(null, null, null, "(User)", ssyscdate,
					esyscdate, pageable);
			ArrayList<WarehouseWorkStatisticsFront> wwsfEntitys = new ArrayList<WarehouseWorkStatisticsFront>();
			TreeMap<String, JsonObject> entityMap = new TreeMap<String, JsonObject>();// 時間||{人名:次數}
			JsonObject whfusers = new JsonObject();
			entitys.forEach(w -> {
				String dateyMd = Fm_T.to_y_M_d(w.getSyscdate());
				if (!w.getWhfuser().equals("")) {
					if (entityMap.containsKey(dateyMd)) {
						// 有同一天?
						JsonObject wwsf = entityMap.get(dateyMd);
						if (wwsf.has(w.getWhfuser())) {
							// 同一人?
							int times = wwsf.get(w.getWhfuser()).getAsInt() + 1;
							wwsf.addProperty(w.getWhfuser(), times);
							entityMap.put(dateyMd, wwsf);
						} else {
							// 全新人?
							wwsf.addProperty(w.getWhfuser(), 1);
							entityMap.put(dateyMd, wwsf);
						}
					} else {
						// 全新的一天?
						JsonObject wwsf = new JsonObject();
						wwsf.addProperty(w.getWhfuser(), 1);
						entityMap.put(dateyMd, wwsf);
					}
					if (!whfusers.has(w.getWhfuser())) {
						whfusers.addProperty(w.getWhfuser(), true);
					}
				}
			});

			// 整理資料
			entityMap.forEach((key, v) -> {
				WarehouseWorkStatisticsFront one = new WarehouseWorkStatisticsFront();
				one.setId(wwsfEntitys.size() + "");
				one.setWwstimes(v.toString());
				one.setWwsdate(key);
				one.setWwsnames(whfusers.toString());
				wwsfEntitys.add(one);
			});

			// Step3-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(wwsfEntitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao
					.findAllByLanguageCellSame("WarehouseWorkStatisticsFront", null, 2);
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
			Field[] fields = WarehouseWorkStatisticsFront.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wwsnames", "Ex:人類名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "ssyscdate", "Ex:(起)", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "esyscdate", "Ex:(終)", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseWorkStatisticsFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					WarehouseWorkStatisticsFront.class);

			ArrayList<WarehouseHistory> entitys = historyDao.findAllBySearch(searchData.getWwsnames(), null, null, "(User)",
					searchData.getSsyscdate(), searchData.getEsyscdate(), pageable);
			ArrayList<WarehouseWorkStatisticsFront> wwsfEntitys = new ArrayList<WarehouseWorkStatisticsFront>();
			TreeMap<String, JsonObject> entityMap = new TreeMap<String, JsonObject>();// 時間||{人名:次數}
			JsonObject whfusers = new JsonObject();
			entitys.forEach(w -> {
				String dateyMd = Fm_T.to_y_M_d(w.getSyscdate());
				if (!w.getWhfuser().equals("")) {
					if (entityMap.containsKey(dateyMd)) {
						// 有同一天?
						JsonObject wwsf = entityMap.get(dateyMd);
						if (wwsf.has(w.getWhfuser())) {
							// 同一人?
							int times = wwsf.get(w.getWhfuser()).getAsInt() + 1;
							wwsf.addProperty(w.getWhfuser(), times);
							entityMap.put(dateyMd, wwsf);
						} else {
							// 全新人?
							wwsf.addProperty(w.getWhfuser(), 1);
							entityMap.put(dateyMd, wwsf);
						}
					} else {
						// 全新的一天?
						JsonObject wwsf = new JsonObject();
						wwsf.addProperty(w.getWhfuser(), 1);
						entityMap.put(dateyMd, wwsf);
					}
					if (!whfusers.has(w.getWhfuser())) {
						whfusers.addProperty(w.getWhfuser(), true);
					}
				}
			});

			// 整理資料
			entityMap.forEach((key, v) -> {
				WarehouseWorkStatisticsFront one = new WarehouseWorkStatisticsFront();
				one.setId(wwsfEntitys.size() + "");
				one.setWwstimes(v.toString());
				one.setWwsdate(key);
				one.setWwsnames(whfusers.toString());
				wwsfEntitys.add(one);
			});
			// Step4-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(wwsfEntitys);
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
		String entityFormatJson = packageService.beanToJson(new WarehouseWorkStatisticsFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
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
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		// =======================查詢語法=======================
		// 拼湊SQL語法
		// =======================查詢參數=======================
		// 資料包裝
		return packageBean;
	}
}
