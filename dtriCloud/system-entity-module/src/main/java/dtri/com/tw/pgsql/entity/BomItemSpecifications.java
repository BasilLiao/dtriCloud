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
import jakarta.persistence.Transient;

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
 *      ---物料規格---<br>
 *      this.bisid = null;key<br>
 *      this.bisgid = null;Gkey<br>
 *      this.bisnb = "";物料號<br>
 *      this.bisname = "";物料名稱<br>
 * 
 *      this.bisspecifications = "";物料規格<br>
 *      this.bisdescription = "";物料描述<br>
 *      this.bissdescripion = "";物料短述<br>
 *      this.bisfname = "";物料/群組 正規畫<br>
 *      this.bisgname = "";群組名稱<br>
 *      this.bisgsplit = "";正規畫切割 <br>
 *      this.bisgcondition = "";條件清單(like/not like/=/!=) <br>
 *      this.bisgffield = "";指定欄位
 * 
 *      this.bisproduct = false;指定顯示單元 產品<br>
 *      this.bisaccessories = false;指定顯示單元 配件<br>
 *      this.bissfproduct = false;指定顯示單元 半成品<br>
 *      this.bisdevelopment = false;指定顯示單元 開發品<br>
 *      this.bispcb = false;指定顯示單元 板階<br>
 * 
 *      this.bismproduct = false;指定顯示單元 必填產品<br>
 *      this.bismaccessories = false;指定顯示單元 必填配件<br>
 *      this.bismsfproduct = false;指定顯示單元 必填半成品<br>
 *      this.bismdevelopment = false;指定顯示單元 必填開發品<br>
 *      this.bismpcb = false;指定顯示單元 必填板階<br>
 * 
 *      this.bisiauto = false;自動導入<br>
 *      this.bisdselect = false;預設選擇<br>
 * 
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
 * @apiNote 標籤使用 @Transient<br>
 *          略過不建立實體資料欄位<br>
 * 
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
@Table(name = "bom_item_specifications")
@EntityListeners(AuditingEntityListener.class)
public class BomItemSpecifications {

