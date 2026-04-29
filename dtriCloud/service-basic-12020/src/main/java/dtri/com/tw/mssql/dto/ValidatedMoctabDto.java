package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedMoctabDto {

    // --- JPA Projection 用的唯讀介面方法 ---

    // 對應 SQL: AS TA001_TA002 (製令單號)
    String getTA001_TA002();

    // 對應 SQL: AS TA009 (預計完工日)
    String getTA009();

    // 對應 SQL: AS TA015_TA017 (預計產量：需生產 - 已入庫)
    BigDecimal getTA015_TA017();

    // --- INVMB (物料主檔) ---

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

    // --- 其他 ---

    // 對應 SQL: AS MC002 (倉別名稱)
    String getMC002();

    // 對應 SQL: AS MA002 (供應商名稱)
    String getMA002();

    // 對應 SQL: AS TK000 ('內製令單')
    String getTK000();

    // =========================================================
    // 靜態內部類別 (純手工 Pojo 版)
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // 用法：ValidatedMoctabDto.Pojo myObj = new ValidatedMoctabDto.Pojo(dto);
    // =========================================================
    public static class Pojo implements ValidatedMoctabDto {

        private String tA001_TA002;
        private String tA009;
        private BigDecimal tA015_TA017;
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

        // 1. 空建構子 (必須保留)
        public Pojo() {
        }

        // 2. 轉換建構子：把「唯讀介面」轉成「可寫物件」
        public Pojo(ValidatedMoctabDto origin) {
            this.tA001_TA002 = origin.getTA001_TA002();
            this.tA009 = origin.getTA009();
            this.tA015_TA017 = origin.getTA015_TA017();
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
        }

        // --- 實作介面方法 (Getters) ---
        @Override
        public String getTA001_TA002() {
            return tA001_TA002;
        }

        @Override
        public String getTA009() {
            return tA009;
        }

        @Override
        public BigDecimal getTA015_TA017() {
            return tA015_TA017;
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

        // --- Setters (讓它變可寫) ---
        public void setTA001_TA002(String tA001_TA002) {
            this.tA001_TA002 = tA001_TA002;
        }

        public void setTA009(String tA009) {
            this.tA009 = tA009;
        }

        public void setTA015_TA017(BigDecimal tA015_TA017) {
            this.tA015_TA017 = tA015_TA017;
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
    }
}