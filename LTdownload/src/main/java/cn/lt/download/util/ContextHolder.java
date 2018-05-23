package cn.lt.download.util;

import android.app.Application;
import android.content.Context;

public class ContextHolder {

    private static Context APP_CONTEXT;

    public static void initAppContext(final Context context) {
        APP_CONTEXT = context;
    }

    public static Context getAppContext() {
        return APP_CONTEXT;
    }
}

