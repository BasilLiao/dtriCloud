package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.entity.AiChatSessions;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
import dtri.com.tw.shared.PackageBean;

@Service
public class ScheduleInfactoryForAiChatService {

	@Autowired
	private ScheduleInfactoryDao infactoryDao;

	// 在類別中定義一個共用的 ObjectMapper (節省資源)
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * @param jsonIntent     解析意圖?
	 * @param currentSession 對話串流
	 * @param packageBean    涵蓋使用者
	 * @param finalContent   使用者問題
	 * @return 涵蓋 相關單位問題+資料庫查詢+優化資料
	 */
	public String getScheduleInfactoryForAi(String jsonIntent, AiChatSessions currentSession, PackageBean packageBean,
			String finalContent) {

		// 2. 解析 JSON (使用 Gson 或 Jackson)
		JsonObject intent = JsonParser.parseString(jsonIntent).getAsJsonObject();

		String sistatus = intent.get("status").isJsonNull() ? null : intent.get("status").getAsString();
		String sinb = intent.get("sinb").isJsonNull() ? null : intent.get("sinb").getAsString();
		String sipnb = intent.get("sipnb").isJsonNull() ? null : intent.get("sipnb").getAsString();
		String sipname = intent.get("sipname").isJsonNull() ? null : intent.get("sipname").getAsString();
		String simcnote = intent.get("simcnote").isJsonNull() ? null : intent.get("simcnote").getAsString();
		String simcstatus = intent.get("simcstatus").isJsonNull() ? null : intent.get("simcstatus").getAsString();
		String sicorder = intent.get("sicorder").isJsonNull() ? null : intent.get("sicorder").getAsString();// 訂單
		String simcdates = intent.get("simcdates").isJsonNull() ? null : intent.get("simcdates").getAsString();// 預計齊料(起)
		String simcdatee = intent.get("simcdatee").isJsonNull() ? null : intent.get("simcdatee").getAsString();// 預計齊料(終)

		Integer simcstatusInt = null;
		if (simcstatus != null && !"null".equalsIgnoreCase(simcstatus) && !simcstatus.isBlank()) {
			simcstatusInt = Integer.parseInt(simcstatus);
		}
		// 3. 呼叫 DAO 進行精準過濾
		// 這樣 1500 筆可能過濾到剩下 5~50 筆，回覆絕對不會再中斷！
		PageRequest pageable = PageRequest.of(0, 2000, Sort.by(Direction.ASC, "siodate"));
		ArrayList<ScheduleInfactory> entitys = infactoryDao.findAllBySearch(sinb, sipnb, null, null, sipname, null,
				sistatus, null, null, null, simcdates, simcdatee, null, simcstatusInt, 0, sicorder, pageable);

		// Step 3: 如果沒查到資料
		if (entitys == null || entitys.isEmpty()) {
			return "根據您的搜尋條件，目前系統內『尚未查詢到相關排程資料』，請確認條件是否輸入正確。";
		}

		// Step 4: 有資料，直接轉成 Markdown 格式回傳
		int dataCount = entitys.size();
		String scheduleData = convertInfactoryToContext(entitys);

		// 判斷語系
		boolean isZh = packageBean.getUserLanguaue().contains("zh-TW");
		String cannedHeader = "";
		cannedHeader += isZh ? "目前為您找到 『%d』 筆相關資料如下：" : "Found %d matching records for you:";

		// 最終組合內容 (這段文字會直接顯示在前端 UI)
		StringBuilder finalResponse = new StringBuilder();
		finalResponse.append(String.format(cannedHeader, dataCount)).append("\n\n");
		finalResponse.append(scheduleData);

		return finalResponse.toString();
	}

