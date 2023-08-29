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
 *      ---倉儲區域負責人-清單---<br>
 *      wk_id <br>
 *      wk_su_id :使用者ID<br>
 *      wk_su_account:使用者帳號<br>
 *      wk_g_list:使用倉儲清單 JSON:[]<br>
 *      wk_wa_s_location:物料 主儲位位置: 關鍵字 Ex:1F-GG-GG-GG<br>
 *      wk_in_su_id:入料 共同負責人 JSON:[]<br>
 *      wk_sh_su_id: 領料 共同負責人 JSON:[]<br>
 * 
 */

@Entity
@Table(name = "warehouse_keeper")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseKeeper {
	public WarehouseKeeper() {
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
		// 倉儲區域負責人-清單
		this.wksuid = 0L;
		this.wkglist = "[]";
		this.wkwaslocation = "[]";
		this.wkinsuaccount = "[]";
		this.wkshsuaccount = "[]";

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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_keeper_seq")
	@SequenceGenerator(name = "warehouse_keeper_seq", sequenceName = "warehouse_keeper_seq", allocationSize = 1)
	@Column(name = "wk_id")
	private Long wkid;
	@Column(name = "wk_su_id", nullable = false, unique = true)
	private Long wksuid;
	@Column(name = "wk_su_account", nullable = false, unique = true, columnDefinition = "varchar(150) default ''")
	private String wksuaccount;
	@Column(name = "wk_g_list", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String wkglist;
	@Column(name = "wk_wa_s_location", nullable = false, columnDefinition = "varchar(120) default '[]'")
	private String wkwaslocation;
	@Column(name = "wk_in_su_account", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String wkinsuaccount;
	@Column(name = "wk_sh_su_account", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String wkshsuaccount;

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

	public Long getWkid() {
		return wkid;
	}

	public void setWkid(Long wkid) {
		this.wkid = wkid;
	}

	public Long getWksuid() {
		return wksuid;
	}

	public void setWksuid(Long wksuid) {
		this.wksuid = wksuid;
	}

	public String getWkwaslocation() {
		return wkwaslocation;
	}

	public void setWkwaslocation(String wkwaslocation) {
		this.wkwaslocation = wkwaslocation;
	}

	public String getWkinsuaccount() {
		return wkinsuaccount;
	}

	public void setWkinsuaccount(String wkinsuaccount) {
		this.wkinsuaccount = wkinsuaccount;
	}

	public String getWkshsuaccount() {
		return wkshsuaccount;
	}

	public void setWkshsuaccount(String wkshsuaccount) {
		this.wkshsuaccount = wkshsuaccount;
	}

	public String getWksuaccount() {
		return wksuaccount;
	}

	public void setWksuaccount(String wksuaccount) {
		this.wksuaccount = wksuaccount;
	}

	public String getWkglist() {
		return wkglist;
	}

	public void setWkglist(String wkglist) {
		this.wkglist = wkglist;
	}

}
