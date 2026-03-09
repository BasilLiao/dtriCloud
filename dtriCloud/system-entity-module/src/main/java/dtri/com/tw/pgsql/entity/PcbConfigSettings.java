package dtri.com.tw.pgsql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * PCB設定資料 (pcb_config_settings) 增加了 columnDefinition 以符合 PostgreSQL 規範
 */
@Setter
@Getter
@Entity
@Table(name = "pcb_config_settings")
@EntityListeners(AuditingEntityListener.class)
public class PcbConfigSettings extends BaseEntity {

	// --- 業務專用欄位 (pcs_) ---

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pcs_id", columnDefinition = "bigserial", unique = true, nullable = false)
	private Long pcsid; // Key

	@Column(name = "pcs_c_name", columnDefinition = "varchar(50) default ''", nullable = false)
	private String pcscname = ""; // 設定檔名稱

	@Column(name = "pcs_version", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcsversion = ""; // 版本號

	@Lob
	@Column(name = "pcs_f_binary", columnDefinition = "bytea", nullable = false)
	private byte[] pcsfbinary; // 二進位檔案資料 (PostgreSQL 使用 bytea)

	@Column(name = "pcs_f_name", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcsfname = ""; // 原始檔名

	@Column(name = "pcs_f_size", columnDefinition = "int8 default 0", nullable = false)
	private Long pcsfsize = 0L; // 檔案大小 (使用 int8 對應 Long)

	@Column(name = "pcs_f_type", columnDefinition = "varchar(50) default ''", nullable = false)
	private String pcsftype = ""; // MIME 類型 / 副檔名

	@Column(name = "pcs_l_count", columnDefinition = "int4 default 0", nullable = false)
	private Integer pcslcount = 0; // 層數

	@Column(name = "pcs_m_type", columnDefinition = "int4 default 0", nullable = false)
	private Integer pcsmtype = 0; // 板材類型(mm)

	@Column(name = "pcs_b_thickness", columnDefinition = "int4 default 0", nullable = false)
	private Integer pcsbthickness = 0; // 銅厚(oz)

}