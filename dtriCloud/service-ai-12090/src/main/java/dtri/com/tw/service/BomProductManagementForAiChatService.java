package dtri.com.tw.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
		// 是否有排除?
		String[] excludeKeywords = null;
		if (intent.has("excludeKeywords") && intent.get("excludeKeywords").isJsonArray()) {
			JsonArray ja = intent.get("excludeKeywords").getAsJsonArray();
			excludeKeywords = new String[ja.size()];
			for (int i = 0; i < ja.size(); i++) {
				excludeKeywords[i] = "%" + ja.get(i).getAsString() + "%";
			}
		}
		String[] excludeNewKeywords = excludeKeywords;

		// 2. 呼叫你寫好的 DAO 進行模糊查詢 (取前 300 筆)
		PageRequest pageable = PageRequest.of(0, 300, Sort.by(Direction.ASC, "bpm_nb"));
		ArrayList<BomProductManagement> entitys = bomDao.findAllByAdvancedSearch(bpmnb, bpmmodel, keywordArray, null,
				pageable);

		// --- 再次檢查：排除邏輯與數量(bisqty)檢查 ---
		if (entitys != null && !entitys.isEmpty()) {
			// 使用 Iterator 安全地移除元素，或使用 Stream 過濾
			entitys.removeIf(e -> {
				// 假設實體中有一個 getBpmbisitem() 或類似的方法取得那段長 JSON
				String rawJson = e.getBpmbisitem();
				if (rawJson == null || rawJson.isEmpty())
					return true; // 沒資料就淘汰

				try {
					JsonObject bomData = JsonParser.parseString(rawJson).getAsJsonObject();
					JsonArray items = bomData.getAsJsonArray("items");

					// 1. 處理排除邏輯 (Exclude Keywords)
					if (excludeNewKeywords != null && excludeNewKeywords.length > 0) {
						for (String ex : excludeNewKeywords) {
							String cleanEx = ex.replace("%", "").toLowerCase(); // 轉小寫進行不感性比對

							for (JsonElement itemElem : items) {
								JsonObject item = itemElem.getAsJsonObject();
								String bisgname = item.get("bisgname").getAsString().toLowerCase();
								String bisfname = item.get("bisfname").getAsString().toLowerCase();

								// 💡 關鍵邏輯：
								// 如果 項目組名稱 或 規格名稱 匹配到了「排除關鍵字」
								if (bisgname.contains(cleanEx) || bisfname.contains(cleanEx)) {
									// 檢查其數量
									int qty = item.get("bisqty").getAsInt();

									// 只有當數量 > 0 時，才符合「排除條件」(因為真的裝了)
									if (qty > 0) {
										log.info("[APDM] 淘汰品號 {}: 含有排除關鍵字 {} 且裝配數量為 {}", e.getBpmnb(), cleanEx, qty);
										return true; // 符合排除條件，淘汰此機台
									} else {
										log.info("[APDM] 保留品號 {}: 雖含有關鍵字 {} 但裝配數量為 0", e.getBpmnb(), cleanEx);
									}
								}
							}
						}
					}

					// 2. 處理包含與數量邏輯 (Inclusion & bisqty >= 1)
					if (intent.has("specKeywords")) {
						JsonArray specKws = intent.getAsJsonArray("specKeywords");
						for (JsonElement kwElem : specKws) {
							String kw = kwElem.getAsString();
							boolean isMatchAndActive = false;

							// 檢查 items 陣列中，該關鍵字對應的項目數量是否 >= 1
							for (JsonElement itemElem : items) {
								JsonObject item = itemElem.getAsJsonObject();
								String bisgname = item.get("bisgname").getAsString();
								String bisfname = item.get("bisfname").getAsString();

								// 檢查該項目是否包含關鍵字 (不論是在組名或規格名)
								if (bisgname.contains(kw) || bisfname.contains(kw)) {
									// 關鍵字匹配到了，檢查數量
									int qty = item.get("bisqty").getAsInt();
									if (qty >= 1) {
										isMatchAndActive = true;
										break;
									}
								}
							}

							// 如果其中一個關鍵字在所有 items 中都找不到 (或數量皆為 0)，則淘汰此機台
							if (!isMatchAndActive) {
								log.info("[APDM] 淘汰品號 {}: 關鍵字 {} 數量不足(bisqty < 1)", e.getBpmnb(), kw);
								return true;
							}
						}
					}

					return false; // 通過檢查，保留

				} catch (Exception ex) {
					log.error("[APDM] 解析品號 {} JSON 失敗: {}", e.getBpmnb(), ex.getMessage());
					return true; // 解析失敗也淘汰，確保資料正確
				}
			});
		}

		if (entitys == null || entitys.isEmpty()) {
			return "⚠️ 在規格資料庫中找不到與 『" + userQuestion + "』 相關的產品資訊。\n\n" + intent;
		}

		// 3. 根據結果筆數決定回傳格式
		StringBuilder sb = new StringBuilder();
		if (entitys.size() == 1) {
			// 只有一筆：顯示超詳細 BOM 表格
			sb.append(formatSingleBomDetailed(entitys.get(0)));
		} else {
			// 多筆：先顯示清單供使用者選擇
			sb.append("📋 為您找到以下相關產品，共" + entitys.size() + "台，請問您想查看哪一台的詳細規格？\n\n" + intent + "\n\n");
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
				你現在是 DTR 產品管理系統的『規格解析核心』。
				      任務：將使用者的問題拆解為 JSON 搜尋參數。
				      回傳格式：純 JSON 對象，禁止任何解釋。

				      # 📋 1. 術語對齊與互斥原則 (Strict Logic)
				      1. 關鍵字轉換 (術語對齊)：
				         - [核心運算]: 「處理器 / 核心」-> "CPU"; 「記憶體 / 內存」-> "RAM"; 「主機板」-> "PCBA"; 「顯卡」-> "GPU Card"
				         - [儲存設備]: 「硬碟 / SSD / 儲存」-> "Storage"; 「第二顆硬碟」-> "Storage 2nd"; 「快拆硬碟 / 快拆SSD」-> "快拆SSD"
				         - [顯示觸控]: 「螢幕 / 面板」-> "LCD"; 「觸控」-> "Touch"; 「觸控筆」-> "Stylus Pen"; 「防窺片」-> "防窺片"
				         - [通訊定位]: 「網路 / WIFI」-> "WIFIBT"; 「4G / 5G / SIM卡」-> "4G/5G Module"; 「GPS / 定位」-> "GPS"
				         - [影像掃描]: 「後相機 / 後鏡頭」-> "BackCamera"; 「前相機 / 前鏡頭」-> "FrontCamera"; 「掃描器 / 條碼」-> "Scanner"; 「RFID」-> "RFID"
				         - [電源感測]: 「電池」-> "Battery"; 「備援電池 / 小電池」-> "BackupBattery"; 「變壓器 / 充電器」-> "Adaptor"; 「電源線」-> "Power Cord"
				         - [環境感測]: 「重力感應」-> "G-SENSOR"; 「亮度感應」-> "Light Sensor"; 「霍爾感應」-> "HallSensor"; 「電子羅盤」-> "E-Compass"
				         - [其他配件]: 「支架」-> "KickStand"; 「認證貼紙」-> "FCC Label"; 「網路供電」-> "POE"
				      2. 🚫 **絕對互斥規則**：
				         - 被歸類到 `excludeKeywords` 的詞（及其對齊後的術語），**嚴禁**出現在 `specKeywords` 中。
				         - 若使用者說「不要 SSD」，則 "Storage" 和 "SSD" 只能出現在 `excludeKeywords`。

				      # ⚙️ 2. 欄位解析準則：
				      1. bpmnb: 產品品號 (90- 開頭)。
				      2. bpmmodel: 產品型號 (如 MA1352-4K, DT135)。
				      3. specKeywords: [使用者「想要」或「指定要」的關鍵字]。
				         - 複合詞拆散，例如「8G RAM」拆為 ["8G", "RAM"]。
				      4. excludeKeywords: [使用者明確表示「不要、排除、無、No」的關鍵字]。
				         - 包含對應的術語。例如「不要 SSD」應填入 ["Storage", "SSD"]。

				      # 💎 3. 輸出範例：
				      問題：「我要 8G 記憶體」
				      回傳：{"bpmnb": null, "bpmmodel": null, "specKeywords": ["8G", "RAM"], "excludeKeywords": []}

				      問題：「MA1352-4K 不要 512GB SSD」
				      回傳：{"bpmnb": null, "bpmmodel": "MA1352-4K", "specKeywords": [], "excludeKeywords": ["Storage", "SSD", "512GB"]}

				      問題：「找 90-320 不要 Samsung 的產品」
				      回傳：{"bpmnb": "90-320", "bpmmodel": null, "specKeywords": [], "excludeKeywords": ["Samsung"]}

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