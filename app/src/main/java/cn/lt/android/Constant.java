package cn.lt.android;

import com.yolanda.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Headers;

/**
 * Created by wenchao on 2016/3/7.
 */
public class Constant {

    public static final String UPGRADE_POP_PERIOD = "upgrade_pop_period"; //客户端升级弹出框时间间隔

//    public static final String AUTO_INSTALL_PERIOD = "auto_install_period";//自动装弹框时间间隔

    public static final String AUTO_UPGRADE = "auto_upgrade";//应用自动升级间隔时间

    public static final String SPREAD_PERIOD = "spread_period";//弹窗推广间隔时间

    public static final String POST_CID_PERIOD = "cid_period";//上报CID间隔时间

    public static final String GDT_PERIOD = "gdt_period";//广点通间隔时间
    public static final String BAIDU_PERIOD = "baidu_period";//广点通间隔时间

    public static final String GDT_STATUS = "gdt_status";//广点通开关
    public static final String GDT_SHOW = "gdt_show";//广点通展示(无用)

    public static final String BAIDU_STATUS = "baidu_status";//百度开关
    public static final String BAIDU_SHOW = "baidu_show";//百度展示(无用)

    public static final String PULLLIVE_STATUS = "pulllive_status";//拉活开关

    public static final String FEIYANG_STATUS = "feiyang_status";//飞扬开关

    public static final String RUIWEI_STATUS = "ruiwei_status";//芮薇开关

    public static final String BACKGROUND_TIME = "background_time";//按下home键的时间

    public static final String FRONT_DESK_TIME = "front_desk_time";//进入APP的时间

    public static final String GDT_LAST_TIME = "gdt_last_time";//广点通最后一次弹出时间


    public static final String SELECTION_PLAY_POP_STATE = "selection_play_state";//精选必备状态

    public static final String JUMP_PAGE_SPECIAL = "page_special";

    public static final String NORMAL_TITLE = "title";

    public static final String EXTRA_TYPE = "extra_type";

    public static final String EXTRA_PKGNAME = "extra_pkg_name";

    public static final String EXTRA_CATEGORY = "extra_category";

    public static final String EXTRA_AD_DOWNLOAD_URL = "extra_adDownloadUrl";

    public static final String EXTRA_AD_MOLD = "extra_ad_mold";


    public static final String EXTRA_AD = "extra_ad";

    public static final String EXTRA_ID = "extra_id";

    public static final String EXTRA_NORMAL_TITLE = "extra_normal_title";

    public static final String EXTRA_TITLE = "extra_title";

    public static final String EXTRA_URL = "extra_url";

    public static final String GO_INSTALL_TAB = "goInstallTab";


    public static final String EXTRA_PAGE = "extra_page";

    public static final String EXTRA_CLICK_FROM_NOTICE = "extra_click_from_notice";

    public static final String EXTRA_PUSH_ID = "push_id";

    public static final String CORNER_COUNT = "corner_count";//桌面角标数字

    public static final String EXTRA_IS_FROM_DEEPLINK = "is_from_deeplink";

    public static final String EXTRA_DEEPLINK_DATA_STR = "deeplink_data_str";

    public static final String EXTRA_DEEPLINK_FROM = "deeplink_from";


    /***
     * 启动页
     */
    public static final String PAGE_LOADING = "qd";
    /***
     * 通知栏
     */
    public static final String PAGE_NOTIFICATION = "notification";
    /***
     * 装机必备
     */
    public static final String PAGE_BIBEI = "zjbb";
    /***
     * 推广图
     */
    public static final String PAGE_SPREAD = "tgt";

    /***
     * 智能列表
     */
    public static final String PAGE_SAMRT_LIST = "znlb";


    /*********************************************软件页面************************************************/

    /***
     * 游戏/应用详情页面
     */
    public static final String PAGE_DETAIL = "yyxq";

    /***
     * 广告详情页面
     */
    public static final String PAGE_AD_DETAIL = "ggxq";

    /**
     * 推荐页面
     */
    public static final String PAGE_RECOMMEND = "tj";
    /*********************************************软件页面************************************************/

    /**
     * 软件-精选页面
     */
    public static final String PAGE_SOFT_HIGHLYSELECT = "rj_jx";
    /**
     * 软件-飙升榜
     */
    public static final String PAGE_SOFT_HOT_RANK = "rj_bd_bs";
    /**
     * 软件-热搜榜
     */
    public static final String PAGE_SOFT_MONTH_RANK = "rj_bd_rs";
    /**
     * 软件-新锐榜
     */
    public static final String PAGE_SOFT_ALL_RANK = "rj_bd_xr";
    /**
     * 软件-分类
     */
    public static final String PAGE_SOFT_CATEGORY = "rj_fl";


    /*********************************************游戏页面************************************************/

