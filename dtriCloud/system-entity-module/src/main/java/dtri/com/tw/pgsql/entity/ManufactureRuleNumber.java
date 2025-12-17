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
 *      ---規則SN清單---<br>
 *      mrn_id : ID<br>
 *      mrn_g_id :GID<br>
 *      mrn_name : 項目名稱 Ex:DT504<br>
 *      mrn_g_name : 項目組名稱 Ex:機種<br>
 *      mrn_val : 項目值 Ex:D504 <br>
 *      mrn_yyww : 年年周周(有綁定才有) Ex:2501<br>
 *      mrn_0000 :流水號(有綁定才有) Ex:0001<br>
 *      mrn_yymm_c :年年周周 勾選<br>
 *      mrn_0000_c : 流水號 勾選<br>
 * 
 */

@Entity
@Table(name = "manufacture_rule_number")
@EntityListeners(AuditingEntityListener.class)
public class ManufactureRuleNumber {
	public ManufactureRuleNumber() {
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
		// 規則SN清單-清單
		this.mrnid = null;
		this.mrngid = null;
		this.mrnname = "";
		this.mrngname = "";
		this.mrnval = "";
		this.mrnyyww = "";
		this.mrn0000 = "";
		this.mrnyywwc = false;
		this.mrn0000c = false;
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

	// 規則SN清單-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manufacture_rule_number_seq")
	@SequenceGenerator(name = "manufacture_rule_number_seq", sequenceName = "manufacture_rule_number_seq", allocationSize = 1)
	@Column(name = "mrn_id")
	private Long mrnid;
	@Column(name = "mrn_g_id", nullable = false)
	private Long mrngid;
	@Column(name = "mrn_name", nullable = false, unique = true, columnDefinition = "varchar(60) default ''")
	private String mrnname;
	@Column(name = "mrn_g_name", nullable = false, columnDefinition = "varchar(60) default ''")
	private String mrngname;
	@Column(name = "mrn_val", nullable = false, columnDefinition = "varchar(60) default ''")
	private String mrnval;
	@Column(name = "mrn_yyww", nullable = false, columnDefinition = "varchar(10) default ''")
	private String mrnyyww;
	@Column(name = "mrn_0000", nullable = false, columnDefinition = "varchar(10) default ''")
	private String mrn0000;
	@Column(name = "mrn_yyww_c", nullable = false, columnDefinition = "boolean default false")
	private Boolean mrnyywwc;
	@Column(name = "mrn_0000_c", nullable = false, columnDefinition = "boolean default false")
	private Boolean mrn0000c;

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

	public Long getMrnid() {
		return mrnid;
	}

	public void setMrnid(Long mrnid) {
		this.mrnid = mrnid;
	}

	public Long getMrngid() {
		return mrngid;
	}

	public void setMrngid(Long mrngid) {
		this.mrngid = mrngid;
	}

	public String getMrnname() {
		return mrnname;
	}

	public void setMrnname(String mrnname) {
		this.mrnname = mrnname;
	}

	public String getMrngname() {
		return mrngname;
	}

	public void setMrngname(String mrngname) {
		this.mrngname = mrngname;
	}

	public String getMrnval() {
		return mrnval;
	}

	public void setMrnval(String mrnval) {
		this.mrnval = mrnval;
	}

	public String getMrnyyww() {
		return mrnyyww;
	}

	public void setMrnyyww(String mrnyyww) {
		this.mrnyyww = mrnyyww;
	}

	public String getMrn0000() {
		return mrn0000;
	}

	public void setMrn0000(String mrn0000) {
		this.mrn0000 = mrn0000;
	}

	public Boolean getMrn0000c() {
		return mrn0000c;
	}

	public void setMrn0000c(Boolean mrn0000c) {
		this.mrn0000c = mrn0000c;
	}

	/**
	 * @return the mrnyywwc
	 */
	public Boolean getMrnyywwc() {
		return mrnyywwc;
	}

	/**
	 * @param mrnyywwc the mrnyywwc to set
	 */
	public void setMrnyywwc(Boolean mrnyywwc) {
		this.mrnyywwc = mrnyywwc;
	}

}
