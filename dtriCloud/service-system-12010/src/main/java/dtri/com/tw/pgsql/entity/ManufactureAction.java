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
 *      mas_class_name:單據名稱<br>
 *      ---單據資料---<br>
 *      mas_class_nb:單別+單號<br>
 *      mas_sn : 序號<br>
 *      mas_type : 單據類型(領料類/入料類)<br>
 *      mas_acceptance : 物料檢驗0=未檢驗 1=已檢驗 2=異常<br>
 *      mas_p_number : 物料號<br>
 *      mas_p_name : 品名<br>
 *      mas_pn_qty : 數量<br>
 *      mas_status : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=立即 / 2=完成<br>
 *      mas_e_date : 預計領料日 <br>
 */
@Entity
public class ManufactureAction {
	public ManufactureAction() {
		// 共用型
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";

		this.sysnote = "";
		// 倉儲區域清單-清單
		this.masaliaswmpnb = "";// : 倉儲_物料號<br>

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
	private String masaliaswmpnb;// : 倉儲+物料號<br>

	// 單據
	@Transient
	private String masclassname;// :單據名稱<br>
	//
	@Transient
	private String masclasssn;// :單別+單號<br>
	@Transient
	private String masnb;// : 序號<br>
	@Transient
	private String mastype;// : 單據類型(領料類/入料類)<br>
	@Transient
	private String masmuser;// : 分配-負責人<br>
	@Transient
	private String masfuser;// : 完成人<br>
	@Transient
	private String maspnumber;// : 物料號<br>
	@Transient
	private String maspname;// : 品名<br>
	@Transient
	private Integer masstatus;// : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=手動標示急迫 / 2=立即<br>
	@Transient
	private Date masedate;// : 預計領/入料日

	@Transient
	private String mastocommand;// 單據指令對象 json [] A511-123456....<br>
	@Transient
	private String masfromcommand;// 單據指令來源 json [] A511-123456....<br>
	@Transient
	private String masfromwho;// :物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>
	@Transient
	private String mastowho;// 物料來源 (廠商 or 倉庫 or 產線) EX:A0001_原物料倉<br>

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

	public String getMasaliaswmpnb() {
		return masaliaswmpnb;
	}

	public void setMasaliaswmpnb(String masaliaswmpnb) {
		this.masaliaswmpnb = masaliaswmpnb;
	}

	public String getMasclassname() {
		return masclassname;
	}

	public void setMasclassname(String masclassname) {
		this.masclassname = masclassname;
	}

	public String getMasclasssn() {
		return masclasssn;
	}

	public void setMasclasssn(String masclasssn) {
		this.masclasssn = masclasssn;
	}

	public String getMasnb() {
		return masnb;
	}

	public void setMasnb(String masnb) {
		this.masnb = masnb;
	}

	public String getMastype() {
		return mastype;
	}

	public void setMastype(String mastype) {
		this.mastype = mastype;
	}

	public String getMasmuser() {
		return masmuser;
	}

	public void setMasmuser(String masmuser) {
		this.masmuser = masmuser;
	}

	public String getMasfuser() {
		return masfuser;
	}

	public void setMasfuser(String masfuser) {
		this.masfuser = masfuser;
	}

	public String getMaspnumber() {
		return maspnumber;
	}

	public void setMaspnumber(String maspnumber) {
		this.maspnumber = maspnumber;
	}

	public String getMaspname() {
		return maspname;
	}

	public void setMaspname(String maspname) {
		this.maspname = maspname;
	}

	public Integer getMasstatus() {
		return masstatus;
	}

	public void setMasstatus(Integer masstatus) {
		this.masstatus = masstatus;
	}

	public Date getMasedate() {
		return masedate;
	}

	public void setMasedate(Date masedate) {
		this.masedate = masedate;
	}

	public String getMastocommand() {
		return mastocommand;
	}

	public void setMastocommand(String mastocommand) {
		this.mastocommand = mastocommand;
	}

	public String getMasfromcommand() {
		return masfromcommand;
	}

	public void setMasfromcommand(String masfromcommand) {
		this.masfromcommand = masfromcommand;
	}

	public String getMasfromwho() {
		return masfromwho;
	}

	public void setMasfromwho(String masfromwho) {
		this.masfromwho = masfromwho;
	}

	public String getMastowho() {
		return mastowho;
	}

	public void setMastowho(String mastowho) {
		this.mastowho = mastowho;
	}

}
