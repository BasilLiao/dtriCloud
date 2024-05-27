package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * A511 廠內製令單<br>
 * A512 委外製令單<br>
 * A521 廠內重工單<br>
 * A522 委外領料單<br>
 * 
 * 
 */
@Entity
@Table(name = "MOCTA")
@EntityListeners(AuditingEntityListener.class)
public class Mocta {
	@Id
	@Column(name = "MOCTA_ID")
	private Long mocid;

	@Column(name = "TA026_TA027_TA028")
	private String ta026_ta027_ta028;// 訂單
	@Column(name = "TA001_TA002")
	private String ta001_ta002;// 製令單
	@Column(name = "TA006")
	private String ta006;// 成品品號

	@Column(name = "TA029")
	private String ta029;// ,--生管備註
	@Column(name = "TA050")
	private String ta050;// ,--訂單生產加工包裝資訊(客戶資訊)
	@Column(name = "MA003")
	private String ma003;// --產品機型

	@Column(name = "TA009")
	private String ta009;// 預計開工日
	@Column(name = "TA010")
	private String ta010;// 預計完工日
	@Column(name = "TA011")
	private String ta011;// --確認結單?1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
	@Column(name = "TB017")
	private String tb017;// --料項備註

	@Column(name = "TB015")
	private String tb015;// 預計領料日
	@Column(name = "TB004")
	private Integer tb004;// 需領用
	@Column(name = "TB005")
	private Integer tb005;// 已領用

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
	// 項次號?
	@Transient
	private String bslnb;

	public String getTa001_ta002() {
		return ta001_ta002;
	}

	public void setTa001_ta002(String ta001_ta002) {
		this.ta001_ta002 = ta001_ta002;
	}

	public String getTa009() {
		return ta009;
	}

	public void setTa009(String ta009) {
		this.ta009 = ta009;
	}

	public String getTa010() {
		return ta010;
	}

	public void setTa010(String ta010) {
		this.ta010 = ta010;
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

	public String getTb015() {
		return tb015;
	}

	public void setTb015(String tb015) {
		this.tb015 = tb015;
	}

	public String getTa006() {
		return ta006;
	}

	public void setTa006(String ta006) {
		this.ta006 = ta006;
	}

	@Override
	public String toString() {
		return "Mocta [ta026_ta027_ta028=" + ta026_ta027_ta028 + ", ta001_ta002=" + ta001_ta002 + ", ta006=" + ta006
				+ ", ta050=" + ta050 + ", ta009=" + ta009 + ", ta010=" + ta010 + ", tb015=" + tb015 + ", tb004=" + tb004
				+ ", tb005=" + tb005 + ", mb001=" + mb001 + ", mb002=" + mb002 + ", mb003=" + mb003 + ", mb017=" + mb017
				+ ", mb032=" + mb032 + ", mb036=" + mb036 + ", mb039=" + mb039 + ", mb040=" + mb040 + ", mc002=" + mc002
				+ ", ma002=" + ma002 + ", tk000=" + tk000 + "]";
	}

	public boolean isNewone() {
		return newone;
	}

	public void setNewone(boolean newone) {
		this.newone = newone;
	}

	public String getBslnb() {
		return bslnb;
	}

	public void setBslnb(String bslnb) {
		this.bslnb = bslnb;
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

	public String getTa050() {
		return ta050;
	}

	public void setTa050(String ta050) {
		this.ta050 = ta050;
	}

	public String getMa003() {
		return ma003;
	}

	public void setMa003(String ma003) {
		this.ma003 = ma003;
	}

	public String getTa011() {
		return ta011;
	}

	public void setTa011(String ta011) {
		this.ta011 = ta011;
	}

	public String getTa029() {
		return ta029;
	}

	public void setTa029(String ta029) {
		this.ta029 = ta029;
	}

	public String getTb017() {
		return tb017;
	}

	public void setTb017(String tb017) {
		this.tb017 = tb017;
	}

}
