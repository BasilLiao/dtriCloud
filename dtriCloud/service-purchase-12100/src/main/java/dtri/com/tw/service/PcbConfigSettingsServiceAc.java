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

import dtri.com.tw.pgsql.dao.PcbConfigSettingsDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.PcbConfigSettings;
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
public class PcbConfigSettingsServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private PcbConfigSettingsDao configSettingsDao;

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
		orders.add(new Order(Direction.DESC, "pcspnb"));// 品號
		orders.add(new Order(Direction.DESC, "pcspname"));// 品名
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<PcbConfigSettings> entitys = configSettingsDao.findPcsBySearch(null, null, null, null, null, null,
					pageable);

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("PcbConfigSettings", null,
					2);
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
			// 有用繼承的方法 : 建立一個存放所有欄位的清單
			Field[] fields = PackageService.getEntityFields(PcbConfigSettings.class);
			// 非繼承的方法物件
			// Field[] fields = PcbConfigSettings.class.getDeclaredFields();

			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "pcspnb", "Ex:品號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "pcspname", "Ex:品名?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "pcspspecification", "Ex:產品規格?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "pcspcbname", "Ex:板廠商?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "pcsrduser", "Ex:RD負責人?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sysnote", "Ex:備註?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			PcbConfigSettings searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					PcbConfigSettings.class);

			ArrayList<PcbConfigSettings> entitys = configSettingsDao.findPcsBySearch(null, null, null, null, null, null,
					pageable);
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
		String entityFormatJson = packageService.beanToJson(new PcbConfigSettings());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("pcsid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<PcbConfigSettings> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<PcbConfigSettings>>() {
					});

			// Step2.資料檢查
			for (PcbConfigSettings entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<PcbConfigSettings> checkDatas = configSettingsDao.findPcsByCheck(null, entityData.getPcspnb(),
						null, null, null, null, null);
				for (PcbConfigSettings checkData : checkDatas) {
					if (checkData.getPcsid().compareTo(entityData.getPcsid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getPcspnb() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<PcbConfigSettings> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getPcsid() != null) {
				// 從資料庫取得原始資料 (Managed 狀態)
				PcbConfigSettings entityDataOld = configSettingsDao.getReferenceById(x.getPcsid());

				// --- 基礎系統欄位更新 ---
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysnote(x.getSysnote());
				entityDataOld.setSysstatus(x.getSysstatus());
				entityDataOld.setSyssort(x.getSyssort());
				entityDataOld.setSysheader(false);

				// --- PCB 業務欄位更新 (補齊部分) ---
				entityDataOld.setPcspnb(x.getPcspnb()); // 品號
				entityDataOld.setPcspname(x.getPcspname()); // 品名
				entityDataOld.setPcspmodel(x.getPcspmodel()); // 型號
				entityDataOld.setPcspspecification(x.getPcspspecification()); // 規格
				entityDataOld.setPcspcbname(x.getPcspcbname()); // 板廠
				entityDataOld.setPcspcbaname(x.getPcspcbaname()); // 打件廠
				entityDataOld.setPcspmuser(x.getPcspmuser()); // PM
				entityDataOld.setPcsrduser(x.getPcsrduser()); // RD
				entityDataOld.setPcscname(x.getPcscname()); // 設定檔名/備註
				entityDataOld.setPcsversion(x.getPcsversion()); // 版本號

				// --- 規格數值更新 ---
				entityDataOld.setPcslcount(x.getPcslcount()); // 層數
				entityDataOld.setPcsmtype(x.getPcsmtype()); // 板材類型
				entityDataOld.setPcsbthickness(x.getPcsbthickness()); // 銅厚

				// --- 檔案資訊更新 (通常檔案名與大小會隨檔案更動) ---
				// 只有當前端有傳入新檔名或新資訊時才更新
				if (x.getPcsfname() != null && !x.getPcsfname().isEmpty()) {
					entityDataOld.setPcsfname(x.getPcsfname());
					entityDataOld.setPcsfsize(x.getPcsfsize());
					entityDataOld.setPcsftype(x.getPcsftype());
				}

				// --- 檔案本體 (bytea) 處理 ---
				// 注意：如果 x.getPcsfbinary() 為空，應保留舊檔，避免被覆蓋為 null
				if (x.getPcsfbinary() != null && x.getPcsfbinary().length > 0) {
					entityDataOld.setPcsfbinary(x.getPcsfbinary());
				}

				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		configSettingsDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	@Transactional // 建議開啟事務，確保批次儲存的一致性
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<PcbConfigSettings> entityDatas = new ArrayList<>();

		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<PcbConfigSettings>>() {
					});

			// Step2.資料檢查
			for (PcbConfigSettings entityData : entityDatas) {
				// 檢查：品號 (pcspnb) 是否已存在？
				// 這裡呼叫我們在 Dao 定義的 findMetadataByCheck，傳入品號進行精準比對
				List<PcbConfigSettings> checkDatas = configSettingsDao.findPcsByCheck(null, entityData.getPcspnb(),
						null, null, null, null, null);

				if (!checkDatas.isEmpty()) {
					// 如果找到資料，表示品號重複
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getPcspnb() });
				}
			}
		}

		// =======================資料整理=======================
		// 資料Data
		ArrayList<PcbConfigSettings> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// --- 系統欄位初始化 ---
			Date now = new Date();
			x.setSysmdate(now);
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(now);
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(now);
			x.setSyscuser(packageBean.getUserAccount());

			x.setSysheader(false);
			x.setSyssort(0); // 預設排序
			x.setSysstatus(0); // 預設狀態

			// --- 核心關鍵：強制將 ID 設為 null 觸發資料庫流水號 ---
			x.setPcsid(null);

			// --- 數值初始化 (防止 null 導致資料庫報錯) ---
			if (x.getPcsfsize() == null)
				x.setPcsfsize(0L);
			if (x.getPcslcount() == null)
				x.setPcslcount(0);
			if (x.getPcsmtype() == null)
				x.setPcsmtype(0);
			if (x.getPcsbthickness() == null)
				x.setPcsbthickness(0);

			saveDatas.add(x);
		});

		// =======================資料儲存=======================
		// 執行批次存檔
		configSettingsDao.saveAll(saveDatas);

		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<PcbConfigSettings> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<PcbConfigSettings>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<PcbConfigSettings> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getPcsid() != null) {
				PcbConfigSettings entityDataOld = configSettingsDao.findById(x.getPcsid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		configSettingsDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<PcbConfigSettings> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<PcbConfigSettings>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<PcbConfigSettings> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getPcsid() != null) {
				PcbConfigSettings entityDataOld = configSettingsDao.getReferenceById(x.getPcsid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		configSettingsDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<PcbConfigSettings> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();

		// =======================查詢語法=======================
		// 1. 注意表名已改為 pcb_config_settings
		String nativeQuery = "SELECT e.* FROM pcb_config_settings e WHERE ";

		for (JsonElement x : reportAry) {
			String[] parts = x.getAsString().split("<_>");
			String cellNameRaw = parts[0]; // 例如：pcspnb
			String where = parts[1];
			String value = parts.length > 2 ? parts[2] : ""; // 有可能空白
			String valueType = parts.length > 3 ? parts[3] : "string";

			// 自動處理欄位轉換 (例如：pcspnb -> pcs_p_nb, sysmdate -> sys_m_date)
			// 邏輯：在 pcs 或 sys 之後，以及某些特定縮寫字元前補底線
			String cellName = cellNameRaw.replace("pcs", "pcs_").replace("sys", "sys_").replace("pcs_p", "pcs_p_")
					.replace("pcs_f", "pcs_f_").replace("pcs_l", "pcs_l_").replace("pcs_m", "pcs_m_")
					.replace("pcs_b", "pcs_b_").replace("sys_m", "sys_m_").replace("sys_c", "sys_c_")
					.replace("sys_o", "sys_o_")
					// 針對特定多底線欄位微調
					.replace("pcs_p_nb", "pcs_p_nb").replace("pcs_p_name", "pcs_p_name")
					.replace("pcs_p_m", "pcs_p_model").replace("pcs_p_specification", "pcs_p_specification")
					.replace("pcs_f_binary", "pcs_f_binary").replace("pcs_f_name", "pcs_f_name")
					.replace("pcs_f_size", "pcs_f_size").replace("pcs_f_type", "pcs_f_type");

			// 清理可能重複的底線 (例如變成了 pcs__p)
			cellName = cellName.replaceAll("_{2,}", "_");

			switch (where) {
			case "AllSame":
				nativeQuery += "(e." + cellName + " = :" + cellNameRaw + ") AND ";
				sqlQuery.put(cellNameRaw, value + "<_>" + valueType);
				break;
			case "NotSame":
				nativeQuery += "(e." + cellName + " != :" + cellNameRaw + ") AND ";
				sqlQuery.put(cellNameRaw, value + "<_>" + valueType);
				break;
			case "Like":
				nativeQuery += "(e." + cellName + " LIKE :" + cellNameRaw + ") AND ";
				sqlQuery.put(cellNameRaw, "%" + value + "%<_>" + valueType);
				break;
			case "NotLike":
				nativeQuery += "(e." + cellName + " NOT LIKE :" + cellNameRaw + ") AND ";
				sqlQuery.put(cellNameRaw, "%" + value + "%<_>" + valueType);
				break;
			case "MoreThan":
				nativeQuery += "(e." + cellName + " >= :" + cellNameRaw + ") AND ";
				sqlQuery.put(cellNameRaw, value + "<_>" + valueType);
				break;
			case "LessThan":
				nativeQuery += "(e." + cellName + " <= :" + cellNameRaw + ") AND ";
				sqlQuery.put(cellNameRaw, value + "<_>" + valueType);
				break;
			}
		}

		// 清除結尾的 AND 並加上排序與分頁
		if (nativeQuery.endsWith("WHERE ")) {
			nativeQuery = nativeQuery.replace("WHERE ", "");
		} else {
			nativeQuery = StringUtils.removeEnd(nativeQuery, "AND ");
		}

		// 根據新欄位排序：品號 (pcs_p_nb)
		nativeQuery += " ORDER BY e.pcs_p_nb ASC, e.pcs_version DESC ";
		nativeQuery += " LIMIT 10000 OFFSET 0 ";

		Query query = em.createNativeQuery(nativeQuery, PcbConfigSettings.class);

		// =======================查詢參數設定=======================
		sqlQuery.forEach((key, valAndType) -> {
			String val = valAndType.split("<_>")[0];
			String tp = valAndType.split("<_>")[1];
			if (tp.equals("dateTime")) {
				query.setParameter(key, Fm_T.toDate(val));
			} else if (tp.equals("number")) {
				// 判斷是否為 Long (ID) 或 Integer
				if (key.contains("id") || key.contains("size")) {
					query.setParameter(key, Long.parseLong(val));
				} else {
					query.setParameter(key, Integer.parseInt(val));
				}
			} else {
				query.setParameter(key, val);
			}
		});

		try {
			entitys = query.getResultList();
		} catch (PersistenceException e) {
			e.printStackTrace(); // 方便 Debug
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, Lan.zh_TW, null);
		}

		// 資料包裝
		packageBean.setEntityJson(packageService.beanToJson(entitys));

		return packageBean;
	}
}
