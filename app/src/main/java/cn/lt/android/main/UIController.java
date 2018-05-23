package cn.lt.android.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import cn.lt.android.Constant;
import cn.lt.android.ads.wanka.WanKa;
import cn.lt.android.main.appdetail.AppDetailActivity;
import cn.lt.android.main.appdetail.ImageViewPagerActivity;
import cn.lt.android.main.appdetail.NormalActivity;
import cn.lt.android.main.download.TaskManagerActivity;
import cn.lt.android.main.personalcenter.AboutUsActivity;
import cn.lt.android.main.personalcenter.AccountCenterActivity;
import cn.lt.android.main.personalcenter.AppUninstallActivity;
import cn.lt.android.main.personalcenter.AppUpgradeActivity;
import cn.lt.android.main.personalcenter.SettingActivity;
import cn.lt.android.main.personalcenter.UserInfoEditActivity;
import cn.lt.android.main.personalcenter.feedback.FeedBackActivity;
import cn.lt.android.main.recommend.CategoryActivity;
import cn.lt.android.main.recommend.CategoryDetailActivity;
import cn.lt.android.main.recommend.NewAppActivity;
import cn.lt.android.main.recommend.SmartListActivity;
import cn.lt.android.main.search.SearchActivity;
import cn.lt.android.main.specialtopic.SpecialTopicActivity;
import cn.lt.android.main.specialtopic.SpecialTopicDetailActivity;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.umsharesdk.NoPlatDownActivity;
import cn.lt.android.util.LogUtils;

/**
 * Created by wenchao on 2016/3/1.
 * ui控制跳转
 */
public class UIController {

    /***
     * 跳转到应用升级
     *
     * @param context
     */
    public static void goUpdateActivity(Context context) {
        LogUtils.e("AppUpdateActivity", "AppUpdateActivity");
        Intent intent = new Intent(context, AppUpgradeActivity.class);
        context.startActivity(intent);
    }

    /**
     * 应用卸载
     *
     * @param context
     */
    public static void goAppUninstallActivity(Context context) {
        Intent intent = new Intent(context, AppUninstallActivity.class);
        context.startActivity(intent);
    }

    /***
     * 任务管理
     *
     * @param context
     */
    public static void goDownloadTask(Context context) {
        Intent intent = new Intent(context, TaskManagerActivity.class);
        context.startActivity(intent);
    }


    /**
     * 去设置
     *
     * @param context
     */
    public static void goSetting(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    /**
     * 关于我们
     *
     * @param context
     */
    public static void goAboutUs(Context context) {
        Intent intent = new Intent(context, AboutUsActivity.class);
        context.startActivity(intent);
    }


    public static void goTaskManager(Context context) {
        Intent intent = new Intent(context, TaskManagerActivity.class);
        context.startActivity(intent);
    }

//    /**
//     * Activity的转场动画
//     * @param push_left_in
//     * @param push_left_out
//     */
//    private static void setActivityTransitionAnimation(int push_left_in, int push_left_out) {
//        ActivityManager.self().topActivity().overridePendingTransition(push_left_in,
//                push_left_out);
//    }

    /**
     * 分类列表
     *
     * @param context
     */
    public static void goCategory(Context context) {
        context.startActivity(new Intent(context, CategoryActivity.class));
    }

    /**
     * 分类详情，是一个游戏列表
     *
     * @param context
     */
    public static void goCategoryDetail(Context context, String categoryType, String id, String title) {
        Intent intent = new Intent(context, CategoryDetailActivity.class);
        intent.putExtra(Constant.EXTRA_TYPE, categoryType);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    /**
     * 应用管理--应用卸载页面
     *
     * @param context
     */
    public static void goAppManager_uninstallPage(Context context) {
        Intent intent = new Intent(context, AppUninstallActivity.class);
        context.startActivity(intent);
    }


    /***
     * 下载管理
     *
     * @param context
     */
    public static void goInstallTask(Context context) {
        context.startActivity(new Intent(context, TaskManagerActivity.class).putExtra(Constant.GO_INSTALL_TAB, true));
    }

    /***
     * 下载管理
     *
     * @param context
     */
    public static void goDownloadTask(Context context, String jumpBy) {
        context.startActivity(new Intent(context, TaskManagerActivity.class).putExtra(NoticeConsts.jumpBy, jumpBy));
    }

    /***
     * 搜索
     *
     * @param context
     */
    public static void goSearchActivity(Context context, String value, boolean isAds, String pageName,String searchAdsId) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra("searchAds", value);
        intent.putExtra("isAds", isAds);
        intent.putExtra("pageName", pageName);
        intent.putExtra("searchAdsId",searchAdsId);
        context.startActivity(intent);
    }

    /***
     * 游戏/应用详情
     *
     * @param context
     */
    public static void goAppDetail(Context context, boolean isAd, String adMold, String id, String pkgName, String type, String fromPage, String category, String downloadUrl) {
        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra(Constant.EXTRA_AD, isAd);
        intent.putExtra(Constant.EXTRA_PKGNAME, pkgName);
        if (isAd) {
            intent.putExtra(Constant.EXTRA_CATEGORY, category);
            intent.putExtra(Constant.EXTRA_AD_DOWNLOAD_URL, downloadUrl);
            intent.putExtra(Constant.EXTRA_AD_MOLD, adMold);
            DCStat.adClickReport(pkgName, category, adMold);
        } else {
            intent.putExtra(Constant.EXTRA_ID, id);
        }
        intent.putExtra(Constant.EXTRA_TYPE, type);
        intent.putExtra(Constant.EXTRA_PAGE, fromPage);
        context.startActivity(intent);
    }

    public static void goAppDetail(Context context, boolean isAd, String adMold, String id, String pkgName, String type, String fromPage, String category, String downloadUrl, JSONObject reportData) {
        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra(Constant.EXTRA_AD, isAd);
        intent.putExtra(Constant.EXTRA_PKGNAME, pkgName);
        if (isAd) {
            intent.putExtra(Constant.EXTRA_CATEGORY, category);
            intent.putExtra(Constant.EXTRA_AD_DOWNLOAD_URL, downloadUrl);
            intent.putExtra(Constant.EXTRA_AD_MOLD, adMold);
            DCStat.adClickReport(pkgName, category, adMold);
        } else {
            intent.putExtra(Constant.EXTRA_ID, id);
        }

        if (reportData != null) {
            intent.putExtra(WanKa.KEY_REPORT_DATA, reportData.toString());
        }
        intent.putExtra(Constant.EXTRA_TYPE, type);
        intent.putExtra(Constant.EXTRA_PAGE, fromPage);
        context.startActivity(intent);
    }

    /***
     * 游戏/应用详情(点击推送跳转的)
     *
     * @param context
     */
    public static void goAppDetailByPush(Context context, String id, String type, String pushId, String reportData) {
        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.EXTRA_TYPE, type);
        intent.putExtra(NoticeConsts.isPush, true);
        intent.putExtra(Constant.EXTRA_PUSH_ID, pushId);
        intent.putExtra(WanKa.KEY_REPORT_DATA, reportData);
        context.startActivity(intent);
    }

