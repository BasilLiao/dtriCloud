package dtri.com.tw.pgsql.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

//🛠️ 1. 錄製模板主檔 (AiRecordTemplate.java)
@Entity
@Table(name = "ai_record_template")
public class AiRecordTemplate extends BaseEntity {

	/**
	 * 
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 **/
	public AiRecordTemplate() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "template_name", nullable = false, columnDefinition = "varchar(255) default ''")
	private String templatename; // 模板名稱

	@Column(name = "usage_instructions", nullable = false, columnDefinition = "text default ''")
	private String usageinstructions; // 使用說明 (點擊時顯示)

	@Column(name = "allowed_groups", nullable = false, columnDefinition = "varchar(255) default ''")
	private String allowedgroups; // 權限群組 (如: PMC,PUR)

	@OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
	@OrderBy("step_order ASC")
	private List<AiRecordNode> nodes;
}
