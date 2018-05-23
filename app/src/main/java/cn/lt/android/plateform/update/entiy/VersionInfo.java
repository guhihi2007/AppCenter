package cn.lt.android.plateform.update.entiy;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.lt.android.entity.PlatVersionBean;

/***
 * 平台升级请求获取信息；
 */
public class VersionInfo implements Serializable, Parcelable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static Parcelable.Creator<VersionInfo> CREATOR = new Creator<VersionInfo>() {

        @Override
        public VersionInfo createFromParcel(Parcel source) {
            return new VersionInfo(source);
        }

        @Override
        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };
    /**
     * apk URL
     */
    private String downloadUrl;
    /**
     * 是否强制升级
     */
    private boolean isForce;
    /**
     * apk MD5
     */
    private String md5;
    /**
     * 升级版本号
     */
    private String mUpgradeVersion;
    /**
     * 升级code
     */
    private long mUpgradeVersionCode;
    /**
     * 升级说明
     */
    private String mUpgradeSum;
    /**
     * 更新日期
     */
    private String mReleaseData;
    private Long mSize;

    public VersionInfo() {
        super();
    }

    public VersionInfo(PlatVersionBean bean) {
        super();
        downloadUrl = bean.getDownload_link();
        md5 = bean.getPackage_md5();
        mUpgradeVersion = bean.getVersion_name();
        mUpgradeSum = bean.getChangelog();
        mReleaseData = bean.getCreated_at();
        mSize = bean.getPackage_size();
        mUpgradeVersionCode = Long.valueOf(bean.getVersion_code());
        isForce = bean.isForce();
    }

    public VersionInfo(Parcel in) {
        try {
            downloadUrl = in.readString();
            md5 = in.readString();
            mUpgradeVersion = in.readString();
            mUpgradeVersionCode = in.readLong();
            mUpgradeSum = in.readString();
            mReleaseData = in.readString();
            isForce = (Boolean) in.readValue(Boolean.class.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Long getmSize() {
        return mSize;
    }

    public void setmSize(Long mSize) {
        this.mSize = mSize;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }

    public long getmUpgradeVersionCode() {
        return mUpgradeVersionCode;
    }

    public void setmUpgradeVersionCode(long mUpgradeVersionCode) {
        this.mUpgradeVersionCode = mUpgradeVersionCode;
    }

    public String getmReleaseData() {
        return mReleaseData;
    }

    public void setmReleaseData(String mReleaseData) {
        this.mReleaseData = mReleaseData;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getmUpgradeVersion() {
        return mUpgradeVersion;
    }

    public void setmUpgradeVersion(String smartUpgradeVersion) {
        this.mUpgradeVersion = smartUpgradeVersion;
    }

    public String getUpgradeIntroduce() {
        return mUpgradeSum;
    }

    public void setUpgradeIntroduce(String upgradeIntroduce) {
        this.mUpgradeSum = upgradeIntroduce;
    }

    public List<String> getIntroduces() {
        final List<String> introduces = new ArrayList<String>();
        if (mUpgradeSum != null) {
            final String SPLITE = "\n";
            final String[] splite = mUpgradeSum.split(SPLITE);
            introduces.addAll(Arrays.asList(splite));
        }

        return introduces;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(downloadUrl);
        dest.writeValue(isForce);
        dest.writeString(md5);
        dest.writeString(mUpgradeVersion);
        dest.writeLong(mUpgradeVersionCode);
        dest.writeString(mUpgradeSum);
        dest.writeString(mReleaseData);
        dest.writeLong(mSize);
    }
}
