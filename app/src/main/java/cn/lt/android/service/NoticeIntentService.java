package cn.lt.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.install.InstallManager;
import cn.lt.android.main.LoadingActivity;
import cn.lt.android.main.download.TaskManagerActivity;
import cn.lt.android.main.personalcenter.AppUpgradeActivity;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.notification.event.AllDownloadFailRetry;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.FromPageManager;
import cn.lt.android.util.LogUtils;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.NetWorkUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by LinJunSheng on 2016/3/14.
 */
public class NoticeIntentService extends IntentService {

    public static final String ACTION = "cn.lt.android.service.InstallIntentService";

    public NoticeIntentService() {
        super("InstallIntentservice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String jumpBy = intent.getStringExtra(NoticeConsts.jumpBy);
        if (null != jumpBy) {
            if (jumpBy.equals(NoticeConsts.jumpByDownloadFault)) {
                reDownload(intent);
                directStartAppDownloadTAG(NoticeConsts.goDownloadTab);
            }
            if (jumpBy.equals(NoticeConsts.jumpByDownloadComplete)) {
                startInstall(intent);
            }
            if (jumpBy.equals(NoticeConsts.wakeAppType)) {
                String appId = intent.getStringExtra("push_id");
                String url = intent.getStringExtra("url");
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                DCStat.pushEvent(appId, "Awake", "clicked", "GETUI", "");
            }
            if (jumpBy.equals(NoticeConsts.jumpByInstallComplete)) {
                openApp(intent);
            }

            if (jumpBy.equals(NoticeConsts.jumpByStartTaskManager)) {
                int jumpType = intent.getIntExtra(NoticeConsts.noticeStartType, 0);
                String appId = intent.getStringExtra("appId");
                if (LTApplication.appIsStart) {
                    directStartAppDownloadTAG(jumpType);
                } else {
                    startActivity(new Intent(getApplicationContext(), LoadingActivity.class).
                            setAction(Intent.ACTION_MAIN).
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).
                            addCategory(Intent.CATEGORY_LAUNCHER).
                            putExtra(Constant.EXTRA_CLICK_FROM_NOTICE, true).
                            putExtra(NoticeConsts.noticeStartType, jumpType));
                }
                DCStat.pushEvent(appId, "App", "clicked", "APP", "");

            }
        }
    }

    private void directStartAppDownloadTAG(int jumpType) {
        Intent downloadTagIntent = new Intent(getApplicationContext(), TaskManagerActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent appManagerIntent = new Intent(getApplicationContext(), AppUpgradeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        FromPageManager.setLastPage(Constant.PAGE_NOTIFICATION);
        switch (jumpType) {
            case NoticeConsts.goDownloadTab:// 应用下载Tab
                startActivity(downloadTagIntent);
                break;
            case NoticeConsts.goDownloadTabByUpgrade:// 点击升级通知跳到 应用下载Tab
                if (NetWorkUtils.isWifi(this)) {
                    startActivity(downloadTagIntent.putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByUpgrade));
                } else {
                    startActivity(appManagerIntent);
                }
                break;
            case NoticeConsts.goDownloadTabByMultiDownloadFault:// 点击多个下载失败的通知跳到 应用下载Tab
                startActivity(downloadTagIntent);
                LTApplication.shareApplication().getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reDownloadMultiFault();
                    }
                }, 300);


//                startActivity(downloadTagIntent.putExtra(NoticeConsts.jumpBy,  NoticeConsts.jumpByMultiDownloadFault));
                break;
            default:
                break;
        }


    }

    private void reDownloadMultiFault() {
        List<AppEntity> downloadList = DownloadTaskManager.getInstance().getDownloadTaskList();
        if (downloadList != null && downloadList.size() != 0) {
            for (AppEntity app : downloadList) {
                int status = app.getStatus();
                switch (status) {
                    case DownloadStatusDef.error:
                    case DownloadStatusDef.retry: {
                        DownloadTaskManager.getInstance().startAfterCheck(this, app, "manual", "retry", "notification", "", "", "push");
                        break;
                    }
                }
            }
            Log.i(LogTAG.PushTAG, "NoticeIntentService~~执行 多个重新下载");
        }

    }

    /**
     * 执行应用安装
     */
    private void startInstall(Intent intent) {
        AppEntity app = (AppEntity) intent.getSerializableExtra("appEntity");

        if (app == null) {
            String appPkg = intent.getStringExtra("appPkg");
            app = DownloadTaskManager.getInstance().getAppEntityByPkg(appPkg);
        }

        if (null != app) {
            InstallManager.getInstance().start(app, "notification", "", false);
            DCStat.pushEvent(app.getAppClientId(), "App", "clicked", "APP", "");
        }
    }

    /**
     * 执行重新下载
     */
    private void reDownload(Intent intent) {
        AppEntity app = (AppEntity) intent.getSerializableExtra("appEntity");

        if (app == null) {
            String appPkg = intent.getStringExtra("appPkg");
            app = DownloadTaskManager.getInstance().getAppEntityByPkg(appPkg);
        }

        if (null != app) {
            DownloadTaskManager.getInstance().startAfterCheck(this, app, "manual", "retry", "notification", "", "", "push");
            DCStat.pushEvent(app.getAppClientId(), "App", "clicked", "APP", "");
        }
    }

    /**
     * 执行 多个重新下载
     */
    private void startMultiDownload() {
        // 执行失败列表重下载
        EventBus.getDefault().post(new AllDownloadFailRetry());
        LogUtils.i(LogTAG.PushTAG, "NoticeIntentService~~执行 多个重新下载");
    }

    private void openApp(Intent intent) {
        AppEntity app = (AppEntity) intent.getSerializableExtra("appEntity");

        if (app != null) {
            //已安装，打开
            AppUtils.openApp(this, app.getPackageName());
            DCStat.pushEvent(app.getAppClientId(), "App", "open", "APP", "");
        } else {
            String appPkg = intent.getStringExtra("appPkg");

            //已安装，打开
            AppUtils.openApp(this, appPkg);

            String id = intent.getStringExtra("appId");
            DCStat.pushEvent(id, "App", "open", "APP", "");
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
