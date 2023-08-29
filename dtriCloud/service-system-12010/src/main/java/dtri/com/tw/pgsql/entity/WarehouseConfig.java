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
 * @see ---共用型---<br>
 *      sys_c_date : 創建時間<br>
 *      sys_c_user : 創建人名<br>
 *      sys_m_date : 修改時間<br>
 *      sys_m_user : 修改人名<br>
 *      sys_ver : 修改版本<br>
 *      sys_note : 備註<br>
 *      sys_status : 資料狀態<br>
 *      sys_sort : 自訂排序<br>
 *      ---倉儲設定-過濾器---<br>
 *      wc_id <br>
 *      wc_alias 倉庫別 Ex:A100<br>
 *      wc_wk_a_name 倉庫別名 Ex:原物料倉<br>
 *      wc_a_d_qty 該倉儲-單據是否 自動扣除數量<br>
 *      wc_a_i_qty該倉儲-單據是否 自動增加數量<br>
 * 
 */

@Entity
@Table(name = "warehouse_config")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseConfig {
	public WarehouseConfig() {
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
		// 倉儲設定-過濾器
		this.wcalias = "";
		this.wcwkaname = "";
		this.wcadqty = false;
		this.wcaiqty = false;
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

	// 倉儲設定-過濾器
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_config_seq")
	@SequenceGenerator(name = "warehouse_config_seq", sequenceName = "warehouse_config_seq", allocationSize = 1)
	@Column(name = "wc_id")
	private Long wcid;
	@Column(name = "wc_alias", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wcalias;
	@Column(name = "wc_wk_a_name", nullable = false, columnDefinition = "varchar(50) default ''")
	private String wcwkaname;
	@Column(name = "wc_a_d_qty", nullable = false, columnDefinition = "boolean default false")
	private Boolean wcadqty;
	@Column(name = "wc_a_i_qty", nullable = false, columnDefinition = "boolean default false")
	private Boolean wcaiqty;

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

	public Long getWcid() {
		return wcid;
	}

	public void setWcid(Long wcid) {
		this.wcid = wcid;
	}

	public String getWcalias() {
		return wcalias;
	}

	public void setWcalias(String wcalias) {
		this.wcalias = wcalias;
	}

	public String getWcwkaname() {
		return wcwkaname;
	}

	public void setWcwkaname(String wcwkaname) {
		this.wcwkaname = wcwkaname;
	}

	public Boolean getWcadqty() {
		return wcadqty;
	}

	public void setWcadqty(Boolean wcadqty) {
		this.wcadqty = wcadqty;
	}

	public Boolean getWcaiqty() {
		return wcaiqty;
	}

	public void setWcaiqty(Boolean wcaiqty) {
		this.wcaiqty = wcaiqty;
	}

}
