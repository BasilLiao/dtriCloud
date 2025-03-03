package dtri.com.tw.pgsql.entity;

import java.util.Date;

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
 *      ---缺料-負責人通知---<br>
 *      this.ssnid = 0L;ID<br>
 *      this.ssnnb = "";成品BOM號<br>
 *      this.ssnsslerpcuser = "";ERP 建單人<br>
 *      this.ssnsuid = 0L;關聯帳號ID<br>
 *      this.ssnsuname = "";關聯帳號名稱<br>
 *      this.ssnsumail = "";關聯帳號MAIL<br>
 *      this.ssnprimary = 0;主要/次要<br>
 *      this.ssnsnotice = false;缺料通知<br>
 *      this.ssninotice = false;場內生管排程通知(周)<br>
		this.ssnimnotice = false;場內生管異動通知(日)<br>
 */

@Entity
@Table(name = "schedule_shortage_notification")
@EntityListeners(ScheduleShortageNotification.class)
public class ScheduleShortageNotification {
	public ScheduleShortageNotification() {
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
		// 缺料清單負責人通知
		this.ssnid = 0L;
		this.ssnnb = "";
		this.ssnsslerpcuser = "";
		this.ssnsuid = 0L;
		this.ssnsuname = "";
		this.ssnsumail = "";
		this.ssnprimary = 0;
		this.ssnsnotice = false;
		this.ssnonotice = false;
		// 廠內
		this.ssninotice = false;
		this.ssnimnotice = false;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_shortage_notification_seq")
	@SequenceGenerator(name = "schedule_shortage_notification_seq", sequenceName = "schedule_shortage_notification_seq", allocationSize = 1)
	@Column(name = "ssn_id")
	private Long ssnid;
	@Column(name = "ssn_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String ssnnb;
	@Column(name = "ssn_ssl_erp_c_user", nullable = false, columnDefinition = "varchar(100) default ''")
	private String ssnsslerpcuser;

	@Column(name = "ssn_su_id", nullable = false)
	private Long ssnsuid;
	@Column(name = "ssn_su_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String ssnsuname;
	@Column(name = "ssn_su_mail", nullable = false, columnDefinition = "varchar(100) default ''")
	private String ssnsumail;
	@Column(name = "ssn_primary", nullable = false, columnDefinition = "int default 0")
	private Integer ssnprimary;

	@Column(name = "ssn_s_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean ssnsnotice;
	@Column(name = "ssn_o_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean ssnonotice;
	@Column(name = "ssn_i_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean ssninotice;
	@Column(name = "ssn_im_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean ssnimnotice;

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

	public Long getSsnid() {
		return ssnid;
	}

	public void setSsnid(Long ssnid) {
		this.ssnid = ssnid;
	}

	public String getSsnnb() {
		return ssnnb;
	}

	public void setSsnnb(String ssnnb) {
		this.ssnnb = ssnnb;
	}

	public String getSsnsslerpcuser() {
		return ssnsslerpcuser;
	}

	public void setSsnsslerpcuser(String ssnsslerpcuser) {
		this.ssnsslerpcuser = ssnsslerpcuser;
	}

	public Long getSsnsuid() {
		return ssnsuid;
	}

	public void setSsnsuid(Long ssnsuid) {
		this.ssnsuid = ssnsuid;
	}

	public String getSsnsuname() {
		return ssnsuname;
	}

	public void setSsnsuname(String ssnsuname) {
		this.ssnsuname = ssnsuname;
	}

	public String getSsnsumail() {
		return ssnsumail;
	}

	public void setSsnsumail(String ssnsumail) {
		this.ssnsumail = ssnsumail;
	}

	public Integer getSsnprimary() {
		return ssnprimary;
	}

	public void setSsnprimary(Integer ssnprimary) {
		this.ssnprimary = ssnprimary;
	}

	public Boolean getSsnsnotice() {
		return ssnsnotice;
	}

	public void setSsnsnotice(Boolean ssnsnotice) {
		this.ssnsnotice = ssnsnotice;
	}

	public Boolean getSsnonotice() {
		return ssnonotice;
	}

	public void setSsnonotice(Boolean ssnonotice) {
		this.ssnonotice = ssnonotice;
	}

	/**
	 * @return the ssninotice
	 */
	public Boolean getSsninotice() {
		return ssninotice;
	}

	/**
	 * @param ssninotice the ssninotice to set
	 */
	public void setSsninotice(Boolean ssninotice) {
		this.ssninotice = ssninotice;
	}

	/**
	 * @return the ssnimnotice
	 */
	public Boolean getSsnimnotice() {
		return ssnimnotice;
	}

	/**
	 * @param ssnimnotice the ssnimnotice to set
	 */
	public void setSsnimnotice(Boolean ssnimnotice) {
		this.ssnimnotice = ssnimnotice;
	}
}