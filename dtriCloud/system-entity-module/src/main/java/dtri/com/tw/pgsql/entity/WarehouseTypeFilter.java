package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * @author Basil
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---倉儲單據過濾器---<br>
 *      wtftype = 0;單據類型 0=入庫 / 1=出庫 / 2=轉移<br>
 *      wtfcode = 單據代號(開頭) Ex:A511 / A521<br>
 *      wtfname = 單據名稱<br>
 *      wtfqccheck = 經手_檢驗單位(通知)<br>
 *      wtfmrcheck = 經手_單據管理(通知+管理);<br>
 *      wtfsepncheck = 經手_儲位負責(通知+執行);<br>
 *      wtfdeflocation = 預設暫存儲位 來源端;<br>
 *      wtfurgency = 急迫性 白=0[一般/預設3天/手動發單] 黃=1[手動標示急迫] 紅=2[立即];<br>
 *      wtfaiqty = 自動入料? 是=該單據時自動結算;<br>
 *      wtfadqty = 自動領料? 是=該單據自動結算;<br>
 * 
 */

@Entity
@Table(name = "warehouse_type_filter")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseTypeFilter {
	public WarehouseTypeFilter() {
		// 共用型
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysodate = new Date();
		this.sysouser = "system";

		this.sysheader = false;
		this.sysstatus = 0;
		this.syssort = 0;// 欄位?排序
		this.sysnote = "";
		// 倉儲單據過濾器-清單
		this.wtftype = 0;
		this.wtfcode = "";
		this.wtfname = "";
		this.wtfqccheck = false;
		this.wtfmrcheck = false;
		this.wtfsepncheck = false;
		this.wtfdeflocation = "";
		this.wtfurgency = 0;
		this.wtfaiqty = false;
		this.wtfadqty = false;

	}

	// 共用型
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

	// 倉儲單據過濾器-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_type_filter_seq")
	@SequenceGenerator(name = "warehouse_type_filter_seq", sequenceName = "warehouse_type_filter_seq", allocationSize = 1)
	@Column(name = "wtf_id")
	private Long wtfid;
	@Column(name = "wtf_type", nullable = false, columnDefinition = "int default 0")
	private Integer wtftype;
	@Column(name = "wtf_code", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String wtfcode;
	@Column(name = "wtf_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wtfname;
	@Column(name = "wtf_qc_check", nullable = false, columnDefinition = "boolean default false")
	private Boolean wtfqccheck;
	@Column(name = "wtf_mr_check", nullable = false, columnDefinition = "boolean default false")
	private Boolean wtfmrcheck;
	@Column(name = "wtf_se_pn_check", nullable = false, columnDefinition = "boolean default false")
	private Boolean wtfsepncheck;
	@Column(name = "wtf_def_location", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wtfdeflocation;
	@Column(name = "wtf_urgency", nullable = false, columnDefinition = "int default 0")
	private Integer wtfurgency;
	@Column(name = "wtf_a_i_qty", nullable = false, columnDefinition = "boolean default false")
	private Boolean wtfaiqty;
	@Column(name = "wtf_a_d_qty", nullable = false, columnDefinition = "boolean default false")
	private Boolean wtfadqty;

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

	public Integer getSysstatus() {
		return sysstatus;
	}

	public void setSysstatus(Integer sysstatus) {
		this.sysstatus = sysstatus;
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

	public Long getWtfid() {
		return wtfid;
	}

	public void setWtfid(Long wtfid) {
		this.wtfid = wtfid;
	}

	public Integer getWtftype() {
		return wtftype;
	}

	public void setWtftype(Integer wtftype) {
		this.wtftype = wtftype;
	}

	public String getWtfcode() {
		return wtfcode;
	}

	public void setWtfcode(String wtfcode) {
		this.wtfcode = wtfcode;
	}

	public String getWtfname() {
		return wtfname;
	}

	public void setWtfname(String wtfname) {
		this.wtfname = wtfname;
	}

	public Boolean getWtfqccheck() {
		return wtfqccheck;
	}

	public void setWtfqccheck(Boolean wtfqccheck) {
		this.wtfqccheck = wtfqccheck;
	}

	public Boolean getWtfmrcheck() {
		return wtfmrcheck;
	}

	public void setWtfmrcheck(Boolean wtfmrcheck) {
		this.wtfmrcheck = wtfmrcheck;
	}

	public Boolean getWtfsepncheck() {
		return wtfsepncheck;
	}

	public void setWtfsepncheck(Boolean wtfsepncheck) {
		this.wtfsepncheck = wtfsepncheck;
	}

	public String getWtfdeflocation() {
		return wtfdeflocation;
	}

	public void setWtfdeflocation(String wtfdeflocation) {
		this.wtfdeflocation = wtfdeflocation;
	}

	public Integer getWtfurgency() {
		return wtfurgency;
	}

	public void setWtfurgency(Integer wtfurgency) {
		this.wtfurgency = wtfurgency;
	}

	public Boolean getWtfaiqty() {
		return wtfaiqty;
	}

	public void setWtfaiqty(Boolean wtfaiqty) {
		this.wtfaiqty = wtfaiqty;
	}

	public Boolean getWtfadqty() {
		return wtfadqty;
	}

	public void setWtfadqty(Boolean wtfadqty) {
		this.wtfadqty = wtfadqty;
	}
}
