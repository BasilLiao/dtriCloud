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
import jakarta.persistence.Transient;

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
 * 
 *      ---物料異動-紀錄---<br>
 *      whwmpnb = "";物料號(品號)<br>
 *      whwmslocation = "";物料 主儲位位置 Ex:1F-GG-GG-GG<br>
 *      whtype = "";事件類型 入料/領料/轉料/清點/其他<br>
 *      whcontent = "";事件內容 Ex:XXX使用者_ 從位置XXXX_料號XXXX_ (入料/領料/轉料/清點/其他)的XX數量_
 *      來至於XXX單據<br>
 *      whmac = "";Mac位置 進行事件內容人<br>
 *      wheqty:ERP 數量<br>
 *      whcqty:Cloud 數量<br>
 *      whfuser:完成人<br>
 *      whcheckin:是否核單(未核單/已核單)<br>
 * 
 */

@Entity
@Table(name = "warehouse_history")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseHistory {
	public WarehouseHistory() {
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
		// 倉儲單據過濾器-清單
		this.whwmpnb = "";
		this.whwmslocation = "";
		this.whtype = "";
		this.whcontent = "";
		this.whmac = "";
		this.wheqty = 0;
		this.whcqty = 0;

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

	// 倉儲單據過濾器-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_history_seq")
	@SequenceGenerator(name = "warehouse_history_seq", sequenceName = "warehouse_history_seq", allocationSize = 1)
	@Column(name = "wh_id")
	private Long whid;
	@Column(name = "wh_wm_p_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String whwmpnb;
	@Column(name = "wh_wm_s_location", nullable = false, columnDefinition = "varchar(250) default ''")
	private String whwmslocation;
	@Column(name = "wh_type", nullable = false, columnDefinition = "varchar(50) default ''")
	private String whtype;
	@Column(name = "wh_content", nullable = false, columnDefinition = "text default ''")
	private String whcontent;
	@Column(name = "wh_mac", nullable = false, columnDefinition = "varchar(50) default ''")
	private String whmac;

	@Column(name = "wh_e_qty", nullable = false, columnDefinition = "int default 0")
	private Integer wheqty;
	@Column(name = "wh_c_qty", nullable = false, columnDefinition = "int default 0")
	private Integer whcqty;
	@Column(name = "wh_f_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String whfuser;
	@Column(name = "wh_checkin", nullable = false, columnDefinition = "varchar(50) default ''")
	private String whcheckin;
	
	@Transient
	private Date ssyscdate;//起始時間
	@Transient
	private Date esyscdate;//結束時間

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

	public Long getWhid() {
		return whid;
	}

	public void setWhid(Long whid) {
		this.whid = whid;
	}

	public String getWhwmpnb() {
		return whwmpnb;
	}

	public void setWhwmpnb(String whwmpnb) {
		this.whwmpnb = whwmpnb;
	}

	public String getWhwmslocation() {
		return whwmslocation;
	}

	public void setWhwmslocation(String whwmslocation) {
		this.whwmslocation = whwmslocation;
	}

	public String getWhtype() {
		return whtype;
	}

	public void setWhtype(String whtype) {
		this.whtype = whtype;
	}

	public String getWhcontent() {
		return whcontent;
	}

	public void setWhcontent(String whcontent) {
		this.whcontent = whcontent;
	}

	public String getWhmac() {
		return whmac;
	}

	public void setWhmac(String whmac) {
		this.whmac = whmac;
	}

	public Integer getWheqty() {
		return wheqty;
	}

	public void setWheqty(Integer wheqty) {
		this.wheqty = wheqty;
	}

	public Integer getWhcqty() {
		return whcqty;
	}

	public void setWhcqty(Integer whcqty) {
		this.whcqty = whcqty;
	}

	public String getWhfuser() {
		return whfuser;
	}

	public void setWhfuser(String whfuser) {
		this.whfuser = whfuser;
	}

	public String getWhcheckin() {
		return whcheckin;
	}

	public void setWhcheckin(String whcheckin) {
		this.whcheckin = whcheckin;
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
