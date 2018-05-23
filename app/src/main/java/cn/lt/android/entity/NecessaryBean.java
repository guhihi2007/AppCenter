package cn.lt.android.entity;

import java.util.List;

import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by dxx on 2016/3/9.
 */
public class NecessaryBean extends BaseBean {
    private int id;
    private String title;
    private List<AppDetailBean> apps;

    public List<AppDetailBean> getApps() {
        return apps;
    }

    public void setApps(List<AppDetailBean> apps) {
        this.apps = apps;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
