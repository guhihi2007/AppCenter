package cn.lt.android.ads.wanka;

import cn.lt.appstore.BuildConfig;

/**
 * Created by chon on 2016/12/27.
 * What? How? Why?
 */

public class WanKaUrl {
    //玩咖测试服
    public static String HOST = "http://package.mhacn.net";
    public static String WANKA_CHANNEL_ID = "99999a";
    public static String WANKA_APP_ID = "b99999";
    public static String WANKA_APP_SECRET = "testappsecret";

    //玩咖正式服
    static {
        if (!BuildConfig.IS_DEBUGABLE) {
            HOST = "http://package.mhacn.com";
            WANKA_CHANNEL_ID = "20007a";
            WANKA_APP_ID = "b1007a";
            WANKA_APP_SECRET = "1cHMtDNC8cuPyk0aI1kEFeSlceWyv5ia";
        }
    }

    // 曝光上报
    static final String EXPOSURE = HOST + "/api/v3/report/exposure?";

    // 详情点击上报
//    public static final String DETAIL_CLICK = HOST + "/api/v3/report/detailclick?";

    // 点击下载按钮上报
    public static final String DOWNLOAD_START = HOST + "/api/v3/report/download/start?";

    // 下载完成上报
    public static final String DOWNLOAD_SUCCESS = HOST + "/api/v3/report/download/success?";

    // 安装上报
    public static final String INSTALL = HOST + "/api/v3/report/install?";

    // 应用激活
//    public static final String ACTIVE = HOST + "/api/v3/report/active?";


    // 自己服务器 下载完成安装完成上报时 请求是否玩咖商务包
    public static final String WHETHER_WANKA = "/wanka/applist";

}
