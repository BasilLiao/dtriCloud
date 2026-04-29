package dtri.com.tw.pgsql.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

//🛠️ 2. 工作節點定義表 (AiRecordNode.java)
@Entity
@Table(name = "ai_record_node")
public class AiRecordNode extends BaseEntity {

	/**
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 */
	public AiRecordNode() {
		super();
		this.connection = new AiRecordConnection();
		this.datastore = new AiRecordDataStore();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private AiRecordTemplate template;

	// 🚀 節點名稱 (用於 UI 顯示與維護辨識)
	@Column(name = "node_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String nodename;

	// 🚀 節點詳細備註 (解釋這個步驟的具體邏輯)
	@Column(name = "node_memo", nullable = false, columnDefinition = "text default ''")
	private String nodememo;

	@Column(name = "step_order", nullable = false, columnDefinition = "int default 0")
	private Integer steporder; // 執行順序
	/**
	 * 1. 解析類 (Data Acquisition) <br>
	 * * EXTRACT (提取資料)：<br>
	 * 作用：最常用的行為。叫 AI 從 PDF 或圖片中，根據 AiNodeKeyword 定義的標籤把數據「摳」出來。 <br>
	 * 情境：從採購單拿單號、拿數量。<br>
	 * 
	 * * OCR_ONLY (純文字辨識)： <br>
	 * 作用：不進行邏輯分析，只把圖片或檔案內容轉成純文字，通常用於存檔備查。<br>
	 * 
	 * =========================================<br>
	 * 2. 邏輯與轉換類 (Logic & Processing) <br>
	 * * MATCH (數據對齊)： <br>
	 * 作用：將 EXTRACT 拿到的資料與資料庫現有的資料進行「配對」。 <br>
	 * 情境：拿 PDF 的廠商名稱去比對資料庫的 vendor_id。<br>
	 * 
	 * * CALC (邏輯運算)： <br>
	 * 作用：叫 AI 進行數學計算或時間計算。 <br>
	 * 情境：將「單價」乘以「數量」算出總額，並核對與 PDF 上的總額是否相符。<br>
	 * 
	 * * VALIDATE (規則驗證)： <br>
	 * 作用：檢查數據是否符合規定的範圍或格式（參考 validation_rule）。 <br>
	 * 情境：檢查入庫溫度是否在 0 ~ 5°C 之間。<br>
	 * 
	 * =========================================<br>
	 * 3. 互動與審核類 (Interaction) <br>
	 * * CONFIRM (等待確認)： <br>
	 * 作用：這就是你的 "Yes / No" 核心。系統暫停執行，將當前的 JSON 數據呈現給使用者，等待輸入 Yes 才繼續下一步。<br>
	 * 
	 * * MANUAL (人工修正)： <br>
	 * 作用：當 AI 信心值過低或 error_handle_node 觸發時，強迫跳出輸入框讓使用者手動修正資料。<br>
	 * 
	 * =========================================<br>
	 * 4. 系統執行類 (System Execution) <br>
	 * * WRITE (寫入資料)： <br>
	 * 作用：將 temp_json_data 中的內容，根據 target_table 執行動態 INSERT。<br>
	 * 
	 * * UPDATE (更新資料)： <br>
	 * 作用：不新增，而是修改資料庫中現有的資料。<br>
	 * 情境：客戶變更訂單交期，更新 so_m 的 delivery_date。<br>
	 * 
	 * * DELETE (硬刪除)：<br>
	 * 作用：物理刪除。直接從資料庫中將符合條件的 Row 抹除。<br>
	 * 情境：錄製失敗後，清理 temp 暫存表的殘留資料。<br>
	 * 
	 * * VOID (作廢/軟刪除)：<br>
	 * 作用：邏輯刪除。不移除數據，而是修改狀態欄位（如改為 'Cancel' 或 'Invalid'）。<br>
	 * 情境：客戶取消訂單，AI 提取單號後將訂單狀態標記為作廢。<br>
	 * 
	 * * REPLACE (覆蓋更新)：<br>
	 * 作用：先刪後加。根據條件刪除舊有的關聯資料，再重新寫入當前解析的新數據。<br>
	 * 情境：更新產品 B.O.M 結構，刪除舊有的零件清單並錄製新版結構。<br>
	 * 
	 * * ROLLBACK (補償/撤回)：<br>
	 * 作用：流程逆轉。當後續節點執行失敗時，自動撤銷（刪除）本流程先前節點已寫入的數據，確保數據一致性。<br>
	 * 情境：單身資料寫入失敗時，自動移除已寫入資料庫的單頭資料。<br>
	 * 
	 * * NOTIFY (通知/告警)：<br>
	 * 作用：不進資料庫，而是觸發系統通知（例如發送 Email、App 推送或看板警示）。<br>
	 * 情境：當 VALIDATE 發現數值異常時，發信通知相關主管。<br>
	 * 
	 * =========================================<br>
	 * 5. 外部對接類 (External Integration) <br>
	 * * API_CALL (執行 API)：<br>
	 * 作用：透過 HTTP REST 呼叫外部系統。系統會根據 AiNodeKeyword 組合 Body 或 Header。<br>
	 * 情境：錄製成功後，自動發送 LINE 通知給主管，或將訂單推送到供應商平台。<br>
	 * 
	 * * EXT_DB_WRITE (外部資料庫寫入)：<br>
	 * 作用：利用 connection_id 建立連線，在非本機的資料庫執行寫入。<br>
	 * 情境：將 AI 解析出的生產數據，直接寫入另一台伺服器的舊版 ERP 資料表。<br>
	 * 
	 * * EXT_DB_QUERY (外部資料庫查詢)：<br>
	 * 作用：從外部資料庫撈取資料供 AI 後續判斷。<br>
	 * 情境：EXTRACT 拿到料號後，去外部 MES 系統查詢該料號的目前庫存水位。<br>
	 * 
	 * =========================================<br>
	 * 6. 專案記憶類 (Template Memory Management) <br>
	 * STORE_LOCAL (存入專案記憶)：<br>
	 * 作用：將當前解析到的數據，永久存入此錄製專案的 AiRecordDataStore 中。<br>
	 * 情境：AI 解析完一份「部門人員名單」，使用者要求系統「記住這份名單」供下次使用。<br>
	 * 
	 * FETCH_LOCAL (讀取專案記憶)：<br>
	 * 作用：從當前專案的 AiRecordDataStore 中抓取先前儲存的資料，供 AI 作為背景參考。<br>
	 * 情境：執行採購錄製時，自動讀取上次錄製存下的「常用零件對照表」來加速辨識。<br>
	 * 
	 * CLEAR_LOCAL (清除專案記憶)：<br>
	 * 作用：刪除該專案下特定的 data_key 資料。<br>
	 * 情境：人員調動，清空該專案舊有的「人員名單」記憶。<br>
	 */
	@Column(name = "node_behavior", nullable = false, columnDefinition = "varchar(50) default ''")
	private String nodebehavior; // 行為 是叫 AI 去 EXTRACT（拿資料）、MATCH（對齊欄位）、還是 WRITE（寫入資料庫）？

	@Column(name = "ai_prompt_template", nullable = false, columnDefinition = "text default ''")
	private String aiprompttemplate; // 協作設定的 AI 指令

	@Column(name = "target_table", nullable = false, columnDefinition = "varchar(100) default ''")
	private String targettable; // 🚀 目標資料表

	// 🚀 成功跳轉目標 (與 error_handle_node 配對)
	// 當行為執行成功時，跳到哪一個節點 ID。若為 0 則按 step_order 執行下一筆。
	@Column(name = "next_node_id", nullable = false, columnDefinition = "bigint default 0")
	private Long nextnodeid;

	@Column(name = "error_handle_node", nullable = false, columnDefinition = "bigint default 0")
	private Long errorhandlenode; // 🚀 失敗跳轉節點

	// 🚀 改為 OneToOne 綁定，設定 CascadeType.ALL 代表儲存 Node 時，會自動儲存 Connection
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "connection_id", referencedColumnName = "id")
	private AiRecordConnection connection;

	// 🚀 同理，讓 Node 直接擁有 DataStore (一對一)
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "datastore_id", referencedColumnName = "id")
	private AiRecordDataStore datastore;

	@OneToMany(mappedBy = "node", cascade = CascadeType.ALL)
	private List<AiRecordNodeKeyword> keywords;// 例如:定義 PDF 的「單價」要對應到外部表的 price 欄位。
}