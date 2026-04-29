package dtri.com.tw.pgsql.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MaterialShortageDto {

    private String mb001; // 料號 (重要：WASM 分組依據)
    private String mb002; // 品名
    private String mb003; // 規格
    private String mb017; // 倉別代號
    private String mc002; // 倉別名稱
    private String mb032; // 供應商代號 (代號基本資料檔)
    private String ma002; // 供應商名稱
    private Integer mb036; // L/T 固定供貨天數
    private Integer mb039; // MOQ 最低補量
    private Integer mb040; // MPQ 最小包裝
    private String tk000; // 單別
    private String tk001; // 單號
    private String tk002; // 預交日 (重要：時間軸排序)
    private String tk003; // 產品品號
    private String ta032; // 加工廠商
    private String tc004; // 客戶代號
    private String copma002; // 客戶名稱
    private Integer invmbmc007; // 目前庫存 (INVMB_MC007)
    private Integer syssy001; // 未領量 (累計)
    private Integer syssy002; // 未交量 (累計)
    private Integer syssy003; // 未領量(當日)
    private Integer syssy004; // 未交量(當日)
    private Integer syssy005; // 待驗量(目前)
    private Integer syssy006; // 庫存餘量(當日)
    private Integer syssy007; // 可用餘量(當日)
    // 考慮到 N對N 替代料，視情況加入替代料標記
    private String syssy008; // 配給(最後預交日)
    private String syssy009; // 配給(預交日*未交量)[新單]:單號
    private String syssy011; // 推薦請購量
    private Integer mc004; // 平均(6)個月均用量

    private Boolean hasreplacement = false; // 是否有替代規則 (控制前端按鈕顯示)
    @JsonIgnore
    private Integer simval; // WASM 計算後的模擬餘額
    @JsonIgnore
    private String simadvice; // WASM 產出的文字建議

    public MaterialShortageDto(String tc004, String copma002) {
        this.tc004 = tc004;
        this.copma002 = copma002;
    }

    public MaterialShortageDto(String tk003) {
        this.tk003 = tk003;
    }

    /**
     * 專用於產品列表 (含名稱)
     * 
     * @param isProduct 僅用於區分建構子簽章
     */
    public MaterialShortageDto(String tk003, String mb002, Boolean isProduct) {
        this.tk003 = tk003;
        this.mb002 = mb002;
    }

    public MaterialShortageDto(
            String mb001, String mb002, String mb003, String mb017, String mc002,
            String mb032, String ma002, Object mb036, Object mb039, Object mb040,
            String tk000, String tk001, Object tk002, String tk003, // tk002 改用 Object
            String ta032, String tc004, String copma002, Object invmbmc007, Object syssy001, Object syssy002,
            Object syssy003,
            Object syssy004, Object syssy005, Object syssy006, Object syssy007, String syssy008,
            String syssy009, String syssy011, Object mc004) {

        this.mb001 = mb001;
        this.mb002 = mb002;
        this.mb003 = mb003;
        this.mb017 = mb017;
        this.mc002 = mc002;
        this.mb032 = mb032;
        this.ma002 = ma002;
        this.mb036 = toInt(mb036);
        this.mb039 = toInt(mb039);
        this.mb040 = toInt(mb040);
        this.tk000 = tk000;
        this.tk001 = tk001;
        this.tk002 = tk002 == null ? "" : tk002.toString().trim(); // 安全處理日期轉字串
        this.tk003 = tk003;
        this.ta032 = ta032;
        this.tc004 = tc004;
        this.copma002 = copma002;
        this.invmbmc007 = toInt(invmbmc007);
        this.syssy001 = toInt(syssy001);
        this.syssy002 = toInt(syssy002);
        this.syssy003 = toInt(syssy003);
        this.syssy004 = toInt(syssy004);
        this.syssy005 = toInt(syssy005);
        this.syssy006 = toInt(syssy006);
        this.syssy007 = toInt(syssy007);
        this.syssy008 = syssy008;
        this.syssy009 = syssy009;
        this.syssy011 = syssy011;
        this.mc004 = toInt(mc004);
    }

    /** 終極防爆轉型 */
    private Integer toInt(Object obj) {
        if (obj == null)
            return 0;
        if (obj instanceof Number)
            return ((Number) obj).intValue();
        try {
            // 處理像 "100.0" 這種帶小數點的字串
            return (int) Math.round(Double.parseDouble(obj.toString().trim()));
        } catch (Exception e) {
            return 0;
        }
    }
}