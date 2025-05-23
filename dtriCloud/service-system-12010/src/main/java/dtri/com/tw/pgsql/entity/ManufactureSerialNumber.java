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
 *      ---產品SN清單---<br>
 *      msn_s_sn : 起始SN號<br>
 *      msn_e_sn : 結束SN號<br>
 *      msn_model : 產品型號<br>
 *      msn_clinet : 客戶<br>
 *      msn_wo :工單號<br>
 *      msn_bom :關聯產品BOM號<br>
 */

@Entity
@Table(name = "manufacture_serial_number")
@EntityListeners(AuditingEntityListener.class)
public class ManufactureSerialNumber {
	public ManufactureSerialNumber() {
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
		// 倉儲區域清單-清單
		this.msnid = null;
		this.msnssn = "";
		this.msnesn = "";
		this.msnmodel = "";
		this.msnclinet = "";
		this.msnwo = "";
		this.msnbom = "";
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

	// 產品SN清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "manufacture_serial_number_seq")
	@SequenceGenerator(name = "manufacture_serial_number_seq", sequenceName = "manufacture_serial_number_seq", allocationSize = 1)
	@Column(name = "msn_id")
	private Long msnid;
	@Column(name = "msn_s_sn", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String msnssn;
	@Column(name = "msn_e_sn", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String msnesn;
	@Column(name = "msn_model", nullable = false, columnDefinition = "varchar(50) default ''")
	private String msnmodel;
	@Column(name = "msn_clinet", nullable = false, columnDefinition = "varchar(50) default ''")
	private String msnclinet;
	@Column(name = "msn_wo", nullable = false, columnDefinition = "varchar(50) default ''")
	private String msnwo;
	@Column(name = "msn_bom", nullable = false, columnDefinition = "varchar(50) default ''")
	private String msnbom;

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

	public Long getMsnid() {
		return msnid;
	}

	public void setMsnid(Long msnid) {
		this.msnid = msnid;
	}

	public String getMsnssn() {
		return msnssn;
	}

	public void setMsnssn(String msnssn) {
		this.msnssn = msnssn;
	}

	public String getMsnesn() {
		return msnesn;
	}

	public void setMsnesn(String msnesn) {
		this.msnesn = msnesn;
	}

	public String getMsnmodel() {
		return msnmodel;
	}

	public void setMsnmodel(String msnmodel) {
		this.msnmodel = msnmodel;
	}

	public String getMsnclinet() {
		return msnclinet;
	}

	public void setMsnclinet(String msnclinet) {
		this.msnclinet = msnclinet;
	}

	public String getMsnwo() {
		return msnwo;
	}

	public void setMsnwo(String msnwo) {
		this.msnwo = msnwo;
	}

	public String getMsnbom() {
		return msnbom;
	}

	public void setMsnbom(String msnbom) {
		this.msnbom = msnbom;
	}

}
