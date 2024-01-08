package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A541 廠內領料單<br>
 * A542 補料單<br>
 * A551 委外領料單<br>
 * A561 廠內退料單<br>
 * A571 委外退料單<br>
 */
@Entity
@Table(name = "MOCTE")
@EntityListeners(AuditingEntityListener.class)
public class Mocte {
	@Id
	@Column(name = "MOCTE_ID")
	private Long mocid;

	@Column(name = "TE001_TE002_TE003")
	private String ta026_ta027_ta028;// --領/退料單號
	@Column(name = "TA001_TA002")
	private String ta001_ta002;// 製令單

	@Column(name = "TA006")
	private String ta006;// 成品品號
	@Column(name = "TA009")
	private String ta009;// 預計開工日
	@Column(name = "TA011")
	private String ta011;// 確認結單?1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
	@Column(name = "TA015")
	private Integer ta015;// 產品套數
	@Column(name = "TA021")
	private String ta021;// --生產線別
	@Column(name = "TA034")
	private String ta034;// 成品品名
	@Column(name = "TA035")
	private String ta035;// 成品規格
	@Column(name = "TC007")
	private String tc007;// 領單頭備註

	@Column(name = "TE005")
	private Integer te005;// 領退料數量(ERP)
	@Column(name = "TB004")
	private Integer tb004;// 需領退用
	@Column(name = "TB005")
	private Integer tb005;// 已領用
	@Column(name = "TB009")
	private String tb009;// 庫別

	@Column(name = "TC008")
	private String tc008;// 單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
	@Column(name = "TC016")
	private String tc016;// --簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
	@Column(name = "TE013")
	private String te013;// 領料說明(可領用量)
	@Column(name = "TE014")
	private String te014;// 備註(來料時間 or 匹配進貨單)
	@Column(name = "TE019")
	private String te019;// --簽核確認碼 Y/N/V

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
	private String tk000;// 製令單
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

	public String getTa026_ta027_ta028() {
		return ta026_ta027_ta028;
	}

	public void setTa026_ta027_ta028(String ta026_ta027_ta028) {
		this.ta026_ta027_ta028 = ta026_ta027_ta028;
	}

	public String getTa001_ta002() {
		return ta001_ta002;
	}

	public void setTa001_ta002(String ta001_ta002) {
		this.ta001_ta002 = ta001_ta002;
	}

	public String getTa006() {
		return ta006;
	}

	public void setTa006(String ta006) {
		this.ta006 = ta006;
	}

	public String getTa009() {
		return ta009;
	}

	public void setTa009(String ta009) {
		this.ta009 = ta009;
	}

	public Integer getTa015() {
		return ta015;
	}

	public void setTa015(Integer ta015) {
		this.ta015 = ta015;
	}

	public String getTa021() {
		return ta021;
	}

	public void setTa021(String ta021) {
		this.ta021 = ta021;
	}

	public String getTa034() {
		return ta034;
	}

	public void setTa034(String ta034) {
		this.ta034 = ta034;
	}

	public String getTa035() {
		return ta035;
	}

	public void setTa035(String ta035) {
		this.ta035 = ta035;
	}

	public Integer getTb004() {
		return tb004;
	}

	public void setTb004(Integer tb004) {
		this.tb004 = tb004;
	}

	public Integer getTb005() {
		return tb005;
	}

	public void setTb005(Integer tb005) {
		this.tb005 = tb005;
	}

	public String getTc008() {
		return tc008;
	}

	public void setTc008(String tc008) {
		this.tc008 = tc008;
	}

	public String getTe013() {
		return te013;
	}

	public void setTe013(String te013) {
		this.te013 = te013;
	}

	public String getTe014() {
		return te014;
	}

	public void setTe014(String te014) {
		this.te014 = te014;
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

	public String getTc016() {
		return tc016;
	}

	public void setTc016(String tc016) {
		this.tc016 = tc016;
	}

	public String getTb009() {
		return tb009;
	}

	public void setTb009(String tb009) {
		this.tb009 = tb009;
	}

	public String getTe019() {
		return te019;
	}

	public void setTe019(String te019) {
		this.te019 = te019;
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

	public String getTc007() {
		return tc007;
	}

	public void setTc007(String tc007) {
		this.tc007 = tc007;
	}

	@Override
	public String toString() {
		return "Mocte [ta026_ta027_ta028=" + ta026_ta027_ta028 + ", ta001_ta002=" + ta001_ta002 + ", ta006=" + ta006
				+ ", ta009=" + ta009 + ", ta011=" + ta011 + ", ta015=" + ta015 + ", ta021=" + ta021 + ", ta034=" + ta034
				+ ", ta035=" + ta035 + ", tb004=" + tb004 + ", tb005=" + tb005 + ", tb009=" + tb009 + ", tc008=" + tc008
				+ ", tc016=" + tc016 + ", te013=" + te013 + ", te014=" + te014 + ", te019=" + te019 + ", mb001=" + mb001
				+ ", mb002=" + mb002 + ", mb003=" + mb003 + ", mb017=" + mb017 + ", mb032=" + mb032 + ", mb036=" + mb036
				+ ", mb039=" + mb039 + ", mb040=" + mb040 + ", mc002=" + mc002 + ", ma002=" + ma002 + ", tk000=" + tk000
				+ "]";
	}

	public Integer getTe005() {
		return te005;
	}

	public void setTe005(Integer te005) {
		this.te005 = te005;
	}

	public String getTa011() {
		return ta011;
	}

	public void setTa011(String ta011) {
		this.ta011 = ta011;
	}

}
