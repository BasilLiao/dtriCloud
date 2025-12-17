package dtri.com.tw.pgsql.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * @author Basil
 * @see ---共用型---<br>
 *      Front 前端物件(ID之外其他添加@Transient)<br>
 * 
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---單據名稱---<br>
 *      wsl_class_name:單據名稱<br>
 *      ---單據資料---<br>
 *      wsl_class_nb:單別+單號<br>
 *      wsl_type : 單據類型(領料類/入料類)<br>
 *      wsl_schedule : 進度<br>
 *      wsl_fuser : 完成人<br>
 */

@Entity
public class WarehouseSynchronizeFront {
	public WarehouseSynchronizeFront() {
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
		this.wslschedule = "0/0";
		this.wslfuser = "";
	}

	// 共用型
	@Transient
	private Date syscdate;
	@Transient
	private String syscuser;
	@Transient
	private Date sysmdate;
	@Transient
	private String sysmuser;
	@Transient
	private Date sysodate;
	@Transient
	private String sysouser;
	@Transient
	private Boolean sysheader;
	@Transient
	private Integer sysstatus;
	@Transient
	private Integer syssort;
	@Transient
	private String sysnote;
	@Id
	private String id;// 單別+單號+序號
	@Transient
	private String gid;// 單別+單號
	// 單據
	@Transient
	private String wslclassname;// :單據名稱<br>
	@Transient
	private String wslclasssn;// :單別+單號<br>
	@Transient
	private String wsltype;// : 單據類型(領料類/入料類)<br>
	@Transient
	private String wslschedule;// 進度(已完成/總數項目)
	@Transient
	private String wslfuser;// 完成人?

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWslclassname() {
		return wslclassname;
	}

	public void setWslclassname(String wslclassname) {
		this.wslclassname = wslclassname;
	}

	public String getWslclasssn() {
		return wslclasssn;
	}

	public void setWslclasssn(String wslclasssn) {
		this.wslclasssn = wslclasssn;
	}

	public String getWsltype() {
		return wsltype;
	}

	public void setWsltype(String wsltype) {
		this.wsltype = wsltype;
	}

	public String getWslschedule() {
		return wslschedule;
	}

	public void setWslschedule(String wslschedule) {
		this.wslschedule = wslschedule;
	}

	public String getWslfuser() {
		return wslfuser;
	}

	public void setWslfuser(String wslfuser) {
		this.wslfuser = wslfuser;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

}
