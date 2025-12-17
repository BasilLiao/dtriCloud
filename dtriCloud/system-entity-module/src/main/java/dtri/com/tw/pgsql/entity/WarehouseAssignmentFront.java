package dtri.com.tw.pgsql.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * @author Basil
 * @see ---共用型---<br>
 *      Front 前端物件(ID之外其他添加@Transient)<br>
 * 
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---倉儲區域設置清單---<br>
 *      was_alias_wmpnb : 倉儲_物料號<br>
 *      was_erp_t_qty : (帳務)此區域物料數量<br>
 *      was_t_qty : (實際)此區域物料數量<br>
 *      was_qc_qty : 進貨待驗<br>
 *      ---單據名稱---<br>
 *      was_class_name:單據名稱<br>
 *      ---單據資料---<br>
 *      was_class_nb:單別+單號<br>
 *      was_sn : 序號<br>
 *      was_type : 單據類型(領料類/入料類)<br>
 *      was_c_user : 核准人<br>
 *      was_m_user : 可分配-負責人<br>
 *      was_f_user : 完成人<br>
 *      was_sm_user : 產線配料人<br>
 *      was_acceptance : 物料檢驗0=未檢驗 1=已檢驗 2=異常<br>
 *      was_p_number : 物料號<br>
 *      was_p_name : 品名<br>
 *      was_pn_qty : 數量<br>
 *      was_status : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=立即 / 2=完成<br>
 *      was_e_date : 預計領料日 <br>
 *      was_from_command : 指示來源<br>
 *      was_from_customer : 客戶資訊<br>
 */
