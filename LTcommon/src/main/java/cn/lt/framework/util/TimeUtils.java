package cn.lt.framework.util;

import android.content.Context;
import android.content.res.Resources;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.lt.framework.R;

/**
 * TimeUtils
 *
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-8-24
 */
public class TimeUtils {


    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR   = 60 * MINUTE;
    public static final long DAY    = 24 * HOUR;
    public static final long WEEK   = 7 * DAY;
    public static final long MONTH  = 30 * DAY;
    public static final long YEAR   = 12 * MONTH;


    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_DATE    = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_FORMAT_MIN     = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    private TimeUtils() {
        throw new AssertionError();
    }

    /**
     * long time to string
     *
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getTime(long timeInMillis) {
        return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    /**
     * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @return
     */
    public static String getCurrentTimeInString() {
        return getTime(getCurrentTimeInLong());
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }

    public static String getFriendlyTimeForDetail(String currTime, String createdTime) {
        try {
            long currTimeMillis    = Long.parseLong(currTime) * 1000;
            long createdTimeMillis = Long.parseLong(createdTime) * 1000;
            return friendlyTimeForDetail(currTimeMillis, createdTimeMillis);
        } catch (NumberFormatException e) {
            return "Unknown";
        }

    }

    private static String friendlyTimeForDetail(long currTimeMillis,
                                                long createdTimeMillis) {
        Date     time     = new Date(createdTimeMillis);
        Date     currTime = new Date(currTimeMillis);
        String   ftime    = "";
        Calendar curr     = Calendar.getInstance();
        curr.setTime(currTime);
        curr.clear(Calendar.HOUR_OF_DAY);
        curr.clear(Calendar.MINUTE);
        curr.clear(Calendar.SECOND);

        Calendar create = Calendar.getInstance();
        create.setTime(time);
        create.clear(Calendar.HOUR_OF_DAY);
        create.clear(Calendar.MINUTE);
        create.clear(Calendar.SECOND);

        Calendar c = Calendar.getInstance();
        c.setTime(time);
        int  hour   = c.get(Calendar.HOUR_OF_DAY);
        int  minute = c.get(Calendar.MINUTE);
        long lt     = create.getTimeInMillis() / 86400000;
        long ct     = curr.getTimeInMillis() / 86400000;
        int  DAY    = (int) (ct - lt);
        if (DAY == 0) {

            if (hour >= 0 && hour < 6) {
                return "凌晨 " + appendZeroForTime(hour) + ":"
                        + appendZeroForTime(minute);
            } else if (hour < 9) {
                return "早上 " + appendZeroForTime(hour) + ":"
                        + appendZeroForTime(minute);
            } else if (hour < 12) {
                return "上午 " + appendZeroForTime(hour) + ":"
                        + appendZeroForTime(minute);
            } else if (hour < 18) {
                return "下午 " + appendZeroForTime(hour) + ":"
                        + appendZeroForTime(minute);
            } else if (hour < 24) {
                return "晚上 " + appendZeroForTime(hour) + ":"
                        + appendZeroForTime(minute);
            } else {
                return "Unknown";
            }
        } else if (DAY == 1) {
            ftime = "昨天 " + appendZeroForTime(hour) + ":"
                    + appendZeroForTime(minute);
        } else if (DAY <= 7) {
            ftime = getWeekOfDate(time) + " " + appendZeroForTime(hour) + ":"
                    + appendZeroForTime(minute);
        } else if (DAY > 7) {

            ftime = DATE_FORMAT_MIN.format(time);
        } else {
            ftime = "Unknown";
        }
        return ftime;
    }

    private static String appendZeroForTime(int s) {
        if (s < 10) {
            return "0" + s;
        }
        return s + "";
    }

    /**
     * 获取是周几
     *
     * @param dt
     * @return
     */
    private static String getWeekOfDate(Date dt) {
        String[] weekDAY = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal     = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDAY[w];
    }

    /**
     * 获取剩余时间字符串
     *
     * @param surplusTime
     * @return
     */
    public static String getSurplusTimeString(long surplusTime) {
        StringBuilder sb     = new StringBuilder("(剩");
        boolean       hasDay = false;
        if (surplusTime / DAY > 0) {
            sb.append(surplusTime / DAY + "天");
            surplusTime = surplusTime % DAY;
            hasDay = true;
        }
        if (surplusTime / HOUR > 0 || hasDay) {
            sb.append(surplusTime / HOUR + "时");
            surplusTime = surplusTime % HOUR;
        }
        sb.append(surplusTime / MINUTE + "分");
        surplusTime = surplusTime % MINUTE;

        sb.append(surplusTime / SECOND + "秒)");
        return sb.toString();
    }

    /**
     * Returns a properly formatted fuzzy string representing time ago
     *
     * @param context Context
     * @return Formatted string
     */
    public static String getTimeAgo(Context context, long ms) {
        long beforeSeconds  = (ms / 1000);
        long nowSeconds     = (int) (Calendar.getInstance().getTimeInMillis() / 1000);
        long timeDifference = nowSeconds - beforeSeconds;

        Resources res = context.getResources();

        if (timeDifference < (MINUTE / 1000)) {
            return res.getString(R.string.fuzzydatetime__seconds_ago, timeDifference);
        } else if (timeDifference < (HOUR / 1000)) {
            return res.getString(R.string.fuzzydatetime__minutes_ago, timeDifference / (MINUTE / 1000));
        } else if (timeDifference < (DAY / 1000)) {
            return res.getString(R.string.fuzzydatetime__hours_ago, timeDifference / (HOUR / 1000));
        } else if (timeDifference < (WEEK / 1000)) {
            return res.getString(R.string.fuzzydatetime__days_ago, timeDifference / (DAY / 1000));
        } else if (timeDifference < (MONTH / 1000)) {
            return res.getString(R.string.fuzzydatetime__weeks_ago, timeDifference / (WEEK / 1000));
        } else if (timeDifference < (YEAR / 1000)) {
            return res.getString(R.string.fuzzydatetime__months_ago, timeDifference / (MONTH / 1000));
        } else {
            return res.getString(R.string.fuzzydatetime__years_ago, timeDifference / (YEAR / 1000));
        }

    }
}
