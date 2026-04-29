package dtri.com.tw.shared;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ExcelTool {
    private final Map<String, Object> column = new HashMap<>();
    private final Map<String, String> languages = new HashMap<>();

    // 私有建構子，強制使用靜態方法開始
    private ExcelTool(String cellName, String zhName) {
        column.put("cellName", cellName);
        column.put("show", 1); // 預設顯示
        column.put("width", 120); // 預設寬度
        languages.put("zh_TW", zhName);
        languages.put("en_US", cellName); // 預設英文用變數名
    }

    /** 開始建立一個欄位 */
    public static ExcelTool def(String cellName, String zhName) {
        return new ExcelTool(cellName, zhName);
    }

    /** 設定寬度 */
    public ExcelTool width(int width) {
        column.put("width", width);
        return this;
    }

    /** 設定是否顯示 (0/1) */
    public ExcelTool show(int show) {
        column.put("show", show);
        return this;
    }

    /** 設定滑鼠移入時的工具提示 (Tooltip) */
    public ExcelTool note(String note) {
        column.put("headerNote", note);
        return this;
    } 

    /** 設定英文名稱 (可選) */
    public ExcelTool en(String enName) {
        languages.put("en_US", enName);
        return this;
    }

    /** 最終輸出為 Map 結構 */
    public Map<String, Object> build() {
        column.put("cellLanguage", new Gson().toJson(languages));
        return column;
    }
}
