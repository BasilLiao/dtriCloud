package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * 借出歸還A151+借入歸還單A161<br>
 */
@Entity
@Table(name = "INVTH")
@EntityListeners(AuditingEntityListener.class)
public class Invth {
	@Id
	@Column(name = "INVTH_ID")
	private Long mocid;

	@Column(name = "TI001_TI002_TI003")
	private String ti001_ti002_ti003;// 借出歸還A151+借入歸還單A161

	@Column(name = "TI014_TI015_TI016")
	private String ti014_ti015_ti016;// --來源

	@Column(name = "TI009")
	private Integer ti009;// 數量
	@Column(name = "TI007")
	private String ti007;// --轉出庫別
	@Column(name = "TI008")
	private String ti008;// --轉入庫別
	@Column(name = "TH006")
	private String th006;// --對象
	@Column(name = "TI022")
	private String ti022;// --確認碼
	@Column(name = "TH027")
	private String th027;// --簽核狀態碼

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

	public String getTi001_ti002_ti003() {
		return ti001_ti002_ti003;
	}

	public void setTi001_ti002_ti003(String ti001_ti002_ti003) {
		this.ti001_ti002_ti003 = ti001_ti002_ti003;
	}

	public String getTi014_ti015_ti016() {
		return ti014_ti015_ti016;
	}

	public void setTi014_ti015_ti016(String ti014_ti015_ti016) {
		this.ti014_ti015_ti016 = ti014_ti015_ti016;
	}

	public Integer getTi009() {
		return ti009;
	}

	public void setTi009(Integer ti009) {
		this.ti009 = ti009;
	}

	public String getTi007() {
		return ti007;
	}

	public void setTi007(String ti007) {
		this.ti007 = ti007;
	}

	public String getTi008() {
		return ti008;
	}

	public void setTi008(String ti008) {
		this.ti008 = ti008;
	}

	public String getTh006() {
		return th006;
	}

	public void setTh006(String th006) {
		this.th006 = th006;
	}

	public String getTi022() {
		return ti022;
	}

	public void setTi022(String ti022) {
		this.ti022 = ti022;
	}

	public String getTh027() {
		return th027;
	}

	public void setTh027(String th027) {
		this.th027 = th027;
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
		return "Invth [ti001_ti002_ti003=" + ti001_ti002_ti003 + ", ti014_ti015_ti016=" + ti014_ti015_ti016 + ", ti009=" + ti009 + ", ti007=" + ti007
				+ ", ti008=" + ti008 + ", th006=" + th006 + ", ti022=" + ti022 + ", th027=" + th027 + ", mb001=" + mb001 + ", mb002=" + mb002
				+ ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040
				+ ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}

}