    /***
     * 游戏/应用详情(deeplink跳转的)
     *
     * @param context
     */
    public static void goAppDetailByDeeplink(Context context, String id, String type, String from) {
        Intent intent = new Intent(context, AppDetailActivity.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.EXTRA_TYPE, type);
        intent.putExtra(Constant.EXTRA_IS_FROM_DEEPLINK, true);
        intent.putExtra(Constant.EXTRA_DEEPLINK_FROM, from);
        context.startActivity(intent);
    }

    /***
     * 用户登录/注册
     *
     * @param type
     */
    public static void goAccountCenter(Activity activity, String type) {
        Intent intent = new Intent(activity, AccountCenterActivity.class);
        intent.putExtra(Constant.TYPE, type);
        activity.startActivityForResult(intent, 0);
    }

    /***
     * 设置新密码
     *
     * @param activity
     * @param type
     */
    public static void goAccountCenter(Activity activity, String type, String resetCode) {
        Intent intent = new Intent(activity, AccountCenterActivity.class);
        intent.putExtra(Constant.TYPE, type);
        intent.putExtra("resetCode", resetCode);
        activity.startActivityForResult(intent, 0);
    }

    /***
     * 设置账户管理
     *
     * @param activity
     * @param type
     */
    public static void goAccountCenter1(Activity activity, String type) {
        Intent intent = new Intent(activity, AccountCenterActivity.class);
        intent.putExtra(Constant.TYPE, type);
        activity.startActivityForResult(intent, 0);
    }

    public static void goMine(Activity activity, String type) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(Constant.TYPE, type);
        activity.startActivity(intent);
    }

    /***
     * 修改/编辑用户资料
     *
     * @param type
     */
    public static void goUserInfoEditPage(Activity activity, String type) {
        Intent intent = new Intent(activity, UserInfoEditActivity.class);
        intent.putExtra(Constant.TYPE, type);
        activity.startActivityForResult(intent, 0);
    }

    /***
     * 修改/编辑用户资料
     */
    public static void goUserInfoEditPage1(Activity activity) {
        Intent intent = new Intent(activity, UserInfoEditActivity.class);
        activity.startActivityForResult(intent, 0);
    }

    /***
     * 跳转到图片浏览器
     *
     * @param activity
     * @param urls
     * @param position
     */
    public static void jumpToImageBrowster(Activity activity, ImageViewPagerActivity.ImageUrl urls, int position) {
        if (urls != null && activity != null) {
            if (position < 0) {
                position = 0;
            }
            Intent intent = new Intent(activity, ImageViewPagerActivity.class);
            intent.putExtra(ImageViewPagerActivity.POSITION, position);
            intent.putExtra(ImageViewPagerActivity.PHOTOS, urls);
            activity.startActivity(intent);
        }
    }

