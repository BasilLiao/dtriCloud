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
 *      soid = ID;<br>
 *      soywdate = 年周:例如 2022-W55 YYYY(西元年)-W00(周)<br>
 *      soodate = 預計開工時間<br>
 *      sofdate = 預計完工時間<br>
 *      sonb = 製令單號<br>
 *      sopnb = 產品品號<br>
 *      sopname = 產品品名<br>
 *      sopspecifications = 產品規格<br>
 *      sorqty = 預計生產<br>
 *      sookqty = 已經生產<br>
 *      sostatus = 狀態 0=暫停中/1=未生產/2=已發料/3=生產中 Y=已完工/y=指定完工<br>
 *      sonote = 製令備註(客戶/國家/訂單)<br>
 *      sofname = 加工廠(代號+中文)<br>
 *      sofodate = 加工廠開工日期<br>
 *      sofokdate = 加工廠完工日期<br>
 *      souname = 開單人名<br>
 *      soscstatus : 生管狀態(自訂義) 生管備料狀態 :
 *      自訂義(1.已發料/2.部分缺料/3.備料中/4.未生產(def)/5.待打件通知)<br>
 *      soscnote = 生管備註(格式) 人+時間+內容 [{date:2024-01-01 10:10:10,
 *      user:Test,content:""},{}]<br>
 *      somcnote = 物控備註(格式) 人+時間+內容<br>
 *      somcdate = 物控 預計其料日<br>
 *      sowmnote = 倉庫備註(格式) 人+時間+內容<br>
 *      sowmprogress = 倉庫 備料進度 50/200<br>
 *      sompnote = 製造備註(格式) 人+時間+內容<br>
 *      sompprogress = 製造 生產進度Ex: 100/500<br>
 *      sosum = 資料串 比對<br>
 * 
 */

