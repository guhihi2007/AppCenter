package cn.lt.android.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import cn.lt.android.db.AppEntity;
import cn.lt.android.notification.bean.PushBaseBean;
import cn.lt.android.service.NoticeIntentService;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/1/21.
 */
public abstract class BaseNotification {
    protected enum NoticBtnType {
        RETRY, INSTALL, OPEN, UPGRADE, GONE, PLATFORM_UPGRADE
    }

    protected NotificationCompat.Builder notifyBuilder;
    protected NotificationManager notifyManager;

    protected static final String AT_RETRY = "马上重试";

    protected static final String RETRY = "重试";
    protected static final String INSTALL = "安装";
    protected static final String OPEN = "打开";
    protected static final String UPGRADE = "升级";

    protected String app;
    protected Context context;

    protected RemoteViews remoteViews;
    private RemoteViews remoteView;

    public BaseNotification(Context context) {
        this.context = context;
        initNotifyBuilder();
    }

    private void initNotifyBuilder() {
        notifyBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_launcher);
        notifyBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);// 通知优先级（最大）
        notifyBuilder.setAutoCancel(true);
        notifyBuilder.setWhen(0L);
        remoteView = new RemoteViews(context.getPackageName(), R.layout.notificatin_normal_layout);
        remoteView.setImageViewBitmap(R.id.iv_noticImage, BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
    }


    protected Intent createIntent(Class clazz) {
        return new Intent(context, clazz);
    }

    protected PendingIntent getPendingIntentForActivity(Intent intent) {
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pendIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendIntent;
    }

    protected PendingIntent getPendingIntentForService(Intent intent) {
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent pendIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendIntent;
    }

    protected void setPendingIntent(PendingIntent pendingIntent) {
        notifyBuilder.setContentIntent(pendingIntent);
    }


    protected void notify(String appName, String title, String subTitle, int notifyId, NoticBtnType type) {
        notifyBuilder.setContentTitle(title);
        notifyBuilder.setContentText(subTitle);
        setNoticBtnType(type);
        setRemoteViewData(subAppName(appName), title, subTitle);
        notifyManager.notify(notifyId, notifyBuilder.build());
        notifyBuilder.setContentIntent(null);
    }

    private String subAppName(String appName) {
        if (appName != null && appName.length() > 6) {
            return appName.substring(0, 6) + "...";
        } else {
            return appName;
        }
    }


    private void setRemoteViewData(String appName, String title, String subTitle) {
        if (null == appName || "".equals(appName)) {
            remoteView.setViewVisibility(R.id.tv_noticAppName, View.GONE);
            remoteView.setViewPadding(R.id.tv_noticTitle, (int) context.getResources().getDimension(R.dimen.notic_title_padding_left), 0, 0, 0);
        } else {
            remoteView.setTextViewText(R.id.tv_noticAppName, appName);
            remoteView.setViewVisibility(R.id.tv_noticAppName, View.VISIBLE);
            remoteView.setViewPadding(R.id.tv_noticTitle, 0, 0, 0, 0);

        }
        remoteView.setTextViewText(R.id.tv_noticTitle, title);
        remoteView.setTextViewText(R.id.tv_noticSubTitle, subTitle);

        notifyBuilder.setContent(remoteView);
    }

    private void setNoticBtnStyle(int visibility, String text, int textColorId, int backgroundId) {
        if (visibility == View.GONE) {
            remoteView.setViewVisibility(R.id.tv_noticLabel, visibility);
        } else {
            remoteView.setViewVisibility(R.id.tv_noticLabel, visibility);
            remoteView.setTextViewText(R.id.tv_noticLabel, text);
            remoteView.setTextColor(R.id.tv_noticLabel, context.getResources().getColor(textColorId));
            remoteView.setInt(R.id.tv_noticLabel, "setBackgroundResource", backgroundId);
        }

    }

    private void setNoticBtnType(NoticBtnType type) {
        if (type == NoticBtnType.PLATFORM_UPGRADE) {
            setNoticBtnStyle(View.VISIBLE, "升级", R.color.notification_app_name, R.drawable.btn_polatform_upgrade_bg);
        } else if (type == NoticBtnType.INSTALL) {
            setNoticBtnStyle(View.VISIBLE, "安装", R.color.notification_text_install, R.drawable.btn_install_bg);
        } else if (type == NoticBtnType.OPEN) {
            setNoticBtnStyle(View.VISIBLE, "打开", R.color.notification_text_open, R.drawable.btn_open_bg);
        } else if (type == NoticBtnType.RETRY) {
            setNoticBtnStyle(View.VISIBLE, "重试", R.color.notification_text_retry, R.drawable.btn_retry_bg);
        } else if (type == NoticBtnType.UPGRADE) {
            setNoticBtnStyle(View.VISIBLE, "升级", R.color.notification_text_upgrade, R.drawable.btn_upgrade_bg);
        } else if (type == NoticBtnType.GONE) {
            setNoticBtnStyle(View.GONE, "", 0, 0);
        }
    }

    protected String budileMultiString(int count) {
        return count + "款应用";
    }

    /**
     * 撤销 应用通知
     *
     * @param app 应用实体
     */
    public void cancelAppNotice(AppEntity app) {
        cancelNotice(parseId(app));
    }


    protected void cancelNotice(int noticeId) {
        notifyManager.cancel(noticeId);
    }

    protected int parseId(AppEntity entity) {
        if (entity.isAdData()) {
            int hash = entity.getPackageName().hashCode();
            return -hash;
        }

        return Integer.valueOf(entity.getId() + "");

    }

    protected int parseId(PushBaseBean bean) {
        return Integer.parseInt(bean.getId());
    }

    protected int parseId(long id) {
        return (int) id;
    }

    /**
     * 设置跳转到应用下载管理页面的Intent
     */
    protected void setGoDownloadPagePendingIntent(int jumpType, String appId) {
        setPendingIntent(getPendingIntentForService(createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByStartTaskManager).
                putExtra(NoticeConsts.noticeStartType, jumpType).
                putExtra("appId", appId)));
    }

    protected void setGoDownloadPageAndRedownPendingIntent(AppEntity app) {
        setPendingIntent(getPendingIntentForService(createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByDownloadFault).
                putExtra("appEntity", app).
                putExtra("appPkg", app.getPackageName()).
                putExtra("appId", app.getAppClientId())));
    }

    private void startByLoadingActivity(int jumpType) {

    }

    private void directStart() {
        setPendingIntent(getPendingIntentForService(createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByStartTaskManager)));
    }

}
