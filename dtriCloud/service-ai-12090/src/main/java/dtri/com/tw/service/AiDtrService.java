package dtri.com.tw.service;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

@Service
public class AiDtrService {

	private static final Logger log = LoggerFactory.getLogger(AiDtrService.class);

	// AI 伺服器端點配置 (RTX 5090)
	private static final String AI_SERVER_IP = "10.1.90.93";
	private static final String WHISPER_URL = "http://" + AI_SERVER_IP + ":8000/v1/audio/transcriptions";
	private static final String VISION_URL = "http://" + AI_SERVER_IP + ":8002/v1/chat/completions";
	private static final String BRAIN_URL = "http://" + AI_SERVER_IP + ":8002/v1/chat/completions";
	private static final String TTS_URL = "http://" + AI_SERVER_IP + ":10300/tts";

	// 模型名稱定義
	private static final String BRAIN_MODEL = "google/gemma-3-12b-it";
	private static final String VISION_MODEL = "google/gemma-3-12b-it";
	private static final String WHISPER_MODEL = "large-v3";

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
			String optimizedText = cleanTextNoise(text, lang);

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
	 * 透過 Gemma 3 模型進行語音標準化處理 針對產線 BOM 與技術規格進行「口語化」轉譯
	 */
	private String cleanTextNoise(String input, VoiceLang lang) {
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
				return aiOptimizedText.replaceAll("）", " ").replaceAll("（", " ").replaceAll("-", " ")
						.replaceAll("*", " ").replaceAll("#", " ").trim();
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
				.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8)).build();
		return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
	}

	/**
	 * 拆分 AI 的思考過程 (<think>) 與最終答案 專門處理 DeepSeek 或 Gemma 等具備「思維鏈」能力的模型回傳值
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
		int start = raw.indexOf("{");
		int end = raw.lastIndexOf("}");
		// 如果沒有陣列
		return (start != -1 && end != -1) ? raw.substring(start, end + 1) : raw;
	}

	/**
	 * 影像強化處理 (針對工業 OCR/Vision) 1. 調整亮對比度 (防止白色標籤過曝) 2. 銳利化處理 (讓條碼與文字邊緣更清晰，提高辨識準確度)
	 */
	public static byte[] enhanceForAi(String imagePath) throws Exception {
		BufferedImage img = ImageIO.read(new File(imagePath));

		// 1. 亮對比度調整: 增益 1.1f (微調，避免全白過亮)
		img = new RescaleOp(1.1f, 0f, null).filter(img, null);

		// 2. 銳利化捲積核 (Sharpen Kernel): 強化邊緣對比
		float[] sharpen = { 0f, -0.5f, 0f, -0.5f, 3f, -0.5f, 0f, -0.5f, 0f };
		img = new ConvolveOp(new Kernel(3, 3, sharpen), ConvolveOp.EDGE_NO_OP, null).filter(img, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos); // 轉為 JPG 格式輸出為 Byte Array
		return baos.toByteArray();
	}
}