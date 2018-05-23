package cn.lt.android.statistics;

import android.text.TextUtils;

import com.google.gson.Gson;

import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdMold;
import cn.lt.android.statistics.eventbean.AcitiveBean;
import cn.lt.android.statistics.eventbean.AdReportBean;
import cn.lt.android.statistics.eventbean.AdViewBean;
import cn.lt.android.statistics.eventbean.AwakeEventBean;
import cn.lt.android.statistics.eventbean.ClickEventBean;
import cn.lt.android.statistics.eventbean.DeleteEventBean;
import cn.lt.android.statistics.eventbean.DownloadEventBean;
import cn.lt.android.statistics.eventbean.InstallEventBean;
import cn.lt.android.statistics.eventbean.PageViewEventBean;
import cn.lt.android.statistics.eventbean.PlatUpgradeEventBean;
import cn.lt.android.statistics.eventbean.PushEventBean;
import cn.lt.android.statistics.eventbean.QuitAppBean;
import cn.lt.android.statistics.eventbean.SearchEventBean;
import cn.lt.android.statistics.eventbean.StartEventBean;
import cn.lt.android.statistics.eventbean.AdShowBean;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.TimeUtils;
import cn.lt.framework.util.PreferencesUtils;

/***
 * 4.0（含）以上统计事件对象；
 * Created by Administrator on 2015/11/25.
 */
public class StatisticsEventData {
    private String action_type;//事件类型
    private String page;//页面名称
    private String from_page;//上级页面/页面来源
    private String page_id;//页面ＩＤ
    private int p1 = 0;//在模块中位置
    private int p2 = 0;//同种类型的View在页面中的位置
    private String id;
    private String srcType;
    private String presentType;
    private String appType;//APP类型
    private String download_mode;//下载/安装模式    手动/自动/一键下载
    private String install_mode;//下载/安装模式    手动/自动/一键下载
    private String install_way;//安装方式    单个安装//一键安装
    private long downloadSize;//已下载大小
    private String keyWord;//搜索关键字
    private String packageInfo;//已安装列表
    private String event;//事件名称
    private String event_detail;//事件详情
    public String download_type;//下载类型:用于区分首次下载和升级下载
    private boolean isAlive;//是否存活，状态
    private String pkgName;//应用/游戏包名
    private int downloadState;
    private String source;//事件来源
    private String ad_type; //广告类型
    private boolean isScreenOn = false;
    private String adListStr;
    private int curPage;
    private String error;
    //    private String error_detail;
    private int success;//是否成功  0：成功 1：失败
    private Boolean effective;
    private String from_id;
    private String resource_type;

    //470拉活新增
    private String task_id;
    private String type;                 // wake、launch、notification、heads-up
    private int show_type;
    private String action_name;
    private String class_name;
    private String deep_link;

    public String getAction_type() {
        return action_type;
    }

    public String getPage_id() {
        return page_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }

    public String getSrcType() {
        return srcType;
    }

    public String getPresentType() {
        return presentType;
    }

