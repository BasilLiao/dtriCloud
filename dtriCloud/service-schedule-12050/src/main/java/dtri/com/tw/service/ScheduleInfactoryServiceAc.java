package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.dao.ScheduleShortageNotificationDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
import dtri.com.tw.pgsql.entity.ScheduleShortageNotification;
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
public class ScheduleInfactoryServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private ScheduleInfactoryDao infactoryDao;

	@Autowired
	private BasicNotificationMailDao notificationMailDao;

	@Autowired
	private ScheduleShortageNotificationDao notificationDao;

	@Autowired
	private EntityManager em;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "siodate"));// 預計開工日
		orders.add(new Order(Direction.ASC, "sifdate"));// 預計完工日
		orders.add(new Order(Direction.ASC, "sinb"));// 工單
		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<ScheduleInfactory> entitys = infactoryDao.findAllBySearch(null, null, null, null, null, null,
					null, null, null, null, null, null, null, null, 0, pageable);

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
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("ScheduleInfactory", null,
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
			Field[] fields = ScheduleInfactory.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sinb", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sipnb", "Ex:排除#", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sipname", "Ex:產品品名?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "sipspecifications", "Ex:產品規格?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "simcnote", "Ex:\"時間?內容", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "siuname", "Ex:開單人?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "simcdates", "Ex:預計-齊料日(起)?", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_lg_1);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "simcdatee", "Ex:預計-齊料日(終)?", true, //
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
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sistatus", "Ex:生產狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// Step3-5. 建立查詢項目
			selectStatusArr = new JsonArray();
			selectStatusArr.add("未結束_0");
			selectStatusArr.add("已結束_2");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sysstatus", "Ex:單據狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// Step3-5. 建立查詢項目{0: "未確認", 1: "未齊料", 2: "已齊料"};
			selectStatusArr = new JsonArray();
			selectStatusArr.add("未確認_0");
			selectStatusArr.add("未齊料_1");
			selectStatusArr.add("已齊料_2");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "simcstatus", "Ex:物控狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// Step3-5. 建立查詢項目
			selectStatusArr = new JsonArray();
			selectStatusArr.add("完工時間_999");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "syssort", "Ex:排序?完工時間", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_1);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ScheduleInfactory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ScheduleInfactory.class);
			searchData.setSysstatus(searchData.getSysstatus() == null ? 0 : searchData.getSysstatus());
			// 時間查閱
			if (searchData.getSimcdates() != null && !searchData.getSimcdates().equals("")) {
				Date simcdates = new Date(Long.parseLong(searchData.getSimcdates()));
				searchData.setSimcdates(Fm_T.to_yMd_Hms(simcdates));
			}
			if (searchData.getSimcdatee() != null && !searchData.getSimcdatee().equals("")) {
				Date simcdatee = new Date(Long.parseLong(searchData.getSimcdatee()));
				searchData.setSimcdatee(Fm_T.to_yMd_Hms(simcdatee));
			}
			List<String> sipnbList = null;
			String notsipnb1 = null;
			String notsipnb2 = null;
			if (searchData.getSipnb() != null && searchData.getSipnb().contains("#")) {
				sipnbList = new ArrayList<String>();
				sipnbList = Arrays.asList(searchData.getSipnb().split(" "));
				notsipnb1 = (sipnbList.size() > 0 && sipnbList.get(0) != null && sipnbList.get(0).contains("#"))
						? sipnbList.get(0).replaceAll("#", "")
						: null;
				notsipnb2 = (sipnbList.size() > 1 && sipnbList.get(1) != null && sipnbList.get(1).contains("#"))
						? sipnbList.get(1).replaceAll("#", "")
						: null;
				searchData.setSipnb(null);
			}
			// 日期(起)
			if (searchData.getSimcdates() != null) {
				searchData.setSimcdates(Fm_T.to_y_M_d(Fm_T.toDate(searchData.getSimcdates())));
			}
			// 日期(終)
			if (searchData.getSimcdatee() != null) {
				searchData.setSimcdatee(Fm_T.to_y_M_d(Fm_T.toDate(searchData.getSimcdatee())));
			}

			ArrayList<ScheduleInfactory> entitys = infactoryDao.findAllBySearch(searchData.getSinb(),
					searchData.getSipnb(), notsipnb1, notsipnb2, searchData.getSipname(),
					searchData.getSipspecifications(), searchData.getSistatus(), searchData.getSifname(),
					searchData.getSiuname(), searchData.getSifodate(), searchData.getSimcdates(),
					searchData.getSimcdatee(), searchData.getSimcnote(), searchData.getSimcstatus(),
					searchData.getSysstatus(), pageable);
			// Step4-2.資料區分(一般/細節)

			if (searchData.getSyssort() == 999) {
				// 年周轉換->依照完工日
				for (ScheduleInfactory e : entitys) {
					String sifdate = e.getSifdate();
					int year = Fm_T.getYear(Fm_T.toDate(sifdate));
					int week = Fm_T.getWeek(Fm_T.toDate(sifdate));
					// 年周格式 → yyyy-Wxx
					String yearWeek = String.format("%d-W%02d", year, week);
					e.setSiywdate(yearWeek);
				}
				// 🔹 2. 依完工日排序
				entitys.sort(Comparator.comparing(e -> e.getSifdate()));

			}

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
		String entityFormatJson = packageService.beanToJson(new ScheduleInfactory());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("siid_");

		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean, String action) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ScheduleInfactory> entityDatas = new ArrayList<>();
		ArrayList<ScheduleInfactory> entityDatasSave = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleInfactory>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// action : mp=製造/wm=倉庫/mc=物控/sc=生管
		entityDatas.forEach(x -> {
			ScheduleInfactory o = infactoryDao.getReferenceById(x.getSiid());
			JsonArray noteOlds = new JsonArray();
			JsonObject noteOne = new JsonObject();

			switch (action) {
			case "mp":// 製造修改
				System.out.println("mp=製造");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());
				if (o.getSimpnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSimpnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSimpnote());
						noteOlds.add(noteOne);
						o.setSimpnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSimpnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSimpnote()).getAsJsonArray();
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
						noteOne.addProperty("content", x.getSimpnote());
						noteOlds.add(noteOne);
						o.setSimpnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}

				break;
			case "wm":// 倉儲修改
				System.out.println("wm=倉庫");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());
				if (o.getSiwmnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSiwmnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSiwmnote());
						noteOlds.add(noteOne);
						o.setSiwmnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSiwmnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSiwmnote()).getAsJsonArray();
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
						noteOne.addProperty("content", x.getSiwmnote());
						noteOlds.add(noteOne);
						o.setSiwmnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}

				break;
			case "mc":// 物控修改
				System.out.println("mc=物控");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());
				System.out.println(x.getSimcdate() + ":" + o.getSimcdate());
				Boolean oldSimcdate = x.getSimcdate().equals(o.getSimcdate());
				if (!x.getSimcdate().equals("")) {
					Date simcDate = Fm_T.toDate(x.getSimcdate()); // 預計其料日
					Date siodate = Fm_T.toDate(o.getSiodate()); // 預計開工日
					if (simcDate != null && siodate != null) {
						// 只比對日期（去除時間）
						LocalDate today = LocalDate.now();
						LocalDate simcLocal = simcDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						LocalDate siodateLocal = siodate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

						// 預計其料日 < 今天 或 < 開工日
						if (simcLocal.isBefore(today) || simcLocal.isBefore(siodateLocal)) {
							o.setSimcstatus(2); // 已齊料
						} else {
							o.setSimcstatus(1); // 未齊料
						}
					} else {
						// 若日期無效，建議記錄 log 或指定預設值
						o.setSimcstatus(0); // 預設未確認
					}
					o.setSimcdate(x.getSimcdate());
				} else {
					o.setSimcstatus(0);
				}
				if (o.getSimcnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSimcnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSimcnote());
						noteOlds.add(noteOne);
						o.setSimcnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					} else {
						// 物控只改日期
						if (!oldSimcdate) {
							noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
							noteOne.addProperty("user", packageBean.getUserAccount());
							noteOne.addProperty("content", x.getSimcdate() + "_" + packageBean.getUserAccount());
							noteOlds.add(noteOne);
							o.setSimcnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
						}
					}
				} else {
					boolean checkNotSame = true;
					String contentNew = x.getSimcnote().replaceAll("\n", "");
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					noteOlds = JsonParser.parseString(o.getSimcnote()).getAsJsonArray();
					JsonElement noteOld = noteOlds.get(noteOlds.size() - 1).getAsJsonObject();
					String contentOld = noteOld.getAsJsonObject().get("content").getAsString().replaceAll("\n", "");
					if (contentNew.equals("") && !oldSimcdate) {
						// 物控只改日期
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSimcdate() + "_" + packageBean.getUserAccount());
						noteOlds.add(noteOne);
						o.setSimcnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
						checkNotSame = false;
						break;
					}
					checkNotSame = !contentNew.equals(contentOld);
					// 必須不相同+不能是沒輸入值
					if (checkNotSame && !contentNew.equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSimcnote());
						noteOlds.add(noteOne);
						o.setSimcnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}

				break;
			case "sc":// 生管修改
				System.out.println("sc=生管");
				o.setSysmdate(new Date());
				o.setSysmuser(packageBean.getUserAccount());

				o.setSiscstatus(x.getSiscstatus() == null ? 0 : x.getSiscstatus());// 生管狀態
				o.setSifodate(x.getSifodate());
				o.setSifokdate(x.getSifokdate());
				o.setSipriority(x.getSipriority());
				// 添加-寄信通知
				if (x.getSipriority() == 1) {
					emailSipriority(o.clone());
				}
				if (o.getSiscnote().equals("[]")) {
					// 空的?+不能是沒輸入值
					if (!x.getSiscnote().equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSiscnote());
						noteOlds.add(noteOne);
						o.setSiscnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				} else {
					// 取出先前的(最新)-最新資料比對->不同內容->添加新的
					String contentNew = x.getSiscnote().replaceAll("\n", "");
					noteOlds = JsonParser.parseString(o.getSiscnote()).getAsJsonArray();
					JsonElement noteOld = noteOlds.get(noteOlds.size() - 1).getAsJsonObject();
					boolean checkNotSame = true;
					String contentOld = noteOld.getAsJsonObject().get("content").getAsString().replaceAll("\n", "");
					if (contentOld.equals(contentNew)) {
						checkNotSame = false;
						// break;
					}

					// 必須不相同+不能是沒輸入值
					if (checkNotSame && !contentNew.equals("")) {
						noteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
						noteOne.addProperty("user", packageBean.getUserAccount());
						noteOne.addProperty("content", x.getSiscnote());
						noteOlds.add(noteOne);
						o.setSiscnote(noteOlds.toString());// 生管備註(格式)人+時間+內容
					}
				}
				break;
			default:
				break;
			}

			entityDatasSave.add(o);
		});

		// =======================資料儲存=======================
		infactoryDao.saveAll(entityDatasSave);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<ScheduleInfactory> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleInfactory>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ScheduleInfactory> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getSiid() != null) {
				ScheduleInfactory entityDataOld = infactoryDao.findById(x.getSiid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		infactoryDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<ScheduleInfactory> entitys = new ArrayList<>();
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
		nativeQuery += " LIMIT 10000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, ScheduleInfactory.class);
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

	// 寄信通知
	private void emailSipriority(ScheduleInfactory one) {

		try {
			// 寄信通知
			// Step1. 取得寄信人
			List<Order> nf_orders = new ArrayList<>();
			nf_orders.add(new Order(Direction.ASC, "ssnsuname"));// 關聯帳號名稱
			nf_orders.add(new Order(Direction.ASC, "ssnsnotice"));// 缺料通知
			PageRequest nf_pageable = PageRequest.of(0, 9999, Sort.by(nf_orders));
			ArrayList<ScheduleShortageNotification> notifications = notificationDao.findAllBySearchSsniqnotice(null,
					null, null, true, nf_pageable);

			// Step2.取得資料
			ArrayList<ScheduleInfactory> infactorys = new ArrayList<ScheduleInfactory>();
			infactorys.add(one);
			System.out.println("測試:" + infactorys.size());

			// Step3.整理資料
			infactorys.forEach(o -> {
				// ====物控備註====
				String somcnoteDiv = "<div>";
				JsonArray somcnotes = JsonParser.parseString(o.getSimcnote()).getAsJsonArray();
				// 避免沒資料
				if (somcnotes.size() > 0) {
					JsonObject somcnote = somcnotes.get(somcnotes.size() - 1).getAsJsonObject();
					somcnoteDiv += "<div>" + somcnote.get("date").getAsString() + "/"
							+ somcnote.get("user").getAsString() + "</div>";
					somcnoteDiv += "<div>" + somcnote.get("content").getAsString() + "</div>";
				}
				somcnoteDiv += "</div>";
				o.setSimcnote(somcnoteDiv);
				// ====生管備註====
				String soscnoteDiv = "<div>";
				JsonArray soscnotes = JsonParser.parseString(o.getSiscnote()).getAsJsonArray();
				// 避免沒資料
				if (soscnotes.size() > 0) {
					JsonObject soscnote = soscnotes.get(soscnotes.size() - 1).getAsJsonObject();
					soscnoteDiv += "<div>" + soscnote.get("date").getAsString() + "/"
							+ soscnote.get("user").getAsString() + "</div>";
					soscnoteDiv += "<div>" + soscnote.get("content").getAsString() + "</div>";
				}
				soscnoteDiv += "</div>";
				o.setSiscnote(soscnoteDiv);
				// ====製造備註====
				String sompnoteDiv = "<div>";
				JsonArray sompnotes = JsonParser.parseString(o.getSimpnote()).getAsJsonArray();
				// 避免沒資料
				if (sompnotes.size() > 0) {
					JsonObject sompnote = sompnotes.get(sompnotes.size() - 1).getAsJsonObject();
					sompnoteDiv += "<div>" + sompnote.get("date").getAsString() + "/"
							+ sompnote.get("user").getAsString() + "</div>";
					sompnoteDiv += "<div>" + sompnote.get("content").getAsString() + "</div>";
				}
				sompnoteDiv += "</div>";
				o.setSimpnote(sompnoteDiv);
				// ====倉庫備註====
				String sowmnoteDiv = "<div>";
				JsonArray sowmnotes = JsonParser.parseString(o.getSiwmnote()).getAsJsonArray();
				// 避免沒資料
				if (sowmnotes.size() > 0) {
					JsonObject sowmnote = sowmnotes.get(sowmnotes.size() - 1).getAsJsonObject();
					sowmnoteDiv += "<div>" + sowmnote.get("date").getAsString() + "/"
							+ sowmnote.get("user").getAsString() + "</div>";
					sowmnoteDiv += "<div>" + sowmnote.get("content").getAsString() + "</div>";
				}
				sowmnoteDiv += "</div>";
				o.setSiwmnote(sowmnoteDiv);
				// 製令單狀態修正
				switch (o.getSistatus()) {
				case "0":
					o.setSistatus("暫停中");
					break;
				case "1":
					o.setSistatus("未生產");
					break;
				case "2":
					o.setSistatus("已發料");
					break;
				case "3":
					o.setSistatus("生產中");
					break;
				case "Y":
					o.setSistatus("已完工");
					break;
				case "y":
					o.setSistatus("指定完工");
					break;
				case "V":
					o.setSistatus("已作廢");
					break;
				}
			});
			// Step3. 取得寄信模塊
			// 寄信件對象
			ArrayList<String> mainUsers = new ArrayList<String>();
			ArrayList<String> secondaryUsers = new ArrayList<String>();
			// 寄信對象條件
			notifications.forEach(r -> {// 沒有設置=全寄信
				// 主要?次要?
				if (r.getSsnprimary() == 0) {
					mainUsers.add(r.getSsnsumail());
				} else {
					secondaryUsers.add(r.getSsnsumail());
				}
			});// 建立信件->寄信對象必須要大於1位&& 且不是空的
			if (mainUsers.size() > 0 && !mainUsers.get(0).equals("")) {
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("Production");
				readyNeedMail.setBnmmail(mainUsers + "");
				readyNeedMail.setBnmmailcc(secondaryUsers + "");// 標題
				readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "]"//
						+ "Cloud system [Schedule Infactory][" + infactorys.get(0).getSinb()
						+ "] Special urgent notification for production orders!");
				// 內容
				String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						// + "<th>項次</th>"//
						+ "<th style='min-width: 65px;'>順序</th>"//
						+ "<th style='min-width: 65px;'>預計開工日</th>"//
						+ "<th style='min-width: 65px;'>預計完工日</th>"//
						+ "<th style='min-width: 100px;'>製令單號</th>"//
						+ "<th style='min-width: 100px;'>產品品號</th>"//
						// + "<th style='min-width: 100px;'>產品品名</th>"//
						+ "<th style='min-width: 40px;'>預計-生產數</th>"//
						// + "<th style='min-width: 40px;'>完成-生產數</th>"/ /
						// + "<th style='min-width: 40px;'>製令單-狀態</th>"//
						+ "<th style='min-width: 80px;'>製令單-負責人</th>"//
						// + "<th style='min-width: 65px;'>生管狀態</th>"//
						// + "<th style='min-width: 220px;'>生管備註</th>"//
						+ "<th style='min-width: 65px;'>物控狀態</th>"//
						+ "<th style='min-width: 65px;'>預計齊料日</th>"//
						+ "<th style='min-width: 220px;'>物控備註</th>"//
						// + "<th style='min-width: 65px;'>倉庫進度</th>"//
						// + "<th style='min-width: 65px;'>倉庫備註</th>"//
						// + "<th style='min-width: 65px;'>製造進度</th>"//
						// + "<th style='min-width: 65px;'>製造備註</th>"//
						// + "<th style='min-width: 65px;'>YYYY(年)-W00(週期)</th>"//
						+ "</tr></thead>"//
						+ "<tbody>";// 模擬12筆資料
				// int r = 1;

				for (ScheduleInfactory oss : infactorys) {
					String siscstatus = "";// 生管狀態
					switch (oss.getSiscstatus()) {
					case 0:
						siscstatus = "未開注意事項";
						break;
					case 1:
						siscstatus = "已開注意事項";
						break;
					case 2:
						siscstatus = "已核准流程卡";
						break;

					}
					String simcstatus = "";// 物控狀態
					switch (oss.getSimcstatus()) {
					case 0:
						simcstatus = "未確認";
						break;
					case 1:
						simcstatus = "未齊料";
						break;
					case 2:
						simcstatus = "已齊料";
						break;
					}
					String sipriority = "";// 物控狀態
					switch (oss.getSipriority()) {
					case 0:
						sipriority = "一般";
						break;
					case -1:
						sipriority = "暫緩";
						break;
					case 1:
						sipriority = "緊急";
						break;
					}
					// 信件資料結構
					bnmcontent += "<tr>"//
							// + "<td>" + (r++) + "</td>"// 項次
							+ "<td>" + sipriority + "</td>"// 預計-緊急?
							+ "<td>" + oss.getSiodate() + "</td>"// 預計-開工日
							+ "<td>" + oss.getSifdate() + "</td>"// 預計-完工日
							+ "<td>" + oss.getSinb() + "</td>"// 製令單號
							+ "<td>" + oss.getSipnb() + "</td>"// 產品品號
							// + "<td>" + oss.getSipname() + "</td>"// 產品品名

							+ "<td>" + oss.getSirqty() + "</td>"// 預計生產數
							// + "<td>" + oss.getSiokqty() + "</td>"// 已生產數

							// + "<td>" + oss.getSistatus() + "</td>"// 製令單-狀態
							+ "<td>" + oss.getSiuname() + "</td>"// 製令單-負責人
							// + "<td>" + siscstatus + "</td>"// 生管狀態
							// + "<td>" + oss.getSiscnote() + "</td>"// 生管備註
							+ "<td>" + simcstatus + "</td>"// 物控狀態
							+ "<td>" + oss.getSimcdate() + "</td>"// 預計-齊料日
							+ "<td>" + oss.getSimcnote() + "</td>"// 物控備註
							// + "<td>" + oss.getSiwmprogress() + "</td>"// 倉庫進度
							// + "<td>" + oss.getSiwmnote() + "</td>"// 倉庫備註
							// + "<td>" + oss.getSimpprogress() + "</td>"// 製造進度
							// + "<td>" + oss.getSimpnote() + "</td>"// 製令單-備註
							// + "<td>" + oss.getSiywdate() + "</td>"// YYYY(西元年)-W00(週期)
							+ "</tr>";
				}
				bnmcontent += "</tbody></table>";
				bnmcontent += "<br><span style='color:red; font-weight:bold;'>※ This is an automated email from the Cloud system. Do not reply。※</span>";
				readyNeedMail.setBnmcontent(bnmcontent);

				// 檢查信件(避免重複)
				if (notificationMailDao.findAllBySearch(null, null, null, readyNeedMail.getBnmtitle(), null, null, null)
						.size() == 0 && infactorys.size() > 0) {
					notificationMailDao.save(readyNeedMail);
				}
			}
		} catch (Exception e) {
			logger.warn(CloudExceptionService.eStktToSg(e));
		}
	}
}
