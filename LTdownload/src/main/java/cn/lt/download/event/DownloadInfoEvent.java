package cn.lt.download.event;


import cn.lt.download.model.DownloadInfo;

/**
 * 内部 通信 事件
 */
public class DownloadInfoEvent extends IEvent {

    public final static String ID = "event.download.downloadInfo";

    public DownloadInfoEvent(final DownloadInfo downloadInfo) {
        super(ID);
        if (downloadInfo != null) {
            setDownloadInfo(downloadInfo);
        }
    }

    private DownloadInfo downloadInfo;

    public void setDownloadInfo(final DownloadInfo info) {
        if (info == null) {
            this.downloadInfo = null;
            return;
        }
        this.downloadInfo = info.copy();
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    public String toString(){
        return "DownloadInfoEvent :" + downloadInfo.toString();
    }
}
