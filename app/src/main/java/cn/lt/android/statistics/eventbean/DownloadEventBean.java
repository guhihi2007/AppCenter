package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/5/27.
 */
public class DownloadEventBean extends BaseEventBean {
    private String page;//页面名称
    private String page_id;//页面ID
    private String app_id;
    private String app_type;//是游戏还是应用
    private String download_mode;//下载方式 : 手动/自动/一键下载
    private String download_type;//下载类型 : 是升级下载还是第一次下载
    private String event;//事件名称(request／request_auto/pause/pause_auto/resume/resume_auto/downloaded/delete/error)
    private long size;//本次下载的大小
    private String ad_type;
    private String pkg_name;
    private String event_detail;//事件详细信息，主要针对自动操作事件
    private String error;//错误类型
//    private String error_deteial;//错误详细信息
    private int success;
    private String source;
    private Boolean effective;
    private int p1;
    private int p2;
    private String from_id;
    private String from_page;
    private String resource_type;
    private String word;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

//    public String getError_deteial() {
//        return error_deteial;
//    }
//
//    public void setError_deteial(String error_deteial) {
//        this.error_deteial = error_deteial;
//    }

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

    public String getDownload_type() {
        return download_type;
    }

    public void setDownload_type(String download_type) {
        this.download_type = download_type;
    }

    public String getPage_id() {
        return page_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }



    public String getPageName() {
        return page;
    }

    public void setPageName(String pageName) {
        this.page = pageName;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getApp_type() {
        return app_type;
    }

    public void setApp_type(String app_type) {
        this.app_type = app_type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean isEffective() {
        return effective;
    }

    public void setEffective(Boolean effective) {
        this.effective = effective;
    }

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
}
