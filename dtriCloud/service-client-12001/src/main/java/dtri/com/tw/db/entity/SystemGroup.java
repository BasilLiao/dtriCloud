package dtri.com.tw.db.entity;

import java.util.Date;
import java.util.Set;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
 *      ---權限群組---<br>
 *      sg_id : 群組ID<br>
 *      sg_group_id : 權限群組ID<br>
 *      sg_name : 群組名稱<br>
 *      sg_sper_id : 功能權限ID<br>
 *      sg_permission : 功能權限驗證<br>
 */
@Entity
@Table(name = "system_group")
@EntityListeners(AuditingEntityListener.class)
public class SystemGroup {
	public SystemGroup() {
		// 共用型
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysodate = new Date();
		this.sysouser = "system";

		this.sysheader = false;
		this.sysstatus = 0;
		this.syssort = 0;
		this.sysnote = "";
		// 群組型
		this.sgname = "";
		this.sgpermission = "000000000000";
		this.systemusers = null;
		this.systemPermission = new SystemPermission();
	}

	// 共用型
	@JsonIgnore
	@Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date syscdate;
	@JsonIgnore
	@Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String syscuser;
	@JsonIgnore
	@Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date sysmdate;
	@JsonIgnore
	@Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String sysmuser;
	@JsonIgnore
	@Column(name = "sys_o_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date sysodate;
	@JsonIgnore
	@Column(name = "sys_o_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
	private String sysouser;
	@Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysheader;
	@Column(name = "sys_status", nullable = false, columnDefinition = "int default 0")
	private Integer sysstatus;
	@Column(name = "sys_sort", nullable = false, columnDefinition = "int default 0")
	private Integer syssort;
	@JsonIgnore
	@Column(name = "sys_note", nullable = false, columnDefinition = "text default ''")
	private String sysnote;

	// 群組型
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_group_seq")
	@SequenceGenerator(name = "system_group_seq", sequenceName = "system_group_seq", allocationSize = 1)
	@Column(name = "sg_id")
	private Long sgid;
	@Column(name = "sg_g_id", nullable = false)
	private Long sggid;
	@Column(name = "sg_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String sgname;
	@Column(name = "sg_permission", nullable = false, columnDefinition = "varchar(12) default '000000000000'")
	private String sgpermission;

	// jackson[使用方式@JsonIgnore=忽略/@JsonInclude忽略特定值]
	@JsonIgnore
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@ManyToMany(mappedBy = "systemgroups")
	Set<SystemUser> systemusers;

	@ManyToOne(targetEntity = SystemPermission.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "sg_sp_id")
	private SystemPermission systemPermission;

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

	public Long getSgid() {
		return sgid;
	}

	public void setSgid(Long sgid) {
		this.sgid = sgid;
	}

	public Long getSggid() {
		return sggid;
	}

	public void setSggid(Long sggid) {
		this.sggid = sggid;
	}

	public String getSgname() {
		return sgname;
	}

	public void setSgname(String sgname) {
		this.sgname = sgname;
	}

	public String getSgpermission() {
		return sgpermission;
	}

	public void setSgpermission(String sgpermission) {
		this.sgpermission = sgpermission;
	}

	public Set<SystemUser> getSystemusers() {
		return systemusers;
	}

	public void setSystemusers(Set<SystemUser> systemusers) {
		this.systemusers = systemusers;
	}

	public SystemPermission getSystemPermission() {
		return systemPermission;
	}

	public void setSystemPermission(SystemPermission systemPermission) {
		this.systemPermission = systemPermission;
	}

}
