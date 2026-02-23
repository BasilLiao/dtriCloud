package dtri.com.tw.pgsql.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Basil
 * @description AI 會話主體表 (Session Controller) 負責管理一段完整對話的生命週期。在生管 AI 中，一個
 *              Session 可能代表「一次排程調整」或「一個專案的詢問」。
 */
@Setter
@Getter
@Entity
@Table(name = "ai_chat_sessions")
@EntityListeners(AuditingEntityListener.class)
public class AiChatSessions extends BaseEntity {

	/**
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 */
	public AiChatSessions() {
		super();
		this.setAcsccontext(""); // 初始化上下文為空字串，避免 JSON 處理時拋出 NPE
		this.setAcstitle(""); // 初始標題為空
		this.setAcsuaccount(""); // 預設使用者 帳號
		this.setAcsbtype("GENERAL");
	}

	/**
	 * 會話唯一識別碼 (PK) acs_id: 作為訊息表 (AiChatMessages) 的外鍵關聯目標。
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "acs_id", nullable = false)
	private Long acsid;

	/**
	 * 關聯系統使用者帳號 (User Account) 作用：區分這段對話是哪位生管人員。使用帳號字串(如: basil_lin)
	 * 儲存，與系統使用者表的主鍵類型保持一致。
	 */
	@Column(name = "acs_u_account", nullable = false, columnDefinition = "varchar(50)")
	private String acsuaccount;

	/**
	 * 會話標題： 作用：在 APP 或網頁左側列表顯示。 實務：通常在第一輪對話後，由 AI 自動總結出一個標題（如：2月10日產線 A 排程建議）。
	 */
	@Column(name = "acs_title", nullable = false, columnDefinition = "text default ''")
	private String acstitle;

	/**
	 * AI 上下文快照 (LLM Context Window Snapshot)： 作用：極其重要！儲存該次會話的 JSON 格式上下文。 實務：AI
	 * 對話有長度限制。我們將重要的排程數據（如工單剩餘時數）轉成 JSON 存於此， 下次詢問時直接餵給 AI，不需要重新從數據庫撈取數千筆資料。
	 */
	@Column(name = "acs_c_context", nullable = false, columnDefinition = "text default ''")
	private String acsccontext;

	/**
	 * 一對多關聯：一個會話包含多則訊息。 mappedBy: 指向 AiChatMessages 中的 'acmsessions' 變數。
	 * CascadeType.ALL: 刪除 Session 時，會自動刪除底下所有的對話訊息（連動刪除）。 OrderBy("acmcat ASC"):
	 * 確保從此 List 拿出的訊息是按時間先後順序排列，方便直接渲染 UI。
	 */
	@OneToMany(mappedBy = "acmsessions", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderBy("acmcat ASC")
	private List<AiChatMessages> aichatmessages;
	/**
	 * 會話業務類型 (Session Business Type) 作用：區分詢問的專業領域，例如：PMC(生管), MC(物控), PUR(採購)。
	 * 實務：這會決定後端串接 AI 時，要餵給 AI 什麼樣的「系統指令(System Prompt)」。<br>
	 * <br>
	 * 生管AI管理人 APMC 生管 (Production Management & Control) 專精於生產排程、工單優先級與產能利用率分析。<br>
	 * 物控AI管理人 AMMC 物控 (Material Control) 專精於庫存水位、物料需求計畫 (MRP) 與缺料預警。<br>
	 * 採購AI管理人 APUR 採購 (Purchasing) 專精於供應商交期、採購單狀態與採購成本分析。<br>
	 * 倉儲AI管理人 AWMS 倉儲 (Warehouse Management) 專精於入庫/出庫效率、儲位優化與盤點準確性。<br>
	 */
	@Column(name = "acs_b_type", nullable = false, length = 20, columnDefinition = "varchar(20) default 'GENERAL'")
	private String acsbtype; // PMC, MC, PUR, QC...

	/**
	 * 會話建立時間： updatable = false: 建立後不可更改，確保紀錄的嚴謹性。
	 */
	@Column(name = "acs_c_at", nullable = false, columnDefinition = "TIMESTAMP default now()", updatable = false)
	@Temporal(TemporalType.TIMESTAMP) // 確保包含時分秒
	private Date acscat = new Date(); // 預設值改為 new Date()

}
