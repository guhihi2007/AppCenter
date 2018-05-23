package cn.lt.android.util;

import cn.lt.android.GlobalParams;

/**
 * Created by LinJunSheng on 2016/4/18.
 */
public class UrlUtil {
    public static String getImageUrl(String halfImageUrl) {
        return GlobalParams.getHostBean().getImage_host() + halfImageUrl;
    }
}