	public BomItemSpecifications() {
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
		// 主體型
		this.bisid = null;
		this.bisgid = null;
		this.bisnb = "";
		this.bisname = "";

		this.bisnb = "";
		this.bisspecifications = "";
		this.bisdescription = "";
		this.bissdescripion = "";
		this.bisfname = "";
		this.bisgname = "";
		this.bisgsplit = "";
		this.bisgcondition = "";
		this.setBisgffield("");
		this.setBisprocess("");

		this.bisproduct = false;
		this.bisaccessories = false;
		this.bissfproduct = false;
		this.bisdevelopment = false;
		this.bispcb = false;
		this.bisiauto = false;
		//
		this.setBismproduct(false);
		this.setBismaccessories(false);
		this.setBismsfproduct(false);
		this.setBismdevelopment(false);
		this.setBismpcb(false);
		this.setBislevel(0);

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

	// 主體型
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bom_item_specifications_seq")
	@SequenceGenerator(name = "bom_item_specifications_seq", sequenceName = "bom_item_specifications_seq", allocationSize = 1)
	@Column(name = "bis_id")
	private Long bisid;
	@Column(name = "bis_g_id")
	private Long bisgid;
	@Column(name = "bis_nb", nullable = false, columnDefinition = "varchar(150) default ''")
	private String bisnb;

	@Column(name = "bis_name", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bisname;
	@Column(name = "bis_specifications", nullable = false, columnDefinition = "text default ''")
	private String bisspecifications;
	@Column(name = "bis_description", nullable = false, columnDefinition = "text default ''")
	private String bisdescription;
	@Column(name = "bis_s_descripion", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bissdescripion;

	@Column(name = "bis_g_f_name", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bisgfname;
	@Column(name = "bis_f_name", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bisfname;
	@Column(name = "bis_g_name", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bisgname;
	@Column(name = "bis_g_split", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bisgsplit;
	@Column(name = "bis_g_condition", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bisgcondition;

	@Column(name = "bis_g_f_field", nullable = false, columnDefinition = "varchar(250) default ''")
	private String bisgffield;

	@Column(name = "bis_product", nullable = false, columnDefinition = "boolean default false")
	private Boolean bisproduct;
	@Column(name = "bis_accessories", nullable = false, columnDefinition = "boolean default false")
	private Boolean bisaccessories;
	@Column(name = "bis_sf_product", nullable = false, columnDefinition = "boolean default false")
	private Boolean bissfproduct;
	@Column(name = "bis_development", nullable = false, columnDefinition = "boolean default false")
	private Boolean bisdevelopment;
	@Column(name = "bis_pcb", nullable = false, columnDefinition = "boolean default false")
	private Boolean bispcb;
	// 必填
	@Column(name = "bis_m_product", nullable = false, columnDefinition = "boolean default false")
	private Boolean bismproduct;
	@Column(name = "bis_m_accessories", nullable = false, columnDefinition = "boolean default false")
	private Boolean bismaccessories;
	@Column(name = "bis_m_sf_product", nullable = false, columnDefinition = "boolean default false")
	private Boolean bismsfproduct;
	@Column(name = "bis_m_development", nullable = false, columnDefinition = "boolean default false")
	private Boolean bismdevelopment;
	@Column(name = "bis_m_pcb", nullable = false, columnDefinition = "boolean default false")
	private Boolean bismpcb;
	//
	@Column(name = "bis_i_auto", nullable = false, columnDefinition = "boolean default false")
	private Boolean bisiauto;
	@Column(name = "bis_d_select", nullable = false, columnDefinition = "boolean default false")
	private Boolean bisdselect;
	@Column(name = "bis_level", nullable = false, columnDefinition = "int default 0")
	private Integer bislevel;

	@Transient
	private String bisprocess;// 物料製成別

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

	public Long getBisid() {
		return bisid;
	}

	public void setBisid(Long bisid) {
		this.bisid = bisid;
	}

	public Long getBisgid() {
		return bisgid;
	}

	public void setBisgid(Long bisgid) {
		this.bisgid = bisgid;
	}

	public String getBisnb() {
		return bisnb;
	}

	public void setBisnb(String bisnb) {
		this.bisnb = bisnb;
	}

	public String getBisname() {
		return bisname;
	}

	public void setBisname(String bisname) {
		this.bisname = bisname;
	}

	public String getBisspecifications() {
		return bisspecifications;
	}

	public void setBisspecifications(String bisspecifications) {
		this.bisspecifications = bisspecifications;
	}

	public String getBisdescription() {
		return bisdescription;
	}

	public void setBisdescription(String bisdescription) {
		this.bisdescription = bisdescription;
	}

	public String getBissdescripion() {
		return bissdescripion;
	}

	public void setBissdescripion(String bissdescripion) {
		this.bissdescripion = bissdescripion;
	}

	public String getBisfname() {
		return bisfname;
	}

	public void setBisfname(String bisfname) {
		this.bisfname = bisfname;
	}

	public String getBisgname() {
		return bisgname;
	}

	public void setBisgname(String bisgname) {
		this.bisgname = bisgname;
	}

	public String getBisgsplit() {
		return bisgsplit;
	}

	public void setBisgsplit(String bisgsplit) {
		this.bisgsplit = bisgsplit;
	}

	public String getBisgcondition() {
		return bisgcondition;
	}

	public void setBisgcondition(String bisgcondition) {
		this.bisgcondition = bisgcondition;
	}

	public Boolean getBisproduct() {
		return bisproduct;
	}

	public void setBisproduct(Boolean bisproduct) {
		this.bisproduct = bisproduct;
	}

	public Boolean getBisaccessories() {
		return bisaccessories;
	}

	public void setBisaccessories(Boolean bisaccessories) {
		this.bisaccessories = bisaccessories;
	}

	public Boolean getBissfproduct() {
		return bissfproduct;
	}

	public void setBissfproduct(Boolean bissfproduct) {
		this.bissfproduct = bissfproduct;
	}

	public Boolean getBisdevelopment() {
		return bisdevelopment;
	}

	public void setBisdevelopment(Boolean bisdevelopment) {
		this.bisdevelopment = bisdevelopment;
	}

	public Boolean getBispcb() {
		return bispcb;
	}

	public void setBispcb(Boolean bispcb) {
		this.bispcb = bispcb;
	}

	public Boolean getBisiauto() {
		return bisiauto;
	}

	public void setBisiauto(Boolean bisiauto) {
		this.bisiauto = bisiauto;
	}

	public String getBisgfname() {
		return bisgfname;
	}

	public void setBisgfname(String bisgfname) {
		this.bisgfname = bisgfname;
	}

	public String getBisgffield() {
		return bisgffield;
	}

	public void setBisgffield(String bisgffield) {
		this.bisgffield = bisgffield;
	}

	public Boolean getBisdselect() {
		return bisdselect;
	}

	public void setBisdselect(Boolean bisdselect) {
		this.bisdselect = bisdselect;
	}

	public String getBisprocess() {
		return bisprocess;
	}

	public void setBisprocess(String bisprocess) {
		this.bisprocess = bisprocess;
	}

	public Integer getBislevel() {
		return bislevel;
	}

	public void setBislevel(Integer bislevel) {
		this.bislevel = bislevel;
	}

	public Boolean getBismpcb() {
		return bismpcb;
	}

	public void setBismpcb(Boolean bismpcb) {
		this.bismpcb = bismpcb;
	}

	public Boolean getBismdevelopment() {
		return bismdevelopment;
	}

	public void setBismdevelopment(Boolean bismdevelopment) {
		this.bismdevelopment = bismdevelopment;
	}

	public Boolean getBismsfproduct() {
		return bismsfproduct;
	}

	public void setBismsfproduct(Boolean bismsfproduct) {
		this.bismsfproduct = bismsfproduct;
	}

	public Boolean getBismaccessories() {
		return bismaccessories;
	}

	public void setBismaccessories(Boolean bismaccessories) {
		this.bismaccessories = bismaccessories;
	}

	public Boolean getBismproduct() {
		return bismproduct;
	}

	public void setBismproduct(Boolean bismproduct) {
		this.bismproduct = bismproduct;
	}

}
