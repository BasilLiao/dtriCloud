package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 銷貨單單 A231/A232<br>
 */
@Entity
@Table(name = "INVTG")
@EntityListeners(AuditingEntityListener.class)
public class Copth {
	@Id
	@Column(name = "COPTH_ID")
	private String mocid;

	@Column(name = "TH001_TH002_TH003")
	private String th001_th002_th003;// -- 銷貨單單 A231/A232

	@Column(name = "TH007")
	private String th007;// ,-- 出庫別
	@Column(name = "TH008")
	private Integer th008;// , --出庫數量
	@Column(name = "TH018")
	private String th018;// --備註
	@Column(name = "TH020")
	private String th020;// --簽核確認碼 Y/N/V
	@Column(name = "TG047")
	private String tg047;// ,--簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]
	@Column(name = "TG042")
	private String tg042;// ,--單據日期時間
	@Column(name = "TG020")
	private String tg020;// ,--單頭備註
	@Column(name = "TG007")
	private String tg007;// ,--客戶對象

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

	public String getTh001_th002_th003() {
		return th001_th002_th003;
	}

	public void setTh001_th002_th003(String th001_th002_th003) {
		this.th001_th002_th003 = th001_th002_th003;
	}

	public String getTh007() {
		return th007;
	}

	public void setTh007(String th007) {
		this.th007 = th007;
	}

	public Integer getTh008() {
		return th008;
	}

	public void setTh008(Integer th008) {
		this.th008 = th008;
	}

	public String getTh018() {
		return th018;
	}

	public void setTh018(String th018) {
		this.th018 = th018;
	}

	public String getTh020() {
		return th020;
	}

	public void setTh020(String th020) {
		this.th020 = th020;
	}

	public String getTg047() {
		return tg047;
	}

	public void setTg047(String tg047) {
		this.tg047 = tg047;
	}

	public String getTg042() {
		return tg042;
	}

	public void setTg042(String tg042) {
		this.tg042 = tg042;
	}

	public String getTg020() {
		return tg020;
	}

	public void setTg020(String tg020) {
		this.tg020 = tg020;
	}

	public String getTg007() {
		return tg007;
	}

	public void setTg007(String tg007) {
		this.tg007 = tg007;
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

	@Override
	public String toString() {
		return "Copth [th001_th002_th003=" + th001_th002_th003 + ", th007=" + th007 + ", th008=" + th008 + ", th018="
				+ th018 + ", th020=" + th020 + ", tg047=" + tg047 + ", tg042=" + tg042 + ", tg020=" + tg020 + "tg007="
				+ tg007 + ", mb001=" + mb001 + ", mb002=" + mb002 + ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032="
				+ mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040 + ", mc002=" + mc002 + ", ma002="
				+ ma002 + ", tk000=" + tk000 + "]";
	}
}
