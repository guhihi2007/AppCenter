package cn.lt.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.text.TextUtils;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.request.WDJRequester;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.install.InstallManager;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;

import static cn.lt.android.LTApplication.loopInstalledList;


/**
 * Created by wenchao on 2016/1/19.
 * 安装/卸载事件监听
 */
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        final String packageName = intent.getData().getSchemeSpecificPart();
        LogUtils.i("InstallReceiver", "开始发广播，包名==" + packageName);
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            //安装包
            LogUtils.i("InstallReceiver", System.currentTimeMillis() + "安装完成==进了广播" + packageName);
            // 豌豆荚的安装完成回传上报(暂时未用到注释掉 by honaf)
            //reportWDJ(packageName);
            InstallManager.getInstance().removeInstallingApp(packageName);
            // 发送应用安装完成的通知（通知栏的）
            LTNotificationManager.getinstance().sendInstallCompleteNotice(packageName);
            // 发送应用可升级的通知（通知栏的）
            LTNotificationManager.getinstance().sendUpgradeNotice();
            EventBus.getDefault().post(Constant.CORNER_COUNT);
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!loopInstalledList.remove(packageName)) {
                        LogUtils.i("InstallReceiver", System.currentTimeMillis() + "6s后安装完成==执行了广播" + packageName);
                        // 发送安装完成通知
                        DCStat.installSuccessEvent(packageName);
                        UpgradeListManager.getInstance().remove(packageName);
                        deleteApk(packageName);
                        postEvent(packageName, InstallEvent.INSTALLED_ADD);
                        // 安装时签名不一致的，重新安装成功后需重记录列表中移除
                        if (InstallManager.getInstance().isSignError(packageName)) {
                            InstallManager.getInstance().removeSignErrorList(packageName);
                        }
                    }
                }
            }, 6000);

        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            //卸载包
            LogUtils.i("InstallReceiver", System.currentTimeMillis() + "卸载" + packageName);
            postEvent(packageName, InstallEvent.UNINSTALL);
            UpgradeListManager.getInstance().remove(packageName);
            boolean isSignError = InstallManager.getInstance().isSignError(packageName);
            if (isSignError) {
                installAgain(packageName);
            } else {
//                LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        LogUtils.i("InstallReceiver", "删除统计表数据" + packageName);
//                        DCStat.appUninstall(packageName);//如果是升级的应用广播会先走卸载再走安装完成，这样就会把统计表数据删除导致升级安装完成的数据上报出现问题,所以需要延时，待安装完成上报成功再删除统计表中的数据
//                    }
//                }, 9000);
            }
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            LogUtils.i("InstallReceiver", System.currentTimeMillis() + "替换完成");
            //更新包
            if ("cn.lt.appstore".equals(packageName)) {
                //版本升级完成上报数据
                String mode = PreferencesUtils.getString(LTApplication.shareApplication(), "installMode");
                DCStat.platUpdateEvent("installed", "", UpdateUtil.getPlatUpgradeType(), TextUtils.isEmpty(mode) ? "" : mode, "", "");
                PreferencesUtils.putString(LTApplication.shareApplication(), "installMode", "");
            }
            postEvent(packageName, InstallEvent.INSTALLED_UPGRADE);
            deleteApk(packageName);
            UpgradeListManager.getInstance().remove(packageName);
            if ("cn.lt.appstore".equals(packageName)) {
                LogUtils.i("UpdateService", "----------干掉进程----"+ GlobalConfig.versionName);  //reason:升级后复用老版本代码
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }

    }

    private void installAgain(String packageName) {
        AppEntity app = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);
        if (app != null) {
            InstallManager.getInstance().start(app, "singErrorAutoInstallAgain", "", false);
        }
    }

    /**
     * 豌豆荚的安装完成回传上报
     */
    private void reportWDJ(String packageName) {
        AppEntity app = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);

        if (app != null && app.getAdMold().equals(AdMold.WanDouJia)) {
            WDJRequester.wdjInstalledReport(app);
        }
    }

    void postEvent(String packageName, int status) {
        EventBus.getDefault().post(new InstallEvent(packageName, status));
    }

    /**
     * 安装完成删除apk
     */
    void deleteApk(String packageName) {
        try {
            DownloadTaskManager.getInstance().remove(packageName, true);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO:
        }
    }
}
