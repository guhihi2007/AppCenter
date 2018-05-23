package cn.lt.android.util;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import cn.lt.android.LTApplication;

public class ToastUtils {
    private static Toast mToast;
    private static Handler mhandler = new Handler(Looper.getMainLooper());
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    public static void showToast(String text) {
        mhandler.removeCallbacks(r);
        if (null != mToast) {
            mToast.setText(text);
        } else {
            mToast = Toast.makeText(LTApplication.instance, text, Toast.LENGTH_SHORT);
        }
        mhandler.postDelayed(r, 3000);
        mToast.show();
    }
}
