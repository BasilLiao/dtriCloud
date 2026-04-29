package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedCoptdDto {

    // --- JPA Projection 用的唯讀介面方法 ---

    // TD001_TD002 (訂單單號)
    String getTD001_TD002();

    // TD008_TD009 (未交數量 = 訂單量 - 已交量)
    BigDecimal getTD008_TD009();

    // TD013 (預計交貨日)
    String getTD013();

    // --- INVMB (物料資訊) ---
    String getMB001(); // 品號

    String getMB002(); // 品名

    String getMB003(); // 規格

    String getMB017(); // 倉別代號

    String getMB032(); // 供應商代號

    Integer getMB036(); // 固定前置天數

    BigDecimal getMB039(); // 最低補量

    BigDecimal getMB040(); // 補貨倍量

    // --- 其他 ---
    String getMC002(); // 倉別名稱

    String getMA002(); // 供應商名稱 (客訂單這裡抓的是物料的預設供應商)

    // TK000 (單別名稱 '客訂單')
    String getTK000();


    // =========================================================
    // 靜態內部類別
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // 用法：ValidatedCoptdDto.Pojo myObj = new ValidatedCoptdDto.Pojo(dto);
    // =========================================================
    public static class Pojo implements ValidatedCoptdDto {

        private String tD001_TD002;
        private BigDecimal tD008_TD009;
        private String tD013;
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
        public Pojo(ValidatedCoptdDto origin) {
            this.tD001_TD002 = origin.getTD001_TD002();
            this.tD008_TD009 = origin.getTD008_TD009();
            this.tD013 = origin.getTD013();
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
        public String getTD001_TD002() { return tD001_TD002; }

        @Override
        public BigDecimal getTD008_TD009() { return tD008_TD009; }

        @Override
        public String getTD013() { return tD013; }

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


        // --- Setters (讓它變可寫) ---
        public void setTD001_TD002(String tD001_TD002) { this.tD001_TD002 = tD001_TD002; }

        public void setTD008_TD009(BigDecimal tD008_TD009) { this.tD008_TD009 = tD008_TD009; }

        public void setTD013(String tD013) { this.tD013 = tD013; }

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