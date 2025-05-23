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
 *      ---廠內排程清單---<br>
 *      siid = ID;<br>
 *      siywdate = 年周:例如 2022-W55 YYYY(西元年)-W00(周)<br>
 *      siodate = 預計開工時間<br>
 *      sifdate = 預計完工時間<br>
 *      sinb = 製令單號<br>
 *      sipnb = 產品品號<br>
 *      sipname = 產品品名<br>
 *      sipspecifications = 產品規格<br>
 *      sirqty = 預計生產<br>
 *      siokqty = 已經生產<br>
 *      sistatus = 狀態 0=暫停中/1=未生產/2=已發料/3=生產中 Y=已完工/y=指定完工<br>
 *      sinote = 製令備註(客戶/國家/訂單)<br>
 *      sifname = 加工廠(代號+中文)<br>
 *      sifodate = 加工廠開工日期<br>
 *      sifokdate = 加工廠完工日期<br>
 *      siuname = 開單人名<br>
 *      siscstatus : 生管狀態(自訂義) 生管備料狀態 : 自訂義(生管 開單狀況 0=未開注意事項 1=已開注意事項
 *      2=已核准流程卡)<br>
 *      siscnote = 生管備註(格式) 人+時間+內容 [{date:2024-01-01 10:10:10,
 *      user:Test,content:""},{}]<br>
 *      simcnote = 物控備註(格式) 人+時間+內容<br>
 *      simcdate = 物控 預計其料日<br>
 *      simcstatus = 0=未確認/1未齊料/2已齊料<br>
 *      siwmnote = 倉庫備註(格式) 人+時間+內容<br>
 *      siwmprogress = 倉庫 備料進度 50/200<br>
 *      simpnote = 製造備註(格式) 人+時間+內容<br>
 *      simpprogress = 製造 生產進度Ex: 100/500<br>
 *      sicorder = "客戶訂單";<br>
 *      sicnote = "客戶備註";<br>
 *      sicpnb = "客戶成品號";<br>
 *      sisum = 資料串 比對<br>
 * 
 * 
 */

