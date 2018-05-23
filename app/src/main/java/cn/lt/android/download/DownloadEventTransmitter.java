package cn.lt.android.download;

import android.os.RemoteException;

import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;
import cn.lt.download.DownloadAgent;
import cn.lt.download.DownloadEventBusManager;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.listener.DownloadEventListener;
import cn.lt.download.model.DownloadInfo;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/1/26.
 * 下载监听器,发布下载状态信息
 */
public class DownloadEventTransmitter extends DownloadEventListener {

    private static final String TAG = "EventTransmitter";

    private static DownloadEventTransmitter instance = new DownloadEventTransmitter();

    public static DownloadEventTransmitter getInstance() {
        return instance;
    }

    private DownloadEventTransmitter() {
        String processName = LTApplication.shareApplication().getProcessName();
        LogUtils.i(TAG, "init eventtransmitter " + processName);
        if (processName.equals("cn.lt.appstore")) {
            DownloadEventBusManager.getEventBus().addListener(DownloadInfoEvent.ID, this);
        }
    }

    @Override
    protected synchronized void pending(DownloadInfoEvent event) {
        DownloadInfo m = event.getDownloadInfo();
        DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.pending, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
        EventBus.getDefault().post(downloadEvent);
    }


    @Override
    protected synchronized void progress(DownloadInfoEvent event) {
        DownloadInfo m = event.getDownloadInfo();
        DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.progress, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
        EventBus.getDefault().post(downloadEvent);
        LogUtils.i(TAG, "progress " + downloadEvent);
    }

    @Override
    protected void blockComplete(DownloadInfoEvent event) {
        DownloadInfo m = event.getDownloadInfo();
        // 先注释掉，解决下载到100%时，按钮无法显示“安装”状态
        DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.blockComplete, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
        EventBus.getDefault().post(downloadEvent);
        LogUtils.i(TAG, "blockComplete " + downloadEvent);
    }

    @Override
    protected synchronized void completed(DownloadInfoEvent event) {
        DownloadInfo m = event.getDownloadInfo();
        DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.completed, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
        EventBus.getDefault().post(downloadEvent);

        // 启动安装
        String pkg = event.getDownloadInfo().getPackageName();
        AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(pkg);
        if (appEntity != null) {
            if (!AppUtils.apkIsNotExist(appEntity.getSavePath())) {
                if (GlobalConfig.canRootInstall(LTApplication.shareApplication()) || PackageUtils.isSystemApplication(LTApplication.shareApplication())) {  //如果是root装就不让点击。
                    InstallManager.getInstance().addInstallingApp(pkg);
                }
                InstallManager.getInstance().start(appEntity, "", "", false);
//            appEntity.lastClickTime= System.currentTimeMillis(); //源头控制 安装点击1秒内无效（同bean绑定）
            }
        }
    }

    @Override
    protected synchronized void paused(DownloadInfoEvent event) {
        LogUtils.i("");
        AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(event.getDownloadInfo().getPackageName());
        if (appEntity == null) {
            AppEntity app = new AppEntity();
            app.setPackageName(event.getDownloadInfo().getPackageName());
            EventBus.getDefault().post(new RemoveEvent(app, true));

        } else {
            DownloadInfo m = event.getDownloadInfo();
            DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.paused, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
            EventBus.getDefault().post(downloadEvent);
        }


    }

    @Override
    protected synchronized void error(DownloadInfoEvent event, Throwable e) {
        if (StorageSpaceDetection.getAvailableSize() / (1048 * 1024) < 1) {    //下载过程中变重试后还是要弹框
            LTApplication.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    StorageSpaceDetection.showEmptyTips(ActivityManager.self().topActivity(), LTApplication.shareApplication().getString(R.string.memory_download_error));
//                        cn.lt.android.util.ToastUtils.showToast(LTApplication.shareApplication().getString(R.string.memory_download_error));
                }
            });
        }

        DownloadInfo m = event.getDownloadInfo();
        if (null != e && null != e.getMessage() && e.getMessage().contains("No address associated with hostname")
//                || e.getMessage().contains("Failed to connect to")
//                || e.getMessage().contains("start runnable but status err")
                ) {

            // 修改远程下载数据库该app的下载状态为暂停
            try {
                DownloadAgent.getImpl().updatePauseStatus(m.getDownloadId());
            } catch (RemoteException re) {

                postError(m);
                return;
            }
            AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(event.getDownloadInfo().getPackageName());
            if (appEntity != null) {
                if (appEntity.getStatus() != DownloadStatusDef.paused) {
                    String mode = appEntity.getIsAppAutoUpgrade() ? "app_auto_upgrade" : "auto";
                    DCStat.downloadRequestReport(appEntity, mode, "pause", "", "", "net_exception", "");//断网上报自动暂停,因为我们的APP在断网的情况下会自动暂停，产品需要此数据
                }

            }
            DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.paused, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
            downloadEvent.errorMessage = LTApplication.shareApplication().getString(R.string.download_error_2);
            EventBus.getDefault().post(downloadEvent);

            LogUtils.i(TAG, "blockComplete " + downloadEvent);
        } else {
            postError(m);
        }
    }


    @Override
    protected synchronized void warn(DownloadInfoEvent event) {
        DownloadInfo m = event.getDownloadInfo();
        DownloadEvent downloadEvent = new DownloadEvent(m.getDownloadId(), DownloadStatusDef.error, m.getPackageName(), m.getSoFarBytes(), m.getTotalBytes());
        EventBus.getDefault().post(downloadEvent);
        LogUtils.i(TAG, "warn " + downloadEvent);
    }

    @Override
    protected synchronized void trigger(DownloadInfoEvent event) {
        DownloadInfo m = event.getDownloadInfo();
        final AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(m.getPackageName());
        if (null != appEntity) {
            LogUtils.i("DCStat", "发生下载，上报下载请求");
            DCStat.downloadRequestReport(appEntity); //上报下载请求
        } else {
            LogUtils.i("GOOD", "查询不到下载对象，无法上报下载请求");
            DCStat.baiduStat(null, "request", "未知下载请求事件：" + m.getPackageName()); //上报下载请求
        }

    }


    private synchronized void postError(DownloadInfo event) {
        DownloadEvent downloadEvent = new DownloadEvent(event.getDownloadId(), DownloadStatusDef.error, event.getPackageName(), event.getSoFarBytes(), event.getTotalBytes());
        EventBus.getDefault().post(downloadEvent);
        LogUtils.i(TAG, "error " + downloadEvent);
    }

}
