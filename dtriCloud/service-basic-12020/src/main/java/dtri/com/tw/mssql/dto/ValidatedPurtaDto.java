package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedPurtaDto {

    // --- JPA Projection 用的唯讀介面方法 ---

    // TB001_TB002 (請購單號)
    String getTB001_TB002();

    // TB003 (請購序號)
    String getTB003();

    // TB009 (數量)
    BigDecimal getTB009();

    // TB011 (交貨日)
    String getTB011();

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

    String getMA002(); // 供應商名稱

    // TK000 (單別名稱 '請購單')
    String getTK000();


    // =========================================================
    // 靜態內部類別 
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // 用法：ValidatedPurtaDto.Pojo myObj = new ValidatedPurtaDto.Pojo(dto);
    // =========================================================
    public static class Pojo implements ValidatedPurtaDto {

        private String tB001_TB002;
        private String tB003;
        private BigDecimal tB009;
        private String tB011;
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
        public Pojo(ValidatedPurtaDto origin) {
            this.tB001_TB002 = origin.getTB001_TB002();
            this.tB003 = origin.getTB003();
            this.tB009 = origin.getTB009();
            this.tB011 = origin.getTB011();
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
        public String getTB001_TB002() { return tB001_TB002; }

        @Override
        public String getTB003() { return tB003; }

        @Override
        public BigDecimal getTB009() { return tB009; }

        @Override
        public String getTB011() { return tB011; }

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
        public void setTB001_TB002(String tB001_TB002) { this.tB001_TB002 = tB001_TB002; }

        public void setTB003(String tB003) { this.tB003 = tB003; }

        public void setTB009(BigDecimal tB009) { this.tB009 = tB009; }

        public void setTB011(String tB011) { this.tB011 = tB011; }

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