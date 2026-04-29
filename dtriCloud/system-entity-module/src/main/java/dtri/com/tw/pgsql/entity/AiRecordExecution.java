package dtri.com.tw.pgsql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

//🛠️ 4. 執行實例追蹤 (AiRecordExecution.java)
@Entity
@Table(name = "ai_record_execution")
public class AiRecordExecution extends BaseEntity {

	/**
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 */
	public AiRecordExecution() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "template_id", nullable = false)
	private AiRecordTemplate template;// 定義任務是「跨廠區採購同步」。

	@Column(name = "current_node_id", nullable = false, columnDefinition = "bigint default 0")
	private Long currentnodeid; // 當前執行到的節點 ID(存好外部 ERP 的 JDBC 連線 或是 API 呼叫。)

	@Column(name = "temp_json_data", nullable = false, columnDefinition = "text default ''")
	private String tempjsondata; // 暫存待確認的解析數據

	@Column(name = "response_message", nullable = false, columnDefinition = "varchar(50) default 'RUNNING'")
	private String responsemessage; // 訊息 (可能會有錯誤資訊/成功回傳資訊)

}
