package cn.lt.android.notification;


import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.wanka.WanKa;
import cn.lt.android.main.LoadingActivity;
import cn.lt.android.notification.bean.PushAwakeBean;
import cn.lt.android.notification.bean.PushBaseBean;
import cn.lt.android.notification.bean.PushGameBean;
import cn.lt.android.notification.bean.PushH5Bean;
import cn.lt.android.notification.bean.PushPlatUpgradeBean;
import cn.lt.android.notification.bean.PushSoftwareBean;
import cn.lt.android.notification.bean.PushTopicBean;
import cn.lt.android.plateform.update.PlatUpdateAction;
import cn.lt.android.plateform.update.PlatUpdateService;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.service.NoticeIntentService;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/1/21.
 * <p/>
 * 用于发送推送相关通知
 */
public class PushNotification extends BaseNotification {
    private int id;
    private String title;
    private String subTitle;
    private String noticeStyle;
    private String appDetailType;
    private String appId;
    private String reportData;

    public static final String defaule = "1";// 默认无图
    public static final String smallPhoto = "2";// 小图
    public static final String bigPhoto = "3";// 大图

    private NotificationCompat.Builder pushBuilder;
    private RemoteViews pushRemoteView;
    private String h5Url;// h5链接
    private String pushId;

    public PushNotification(Context context) {
        super(context);
    }

