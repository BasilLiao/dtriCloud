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
public class Bomtd implements Cloneable {
	public Bomtd() {
		super();
	}

	@Id
	@Column(name = "BOMTD_ID")
	private String mocid;

	@Column(name = "TE001_TE002_TE003")
	private String te001_te002_te003;// 組合單/A421

	@Column(name = "TE004")
	private String te004;// --(-)元件號
	@Column(name = "TE007")
	private String te007;// --(-)出庫
	@Column(name = "TE008")
	private Integer te008;// --(-)元件數量
	@Column(name = "TE010")
	private String te010;// --(-)確認碼 Y/N/V

	@Column(name = "TD004")
	private String td004;// --(+)成品號
	@Column(name = "TD007")
	private Integer td007;// --(+)成品數量
	@Column(name = "TD010")
	private String td010;// --(+)入庫
	@Column(name = "TD016")
	private String td016;// --(+)簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]

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

	public String getTe001_te002_te003() {
		return te001_te002_te003;
	}

	public void setTe001_te002_te003(String te001_te002_te003) {
		this.te001_te002_te003 = te001_te002_te003;
	}

	public String getTe004() {
		return te004;
	}

	public void setTe004(String te004) {
		this.te004 = te004;
	}

	public String getTe007() {
		return te007;
	}

	public void setTe007(String te007) {
		this.te007 = te007;
	}

	public Integer getTe008() {
		return te008;
	}

	public void setTe008(Integer te008) {
		this.te008 = te008;
	}

	public String getTe010() {
		return te010;
	}

	public void setTe010(String te010) {
		this.te010 = te010;
	}

	public String getTd004() {
		return td004;
	}

	public void setTd004(String td004) {
		this.td004 = td004;
	}

	public Integer getTd007() {
		return td007;
	}

	public void setTd007(Integer td007) {
		this.td007 = td007;
	}

	public String getTd010() {
		return td010;
	}

	public void setTd010(String td010) {
		this.td010 = td010;
	}

	public String getTd016() {
		return td016;
	}

	public void setTd016(String td016) {
		this.td016 = td016;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public String toString() {
		return "Invte [te001_te002_te003=" + te001_te002_te003 + ", te004=" + te004 + ", te007=" + te007 + ", te008=" + te008 + ", te010=" + te010
				+ ", td004=" + td004 + ", td007=" + td007 + ", td010=" + td010 + ", td016=" + td016 + ", mb001=" + mb001 + ", mb002=" + mb002
				+ ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040
				+ ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}
}
