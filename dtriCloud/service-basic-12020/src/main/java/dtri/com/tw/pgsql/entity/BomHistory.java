package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
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
 *      ---BOM異動-紀錄---<br>
 *      private Long bhid;KEY<br>
 *      private String bhatype;動作種類CUD (AllNew = 全新的BOM/ New = 新的物料/
 *      [Old/Update]新舊料替換/ Delete=移除物料) EX:Old_1 對應 Update_1<br>
 *      private String bhnb;BOM號 EX:90-313<br>
 *      private String bhmodel;BOM型號 EX:504TY<br>
 *      private String bhpnb;物料號<br>
 *      private Integer bhpqty;數量<br>
 *      private String bhpprocess;製成別<br>
 *      private Boolean bhnotification;是否已登記通知<br>
 *      private Integer bhlevel;幾層?<br>
 * 
 */

@Entity
@Table(name = "bom_history")
@EntityListeners(AuditingEntityListener.class)
public class BomHistory {
	public BomHistory() {
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
		// BOM異動-清單
		this.bhid = null;
		this.bhatype = "";
		this.bhnb = "";
		this.bhmodel = "";
		this.bhpqty = 0;
		this.bhpprocess = "";
		this.bhnotification = false;
		this.bhlevel = 0;
	}

	@PreUpdate
	protected void onUpdate() {
		this.sysmdate = new Date(System.currentTimeMillis() / 1000 * 1000);
		this.sysmuser = "system";
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bom_history_seq")
	@SequenceGenerator(name = "bom_history_seq", sequenceName = "bom_history_seq", allocationSize = 1)
	@Column(name = "bn_id")
	private Long bhid;
	@Column(name = "bh_a_type", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bhatype;
	@Column(name = "bh_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bhnb;
	@Column(name = "bh_model", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bhmodel;
	@Column(name = "bh_p_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bhpnb;
	@Column(name = "bh_p_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bhpqty;

	@Column(name = "bh_level", nullable = false, columnDefinition = "int default 0")
	private Integer bhlevel;

	@Column(name = "bh_p_process", nullable = false, columnDefinition = "varchar(100) default ''")
	private String bhpprocess;
	@Column(name = "bh_notification", nullable = false, columnDefinition = "boolean default false")
	private Boolean bhnotification;

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

	public Long getBhid() {
		return bhid;
	}

	public void setBhid(Long bhid) {
		this.bhid = bhid;
	}

	public String getBhatype() {
		return bhatype;
	}

	public void setBhatype(String bhatype) {
		this.bhatype = bhatype;
	}

	public String getBhnb() {
		return bhnb;
	}

	public void setBhnb(String bhnb) {
		this.bhnb = bhnb;
	}

	public String getBhmodel() {
		return bhmodel;
	}

	public void setBhmodel(String bhmodel) {
		this.bhmodel = bhmodel;
	}

	public String getBhpnb() {
		return bhpnb;
	}

	public void setBhpnb(String bhpnb) {
		this.bhpnb = bhpnb;
	}

	public Integer getBhpqty() {
		return bhpqty;
	}

	public void setBhpqty(Integer bhpqty) {
		this.bhpqty = bhpqty;
	}

	public String getBhpprocess() {
		return bhpprocess;
	}

	public void setBhpprocess(String bhpprocess) {
		this.bhpprocess = bhpprocess;
	}

	public Boolean getBhnotification() {
		return bhnotification;
	}

	public void setBhnotification(Boolean bhnotification) {
		this.bhnotification = bhnotification;
	}

	public Integer getBhlevel() {
		return bhlevel;
	}

	public void setBhlevel(Integer bhlevel) {
		this.bhlevel = bhlevel;
	}

}
