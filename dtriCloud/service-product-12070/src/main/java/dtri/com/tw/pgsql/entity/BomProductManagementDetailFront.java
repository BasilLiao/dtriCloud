package dtri.com.tw.pgsql.entity;

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
 *      ---產品細節項目---<br>
 *      bpmid:ID(產品ID)<br>
 *      "bisid(ID)":ID(項目ID),<br>
 *      "bisgid":GID(項目組ID),<br>
 *      "bisnb(物料號)":XXXX,<br>
 *      "bisname(物料名)":XXXX,<br>
 *      "qty(數量)":100,<br>
 *      "bisgname":"群組名稱",<br>
 *      "bisgfname(正規化群組值)":"XX XX XX",<br>
 *      "bisfname(正規化項目值)":"XX,XX,XX",<br>
 *      "bissdescripion(短敘述)":"",<br>
 *      "bisprocess(製程別)":"",<br>
 *      "where(條件)":[物料號_正負_數量,...],<br>
 * 
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
public class BomProductManagementDetailFront {

	public BomProductManagementDetailFront() {
		// 共用型
		this.sysheader = false;
		this.sysstatus = 0;
		this.sysnote = "";
		// 主體型
		this.bpmid = null;//
		this.bisid = null;//
		this.bisgid = null;//
		this.bisnb = "";//
		this.bisname = "";//
		this.bisqty = 0;//
		this.bisgname = "";//
		this.bisgfname = "";//
		this.bisfname = "";//
		this.bissdescripion = "";//
		this.biswhere = "";//
		this.setBisprocess("");//
	}

	// 共用型

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
	private Long bpmid;//
	@Transient
	private Long bisid;//
	@Transient
	private Long bisgid;//
	@Transient
	private String bisnb;//
	@Transient
	private String bisname;//
	@Transient
	private Integer bisqty;//
	@Transient
	private String bisgname;//
	@Transient
	private String bisgfname;//
	@Transient
	private String bisfname;//
	@Transient
	private String bissdescripion;//
	@Transient
	private String bisprocess;//
	@Transient
	private String biswhere;//
	

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

	public Long getBpmid() {
		return bpmid;
	}

	public void setBpmid(Long bpmid) {
		this.bpmid = bpmid;
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

	public Integer getBisqty() {
		return bisqty;
	}

	public void setBisqty(Integer bisqty) {
		this.bisqty = bisqty;
	}

	public String getBisgname() {
		return bisgname;
	}

	public void setBisgname(String bisgname) {
		this.bisgname = bisgname;
	}

	public String getBisgfname() {
		return bisgfname;
	}

	public void setBisgfname(String bisgfname) {
		this.bisgfname = bisgfname;
	}

	public String getBisfname() {
		return bisfname;
	}

	public void setBisfname(String bisfname) {
		this.bisfname = bisfname;
	}

	public String getBissdescripion() {
		return bissdescripion;
	}

	public void setBissdescripion(String bissdescripion) {
		this.bissdescripion = bissdescripion;
	}

	public String getBiswhere() {
		return biswhere;
	}

	public void setBiswhere(String biswhere) {
		this.biswhere = biswhere;
	}

	public String getBisprocess() {
		return bisprocess;
	}

	public void setBisprocess(String bisprocess) {
		this.bisprocess = bisprocess;
	}
}
