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
 */
@Entity
@Table(name = "MOCTA")
@EntityListeners(AuditingEntityListener.class)
public class MoctaScheduleOutsourcer {
	@Id
	@Column(name = "MOCTA_ID")
	private String mocid;

	@Column(name = "TA001_TA002")
	private String ta001_ta002;// 製令單
	@Column(name = "TA006")
	private String ta006;// --成品品號
	@Column(name = "TA034")
	private String ta034;// --產品品名
	@Column(name = "TA035")
	private String ta035;// --產品規格

	@Column(name = "TA015")
	private Integer ta015;// --預計生產數
	@Column(name = "TA017")
	private Integer ta017;// --目前生產數

	@Column(name = "TA009")
	private String ta009;// 預計開工日
	@Column(name = "TA010")
	private String ta010;// 預計完工日
	@Column(name = "TA011")
	private String ta011;// 狀態碼1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
	@Column(name = "TA013")
	private String ta013;// 作廢狀態Y/N/V

	@Column(name = "TA029")
	private String ta029;// --製令備註(客戶/國家/訂單)
	@Column(name = "TA054")
	private String ta054;// --製令-自訂義備註(自動帶出)
	@Column(name = "TA032")
	private String ta032;// --加工廠(代號)
	@Column(name = "MA002")
	private String ma002;// --加工廠(中文)
	@Column(name = "TD004")
	private String td004;// --客戶品號

	@Column(name = "TC012")
	private String tc012;// --客戶-訂單單號
	@Column(name = "TC001_TC002")
	private String tc001_tc002;// --公司-訂單單號

	@Column(name = "CREATE_DATE")
	private String createdate;// 單據建立時間
	@Column(name = "MODI_DATE")
	private String modidate;// 單據修改時間
	@Column(name = "CREATOR")
	private String creator;// 單據建立者
	@Column(name = "MODIFIER")
	private String modifier;// 單據修改立者

	// 檢查新的?
	@Transient
	private boolean newone;

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

	public String getMocid() {
		return mocid;
	}

	public void setMocid(String mocid) {
		this.mocid = mocid;
	}

	public String getTa006() {
		return ta006;
	}

	public void setTa006(String ta006) {
		this.ta006 = ta006;
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

	public Integer getTa015() {
		return ta015;
	}

	public void setTa015(Integer ta015) {
		this.ta015 = ta015;
	}

	public Integer getTa017() {
		return ta017;
	}

	public void setTa017(Integer ta017) {
		this.ta017 = ta017;
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

	public String getTa054() {
		return ta054;
	}

	public void setTa054(String ta054) {
		this.ta054 = ta054;
	}

	public String getTa032() {
		return ta032;
	}

	public void setTa032(String ta032) {
		this.ta032 = ta032;
	}

	public String getMa002() {
		return ma002;
	}

	public void setMa002(String ma002) {
		this.ma002 = ma002;
	}

	public String getTd004() {
		return td004;
	}

	public void setTd004(String td004) {
		this.td004 = td004;
	}

	public String getTc012() {
		return tc012;
	}

	public void setTc012(String tc012) {
		this.tc012 = tc012;
	}

	public String getTc001_tc002() {
		return tc001_tc002;
	}

	public void setTc001_tc002(String tc001_tc002) {
		this.tc001_tc002 = tc001_tc002;
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

	public boolean isNewone() {
		return newone;
	}

	public void setNewone(boolean newone) {
		this.newone = newone;
	}

	@Override
	public String toString() {
		return "MoctaScheduleOutsourcer [mocid=" + mocid + ", ta001_ta002=" + ta001_ta002 + ", ta006=" + ta006
				+ ", ta034=" + ta034 + ", ta035=" + ta035 + ", ta015=" + ta015 + ", ta017=" + ta017 + ", ta009=" + ta009
				+ ", ta010=" + ta010 + ", ta011=" + ta011 + ", ta029=" + ta029 + ", ta054=" + ta054 + ", ta032=" + ta032
				+ ", ma002=" + ma002 + ", td004=" + td004 + ", tc012=" + tc012 + ", tc001_tc002=" + tc001_tc002 + "]";
	}

	public String getTa013() {
		return ta013;
	}

	public void setTa013(String ta013) {
		this.ta013 = ta013;
	}

	/**
	 * @return the modifier
	 */
	public String getModifier() {
		return modifier;
	}

	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
}
