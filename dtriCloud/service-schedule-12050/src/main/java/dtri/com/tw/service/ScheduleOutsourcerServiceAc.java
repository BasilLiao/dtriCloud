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

import dtri.com.tw.pgsql.dao.ScheduleOutsourcerDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
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
public class ScheduleOutsourcerServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private ScheduleOutsourcerDao outsourcerDao;

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
		orders.add(new Order(Direction.ASC, "soscstatus"));// 生管狀態
		orders.add(new Order(Direction.ASC, "sofodate"));// 加工廠開工日
		orders.add(new Order(Direction.ASC, "somcdate"));// 預計齊料日
		orders.add(new Order(Direction.ASC, "sonb"));// 工單
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<ScheduleOutsourcer> entitys = outsourcerDao.findAllBySearch(null, null, null, null, null, null,
					null, null, null, null, 0, pageable);

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("ScheduleOutsourcer", null,
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
			Field[] fields = ScheduleOutsourcer.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sonb", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sopnb", "Ex:產品品號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sopname", "Ex:產品品名?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sopspecifications", "Ex:產品規格?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sofname", "Ex:加工廠?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "souname", "Ex:開單人?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sofodate", "Ex:加工廠-上線日?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "somcdates", "Ex:預計-齊料日(起)?", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "somcdatee", "Ex:預計-齊料日(終)?", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_1);

			// Step3-5. 建立查詢項目 狀態 0=暫停中/1=未生產/2=已發料/3=生產中 Y=已完工/y=指定完工<br>
			JsonArray selectStatusArr = new JsonArray();
			selectStatusArr.add("暫停中_0");
			selectStatusArr.add("未生產_1");
			selectStatusArr.add("已發料_2");
			selectStatusArr.add("生產中_3");
			selectStatusArr.add("已完工_Y");
			selectStatusArr.add("指定完工_y");
			selectStatusArr.add("已作廢_V");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sostatus", "Ex:單據狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// Step3-5. 建立查詢項目
			selectStatusArr = new JsonArray();
			selectStatusArr.add("未結束_0");
			selectStatusArr.add("已結束_2");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sysstatus", "Ex:狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ScheduleOutsourcer searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ScheduleOutsourcer.class);
			searchData.setSysstatus(searchData.getSysstatus() == null ? 0 : searchData.getSysstatus());
			// 時間查閱
			if (searchData.getSomcdates() != null && !searchData.getSomcdates().equals("")) {
				Date somcdates = new Date(Long.parseLong(searchData.getSomcdates()));
				searchData.setSomcdates(Fm_T.to_yMd_Hms(somcdates));
			}
			if (searchData.getSomcdatee() != null && !searchData.getSomcdatee().equals("")) {
				Date somcdatee = new Date(Long.parseLong(searchData.getSomcdatee()));
				searchData.setSomcdatee(Fm_T.to_yMd_Hms(somcdatee));
			}
			ArrayList<ScheduleOutsourcer> entitys = outsourcerDao.findAllBySearch(searchData.getSonb(),
					searchData.getSopnb(), searchData.getSopname(), searchData.getSopspecifications(),
					searchData.getSostatus(), searchData.getSofname(), searchData.getSouname(),
					searchData.getSofodate(), searchData.getSomcdates(), searchData.getSomcdatee(),
					searchData.getSysstatus(), pageable);
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
		String entityFormatJson = packageService.beanToJson(new ScheduleOutsourcer());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("soid_");

		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_somcdates_somcdatee");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean, String action) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ScheduleOutsourcer> entityDatas = new ArrayList<>();
		ArrayList<ScheduleOutsourcer> entityDatasSave = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleOutsourcer>>() {
					});
			// Step2.資料檢查
		}

		// =======================資料整理=======================
		// action : mp=製造/wm=倉庫/mc=物控/sc=生管
		entityDatas.forEach(x -> {
			ScheduleOutsourcer o = outsourcerDao.getReferenceById(x.getSoid());
			JsonArray noteOlds = new JsonArray();
			JsonObject noteOne = new JsonObject();

			switch (action) {
			case "mp":// 製造修改
				System.out.println("mp=製造");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());
				if (o.getSompnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSompnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSompnote());
						noteOlds.add(noteOne);
						o.setSompnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSompnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSompnote()).getAsJsonArray();
					JsonElement noteOld = noteOlds.get(noteOlds.size() - 1).getAsJsonObject();
					boolean checkNotSame = true;
					String contentOld = noteOld.getAsJsonObject().get("content").getAsString().replaceAll("\n", "");
					if (contentOld.equals(contentNew)) {
						checkNotSame = false;
						break;
					}

					// 必須不相同+不能是沒輸入值
					if (checkNotSame && !contentNew.equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSompnote());
						noteOlds.add(noteOne);
						o.setSompnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}

				break;
			case "wm":// 倉儲修改
				System.out.println("wm=倉庫");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());
				if (o.getSowmnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSowmnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSowmnote());
						noteOlds.add(noteOne);
						o.setSowmnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSowmnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSowmnote()).getAsJsonArray();
					JsonElement noteOld = noteOlds.get(noteOlds.size() - 1).getAsJsonObject();
					boolean checkNotSame = true;
					String contentOld = noteOld.getAsJsonObject().get("content").getAsString().replaceAll("\n", "");
					if (contentOld.equals(contentNew)) {
						checkNotSame = false;
						break;
					}

					// 必須不相同+不能是沒輸入值
					if (checkNotSame && !contentNew.equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSowmnote());
						noteOlds.add(noteOne);
						o.setSowmnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}

				break;
			case "mc":// 物控修改
				System.out.println("mc=物控");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());

				if (!x.getSomcdate().equals("")) {
					Date yMd = Fm_T.toDate(x.getSomcdate());
					if (yMd.before(new Date())) {// yMd < 今天?=1
						// 0=未確認/1未齊料/2已齊料
						o.setSomcstatus(2);
					} else {
						o.setSomcstatus(1);
					}
					o.setSomcdate(x.getSomcdate());
				} else {
					o.setSomcstatus(0);
				}
				if (o.getSomcnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSomcnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSomcnote());
						noteOlds.add(noteOne);
						o.setSomcnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSomcnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSomcnote()).getAsJsonArray();
					JsonElement noteOld = noteOlds.get(noteOlds.size() - 1).getAsJsonObject();
					boolean checkNotSame = true;
					String contentOld = noteOld.getAsJsonObject().get("content").getAsString().replaceAll("\n", "");
					if (contentOld.equals(contentNew)) {
						checkNotSame = false;
						break;
					}

					// 必須不相同+不能是沒輸入值
					if (checkNotSame && !contentNew.equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSomcnote());
						noteOlds.add(noteOne);
						o.setSomcnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}

				break;
			case "sc":// 生管修改
				System.out.println("sc=生管");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());

				o.setSoscstatus(x.getSoscstatus());
				o.setSofodate(x.getSofodate());
				o.setSofokdate(x.getSofokdate());

				if (o.getSoscnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSoscnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSoscnote());
						noteOlds.add(noteOne);
						o.setSoscnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSoscnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSoscnote()).getAsJsonArray();
					JsonElement noteOld = noteOlds.get(noteOlds.size() - 1).getAsJsonObject();
					boolean checkNotSame = true;
					String contentOld = noteOld.getAsJsonObject().get("content").getAsString().replaceAll("\n", "");
					if (contentOld.equals(contentNew)) {
						checkNotSame = false;
						break;
					}

					// 必須不相同+不能是沒輸入值
					if (checkNotSame && !contentNew.equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSoscnote());
						noteOlds.add(noteOne);
						o.setSoscnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}
				break;
			default:
				break;
			}

			entityDatasSave.add(o);
		});

		// =======================資料儲存=======================
		outsourcerDao.saveAll(entityDatasSave);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ScheduleOutsourcer> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleOutsourcer>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ScheduleOutsourcer> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSoid() != null) {
				ScheduleOutsourcer entityDataOld = outsourcerDao.findById(x.getSoid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		outsourcerDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<ScheduleOutsourcer> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM schedule_outsourcer e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");

			cellName = cellName.replace("so", "so_");
			cellName = cellName.replace("so_ywdate", "so_yw_date");
			cellName = cellName.replace("so_odate", "so_o_date");
			cellName = cellName.replace("so_fdate", "so_f_date");
			cellName = cellName.replace("so_pnb", "so_p_nb");
			cellName = cellName.replace("so_pname", "so_p_name");
			cellName = cellName.replace("so_pspecifications", "so_p_specifications");
			cellName = cellName.replace("so_rqty", "so_r_qty");

			cellName = cellName.replace("so_okqty", "so_ok_qty");
			cellName = cellName.replace("so_fname", "so_f_name");
			cellName = cellName.replace("so_fodate", "so_f_o_date");
			cellName = cellName.replace("so_fokdate", "so_f_ok_date");
			cellName = cellName.replace("so_uname", "so_u_name");
			cellName = cellName.replace("so_scnote", "so_sc_note");
			cellName = cellName.replace("so_scstatus", "so_sc_status");
			cellName = cellName.replace("so_mcnote", "so_mc_note");

			cellName = cellName.replace("so_mcdate", "so_mc_date");
			cellName = cellName.replace("so_wmnote", "so_wm_note");
			cellName = cellName.replace("so_wmprogress", "so_wm_progress");
			cellName = cellName.replace("so_mpprogress", "so_mp_progress");

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
		nativeQuery += " order by e.ssl_bsl_sn_nb asc";
		nativeQuery += " LIMIT 25000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, ScheduleOutsourcer.class);
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
