package cn.lt.android.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import cn.lt.android.LTApplication;

/**
 * Created by LinJunSheng on 2016/4/21.
 */
public class MetaDataUtil {
    public static String getMetaData(String key) {
        String value = "";
        try {
            PackageManager pm = LTApplication.instance.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(LTApplication.instance.getPackageName(), PackageManager
                    .GET_META_DATA);
            value = ai.metaData.getString(key);
            if (TextUtils.isEmpty(value)) {
                int i = ai.metaData.getInt(key,-1);
                value = String.valueOf(i);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
