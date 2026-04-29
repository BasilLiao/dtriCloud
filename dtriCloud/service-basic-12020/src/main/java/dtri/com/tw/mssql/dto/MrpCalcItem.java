package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

/**
 * MRP 運算核心物件 (欄位名稱對應 DB)
 * 用途：統一轉成這個物件後，進行排序與配給運算
 */
public class MrpCalcItem implements Comparable<MrpCalcItem> {

    // --- 1. 基本識別 (完全對應 DB) ---
    private String MB001; // 料號
    private String MB002; // 品名
    private String MB003; // 規格

    // --- 2. 單據資訊 (對應 DB: MaterialShortageList) ---
    private String TK000; // 單別 (DocType: 庫存/採購/客訂單...)
    private String TK001; // 單號 (DocNo)
    private String TK002; // 預計日期 (Date: yyyyMMdd)
    private String TK003; // 產品品號 (ProductNo)
    private String SYS_NOTE; // 備註 (暫存 SYS_SY009 分配明細用) /*還不確定備註流程是如何 還未開發 */
    private String TA032; // 加工廠商
    private String TC004; // 客戶代號
    private String COPMA002; // 客戶名稱
    // --- 3. 數量核心 (運算用) ---
    // 正數(+)代表供給，負數(-)代表需求
    // 最後會拆分給 SYS_SY003(未領) 和 SYS_SY004(未交)
    private BigDecimal QTY;

    // 待驗量 (SYS_SY005)
    private BigDecimal SYS_SY005;

    // --- 4. 主檔參數 (對應 INVMB / DB 欄位) ---
    private BigDecimal MC004; // 平均月用量
    private Integer MB036; // L/T 固定供貨天數
    private BigDecimal MB039; // MOQ 最低補量
    private BigDecimal MB040; // MPQ 最小包裝

    // --- 5. 補充資訊 (對應 DB) ---
    private String MB032; // 供應商代號
    private String MA002; // 供應商名稱
    private String MB017; // 倉別代號
    private String MC002; // 倉別名稱
    private String sourceRaw; // 原始資料

    // --- 6. 演算法專用 ---
    // 記錄這張供給單「還剩下多少」沒被吃掉
    private BigDecimal REMAINING_QTY = BigDecimal.ZERO;
    // 初始
    private BigDecimal INITIAL_QTY;

    // 為了判斷 [新採] 邏輯加入的暫存欄位
    private String CREATE_DATE;

    // ============================================
    // 建構子
    // ============================================
    public MrpCalcItem() {
        this.QTY = BigDecimal.ZERO;
        this.SYS_SY005 = BigDecimal.ZERO;
        this.REMAINING_QTY = BigDecimal.ZERO;
        this.MC004 = BigDecimal.ZERO;
        this.MB039 = BigDecimal.ZERO;
        this.MB040 = BigDecimal.ZERO;
    }

    // 供給量初始化
    public void initSupply() {
        // 1. 取得當前數量 (防呆: 若 null 則視為 0)
        BigDecimal currentQty = (this.QTY != null) ? this.QTY : BigDecimal.ZERO;

        // 2. 判斷是否為供給 (大於 0)
        if (currentQty.compareTo(BigDecimal.ZERO) > 0) {
            // 是供給：剩餘量 = 原始數量
            this.REMAINING_QTY = currentQty;

            if (this.INITIAL_QTY == null) {
                this.INITIAL_QTY = currentQty;
            }
        } else {
            // 是需求或零：剩餘供給量歸零
            this.REMAINING_QTY = BigDecimal.ZERO;

            // 需求單通常不需要 INITIAL_QTY，或視為 0
            if (this.INITIAL_QTY == null) {
                this.INITIAL_QTY = BigDecimal.ZERO;
            }
        }
    }

    // 關鍵方法：排序邏輯 (Step 2 會用到)
    @Override
    public int compareTo(MrpCalcItem other) {
        // 1. 比對料號 (MB001) - 防呆用
        int idComp = (this.MB001 == null ? "" : this.MB001).compareTo(other.MB001 == null ? "" : other.MB001);
        if (idComp != 0)
            return idComp;

        // 2. 比對日期 (TK002) - 越早越前面 (Null 視為 99999999)
        String d1 = (this.TK002 == null) ? "99999999" : this.TK002;
        String d2 = (other.TK002 == null) ? "99999999" : other.TK002;
        int dateComp = d1.compareTo(d2);
        if (dateComp != 0)
            return dateComp;

        // 3. 比對進出貨性質 (Supply First)
        // 我們不直接比 QTY 大小，而是先分「正營」和「負營」
        boolean isSupply1 = this.QTY != null && this.QTY.compareTo(BigDecimal.ZERO) >= 0;
        boolean isSupply2 = other.QTY != null && other.QTY.compareTo(BigDecimal.ZERO) >= 0;

        if (isSupply1 && !isSupply2)
            return -1; // 我是進貨(前)，他是出貨(後)
        if (!isSupply1 && isSupply2)
            return 1; // 我是出貨(後)，他是進貨(前)

        // 4. 修正點：如果日期一樣、性質也一樣，改比「單號 TK001」
        // 讓舊單號 (A331-2507...) 排在新單號 (A331-2512...) 前面
        String no1 = (this.TK001 == null) ? "" : this.TK001;
        String no2 = (other.TK001 == null) ? "" : other.TK001;

        return no1.compareTo(no2); // 升序 (String 自然排序)
    }

