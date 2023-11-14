package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 調撥/費用/A111-A112-A119-A121<br>
 */
@Entity
@Table(name = "INVTA")
@EntityListeners(AuditingEntityListener.class)
public class Invta implements Cloneable {
	public Invta() {
		super();
	}

	@Id
	@Column(name = "INVTA_ID")
	private Long mocid;

	@Column(name = "TB001_TB002_TB003")
	private String tb001_tb002_tb003;// 調撥/費用/A111-A112-A119-A121

	@Column(name = "TB007")
	private Integer tb007;// 數量
	@Column(name = "TB012")
	private String tb012;// --轉出庫別
	@Column(name = "TB013")
	private String tb013;// --轉入庫別

	@Column(name = "TB018")
	private String tb018;// --確認碼
	@Column(name = "TA016")
	private String ta016;// --簽核狀態碼

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

	public Long getMocid() {
		return mocid;
	}

	public void setMocid(Long mocid) {
		this.mocid = mocid;
	}

	public String getTb001_tb002_tb003() {
		return tb001_tb002_tb003;
	}

	public void setTb001_tb002_tb003(String tb001_tb002_tb003) {
		this.tb001_tb002_tb003 = tb001_tb002_tb003;
	}

	public Integer getTb007() {
		return tb007;
	}

	public void setTb007(Integer tb007) {
		this.tb007 = tb007;
	}

	public String getTb012() {
		return tb012;
	}

	public void setTb012(String tb012) {
		this.tb012 = tb012;
	}

	public String getTb013() {
		return tb013;
	}

	public void setTb013(String tb013) {
		this.tb013 = tb013;
	}

	public String getTb018() {
		return tb018;
	}

	public void setTb018(String tb018) {
		this.tb018 = tb018;
	}

	public String getTa016() {
		return ta016;
	}

	public void setTa016(String ta016) {
		this.ta016 = ta016;
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
		return "Invta [tb001_tb002_tb003=" + tb001_tb002_tb003 + ", tb007=" + tb007 + ", tb012=" + tb012 + ", tb013=" + tb013 + ", tb018=" + tb018
				+ ", ta016=" + ta016 + ", mb001=" + mb001 + ", mb002=" + mb002 + ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032
				+ ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040 + ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
