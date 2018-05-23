package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/5/27.
 */
public class PageViewEventBean extends BaseEventBean {
    private String id;//页面ID，主要集中在应用详情，分类列表和专题详情页面有该字段
    private String page;//页面名称
    private String from_page;//上一级页面
    private String ad_type;
    private String from_id;
    private String word;
    private String event_detail;

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
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

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getFrom_page() {
        return from_page;
    }

    public void setFrom_page(String from_page) {
        this.from_page = from_page;
    }

    public String getEvent_detail() {
        return event_detail;
    }

    public void setEvent_detail(String event_detail) {
        this.event_detail = event_detail;
    }
}
