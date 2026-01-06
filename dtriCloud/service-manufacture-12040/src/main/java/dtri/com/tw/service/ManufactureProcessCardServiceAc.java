package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
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

import dtri.com.tw.pgsql.dao.BomSoftwareHardwareDao;
import dtri.com.tw.pgsql.dao.ManufactureRuleNumberDao;
import dtri.com.tw.pgsql.dao.ManufactureSerialNumberDao;
import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.dao.ScheduleProductionHistoryDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.BomSoftwareHardware;
import dtri.com.tw.pgsql.entity.ManufactureRuleNumber;
import dtri.com.tw.pgsql.entity.ManufactureSerialNumber;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
import dtri.com.tw.pgsql.entity.ScheduleProductionHistory;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.Fm_Char;
import dtri.com.tw.shared.Fm_T;
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
	private ScheduleInfactoryDao infactoryDao;

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
		orders.add(new Order(Direction.DESC, "syscdate"));// 建立單據日
		orders.add(new Order(Direction.ASC, "sphpon"));// 工單號

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			// 1. 取得當前標準的 YYWW (例如 "2601")
			// 使用修正後的工具類，確保取到的是 "週年份"
			String currentYYWW = Fm_T.getYYWW(new Date());
			List<ManufactureRuleNumber> mrn = ruleNumberDao.findAll();

			mrn.forEach(rn -> {
				// 檢查：若規則設定為 "依週期重置" (Mrnyywwc) 且 "週期不一致"
				if (rn.getMrnyywwc() && !rn.getMrnyyww().equals(currentYYWW)) {
					// 更新週期為新的 YYWW
					rn.setMrnyyww(currentYYWW);
					// 檢查：是否需要重置流水號 (Mrn0000c)
					if (rn.getMrn0000c()) {
						// 先預設重置為 0001
						String nextSerial = "0001";

						// 查詢資料庫是否已有該週期 (2601) 的流水號紀錄
						// 邏輯：前綴(Mrnval) + 週期(2601)
						List<ManufactureSerialNumber> serialNumbers = serialNumberDao
								.findAllBySn(rn.getMrnval() + rn.getMrnyyww());

						// 若資料庫已有資料，則抓出最後一筆並 +1
						// 注意：這裡建議確保 serialNumbers 有依照 ID 或 SN 排序，取第一筆或最後一筆才準確
						if (!serialNumbers.isEmpty()) {
							// 假設 get(0) 是最新的一筆
							String msnesn = serialNumbers.get(0).getMsnesn();

							// 防呆：確保長度足夠截取後四碼
							if (msnesn != null && msnesn.length() >= 4) {
								String last4 = msnesn.substring(msnesn.length() - 4);
								try {
									int last4Int = Integer.parseInt(last4);
									nextSerial = String.format("%04d", last4Int + 1);
								} catch (NumberFormatException e) {
									// 若轉換失敗(非數字)，維持 "0001" 或記錄錯誤 log
									System.err.println("流水號解析失敗: " + msnesn);
								}
							}
						}
						// 設定新的流水號
						rn.setMrn0000(nextSerial);
					}
					// 儲存更新
					ruleNumberDao.save(rn);
				}
			});

			// Step3-1.取得資料(一般/細節)
			ArrayList<ScheduleProductionHistory> entitys = productionHistoryDao.findAllBySearch(null, null, null, null,
					2, null, null, null, pageable);

			// Step3-2.資料區分(一般/細節)
			// 取得MES配置的標籤與工作站
			JsonObject setMES = new JsonObject();
			try {
				// 1. 建立 JSON 資料
				JsonObject jsonString = new JsonObject();
				jsonString.addProperty("action", "get_work_program");

				// 2. Cookie 管理（可選）
				BasicCookieStore cookieStore = new BasicCookieStore();
				// 3. 建立支援自簽憑證的 SSL Context
				SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(builder.build(),
						NoopHostnameVerifier.INSTANCE // 忽略主機名驗證
				);
				// 4. 建立 HttpClient
				CloseableHttpClient httpclient = HttpClients.custom()//
						.setSSLSocketFactory(sslConnectionFactory)//
						.setDefaultCookieStore(cookieStore)//
						.build();

				// 5. 建立 POST 請求
				// HttpPost request = new
				// HttpPost("https://127.0.0.1:8088/dtrimes/ajax/api.basil"); //測試用
				HttpPost request = new HttpPost("https://10.1.90.53:8088/dtrimes/ajax/api.basil"); // 正式用
				request.setHeader("Content-Type", "application/json;charset=UTF-8");
				request.setEntity(new StringEntity(jsonString.toString(), StandardCharsets.UTF_8));

				// 6. 發送請求與處理回應
				CloseableHttpResponse response = httpclient.execute(request);
				String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

				System.out.println("Response:");
				System.out.println(responseBody);
				setMES = (JsonObject) JsonParser.parseString(responseBody);
			} catch (Exception e) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
						new String[] { "取得 MES 資料 連線失敗!" });
			}
			// 取得SN規則清單
			JsonObject setMRN = new JsonObject();
			List<Order> orderMRNs = new ArrayList<>();
			orderMRNs.add(new Order(Direction.ASC, "syssort"));// 排序
			orderMRNs.add(new Order(Direction.ASC, "mrngid"));// 規則群組
			orderMRNs.add(new Order(Direction.ASC, "mrnname"));// 規則名稱
			PageRequest pageableMRN = PageRequest.of(batch, total, Sort.by(orderMRNs));
			ArrayList<ManufactureRuleNumber> entityMRNs = ruleNumberDao.findAllBySearch(null, null, pageableMRN);
			String entityJsonMRN = packageService.beanToJson(entityMRNs);
			setMRN.add("ManufactureRuleNumber", (JsonArray) JsonParser.parseString(entityJsonMRN));
			//

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
			selectArr.add("預約生產(已開-生產事項)_2");// 已開-生產注意事項
			selectArr.add("準備生產(已開-流程卡)_3");// 已開-流程卡
			selectArr.add("生產中(開始-工作站)_4");
			selectArr.add("生產結束_5");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "sphprogress", "Ex:單據狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons_sph);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			searchSetJsonAll.add("resultDetailThead_mrn", resultDetailTJsons_mrn);
			searchSetJsonAll.add("resultDetailThead_bsh", resultDetailTJsons_bsh);
			searchSetJsonAll.add("resultMES", setMES);
			searchSetJsonAll.add("resultMRN", setMRN);

			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			ScheduleProductionHistory searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					ScheduleProductionHistory.class);

			// Step4-2.資料區分(一般/細節)
			ArrayList<ScheduleProductionHistory> entitys = productionHistoryDao.findAllBySearch(searchData.getSphpon(),
					searchData.getSphbpmnb(), null, null, searchData.getSphprogress(), null, null, null, pageable);

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
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_sphsdate_sphindate_sphhdate");
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
					// 1. 重複存檔 ?
					if (checkOne.getSphprogress() == 3 && entityData.getSphprogress() == 3) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
								new String[] { entityData.getSphpon() });
					} else if (entityData.getSphprogress() == 2) {
						// 無須檢查
						continue;
					}
					// 2. 檢查 SN是否重複?(無SN除外)
					if (!entityData.getSphpontype().equals("A511_no_sn")
							&& !entityData.getSphpontype().equals("A521_old_sn")) {

						String sphssn = entityData.getSphssn();// 開始SN
						String sphesn = entityData.getSphesn();// 結束SN
						ArrayList<ManufactureSerialNumber> numbers = serialNumberDao.findAllByCheck(sphssn, sphesn,
								null, null);
						Boolean checknb = false;
						String checkVal = "";
						for (ManufactureSerialNumber n : numbers) {
							// 排除相同工單
							if (!n.getMsnwo().equals(entityData.getSphpon())) {
								checknb = true;
								checkVal = n.getMsnwo() + " :S_SN: " + n.getMsnssn() + " :E_SN: " + n.getMsnesn()
										+ " & ";
							}
						}
						if (checknb) {
							throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
									new String[] { checkVal });

						}
					}
				} else {
					// 沒有此工單?
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<ScheduleProductionHistory> saveDatas = new ArrayList<>();
		ArrayList<BomSoftwareHardware> softwareHardwares = new ArrayList<>();
		ArrayList<ManufactureSerialNumber> serialNumbers = new ArrayList<>();
		ArrayList<ManufactureRuleNumber> ruleNumbers = new ArrayList<>();

		for (ScheduleProductionHistory x : entityDatas) {

			// 排除 沒有ID
			if (x.getSphid() != null) {
				ScheduleProductionHistory entityDataOld = productionHistoryDao.getReferenceById(x.getSphid());
				ManufactureRuleNumber number = new ManufactureRuleNumber();
				BomSoftwareHardware hardware = new BomSoftwareHardware();
				ManufactureSerialNumber serialNumber = new ManufactureSerialNumber();
				// 只解鎖則不更新
				System.out.println(x.getSphprogress());
				if (x.getSphprogress() == 2) {
					entityDataOld.setSphprogress(x.getSphprogress());
				} else {
					entityDataOld.setSysmdate(new Date());
					entityDataOld.setSysmuser(packageBean.getUserAccount());

					if (!x.getSphpontype().equals("A511_no_sn") && !x.getSphpontype().equals("A521_old_sn")) {
						entityDataOld.setSphssn(x.getSphssn());// 開始SN
						entityDataOld.setSphesn(x.getSphesn());// 結束SN
					} else {
						// 無SN
						entityDataOld.setSphssn("");// 開始SN
						entityDataOld.setSphesn("");// 結束SN
					}

					entityDataOld.setSphprnote1(x.getSphprnote1());// 製造1
					entityDataOld.setSphprnote2(x.getSphprnote2());// 製造2
					entityDataOld.setSphbsh(x.getSphbsh());// 產品軟硬體
					entityDataOld.setSphline(x.getSphline());// 生產線
					entityDataOld.setSphworkstation(x.getSphworkstation());// 工作站
					entityDataOld.setSphlable(x.getSphlable());// 標籤
					entityDataOld.setSphpontype(x.getSphpontype());// 製令單類型
					entityDataOld.setSphwarranty(x.getSphwarranty());// 保固年
					entityDataOld.setSphindate(x.getSphindate());// 實際投線日
					entityDataOld.setSphpartno(x.getSphpartno());// 客戶組件(PartNo)
					entityDataOld.setSphmfgpartno(x.getSphmfgpartno());// 工廠認證(MFGP No)
					entityDataOld.setSphprogress(x.getSphprogress());// 進度 完成ERP工單(準備物料)=1 完成注意事項(預約生產)=2完成->流程卡(準備生產)=3
																		// (生產中)=4 (生產結束)=5
					entityDataOld.setSphrsn(x.getSphrsn());// 序號規則
					entityDataOld.setSphpmnote(x.getSphpmnote());// 製造 PM添加備註
					// 標記-修改廠內排程
					ArrayList<ScheduleInfactory> infactories = infactoryDao.findAllByCheck(entityDataOld.getSphpon(),
							null, null);
					if (infactories.size() == 1) {
						ScheduleInfactory infactory = infactories.get(0);
						infactory.setSiscstatus(2);// 已經單據->為2已經開流程卡
						infactoryDao.save(infactory);
					}

					// 更新或新增SN清單
					ArrayList<ManufactureSerialNumber> numbers = serialNumberDao.findAllBySearch(null, x.getSphpon(),
							null, null);
					if (numbers.size() > 0) {
						serialNumber = numbers.get(0);
					}
					serialNumber.setMsnbom(entityDataOld.getSphbpmnb());// BOM
					serialNumber.setMsnclinet(entityDataOld.getSphoname());// 客戶
					serialNumber.setMsnssn(entityDataOld.getSphssn());// 開始
					serialNumber.setMsnesn(entityDataOld.getSphesn());// 結束
					serialNumber.setMsnmodel(entityDataOld.getSphbpmmodel());// 型號
					serialNumber.setMsnwo(entityDataOld.getSphpon());// 工單號

					// 更新軟硬體版本
					ArrayList<BomSoftwareHardware> hardwares = bomSoftwareHardwareDao
							.findAllBySearch(entityDataOld.getSphoname(), entityDataOld.getSphbpmnb(), null, null);
					JsonObject sphbshJSON = JsonParser.parseString(x.getSphbsh()).getAsJsonObject().get("pr_s_item")
							.getAsJsonObject();

					if (hardwares.size() > 0) {
						hardware = hardwares.get(0);
					}
					hardware.setBshcname(entityDataOld.getSphoname() + "(" + entityDataOld.getSphocountry() + ")");// 客戶+國家
					hardware.setBshnb(entityDataOld.getSphbpmnb());// BOM號
					hardware.setBshhmbecn(sphbshJSON.get("MB_ECN").getAsString());
					hardware.setBshhnvram(sphbshJSON.get("NV_RAM").getAsString());
					hardware.setBshmodel(sphbshJSON.get("Model").getAsString());
					hardware.setBshsbios(sphbshJSON.get("BIOS").getAsString());
					hardware.setBshhmb(sphbshJSON.get("MB_Ver").getAsString());
					hardware.setBshsec(sphbshJSON.get("EC").getAsString());
					hardware.setBshsos(sphbshJSON.get("OS").getAsString());
					hardware.setSysnote1(sphbshJSON.get("Note1").getAsString());
					hardware.setSysnote2(sphbshJSON.get("Note2").getAsString());

					// 如果吳SN掠過 更新SN序號規則
					if (!x.getSphpontype().equals("A511_no_sn") && !x.getSphpontype().equals("A521_old_sn")) {

						if (!x.getSphrsn().equals("") && !x.getSphrsn().split("_")[0].equals("")) {
							number = ruleNumberDao.getReferenceById(Long.parseLong(x.getSphrsn().split("_")[0]));
							Integer mrn0000 = Integer.parseInt(number.getMrn0000());
							int increment = entityDataOld.getSphoqty();
							// 模擬環狀累加，超過 9999 時從 1 開始
							mrn0000 = (mrn0000 + increment) % 10000;
							// 防止結果為 0（0000 不合法），強制補為 1
							mrn0000 = (mrn0000 == 0) ? 1 : mrn0000;
							String mrnStr = String.format("%04d", mrn0000);
							number.setMrn0000(mrnStr);
						}
					}
					// 暫存
					serialNumbers.add(serialNumber);
					softwareHardwares.add(hardware);
					saveDatas.add(entityDataOld);
					ruleNumbers.add(number);
					// 傳送MES系統->建立資料
					try {
						// 1. 建立 JSON 資料
						JsonObject jsonString = new JsonObject();
						jsonString.addProperty("action", "production_create");
						JsonArray jsonCreates = new JsonArray();
						JsonObject jsonCreate = new JsonObject();// 需創建資料
						// 序號清單
						String prefix = entityDataOld.getSphrsn().split("_")[1]; // 前固定序號
						String startStr = entityDataOld.getSphrsn().split("_")[2]; // 起始序號，如 "001"
						int quantity = entityDataOld.getSphoqty(); // 需產生幾筆序號
						// 取得補零長度
						int serialLength = startStr.length();
						int startNum = Integer.parseInt(startStr);
						JsonArray snList = new JsonArray(); // 最終儲存的 JsonArray
						for (int i = 0; i < quantity; i++) {
							int currentNum = startNum + i;
							String paddedSerial = String.format("%0" + serialLength + "d", currentNum);
							String fullSN = prefix + paddedSerial;
							snList.add(fullSN); // 加入陣列
						}
						// 放入
						jsonCreate.add("sn_list", snList);

						// 規格內容
						JsonObject pr_b_item = new JsonObject();
						JsonObject pr_material = new JsonObject();
						JsonArray sphbisitem = JsonParser.parseString(entityDataOld.getSphbisitem()).getAsJsonObject()
								.get("items").getAsJsonArray();
						for (JsonElement element : sphbisitem) {
							JsonObject pr_b_one = new JsonObject();
							JsonObject pr_m_one = new JsonObject();
							// 項目組名
							String bisgname = element.getAsJsonObject().getAsJsonPrimitive("bisgname").getAsString();
							// 項目內容
							String bisfnameStr = element.getAsJsonObject().get("bisfname").getAsString();
							JsonArray bisfnameArray = JsonParser.parseString(bisfnameStr).getAsJsonArray();
							StringBuilder joined = new StringBuilder();
							for (int i = 0; i < bisfnameArray.size(); i++) {
								joined.append(bisfnameArray.get(i).getAsString());
								if (i < bisfnameArray.size() - 1) {
									joined.append(", ");
								}
							}
							String bisfnameJoined = joined.toString().trim(); // 去除最後空格
							// 數量
							String bisqty = element.getAsJsonObject().getAsJsonPrimitive("bisqty").getAsString();
							pr_b_one.addProperty("Is", bisfnameJoined.trim());
							pr_b_one.addProperty("Qty", bisqty);
							pr_b_item.add(bisgname, pr_b_one);
							// Key Part 物料號
							String bisnb = element.getAsJsonObject().get("bisnb").getAsString();
							String bisname = element.getAsJsonObject().get("bisname").getAsString();
							if (!bisnb.contains("customize")) {
								pr_m_one.addProperty("Is", bisnb);
								pr_m_one.addProperty("Name", bisname);
								pr_material.add(bisgname, pr_m_one);
							}
						}
						jsonCreate.add("pr_b_item", pr_b_item);
						// 產品Key Part物料號
						jsonCreate.add("pr_material", pr_material);

						// 軟硬體
						String sphbsh = entityDataOld.getSphbsh();

						JsonObject sphbshJson = JsonParser.parseString(sphbsh).getAsJsonObject().get("pr_s_item")
								.getAsJsonObject();
						JsonObject sphbshJsonSend = new JsonObject();
						for (Map.Entry<String, JsonElement> entry : sphbshJson.entrySet()) {
							JsonObject sphbshJsonOne = new JsonObject();
							String key = entry.getKey().trim();
							String val = entry.getValue().getAsString().trim();
							sphbshJsonOne.addProperty("Is", val);
							sphbshJsonSend.add(key, sphbshJsonOne);
						}
						jsonCreate.add("pr_s_item", sphbshJsonSend);
						//
						jsonCreate.addProperty("ph_type", entityDataOld.getSphpontype());// "A511_has_sn",
						jsonCreate.addProperty("ph_p_name", entityDataOld.getSphbpmnb());// 產品品號 "90-139-M110R00",
						jsonCreate.addProperty("ph_pb_g_id", "");// 固定
						jsonCreate.addProperty("ph_order_id", entityDataOld.getSphonb());// "US110840981", 訂單號
						jsonCreate.addProperty("ph_e_s_date", Fm_T.to_y_M_d(entityDataOld.getSphhdate()));// "2025-07-25"預計出貨日
						jsonCreate.addProperty("sys_m_user", "");// 固定
						jsonCreate.addProperty("ph_schedule", "");// 固定
						jsonCreate.addProperty("ph_wp_id", entityDataOld.getSphworkstation());// 工作站ID
						jsonCreate.addProperty("sys_c_user", "");// 固定
						jsonCreate.addProperty("ph_c_from", "DTR Cloud");// 固定 來源
						jsonCreate.addProperty("pr_name", entityDataOld.getSphname());// 產品名稱

						jsonCreate.addProperty("ph_id", "");// 固定
						jsonCreate.addProperty("sys_ver", "0");// 固定
						jsonCreate.addProperty("ph_ll_g_name", entityDataOld.getSphlable());// 標籤組
						jsonCreate.addProperty("ph_mfg_p_no", entityDataOld.getSphmfgpartno());// 工廠認證(MFGP No)
						jsonCreate.addProperty("ph_ps_no", entityDataOld.getSphpartno());//// 客戶組件(PartNo)
						jsonCreate.addProperty("ph_p_qty", entityDataOld.getSphoqty());// 需求數量
						jsonCreate.addProperty("ph_p_ok_qty", "0");// 固定 已完成數量
						//
						jsonCreate.addProperty("sys_status", "0");// 狀態
						jsonCreate.addProperty("ph_wc_line", entityDataOld.getSphline());// 4F
						//
						jsonCreate.addProperty("ps_b_f_sn", entityDataOld.getSphrsn().split("_")[1]);// 前固定序號
						jsonCreate.addProperty("ps_b_sn", entityDataOld.getSphrsn().split("_")[2]);// 序號浮動 "001",
						jsonCreate.addProperty("ph_s_date", "");// 固定 開始時間
						jsonCreate.addProperty("sys_m_date", "");// 固定
						jsonCreate.addProperty("pr_bom_id", entityDataOld.getSphbpmnb());// BOM號
						jsonCreate.addProperty("pr_bom_c_id", entityDataOld.getSphobpmnb());// 訂單BOM號
						jsonCreate.addProperty("pr_p_model", entityDataOld.getSphbpmmodel());// 型號
						jsonCreate.addProperty("ps_sn_6", "000");// 固定
						jsonCreate.addProperty("ps_sn_5", "0");// 固定
						jsonCreate.addProperty("ps_sn_4", "0000");// 固定
						jsonCreate.addProperty("ps_sn_3", "0");// 固定
						jsonCreate.addProperty("ps_sn_2", "0");// 固定
						jsonCreate.addProperty("ps_sn_1", "000");// 固定
						//
						jsonCreate.addProperty("ph_c_name",
								entityDataOld.getSphoname() + "(" + entityDataOld.getSphocountry() + ")");// 客戶名稱+國家
						jsonCreate.addProperty("ph_pr_id", entityDataOld.getSphpon());// 工單號
						jsonCreate.addProperty("pr_s_sn", entityDataOld.getSphssn());// 開始SN
						jsonCreate.addProperty("pr_e_sn", entityDataOld.getSphesn());// 結束SN
						//
						jsonCreate.addProperty("sys_sort", "0");// 固定
						jsonCreate.addProperty("pr_specification",
								Fm_Char.sanitizeText(entityDataOld.getSphspecification()));// 產品規格
						jsonCreate.addProperty("pr_p_v", "");// 固定
						jsonCreate.addProperty("sys_c_date", "");// 固定
						jsonCreate.addProperty("ph_w_years", entityDataOld.getSphwarranty());// 保固
						jsonCreate.addProperty("sys_sn_auto", false);
						jsonCreate.addProperty("sys_note", entityDataOld.getSphpmnote());

						jsonCreates.add(jsonCreate);
						jsonString.add("create", jsonCreates);
						System.out.println(jsonString.toString());
						// 2. Cookie 管理（可選）
						BasicCookieStore cookieStore = new BasicCookieStore();
						// 3. 建立支援自簽憑證的 SSL Context
						SSLContextBuilder builder = new SSLContextBuilder();
						builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
						SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(
								builder.build(), NoopHostnameVerifier.INSTANCE // 忽略主機名驗證
						);
						// 4. 建立 HttpClient
						CloseableHttpClient httpclient = HttpClients.custom()//
								.setSSLSocketFactory(sslConnectionFactory)//
								.setDefaultCookieStore(cookieStore)//
								.build();

						// 5. 建立 POST 請求
						// HttpPost request = new
						// HttpPost("https://127.0.0.1:8088/dtrimes/ajax/api.basil");
						HttpPost request = new HttpPost("https://10.1.90.53:8088/dtrimes/ajax/api.basil");
						request.setHeader("Content-Type", "application/json;charset=UTF-8");
						request.setEntity(new StringEntity(jsonString.toString(), StandardCharsets.UTF_8));

						// 6. 發送請求與處理回應
						CloseableHttpResponse response = httpclient.execute(request);
						String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

						System.out.println("Response:" + responseBody);
					} catch (Exception e) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
								new String[] { "取得 MES 資料 連線失敗!" });
					}

					packageBean.setCallBackValue("Save");

				}
			}
		}

		// =======================資料儲存=======================
		// 資料Data

		serialNumberDao.saveAll(serialNumbers);
		bomSoftwareHardwareDao.saveAll(softwareHardwares);
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
//		// =======================資料準備 =======================
//		ArrayList<ScheduleProductionHistory> entityDatas = new ArrayList<>();
//		// =======================資料檢查=======================
//		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
//			// Step1.資料轉譯(一般)
//			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
//					new TypeReference<ArrayList<ScheduleProductionHistory>>() {
//					});
//			// Step2.資料檢查
//		}
//		// =======================資料整理=======================
//		// Step3.一般資料->寫入
//		ArrayList<ScheduleProductionHistory> saveDatas = new ArrayList<>();
//		entityDatas.forEach(x -> {
//			// 排除 沒有ID
//			if (x.getSphid() != null) {
//				ScheduleProductionHistory entityDataOld = productionHistoryDao.findById(x.getSphid()).get();
//				int stop = productionHistoryDao
//						.findAllBySearch(entityDataOld.getSphpon(), null, null, null, null, null, null, null, null)
//						.size();
//				entityDataOld.setSysmdate(new Date());
//				entityDataOld.setSysmuser(packageBean.getUserAccount());
//				entityDataOld.setSysstatus(2);
//				entityDataOld.setSphpon(entityDataOld.getSphpon() + "Stop" + stop);
//				saveDatas.add(entityDataOld);
//			}
//		});
//		// =======================資料儲存=======================
//		// 資料Data
//		productionHistoryDao.saveAll(saveDatas);
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
