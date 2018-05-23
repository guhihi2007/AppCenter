package cn.lt.android.statistics;

public class ReportEvent {

    /**
     * 统计时间的ActionType:
     * 非下载点击事件；
     */
    public static final String ACTION_CLICK = "click";
    /**
     * 页面浏览；
     */
    public static final String ACTION_PAGEVIEW = "page_view";

    /***
     * 广告展示
     */
    public static final String ACTION_ADVIEW = "ads_view";

    /***
     * 广告展示
     */
    public static final String ACTION_ADS_PAGEVIEW = "ads_page_view";

    /***
     * 广告点击
     */
    public static final String ACTION_ADCLICK = "ads_click";
    /***
     * 应用启动
     */
    public static final String ACTION_APP_START = "app_start";
    /***
     * 应用退出
     */
    public static final String ACTION_APP_END = "app_quit";
    /***
     * 用户删除操作
     */
    public static final String ACTION_DELETE = "delete";

    /***
     * 唤醒
     */
    public static final String ACTION_AWAKE = "wake";
    /********************************************应用下载相关*****************************************************/
    /**
     * 统计时间的ActionType:
     * 主动下载请求；
     */
    public static final String ACTION_DOWNLOAD = "download";

    /***
     * 广告下载的ActionType
     */
    public static final String ACTION_AD_DOWNLOAD = "ads_download";

    /**
     * 统计时间的ActionType:
     * 主动下载成功，安装；
     */
    public static final String ACTION_INSTALL = "install";

    /***
     * 广告安装
     */
    public static final String ACTION_AD_INSTALL = "ads_install";

    /**
     * 统计时间的ActionType:
     * 主动升级下载成功；
     */
    public static final String ACTION_UPDATEINSTALL = "updateInstall";

    /**
     * 统计时间的ActionType:
     * 平台下载成功；
     */
    public static final String ACTION_PLATUPGRADE = "client";

    /******************************************
     * 推送相关
     *******************************************/

    public static final String ACTION_PUSH = "push";

    /***
     * 通知连
     */
    public static final String ACTION_NOTIFICATION = "a";

    /**
     * 统计时间的ActionType:
     * 搜索点击；
     */
    public static final String ACTION_SEARCH = "search";

    /***
     * 统计搜索推荐点击事件
     */
    public static final String ACTION_SEARCH_RECOMMEND = "search_recommend_click";

    /***
     * 广告api展示上报
     */
    public static final String ACTION_AD_SHOW = "action_ad_show";
    /***
     * 第三方SDK广告统计
     */
    public static final String ACTION_AD_REPORT = "ad_report";
    /***
     * 第三方唤醒拉活事件
     */
    public static final String ACTION_ACTIVE = "active";

    /****************************唤醒拉活event***********************************/
    public static final String event_received = "received";//收到任务
    public static final String event_show = "show";
    public static final String event_clicked = "clicked";
    public static final String event_open = "open";//打开应用
    public static final String event_awake = "awake";//唤醒服务

}
