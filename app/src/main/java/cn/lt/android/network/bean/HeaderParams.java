package cn.lt.android.network.bean;

import android.app.Activity;
import android.content.Context;

import java.util.UUID;

import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.SharePreferenceUtil;

public class HeaderParams {
    public String uuid;
    String imei;
    String version;
    int version_code;
    String os_version;
    String device;//手机品牌
    String metrics;
    String channel;
    Long memory_size;//内存大小
    String install_time;//安装时间
    String project;
    boolean hasSIM; //有无SIM卡
    String brand;//手机品牌

    public HeaderParams() {
        this(LTApplication.instance);
    }

    public HeaderParams(Context context) {
        LTApplication application = (LTApplication) context.getApplicationContext();
        uuid = (String) SharePreferenceUtil.get("UUID", "");
        if (uuid.equals("")) {
            uuid = UUID.randomUUID().toString();
            SharePreferenceUtil.put("UUID", uuid);
        }
        imei = AppUtils.getIMEIOrAndroid(application);
        version = AppUtils.getVersionName(application);
        version_code = AppUtils.getVersionCode(application);
        os_version = AppUtils.getAndroidSDKVersion();
        device = AppUtils.getDeviceName();
        if (context instanceof Activity) {
            metrics = AppUtils.getScreenWidth(context) + "*" + AppUtils.getScreenHeight(context);
        } else {
            metrics = "1080*1920";
        }
        channel = GlobalConfig.CHANNEL;
        memory_size = AppUtils.getAvailablMemorySize();
        install_time = String.valueOf(System.currentTimeMillis());
        project = "AppCenter";
        hasSIM = AppUtils.hasSIMCard(application);
        brand = AppUtils.getBrand();
    }

    public Long getMemory_size() {
        return memory_size;
    }

    public HeaderParams setMemory_size(Long memory_size) {
        this.memory_size = memory_size;
        return this;
    }
}