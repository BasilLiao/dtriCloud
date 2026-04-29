package dtri.com.tw.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.entity.AiChatMessages;
import dtri.com.tw.pgsql.entity.AiChatMessages.MessageRole;

@Service
public class AiDtrService {

	private static final Logger log = LoggerFactory.getLogger(AiDtrService.class);

	// AI 伺服器端點配置 (RTX 5090)
	private static final String AI_SERVER_IP = "10.1.90.93";
	private static final String WHISPER_URL = "http://" + AI_SERVER_IP + ":8000/v1/audio/transcriptions";
	private static final String VISION_URL = "http://" + AI_SERVER_IP + ":8002/v1/chat/completions";
	private static final String BRAIN_URL = "http://" + AI_SERVER_IP + ":8002/v1/chat/completions";
	private static final String TTS_URL = "http://" + AI_SERVER_IP + ":10300/tts";
	// 🚀 新增：伺服器驗證金鑰
	private static final String AI_API_KEY = "my-super-secret-key-2026";

	// 模型名稱定義
	private static final String BRAIN_MODEL = "google/gemma-3-12b-it";
	private static final String VISION_MODEL = "google/gemma-3-12b-it";
	private static final String WHISPER_MODEL = "large-v3";

	// 🚀 新增：定義最大請求字元限制 (估計約 30k-50k Tokens)
	// Gemma-3-12b 這種模型，Context Window 通常很大 (128K)，但為了節省頻寬與推理時間，我們可以設定一個安全的 字元上限（例如
	// 100,000 字元）。
	// 🚀 針對 32K 限制的字元守衛 (28672 tokens * 約 2.3 字元 ≈ 65000 字元)
	private static final int MAX_REQUEST_CHARS = 65000;

