package dtri.com.tw.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.entity.BomProductManagement;

@Service
public class BomProductManagementForAiChatService {

	private static final Logger log = LoggerFactory.getLogger(BomProductManagementForAiChatService.class);

	@Autowired
	private BomProductManagementDao bomDao;

	// Jackson 用於解析 bpmbisitem 內的複雜 JSON
	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * 核心方法：取得產品規格並格式化為 AI 上下文
	 * 
	 * @param jsonIntent   由 AI 解析出的搜尋意圖 (JSON 格式)
	 * @param userQuestion 使用者的原始問題
	 * @return 格式化後的 Markdown 字串
	 */
	public String getBomProductForAi(String jsonIntent, String userQuestion) {
		log.info("[APDM] 接收到搜尋意圖: {}", jsonIntent);

		// 1. 解析 AI 傳回的意圖 JSON
		JsonObject intent = JsonParser.parseString(jsonIntent).getAsJsonObject();

		String bpmnb = intent.get("bpmnb").isJsonNull() ? null : intent.get("bpmnb").getAsString();
		String bpmmodel = intent.get("bpmmodel").isJsonNull() ? null : intent.get("bpmmodel").getAsString();
		// 2. 處理多重關鍵字 (轉換為 SQL 格式: %keyword%)
		String[] keywordArray = null;
		if (intent.has("specKeywords") && intent.get("specKeywords").isJsonArray()) {
			JsonArray ja = intent.get("specKeywords").getAsJsonArray();
			keywordArray = new String[ja.size()];
			for (int i = 0; i < ja.size(); i++) {
				keywordArray[i] = "%" + ja.get(i).getAsString() + "%";
			}
		}
		String[] excludeKeywords = null;
		if (intent.has("excludeKeywords") && intent.get("excludeKeywords").isJsonArray()) {
			JsonArray ja = intent.get("excludeKeywords").getAsJsonArray();
			excludeKeywords = new String[ja.size()];
			for (int i = 0; i < ja.size(); i++) {
				excludeKeywords[i] = "%" + ja.get(i).getAsString() + "%";
			}
		}

		// 2. 呼叫你寫好的 DAO 進行模糊查詢 (取前 50 筆)

		ArrayList<BomProductManagement> entitys = bomDao.findAllByAdvancedSearch(bpmnb, bpmmodel, keywordArray,
				excludeKeywords);

		if (entitys == null || entitys.isEmpty() ) {
			return "⚠️ 在規格資料庫中找不到與 『" + userQuestion + "』 相關的產品資訊。";
		}

		// 3. 根據結果筆數決定回傳格式
		StringBuilder sb = new StringBuilder();
		if (entitys.size() == 1) {
			// 只有一筆：顯示超詳細 BOM 表格
			sb.append(formatSingleBomDetailed(entitys.get(0)));
		} else {
			// 多筆：先顯示清單供使用者選擇
			sb.append("📋 為您找到以下相關產品，共" + entitys.size() + "台，請問您想查看哪一台的詳細規格？\n\n");
			sb.append("| 品號 (BOM NB) | 型號 (Model) | 產品描述 | 產品類型 |\n");
			sb.append("|:---|:---|:---|:---|\n");
			for (BomProductManagement item : entitys) {
				sb.append("| ")//
						.append(item.getBpmnb()).append(" | ")//
						.append(item.getBpmmodel()).append(" | ")//
						.append(cleanMarkdownCell(item.getSysnote())).append(" | ")//
						.append(item.getBpmtypename()).append(" |\n");
			}
			sb.append("\n💡 *提示：您可以直接輸入品號來查看詳細 BOM 內容。*");
		}

		return sb.toString();
	}

