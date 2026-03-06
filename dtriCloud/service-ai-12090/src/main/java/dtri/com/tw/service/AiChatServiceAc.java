package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.AiChatMessagesDao;
import dtri.com.tw.pgsql.dao.AiChatSessionsDao;
import dtri.com.tw.pgsql.dao.AiVoiceMetadataDao;
import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.AiChatMessages;
import dtri.com.tw.pgsql.entity.AiChatMessages.MessageRole;
import dtri.com.tw.pgsql.entity.AiChatSessions;
import dtri.com.tw.pgsql.entity.AiVoiceMetadata;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
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
	private GeminiAiService geminiAiService;

	@Autowired
	private ScheduleInfactoryDao infactoryDao;

	@Autowired
	private EntityManager em;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
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
	@Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// ======================= 1. 資料準備 =======================
		AiChatSessions inputSession = packageService.jsonToBean(packageBean.getEntityJson(), AiChatSessions.class);

		if (inputSession.getAichatmessages() == null || inputSession.getAichatmessages().isEmpty()) {
			throw new CloudExceptionService(packageBean, ErColor.danger, ErCode.W1000, Lan.zh_TW,
					new String[] { "無有效訊息內容" });
		}
		AiChatMessages userMsgInput = inputSession.getAichatmessages().get(0);
		String finalContent = userMsgInput.getAcmcontent(); // 預設為文字內容

		// ======================= 2. 語音辨識處理 (STT) =======================
		// 檢查是否為語音訊息且攜帶 Base64 資料 (暫放 acmaurl)
		if ("VOICE".equals(userMsgInput.getAcmmtype()) && userMsgInput.getAcmaurl() != null
				&& !userMsgInput.getAcmaurl().isEmpty()) {
			try {
				// A. 解碼 Base64
				byte[] audioBytes = java.util.Base64.getDecoder().decode(userMsgInput.getAcmaurl());

				// B. 呼叫本地 Vosk 進行辨識
				finalContent = executeVoskSTT(audioBytes);

				// 如果辨識結果為空，給予預設值
				if (finalContent == null || finalContent.trim().isEmpty())
					finalContent = "[語音內容無法辨識]";
			} catch (Exception e) {
				System.err.println("Vosk 辨識錯誤: " + e.getMessage());
				finalContent = "[語音辨識失敗]";
			}
		}

		// ======================= 3. 會話生命週期管理 =======================
		AiChatSessions currentSession;
		if (inputSession.getAcsid() == null || inputSession.getAcsid() == 0L) {
			currentSession = new AiChatSessions();
			currentSession.setAcsuaccount(packageBean.getUserAccount());
			currentSession.setAcsbtype(inputSession.getAcsbtype());
			// 標題自動取辨識後的內容
			currentSession
					.setAcstitle(finalContent.length() > 20 ? finalContent.substring(0, 20) + "..." : finalContent);
			currentSession.setSyscuser(packageBean.getUserAccount());
			currentSession = aiChatSessionsDao.save(currentSession);
		} else {
			currentSession = aiChatSessionsDao.findById(inputSession.getAcsid())
					.orElseThrow(() -> new Exception("找不到指定的對話紀錄"));
		}

		// ======================= 4. 儲存使用者訊息 =======================
		AiChatMessages userMsg = new AiChatMessages();
		userMsg.setAcmsessions(currentSession);
		userMsg.setRole(MessageRole.USER);
		userMsg.setAcmcontent(finalContent); // 存入辨識後的文字
		userMsg.setAcmmtype(userMsgInput.getAcmmtype());
		userMsg.setSyscuser(packageBean.getUserAccount());

		if ("VOICE".equals(userMsgInput.getAcmmtype())) {
			AiVoiceMetadata voice = new AiVoiceMetadata();
			voice.setAichatmessages(userMsg);
			voice.setAvmttext(finalContent);
			voice.setAvmduration(0.0); // 可由前端傳入或在此計算
			voice.setAvmsrate(16000); // 配合前端 Recorder.js 設定
			voice.setAvmvmodel("Vosk-CN-Small");
			voice.setSyscuser(packageBean.getUserAccount());
			userMsg.setVoiceMetadata(voice);
		}
		aiChatMessagesDao.save(userMsg);

		// ======================= 5. AI 分析 (Gemini) =======================
		// 5.1 取得 1500 筆排程
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "siodate"));
		orders.add(new Order(Direction.ASC, "sifdate"));
		orders.add(new Order(Direction.ASC, "sinb"));
		PageRequest pageable = PageRequest.of(0, 1500, Sort.by(orders));
		ArrayList<ScheduleInfactory> entitys = infactoryDao.findAllBySearch(null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, 0, pageable);

		entitys.forEach(x -> {
			x.setSiscnote(getLatestNote(x.getSiscnote()));
			x.setSimcnote(getLatestNote(x.getSimcnote()));
			x.setSiwmnote(getLatestNote(x.getSiwmnote()));
			x.setSimpnote(getLatestNote(x.getSimpnote()));
		});

		String scheduleContext = convertInfactoryToContext(entitys);

		// 5.2 呼叫 Gemini (告知其輸入可能來自語音辨識)
		String systemPrompt = String.format("\"你現在是專業的『%s』助理。請根據提供的 CSV/Markdown 排程數據進行分析。\n"
				+ " 【重要輸出規則】：\n"
				+ " 1.當需要列出多筆製令、進度或物料清單時，**務必使用 Markdown 表格格式**呈現。\n"
				+ " 2.表格必須包含對齊行，例如：\n"
				+ " |製令單號|狀態|品名|進度|\n"
				+ " |:-|:-|:-|:-|\n"
				+ " 3.請自動將輸入的語音錯字修正為正確的排程術語。\n"
				+ " 4.回答要簡潔有力。",
				currentSession.getAcsbtype());
		String aiAnswer = geminiAiService.generateResponse(systemPrompt, finalContent, scheduleContext);

		// ======================= 6. 儲存與回傳 =======================
		AiChatMessages aiResMsg = new AiChatMessages();
		aiResMsg.setAcmsessions(currentSession);
		aiResMsg.setRole(MessageRole.ASSISTANT);
		aiResMsg.setAcmcontent(aiAnswer);
		aiResMsg.setAcmmtype("TEXT");
		aiResMsg.setSyscuser("GEMINI_AI");
		aiChatMessagesDao.save(aiResMsg);

		List<AiChatMessages> fullHistory = aiChatMessagesDao.findAllBySessionId(currentSession.getAcsid());
		currentSession.setAichatmessages(fullHistory);
		ArrayList<AiChatSessions> lAiChatSessions = new ArrayList<AiChatSessions>();
		lAiChatSessions.add(currentSession);

		packageBean.setEntityJson(packageService.beanToJson(lAiChatSessions));
		JsonObject extra = new JsonObject();
		extra.addProperty("currentSessionId", currentSession.getAcsid());
		packageBean.setCallBackValue(extra.toString());

		return packageBean;
	}

	/**
	 * 使用 Vosk 進行語音轉文字 必須確保 resources/models/vosk-cn 資料夾存在且包含模型檔案
	 */
	private String executeVoskSTT(byte[] audioBytes) throws Exception {
		// 1. 取得模型路徑 (建議使用絕對路徑或確保 ClassLoader 讀取正確)
		// 在 Spring 環境中，通常模型會放在外部目錄或是解壓後的 temp 目錄
		// 簡單開發時可直接指向專案內路徑
		String modelPath = "src/main/resources/models/vosk-cn";

		try (org.vosk.Model model = new org.vosk.Model(modelPath);
				java.io.InputStream ais = new java.io.ByteArrayInputStream(audioBytes);
				org.vosk.Recognizer recognizer = new org.vosk.Recognizer(model, 16000.0f)) {

			byte[] buffer = new byte[4096];
			int nbytes;
			while ((nbytes = ais.read(buffer)) >= 0) {
				// 餵入 PCM 數據
				recognizer.acceptWaveForm(buffer, nbytes);
			}

			// 解析結果 JSON: {"text" : "今天 排程 如何"}
			String jsonStr = recognizer.getFinalResult();
			JsonObject resultJson = JsonParser.parseString(jsonStr).getAsJsonObject();

			return resultJson.get("text").getAsString();
		}
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

	/**
	 * 將 1500 筆排程實體轉換為精簡的 Markdown 表格
	 */
	private String convertInfactoryToContext(List<ScheduleInfactory> entitys) {
		StringBuilder sb = new StringBuilder();
		// 標題行：挑選對決策最重要的欄位
		sb.append("|製令單號|生產狀態(0=暫停中/1=未生產/2=已發料/3=生產中Yy=已完工)|產品品名|生產進度|齊料日|物料狀態(0=未確認/1未齊料/2已齊料)|物控備註|生管備註|\n");

		for (ScheduleInfactory item : entitys) {
			// 2. 清洗所有不可見字元 (Tab, Newline, Unicode Space)
			String shortPName = (item.getSipname() == null) ? ""
					: item.getSipname().replaceAll("[\\x00-\\x1F\\x7F-\\x9F\\s]+", "");
			shortPName = shortPName.replaceAll("[\\r\\n\\t]+", " ").trim();
			// if (shortPName.length() > 8) shortPName = shortPName.substring(0, 8); //
			// 品名縮短至 8 字

			// 3. 備註清洗：移除 JSON 結構，並過濾掉重複的空白
			String siscnote = "";
			if (siscnote != null) {// 生管
				siscnote = cleanNote(item.getSiscnote());
				siscnote = siscnote.replaceAll("[\\s\\r\\n\\t]+", " ").replaceAll("(\\\\r|\\\\n|\\\\t)+", " ").trim();
			}
			String simcnote = "";
			if (simcnote != null) {// 物控
				simcnote = cleanNote(item.getSimcnote());
				simcnote = simcnote.replaceAll("[\\s\\r\\n\\t]+", " ").replaceAll("(\\\\r|\\\\n|\\\\t)+", " ").trim();
			}
			
			// 4. 緊湊拼接 (CSV 格式)
			sb.append(item.getSinb()).append("|") // ID
					.append(item.getSistatus()).append("|") // S
					.append(shortPName).append("|") // N
					.append(item.getSiokqty()).append("/").append(item.getSirqty()).append("|") //// P (分子)/ P (分母)
					.append(item.getSimcdate()).append("|") // D
					.append(item.getSimcstatus()).append("|") // M
					.append(simcnote).append("|") //
					.append(siscnote).append("\n"); // R
		}
		return sb.toString();
	}

	// 輔助方法：清理生管備註 (因為你原始格式是 JSON 字串)
	private String cleanNote(String noteJson) {
		if (noteJson == null || noteJson.equals("[]") || noteJson.isEmpty())
			return "-";
		// 簡單移除 JSON 符號，只留文字內容，避免 AI 混淆
		return noteJson.replaceAll("[\\[\\]{}\"]", "").replace("content:", "").replace("user:", "");
	}

	// --- 輔助方法：解析 JSON Array 並取得最新一筆 ---
	private String getLatestNote(String jsonArrayStr) {
		if (jsonArrayStr == null || jsonArrayStr.isEmpty() || jsonArrayStr.equals("[]")) {
			return "[]";
		}
		try {
			JsonArray array = JsonParser.parseString(jsonArrayStr).getAsJsonArray();
			if (array.size() > 0) {
				// 取出第 0 筆 (最新的紀錄)
				JsonElement latest = array.get(0);
				JsonArray newArray = new JsonArray();
				newArray.add(latest);
				return newArray.toString(); // 回傳只含一筆的 JSON Array
			}
		} catch (Exception e) {
			// 若解析失敗則回傳原值或空，避免程式中斷
			return "[]";
		}
		return "[]";
	}
}
