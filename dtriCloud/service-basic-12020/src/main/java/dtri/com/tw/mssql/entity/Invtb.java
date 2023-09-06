package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 物料清單<br>
 */
@Entity
@Table(name = "INVTB")
@EntityListeners(AuditingEntityListener.class)
public class Invtb {
	@Id
	@Column(name = "INVTB_ID")
	private Long mocid;

	@Column(name = "MB001")
	private String mb001;// 品號
	@Column(name = "MB002")
	private String mb002;// 品名
	@Column(name = "MB003")
	private String mb003;// 規格

	@Column(name = "MC002")
	private String mc002;// 倉別代號
	@Column(name = "CMC002")
	private String cmc002;// 倉別名稱
	@Column(name = "MC003")
	private String mc003;// 儲位位置
	@Column(name = "MC007")
	private Integer mc007;// 儲位數量

	@Column(name = "MB017")
	private String mb017;// 主要-倉別代號
	@Column(name = "MB032")
	private String mb032;// 主要-供應商代號
	@Column(name = "MB036")
	private Integer mb036;// 主要-固定前置天數
	@Column(name = "MB039")
	private Integer mb039;// 主要-最低補量
	@Column(name = "MB040")
	private Integer mb040;// 主要-補貨倍量
	@Column(name = "MA002")
	private String ma002;// 主要-供應商名稱

	// 檢查新的?
	@Transient
	private boolean newone;

	public Long getMocid() {
		return mocid;
	}

	public void setMocid(Long mocid) {
		this.mocid = mocid;
	}

	public String getMb001() {
		return mb001;
	}

	public void setMb001(String mb001) {
		this.mb001 = mb001;
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

	public String getMc002() {
		return mc002;
	}

	public void setMc002(String mc002) {
		this.mc002 = mc002;
	}

	public String getCmc002() {
		return cmc002;
	}

	public void setCmc002(String cmc002) {
		this.cmc002 = cmc002;
	}

	public String getMc003() {
		return mc003;
	}

	public void setMc003(String mc003) {
		this.mc003 = mc003;
	}

	public Integer getMc007() {
		return mc007;
	}

	public void setMc007(Integer mc007) {
		this.mc007 = mc007;
	}

	public String getMb017() {
		return mb017;
	}

	public void setMb017(String mb017) {
		this.mb017 = mb017;
	}

	public String getMb032() {
		return mb032;
	}

	public void setMb032(String mb032) {
		this.mb032 = mb032;
	}

	public Integer getMb036() {
		return mb036;
	}

	public void setMb036(Integer mb036) {
		this.mb036 = mb036;
	}

	public Integer getMb039() {
		return mb039;
	}

	public void setMb039(Integer mb039) {
		this.mb039 = mb039;
	}

	public Integer getMb040() {
		return mb040;
	}

	public void setMb040(Integer mb040) {
		this.mb040 = mb040;
	}

	public String getMa002() {
		return ma002;
	}

	public void setMa002(String ma002) {
		this.ma002 = ma002;
	}

	public boolean isNewone() {
		return newone;
	}

	public void setNewone(boolean newone) {
		this.newone = newone;
	}

	@Override
	public String toString() {
		return "Invtb [mb001=" + mb001 + ", mb002=" + mb002 + ", mb003=" + mb003 + ", mc002=" + mc002 + ", cmc002=" + cmc002 + ", mc003=" + mc003
				+ ", mc007=" + mc007 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040
				+ ", ma002=" + ma002 + "]";
	}

}
