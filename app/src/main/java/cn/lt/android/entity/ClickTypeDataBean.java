package cn.lt.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/3/12.
 */
public class ClickTypeDataBean implements Serializable,Parcelable{
    private String url;
    private String id;
    private String apps_type;
    private String title;
    private String page;
    private boolean isAd;

    private boolean is_replace;
    private String package_name;
    private JSONObject reportData;

    public ClickTypeDataBean(String url, String id, String apps_type, String title, String page, boolean isAd, boolean is_replace, String package_name, JSONObject reportData) {
        this.url = url;
        this.id = id;
        this.apps_type = apps_type;
        this.title = title;
        this.page = page;
        this.isAd = isAd;
        this.is_replace = is_replace;
        this.package_name = package_name;
        this.reportData = reportData;
    }

    public ClickTypeDataBean(){
    }

    protected ClickTypeDataBean(Parcel in) {
        url = in.readString();
        id = in.readString();
        apps_type = in.readString();
        title = in.readString();
        page = in.readString();
        isAd = in.readByte() != 0;
        is_replace = in.readByte() != 0;
        package_name = in.readString();
    }

    public static final Creator<ClickTypeDataBean> CREATOR = new Creator<ClickTypeDataBean>() {
        @Override
        public ClickTypeDataBean createFromParcel(Parcel in) {
            return new ClickTypeDataBean(in);
        }

        @Override
        public ClickTypeDataBean[] newArray(int size) {
            return new ClickTypeDataBean[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public JSONObject getReportData() {
        return reportData;
    }

    public void setReportData(JSONObject reportData) {
        this.reportData = reportData;
    }

    public boolean is_replace() {
        return is_replace;
    }

    public void setIs_replace(boolean is_replace) {
        this.is_replace = is_replace;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(id);
        dest.writeString(apps_type);
        dest.writeString(title);
        dest.writeString(page);
        dest.writeByte((byte) (isAd ? 1 : 0));
        dest.writeByte((byte) (is_replace ? 1 : 0));
        dest.writeString(package_name);
    }
}