    /**
     * 游戏-精选页面
     */
    public static final String PAGE_GAME_HIGHLYSELECT = "yx_jx";
    /**
     * 游戏-单机排行
     */
    public static final String PAGE_GAME_OFFLINE_RANK = "yx_bd_dj";
    /**
     * 游戏-网游排行
     */
    public static final String PAGE_GAME_LINE_RANK = "yx_bd_wy";
    /**
     * 游戏-总榜
     */
    public static final String PAGE_GAME_ALL_RANK = "yx_bd_jp";
    /**
     * 游戏-分类
     */
    public static final String PAGE_GAME_CATEGORY = "yx_fl";

    /***
     * 分类-详情
     */
    public static final String PAGE_CATEGORY_DETAIL = "fl_xq";


    /*********************************************榜单页面************************************************/

    /**
     * 榜单-游戏榜单
     */
    public static final String PAGE_GAME_RANK = "bd_yx";
    /**
     * 榜单-软件榜单
     */
    public static final String PAGE_SOFT_RANK = "bd_rj";

    /*********************************************我页面************************************************/

    /**
     * 我
     */
    public static final String PAGE_MINE = "mine";


    /*********************************************任务管理页面************************************************/
    /**
     * 应用下载页面
     */
    public static final String PAGE_DOWNLOAD = "rw_xz";
    /**
     * 应用安装页面
     */
    public static final String PAGE_INSTALL = "rw_az";


    /*********************************************搜索页面************************************************/


    /**
     * 搜索广告页面
     */
    public static final String PAGE_SEARCH_ADS = "ss_gg";
    /***
     * 搜索推荐页
     */
    public static final String PAGE_SEARCH_ADV = "ss_tj";
    /**
     * 搜索结果页
     */
    public static final String PAGE_SEARCH_RESULT = "ss_jg";
    /**
     * 搜索自动匹配页
     */
    public static final String PAGE_SEARCH_AUTOMATCH = "ss_zdpp";
    /**
     * 搜素无结果页
     */
    public static final String PAGE_SEARCH_NODATA = "ss_wsj";


    /*********************************************
     * 个人中心页面
     ************************************************/

    public static final String PAGE_PERSONALCENTER = "grzx";

    /**
     * 登录页面
     */
    public static final String PAGE_LOGIN = "gr_dl";
    /**
     * 注册页面
     */
    public static final String PAGE_REGISTER = "gr_zc";
    /**
     * 注册成功-修改昵称和头像页面
     */
    public static final String PAGE_REGISTER_SUCCESS = "gr_cg";
    /**
     * 账号管理页面
     */
    public static final String PAGE_ACCOUNT_MANAGE = "gr_zh";
    /**
     * 修改头像页面
     */
    public static final String PAGE_MODIFY_AVATOR = "gr_tx";
    /**
     * 修改昵称
     */
    public static final String PAGE_MODIFY_NICKNAME = "gr_nc";
    /**
     * 修改手机
     */
    public static final String PAGE_MODIFY_MOBILE = "gr_sj";
    /**
     * 忘记密码
     */
    public static final String PAGE_FORGET_PWD = "gr_wj";
    /**
     * 设置新密码
     */
    public static final String PAGE_SET_NEWPWD = "gr_xmm";
    /**
     * 修改密码
     */
    public static final String PAGE_MODIFY_PWD = "gr_xg";
    /**
     * 管理-应用更新
     */
    public static final String PAGE_APP_UDPATE = "gr_gl_gx";
    /**
     * 管理-应用卸载
     */
    public static final String PAGE_APP_UNINSTALL = "gr_gl_xz";

    /**
     * 应用设置
     */
    public static final String PAGE_APP_SETTING = "gr_sz";
    /**
     * 版本更新
     */
    public static final String PAGE_PLATFORM_UPDATE = "gr_gx";
    /**
     * 关于我们
     */
    public static final String PAGE_ABOUT_US = "gr_gy";
    /**
     * 任务管理
     */
    public static final String PAGE_TASK_MANAGE = "gr_rw";
    /**
     * 用户协议
     */
    public static final String PAGE_USER_AGREENMENT = "gr_xy";

    /**
     * 浮层广告
     */
    public static final String PAGE_FLOAT = "Float";


    /*********************************************跳转类型************************************************/


    /*********************************************
     * 专题页面
     ************************************************/

        /*专题详情页面*/

    public static final String PAGE_SPECIAL_DETAIL = "ztxq";


    /**
     * 关于我们
     */
    public static final String PAGE_ABOUT_US_JUMP = "page_about_us";
    /***
     * 选项卡专题页面
     */
    public static final String PAGE_NORMAL_LIST = "xxk";

    public static final String PAGE_NORMAL_LIST_TITLE = "xxk_";
    public static final String PAGE_NORMAL_PT_TITLE = "pt_";

