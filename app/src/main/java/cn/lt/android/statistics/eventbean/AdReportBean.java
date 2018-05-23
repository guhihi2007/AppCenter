package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2017/8/4.
 */

public class AdReportBean extends BaseEventBean {
    private String event;
    private String ad_source;
    private String ad_type;
    private String id;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getAd_source() {
        return ad_source;
    }

    public void setAd_source(String ad_source) {
        this.ad_source = ad_source;
    }

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
