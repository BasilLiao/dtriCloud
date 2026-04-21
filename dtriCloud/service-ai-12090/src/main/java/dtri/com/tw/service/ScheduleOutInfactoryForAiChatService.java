package dtri.com.tw.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import dtri.com.tw.pgsql.dao.ScheduleOutsourcerDao;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
import dtri.com.tw.service.AiDtrService.VoiceLang;

@Service
public class ScheduleOutInfactoryForAiChatService {

	@Autowired
	private ScheduleInfactoryDao infactoryDao;

	@Autowired
	private ScheduleOutsourcerDao outsourcerDao;

	// 在類別中定義一個共用的 ObjectMapper (節省資源)
	private final ObjectMapper mapper = new ObjectMapper();
	private static final Logger log = LoggerFactory.getLogger(ScheduleOutInfactoryForAiChatService.class);

	/**
	 * @param jsonIntent     解析意圖?
	 * @param currentSession 對話串流
	 * @param packageBean    涵蓋使用者
	 * @param finalContent   使用者問題
	 * @return 涵蓋 相關單位問題+資料庫查詢+優化資料
	 */
	public String getScheduleInfactoryForAi(String jsonIntent, String finalContent) {

		// 2. 解析 JSON (使用 Gson 或 Jackson)
		JsonObject intent = JsonParser.parseString(jsonIntent).getAsJsonObject();

		String sistatus = intent.get("status").isJsonNull() ? null : intent.get("status").getAsString();
		String sinb = intent.get("sinb").isJsonNull() ? null : intent.get("sinb").getAsString();
		String sipnb = intent.get("sipnb").isJsonNull() ? null : intent.get("sipnb").getAsString();
		String sipname = intent.get("sipname").isJsonNull() ? null : intent.get("sipname").getAsString();
		String simcnote = intent.get("simcnote").isJsonNull() ? null : intent.get("simcnote").getAsString();// 物控
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
		PageRequest pageableIn = PageRequest.of(0, 500, Sort.by(Direction.ASC, "sinb"));
		PageRequest pageableOut = PageRequest.of(0, 500, Sort.by(Direction.ASC, "sonb"));
		ArrayList<ScheduleInfactory> entitysIn = infactoryDao.findAllBySearch(sinb, sipnb, sipname, null, sistatus,
				null, null, null, simcdates, simcdatee, simcnote, simcstatusInt, 0, sicorder, pageableIn);

		ArrayList<ScheduleOutsourcer> entitysOut = outsourcerDao.findAllBySearch(sinb, sipnb, sipname, null, sistatus,
				null, null, null, simcdates, simcdatee, simcnote, simcstatusInt, 0, pageableOut);

		// Step 3: 如果沒查到資料
		if ((entitysIn == null || entitysIn.isEmpty()) && (entitysOut == null || entitysOut.isEmpty())) {
			return "";
		} else {

			// Step 4: 開始分段組合內容
			StringBuilder finalResponse = new StringBuilder();

			// 4-1. 頂部總結
			int totalCount = entitysIn.size() + entitysOut.size();

			finalResponse.append(String.format("### 📊 查詢總結\n目前總共為您找到 『%d』 筆相關排程資料。\n\n", totalCount));
			// 使用 🔍 (放大鏡) 或 📋 (剪貼簿) 圖示
			finalResponse.append(String.format("<sub>⚙️ 系統解析意圖：『%s』</sub>\n\n", intent.toString()));

			// 判斷是否有資料
			// 4-2. 【廠內生產區段】
			if (entitysIn != null && !entitysIn.isEmpty()) {
				finalResponse.append("### 🏭 廠內生產排程 (In-factory)\n");
				finalResponse.append(String.format("找到 %d 筆廠內資料：\n\n", entitysIn.size()));
				finalResponse.append(convertInfactoryToContext(entitysIn));
				finalResponse.append("\n\n---\n\n"); // 視覺分割線
			}

			// 4-3. 【委外代工區段】
			if (entitysOut != null && !entitysOut.isEmpty()) {
				finalResponse.append("### 🚚 委外代工排程 (Outsourced)\n");
				finalResponse.append(String.format("找到 %d 筆委外資料：\n\n", entitysOut.size()));
				finalResponse.append(convertOutsourcerToContext(entitysOut));
				finalResponse.append("\n\n");
			}
			return finalResponse.toString();
		}
	}

