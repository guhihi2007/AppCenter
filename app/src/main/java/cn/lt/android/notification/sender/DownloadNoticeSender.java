package cn.lt.android.notification.sender;

import android.content.Context;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.AppEntityDao;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.notification.DownloadNotification;
import cn.lt.android.statistics.DCStatIdJoint;
import cn.lt.download.DownloadStatusDef;
import de.greenrobot.event.EventBus;

/**
 * Created by LinJunSheng on 2016/3/7.
 */
public class DownloadNoticeSender {
    private final Context              context;
    private       ExecutorService      mThreadPool;
    private       DownloadNotification downloadNotification;

    public DownloadNoticeSender(Context context, ExecutorService mThreadPool) {
        this.context = context;
        this.mThreadPool = mThreadPool;
        downloadNotification = new DownloadNotification(context);
        EventBus.getDefault().register(this);
    }

    /** 监听 正在下载、下载暂停、下载失败*/
    public void onEventMainThread(DownloadEvent downloadEvent) {
        if(downloadEvent.status == DownloadStatusDef.progress) {
            return;
        }
        handleNoticeEvent(downloadEvent);
    }

    /** 处理接收到的消息*/
    private synchronized void handleNoticeEvent(DownloadEvent downloadEvent) {
        if (LTApplication.appIsExit) {// app已经退出，无需再处理
            return;
        }

        if(downloadEvent != null && downloadEvent.status == DownloadStatusDef.completed) {
            final AppEntity app = DownloadTaskManager.getInstance().getAppEntityByPkg(downloadEvent.packageName);
            if(app == null || app.getIsAppAutoUpgrade()) {
                return;
            }
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    downloadNotification.cancelAppNotice(app);
                    downloadNotification.downloadComplete(app);
                }
            });

            return;
        }

        AppEntityDao dao = GlobalParams.getAppEntityDao();

        List<AppEntity> pending = new ArrayList<AppEntity>();
        List<AppEntity> paused = new ArrayList<AppEntity>();
        List<AppEntity> error = new ArrayList<AppEntity>();
        List<AppEntity> all = new ArrayList<AppEntity>();

        List<AppEntity> list = dao.queryBuilder().list();

        for (AppEntity app : list) {
            try {
                int status = DownloadTaskManager.getInstance().getState(app);
                if(downloadEvent != null
                        && downloadEvent.status != DownloadStatusDef.completed
                        && status != DownloadStatusDef.completed){
                    all.add(app);
                }
                switch (status) {
                    case DownloadStatusDef.paused:
                        paused.add(app);
                        break;
                    case DownloadStatusDef.error:
                        error.add(app);
                        break;
                    case DownloadStatusDef.pending:
                    case DownloadStatusDef.progress:{
                        pending.add(app);
                        break;
                    }

                }
            } catch (RemoteException re) {
                re.printStackTrace();
                return;
            }
        }

        pushNotice(pending, paused, error, all);

    }


    private void pushNotice(final List<AppEntity> pending,final List<AppEntity> paused,
                            final List<AppEntity> error ,final List<AppEntity> all) {

        mThreadPool.execute(new Runnable(){
            @Override
            public void run() {

                for (AppEntity app:all) {
                    downloadNotification.cancelAppNotice(app);
                }

                // 下载通知
                if (pending.size() > 0 ) {
                    if (pending.size() == 1) {
                        AppEntity app = pending.get(0);

                        // 属于应用自动下载的，不发通知
                        if(app.getIsAppAutoUpgrade()) {
                            return;
                        }

                        downloadNotification.singleDownload(app);
                        downloadNotification.cancelMultiDownloadingNotice();
                    } else {
                        // 拼接要上报的列表id
                        String appIds = DCStatIdJoint.jointIdByAppEntity(pending);
                        int pendingCount = getCount(pending);
                        if(pendingCount <= 0) {
                            return;
                        }
                        downloadNotification.multiDownload(pendingCount, appIds);
                    }
                } else {
                    downloadNotification.cancelMultiDownloadingNotice();
                }

                // 暂停通知
                if (paused.size() > 0) {
                    // 拼接要上报的列表id
                    String appIds = DCStatIdJoint.jointIdByAppEntity(paused);
                    int pauseCount = getCount(paused);
                    if(pauseCount <= 0) {
                        downloadNotification.cancelDownloadPauseNotice();
                        return;
                    }
                    downloadNotification.appDownloadPause(pauseCount, appIds);
                } else {
                    downloadNotification.cancelDownloadPauseNotice();
                }

                // 下载错误通知
                if (error.size() > 0) {
                    if (error.size() == 1) {
                        AppEntity app = error.get(0);

                        // 属于应用自动下载的，不发通知
                        if(app.getIsAppAutoUpgrade()) {
                            return;
                        }

                        downloadNotification.singleDownloadFault(app);
                        downloadNotification.cancelMultiDownloadFailNotice();
                    }
                    else {
                        // 拼接要上报的列表id
                        String appIds = DCStatIdJoint.jointIdByAppEntity(error);
                        int errorCount = getCount(error);
                        if(errorCount <= 0) {
                            return;
                        }
                        downloadNotification.multiDownloadFault(errorCount, appIds);
                    }
                } else {
                    downloadNotification.cancelMultiDownloadFailNotice();
                }

            }
        });
    }

    /** 计算要发出通知的数目*/
    private int getCount(List<AppEntity> appList) {
        int count = 0;
        for (AppEntity app : appList) {
            // 属于应用自动升级的的不作计数
            if(!app.getIsAppAutoUpgrade()) {
                count ++;
            }
        }
        return count;
    }

    /** 监听 删除下载任务*/
    public void onEventMainThread(RemoveEvent event) {
        AppEntity app = event.mAppEntity;
        if(app != null) {
            downloadNotification.cancelAppNotice(app);
        }
        handleNoticeEvent(null);

    }

}