    /***
     * 混合分类
     */
    public static final String PAGE_SINGLE_CATEGORY = "hhfl";


    /*********************************************
     * 后台与客户端约定的跳转类型
     ************************************************/


    public static final String PAGE_RECOMMEND_SUB = "page_recommend";


    /***
     * 必备页面
     */
    public static final String PAGE_NECESSARY = "bb";

    /**
     * 退出安装弹窗
     */
    public static final String QUIT_DIALOG = "quit_dialog";


    /**
     * 分类页面
     */
    public static final String PAGE_CLASSIFY = "page_classify";

    /**
     * 分类详情页面
     */
    public static final String PAGE_CLASSIFY_DETAIL = "page_classify_detail";


    /**
     * 登录页面
     */

    public static final String PAGE_LOGIN_JUMP = "page_login";

    /***
     * H5浏览页面
     */
    public static final String PAGE_H5 = "h5";

    /**
     * 注册页面
     */

    public static final String PAGE_REGISTER_JUMP = "page_register";

    /*新品页面*/

    public static final String PAGE_NEW = "xp";
    /*专题页面*/

    public static final String PAGE_SPECIAL = "zt";
    /***
     * 软件专题
     */
    public static final String PAGE_SOFT_SPECIAL = "rjzt";
    /***
     * 游戏专题
     */
    public static final String PAGE_GAMES_SPECIAL = "yxzt";

    /*专题详情页面*/

    public static final String PAGE_SPECIAL_DETAIL2 = "page_special_detail";
    /*榜單页面*/

    public static final String PAGE_LIST = "page_list";
    /* 活动详情*/

    public static final String PAGE_SPECIAL_ACTIVITY = "page_special_activity";
    /*软件精选页面*/

    public static final String PAGE_APP_CHOICE = "page_app_choice";
    /* 软件榜单页面---*/

    public static final String PAGE_APP_LIST = "page_app_list";

    /*软件分类页面*/

    public static final String PAGE_APP_CLASSIFY = "page_app_classify";
    /* 软件专题页面*/

    public static final String PAGE_APP_SPECIAL = "page_app_special";
    /*   游戏精选页面*/

    public static final String PAGE_GAME_CHOICE = "page_game_choice";
   /*游戏榜单页面*/

    public static final String PAGE_GAME_LIST = "page_game_list";
   /* 游戏分类页面*/

    public static final String PAGE_GAME_CLASSIFY = "page_game_classify";
    /*游戏专题页面*/

    public static final String PAGE_GAME_SPECIAL = "page_game_special";
    /*搜索页面*/


    public static final String JUMP_PAGE_SEARCH = "page_search";

    /**
     * 智能列表
     */
    public static final String JUMP_PAGE_INTELLECTIVE_ADS_LISTS = "intellective_ads_lists";


    public static final String PAGE_TASK_MANAGEMENT = "page_task_management";

    /**
     * 应用管理页面
     */

    public static final String PAGE_APP_MANAGEMENT = "page_app_management";
/*    *
     * 设置页面*/

    public static final String PAGE_SET = "page_set";
/*    *
     * 反馈页面*/

    public static final String PAGE_FEEDBACK = "page_feedback";

//    *
//     * 帐号管理页面

    public static final String PAGE_ID_MANAGEMENT = "page_id_management";
//    *
//     * 修改昵称页面

    public static final String PAGE_CHANGE_NICKNAME = "page_change_nickname";
//    *
//     * 修改手机号页面

    public static final String PAGE_CHANGE_PHONE_NUMBER = "page_change_phone_number";
//    *
//     * 修改密码页面

    public static final String PAGE_CHANGE_PASSWORD = "page_change_password";
//    *
//     * 忘记密码页面

    public static final String PAGE_FORGOT_PASSWORD = "page_forgot_password";
//    *
//     * 找回密码页面

    public static final String PAGE_RETRIEVE_PASSWORD = "page_retrieve_password";

//    **
//     * behavior


    public static final String TYPE = "type";

//    **
//     * 用户是否登录

    public static final String IS_LOGIN = "isLogin";


//    **
//     * 用户登录

    public static final String USER_LOGIN = "user_login";
//    **
//     * 用户注册

    public static final String USER_REGISTER = "user_register";
//    **
//     * 重置密码

    public static final String RESET_PWD = "reset_pwd";
//    **
//     * 忘记密码/找回密码

    public static final String GET_BACK_PWD = "get_back_pwd";
//    **
//     * 用户资料/账号管理

    public static final String USER_INFO = "user_info";
//    **
//     * 修改手机号码


    public static final String MODIFY_MOBILE = "modify_mobile";
//    **
//     * 修改昵称

    public static final String MODIFY_NICKNAME = "modify_nickname";
//    **
//     * 修改密码

    public static final String MODIFY_PWD = "modify_pwd";
//    **
//     * 用户协议

