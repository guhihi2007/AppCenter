package cn.lt.android.notification;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import cn.lt.android.db.AppEntity;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.plateform.update.PlatUpdateAction;
import cn.lt.android.plateform.update.PlatUpdateService;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.DCStatIdJoint;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/1/21.
 * <p/>
 * 用于发送应用升级通知
 */
public class UpgradeNotification extends BaseNotification {

    private static final String CAN_UPGRADE = "可升级";
    private static final String AT_UPGRADE = "马上升级";
    private static final String APP_CENTER_IS_DOWNLOADING = "应用市场正在下载";

    private static final String PLATFORM_UPGRADE_FAIL = "应用市场升级失败";

    private NotificationCompat.Builder platformUpgradeNotifyBuilder;

    public UpgradeNotification(Context context) {
        super(context);

        initBuilder();
    }

    private void initBuilder() {
        platformUpgradeNotifyBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_launcher);
        platformUpgradeNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        platformUpgradeNotifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);// 通知优先级（最大）
        platformUpgradeNotifyBuilder.setAutoCancel(true);
    }

    /**
     * 单个应用升级通知
     */
    public void singleCanUpgrade(AppEntity app) {
        setGoDownloadPagePendingIntent(NoticeConsts.goDownloadTabByUpgrade, app.getAppClientId());
        notify(app.getName(), CAN_UPGRADE, AT_UPGRADE, parseId(app), NoticBtnType.UPGRADE);
    }

    /**
     * 多个应用升级通知
     */
    public void multiUpgrade(int upgradeCount) {
        // 拼接要上报的列表id
        String appIds = DCStatIdJoint.jointIdByAppDetailBean(UpgradeListManager.getInstance().getUpgradeAppList());

        setGoDownloadPagePendingIntent(NoticeConsts.goDownloadTabByUpgrade, appIds);
        notify(null, budileMultiString(upgradeCount) + CAN_UPGRADE, AT_UPGRADE, NoticeConsts.MUTLI_UPGRADE_NOTIFY_ID, NoticBtnType.UPGRADE);
    }

    /**
     * 撤销 多个应用可升级 通知
     */
    public void cancelMultiCanUpgradeNotice() {
        cancelNotice(NoticeConsts.MUTLI_UPGRADE_NOTIFY_ID);
    }

    /**
     * 平台升级
     *
     * @param version 版本号
     */
    public void platformUpgrage(String version) {
        setPlatGoUpgradePendingIntent();
        notify(null, "发现新版本，请升级！", "v" + version, NoticeConsts.PLATFORM_UPGRAGE_NOTIFY_ID, NoticBtnType.PLATFORM_UPGRADE);
        DCStat.pushEvent("", "platUpgrade", "received", "APP", "");
    }

    /**
     * 平台升级完成
     */
    public void platformUpgrageComplete() {
        setPlatUpgradeCompletePendingIntent();
        notify("应用市场", "下载完成", "马上安装", NoticeConsts.PLATFORM_UPGRAGE_COMPLETE_NOTIFY_ID, NoticBtnType.INSTALL);
//        DCStat.platUpdateEvent("downloaded", "Notification", UpdateUtil.getPlatUpgradeType(), "", "", "");
    }

    /**
     * 平台升级失败
     */
    public void platformUpgrageFail() {
        setPlatUpgradeCompletePendingIntent();
        notify(null, PLATFORM_UPGRADE_FAIL, AT_RETRY, NoticeConsts.PLATFORM_UPGRAGE_FAIL_NOTIFY_ID, NoticBtnType.RETRY);
//        DCStat.platUpdateEvent("download_error", "Notification", UpdateUtil.getPlatUpgradeType(), "", "", "");
    }

    /**
     * 平台升级进度
     */
    public void platformUpgrageProgress(int progress, int length) {
        if (progress < length) {
            platformUpgradeNotifyBuilder.setContentTitle(APP_CENTER_IS_DOWNLOADING);
            platformUpgradeNotifyBuilder.setProgress(length, progress, false);
            notifyManager.notify(NoticeConsts.PLATFORM_UPGRAGE_PROGRESS_NOTIFY_ID, platformUpgradeNotifyBuilder.build());
        } else {
            cancelPlatformUpgrageProgressNotice();
            platformUpgrageComplete();
        }
    }

    /**
     * 撤销 平台升级 通知
     */
    public void cancelPlatformUpgrageNotice() {
        cancelNotice(NoticeConsts.PLATFORM_UPGRAGE_NOTIFY_ID);
    }

    /**
     * 撤销 平台升级失败的 通知
     */
    public void cancelPlatformUpgrageFailNotice() {
        cancelNotice(NoticeConsts.PLATFORM_UPGRAGE_FAIL_NOTIFY_ID);
    }

    /**
     * 撤销 平台升级进度 通知
     */
    public void cancelPlatformUpgrageProgressNotice() {
        cancelNotice(NoticeConsts.PLATFORM_UPGRAGE_PROGRESS_NOTIFY_ID);
    }

    /**
     * 启动平台下的PendingIntent(弹出升级通知栏)
     */
    private void setPlatGoUpgradePendingIntent() {
        setPendingIntent(getPendingIntentForService(createIntent(PlatUpdateService.class).
                setAction(PlatUpdateAction.SERVICE_START_ACTION).
                putExtra(PlatUpdateAction.ACTION, PlatUpdateAction.ACTION_NOTIFICATION).
                putExtra(NoticeConsts.isPush, "no")));// 发现传布尔值会收不到，所以暂时用String
    }

    /**
     * 平台下载完的PendingIntent
     */
    private void setPlatUpgradeCompletePendingIntent() {
        setPendingIntent(getPendingIntentForService(createIntent(PlatUpdateService.class).
                setAction(PlatUpdateAction.SERVICE_START_ACTION).
                putExtra(PlatUpdateAction.ACTION, PlatUpdateAction.ACTION_NOTIFICATION).
                putExtra("retryUpgrade", true)));
    }

}
