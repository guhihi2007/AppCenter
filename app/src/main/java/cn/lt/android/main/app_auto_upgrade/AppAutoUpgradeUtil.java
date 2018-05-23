package cn.lt.android.main.app_auto_upgrade;

import com.google.gson.Gson;

import java.util.List;

import cn.lt.android.entity.AppDetailBean;

/**
 * Created by LinJunSheng on 2016/8/26.
 */

public class AppAutoUpgradeUtil {

    public static String toJsonData(List<AppDetailBean> list) {
        return new Gson().toJson(list);
    }
}
