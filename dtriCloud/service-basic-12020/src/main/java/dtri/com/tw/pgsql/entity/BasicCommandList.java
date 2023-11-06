package dtri.com.tw.pgsql.entity;

import java.util.Date;

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
 *      bcl_id : ID_KEY<br>
 *      bcl_nb : 項次號(從0開始)<br>
 *      bcl_class : 單別 A511/A521....<br>
 *      bcl_sn : 單號<br>
 *      bcl_type : 單據類型 (製令單/內製令....等等)<br>
 *      bcl_checkin : 核單 / 核項目 0=未核單 1=已核單<br>
 *      bcl_c_user: 核准人<br>
 *      bcl_f_user: 完成人<br>
 *      bcl_product: 產品<br>
 *      bcl_acceptance:檢驗項目: 0=未檢驗 1=已檢驗 2=異常<br>
 *      bcl_p_number:物料號 Ex:50-117-238132<br>
 *      bcl_p_name:物料品名 Ex:DT504T Mix Color ...<br>
 *      bcl_p_specification:物料規格<br>
 *      bcl_pn_qty:數量<br>
 *      bcl_to_command:單據指令對象 json [] A511-123456_領料單....<br>
 *      bcl_from_command:單據指令來源 json [] A511-123456_訂單....<br>
 *      bcl_to_who:物料對象 (倉庫)EX:A0001_原物料倉<br>
 *      bcl_from_who:物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
 *      bcl_status:單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=立即 / 2=完成<br>
 *      bcl_e_date:預計時間(入料日) 指 單一項目 到齊時間 或是預定 入料時間<br>
 *      bcl_f_date:預計時間(到齊日) 指 整張單都到齊 的時間<br>
 * 
 * 
 */
/**
 * @author Basil
 *
 */
