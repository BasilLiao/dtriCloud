package dtri.com.tw.shared;

public class Fm_Char {
	/**
	 * 清洗產品規格字串 1. 移除換行、Tab 2. 轉換 HTML entity（amp; → &） 3. 壓縮多餘空白 4. Trim 前後空白
	 */
	public static String sanitizeText(String input) {
		if (input == null) {
			return "";
		}
		return input
				// 移除換行與 tab
				.replaceAll("[\\r\\n\\t]", " ")
				// 修正常見 HTML entity 殘留
				.replaceAll("&amp;", "&").replaceAll("&nbsp;", " ")
				// 壓縮連續空白
				.replaceAll("\\s{2,}", " ")
				// 去除前後空白
				.trim();
	}
}
