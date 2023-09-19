package dtri.com.tw.pgsql.entity;

import java.util.Date;

/**
 * @author Basil
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---倉儲區域設置清單---<br>
 *      wss_erp_t_qty : (帳務)此區域物料數量<br>
 *      wss_t_qty : (實際)此區域物料數量<br>
 *      ---單據名稱---<br>
 *      wss_class_name:單據名稱<br>
 *      ---單據資料---<br>
 *      wss_class_nb:單別+單號<br>
 *      wss_sn : 序號<br>
 *      wss_type : 單據類型(領料類/入料類)<br>
 *      wss_c_user : 核准人<br>
 *      wss_m_user : 可分配-負責人<br>
 *      wss_f_user : 完成人<br>
 *      wss_acceptance : 物料檢驗0=未檢驗 1=已檢驗 2=異常<br>
 *      wss_p_number : 物料號<br>
 *      wss_p_name : 品名<br>
 *      wss_pn_qty : 數量<br>
 *      wss_pn_g_qty : 已取數量<br>
 */

public class WarehouseSynchronize {
	public WarehouseSynchronize() {
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
		this.setWsspngqty(0);
		// 倉儲區域清單-清單
		this.wsserptqty = 0;// : (帳務)此區域物料數量<br>
		this.wsstqty = 0;// : (實際)此區域物料數量<br>
	}

	// 共用型
	private Date syscdate;
	private String syscuser;
	private Date sysmdate;
	private String sysmuser;
	private Date sysodate;
	private String sysouser;

	private Boolean sysheader;
	private Integer sysstatus;
	private Integer syssort;
	private String sysnote;

	private String id;// 單別+單號+序號
	// 倉儲區域清單-清單
	private Integer wsserptqty;// : (帳務)此區域物料數量<br>
	private Integer wsstqty;// : (實際)此區域物料數量<br>
	// 單據
	private String wssclassname;// :單據名稱<br>
	//
	private String wssclasssn;// :單別+單號<br>
	private String wssnb;// : 序號<br>
	private String wsstype;// : 單據類型(領料類/入料類)<br>
	private String wsscuser;// 核准人
	private String wssfuser;// : 完成人<br>
	private String wsspnumber;// : 物料號<br>
	private Integer wsspnqty;// : 需(領/取)數量<br>
	private Integer wsspngqty;// : 已(領/取)數量<br>

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

	public Integer getWsserptqty() {
		return wsserptqty;
	}

	public void setWsserptqty(Integer wsserptqty) {
		this.wsserptqty = wsserptqty;
	}

	public Integer getWsstqty() {
		return wsstqty;
	}

	public void setWsstqty(Integer wsstqty) {
		this.wsstqty = wsstqty;
	}

	public String getWssclassname() {
		return wssclassname;
	}

	public void setWssclassname(String wssclassname) {
		this.wssclassname = wssclassname;
	}

	public String getWssclasssn() {
		return wssclasssn;
	}

	public void setWssclasssn(String wssclasssn) {
		this.wssclasssn = wssclasssn;
	}

	public String getWssnb() {
		return wssnb;
	}

	public void setWssnb(String wssnb) {
		this.wssnb = wssnb;
	}

	public String getWsstype() {
		return wsstype;
	}

	public void setWsstype(String wsstype) {
		this.wsstype = wsstype;
	}

	public String getWsscuser() {
		return wsscuser;
	}

	public void setWsscuser(String wsscuser) {
		this.wsscuser = wsscuser;
	}

	public String getWssfuser() {
		return wssfuser;
	}

	public void setWssfuser(String wssfuser) {
		this.wssfuser = wssfuser;
	}

	public String getWsspnumber() {
		return wsspnumber;
	}

	public void setWsspnumber(String wsspnumber) {
		this.wsspnumber = wsspnumber;
	}

	public Integer getWsspnqty() {
		return wsspnqty;
	}

	public void setWsspnqty(Integer wsspnqty) {
		this.wsspnqty = wsspnqty;
	}

	public Integer getWsspngqty() {
		return wsspngqty;
	}

	public void setWsspngqty(Integer wsspngqty) {
		this.wsspngqty = wsspngqty;
	}
}
