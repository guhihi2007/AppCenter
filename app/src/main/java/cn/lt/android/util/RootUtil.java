package cn.lt.android.util;

import java.io.File;

/**
 * Created by LinJunSheng on 2016/8/29.
 */

public class RootUtil {

    /**
     * 判断手机是否已经root（不会触发root授权）
     */
    public static boolean isRootDevice() {
        boolean root = false;

        try {
            if ((!new File("/system/bin/su").exists())
                    && (!new File("/system/xbin/su").exists())) {
                root = false;
            } else {
                root = true;
            }

        } catch (Exception e) {
        }

        return root;
    }

}