    public BigDecimal getINITIAL_QTY() {
        return INITIAL_QTY;
    }

    public void setINITIAL_QTY(BigDecimal iNITIAL_QTY) {
        INITIAL_QTY = iNITIAL_QTY;
    }

    public String getTA032() {
        return TA032;
    }

    public void setTA032(String TA032) {
        this.TA032 = TA032;
    }

    public String getMB001() {
        return MB001;
    }

    public void setMB001(String MB001) {
        this.MB001 = MB001;
    }

    public String getMB002() {
        return MB002;
    }

    public void setMB002(String MB002) {
        this.MB002 = MB002;
    }

    public String getMB003() {
        return MB003;
    }

    public void setMB003(String MB003) {
        this.MB003 = MB003;
    }

    public String getTK000() {
        return TK000;
    }

    public void setTK000(String TK000) {
        this.TK000 = TK000;
    }

    public String getTK001() {
        return TK001;
    }

    public void setTK001(String TK001) {
        this.TK001 = TK001;
    }

    public String getTK002() {
        return TK002;
    }

    public void setTK002(String TK002) {
        this.TK002 = TK002;
    }

    public String getTC004() {
        return TC004;
    }

    public void setTC004(String TC004) {
        this.TC004 = TC004;
    }

    public String getCOPMA002() {
        return COPMA002;
    }

    public void setCOPMA002(String COPMA002) {
        this.COPMA002 = COPMA002;
    }

    public String getTK003() {
        return TK003;
    }

    public void setTK003(String TK003) {
        this.TK003 = TK003;
    }

    public String getSYS_NOTE() {
        return SYS_NOTE;
    }

    public void setSYS_NOTE(String SYS_NOTE) {
        this.SYS_NOTE = SYS_NOTE;
    }

    public BigDecimal getQTY() {
        return QTY;
    }

    public void setQTY(BigDecimal QTY) {
        this.QTY = QTY;
    }

    public BigDecimal getSYS_SY005() {
        return SYS_SY005;
    }

    public void setSYS_SY005(BigDecimal SYS_SY005) {
        this.SYS_SY005 = SYS_SY005;
    }

    public BigDecimal getMC004() {
        return MC004;
    }

    public void setMC004(BigDecimal MC004) {
        this.MC004 = MC004;
    }

    public Integer getMB036() {
        return MB036;
    }

    public void setMB036(Integer MB036) {
        this.MB036 = MB036;
    }

    public BigDecimal getMB039() {
        return MB039;
    }

    public void setMB039(BigDecimal MB039) {
        this.MB039 = MB039;
    }

    public BigDecimal getMB040() {
        return MB040;
    }

    public void setMB040(BigDecimal MB040) {
        this.MB040 = MB040;
    }

    public String getMB032() {
        return MB032;
    }

    public void setMB032(String MB032) {
        this.MB032 = MB032;
    }

    public String getMA002() {
        return MA002;
    }

    public void setMA002(String MA002) {
        this.MA002 = MA002;
    }

    public String getMB017() {
        return MB017;
    }

    public void setMB017(String MB017) {
        this.MB017 = MB017;
    }

    public String getMC002() {
        return MC002;
    }

    public void setMC002(String MC002) {
        this.MC002 = MC002;
    }

    public BigDecimal getREMAINING_QTY() {
        return REMAINING_QTY;
    }

    public void setREMAINING_QTY(BigDecimal REMAINING_QTY) {
        this.REMAINING_QTY = REMAINING_QTY;
    }

    public String getSourceRaw() {
        return sourceRaw;
    }

    public void setSourceRaw(String sourceRaw) {
        this.sourceRaw = sourceRaw;
    }

    public String getCREATE_DATE() {
        return CREATE_DATE;
    }

    public void setCREATE_DATE(String CREATE_DATE) {
        this.CREATE_DATE = CREATE_DATE;
    }

    // toString 方便除錯
    @Override
    public String toString() {
        return "Item{" +
                "MB001='" + MB001 + '\'' +
                ", TK000='" + TK000 + '\'' +
                ", TK001='" + TK001 + '\'' +
                ", TK002='" + TK002 + '\'' +
                ", QTY=" + QTY +
                '}';
    }
}