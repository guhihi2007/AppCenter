package cn.lt.android.entity;

import java.util.List;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/3/14.
 */
public class TabTopicBean extends BaseBean {
    private String title;
    private String type;
    private List<TagBean> applists;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TagBean> getApplists() {
        return applists;
    }

    public void setApplists(List<TagBean> applists) {
        this.applists = applists;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
