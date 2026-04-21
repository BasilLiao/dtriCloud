package dtri.com.tw.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import dtri.com.tw.service.AiDtrService.VoiceLang;

@Service
public class BomProductForAiChatService {

	private static final Logger log = LoggerFactory.getLogger(BomProductForAiChatService.class);

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
				keywordArray[i] = "%" + ja.get(i).getAsJsonArray().get(0).getAsString() + "%";
			}

		}

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

				// 是否有排除?
				try {
					JsonObject bomData = JsonParser.parseString(rawJson).getAsJsonObject();
					JsonArray items = bomData.getAsJsonArray("items");

					// 1. 處理排除邏輯 (支援單一詞與組合詞)
					if (intent.has("excludeKeywords") && intent.get("excludeKeywords").isJsonArray()) {
						JsonArray excludeGroups = intent.getAsJsonArray("excludeKeywords");

						for (JsonElement groupElem : excludeGroups) {
							// 取得一個排除小組，例如 ["RAM"] 或 ["16G", "RAM"]
							JsonArray group = groupElem.getAsJsonArray();

							for (JsonElement itemElem : items) {
								JsonObject item = itemElem.getAsJsonObject();
								String nameStr = (item.get("bisgname").getAsString() + " "
										+ item.get("bisfname").getAsString()).toLowerCase();

								// 💡 判斷邏輯：這個零件是否「完全包含」這個排除小組裡的所有關鍵字？
								boolean isMatchThisGroup = true;
								for (JsonElement kwElem : group) {
									String kw = kwElem.getAsString().toLowerCase();
									if (!nameStr.contains(kw)) {
										isMatchThisGroup = false; // 只要有一部分不符合，就不算「命中」這個排除組
										break;
									}
								}

								// 如果命中排除組合 (例如：這是 RAM 且含有 16G) 且數量 > 0
								if (isMatchThisGroup) {
									int qty = item.get("bisqty").getAsInt();
									if (qty > 0) {
										System.out.println("[APDM] 淘汰品號 {}: 命中排除組合 {}" + e.getBpmnb() + ":" + group);
										log.info("[APDM] 淘汰品號 {}: 命中排除組合 {}", e.getBpmnb(), group);
										return true; // 發現要排除的東西，淘汰整台機台
									}
								}
							}
						}
					}

					// 2. 處理包含與數量邏輯 (Inclusion & bisqty >= 1)
					if (intent.has("specKeywords")) {
						JsonArray specKws = intent.getAsJsonArray("specKeywords");
						for (JsonElement kwElem : specKws) {
							// 💡 修正 1：將 AI 傳入的關鍵字轉為小寫
							String bisgnamekw = kwElem.getAsJsonArray().get(0).getAsString().toLowerCase();// 檢查群組名稱
							String bisfnamekw = "";// 檢查群組內容
							if (kwElem.getAsJsonArray().size() > 1) {
								bisfnamekw = kwElem.getAsJsonArray().get(1).getAsString().toLowerCase();
							}

							boolean isMatchAndActive = false;

							// 檢查 items 陣列中，該關鍵字對應的項目數量是否 >= 1
							for (JsonElement itemElem : items) {
								JsonObject item = itemElem.getAsJsonObject();

								// 💡 修正 2：將資料庫內的組名與規格名也轉為小寫再比對
								String bisgname = item.get("bisgname").getAsString().toLowerCase();
								String bisfname = item.get("bisfname").getAsString().toLowerCase();

								// 執行不分大小寫的包含檢查
								if (bisgname.contains(bisgnamekw)
										&& (bisfnamekw.equals("") || bisfname.contains(bisfnamekw))) {
									// 關鍵字匹配到了，檢查數量
									// 💡 修正 3：確保 bisqty 轉型安全
									int qty = 0;
									try {
										qty = item.get("bisqty").getAsInt();
									} catch (Exception ex) {
										// 如果 bisqty 是字串格式 "1"
										qty = Integer.parseInt(item.get("bisqty").getAsString());
									}

									if (qty >= 1) {
										isMatchAndActive = true;
										break;
									}
								}
							}

							// 如果其中一個關鍵字在所有 items 中都找不到 (或數量皆為 0)，則淘汰此機台
							if (!isMatchAndActive) {
								log.info("[APDM] 淘汰品號 {}: 關鍵字 {} 數量不足(bisqty < 1) 或大小寫不匹配", e.getBpmnb(),
										bisgnamekw + ":" + bisfnamekw);
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
			return "⚠️ 在規格資料庫中找不到與 『" + userQuestion + "』 相關的產品資訊。 \n\n"//
					+ "(No relevant product information can be found in the specifications database.)。\n\n"//
					+ "系統條件 : " + intent + "\n\n";
		}

		// 3. 根據結果筆數決定回傳格式
		StringBuilder sb = new StringBuilder();
		if (entitys.size() == 1) {
			// 只有一筆：顯示超詳細 BOM 表格
			sb.append(formatSingleBomDetailed(entitys.get(0), intent));
		} else {
			// 多筆：先顯示清單供使用者選擇
			sb.append("📋 為您找到以下相關產品，共『" + entitys.size() + "』台，請問您想查看哪一台的詳細規格？\n\n");
			sb.append("We found the following related products, totaling 『" + entitys.size()
					+ "』 units. Which unit would you like to view its detailed specifications? \n\n" //
					+ "系統條件 : " + intent + " \n\n");
			sb.append("| 產品品號 (BOM NB) | 型號 (Model) | 產品描述 (Context) | 產品類型(Type) |\n");
			sb.append("|:---|:---|:---|:---|\n");
			for (BomProductManagement item : entitys) {
				sb.append("| ")//
						.append(item.getBpmnb()).append(" | ")//
						.append(item.getBpmmodel()).append(" | ")//
						.append(cleanMarkdownCell(item.getSysnote())).append(" | ")//
						.append(item.getBpmtypename()).append(" |\n");
			}
			sb.append("\n💡 *提示：您可以直接輸入品號來查看詳細 BOM 內容。* \n\n");
			sb.append(
					"\n💡 *Tip: You can directly enter the item number to view the detailed BOM (Bill of Materials)。*");
		}
		return sb.toString();
	}

	/**
	 * 將單一筆 BOM 轉換為詳細的 Markdown 表格 (解析 bpmbisitem)
	 */
	private String formatSingleBomDetailed(BomProductManagement bom, JsonObject intent) {
		StringBuilder sb = new StringBuilder();
		sb.append("## 🔍 產品規格詳情(Info): ").append("\n");
		sb.append("- **型號(model)**: `").append(bom.getBpmmodel()).append("`\n");
		sb.append("- **品號(P/N)**: `").append(bom.getBpmnb()).append("`\n");
		sb.append("- **類別(Type)**: ").append(bom.getBpmtypename()).append("\n");
		sb.append("- **備註(Note)**: ").append(bom.getSysnote().isEmpty() ? "-" : bom.getSysnote()).append("\n\n");
		sb.append("- 🔍 系統條件(system conditions): ").append(intent).append("\n");
		sb.append("### 🛠️ 硬體配置清單 (BOM Items)\n");
		sb.append("| 類別 (Group) | 規格內容 (Specification) | 數量(Qty) | 製程(P) | 階層(L) |\n");
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
				        ⚠️ **核心指令：精準比對與縮寫防誤判**
				         - **縮寫隔離**：嚴禁將與清單術語相似但意義不同的縮寫進行轉換。
				         - **範例：POAG 是一種接地端子，絕對不等於 POE (網路供電)，必須保留原字 "POAG"。**
				         - **相似詞處理**：僅針對語意明確相同者（內存=RAM）進行轉換。
				         - **原字保留**：若詞彙不屬於下方硬體類別（如：軍規、防水、POAG、顏色、品牌），必須「完全保留原文字」。

				        ⚠️ **同音字與語音容錯 (Phonetic Correction)**
				         - **自動修正**：使用者輸入之關鍵字若為「同音異字」或「拼音相似」，請依據生產管理語境自動校正。
				         - **範例**：
				         - 「公單」、「功單」、「供單」 -> **一律視為「工單標籤」並丟棄**。
				         - 「內存」 -> 「RAM」。
				         - **識別優先**：若字串符合 A-XXXX 或 90-XXXX 格式，無論前方接的是「公單」還是「工單」，皆判定該字串為 `sinb` 或 `bpmnb`，且將該標籤詞（公單/工單）直接從 `specKeywords` 中剔除。
				         - [單位識別原則]: 
                         - 若數字後接「"」或「吋」，一律對齊至 "LCD"。
                         - 範例：「7"」、「10.1吋」、「12吋」 -> 分別對應 ["LCD", "7吋"], ["LCD", "10.1吋"]。
				         - **範例**：["LCD", "7吋"] 是正確的；["LCD", "7""] 是錯誤的。

				      1. 關鍵字轉換 (術語對齊)：
				         - [核心運算]: 「處理器 / 核心」-> "CPU"; 「記憶體 / 內存」-> "RAM"; 「主機板」-> "PCBA"; 「子卡 / 擴充板」-> "IO PCBA"; 「安全晶片 / 加密模組」-> "TPM"; 「顯卡 / 繪圖卡」-> "GPU Card"
				         - [儲存設備]: 「硬碟 / SSD / 儲存」-> "Storage"; 「第二顆硬碟」-> "Storage 2nd"; 「快拆硬碟 / 快拆SSD」-> "快拆SSD"; 「第二個快拆SSD」-> "快拆SSD 2nd"; 「SD卡 / 記憶卡」-> "SDCard"
				         - [顯示觸控]: 「螢幕 / 面板」-> "LCD"; 「觸控」-> "Touch"; 「觸控筆」-> "Stylus Pen"; 「防窺片」-> "防窺片"; 「顯示接口」-> "Displayport"; 「閱讀燈」-> "ReadingLight"; 「鍵盤」-> "Keyboard"
				         - [通訊定位]: 「網路 / WIFI / 藍牙」-> "WIFIBT"; 「藍牙位址 / BT位址」-> "BT MAC"; 「4G / 5G / SIM卡」-> "4G/5G Module"; 「虛擬SIM / eSIM」-> "eSIM"; 「GPS / 定位」-> "GPS"; 「天線引出 / 天線接口」-> "RF pass-through"
				         - [影像掃描]: 「後相機 / 後鏡頭」-> "BackCamera"; 「前相機 / 前鏡頭」-> "FrontCamera"; 「掃描器 / 條碼孔」-> "Scanner"; 「刷卡器 / 磁條機」-> "MSR/EMV"; 「智慧卡 / IC卡」-> "Smart Card"; 「RFID / NFC / 近場通訊」-> "NFC"
				         - [電源系統]: 「電池」-> "Battery"; 「備援電池 / 小電池」-> "BackupBattery"; 「電池座充」-> "Battery Changer"; 「變壓器 / 充電器」-> "Adaptor"; 「電源線」-> "Power Cord"
				         - [環境感測]: 「重力感應」-> "G-SENSOR"; 「亮度感應」-> "Light Sensor"; 「距離感應 / P-Sensor」-> "P-Sensor"; 「霍爾感應 / 合蓋感應」-> "HallSensor"; 「電池霍爾感應」-> "Battery Hall Sensor"; 「電子羅盤」-> "E-Compass"
				         - [特殊介面]: 「軍規接頭 / Bernier」-> "Bernier CONN"; 「費雪接頭 / Fischer」-> "周邊-Fischer Connector"; 「Glenair接頭」-> "Glenair Connector"; 「接地端子 / POAG」-> "POAG"; 「頂針連接 / POGO」-> "POGO"; 「網路供電 / POE」-> "POE"
				         - [周邊介面]: 「USB2.0」-> "周邊-USB2.0"; 「USB3.0」-> "周邊-USB3.0"; 「TypeC接口」-> "周邊-TypeC"; 「TypeC充電口」-> "周邊-TypeC Power in"; 「網口 / LAN」-> "周邊-LAN"; 「麥克風 / Mic」-> "周邊-Mic"; 「喇叭 / Speak」-> "周邊-Speak"; 「光纖 / SFP」 -> "周邊-SFP光纖模組"; 「耳機孔 / Headset」-> "周邊-Headset"; 「HDMI」-> "周邊-HDMI"; 「串口 / RS232」-> "周邊-RS232"; 「DC輸入」-> "周邊-DC-IN"; 「DC輸出」-> "周邊-DC-OUT"
				         - [機構外觀]: 「顏色」-> "Color"; 「手把 / 提把」-> "周邊-HANDLE"; 「手帶 / 腕帶」-> "周邊-HANDSTRAP"; 「支架」-> "KickStand"; 「鎖孔 / Lock」-> "周邊-Lock"; 「散熱墊 / 導熱墊」-> "Heat-Pad"; 「導熱矽膠」-> "導熱硅膠"; 「噴漆 / 塗層」-> "Coating"; 「標誌 / Logo」-> "Logo"
				         - [認證標籤]: 「大陸認證」-> "CCC Label"; 「美國認證」-> "FCC Label"; 「認證標籤 / 合規標籤」-> "FCC & CCC Label"; 「能源標籤」-> "Energy Label"				      
				      
				      2. 🚫 **絕對互斥規則**：
				         - 被歸類到 `excludeKeywords` 的詞（及其對齊後的術語），**嚴禁**出現在 `specKeywords` 中。
				         - 若使用者說「不要 SSD」，則 "Storage" 和 "SSD" 只能出現在 `excludeKeywords`。
				         - 每個子陣列代表一個「不可出現的組合」。
				         - 範例 A (單一排除)：「不要 RAM」 -> [ ["RAM"] ]
				         - 範例 B (組合排除)：「不要 16G RAM」 -> [ ["16G", "RAM"] ]
				         - 範例 C (多項排除)：「不要 16G RAM 且不要 SSD」 -> [ ["16G", "RAM"], ["Storage"] ]

				      # ⚙️ 2. 欄位解析準則：
				      1. bpmnb: 產品品號 (90- 開頭)。
				      2. bpmmodel: 產品型號/系列 (如 MA1352-4K, DT135)。
				         - ⚠️ **核心要求**：請自動剔除「描述性填充詞」。
				         - 填充詞清單：series, model, version, unit, 系列, 型號, 版本, 台, 個。
				         - 範例：「504 series」只提取 "504"；「MA1352 model」只提取 "MA1352"。
				      3. specKeywords: [使用者「想要」或「指定要」的關鍵字]。
				         - 複合詞拆散，例如「8G RAM」拆為 ["8G", "RAM"]。
				      4. excludeKeywords: [使用者明確表示「不要、排除、無、No」的關鍵字]。
				         - 包含對應的術語。例如「不要 SSD」應填入 ["Storage", "SSD"]。

				      # 💎 3. 輸出範例：
				      問題：「我要 8G 記憶體」
				      回傳：{"bpmnb": null, "bpmmodel": null, "specKeywords": [["RAM", "8G"]], "excludeKeywords": []}

				      問題：「我要 5g 產品」
				      回傳：{"bpmnb": null, "bpmmodel": null, "specKeywords": [["4G/5G Module", "5G"]], "excludeKeywords": []}

				      問題：「MA1352-4K 不要 512GB SSD」
				      回傳：{"bpmnb": null, "bpmmodel": "MA1352-4K", "specKeywords": [], "excludeKeywords": [["Storage", "512GB"]]}

				      問題：「找 90-320 不要 Samsung 的產品」
				      回傳：{"bpmnb": "90-320", "bpmmodel": null, "specKeywords": [], "excludeKeywords": [["Samsung"]]}

				      問題：「不要 RAM，也不要 SSD」
				      回傳：{"bpmnb": null, "bpmmodel": null, "specKeywords": [], "excludeKeywords": [ ["RAM"], ["Storage"] ]}

				      問題：「I need a 504 series with i7, but no 16G RAM.」
				      回傳：{"bpmnb": null, "bpmmodel": "504", "specKeywords": [["CPU", "i7"]], "excludeKeywords": [ ["RAM", "16G"] ]}

				      問題：「我要找有攝像頭跟 16G 內存的 504 series」
				      回傳：{"bpmnb": null, "bpmmodel": "504", "specKeywords": [["FrontCamera"], ["RAM", "16G"]], "excludeKeywords": []}
				      (解釋：內存 -> RAM, 攝像頭 -> FrontCamera)
				      
				      問題：「我要找有攝像頭跟 7" 內存的 504 series」
				      回傳：{"bpmnb": null, "bpmmodel": "504", "specKeywords": [["FrontCamera"], ["LCD", "7吋"]], "excludeKeywords": []}
				      (解釋：7吋 -> LCD, 攝像頭 -> FrontCamera)

				      問題：「MA1352-4K，要粉藍色外殼，且必須有軍規防震技術」
				      回傳：{"bpmnb": null, "bpmmodel": "MA1352-4K", "specKeywords": [["粉藍色外殼"], ["軍規防震技術"]], "excludeKeywords": []}
				      (解釋：顏色特徵與軍規技術不在清單內，故「完全保留原文字」)

				      問題：「幫我找 DT301，要用 Samsung 的硬碟，但不要 Intel 的 CPU」
				      回傳：{"bpmnb": null, "bpmmodel": "DT301", "specKeywords": [["Storage", "Samsung"]], "excludeKeywords": [["Intel", "CPU"]]}
				      (解釋：Samsung 不在清單則保留；Intel CPU 是組合排除)

				      問題：「I need a 504 series with i7, but no 1TB SSD or POAG.」
				      回傳：{"bpmnb": null, "bpmmodel": "504", "specKeywords": [["CPU", "i7"]], "excludeKeywords": [["Storage", "1TB"], ["POAG"]]}
				      (解釋：POAG 不在清單中，故完整保留原字，不可轉為 POE)

				      問題：「我要一台有防水接頭的機器，且不需要 PoE 功能」
				      回傳：{"bpmnb": null, "bpmmodel": null, "specKeywords": [["防水接頭"]], "excludeKeywords": [["POE"]]}
				      (解釋：PoE 正確轉換為標籤 POE)

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

	/**
	 * 🧠 產品專家模式：深度解析產品規格與特性敘述 * @param scheduleData 原始 Markdown 排程表格
	 * 
	 * @param userQuestion 使用者問題
	 * @param targetLang   目標語系
	 */
	public String getProductExpertAdvice(String scheduleData, String userQuestion, VoiceLang targetLang) {
		log.info("[大腦] 執行產品專家模式 -> 規格解析與敘述生成...");

		String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String langName = switch (targetLang) {
		case ZH_TW -> "台灣繁體中文 (Traditional Chinese)";
		case EN_US -> "Natural English (Professional)";
		case VI_VN -> "Vietnamese (Industrial Standard)";
		};

		String productPrompt = """
				[System Role]
				你是一位擁有 20 年經驗的『高級產品解決方案架構師 (Product Solutions Architect)』，精通工業電腦 (IPC) 與電子產品硬體規格。

				[Current Context]
				當前系統日期時間為：%s。

				[Analysis Task]
				1. 🚀【產品規格解碼】：
				   - 請深度解析「產品品名 (Product Name)」與「產品品號」。
				   - 將縮寫轉化為專業描述。範例："i7" -> "搭載高效能 Intel Core i7 處理器"、"16G" -> "配備 16GB 高速記憶體"、"NoCam" -> "無鏡頭隱私版本"。
				   - 識別產品系列（如 DT 系列可能代表平板或桌上型電腦）。

				2. 🚀【版本與特性分析】：
				   - 關注「產品品名」中的 Version (如 R1.1, R1.2)。
				   - 若「生管備註」中提到 ECN 或 OS (如 *No OS, Win 10)，請將其納入產品特性說明。
				   - 說明該版本的主要硬體組成與其應用優勢。

				3. 🚀【專業敘述生成】：
				   - 請不要只列出清單，要用「專業導覽員」的語氣，將規格串聯成一段流暢的產品介紹。
				   - 適合語音播報，節奏感強，重點突出。

				4. 🚀【排除邏輯】：
				   - **不要**過度分析生產進度、數量或延遲（那是生管專家的事）。
				   - 專注於「這是什麼產品」、「它有什麼規格」、「它的版本特點」。

				[Output Rules]
				- **強制語言**：請完全使用『%s』輸出。
				- **限制**：字數 300-500 字，移除 Markdown 符號（如 *, #, |, -）。

				[Output Example]
				{
				  "aIsuggestion": "這款產品是我們專為工業環境設計的 D T 系列平板，型號為 3 1 3 T T。硬體核心採用了強力運作的 i 7 處理器，並搭配 1 6 G 記憶體，能確保在多工環境下穩定運行..."
				}

				[User Question]
				%s

				[Schedule Data]
				%s
				"""
				.formatted(currentDateTime, langName, userQuestion, scheduleData);

		try {
			return productPrompt;
		} catch (Exception e) {
			log.error("[大腦] 產品專家分析失敗: {}", e.getMessage());
			return "{\"aIsuggestion\": \"Product Analysis Failed.\"}";
		}
	}
}