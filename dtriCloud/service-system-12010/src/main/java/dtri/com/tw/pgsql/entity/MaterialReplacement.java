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
 *      ---PM參數設置---<br>
 *      mr_id : 主key <br>
 *      mr_nb : ERP 物料號(品號)<br>
 *      mr_name : ERP 物料名稱(品名)<br>
 *      mr_specification : ERP 物料規格(規格)<br>
 *      mr_note : 物料備註<br>
 *      mr_sub_note : 物料替代料<br>
 *      mr_nn_note : N對N替料<br>
 *      mr_cl_note : 客戶替代料<br>
 *      mr_p_note : 產品替代料<br>
 *      mr_prove : 替代料證明<br>
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
@Table(name = "material_replacement")
@EntityListeners(AuditingEntityListener.class)
public class MaterialReplacement {

	public MaterialReplacement() {
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

		this.mrclnote = "";
		this.mrid = null;
		this.mrname = "";
		this.mrnb = "";
		this.mrnnnote = "";
		this.mrnote = "";
		this.mrpnote = "";
		this.mrspecification = "";
		this.mrsubnote = "";
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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_replacement_seq")
	@SequenceGenerator(name = "material_replacement_seq", sequenceName = "material_replacement_seq", allocationSize = 1)
	@Column(name = "mr_id")
	private Long mrid;
	@Column(name = "mr_nb", nullable = false, unique = true, columnDefinition = "varchar(50) default ''")
	private String mrnb;
	@Column(name = "mr_name", nullable = false, columnDefinition = "text default ''")
	private String mrname;
	@Column(name = "mr_specification", nullable = false, columnDefinition = "text default ''")
	private String mrspecification;
	@Column(name = "mr_note", nullable = false, columnDefinition = "text default ''")
	private String mrnote;
	@Column(name = "mr_sub_note", nullable = false, columnDefinition = "text default ''")
	private String mrsubnote;

	@Column(name = "mr_nn_note", nullable = false, columnDefinition = "text default ''")
	private String mrnnnote;
	@Column(name = "mr_cl_note", nullable = false, columnDefinition = "text default ''")
	private String mrclnote;
	@Column(name = "mr_p_note", nullable = false, columnDefinition = "text default ''")
	private String mrpnote;
	@Column(name = "mr_prove", nullable = false, columnDefinition = "text default ''")
	private String mrprove;

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

	public Long getMrid() {
		return mrid;
	}

	public void setMrid(Long mrid) {
		this.mrid = mrid;
	}

	public String getMrnb() {
		return mrnb;
	}

	public void setMrnb(String mrnb) {
		this.mrnb = mrnb;
	}

	public String getMrname() {
		return mrname;
	}

	public void setMrname(String mrname) {
		this.mrname = mrname;
	}

	public String getMrspecification() {
		return mrspecification;
	}

	public void setMrspecification(String mrspecification) {
		this.mrspecification = mrspecification;
	}

	public String getMrnote() {
		return mrnote;
	}

	public void setMrnote(String mrnote) {
		this.mrnote = mrnote;
	}

	public String getMrsubnote() {
		return mrsubnote;
	}

	public void setMrsubnote(String mrsubnote) {
		this.mrsubnote = mrsubnote;
	}

	public String getMrnnnote() {
		return mrnnnote;
	}

	public void setMrnnnote(String mrnnnote) {
		this.mrnnnote = mrnnnote;
	}

	public String getMrclnote() {
		return mrclnote;
	}

	public void setMrclnote(String mrclnote) {
		this.mrclnote = mrclnote;
	}

	public String getMrpnote() {
		return mrpnote;
	}

	public void setMrpnote(String mrpnote) {
		this.mrpnote = mrpnote;
	}

	public String getMrprove() {
		return mrprove;
	}

	public void setMrprove(String mrprove) {
		this.mrprove = mrprove;
	}
}
