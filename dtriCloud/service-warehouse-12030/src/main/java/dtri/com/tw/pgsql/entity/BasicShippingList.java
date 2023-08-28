package dtri.com.tw.pgsql.entity;

import java.util.Date;
import java.util.Objects;

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
 *      ---入料單-清單---<br>
 *      bsl_id : ID_KEY<br>
 *      bsl_nb : 項次號(從0開始)<br>
 *      bsl_class : 單別 A511/A521....<br>
 *      bsl_sn : 單號<br>
 *      bsl_type : 單據類型 (製令單/內製令....等等)<br>
 *      bsl_checkin : 核單 / 核項目 0=未核單 1=已核單<br>
 *      bsl_c_user: 核准人<br>
 *      bsl_f_user: 完成人<br>
 *      bsl_acceptance:檢驗項目: 0=未檢驗 1=已檢驗 2=異常<br>
 *      bsl_p_number:物料號 Ex:50-117-238132<br>
 *      bsl_p_name:物料品名 Ex:DT504T Mix Color ...<br>
 *      bsl_p_specification:物料規格<br>
 *      bsl_pn_qty:數量<br>
 *      bsl_pn_g_qty:已領數量<br>
 *      bsl_to_command:單據指令對象 json [] A511-123456....<br>
 *      bsl_from_command:單據指令來源 json [] A511-123456....<br>
 *      bsl_to_who:物料對象 (倉庫)EX:A0001_原物料倉<br>
 *      bsl_from_who:物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
 *      bsl_status:單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=立即 / 2=完成<br>
 *      bsl_e_date:預計時間(入料日) 指 單一項目 到齊時間 或是預定 入料時間<br>
 *      bsl_f_date:預計時間(到齊日) 指 整張單都到齊 的時間<br>
 * 
 * 
 */
/**
 * @author Basil
 *
 */
