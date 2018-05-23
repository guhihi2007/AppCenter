package cn.lt.android.entity;

import android.text.TextUtils;

import org.json.JSONObject;

import cn.lt.android.db.AppEntity;
import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * 渲染应用列表所使用的bean
 * Created by LinJunSheng on 2016/6/24.
 */
public class AppBriefBean extends BaseBean {
    private String id;
    private String name;
    private String apps_type;
    private String alias;
    private String package_md5;
    private String package_name;
    private String package_size;
    private String version_code;
    private String version_name;
    private String corner_url;
    private String reviews;
    private String created_at;
    private String download_url;
    private String icon_url;
    private String download_count;
    private String description;
    private String category;

    private boolean is_ads = false;
    private boolean is_replace = false;
    private JSONObject reportData;

    public boolean isPositionLast = false;

    /** 广告所属公司*/
    private String adMold = "";

    private AppEntity downloadAppEntity;

    public AppEntity getDownloadAppEntity() {
        return downloadAppEntity;
    }

    public void setDownloadAppEntity(AppEntity downloadAppEntity) {
        this.downloadAppEntity = downloadAppEntity;
    }

    public String getId() {
        return id;
    }

//    public long getAppServerId(){
//        if(TextUtils.isEmpty(getId())) {
//            return -1;
//        } else {
//            return Long.parseLong(id);
//        }
//
//    }

    public String getAppClientId(){
        if(isAdData()) {
            return package_name;
        } else {
            return getId();
        }
    }

    public boolean isAdData(){
        if(TextUtils.isEmpty(getId()) || Integer.valueOf(getId()) <= 0) {
            return true;
        }
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApps_type() {
        return apps_type;
    }

    public void setApps_type(String apps_type) {
        this.apps_type = apps_type;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPackage_md5() {
        return package_md5;
    }

    public void setPackage_md5(String package_md5) {
        this.package_md5 = package_md5;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getPackage_size() {
        return package_size;
    }

    public void setPackage_size(String package_size) {
        this.package_size = package_size;
    }

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getCorner_url() {
        return corner_url;
    }

    public void setCorner_url(String corner_url) {
        this.corner_url = corner_url;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getDownload_count() {
        return download_count;
    }

    public void setDownload_count(String download_count) {
        this.download_count = download_count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAD() {
        return is_ads;
    }

    public void setAD(boolean AD) {
        is_ads = AD;
    }

    public boolean canReplace() {
        return is_replace;
    }

    public void setCanReplace(boolean is_replace) {
        this.is_replace = is_replace;
    }

    public String getAdMold() {
        return adMold;
    }

    public void setAdMold(String adMold) {
        this.adMold = adMold;
    }

    public JSONObject getReportData() {
        return reportData;
    }

    public void setReportData(JSONObject reportData) {
        this.reportData = reportData;
    }

    @Override
    public String toString() {
        return "AppBriefBean{" +
                "package_name='" + package_name + '\'' +
                '}';
    }
}