@Entity
@Table(name = "schedule_outsourcer")
@EntityListeners(AuditingEntityListener.class)
public class ScheduleOutsourcer {
	public ScheduleOutsourcer() {
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
		this.soid = null;
		this.soywdate = "";
		this.soodate = "";
		this.sofdate = "";
		this.sonb = "";
		this.sopnb = "";
		this.sopname = "";
		this.sopspecifications = "";
		this.sorqty = 0;
		this.sookqty = 0;
		this.sostatus = "0";
		this.sonote = "";
		this.sofname = "";
		this.sofodate = "";
		this.sofokdate = "";
		this.souname = "";
		this.soscnote = "[]";
		this.somcnote = "[]";
		this.somcdate = "";
		this.sowmnote = "[]";
		this.sowmprogress = "";
		this.sompnote = "[]";
		this.sompprogress = "";
		this.sosum = "";// 異動資料判斷?
		this.locked = false;
		// 標記更新
		this.newTage = new JsonObject();
		this.newTage.addProperty("all", "");
		this.newTage.addProperty("soscnote", "");
		this.newTage.addProperty("somcnote", "");
		this.newTage.addProperty("sowmnote", "");
		this.newTage.addProperty("sompnote", "");
		this.newTage.addProperty("sorqty", "");
		this.newTage.addProperty("sookqty", "");

		this.newTage.addProperty("sostatus", "");
		this.newTage.addProperty("sonote", "");
		this.newTage.addProperty("sofname", "");
		this.newTage.addProperty("sofodate", "");
		this.newTage.addProperty("sofokdate", "");
		this.newTage.addProperty("somcdate", "");

		this.newTage.addProperty("sowmprogress", "");
		this.newTage.addProperty("sompprogress", "");
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_outsourcer_seq")
	@SequenceGenerator(name = "schedule_outsourcer_seq", sequenceName = "schedule_outsourcer_seq", allocationSize = 1)
	@Column(name = "so_id")
	private Long soid;
	@Column(name = "so_yw_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String soywdate;
	@Column(name = "so_o_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String soodate;
	@Column(name = "so_f_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String sofdate;

	@Column(name = "so_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sonb;
	@Column(name = "so_p_nb", nullable = false, columnDefinition = "varchar(100) default ''")
	private String sopnb;
	@Column(name = "so_p_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String sopname;

	@Column(name = "so_p_specifications", nullable = false, columnDefinition = "text default ''")
	private String sopspecifications;
	@Column(name = "so_r_qty", nullable = false, columnDefinition = "int default 0")
	private Integer sorqty;
	@Column(name = "so_ok_qty", nullable = false, columnDefinition = "int default 0")
	private Integer sookqty;
	@Column(name = "so_status", nullable = false, columnDefinition = "varchar(10) default ''")
	private String sostatus;
	@Column(name = "so_note", nullable = false, columnDefinition = "text default ''")
	private String sonote;
	@Column(name = "so_f_name", nullable = false, columnDefinition = "varchar(100) default ''")
	private String sofname;

	@Column(name = "so_f_o_date", nullable = false, columnDefinition = "varchar(120) default ''")
	private String sofodate;
	@Column(name = "so_f_ok_date", nullable = false, columnDefinition = "varchar(120) default ''")
	private String sofokdate;
	@Column(name = "so_u_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String souname;

	@Column(name = "so_sc_status", nullable = false, columnDefinition = "int default 0")
	private Integer soscstatus;
	@Column(name = "so_sc_note", nullable = false, columnDefinition = "text default ''")
	private String soscnote;
	@Column(name = "so_mc_note", nullable = false, columnDefinition = "text default ''")
	private String somcnote;
	@Column(name = "so_mc_date", nullable = false, columnDefinition = "varchar(20) default ''")
	private String somcdate;
	@Column(name = "so_wm_note", nullable = false, columnDefinition = "text default ''")
	private String sowmnote;
	@Column(name = "so_wm_progress", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sowmprogress;
	@Column(name = "so_mp_note", nullable = false, columnDefinition = "text default ''")
	private String sompnote;
	@Column(name = "so_mp_progress", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sompprogress;
	@Column(name = "so_sum", nullable = false, columnDefinition = "text default ''")
	private String sosum;

	@Transient
	private Boolean locked;// 鎖定?

	/**
	 * 新的def<br>
	 * {"all":"2024-01-01",全新<br>
	 * "soscnote":"2024-01-01", 生管備註(格式) def:""空值<br>
	 * "somcnote":"2024-01-01", 物控備註(格式) def:""空值<br>
	 * "sowmnote":"2024-01-01", 倉庫備註(格式) def:""空值<br>
	 * "sompnote":"2024-01-01", 製造備註(格式) def:""空值<br>
	 * "sorqty":"2024-01-01", 預計生產<br>
	 * "sookqty":"2024-01-01", 已經生產<br>
	 * "sostatus":"2024-01-01", 狀態 0=暫停中/1=未生產/2=已發料/3=生產中 Y=已完工/y=指定完工<br>
	 * "sonote":"2024-01-01", 製令備註(客戶/國家/訂單)<br>
	 * "sofname":"2024-01-01", 加工廠(代號+中文)<br>
	 * "sofodate":"2024-01-01", 加工廠開工日期<br>
	 * "sofokdate":"2024-01-01",加工廠完工日期<br>
	 * "somcdate":"2024-01-01", 物控 預計其料日<br>
	 * "sowmprogress":"2024-01-01", 倉庫 備料進度 50/200<br>
	 * "sompprogress":"2024-01-01", 製造 生產進度Ex: 100/500<br>
	 * }<br>
	 **/
	@Transient
	private JsonObject newTage;
	
	

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

	public Long getSoid() {
		return soid;
	}

	public void setSoid(Long soid) {
		this.soid = soid;
	}

	public String getSoywdate() {
		return soywdate;
	}

	public void setSoywdate(String soywdate) {
		this.soywdate = soywdate;
	}

	public String getSoodate() {
		return soodate;
	}

	public void setSoodate(String soodate) {
		this.soodate = soodate;
	}

	public String getSofdate() {
		return sofdate;
	}

	public void setSofdate(String sofdate) {
		this.sofdate = sofdate;
	}

	public String getSonb() {
		return sonb;
	}

	public void setSonb(String sonb) {
		this.sonb = sonb;
	}

	public String getSopnb() {
		return sopnb;
	}

	public void setSopnb(String sopnb) {
		this.sopnb = sopnb;
	}

	public String getSopname() {
		return sopname;
	}

	public void setSopname(String sopname) {
		this.sopname = sopname;
	}

	public String getSopspecifications() {
		return sopspecifications;
	}

	public void setSopspecifications(String sopspecifications) {
		this.sopspecifications = sopspecifications;
	}

	public Integer getSorqty() {
		return sorqty;
	}

	public void setSorqty(Integer sorqty) {
		this.sorqty = sorqty;
	}

	public Integer getSookqty() {
		return sookqty;
	}

	public void setSookqty(Integer sookqty) {
		this.sookqty = sookqty;
	}

	public String getSostatus() {
		return sostatus;
	}

	public void setSostatus(String sostatus) {
		this.sostatus = sostatus;
	}

	public String getSonote() {
		return sonote;
	}

	public void setSonote(String sonote) {
		this.sonote = sonote;
	}

	public String getSofname() {
		return sofname;
	}

	public void setSofname(String sofname) {
		this.sofname = sofname;
	}

	public String getSofodate() {
		return sofodate;
	}

	public void setSofodate(String sofodate) {
		this.sofodate = sofodate;
	}

	public String getSofokdate() {
		return sofokdate;
	}

	public void setSofokdate(String sofokdate) {
		this.sofokdate = sofokdate;
	}

	public String getSouname() {
		return souname;
	}

	public void setSouname(String souname) {
		this.souname = souname;
	}

	public String getSoscnote() {
		return soscnote;
	}

	public void setSoscnote(String soscnote) {
		this.soscnote = soscnote;
	}

	public String getSomcnote() {
		return somcnote;
	}

	public void setSomcnote(String somcnote) {
		this.somcnote = somcnote;
	}

	public String getSomcdate() {
		return somcdate;
	}

	public void setSomcdate(String somcdate) {
		this.somcdate = somcdate;
	}

	public String getSowmnote() {
		return sowmnote;
	}

	public void setSowmnote(String sowmnote) {
		this.sowmnote = sowmnote;
	}

	public String getSowmprogress() {
		return sowmprogress;
	}

	public void setSowmprogress(String sowmprogress) {
		this.sowmprogress = sowmprogress;
	}

	public String getSompnote() {
		return sompnote;
	}

	public void setSompnote(String sompnote) {
		this.sompnote = sompnote;
	}

	public String getSompprogress() {
		return sompprogress;
	}

	public void setSompprogress(String sompprogress) {
		this.sompprogress = sompprogress;
	}

	public String getSosum() {
		return sosum;
	}

	public void setSosum(String sosum) {
		this.sosum = sosum;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public Integer getSoscstatus() {
		return soscstatus;
	}

	public void setSoscstatus(Integer soscstatus) {
		this.soscstatus = soscstatus;
	}

	public JsonObject getNewTage() {
		return newTage;
	}

	public void setNewTage(JsonObject newTage) {
		this.newTage = newTage;
	}

}
