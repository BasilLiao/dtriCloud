package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Basil
 * @description AI 對話訊息明細表 此表負責紀錄每一則對話的具體內容，包括使用者提問、AI 回覆、以及產出的 Excel 附件路徑。
 */
@Setter
@Getter
@Entity
@Table(name = "ai_chat_messages")
@EntityListeners(AuditingEntityListener.class)
public class AiChatMessages extends BaseEntity {

	/**
	 * 無參建構子：符合 JPA 規範，並初始化基本欄位避免 null 導致的 JSON 解析錯誤。
	 */
	public AiChatMessages() {
		super();
		this.setAcmcontent(""); // 防止前端顯示時出現 null 字樣
		this.setAcmmtype("TEXT"); // 預設為一般文字
		this.setAcmaurl(""); // 預設無附件路徑
	}

	/**
	 * 訊息唯一識別碼 (PK)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 多對一關聯：對應所屬的會話 (Session)。 FetchType.LAZY: 延遲載入，避免查詢訊息時自動撈出整個 Session，提升效能。
	 * acm_s_id: 外鍵，指向 ai_chat_sessions 表。
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "acm_s_id", nullable = false)
	private AiChatSessions acmsessions;

	/**
	 * 訊息角色： USER: 現場人員/生管人員提問。 ASSISTANT: AI 的回覆。 SYSTEM: 下達給 AI 的隱藏指令（如：你現在是生管專家）。
	 * EnumType.STRING: 資料庫儲存字串（如 'USER'），增加維護可讀性。
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MessageRole role;

	/**
	 * 訊息內文： 使用 text 類型以容納長篇的 AI 分析報告或排程說明。
	 */
	@Column(name = "acm_content", nullable = false, columnDefinition = "text default ''")
	private String acmcontent;

	/**
	 * 訊息類型： TEXT: 單純對話。 EXCEL: 當 AI 生成排程表時標註。 VOICE: 當這則訊息是語音輸入時標註。
	 * 作用：前端可根據此類型顯示不同的 UI 元件（如：下載按鈕或語音播放器）。
	 */
	@Column(name = "acm_m_type", nullable = false, columnDefinition = "text default ''")
	private String acmmtype;

	/**
	 * 附件路徑： 儲存 Excel 報表或語音檔在伺服器（或 S3）的路徑。 當 acmmtype 為 'EXCEL' 時，此欄位存放下載 URL。
	 */
	@Column(name = "acm_a_url", nullable = true, columnDefinition = "text default ''")
	private String acmaurl;

	/**
	 * 一對一雙向關聯：掛載語音元數據。 mappedBy: 由 AiVoiceMetadata 表中的 aichatmessages 欄位維護關聯。
	 * CascadeType.ALL: 刪除訊息時，同步刪除對應的語音紀錄。
	 */
	@OneToOne(mappedBy = "aichatmessages", cascade = CascadeType.ALL)
	private AiVoiceMetadata voiceMetadata;

	/**
	 * 訊息建立時間： 用於排序對話歷史（Chat History）。
	 */
	@Column(name = "acm_c_at", nullable = false, columnDefinition = "TIMESTAMP default now()")
	@Temporal(TemporalType.TIMESTAMP) // 確保包含時分秒
	private Date acmcat = new Date();

	/**
	 * 訊息角色列舉
	 */
	public enum MessageRole {
		USER, ASSISTANT, SYSTEM
	}
}
