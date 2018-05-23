package cn.lt.android.entity;

import java.util.List;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by LinJunSheng on 2016/3/10.
 */

public class SpecialTopicDetailBean extends BaseBean{
    private String lead_content;
    private String banner;
    private String created_at;
    private String id;
    private String type;
    private String title;
    private List<AppDetailBean> apps;

    public void setLead_content(String lead_content) {
        this.lead_content = lead_content;
    }

    public String getLead_content() {
        return this.lead_content;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getBanner() {
        return this.banner;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_at() {
        return this.created_at;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public List<AppDetailBean> getApps() {
        return apps;
    }

    public void setApps(List<AppDetailBean> apps) {
        this.apps = apps;
    }

    @Override
    public String toString() {
        return "SpecialTopicDetailBean{" +
                "lead_content='" + lead_content + '\'' +
                ", banner='" + banner + '\'' +
                ", created_at='" + created_at + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", apps=" + apps +
                '}';
    }
}