	// HTTP 用戶端 (設定 60 秒超時以應對大量數據推理)
	private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(60)).build();

	// ==========================================
	// 1. 耳朵：語音處理模組 (STT)
	// ==========================================
	public String processAudio(String audioPath) {
		log.info("[耳朵] 正在辨識音檔: {}", audioPath);
		try {
			Path path = Path.of(audioPath);
			String boundary = "Boundary-" + UUID.randomUUID().toString();
			byte[] body = createMultipartBody(path, boundary);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(WHISPER_URL))
					.header("Content-Type", "multipart/form-data; boundary=" + boundary)
					.POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();

			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			String transcription = extractJsonValue(response.body(), "text");

			log.info("[耳朵] 辨識結果: [{}]", transcription);
			return transcription;
		} catch (Exception e) {
			log.error("[耳朵] 失敗: {}", e.getMessage());
			return "";
		}
	}

	// ==========================================
	// 2. 眼睛：視覺辨識模組
	// ==========================================
	public String processImage(List<String> imagePaths, String prompt) {
		try {
			long startTime = System.currentTimeMillis();
			List<String> contentItems = new ArrayList<>();
			contentItems.add("{\"type\": \"text\", \"text\": \"%s\"}".formatted(escapeJson(prompt)));

			for (String path : imagePaths) {
				byte[] enhanced = enhanceForAi(path);
				String base64 = Base64.getEncoder().encodeToString(enhanced);
				contentItems.add("{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64,%s\"}}"
						.formatted(base64));
			}

			String jsonPayload = "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":[%s]}],\"max_tokens\":2048,\"temperature\":0.1}"
					.formatted(VISION_MODEL, String.join(",", contentItems));

			log.info("[眼睛] 呼叫 8002 視覺推理中...");
			String response = postRequest(VISION_URL, jsonPayload);
			String finalResult = extractJsonValueFromOpenAi(response, "content");

			log.info("[眼睛] 結論: {} (耗時: {} ms)", finalResult, (System.currentTimeMillis() - startTime));
			return finalResult;
		} catch (Exception e) {
			log.error("[眼睛] 失敗: {}", e.getMessage());
			return "[視覺辨識異常]";
		}
	}

	/**
	 * 5090 多模態處理核心：支援自動歷史紀錄裁切 (FIFO 策略) * @param historyMessage 歷史對話清單
	 * 
	 * @param images 圖片路徑清單
	 * @param files  文件路徑清單
	 * @param prompt 使用者指令
	 */
	public String processMultimodal(List<AiChatMessages> historyMessage, List<String> images, List<String> files,
			String prompt) {
		try {
			// 🚀 1. 計算「當前這一回」的資料量 (圖片 Base64 + 文件數據 + 指令)
			// 這是絕對不能被刪除的內容，所以我們優先計算它的權重。
			long currentTurnSize = estimateCurrentTurnSize(prompt, images, files);

			// 🚀 2. 動態裁切歷史紀錄 (Token 管理)
			// 如果 (歷史 + 當前) > 上限，則依序拋棄最舊的訊息。
			List<AiChatMessages> trimmedHistory = trimHistory(historyMessage, currentTurnSize);

			// 🚀 3. 建立對話陣列容器
			List<String> messagesJsonParts = new ArrayList<>();

			// 處理裁切後的歷史紀錄
			if (trimmedHistory != null && !trimmedHistory.isEmpty()) {
				for (int i = 0; i < trimmedHistory.size(); i++) {
					AiChatMessages hm = trimmedHistory.get(i);

					// 跳過重複訊息邏輯
					if (i == trimmedHistory.size() - 1 && hm.getRole() == MessageRole.USER
							&& hm.getAcmcontent().equals(prompt)) {
						continue;
					}

					String roleStr = hm.getRole().name().toLowerCase();
					String contentStr = hm.getAcmcontent();

					// 🚀 注入 Snapshot 到 SYSTEM
					if (hm.getRole() == MessageRole.SYSTEM) {
						String latestSnapshot = hm.getAcmsessions().getAcsccontext();
						contentStr = contentStr.replace("{{current_snapshot}}",
								(latestSnapshot == null || latestSnapshot.isEmpty()) ? "{}" : latestSnapshot);
						contentStr = contentStr.replace("{{user_input}}", prompt);
					}

					// 🚀【核心修正點】：統一格式
					// 不論是 SYSTEM, USER 還是 ASSISTANT 的歷史紀錄，
					// 通通包裝成 [{"type": "text", "text": "..."}] 的 JSON Array 格式。
					// 這樣 vLLM 的 Jinja2 模板在處理多模態請求時，才不會因為「型別不一」而報錯。
					String wrappedContent = "[{\"type\": \"text\", \"text\": \"%s\"}]"
							.formatted(escapeJson(contentStr));

					// 注意：content 欄位後方不加雙引號，因為 wrappedContent 本身就是 JSON 陣列
					messagesJsonParts.add("{\"role\": \"%s\", \"content\": %s}".formatted(roleStr, wrappedContent));
				}
			}

			// 🚀 4. 準備「當前這一次」的多模態 ContentItems
			List<String> currentContentItems = new ArrayList<>();
			currentContentItems.add("{\"type\": \"text\", \"text\": \"%s\"}".formatted(escapeJson(prompt)));

			// B. 處理文件檔案 (PDF/Excel/Word)
			for (String path : files) {
				File f = new File(path);
				String name = f.getName().toLowerCase();
				if (name.endsWith(".pdf")) {
					List<byte[]> pdfPages = DocumentToolService.pdfToImages(f);
					for (byte[] page : pdfPages) {
						String b64 = Base64.getEncoder().encodeToString(page);
						currentContentItems.add(
								"{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64,%s\"}}"
										.formatted(b64));
					}
					currentContentItems.add("{\"type\": \"text\", \"text\": \"[PDF數據]: %s\"}"
							.formatted(escapeJson(DocumentToolService.pdfToJson(f))));
				} else if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
					currentContentItems.add("{\"type\": \"text\", \"text\": \"[Excel數據]: %s\"}"
							.formatted(escapeJson(DocumentToolService.excelToJson(f))));
				} else if (name.endsWith(".docx") || name.endsWith(".doc")) {
					currentContentItems.add("{\"type\": \"text\", \"text\": \"[Word數據]: %s\"}"
							.formatted(escapeJson(DocumentToolService.wordToJson(f))));
				}
			}

			// C. 處理圖片 (影像強化)
			for (String path : images) {
				byte[] img = enhanceForAi(path);
				String b64 = Base64.getEncoder().encodeToString(img);
				currentContentItems
						.add("{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64,%s\"}}"
								.formatted(b64));
			}

			// 🚀 5. 組裝當前訊息與最終 Payload
			String currentMsgJson = "{\"role\": \"user\", \"content\": [%s]}"
					.formatted(String.join(",", currentContentItems));
			messagesJsonParts.add(currentMsgJson);

			String jsonPayload = "{\"model\":\"%s\", \"messages\": [%s], \"max_tokens\": 4096, \"temperature\": 0.2}"
					.formatted(VISION_MODEL, String.join(",", messagesJsonParts));

			log.info("[大腦] 歷史裁切後剩餘 {} 則。Payload 估計大小: {} 字元。發送請求...", trimmedHistory.size(), jsonPayload.length());

			String response = postRequest(VISION_URL, jsonPayload);
			Map<String, String> splitResult = splitAiResponse(response);

			log.info("[大腦] 推理完成");
			return cleanJson(splitResult.get("answer"));

		} catch (Exception e) {
			log.error("[全能分析] 嚴重失敗: {}", e.getMessage());
			return "[多模態辨識異常]: " + e.getMessage();
		}
	}

	// ==========================================
	// 3. 大腦：邏輯思考模組
	// ==========================================
	public String processText(String prompt) {
		log.info("[大腦] 處理推理請求，長度: {}", prompt.length());
		String jsonPayload = "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":4096,\"temperature\":0.3}"
				.formatted(BRAIN_MODEL, escapeJson(prompt));

		try {
			String response = postRequest(BRAIN_URL, jsonPayload);
			Map<String, String> splitResult = splitAiResponse(response);
			log.info("[大腦] 推理完成");
			return cleanJson(splitResult.get("answer"));
		} catch (Exception e) {
			log.error("[大腦] 失敗: {}", e.getMessage());
			return "[AI大腦回應異常]";
		}
	}

	// ==========================================
	// 4. 嘴巴：語音合成模組 (TTS - MP3 版)
	// 🚀 多語系模型映射表 (固定對照，不可變)
	// ==========================================

	// 🚀 更新：語系枚舉簡化為短代碼，對應 Docker 內的資料夾名稱
	public enum VoiceLang {
		ZH_TW("zh"), EN_US("en"), VI_VN("vi");

		private final String shortCode;

		VoiceLang(String shortCode) {
			this.shortCode = shortCode;
		}

		public String getShortCode() {
			return shortCode;
		}
	}

	/**
	 * @param text     合成文字
	 * @param fileName 存檔名稱 (不含副檔名)
	 * @param lang     語系列舉 (ZH_TW, EN_US, VI_VN)
	 * @param saveDir  指定存檔資料夾
	 */
	public void downloadVoice(String text, String fileName, VoiceLang lang, String saveDir) {
		try {
			// 1. 文字預處理：透過 Gemma 3 優化播報節奏 (保留您原本強大的 cleanTextNoise)
			String optimizedText = cleanTextNoise("", text, lang);

			// 2. 編碼 URL 參數
			String encodedText = URLEncoder.encode(optimizedText, StandardCharsets.UTF_8);
			String targetLang = lang.getShortCode();

			// 🚀 關鍵：URL 修改為對應新的 Docker 接口 (?text=...&lang=...)
			String urlString = String.format("%s?text=%s&lang=%s", TTS_URL, encodedText, targetLang);

			log.info("[嘴巴] 正在向 5090 請求 MP3 語音: [{}], 語系: {}", optimizedText, targetLang);

			// 3. 建立請求 (設定 45 秒超時，應對長難句合成)
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).timeout(Duration.ofSeconds(45))
					.GET().build();

			// 🚀 關鍵：副檔名改為 .mp3
			Path path = Path.of(saveDir, fileName + ".mp3");

			// 4. 發送並存檔
			HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(path));

			if (response.statusCode() == 200) {
				log.info("[嘴巴] MP3 存檔成功: {}", path.toAbsolutePath());
			} else {
				log.error("[嘴巴] 伺服器回傳錯誤代碼: {}", response.statusCode());
			}

		} catch (Exception e) {
			log.error("[嘴巴] 語音合成連線失敗: {}", e.getMessage());
		}
	}

	/**
	 * 透過 Gemma 3 模型進行語音標準化處理 去除多餘的符號
	 */
	private String cleanTextNoise(String history, String input, VoiceLang lang) {
		if (input == null || input.isBlank()) {
			return "";
		}

		// 根據傳入語系動態決定指令 (如果是越文或英文，則要求 AI 輸出對應語言)
		String targetLangName = switch (lang) {
		case ZH_TW -> "台灣繁體中文 (口語化)";
		case EN_US -> "Natural English (Professional)";
		case VI_VN -> "Vietnamese (Industrial Standard)";
		};

		// 🚀 升級版 Gemma 3 專用推理翻譯
		String ttsCleanerPrompt = """
				[System Role]
				你是一位專業的工業產線語音播報員。你的任務是將生硬的技術資料 (User Data) 轉換為「完全符合目標語系」且「流暢」的播報稿。

				[Target Language]
				**必須完全使用以下語言輸出：%s**。
				(警告：嚴禁在輸出中保留任何原始語言的字元，例如在英文播報中出現中文字)。

				[Task]
				1. 🚀【跨語言翻譯】：
				   - 將所有中文欄位標題翻譯為目標語言。
				     (例如：製令單 -> Work Order / 單狀態 -> Status / 齊料 -> Materials Ready / 備註 -> Notes)。
				   - 將狀態符號轉化為口語：✅ 轉化為 "Confirmed" 或 "Ready"，❌ 轉化為 "Missing" 或 "Shortage"。
				2. 🚀【智慧縮寫擴展】：
				   - "BK" -> "Black", "W/TP" -> "With Touch Panel", "Ver." -> "Version", "Qty" -> "Quantity"。
				3. 🚀【字元規範化 (解決 TTS 報錯)】：
				   - **強制** 將所有全形符號（如：，、。（ ））轉換為半形符號或空格。
				   - 型號（如 A511, DT313）在英數之間插入空格（如：A 5 1 1），確保 TTS 逐字朗讀。
				4. 🚀【流暢度與節奏】：
				   - 每一段落結尾加入自然的停頓詞。
				   - 移除所有 Markdown 語法（* # | -）。

				[User Data]
				%s

				[Strict Rules]
				1. 輸出長度必須在 500 字以內。
				2. 絕對不允許出現目標語言以外的文字。
				3. 移除所有無法朗讀的特殊符號。

				[Output Example (If English)]
				User: "製令單: A511, 狀態: ✅齊料"
				Assistant: "Work order A 5 1 1. Status: Materials are ready."
				""".formatted(targetLangName, input);

		try {
			// 呼叫 8002 端口的 Gemma 3 推理大腦
			String aiOptimizedText = processText(ttsCleanerPrompt);

			if (aiOptimizedText != null) {
				// 確保最後清除任何漏掉的符號
				// [ ] 內部的符號代表「只要符合其中一個就替換」
				// \\* 代表轉義星號，使其變回普通文字
				// \\- 代表轉義減號
				// 🚀 同時去除：全形括號、減號、星號、井字號、底線、以及中式引號「 」
				aiOptimizedText = aiOptimizedText.replaceAll("[（）\\-*#_「」]", " ").trim();

				// 如果你還想把多個連續空格縮減為一個空格，可以再補這行：
				aiOptimizedText = aiOptimizedText.replaceAll("\\s+", " ");
				return aiOptimizedText;

			}
		} catch (Exception e) {
			log.error("[語音優化] 大腦推理失敗，使用基礎清洗...");
			return input.replaceAll("[_/-]", " ");
		}
		return "";
	}

	// ==========================================
	// 🛠️ 內部工具方法
	// ==========================================

	/**
	 * 執行標準 HTTP POST 請求 (JSON 格式)
	 * 
	 * @param url         目標端點 (例如 Brain 或 Vision URL)
	 * @param jsonPayload 準備好的 JSON 字串
	 * @return 伺服器回傳的 Raw Body
	 */
	private String postRequest(String url, String jsonPayload) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
				// 🚀 新增這行：帶上通行密碼
				.header("Authorization", "Bearer " + AI_API_KEY)
				.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8)).build();
		return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
	}

	/**
	 * 拆分 AI 的思考過程 (<think>) 與最終答案 專門處理 Gemma 等具備「思維鏈」能力的模型回傳值
	 * 
	 * @param rawResponse 從 OpenAI 格式中提取出的原始內容
	 * @return 包含 "thought"(思考) 與 "answer"(結論) 的 Map
	 */
	public Map<String, String> splitAiResponse(String rawResponse) {
		Map<String, String> result = new HashMap<>();
		// 先從 OpenAI JSON 結構中抓出 content 欄位
		String content = extractJsonValueFromOpenAi(rawResponse, "content");

		// 使用正則表達式尋找 <think> 標籤中的內容 (DOTALL 代表包含換行)
		Pattern pattern = Pattern.compile("<think>(.*?)</think>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		if (matcher.find()) {
			result.put("thought", matcher.group(1).trim());
			result.put("answer", content.replaceAll("<think>.*?</think>", "").trim());
		} else {
			result.put("thought", "無思考紀錄");
			result.put("answer", content);
		}
		return result;
	}

	/**
	 * 手動構建 Multipart 表單 (用於 Whisper STT) 因為 Java 11 HttpClient 內建不支援
	 * Multipart，故手動封裝邊界數據
	 * 
	 * @param filePath 音檔路徑
	 * @param boundary 隨機生成的邊界字串
	 */
	private static byte[] createMultipartBody(Path filePath, String boundary) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] newLine = "\r\n".getBytes(StandardCharsets.UTF_8);

		// 寫入檔案部分
		os.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.getFileName() + "\"\r\n")
				.getBytes(StandardCharsets.UTF_8));
		os.write("Content-Type: audio/mpeg\r\n\r\n".getBytes(StandardCharsets.UTF_8));
		Files.copy(filePath, os);
		os.write(("\r\n--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));

		// 寫入指定模型名稱
		os.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
		os.write((WHISPER_MODEL + "\r\n").getBytes(StandardCharsets.UTF_8));
		os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
		return os.toByteArray();
	}

	/**
	 * 基礎 JSON 欄位提取 (正則版) 用於簡單的單層 JSON 結構提取
	 */
	private static String extractJsonValue(String json, String key) {
		Pattern p = Pattern.compile("\"" + key + "\":\\s*\"(.*?)\"");
		Matcher m = p.matcher(json);
		return m.find() ? m.group(1) : "";
	}

	/**
	 * 進階 OpenAI 格式內容提取 處理包含轉義引號 (\\") 與 換行 (\\n) 的複雜文本塊
	 */
	private static String extractJsonValueFromOpenAi(String json, String key) {
		try {
			// 使用「後行斷言」(Negative Lookbehind) 排除掉被轉義過的引號
			Pattern p = Pattern.compile("\"" + key + "\":\\s*\"(.*?)(?<!\\\\)\"");
			Matcher m = p.matcher(json);
			if (m.find()) {
				// 將回傳的 JSON 轉義字元還原成正常的 Java 字串格式
				return m.group(1).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
			}
		} catch (Exception e) {
			log.error("OpenAI JSON 解析失敗: {}", e.getMessage());
		}
		return "";
	}

	/**
	 * JSON 字串轉義處理 防止 Prompt 內容中的雙引號或換行導致 JSON 格式崩潰
	 */
	private static String escapeJson(String input) {
		if (input == null)
			return "";
		return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	/**
	 * JSON 內容清洗器 當 AI 回傳 Markdown 格式 (如 ```json ... ```) 時，強行截取第一個 { 到最後一個 }
	 */
	public String cleanJson(String raw) {
		if (raw == null || raw.isEmpty())
			return "";

		// 🚀 移除開頭的 ```json (不分大小寫) 或單純的 ```
		// 🚀 移除結尾的 ```
		// (?i) 代表不區分大小寫，\\s* 代表處理可能存在的空格
		String processed = raw.replaceAll("(?i)```json", "").replaceAll("(?i)```", "");

		return processed.trim();
	}

	/**
	 * 影像強化處理：針對工業 OCR/視覺辨識優化 處理流程：格式標準化 -> 亮度對比度微調 -> 銳利化邊緣 -> 輸出 Byte Array
	 * * @param imagePath 原始圖片的路徑
	 * 
	 * @return 強化後的 JPG 圖片位元組陣列 (byte[])
	 * @throws Exception 處理過程中的 IO 或影像處理異常
	 */
	public static byte[] enhanceForAi(String imagePath) throws Exception {
		// 1. 讀取檔案並驗證是否存在
		File inputFile = new File(imagePath);
		if (!inputFile.exists()) {
			throw new FileNotFoundException("找不到影像檔案: " + imagePath);
		}

		BufferedImage sourceImg = ImageIO.read(inputFile);
		if (sourceImg == null) {
			throw new Exception("無法解析影像格式 (可能檔案損毀或不支援): " + imagePath);
		}

		// 2. 格式標準化 (關鍵步驟)
		// 許多 PNG 帶有透明通道 (Alpha) 或索引色 (Indexed Color)，這會導致濾鏡處理失敗。
		// 我們建立一個新的 RGB 畫布，將原圖「畫」上去，強制轉為標準 TYPE_INT_RGB 格式。
		BufferedImage img = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.drawImage(sourceImg, 0, 0, null);
		g.dispose(); // 釋放繪圖資源

		// 3. 亮度與對比度調整 (RescaleOp)
		// 增益 (Gain) 1.1f：稍微增加對比與亮度。
		// 偏移量 (Offset) 0f：不進行全局亮度平移。
		// 目的：防止白色標籤在強光下過曝，同時讓文字與背景對比更明顯。
		RescaleOp rescale = new RescaleOp(1.1f, 0f, null);
		img = rescale.filter(img, null);

		// 4. 銳利化處理 (ConvolveOp)
		// 卷積核矩陣定義如下：
		// [ 0, -0.5, 0 ]
		// [ -0.5, 3, -0.5 ]
		// [ 0, -0.5, 0 ]
		// 目的：強化文字邊緣，減少標籤印刷模糊造成的辨識錯誤。
		// 中心點 3f 為強化權重，負值為削弱週邊，達到銳利化效果。
		float[] sharpenKernel = { 0f, -0.5f, 0f, -0.5f, 3f, -0.5f, 0f, -0.5f, 0f };
		Kernel kernel = new Kernel(3, 3, sharpenKernel);
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		img = op.filter(img, null);

		// 5. 輸出轉換為位元組陣列
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 由於我們前面已轉為 TYPE_INT_RGB，這裡轉 JPG 不會出錯
		boolean writeSuccess = ImageIO.write(img, "jpg", baos);

		if (!writeSuccess) {
			throw new Exception("影像寫入 Byte Array 失敗 (ImageIO.write 回傳 false)");
		}

		return baos.toByteArray();
	}

	/**
	 * 垃圾回收 (Memory Leak)：
	 */
	public void cleanupTempFiles(List<String> paths) {
		for (String path : paths) {
			try {
				File f = new File(path);
				if (f.exists()) {
					f.delete();
					log.debug("[系統] 已刪除暫存檔: {}", path);
				}
			} catch (Exception e) {
				log.warn("[系統] 無法刪除暫存檔: {}", e.getMessage());
			}
		}
	}
	// ==========================================
	// 🛠️ 新增：Token/字元管理工具
	// ==========================================

	/**
	 * 估算「當前這一回」的內容大小 (文字 + Base64 圖片 + 檔案資料)
	 */
	private long estimateCurrentTurnSize(String prompt, List<String> images, List<String> files) {
		long size = (prompt != null) ? prompt.length() : 0;
		// 圖片 Base64 非常大，每一張約增加 1.3 倍體積
		for (String img : images)
			size += (new File(img).length() * 1.35);
		for (String file : files)
			size += (new File(file).length()); // 文件數據簡化計算
		return size;
	}

	/**
	 * 核心裁切邏輯：確保 Index 0 的 SYSTEM 訊息不被移除
	 */
	private List<AiChatMessages> trimHistory(List<AiChatMessages> history, long currentTurnSize) {
		if (history == null || history.isEmpty())
			return new ArrayList<>();

		List<AiChatMessages> trimmed = new ArrayList<>(history);
		long totalSize = calculateHistorySize(trimmed) + currentTurnSize;

		// 🚀 只要總量超過上限且還有對話可刪
		while (totalSize > MAX_REQUEST_CHARS && trimmed.size() > 1) {
			// 如果索引 0 是 SYSTEM 訊息，我們就刪除索引 1 (最舊的一則 User/Assistant 對話)
			if (trimmed.get(0).getRole() == MessageRole.SYSTEM) {
				AiChatMessages removed = trimmed.remove(1);
				totalSize -= (removed.getAcmcontent() != null ? removed.getAcmcontent().length() : 0);
				log.warn("[Token管理] 裁切最舊對話 (保留規則層): {}",
						removed.getAcmcontent().substring(0, Math.min(5, removed.getAcmcontent().length())));
			} else {
				// 如果沒有 SYSTEM 訊息，正常從最前面刪
				AiChatMessages removed = trimmed.remove(0);
				totalSize -= (removed.getAcmcontent() != null ? removed.getAcmcontent().length() : 0);
			}
		}
		return trimmed;
	}

	private long calculateHistorySize(List<AiChatMessages> history) {
		return history.stream().mapToLong(m -> m.getAcmcontent() != null ? m.getAcmcontent().length() : 0).sum();
	}

}