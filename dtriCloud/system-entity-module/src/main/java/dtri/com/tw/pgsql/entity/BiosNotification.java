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
 *      ---bios負責人通知---<br>
 *      this.bpid = 0L;ID<br>
 *      this.bvmodel = "";機種別<br>
 *      this.bpsuid = 0L;使用者ID<br>
 *      this.bpsuname = "";使用者名稱<br>
 *      this.bpmnotice = false; 維護客製自動通知<br>
 *      this.bponotice = false; 製令建立自動通知<br>
 */

@Entity
@Table(name = "bios_notification")
@EntityListeners(AuditingEntityListener.class)
public class BiosNotification {
	public BiosNotification() {
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
		// bios負責人通知
		this.bnid = 0L;
		this.bnbvmodel = "";
		this.bnsuid = 0L;
		this.bnsuname = "";
		this.bnsumail = "";
		this.bnprimary = 0;
		this.bnmnotice = false;
		this.bnonotice = false;

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

	// 負責人通知-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bios_notification_seq")
	@SequenceGenerator(name = "bios_notification_seq", sequenceName = "bios_notification_seq", allocationSize = 1)
	@Column(name = "bn_id")
	private Long bnid;
	@Column(name = "bn_bv_model", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bnbvmodel;
	@Column(name = "bn_su_id", nullable = false)
	private Long bnsuid;
	@Column(name = "bn_su_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bnsuname;
	@Column(name = "bn_su_mail", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bnsumail;
	@Column(name = "bn_primary", nullable = false, columnDefinition = "int default 0")
	private Integer bnprimary;

	@Column(name = "bn_m_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean bnmnotice;
	@Column(name = "bn_o_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean bnonotice;

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

	public Long getBnid() {
		return bnid;
	}

	public void setBnid(Long bnid) {
		this.bnid = bnid;
	}

	public String getBnbvmodel() {
		return bnbvmodel;
	}

	public void setBnbvmodel(String bnbvmodel) {
		this.bnbvmodel = bnbvmodel;
	}

	public Long getBnsuid() {
		return bnsuid;
	}

	public void setBnsuid(Long bnsuid) {
		this.bnsuid = bnsuid;
	}

	public String getBnsuname() {
		return bnsuname;
	}

	public void setBnsuname(String bnsuname) {
		this.bnsuname = bnsuname;
	}

	public String getBnsumail() {
		return bnsumail;
	}

	public void setBnsumail(String bnsumail) {
		this.bnsumail = bnsumail;
	}

	public Integer getBnprimary() {
		return bnprimary;
	}

	public void setBnprimary(Integer bnprimary) {
		this.bnprimary = bnprimary;
	}

	public Boolean getBnmnotice() {
		return bnmnotice;
	}

	public void setBnmnotice(Boolean bnmnotice) {
		this.bnmnotice = bnmnotice;
	}

	public Boolean getBnonotice() {
		return bnonotice;
	}

	public void setBnonotice(Boolean bnonotice) {
		this.bnonotice = bnonotice;
	}

}
