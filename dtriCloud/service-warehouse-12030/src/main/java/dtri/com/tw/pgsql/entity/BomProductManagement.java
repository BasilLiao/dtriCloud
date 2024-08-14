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
 *      ---負責人設置---<br>
 *      bpm_id : 主key <br>
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
@Table(name = "bom_product_management")
@EntityListeners(AuditingEntityListener.class)
public class BomProductManagement {

	public BomProductManagement() {
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

		this.bpmid = null;
		this.bpmnb = "";
		this.bpmmodel = "";
		this.bpmtype = 0;
		this.bpmtypename = "";
		this.setBpmbisitem("");
		this.bpmbisname = "";
		this.bpmbpsnv = "";
		this.bpmbisitem = "";
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bom_product_management_seq")
	@SequenceGenerator(name = "bom_product_management_seq", sequenceName = "bom_product_management_seq", allocationSize = 1)
	@Column(name = "bpm_id")
	private Long bpmid;

	@Column(name = "bpm_nb", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bpmnb;
	@Column(name = "bpm_model", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bpmmodel;
	@Column(name = "bpm_type", nullable = false, columnDefinition = "int default 0")
	private Integer bpmtype;
	@Column(name = "bpm_type_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String bpmtypename;
	@Column(name = "bpm_bis_item", nullable = false, columnDefinition = "text default ''")
	private String bpmbisitem;
	@Column(name = "bpm_bis_name", nullable = false, columnDefinition = "text default ''")
	private String bpmbisname;
	@Column(name = "bpm_bps_nv", nullable = false, columnDefinition = "text default ''")
	private String bpmbpsnv;

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

	public Long getBpmid() {
		return bpmid;
	}

	public void setBpmid(Long bpmid) {
		this.bpmid = bpmid;
	}

	public String getBpmnb() {
		return bpmnb;
	}

	public void setBpmnb(String bpmnb) {
		this.bpmnb = bpmnb;
	}

	public String getBpmmodel() {
		return bpmmodel;
	}

	public void setBpmmodel(String bpmmodel) {
		this.bpmmodel = bpmmodel;
	}

	public Integer getBpmtype() {
		return bpmtype;
	}

	public void setBpmtype(Integer bpmtype) {
		this.bpmtype = bpmtype;
	}

	public String getBpmtypename() {
		return bpmtypename;
	}

	public void setBpmtypename(String bpmtypename) {
		this.bpmtypename = bpmtypename;
	}

	public String getBpmbisname() {
		return bpmbisname;
	}

	public void setBpmbisname(String bpmbisname) {
		this.bpmbisname = bpmbisname;
	}

	public String getBpmbpsnv() {
		return bpmbpsnv;
	}

	public void setBpmbpsnv(String bpmbpsnv) {
		this.bpmbpsnv = bpmbpsnv;
	}

	public String getBpmbisitem() {
		return bpmbisitem;
	}

	public void setBpmbisitem(String bpmbisitem) {
		this.bpmbisitem = bpmbisitem;
	}

}