	/** 將 N 筆排程轉為 Markdown (修復格式與優化閱讀) */
	private String convertInfactoryToContext(List<ScheduleInfactory> entitys) {
		if (entitys == null || entitys.isEmpty())
			return "查無排程資料。";

		StringBuilder sb = new StringBuilder();
		// 1. 建立標題
		sb.append("|製令|品號|品名|狀態|進度|生管/物控備註|料況|訂單號|預計齊料日\n");
		// 2. 建立分隔線 (必須也是 9 欄，否則表格會破裂)
		// :--- 代表靠左，:---: 代表置中
		sb.append("|:---|:---|:---|:---:|:---:|:---|:---:|:---|:---:|\n");

		for (ScheduleInfactory item : entitys) {
			// 品名截斷
			String sipname = item.getSipname();
			if (sipname == null)
				sipname = "-";
			sipname = sipname.length() > 30 ? sipname.substring(0, 30) : sipname;

			// 轉換生產狀態 (0=暫停, 1=未產, 2,3=生產中, Y=完工)
			String statusText = formatStatus(item.getSistatus());

			// 轉換料況狀態 (0=未確, 1=缺料, 2=齊料)
			String mcStatusText = formatMcStatus(item.getSimcstatus() + "");

			// 整合備註 (縮減 Token 寬度)
			String scNote = cleanNote(getLatestNoteContent(item.getSiscnote()));
			String mcNote = cleanNote(getLatestNoteContent(item.getSimcnote()));
			String combinedNote = (scNote.equals("-") && mcNote.equals("-")) ? "-" : scNote + " / " + mcNote;

			// 預計齊料日處理 (避免 null)
			String mDate = (item.getSimcdate() == null) ? "-" : item.getSimcdate().toString();

			// 3. 填入數據 (確保也是 9 個 |)
			sb.append("|").append(item.getSinb()) // 1. 製令
					.append("|").append(item.getSipnb()) // 2. 品號
					.append("|").append(sipname) // 3. 品名
					.append("|").append(statusText) // 4. 狀態
					.append("|").append(item.getSiokqty()).append("/").append(item.getSirqty()) // 5. 進度
					.append("|").append(combinedNote) // 6. 備註
					.append("|").append(mcStatusText) // 7. 料況
					.append("|").append(item.getSicorder()) // 8. 訂單號
					.append("|").append(mDate) // 9. 預計齊料
					.append("|\n");
		}
		return sb.toString();
	}

	/** 輔助：生產狀態代碼翻譯 */
	private String formatStatus(String status) {
		if (status == null)
			return "-";
		return switch (status) {
		case "1" -> "🔴未生產";
		case "2" -> "🔴已發料";
		case "3" -> "🔵生產中";
		case "Y", "y" -> "🟢完工";
		case "0" -> "🟡暫停";
		default -> status;
		};
	}

	/** 輔助：料況狀態代碼翻譯 */
	private String formatMcStatus(String status) {
		if ("2".equals(status))
			return "✅齊料";
		if ("1".equals(status))
			return "❌缺料";
		return "⏳待確";
	}

