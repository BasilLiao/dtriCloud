package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A341 國內進貨單<br>
 * A342 國外進貨單<br>
 * A343 台北進貨單<br>
 * A345 無採購進貨單<br>
 */
@Entity
@Table(name = "PURTH")
@EntityListeners(AuditingEntityListener.class)
public class Purth {
	@Id
	@Column(name = "PURTH_ID")
	private Long purid;

	@Column(name = "TH011_TH012_TH013")
	private String th011_th012_th013;// 採購單
	@Column(name = "TH001_TH002")
	private String th001_th002;// 進貨單
	@Column(name = "TH003")
	private String th003;// 進貨單序號
	@Column(name = "TH007")
	private Integer th007;// 進貨數量
	@Column(name = "TH009")
	private String th009;// 庫別
	@Column(name = "TH014")
	private String th014;// 驗收時間
	@Column(name = "TH028")
	private String th028;// 檢驗狀態0.免檢,1.待驗,2.合格,3.不良,4.特採
	@Column(name = "TH030")
	private String th030;// 簽核確認碼 Y/N/V
	@Column(name = "TH050")
	private String th050;// 簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]

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
	private String tk000;// 進貨單
	@Column(name = "CREATE_DATE")
	private String createdate;// 單據建立時間
	@Column(name = "MODI_DATE")
	private String modidate;// 單據修改時間
	@Column(name = "CREATOR")
	private String creator;// 單據建立者

	// 檢查新的?
	@Transient
	private boolean newone;

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

	public Long getPurid() {
		return purid;
	}

	public void setPurid(Long purid) {
		this.purid = purid;
	}

	public String getTh011_th012_th013() {
		return th011_th012_th013;
	}

	public void setTh011_th012_th013(String th011_th012_th013) {
		this.th011_th012_th013 = th011_th012_th013;
	}

	public String getTh001_th002() {
		return th001_th002;
	}

	public void setTh001_th002(String th001_th002) {
		this.th001_th002 = th001_th002;
	}

	public String getTh003() {
		return th003;
	}

	public void setTh003(String th003) {
		this.th003 = th003;
	}

	public Integer getTh007() {
		return th007;
	}

	public void setTh007(Integer th007) {
		this.th007 = th007;
	}

	public String getTh014() {
		return th014;
	}

	public void setTh014(String th014) {
		this.th014 = th014;
	}

	public String getTh028() {
		return th028;
	}

	public void setTh028(String th028) {
		this.th028 = th028;
	}

	public String getTh050() {
		return th050;
	}

	public void setTh050(String th050) {
		this.th050 = th050;
	}

	public String getTh009() {
		return th009;
	}

	public void setTh009(String th009) {
		this.th009 = th009;
	}

	public String getTh030() {
		return th030;
	}

	public void setTh030(String th030) {
		this.th030 = th030;
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
		return "Purth [th011_th012_th013=" + th011_th012_th013 + ", th001_th002=" + th001_th002 + ", th003=" + th003 + ", th007=" + th007 + ", th009="
				+ th009 + ", th014=" + th014 + ", th028=" + th028 + ", th030=" + th030 + ", th050=" + th050 + ", mb001=" + mb001 + ", mb002=" + mb002
				+ ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040
				+ ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}
}
