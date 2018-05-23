package cn.lt.android.widget;

import android.view.View;

import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.download.DownloadStatusDef;

/**
 * Created by LinJunSheng on 2016/8/19.
 */

public class OrderWifiDownloadClickListener implements View.OnClickListener {

    private AppEntity appEntity;

    public OrderWifiDownloadClickListener(AppEntity appEntity) {
        this.appEntity = appEntity;
    }

    @Override
    public void onClick(View v) {
        appEntity.setIsOrderWifiDownload(true);
        int state = appEntity.getStatus();
        LogUtils.i("Erosion","OrderWifiDownloadClickListener===" + state);
        if (state == DownloadStatusDef.INVALID_STATUS || state == InstallState.upgrade) {
            DCStat.downloadRequestReport(appEntity, "manual", InstallState.upgrade == state ? "upgrade" : "request", "", "", "network_change", "book_wifi"); //解决预约Wifi下载没有存库导致的下载请求缺少部分字段(download_type)
        }
        if (state == InstallState.upgrade) {
            appEntity.setIsOrderWifiUpgrade(InstallState.upgrade);
        }
        DownloadTaskManager.getInstance().start(appEntity);
    }
}
