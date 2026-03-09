package dtri.com.tw.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class GeminiAiService {

	// 控制模式：免費版 "FREE" 或 企業版 "VERTEX"
	// @Value("${google.ai.mode:FREE}")
	@Value("${google.ai.mode:VERTEX}")
	private String aiMode;

	@Value("${google.ai.api.key:}") // 用於 FREE 模式
	private String apiKey;

	@Value("${google.gcp.project-id:}") // 用於 VERTEX 模式
	private String projectId;

	@Value("${google.gcp.location:us-central1}")
	private String location;

	@Value("${google.ai.model:gemini-2.0-flash}")
	private String modelName;

	@Value("${google.ai.model:src/main/resources/gcp-vertex-key.json}")
	private String keyPath;

	// Vertex AI 成員
	private VertexAI vertexAi;
	private GenerativeModel vertexModel;

	// 免費版 WebClient
	private final WebClient webClient = WebClient.builder()
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)).build();

	@PostConstruct
	public void init() throws IOException {
		if ("VERTEX".equalsIgnoreCase(aiMode)) {
			System.out.println("🚀 初始化模式: Vertex AI (Enterprise)");

			// 💡 關鍵修正：不再使用 FileInputStream(path)，改用 getResourceAsStream
			// 這能確保在 IDE 運行或 JAR 包執行時都能讀到 classpath 下的檔案
			try (InputStream is = getClass().getClassLoader().getResourceAsStream("gcp-vertex-key.json")) {
				if (is == null) {
					throw new IOException("找不到憑證檔案: gcp-vertex-key.json");
				}

				GoogleCredentials credentials = GoogleCredentials.fromStream(is)
						.createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-platform"));

				this.vertexAi = new VertexAI.Builder().setProjectId(projectId).setLocation(location)
						.setCredentials(credentials).build();
				this.vertexModel = new GenerativeModel(modelName, vertexAi);
			}
		} else {
			System.out.println("☁️ 初始化模式: Gemini API (Free)");
		}
	}

	public String generateResponse(String systemPrompt, String userPrompt, String jsonData) {
		String fullPrompt = String.format("系統指令：%s\n\n數據資料：%s\n\n使用者提問：%s", systemPrompt, jsonData, userPrompt);

		if ("VERTEX".equalsIgnoreCase(aiMode)) {
			return callVertexAi(fullPrompt);
		} else {
			return callFreeGemini(fullPrompt);
		}
	}

	/** 模式 A: Vertex AI SDK */
	private String callVertexAi(String fullPrompt) {
		try {
			GenerateContentResponse response = vertexModel.generateContent(fullPrompt);
			System.out.println("--- Vertex AI Usage ---");
			System.out.println("Prompt Tokens: " + response.getUsageMetadata().getPromptTokenCount());
			System.out.println("Response Tokens: " + response.getUsageMetadata().getCandidatesTokenCount());
			return ResponseHandler.getText(response);
		} catch (Exception e) {
			return "Vertex AI 錯誤: " + e.getMessage();
		}
	}

	/** 模式 B: 免費版 REST API */
	private String callFreeGemini(String fullPrompt) {
		String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
				modelName, apiKey);

		// 構建結構化的 JSON Body
		JsonObject requestBody = new JsonObject();
		JsonArray contents = new JsonArray();
		JsonObject contentObj = new JsonObject();
		JsonArray parts = new JsonArray();
		JsonObject textPart = new JsonObject();
		textPart.addProperty("text", fullPrompt);
		parts.add(textPart);
		contentObj.add("parts", parts);
		contents.add(contentObj);
		requestBody.add("contents", contents);

		try {
			// 關鍵修改：bodyToMono 改為 String.class
			String responseBody = webClient.post().uri(url).contentType(MediaType.APPLICATION_JSON)
					.bodyValue(requestBody.toString()).retrieve().bodyToMono(String.class) // 直接拿原始 JSON 字串
					.block();

			return extractTextFromMap(responseBody);
		} catch (Exception e) {
			return "Gemini 免費版錯誤: " + e.getMessage();
		}
	}

	private String extractTextFromMap(String jsonResponse) {
		try {
			// 1. 直接將原始字串解析為 JsonObject
			JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

			// 2. 透過連鎖調用安全地取得 text
			// 結構：candidates[0] -> content -> parts[0] -> text
			return json.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content")
					.getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();

		} catch (Exception e) {
			// 紀錄錯誤 Log 以利排查
			System.err.println("JSON 解析失敗: " + e.getMessage());
			return "回傳解析異常";
		}
	}

	@PreDestroy
	public void close() {
		if (this.vertexAi != null)
			this.vertexAi.close();
	}
}