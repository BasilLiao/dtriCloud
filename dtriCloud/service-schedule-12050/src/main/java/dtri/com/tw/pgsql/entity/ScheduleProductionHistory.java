package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
 *      ---生產紀錄---<br>
 *      this.sphid = 0L;<br>
 *      this.sphbpmnb = "";BOM 的產品號<br>
 *      this.sphbpmmodel = "";BOM 的產品型號<br>
 *      this.sphbpmtype = "";產品歸類 0 = 開發BOM/1 = 產品BOM/ 2 = 配件BOM/3 = 半成品BOM/ 3 =
 *      板階BOM<br>
 *      this.sphbpmtypename = "";產品歸類 名稱<br>
 *      this.sphbisitem = "";{ items:[ {"bisid(ID)":ID, "bisgid":GID,
 *      "bisnb(物料號)":XXXX, "bisname(物料名)":XXXX, "bisqty(數量)":100,
 *      "bisgname":"群組名稱", "bisgfname(正規化群組值)":"XX XX XX",
 *      "bisfname(正規化項目值)":"XX,XX,XX", "bissdescripion(短敘述)":"",
 *      "bisprocess(製程別)":"", }, {"bisid(ID)": ..... }],
 *      basic:[物料號1_數量,物料號2_數量...] }<br>
 *      this.sphbpsnv = "";產品參數設置 [Name_Value ,.....]<br>
 *      this.sphbpsuser = "";BOM負責人<br>
 *      this.sphpon = "";製令單號<br>
 *      this.sphonb = "";訂單號<br>
 *      this.sphoname = "";訂單客戶<br>
 *      this.sphocountry = "";訂單國家<br>
 *      this.sphhdate = "";預計出貨日<br>
 *      this.sphfrom = "";規格來源 生管自訂/產品經理<br>
 *      this.sphstatus = 0;["作廢單_0","有效單_1","自訂單_2"]<br>
 *      this.sphprogress = 0;進度
 *      ["生產注意事項(未開)_1","生產注意事項(已開)_2","流程卡(已開)_3","生產中_4","生產結束_5"] <br>
 *      this.sphssn = "";開始SN<br>
 *      this.sphesn = "";結束SN<br>
 *      this.sphpmnote = "";產品經理事項<br>
 *      this.sphscnote = "";生管備註事項<br>
 *      this.sphprnote1 = "";生產事項1<br>
 *      this.sphprnote2 = "";生產事項2<br>
 * 
 * 
 */

