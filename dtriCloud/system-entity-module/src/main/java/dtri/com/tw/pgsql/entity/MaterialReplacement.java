package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Basil
 *         * [功能] N:N 替代料基本資訊
 *         [核心變更]
 *         1. mr_nb: 不再唯一，可重複建立不同 Scope 的規則
 */
@Entity
@Table(name = "material_replacement", indexes = {
		@Index(name = "idx_mr_nb", columnList = "mr_nb"), // 加速主料號查詢
})
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class MaterialReplacement {

	public MaterialReplacement() {
		// --- 共用型初始化 ---
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysodate = new Date();
		this.sysouser = "system";

		this.sysheader = false;
		this.sysstatus = 0;
		this.syssort = 0;
		this.sysnote = "";

		// --- 業務型初始化 ---
		this.mrid = null;
		this.mrnb = "";
		this.mrname = "";
		this.mrspecification = "";
		this.mrnote = "";

		// --- 舊欄位保留 ---
		this.mrclnote = "";
		this.mrnnnote = "";
		this.mrpnote = "";
		this.mrsubnote = "";
		this.mrprove = "";
	}

	// ==============================================
	// 系統共用欄位 (System Fields)
	// ==============================================
	@Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date syscdate;
	@Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String syscuser;
	@Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date sysmdate;
	@Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String sysmuser;
	@Column(name = "sys_o_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date sysodate;
	@Column(name = "sys_o_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String sysouser;

	@Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysheader;
	@Column(name = "sys_status", nullable = false, columnDefinition = "int default 0")
	private Integer sysstatus;
	@Column(name = "sys_sort", nullable = false, columnDefinition = "int default 0")
	private Integer syssort;
	@Column(name = "sys_note", nullable = false, columnDefinition = "text default ''")
	private String sysnote;

	// 版本控制 (解決併發問題)
	@Version
	@Column(name = "sys_ver", nullable = false)
	private Integer sysver = 0;

	// ==============================================
	// 業務主鍵 (Business ID)
	// ==============================================
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_replacement_seq")
	@SequenceGenerator(name = "material_replacement_seq", sequenceName = "material_replacement_seq", allocationSize = 1)
	@Column(name = "mr_id", nullable = false)
	private Long mrid;

	// ==============================================
	// 核心欄位：配方與搜尋 (Core Formula Fields)
	// ==============================================

	// 1. 搜尋索引：來源配方的第一個料號 (Primary Source P/N)
	// 注意：這裡移除了 unique=true
	@Column(name = "mr_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String mrnb;

	// ==============================================
	// 舊欄位與描述性欄位 (Legacy / Descriptive)
	// ==============================================
	@Column(name = "mr_name", nullable = false, columnDefinition = "text default ''")
	private String mrname;
	@Column(name = "mr_specification", nullable = false, columnDefinition = "text default ''")
	private String mrspecification;
	@Column(name = "mr_note", nullable = false, columnDefinition = "text default ''")
	private String mrnote;

	@Column(name = "mr_sub_note", nullable = false, columnDefinition = "text default ''")
	private String mrsubnote;
	@Column(name = "mr_nn_note", nullable = false, columnDefinition = "text default ''")
	private String mrnnnote;
	@Column(name = "mr_cl_note", nullable = false, columnDefinition = "text default ''")
	private String mrclnote;
	@Column(name = "mr_p_note", nullable = false, columnDefinition = "text default ''")
	private String mrpnote;
	@Column(name = "mr_prove", nullable = false, columnDefinition = "text default ''")
	private String mrprove;

	// ==============================================
	// Getters and Setters
	// ==============================================

	public Long getMrid() {
		return mrid;
	}

	public void setMrid(Long mrid) {
		this.mrid = mrid;
	}

	public String getMrnb() {
		return mrnb;
	}

	public void setMrnb(String mrnb) {
		this.mrnb = mrnb;
	}

	public Integer getSysver() {
		return sysver;
	}

	public void setSysver(Integer sysver) {
		this.sysver = sysver;
	}

	public Integer getSysstatus() {
		return sysstatus;
	}

	public void setSysstatus(Integer sysstatus) {
		this.sysstatus = sysstatus;
	}

	public Date getSyscdate() {
		return syscdate;
	}

	public void setSyscdate(Date syscdate) {
		this.syscdate = syscdate;
	}

	public String getSyscuser() {
		return syscuser;
	}

	public void setSyscuser(String syscuser) {
		this.syscuser = syscuser;
	}

	public Date getSysmdate() {
		return sysmdate;
	}

	public void setSysmdate(Date sysmdate) {
		this.sysmdate = sysmdate;
	}

	public String getSysmuser() {
		return sysmuser;
	}

	public void setSysmuser(String sysmuser) {
		this.sysmuser = sysmuser;
	}

	public String getMrname() {
		return mrname;
	}

	public void setMrname(String mrname) {
		this.mrname = mrname;
	}

	public String getMrspecification() {
		return mrspecification;
	}

	public void setMrspecification(String mrspecification) {
		this.mrspecification = mrspecification;
	}

	public String getMrnote() {
		return mrnote;
	}

	public void setMrnote(String mrnote) {
		this.mrnote = mrnote;
	}

	// 其餘未列出的 Getter/Setter 請保留或由 IDE 生成 (例如 sysodate, sysouser 等)
	public Date getSysodate() {
		return sysodate;
	}

	public void setSysodate(Date sysodate) {
		this.sysodate = sysodate;
	}

	public String getSysouser() {
		return sysouser;
	}

	public void setSysouser(String sysouser) {
		this.sysouser = sysouser;
	}

	public Boolean getSysheader() {
		return sysheader;
	}

	public void setSysheader(Boolean sysheader) {
		this.sysheader = sysheader;
	}

	public Integer getSyssort() {
		return syssort;
	}

	public void setSyssort(Integer syssort) {
		this.syssort = syssort;
	}

	public String getSysnote() {
		return sysnote;
	}

	public void setSysnote(String sysnote) {
		this.sysnote = sysnote;
	}

	public String getMrsubnote() {
		return mrsubnote;
	}

	public void setMrsubnote(String mrsubnote) {
		this.mrsubnote = mrsubnote;
	}

	public String getMrnnnote() {
		return mrnnnote;
	}

	public void setMrnnnote(String mrnnnote) {
		this.mrnnnote = mrnnnote;
	}

	public String getMrclnote() {
		return mrclnote;
	}

	public void setMrclnote(String mrclnote) {
		this.mrclnote = mrclnote;
	}

	public String getMrpnote() {
		return mrpnote;
	}

	public void setMrpnote(String mrpnote) {
		this.mrpnote = mrpnote;
	}

	public String getMrprove() {
		return mrprove;
	}

	public void setMrprove(String mrprove) {
		this.mrprove = mrprove;
	}

}