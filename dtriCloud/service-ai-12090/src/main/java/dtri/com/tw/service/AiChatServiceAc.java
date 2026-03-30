package dtri.com.tw.service;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.AiChatMessagesDao;
import dtri.com.tw.pgsql.dao.AiChatSessionsDao;
import dtri.com.tw.pgsql.dao.AiVoiceMetadataDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.AiChatMessages;
import dtri.com.tw.pgsql.entity.AiChatMessages.MessageRole;
import dtri.com.tw.pgsql.entity.AiChatSessions;
import dtri.com.tw.pgsql.entity.AiVoiceMetadata;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.persistence.EntityManager;

@Service
public class AiChatServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private AiChatMessagesDao aiChatMessagesDao;
	@Autowired
	private AiChatSessionsDao aiChatSessionsDao;
	@Autowired
	private AiVoiceMetadataDao aiVoiceMetadataDao;

	@Autowired
	private QdrantSyncService qdrantSyncService;

	@Autowired
	private ScheduleInfactoryForAiChatService aboutScheduleInfactory;

	@Autowired
	private BomProductManagementForAiChatService aboutBomProduct;

	@Autowired
	ScheduleInfactoryForQdrantService scheduleInfactoryForQdrantService;

	// 注入你剛才寫好的 DtrAiService (5090 伺服器呼叫端)
	@Autowired
	private AiDtrService dtrAiService;
	@Autowired
	private EntityManager em;

	private static final Logger log = LoggerFactory.getLogger(AiChatServiceAc.class);

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			// 測試用-建立向量資料庫(Step1.)因目前尚未有此模糊查詢需求
//			try {
//				//封裝 
//				List<PointStruct> payloads = scheduleInfactoryForQdrantService.packagePayloads();
//				// 💡 呼叫你寫好的同步方法
//				qdrantSyncService.syncAllToQdrant(payloads);
//				System.out.println("✅ 同步指令已送出，請查看 Console 日誌與 Qdrant Dashboard！");
//			} catch (Exception e) {
//				System.out.println("❌ 同步失敗：" + e.getMessage());
//			}
			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLAiChatMessages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLAiChatSessions = new HashMap<>();
			Map<String, SystemLanguageCell> mapLAiVoiceMetadata = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> lAiChatMessages = languageDao.findAllByLanguageCellSame("AiChatMessages",
					null, 2);
			lAiChatMessages.forEach(x -> {
				mapLAiChatMessages.put(x.getSltarget(), x);
			});
			ArrayList<SystemLanguageCell> lAiVoiceMetadata = languageDao.findAllByLanguageCellSame("AiVoiceMetadata",
					null, 2);
			lAiVoiceMetadata.forEach(x -> {
				mapLAiVoiceMetadata.put(x.getSltarget(), x);
			});
			ArrayList<SystemLanguageCell> lAiChatSessions = languageDao.findAllByLanguageCellSame("AiChatSessions",
					null, 2);
			lAiChatSessions.forEach(x -> {
				mapLAiChatSessions.put(x.getSltarget(), x);
			});
			//
			Field[] fieldsAiChatMessages = AiChatMessages.class.getDeclaredFields();
			Field[] fieldsAiVoiceMetadata = AiVoiceMetadata.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");
			JsonObject searchSetJsonAll = new JsonObject();// 整備好資料
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fieldsAiChatMessages, exceptionCell, mapLAiChatMessages);
			// 欄位翻譯(細節)
			resultDetailTJsons = packageService.resultSet(fieldsAiVoiceMetadata, exceptionCell, mapLAiVoiceMetadata);

			// Step3-5. 建立查詢項目->暫時不需要 查詢欄位
			/*
			 * searchJsons = packageService.searchSet(searchJsons, null, "bisgname",
			 * "Ex:項目組名稱?", true, // PackageService.SearchType.text,
			 * PackageService.SearchWidth.col_lg_2);
			 */
			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());

		} else {

			// Step4-1. 取得資料(一般/細節)
			// 取得對話紀錄 By User
			AiChatSessions searchHistory = new AiChatSessions();
			searchHistory.setAcsuaccount(packageBean.getUserAccount());

			// 2. 取得對話紀錄清單 (依據使用者帳號或特定條件)
			// 假設你的 dao 有 findAllBySession 方法，且需按時間倒序排列
			ArrayList<AiChatSessions> lAiChatSessions = aiChatSessionsDao.findAllByAccount(packageBean.getUserAccount(),
					null);

			// 3. 將 List 轉回 Json 字串放入 PackageBean
			String entityJson = packageService.beanToJson(lAiChatSessions);
			packageBean.setEntityJson(entityJson);
		}
		//
		AiChatSessions formatAiChatSessions = new AiChatSessions();
		AiChatMessages formatAiChatMessages = new AiChatMessages();
		AiVoiceMetadata formatAiVoiceMetadata = new AiVoiceMetadata();
		formatAiChatMessages.setVoiceMetadata(formatAiVoiceMetadata);
		List<AiChatMessages> formatAiChatMessagess = new ArrayList<AiChatMessages>();
		formatAiChatMessagess.add(formatAiChatMessages);
		formatAiChatSessions.setAichatmessages(formatAiChatMessagess);
		//
		String entityFormatJson = packageService.beanToJson(formatAiChatSessions);
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("acsid_acsid");
		// 時間格式
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		return packageBean;
	}

	/** 新增資料 (包含語音辨識與 AI 分析) */
	/** 新增資料 (核心：串接 5090 耳朵、眼睛、大腦) */
	@Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// ======================= 1. 資料解析 =======================
		AiChatSessions inputSession = packageService.jsonToBean(packageBean.getEntityJson(), AiChatSessions.class);

		if (inputSession.getAichatmessages() == null || inputSession.getAichatmessages().isEmpty()) {
			throw new CloudExceptionService(packageBean, ErColor.danger, ErCode.W1000, Lan.zh_TW,
					new String[] { "內容不能為空" });
		}

		AiChatMessages userMsgInput = inputSession.getAichatmessages().get(0);
		String finalContent = userMsgInput.getAcmcontent();

		// ======== 2. 耳朵處理 (STT - 呼叫 5090 Whisper)========
		if ("VOICE".equals(userMsgInput.getAcmmtype())) {
			try {
				log.info("[AI整合] 收到語音訊息，準備呼叫 5090 Whisper...");
				// A. 將 Base64 轉為臨時實體檔案，因為 DtrAiService 需要路徑
				byte[] audioBytes = Base64.getDecoder().decode(userMsgInput.getAcmaurl());
				File tempFile = File.createTempFile("dtr_voice_", ".mp3");
				try (FileOutputStream fos = new FileOutputStream(tempFile)) {
					fos.write(audioBytes);
				}

				// B. 呼叫 5090 耳朵
				finalContent = dtrAiService.processAudio(tempFile.getAbsolutePath());
				tempFile.delete(); // 用完即刪

				if (finalContent == null || finalContent.isBlank())
					finalContent = "[語音辨識內容為空]";
			} catch (Exception e) {
				log.error("Whisper 辨識失敗: {}", e.getMessage());
				finalContent = "[語音服務暫時不可用]";
			}
		}

		// ======== 3. 會話紀錄管理 (存入資料庫) ========
		AiChatSessions currentSession;
		if (inputSession.getAcsid() == null || inputSession.getAcsid() == 0L) {
			currentSession = new AiChatSessions();
			currentSession.setAcsuaccount(packageBean.getUserAccount());
			currentSession.setAcsbtype(inputSession.getAcsbtype());
			currentSession
					.setAcstitle(finalContent.length() > 15 ? finalContent.substring(0, 15) + "..." : finalContent);
			currentSession.setSyscuser(packageBean.getUserAccount());
			currentSession = aiChatSessionsDao.save(currentSession);
		} else {
			currentSession = aiChatSessionsDao.findById(inputSession.getAcsid()).get();
		}

		// 儲存 USER 訊息
		AiChatMessages userMsg = new AiChatMessages();
		userMsg.setAcmsessions(currentSession);
		userMsg.setRole(MessageRole.USER);
		userMsg.setAcmcontent(finalContent);
		userMsg.setAcmmtype(userMsgInput.getAcmmtype());
		userMsg.setSyscuser(packageBean.getUserAccount());
		aiChatMessagesDao.save(userMsg);

		// 如果是語音，額外存元數據
		if ("VOICE".equals(userMsgInput.getAcmmtype())) {
			AiVoiceMetadata voice = new AiVoiceMetadata();
			voice.setAichatmessages(userMsg);
			voice.setAvmttext(finalContent);
			voice.setAvmvmodel("Whisper-v3-5090");
			aiVoiceMetadataDao.save(voice);
		}

		// ======== 4. 大腦分析 (DeepSeek - 結合排程數據) ========

		// 4.1 指定對話類型?生管?物控?製造?倉庫?產品
		/**
		 * 會話業務類型 (Session Business Type) 作用：區分詢問的專業領域，例如：PMC(生管), MC(物控), PUR(採購)。
		 * 實務：這會決定後端串接 AI 時，要餵給 AI 什麼樣的「系統指令(System Prompt)」。<br>
		 * <br>
		 * 生管AI管理人 APMC 生管 (Production Management & Control) 專精於生產排程、工單優先級與產能利用率分析。<br>
		 * 物控AI管理人 AMMC 物控 (Material Control) 專精於庫存水位、物料需求計畫 (MRP) 與缺料預警。<br>
		 * 採購AI管理人 APUR 採購 (Purchasing) 專精於供應商交期、採購單狀態與採購成本分析。<br>
		 * 倉儲AI管理人 AWMS 倉儲 (Warehouse Management) 專精於入庫/出庫效率、儲位優化與盤點準確性。<br>
		 * // PMC, MC, PUR, QC...
		 * 
		 */
		log.info("[AI整合] 當前業務類型: {}，正在準備對應數據...", currentSession.getAcsbtype());

		String brainPrompt = "";
		String bType = currentSession.getAcsbtype() == null ? "GENERAL" : currentSession.getAcsbtype();
		Boolean checkOK = true;
		String aiCleanAnswer = "";
		String intentPrompt = "";
		String jsonIntentRaw = "";
		String cleanJson = "";
		// 測試用-向量 資料庫->查詢->因目前尚未有此模糊查詢需求
		// qdrantSyncService.searchInQdrant("90-302");

		// 使用 Switch 根據業務類型 (PMC, MC, PUR...) 切換數據抓取邏輯
		switch (bType) {
		case "APMC": // 生管 (Production Management & Control)
			log.info("[AI整合] 執行 PMC 生管分析邏輯...");

			// --- Step 1: 讓 AI 分析條件查詢 (Intent Extraction) ---
			intentPrompt = aboutScheduleInfactory.extractSearchIntent(finalContent);
			jsonIntentRaw = dtrAiService.processText(intentPrompt);

			// 💡 關鍵：清洗 Markdown 標籤，只抓 JSON 核心
			// 簡單提取 JSON 內容 (如果回傳是原始 JSON)
			cleanJson = dtrAiService.cleanJson(extractContent(jsonIntentRaw));

			// --- Step 2: 如果分析失敗 (格式不對或抓不到括號) ---
			if (!cleanJson.startsWith("{") || !cleanJson.endsWith("}")) {
				aiCleanAnswer = "抱歉，我無法精準解析您的查詢條件，請提供更具體的單號、品名或狀態（例如：請幫我查 A521 且已開工的單子）";
				checkOK = false;
			} else {
				brainPrompt = aboutScheduleInfactory.getScheduleInfactoryForAi(cleanJson, currentSession, packageBean,
						finalContent);
			}
			break;

		case "AMMC": // 物控 (Material Control)
			log.info("[AI整合] 執行 MC 物控分析邏輯 (預留區)...");
			// 未來這裡呼叫 aboutMaterialControl.getMaterialForAi(...)
			brainPrompt = "目前為物控測試模式。使用者問題: " + finalContent;
			break;

		case "APUR": // 採購 (Purchasing)
			log.info("[AI整合] 執行 PUR 採購分析邏輯 (預留區)...");
			brainPrompt = "目前為採購測試模式。使用者問題: " + finalContent;
			break;

		case "AWMS": // 倉儲 (Warehouse Management)
			log.info("[AI整合] 執行 WMS 倉儲分析邏輯 (預留區)...");
			brainPrompt = "目前為倉儲測試模式。使用者問題: " + finalContent;
			break;

		case "APDM": // 產品管理 (Product Management / BOM 規格)
			log.info("[AI整合] 執行 PM 產品規格分析...");

			// 1. 讓 8002 埠位解析使用者的搜尋意圖
			intentPrompt = aboutBomProduct.extractProductIntent(finalContent);
			jsonIntentRaw = dtrAiService.processText(intentPrompt);
			cleanJson = dtrAiService.cleanJson(extractContent(jsonIntentRaw));

			// --- Step 2: 如果分析失敗 (格式不對或抓不到括號) ---
			if (!cleanJson.startsWith("{") || !cleanJson.endsWith("}")) {
				aiCleanAnswer = "抱歉，我無法精準解析您的查詢條件。請嘗試提供更具體的品號或型號（例如：『請幫我查 90-320 的規格』或『DT135WN 配什麼 CPU？』）";
				checkOK = false;
			} else {
				// 3. 呼叫 Service 抓取資料庫數據並轉為 Markdown 表格
				String bomTableContext = aboutBomProduct.getBomProductForAi(cleanJson, finalContent);

				// 💡 4. 進階優化：讓 AI 結合表格回答問題 (RAG 最終步驟)
				// 如果查不到資料，bomTableContext 裡面已經有提示文字了
				if (bomTableContext.contains("⚠️") || bomTableContext.contains("查無")) {
					brainPrompt = bomTableContext;
				} else {
					// 有資料
					brainPrompt = bomTableContext;

				}
			}

			break;

		default: // 通用模式
			log.warn("[AI整合] 未知業務類型，使用通用 Prompt 處理");
			brainPrompt = "你現在是 DTR 綜合助理。請回答使用者問題: " + finalContent;
			break;
		}

		// 解析成功?
		if (checkOK) {
			log.info("[AI整合] 回傳...");
			aiCleanAnswer = brainPrompt;
		}

		// ======================= 5. 儲存 AI 回覆並返回 =======================
		AiChatMessages aiResMsg = new AiChatMessages();
		aiResMsg.setAcmsessions(currentSession);
		aiResMsg.setRole(MessageRole.ASSISTANT);
		aiResMsg.setAcmcontent(aiCleanAnswer);
		aiResMsg.setAcmmtype("TEXT");
		aiResMsg.setSyscuser("DEEPSEEK_5090");
		aiChatMessagesDao.save(aiResMsg);

		// 回傳當前會話完整歷史
		List<AiChatMessages> history = aiChatMessagesDao.findAllBySessionId(currentSession.getAcsid());
		currentSession.setAichatmessages(history);
		ArrayList<AiChatSessions> resList = new ArrayList<>();
		resList.add(currentSession);

		packageBean.setEntityJson(packageService.beanToJson(resList));
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {

		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<AiChatSessions> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<AiChatSessions>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<AiChatSessions> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getAcsid() != null) {
				AiChatSessions entityDataOld = aiChatSessionsDao.getReferenceById(x.getAcsid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		aiChatSessionsDao.deleteAll(saveDatas);

		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		return packageBean;
	}

	/** 輔助：提取回傳 JSON 中的文字內容 */
	private String extractContent(String jsonResponse) {
		try {
			JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
			if (json.has("choices")) {
				return json.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject()
						.get("content").getAsString();
			}
			return jsonResponse;
		} catch (Exception e) {
			return jsonResponse;
		}
	}

	/**
	 * * 發送新訊息 (核心動作) 包含：建立/取得會話 -> 儲存使用者訊息(文字或語音) -> 模擬 AI 回覆
	 */
	@Transactional
	public PackageBean sendMessage(PackageBean packageBean) throws Exception {
		// 1. 解析前端傳入的資料 (通常包含 sessionId, content, role, type 等)
		JsonObject entityJson = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonObject();

		Long sessionId = entityJson.has("acsid") ? entityJson.get("acsid").getAsLong() : null;
		String userContent = entityJson.get("acmcontent").getAsString();
		String messageType = entityJson.get("acmmtype").getAsString(); // TEXT or VOICE

		// 2. 獲取或建立會話 (Session)
		AiChatSessions session;
		if (sessionId == null || sessionId == 0L) {
			session = new AiChatSessions();
			session.setAcsuaccount(packageBean.getUserAccount()); // 關聯當前登入者
			session.setAcstitle(userContent.length() > 20 ? userContent.substring(0, 20) + "..." : userContent);
			session.setSyscuser(packageBean.getUserAccount());
			session = aiChatSessionsDao.save(session);
		} else {
			session = aiChatSessionsDao.findById(sessionId).orElseThrow(() -> new Exception("Session not found"));
		}

		// 3. 儲存使用者訊息 (USER Message)
		AiChatMessages userMsg = new AiChatMessages();
		userMsg.setAcmsessions(session);
		userMsg.setRole(MessageRole.USER);
		userMsg.setAcmcontent(userContent);
		userMsg.setAcmmtype(messageType);
		userMsg.setSyscuser(packageBean.getUserAccount());
		aiChatMessagesDao.save(userMsg);

		// 4. 如果是語音訊息，儲存語音元數據 (Voice Metadata)
		if ("VOICE".equals(messageType)) {
			AiVoiceMetadata voice = new AiVoiceMetadata();
			voice.setAichatmessages(userMsg);
			voice.setAvmttext(userContent); // 辨識後的文字
			voice.setAvmduration(entityJson.get("duration").getAsDouble());
			voice.setAvmsrate(entityJson.get("srate").getAsInt());
			voice.setAvmvmodel("Whisper-v3");
			voice.setSyscuser(packageBean.getUserAccount());
			aiVoiceMetadataDao.save(voice);
		}

		// 5. 模擬 AI 回覆邏輯 (未來此處對接 Google AI)
		// 這裡我們模擬回傳一個文字描述 + 一個預計生成的 Excel 連結
		AiChatMessages aiMsg = new AiChatMessages();
		aiMsg.setAcmsessions(session);
		aiMsg.setRole(MessageRole.ASSISTANT);
		aiMsg.setAcmcontent("已收到您的生管指令，正在分析排程... 請稍候下載報表。");
		aiMsg.setAcmmtype("EXCEL");
		aiMsg.setAcmaurl("/files/reports/schedule_20260210.xlsx"); // 模擬路徑
		aiMsg.setSyscuser("AI_ENGINE");
		aiChatMessagesDao.save(aiMsg);

		// 6. 包裝回傳資料
		ArrayList<AiChatMessages> history = aiChatMessagesDao.findAllBySessionId(session.getAcsid());
		packageBean.setEntityJson(packageService.beanToJson(history));

		return packageBean;
	}

}
