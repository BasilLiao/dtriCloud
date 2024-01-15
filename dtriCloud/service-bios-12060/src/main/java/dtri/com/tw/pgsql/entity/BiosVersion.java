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
 *      this.bvid = 0L;ID<br>
 *      this.bvmodel = "";配對產品 機型 EX:DT313<br>
 *      this.bvcpugenerations = "";CPU世代<br>
 *      this.bvversion = "";BIOS 基本輸入輸出系統 版本<br>
 *      this.bvoversion = "";BIOS 基本輸入輸出系統 (根源)<br>
 *      this.bvcname = "";客戶名稱 EX:BSC<br>
 *      this.bvcnation = "";客戶國家<br>
 *      this.bvecversion = "";BIOS 嵌入式控制器 版本<br>
 *      this.bverpcuser = "";BIOS 工程變更單<br>
 *      this.bvupnote = "";BIOS 更新 資訊<br>
 *      this.bvnosupnote = "";BIOS 不支援 資訊<br>
 *      this.bvbugnote = "";BIOS 已知問題<br>
 *      this.bvclock = false;客戶鎖定<br>
 *      this.bvcagree = false;客戶同意<br>
 *      this.bvmnotice = false;維護客製自動通知<br>
 *      this.bvonotice = false;製令建立自動通知<br>
 *      this.bvpnb = "";跟產品相關系 90階 料號<br>
 *      this.bvmnb = "";跟板階相關系 81階 料號<br>
 * 
 * 
 */

@Entity
@Table(name = "bios_version")
@EntityListeners(AuditingEntityListener.class)
public class BiosVersion {
	public BiosVersion() {
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
		this.bvid = 0L;
		this.bvmodel = "";
		this.bvcpugenerations = "";
		this.bvversion = "";
		this.bvoversion = "";
		//
		this.bvcname = "";
		this.bvcnation = "";
		this.bvecversion = "";
		this.bvecnnb = "";
		//
		this.bvupnote = "";
		this.bvnosupnote = "";
		this.bvbugnote = "";
		//
		this.bvclock = false;
		this.bvcagree = false;
		this.bvmnotice = false;
		this.bvonotice = false;
		//
		this.bvpnb = "";
		this.bvmnb = "";

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

	// 版本管理-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bios_version_seq")
	@SequenceGenerator(name = "bios_version_seq", sequenceName = "bios_version_seq", allocationSize = 1)
	@Column(name = "bv_id")
	private Long bvid;
	@Column(name = "bv_model", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bvmodel;
	@Column(name = "bv_cpu_generations", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bvcpugenerations;
	@Column(name = "bv_version", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String bvversion;
	@Column(name = "bv_o_version", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bvoversion;

	@Column(name = "bv_c_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bvcname;
	@Column(name = "bv_c_nation", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bvcnation;
	@Column(name = "bv_ec_version", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bvecversion;
	@Column(name = "bv_ecn_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bvecnnb;

	@Column(name = "bv_up_note", nullable = false, columnDefinition = "text default ''")
	private String bvupnote;
	@Column(name = "bv_no_sup_note", nullable = false, columnDefinition = "text default ''")
	private String bvnosupnote;
	@Column(name = "bv_bug_note", nullable = false, columnDefinition = "text default ''")
	private String bvbugnote;

	@Column(name = "bv_c_lock", nullable = false, columnDefinition = "boolean default false")
	private Boolean bvclock;
	@Column(name = "bv_c_agree", nullable = false, columnDefinition = "boolean default false")
	private Boolean bvcagree;
	@Column(name = "bv_m_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean bvmnotice;
	@Column(name = "bv_o_notice", nullable = false, columnDefinition = "boolean default false")
	private Boolean bvonotice;

	@Column(name = "bv_p_nb", nullable = false, columnDefinition = "text default ''")
	private String bvpnb;
	@Column(name = "bv_m_nb", nullable = false, columnDefinition = "text default ''")
	private String bvmnb;

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

	public Long getBvid() {
		return bvid;
	}

	public void setBvid(Long bvid) {
		this.bvid = bvid;
	}

	public String getBvmodel() {
		return bvmodel;
	}

	public void setBvmodel(String bvmodel) {
		this.bvmodel = bvmodel;
	}

	public String getBvcpugenerations() {
		return bvcpugenerations;
	}

	public void setBvcpugenerations(String bvcpugenerations) {
		this.bvcpugenerations = bvcpugenerations;
	}

	public String getBvversion() {
		return bvversion;
	}

	public void setBvversion(String bvversion) {
		this.bvversion = bvversion;
	}

	public String getBvoversion() {
		return bvoversion;
	}

	public void setBvoversion(String bvoversion) {
		this.bvoversion = bvoversion;
	}

	public String getBvcname() {
		return bvcname;
	}

	public void setBvcname(String bvcname) {
		this.bvcname = bvcname;
	}

	public String getBvcnation() {
		return bvcnation;
	}

	public void setBvcnation(String bvcnation) {
		this.bvcnation = bvcnation;
	}

	public String getBvecversion() {
		return bvecversion;
	}

	public void setBvecversion(String bvecversion) {
		this.bvecversion = bvecversion;
	}

	public String getBvupnote() {
		return bvupnote;
	}

	public void setBvupnote(String bvupnote) {
		this.bvupnote = bvupnote;
	}

	public String getBvnosupnote() {
		return bvnosupnote;
	}

	public void setBvnosupnote(String bvnosupnote) {
		this.bvnosupnote = bvnosupnote;
	}

	public String getBvbugnote() {
		return bvbugnote;
	}

	public void setBvbugnote(String bvbugnote) {
		this.bvbugnote = bvbugnote;
	}

	public Boolean getBvclock() {
		return bvclock;
	}

	public void setBvclock(Boolean bvclock) {
		this.bvclock = bvclock;
	}

	public Boolean getBvcagree() {
		return bvcagree;
	}

	public void setBvcagree(Boolean bvcagree) {
		this.bvcagree = bvcagree;
	}

	public Boolean getBvmnotice() {
		return bvmnotice;
	}

	public void setBvmnotice(Boolean bvmnotice) {
		this.bvmnotice = bvmnotice;
	}

	public Boolean getBvonotice() {
		return bvonotice;
	}

	public void setBvonotice(Boolean bvonotice) {
		this.bvonotice = bvonotice;
	}

	public String getBvpnb() {
		return bvpnb;
	}

	public void setBvpnb(String bvpnb) {
		this.bvpnb = bvpnb;
	}

	public String getBvmnb() {
		return bvmnb;
	}

	public void setBvmnb(String bvmnb) {
		this.bvmnb = bvmnb;
	}

	public String getBvecnnb() {
		return bvecnnb;
	}

	public void setBvecnnb(String bvecnnb) {
		this.bvecnnb = bvecnnb;
	}

}