@Entity
@Table(name = "basic_shipping_list")
@EntityListeners(AuditingEntityListener.class)
public class BasicShippingList {
	public BasicShippingList() {
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
		// 入料單-清單
		this.bslid = null;
		this.bslnb = "";
		this.bslclass = "";
		this.bslsn = "";
		this.bsltype = "";
		this.bslcheckin = 0;
		this.bslcuser = "";
		this.bslfuser = "";
		this.bslacceptance = 0;
		this.bslpnumber = "";
		this.bslpname = "";
		this.bslpspecification = "";
		this.bslpnqty = 0;
		this.bslpngqty = 0;
		this.bsltocommand = "[]";
		this.bsltowho = "[]";
		this.bslfromcommand = "[]";
		this.bslfromwho = "[]";
		this.bslstatus = 0;
		this.bslfdate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.bsledate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.bsltfilter = false;
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

	// 入料單-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basic_shipping_list_seq")
	@SequenceGenerator(name = "basic_shipping_list_seq", sequenceName = "basic_shipping_list_seq", allocationSize = 1)
	@Column(name = "bsl_id")
	private Long bslid;
	@Column(name = "bsl_nb", nullable = false, columnDefinition = "varchar(10) default ''")
	private String bslnb;
	@Column(name = "bsl_class", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bslclass;
	@Column(name = "bsl_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bslsn;
	@Column(name = "bsl_type", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bsltype;

	@Column(name = "bsl_checkin", nullable = false, columnDefinition = "int default 0")
	private Integer bslcheckin;
	@Column(name = "bsl_c_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bslcuser;
	@Column(name = "bsl_f_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bslfuser;
	@Column(name = "bsl_acceptance", nullable = false, columnDefinition = "int default 0")
	private Integer bslacceptance;

	@Column(name = "bsl_p_number", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bslpnumber;
	@Column(name = "bsl_p_name", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bslpname;
	@Column(name = "bsl_p_specification", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bslpspecification;
	@Column(name = "bsl_pn_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bslpnqty;
	@Column(name = "bsl_pn_a_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bslpnaqty;
	@Column(name = "bsl_pn_g_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bslpngqty;

	@Column(name = "bsl_to_command", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bsltocommand;
	@Column(name = "bsl_from_command", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bslfromcommand;
	@Column(name = "bsl_to_who", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bsltowho;
	@Column(name = "bsl_from_who", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bslfromwho;
	@Column(name = "bsl_status", nullable = false, columnDefinition = "int default 0")
	private Integer bslstatus;

	@Column(name = "bsl_e_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date bsledate;
	@Column(name = "bsl_f_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date bslfdate;
	@Column(name = "bsl_t_filter", nullable = false, columnDefinition = "boolean default false")
	private Boolean bsltfilter;
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

	public Long getBslid() {
		return bslid;
	}

	public void setBslid(Long bslid) {
		this.bslid = bslid;
	}

	public String getBslnb() {
		return bslnb;
	}

	public void setBslnb(String bslnb) {
		this.bslnb = bslnb;
	}

	public String getBslclass() {
		return bslclass;
	}

	public void setBslclass(String bslclass) {
		this.bslclass = bslclass;
	}

	public String getBslsn() {
		return bslsn;
	}

	public void setBslsn(String bslsn) {
		this.bslsn = bslsn;
	}

	public String getBsltype() {
		return bsltype;
	}

	public void setBsltype(String bsltype) {
		this.bsltype = bsltype;
	}

	public Integer getBslcheckin() {
		return bslcheckin;
	}

	public void setBslcheckin(Integer bslcheckin) {
		this.bslcheckin = bslcheckin;
	}

	public String getBslcuser() {
		return bslcuser;
	}

	public void setBslcuser(String bslcuser) {
		this.bslcuser = bslcuser;
	}

	public String getBslfuser() {
		return bslfuser;
	}

	public void setBslfuser(String bslfuser) {
		this.bslfuser = bslfuser;
	}

	public Integer getBslacceptance() {
		return bslacceptance;
	}

	public void setBslacceptance(Integer bslacceptance) {
		this.bslacceptance = bslacceptance;
	}

	public String getBslpnumber() {
		return bslpnumber;
	}

	public void setBslpnumber(String bslpnumber) {
		this.bslpnumber = bslpnumber;
	}

	public String getBslpname() {
		return bslpname;
	}

	public void setBslpname(String bslpname) {
		this.bslpname = bslpname;
	}

	public String getBslpspecification() {
		return bslpspecification;
	}

	public void setBslpspecification(String bslpspecification) {
		this.bslpspecification = bslpspecification;
	}

	public Integer getBslpnqty() {
		return bslpnqty;
	}

	public void setBslpnqty(Integer bslpnqty) {
		this.bslpnqty = bslpnqty;
	}

	public String getBsltocommand() {
		return bsltocommand;
	}

	public void setBsltocommand(String bsltocommand) {
		this.bsltocommand = bsltocommand;
	}

	public String getBslfromcommand() {
		return bslfromcommand;
	}

	public void setBslfromcommand(String bslfromcommand) {
		this.bslfromcommand = bslfromcommand;
	}

	public String getBsltowho() {
		return bsltowho;
	}

	public void setBsltowho(String bsltowho) {
		this.bsltowho = bsltowho;
	}

	public String getBslfromwho() {
		return bslfromwho;
	}

	public void setBslfromwho(String bslfromwho) {
		this.bslfromwho = bslfromwho;
	}

	public Integer getBslstatus() {
		return bslstatus;
	}

	public void setBslstatus(Integer bslstatus) {
		this.bslstatus = bslstatus;
	}

	public Date getBsledate() {
		return bsledate;
	}

	public void setBsledate(Date bsledate) {
		this.bsledate = bsledate;
	}

	public Date getBslfdate() {
		return bslfdate;
	}

	public void setBslfdate(Date bslfdate) {
		this.bslfdate = bslfdate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bslacceptance, bslcheckin, bslclass, bslcuser, bsledate, bslfdate, bslfromcommand, bslfromwho, bslfuser, bslid, bslnb,
				bslpname, bslpnqty, bslpnumber, bslpspecification, bslsn, bslstatus, bsltocommand, bsltowho, bsltype, syscdate, syscuser, sysheader,
				sysmdate, sysmuser, sysnote, sysodate, sysouser, syssort, sysstatus);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicShippingList other = (BasicShippingList) obj;
		return Objects.equals(bslacceptance, other.bslacceptance) && Objects.equals(bslcheckin, other.bslcheckin)
				&& Objects.equals(bslclass, other.bslclass) && Objects.equals(bslcuser, other.bslcuser) && Objects.equals(bsledate, other.bsledate)
				&& Objects.equals(bslfdate, other.bslfdate) && Objects.equals(bslfromcommand, other.bslfromcommand)
				&& Objects.equals(bslfromwho, other.bslfromwho) && Objects.equals(bslfuser, other.bslfuser) && Objects.equals(bslid, other.bslid)
				&& Objects.equals(bslnb, other.bslnb) && Objects.equals(bslpname, other.bslpname) && Objects.equals(bslpnqty, other.bslpnqty)
				&& Objects.equals(bslpnumber, other.bslpnumber) && Objects.equals(bslpspecification, other.bslpspecification)
				&& Objects.equals(bslsn, other.bslsn) && Objects.equals(bslstatus, other.bslstatus)
				&& Objects.equals(bsltocommand, other.bsltocommand) && Objects.equals(bsltowho, other.bsltowho)
				&& Objects.equals(bsltype, other.bsltype) && Objects.equals(syscdate, other.syscdate) && Objects.equals(syscuser, other.syscuser)
				&& Objects.equals(sysheader, other.sysheader) && Objects.equals(sysmdate, other.sysmdate) && Objects.equals(sysmuser, other.sysmuser)
				&& Objects.equals(sysnote, other.sysnote) && Objects.equals(sysodate, other.sysodate) && Objects.equals(sysouser, other.sysouser)
				&& Objects.equals(syssort, other.syssort) && Objects.equals(sysstatus, other.sysstatus);
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public Integer getBslpnaqty() {
		return bslpnaqty;
	}

	public void setBslpnaqty(Integer bslpnaqty) {
		this.bslpnaqty = bslpnaqty;
	}

	public Integer getBslpngqty() {
		return bslpngqty;
	}

	public void setBslpngqty(Integer bslpngqty) {
		this.bslpngqty = bslpngqty;
	}

	public Boolean getBsltfilter() {
		return bsltfilter;
	}

	public void setBsltfilter(Boolean bsltfilter) {
		this.bsltfilter = bsltfilter;
	}

}
