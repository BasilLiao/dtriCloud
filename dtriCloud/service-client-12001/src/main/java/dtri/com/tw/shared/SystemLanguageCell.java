package dtri.com.tw.shared;

import java.util.Date;

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
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---系統翻譯---<br>
 *      sl_id : ID<br>
 *      sl_sp_control : 單元 後台控制ID<br>
 *      sl_target : 翻譯目標 名稱<br>
 *      sl_class : 翻譯種類 0=無作用 1=Menu item(功能項目名稱) 2=Cell(欄位名稱) 3=訊息翻譯<br>
 *      sl_language : 翻譯國家(使用底線分割) zh-TW_en-US_vi-VN <br>
 *      sl_c_show : 查詢-欄位顯示?<br>
 *      sl_c_width: 查詢-欄位寬度?<br>
 *      sl_cm_type: 修改-欄位類型<br>
 *      sl_cm_select:修改-欄位選單<br>
 *      sl_cm_placeholder:修改-欄位提示<br>
 *      sl_cm_def_val:修改-欄位預設文字<br>
 *      sl_cm_show:修改-欄位顯示?<br>
 *      sl_cm_must:修改-欄位必填?<br>
 *      sl_cm_fixed:修改-欄位固定?<br>
 */
public class SystemLanguageCell {
	public SystemLanguageCell() {
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

		// 系統語言
		this.slspcontrol = "";
		this.sltarget = "";
		this.slclass = 0;
		this.sllanguage = "";
		this.slcshow = 0;// 欄位?顯示
		this.slcwidth = 100;// 欄位?寬度
		// 修改欄位設置
		this.slcmtype = "text";
		this.slcmselect = "[]";
		this.slcmplaceholder = "";
		this.slcmdefval = "";
		this.slcmshow = 1;
		this.slcmmust = 1;
		this.slcmfixed = 0;
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

	// 系統語言型
	private Long slid;
	private String slspcontrol;
	private String sltarget;
	private Integer slclass;
	private String sllanguage;
	private Integer slcshow;
	private Integer slcwidth;

	private String slcmtype;
	private String slcmselect;
	private String slcmplaceholder;
	private String slcmdefval;
	private Integer slcmshow;
	private Integer slcmmust;
	private Integer slcmfixed;

	// 前端格式-修改/查詢用
	@Transient
	private String sl_zhTW;
	@Transient
	private String sl_zhCN;
	@Transient
	private String sl_enUS;
	@Transient
	private String sl_viVN;

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

	public Long getSlid() {
		return slid;
	}

	public void setSlid(Long slid) {
		this.slid = slid;
	}

	public String getSlspcontrol() {
		return slspcontrol;
	}

	public void setSlspcontrol(String slspcontrol) {
		this.slspcontrol = slspcontrol;
	}

	public String getSltarget() {
		return sltarget;
	}

	public void setSltarget(String sltarget) {
		this.sltarget = sltarget;
	}

	public Integer getSlclass() {
		return slclass;
	}

	public void setSlclass(Integer slclass) {
		this.slclass = slclass;
	}

	public String getSllanguage() {
		return sllanguage;
	}

	public void setSllanguage(String sllanguage) {
		this.sllanguage = sllanguage;
	}

	public Integer getSlcshow() {
		return slcshow;
	}

	public void setSlcshow(Integer slcshow) {
		this.slcshow = slcshow;
	}

	public Integer getSlcwidth() {
		return slcwidth;
	}

	public void setSlcwidth(Integer slcwidth) {
		this.slcwidth = slcwidth;
	}

	public String getSlcmtype() {
		return slcmtype;
	}

	public void setSlcmtype(String slcmtype) {
		this.slcmtype = slcmtype;
	}

	public String getSlcmselect() {
		return slcmselect;
	}

	public void setSlcmselect(String slcmselect) {
		this.slcmselect = slcmselect;
	}

	public String getSlcmplaceholder() {
		return slcmplaceholder;
	}

	public void setSlcmplaceholder(String slcmplaceholder) {
		this.slcmplaceholder = slcmplaceholder;
	}

	public String getSlcmdefval() {
		return slcmdefval;
	}

	public void setSlcmdefval(String slcmdefval) {
		this.slcmdefval = slcmdefval;
	}

	public Integer getSlcmmust() {
		return slcmmust;
	}

	public void setSlcmmust(Integer slcmmust) {
		this.slcmmust = slcmmust;
	}

	public Integer getSlcmshow() {
		return slcmshow;
	}

	public void setSlcmshow(Integer slcmshow) {
		this.slcmshow = slcmshow;
	}

	public Integer getSlcmfixed() {
		return slcmfixed;
	}

	public void setSlcmfixed(Integer slcmfixed) {
		this.slcmfixed = slcmfixed;
	}

	public String getSl_zhTW() {
		return sl_zhTW;
	}

	public void setSl_zhTW(String sl_zhTW) {
		this.sl_zhTW = sl_zhTW;
	}

	public String getSl_zhCN() {
		return sl_zhCN;
	}

	public void setSl_zhCN(String sl_zhCN) {
		this.sl_zhCN = sl_zhCN;
	}

	public String getSl_enUS() {
		return sl_enUS;
	}

	public void setSl_enUS(String sl_enUS) {
		this.sl_enUS = sl_enUS;
	}

	public String getSl_viVN() {
		return sl_viVN;
	}

	public void setSl_viVN(String sl_viVN) {
		this.sl_viVN = sl_viVN;
	}

}
