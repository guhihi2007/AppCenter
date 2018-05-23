package cn.lt.android.main.requisite.manger;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class SharedPreference {

    public static String PRE_FILE_NAME = "requisite";
    public static final String LAST_SHOW_TIME = "lastShowTime";
    public static final String HAS_DATA = "hasData";
    public static final String CACHED = "cached";
    public static final String TEMPL_ID = "templ_id";

    public static long getLastShowTime(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getLong(LAST_SHOW_TIME, 0);
    }

    public static void saveShowTime(Context context, long time) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(LAST_SHOW_TIME, time);
        editor.commit();
    }

    public static boolean hasData(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getBoolean(HAS_DATA, true);
    }

    public static void saveHasData(Context context, boolean hasData) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(HAS_DATA, hasData);
        editor.commit();
    }

    public static boolean isCached(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getBoolean(CACHED, false);
    }

    public static void saveCached(Context context, boolean cached) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(CACHED, cached);
        editor.commit();
    }


    public static int getTmeplId(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getInt(TEMPL_ID, 0);
    }

    public static void saveTmeplId(Context context, int id) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(TEMPL_ID, id);
        editor.commit();
    }
}
