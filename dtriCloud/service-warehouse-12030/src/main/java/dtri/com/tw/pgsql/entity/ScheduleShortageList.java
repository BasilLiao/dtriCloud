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
 *      ---倉儲缺料清單---<br>
 *      sslid = ID;<br>
 *      sslbslsnnb = "單別_單號_單序";<br>
 *      sslpnumber = "物料號";<br>
 *      sslpname = "物料名";<br>
 *      sslpnqty = 0;需領用量<br>
 *      sslpngqty = 0;已領用量<br>
 *      sslpnlqty = 0;缺少數量<br>
 *      sslfuser = "";領料員<br>
 *      sslerpcuser = 建單人<br>
 *      sslfromcommand = 單據來源<br>
 *      syshnote : = 單據備註<br>

 * 
 */

@Entity
@Table(name = "schedule_shortage_list")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleShortageList {
	public ScheduleShortageList() {
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
		// 倉儲缺料-清單
		sslid = 0L;
		sslbslsnnb = "";
		sslpnumber = "";
		sslpname = "";
		sslpnqty = 0;
		sslpngqty = 0;
		sslpnlqty = 0;
		sslfuser = "";
		//
		sslerpcuser = "";
		sslfromcommand = "";
		syshnote = "";
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

	// 倉儲區域負責人-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_shortage_list_seq")
	@SequenceGenerator(name = "schedule_shortage_list_seq", sequenceName = "schedule_shortage_list_seq", allocationSize = 1)
	@Column(name = "ssl_id")
	private Long sslid;
	@Column(name = "ssl_bsl_sn_nb", nullable = false, columnDefinition = "varchar(20) default ''")
	private String sslbslsnnb;
	@Column(name = "ssl_p_number", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sslpnumber;
	@Column(name = "ssl_p_name", nullable = false, columnDefinition = "varchar(250) default ''")
	private String sslpname;
	@Column(name = "ssl_pn_qty", nullable = false, columnDefinition = "int default 0")
	private Integer sslpnqty;
	@Column(name = "ssl_pn_g_qty", nullable = false, columnDefinition = "int default 0")
	private Integer sslpngqty;
	@Column(name = "ssl_pn_l_qty", nullable = false, columnDefinition = "int default 0")
	private Integer sslpnlqty;
	@Column(name = "ssl_f_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sslfuser;

	@Column(name = "ssl_erp_c_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sslerpcuser;
	@Column(name = "ssl_from_command", nullable = false, columnDefinition = "varchar(250) default ''")
	private String sslfromcommand;

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

	public Long getSslid() {
		return sslid;
	}

	public void setSslid(Long sslid) {
		this.sslid = sslid;
	}

	public String getSslbslsnnb() {
		return sslbslsnnb;
	}

	public void setSslbslsnnb(String sslbslsnnb) {
		this.sslbslsnnb = sslbslsnnb;
	}

	public String getSslpnumber() {
		return sslpnumber;
	}

	public void setSslpnumber(String sslpnumber) {
		this.sslpnumber = sslpnumber;
	}

	public String getSslpname() {
		return sslpname;
	}

	public void setSslpname(String sslpname) {
		this.sslpname = sslpname;
	}

	public Integer getSslpnqty() {
		return sslpnqty;
	}

	public void setSslpnqty(Integer sslpnqty) {
		this.sslpnqty = sslpnqty;
	}

	public Integer getSslpngqty() {
		return sslpngqty;
	}

	public void setSslpngqty(Integer sslpngqty) {
		this.sslpngqty = sslpngqty;
	}

	public Integer getSslpnlqty() {
		return sslpnlqty;
	}

	public void setSslpnlqty(Integer sslpnlqty) {
		this.sslpnlqty = sslpnlqty;
	}

	public String getSslfuser() {
		return sslfuser;
	}

	public void setSslfuser(String sslfuser) {
		this.sslfuser = sslfuser;
	}

	public String getSyshnote() {
		return syshnote;
	}

	public void setSyshnote(String syshnote) {
		this.syshnote = syshnote;
	}

	public String getSslerpcuser() {
		return sslerpcuser;
	}

	public void setSslerpcuser(String sslerpcuser) {
		this.sslerpcuser = sslerpcuser;
	}

	public String getSslfromcommand() {
		return sslfromcommand;
	}

	public void setSslfromcommand(String sslfromcommand) {
		this.sslfromcommand = sslfromcommand;
	}

}
