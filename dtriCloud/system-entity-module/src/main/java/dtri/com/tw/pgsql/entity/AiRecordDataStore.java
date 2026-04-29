package dtri.com.tw.pgsql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//🛠️ 6.「專案資料儲存表」這張表專門用來存放該錄製專案「記住」的而外資料（如人員名單、零件對照表）。
@Entity
@Table(name = "ai_record_data_store")
public class AiRecordDataStore extends BaseEntity {

	/**
	 * 無參建構子：手寫以符合 JPA 反射機制，並初始化欄位預設值。 super() 確保父類別 BaseEntity 的 sys_ 欄位也能正確初始化。
	 */
	public AiRecordDataStore() {
		super();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_key", nullable = false, columnDefinition = "varchar(100) default ''")
	private String data_key;// 資料標籤 (例如: personnel_list, safety_checklist)

	@Column(name = "data_value", nullable = false, columnDefinition = "text default ''")
	private String data_value;// 儲存的內容 (通常是 JSON 格式)

}