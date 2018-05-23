package cn.lt.android.util;

/**
 * Created by wenchao on 2015/11/19.
 */
public class ViewUtils {

    private static long lastClickTime;

    /**
     * 判定是否快速点击
     * @return
     */
    public static boolean isFastClick(){
        long time = System.currentTimeMillis();
        if(Math.abs(time - lastClickTime) < 500){
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
