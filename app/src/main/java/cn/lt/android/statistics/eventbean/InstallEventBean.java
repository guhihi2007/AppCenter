package cn.lt.android.statistics.eventbean;

import cn.lt.android.db.AppEntity;
import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/5/28.
 */
public class InstallEventBean extends BaseEventBean {
    private String page;//页面发起地址
    private AppEntity appInfo;//应用信息
    private String page_id;//页面ID
    private String install_mode;//安装方式(//system/auto/root/manual)
    private String download_mode;//下载模式 （auto /normal）
    private String event;//install / installed / error
    private String app_id;
    private String app_type;
    private String install_type;//安装类型 是升级的安装还是普通安装
    private String install_way;// //d单个安装/一键安装
    private String ad_type;
    private int success;//成功为0失败为1
    private String pkg_name;
    private String error;
    private String event_detail;//用于描述事件发生的详细信息
    private Boolean effective;
    private int p1;
    private int p2;
    private String from_id;
    private String from_page;
    private String resource_type;
    private String word;

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getFrom_page() {
        return from_page;
    }

    public void setFrom_page(String from_page) {
        this.from_page = from_page;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
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

    public String getPkg_name() {
        return pkg_name;
    }

    public void setPkg_name(String pkg_name) {
        this.pkg_name = pkg_name;
    }

    public String getInstall_way() {
        return install_way;
    }

    public void setInstall_way(String install_way) {
        this.install_way = install_way;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
    }

    public String getDownload_mode() {
        return download_mode;
    }

    public void setDownload_mode(String download_mode) {
        this.download_mode = download_mode;
    }

    public String getPage_id() {
        return page_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }

    public String getInstall_type() {
        return install_type;
    }

    public void setInstall_type(String install_type) {
        this.install_type = install_type;
    }

    public AppEntity getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(AppEntity appInfo) {
        this.appInfo = appInfo;
    }

    public String getInstall_mode() {
        return install_mode;
    }

    public void setInstall_mode(String install_mode) {
        this.install_mode = install_mode;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
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

    public Boolean isEffective() {
        return effective;
    }

    public void setEffective(Boolean effective) {
        this.effective = effective;
    }
}
