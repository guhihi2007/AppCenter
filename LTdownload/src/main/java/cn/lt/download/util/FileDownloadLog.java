
package cn.lt.download.util;

import android.util.Log;
public class FileDownloadLog {

    public static boolean NEED_LOG = true;

    private final static String TAG = "LTDownloader.";

    public static void e(Object o, Throwable e, String msg, Object... args) {
        log(Log.ERROR, o, e, msg, args);
    }

    public static void e(Object o, String msg, Object... args) {
        log(Log.ERROR, o, msg, args);
    }

    public static void i(Object o, String msg, Object... args) {
        log(Log.INFO, o, msg, args);
    }

    public static void d(Object o, String msg, Object... args) {
        log(Log.DEBUG, o, msg, args);
    }

    public static void w(Object o, String msg, Object... args) {
        log(Log.WARN, o, msg, args);
    }

    public static void v(Object o, String msg, Object... args) {
        log(Log.VERBOSE, o, msg, args);
    }

    private static void log(int priority, Object o, String message, Object... args) {
        log(priority, o, null, message, args);
    }

    private static void log(int priority, Object o, Throwable throwable, String message, Object... args) {
        if (!NEED_LOG) {
            return;
        }

        try{
            Log.println(priority, getTag(o), String.format(message, args));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    private static String getTag(final Object o) {
        return TAG + ((o instanceof Class) ? ((Class) o).getSimpleName() : o.getClass().getSimpleName());
    }
}
