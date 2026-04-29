package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedMoctaDto {

    // --- JPA Projection 用的唯讀介面方法 ---

    // 對應 SQL: AS TA001_TA002 (製令單號)
    String getTA001_TA002();

    // 對應 SQL: TA032 (加工廠商))
    String getTA032();

    // 對應 SQL: TA006 (產品品號)
    String getTA006();

    // 對應 SQL: AS TA009 (預計領料日)
    String getTA009();

    // 對應 SQL: TA010 (預計完工)
    String getTA010();

    // 對應 SQL: TB017 (備註)
    String getTB017();

    // 對應 SQL: AS TB004_TB005 (需領 - 已領)
    BigDecimal getTB004_TB005();

    // --- 下面是 INVMB (物料主檔) ---

    // 對應 SQL: MB001 (品號)
    String getMB001();

    // 對應 SQL: MB002 (品名)
    String getMB002();

    // 對應 SQL: MB003 (規格)
    String getMB003();

    // 對應 SQL: MB017 (倉別代號)
    String getMB017();

    // 對應 SQL: MB032 (供應商代號)
    String getMB032();

    // 對應 SQL: MB036 (固定前置天數)
    Integer getMB036();

    // 對應 SQL: MB039 (最低補量)
    BigDecimal getMB039();

    // 對應 SQL: MB040 (補貨倍量)
    BigDecimal getMB040();

    String getTC004(); // 客戶代號

    String getCOPMA002(); // 客戶名稱

    // --- 其他 ---

    // 對應 SQL: AS MC002 (倉別名稱)
    String getMC002();

    // 對應 SQL: AS MA002 (供應商名稱)
    String getMA002();

    // 對應 SQL: AS TK000 (單據類型 '製令單')
    String getTK000();

    // =========================================================
    // 靜態內部類別
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // 用法：ValidatedMoctaDto.Pojo myObj = new ValidatedMoctaDto.Pojo(dto);
    // =========================================================
    public static class Pojo implements ValidatedMoctaDto {

        private String tA001_TA002;
        private String tA006;
        private String tA009;
        private String tA010;
        private String tB017;
        private BigDecimal tB004_TB005;
        private String mB001;
        private String mB002;
        private String mB003;
        private String mB017;
        private String mB032;
        private Integer mB036;
        private BigDecimal mB039;
        private BigDecimal mB040;
        private String mC002;
        private String mA002;
        private String tK000;
        private String ta032;
        private String tc004;
        private String copma002;

        // 1. 空建構子 (必須保留)
        public Pojo() {
        }

        // 2. 轉換建構子：把「唯讀介面」轉成「可寫物件」
        public Pojo(ValidatedMoctaDto origin) {
            this.ta032 = origin.getTA032();
            this.tA001_TA002 = origin.getTA001_TA002();
            this.tA006 = origin.getTA006();
            this.tA009 = origin.getTA009();
            this.tA010 = origin.getTA010();
            this.tB017 = origin.getTB017();
            this.tB004_TB005 = origin.getTB004_TB005();
            this.mB001 = origin.getMB001();
            this.mB002 = origin.getMB002();
            this.mB003 = origin.getMB003();
            this.mB017 = origin.getMB017();
            this.mB032 = origin.getMB032();
            this.mB036 = origin.getMB036();
            this.mB039 = origin.getMB039();
            this.mB040 = origin.getMB040();
            this.mC002 = origin.getMC002();
            this.mA002 = origin.getMA002();
            this.tK000 = origin.getTK000();
            this.tc004 = origin.getTC004();
            this.copma002 = origin.getCOPMA002();
        }

        // --- 實作介面方法 (Getters) ---

        @Override
        public String getTA032() {
            return ta032;
        }

        @Override
        public String getTA001_TA002() {
            return tA001_TA002;
        }

        @Override
        public String getTA006() {
            return tA006;
        }

        @Override
        public String getTA009() {
            return tA009;
        }

        @Override
        public String getTA010() {
            return tA010;
        }

        @Override
        public String getTB017() {
            return tB017;
        }

        @Override
        public BigDecimal getTB004_TB005() {
            return tB004_TB005;
        }

        @Override
        public String getMB001() {
            return mB001;
        }

        @Override
        public String getMB002() {
            return mB002;
        }

        @Override
        public String getMB003() {
            return mB003;
        }

        @Override
        public String getMB017() {
            return mB017;
        }

        @Override
        public String getMB032() {
            return mB032;
        }

        @Override
        public Integer getMB036() {
            return mB036;
        }

        @Override
        public BigDecimal getMB039() {
            return mB039;
        }

        @Override
        public BigDecimal getMB040() {
            return mB040;
        }

        @Override
        public String getMC002() {
            return mC002;
        }

        @Override
        public String getMA002() {
            return mA002;
        }

        @Override
        public String getTK000() {
            return tK000;
        }

        @Override
        public String getTC004() {
            return tc004;
        }

        @Override
        public String getCOPMA002() {
            return copma002;
        }

        // --- Setters (讓它變可寫) ---
        public void setTA001_TA002(String tA001_TA002) {
            this.tA001_TA002 = tA001_TA002;
        }

        public void setTA006(String tA006) {
            this.tA006 = tA006;
        }

        public void setTA009(String tA009) {
            this.tA009 = tA009;
        }

        public void setTA010(String tA010) {
            this.tA010 = tA010;
        }

        public void setTB017(String tB017) {
            this.tB017 = tB017;
        }

        public void setTB004_TB005(BigDecimal tB004_TB005) {
            this.tB004_TB005 = tB004_TB005;
        }

        public void setMB001(String mB001) {
            this.mB001 = mB001;
        }

        public void setMB002(String mB002) {
            this.mB002 = mB002;
        }

        public void setMB003(String mB003) {
            this.mB003 = mB003;
        }

        public void setMB017(String mB017) {
            this.mB017 = mB017;
        }

        public void setMB032(String mB032) {
            this.mB032 = mB032;
        }

        public void setMB036(Integer mB036) {
            this.mB036 = mB036;
        }

        public void setMB039(BigDecimal mB039) {
            this.mB039 = mB039;
        }

        public void setMB040(BigDecimal mB040) {
            this.mB040 = mB040;
        }

        public void setMC002(String mC002) {
            this.mC002 = mC002;
        }

        public void setMA002(String mA002) {
            this.mA002 = mA002;
        }

        public void setTK000(String tK000) {
            this.tK000 = tK000;
        }

        public void setTC004(String tc004) {
            this.tc004 = tc004;
        }

        public void setCOPMA002(String copma002) {
            this.copma002 = copma002;
        }
    }
}