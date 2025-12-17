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
 *      ---BOM結構-清單---<br>
 *		this.bbi_id = null;KEY<br>
 *		this.bbi_sn = "";主項物料號<br>
 *		this.bbi_snnb = "";主項物料號+序號<br>
 *		this.bbi_name = "";主項目品名<br>
 *		this.bbi_specification = "";主項目規格<br>
 *		this.bbi_i_sn = "";子項物料號<br>
 *		this.bbi_i_name = "";子項目品名<br>
 *		this.bbi_i_specification = "";子項目規格<br>
 *		this.bbi_i_qty = 0;子項目數量<br>
 *		this.bbi_i_level = 0;子項目層級<br>
 * 
 * 
 */
/**
 * @author Basil
 *
 */
@Entity
@Table(name = "basic_bom_ingredients")
@EntityListeners(AuditingEntityListener.class)
public class BasicBomIngredients {
	public BasicBomIngredients() {
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

		// BOM成品組成
		this.bbiid = null;
		this.bbisn = "";
		this.bbisnnb = "";
		this.bbiname = "";
		this.bbispecification = "";
		this.bbiisn = "";
		this.bbiiname = "";
		this.bbiispecification = "";
		this.bbiiqty = 0;
		this.bbiilevel = 0;
		this.checksum = "";

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

	// BOM結構清單-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basic_command_list_seq")
	@SequenceGenerator(name = "basic_command_list_seq", sequenceName = "basic_command_list_seq", allocationSize = 1)
	@Column(name = "bbi_id")
	private Long bbiid;
	@Column(name = "bbi_sn", nullable = false, columnDefinition = "varchar(60) default ''")
	private String bbisn;
	@Column(name = "bbi_nb", nullable = false, columnDefinition = "varchar(30) default ''")
	private String bbinb;
	@Column(name = "bbi_sn_nb", nullable = false, unique = true, columnDefinition = "varchar(60) default ''")
	private String bbisnnb;
	@Column(name = "bbi_name", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bbiname;
	@Column(name = "bbi_specification", nullable = false, columnDefinition = "text default ''")
	private String bbispecification;
	@Column(name = "bbi_description", nullable = false, columnDefinition = "text default ''")
	private String bbidescription;

	@Column(name = "bbi_i_sn", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bbiisn;
	@Column(name = "bbi_i_name", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bbiiname;
	@Column(name = "bbi_i_specification", nullable = false, columnDefinition = "text default ''")
	private String bbiispecification;
	@Column(name = "bbi_i_description", nullable = false, columnDefinition = "text default ''")
	private String bbiidescription;
	@Column(name = "bbi_i_process", nullable = false, columnDefinition = "varchar(60) default ''")
	private String bbiiprocess;
	@Column(name = "bbi_i_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bbiiqty;
	@Column(name = "bbi_i_level", nullable = false, columnDefinition = "int default 0")
	private Integer bbiilevel;

	@Column(name = "bbi_i_s_erp", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bbiiserp;

	@Column(name = "check_sum", nullable = false, columnDefinition = "text default ''")
	private String checksum;

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

	public Long getBbiid() {
		return bbiid;
	}

	public void setBbiid(Long bbiid) {
		this.bbiid = bbiid;
	}

	public String getBbisn() {
		return bbisn;
	}

	public void setBbisn(String bbisn) {
		this.bbisn = bbisn;
	}

	public String getBbisnnb() {
		return bbisnnb;
	}

	public void setBbisnnb(String bbisnnb) {
		this.bbisnnb = bbisnnb;
	}

	public String getBbiname() {
		return bbiname;
	}

	public void setBbiname(String bbiname) {
		this.bbiname = bbiname;
	}

	public String getBbispecification() {
		return bbispecification;
	}

	public void setBbispecification(String bbispecification) {
		this.bbispecification = bbispecification;
	}

	public String getBbiisn() {
		return bbiisn;
	}

	public void setBbiisn(String bbiisn) {
		this.bbiisn = bbiisn;
	}

	public String getBbiiname() {
		return bbiiname;
	}

	public void setBbiiname(String bbiiname) {
		this.bbiiname = bbiiname;
	}

	public String getBbiispecification() {
		return bbiispecification;
	}

	public void setBbiispecification(String bbiispecification) {
		this.bbiispecification = bbiispecification;
	}

	public Integer getBbiiqty() {
		return bbiiqty;
	}

	public void setBbiiqty(Integer bbiiqty) {
		this.bbiiqty = bbiiqty;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getBbiiserp() {
		return bbiiserp;
	}

	public void setBbiiserp(String bbiiserp) {
		this.bbiiserp = bbiiserp;
	}

	public String getBbinb() {
		return bbinb;
	}

	public void setBbinb(String bbinb) {
		this.bbinb = bbinb;
	}

	public String getBbidescription() {
		return bbidescription;
	}

	public void setBbidescription(String bbidescription) {
		this.bbidescription = bbidescription;
	}

	public String getBbiidescription() {
		return bbiidescription;
	}

	public void setBbiidescription(String bbiidescription) {
		this.bbiidescription = bbiidescription;
	}

	public String getBbiiprocess() {
		return bbiiprocess;
	}

	public void setBbiiprocess(String bbiiprocess) {
		this.bbiiprocess = bbiiprocess;
	}

	/**
	 * @return the bbiilevel
	 */
	public Integer getBbiilevel() {
		return bbiilevel;
	}

	/**
	 * @param bbiilevel the bbiilevel to set
	 */
	public void setBbiilevel(Integer bbiilevel) {
		this.bbiilevel = bbiilevel;
	}

}
