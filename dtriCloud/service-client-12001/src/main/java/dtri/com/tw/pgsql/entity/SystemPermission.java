package dtri.com.tw.pgsql.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * @author Basil
 * @see 功能權限<br>
 *      sp_id : 單元ID<br>
 *      sp_name : 單元名稱<br>
 *      sp_g_id : 單元群組ID<br>
 *      sp_g_name : 單元群組名稱<br>
 *      sp_control : 單元控制名稱<br>
 *      sp_permission : 權限<br>
 *      sp_type:功能類型<br>
 */
@Entity
@Table(name = "system_permission")
@EntityListeners(AuditingEntityListener.class)
public class SystemPermission {
	public SystemPermission() {
		// 共用型
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysheader = false;

		// 功能權限
		this.spname = "";
		this.spgname = "";
		this.spcontrol = "";
		this.sppermission = "111111111111";
		this.sptype = 0;
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

	// 前端格式-翻譯
	@Transient
	private String language;

	// 功能權限
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_permission_seq")
	@SequenceGenerator(name = "system_permission_seq", sequenceName = "system_permission_seq", allocationSize = 1)
	@Column(name = "sp_id")
	private Long spid;
	@Column(name = "sp_name", nullable = false, columnDefinition = "varchar(50)")
	private String spname;
	@Column(name = "sp_g_id", nullable = false)
	private Long spgid;
	@Column(name = "sp_g_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String spgname;
	@Column(name = "sp_control", nullable = false, columnDefinition = "varchar(50) default ''")
	private String spcontrol;
	@Column(name = "sp_permission", nullable = false, columnDefinition = "varchar(12) default '111111111111'")
	private String sppermission;
	@Column(name = "sp_type", nullable = false, columnDefinition = "int default 0")
	private Integer sptype;

	@JsonIgnore
	@OneToMany(mappedBy = "systemPermission")
	private List<SystemGroup> systemGroup;

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

	public Long getSpid() {
		return spid;
	}

	public void setSpid(Long spid) {
		this.spid = spid;
	}

	public String getSpname() {
		return spname;
	}

	public void setSpname(String spname) {
		this.spname = spname;
	}

	public Long getSpgid() {
		return spgid;
	}

	public void setSpgid(Long spgid) {
		this.spgid = spgid;
	}

	public String getSpgname() {
		return spgname;
	}

	public void setSpgname(String spgname) {
		this.spgname = spgname;
	}

	public String getSpcontrol() {
		return spcontrol;
	}

	public void setSpcontrol(String spcontrol) {
		this.spcontrol = spcontrol;
	}

	public String getSppermission() {
		return sppermission;
	}

	public void setSppermission(String sppermission) {
		this.sppermission = sppermission;
	}

	public Integer getSptype() {
		return sptype;
	}

	public void setSptype(Integer sptype) {
		this.sptype = sptype;
	}

	public List<SystemGroup> getSystemGroup() {
		return systemGroup;
	}

	public void setSystemGroup(List<SystemGroup> systemGroup) {
		this.systemGroup = systemGroup;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
