package cn.lt.android.notification;

/**
 * Created by LinJunSheng on 2016/3/14.
 */
public class NoticeConsts {
    /**
     * 多个应用下载
     */
    public static final int MULTI_DOWNLOADING_NOTIFY_ID = 99999901;

    /**
     * 多个应用下载失败
     */
    public static final int MUTLI_DOWLOAD_FAIL_NOTIFY_ID = 99999902;

    /**
     * 多个应用可升级
     */
    public static final int MUTLI_UPGRADE_NOTIFY_ID = 99999903;

    /**
     * 平台可升级
     */
    public static final int PLATFORM_UPGRAGE_NOTIFY_ID = 99999905;

    /**
     * 平台升级失败
     */
    public static final int PLATFORM_UPGRAGE_FAIL_NOTIFY_ID = 99999906;

    /**
     * 平台升级进度
     */
    public static final int PLATFORM_UPGRAGE_PROGRESS_NOTIFY_ID = 99999907;

    /**
     * 平台升级完成
     */
    public static final int PLATFORM_UPGRAGE_COMPLETE_NOTIFY_ID = 99999908;

    /**
     * 应用下载暂停
     */
    public static final int APP_DOWNLOAD_PAUSE_NOTIFY_ID = 99999909;

    /**
     * 通知跳转Intent参数名
     */
    public static final String jumpBy = "jumpBy";

    /**
     * 来至点击 升级通知 的跳转
     */
    public static final String jumpByUpgrade = "jumpByUpgrade";

    /**
     * 来至点击 下载失败 通知的跳转
     */
    public static final String jumpByDownloadFault = "jumpByDownloadFault";

    /**
     * 来至点击 多个应用下载失败 通知的跳转
     */
    public static final String jumpByMultiDownloadFault = "jumpByMultiDownloadFault";

    /**
     * 来至点击 下载完成 通知的跳转
     */
    public static final String jumpByDownloadComplete = "jumpByDownloadComplete";

    /**
     * 来至点击 直接启动下载任务页面 通知的跳转
     */
    public static final String jumpByStartTaskManager = "directStart";

    /**
     * 来至点击 安装完成 通知的跳转
     */
    public static final String jumpByInstallComplete = "jumpByInstallComplete";

    /**
     * 点击通知需要跳转页面的类型
     */
    public static final String noticeStartType = "noticeStartType";
    /*跳转到其他APP*/
    public static final String wakeAppType = "wakeAppType";

    /**
     * 是推送
     */
    public static final String isPush = "isPush";

    /**
     * 是APP推送
     */
    public static final String isPushAPP = "isPushAPP";

    /**
     * 是专题推送
     */
    public static final String isPushTopic = "isPushTopic";

    /**
     * 是H5推送
     */
    public static final String isPushH5 = "isPushH5";

    /**
     * 推送Bundle数据
     */
    public static final String pushBundle = "pushBundle";

    /**
     * 跳转到 任务管理-应用下载Tab
     */
    public static final int goDownloadTab = 101;

    /**
     * 从点击 升级通知 跳转到 任务管理-应用下载Tab
     */
    public static final int goDownloadTabByUpgrade = 102;

    /**
     * 从点击 多个应用下载失败 跳转到 任务管理-应用下载Tab
     */
    public static final int goDownloadTabByMultiDownloadFault = 103;

    /**
     * 从点击 单个个应用下载失败 跳转到 任务管理-应用下载Tab
     */
    public static final int goDownloadTabBySingleDownloadFault = 104;

}
