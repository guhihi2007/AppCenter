package cn.lt.download.model;

import android.os.Parcel;
import android.os.Parcelable;

import cn.lt.download.DownloadStatusDef;


/**
 * ui进程与:downloader进程 相互通信对象
 */
public class DownloadInfo implements Parcelable {

    private byte status;
    private int downloadId;
    private String packageName;

    private long soFarBytes;
    private long totalBytes;
    private boolean isContinue;
    private String etag;

    private Throwable throwable;

    private int retryingTimes;

    private boolean useOldFile;

    public DownloadInfo(final DownloadModel model) {
        this.status = model.getStatus();
        this.downloadId = model.getId();
        this.packageName = model.getPackageName();
        this.soFarBytes = model.getSoFar();
        this.totalBytes = model.getTotal();
        this.etag = model.getETag();
    }



    public int getRetryingTimes() {
        return retryingTimes;
    }

    public void setRetryingTimes(int retryingTimes) {
        this.retryingTimes = retryingTimes;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public void setIsContinue(boolean isContinue) {
        this.isContinue = isContinue;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public long getSoFarBytes() {
        return soFarBytes;
    }

    public void setSoFarBytes(long soFarBytes) {
        this.soFarBytes = soFarBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public boolean isUseOldFile() {
        return useOldFile;
    }

    public void setUseOldFile(boolean useOldFile) {
        this.useOldFile = useOldFile;
    }

    public DownloadInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.status);
        dest.writeInt(this.downloadId);
        dest.writeString(packageName);
        dest.writeLong(this.soFarBytes);
        dest.writeLong(this.totalBytes);
        dest.writeInt(this.retryingTimes);
        dest.writeByte(useOldFile ? (byte) 1 : (byte) 0);
        dest.writeByte(isContinue ? (byte) 1 : (byte) 0);

        // For fewer copies
        switch (this.status) {
            case DownloadStatusDef.connected:
                dest.writeString(this.etag);
                break;
            case DownloadStatusDef.error:
                dest.writeSerializable(this.throwable);
                break;
            case DownloadStatusDef.retry:
                dest.writeSerializable(this.throwable);
                break;
        }
    }

    protected DownloadInfo(Parcel in) {
        this.status = in.readByte();
        this.downloadId = in.readInt();
        this.packageName = in.readString();
        this.soFarBytes = in.readLong();
        this.totalBytes = in.readLong();
        this.retryingTimes = in.readInt();
        this.useOldFile = in.readByte() == 1;
        this.isContinue = in.readByte() == 1;
        // For fewer copies
        switch (this.status) {

            case DownloadStatusDef.connected:
                this.etag = in.readString();
                break;
            case DownloadStatusDef.error:
                this.throwable = (Throwable) in.readSerializable();
                break;
            case DownloadStatusDef.retry:
                this.throwable = (Throwable) in.readSerializable();
                break;
        }
    }

    public DownloadInfo copy() {
        final DownloadInfo model = new DownloadInfo();

        model.status = this.status;
        model.downloadId = this.downloadId;
        model.packageName = this.packageName;
        model.soFarBytes = this.soFarBytes;
        model.totalBytes = this.totalBytes;
        model.etag = this.etag;
        model.isContinue = this.isContinue;
        model.throwable = this.throwable;
        model.retryingTimes = this.retryingTimes;
        model.useOldFile = this.useOldFile;

        return model;
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        public DownloadInfo createFromParcel(Parcel source) {
            return new DownloadInfo(source);
        }

        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String toString(){
        return "DownloadInfo[" + this.packageName + "|"+ this.status + "|" + this.getSoFarBytes() + "|" +this.getTotalBytes() + "]";
    }
}