	// 輔助方法：先行過濾文字含意
	public String extractSearchIntent(String userQuestion) {
		// 注入當前系統日期，方便 32B 推算「這週」、「下個月」等時間
		String currentDate = java.time.LocalDate.now().toString();

		String intentPrompt = """
				你現在是 DTR 生管系統的『高階意圖解析引擎』。請從使用者的問題中提取精準的查詢條件。
				當前系統日期為：%s (YYYY-MM-DD HH:mm:ss)。

				# 日期與時間處理準則 (嚴格執行)：
				         1. **格式規範**：所有輸出日期必須符合 `YYYY-MM-DD HH:mm:ss` 格式。
				         2. **區間邊界定義**：
				            - 若使用者說「...之前」(Before)：`simcdatee` 應設為該日期的 **23:59:59**。
				            - 若使用者說「...之後」(After)：`simcdates` 應設為該日期的 **00:00:00**。
				            - 若使用者說「今天」：`simcdates` 為 00:00:00，`simcdatee` 為 23:59:59。
				         3. **語意衝突**：
				            - 「齊料日」是日期欄位。除非明確提到「已齊料」或「料齊了」，否則 `mcstatus` 保持 null。

				# 欄位解析與對應規則：
				1. status (生產狀態):
				   - "1": 未生產 (關鍵字：待產、還沒做、尚未)
				   - "2": 備料中 (關鍵字：準備物料、撿料、籌備中)
				   - "3": 已開工 (關鍵字：生產中、在做、動工、在線、已發料)
				   - "Y": 已完工 (關鍵字：好了、結束、完成、完工)

				2. simcstatus (料況狀態):
				   - 0: 未確認 (關鍵字：沒確認、不知道料況)
				   - 1: 未齊料 (關鍵字：缺料、料沒齊、缺件、欠料)
				   - 2: 已齊料 (關鍵字：必須明確提到『已經』或『狀態為』齊料)

				3. 關鍵字識別：
				   - sinb: 製令單號（格式如 A511-260123... 或 A521...）。
				   - sipnb: 品號/料號（格式如 90-340... 或 90-504...）。
				   - sipname: 產品名稱關鍵字（如：主機板、DT340TA、顯示器）。
				   - simcnote: 提取備註內的特殊要求（如：先貼、延後、特急、急件、補料）。
				   - sicorder: 訂單號碼（使用者提到特定的『訂單』或『PO號』時提取）。

				4. 日期區間推理 (simcdates / simcdatee):
				   - 若提及「這週」、「三月份」、「明天」等，請換算成 YYYY-MM-DD 23:59:59。
				   - simcdates 為區間起始日，simcdatee 為區間結束日。

				# 輸出格式 (嚴格 JSON，禁止輸出額外文字或解釋):
				{
				  "status": "狀態碼或null",
				  "sinb": "單號或null",
				  "sipnb": "料號或null",
				  "sipname": "品名或null",
				  "simcnote": "備註關鍵字或null",
				  "simcstatus": "料況碼或null",
				  "sicorder": "訂單號或null",
				  "simcdates": "YYYY-MM-DD HH:mm:ss或null",
				  "simcdatee": "YYYY-MM-DD HH:mm:ss或null"
				}

				使用者問題： %s
				""".formatted(currentDate, userQuestion);
		return intentPrompt;
	}

	// 輔助方法：清理生管備註 (因為你原始格式是 JSON 字串)
	private String cleanNote(String note) {
		if (note == null || note.isEmpty() || note.equals("[]"))
			return "-";
		return note.replaceAll("[\\[\\]{}\"]", "").replace("content:", "").trim();
	}

	/**
	 * 專門解析備註 JSON 陣列，僅提取第一筆 (Index 0) 的 content 內容 並將換行符號取代為空格，確保 Markdown 表格結構不破裂。
	 */
	private String getLatestNoteContent(String jsonStr) {
		if (jsonStr == null || jsonStr.isEmpty() || jsonStr.equals("[]")) {
			return "-";
		}
		try {
			// 1. 解析成 JsonNode
			JsonNode root = mapper.readTree(jsonStr);

			// 2. 檢查是否為陣列且不為空
			if (root.isArray() && root.size() > 0) {
				JsonNode latestEntry = root.get(0);

				// 3. 提取 content 欄位
				if (latestEntry.has("content")) {
					String content = latestEntry.get("content").asText();

					if (content == null || content.isBlank()) {
						return "-";
					}

					// 🌟 關鍵修正：將換行符號 (\r 或 \n) 取代為一個空格
					// 使用正則表達式 [\\r\\n]+ 可以同時處理連續換行
					return content.replaceAll("[\\r\\n]+", " ").trim();
				}
			}
		} catch (Exception e) {
			// 4. 保底邏輯：如果解析失敗，除了原本的清洗，也要順便處理換行
			return jsonStr.replaceAll("[\\[\\]{}\"]", "").replace("content:", "").replaceAll("[\\r\\n]+", " ").trim();
		}
		return "-";
	}

}
