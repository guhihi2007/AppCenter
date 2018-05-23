package cn.lt.android.ads.wanka;

import cn.lt.android.util.LogUtils;

/**
 * Created by chon on 2017/1/6.
 * What? How? Why?
 */

public class WanKaLog {
//    private static boolean debug = BuildConfig.enableLog;
    private static final String TAG = "wanka";

    public static void e (String tag,String message) {
//        if (debug) {
//            Log.e(tag,message);
//        }
            LogUtils.e(tag,message);
    }

    public static void e (String message) {
//        if (debug) {
//            Log.e(TAG,message);
//        }
        LogUtils.e(TAG,message);
    }

    public static void w (String tag,String message) {
//        if (debug) {
//            Log.w(tag,message);
//        }
        LogUtils.e(tag,message);
    }

    public static void w (String message) {
//        if (debug) {
//            Log.w(TAG,message);
//        }
        LogUtils.w(TAG,message);
    }
}
