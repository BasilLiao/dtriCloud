package dtri.com.tw.pgsql.entity;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
 *      bil_id : ID_KEY<br>
 *      bil_nb : 項次號(從0開始)<br>
 *      bil_class : 單別 A511/A521....<br>
 *      bil_sn : 單號<br>
 *      bil_type : 單據類型 (製令單/內製令....等等)<br>
 *      bil_checkin : 核單 / 核項目 0=未核單 1=已核單<br>
 *      bil_c_user: 核准人<br>
 *      bil_f_user: 完成人<br>
 *      bil_s_user: 最後-同步人<br>
 *      bil_m_user: 分配-負責人<br>
 *      
 *      bil_p_already:打印過?0=尚未1=已經<br>
 *      bil_acceptance:檢驗項目: 0=未檢驗 1=已檢驗 2=異常<br>
 *      bil_p_number:物料號 Ex:50-117-238132<br>
 *      bil_p_name:物料品名 Ex:DT504T Mix Color ...<br>
 *      bil_p_specification:物料規格<br>
 *      bil_pn_qty:須入數量<br>
 *      bil_pn_g_qty:已入數量<br>
 *      bil_pn_a_qty:提前領取量<br>
 *      bil_pn_o_qty:超入量<br>  
 *      bil_to_command:單據指令對象 json [] A511-123456....<br>
 *      bil_from_command:單據指令來源 json [] A511-123456....<br>
 *      bil_to_who:物料對象 (倉庫)EX:A0001_原物料倉<br>
 *      bil_from_who:物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
 *      bil_status:單據狀態 3 = 取消 / 4=暫停 /5=全數歸還/ 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
 *      bil_e_date:預計時間(入料日) 指 單一項目 到齊時間 或是預定 入料時間<br>
 *      bil_f_date:預計時間(到齊日) 指 整張單都到齊 的時間<br>
 * 
 * 
 */
/**
 * @author Basil
 *
 */
