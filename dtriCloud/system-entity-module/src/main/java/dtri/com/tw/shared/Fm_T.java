package dtri.com.tw.shared;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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

	/** get Date to week **/
	public static int getWeek(Date date) {
		if (date == null) {
			return 0;
		}

		Calendar cal = Calendar.getInstance(Locale.TAIWAN);
		cal.setFirstDayOfWeek(Calendar.MONDAY); // 設定每週從星期一開始
		cal.setMinimalDaysInFirstWeek(4); // 至少 4 天才算完整第一週
		cal.setTime(date);

		int year = cal.get(Calendar.YEAR);
		int week = cal.get(Calendar.WEEK_OF_YEAR);

		// 修正跨年問題（避免 12 月最後幾天變成下一年的第 1 週）
		if (week == 1 && cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
			week = 53; // 設定為上一年的最後一週
		}

		// 修正 1 月第一週仍屬於上一年的問題（確保從 1 開始）
		if (cal.get(Calendar.MONTH) == Calendar.JANUARY && week >= 52) {
			week = 1; // 設為 1 而不是 0
		}

		return week;
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
