package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedMoctiDto {

    // --- JPA Projection 用的唯讀介面方法 ---
    String getTI001_TI002();
    String getTI003();
    BigDecimal getTI007();
    String getTI014(); // 對應時間/驗收日，與 PURTH TH014 類似
    String getMB001();
    String getMB002();
    String getMB003();
    String getMB017();
    String getMB032();
    Integer getMB036();
    BigDecimal getMB039();
    BigDecimal getMB040();
    String getMC002();
    String getMA002();
    String getTK000();

    // =========================================================
    // 靜態內部類別 
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // =========================================================
    public static class Pojo implements ValidatedMoctiDto {

        private String tI001_TI002;
        private String tI003;
        private BigDecimal tI007;
        private String tI014;
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

        // 1. 空建構子
        public Pojo() {
        }

        // 2. 轉換建構子
        public Pojo(ValidatedMoctiDto origin) {
            this.tI001_TI002 = origin.getTI001_TI002();
            this.tI003 = origin.getTI003();
            this.tI007 = origin.getTI007();
            this.tI014 = origin.getTI014();
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
        public String getTI001_TI002() { return tI001_TI002; }
        @Override
        public String getTI003() { return tI003; }
        @Override
        public BigDecimal getTI007() { return tI007; }
        @Override
        public String getTI014() { return tI014; }
        @Override
        public String getMB001() { return mB001; }
        @Override
        public String getMB002() { return mB002; }
        @Override
        public String getMB003() { return mB003; }
        @Override
        public String getMB017() { return mB017; }
        @Override
        public String getMB032() { return mB032; }
        @Override
        public Integer getMB036() { return mB036; }
        @Override
        public BigDecimal getMB039() { return mB039; }
        @Override
        public BigDecimal getMB040() { return mB040; }
        @Override
        public String getMC002() { return mC002; }
        @Override
        public String getMA002() { return mA002; }
        @Override
        public String getTK000() { return tK000; }

        // --- Setters ---
        public void setTI001_TI002(String tI001_TI002) { this.tI001_TI002 = tI001_TI002; }
        public void setTI003(String tI003) { this.tI003 = tI003; }
        public void setTI007(BigDecimal tI007) { this.tI007 = tI007; }
        public void setTI014(String tI014) { this.tI014 = tI014; }
        public void setMB001(String mB001) { this.mB001 = mB001; }
        public void setMB002(String mB002) { this.mB002 = mB002; }
        public void setMB003(String mB003) { this.mB003 = mB003; }
        public void setMB017(String mB017) { this.mB017 = mB017; }
        public void setMB032(String mB032) { this.mB032 = mB032; }
        public void setMB036(Integer mB036) { this.mB036 = mB036; }
        public void setMB039(BigDecimal mB039) { this.mB039 = mB039; }
        public void setMB040(BigDecimal mB040) { this.mB040 = mB040; }
        public void setMC002(String mC002) { this.mC002 = mC002; }
        public void setMA002(String mA002) { this.mA002 = mA002; }
        public void setTK000(String tK000) { this.tK000 = tK000; }
    }
}
