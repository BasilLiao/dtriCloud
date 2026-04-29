package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface ValidatedInvmbDto {

    // --- JPA Projection 用的唯讀介面方法 ---

    // MB001 (品號)
    String getMB001();

    // MC007 (庫存總數 - 經由 SUM 計算)
    BigDecimal getMC007();

    // (庫存安全數量)
    String getMC004();

    // MB036 (LT - 固定前置天數) - 雖然庫存沒有，但為了 DTO 統一介面，若有需要可補，目前介面保持原樣

    // =========================================================
    // 靜態內部類別
    // 用途：當你需要修改數據、加減運算時，請 new 這個類別
    // 用法：ValidatedInvmbkDto.Pojo myObj = new ValidatedInvmbkDto.Pojo(dto);
    // =========================================================
    public static class Pojo implements ValidatedInvmbDto {

        private String mB001;
        private BigDecimal mC007;
        private String mC004;

        // 🔥 為了配合新的 Service 邏輯，我建議這裡補上這幾個欄位
        // 雖然原始 Interface 沒有，但 POJO 是用來運算的，加上去比較保險
        private Integer mB036; // LT
        private BigDecimal mB039; // MOQ
        private BigDecimal mB040; // MPQ

        // 1. 空建構子 (必須保留)
        public Pojo() {
        }

        // 2. 轉換建構子：把「唯讀介面」轉成「可寫物件」
        public Pojo(ValidatedInvmbDto origin) {
            this.mB001 = origin.getMB001();
            this.mC007 = origin.getMC007();
            this.mC004 = origin.getMC004();
        }

        // --- 實作介面方法 (Getters) ---
        @Override
        public String getMB001() {
            return mB001;
        }

        @Override
        public BigDecimal getMC007() {
            return mC007;
        }

        @Override
        public String getMC004() {
            return mC004;
        }

        // --- 額外 Getters (For Logic) ---
        public Integer getMB036() {
            return mB036;
        }

        public BigDecimal getMB039() {
            return mB039;
        }

        public BigDecimal getMB040() {
            return mB040;
        }

        // --- Setters (讓它變可寫) ---
        public void setMB001(String mB001) {
            this.mB001 = mB001;
        }

        public void setMC007(BigDecimal mC007) {
            this.mC007 = mC007;
        }

        public void setMC004(String mC004) {
            this.mC004 = mC004;
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
    }
}