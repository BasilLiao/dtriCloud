package dtri.com.tw.pgsql.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * @author Basil
 * 
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---參數設定---<br>
 *      sc_id : 主key <br>
 *      sc_g_id : 群組主key <br>
 *      sc_name : 參數名稱<br>
 *      sc_g_name : 參數群組名稱<br>
 *      sc_value : 值<br>
 * 
 */
@Entity
public class SystemConfigDetail {

	public SystemConfigDetail() {
		// 共用型
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
		// 主體型
		this.scname = "";
		this.scgname = "";
		this.scvalue = "";
		// 前端格式-修改/查詢用(翻譯與欄位)
		this.setSysmdatestart(null);
		this.setSysmdateend(null);
		this.scgid = null;
	}

	// 共用型
	private Date syscdate;
	private String syscuser;
	private Date sysmdate;
	private String sysmuser;
	private Date sysodate;
	private String sysouser;

	private Boolean sysheader;
	private Integer sysstatus;
	private Integer syssort;
	private String sysnote;

	// 主體型
	@Id
	private Long scid;
	private Long scgid;
	private String scname;
	private String scgname;
	private String scvalue;

	// 前端格式-修改/查詢用(翻譯與欄位)
	@Transient
	private Date sysmdatestart;
	@Transient
	private Date sysmdateend;

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

	public Long getScid() {
		return scid;
	}

	public void setScid(Long scid) {
		this.scid = scid;
	}

	public String getScname() {
		return scname;
	}

	public void setScname(String scname) {
		this.scname = scname;
	}

	public String getScgname() {
		return scgname;
	}

	public void setScgname(String scgname) {
		this.scgname = scgname;
	}

	public String getScvalue() {
		return scvalue;
	}

	public void setScvalue(String scvalue) {
		this.scvalue = scvalue;
	}

	public Date getSysmdatestart() {
		return sysmdatestart;
	}

	public void setSysmdatestart(Date sysmdatestart) {
		this.sysmdatestart = sysmdatestart;
	}

	public Date getSysmdateend() {
		return sysmdateend;
	}

	public void setSysmdateend(Date sysmdateend) {
		this.sysmdateend = sysmdateend;
	}

	public Long getScgid() {
		return scgid;
	}

	public void setScgid(Long scgid) {
		this.scgid = scgid;
	}

}
