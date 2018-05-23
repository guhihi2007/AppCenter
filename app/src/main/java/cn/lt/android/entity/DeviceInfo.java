package cn.lt.android.entity;

import android.app.Activity;

import java.io.Serializable;

import cn.lt.android.GlobalConfig;
import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.framework.util.DeviceUtils;
import cn.lt.framework.util.ScreenUtils;

/**
 * Created by wenchao on 2016/3/14.
 * 设备信息，用于h5页面上传参数
 */
public class DeviceInfo implements Serializable {
    public String uuid;
    String imei;
    String version;
    int    version_code;
    String os_version;
    String device;
    String metrics;
    String channel;
    String access_token;

    public DeviceInfo(Activity context) {
        uuid = (String) SharePreferenceUtil.get("UUID", "");
        imei = DeviceUtils.getIMEI(context);
        version = DeviceUtils.getAppVersionName(context);
        version_code = DeviceUtils.getAppVersionCode(context);
        os_version =DeviceUtils.getAndroidSDKVersion();
        device = DeviceUtils.getDeviceName();
        int[] widthAndHeight = ScreenUtils.getScreenWidthAndHeight(context);
        metrics = widthAndHeight[0]+"*"+widthAndHeight[1];
        channel = GlobalConfig.CHANNEL;

        UserBaseInfo userBaseInfo = UserInfoManager.instance().getUserInfo();
        if (userBaseInfo != null) {
            access_token = userBaseInfo.getToken();
        } else {
            access_token = "";
        }

    }

}
