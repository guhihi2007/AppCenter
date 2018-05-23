package cn.lt.android.notification;

import android.app.NotificationManager;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lt.android.notification.bean.PushBaseBean;
import cn.lt.android.notification.sender.DownloadNoticeSender;
import cn.lt.android.notification.sender.InstallNoticeSender;
import cn.lt.android.notification.sender.PushNoticeSender;
import cn.lt.android.notification.sender.UpgradeNoticeSender;
import cn.lt.android.service.CoreService;

/**
 * Created by 林俊生 on 2016/1/21.
 */
public class LTNotificationManager {
    private Context context;
    private static LTNotificationManager instance;
    private DownloadNoticeSender downloadNoticeSender;
    private InstallNoticeSender installNoticeSender;
    private UpgradeNoticeSender upgradeNoticeSender;
    private PushNoticeSender pushNoticeSender;

    private ExecutorService mThreadPool;

    public static LTNotificationManager getinstance() {
        if (instance == null) {
            synchronized (LTNotificationManager.class) {
                if (instance == null) {
                    instance = new LTNotificationManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        mThreadPool = Executors.newSingleThreadExecutor();
        if(downloadNoticeSender == null) {
            synchronized (LTNotificationManager.class) {
                if (downloadNoticeSender == null) {
                    downloadNoticeSender = new DownloadNoticeSender(context, mThreadPool);
                }
            }
        }

        if(installNoticeSender == null) {
            synchronized (LTNotificationManager.class) {
                if (installNoticeSender == null) {
                    installNoticeSender = new InstallNoticeSender(context, mThreadPool);
                }
            }
        }

        if(upgradeNoticeSender == null) {
            synchronized (LTNotificationManager.class) {
                if (upgradeNoticeSender == null) {
                    upgradeNoticeSender = new UpgradeNoticeSender(context, mThreadPool);
                }
            }
        }

        if(pushNoticeSender == null) {
            synchronized (LTNotificationManager.class) {
                if (pushNoticeSender == null) {
                    pushNoticeSender = new PushNoticeSender(context, mThreadPool);
                }
            }
        }

    }

    /**
     * 发送安装相关的通知
     *
     * @param packageName  应用包名
     */
    public void sendInstallCompleteNotice(String packageName) {
        // 应用安装成功
        installNoticeSender.sendInstallComplete(packageName);
    }


    /**
     * 发送APP升级相关的通知
     */
    public void sendUpgradeNotice() {
        upgradeNoticeSender.sendCanUpgradeNotice();
    }

    /**
     * 发送本平台升级的通知
     *
     * @param version 平台版本号
     */
    public void sendPlatformUpgrageNotice(String version) {
        upgradeNoticeSender.sendPlatformUpgrage(version);
    }

    /**
     * 发送本平台升级失败通知
     */
    public void sendPlatformUpgrageFailNotice() {
        upgradeNoticeSender.sendPlatformUpgrageFail();
    }

    /**
     * 发送本平台升级进度的通知
     *
     * @param progress 当前进度值
     * @param length   总进度
     */
    public void sendPlatformUpgrageProgressNotice(int progress, int length) {
        upgradeNoticeSender.sendPlatformUpgrageProgress(progress, length);
    }


    /**
     * 发送推送通知
     *
     * @param bean 推送实体
     */
    public void sendPushNotic(PushBaseBean bean) {
        pushNoticeSender.senPushNotice(bean);
    }

    /**
     * 取消所有通知
     */
    public void cancelAllNotice() {
//        downloadNoticeSender.clearALlDownloadNoticeList();
        ((NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    public void requestPushData(String pushId) {
        context.startService(CoreService.getPushIntent(context, pushId));
    }

}
