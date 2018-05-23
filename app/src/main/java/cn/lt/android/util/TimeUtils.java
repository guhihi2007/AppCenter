package cn.lt.android.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cn.lt.android.LogTAG;
import cn.lt.android.db.AppEntity;

public class TimeUtils {

	public static final long minute = 60 * 1000;
	public static final long hour = 60 * minute;
	public static final long day = 24 * hour;

	/**
	 * 将yyyy-mm-dd格式的字符串转换成Date
	 * 
	 * @param dstr
	 * @return
	 */
	public static Date getStringToDate(String dstr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(dstr);
		} catch (ParseException e) {
		}
		return date;
	}

	/**
	 * 针对downloadbar 1秒内只相应点击事件一次
	 * @return
	 * @param entity
	 */
	public static boolean downloadBarisFastClick(AppEntity entity){
		if(entity==null)return false;
		long time = System.currentTimeMillis();
		if(time - entity.lastClickTime < 5000){
			return true;
		}
		entity.lastClickTime = time;
		return false;
	}
	
	public static Date getDateToStringHaveHour(String dstr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = sdf.parse(dstr);
		} catch (ParseException e) {
		}
		return date;
	}

	/***
	 * 游戏详情时间用
	 * @param dstr
	 * @return
     */
	public static String getDateToString(String dstr) {
		if(dstr.contains("-")) {
			return dstr.replaceAll("-", "/");
		}

		Date date = getDateToStringHaveHour(dstr);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		String time = formatter.format(date);
		return time;
	}

	public static String getStringToDateHaveHour(long time) {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

	public static String getLongtoString(long time) {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd ");
		return formatter.format(date);
	}

	public static String getLongtoYear(long time) {
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
		return formatter.format(date);
	}



	/**
	 * 从yyyy-MM-dd HH:mm:ss格式转为yyyy-MM-dd
	 * 
	 * @param dstr
	 * @return
	 */
	public static String getStringToString(String dstr) {
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		// Date date = null;
		// String re = null;
		// try {
		// date = sdf.parse(dstr);
		// re = formatter.format(date);
		// } catch (ParseException e) {
		// }
		return dstr.substring(0, dstr.indexOf(" "));
	}

	public static String formatIntToTimeStr(int time) {
		StringBuilder builder = new StringBuilder();
		int hour = time / 3600;
		int minute = (time - hour * 3600) / 60;
		int second = (time - hour * 3600 - minute * 60);
		if (hour == 0) {
			builder.append("  剩").append(minute).append("分")
					.append(second).append("秒");
		} else {
			builder.append(" 剩").append(hour).append("时").append(minute)
					.append("分");
		}

		return builder.toString();
	}

	/**
	 * 获取2个时间的间隔时间,
	 * @param fromTime
	 * @param toTime 格式 yyyy-MM-dd HH:mm:ss
	 * @return 毫秒数
	 */
	public static long getIntervalTime(String fromTime, String toTime){
		Date fromDate = getDateToStringHaveHour(fromTime);
		Date toDate = getDateToStringHaveHour(toTime);
		long ms = Math.abs(fromDate.getTime()-toDate.getTime());
		return ms;
	}

	/**
	 * 距离当前时间的时间差
	 * @param fromTime
	 * @return 毫秒数
	 */
	public static long getInterval(String fromTime){
		return Math.abs(System.currentTimeMillis() - getDateToStringHaveHour(fromTime).getTime());

	}


	public static String curtimeDifference(String str) {
		if(TextUtils.isEmpty(str)){
			return "";
		}
		long time = getDateToStringHaveHour(str).getTime();
		long tempTime = System.currentTimeMillis() - time;
		int re = (int) (tempTime / day);
		if (re > 0) {
			return getStringToDateHaveHour(time);
		}
		re = (int) (tempTime / hour);
		if (re > 0) {
			return re + "小时前";
		}
		re = (int) (tempTime / minute);
		if (re > 0) {
			return re + "分钟前";
		}
		return "刚刚";
	}

	/***
	 *
	 * @param str
	 * @return
	 */
	public static String curtimeDifference2(String str) {
		if(TextUtils.isEmpty(str)){
			return "";
		}
		long time = getDateToStringHaveHour(str).getTime();
		long tempTime = System.currentTimeMillis() - time;
		int re = (int) (tempTime / day);
		if (re > 0) {
			return getLongtoString(time);
		}
		re = (int) (tempTime / hour);
		if (re > 0) {
			return re + "小时前";
		}
		re = (int) (tempTime / minute);
		if (re > 0) {
			return re + "分钟前";
		}
		return "刚刚";
	}
	
	public static String formatTime(Date date){
		StringBuffer sb =  new StringBuffer() ;
		SimpleDateFormat myFmt=new SimpleDateFormat("hh : mm");
		GregorianCalendar gc = new GregorianCalendar();
		int m = gc.get(GregorianCalendar.AM_PM);
		if (m==0) {
			sb.append("上午"+myFmt.format(date));
		}else {
			sb.append("下午" + myFmt.format(date));
		}
		return sb.toString();
		
	}

	/**事件字符串转换成事件字符串*/
	public static String StringToString(String dataStr){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date data = null;
		String dateString="";
		try {
			data = sdf.parse(dataStr);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			dateString = formatter.format(data);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateString;
	}
	/**
	 * 通过字符串格式的时间获取毫秒；
	 *
	 * @param date
	 *            字符串时间
	 * @param format
	 *            时间格式
	 * @return 毫秒
	 * @throws ParseException
	 */
	public static long string2Long(String date, SimpleDateFormat format)
			throws ParseException {
		return format.parse(date).getTime();
	}

	/** 判断是否同一天*/
	public static boolean isSameDate(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);

		boolean isSameYear = cal1.get(Calendar.YEAR) == cal2
				.get(Calendar.YEAR);
		boolean isSameMonth = isSameYear
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
		boolean isSameDate = isSameMonth
				&& cal1.get(Calendar.DAY_OF_MONTH) == cal2
				.get(Calendar.DAY_OF_MONTH);

		return isSameDate;
	}

	/** 判断是否超过指定时间*/
	public static boolean isExceedJianGe(long recordTime, long jianGeTime) {
		boolean flag = false;

		long curTime = System.currentTimeMillis();

		if((curTime - recordTime) > (jianGeTime * 1000)) {
			flag = true;
		}

		return flag;
	}

	/** 判断是否超过一周*/
	public static boolean isExceedWeek(long recordTime) {
		boolean flag = false;

		long curTime = System.currentTimeMillis();
		LogUtils.i(LogTAG.appAutoUpgradeDialog, "curTime - recordTime = " + (curTime - recordTime));
		if((curTime - recordTime) > (1000 * 86400 * 7)) {
			flag = true;
		}

		return flag;
	}

	/** 判断是否超过一天*/
	public static boolean isExceedDay(long recordTime) {
		boolean flag = false;

		long curTime = System.currentTimeMillis();

		if((curTime - recordTime) > (1000 * 3600 * 24)) {
			flag = true;
		}

		return flag;
	}

	/**
	 * 判断是否超过多少小时
	 * @param recordTime 上一次获取时间
	 * @param hourCount 超过多少个小时
     */
	public static boolean isExceedHour(long recordTime, int hourCount) {

		long curTime = System.currentTimeMillis();

		return ((curTime - recordTime) > (1000 * 3600 * hourCount));
	}

}
