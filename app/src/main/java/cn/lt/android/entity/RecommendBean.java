package cn.lt.android.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONObject;

import cn.lt.android.db.AppEntity;
import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/3/4.
 */
public class RecommendBean extends BaseBean implements Parcelable {

    private String id;
    private String apps_type;
    private String name;
    private String alias;
    private String icon_url;
    private String corner_url;
    private String package_size;
    private String package_name;
    private String download_url;
    private AppEntity appEntity;

    private String download_count;
    private String reviews;

    private String version_name;

    private String version_code;

    private boolean is_replace = false;
    private JSONObject reportData;

    protected RecommendBean(Parcel in) {
        id = in.readString();
        apps_type = in.readString();
        name = in.readString();
        alias = in.readString();
        icon_url = in.readString();
        corner_url = in.readString();
        package_size = in.readString();
        package_name = in.readString();
        download_url = in.readString();
        download_count = in.readString();
        reviews = in.readString();
        version_name = in.readString();
        version_code = in.readString();
        is_replace = in.readByte() != 0;
    }

    public static final Creator<RecommendBean> CREATOR = new Creator<RecommendBean>() {
        @Override
        public RecommendBean createFromParcel(Parcel in) {
            return new RecommendBean(in);
        }

        @Override
        public RecommendBean[] newArray(int size) {
            return new RecommendBean[size];
        }
    };

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public boolean isAdData(){
        if(TextUtils.isEmpty(getId()) || Integer.valueOf(getId()) <= 0) {
            return true;
        }
        return false;
    }

    public String getAppClientId(){
        if(isAdData()) {
            return package_name;
        } else {
            return getId();
        }
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getPackage_size() {
        return package_size;
    }

    public void setPackage_size(String package_size) {
        this.package_size = package_size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApps_type() {
        return apps_type;
    }

    public void setApps_type(String apps_type) {
        this.apps_type = apps_type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getCorner_url() {
        return corner_url;
    }

    public void setCorner_url(String corner_url) {
        this.corner_url = corner_url;
    }

    public AppEntity getAppEntity() {
        return appEntity;
    }

    public void setAppEntity(AppEntity appEntity) {
        this.appEntity = appEntity;
    }

    public String getDownload_count() {
        return download_count;
    }

    public void setDownload_count(String download_count) {
        this.download_count = download_count;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public boolean is_replace() {
        return is_replace;
    }

    public void setIs_replace(boolean is_replace) {
        this.is_replace = is_replace;
    }

    public JSONObject getReportData() {
        return reportData;
    }

    public void setReportData(JSONObject reportData) {
        this.reportData = reportData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(apps_type);
        dest.writeString(name);
        dest.writeString(alias);
        dest.writeString(icon_url);
        dest.writeString(corner_url);
        dest.writeString(package_size);
        dest.writeString(package_name);
        dest.writeString(download_url);
        dest.writeString(download_count);
        dest.writeString(reviews);
        dest.writeString(version_name);
        dest.writeString(version_code);
        dest.writeByte((byte) (is_replace ? 1 : 0));
    }
}
