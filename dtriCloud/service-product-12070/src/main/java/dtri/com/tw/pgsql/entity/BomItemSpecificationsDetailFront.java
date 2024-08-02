package dtri.com.tw.pgsql.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
 * 
 *      this.bisproduct = false;指定顯示單元 產品<br>
 *      this.bisaccessories = false;指定顯示單元 配件<br>
 *      this.bissfproduct = false;指定顯示單元 半成品<br>
 *      this.bisdevelopment = false;指定顯示單元 開發品<br>
 *      this.bispcb = false;指定顯示單元 板階<br>
 *      this.bisiauto = false;自動導入<br>
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
public class BomItemSpecificationsDetailFront {

	public BomItemSpecificationsDetailFront() {
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

		this.bisproduct = false;
		this.bisaccessories = false;
		this.bissfproduct = false;
		this.bisdevelopment = false;
		this.bispcb = false;
		this.bisiauto = false;

	}

	// 共用型
	@Transient
	private Date syscdate;
	@Transient
	private String syscuser;
	@Transient
	private Date sysmdate;
	@Transient
	private String sysmuser;
	@Transient
	private Date sysodate;
	@Transient
	private String sysouser;

	@Transient
	private Boolean sysheader;
	@Transient
	private Integer sysstatus;
	@Transient
	private Integer syssort;
	@Transient
	private String sysnote;

	// 主體型
	@Id
	private Long bisid;
	@Transient
	private Long bisgid;
	@Transient
	private String bisnb;

	@Transient
	private String bisname;
	@Transient
	private String bisspecifications;
	@Transient
	private String bisdescription;
	@Transient
	private String bissdescripion;

	@Transient
	private String bisfname;
	@Transient
	private String bisgname;
	@Transient
	private String bisgsplit;
	@Transient
	private String bisgcondition;
	@Transient
	private String bisgffield;
	@Transient
	private String bisgfname;

	@Transient
	private Boolean bisproduct;
	@Transient
	private Boolean bisaccessories;
	@Transient
	private Boolean bissfproduct;
	@Transient
	private Boolean bisdevelopment;
	@Transient
	private Boolean bispcb;
	@Transient
	private Boolean bisiauto;

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

	public String getBisgffield() {
		return bisgffield;
	}

	public void setBisgffield(String bisgffield) {
		this.bisgffield = bisgffield;
	}

	public String getBisgfname() {
		return bisgfname;
	}

	public void setBisgfname(String bisgfname) {
		this.bisgfname = bisgfname;
	}

}
