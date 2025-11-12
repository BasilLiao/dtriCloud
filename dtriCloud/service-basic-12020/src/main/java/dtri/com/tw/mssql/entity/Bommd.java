package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 組合單/A421<br>
 */
@Entity
@Table(name = "BOMTD")
@EntityListeners(AuditingEntityListener.class)
public class Bommd implements Cloneable {
	public Bommd() {
		super();
		this.newone = true;
	}

	@Id
	@Column(name = "BOMMD_ID")
	private String bomid;

	@Column(name = "MD001")
	private String md001;// --主元件-品號
	@Column(name = "MB002")
	private String mb002;// --主元件-品名
	@Column(name = "MB003")
	private String mb003;// --主元件-規格
	@Column(name = "MB009")
	private String mb009;// --主元件-商品描述

	@Column(name = "MD002")
	private String md002;// --子元件-序號
	@Column(name = "MD003")
	private String md003;// --子元件-品號
	@Column(name = "MD006")
	private Integer md006;// --子元件-用量
	@Column(name = "MD009")
	private String md009;// --子元件-製成
	@Column(name = "MD016")
	private String md016;// --子元件-備註

	@Column(name = "CMB002")
	private String cmb002;// --子元件-品名
	@Column(name = "CMB003")
	private String cmb003;// --子元件-規格
	@Column(name = "CMB009")
	private String cmb009;// --子元件-商品描述

	@Column(name = "CREATE_DATE")
	private String mdcdate;// --建立時間
	@Column(name = "MODI_DATE")
	private String mdmdate;// --修改時間
	@Column(name = "CREATOR")
	private String mdcuser;// --創建人
	@Column(name = "MODIFIER")
	private String mdmuser;// --修改人

	// 檢查新的?
	@Transient
	private boolean newone;

	public String getBomid() {
		return bomid;
	}

	public void setBomid(String bomid) {
		this.bomid = bomid;
	}

	public String getMd001() {
		return md001;
	}

	public void setMd001(String md001) {
		this.md001 = md001;
	}

	public String getMd002() {
		return md002;
	}

	public void setMd002(String md002) {
		this.md002 = md002;
	}

	public String getMd003() {
		return md003;
	}

	public void setMd003(String md003) {
		this.md003 = md003;
	}

	public Integer getMd006() {
		return md006;
	}

	public void setMd006(Integer md006) {
		this.md006 = md006;
	}

	public String getMd009() {
		return md009;
	}

	public void setMd009(String md009) {
		this.md009 = md009;
	}

	public String getMdcdate() {
		return mdcdate;
	}

	public void setMdcdate(String mdcdate) {
		this.mdcdate = mdcdate;
	}

	public String getMdmdate() {
		return mdmdate;
	}

	public void setMdmdate(String mdmdate) {
		this.mdmdate = mdmdate;
	}

	public String getMdcuser() {
		return mdcuser;
	}

	public void setMdcuser(String mdcuser) {
		this.mdcuser = mdcuser;
	}

	public String getMdmuser() {
		return mdmuser;
	}

	public void setMdmuser(String mdmuser) {
		this.mdmuser = mdmuser;
	}

	public String getMd016() {
		return md016;
	}

	public void setMd016(String md016) {
		this.md016 = md016;
	}

	public String getMb002() {
		return mb002;
	}

	public void setMb002(String mb002) {
		this.mb002 = mb002;
	}

	public String getMb003() {
		return mb003;
	}

	public void setMb003(String mb003) {
		this.mb003 = mb003;
	}

	public boolean isNewone() {
		return newone;
	}

	public void setNewone(boolean newone) {
		this.newone = newone;
	}

	public String getMb009() {
		return mb009;
	}

	public void setMb009(String mb009) {
		this.mb009 = mb009;
	}

	public String getCmb002() {
		return cmb002;
	}

	public void setCmb002(String cmb002) {
		this.cmb002 = cmb002;
	}

	public String getCmb003() {
		return cmb003;
	}

	public void setCmb003(String cmb003) {
		this.cmb003 = cmb003;
	}

	public String getCmb009() {
		return cmb009;
	}

	public void setCmb009(String cmb009) {
		this.cmb009 = cmb009;
	}

	@Override
	public String toString() {
		return "Bommd [md001=" + md001 + ", mb002=" + mb002 + ", mb003=" + mb003 + ", mb009=" + mb009 + ", md002="
				+ md002 + ", md003=" + md003 + ", md006=" + md006 + ", md009=" + md009 + ", md016=" + md016
				+ ", cmb002=" + cmb002 + ", cmb003=" + cmb003 + ", cmb009=" + cmb009 + "]";
	}

}
