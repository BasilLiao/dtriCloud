package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 借出A131+借入單A141<br>
 */
@Entity
@Table(name = "INVTG")
@EntityListeners(AuditingEntityListener.class)
public class Invtg {
	@Id
	@Column(name = "INVTG_ID")
	private String mocid;

	@Column(name = "TG001_TG002_TG003")
	private String tg001_tg002_tg003;// --借出A131+借入單A141

	@Column(name = "TF015")
	private String tf015;// --借出對象
	@Column(name = "TG007")
	private String tg007;// --轉出庫別
	@Column(name = "TG008")
	private String tg008;// --轉入庫別
	@Column(name = "TG009")
	private Integer tg009;// 數量
	@Column(name = "TG022")
	private String tg022;// --確認碼(單身)
	@Column(name = "TF020")
	private String tf020;// --確認碼(頭身)
	@Column(name = "TF028")
	private String tf028;// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]

	@Column(name = "MB001")
	private String mb001;// 品號
	@Column(name = "MB002")
	private String mb002;// 品名
	@Column(name = "MB003")
	private String mb003;// 規格
	@Column(name = "MB017")
	private String mb017;// 倉別代號
	@Column(name = "MB032")
	private String mb032;// 供應商代號
	@Column(name = "MB036")
	private Integer mb036;// 固定前置天數
	@Column(name = "MB039")
	private Integer mb039;// 最低補量
	@Column(name = "MB040")
	private Integer mb040;// 補貨倍量
	@Column(name = "MC002")
	private String mc002;// 倉別名稱
	@Column(name = "MA002")
	private String ma002;// 供應商名稱
	@Column(name = "TK000")
	private String tk000;// 入庫單
	@Column(name = "CREATE_DATE")
	private String createdate;// 單據建立時間
	@Column(name = "MODI_DATE")
	private String modidate;// 單據修改時間
	@Column(name = "CREATOR")
	private String creator;// 單據建立者

	// 檢查新的?
	@Transient
	private boolean newone;

	public String getMocid() {
		return mocid;
	}

	public void setMocid(String mocid) {
		this.mocid = mocid;
	}

	public String getTg001_tg002_tg003() {
		return tg001_tg002_tg003;
	}

	public void setTg001_tg002_tg003(String tg001_tg002_tg003) {
		this.tg001_tg002_tg003 = tg001_tg002_tg003;
	}

	public String getTf015() {
		return tf015;
	}

	public void setTf015(String tf015) {
		this.tf015 = tf015;
	}

	public String getTg007() {
		return tg007;
	}

	public void setTg007(String tg007) {
		this.tg007 = tg007;
	}

	public String getTg008() {
		return tg008;
	}

	public void setTg008(String tg008) {
		this.tg008 = tg008;
	}

	public Integer getTg009() {
		return tg009;
	}

	public void setTg009(Integer tg009) {
		this.tg009 = tg009;
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

	public String getTg022() {
		return tg022;
	}

	public void setTg022(String tg022) {
		this.tg022 = tg022;
	}

	public String getTf028() {
		return tf028;
	}

	public void setTf028(String tf028) {
		this.tf028 = tf028;
	}

	public String getCreatedate() {
		return createdate;
	}

	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}

	public String getModidate() {
		return modidate;
	}

	public void setModidate(String modidate) {
		this.modidate = modidate;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String toString() {
		return "Invtg [tg001_tg002_tg003=" + tg001_tg002_tg003 + ", tf015=" + tf015 + ", tg007=" + tg007 + ", tg008=" + tg008 + ", tg009=" + tg009
				+ ", tg022=" + tg022 + ", tf028=" + tf028 + ", mb001=" + mb001 + ", mb002=" + mb002 + ", mb003=" + mb003 + ", mb017=" + mb017
				+ ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040 + ", mc002=" + mc002 + ", ma002=" + ma002
				+ ", tk000=" + tk000 + "]";
	}

	/**
	 * @return the tf020
	 */
	public String getTf020() {
		return tf020;
	}

	/**
	 * @param tf020 the tf020 to set
	 */
	public void setTf020(String tf020) {
		this.tf020 = tf020;
	}

}
