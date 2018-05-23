package cn.lt.android.event;

/**
 * Created by wenchao on 2016/1/26.
 * 下载事件
 */
public class DownloadEvent {
    public int status;
    public int  downloadId;
    public String packageName;
    public long soFarBytes;
    public long totalBytes;
    public String errorMessage;


    public DownloadEvent( int downloadId,int status, String packageName, long soFarBytes, long totalBytes ) {
        this.status = status;
        this.downloadId = downloadId;
        this.packageName = packageName;
        this.soFarBytes = soFarBytes;
        this.totalBytes = totalBytes;
    }


    public DownloadEvent( int downloadId,int status, String packageName ) {
        this.status = status;
        this.downloadId = downloadId;
        this.packageName = packageName;
    }

    @Override
    public String toString(){
        return "DownloadEvent:pkg=" + packageName + "|status=" + this.status + "!sofar=" + soFarBytes + "|total=" + totalBytes;
    }
}