@Entity
@Table(name = "basic_command_list")
@EntityListeners(AuditingEntityListener.class)
public class BasicCommandList {
	public BasicCommandList() {
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
		this.bclid = null;
		this.bclnb = "";
		this.bclclass = "";
		this.bclsn = "";
		this.bcltype = "";
		this.bclcheckin = 0;
		this.bclcuser = "";
		this.bclfuser = "";
		this.bclacceptance = 0;
		this.bclpnumber = "";
		this.bclpname = "";
		this.bclpspecification = "";
		this.bclpnqty = 0;
		this.bclpnaqty = 0;
		this.bcltocommand = "[]";
		this.bcltowho = "[]";
		this.bclfromcommand = "[]";
		this.bclfromwho = "[]";
		this.bclstatus = 0;
		this.bclfdate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.bcledate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.checkrm = true;
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "basic_command_list_seq")
	@SequenceGenerator(name = "basic_command_list_seq", sequenceName = "basic_command_list_seq", allocationSize = 1)
	@Column(name = "bcl_id")
	private Long bclid;
	@Column(name = "bcl_nb", nullable = false, columnDefinition = "varchar(10) default ''")
	private String bclnb;
	@Column(name = "bcl_class", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bclclass;
	@Column(name = "bcl_sn", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bclsn;
	@Column(name = "bcl_type", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bcltype;
	@Column(name = "bcl_product", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bclproduct;

	@Column(name = "bcl_checkin", nullable = false, columnDefinition = "int default 0")
	private Integer bclcheckin;
	@Column(name = "bcl_c_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bclcuser;
	@Column(name = "bcl_f_user", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bclfuser;
	@Column(name = "bcl_acceptance", nullable = false, columnDefinition = "int default 0")
	private Integer bclacceptance;

	@Column(name = "bcl_p_number", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bclpnumber;
	@Column(name = "bcl_p_name", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bclpname;
	@Column(name = "bcl_p_specification", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bclpspecification;
	@Column(name = "bcl_pn_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bclpnqty;
	@Column(name = "bcl_pn_a_qty", nullable = false, columnDefinition = "int default 0")
	private Integer bclpnaqty;

	@Column(name = "bcl_to_command", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bcltocommand;
	@Column(name = "bcl_from_command", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bclfromcommand;
	@Column(name = "bcl_to_who", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bcltowho;
	@Column(name = "bcl_from_who", nullable = false, columnDefinition = "varchar(150) default '[]'")
	private String bclfromwho;
	@Column(name = "bcl_status", nullable = false, columnDefinition = "int default 0")
	private Integer bclstatus;

	@Column(name = "bcl_e_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date bcledate;
	@Column(name = "bcl_f_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date bclfdate;
	@Column(name = "check_sum", nullable = false, columnDefinition = "text default ''")
	private String checksum;

	@JsonIgnore
	@Column(name = "check_rm", nullable = false, columnDefinition = "boolean default true")
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

	public Long getBclid() {
		return bclid;
	}

	public void setBclid(Long bclid) {
		this.bclid = bclid;
	}

	public String getBclnb() {
		return bclnb;
	}

	public void setBclnb(String bclnb) {
		this.bclnb = bclnb;
	}

	public String getBclclass() {
		return bclclass;
	}

	public void setBclclass(String bclclass) {
		this.bclclass = bclclass;
	}

	public String getBclsn() {
		return bclsn;
	}

	public void setBclsn(String bclsn) {
		this.bclsn = bclsn;
	}

	public String getBcltype() {
		return bcltype;
	}

	public void setBcltype(String bcltype) {
		this.bcltype = bcltype;
	}

	public Integer getBclcheckin() {
		return bclcheckin;
	}

	public void setBclcheckin(Integer bclcheckin) {
		this.bclcheckin = bclcheckin;
	}

	public String getBclcuser() {
		return bclcuser;
	}

	public void setBclcuser(String bclcuser) {
		this.bclcuser = bclcuser;
	}

	public String getBclfuser() {
		return bclfuser;
	}

	public void setBclfuser(String bclfuser) {
		this.bclfuser = bclfuser;
	}

	public Integer getBclacceptance() {
		return bclacceptance;
	}

	public void setBclacceptance(Integer bclacceptance) {
		this.bclacceptance = bclacceptance;
	}

	public String getBclpnumber() {
		return bclpnumber;
	}

	public void setBclpnumber(String bclpnumber) {
		this.bclpnumber = bclpnumber;
	}

	public String getBclpname() {
		return bclpname;
	}

	public void setBclpname(String bclpname) {
		this.bclpname = bclpname;
	}

	public String getBclpspecification() {
		return bclpspecification;
	}

	public void setBclpspecification(String bclpspecification) {
		this.bclpspecification = bclpspecification;
	}

	public Integer getBclpnqty() {
		return bclpnqty;
	}

	public void setBclpnqty(Integer bclpnqty) {
		this.bclpnqty = bclpnqty;
	}

	public String getBcltocommand() {
		return bcltocommand;
	}

	public void setBcltocommand(String bcltocommand) {
		this.bcltocommand = bcltocommand;
	}

	public String getBclfromcommand() {
		return bclfromcommand;
	}

	public void setBclfromcommand(String bclfromcommand) {
		this.bclfromcommand = bclfromcommand;
	}

	public String getBcltowho() {
		return bcltowho;
	}

	public void setBcltowho(String bcltowho) {
		this.bcltowho = bcltowho;
	}

	public String getBclfromwho() {
		return bclfromwho;
	}

	public void setBclfromwho(String bclfromwho) {
		this.bclfromwho = bclfromwho;
	}

	public Integer getBclstatus() {
		return bclstatus;
	}

	public void setBclstatus(Integer bclstatus) {
		this.bclstatus = bclstatus;
	}

	public Date getBcledate() {
		return bcledate;
	}

	public void setBcledate(Date bcledate) {
		this.bcledate = bcledate;
	}

	public Date getBclfdate() {
		return bclfdate;
	}

	public void setBclfdate(Date bclfdate) {
		this.bclfdate = bclfdate;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@Override
	public String toString() {
		return "BasicCommandList [syscdate=" + syscdate + ", syscuser=" + syscuser + ", sysmdate=" + sysmdate + ", sysmuser=" + sysmuser
				+ ", sysodate=" + sysodate + ", sysouser=" + sysouser + ", sysheader=" + sysheader + ", sysstatus=" + sysstatus + ", syssort="
				+ syssort + ", sysnote=" + sysnote + ", bclid=" + bclid + ", bclnb=" + bclnb + ", bclclass=" + bclclass + ", bclsn=" + bclsn
				+ ", bcltype=" + bcltype + ", bclcheckin=" + bclcheckin + ", bclcuser=" + bclcuser + ", bclfuser=" + bclfuser + ", bclacceptance="
				+ bclacceptance + ", bclpnumber=" + bclpnumber + ", bclpname=" + bclpname + ", bclpspecification=" + bclpspecification + ", bclpnqty="
				+ bclpnqty + ", bcltocommand=" + bcltocommand + ", bclfromcommand=" + bclfromcommand + ", bcltowho=" + bcltowho + ", bclfromwho="
				+ bclfromwho + ", bclstatus=" + bclstatus + ", bcledate=" + bcledate + ", bclfdate=" + bclfdate + ", checksum=" + checksum + "]";
	}

	public String getBclproduct() {
		return bclproduct;
	}

	public void setBclproduct(String bclproduct) {
		this.bclproduct = bclproduct;
	}

	public Integer getBclpnaqty() {
		return bclpnaqty;
	}

	public void setBclpnaqty(Integer bclpnaqty) {
		this.bclpnaqty = bclpnaqty;
	}

	public Boolean getCheckrm() {
		return checkrm;
	}

	public void setCheckrm(Boolean checkrm) {
		this.checkrm = checkrm;
	}

}