@Entity
@Table(name = "basic_incoming_list")
@EntityListeners(AuditingEntityListener.class)
public class BasicIncomingList {
	public BasicIncomingList() {
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
		this.bilid = null;
		this.bilnb = "";
		this.bilclass = "";
		this.bilsn = "";
		this.biltype = "";
		this.bilcheckin = 0;
		this.bilcuser = "";
		this.bilfuser = "";
		this.bilsuser = "";
		this.bilacceptance = 0;
		this.bilpnumber = "";
		this.bilpname = "";
		this.bilpspecification = "";
		this.bilpnqty = 0;
		this.bilpngqty = 0;
		this.bilpnaqty = 0;
		this.bilpnoqty = 0;
		this.biltocommand = "[]";
		this.bilfromcommand = "[]";
		this.biltowho = "[]";
		this.bilfromwho = "[]";
		this.bilstatus = 0;
		this.bilfdate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.biledate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.biltfilter = false;
		this.bilpalready = 0;
		this.checkrm = false;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basic_incoming_list_seq")
	@SequenceGenerator(name = "basic_incoming_list_seq", sequenceName = "basic_incoming_list_seq", allocationSize = 1)
	@Column(name = "bil_id")
	private Long bilid;
	@Column(name = "bil_nb", nullable = false, columnDefinition = "varchar(10) default ''")
	private String bilnb;
	@Column(name = "bil_class", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bilclass;
	@Column(name = "bil_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bilsn;
	@Column(name = "bil_type", nullable = false, columnDefinition = "varchar(50) default ''")
	private String biltype;

	@Column(name = "bil_checkin", nullable = false, columnDefinition = "int default 0")
	private Integer bilcheckin;
	@Column(name = "bil_c_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bilcuser;
	@Column(name = "bil_f_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bilfuser;
	@Column(name = "bil_s_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bilsuser;
	@Column(name = "bil_m_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bilmuser;

	@Column(name = "bil_p_already", nullable = false, columnDefinition = "int default 0")
	private Integer bilpalready;
	@Column(name = "bil_acceptance", nullable = false, columnDefinition = "int default 0")
	private Integer bilacceptance;

	@Column(name = "bil_p_number", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bilpnumber;
	@Column(name = "bil_p_name", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bilpname;
	@Column(name = "bil_p_specification", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bilpspecification;
	@Column(name = "bil_pn_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bilpnqty;
	@Column(name = "bil_pn_a_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bilpnaqty;
	@Column(name = "bil_pn_g_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bilpngqty;
	@Column(name = "bil_pn_o_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bilpnoqty;

	@Column(name = "bil_to_command", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String biltocommand;
	@Column(name = "bil_from_command", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bilfromcommand;
	@Column(name = "bil_to_who", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String biltowho;
	@Column(name = "bil_from_who", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bilfromwho;
	@Column(name = "bil_status", nullable = false, columnDefinition = "int default 0")
	private Integer bilstatus;

	@Column(name = "bil_e_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date biledate;
	@Column(name = "bil_f_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date bilfdate;
	@Column(name = "bil_t_filter", nullable = false, columnDefinition = "boolean default false")
	private Boolean biltfilter;

	@Column(name = "check_sum", nullable = false, columnDefinition = "text default ''")
	private String checksum;

	@JsonIgnore
	@Column(name = "check_rm", nullable = false, columnDefinition = "boolean default false")
	private Boolean checkrm;// 檢查移除

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

	public Long getBilid() {
		return bilid;
	}

	public void setBilid(Long bilid) {
		this.bilid = bilid;
	}

	public String getBilnb() {
		return bilnb;
	}

	public void setBilnb(String bilnb) {
		this.bilnb = bilnb;
	}

	public String getBilclass() {
		return bilclass;
	}

	public void setBilclass(String bilclass) {
		this.bilclass = bilclass;
	}

	public String getBilsn() {
		return bilsn;
	}

	public void setBilsn(String bilsn) {
		this.bilsn = bilsn;
	}

	public String getBiltype() {
		return biltype;
	}

	public void setBiltype(String biltype) {
		this.biltype = biltype;
	}

	public Integer getBilcheckin() {
		return bilcheckin;
	}

	public void setBilcheckin(Integer bilcheckin) {
		this.bilcheckin = bilcheckin;
	}

	public String getBilcuser() {
		return bilcuser;
	}

	public void setBilcuser(String bilcuser) {
		this.bilcuser = bilcuser;
	}

	public String getBilfuser() {
		return bilfuser;
	}

	public void setBilfuser(String bilfuser) {
		this.bilfuser = bilfuser;
	}

	public Integer getBilacceptance() {
		return bilacceptance;
	}

	public void setBilacceptance(Integer bilacceptance) {
		this.bilacceptance = bilacceptance;
	}

	public String getBilpnumber() {
		return bilpnumber;
	}

	public void setBilpnumber(String bilpnumber) {
		this.bilpnumber = bilpnumber;
	}

	public String getBilpname() {
		return bilpname;
	}

	public void setBilpname(String bilpname) {
		this.bilpname = bilpname;
	}

	public String getBilpspecification() {
		return bilpspecification;
	}

	public void setBilpspecification(String bilpspecification) {
		this.bilpspecification = bilpspecification;
	}

	public Integer getBilpnqty() {
		return bilpnqty;
	}

	public void setBilpnqty(Integer bilpnqty) {
		this.bilpnqty = bilpnqty;
	}

	public String getBiltocommand() {
		return biltocommand;
	}

	public void setBiltocommand(String biltocommand) {
		this.biltocommand = biltocommand;
	}

	public String getBilfromcommand() {
		return bilfromcommand;
	}

	public void setBilfromcommand(String bilfromcommand) {
		this.bilfromcommand = bilfromcommand;
	}

	public String getBiltowho() {
		return biltowho;
	}

	public void setBiltowho(String biltowho) {
		this.biltowho = biltowho;
	}

	public String getBilfromwho() {
		return bilfromwho;
	}

	public void setBilfromwho(String bilfromwho) {
		this.bilfromwho = bilfromwho;
	}

	public Integer getBilstatus() {
		return bilstatus;
	}

	public void setBilstatus(Integer bilstatus) {
		this.bilstatus = bilstatus;
	}

	public Date getBiledate() {
		return biledate;
	}

	public void setBiledate(Date biledate) {
		this.biledate = biledate;
	}

	public Date getBilfdate() {
		return bilfdate;
	}

	public void setBilfdate(Date bilfdate) {
		this.bilfdate = bilfdate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bilacceptance, bilcheckin, bilclass, bilcuser, biledate, bilfdate, bilfromcommand, bilfromwho, bilfuser, bilid, bilnb,
				bilpname, bilpnqty, bilpnumber, bilpspecification, bilsn, bilstatus, biltocommand, biltowho, biltype, syscdate, syscuser, sysheader,
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
		BasicIncomingList other = (BasicIncomingList) obj;
		return Objects.equals(bilacceptance, other.bilacceptance) && Objects.equals(bilcheckin, other.bilcheckin)
				&& Objects.equals(bilclass, other.bilclass) && Objects.equals(bilcuser, other.bilcuser) && Objects.equals(biledate, other.biledate)
				&& Objects.equals(bilfdate, other.bilfdate) && Objects.equals(bilfromcommand, other.bilfromcommand)
				&& Objects.equals(bilfromwho, other.bilfromwho) && Objects.equals(bilfuser, other.bilfuser) && Objects.equals(bilid, other.bilid)
				&& Objects.equals(bilnb, other.bilnb) && Objects.equals(bilpname, other.bilpname) && Objects.equals(bilpnqty, other.bilpnqty)
				&& Objects.equals(bilpnumber, other.bilpnumber) && Objects.equals(bilpspecification, other.bilpspecification)
				&& Objects.equals(bilsn, other.bilsn) && Objects.equals(bilstatus, other.bilstatus)
				&& Objects.equals(biltocommand, other.biltocommand) && Objects.equals(biltowho, other.biltowho)
				&& Objects.equals(biltype, other.biltype) && Objects.equals(syscdate, other.syscdate) && Objects.equals(syscuser, other.syscuser)
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

	public Integer getBilpnaqty() {
		return bilpnaqty;
	}

	public void setBilpnaqty(Integer bilpnaqty) {
		this.bilpnaqty = bilpnaqty;
	}

	public Integer getBilpngqty() {
		return bilpngqty;
	}

	public void setBilpngqty(Integer bilpngqty) {
		this.bilpngqty = bilpngqty;
	}

	public Boolean getBiltfilter() {
		return biltfilter;
	}

	public void setBiltfilter(Boolean biltfilter) {
		this.biltfilter = biltfilter;
	}

	public String getBilmuser() {
		return bilmuser;
	}

	public void setBilmuser(String bilmuser) {
		this.bilmuser = bilmuser;
	}

	public Integer getBilpnoqty() {
		return bilpnoqty;
	}

	public void setBilpnoqty(Integer bilpnoqty) {
		this.bilpnoqty = bilpnoqty;
	}

	public String getBilsuser() {
		return bilsuser;
	}

	public void setBilsuser(String bilsuser) {
		this.bilsuser = bilsuser;
	}

	public Integer getBilpalready() {
		return bilpalready;
	}

	public void setBilpalready(Integer bilpalready) {
		this.bilpalready = bilpalready;
	}

	public Boolean getCheckrm() {
		return checkrm;
	}

	public void setCheckrm(Boolean checkrm) {
		this.checkrm = checkrm;
	}

}
