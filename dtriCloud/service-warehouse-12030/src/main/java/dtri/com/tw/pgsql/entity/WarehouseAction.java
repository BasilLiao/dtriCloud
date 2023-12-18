package dtri.com.tw.pgsql.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

/**
 * @author Basil
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 * 
 *      ---單據名稱---<br>
 *      was_class_name:單據名稱<br>
 *      ---單據資料---<br>
 *      was_class_nb:單別+單號<br>
 *      was_sn : 序號<br>
 *      was_type : 單據類型(領料類/入料類)<br>
 *      was_m_user : 分配-負責人<br>
 *      was_f_user : 完成人<br>
 *      was_acceptance : 物料檢驗0=未檢驗 1=已檢驗 2=異常<br>
 *      was_p_number : 物料號<br>
 *      was_p_name : 品名<br>
 *      was_pn_qty : 數量<br>
 *      was_status : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=立即 / 2=完成<br>
 *      was_e_date : 預計領料日 <br>
 */
@Entity
public class WarehouseAction {
	public WarehouseAction() {
		// 共用型
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";

		this.sysnote = "";
		// 倉儲區域清單-清單
		this.wasaliaswmpnb = "";// : 倉儲_物料號<br>

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
	private String sysnote;
	@Id
	private String id;// 單別+單號+序號
	@Transient
	private String gid;// 別+單號
	// 倉儲區域清單-清單
	@Transient
	private String wasaliaswmpnb;// : 倉儲+物料號<br>

	// 單據
	@Transient
	private String wasclassname;// :單據名稱<br>
	@Transient
	private String wasclasssn;// :單別+單號<br>
	@Transient
	private String wasnb;// : 序號<br>
	@Transient
	private String wastype;// : 單據類型(領料類/入料類)<br>
	@Transient
	private String wasmuser;// : 分配-負責人<br>
	@Transient
	private String wasfuser;// : 完成人<br>
	@Transient
	private String waspnumber;// : 物料號<br>
	@Transient
	private String waspname;// : 品名<br>
	@Transient
	private Integer wasstatus;// : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
	@Transient
	private Date wasedate;// : 預計領/入料日

	@Transient
	private String wastocommand;// 單據指令對象 json [] A511-123456....<br>
	@Transient
	private String wasfromcommand;// 單據指令來源 json [] A511-123456....<br>
	@Transient
	private String wasfromwho;// :物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
	@Transient
	private String wastowho;// 物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>

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

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getWasaliaswmpnb() {
		return wasaliaswmpnb;
	}

	public void setWasaliaswmpnb(String wasaliaswmpnb) {
		this.wasaliaswmpnb = wasaliaswmpnb;
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

	public String getWasnb() {
		return wasnb;
	}

	public void setWasnb(String wasnb) {
		this.wasnb = wasnb;
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

	public Date getWasedate() {
		return wasedate;
	}

	public void setWasedate(Date wasedate) {
		this.wasedate = wasedate;
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

}
