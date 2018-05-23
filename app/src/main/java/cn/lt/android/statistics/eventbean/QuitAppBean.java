package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/7/20.
 */
public class QuitAppBean extends BaseEventBean {
    private String from_page;

    public String getFrom_page() {
        return from_page;
    }

    public void setFrom_page(String from_page) {
        this.from_page = from_page;
    }
}
