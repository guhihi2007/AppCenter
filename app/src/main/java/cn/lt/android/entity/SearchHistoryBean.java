package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by dxx on 2016/3/11.
 */
public class SearchHistoryBean extends BaseBean {

    private String title;
    private String apps_type = "searchHistory";
    private long id;
    private String package_name;
    private  int pos;


    private boolean isAdv;
    private String category;
    private String downloadUrl;

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public SearchHistoryBean() {
    }

    public SearchHistoryBean(int id, String title, String apps_type) {
        this.title = title;
        this.apps_type = apps_type;
        this.id = id;
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

    public SearchHistoryBean(String title) {
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

}
