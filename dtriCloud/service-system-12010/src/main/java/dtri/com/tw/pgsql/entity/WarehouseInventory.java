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
 *      ---盤點清單-清單---<br>
 *		wi_wm_p_nb : 物料號(品號)<br>
 *		wi_wa_s_location : 物料儲位<br>
 *		wi_wa_alias : 物料倉儲<br>
 *		wi_date : 盤點時間<br>
 *		wi_user : 盤點人<br>
 *		wi_now_qty : 盤點數<br>
 *		wi_g_now_qty : 盤點差距(+ -)<br>
 *		wi_r_qty : 實際數<br>
 *		wi_t_qty : 在途數<br>
 *		wi_n_qty : 帳務數<br>
 *		wi_c_user : 確認人<br>
 *		wi_check : 確認勾選<br>
 *		wi_incoming : 入料單類型 JSON格式['單據_數量']<br>
 *		wi_shipping :領料單類型 JSON格式['單據_數量']<br>
 * 
 * 
 */
/**
 * @author Basil
 *
 */
@Entity
@Table(name = "warehouse_inventory")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseInventory {
	public WarehouseInventory() {
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
		this.syshnote = "";
		// 盤點-清單
		this.wiwmpnb = "";
		this.wiwmname = "";
		this.wiwaslocation = "";
		this.wiwaalias = "";
		this.widate = new Date();
		this.wiuser = "";
		this.winowqty = 0;
		this.wignowqty = 0;
		this.wirqty = 0;
		this.witqty = 0;
		this.winqty = 0;
		this.wicuser = "";
		this.wicheck = false;
		this.wiincoming = "";
		this.wishipping = "";

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
	@Column(name = "sys_h_note", nullable = false, columnDefinition = "text default ''")
	private String syshnote;
	@Column(name = "sys_ho_note", nullable = false, columnDefinition = "text default ''")
	private String syshonote;

	// 盤點-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_inventory_seq")
	@SequenceGenerator(name = "warehouse_inventory_seq", sequenceName = "warehouse_inventory_seq", allocationSize = 1)
	@Column(name = "wi_id")
	private Long wiid;

	@Column(name = "wi_wm_p_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wiwmpnb;
	@Column(name = "wi_wm_name", nullable = false, columnDefinition = "varchar(200) default ''")
	private String wiwmname;
	@Column(name = "wi_wa_s_location", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wiwaslocation;
	@Column(name = "wi_wa_alias", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wiwaalias;
	@Column(name = "wi_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date widate;
	@Column(name = "wi_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wiuser;
	@Column(name = "wi_now_qty", nullable = false, columnDefinition = "int default 0")
	private Integer winowqty;
	@Column(name = "wi_g_now_qty", nullable = false, columnDefinition = "int default 0")
	private Integer wignowqty;
	@Column(name = "wi_r_qty", nullable = false, columnDefinition = "int default 0")
	private Integer wirqty;
	@Column(name = "wi_t_qty", nullable = false, columnDefinition = "int default 0")
	private Integer witqty;
	@Column(name = "wi_n_qty", nullable = false, columnDefinition = "int default 0")
	private Integer winqty;
	@Column(name = "wi_c_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wicuser;
	@Column(name = "wi_check", nullable = false, columnDefinition = "boolean default false")
	private Boolean wicheck;
	@Column(name = "wi_incoming", nullable = false, columnDefinition = "text default ''")
	private String wiincoming;
	@Column(name = "wi_shipping", nullable = false, columnDefinition = "text default ''")
	private String wishipping;

	@Transient
	private Date ssyscdate;// 起始時間
	@Transient
	private Date esyscdate;// 結束時間

	public String getSyshnote() {
		return syshnote;
	}

	public void setSyshnote(String syshnote) {
		this.syshnote = syshnote;
	}

	public String getWiwmpnb() {
		return wiwmpnb;
	}

	public void setWiwmpnb(String wiwmpnb) {
		this.wiwmpnb = wiwmpnb;
	}

	public String getWiwaslocation() {
		return wiwaslocation;
	}

	public void setWiwaslocation(String wiwaslocation) {
		this.wiwaslocation = wiwaslocation;
	}

	public String getWiwaalias() {
		return wiwaalias;
	}

	public void setWiwaalias(String wiwaalias) {
		this.wiwaalias = wiwaalias;
	}

	public Date getWidate() {
		return widate;
	}

	public void setWidate(Date widate) {
		this.widate = widate;
	}

	public String getWiuser() {
		return wiuser;
	}

	public void setWiuser(String wiuser) {
		this.wiuser = wiuser;
	}

	public Integer getWinowqty() {
		return winowqty;
	}

	public void setWinowqty(Integer winowqty) {
		this.winowqty = winowqty;
	}

	public Integer getWignowqty() {
		return wignowqty;
	}

	public void setWignowqty(Integer wignowqty) {
		this.wignowqty = wignowqty;
	}

	public Integer getWirqty() {
		return wirqty;
	}

	public void setWirqty(Integer wirqty) {
		this.wirqty = wirqty;
	}

	public Integer getWitqty() {
		return witqty;
	}

	public void setWitqty(Integer witqty) {
		this.witqty = witqty;
	}

	public Integer getWinqty() {
		return winqty;
	}

	public void setWinqty(Integer winqty) {
		this.winqty = winqty;
	}

	public String getWicuser() {
		return wicuser;
	}

	public void setWicuser(String wicuser) {
		this.wicuser = wicuser;
	}

	public Boolean getWicheck() {
		return wicheck;
	}

	public void setWicheck(Boolean wicheck) {
		this.wicheck = wicheck;
	}

	public String getWiincoming() {
		return wiincoming;
	}

	public void setWiincoming(String wiincoming) {
		this.wiincoming = wiincoming;
	}

	public String getWishipping() {
		return wishipping;
	}

	public void setWishipping(String wishipping) {
		this.wishipping = wishipping;
	}

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

	public String getSyshonote() {
		return syshonote;
	}

	public void setSyshonote(String syshonote) {
		this.syshonote = syshonote;
	}

	public Long getWiid() {
		return wiid;
	}

	public void setWiid(Long wiid) {
		this.wiid = wiid;
	}

	/**
	 * @return the wiwmname
	 */
	public String getWiwmname() {
		return wiwmname;
	}

	/**
	 * @param wiwmname the wiwmname to set
	 */
	public void setWiwmname(String wiwmname) {
		this.wiwmname = wiwmname;
	}

}
