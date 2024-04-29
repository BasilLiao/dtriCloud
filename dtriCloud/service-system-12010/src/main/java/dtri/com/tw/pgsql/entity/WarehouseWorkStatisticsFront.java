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
 *      ---(倉儲)績效-統計報告---<br>
 *      wws_id : 倉儲_物料號<br>
 * 
 */
@Entity
public class WarehouseWorkStatisticsFront {
	public WarehouseWorkStatisticsFront() {
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

		// 倉儲區域清單-清單
		this.wwsdate = "";//
		this.wwsnames = "";//
		this.wwstimes = "";//
		this.wwspercentage = "";//
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
	private String id;
	@Transient //
	private String gid;//
	// 績效-統計報告
	@Transient
	private String wwsdate;// 時間yyyy-MM-dd
	@Transient
	private String wwsnames;// 人名(JSON)
	@Transient
	private String wwstimes;// 次數(JSON)
	@Transient
	private String wwspercentage;// 百分比
	@Transient
	private Date ssyscdate;// 起始時間
	@Transient
	private Date esyscdate;// 結束時間

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

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getWwsdate() {
		return wwsdate;
	}

	public void setWwsdate(String wwsdate) {
		this.wwsdate = wwsdate;
	}

	public String getWwsnames() {
		return wwsnames;
	}

	public void setWwsnames(String wwsnames) {
		this.wwsnames = wwsnames;
	}

	public String getWwstimes() {
		return wwstimes;
	}

	public void setWwstimes(String wwstimes) {
		this.wwstimes = wwstimes;
	}

	public String getWwspercentage() {
		return wwspercentage;
	}

	public void setWwspercentage(String wwspercentage) {
		this.wwspercentage = wwspercentage;
	}

	public Date getSsyscdate() {
		return ssyscdate;
	}

	public void setSsyscdate(Date ssyscdate) {
		this.ssyscdate = ssyscdate;
	}

	public Date getEsyscdate() {
		return esyscdate;
	}

	public void setEsyscdate(Date esyscdate) {
		this.esyscdate = esyscdate;
	}

}
