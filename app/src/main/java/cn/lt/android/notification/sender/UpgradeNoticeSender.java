package cn.lt.android.notification.sender;

import android.content.Context;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.notification.UpgradeNotification;

/**
 * Created by LinJunSheng on 2016/3/7.
 */
public class UpgradeNoticeSender {
    private UpgradeNotification upgradeNotification;
    private ExecutorService mThreadPool;

    public UpgradeNoticeSender(Context context, ExecutorService mThreadPool) {
        this.mThreadPool = mThreadPool;
        upgradeNotification = new UpgradeNotification(context);
    }

    /**
     * 发送应用可升级通知
     */
    public void sendCanUpgradeNotice() {
        // 没有可升级，取消所有跟应用升级相关的通知
        if (getCanUpgradeCount() == 0) {
            upgradeNotification.cancelMultiCanUpgradeNotice();
        }

        // 有一个可升级
        if (getCanSendUpgradeNoticList().size() == 1) {
            AppDetailBean bean = getCanSendUpgradeNoticList().get(0);
            if (bean != null) {
                try {
                    DownloadTaskManager.getInstance().transfer(bean);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return; // TODO
                }
                singleCanUpgrade(bean.getDownloadAppEntity());
            }
        }

        // 多个可升级
        if (getCanUpgradeCount() > 1) {
            multiUpgrade();
        }
    }

    /**
     * 发送多个应用可升级通知
     */
    public void sendMultiCanUpgradeNotice() {
        multiUpgrade();
    }

    /**
     * 平台升级通知
     *
     * @param version 版本号
     */
    public void sendPlatformUpgrage(final String version) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                upgradeNotification.cancelPlatformUpgrageFailNotice();
                upgradeNotification.platformUpgrage(version);
            }
        });
    }

    /**
     * 平台升级失败通知
     */
    public void sendPlatformUpgrageFail() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                upgradeNotification.cancelPlatformUpgrageNotice();
                upgradeNotification.cancelPlatformUpgrageProgressNotice();
                upgradeNotification.platformUpgrageFail();
            }
        });
    }

    /**
     * 平台升级进度通知
     *
     * @param progress 进度
     * @param length   总数
     */
    public void sendPlatformUpgrageProgress(final int progress, final int length) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                upgradeNotification.cancelPlatformUpgrageNotice();
                upgradeNotification.cancelPlatformUpgrageFailNotice();
                upgradeNotification.platformUpgrageProgress(progress, length);
            }
        });
    }

    private void multiUpgrade() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                upgradeNotification.multiUpgrade(getCanUpgradeCount());
            }
        });
    }

    private void singleCanUpgrade(final AppEntity app) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                upgradeNotification.cancelMultiCanUpgradeNotice();
                upgradeNotification.singleCanUpgrade(app);
            }
        });
    }

    private int getCanUpgradeCount() {
        int upgradeCount = 0;
        List<AppDetailBean> apps = UpgradeListManager.getInstance().getUpgradeAppList();
        for (int i = 0; i < apps.size(); i++) {
            if (!DownloadTaskManager.getInstance().isInTask(apps.get(i).getPackage_name())) {
                upgradeCount++;
            }
        }
        return upgradeCount;
    }

    private List<AppDetailBean> getCanSendUpgradeNoticList() {
        List<AppDetailBean> list = new ArrayList<>();
        List<AppDetailBean> apps = UpgradeListManager.getInstance().getUpgradeAppList();

        for (int i = 0; i < apps.size(); i++) {
            if (!DownloadTaskManager.getInstance().isInTask(apps.get(i).getPackage_name())) {
                list.add(apps.get(i));
            }
        }
        return list;
    }


}
