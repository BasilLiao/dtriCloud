package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import dtri.com.tw.pgsql.entity.BomItemSpecifications;
import dtri.com.tw.pgsql.entity.BomItemSpecificationsDetailFront;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
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
			// 有請求詢問問題
			// Step4-1. 取得資料(一般/細節)
			AiChatSessions searchData = packageService.jsonToBean(packageBean.getEntityJson(), AiChatSessions.class);

			// 在 AiChatService.java 中根據 acsbtype 設定 AI 角色
			String systemPrompt = "";
			switch (searchData.getAcsbtype()) {
			case "APMC":
				systemPrompt = "你現在是專業的生管助理，請分析這份排程資料...";
				break;
			case "AMMC":
				systemPrompt = "你現在是專業的物控助理，請檢查目前的缺料狀況...";
				break;
			case "APUR":
				systemPrompt = "你現在是專業的採購助理，請追蹤供應商的交期...";
				break;
			case "AWMS":
				systemPrompt = "你現在是專業的倉儲助理，請提供儲位優化建議...";
				break;
			default:
				systemPrompt = "你是一個通用型的企業助理，請協助處理以下問題...";
			}

			// 將 systemPrompt 餵給 Google AI...

			packageBean = sendMessage(packageBean);
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

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {

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
}
