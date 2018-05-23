package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/6/2.
 */
public class StartEventBean extends BaseEventBean {
    private String app_info;

    private boolean is_screenOn;

    public boolean is_screenOn() {
        return is_screenOn;
    }

    public void setIs_screenOn(boolean is_screenOn) {
        this.is_screenOn = is_screenOn;
    }

    public String getApp_info() {
        return app_info;
    }

    public void setApp_info(String app_info) {
        this.app_info = app_info;
    }
}