	/**
	 * 將單一筆 BOM 轉換為詳細的 Markdown 表格 (解析 bpmbisitem)
	 */
	private String formatSingleBomDetailed(BomProductManagement bom) {
		StringBuilder sb = new StringBuilder();
		sb.append("## 🔍 產品規格詳情: ").append(bom.getBpmmodel()).append("\n");
		sb.append("- **品號**: `").append(bom.getBpmnb()).append("`\n");
		sb.append("- **類別**: ").append(bom.getBpmtypename()).append("\n");
		sb.append("- **備註**: ").append(bom.getSysnote().isEmpty() ? "-" : bom.getSysnote()).append("\n\n");

		sb.append("### 🛠️ 硬體配置清單 (BOM Items)\n");
		sb.append("| 類別 (Group) | 規格內容 (Specification) | 數量 | 製程 | 階層 |\n");
		sb.append("|:---|:---|:---:|:---:|:---:|\n");

		try {
			// 解析 bpmbisitem 內的 JSON 字串
			JsonNode root = mapper.readTree(bom.getBpmbisitem());
			JsonNode items = root.get("items");

			if (items != null && items.isArray()) {
				for (JsonNode node : items) {
					String gName = node.get("bisgname").asText(); // 類別
					String fName = node.get("bisfname").toString(); // 規格陣列
					String qty = node.get("bisqty").asText();
					String process = node.get("bisprocess").asText();
					String level = node.get("bislevel").asText();

					// 過濾掉無效零件 (No/無)
					if (fName.contains("No(無)"))
						continue;

					// 清理規格文字中的 JSON 符號 [ ] "
					String cleanSpec = fName.replaceAll("[\\[\\]\"]", "").replace(",", " ");

					sb.append("| ").append(gName).append(" | ").append(cleanSpec).append(" | ").append(qty)
							.append(" | ").append(process).append(" | ").append(level).append(" |\n");
				}
			}
		} catch (Exception e) {
			log.error("BOM JSON 解析失敗: {}", e.getMessage());
			sb.append("\n⚠️ *此產品的詳細 BOM 格式解析異常，請洽系統管理員。*");
		}

		return sb.toString();
	}

	/**
	 * 意圖解析 Prompt：引導 AI 將問題拆解為 DAO 所需的參數
	 */
	public String extractProductIntent(String userQuestion) {
		return """
				你現在是 DTR 產品管理系統的『規格搜尋解析引擎』。
				請從使用者的問題中提取搜尋參數，並回傳一個「純 JSON 對象」，禁止任何解釋。

				# 欄位定義與規範：
				1. bpmnb (String): 產品品號。通常以 '90-' 開頭。若無則填 null。
				2. bpmmodel (String): 整機型號。指機器的名字 (如 DT135, 313TY)。若無則填 null。
				3. specKeywords (Array of String): 規格關鍵字陣列。
				   - 包含：品牌(Intel/Samsung)、零件名(CPU/SSD/記憶體)、規格參數(8G/N250/黑色)。
				   - ⚠️ 即使只有一個關鍵字，也必須放在陣列中，例如 ["Intel"]。
				   - 若提及零件，請同時加入中英文同義詞。例如提及「記憶體」，可存入 ["RAM"]。
				   - 若無關鍵字則填 []。
				4. excludeKeywords: [必須排除的規格陣列]。 (關鍵字如：不要、除了、排除、無)
				   - 若無關鍵字則填 []。

				# 解析準則：
				- CPU、RAM、SSD、品牌名稱一律放入 specKeywords 陣列中。
				- 為了提升 SQL 搜尋成功率，請將複合詞拆散。例如「8G記憶體」拆為 ["8G", "記憶體"]。

				# 輸出 JSON 結構樣板：
				{
				  "bpmnb": null,
				  "bpmmodel": null,
				  "specKeywords": [],
				  "excludeKeywords": []
				}

				# 範例測試：
				問題：「我要 8G 記憶體且 CPU 為 N250 的產品」
				回傳：{"bpmnb": null, "bpmmodel": null, "specKeywords": ["8G", "記憶體", "N250", "CPU"], "excludeKeywords": []}

				問題：「幫我查 90-320 這台」
				回傳：{"bpmnb": "90-320", "bpmmodel": null, "specKeywords": [], "excludeKeywords": []}

	            問題："我要 8G 記憶體，但不要 Samsung 的"
	            回傳：{"bpmnb": null, "bpmmodel": null,"specKeywords": ["8G"], "excludeKeywords": ["Samsung"]}

				使用者問題： %s
				"""
				.formatted(userQuestion);
	}

	/**
	 * 💡 輔助方法：清洗字串以符合 Markdown 表格規範 1. 將換行符號替換為空格 2. 轉義 | 符號防止表格欄位錯位
	 */
	private String cleanMarkdownCell(String input) {
		if (input == null || input.trim().isEmpty()) {
			return "-";
		}
		return input.replaceAll("[\\r\\n]+", " ") // 將所有換行變為空格
				.replace("|", "\\|") // 轉義 Pipe 符號
				.trim();
	}
}