    public String getAppType() {
        return appType;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public String getPackageInfo() {
        return packageInfo;
    }

    public Boolean getEffective() {
        return effective;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getShow_type() {
        return show_type;
    }

    public void setShow_type(int show_type) {
        this.show_type = show_type;
    }

    public String getAction_name() {
        return action_name;
    }

    public void setAction_name(String action_name) {
        this.action_name = action_name;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getDeep_link() {
        return deep_link;
    }

    public void setDeep_link(String deep_link) {
        this.deep_link = deep_link;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public StatisticsEventData() {
    }

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

//    public String getError_detail() {
//        return error_detail;
//    }
//
//    public void setError_detail(String error_detail) {
//        this.error_detail = error_detail;
//    }

    public String getInstall_way() {
        return install_way;
    }

    public void setInstall_way(String install_way) {
        this.install_way = install_way;
    }


    public String getFrom_page() {
        return from_page;
    }

    public boolean isScreenOn() {
        return isScreenOn;
    }

    public void setScreenOn(boolean screenOn) {
        isScreenOn = screenOn;
    }

    public String getAd_type() {
        return ad_type;
    }

    public void setAd_type(String ad_type) {
        this.ad_type = ad_type;
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

    public String getDownload_mode() {
        return download_mode;
    }

    public void setDownload_mode(String download_mode) {
        this.download_mode = download_mode;
    }

    public String getInstall_mode() {
        return install_mode;
    }

    public void setInstall_mode(String install_mode) {
        this.install_mode = install_mode;
    }


    public String getDownload_type() {
        return download_type;
    }

    public void setDownload_type(String download_type) {
        this.download_type = download_type;
    }

    public String getSource() {
        return source;
    }


    public void setSource(String source) {
        this.source = source;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getEvent() {
        return event;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }


    public void setFrom_page(String from_page) {
        this.from_page = from_page;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setPresentType(String presentType) {
        this.presentType = presentType;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }


    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public void setAction_type(String action_type) {
        this.action_type = action_type;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }


    public void setPackageInfo(String packageInfo) {
        this.packageInfo = packageInfo;
    }


    public StatisticsEventData(String page) {
        this.page = page;
    }


    public String getActionType() {
        return action_type;
    }

    public void setActionType(String actionType) {
        this.action_type = actionType;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPageID() {
        return page_id;
    }

    public void setPageID(String pageID) {
        this.page_id = pageID;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }


    public void setAppType(String appType) {
        this.appType = appType;
    }


    public void setEvent_detail(String event_detail) {
        this.event_detail = event_detail;
    }

    public String getEvent_detail() {
        return event_detail;
    }

    public String getAdListStr() {
        return adListStr;
    }

    public void setAdListStr(String adListStr) {
        this.adListStr = adListStr;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public Boolean isEffective() {
        return effective;
    }

    public void setEffective(Boolean effective) {
        this.effective = effective;
    }

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }

    public String getString(String actionType) {
        if (TextUtils.isEmpty(actionType)) {
            LogUtils.i("zzz", "ActionType为空");
            return null;
        }
        String s = getStatString(actionType);
        LogUtils.i("zzz", "actionType-->" + actionType);

        return s;
    }

    /***
     * 根据ActionType转换成相应的ＪＳＯＮ数据
     *
     * @param actionType
     * @return
     */
    private String getStatString(String actionType) {
        String s;
        Gson gson = new Gson();
        String netWorkType = NetUtils.getNetworkType(LTApplication.shareApplication());
        long currentTimeMillis = System.currentTimeMillis();
        String currentTime = String.valueOf(currentTimeMillis);
        String timeFormat = TimeUtils.getStringToDateHaveHour(currentTimeMillis);
        String imei = AppUtils.getIMEI(LTApplication.instance);
        if (ReportEvent.ACTION_CLICK.equals(actionType) || ReportEvent.ACTION_ADCLICK.equals(actionType)) {//广告位点击
            ClickEventBean data = new ClickEventBean();
            data.setAction_type(actionType);
            data.setPage(page);
            data.setP1(p1);
            data.setResource_id(TextUtils.isEmpty(id) ? "" : id);
            data.setResource_type(presentType);
            data.setP2(p2);
            data.setWord(keyWord);
            data.setAd_type(TextUtils.isEmpty(ad_type) ? "" : ad_type);
            data.setNet(netWorkType);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_PAGEVIEW.equals(actionType) || ReportEvent.ACTION_ADS_PAGEVIEW.equals(actionType)) {//页面浏览
            PageViewEventBean data = new PageViewEventBean();
            data.setId(TextUtils.isEmpty(id) ? "" : id);
            data.setAction_type(actionType);
            data.setPage(page);
            data.setFrom_page(from_page);
            data.setNet(netWorkType);
            if (ReportEvent.ACTION_ADS_PAGEVIEW.equals(actionType)) {
                data.setAd_type(ad_type);
            }
            data.setFrom_id(TextUtils.isEmpty(from_id) ? "" : from_id);
            data.setWord(keyWord);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            data.setEvent_detail(event_detail);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_ADVIEW.equals(actionType)) {//广告展示
            AdViewBean data = new AdViewBean();
            data.setId(TextUtils.isEmpty(id) ? "" : id);
            data.setAction_type(actionType);
            data.setCategory(appType);
            data.setPage(page);
            data.setNet(netWorkType);
            data.setPkg_name(pkgName);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_SEARCH.equals(actionType)) {//搜索
            SearchEventBean data = new SearchEventBean();
            data.setAction_type(actionType);
            data.setPage(page);
            data.setWord(keyWord);
            data.setTime(currentTime);
            data.setNet(netWorkType);
            data.setTimeformat(timeFormat);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_SEARCH_RECOMMEND.equals(actionType)) {//搜索
            SearchEventBean data = new SearchEventBean();
            data.setAction_type(actionType);
            data.setWord(keyWord);
            data.setResource_typ(resource_type);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setNet(netWorkType);
            data.setP2(p2);
            data.setP1(p1);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_PLATUPGRADE.equals(actionType)) {//平台升级
            PlatUpgradeEventBean data = new PlatUpgradeEventBean();
            String toVersionCode = PreferencesUtils.getString(LTApplication.shareApplication(), "newVersionCode", "0");
            data.setAction_type(actionType);
            data.setFrom_version(String.valueOf(AppUtils.getVersionCode(LTApplication.shareApplication())));
            if (!TextUtils.isEmpty(toVersionCode)) {
                data.setTo_version(toVersionCode);
            }
            data.setEvent(event);
            data.setFrom(source);
            data.setUpgrade_type(srcType);
            data.setInstall_mode(install_mode);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setError(error);
            data.setEvent_detail(TextUtils.isEmpty(event_detail) ? "" : event_detail);
            data.setNet(netWorkType);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_DOWNLOAD.equals(actionType) || ReportEvent.ACTION_AD_DOWNLOAD.equals(actionType)) {//应用下载
            DownloadEventBean data = new DownloadEventBean();
            data.setAction_type(actionType);
            data.setPageName(page);
            data.setEvent(event);
            data.setDownload_type(download_type);
            data.setDownload_mode(download_mode);
            data.setPkg_name(pkgName);
            data.setApp_type(appType);
            data.setApp_id(TextUtils.isEmpty(id) ? "" : id);
            data.setPage_id(TextUtils.isEmpty(page_id) ? "" : page_id);
            data.setAd_type(ad_type);
            data.setNet(netWorkType);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setSuccess(success);
            data.setError(TextUtils.isEmpty(error) ? "" : error);
            data.setEvent_detail(TextUtils.isEmpty(event_detail) ? "" : event_detail); //用于描述事件的详细信息
//            data.setError_deteial(TextUtils.isEmpty(error_detail) ? "" : error_detail);
            data.setSize(downloadSize);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            if (("downloaded".equals(event) || "request".equals(event)) && (AdMold.CHANG_WEI.equals(ad_type) || AdMold.WanKa.equals(ad_type))) {
                data.setEffective(effective);
            }
            data.setP1(p1);
            data.setP2(p2);
            data.setFrom_page(TextUtils.isEmpty(from_page) ? "" : from_page);
            data.setFrom_id(TextUtils.isEmpty(from_id) ? "" : from_id);
            data.setWord(TextUtils.isEmpty(keyWord) ? "" : keyWord);
            data.setResource_type(TextUtils.isEmpty(resource_type) ? "" : resource_type);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_INSTALL.equals(actionType) || ReportEvent.ACTION_UPDATEINSTALL.equals(actionType) || ReportEvent.ACTION_AD_INSTALL.equals(actionType)) {//应用安装/平台安装
            InstallEventBean data = new InstallEventBean();
            data.setAction_type(actionType);
            data.setNet(netWorkType);
            data.setApp_id(TextUtils.isEmpty(id) ? "" : id);
            data.setEvent(event);
            data.setApp_type(appType);
            data.setInstall_type(download_type);//安装类型：第一次安装OR升级安装
            data.setInstall_mode(install_mode); //安装方式：系统装，自动装，ROOT装
            data.setDownload_mode(download_mode);
            data.setPkg_name(pkgName);
            data.setInstall_way(install_way);
            data.setPage(page);
            data.setPage_id(TextUtils.isEmpty(page_id) ? "" : page_id);
            data.setAd_type(ad_type);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setError(error == null ? "" : error);
            data.setEvent_detail(event_detail == null ? "" : event_detail);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            if ("installed".equals(event) && (AdMold.CHANG_WEI.equals(ad_type) || AdMold.WanKa.equals(ad_type))) {
                data.setEffective(effective);
            }
            data.setP1(p1);
            data.setP2(p2);
            data.setFrom_page(TextUtils.isEmpty(from_page) ? "" : from_page);
            data.setWord(TextUtils.isEmpty(keyWord) ? "" : keyWord);
            data.setFrom_id(TextUtils.isEmpty(from_id) ? "" : from_id);
            data.setResource_type(TextUtils.isEmpty(resource_type) ? "" : resource_type);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_PUSH.equals(actionType)) {//推送
            PushEventBean data = new PushEventBean();
            data.setAction_type(actionType);
            data.setPush_id(TextUtils.isEmpty(id) ? "" : id);
            data.setEvent(event);
            data.setPush_type("APP".equals(download_type) ? "APP(" + presentType + ")" : "GETUI(" + presentType + ")");
            data.setNet(netWorkType);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            data.setApp_id(from_id);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_AWAKE.equals(actionType)) {//服务唤醒
            AwakeEventBean data = new AwakeEventBean();
            data.setAction_type(actionType);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setNet(netWorkType);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_APP_START.equals(actionType)) {//应用启动页面
            StartEventBean data = new StartEventBean();
            data.setAction_type(actionType);
            data.setApp_info(packageInfo);
            data.setNet(netWorkType);
            data.setIs_screenOn(isScreenOn);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_APP_END.equals(actionType)) {//应用退出
            QuitAppBean data = new QuitAppBean();
            data.setAction_type(actionType);
            data.setNet(netWorkType);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_DELETE.equals(actionType)) {//下载管理删除下载任务或者安装任务
            DeleteEventBean data = new DeleteEventBean();
            data.setAction_type(actionType);
            data.setPage(page);
            data.setApp_id(id);
            data.setEvent(event);
            data.setApp_type(appType);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setNet(netWorkType);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_AD_SHOW.equals(actionType)) {
            AdShowBean data = new AdShowBean();
            data.setAction_type(actionType);
            data.setPageName(page);
            data.setAdListStr(adListStr);
            data.setCurPage(curPage);
            data.setAdMold(ad_type);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setNet(netWorkType);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            s = gson.toJson(data);
        } else if (ReportEvent.ACTION_AD_REPORT.equals(actionType)) {
            AdReportBean data = new AdReportBean();
            data.setAction_type(actionType);
            data.setAd_source(source);
            data.setEvent(event);
            data.setAd_type(ad_type);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setNet(netWorkType);
            data.setImei(TextUtils.isEmpty(imei) ? "" : imei);
            data.setId(id);
            s = gson.toJson(data);
        }else if (ReportEvent.ACTION_ACTIVE.equals(actionType)) {
            AcitiveBean data = new AcitiveBean();
            data.setAction_type(actionType);
            data.setEvent(event);
            data.setTime(currentTime);
            data.setTimeformat(timeFormat);
            data.setNet(netWorkType);
            data.setTask_id(task_id);
            data.setAction_name(action_name);
            data.setType(type);
            data.setPkgName(pkgName);
            data.setShow_type(show_type);
            data.setClass_name(class_name);
            data.setDeep_link(deep_link);
            s = gson.toJson(data);
        } else {
            s = "获取ActionType错误";
        }
        return s;
    }


}

