package cn.lt.android.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by atian on 2016/2/24.
 */
public class ScreenSizeUtils {
    public static int[] getScreenSize(Context context) {
        int[] size = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        size[0] = dm.widthPixels;
        size[1] = dm.heightPixels;
        return size;
    }
}
