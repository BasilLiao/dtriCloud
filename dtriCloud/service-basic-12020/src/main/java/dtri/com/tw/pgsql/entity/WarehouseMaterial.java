package dtri.com.tw.pgsql.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
 *      ---物料清單---<br>
 *      wmpnb : 物料號<br>
 *      wmname : 物料名稱<br>
 *      wmspecification : 物料規格<br>
 *      wmidate : new Date(253402271940000L);// 9999-12-31 23:59:00 最後盤點時間<br>
 *      wmimg : 圖片<br>
 *      wmadqty :自動減少<br>
 *      wmaiqty :自動增加<br>
 */

@Entity
@Table(name = "warehouse_material")
@EntityListeners(AuditingEntityListener.class)
public class WarehouseMaterial {
	public WarehouseMaterial() {
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
		// 倉儲物料-清單
		this.wmpnb = "";
		this.wmname = "";
		this.wmspecification = "";
		this.wmidate = new Date(253402271940000L);// 9999-12-31 23:59:00
		this.wmimg = "";
		this.wmadqty = false;
		this.wmaiqty = false;
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

	// 倉儲物料-清單
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "warehouse_material_seq")
	@SequenceGenerator(name = "warehouse_material_seq", sequenceName = "warehouse_material_seq", allocationSize = 1)
	@Column(name = "wm_id")
	private Long wmid;
	@Column(name = "wm_p_nb", nullable = false, unique = true, columnDefinition = "varchar(150) default ''")
	private String wmpnb;
	@Column(name = "wm_name", nullable = false, columnDefinition = "varchar(150) default ''")
	private String wmname;
	@Column(name = "wm_specification", nullable = false, columnDefinition = "varchar(250) default ''")
	private String wmspecification;
	@Column(name = "wm_i_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
	private Date wmidate;
	@Column(name = "wm_img", nullable = false, columnDefinition = "text default ''")
	private String wmimg;
	@Column(name = "wm_a_d_qty", nullable = false, columnDefinition = "boolean default false")
	private Boolean wmadqty;
	@Column(name = "wm_a_i_qty", nullable = false, columnDefinition = "boolean default false")
	private Boolean wmaiqty;
	@Column(name = "check_sum", nullable = false, columnDefinition = "text default ''")
	private String checksum;

	@OneToMany(mappedBy = "material")
	private List<WarehouseArea> warehouseAreas;

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

	public Long getWmid() {
		return wmid;
	}

	public void setWmid(Long wmid) {
		this.wmid = wmid;
	}

	public String getWmpnb() {
		return wmpnb;
	}

	public void setWmpnb(String wmpnb) {
		this.wmpnb = wmpnb;
	}

	public String getWmname() {
		return wmname;
	}

	public void setWmname(String wmname) {
		this.wmname = wmname;
	}

	public String getWmspecification() {
		return wmspecification;
	}

	public void setWmspecification(String wmspecification) {
		this.wmspecification = wmspecification;
	}

	public Date getWmidate() {
		return wmidate;
	}

	public void setWmidate(Date wmidate) {
		this.wmidate = wmidate;
	}

	public String getWmimg() {
		return wmimg;
	}

	public void setWmimg(String wmimg) {
		this.wmimg = wmimg;
	}

	public Boolean getWmadqty() {
		return wmadqty;
	}

	public void setWmadqty(Boolean wmadqty) {
		this.wmadqty = wmadqty;
	}

	public Boolean getWmaiqty() {
		return wmaiqty;
	}

	public void setWmaiqty(Boolean wmaiqty) {
		this.wmaiqty = wmaiqty;
	}

	public List<WarehouseArea> getWarehouseAreas() {
		return warehouseAreas;
	}

	public void setWarehouseAreas(List<WarehouseArea> warehouseAreas) {
		this.warehouseAreas = warehouseAreas;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

}
