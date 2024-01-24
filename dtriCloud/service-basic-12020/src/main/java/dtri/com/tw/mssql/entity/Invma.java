package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 機種別清單<br>
 */
@Entity
@Table(name = "INVMA")
@EntityListeners(AuditingEntityListener.class)
public class Invma implements Cloneable {
	public Invma() {
		this.newone = true;
	}

	@Id
	@Column(name = "INVMA_ID")
	private Long invid;

	@Column(name = "MA001")
	private String ma001;// 分類方式
	@Column(name = "MA002")
	private String ma002;// 產品代號
	@Column(name = "MA003")
	private String ma003;// 產品機種別

	// 檢查新的?
	@Transient
	private boolean newone;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public Long getInvid() {
		return invid;
	}

	public void setInvid(Long invid) {
		this.invid = invid;
	}

	public String getMa001() {
		return ma001;
	}

	public void setMa001(String ma001) {
		this.ma001 = ma001;
	}

	public String getMa002() {
		return ma002;
	}

	public void setMa002(String ma002) {
		this.ma002 = ma002;
	}

	public String getMa003() {
		return ma003;
	}

	public void setMa003(String ma003) {
		this.ma003 = ma003;
	}

	public boolean isNewone() {
		return newone;
	}

	public void setNewone(boolean newone) {
		this.newone = newone;
	}

	@Override
	public String toString() {
		return "Invma [ma001=" + ma001 + ", ma002=" + ma002 + ", ma003=" + ma003 + "]";
	}

}
