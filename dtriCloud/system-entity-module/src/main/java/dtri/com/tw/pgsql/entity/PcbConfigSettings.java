package dtri.com.tw.pgsql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	// 2. 全參數建構子 (手動建立新物件時使用)
	public PcbConfigSettings() {
		super();
		this.pcsid = null;
		this.pcspnb = "";
		this.pcspname = "";
		this.pcspmodel = "";
		this.pcspspecification = "";
		this.pcspcbname = "";
		this.pcspcbaname = "";
		this.pcspmuser = "";
		this.pcsrduser = "";
		this.pcscname = "";
		this.pcsversion = "";
		this.pcsfbinary = null;
		this.pcsfname = "";
		this.pcsfsize = 0L;
		this.pcsftype = "";
		this.pcslcount = 0;
		this.pcsmtype = 0;
		this.pcsbthickness = 0;
	}
	// --- 業務專用欄位 (pcs_) ---

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pcs_id", columnDefinition = "bigserial", unique = true, nullable = false)
	private Long pcsid; // Key

	@Column(name = "pcs_p_nb", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspnb = ""; // 產品號

	@Column(name = "pcs_p_name", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspname = ""; // 產品名

	@Column(name = "pcs_p_model", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspmodel = ""; // 產品型號

	@Column(name = "pcs_p_specification", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspspecification = ""; // 產品規格

	@Column(name = "pcs_pcb_name", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspcbname = ""; // 板廠-名稱

	@Column(name = "pcs_pcba_name", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspcbaname = ""; // 打件廠-名稱

	@Column(name = "pcs_pm_user", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcspmuser = ""; // PM負責人

	@Column(name = "pcs_rd_user", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcsrduser = ""; // RD負責人

	@Column(name = "pcs_c_name", columnDefinition = "varchar(50) default ''", nullable = false)
	private String pcscname = ""; // 設定檔名稱

	@Column(name = "pcs_version", columnDefinition = "varchar(150) default ''", nullable = false)
	private String pcsversion = ""; // 版本號

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

	// --- 修改 Setter：接收前端字串並拆解 ---
	public void setPcsfbinary(Object value) {
		if (value instanceof String && ((String) value).startsWith("data:")) {
			// 文字型態處理
			String str = (String) value;
			String[] parts = str.split(",");
			String header = parts[0];
			String content = parts[1];

			// 拆解 Header 屬性 (name, size, type)
			String[] attributes = header.split(";");
			for (String attr : attributes) {
				if (attr.startsWith("data:"))
					this.pcsftype = attr.substring(5);
				if (attr.startsWith("name:"))
					this.pcsfname = attr.substring(5);
				if (attr.startsWith("size:"))
					this.pcsfsize = Long.parseLong(attr.substring(5));
			}
			this.pcsfbinary = java.util.Base64.getDecoder().decode(content);

		} else if (value instanceof byte[]) {
			// 二進制處理
			this.pcsfbinary = (byte[]) value;
		}
	}

	@JsonProperty("pcsfbinary")
	public String getPcsfbinaryString() {
		// 讓 Jackson 在轉 JSON 給前端時，自動呼叫這個方法。來避免原先的getPcsfbinary(2進位)
		if (this.pcsfbinary != null && this.pcsfbinary.length > 0) {
			String base64Content = java.util.Base64.getEncoder().encodeToString(this.pcsfbinary);
			return "data:" + this.pcsftype + ";name=" + this.pcsfname + ";size=" + this.pcsfsize + ";base64,"
					+ base64Content;
		}
		return "";
	}

	// 1. 防止 pcsftype 被空值複寫
	public void setPcsftype(String pcsftype) {
		// 只有在傳入值不為空，或者目前欄位還是空的時候才寫入
		if (pcsftype != null && !pcsftype.trim().isEmpty()) {
			this.pcsftype = pcsftype;
		}
	}

	// 2. 防止 pcsfname 被空值複寫
	public void setPcsfname(String pcsfname) {
		if (pcsfname != null && !pcsfname.trim().isEmpty()) {
			this.pcsfname = pcsfname;
		}
	}

	// 3. 防止 pcsfsize 被 0 複寫 (選用)
	public void setPcsfsize(Long pcsfsize) {
		if (pcsfsize != null && pcsfsize > 0) {
			this.pcsfsize = pcsfsize;
		}
	}

}