@Entity
@Table(name = "schedule_production_history")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleProductionHistory {
	public ScheduleProductionHistory() {
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
		// 生產紀錄
		this.sphid = 0L;
		this.sphbpmnb = "";
		this.sphbpmmodel = "";
		this.sphbpmtype = "";
		this.sphbpmtypename = "";
		this.sphbisitem = "";
		this.sphbpsnv = "";
		this.sphbpsuser = "";
		this.setSphpon("");
		this.sphonb = "";
		this.sphoname = "";
		this.sphocountry = "";
		this.sphhdate = "";
		this.sphfrom = "";
		this.sphstatus = 0;
		this.sphprogress = 0;
		this.sphssn = "";
		this.sphesn = "";
		this.sphpmnote = "";
		this.sphscnote = "";
		this.sphprnote1 = "";
		this.sphprnote2 = "";
	}

	@PrePersist
	protected void onCreate() {
		this.syscdate = new Date(System.currentTimeMillis() / 1000 * 1000); // 去除毫秒
		this.syscuser = "system";
		this.sysmdate = new Date(System.currentTimeMillis() / 1000 * 1000);
		this.sysmuser = "system";
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

	// 生產紀錄-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_production_history_seq")
	@SequenceGenerator(name = "schedule_production_history_seq", sequenceName = "schedule_production_history_seq", allocationSize = 1)
	@Column(name = "sph_id")
	private Long sphid;

	@Column(name = "sph_bpm_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphbpmnb;
	@Column(name = "sph_bpm_model", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphbpmmodel;
	@Column(name = "sph_bpm_type", nullable = false, columnDefinition = "varchar(10) default ''")
	private String sphbpmtype;
	@Column(name = "sph_bpm_type_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphbpmtypename;
	@Column(name = "sph_bis_item", nullable = false, columnDefinition = "text default ''")
	private String sphbisitem;
	@Column(name = "sph_bps_nv", nullable = false, columnDefinition = "text default ''")
	private String sphbpsnv;
	@Column(name = "sph_bps_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphbpsuser;
	//
	@Column(name = "sph_pon", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphpon;
	@Column(name = "sph_o_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphonb;
	@Column(name = "sph_o_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphoname;
	@Column(name = "sph_o_country", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphocountry;
	@Column(name = "sph_h_date", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphhdate;
	@Column(name = "sph_from", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphfrom;
	@Column(name = "sph_status", nullable = false, columnDefinition = "int default 0")
	private Integer sphstatus;
	@Column(name = "sph_progress", nullable = false, columnDefinition = "int default 0")
	private Integer sphprogress;
	//
	@Column(name = "sph_s_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphssn;
	@Column(name = "sph_e_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sphesn;
	//
	@Column(name = "sph_pm_note", nullable = false, columnDefinition = "text default ''")
	private String sphpmnote;
	@Column(name = "sph_sc_note", nullable = false, columnDefinition = "text default ''")
	private String sphscnote;
	@Column(name = "sph_pr_note1", nullable = false, columnDefinition = "text default ''")
	private String sphprnote1;
	@Column(name = "sph_pr_note2", nullable = false, columnDefinition = "text default ''")
	private String sphprnote2;

	@Column(name = "sph_pr_nv", nullable = false, columnDefinition = "text default ''")
	private String sphprnv;
	@Column(name = "sph_sc_package", nullable = false, columnDefinition = "text default ''")
	private String sphscpackage;
	@Column(name = "sph_sc_nv", nullable = false, columnDefinition = "text default ''")
	private String sphscnv;

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

	public Long getSphid() {
		return sphid;
	}

	public void setSphid(Long sphid) {
		this.sphid = sphid;
	}

	public String getSphbpmnb() {
		return sphbpmnb;
	}

	public void setSphbpmnb(String sphbpmnb) {
		this.sphbpmnb = sphbpmnb;
	}

	public String getSphbpmmodel() {
		return sphbpmmodel;
	}

	public void setSphbpmmodel(String sphbpmmodel) {
		this.sphbpmmodel = sphbpmmodel;
	}

	public String getSphbpmtype() {
		return sphbpmtype;
	}

	public void setSphbpmtype(String sphbpmtype) {
		this.sphbpmtype = sphbpmtype;
	}

	public String getSphbpmtypename() {
		return sphbpmtypename;
	}

	public void setSphbpmtypename(String sphbpmtypename) {
		this.sphbpmtypename = sphbpmtypename;
	}

	public String getSphbisitem() {
		return sphbisitem;
	}

	public void setSphbisitem(String sphbisitem) {
		this.sphbisitem = sphbisitem;
	}

	public String getSphbpsnv() {
		return sphbpsnv;
	}

	public void setSphbpsnv(String sphbpsnv) {
		this.sphbpsnv = sphbpsnv;
	}

	public String getSphbpsuser() {
		return sphbpsuser;
	}

	public void setSphbpsuser(String sphbpsuser) {
		this.sphbpsuser = sphbpsuser;
	}

	public String getSphonb() {
		return sphonb;
	}

	public void setSphonb(String sphonb) {
		this.sphonb = sphonb;
	}

	public String getSphoname() {
		return sphoname;
	}

	public void setSphoname(String sphoname) {
		this.sphoname = sphoname;
	}

	public String getSphocountry() {
		return sphocountry;
	}

	public void setSphocountry(String sphocountry) {
		this.sphocountry = sphocountry;
	}

	public String getSphhdate() {
		return sphhdate;
	}

	public void setSphhdate(String sphhdate) {
		this.sphhdate = sphhdate;
	}

	public String getSphfrom() {
		return sphfrom;
	}

	public void setSphfrom(String sphfrom) {
		this.sphfrom = sphfrom;
	}

	public Integer getSphstatus() {
		return sphstatus;
	}

	public void setSphstatus(Integer sphstatus) {
		this.sphstatus = sphstatus;
	}

	public Integer getSphprogress() {
		return sphprogress;
	}

	public void setSphprogress(Integer sphprogress) {
		this.sphprogress = sphprogress;
	}

	public String getSphssn() {
		return sphssn;
	}

	public void setSphssn(String sphssn) {
		this.sphssn = sphssn;
	}

	public String getSphesn() {
		return sphesn;
	}

	public void setSphesn(String sphesn) {
		this.sphesn = sphesn;
	}

	public String getSphpmnote() {
		return sphpmnote;
	}

	public void setSphpmnote(String sphpmnote) {
		this.sphpmnote = sphpmnote;
	}

	public String getSphscnote() {
		return sphscnote;
	}

	public void setSphscnote(String sphscnote) {
		this.sphscnote = sphscnote;
	}

	public String getSphprnote1() {
		return sphprnote1;
	}

	public void setSphprnote1(String sphprnote1) {
		this.sphprnote1 = sphprnote1;
	}

	public String getSphprnote2() {
		return sphprnote2;
	}

	public void setSphprnote2(String sphprnote2) {
		this.sphprnote2 = sphprnote2;
	}

	public String getSphprnv() {
		return sphprnv;
	}

	public void setSphprnv(String sphprnv) {
		this.sphprnv = sphprnv;
	}

	public String getSphscpackage() {
		return sphscpackage;
	}

	public void setSphscpackage(String sphscpackage) {
		this.sphscpackage = sphscpackage;
	}

	public String getSphscnv() {
		return sphscnv;
	}

	public void setSphscnv(String sphscnv) {
		this.sphscnv = sphscnv;
	}

	public String getSphpon() {
		return sphpon;
	}

	public void setSphpon(String sphpon) {
		this.sphpon = sphpon;
	}

}
