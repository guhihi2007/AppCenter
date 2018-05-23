package cn.lt.android.util;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.text.TextUtils;

import java.lang.reflect.Field;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.Configure;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.wake.WaKeLog;
import cn.lt.framework.util.PreferencesUtils;

import static cn.lt.android.Constant.DEFAULT_PERIOD;
import static cn.lt.android.plateform.update.UpdateUtil.getDialogShowLastTime;
import static cn.lt.android.plateform.update.UpdateUtil.getSpreadDialogLastTime;

/**
 * Created by ATian on 2016/11/18.
 *
 * @des 弹框管理
 */

public class PopWidowManageUtil {

    /***
     * 自动装弹出框
     *
     * @param context
     * @return
     */
    public static boolean needAutoInstallDialog(Context context) {
        long lastTime = PreferencesUtils.getLong(context, SharePreferencesKey.AUTO_INSTALL_TIME, 0);
        long periodTime = PreferencesUtils.getLong(context, Constant.AUTO_INSTALL_PERIOD, 1000 * 60 * 60 * 24 * 7L);
        boolean hasShowed = PreferencesUtils.getBoolean(context, Constant.AUTO_INSTALL_SHOWED, false);
        return needShow(lastTime, periodTime, hasShowed);
    }

    /***
     * 平台升级框弹出时间
     *
     * @param context
     * @return
     */
    public static boolean needShowClientUpdateDialog(Context context) {
        long lastTime = getDialogShowLastTime(context);
        long periodTime = PreferencesUtils.getLong(context, Constant.UPGRADE_POP_PERIOD, DEFAULT_PERIOD);
        boolean hasShowed = PreferencesUtils.getBoolean(context, Constant.CLIENT_UPDATE_SHOWED, false);
        return needShow(lastTime, periodTime, hasShowed);
    }

    /***
     * 弹窗推广时间
     *
     * @param context
     * @return
     */
    public static boolean needShowSpreadDialog(Context context) {
        boolean state = PreferencesUtils.getBoolean(context, Constant.SELECTION_PLAY_POP_STATE, false);
        if (state) {
            long lastTime = getSpreadDialogLastTime(context);
            long periodTime = PreferencesUtils.getLong(context, Constant.SPREAD_PERIOD, DEFAULT_PERIOD);
            boolean hasShowed = PreferencesUtils.getBoolean(context, Constant.SELECTION_PLAY_SHOWED, false);
            return needShow(lastTime, periodTime, hasShowed);
        } else {
            return false;
        }

    }

    /***
     * 一天上报一次ＣＩＤ
     *
     * @param context
     * @return
     */
    public static boolean needPostLoacalData(Context context) {
        long nowTime = System.currentTimeMillis();
        long lastTime = PreferencesUtils.getLong(context, Constant.POST_CID_PERIOD, 1000 * 60 * 60 * 24 * 1);
        long gapTime = (nowTime - lastTime) / (1000 * 60 * 60 * 24 * 1);
        return gapTime > 0;
    }

    /***
     * 广点通广告弹出频率
     *
     * @param context
     * @return
     */
    public static boolean needShowGDT(Context context) {
        long serverTime = 0;
        long time = PreferencesUtils.getLong(context, Constant.BACKGROUND_TIME, System.currentTimeMillis());
        long lastTIme = PreferencesUtils.getLong(context, Constant.FRONT_DESK_TIME, 0);
        long gapTime = (lastTIme - time);

        boolean gdtStatus = PreferencesUtils.getBoolean(context, Constant.GDT_STATUS, false);
        boolean baiduStatus = PreferencesUtils.getBoolean(context, Constant.BAIDU_STATUS, false);
        if (baiduStatus) {
            serverTime = PreferencesUtils.getLong(context, Constant.BAIDU_PERIOD, 0);
            LogUtils.i("Erosion", "baiduTime===" + gapTime + ",baiduserverTime===" + serverTime);
        } else {
            if (gdtStatus) {
                serverTime = PreferencesUtils.getLong(context, Constant.GDT_PERIOD, 0);
                LogUtils.i("Erosion", "gapTime===" + gapTime + ",serverTime===" + serverTime);
            }
        }
        return gapTime >= serverTime && serverTime != 0;
    }

