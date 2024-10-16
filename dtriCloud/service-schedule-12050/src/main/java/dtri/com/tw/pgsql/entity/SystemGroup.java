package dtri.com.tw.pgsql.entity;

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
		// 權限名稱
		this.spname = "";
		this.spid = null;
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

	// UI使用(權限名稱/ID/Permission List)
	@Transient
	private String spname;
	@Transient
	private Long spid;
	@Transient
	private Boolean pS6;// 特殊6(S6)
	@Transient
	private Boolean pS5;// 特殊5(S5)
	@Transient
	private Boolean pS4;// 特殊4(S4)
	@Transient
	private Boolean pS3;// 特殊3(S3)
	@Transient
	private Boolean pS2;// 特殊2(S2)
	@Transient
	private Boolean pS1;// 特殊1(S1)
	@Transient
	private Boolean pDD;// 完全移除(DD)
	@Transient
	private Boolean pAD;// 作廢(AD)
	@Transient
	private Boolean pAC;// 新增(AC)
	@Transient
	private Boolean pAU;// 修改(AU)
	@Transient
	private Boolean pAR;// 查詢(AR)
	@Transient
	private Boolean pAA;// 訪問(AA)

	public Boolean getpS6() {
		return pS6;
	}

	public void setpS6(Boolean pS6) {
		this.pS6 = pS6;
	}

	public Boolean getpS5() {
		return pS5;
	}

	public void setpS5(Boolean pS5) {
		this.pS5 = pS5;
	}

	public Boolean getpS4() {
		return pS4;
	}

	public void setpS4(Boolean pS4) {
		this.pS4 = pS4;
	}

	public Boolean getpS3() {
		return pS3;
	}

	public void setpS3(Boolean pS3) {
		this.pS3 = pS3;
	}

	public Boolean getpS2() {
		return pS2;
	}

	public void setpS2(Boolean pS2) {
		this.pS2 = pS2;
	}

	public Boolean getpS1() {
		return pS1;
	}

	public void setpS1(Boolean pS1) {
		this.pS1 = pS1;
	}

	public Boolean getpDD() {
		return pDD;
	}

	public void setpDD(Boolean pDD) {
		this.pDD = pDD;
	}

	public Boolean getpAD() {
		return pAD;
	}

	public void setpAD(Boolean pAD) {
		this.pAD = pAD;
	}

	public Boolean getpAC() {
		return pAC;
	}

	public void setpAC(Boolean pAC) {
		this.pAC = pAC;
	}

	public Boolean getpAU() {
		return pAU;
	}

	public void setpAU(Boolean pAU) {
		this.pAU = pAU;
	}

	public Boolean getpAR() {
		return pAR;
	}

	public void setpAR(Boolean pAR) {
		this.pAR = pAR;
	}

	public Boolean getpAA() {
		return pAA;
	}

	public void setpAA(Boolean pAA) {
		this.pAA = pAA;
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

	public String getSpname() {
		return spname;
	}

	public void setSpname(String spname) {
		this.spname = spname;
	}

	public Long getSpid() {
		return spid;
	}

	public void setSpid(Long spid) {
		this.spid = spid;
	}

}
