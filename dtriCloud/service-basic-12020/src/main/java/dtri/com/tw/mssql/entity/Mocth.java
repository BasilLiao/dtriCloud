package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A591 委外入庫單<br>
 */
@Entity
@Table(name = "MOCTH")
@EntityListeners(AuditingEntityListener.class)
public class Mocth {
	@Id
	@Column(name = "MOCTH_ID")
	private Long mocid;

	@Column(name = "TI001_TI002_TI003")
	private String ti001_ti002_ti003;// --委外入庫單
	@Column(name = "TI013_TI014")
	private String ti013_ti014;// 製令單

	@Column(name = "TI007")
	private Integer ti007;// 入庫數量
	@Column(name = "TI009")
	private String ti009;// 庫別

	@Column(name = "TI035")
	private String ti035;// --1.待驗,Y(2).檢驗合格,N(3).檢驗不合格,0.免檢
	@Column(name = "TI037")
	private String ti037;// --確認碼 Y/N/V
	@Column(name = "TI048")
	private String ti048;// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
	@Column(name = "TH005")
	private String th005;// --加工廠商之代號

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

	public String getTi001_ti002_ti003() {
		return ti001_ti002_ti003;
	}

	public void setTi001_ti002_ti003(String ti001_ti002_ti003) {
		this.ti001_ti002_ti003 = ti001_ti002_ti003;
	}

	public String getTi013_ti014() {
		return ti013_ti014;
	}

	public void setTi013_ti014(String ti013_ti014) {
		this.ti013_ti014 = ti013_ti014;
	}

	public Integer getTi007() {
		return ti007;
	}

	public void setTi007(Integer ti007) {
		this.ti007 = ti007;
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

	public String getTi035() {
		return ti035;
	}

	public void setTi035(String ti035) {
		this.ti035 = ti035;
	}

	public String getTi048() {
		return ti048;
	}

	public void setTi048(String ti048) {
		this.ti048 = ti048;
	}

	public String getTh005() {
		return th005;
	}

	public void setTh005(String th005) {
		this.th005 = th005;
	}

	public String getTi009() {
		return ti009;
	}

	public void setTi009(String ti009) {
		this.ti009 = ti009;
	}

	public String getTi037() {
		return ti037;
	}

	public void setTi037(String ti037) {
		this.ti037 = ti037;
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
		return "Mocth [ti001_ti002_ti003=" + ti001_ti002_ti003 + ", ti013_ti014=" + ti013_ti014 + ", ti007=" + ti007 + ", ti009=" + ti009 + ", ti035="
				+ ti035 + ", ti037=" + ti037 + ", ti048=" + ti048 + ", th005=" + th005 + ", mb001=" + mb001 + ", mb002=" + mb002 + ", mb003=" + mb003
				+ ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040 + ", mc002=" + mc002
				+ ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}

}
