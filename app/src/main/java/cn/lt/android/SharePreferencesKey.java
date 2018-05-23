package cn.lt.android;

/**
 * Created by wenchao on 2016/1/19.
 */
public class SharePreferencesKey {
    /***
     * 用户ID
     */
    public static final String USERMOBILE = "userMobile";
    /***
     * 用户名
     */
    public static final String USERNAME = "userName";
    /***
     * 用户昵称
     */
    public static final String NICKNAME = "nickname";
    /***
     * 用户头像
     */
    public static final String USERAVATAR = "userAvatar";
    /***
     * 登录ＴＯＫＥＮ
     */
    public static final String TOKENKEY = "tokenkey";
    /***
     * 登录用户id
     */
    public static final String USERIDKEY = "userid";

    /**仅在wifi网络下自动升级*/
    public static final String AUTO_UPGRADE_ONLY_IN_WIFI = "AUTO_UPGRADE_ONLY_IN_WIFI";
    /**root自动安装*/
    public static final String AUTO_INSTALL_BY_ROOT = "AUTO_INSTALL_BY_ROOT";
    /**自动删除安装包*/
    public static final String AUTO_DELETE_APK = "AUTO_DELETE_APK";
    /**上次弹出免root自动装时间*/
    public final static String AUTO_INSTALL_TIME = "AUTO_INSTALL_TIME";
    /**带小红点的按钮是否被点击过*/
    public final static String IS_ONCLICK = "IS_ONCLICK";

    /**用户是否自己选择了root安装设置*/
    public static final String INSTALL_BY_ROOT_USER_IS_CHANGE = "INSTALL_BY_ROOT_USER_IS_CHANGE";


    /********************************************应用自动升级****************************************************/
    /**
     * 应用自动升级 的文件名
     */
    public final static String APP_AUTO_UPGRADE = "app_auto_upgrade";

    /**
     * (数据字段)记录应用自动升级弹窗开关
     */
    public final static String AUTO_UPGRADE_DIALOG_SWITCH = "auto_upgrade_dialog_switch";

    public final static String AUTO_UPGRADE_DIALOG_SHOW = "auto_upgrade_dialog_show";

    /**
     * (数据字段)记录应用自动升级弹窗时间
     */
    public final static String AUTO_UPGRADE_DIALOG_TIME   = "auto_upgrade_dialog_time";

    /**
     * (数据字段)记录应用自动升级弹窗时间间隔
     */
    public final static String AUTO_UPGRADE_DIALOG_JIAN_GE   = "auto_upgrade_dialog_jian_ge";

    /**
     * (数据字段)记录应用自动弹窗已经弹过
     */
    public final static String AUTO_UPGRADE_IS_DIALOGED   = "auto_upgrade_is_dialoged";

    /**
     * (数据字段)是否已经发开应用自动升级
     */
    public final static String IS_OPEN_APP_AUTO_UPGRADE   = "is_open_app_auto_upgrade";

    /**
     * (数据字段)获取升级列表的时间
     */
    public final static String GET_UPGRADE_LIST_TIME   = "get_upgrade_list_time";

    /**
     * (数据字段)获取是否黑名单的时间
     */
    public final static String GET_IS_BLACKLIST_TIME = "get_is_blacklist_time";

    /**
     * (数据字段)是否被拉入应用自动升级的黑名单
     */
    public final static String IS_BLACKLIST = "is_blacklist";
    /********************************************应用自动升级****************************************************/


    /********************************************浮层广告****************************************************/
    /** 浮层广告开关*/
    public final static String NEED_SHOW_FLOAT_AD = "need_show_float_ad";
    public final static String SHOW_FLOAT_AD = "show_float_ad";

    /** 浮层广告间隔时间*/
    public final static String FLOAT_AD_PERIOD_TIME = "float_ad_period_time";

    /** 浮层广告最后一次展示时间*/
    public final static String FLOAT_AD_LAST_SHOW_TIME = "float_ad_last_show_time";
    /********************************************浮层广告****************************************************/


}
