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
 *      wawmpnb : 物料號<br>
 *      waerptqty : (帳務)此區域物料數量<br>
 *      watqty : (實際)此區域物料數量<br>
 *      waslocation :FF-FF-FF-FF位置<br>
 *      watype : false(主儲位/副儲位)<br>
 *      waalias : 區域庫別代號<br>
 *      waaname : 區域庫別名稱<br>
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
		this.wkwaslocation = "";
		this.wkinsuid = "[]";
		this.wkshsuid = "[]";

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
	@Column(name = "wk_g_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wkgname;
	@Column(name = "wk_wa_s_location", nullable = false, columnDefinition = "varchar(12) default ''")
	private String wkwaslocation;
	@Column(name = "wk_in_su_id", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String wkinsuid;
	@Column(name = "wk_sh_su_id", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String wkshsuid;

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

	public String getWkinsuid() {
		return wkinsuid;
	}

	public void setWkinsuid(String wkinsuid) {
		this.wkinsuid = wkinsuid;
	}

	public String getWkshsuid() {
		return wkshsuid;
	}

	public void setWkshsuid(String wkshsuid) {
		this.wkshsuid = wkshsuid;
	}

	public String getWkgname() {
		return wkgname;
	}

	public void setWkgname(String wkgname) {
		this.wkgname = wkgname;
	}

}