    /***
     * 百度广告弹出频率
     *
     * @param context
     * @return
     */
    public static boolean needShowBAIDU(Context context) {
        long time = PreferencesUtils.getLong(context, Constant.BACKGROUND_TIME, System.currentTimeMillis());
        long lastTIme = PreferencesUtils.getLong(context, Constant.FRONT_DESK_TIME, 0);
        long gapTime = (lastTIme - time);
        long serverTime = PreferencesUtils.getLong(context, Constant.BAIDU_PERIOD, 0);
        LogUtils.i("Erosion", "baidugapTime===" + gapTime + ",baiduserverTime===" + serverTime);
        return gapTime >= serverTime && serverTime != 0;
    }

    /***
     * 应用自动升级
     *
     * @param context
     */
    public static boolean needPromptAutoUpgrade(final Context context) {
        boolean hasAppAutoUpgradeDialog = true;
        do {
            // 判断是否为黑名单
            String isBlacklist = (String) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "");
            if (!TextUtils.isEmpty(isBlacklist) && isBlacklist.equals("yes")) {
                LogUtils.i(LogTAG.appAutoUpgradeDialog, "此用户被设置成黑名单，不需要弹窗了。。。");
                hasAppAutoUpgradeDialog = false;
                break;
            }

            // 应用升级弹窗开关是否开启
//            boolean autoUpgradeDialogSwitch = (boolean) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH, true);
            boolean autoUpgradeDialogSwitch = PreferencesUtils.getBoolean(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH, true);
            if (!autoUpgradeDialogSwitch) {
                LogUtils.i(LogTAG.appAutoUpgradeDialog, "自动升级弹窗开关关闭了。。。");
                hasAppAutoUpgradeDialog = false;
                break;
            }
            // 判断是否有可升级的应用
            boolean hasUpgradeApps = UpgradeListManager.getInstance().getAllUpgradeAppList().size() > 0;
            if (!hasUpgradeApps) {
                hasAppAutoUpgradeDialog = false;
                break;
            }
            // 判断是否已经打开了自动升级开关
            boolean isOpenAutoUpgrade = GlobalConfig.getIsOpenAutoUpgradeApp(context);
            if (isOpenAutoUpgrade) {
                hasAppAutoUpgradeDialog = false;
                break;
            } else {
                // 判断应用自动升级弹窗是否满足指定的时间条件
                long appAutoUpgradeDialogJianGeTime = PreferencesUtils.getLong(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, 0L) / 1000;
                long appAutoUpgradeDialogTime = (long) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_TIME, 0L);
                LogUtils.i(LogTAG.appAutoUpgradeDialog, "上次记录的时间是：" + appAutoUpgradeDialogTime + ", 上一次弹窗时间是 = " + TimeUtils.getStringToDateHaveHour(appAutoUpgradeDialogTime));
                if (appAutoUpgradeDialogTime != 0 && appAutoUpgradeDialogJianGeTime != 1) {
                    boolean isExceed = false;
                    if (appAutoUpgradeDialogJianGeTime == 0) {
                        isExceed = TimeUtils.isExceedWeek(appAutoUpgradeDialogTime);
                        LogUtils.i(LogTAG.appAutoUpgradeDialog, "间隔时间是0,上次弹窗时间与现在相差=" + (System.currentTimeMillis() - appAutoUpgradeDialogTime) + ", 是否超过自定义默认的一周 = " + isExceed);
                    } else if (appAutoUpgradeDialogJianGeTime == 999999999999L) {// 只弹一次
                        boolean isDialoged = (boolean) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_IS_DIALOGED, false);
                        LogUtils.i(LogTAG.appAutoUpgradeDialog, "时间设置为只弹一次，是否已经弹过 = " + isDialoged);
                        if (isDialoged) {
                            hasAppAutoUpgradeDialog = false;
                            break;
                        }
                    } else {
                        isExceed = TimeUtils.isExceedJianGe(appAutoUpgradeDialogTime, appAutoUpgradeDialogJianGeTime);
                        LogUtils.i(LogTAG.appAutoUpgradeDialog, "自定义了间隔时间：" + appAutoUpgradeDialogJianGeTime + ",与现在相差=" + (System.currentTimeMillis() - appAutoUpgradeDialogTime) + " 是否有超过间隔时间 = " + isExceed);
                    }

                    if (!isExceed) {
                        hasAppAutoUpgradeDialog = false;
                        break;
                    }
                }
            }
            // 判断是否wif环境
            if (!NetUtils.isWifi(context)) {
                hasAppAutoUpgradeDialog = false;
                break;
            }
            // 判断是否系统的权限
            boolean isSystemApp = PackageUtils.isSystemApplication(LTApplication.shareApplication());
            if (!isSystemApp) {
                hasAppAutoUpgradeDialog = false;
                break;
            }

            // 判断是否大于500M
            if (FileSizeUtil.getAvailableExternalMemorySize() / (1048 * 1024) <= 500) {
                hasAppAutoUpgradeDialog = false;
                break;
            }

        } while (false);
        return hasAppAutoUpgradeDialog;
    }

    /***
     * 根据弹窗条件判断是否需要弹窗
     *
     * @param lastTime
     * @param periodTime
     * @param isShowed
     * @return
     */
    private static boolean needShow(Long lastTime, Long periodTime, boolean isShowed) {
        long nowTime = System.currentTimeMillis();

        /* 0是没拿到值*/
        if (periodTime == 0) {
            return !isShowed;
        } else {
            if (lastTime == 0 || nowTime - lastTime >= periodTime) {
                return true;
            } else {
                return !isShowed;
            }
        }
    }

    /***
     * 保存弹出框/服务检测时间等配置信息
     * 保存条件：
     * 1.服务器时间不能为0
     * 2.服务器时间与本地时间不一致
     *
     * @param config
     */
    public static void saveConfigInfo(Context context, ConfigureBean config) {
//        saveAutoUpgradeAppConfigInfo(config);
//        Long upgradeTime = PreferencesUtils.getLong(context, Constant.UPGRADE_POP_PERIOD, DEFAULT_PERIOD);//升级弹框时间
//        Long autoInstallTime = PreferencesUtils.getLong(context, Constant.AUTO_INSTALL_PERIOD, DEFAULT_PERIOD);//自动装弹框时间    默认为7天
//        Long spreadTime = PreferencesUtils.getLong(context, Constant.SPREAD_PERIOD, DEFAULT_PERIOD);//弹窗推广时间
//        Long gdtTime = PreferencesUtils.getLong(context, Constant.GDT_PERIOD, DEFAULT_PERIOD);//广点通开屏广告配置
////        Long baiduTime = PreferencesUtils.getLong(context, Constant.BAIDU_PERIOD, DEFAULT_PERIOD);//百度开屏广告配置
//        Long floatPeriodTime = PreferencesUtils.getLong(context, SharePreferencesKey.FLOAT_AD_PERIOD_TIME, DEFAULT_PERIOD);// 浮层广告间隔时间

        // 遍历config
        Field[] declaredFields = ConfigureBean.class.getDeclaredFields();
        LogUtils.i("Erosion", "declaredFields:");
        try {
            for (Field field : declaredFields) {
                field.setAccessible(true);
                Configure configure = (Configure) field.get(config);
                LogUtils.i("Erosion","configure:" + configure.toString());

                ConfigureBean.ConfigureKey annotation;
                try {
                    annotation = field.getAnnotation(ConfigureBean.ConfigureKey.class);
                } catch (Exception e) {
                    LogUtils.e("LoadingActivity", "未设置 ConfigureBean.ConfigureKey 注解");
                    break;
                }

                String[] value = annotation.value();
                String keyState = value[0];
                LogUtils.i("LoadingActivity", keyState + " -> keyState");
                if (configure.getStatus().equals("open")) {
                    LogUtils.i("LoadingActivity", field.getName() + " -> 已开启");
                    if (value.length >= 3) {
                        String keyPeriod = value[1];
                        String keyShowed = value[2];
                        LogUtils.i("LoadingActivity", "keyPeriod:" + keyPeriod + " ,keyShowed:" + keyShowed);

                        PreferencesUtils.putBoolean(context, keyState, true);

                        Long periodTime = PreferencesUtils.getLong(context, keyPeriod, Constant.DEFAULT_PERIOD);//客户端升级弹框时间
                        if (periodTime != configure.getTime() * 1000) {
                            PreferencesUtils.putLong(context, keyPeriod, configure.getTime() * 1000);
                            PreferencesUtils.putBoolean(context, keyShowed, false);//后台有更改弹出规则时，复位该值
                        }
                    } else {
                        PreferencesUtils.putBoolean(context, keyState, true);
                    }

                } else {
                    if (value.length >= 2) {
                        PreferencesUtils.putLong(context, value[1], DEFAULT_PERIOD);
                    }
                    PreferencesUtils.putBoolean(context, keyState, false);
                    LogUtils.i("LoadingActivity", field.getName() + " -> 已关闭");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            LogUtils.i("Erosion", "IllegalAccessException:" + e.getMessage().toString());
        }


//        if ("open".equals(config.getClient_update().getStatus())) {
//            if (upgradeTime != config.getClient_update().getTime() * 1000) {
//                LogUtils.i("Loading", "保存平台升级弹框时间");
//                PreferencesUtils.putLong(context, Constant.UPGRADE_POP_PERIOD, config.getClient_update().getTime() * 1000);
//                PreferencesUtils.putBoolean(context, Constant.CLIENT_UPDATE_SHOWED, false);//后台有更改弹出规则时，复位该值
//            }
//        } else {
//            PreferencesUtils.putLong(context, Constant.UPGRADE_POP_PERIOD, DEFAULT_PERIOD);
//        }
//        if ("open".equals(config.getAuto_install().getStatus())) {
//            if (autoInstallTime != config.getAuto_install().getTime() * 1000) {
//                LogUtils.i("Loading", "保存自动装弹框时间");
//                PreferencesUtils.putLong(context, Constant.AUTO_INSTALL_PERIOD, config.getAuto_install().getTime() * 1000);
//                PreferencesUtils.putBoolean(context, Constant.AUTO_INSTALL_SHOWED, false);//后台有更改弹出规则时，复位该值
//            }
//        } else {
//            PreferencesUtils.putLong(context, Constant.AUTO_INSTALL_PERIOD, DEFAULT_PERIOD);
//        }
//        if ("open".equals(config.getSpread().getStatus())) {
//            PreferencesUtils.putBoolean(context, Constant.SELECTION_PLAY_POP_STATE, true);
//            if (null != config.getSpread() && spreadTime != config.getSpread().getTime() * 1000) {
//                LogUtils.i("Loading", "保存弹框推广时间");
//                PreferencesUtils.putLong(context, Constant.SPREAD_PERIOD, config.getSpread().getTime() * 1000);
//                PreferencesUtils.putBoolean(context, Constant.SELECTION_PLAY_SHOWED, false);//后台有更改弹出规则时，复位该值
//            }
//        } else {
//            PreferencesUtils.putBoolean(context, Constant.SELECTION_PLAY_POP_STATE, false);
//        }
////        PreferencesUtils.putString(context, Constant.BAIDU_STATUS, config.getBaidu_ads().getStatus());
//        if ("open".equals(config.getBaidu_ads().getStatus())) {
//            if (null != config.getBaidu_ads() && gdtTime != config.getBaidu_ads().getTime() * 1000) {
//                PreferencesUtils.putLong(context, Constant.BAIDU_PERIOD, config.getBaidu_ads().getTime() * 1000);
//                PreferencesUtils.putBoolean(context,Constant.BAIDU_STATUS,true);
//            }
//        } else {
//            PreferencesUtils.putLong(context, Constant.BAIDU_PERIOD, DEFAULT_PERIOD);
//            PreferencesUtils.putBoolean(context,Constant.BAIDU_STATUS,false);
//        }
//
//        LogUtils.i("Loading", "百度开屏广告开关：" + config.getBaidu_ads().getStatus());
////        PreferencesUtils.putString(context, Constant.GDT_STATUS, config.getGuangdiantong_ads().getStatus());
//        if ("open".equals(config.getGuangdiantong_ads().getStatus())) {
//            if (null != config.getGuangdiantong_ads() && gdtTime != config.getGuangdiantong_ads().getTime() * 1000) {
//                PreferencesUtils.putLong(context, Constant.GDT_PERIOD, config.getGuangdiantong_ads().getTime() * 1000);
//                PreferencesUtils.putBoolean(context,Constant.GDT_STATUS,true);
//            }
//        } else {
//            PreferencesUtils.putLong(context, Constant.GDT_PERIOD, DEFAULT_PERIOD);
//            PreferencesUtils.putBoolean(context,Constant.GDT_STATUS,false);
//        }
//        if (config.getThird_party_wk_app() != null) {
////            Constant.WK_SWITCH = config.getThird_party_wk_app().getStatus(); //保存玩咖曝光开关
//            if (config.getThird_party_wk_app().getStatus().equals("open")) {
//                PreferencesUtils.putBoolean(context,Constant.WK_SWITCH,true);
//            } else {
//                PreferencesUtils.putBoolean(context,Constant.WK_SWITCH,false);
//            }
//        }

//         浮层广告配置保存
//        if (null != config.getFloating_ads() && "open".equals(config.getFloating_ads().getStatus())) {
//            PreferencesUtils.putBoolean(context, SharePreferencesKey.NEED_SHOW_FLOAT_AD, true);
//            if (floatPeriodTime != config.getFloating_ads().getTime() * 1000) {
//                PreferencesUtils.putLong(context, SharePreferencesKey.FLOAT_AD_PERIOD_TIME, config.getFloating_ads().getTime() * 1000);
//            }
//        } else {
//            PreferencesUtils.putBoolean(context, SharePreferencesKey.NEED_SHOW_FLOAT_AD, false);
//        }
        //拉活开关
//        WaKeLog.i("启动页获取后台拉活配置开关状态:" + config.getApp_pulllive().getStatus());
//        Configure configure = new Configure(config.getApp_pulllive().getStatus());
//        PersistUtil.persistData(configure, Constant.PULLLIVE_STATUS);
    }

    private static void saveAutoUpgradeAppConfigInfo(ConfigureBean config, Context context) {
        if (null == config.getAuto_upgrade()) {
//            SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, 0L);
//            SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH, false);
            PreferencesUtils.putBoolean(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH, false);
            PreferencesUtils.putLong(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, 0L);
            return;
        }

        // 自动升级弹窗开关
        String autoUpgradeDialogSwitch = config.getAuto_upgrade().getStatus();
        boolean isOpen = false;
        if (autoUpgradeDialogSwitch.equals("open")) {
            isOpen = true;
        } else if (autoUpgradeDialogSwitch.equals("close")) {
            isOpen = false;
        }
//        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH, isOpen);
        PreferencesUtils.putBoolean(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_SWITCH, isOpen);

        // 应用自动升级间隔时间
//        long autoUpgradeJianGeTime = (long) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, 0L);
        long autoUpgradeJianGeTime = PreferencesUtils.getLong(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, 0L);

        if (autoUpgradeJianGeTime != config.getAuto_upgrade().getTime()) {
//            SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, config.getAuto_upgrade().getTime());
            PreferencesUtils.putLong(context, SharePreferencesKey.AUTO_UPGRADE_DIALOG_JIAN_GE, config.getAuto_upgrade().getTime());
        }
    }

    /***
     * 弹窗推广时间
     *
     * @param context
     * @return
     */
    public static boolean needShowFloatAdDialog(Context context) {
        boolean needShowFloat = PreferencesUtils.getBoolean(context, SharePreferencesKey.NEED_SHOW_FLOAT_AD, false);
        if (needShowFloat) {
            LogUtils.i(LogTAG.floatAdTAG, "浮层广告开关是 开");

            long lastTime = PreferencesUtils.getLong(context, SharePreferencesKey.FLOAT_AD_LAST_SHOW_TIME, 0);
            long periodTime = PreferencesUtils.getLong(context, SharePreferencesKey.FLOAT_AD_PERIOD_TIME, DEFAULT_PERIOD);

            LogUtils.i(LogTAG.floatAdTAG, "最后一次浮层广告时间 = " + TimeUtils.getStringToDateHaveHour(lastTime) + ", 当前时间 = " + TimeUtils.getStringToDateHaveHour(System.currentTimeMillis()) + ", 间隔时间参数 = " + periodTime);

            boolean canShow = needShow(lastTime, periodTime, lastTime != 0);

            if (canShow) {
                LogUtils.i(LogTAG.floatAdTAG, "满足时间间隔条件");
            } else {
                LogUtils.i(LogTAG.floatAdTAG, "不满足弹窗时间间隔条件");
            }

            return canShow;
        }

        LogUtils.i(LogTAG.floatAdTAG, "浮层广告开关是 关");
        return false;
    }

}
