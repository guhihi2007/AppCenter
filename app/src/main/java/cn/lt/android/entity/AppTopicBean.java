package cn.lt.android.entity;

import java.util.List;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/3/12.
 */
public class AppTopicBean extends BaseBean {
    public static final int IS_FIRST = 1;
    public static final int IS_LAST = 2;
    public static final int IS_ONLY = 3;
    public static final int DEFAULT = 4;

    private String topic_name;
    private String topic_title;
    private String title_color;
    private List<AppDetailBean> apps;
    private int positionType = 0;
    private int show_type = 4;

    public List<AppBriefBean> getBriefApps() {
        return briefApps;
    }

    public void setBriefApps(List<AppBriefBean> briefApps) {
        this.briefApps = briefApps;
    }

    private List<AppBriefBean> briefApps;

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public String getTopic_title() {
        return topic_title;
    }

    public void setTopic_title(String topic_title) {
        this.topic_title = topic_title;
    }

    public String getTitle_color() {
        return title_color;
    }

    public void setTitle_color(String title_color) {
        this.title_color = title_color;
    }

    public List<AppDetailBean> getApps() {
        return apps;
    }

    public void setApps(List<AppDetailBean> apps) {
        this.apps = apps;
    }

    public int getPositionType() {
        return positionType;
    }

    public void setPositionType(int positionType) {
        this.positionType = positionType;
    }

    public int getShow_type() {
        return show_type;
    }

    public void setShow_type(int show_type) {
        this.show_type = show_type;
    }

}
