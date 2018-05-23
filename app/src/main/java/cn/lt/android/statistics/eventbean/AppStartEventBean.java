package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/5/30.
 */
public class AppStartEventBean extends BaseEventBean{
    private String event;//应用启动/退出
    private String duration;//如果是退出则包含本次使用时长（毫秒）

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }



}
