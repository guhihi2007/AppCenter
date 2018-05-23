package cn.lt.download.i;

import cn.lt.download.i.IFileDownloadIPCCallback;
import cn.lt.download.model.DownloadInfo;

interface IFileDownloadIPCService {

    oneway void registerCallback(in IFileDownloadIPCCallback callback);
    oneway void unregisterCallback(in IFileDownloadIPCCallback callback);

    DownloadInfo checkReuse(String url, String path);
    DownloadInfo checkReuse2(int id);
    DownloadInfo getCommonDownloadInfo(int downloadId); //获取DownloadInfo
    boolean checkDownloading(String url, String path);
    oneway void start(String packageName,String url, String path, int callbackProgressTimes, int autoRetryTimes, boolean isOrderWifiDownload);
    boolean pause(int downloadId);
    void pauseAllTasks();

    long getSofar(int downloadId);
    long getTotal(int downloadId);
    int getStatus(int downloadId);
    boolean isIdle();
    boolean remove(int downloadId);

    void updateToPauseStatus(int downloadId);
}
