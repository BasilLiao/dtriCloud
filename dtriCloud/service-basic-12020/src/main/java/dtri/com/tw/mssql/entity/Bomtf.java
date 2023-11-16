package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 拆解單/A431<br>
 */
@Entity
@Table(name = "BOMTF")
@EntityListeners(AuditingEntityListener.class)
public class Bomtf implements Cloneable {
	public Bomtf() {
		super();
	}

	@Id
	@Column(name = "BOMTF_ID")
	private Long mocid;
	// (BOMTG.TG001+'-'+TRIM(BOMTG.TG002)+'-'+BOMTG.TG003) as TG001_TG002_TG003,--單號

	@Column(name = "TG001_TG002_TG003")
	private String tg001_tg002_tg003;// OK 拆解單/A431

	@Column(name = "TF004")
	private String te004;// --(-)成品號
	@Column(name = "TF007")
	private Integer tf007;// --(-)成品數量
	@Column(name = "TF008")
	private String tf008;// --(-)出庫
	@Column(name = "TF014")
	private String tf014;// --(-)簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]

	@Column(name = "TG004")
	private String tg004;// --(+)元件號
	@Column(name = "TG007")
	private String tg007;// --(+)入庫
	@Column(name = "TG008")
	private Integer tg008;// --(+)元件數量
	@Column(name = "TG010")
	private String tg010;// --(+)確認碼 Y/N/V

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

	public String getTe004() {
		return te004;
	}

	public void setTe004(String te004) {
		this.te004 = te004;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	
	public String getTg001_tg002_tg003() {
		return tg001_tg002_tg003;
	}

	public void setTg001_tg002_tg003(String tg001_tg002_tg003) {
		this.tg001_tg002_tg003 = tg001_tg002_tg003;
	}

	public Integer getTf007() {
		return tf007;
	}

	public void setTf007(Integer tf007) {
		this.tf007 = tf007;
	}

	public String getTf008() {
		return tf008;
	}

	public void setTf008(String tf008) {
		this.tf008 = tf008;
	}

	public String getTf014() {
		return tf014;
	}

	public void setTf014(String tf014) {
		this.tf014 = tf014;
	}

	public String getTg004() {
		return tg004;
	}

	public void setTg004(String tg004) {
		this.tg004 = tg004;
	}

	public String getTg007() {
		return tg007;
	}

	public void setTg007(String tg007) {
		this.tg007 = tg007;
	}

	public Integer getTg008() {
		return tg008;
	}

	public void setTg008(Integer tg008) {
		this.tg008 = tg008;
	}

	public String getTg010() {
		return tg010;
	}

	public void setTg010(String tg010) {
		this.tg010 = tg010;
	}

	@Override
	public String toString() {
		return "Bomtf [tg001_tg002_tg003=" + tg001_tg002_tg003 + ", te004=" + te004 + ", tf007=" + tf007 + ", tf008=" + tf008 + ", tf014=" + tf014
				+ ", tg004=" + tg004 + ", tg007=" + tg007 + ", tg008=" + tg008 + ", tg010=" + tg010 + ", mb001=" + mb001 + ", mb002=" + mb002
				+ ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040
				+ ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000 + ", createdate=" + createdate + ", modidate=" + modidate
				+ ", creator=" + creator + "]";
	}

}
