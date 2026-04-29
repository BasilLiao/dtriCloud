package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedPurtcDto {

    // --- JPA Projection 用的唯讀介面方法 ---

    // CREATE_DATE (建立時間)
    String getCreateDate();

    // TC001_TC002 (採購單號)
    String getTC001_TC002();

    // TD003 (採購單序號)
    String getTD003();

    // TD012 (預交日)
    String getTD012();

    // TD008_TH007 (未交量 = 採購量 - 已交 - 待驗)
    BigDecimal getTD008_TH007();

    // --- INVMB (物料資訊) ---
    String getMB001(); // 品號

    String getMB002(); // 品名

    String getMB003(); // 規格

    String getMB017(); // 倉別代號

    String getMB032(); // 供應商代號 (來自 TC004)

    Integer getMB036(); // 固定前置天數

    BigDecimal getMB039(); // 最低補量

    BigDecimal getMB040(); // 補貨倍量

    // --- 其他 ---
    String getMC002(); // 倉別名稱

    String getMA002(); // 供應商名稱

    // TH007 (待驗中量)
    BigDecimal getTH007();

    // TK000 (單別名稱 '採購單')
    String getTK000();

    // =========================================================
    // 靜態內部類別 
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // 用法：ValidatedPurtcDto.Pojo myObj = new ValidatedPurtcDto.Pojo(dto);
    // =========================================================
    public static class Pojo implements ValidatedPurtcDto {

        private String createDate;
        private String tC001_TC002;
        private String tD003;
        private String tD012;
        private BigDecimal tD008_TH007;
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
        private BigDecimal tH007;
        private String tK000;

        // 1. 空建構子 (必須保留)
        public Pojo() {
        }

        // 2. 轉換建構子：把「唯讀介面」轉成「可寫物件」
        public Pojo(ValidatedPurtcDto origin) {
            this.createDate = origin.getCreateDate();
            this.tC001_TC002 = origin.getTC001_TC002();
            this.tD003 = origin.getTD003();
            this.tD012 = origin.getTD012();
            this.tD008_TH007 = origin.getTD008_TH007();
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
            this.tH007 = origin.getTH007();
            this.tK000 = origin.getTK000();
        }

        // --- 實作介面方法 (Getters) ---
        @Override
        public String getCreateDate() {
            return createDate;
        }

        @Override
        public String getTC001_TC002() {
            return tC001_TC002;
        }

        @Override
        public String getTD003() {
            return tD003;
        }

        @Override
        public String getTD012() {
            return tD012;
        }

        @Override
        public BigDecimal getTD008_TH007() {
            return tD008_TH007;
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
        public BigDecimal getTH007() {
            return tH007;
        }

        @Override
        public String getTK000() {
            return tK000;
        }

        // --- Setters (讓它變可寫) ---
        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public void setTC001_TC002(String tC001_TC002) {
            this.tC001_TC002 = tC001_TC002;
        }

        public void setTD003(String tD003) {
            this.tD003 = tD003;
        }

        public void setTD012(String tD012) {
            this.tD012 = tD012;
        }

        public void setTD008_TH007(BigDecimal tD008_TH007) {
            this.tD008_TH007 = tD008_TH007;
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

        public void setTH007(BigDecimal tH007) {
            this.tH007 = tH007;
        }

        public void setTK000(String tK000) {
            this.tK000 = tK000;
        }
    }
}