package dtri.com.tw.shared;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

public class CloudExceptionService extends Exception {

	/**
	 * 自訂義錯誤
	 */
	private static final long serialVersionUID = 1L;

	// 錯誤顏色固定 (黃 紅)
	public enum ErColor {
		warning, danger
	};

	// 故障代號限制
	public enum ErCode {
		E1000, W1000, W1001, W1002, W1003, W1004, W1005
	};

	// 故障語言
	public enum Lan {
		zh_TW, zh_CH, en_US, vi_VN;
	};

	private String errorColor;
	private String errorCode;
	private String errorCodeMessage;

	// 錯誤訊息-比對不到錯誤
	public final static String W0000_en_US = "[W0000] System exception error!!";
	// 錯誤訊息-查無資料
	public final static String W1000_zh_TW = "[W1000] 查無資料!!";
	public final static String W1000_en_US = "[W1000] No data found!!";
	public final static String W1000_vi_VN = "[W1000] Kiểm tra không có thông tin!!";
	// 錯誤訊息-資料已重複
	public final static String W1001_zh_TW = "[W1001] 資料重複: ${0} !!";
	public final static String W1001_en_US = "[W1001] Duplicate data: ${0} !!";
	public final static String W1001_vi_VN = "[W1001] Dữ liệu trùng lặp: ${0} !!";
	// 錯誤訊息-Detail 找不到 Data 關聯
	public final static String W1002_zh_TW = "[W1002] Detail 找不到 Data 關聯!!";
	public final static String W1002_en_US = "[W1002] Detail could not find Data association!!";
	public final static String W1002_vi_VN = "[W1002] Không thể tìm thấy chi tiết Liên kết dữ liệu!!";
	// 錯誤訊息-缺少 Data 資料
	public final static String W1003_zh_TW = "[W1003] 資料缺少 : ${0} !!";
	public final static String W1003_en_US = "[W1003] lack of data : ${0} !!";
	public final static String W1003_vi_VN = "[W1003] thiếu dữ liệu : ${0} !!";
	// 錯誤訊息-缺少 Data 資料
	public final static String W1004_zh_TW = "[W1004] 指令語法錯誤 請檢查 屬性/條件 內容 !!";
	public final static String W1004_en_US = "[W1004] Instruction syntax error Please check attribute/condition content !!";
	public final static String W1004_vi_VN = "[W1004] Lỗi cú pháp lệnh Vui lòng kiểm tra nội dung thuộc tính/điều kiện !!";
	// 錯誤訊息-服務器尚未開啟
	public final static String W1005_zh_TW = "[W1005] 服務器尚未開啟!!";
	public final static String W1005_en_US = "[W1005] The server is not started yet!!";
	public final static String W1005_vi_VN = "[W1005] Máy chủ chưa được khởi động!!";

	// Parameterless Constructor
	public CloudExceptionService() {
	}

	// Constructor that accepts a message
	/**
	 * @param ErrorColor 錯誤顏色
	 * @param errorCode  錯誤碼
	 * @param message    訊息(如果:有則帶入 沒有澤預設)
	 * @param Language   國家語言?
	 */
	public CloudExceptionService(PackageBean packageBean, ErColor errorColor, ErCode errorCode, Lan language, String[] message) {
		super("[" + errorColor + " " + errorCode + "]:" + language + " " + message);
		this.setErrorColor(errorColor + "");
		this.setErrorCode(errorCode + "");
		String languageDef = language + "";
		switch (packageBean.getUserLanguaue()) {
		case "zh_TW":
			languageDef = "zh_TW";
			break;
		case "en_US":
			languageDef = "en_US";
			break;
		case "vi_VN":
			languageDef = "vi_VN";
			break;
		}

		try {
			// 尋找-比對錯誤代碼
			Field errorMessage = this.getClass().getDeclaredField(errorCode + "_" + languageDef);
			this.setErrorCodeMessage((String) errorMessage.get(this.getClass()));
			// 尋找-特殊標記取代
			if (message != null) {
				for (int i = 0; i < message.length; i++) {
					String errorCodeMessage = this.getErrorCodeMessage();
					errorCodeMessage = errorCodeMessage.replace("${" + i + "}", message[i]);
					this.setErrorCodeMessage(errorCodeMessage);
				}
			}
		} catch (Exception e) {
			// 未知-錯誤訊息(比對不到)
			this.setErrorCodeMessage(W0000_en_US);
			e.printStackTrace();
		}
		packageBean.setInfoColor(errorColor + "");
		packageBean.setInfo(this.getErrorCodeMessage());
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorCodeMessage() {
		return errorCodeMessage;
	}

	public void setErrorCodeMessage(String errorCodeMessage) {
		this.errorCodeMessage = errorCodeMessage;
	}

	public String getErrorColor() {
		return errorColor;
	}

	public void setErrorColor(String errorColor) {
		this.errorColor = errorColor;
	}
	
	/** 轉換文字 **/
	public static String eStktToSg(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		return baos.toString();
	}

}
