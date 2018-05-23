package cn.lt.android.download;


import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.download.DownloadEventBusManager;
import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.listener.DownloadEventListener;
import de.greenrobot.event.EventBus;

/**
 * Created by liangxiaokai on 16/6/3.
 */
public class DownloadReportListener extends DownloadEventListener {
//    HashMap<String, Integer> failedTashMap = new HashMap<>();

    public DownloadReportListener() {
        String processName = LTApplication.shareApplication().getProcessName();
        if (processName.equals("cn.lt.appstore")) {
            DownloadEventBusManager.getEventBus().addListener(DownloadInfoEvent.ID, this);
        }
    }

    @Override
    protected void pending(DownloadInfoEvent event) {
    }

    @Override
    protected void progress(DownloadInfoEvent event) {
    }

    @Override
    protected void blockComplete(DownloadInfoEvent event) {
        LogUtils.i("DownloadReportListener", "blockComplete " + event.getDownloadInfo().getPackageName());
    }

    @Override
    protected void completed(DownloadInfoEvent event) {
        try {
            final AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(event.getDownloadInfo().getPackageName());
            if (null != appEntity) {
                if (AppUtils.apkIsNotExist(appEntity.getSavePath())) {
                    DCStat.downloadFialedEvent(appEntity, "", "packageError", "", "安装包不存在", "downloaded", "");
                    DownloadTaskManager.getInstance().remove(appEntity);
                    // 这里延迟是针对配置差的手机，不然页面更新会有异常
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new ApkNotExistEvent());
//                                ToastUtils.showToast(appEntity.getName() + " 的安装包不存在，正在为您重新下载");
                        }
                    }, 500);

                } else {
                    DCStat.downloadCompletedEvent(appEntity);
                }
                // 如果是重试之后的下载成功从failedTashMap内移除
//                String AppEntityFlag = appEntity.getPackageName() + appEntity.getAppClientId();
//                failedTashMap.remove(AppEntityFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void paused(DownloadInfoEvent event) {
        LogUtils.i("downloadReport", "收到暂停事件");
    }

    @Override
    protected void error(DownloadInfoEvent event, Throwable e) {
        try {
            AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(event.getDownloadInfo().getPackageName());
            if (null != appEntity) {
                String downloadErrorMsg = e.getMessage();
                LogUtils.i("DownloadErrorMsg", "下载出错信息==" + downloadErrorMsg);
                DCStat.downloadFialedEvent(appEntity, "", "downloadError", "", downloadErrorMsg, "download", "");
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    @Override
    protected void warn(DownloadInfoEvent event) {
    }

    @Override
    protected void trigger(DownloadInfoEvent event) {
    }
}