	/** 將 N 筆排程轉為 Markdown (修復格式與優化閱讀) */
	private String convertInfactoryToContext(List<ScheduleInfactory> entitys) {
		if (entitys == null || entitys.isEmpty())
			return "查無排程資料。";

		StringBuilder sb = new StringBuilder();
		// 1. 建立標題
		String manufTitle = "<img width='250' height='0' src='data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7' />";
		sb.append("|製令單|產品品號|產品品名|單狀態|預計生產|總進度|預計完工日|生管備註|製造生產(已過站數)|物控備註|料狀況|訂單號|預計齊料日\n");
		// 2. 建立分隔線 (必須也是 9 欄，否則表格會破裂)
		// :--- 代表靠左，:---: 代表置中
		sb.append("|:---|:---|:---|:---:|:---:|:---:|:---|:---|:---:|:---:|:---|:---|:---:|\n");

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
			String siscnote = cleanNote(getLatestNoteContent(item.getSiscnote()));// 6. 生管備註
			String simpnote = cleanNote(getLatestNoteContent(item.getSimpnote()));// 7. 製造備註
			String simcnote = cleanNote(getLatestNoteContent(item.getSimcnote()));// 8. 物控備註

			// 預計齊料日處理 (避免 null)
			String mDate = (item.getSimcdate() == null) ? "-" : item.getSimcdate().toString();

			// 3. 填入數據 (確保也是 9 個 |)
			sb.append("|").append(item.getSinb()) // 1. 製令
					.append("|").append(item.getSipnb()) // 2. 品號
					.append("|").append(sipname) // 3. 品名
					.append("|").append(statusText) // 4. 狀態
					.append("|").append(item.getSirqty()) // 5. 預計生產數
					.append("|").append(item.getSiokqty()).append("/").append(item.getSirqty()) // 5. 進度
					.append("|").append(item.getSifdate()) // 10. 預計完工日
					.append("|").append(siscnote) // 6. 生管備註
					.append("|").append(simpnote) // 7. 製造備註
					.append("|").append(simcnote) // 8. 物控備註
					.append("|").append(mcStatusText) // 9. 料況
					.append("|").append(item.getSicorder() + " ") // 10. 訂單號
					.append("|").append(mDate) // 11. 預計齊料
					.append("|\n");
		}
		return sb.toString();
	}

	/** 將 N 筆排程轉為 Markdown (修復格式與優化閱讀) */
	private String convertOutsourcerToContext(List<ScheduleOutsourcer> entitys) {
		if (entitys == null || entitys.isEmpty())
			return "查無排程資料。";

		StringBuilder sb = new StringBuilder();
		// 1. 建立標題
		String manufTitle = "<img width='250' height='0' src='data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7' />";
		sb.append("|製令單|產品品號|產品品名|單狀態|預計生產|總進度|預計完工日|生管備註|製造生產(已過站數)|物控備註|料狀況|訂單號|預計齊料日\n");
		// 2. 建立分隔線 (必須也是 9 欄，否則表格會破裂)
		// :--- 代表靠左，:---: 代表置中
		sb.append("|:---|:---|:---|:---:|:---:|:---:|:---|:---|:---:|:---|:---:|:---|:---:|\n");

		for (ScheduleOutsourcer item : entitys) {
			// 品名截斷
			String sipname = item.getSopname();
			if (sipname == null)
				sipname = "-";
			sipname = sipname.length() > 30 ? sipname.substring(0, 30) : sipname;

			// 轉換生產狀態 (0=暫停, 1=未產, 2,3=生產中, Y=完工)
			String statusText = formatStatus(item.getSostatus());

			// 轉換料況狀態 (0=未確, 1=缺料, 2=齊料)
			String mcStatusText = formatMcStatus(item.getSomcstatus() + "");

			// 整合備註 (縮減 Token 寬度)
			String siscnote = cleanNote(getLatestNoteContent(item.getSoscnote()));// 6. 生管備註
			String simpnote = cleanNote(getLatestNoteContent(item.getSompnote()));// 7. 製造備註
			String simcnote = cleanNote(getLatestNoteContent(item.getSomcnote()));// 8. 物控備註

			// 預計齊料日處理 (避免 null)
			String mDate = (item.getSomcdate() == null) ? "-" : item.getSomcdate().toString();

			// 3. 填入數據 (確保也是 9 個 |)
			sb.append("|").append(item.getSonb()) // 1. 製令
					.append("|").append(item.getSopnb()) // 2. 品號
					.append("|").append(sipname) // 3. 品名
					.append("|").append(statusText) // 4. 狀態
					.append("|").append(item.getSorqty()) // 5. 總數
					.append("|").append(item.getSookqty()).append("/").append(item.getSorqty()) // 5. 進度
					.append("|").append(item.getSofdate() + " ") // 10. 預計完工日
					.append("|").append(siscnote) // 6. 生管備註
					.append("|").append(simpnote) // 7. 製造備註
					.append("|").append(simcnote) // 8. 物控備註
					.append("|").append(mcStatusText) // 9. 料況
					.append("|").append("") // 10. 訂單號
					.append("|").append(mDate) // 11. 預計齊料
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

	/**
	 * 意圖解析 Prompt：引導 AI 將「生管排程」問題拆解為 API 搜尋參數
	 */
	public String extractSearchIntent(String userQuestion) {
		// 注入當前系統日期與時間，確保 AI 能計算「下週」、「月底」
		String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		return """
				你現在是 DTR 生管系統的『生產排程意圖解析引擎』。
				任務：從使用者的問題中提取精準的查詢 JSON 物件。禁止任何解釋。
				當前系統時間為：%s (請以此為基準推算相對時間)。

				# 📋 1. 核心過濾邏輯 (重要)
				⚠️ **中性詢問原則**：
				    - 若使用者僅提到「料況」、「進度」、「狀態」、「情況」而**未指定**特定狀態（如：缺、齊、完工、待產）。
				    - 則對應的 `simcstatus` 或 `status` 必須設為 **null**。
				    - 範例：「查單號 A521 的料況」 -> `simcstatus` 設為 null (因為要看該單的所有料況資訊)。

				# 📅 2. 日期與時間處理準則 (嚴格執行)
				- **格式要求**：所有日期必須為 `YYYY-MM-DD HH:mm:ss`。
				- **區間邏輯**：
				    - 「今天」：起始 %s 00:00:00，結束 %s 23:59:59。
				    - 「...之前」(Before)：僅設定 `simcdatee` (結束時間) 為該日 23:59:59。
				    - 「...之後」(After)：僅設定 `simcdates` (起始時間) 為該日 00:00:00。
				    - 「這週」：指本週一 00:00:00 至本週日 23:59:59。
				    - 「三月份」：指 2026-03-01 00:00:00 至 2026-03-31 23:59:59。

				# ⚙️ 3. 欄位解析與對齊規則 (完整字典)
				    3-1. status (生產狀態 - 系統代碼):
				       - "1": 待產/尚未開始 (關鍵字：還沒做、尚未、待產、排隊中、掛單)。
				       - "2": 備料/撿料中/已發料 (關鍵字：準備物料、發料中、撿料、籌備、料件出庫)。
				       - "3": 已開工/生產中/在線 (關鍵字：做一半、在線上、動工、在做、加工中、進行中)。
				       - "Y": 已完工/好了 (關鍵字：完成了、結案、入庫、完工、好了)。
				       - **null**: 當詢問「進度、狀態、情況」而無具體形容詞時，保持 null。

				    3-2. simcstatus (料況狀態 - 系統代碼):
				       - 0: 未確認 (關鍵字：沒確認料況、不知道料齊沒、料況不明)。
				       - 1: 缺料/未齊/欠料 (關鍵字：料沒齊、缺件、少零件、欠料、料不夠)。
				       - 2: 已齊料/料齊了 (關鍵字：料全到了、可以開工、料齊、全部到齊)。
				       - **null**: 當詢問「料況」中性詞，或完全沒提料況時，保持 null。

				    3-3. sinb (製令單號):
				       - **識別特徵**：通常以英文字母開頭，後接年份與序號。
				       - **範例格式**：A511-26041001, A521... (嚴禁繁簡轉換或自作聰明修改英文字母)。

				    3-4. sipnb (產品品號/料號):
				       - **識別特徵**：通常為 90- 開頭的長字串。
				       - **範例格式**：90-504-001, 90-340TA... (必須完全保留原字，包含連字號)。

				    3-5. sipname (產品名稱/關鍵字):
				       - **識別特徵**：使用者提到的機型名稱、系列名稱或描述。
				       - **範例**：DT340, MA1352, 主機板, 外殼 (若不在品號格式內，通通放入此欄位)。

				    3-6. sicorder (訂單號碼/PO號):
				       - **識別特徵**：使用者提到「訂單、PO、客人單」。
				       - **範例格式**：PO-20260401, S0-123... (提取純粹的號碼部分)。

				    3-7. simcnote (備註關鍵字):
				       - **任務**：提取備註內的「行為指令」或「優先級」。
				       - **觸發字**：特急、延後、特製、先貼標、急件、補料、客人親自驗貨。

				    3-8. simcdates / simcdatee (查詢日期邊界):
				       - **simcdates (起始)**：使用者提到的時間點（或區間起點）之 00:00:00。
				       - **simcdatee (結束)**：使用者提到的時間點（或區間終點）之 23:59:59。
				       - **邏輯範例**：
				       - 「...之前」：simcdates = null, simcdatee = 指定日期 23:59:59。
				       - 「...之後」：simcdates = 指定日期 00:00:00, simcdatee = null。


				# 💎 4. 輸出範例：
				       問題：「幫我查今天有哪些待產的單子？」
				       回傳：{"status": "1", "sinb": null, "sipnb": null, "sipname": null, "simcnote": null, "simcstatus": null, "sicorder": null, "simcdates": "2026-04-10 00:00:00", "simcdatee": "2026-04-10 23:59:59"}

				       問題：「型號 DT340TA 在三月底前缺料的生產中訂單」
				       回傳：{"status": "3", "sinb": null, "sipnb": null, "sipname": "DT340TA", "simcnote": null, "simcstatus": 1, "sicorder": null, "simcdates": null, "simcdatee": "2026-03-31 23:59:59"}

				       問題：「單號 A511-26041001 有沒有備註要特急？」
				       回傳：{"status": null, "sinb": "A511-26041001", "sipnb": null, "sipname": null, "simcnote": "特急", "simcstatus": null, "sicorder": null, "simcdates": null, "simcdatee": null}

				       問題：「下週有哪些已經完工的訂單？」
				       回傳：{"status": "Y", "sinb": null, "sipnb": null, "sipname": null, "simcnote": null, "simcstatus": null, "sicorder": null, "simcdates": "2026-04-13 00:00:00", "simcdatee": "2026-04-19 23:59:59"}

				       問題：「A521-260401003料況」
				       回傳：{"status": null, "sinb": "A521-260401003", "sipnb": null, "sipname": null, "simcnote": null, "simcstatus": null, "sicorder": null, "simcdates": null, "simcdatee": null}
				       (解釋：使用者是想看這張單子的料況資訊，不應限制料況狀態碼)

				       問題：「幫我查今天有哪些缺料的單子？」
				       回傳：{"status": null, "sinb": null, "sipnb": null, "sipname": null, "simcnote": null, "simcstatus": 1, "sicorder": null, "simcdates": "2026-04-10 00:00:00", "simcdatee": "2026-04-10 23:59:59"}

				       問題：「單號 A511 的生產進度如何？」
				       回傳：{"status": null, "sinb": "A511", "sipnb": null, "sipname": null, "simcnote": null, "simcstatus": null, "sicorder": null, "simcdates": null, "simcdatee": null}
				       (解釋：中性詢問「進度」，status 保持 null)

				       問題：「查 90-504 已經齊料的訂單」
				       回傳：{"status": null, "sinb": null, "sipnb": "90-504", "sipname": null, "simcnote": null, "simcstatus": 2, "sicorder": null, "simcdates": null, "simcdatee": null}

				使用者問題： %s
				"""
				.replace("\t", " ") // 💡 預防 vLLM 報錯
				.formatted(currentDate, currentDate.substring(0, 10), currentDate.substring(0, 10), userQuestion);
	}

	/**
	 * 🧠 生管專家分析：優先給予建議，再執行全文翻譯
	 * 
	 * @param scheduleData 原始 Markdown 排程表格
	 * @param userQuestion 使用者問題
	 * @param targetLang   目標語系 (作為系統參考)
	 */
	public String getExpertAdvice(String scheduleData, String userQuestion, VoiceLang targetLang) {
		log.info("[大腦] 執行專家分析建議 -> 語系偵測 -> 全文翻譯...");

		// 1. 取得時間基準
		String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		// 2. 決定語系名稱
		String langName = switch (targetLang) {
		case ZH_TW -> "台灣繁體中文 (Traditional Chinese)";
		case EN_US -> "Natural English (Professional)";
		case VI_VN -> "Vietnamese (Industrial Standard)";
		};

		// 3. 封裝高效能 Prompt
		String expertPrompt = """
				[System Role]
				你是一位擁有 20 年經驗的『高級生管與物控專家 (SMC Expert)』。

				[Current Context]
				當前系統日期時間為：%s。

				[Analysis Task]
				1. 🚀【數量與基準規則】：
				   - **分母基準**：請以『預計生產』欄位的數值作為所有進度計算的唯一分母。
				   - **進度判斷**：對比『總進度』。例如 8/9 代表已有 8 台完成或入庫，1 台待處理。
				   - **過站解析**：對比『製造生產(已過站數)』中的各站數字。
				   - **空白處理**：若該欄位顯示為『-』或空白，代表目前沒有詳細站點追蹤資料。此時請僅依據『總進度』進行分析，絕對不可虛構站點名稱（如自行編造整理站、包裝站）。
                   - 若站點數字 = 預計生產，代表該站 100%% 完成。
                   - 若站點數字 < 預計生產，代表產品目前正卡在該站點。
                   - 範例：預計生產 20，整理站 17，代表整理站完成率為 85%%，還有 3 台在製品。

				2. 🚀【日期格式嚴格防錯】：
				   - **備註區間**：在「物控備註」或「生管備註」中看到的「X/Y」（如 2/25, 3/6）**絕對是日期（月/日）**。
				   - **禁止誤判**：絕對不可將日期誤判為「庫存數量」或「比例」。
				   - **延遲判定**：若「料狀況」顯示為「✅齊料」，代表物料已到位，即使「預計齊料日」是過去的時間，也不應判定為「嚴重延遲風險」。

				3. 🚀【專家建議診斷】：
				   - 分析當前日期與「預計完工日」的距離。
				   - 找出產線真正的瓶頸站點（第一個數字未達標的站點）。
				   - 語氣口語化，適合語音播報。

				4. 🚀【進度語意校正】：
                   - 總進度 0/20 代表尚未完成入庫，絕對不可說『進度已達 100%%』。
                   - 請區分『系統進度 (總進度)』與『現場 WIP 進度 (過站數)』。
                   - 應描述為：『雖然系統總進度尚未入庫，但現場在製品 (WIP) 已推移至整理站與包裝站。』"

		        5. 🚀【瓶頸站點定義】：
                   - 瓶頸站點是『第一個數字為 0 或明顯落後』的站點。
                   - 在此案例中，整理站是『進行中』，包裝站是『待開工』，兩者皆需關注。"

				[Output Rules]
				- **強制語言**：請完全使用『%s』輸出。
				- **限制**：不回傳表格，字數 300-600 字，移除 Markdown 符號（如 *, #, |, -）。

				[Output Example]
				{
                  "aIsuggestion": "根據目前 A521 的狀況，整理站已完成 85%%，建議立即催促包裝站準備。"
				}

				[Output Format]
				{
				  "aIsuggestion": "專家分析建議內容"
				}

				[User Question]
				%s

				[Schedule Data]
				%s

				""".formatted(currentDateTime, langName, userQuestion, scheduleData);

		try {
			// 正式運行：return processText(expertPrompt);
			return expertPrompt;
		} catch (Exception e) {
			log.error("[大腦] 專家分析失敗: {}", e.getMessage());
			return "{\"aIsuggestion\": \"AI Inference Failed.\", \"translation\": \"Error\"}";
		}
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
