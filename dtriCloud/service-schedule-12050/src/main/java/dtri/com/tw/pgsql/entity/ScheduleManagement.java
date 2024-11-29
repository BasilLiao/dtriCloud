package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.google.gson.JsonObject;

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
 *      ---外包排程清單---<br>
 *      smid = ID;<br>
 *      private String smodate;<br>
 *      private String smfdate;<br>
 *      //產品資訊<br>
 *      private String smnb;<br>
 *      private String smpnb;<br>
 *      private String smpname;<br>
 *      private String smpspecifications;<br>
 *      //訂單狀況<br>
 *      private Integer smrqty;<br>
 *      private Integer smokqty;<br>
 *      private String smrdate;<br>
 *      private String smokdate;<br>
 *      private String smstatus;<br>
 *      private String sm_c_order;<br>
 *      private String sm_c_note;<br>
 *      private String smvinorder;<br>
 *      // 產品備註<br>
 *      private String smpmnote;<br>
 *      // 生管備註<br>
 *      private String smmonote;<br>
 *      private Integer smmostatus;<br>
 *      // 物控備註<br>
 *      private String smprnote;<br>
 *      private String smprdate;<br>
 *      private Integer smprstatus;<br>
 *      // 倉庫備註<br>
 *      private String smvnnote;<br>
 *      private Integer smvnstatus;<br>
 *      private String smmesnote;<br>
 *      //<br>
 *      private String smwdate;<br>
 *      private String smsum;<br>
 * 
 * 
 */

