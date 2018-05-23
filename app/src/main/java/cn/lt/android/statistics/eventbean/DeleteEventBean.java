package cn.lt.android.statistics.eventbean;

import cn.lt.android.db.AppEntity;
import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/6/4.
 */
public class DeleteEventBean extends BaseEventBean {
    private AppEntity appInfo;
    private String app_id;
    private String app_type;
    private String page;
    private String event;


    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_type() {
        return app_type;
    }

    public void setApp_type(String app_type) {
        this.app_type = app_type;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public AppEntity getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppEntity appInfo) {
        this.appInfo = appInfo;
    }
}