    /**
     * 发送APP推送通知
     */
    public void sendPushAppNotification(PushBaseBean bean, final Bitmap icon, final Bitmap image) {
        initNotification();
        setBaseData(bean);
        pushBuilder.setLargeIcon(icon);//设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
        LogUtils.i(LogTAG.PushTAG, "isGame: id = " + id);
        setGoLoadingActivityPendingIntent(createPushAppBundle());

        if (noticeStyle.equals(defaule)) {
            notify(title, subTitle, id);
        } else {
            setImageAndNotify(image);
        }

        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recycleBitmap(icon);
                recycleBitmap(image);
            }
        }, 1000);

    }

    /**
     * 发送专题推送通知
     */
    public void sendPushTopicNotification(PushTopicBean topic, final Bitmap icon, final Bitmap image) {
        initNotification();
        setBaseData(topic);
        LogUtils.i(LogTAG.PushTAG, "isTopice");
        pushBuilder.setLargeIcon(icon);//设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
        setGoLoadingActivityPendingIntent(createPushTopicBundle());

        if (noticeStyle.equals(defaule)) {
            LogUtils.i(LogTAG.PushTAG, "没有图片，默认样式，直接发出通知~end");
            notify(title, subTitle, id);
        } else {
            setImageAndNotify(image);
        }

        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recycleBitmap(icon);
                recycleBitmap(image);
            }
        }, 1000);

    }

    // 如果传了图片，设置显示图片并发出通知
    private void setImageAndNotify(Bitmap image) {
        // 小图
        if (noticeStyle.equals(smallPhoto)) {
            LogUtils.i(LogTAG.PushTAG, "设置 小图 RemoteViews,并发出通知~end");
            pushRemoteView = getOnlyPictureStyle(image);
            if (pushRemoteView != null) {
                pushBuilder.setContent(pushRemoteView);
            }
            notify(title, subTitle, id);
        }

        // 大图
        if (noticeStyle.equals(bigPhoto)) {
            if (android.os.Build.VERSION.SDK_INT > 16) {
                LogUtils.i(LogTAG.PushTAG, "设置 大图 RemoteViews,并发出通知~end");
                try {
                    pushRemoteView = getOnlyPictureStyle(image);
                    pushBuilder.setContentTitle(title);
                    pushBuilder.setContentText(subTitle);
                    Notification notification = pushBuilder.build();
                    notification.bigContentView = pushRemoteView;
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    notifyManager.notify(id, notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 发送平台升级推送通知
     */
    public void sendPlatformUpgradeNotification(PushPlatUpgradeBean upgradeBean, final Bitmap noticeIcon) {
        initNotification();
        setBaseData(upgradeBean);
        pushBuilder.setLargeIcon(noticeIcon);//设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
        LogUtils.i(LogTAG.PushTAG, "isPlatformUpgrade");
        String pushChannel = upgradeBean.getApp_channel();
        checkNeedUpgrade(pushChannel, new BitmapRecycleCallback() {
            @Override
            public void recycle() {
                recycleBitmap(noticeIcon);// 回收图片资源
            }
        });

    }

    // 联网检测平台是否需要升级
    private void checkNeedUpgrade(final String pushChannel, final BitmapRecycleCallback callback) {
        LogUtils.i(LogTAG.PushTAG, "准备联网检测是否需要升级！");
//        DCStat.platUpdateEvent("request", "notification", UpdateUtil.getPlatUpgradeType(), "", "");//通知栏发出检查版本更新请求上报
        VersionCheckManger.getInstance().checkVerison(new VersionCheckManger.VersionCheckCallback() {
            @Override
            public void callback(Result result, VersionInfo info) {
                if (result == Result.have) {
                    LogUtils.i(LogTAG.PushTAG, "有新版本，需要升级！版本号");
                    String channelName = GlobalConfig.CHANNEL;

                    LogUtils.i(LogTAG.PushTAG, "本地 channel = " + channelName);
                    LogUtils.i(LogTAG.PushTAG, "服务器 channel = " + pushChannel);

                    if (pushChannel.equalsIgnoreCase("all") || pushChannel.equalsIgnoreCase(channelName)) {

                        platUpGrade();
                        DCStat.pushEvent(pushId, "platUpgrade", "received", "GETUI", "");//统计推送到达
                        LogUtils.i(LogTAG.PushTAG, "成功发出版本升级通知了~");
                    } else {
                        LogUtils.i(LogTAG.PushTAG, "传来的channel与本地不一致，发出版本升级通知失败！");
                    }
                } else {
                    LogUtils.i(LogTAG.PushTAG, "没有新版本升级哦！！");
                }

                if (callback != null) {
                    callback.recycle();// 回收图片资源
                }
            }

        }, true);
    }

    // 发出升级通知
    private void platUpGrade() {
        setPlatGoUpgradePendingIntent();
        notify(title, subTitle, NoticeConsts.PLATFORM_UPGRAGE_NOTIFY_ID);
    }

    public void sendPushH5Notification(PushH5Bean H5Bean, Bitmap icon, Bitmap image) {
        initNotification();
        setBaseData(H5Bean);
        LogUtils.i(LogTAG.PushTAG, "isH5");
        pushBuilder.setLargeIcon(icon);//设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
        setGoLoadingActivityPendingIntent(createPushH5Bundle());
        LogUtils.i(LogTAG.PushTAG, "点击");
        if (noticeStyle.equals(defaule)) {
            LogUtils.i(LogTAG.PushTAG, "没有图片，默认样式，直接发出通知~end");
            notify(title, subTitle, id);
        } else {
            setImageAndNotify(image);
        }

        recycleBitmap(icon);
        recycleBitmap(image);
    }

    /***
     * 沉默应用拉活通知处理
     * @param awakeBean
     * @param icon
     * @param image
     */
    public void sendPushAwakeNotification(PushAwakeBean awakeBean, Bitmap icon, Bitmap image) {
        initNotification();
        LogUtils.i(LogTAG.PushTAG, "isAwake");
        pushBuilder.setLargeIcon(icon);
        noticeStyle = awakeBean.getNotice_style();
        setJumpOtherAppIntent(awakeBean);
        if (noticeStyle.equals(defaule)) {
            LogUtils.i(LogTAG.PushTAG, "没有图片，默认样式，直接发出通知~end");
            notify(awakeBean.getMain_title(), awakeBean.getSub_title(), parseId(awakeBean));
        } else {
            setImageAndNotify(image);
        }
        recycleBitmap(icon);
        recycleBitmap(image);
    }

    // 初始化通知
    private void initNotification() {
        try {
            pushBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_launcher);
            pushBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
            pushBuilder.setPriority(NotificationCompat.PRIORITY_MAX);// 通知优先级（最大）
            pushBuilder.setAutoCancel(true);
            pushBuilder.setWhen(0L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBaseData(PushBaseBean bean) {
        id = parseId(bean);
        title = bean.getMain_title();
        subTitle = bean.getSub_title();
        pushId = bean.pushId;

        if (bean instanceof PushSoftwareBean) {
            LogUtils.i(LogTAG.PushTAG, "是软件推送");
            PushSoftwareBean software = (PushSoftwareBean) bean;
            noticeStyle = software.getNotice_style();
            appDetailType = software.type;
            appId = software.getApp().getAppClientId();
            if (software.getApp().getReportData() != null) {
                reportData = software.getApp().getReportData().toString();
            }
        }

        if (bean instanceof PushGameBean) {
            LogUtils.i(LogTAG.PushTAG, "是游戏推送");
            PushGameBean game = (PushGameBean) bean;
            noticeStyle = game.getNotice_style();
            appDetailType = game.type;
            appId = game.getApp().getAppClientId();
            if (game.getApp().getReportData() != null) {
                reportData = game.getApp().getReportData().toString();
            }
        }

        if (bean instanceof PushTopicBean) {
            noticeStyle = bean.getNotice_style();
        }

        if (bean instanceof PushH5Bean) {
            PushH5Bean h5Bean = (PushH5Bean) bean;
            noticeStyle = h5Bean.getNotice_style();
            h5Url = h5Bean.getH5();
        }
    }

    private void setGoLoadingActivityPendingIntent(Bundle bundle) {
        pushBuilder.setContentIntent(getPendingIntentForActivity(createIntent(LoadingActivity.class).
                setAction(Intent.ACTION_MAIN).
                addCategory(Intent.CATEGORY_LAUNCHER).
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
                putExtra(Constant.EXTRA_CLICK_FROM_NOTICE, true).
                putExtra(NoticeConsts.pushBundle, bundle)));
    }

    /**
     * 启动平台下的PendingIntent
     */
    private void setPlatGoUpgradePendingIntent() {
        pushBuilder.setContentIntent(getPendingIntentForService(createIntent(PlatUpdateService.class).
                setAction(PlatUpdateAction.SERVICE_START_ACTION).
                putExtra(PlatUpdateAction.ACTION, PlatUpdateAction.ACTION_NOTIFICATION).
                putExtra(Constant.EXTRA_PUSH_ID, pushId).
                putExtra(NoticeConsts.isPush, "yes")));// 发现传布尔值会收不到，所以暂时用String
    }


    private RemoteViews getOnlyPictureStyle(Bitmap image) {
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.layout_push_image);
        remoteView.setImageViewBitmap(R.id.iv_pushImage, image);
        return remoteView;
    }

    private void notify(String title, String subTitle, int notifyId) {
        pushBuilder.setContentTitle(title);
        pushBuilder.setContentText(subTitle);
        notifyManager.notify(notifyId, pushBuilder.build());
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private Bundle createPushAppBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(NoticeConsts.isPush, true);
        bundle.putBoolean(NoticeConsts.isPushAPP, true);
        bundle.putString(Constant.EXTRA_TYPE, appDetailType);
        bundle.putString(Constant.EXTRA_ID, appId);
        bundle.putString(Constant.EXTRA_PUSH_ID, pushId);
        bundle.putString(WanKa.KEY_REPORT_DATA, reportData);
        return bundle;
    }

    private Bundle createPushTopicBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(NoticeConsts.isPush, true);
        bundle.putBoolean(NoticeConsts.isPushTopic, true);
        bundle.putString(Constant.EXTRA_TITLE, "");
        bundle.putString(Constant.EXTRA_ID, String.valueOf(id));
        bundle.putString(Constant.EXTRA_PUSH_ID, pushId);
        return bundle;
    }

    private Bundle createPushH5Bundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(NoticeConsts.isPush, true);
        bundle.putBoolean(NoticeConsts.isPushH5, true);
        bundle.putString(Constant.EXTRA_TITLE, title);
        bundle.putString(Constant.EXTRA_URL, h5Url);
        bundle.putString(Constant.EXTRA_PUSH_ID, pushId);
        return bundle;
    }

    // 图片回收回调接口
    private interface BitmapRecycleCallback {
        void recycle();
    }

    protected void setJumpOtherAppIntent(PushAwakeBean bean) {
        pushBuilder.setContentIntent(getPendingIntentForService(createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).putExtra(NoticeConsts.jumpBy, NoticeConsts.wakeAppType).putExtra("push_id", bean.getId()).putExtra("url", bean.getPulladdress())));

    }
}
