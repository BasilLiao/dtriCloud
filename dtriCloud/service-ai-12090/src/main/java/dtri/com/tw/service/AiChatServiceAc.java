package dtri.com.tw.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import dtri.com.tw.pgsql.entity.AiChatAttachments;
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
	private AiRecordService aiRecordService;

	@Autowired
	private ScheduleOutInfactoryForAiChatService aboutScheduleInfactory;

	@Autowired
	private BomProductForAiChatService aboutBomProduct;

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
		List<AiChatAttachments> formatAiChatAttachments = new ArrayList<AiChatAttachments>();
		formatAiChatMessages.setVoiceMetadata(formatAiVoiceMetadata);
		formatAiChatMessages.setAttachments(formatAiChatAttachments);
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

	/**
	 * * 新增資料 (核心：串接 5090 耳朵、眼睛、大腦、嘴巴) 支援：語音辨識(STT)、視覺辨識(Vision)、邏輯分析(LLM)、語音合成(TTS)
	 */
	@Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// ======================= 1. 資料解析 =======================
		AiChatSessions inputSession = packageService.jsonToBean(packageBean.getEntityJson(), AiChatSessions.class);

		if (inputSession.getAichatmessages() == null || inputSession.getAichatmessages().isEmpty()) {
			throw new CloudExceptionService(packageBean, ErColor.danger, ErCode.W1000, Lan.zh_TW,
					new String[] { "內容不能為空" });
		}

		AiChatMessages userMsgInput = inputSession.getAichatmessages().get(0);
		String finalContent = userMsgInput.getAcmcontent(); // 使用者輸入的文字
		String mType = userMsgInput.getAcmmtype(); // 訊息類型 (TEXT, VOICE, IMAGE)

		// ======================= 🚀 2. 語系判定 (關鍵步驟) =======================
		// 取得使用這語系zh-TW / zh-CN / en-US / vi-VN
		// 取得使用者語系 (例如: "zh-TW", "en-US", "vi-VN" 或 null)
		String rawUserLg = packageBean.getUserLanguaue();

		// 定義最終要給 5090 AI 使用的語系列舉
		AiDtrService.VoiceLang targetLang;

		if (rawUserLg == null || rawUserLg.isBlank()) {
			// A. 如果沒有語系資料，預設使用英文
			targetLang = AiDtrService.VoiceLang.EN_US;
			log.info("[AI整合] 使用者語系為空，預設降級為: EN_US");
		} else if (rawUserLg.contains("zh")) {
			// B. 包含 zh (zh-TW, zh-CN) 皆判定為中文
			targetLang = AiDtrService.VoiceLang.ZH_TW;
		} else if (rawUserLg.contains("vi")) {
			// C. 包含 vi (vi-VN) 判定為越南文
			targetLang = AiDtrService.VoiceLang.VI_VN;
		} else if (rawUserLg.contains("en")) {
			// D. 包含 en (en-US, en-GB) 判定為英文
			targetLang = AiDtrService.VoiceLang.EN_US;
		} else {
			// E. 沒對到 (例如 fr-FR)，預設使用英文
			targetLang = AiDtrService.VoiceLang.EN_US;
			log.info("[AI整合] 未定義語系 {}, 預設降級為: EN_US", rawUserLg);
		}
		log.info("[AI整合] 最終判定語系為: {} (Code: {})", targetLang, targetLang.getShortCode());

		// ======== 3. 感官預處理 (耳朵 STT / 眼睛 Vision) ========
		byte[] userVoiceBytes = userMsgInput.getAcmvdata(); // 取得語音二進位

		// A. 耳朵處理 (STT - 呼叫 5090 Whisper)
		if ("VOICE".equals(mType) && userVoiceBytes != null) {
			try {
				log.info("[AI整合] 收到語音訊息 (Size: {} bytes)，準備呼叫 5090 Whisper...", userVoiceBytes.length);
				File tempFile = File.createTempFile("dtr_voice_", ".wav");
				try (FileOutputStream fos = new FileOutputStream(tempFile)) {
					fos.write(userVoiceBytes);
				}
				finalContent = dtrAiService.processAudio(tempFile.getAbsolutePath());
				tempFile.delete();
				if (finalContent == null || finalContent.isBlank())
					finalContent = "[語音辨識內容為空]";
			} catch (Exception e) {
				log.error("Whisper 辨識失敗: {}", e.getMessage());
				finalContent = "[語音服務暫時不可用]";
			}
		}
		// B. 眼睛+檔案 處理 (Vision - 呼叫 5090 Gemma 3 Vision)
		// ======== 🚀 B. 多模態素材收集 (修正版) ========
		List<String> imagePaths = new ArrayList<>();
		List<String> filePaths = new ArrayList<>();

		// 1. 遍歷前端傳來的所有附件
		if (userMsgInput.getAttachments() != null && !userMsgInput.getAttachments().isEmpty()) {
			log.info("[AI整合] 開始處理附件清單，共 {} 件...", userMsgInput.getAttachments().size());

			for (AiChatAttachments item : userMsgInput.getAttachments()) {
				byte[] data = item.getFileData();
				if (data == null || data.length == 0)
					continue;

				String fileName = item.getFileName();
				String type = item.getFileType(); // 這是你在 Entity 存的 IMAGE 或 FILE

				try {
					if ("IMAGE".equals(type)) {
						// 📸 處理圖片：統一轉存為 jpg 暫存檔
						File tempImg = File.createTempFile("dtr_vision_", ".jpg");
						try (FileOutputStream fos = new FileOutputStream(tempImg)) {
							fos.write(data);
						}
						imagePaths.add(tempImg.getAbsolutePath());
						log.info("[眼睛] 圖片素材就緒: {}", tempImg.getName());

					} else if ("FILE".equals(type)) {
						// 📄 處理檔案：根據原始副檔名建立暫存檔 (這對 PDFBox/POI 很重要)
						String suffix = ".tmp";
						if (fileName.toLowerCase().endsWith(".pdf"))
							suffix = ".pdf";
						else if (fileName.toLowerCase().endsWith(".xlsx"))
							suffix = ".xlsx";
						else if (fileName.toLowerCase().endsWith(".xls"))
							suffix = ".xls";
						else if (fileName.toLowerCase().endsWith(".docx"))
							suffix = ".docx";
						else if (fileName.toLowerCase().endsWith(".doc"))
							suffix = ".doc";

						File tempDoc = File.createTempFile("dtr_doc_", suffix);
						try (FileOutputStream fos = new FileOutputStream(tempDoc)) {
							fos.write(data);
						}
						filePaths.add(tempDoc.getAbsolutePath());
						log.info("[書本] 文件素材就緒: {}", tempDoc.getName());
					}
				} catch (IOException e) {
					log.error("建立暫存檔失敗: {}", e.getMessage());
				}
			}
		}

		// ======== 4. 會話紀錄管理 (存入資料庫) ========
		AiChatSessions currentSession;
		if (inputSession.getAcsid() == null || inputSession.getAcsid() == 0L) {
			currentSession = new AiChatSessions();
			currentSession.setAcsuaccount(packageBean.getUserAccount());
			currentSession.setAcsbtype(inputSession.getAcsbtype());
			currentSession
					.setAcstitle(finalContent.length() > 15 ? finalContent.substring(0, 15) + "..." : finalContent);
			currentSession.setSyscuser(packageBean.getUserAccount());

			// 🚀 關鍵修改：存入 Record ID
			currentSession.setAcsrid(inputSession.getAcsrid());
			currentSession
					.setAcstitle(finalContent.length() > 15 ? finalContent.substring(0, 15) + "..." : finalContent);
			currentSession.setSyscuser(packageBean.getUserAccount());

			currentSession = aiChatSessionsDao.save(currentSession);
			// 🚀 關鍵：建立該會話的「靈魂」—— SYSTEM 規則
			if ("AREC".equals(currentSession.getAcsbtype())) {
				AiChatMessages systemMsg = new AiChatMessages();
				systemMsg.setAcmsessions(currentSession);
				systemMsg.setRole(MessageRole.SYSTEM); // 👈 標記為 SYSTEM
				// 這裡存入的是「純規則模板」，不帶入動態快照，快照我們發送前再填
				systemMsg.setAcmcontent(aiRecordService.role);
				systemMsg.setSyscuser("SYSTEM_ARCHITECT");
				aiChatMessagesDao.save(systemMsg);
			}
		} else {
			currentSession = aiChatSessionsDao.findById(inputSession.getAcsid()).get();
		}

		// 儲存 USER 訊息 (包含二進位原始數據)
		AiChatMessages userMsg = new AiChatMessages();
		userMsg.setAcmsessions(currentSession);
		userMsg.setRole(MessageRole.USER);
		userMsg.setAcmcontent(finalContent);
		// 🚀 關鍵修正 1：如果原本是語音輸入，將類型改為 TEXT
		// 這樣前端渲染時才不會出現一個「空的播放按鈕」
		if ("VOICE".equals(mType)) {
			userMsg.setAcmmtype("TEXT");
		} else {
			userMsg.setAcmmtype(mType);
		}
		// userMsg.setAcmvdata(userVoiceBytes); // 存入語音 Bytea

		userMsg.setSyscuser(packageBean.getUserAccount());
		// 🚀 關鍵修正：不要直接 setAttachments，改用 loop 調用輔助方法
		if (userMsgInput.getAttachments() != null) {
			for (AiChatAttachments att : userMsgInput.getAttachments()) {
				// 使用輔助方法，確保 attachment.setMessage(userMsg) 被執行
				userMsg.addAttachment(att.getFileName(), att.getFileType(), att.getFileData());
			}
		}
		aiChatMessagesDao.save(userMsg);

		// ======== 5. 大腦分析 (Gemma - 結合業務數據) ========
		log.info("[AI整合] 當前業務類型: {}，正在準備對應數據...", currentSession.getAcsbtype());

		String bType = currentSession.getAcsbtype() == null ? "GENERAL" : currentSession.getAcsbtype();
		Boolean checkOK = true;

		String intentPrompt = "";// 語意分析(使用者)
		String jsonIntentRaw = "";// KeyWord(大腦)
		String brainPrompt = "";// 查詢到的資料

		String intentLastPrompt = "";// 語意分析(專家)
		String jsonIntentLastRaw = "";// 專家講解(大腦)
		// String translation = "";// 已經翻譯好的資料
		String aIsuggestion = "";// AI語音用

		String aiCleanAnswer = "";// 回傳訊息

		// 使用 Switch 根據業務類型 (PMC, MC, PUR...) 切換數據抓取邏輯
		switch (bType) {

		case "AREC": // 錄製型
			log.info("[AI整合] 執行 AREC 錄製分析邏輯...");
			// 🧠 C. 統一交付大腦 (一次性呼叫)
			if (!finalContent.equals("") || !imagePaths.isEmpty() || !filePaths.isEmpty()) {

				// 1. 取得目前快照 (Context)
				String currentSnapshot = currentSession.getAcsccontext();
				if (currentSnapshot == null || currentSnapshot.isEmpty()) {
					currentSnapshot = "{}"; // 若是新對話，給予空 JSON
				}

				// 2. 注入指令模板 (將指令中的變數替換掉)
				String enrichedRole = aiRecordService.role.replace("{{current_snapshot}}", currentSnapshot)
						.replace("{{user_input}}", finalContent);

				// 3. 取得歷史紀錄
				List<AiChatMessages> history = aiChatMessagesDao.findAllBySessionId(currentSession.getAcsid());

				// 4. 呼叫大腦 (注意：我們將 enrichedRole 作為 prompt 傳入，確保規則始終置頂)
				// brainPrompt = dtrAiService.processMultimodal(history, imagePaths, filePaths,
				// enrichedRole);
				// 4. 🚀 修改點：直接傳 finalContent，不要傳帶有規則的 enrichedRole
				brainPrompt = dtrAiService.processMultimodal(history, imagePaths, filePaths, finalContent);

				// 5. 🚀 快照自動更新：從 AI 的回覆中擷取最新的 JSON 並存入 Session Context
				String newSnapshot = dtrAiService.cleanJson(brainPrompt);
				if (newSnapshot.contains("{") && newSnapshot.contains("}")) {
					// 找到真正的 JSON 起始點 (避免 AI 前面的文字干擾)
					int start = newSnapshot.indexOf("{");
					int end = newSnapshot.lastIndexOf("}");
					String extractedJson = newSnapshot.substring(start, end + 1);

					log.info("[AREC] 偵測到新快照，更新 Session 上下文...");
					currentSession.setAcsccontext(extractedJson);
					aiChatSessionsDao.save(currentSession);
				}

				aIsuggestion = brainPrompt; // 用於語音播報
			}

			break;
		case "AMTI": // 共用型
			log.info("[AI整合] 執行 AMTI 多模組...");
			// 🧠 C. 統一交付大腦 (一次性呼叫)
			if (!finalContent.equals("") || !imagePaths.isEmpty() || !filePaths.isEmpty()) {
				log.info("[大腦] 啟動多模態分析：圖片 x{}, 檔案 x{}", imagePaths.size(), filePaths.size());
				// 取得歷史紀錄
				List<AiChatMessages> history = aiChatMessagesDao.findAllBySessionId(currentSession.getAcsid());
				// 🚀 關鍵：你需要一個能同時接收圖片與檔案清單的方法
				// 假設你在 dtrAiService 建立了一個 processMultimodal 方法
				brainPrompt = dtrAiService.processMultimodal(history, imagePaths, filePaths, finalContent);
				// 語音?
				aIsuggestion = brainPrompt;

			}

			break;
		case "APMC": // 生管 (Production Management & Control)
			log.info("[AI整合] 執行 PMC 生管分析邏輯...");
			/**
			 * 流程: Step1.System[語意補強](extractSearchIntent) -> <br>
			 * Step2.AI[大腦思考]取得KeyWord(processText) -> <br>
			 * Step3.System提取[Cloud資料] & 整理資料 -> <br>
			 * Step4.System[語意補強]取得專家腳色(getExpertAdvice) -> <br>
			 * Step5.AI[大腦思考]取得專業建議 & 翻譯(processText)-> <br>
			 * 
			 **/
			// 語意補強
			intentPrompt = aboutScheduleInfactory.extractSearchIntent(finalContent);
			// [大腦思考] 取得大腦回傳資料後 資料提取 & 格式
			jsonIntentRaw = dtrAiService.processText(intentPrompt);
			// 提取Cloud資料 & 整理資料(如果沒資料則 回傳空)
			brainPrompt = aboutScheduleInfactory.getScheduleInfactoryForAi(jsonIntentRaw, finalContent);
			// 專家模式
			if (userMsgInput.getAcmeon()) {
				// 語意補強 專業建議
				intentLastPrompt = aboutScheduleInfactory.getExpertAdvice(brainPrompt, finalContent, targetLang);
				// 最終 專業建議[大腦思考]
				jsonIntentLastRaw = dtrAiService.processText(intentLastPrompt);

				try {
					// 嘗試從 JSON 提取建議
					aIsuggestion = JsonParser.parseString(jsonIntentLastRaw).getAsJsonObject().get("aIsuggestion")
							.getAsString();
				} catch (Exception e) {
					// 若不是 JSON 或解析失敗，將整串文字視為建議，並強行修正為 JSON 格式
					aIsuggestion = jsonIntentLastRaw;
					jsonIntentLastRaw = String.format("{\"aIsuggestion\":\"%s\"}",
							aIsuggestion.replace("\"", "\\\"").replace("\n", " "));
				}

				// 取得 JSON(translation 翻譯)(aIsuggestion 專家建議)
				JsonObject intentJson = JsonParser.parseString(jsonIntentLastRaw).getAsJsonObject();
				// AI語音用(專家說明)
				if (!intentJson.get("aIsuggestion").isJsonNull()) {
					aIsuggestion = intentJson.get("aIsuggestion").getAsString();
				}
				//
				brainPrompt += "\n\n---\n\n";
				brainPrompt += aIsuggestion;
			}
			break;

		case "AMMC": // 物控 (Material Control)
			log.info("[AI整合] 執行 MC 物控分析邏輯 (預留區)...");
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
			// 語意補強
			intentPrompt = aboutBomProduct.extractProductIntent(finalContent);
			// [大腦思考] 取得大腦回傳資料後 資料提取 & 格式
			jsonIntentRaw = dtrAiService.processText(intentPrompt);
			// 提取Cloud資料 & 整理資料(如果沒資料則 回傳空)
			brainPrompt = aboutBomProduct.getBomProductForAi(jsonIntentRaw, finalContent);
			if (userMsgInput.getAcmeon()) {
				// 語意補強 專業建議
				intentLastPrompt = aboutBomProduct.getProductExpertAdvice(brainPrompt, finalContent, targetLang);
				// 最終 專業建議[大腦思考]
				jsonIntentLastRaw = dtrAiService.processText(intentLastPrompt);

				try {
					// 嘗試從 JSON 提取建議
					aIsuggestion = JsonParser.parseString(jsonIntentLastRaw).getAsJsonObject().get("aIsuggestion")
							.getAsString();
				} catch (Exception e) {
					// 若不是 JSON 或解析失敗，將整串文字視為建議，並強行修正為 JSON 格式
					aIsuggestion = jsonIntentLastRaw;
					jsonIntentLastRaw = String.format("{\"aIsuggestion\":\"%s\"}",
							aIsuggestion.replace("\"", "\\\"").replace("\n", " "));
				}

				// 取得 JSON(translation 翻譯)(aIsuggestion 專家建議)
				JsonObject intentJson = JsonParser.parseString(jsonIntentLastRaw).getAsJsonObject();

				// AI語音用(專家說明)
				if (!intentJson.get("aIsuggestion").isJsonNull()) {
					aIsuggestion = intentJson.get("aIsuggestion").getAsString();
				}
				//
				brainPrompt += "\n\n---\n\n";
				brainPrompt += aIsuggestion;

			}

			break;

		default: // 通用模式
			log.warn("[AI整合] 未知業務類型，使用通用 Prompt 處理");
			brainPrompt = dtrAiService.processText("你現在是 DTR 綜合助理。請回答: " + finalContent);
			break;
		}
		// 無資料應用
		if (brainPrompt.equals("")) {
			aiCleanAnswer = "⚠️ 無法執行 『" + finalContent + "』 相關的資訊。 \n\n";
			aiCleanAnswer += "系統條件 : " + jsonIntentRaw + "\n\n";
			checkOK = false;
		}

		if (checkOK) {
			aiCleanAnswer = brainPrompt;
		}

		// ======== 6. 嘴巴合成 (TTS - 讓 AI 回覆自動語音播放) ========
		byte[] aiVoiceBytes = null;
		// 使否有藥用語音
		if (userMsgInput.getAcmvon() && !aIsuggestion.equals("")) {
			try {
				// 🚀 取得目前專案根目錄
				String projectPath = System.getProperty("user.dir");
				String tempDir = projectPath + File.separator + "temp_voice";

				// 🚀 確保暫存資料夾存在
				File dir = new File(tempDir);
				if (!dir.exists()) {
					dir.mkdirs();
					log.info("[系統] 建立語音暫存資料夾: {}", tempDir);
				}

				String voiceFileName = "DTR_TTS_" + UUID.randomUUID().toString().substring(0, 8);

				// 🚀 呼叫 5090 合成音檔至專案暫存區
				dtrAiService.downloadVoice(aIsuggestion, voiceFileName, targetLang, tempDir);

				// 🚀 讀取合成好的音檔並存入資料庫 (Bytea)
				File voiceFile = new File(tempDir + File.separator + voiceFileName + ".mp3");
				if (voiceFile.exists()) {
					aiVoiceBytes = Files.readAllBytes(voiceFile.toPath());
					log.info("[嘴巴] 音檔已轉為二進位並準備入庫，大小: {} bytes", aiVoiceBytes.length);

					voiceFile.delete(); // 🚀 存入 DB 後立即刪除專案內的暫存檔，保持乾淨
				}
			} catch (Exception e) {
				log.warn("[嘴巴] 合成或讀取失敗，僅回傳文字: {}", e.getMessage());
			}
		}
		// ======================= 6. 儲存 AI 回覆並返回 =======================
		AiChatMessages aiResMsg = new AiChatMessages();
		aiResMsg.setAcmsessions(currentSession);
		aiResMsg.setRole(MessageRole.ASSISTANT);
		aiResMsg.setAcmcontent(aiCleanAnswer);
		aiResMsg.setAcmmtype("VOICE"); // 標註為語音類型，觸發前端播放按鈕
		aiResMsg.setAcmvdata(aiVoiceBytes); // 存入回覆語音 Bytea
		aiResMsg.setSyscuser("Gemma3_12B_it_5090");
		aiChatMessagesDao.save(aiResMsg);
		// 2. 🧠 統一交付大腦 (一次性呼叫多模態方法)
		if (!imagePaths.isEmpty() || !filePaths.isEmpty()) {
			// 呼叫我們之前寫好的全能處理核心
			// finalContent = dtrAiService.processMultimodal(imagePaths, filePaths,
			// finalContent);

			// 🧹 重要：處理完畢後立即刪除暫存檔，釋放硬碟空間
			dtrAiService.cleanupTempFiles(imagePaths);
			dtrAiService.cleanupTempFiles(filePaths);
		}

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
