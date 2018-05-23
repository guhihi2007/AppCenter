package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/5/30.
 */
public class PlatUpgradeEventBean extends BaseEventBean {
    private String from_version;
    private String to_version;
    private String upgrade_type;//升级类型（普通升级/强制升级）
    private String from;//升级来源（页面/服务/通知栏）
    private String event;
    private String install_mode;
    private String error;
    private String event_detail;


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getFrom_version() {
        return from_version;
    }

    public void setFrom_version(String from_version) {
        this.from_version = from_version;
    }

    public String getTo_version() {
        return to_version;
    }

    public void setTo_version(String to_version) {
        this.to_version = to_version;
    }

    public String getUpgrade_type() {
        return upgrade_type;
    }

    public void setUpgrade_type(String upgrade_type) {
        this.upgrade_type = upgrade_type;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getInstall_mode() {
        return install_mode;
    }

    public void setInstall_mode(String install_mode) {
        this.install_mode = install_mode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getEvent_detail() {
        return event_detail;
    }

    public void setEvent_detail(String event_detail) {
        this.event_detail = event_detail;
    }
}