    public static final String USER_AGREEMENT = "user_agreement";
//    **
//     * 设置昵称和头像

    public static final String SETNICKNAME = "set_nickname";
    /***
     * 移动网络类型3G或4G
     */
    public static final int NET_MOBILE_PHONE = 1;
    /***
     * wifi类型
     */
    public static final int NET_WIFI = 2;

    /***
     * 无网络类型
     */
    public static final int NO_NET = 3;

    /**
     * 专题类型---软件、游戏
     */
    public static final String SPECIALTOP_TYPE_SOFT = "software";
    public static final String SPECIALTOP_TYPE_GAME = "game";

    /**
     * Unknown network class
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;

    /**
     * wifi net work
     */
    public static final int NETWORK_WIFI = 1;

    /**
     * "2G" networks
     */
    public static final int NETWORK_CLASS_2_G = 2;

    /**
     * "3G" networks
     */
    public static final int NETWORK_CLASS_3_G = 3;

    /**
     * "4G" networks
     */
    public static final int NETWORK_CLASS_4_G = 4;
    /**
     * 需要更新的时间间隔
     */

    public static final long CHECKVERSIONPERIOD = 1000 * 60 * 60 * 8L;//5分钟检查一次版本升级（正式）1000 * 60 * 60 * 8L

    public static final long CHECK_RUIWEI_PREIOD = 1000 * 60 * 5; //5分钟检查一次芮薇的状态

    public static final long DEFAULT_PERIOD = 1000 * 60 * 60 * 24 * 7L; //默认弹框时间(一周)

    public static final String CLIENT_UPDATE_PERIOD = "client_update"; //客户端升级弹出框时间间隔

    public static final String AUTO_INSTALL_PERIOD = "auto_install";//自动装弹框时间间隔

    public static final String AUTO_UPGRADE_PERIOD = "auto_upgrade";//应用自动升级间隔时间

    public static final String SELECTION_PLAY_PERIOD = "selection_play";//精选必备间隔时间


    public static final String CLIENT_UPDATE_SHOWED = "client_update_tag"; //客户端升级弹出框是否已经展示过

    public static final String CLIENT_UPDATE_STATE = "client_update_state"; //客户端升级弹出框状态

    public static final String AUTO_INSTALL_SHOWED = "auto_install_tag";    //自动装弹出框是否已经弹出过

    public static final String AUTO_INSTALL_STATE = "auto_install_state";    //自动装弹出框状态

    public static final String AUTO_UPGRADE_SHOWED = "auto_upgrade_tag";

    public static final String SELECTION_PLAY_SHOWED = "selection_tag";  //精选必玩是否已经弹出过

    public static final String WK_SWITCH = "wanka_switch";//玩咖曝光开关

    public static final String STATUS_OPEN = "open";//玩咖曝光开关
    public static final String STATUS_CLOSE = "close";//玩咖曝光开关

    public static final String INSTALL_PKG = "install_pkg";//安装器名称

    public static final String DEF_INSTALL_PKG = "cn.lt.appstore";//默认安装器包名
    /***
     * 芮微广告相关配置
     */
    public static final String RW_CHANEL = "1000_2201_04900100";//芮微广告渠道
    public static final String RW_APPID = "8ac05736-4cbf-4ad6-b633-1f86c4a730ba";//芮微appid
    public static final String RW_TOKEN = "8ce4dffc44237fab";//芮微广告TOKEN


    public static String GeTuipushCID = "";//个推CID

    public static String FY_SWITCH = "close";//飞扬开关

    public static String FY_CHANEL = "c101070110400v7";//飞扬开关 c202070360300v7 为测试用渠道

    public static String APPID = "1105999844";//广点通资源位ID

    public static String BANNERID = "8010629697343656";//广点通Banner广告位ID

    public static String SplashPosID = "3030729476663199";//广点通广告位ID

    public static String baiduSplashId = "4763988";//百度开屏广告ID

    /**
     * 获取最后一页
     *
     * @param headers
     * @return
     */
    public static int getLastPage(Headers headers) {
        try {
            String xLinks = headers.get("X-Links");
            JSONObject jsonObject = new JSONObject(xLinks);
            int lastPage = jsonObject.getInt("last_page");
            return lastPage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GlobalConfig.FIRST_PAGE;
    }

    /**
     * 推荐页 获取最后一页
     *
     * @param response
     * @return
     */
    public static int getLastPage(Response<String> response) {
        try {
            com.yolanda.nohttp.Headers headers = response.getHeaders();
            JSONObject jsonObject = new JSONObject(headers.toJSONString());
            JSONArray jsonArray = jsonObject.getJSONArray("X-Links");
            String string = jsonArray.getString(0);
            JSONObject json = new JSONObject(string);
            int last_page = json.getInt("last_page");
            return last_page;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GlobalConfig.FIRST_PAGE;
    }

}
