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

//🛠️ 3. 節點關鍵字配置 (AiNodeKeyword.java)
@Entity
@Table(name = "ai_node_keyword")
public class AiRecordNodeKeyword extends BaseEntity {

	/**
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 */
	public AiRecordNodeKeyword() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "node_id", nullable = false)
	private AiRecordNode node;

	@Column(name = "source_label", nullable = false, columnDefinition = "varchar(255) default ''")
	private String sourcelabel; // PDF/Word/Excel... 標籤 (如: PO No.)

	@Column(name = "target_column", nullable = false, columnDefinition = "varchar(255) default ''")
	private String targetcolumn; // DB 欄位 (如: order_id)

	@Column(name = "data_type", nullable = false, columnDefinition = "varchar(50) default 'String'")
	private String datatype; // 數據類型

	@Column(name = "is_multi_row", nullable = false, columnDefinition = "varchar(1) default 'N'")
	private String ismultirow; // 🚀 是否為多列清單 (Y/N)

	@Column(name = "validation_rule", nullable = false, columnDefinition = "text default ''")
	private String validationrule; // 🚀 驗證規則 (例如: >0, length<10)

	@Column(name = "value", nullable = false, columnDefinition = "text default ''")
	private String value; // 🚀 抓取到的值

}