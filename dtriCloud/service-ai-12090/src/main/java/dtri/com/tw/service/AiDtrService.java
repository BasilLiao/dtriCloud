package dtri.com.tw.service;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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

import org.springframework.stereotype.Service;

@Service
public class AiDtrService {
	// AI 伺服器端點配置 (RTX 5090)
	private static final String AI_SERVER_IP = "10.1.90.93";
	private static final String WHISPER_URL = "http://" + AI_SERVER_IP + ":8000/v1/audio/transcriptions";
	private static final String VISION_URL = "http://" + AI_SERVER_IP + ":8001/v1/chat/completions";
	private static final String BRAIN_URL = "http://" + AI_SERVER_IP + ":8002/v1/chat/completions";

	// 模型名稱定義
	//private static final String BRAIN_MODEL = "casperhansen/deepseek-r1-distill-qwen-14b-awq";
	private static final String BRAIN_MODEL = "casperhansen/deepseek-r1-distill-qwen-32b-awq";
	private static final String VISION_MODEL = "OpenGVLab/InternVL2-8B-AWQ";
	private static final String WHISPER_MODEL = "large-v3"; // 對齊 Docker 設定

	// HTTP 用戶端 (設定 60 秒超時以應對大量數據推理)
	private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(60)).build();
//使用範例
//	public static void main(String[] args) {
//		System.out.println("=== DTR AI 自動化檢測系統 啟動 ===");
//
//		// 1. 耳朵：從 MP3 檔案獲取指令
//		String audioFilePath = "C:/Users/q2551/Videos/螢幕錄製內容/螢幕錄製 2026-02-05 144738.mp3";
//		processAudio(audioFilePath);
//
//		// 2. 影像路徑設定
//		String voicePrompt = "請比對兩張照片箱子側面，第一張照片是正確樣本，請檢察第二張照片是否有少貼標籤？若少於則回傳 false。";
//		List<String> imagePaths = new ArrayList<>();
//		imagePaths.add("C:/Users/q2551/Downloads/外鄉標籤Image.jpg");
//		imagePaths.add("C:/Users/q2551/Downloads/外鄉標籤2Image.jpg");
//
//		// 3. 眼睛：將語音辨識出的文字作為 Prompt 傳給視覺模型
//		processImage(imagePaths, voicePrompt);
//
//		// 4. 大腦：進階邏輯
//		processText("根據剛才辨識出的結果，說明為何標籤數量不符？");
//	}

	// ==========================================
	// 1. 耳朵：語音處理模組 (STT)
	// 功能：將生產現場錄製的 MP3 轉換為文字指令
	// ==========================================
	public String processAudio(String audioPath) {
		long startTime = System.currentTimeMillis();
		System.out.println("[耳朵] 正在讀取音檔: " + audioPath);
		try {
			Path path = Path.of(audioPath);
			String boundary = "Boundary-" + UUID.randomUUID().toString();

			// 封裝建立 Multipart Body (包含 file 和 model 兩個欄位) 格式 (這是 Whisper 要求的格式)
			byte[] body = createMultipartBody(path, boundary);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(WHISPER_URL))
					.header("Content-Type", "multipart/form-data; boundary=" + boundary)
					.POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();

			HttpResponse<String> response = client.send(request,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			// 從 JSON 中提取 "text" 欄位
			String transcription = extractJsonValue(response.body(), "text");

			long duration = System.currentTimeMillis() - startTime;
			System.out.println(String.format("[耳朵] 識別完成! 內容: [%s] (耗時: %d ms)", transcription, duration));
			return transcription;
		} catch (Exception e) {
			System.err.println("[錯誤] 語音模組失敗: " + e.getMessage());
			return "";
		}
	}

	// ==========================================
	// 2. 眼睛：視覺辨識模組 (VLM)
	// 功能：比對產線照片、檢查標籤或產品外觀
	// ==========================================
	public static void processImage(List<String> imagePaths, String prompt) {
		try {
			long startTime = System.currentTimeMillis();
			List<String> contentItems = new ArrayList<>();
			// 添加文字 Prompt
			contentItems.add("{\"type\": \"text\", \"text\": \"%s\"}".formatted(escapeJson(prompt)));

			// 圖片預處理：強化對比度與銳利度，並轉為 Base64
			for (String path : imagePaths) {
				System.out.println("[視覺預處理] 正在強化圖片: " + path);
				byte[] enhanced = enhanceForAi(path);
				String base64 = Base64.getEncoder().encodeToString(enhanced);
				contentItems.add("{\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/jpeg;base64,%s\"}}"
						.formatted(base64));
			}

			// 構建 OpenAI 相容格式的 Payload
			String jsonPayload = "{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":[%s]}],\"max_tokens\":2048,\"temperature\":0.1}"
					.formatted(VISION_MODEL, String.join(",", contentItems));

			System.out.println("[眼睛] 正在呼叫 8001 (使用圖片辨識)...");
			String response = postRequest(VISION_URL, jsonPayload);

			long duration = System.currentTimeMillis() - startTime;
			System.out.println(String.format("[眼睛] 辨識任務結束 (耗時: %d ms), 回傳: %s", duration, response));
		} catch (Exception e) {
			System.err.println("[錯誤] 視覺模組失敗: " + e.getMessage());
		}
	}

	// ==========================================
	// 3. 大腦：邏輯思考模組 (LLM)
	// 功能：處理排程大數據、回答邏輯問題、拆分思考與正式回覆
	// ==========================================
	public String processText(String prompt) {
		long startTime = System.currentTimeMillis();
		System.out.println("[大腦] 接收到分析請求，長度: " + prompt.length());

		// 1. 取得總容量 (對齊 Docker 設定)
		final int TOTAL_CONTEXT = 24576;

		// 2. 估算輸入的 Token 數 (簡單算法：字元數 / 1.5)
		int estimatedInputTokens = (int) (prompt.length() / 1.5);

		// 3. 動態計算剩餘可用的輸出空間
		// 留 10% 作為緩衝，避免邊界錯誤
		int safeMaxTokens = (int) ((TOTAL_CONTEXT - estimatedInputTokens) * 0.9);

		// 5. 极低限度保護 (如果輸入太長，至少留 256 給 AI 報錯用)
		if (safeMaxTokens < 256) {
			System.err.println("[警告] 輸入資料過大，可能導致溢出！");
			safeMaxTokens = 256;
		}
		// 構建 Payload (注意：加上括號確保 formatted 能作用於整串字串)
		/**
		 * role:使用者?user/system
		 * 
		 * content: 這是最重要的資料區塊。所有的排程 問題+資訊
		 * 
		 * max_tokens : 輸出上限 / 緩衝區大小 用意：限制 AI 回傳答案的最大長度。 <br>
		 * Total Context(總空間) = Prompt Tokens(輸入) + Max Tokens(預留輸出) <br>
		 * --max-model-len 32768 一個中文字或特殊符號大約佔 1.5 ~ 2 個 Token <br>
		 * 
		 * temperature : 低數值 (0.1 ~ 0.3)：AI 會變得非常死板、嚴謹。適合用於「標籤比對」、「數量核對」，答案會很穩定。 <br>
		 * 中數值 (0.6 ~ 0.7)：你目前使用的設定。這最適合「生管分析」，讓 AI 具備一點推理能力，能靈活判斷備註內容，但又不至於胡說八道。<br>
		 * 高數值 (0.9 以上)：AI 會開始亂想。在生管系統中絕對不要設這麼高，否則它會生出不存在的製令單號。
		 */
		String jsonPayload = ("{\"model\":\"%s\"," //
				+ "\"messages\":[{\"role\":\"user\","//
				+ "\"content\":\"%s\"}],"//
				+ "\"max_tokens\":4096," //
				+ "\"temperature\":0.5}").formatted(BRAIN_MODEL, escapeJson(prompt));

		try {
			// 執行 POST 請求
			String response = postRequest(BRAIN_URL, jsonPayload);

			// 1. 先提取 JSON 內的 content 內容 (這裡簡化，假設 response 已含 content)
			// 在真實 Spring 環境中建議用 Jackson 解析，這裡先處理 raw 文字拆分
			Map<String, String> splitResult = splitAiResponse(response);

			long duration = System.currentTimeMillis() - startTime;
			System.out.println("-------------------------------------------");
			System.out.println("[大腦] 思考過程 (Thought):\n" + splitResult.get("thought"));
			System.out.println("[大腦] 最終結論 (Answer):\n" + splitResult.get("answer"));
			System.out.println(String.format("=== 大腦任務完成 (總耗時: %d ms) ===", duration));

			// 返回包含 <think> 的原始結果，由呼叫端決定如何使用，或只回傳 answer
			return splitResult.get("answer");
		} catch (Exception e) {
			System.err.println("[錯誤] 大腦推理失敗: " + e.getMessage());
			return "[錯誤] 伺服器回應異常";
		}
	}

	/**
	 * 核心工具：拆分 DeepSeek 的 <think> 標籤 將 AI 的思考過程與最終答案分離，提升產線報表的可讀性
	 */
	public Map<String, String> splitAiResponse(String rawResponse) {
		Map<String, String> result = new HashMap<>();
		String thought = "無思考紀錄";
		String answer = rawResponse;

		// Regex: 尋找 <think> 標籤及其內部的所有內容 (包含換行)
		Pattern pattern = Pattern.compile("<think>(.*?)</think>", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(rawResponse);

		if (matcher.find()) {
			thought = matcher.group(1).trim(); // 提取思考過程
			answer = rawResponse.replaceAll("<think>.*?</think>", "").trim(); // 移除標籤，保留純答案
		}

		result.put("thought", thought);
		result.put("answer", answer);
		return result;
	}

	// ==========================================
	// 輔助工具：Multipart 封裝與 JSON 解析
	// ==========================================

	/** 建立音檔上傳所需的 Multipart Body */
	private static byte[] createMultipartBody(Path filePath, String boundary) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] newLine = "\r\n".getBytes(StandardCharsets.UTF_8);

		// 欄位 1: file
		os.write(("--" + boundary).getBytes(StandardCharsets.UTF_8));
		os.write(newLine);
		os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filePath.getFileName() + "\"")
				.getBytes(StandardCharsets.UTF_8));
		os.write(newLine);
		os.write("Content-Type: audio/mpeg".getBytes(StandardCharsets.UTF_8));
		os.write(newLine);
		os.write(newLine);
		Files.copy(filePath, os);
		os.write(newLine);

		// 欄位 2: model
		os.write(("--" + boundary).getBytes(StandardCharsets.UTF_8));
		os.write(newLine);
		os.write("Content-Disposition: form-data; name=\"model\"".getBytes(StandardCharsets.UTF_8));
		os.write(newLine);
		os.write(newLine);
		os.write(WHISPER_MODEL.getBytes(StandardCharsets.UTF_8));
		os.write(newLine);

		os.write(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
		os.write(newLine);
		return os.toByteArray();
	}

	/** 簡易 JSON 提取 (僅用於 STT text 提取) */
	private static String extractJsonValue(String json, String key) {
		String pattern = "\"" + key + "\":\"";
		int start = json.indexOf(pattern);
		if (start == -1)
			return "";
		start += pattern.length();
		int end = json.indexOf("\"", start);
		return json.substring(start, end);
	}

	/** 執行標準 HTTP POST 請求 */
	private static String postRequest(String url, String jsonPayload) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8)).build();
		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		return response.body();
	}

	/** 處理 JSON 特殊字元轉義，防止 Payload 損壞 */
	private static String escapeJson(String input) {
		return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	/** 處理 JSON 清除特定格式 只刷取JSON內容 */
	public String cleanJson(String raw) {
		if (raw == null)
			return "{}";
		// 尋找第一個 { 到最後一個 }
		int start = raw.indexOf("{");
		int end = raw.lastIndexOf("}");
		if (start != -1 && end != -1 && start < end) {
			return raw.substring(start, end + 1);
		}
		return raw.trim();
	}

	/** 圖片強化處理：增加對比度(1.2x)與銳利度，協助 AI 辨識細小元件 */
	public static byte[] enhanceForAi(String imagePath) throws Exception {
		BufferedImage img = ImageIO.read(new File(imagePath));
		img = new RescaleOp(1.2f, 0f, null).filter(img, null);
		float[] sharpen = { 0f, -1f, 0f, -1f, 5f, -1f, 0f, -1f, 0f };
		img = new ConvolveOp(new Kernel(3, 3, sharpen), ConvolveOp.EDGE_NO_OP, null).filter(img, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "jpg", baos);
		return baos.toByteArray();
	}
}