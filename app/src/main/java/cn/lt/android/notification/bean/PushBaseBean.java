package cn.lt.android.notification.bean;

import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by LinJunSheng on 2016/3/22.
 */
public class PushBaseBean extends BaseBean {
    public String pushId;
    private String id;
    private String main_title;
    private String icon;
    private String notice_style;
    private String image;
    private String sub_title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMain_title() {
        return main_title;
    }

    public void setMain_title(String main_title) {
        this.main_title = main_title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getNotice_style() {
        return notice_style;
    }

    public void setNotice_style(String notice_style) {
        this.notice_style = notice_style;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSub_title() {
        return sub_title;
    }

    public void setSub_title(String sub_title) {
        this.sub_title = sub_title;
    }

    @Override
    public String toString() {
        return "PushBaseBean{" +
                "id='" + id + '\'' +
                ", main_title='" + main_title + '\'' +
                ", icon='" + icon + '\'' +
                ", notice_style='" + notice_style + '\'' +
                ", image='" + image + '\'' +
                ", sub_title='" + sub_title + '\'' +
                '}';
    }

    protected AppDetailBean app;

    public AppDetailBean getApp() {
        return app;
    }

    public void setApp(AppDetailBean app) {
        this.app = app;
    }
}
