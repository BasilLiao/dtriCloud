package dtri.com.tw.service; // 請根據你的專案路徑調整

import static io.qdrant.client.ValueFactory.value; // 💡 關鍵：解決 Value 衝突的救星
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;

@Service
public class QdrantSyncService {

	// 🛠️ 5090 伺服器配置
	private final String SERVER_IP = "10.1.90.93";
	private final String COLLECTION_NAME = "schedule_infactory";

	// Qdrant gRPC 連線 (Port 6334)
	private final QdrantClient qdrantClient = new QdrantClient(
			QdrantGrpcClient.newBuilder(SERVER_IP, 6334, false).build());

	// vLLM Embedding API (Port 8003)
	private final String EMBEDDING_URL = "http://" + SERVER_IP + ":8003/v1/embeddings";

	/**
	 * 執行同步：接收外部封裝好的點位資訊，並由 AI 補上向量後存入 Qdrant * @param points 外部已經封裝好 ID 與 Payload
	 * 的資料清單
	 */
	public void syncAllToQdrant(List<PointStruct> points) throws Exception {
		if (points == null || points.isEmpty()) {
			System.out.println("⚠️ [DTR AI] 傳入的 points 清單為空，取消同步。");
			return;
		}

		RestTemplate restTemplate = new RestTemplate();
		// 💡 建立一個「出貨暫存區」，不要直接動原本的 points 清單
		List<PointStruct> batchList = new ArrayList<>();

		System.out.println("🚀 [DTR AI] 開始同步，外部封裝筆數：" + points.size());

		for (PointStruct originalPoint : points) {
			try {
				// 1. 從原本的 Payload 中提取要轉向量的特徵文字
				// 💡 假設你在外部封裝時，將要比對的文字存在 "feature_text" 這個 Key 裡面
				String textToVector = originalPoint.getPayloadMap().getOrDefault("feature_text", value(""))
						.getStringValue();

				// 2. 呼叫 vLLM (8003) 取得 1024 維向量
				List<Float> vector = getVectorFromVllm(restTemplate, textToVector);
				if (vector.isEmpty())
					continue;

				// 3. 重新建立 Point (因為 PointStruct 是不可變的，必須用 newBuilder 重新封裝)
				PointStruct finalizedPoint = PointStruct.newBuilder().setId(originalPoint.getId()) // 保留原本的 ID (si_id)
						.setVectors(vectors(vector)) // 注入剛產生的 AI 向量
						.putAllPayload(originalPoint.getPayloadMap()) // 繼承原本所有的欄位
						.build();

				batchList.add(finalizedPoint);

				// 4. 批次寫入 (每 100 筆為一組傳送給 5090 伺服器)
				if (batchList.size() >= 100) {
					qdrantClient.upsertAsync(COLLECTION_NAME, batchList).get();
					batchList.clear(); // 💡 清空的是暫存區，不是原始資料
					System.out.print(">");
				}
			} catch (Exception e) {
				System.err.println("❌ 同步單筆點位失敗 (ID: " + originalPoint.getId() + "): " + e.getMessage());
			}
		}

		// 5. 處理最後不到 100 筆的殘留資料
		if (!batchList.isEmpty()) {
			qdrantClient.upsertAsync(COLLECTION_NAME, batchList).get();
		}
		System.out.println("\n✅ [DTR AI] 同步完成！所有點位已補齊向量並存入 Qdrant。");
	}

	/**
	 * 2. 語意搜尋：輸入模糊字句，找回最相關的 5 筆排程
	 */
	public List<Map<String, Value>> searchSchedule(String queryText) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		// A. 將問題向量化
		List<Float> queryVector = getVectorFromVllm(restTemplate, queryText);
		if (queryVector.isEmpty())
			return new ArrayList<>();

		// B. 去 Qdrant 進行餘弦相似度檢索
		List<ScoredPoint> results = qdrantClient.searchAsync(SearchPoints.newBuilder()
				.setCollectionName(COLLECTION_NAME).addAllVector(queryVector).setWithPayload(enable(true)).setLimit(5) // 取前
																														// 5
																														// 名最像的
				.build()).get();

		// C. 提取 Payload 並回傳
		return results.stream().map(ScoredPoint::getPayloadMap).toList();
	}

	/**
	 * 3. 呼叫 vLLM (8003) 翻譯官
	 */
	@SuppressWarnings("unchecked")
	private List<Float> getVectorFromVllm(RestTemplate restTemplate, String text) {
		Map<String, Object> request = new HashMap<>();
		request.put("model", "BAAI/bge-m3");
		request.put("input", text);

		try {
			// 1. 呼叫 API
			Map<String, Object> response = restTemplate.postForObject(EMBEDDING_URL, request, Map.class);

			// 2. 取得原始數據 (此時可能是 List<Double> 或 List<Number>)
			List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
			Object rawEmbedding = data.get(0).get("embedding");

			if (rawEmbedding instanceof List) {
				List<?> rawList = (List<?>) rawEmbedding;
				List<Float> floatVector = new ArrayList<>();

				// 3. 關鍵：逐一轉換為 Float
				for (Object val : rawList) {
					if (val instanceof Number) {
						floatVector.add(((Number) val).floatValue()); // 強制轉為 float
					}
				}
				return floatVector;
			}
		} catch (Exception e) {
			System.err.println("❌ vLLM (8003) 向量轉換失敗: " + e.getMessage());
		}
		return new ArrayList<>();
	}

	/**
	 * 語義搜尋：輸入模糊字句，找回最相關的 5 筆排程
	 */
	public List<Map<String, String>> searchInQdrant(String queryText) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		// 1. 將使用者的問題「向量化」 (呼叫 8003 埠位)
		List<Float> queryVector = getVectorFromVllm(restTemplate, queryText);
		if (queryVector.isEmpty())
			return new ArrayList<>();

		// 2. 在 Qdrant 中進行「餘弦相似度」比對
		// 就像是在 1024 維空間裡找距離 queryVector 最近的點
		List<ScoredPoint> results = qdrantClient
				.searchAsync(SearchPoints.newBuilder().setCollectionName(COLLECTION_NAME) // 你定義的 "schedule_infactory"
						.addAllVector(queryVector) // 傳入問題向量
						.setWithPayload(enable(true)) // 💡 記得要把資料帶回來
						.setLimit(20) // 取最像的前 20 筆
						.build())
				.get();

		// 3. 將結果從 Qdrant 格式轉換為簡單的 Java Map，方便前端顯示
		List<Map<String, String>> finalResults = new ArrayList<>();
		for (ScoredPoint point : results) {
			Map<String, String> row = new HashMap<>();
			// 取得該點位的得分 (相似度，通常 0.8 以上就很準)
			row.put("score", String.format("%.2f", point.getScore()));

			// 提取我們當初存入的所有 Payload
			point.getPayloadMap().forEach((key, value) -> {
				row.put(key, value.getStringValue());
			});
			finalResults.add(row);
		}

		return finalResults;
	}
}