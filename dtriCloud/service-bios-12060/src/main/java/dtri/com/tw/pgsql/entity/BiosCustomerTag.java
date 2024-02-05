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
 *      ---BIOS顧客標記---<br>
 *      this.bpid = 0L;ID<br>
 *      this.bvmodel = "";機種別<br>
 *      this.bpsuid = 0L;使用者ID<br>
 *      this.bpsuname = "";使用者名稱<br>
 *      this.bpmnotice = false; 維護客製自動通知<br>
 *      this.bponotice = false; 製令建立自動通知<br>
 */

@Entity
@Table(name = "bios_customer_tag")
@EntityListeners(AuditingEntityListener.class)
public class BiosCustomerTag {
	public BiosCustomerTag() {
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
		// BIOS顧客標記
		this.bctid = 0L;
		this.bctbclid = 0L;// 之後綁定客戶ID
		this.bctname = "";// 客戶公司名稱
		this.bctnabbreviation = "";// 客戶縮寫
		this.bctfeatures = "";// 客戶特性
		this.bctdsettings = "";// 預設設定
		this.bctlogo = "";// 圖片
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bios_customer_tag_seq")
	@SequenceGenerator(name = "bios_customer_tag_seq", sequenceName = "bios_customer_tag_seq", allocationSize = 1)
	@Column(name = "bct_id")
	private Long bctid;
	@Column(name = "bct_bcl_id", nullable = false)
	private Long bctbclid;

	@Column(name = "bct_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bctname;
	@Column(name = "bct_n_abbreviation", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bctnabbreviation;

	@Column(name = "bct_features", nullable = false, columnDefinition = "text default ''")
	private String bctfeatures;
	@Column(name = "bct_d_settings", nullable = false, columnDefinition = "text default ''")
	private String bctdsettings;
	@Column(name = "bct_logo", nullable = false, columnDefinition = "text default ''")
	private String bctlogo;

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

	public Long getBctid() {
		return bctid;
	}

	public void setBctid(Long bctid) {
		this.bctid = bctid;
	}

	public Long getBctbclid() {
		return bctbclid;
	}

	public void setBctbclid(Long bctbclid) {
		this.bctbclid = bctbclid;
	}

	public String getBctname() {
		return bctname;
	}

	public void setBctname(String bctname) {
		this.bctname = bctname;
	}

	public String getBctnabbreviation() {
		return bctnabbreviation;
	}

	public void setBctnabbreviation(String bctnabbreviation) {
		this.bctnabbreviation = bctnabbreviation;
	}

	public String getBctfeatures() {
		return bctfeatures;
	}

	public void setBctfeatures(String bctfeatures) {
		this.bctfeatures = bctfeatures;
	}

	public String getBctdsettings() {
		return bctdsettings;
	}

	public void setBctdsettings(String bctdsettings) {
		this.bctdsettings = bctdsettings;
	}

	public String getBctlogo() {
		return bctlogo;
	}

	public void setBctlogo(String bctlogo) {
		this.bctlogo = bctlogo;
	}
}