@Entity
@Table(name = "schedule_infactory")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleInfactory {
	public ScheduleInfactory() {
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
		// 外包排程清單
		this.siid = null;
		this.siywdate = "";
		this.siodate = "";
		this.sifdate = "";
		this.sinb = "";
		this.sipnb = "";
		this.sipname = "";
		this.sipspecifications = "";
		this.sirqty = 0;
		this.siokqty = 0;
		this.sistatus = "0";
		this.sinote = "";
		this.sifname = "";
		this.sifodate = "";
		this.sifokdate = "";
		this.siuname = "";
		this.siscnote = "[]";
		this.siscstatus = 0;//生管狀態(自訂義) 生管備料狀態 : 自訂義(生管 開單狀況 0=未開注意事項 1=已開注意事項
		this.simcnote = "[]";
		this.simcdate = "";
		this.simcstatus = 0;// 0=未確認/1未齊料/2已齊料
		this.siwmnote = "[]";
		this.siwmprogress = "";
		this.simpnote = "[]";
		this.simpprogress = "";
		this.sipmnote = "";
		//
		this.sicorder = "";
		this.sicnote = "";
		this.sicpnb = "";

		this.sisum = "";// 異動資料判斷?
		// 標記更新
		JsonObject tagString = new JsonObject();
		tagString.addProperty("all", "");
		// 生管
		tagString.addProperty("sifodate", "");
		tagString.addProperty("sifokdate", "");
		tagString.addProperty("siscstatus", "");
		tagString.addProperty("siscnote", "");
		// 物控
		tagString.addProperty("simcnote", "");
		tagString.addProperty("simcstatus", "");
		tagString.addProperty("simcdate", "");
		// 倉庫
		tagString.addProperty("siwmnote", "");
		tagString.addProperty("siwmprogress", "");
		// 製造
		tagString.addProperty("simpnote", "");
		tagString.addProperty("simpprogress", "");
		// 單據
		tagString.addProperty("sirqty", "");
		tagString.addProperty("siokqty", "");
		tagString.addProperty("sistatus", "");
		tagString.addProperty("sinote", "");
		tagString.addProperty("sifname", "");
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

	// 場內排程-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_infactory_seq")
	@SequenceGenerator(name = "schedule_infactory_seq", sequenceName = "schedule_infactory_seq", allocationSize = 1)
	@Column(name = "si_id")
	private Long siid;
	@Column(name = "si_yw_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String siywdate;
	@Column(name = "si_o_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String siodate;
	@Column(name = "si_f_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String sifdate;

	@Column(name = "si_nb", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String sinb;
	@Column(name = "si_p_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String sipnb;
	@Column(name = "si_p_name", nullable = false, columnDefinition = "varchar(300) default ''")
	private String sipname;

	@Column(name = "si_p_specifications", nullable = false, columnDefinition = "text default ''")
	private String sipspecifications;
	@Column(name = "si_r_qty", nullable = false, columnDefinition = "int default 0")
	private Integer sirqty;
	@Column(name = "si_ok_qty", nullable = false, columnDefinition = "int default 0")
	private Integer siokqty;
	@Column(name = "si_status", nullable = false, columnDefinition = "varchar(10) default ''")
	private String sistatus;
	@Column(name = "si_note", nullable = false, columnDefinition = "text default ''")
	private String sinote;
	@Column(name = "si_f_name", nullable = false, columnDefinition = "varchar(500) default ''")
	private String sifname;

	@Column(name = "si_f_o_date", nullable = false, columnDefinition = "varchar(120) default ''")
	private String sifodate;
	@Column(name = "si_f_ok_date", nullable = false, columnDefinition = "varchar(120) default ''")
	private String sifokdate;
	@Column(name = "si_u_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String siuname;

	@Column(name = "si_sc_status", nullable = false, columnDefinition = "int default 0")
	private Integer siscstatus;
	@Column(name = "si_sc_note", nullable = false, columnDefinition = "text default ''")
	private String siscnote;
	@Column(name = "si_mc_note", nullable = false, columnDefinition = "text default ''")
	private String simcnote;
	@Column(name = "si_mc_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String simcdate;
	@Column(name = "si_mc_status", nullable = false, columnDefinition = "int default 0")
	private Integer simcstatus;

	@Column(name = "si_wm_note", nullable = false, columnDefinition = "text default ''")
	private String siwmnote;
	@Column(name = "si_wm_progress", nullable = false, columnDefinition = "varchar(50) default ''")
	private String siwmprogress;
	@Column(name = "si_mp_note", nullable = false, columnDefinition = "text default ''")
	private String simpnote;
	@Column(name = "si_mp_progress", nullable = false, columnDefinition = "varchar(50) default ''")
	private String simpprogress;
	@Column(name = "si_pm_note", nullable = false, columnDefinition = "text default ''")
	private String sipmnote;
	//
	@Column(name = "si_c_order", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sicorder;
	@Column(name = "si_c_note", nullable = false, columnDefinition = "text default ''")
	private String sicnote;
	@Column(name = "si_c_p_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sicpnb;

	@Column(name = "si_sum", nullable = false, columnDefinition = "text default ''")
	private String sisum;

	@Transient
	private String simcdates;// 預計期料日-起
	@Transient
	private String simcdatee;// 預計期料日-終

	/**
	 * 新的def<br>
	 * {"all":"2024-01-01",全新<br>
	 * "siscnote":"2024-01-01", 生管備註(格式) def:""空值<br>
	 * "simcnote":"2024-01-01", 物控備註(格式) def:""空值<br>
	 * "siwmnote":"2024-01-01", 倉庫備註(格式) def:""空值<br>
	 * "simpnote":"2024-01-01", 製造備註(格式) def:""空值<br>
	 * "sirqty":"2024-01-01", 預計生產<br>
	 * "siokqty":"2024-01-01", 已經生產<br>
	 * "sistatus":"2024-01-01", 狀態 0=暫停中/1=未生產/2=已發料/3=生產中 Y=已完工/y=指定完工<br>
	 * "sinote":"2024-01-01", 製令備註(客戶/國家/訂單)<br>
	 * "sifname":"2024-01-01", 加工廠(代號+中文)<br>
	 * "sifodate":"2024-01-01", 加工廠開工日期<br>
	 * "sifokdate":"2024-01-01",加工廠完工日期<br>
	 * "simcdate":"2024-01-01", 物控 預計其料日<br>
	 * "siwmprogress":"2024-01-01", 倉庫 備料進度 50/200<br>
	 * "simpprogress":"2024-01-01", 製造 生產進度Ex: 100/500<br>
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

	public Long getSiid() {
		return siid;
	}

	public void setSiid(Long siid) {
		this.siid = siid;
	}

	public String getSiywdate() {
		return siywdate;
	}

	public void setSiywdate(String siywdate) {
		this.siywdate = siywdate;
	}

	public String getSiodate() {
		return siodate;
	}

	public void setSiodate(String siodate) {
		this.siodate = siodate;
	}

	public String getSifdate() {
		return sifdate;
	}

	public void setSifdate(String sifdate) {
		this.sifdate = sifdate;
	}

	public String getSinb() {
		return sinb;
	}

	public void setSinb(String sinb) {
		this.sinb = sinb;
	}

	public String getSipnb() {
		return sipnb;
	}

	public void setSipnb(String sipnb) {
		this.sipnb = sipnb;
	}

	public String getSipname() {
		return sipname;
	}

	public void setSipname(String sipname) {
		this.sipname = sipname;
	}

	public String getSipspecifications() {
		return sipspecifications;
	}

	public void setSipspecifications(String sipspecifications) {
		this.sipspecifications = sipspecifications;
	}

	public Integer getSirqty() {
		return sirqty;
	}

	public void setSirqty(Integer sirqty) {
		this.sirqty = sirqty;
	}

	public Integer getSiokqty() {
		return siokqty;
	}

	public void setSiokqty(Integer siokqty) {
		this.siokqty = siokqty;
	}

	public String getSistatus() {
		return sistatus;
	}

	public void setSistatus(String sistatus) {
		this.sistatus = sistatus;
	}

	public String getSinote() {
		return sinote;
	}

	public void setSinote(String sinote) {
		this.sinote = sinote;
	}

	public String getSifname() {
		return sifname;
	}

	public void setSifname(String sifname) {
		this.sifname = sifname;
	}

	public String getSifodate() {
		return sifodate;
	}

	public void setSifodate(String sifodate) {
		this.sifodate = sifodate;
	}

	public String getSifokdate() {
		return sifokdate;
	}

	public void setSifokdate(String sifokdate) {
		this.sifokdate = sifokdate;
	}

	public String getSiuname() {
		return siuname;
	}

	public void setSiuname(String siuname) {
		this.siuname = siuname;
	}

	public String getSiscnote() {
		return siscnote;
	}

	public void setSiscnote(String siscnote) {
		this.siscnote = siscnote;
	}

	public String getSimcnote() {
		return simcnote;
	}

	public void setSimcnote(String simcnote) {
		this.simcnote = simcnote;
	}

	public String getSimcdate() {
		return simcdate;
	}

	public void setSimcdate(String simcdate) {
		this.simcdate = simcdate;
	}

	public String getSiwmnote() {
		return siwmnote;
	}

	public void setSiwmnote(String siwmnote) {
		this.siwmnote = siwmnote;
	}

	public String getSiwmprogress() {
		return siwmprogress;
	}

	public void setSiwmprogress(String siwmprogress) {
		this.siwmprogress = siwmprogress;
	}

	public String getSimpnote() {
		return simpnote;
	}

	public void setSimpnote(String simpnote) {
		this.simpnote = simpnote;
	}

	public String getSimpprogress() {
		return simpprogress;
	}

	public void setSimpprogress(String simpprogress) {
		this.simpprogress = simpprogress;
	}

	public String getSisum() {
		return sisum;
	}

	public void setSisum(String sisum) {
		this.sisum = sisum;
	}

	public Integer getSiscstatus() {
		return siscstatus;
	}

	public void setSiscstatus(Integer siscstatus) {
		this.siscstatus = siscstatus;
	}

	public String getSimcdates() {
		return simcdates;
	}

	public void setSimcdates(String simcdates) {
		this.simcdates = simcdates;
	}

	public String getSimcdatee() {
		return simcdatee;
	}

	public void setSimcdatee(String simcdatee) {
		this.simcdatee = simcdatee;
	}

	public Integer getSimcstatus() {
		return simcstatus;
	}

	public void setSimcstatus(Integer simcstatus) {
		this.simcstatus = simcstatus;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getSipmnote() {
		return sipmnote;
	}

	public void setSipmnote(String sipmnote) {
		this.sipmnote = sipmnote;
	}

	public String getSicorder() {
		return sicorder;
	}

	public void setSicorder(String sicorder) {
		this.sicorder = sicorder;
	}

	public String getSicnote() {
		return sicnote;
	}

	public void setSicnote(String sicnote) {
		this.sicnote = sicnote;
	}

	public String getSicpnb() {
		return sicpnb;
	}

	public void setSicpnb(String sicpnb) {
		this.sicpnb = sicpnb;
	}

}
