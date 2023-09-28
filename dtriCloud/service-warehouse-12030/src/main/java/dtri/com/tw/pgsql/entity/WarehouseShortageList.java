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
 *      wslid = ID;<br>
 *      wslbslsnnb = "單別_單號_單序";<br>
 *      wslpnumber = "物料號";<br>
 *      wslpname = "物料名";<br>
 *      wslpnqty = 0;需領用量<br>
 *      wslpngqty = 0;已領用量<br>
 *      wslpnlqty = 0;缺少數量<br>
 *      wslfuser = "";領料員<br>
 * 
 */

@Entity
@Table(name = "warehouse_shortage_list")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseShortageList {
	public WarehouseShortageList() {
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
		wslid = 0L;
		wslbslsnnb = "";
		wslpnumber = "";
		wslpname = "";
		wslpnqty = 0;
		wslpngqty = 0;
		wslpnlqty = 0;
		wslfuser = "";
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

	// 倉儲區域負責人-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_shortage_list_seq")
	@SequenceGenerator(name = "warehouse_shortage_list_seq", sequenceName = "warehouse_shortage_list_seq", allocationSize = 1)
	@Column(name = "wsl_id")
	private Long wslid;
	@Column(name = "wsl_bsl_sn_nb", nullable = false, columnDefinition = "varchar(20) default ''")
	private String wslbslsnnb;
	@Column(name = "wsl_p_number", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wslpnumber;
	@Column(name = "wsl_p_name", nullable = false, columnDefinition = "varchar(250) default ''")
	private String wslpname;
	@Column(name = "wsl_pn_qty", nullable = false, columnDefinition = "int default 0")
	private Integer wslpnqty;
	@Column(name = "wsl_pn_g_qty", nullable = false, columnDefinition = "int default 0")
	private Integer wslpngqty;
	@Column(name = "wsl_pn_l_qty", nullable = false, columnDefinition = "int default 0")
	private Integer wslpnlqty;
	@Column(name = "wsl_f_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wslfuser;
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
	public Long getWslid() {
		return wslid;
	}
	public void setWslid(Long wslid) {
		this.wslid = wslid;
	}
	public String getWslbslsnnb() {
		return wslbslsnnb;
	}
	public void setWslbslsnnb(String wslbslsnnb) {
		this.wslbslsnnb = wslbslsnnb;
	}
	public String getWslpnumber() {
		return wslpnumber;
	}
	public void setWslpnumber(String wslpnumber) {
		this.wslpnumber = wslpnumber;
	}
	public String getWslpname() {
		return wslpname;
	}
	public void setWslpname(String wslpname) {
		this.wslpname = wslpname;
	}
	public Integer getWslpnqty() {
		return wslpnqty;
	}
	public void setWslpnqty(Integer wslpnqty) {
		this.wslpnqty = wslpnqty;
	}
	public Integer getWslpngqty() {
		return wslpngqty;
	}
	public void setWslpngqty(Integer wslpngqty) {
		this.wslpngqty = wslpngqty;
	}
	public Integer getWslpnlqty() {
		return wslpnlqty;
	}
	public void setWslpnlqty(Integer wslpnlqty) {
		this.wslpnlqty = wslpnlqty;
	}
	public String getWslfuser() {
		return wslfuser;
	}
	public void setWslfuser(String wslfuser) {
		this.wslfuser = wslfuser;
	}
	
}
