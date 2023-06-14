package dtri.com.tw.db.entity;

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
 * 
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---使用者帳戶---<br>
 *      su_id : 主key <br>
 *      su_name : 使用者名稱<br>
 *      su_e_name : 使用者英文名稱<br>
 *      su_position : 使用者[單位]名稱<br>
 *      su_template : 使用者[階級]名稱<br>
 *      階級 1=[約聘]/ 2=[助理] 3=[一般職員]/ 4=[主任] 5=[組長] 6=[領班] 7=[課長] 8=[副理]
 *      9=[經理]<br>
 *      su_account : 使用者帳號<br>
 *      su_password : 使用者密碼<br>
 *      su_email : 使用者E mail<br>
 *      su_sgid : 使用者權限群組<br>
 * 
 * @apiNote 標籤使用 @GeneratedValue<br>
 *          JPA提供的四種標準用法為TABLE，SEQUENCE，IDENTITY，AUTO。 <br>
 *          a，TABLE：使用一個特定的數據庫表格來保存主鍵。<br>
 *          b，SEQUENCE：根據底層數據庫的序列來生成主鍵，條件是數據庫支持序列。 <br>
 *          c，IDENTITY：主鍵由數據庫自動生成（主要是自動增長型）<br>
 *          d，AUTO：主鍵由程序控制。
 * 
 * @apiNote 標籤使用 @Column<br>
 *          varchar(50)<br>
 *          default ''<br>
 * 
 * @apiNote 標籤使用2<br>
 *          cascade CascadeType.PERSIST 在儲存時一併儲存 被參考的物件。 <br>
 *          CascadeType.MERGE 在合併修改時一併 合併修改被參考的物件。<br>
 *          CascadeType.REMOVE 在移除時一併移除 被參考的物件。 <br>
 *          CascadeType.REFRESH 在更新時一併更新 被參考的物件。<br>
 *          CascadeType.ALL 無論儲存、合併、 更新或移除，一併對被參考物件作出對應動作。<br>
 * 
 *          FetchType.LAZY時，
 *          除非真正要使用到該屬性的值，否則不會真正將資料從表格中載入物件，所以EntityManager後，才要載入該屬性值，就會發生例外錯誤，解決的方式
 *          之一是在EntityManager關閉前取得資料，另一個方式則是標示為FetchType.EARGE， 表示立即從表格取得資料
 * 
 * @Basic FetchType.EARGE <br>
 * @OneToOne FetchType.EARGE<br>
 * @ManyToOne FetchType.EARGE<br>
 * @OneToMany FetchType.LAZY<br>
 * @ManyToMany FetchType.LAZY<br>
 * 
 * 
 *             joinColumns：中間表的外來鍵欄位關聯當前實體類所對應表的主鍵欄位
 *             inverseJoinColumn：中間表的外來鍵欄位關聯對方表的主鍵欄位
 **/

@Entity
@Table(name = "system_user")
@EntityListeners(AuditingEntityListener.class)
public class SystemUser {

	public SystemUser() {
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";
		this.sysver = 0;
		this.sysnote = "";
		this.syssort = 0;
		this.sysstatus = 0;
		this.sysgheader = false;
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

	@Column(name = "sys_ver", columnDefinition = "int default 0")
	private Integer sysver;

	@Column(name = "sys_note", columnDefinition = "text default ''")
	private String sysnote;

	@Column(name = "sys_status", columnDefinition = "int default 0")
	private Integer sysstatus;

	@Column(name = "sys_sort", columnDefinition = "int default 0")
	private Integer syssort;

	@Column(name = "sys_g_header", nullable = false, columnDefinition = "boolean default false")
	private Boolean sysgheader;

	// 主體型
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "system_user_seq")
	@SequenceGenerator(name = "system_user_seq", sequenceName = "system_user_seq", allocationSize = 1)
	@Column(name = "su_id")
	private Long suid;

	@Column(name = "su_sg_g_id")
	private Long susggid;

	@Column(name = "su_name", nullable = false, columnDefinition = "varchar(50)")
	private String suname;

	@Column(name = "su_e_name", columnDefinition = "varchar(50)")
	private String suename;

	@Column(name = "su_position", columnDefinition = "varchar(50)")
	private String suposition;

	@Column(name = "su_template", nullable = false, columnDefinition = "varchar(50) default '一般職員'")
	private String sutemplate;

	@Column(name = "su_account", nullable = false, unique = true, columnDefinition = "varchar(50)")
	private String suaccount;

	@Column(name = "su_password", nullable = false, columnDefinition = "varchar(256)")
	private String supassword;

	@Column(name = "su_email", columnDefinition = "varchar(200) default ''")
	private String suemail;

	public Boolean getSysgheader() {
		return sysgheader;
	}

	public void setSysgheader(Boolean sysgheader) {
		this.sysgheader = sysgheader;
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

	public Integer getSysver() {
		return sysver;
	}

	public void setSysver(Integer sysver) {
		this.sysver = sysver;
	}

	public String getSysnote() {
		return sysnote;
	}

	public void setSysnote(String sysnote) {
		this.sysnote = sysnote;
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

	public Long getSuid() {
		return suid;
	}

	public void setSuid(Long suid) {
		this.suid = suid;
	}

	public Long getSusggid() {
		return susggid;
	}

	public void setSusggid(Long susggid) {
		this.susggid = susggid;
	}

	public String getSuname() {
		return suname;
	}

	public void setSuname(String suname) {
		this.suname = suname;
	}

	public String getSuename() {
		return suename;
	}

	public void setSuename(String suename) {
		this.suename = suename;
	}

	public String getSuposition() {
		return suposition;
	}

	public void setSuposition(String suposition) {
		this.suposition = suposition;
	}

	public String getSuaccount() {
		return suaccount;
	}

	public void setSuaccount(String suaccount) {
		this.suaccount = suaccount;
	}

	public String getSupassword() {
		return supassword;
	}

	public void setSupassword(String supassword) {
		this.supassword = supassword;
	}

	public String getSuemail() {
		return suemail;
	}

	public void setSuemail(String suemail) {
		this.suemail = suemail;
	}

	public String getSutemplate() {
		return sutemplate;
	}

	public void setSutemplate(String sutemplate) {
		this.sutemplate = sutemplate;
	}
}
