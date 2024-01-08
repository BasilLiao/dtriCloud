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
 *      ---bios版本管理---<br>
 *      this.bnid = 0L;ID<br>
 *      this.bnmkind = "";<br>
 *      this.bnmmail = "";信件對象Mail<br>
 *      this.bnmmailcc = "";信件次要對象Mail<br>
 *      this.bnmcontent = "";信件內容<br>
 *      this.bnmsend = "";發信?狀態<br>
 * 
 * 
 */

@Entity
@Table(name = "basic_notification_mail")
@EntityListeners(AuditingEntityListener.class)
public class BasicNotificationMail {
	public BasicNotificationMail() {
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
		// Bios版本管理
		this.bnmid = 0L;
		this.bnmkind = "";
		this.bnmmail = "";
		this.bnmcontent = "";
		this.bnmsend = false;
	}

	public String getBnmmailcc() {
		return bnmmailcc;
	}
	public void setBnmmailcc(String bnmmailcc) {
		this.bnmmailcc = bnmmailcc;
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
	@Column(name = "sys_h_note", nullable = false, columnDefinition = "text default ''")
	private String syshnote;

	// mail-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basic_notification_mail_seq")
	@SequenceGenerator(name = "basic_notification_mail_seq", sequenceName = "basic_notification_mail_seq", allocationSize = 1)
	@Column(name = "bnm_id")
	private Long bnmid;
	@Column(name = "bnm_kind", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bnmkind;
	@Column(name = "bnm_mail", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bnmmail;
	@Column(name = "bnm_mail_cc", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bnmmailcc;
	@Column(name = "bnm_content", nullable = false, columnDefinition = "text default ''")
	private String bnmcontent;
	@Column(name = "bnm_send", nullable = false, columnDefinition = "boolean default false")
	private Boolean bnmsend;
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

	public String getSyshnote() {
		return syshnote;
	}

	public void setSyshnote(String syshnote) {
		this.syshnote = syshnote;
	}

	public Long getBnmid() {
		return bnmid;
	}

	public void setBnmid(Long bnmid) {
		this.bnmid = bnmid;
	}

	public String getBnmkind() {
		return bnmkind;
	}

	public void setBnmkind(String bnmkind) {
		this.bnmkind = bnmkind;
	}

	public String getBnmmail() {
		return bnmmail;
	}

	public void setBnmmail(String bnmmail) {
		this.bnmmail = bnmmail;
	}

	public String getBnmcontent() {
		return bnmcontent;
	}

	public void setBnmcontent(String bnmcontent) {
		this.bnmcontent = bnmcontent;
	}

	public Boolean getBnmsend() {
		return bnmsend;
	}

	public void setBnmsend(Boolean bnmsend) {
		this.bnmsend = bnmsend;
	}
	

}