@Entity
@Table(name = "schedule_management")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleManagement {
	public ScheduleManagement() {
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
		// 場內排程清單

		// 標記更新
		JsonObject tagString = new JsonObject();
		tagString.addProperty("all", "");
		// 生管
		tagString.addProperty("smmonote", "");// 生管備註(格式化)-人+時間+內容
		tagString.addProperty("smmostatus", "");// 生管 開單狀況/0=未開注意事項/1=已開注意事項/2=已核准流程卡
		// 物控
		tagString.addProperty("smprnote", "");// 物控備註(格式化) json格式
		tagString.addProperty("smprdate", "");// 物控-最快齊料日
		tagString.addProperty("smprstatus", "");// 物控0=未確認/1未齊料/2已齊料
		// 倉庫
		tagString.addProperty("smvnnote", "");// 倉儲備註(格式化) json格式
		tagString.addProperty("smvnstatus", "");// 倉儲0 = 未撿料/1=撿料中/2=已完成
		// 製造
		tagString.addProperty("smmesnote", "");// 製造備註(格式化) json格式
		// 產品經理
		tagString.addProperty("smpmnote", "");// 製造備註(格式化) json格式

		// 單據
		tagString.addProperty("sorqty", "");
		tagString.addProperty("sookqty", "");
		tagString.addProperty("sostatus", "");
		tagString.addProperty("sonote", "");
		tagString.addProperty("sofname", "");
		// Locked
		tagString.addProperty("locked", "");
		tagString.addProperty("lockedtime", "");
		tagString.addProperty("lockeduser", "");

		this.setTag(tagString.toString());
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

	// 場內生管排程-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_management_seq")
	@SequenceGenerator(name = "schedule_management_seq", sequenceName = "schedule_management_seq", allocationSize = 1)
	@Column(name = "sm_id")
	private Long smid;

	@Column(name = "sm_note", nullable = false, unique = true, columnDefinition = "text default ''")
	private String smnote;
	@Column(name = "sm_nb", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String smnb;
	@Column(name = "sm_p_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String smpnb;
	@Column(name = "sm_p_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String smpname;
	@Column(name = "sm_p_specifications", nullable = false, columnDefinition = "text default ''")
	private String smpspecifications;

	@Column(name = "sm_r_qty", nullable = false, columnDefinition = "int default 0")
	private Integer smrqty;
	@Column(name = "sm_ok_qty", nullable = false, columnDefinition = "int default 0")
	private Integer smokqty;
	@Column(name = "sm_r_date", nullable = false, columnDefinition = "varchar(10) default ''")
	private String smrdate;
	@Column(name = "sm_ok_date", nullable = false, columnDefinition = "varchar(10) default ''")
	private String smokdate;
	@Column(name = "sm_status", nullable = false, columnDefinition = "varchar(10) default ''")
	private String smstatus;

	@Column(name = "sm_c_order", nullable = false, columnDefinition = "varchar(100) default ''")
	private String smcorder;
	@Column(name = "sm_c_note", nullable = false, columnDefinition = "varchar(100) default ''")
	private String smcnote;
	@Column(name = "sm_v_in_order", nullable = false, columnDefinition = "varchar(100) default ''")
	private String smvinorder;
	// 產品備註
	@Column(name = "sm_pm_note", nullable = false, columnDefinition = "text default ''")
	private String smpmnote;
	// 生管備註
	@Column(name = "sm_mo_note", nullable = false, columnDefinition = "text default ''")
	private String smmonote;
	@Column(name = "sm_mo_status", nullable = false, columnDefinition = "int default 0")
	private Integer smmostatus;
	// 物控備註
	@Column(name = "sm_pr_note", nullable = false, columnDefinition = "text default ''")
	private String smprnote;
	@Column(name = "sm_pr_date", nullable = false, columnDefinition = "varchar(10) default ''")
	private String smprdate;
	@Column(name = "sm_pr_status", nullable = false, columnDefinition = "int default 0")
	private Integer smprstatus;

	// 倉庫備註
	@Column(name = "sm_vn_note", nullable = false, columnDefinition = "text default ''")
	private String smvnnote;
	@Column(name = "sm_vn_status", nullable = false, columnDefinition = "int default 0")
	private Integer smvnstatus;

	// 製造備註
	@Column(name = "sm_mes_note", nullable = false, columnDefinition = "text default ''")
	private String smmesnote;

	@Column(name = "sm_w_date", nullable = false, columnDefinition = "varchar(50) default ''")
	private String smwdate;
	@Column(name = "smsum", nullable = false, columnDefinition = "text default ''")
	private String smsum;

	@Transient
	private String smprdates;// 預計期料日-起
	@Transient
	private String smprdatee;// 預計期料日-終

	/**
	 * 修改>亮燈?新的def<br>
	 * {"all":"2024-01-01",全新<br>
	 * "smpmnote":"2024-01-01", 產品備註(格式) def:""空值<br>
	 * "smmonote":"2024-01-01", 生管備註(格式) def:""空值<br>
	 * "smprnote":"2024-01-01", 物控備註(格式) def:""空值<br>
	 * "smvnnote":"2024-01-01", 倉庫備註(格式) def:""空值<br>
	 * "smmesnote":"2024-01-01", 製造備註(格式) def:""空值<br>
	 * 
	 * "smrqty":"2024-01-01", 預計生產<br>
	 * "smokqty":"2024-01-01", 已經生產<br>
	 * "smrdate":"2024-01-01", 預計開工日<br>
	 * "smokdate":"2024-01-01", 預計完工日<br>
	 * "smstatus":"2024-01-01", 狀態 0=暫停中/1=未生產/2=已發料/3=生產中 Y=已完工/y=指定完工<br>
	 * "smnote":"2024-01-01", 製令備註(客戶/國家/訂單)<br>
	 * 
	 * 
	 * "smprdate":"2024-01-01", 物控 預計其料日<br>
	 * 
	 * locked, true/false<br>
	 * lockedtime, Long 時間戳記 (5分鐘)<br>
	 * lockeduser, Acc(綁定帳號)<br>
	 * }<br>
	 **/
	@Transient
	private String tag;

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

	public Long getSmid() {
		return smid;
	}

	public void setSmid(Long smid) {
		this.smid = smid;
	}

	public String getSmnb() {
		return smnb;
	}

	public void setSmnb(String smnb) {
		this.smnb = smnb;
	}

	public String getSmpnb() {
		return smpnb;
	}

	public void setSmpnb(String smpnb) {
		this.smpnb = smpnb;
	}

	public String getSmpname() {
		return smpname;
	}

	public void setSmpname(String smpname) {
		this.smpname = smpname;
	}

	public String getSmpspecifications() {
		return smpspecifications;
	}

	public void setSmpspecifications(String smpspecifications) {
		this.smpspecifications = smpspecifications;
	}

	public Integer getSmrqty() {
		return smrqty;
	}

	public void setSmrqty(Integer smrqty) {
		this.smrqty = smrqty;
	}

	public Integer getSmokqty() {
		return smokqty;
	}

	public void setSmokqty(Integer smokqty) {
		this.smokqty = smokqty;
	}

	public String getSmstatus() {
		return smstatus;
	}

	public void setSmstatus(String smstatus) {
		this.smstatus = smstatus;
	}

	public String getSmcorder() {
		return smcorder;
	}

	public void setSmcorder(String smcorder) {
		this.smcorder = smcorder;
	}

	public String getSmcnote() {
		return smcnote;
	}

	public void setSmcnote(String smcnote) {
		this.smcnote = smcnote;
	}

	public String getSmvinorder() {
		return smvinorder;
	}

	public void setSmvinorder(String smvinorder) {
		this.smvinorder = smvinorder;
	}

	public String getSmpmnote() {
		return smpmnote;
	}

	public void setSmpmnote(String smpmnote) {
		this.smpmnote = smpmnote;
	}

	public String getSmmonote() {
		return smmonote;
	}

	public void setSmmonote(String smmonote) {
		this.smmonote = smmonote;
	}

	public Integer getSmmostatus() {
		return smmostatus;
	}

	public void setSmmostatus(Integer smmostatus) {
		this.smmostatus = smmostatus;
	}

	public String getSmprnote() {
		return smprnote;
	}

	public void setSmprnote(String smprnote) {
		this.smprnote = smprnote;
	}

	public String getSmprdate() {
		return smprdate;
	}

	public void setSmprdate(String smprdate) {
		this.smprdate = smprdate;
	}

	public Integer getSmprstatus() {
		return smprstatus;
	}

	public void setSmprstatus(Integer smprstatus) {
		this.smprstatus = smprstatus;
	}

	public String getSmvnnote() {
		return smvnnote;
	}

	public void setSmvnnote(String smvnnote) {
		this.smvnnote = smvnnote;
	}

	public Integer getSmvnstatus() {
		return smvnstatus;
	}

	public void setSmvnstatus(Integer smvnstatus) {
		this.smvnstatus = smvnstatus;
	}

	public String getSmmesnote() {
		return smmesnote;
	}

	public void setSmmesnote(String smmesnote) {
		this.smmesnote = smmesnote;
	}

	public String getSmwdate() {
		return smwdate;
	}

	public void setSmwdate(String smwdate) {
		this.smwdate = smwdate;
	}

	public String getSmsum() {
		return smsum;
	}

	public void setSmsum(String smsum) {
		this.smsum = smsum;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getSmprdates() {
		return smprdates;
	}

	public void setSmprdates(String smprdates) {
		this.smprdates = smprdates;
	}

	public String getSmprdatee() {
		return smprdatee;
	}

	public void setSmprdatee(String smprdatee) {
		this.smprdatee = smprdatee;
	}

	public String getSmrdate() {
		return smrdate;
	}

	public void setSmrdate(String smrdate) {
		this.smrdate = smrdate;
	}

	public String getSmokdate() {
		return smokdate;
	}

	public void setSmokdate(String smokdate) {
		this.smokdate = smokdate;
	}

	public String getSmnote() {
		return smnote;
	}

	public void setSmnote(String smnote) {
		this.smnote = smnote;
	}

}