@Entity
public class WarehouseAssignmentFront {
	public WarehouseAssignmentFront() {
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
		this.syshnote = "";// 單據備註
		// 倉儲區域清單-清單
		this.wasaliaswmpnb = "";// : 倉儲_物料號<br>
		this.wasschedule = "0/0";
		this.wascischedule = "0/0";
		this.waserpcuser = "";
		this.wassmuser = "";
		this.setWasfromcustomer("");
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
	@Transient
	private String syshnote;

	@Id
	private String id;
	@Transient // 單別+單號+序號
	private String gid;// 別+單號
	// 倉儲區域清單-清單
	@Transient
	private String wasaliaswmpnb;
	// 單據
	@Transient
	private String wasclassname;// :單據名稱<br>
	//
	@Transient
	private String wasclasssn;// :單別+單號<br>
	@Transient
	private String wasnb;// : 序號<br>
	@Transient
	private String wastype;// : 單據類型(領料類/入料類)<br>
	@Transient
	private String wascuser;// 核准人
	@Transient
	private String wassmuser;// 產線配料清點人
	@Transient
	private String wasmuser;// : 可分配-負責人<br>
	@Transient
	private String wasfuser;// : 完成人<br>
	@Transient
	private String wasacceptance;// : 物料檢驗 0=未檢驗 1=已檢驗 2=異常<br>
	@Transient
	private String waspnumber;// : 物料號<br>
	@Transient
	private String waspname;// : 品名<br>

	@Transient
	private Integer wasstatus;// : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
	@Transient
	private String wasstatusname;// : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
	@Transient
	private Date wasedate;// : 預計領/入料日
	@Transient
	private Date wassdate;// : 預計出貨日
	@Transient
	private String wasschedule;// 進度(50/100)
	@Transient
	private String wascischedule;// 集結-進度(50/100)
	@Transient
	private String waspalready;// 是否已打印(已打印/未打印)
	@Transient
	private String wastocommand;// 單據指令對象 json [] A511-123456....<br>
	@Transient
	private String wasfromcommand;// 單據指令來源 json [] A511-123456....<br>
	@Transient
	private String wasfromcustomer;// 單據來源客戶

	@Transient
	private String wasfromwho;// :物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
	@Transient
	private String wastowho;// 物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
	@Transient
	private String waserpcuser;// 單據開單人....<br>

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWasclassname() {
		return wasclassname;
	}

	public void setWasclassname(String wasclassname) {
		this.wasclassname = wasclassname;
	}

	public String getWasclasssn() {
		return wasclasssn;
	}

	public void setWasclasssn(String wasclasssn) {
		this.wasclasssn = wasclasssn;
	}

	public String getWastype() {
		return wastype;
	}

	public void setWastype(String wastype) {
		this.wastype = wastype;
	}

	public String getWasmuser() {
		return wasmuser;
	}

	public void setWasmuser(String wasmuser) {
		this.wasmuser = wasmuser;
	}

	public String getWasfuser() {
		return wasfuser;
	}

	public void setWasfuser(String wasfuser) {
		this.wasfuser = wasfuser;
	}

	public String getWasacceptance() {
		return wasacceptance;
	}

	public void setWasacceptance(String wasacceptance) {
		this.wasacceptance = wasacceptance;
	}

	public String getWaspnumber() {
		return waspnumber;
	}

	public void setWaspnumber(String waspnumber) {
		this.waspnumber = waspnumber;
	}

	public String getWaspname() {
		return waspname;
	}

	public void setWaspname(String waspname) {
		this.waspname = waspname;
	}

	public Integer getWasstatus() {
		return wasstatus;
	}

	public void setWasstatus(Integer wasstatus) {
		this.wasstatus = wasstatus;
	}

	public String getWasstatusname() {
		return wasstatusname;
	}

	public void setWasstatusname(String wasstatusname) {
		this.wasstatusname = wasstatusname;
	}

	public Date getWasedate() {
		return wasedate;
	}

	public void setWasedate(Date wasedate) {
		this.wasedate = wasedate;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getWasnb() {
		return wasnb;
	}

	public void setWasnb(String wasnb) {
		this.wasnb = wasnb;
	}

	public String getWastocommand() {
		return wastocommand;
	}

	public void setWastocommand(String wastocommand) {
		this.wastocommand = wastocommand;
	}

	public String getWasfromcommand() {
		return wasfromcommand;
	}

	public void setWasfromcommand(String wasfromcommand) {
		this.wasfromcommand = wasfromcommand;
	}

	public String getWasfromwho() {
		return wasfromwho;
	}

	public void setWasfromwho(String wasfromwho) {
		this.wasfromwho = wasfromwho;
	}

	public String getWastowho() {
		return wastowho;
	}

	public void setWastowho(String wastowho) {
		this.wastowho = wastowho;
	}

	public String getWasaliaswmpnb() {
		return wasaliaswmpnb;
	}

	public void setWasaliaswmpnb(String wasaliaswmpnb) {
		this.wasaliaswmpnb = wasaliaswmpnb;
	}

	public String getWascuser() {
		return wascuser;
	}

	public void setWascuser(String wascuser) {
		this.wascuser = wascuser;
	}

	public String getWasschedule() {
		return wasschedule;
	}

	public void setWasschedule(String wasschedule) {
		this.wasschedule = wasschedule;
	}

	public String getWaspalready() {
		return waspalready;
	}

	public void setWaspalready(String waspalready) {
		this.waspalready = waspalready;
	}

	public String getWaserpcuser() {
		return waserpcuser;
	}

	public void setWaserpcuser(String waserpcuser) {
		this.waserpcuser = waserpcuser;
	}

	public String getSyshnote() {
		return syshnote;
	}

	public void setSyshnote(String syshnote) {
		this.syshnote = syshnote;
	}

	public String getWassmuser() {
		return wassmuser;
	}

	public void setWassmuser(String wassmuser) {
		this.wassmuser = wassmuser;
	}

	public String getWascischedule() {
		return wascischedule;
	}

	public void setWascischedule(String wascischedule) {
		this.wascischedule = wascischedule;
	}

	public Date getWassdate() {
		return wassdate;
	}

	public void setWassdate(Date wassdate) {
		this.wassdate = wassdate;
	}

	/**
	 * @return the wasfromcustomer
	 */
	public String getWasfromcustomer() {
		return wasfromcustomer;
	}

	/**
	 * @param wasfromcustomer the wasfromcustomer to set
	 */
	public void setWasfromcustomer(String wasfromcustomer) {
		this.wasfromcustomer = wasfromcustomer;
	}

}
