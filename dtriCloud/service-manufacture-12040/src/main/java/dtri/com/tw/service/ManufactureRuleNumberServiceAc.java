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

import dtri.com.tw.pgsql.dao.ManufactureRuleNumberDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.ManufactureRuleNumber;
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
public class ManufactureRuleNumberServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private ManufactureRuleNumberDao ruleNumberDao;

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
		orders.add(new Order(Direction.ASC, "syssort"));// 排序
		orders.add(new Order(Direction.ASC, "mrngid"));// 規則群組
		orders.add(new Order(Direction.ASC, "mrnname"));// 規則名稱

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<ManufactureRuleNumber> entitys = ruleNumberDao.findAllBySearch(null, null, pageable);

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("ManufactureRuleNumber",
					null, 2);
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
			Field[] fields = ManufactureRuleNumber.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "mrngname", "Ex:項目組名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "mrnname", "Ex:項目名稱?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ManufactureRuleNumber searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ManufactureRuleNumber.class);

			// Step4-2.資料區分(一般/細節)
			ArrayList<ManufactureRuleNumber> entitys = ruleNumberDao.findAllBySearch(searchData.getMrngname(),
					searchData.getMrnname(), pageable);

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
		String entityFormatJson = packageService.beanToJson(new ManufactureRuleNumber());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("mrnid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ManufactureRuleNumber> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ManufactureRuleNumber>>() {
					});

			// Step2.資料檢查
			for (ManufactureRuleNumber entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<ManufactureRuleNumber> checkDatas = ruleNumberDao.findAllByCheck(entityData.getMrngname(),
						entityData.getMrnname(), null);
				for (ManufactureRuleNumber checkData : checkDatas) {
					if (checkData.getMrnid().compareTo(entityData.getMrnid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getMrnname() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ManufactureRuleNumber> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getMrnid() != null) {
				ManufactureRuleNumber entityDataOld = ruleNumberDao.getReferenceById(x.getMrnid());

				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setMrngname(x.getMrngname());// 群組名稱

				entityDataOld.setMrnname(x.getMrnname());// 項目名稱
				entityDataOld.setMrnval(x.getMrnval());// 值
				entityDataOld.setMrn0000c(x.getMrn0000c());// 勾選
				entityDataOld.setMrnyywwc(x.getMrnyywwc());// 勾選
				// 處理 mrn0000 欄位（固定4碼格式）
				String mrn0000 = x.getMrn0000();
				if (mrn0000 != null) {
					mrn0000 = mrn0000.length() > 4 ? mrn0000.substring(0, 4)
							: String.format("%4s", mrn0000).replace(' ', '0');
					entityDataOld.setMrn0000(mrn0000);
				}

				// 處理 mrnyyww 欄位（固定4碼格式）
				String mrnyyww = x.getMrnyyww();
				if (mrnyyww != null) {
					mrnyyww = mrnyyww.length() > 4 ? mrnyyww.substring(0, 4)
							: String.format("%4s", mrnyyww).replace(' ', '0');
					entityDataOld.setMrnyyww(mrnyyww);
				}

				if (!x.getMrn0000c()) {
					entityDataOld.setMrn0000("");
				}
				if (!x.getMrnyywwc()) {
					entityDataOld.setMrnyyww("");
				}

				// 群駔名稱變化
				ArrayList<ManufactureRuleNumber> numbers = ruleNumberDao.findAllByMrngid(entityDataOld.getMrngid());
				numbers.forEach(g -> {
					g.setMrngname(x.getMrngname());
					g.setSyssort(x.getSyssort());
				});
				ruleNumberDao.saveAll(numbers);

				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		ruleNumberDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<ManufactureRuleNumber> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ManufactureRuleNumber>>() {
					});

			// Step2.資料檢查
			for (ManufactureRuleNumber entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<ManufactureRuleNumber> checkDatas = ruleNumberDao.findAllByCheck(entityData.getMrngname(),
						entityData.getMrnname(), null);
				if (checkDatas.size() != 0) {// 有重複?
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getMrnname() });

				}
			}
		}

		// =======================資料整理=======================
		// 資料Data
		ArrayList<ManufactureRuleNumber> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 新增
			x.setMrnid(null);
			Long gid = x.getMrngid();
			// 如果是全新的
			if (gid == null) {
				// 是否有建立[manufacture_rule_number_g_seq]?
				if (ruleNumberDao.checkIfSequenceExists() == 0) {
					ruleNumberDao.createManufactureRuleNumberGSeq();
				}
				gid = ruleNumberDao.getManufactureRuleNumberGSeq();
				x.setMrngid(gid);
			} else {
				// 如果不是新的 是用複製的
				ArrayList<ManufactureRuleNumber> oldGroup = ruleNumberDao.findAllByMrngid(gid);
				if (oldGroup.size() > 0) {
					x.setMrngname(oldGroup.get(0).getMrngname());
					x.setSyssort(oldGroup.get(0).getSyssort());
				} else {
					// 任何複製 可能意外?不執行
					return;
				}

			}
			// 掛載-流水號
			if (x.getMrn0000c()) {
				x.setMrn0000("0001");
			}
			// 掛載-年周
			if (x.getMrnyywwc()) {
				// 取得當年當周
				int week = Fm_T.getWeek(new Date());
				int year = Fm_T.getYear(new Date());
				String weekStr = String.format("%02d", week); // 例：3 → "03"
				String yearStr = String.format("%02d", year % 100); // "24"
				x.setMrnyyww(yearStr + weekStr);
			}

			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSysheader(false);
			saveDatas.add(x);
		});
		// =======================資料儲存=======================
		// 資料Detail
		ruleNumberDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ManufactureRuleNumber> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ManufactureRuleNumber>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ManufactureRuleNumber> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getMrnid() != null) {
				ManufactureRuleNumber entityDataOld = ruleNumberDao.findById(x.getMrnid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		ruleNumberDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ManufactureRuleNumber> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ManufactureRuleNumber>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ManufactureRuleNumber> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getMrnid() != null) {
				ManufactureRuleNumber entityDataOld = ruleNumberDao.getReferenceById(x.getMrnid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		ruleNumberDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<ManufactureRuleNumber> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM manufacture_rule_number e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("mrn", "mrn_");

			cellName = cellName.replace("mrn_gid", "mrn_g_id");
			cellName = cellName.replace("mrn_yymmc", "mrn_yymm_c");
			cellName = cellName.replace("mrn_0000c", "mrn_0000_c");
			cellName = cellName.replace("mrn_gname", "mrn_g_name");

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
		nativeQuery += " order by e.mrn_g_name asc, e.mrn_name asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, ManufactureRuleNumber.class);
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
