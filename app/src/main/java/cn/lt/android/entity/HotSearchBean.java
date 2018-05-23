package cn.lt.android.entity;

import org.json.JSONObject;

import java.util.List;

import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by dxx on 2016/3/11.
 */
public class HotSearchBean extends BaseBean {

    private String title;
    private String apps_type;
    private long id;
    private String package_name;
    private List<AppDetailBean> apps;

    private boolean is_replace;
    private JSONObject reportData;


    private boolean isAdv;
    private String category;
    private String downloadUrl;


    public HotSearchBean() {
    }

    public HotSearchBean(int id, String title, String apps_type) {
        this.title = title;
        this.apps_type = apps_type;
        this.id = id;
    }

    public List<AppDetailBean> getApps() {
        return apps;
    }

    public void setApps(List<AppDetailBean> apps) {
        this.apps = apps;
    }

    public boolean isAdv() {
        return isAdv;
    }

    public String getPackage_name() {
        return package_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setAdv(boolean adv) {
        isAdv = adv;
    }

    public void setPakageName(String pakageName) {
        this.package_name = pakageName;
    }

    public HotSearchBean(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public boolean canReplace() {
        return is_replace;
    }

    public void setCanReplace(boolean is_replace) {
        this.is_replace = is_replace;
    }

    public JSONObject getReportData() {
        return reportData;
    }

    public void setReportData(JSONObject reportData) {
        this.reportData = reportData;
    }

}
