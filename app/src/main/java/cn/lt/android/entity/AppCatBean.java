package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/3/2.
 */
public class AppCatBean extends BaseBean {
    private String id;
    private String title;
    private String image;
    private AppDetailBean[] apps;
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public AppDetailBean[] getApps() {
        return apps;
    }

    public void setApps(AppDetailBean[] apps) {
        this.apps = apps;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
