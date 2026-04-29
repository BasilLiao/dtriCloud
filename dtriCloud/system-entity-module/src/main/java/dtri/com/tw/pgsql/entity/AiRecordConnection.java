package dtri.com.tw.pgsql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//🛠️ 5. 新增「連線配置表」為了安全性與重複利用，我們不把帳號密碼寫在節點裡，而是統一管理。
@Entity
@Table(name = "ai_record_connection")
public class AiRecordConnection extends BaseEntity {

	/**
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 */
	public AiRecordConnection() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "conn_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String connname; // 連線名稱 (例如: ERP_DB, LINE_API)

	@Column(name = "conn_type", nullable = false, columnDefinition = "varchar(50) default ''")
	private String conntype; // 類型: DATABASE (JDBC), API (REST)

	@Column(name = "conn_url", nullable = false, columnDefinition = "text default ''")
	private String connurl; // JDBC URL 或 API (String) (例: 'http://10.1.0.5:8080/api')

	@Column(name = "conn_user", nullable = false, columnDefinition = "varchar(100) default ''")
	private String connuser; // 帳號

	@Column(name = "conn_pwd", nullable = false, columnDefinition = "varchar(100) default ''")
	private String connpwd; // 密碼 (建議加密)

	@Column(name = "conn_driver", nullable = false, columnDefinition = "varchar(100) default ''")
	private String conndriver; // 資料庫驅動 (如: com.microsoft.sqlserver.jdbc.SQLServerDriver)

	@Column(name = "conn_auth", nullable = false, columnDefinition = "varchar(100) default ''")
	private String connauth; // 驗證權鑰或密碼 (String) (例: 'bearer_token_xyz')

}
