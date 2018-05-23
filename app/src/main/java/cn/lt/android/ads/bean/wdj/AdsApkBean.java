package cn.lt.android.ads.bean.wdj;

/**
 * Created by ltbl on 2016/6/28.
 */
public class AdsApkBean {
    private long bytes;
    private AdsDownloadUrl downloadUrl;
    private String md5;
    private String signature;
    private String versionCode;
    private String versionName;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public AdsDownloadUrl getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(AdsDownloadUrl downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
