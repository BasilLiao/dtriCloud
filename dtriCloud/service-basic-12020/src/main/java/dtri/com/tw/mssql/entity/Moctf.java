package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A581 入庫單<br>
 */
@Entity
@Table(name = "MOCTF")
@EntityListeners(AuditingEntityListener.class)
public class Moctf {
	@Id
	@Column(name = "MOCTF_ID")
	private Long mocid;

	@Column(name = "TG001_TG002_TG003")
	private String tg001_tg002_tg003;// --入庫單
	@Column(name = "TG014_TG015")
	private String tg014_tg015;// 製令單
	@Column(name = "TG010")
	private String tg010;// 庫別
	@Column(name = "TG011")
	private Integer tg011;// 入庫數量
	@Column(name = "TG016")
	private String tg016;// --入庫代驗(1.待驗,Y.檢驗合格,N.檢驗不合格,0.免檢)
	@Column(name = "TG020")
	private String tg020;// --備註(電池充放電?)
	@Column(name = "TG022")
	private String tg022;// --簽核確認碼 Y/N/V
	@Column(name = "TF003")
	private String tf003;// --入庫時間
	@Column(name = "TF014")
	private String tf014;// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]

	@Column(name = "MB001")
	private String mb001;// 品號
	@Column(name = "MB002")
	private String mb002;// 品名
	@Column(name = "MB003")
	private String mb003;// 規格
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
	@Column(name = "MC002")
	private String mc002;// 主要-倉別名稱
	@Column(name = "MA002")
	private String ma002;// 主要-供應商名稱
	@Column(name = "TK000")
	private String tk000;// 入庫單

	// 檢查新的?
	@Transient
	private boolean newone;

	public Long getMocid() {
		return mocid;
	}

	public void setMocid(Long mocid) {
		this.mocid = mocid;
	}

	public String getTg001_tg002_tg003() {
		return tg001_tg002_tg003;
	}

	public void setTg001_tg002_tg003(String tg001_tg002_tg003) {
		this.tg001_tg002_tg003 = tg001_tg002_tg003;
	}

	public String getTg014_tg015() {
		return tg014_tg015;
	}

	public void setTg014_tg015(String tg014_tg015) {
		this.tg014_tg015 = tg014_tg015;
	}

	public Integer getTg011() {
		return tg011;
	}

	public void setTg011(Integer tg011) {
		this.tg011 = tg011;
	}

	public String getTg016() {
		return tg016;
	}

	public void setTg016(String tg016) {
		this.tg016 = tg016;
	}

	public String getTg020() {
		return tg020;
	}

	public void setTg020(String tg020) {
		this.tg020 = tg020;
	}

	public String getTf003() {
		return tf003;
	}

	public void setTf003(String tf003) {
		this.tf003 = tf003;
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

	public String getMc002() {
		return mc002;
	}

	public void setMc002(String mc002) {
		this.mc002 = mc002;
	}

	public String getMa002() {
		return ma002;
	}

	public void setMa002(String ma002) {
		this.ma002 = ma002;
	}

	public String getTk000() {
		return tk000;
	}

	public void setTk000(String tk000) {
		this.tk000 = tk000;
	}

	public boolean isNewone() {
		return newone;
	}

	public void setNewone(boolean newone) {
		this.newone = newone;
	}

	public String getTf014() {
		return tf014;
	}

	public void setTf014(String tf014) {
		this.tf014 = tf014;
	}

	public String getTg010() {
		return tg010;
	}

	public void setTg010(String tg010) {
		this.tg010 = tg010;
	}

	public String getTg022() {
		return tg022;
	}

	public void setTg022(String tg022) {
		this.tg022 = tg022;
	}

	@Override
	public String toString() {
		return "Moctf [tg001_tg002_tg003=" + tg001_tg002_tg003 + ", tg014_tg015=" + tg014_tg015 + ", tg010=" + tg010 + ", tg011=" + tg011 + ", tg016="
				+ tg016 + ", tg020=" + tg020 + ", tg022=" + tg022 + ", tf003=" + tf003 + ", tf014=" + tf014 + ", mb001=" + mb001 + ", mb002=" + mb002
				+ ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040
				+ ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}

}
