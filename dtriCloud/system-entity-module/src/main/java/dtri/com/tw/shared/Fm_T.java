package dtri.com.tw.shared;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Fm_T {

	private static SimpleDateFormat format_yyyyMMdd_HHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat format_yyyyMMdd_HHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static SimpleDateFormat format_yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat format_yyyyMMdd_nos = new SimpleDateFormat("yyyyMMdd");

	/** 格式:yyyy-MM-dd HH:mm **/
	public static String to_yMd_Hm(Date date) {
		return format_yyyyMMdd_HHmm.format(date);
	}

	/** 格式:yyyy-MM-dd HH:mm:ss **/
	public static String to_yMd_Hms(Date date) {
		return format_yyyyMMdd_HHmmss.format(date);
	}

	/** 格式:yyyy-MM-dd **/
	public static String to_y_M_d(Date date) {
		return format_yyyyMMdd.format(date);
	}

	/** 格式:yyyyMMdd **/
	public static String to_yMd(Date date) {
		return format_yyyyMMdd_nos.format(date);
	}

	/** 計算:加/減 天數 **/
	public static Date to_count(Integer n, Date dt) {
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, n);
		dt = c.getTime();
		return dt;
	}

	/** 計算:相差多少 小時 **/
	public static long to_hiff(Date dt1, Date dt2) {
		long diff = dt1.getTime() - dt2.getTime();
		TimeUnit time = TimeUnit.HOURS;
		long diffrence = time.convert(diff, TimeUnit.MILLISECONDS);
		return diffrence;
	}

	/** 計算:相差多少 日 **/
	public static long to_diff(Date dt1, Date dt2) {
		long diff = dt1.getTime() - dt2.getTime();
		TimeUnit time = TimeUnit.DAYS;
		long diffrence = time.convert(diff, TimeUnit.MILLISECONDS);
		return diffrence;
	}

	/** yyyy-MM-dd to Date **/
	public static Date toDate(String dt) {

		try {
			return format_yyyyMMdd.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;

	}

	/** yyyyMMdd to Date **/
	public static Date toYMDate(String dt) {

		try {
			return format_yyyyMMdd_nos.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** yyyy-MM-dd HH:mm:ss to Date **/
	public static Date toDateTime(String dt) {

		try {
			return format_yyyyMMdd_HHmmss.parse(dt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 取得 "週年份" (Week-Based Year) 用途：解決跨年週數問題 (例如 2025/12/29 應該屬於 2026 年)
	 */
	public static int getWeekBasedYear(Date date) {
		if (date == null)
			return 0;
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		// 關鍵：使用 ISO 週日曆的年份，而非普通日曆年
		return localDate.get(IsoFields.WEEK_BASED_YEAR);
	}

	/**
	 * 取得 ISO 週數
	 */
	public static int getWeek(Date date) {
		if (date == null)
			return 0;
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
	}

	/**
	 * [推薦] 直接取得 YYWW 格式字串 例如：2025/12/29 -> 回傳 "2601"
	 */
	public static String getYYWW(Date date) {
		if (date == null)
			return "";

		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		// 取得 週年份 (例如 2026)
		int year = localDate.get(IsoFields.WEEK_BASED_YEAR);
		// 取得 週數 (例如 1)
		int week = localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);

		// 格式化：取年份後兩位 + 兩位週數
		return String.format("%02d%02d", year % 100, week);
	}

	/** get Date to Year **/
	public static int getYear(Date date) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int year = cal.get(Calendar.YEAR);
			return year;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
