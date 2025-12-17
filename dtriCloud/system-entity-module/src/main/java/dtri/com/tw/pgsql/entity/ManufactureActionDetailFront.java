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
 * 
 *      ---倉儲區域設置清單---<br>
 *      mas_alias_wmpnb : 倉儲_物料號<br>
 *      mas_erp_t_qty : (帳務)此區域物料數量<br>
 *      mas_t_qty : (實際)此區域物料數量<br>
 *      mas_qc_qty : 進貨待驗<br>
 *      ---單據名稱---<br>
 *      mas_class_name:單據名稱<br>
 *      ---單據資料---<br>
 *      mas_class_nb:單別+單號<br>
 *      mas_sn : 序號<br>
 *      mas_type : 單據類型(領料類/入料類)<br>
 *      mas_m_user : 分配-負責人<br>
 *      mas_f_user : 完成人<br>
 *      mas_acceptance : 物料檢驗0=未檢驗 1=已檢驗 2=異常<br>
 *      mas_p_number : 物料號<br>
 *      mas_p_name : 品名<br>
 *      mas_pn_qty : 數量<br>
 *      mas_status : 單據狀態 3 = 取消 / 4=暫停 / 0=預設(3天) / 1=立即 / 2=完成<br>
 *      mas_e_date : 預計領料日 <br>
 */
@Entity
public class ManufactureActionDetailFront {
	public ManufactureActionDetailFront() {
		// 共用型
		this.syscdate = new Date();
		this.syscuser = "system";
		this.sysmdate = new Date();
		this.sysmuser = "system";

		this.sysnote = "";
		// 倉儲區域清單-清單
		this.masaliaswmpnb = "";// : 倉儲_物料號<br>
		this.maserptqty = 0;// : (帳務)此區域物料數量<br>
		this.mastqty = 0;// : (實際)此區域物料數量<br>
		this.masqcqty = 0;// : 進貨待驗<br>
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
	@Transient
	private Integer maserptqty;// : (帳務)此區域物料數量<br>
	@Transient
	private Integer mastqty;// : (實際)此區域物料數量<br>
	@Transient
	private Integer masqcqty;// : 進貨待驗<br>

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
	private String massmuser;// : 產線配料人<br>
	@Transient
	private String masfuser;// : 完成人<br>
	@Transient
	private String maspnumber;// : 物料號<br>
	@Transient
	private String maspname;// : 品名<br>
	@Transient
	private Integer maspnqty;// : 數量<br>
	@Transient
	private Integer maspngqty;// : 已完成數量<br>
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

	public Integer getMaserptqty() {
		return maserptqty;
	}

	public void setMaserptqty(Integer maserptqty) {
		this.maserptqty = maserptqty;
	}

	public Integer getMastqty() {
		return mastqty;
	}

	public void setMastqty(Integer mastqty) {
		this.mastqty = mastqty;
	}

	public Integer getMasqcqty() {
		return masqcqty;
	}

	public void setMasqcqty(Integer masqcqty) {
		this.masqcqty = masqcqty;
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

	public String getMassmuser() {
		return massmuser;
	}

	public void setMassmuser(String massmuser) {
		this.massmuser = massmuser;
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

	public Integer getMaspnqty() {
		return maspnqty;
	}

	public void setMaspnqty(Integer maspnqty) {
		this.maspnqty = maspnqty;
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

	public Integer getMaspngqty() {
		return maspngqty;
	}

	public void setMaspngqty(Integer maspngqty) {
		this.maspngqty = maspngqty;
	}
}