/*    *//**
     * 必备、新品tab、普通列表 公用
     *
     * @param context
     *//*
    public static void goNecessary(Context context, String id,int pageType) {
        Intent intent = new Intent(context, NewAppActivity.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.EXTRA_TYPE, pageType);
        context.startActivity(intent);
    }*/

    /**
     * 必备、新品tab、普通列表 公用
     *
     * @param context
     */
    public static void goNecessary(Context context, String id, int pageType, String title) {
        Intent intent = new Intent(context, NewAppActivity.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.EXTRA_TYPE, pageType);
        intent.putExtra(Constant.EXTRA_NORMAL_TITLE, title);
        context.startActivity(intent);
    }

    /**
     * 智能列表
     *
     * @param context
     */
    public static void goSamrtList(Context context, String id) {
        Intent intent = new Intent(context, SmartListActivity.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        context.startActivity(intent);

    }

    public static void goNormalList(Context context, String id, String title) {
        Intent intent = new Intent(context, NormalActivity.class);
        intent.putExtra(Constant.EXTRA_ID, id);
        intent.putExtra(Constant.NORMAL_TITLE, title);
        context.startActivity(intent);
    }

    /**
     * 新品
     *
     * @param context
     */
//    public static void goNewApp(Context context, String id) {
//        Intent intent = new Intent(context, NewAppActivity.class);
//        intent.putExtra(Constant.EXTRA_ID, id);
//        context.startActivity(intent);
//    }

    /**
     * 专题
     *
     * @param context
     */
    public static void goSpecial(Context context, String activityType) {
        Intent intent = new Intent(context, SpecialTopicActivity.class);
        context.startActivity(intent.putExtra("activityType", activityType));
    }

    /**
     * 反馈
     *
     * @param context
     */
    public static void goFeedback(Context context) {
        Intent intent = new Intent(context, FeedBackActivity.class);
        context.startActivity(intent);
    }

    /**
     * 专题详情
     *
     * @param context
     */
    public static void goSpecialDetail(Context context, String topicId, String title, String pageName, boolean isByPush, boolean isByDeeplink) {
        Intent intent = new Intent(context, SpecialTopicDetailActivity.class);
        context.startActivity(intent.putExtra("topicId", topicId).
                putExtra("pageTitle", title).
                putExtra(Constant.EXTRA_PAGE, pageName).
                putExtra(NoticeConsts.isPush, isByPush).
                putExtra(Constant.EXTRA_IS_FROM_DEEPLINK, isByDeeplink));
    }


    /**
     * web页面
     *
     * @param context
     * @param title
     * @param url
     */
    public static void goWebView(Context context, String title, String url, boolean isByPush) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(Constant.EXTRA_TITLE, title);
        intent.putExtra(Constant.EXTRA_URL, url);
        intent.putExtra(NoticeConsts.isPush, isByPush);
        context.startActivity(intent);
    }


    /**
     * 跳首页
     *
     * @param context 上下文对象；
     * @param mainTab {@link MainActivity#PAGE_TAB_RECOMMEND }，
     *                {@link MainActivity#PAGE_TAB_SOFT}，
     *                {@link MainActivity#PAGE_TAB_GAME}，
     *                {@link MainActivity#PAGE_TAB_RANK}
     * @param subTab  {@link MainActivity#PAGE_TAB_SOFT_SUB_INDEX}，
     *                {@link MainActivity#PAGE_TAB_SOFT_SUB_RANK}，
     *                {@link MainActivity#PAGE_TAB_SOFT_SUB_CAT}，
     *                {@link MainActivity#PAGE_TAB_GAME_SUB_INDEX}，
     *                {@link MainActivity#PAGE_TAB_GAME_SUB_RANK}，
     *                {@link MainActivity#PAGE_TAB_GAME_SUB_CAT},
     *                {@link MainActivity#PAGE_TAB_RANK_SUB_GAME},
     *                {@link MainActivity#PAGE_TAB_RANK_SUB_SOFT}
     */
    public static void goHomePage(Context context, int mainTab, int subTab) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.INTENT_JUMP_KEY_MAIN_TAB, mainTab);
        intent.putExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB, subTab);
        context.startActivity(intent);
    }

    /**
     * 没有安装分享平台页面
     */
    public static void goNoInstallSharePlat(Context context, String platName, String resourceType) {
        Intent intent = new Intent(context, NoPlatDownActivity.class);
        intent.putExtra("platName", platName);
        intent.putExtra("resourceType", resourceType);
        context.startActivity(intent);
    }
}
