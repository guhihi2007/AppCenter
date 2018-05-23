package cn.lt.android.statistics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mobstat.StatService;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.wanka.WanKaLog;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.ads.wanka.WanKaUrl;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.StatisticsEntity;
import cn.lt.android.db.WakeTaskEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.MainActivity;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckIsApkFile;
import cn.lt.android.util.FromPageManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.entity.SilentTask;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/***
 * Created by Administrator on 2015/12/28.
 */
public class DCStat {
    /***
     * 页面浏览/跳转/切换上报
     *
     * @param data
     */
    public static synchronized void pageJumpEvent(StatisticsEventData data) {
        try {
            if (data != null && !TextUtils.isEmpty(data.getPage()) && !data.getPage().equals(data.getFrom_page()) || data.getPage().equals(data.getFrom_page()) && data.getPage().equals(Constant.PAGE_DETAIL)) {
                if (data.getPage().equals(Constant.PAGE_BIBEI)) {
                    LTApplication.instance.is_bibei = data.getPage();//装机必备，推广图等弹框不存内存。
                }
                if (!data.getPage().equals(Constant.PAGE_BIBEI) && !data.getPage().equals(Constant.PAGE_SPREAD) && !data.getPage().equals("gr_gx")) {
                    LTApplication.instance.current_page = data.getPage();//装机必备，推广图等弹框不存内存。
                    LogUtils.i("Erosion", "fwefewffe=====" + data.getId() + ",page===" + data.getPage());
                    FromPageManager.setLastPage(data.getPage());
                    FromPageManager.setLastPageId(data.getId());
                }
                if (FromPageManager.isWordByPage(data.getPage()) || FromPageManager.isWordByLastPage(FromPageManager.getLastPage())) {
                    data.setKeyWord(LTApplication.instance.word);
                } else {
                    data.setKeyWord("");
                }
                data.setFrom_page(FromPageManager.getLastPage());

                if (FromPageManager.setFromIdByPage()) {
                    LogUtils.i("Erosion", "from_id=====" + FromPageManager.getLastPageId());
                    data.setFrom_id(FromPageManager.getLastPageId());
                }

                StatManger.self().submitDataToService(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 点击事件上报
     *
     * @param data
     */
    public static synchronized void clickEvent(StatisticsEventData data) {
        try {
            if (data != null && !TextUtils.isEmpty(data.getActionType())) {
                data.setPage(LTApplication.instance.current_page);
                LogUtils.i("Erosion", "mPageName:::" + LTApplication.instance.current_page);
                if (FromPageManager.isWordByPage(data.getPage())) {
                    data.setKeyWord(LTApplication.instance.word);
                } else {
                    data.setKeyWord("");
                }
                StatManger.self().submitDataToService(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 搜索事件统计
     *
     * @param keyWord
     * @param pageName
     */
    public static synchronized void searchEvent(String keyWord, String pageName, String pagePP) {
        try {
            if (!TextUtils.isEmpty(pageName)) {
                StatisticsEventData data = new StatisticsEventData();
                data.setActionType(ReportEvent.ACTION_SEARCH);
                if (TextUtils.isEmpty(pagePP)) {
                    data.setPage(LTApplication.instance.current_page);   //暂时直接从内存去拿
                } else {
                    data.setPage(LTApplication.instance.current_page + pagePP);
                }
                data.setKeyWord(keyWord);
                StatManger.self().submitDataToService(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baiduStat(null, "onclick", "搜索事件，关键字：" + keyWord);
        }
    }

    /***
     * 搜索推荐点击事件统计
     *
     * @param keyWord
     */
    public static synchronized void searchRecommendClickEvent(String keyWord, String appType, int p2) {
        try {
            if (!TextUtils.isEmpty(keyWord)) {
                StatisticsEventData data = new StatisticsEventData();
                data.setActionType(ReportEvent.ACTION_SEARCH_RECOMMEND);
                data.setKeyWord(keyWord);
                data.setResource_type(appType);
                data.setP2(p2);
                StatManger.self().submitDataToService(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DCStat.baiduStat(null, "onclick", "搜索推荐点击事件(类型):" + keyWord + "(" + appType + ")");
        }
    }

    /***
     * @param game         当前下载对象
     * @param mode         事件模式：manual 代表事件由用户产生  auto 代表系统自动处理行为
     * @param event        事件类型
     * @param pageName     页面名称
     * @param pageId       页面ID (游戏详情页、分类详情页、专题详情页，其他页面为空)
     * @param event_reason 事件详细信息：描述事件发生的详细信息
     * @param source       业务来源  (普通下载、推送、一键下载、应用自动升级下载、预约wifi下载、一键升级下载等)
     */
    public static synchronized void downloadRequestReport(AppEntity game, String mode, String event, String pageName, String pageId, String event_reason, String source) {
        if (null != game) {
            try {
                int status = game.getStatus();
                LogUtils.i("Erosion", "downloadRequestReport status=====" + game.p1 + ",p2===" + game.p2);
                StatisticsEventData data = new StatisticsEventData();
                StatisticsEntity entity = getEntityFromDB(game.getPackageName());
                data.setPage(TextUtils.isEmpty(pageName) ? entity == null ? "" : entity.getMPage() : pageName);
                data.setActionType(game.isAdData() ? ReportEvent.ACTION_AD_DOWNLOAD : ReportEvent.ACTION_DOWNLOAD);
                data.setPkgName(game.getPackageName());
                data.setDownloadState(game.getStatus());
                data.setAppType(game.getApps_type());
                data.setAd_type(game.getAdMold());      // 豌豆荚/玩咖API广告 或者没有
                if (game.getReportData() != null) {
                    if (!AdMold.WanKa.equals(game.getAdMold())) {
                        data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                    }
                }
                data.setId(game.getAppClientId());
                data.setEvent(event);
                data.setDownload_mode(mode);
                data.setDownloadSize(game.getSoFar());
                data.setPageID(pageId);
                data.setSource(source);
                data.setP1(game.p1);
                data.setP2(game.p2);
                data.setFrom_page(FromPageManager.getLastPage());
                data.setResource_type(TextUtils.isEmpty(game.resource_type) ? "" : game.resource_type);

                if (FromPageManager.isWordByPage(pageName) || FromPageManager.isWordByLastPage(FromPageManager.getLastPage())) {
                    data.setKeyWord(LTApplication.instance.word);
                } else {
                    data.setKeyWord("");
                }
                if (FromPageManager.setFromIdByPage()) {
                    data.setFrom_id(FromPageManager.getLastPageId());
                }
                if (game.getIsOrderWifiUpgrade() == InstallState.upgrade || "app_auto_upgrade".equals(source)) {
                    data.setDownload_type("upgrade");
                } else {
                    boolean isInstalled = AppUtils.isInstalled(game.getPackageName());
                    data.setDownload_type(InstallState.upgrade == status ? "upgrade" : (isInstalled ? "upgrade" : "first")); //当任务是重试状态下，获取不到升级状态，这里通过判断是否已安装确定是否是升级 ATian
                }
                data.setEvent_detail(event_reason);   //事件的详细原因
                if ("auto".equals(mode)) {
                    if (pageName.equals("notification")) {
                        data.setPage(pageName);
                    } else {
                        data.setPage(entity == null ? LTApplication.instance.current_page : entity.getMPage());   //如果是自动下载页面名称要从数据库中查询最后一次的页面名称，防止客户端升级后页面名称失效
                    }
                }
                if ("request".equals(event) || "upgrade".equals(event)) {//如果是下载或者升级则保存至数据库为后面的其他操作提供数据
                    LogUtils.i("DCState", "保存downloadType:" + status);
                    StatManger.self().saveDownloadTempData(data);
                } else {
                    data.setDownload_type(entity == null ? "first" : entity.getMDownloadType());
                    if ("app_auto_upgrade".equals(source)) {
                        data.setDownload_type("upgrade");
                    }
                    StatManger.self().submitDataToService(data);   //上报
                }

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i("DCStat", "数据异常===" + e.getMessage());
            } finally {
                if (!"request".equals(event)) {
                    baiduStat(null, getEventName(event), game.getPackageName());
                }
            }
        }
    }

    /**
     * 下载上报
     * 只有真正发生下载才会走这里上报下载请求上报
     *
     * @param appEntity
     */
    public static synchronized void downloadRequestReport(AppEntity appEntity) {
        if (null != appEntity) {
            try {
                StatisticsEntity statisticsEntity = getEntityFromDB(appEntity.getPackageName());
                StatisticsEventData data = new StatisticsEventData();
                if (null != statisticsEntity) {
                    data.setPage(statisticsEntity.getMPage());
                    data.setActionType(appEntity.isAdData() ? ReportEvent.ACTION_AD_DOWNLOAD : ReportEvent.ACTION_DOWNLOAD);
                    data.setPkgName(appEntity.getPackageName());
                    data.setDownloadState(appEntity.getStatus());
                    data.setAppType(appEntity.getApps_type());

//                    data.setAd_type(appEntity.isAdData() ? "wandoujia" : "");//如果非广告不需要ad_type字段
                    data.setAd_type(appEntity.getAdMold());      // 豌豆荚/玩咖API广告 或者没有
                    if (appEntity.getReportData() != null) {
                        if (!AdMold.WanKa.equals(appEntity.getAdMold())) {
                            data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                        }
                    }
                    data.setId(appEntity.getAppClientId());
                    data.setEvent("request");
                    data.setEffective(true);
                    data.setDownload_type(statisticsEntity.getMDownloadType());
                    data.setDownload_mode(statisticsEntity.getMDownloadMode());
                    data.setDownloadSize(appEntity.getSoFar());
                    data.setPageID(statisticsEntity.getMPageID());
                    data.setEvent_detail(statisticsEntity.getMRemark());//用Remark字段查询时间详细原因
                    int tempP1 = 0;
                    int tempP2 = 0;
                    if (statisticsEntity.getP1() != null) {
                        tempP1 = statisticsEntity.getP1();
                    }
                    if (statisticsEntity.getP2() != null) {
                        tempP2 = statisticsEntity.getP2();
                    }
                    data.setP1(tempP1);
                    data.setP2(tempP2);

                    data.setFrom_page(statisticsEntity.getFrom_page());
                    data.setResource_type(statisticsEntity.getResource_type());
                    data.setKeyWord(statisticsEntity.getWord());
                    data.setFrom_id(statisticsEntity.getFrom_id());
                    StatManger.self().submitDataToService(data);   //上报
                } else {
                    LogUtils.i("DCStat", "没有查询到下载数据");
                    data.setActionType(appEntity.isAdData() ? ReportEvent.ACTION_AD_DOWNLOAD : ReportEvent.ACTION_DOWNLOAD);
                    data.setPkgName(appEntity.getPackageName());
                    data.setDownloadState(appEntity.getStatus());
                    data.setAppType(appEntity.getApps_type());
                    data.setAd_type(appEntity.getAdMold());      // 豌豆荚/玩咖API广告 或者没有
                    if (appEntity.getReportData() != null) {
                        if (!AdMold.WanKa.equals(appEntity.getAdMold())) {
                            data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                        }
                    }
                    data.setId(appEntity.getAppClientId());
                    data.setEvent("request");
                    data.setEffective(true);
                    StatManger.self().submitDataToService(data);   //上报
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                baiduStat(null, "app_download_request", "下载请求：" + appEntity.getPackageName());
            }
        }


    }


    /***
     * 下载完成
     *
     * @param game
     */
    public static synchronized void downloadCompletedEvent(final AppEntity game) {
        if (game != null) {
            try {
                final StatisticsEventData data = new StatisticsEventData();

                data.setActionType(game.isAdData() ? ReportEvent.ACTION_AD_DOWNLOAD : ReportEvent.ACTION_DOWNLOAD);
                data.setEvent("downloaded");
                String realPakageName = CheckIsApkFile.getPackageNameByPackageManager(game.getSavePath(), LTApplication.shareApplication());
                data.setPkgName(realPakageName != null ? realPakageName : game.getPackageName());
                StatisticsEntity entity = getEntityFromDB(game.getPackageName());
                data.setDownload_type(entity == null ? "" : entity.getMDownloadType());
                data.setPage(entity == null ? "" : entity.getMPage());
                data.setId(game.getAppClientId());
                data.setPageID(entity == null ? "" : entity.getMPageID());
                data.setDownload_mode(entity == null ? "normal" : entity.getMDownloadMode());
                data.setEvent_detail(entity == null ? "" : entity.getMRemark());
                data.setAppType(game.getApps_type());
                data.setDownloadSize(0);
                data.setAd_type(game.getAdMold());   // 豌豆荚/玩咖API广告 或者没有
                int tempP1 = 0;
                int tempP2 = 0;
                if (entity != null) {
                    if (entity.getP1() != null) {
                        tempP1 = entity.getP1();
                    }
                    if (entity.getP2() != null) {
                        tempP2 = entity.getP2();
                    }
                }

                data.setP1(tempP1);
                data.setP2(tempP2);
                data.setFrom_id(entity == null ? "" : entity.getFrom_id());
                data.setFrom_page(entity == null ? "" : entity.getFrom_page());
                data.setResource_type(entity == null ? "" : entity.getResource_type());
                data.setKeyWord(entity == null ? "" : entity.getWord());
                if (game.getReportData() != null) {
                    if (!AdMold.WanKa.equals(game.getAdMold())) {
                        data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                    }
                    // 在上报之前先询问服务器是否是玩咖的商务包
                    WanKaManager.whetherBusiness(game.getPackageName(), new SimpleResponseListener<JSONObject>() {
                        @Override
                        public void onSucceed(int what, Response<JSONObject> response) {
                            data.setEffective(true);
                        }

                        @Override
                        public void onFailed(int what, Response<JSONObject> response) {
                            data.setEffective(false);
                        }

                        @Override
                        public void onFinish(int what) {
                            StatManger.self().submitDataToService(data);
                        }
                    }, "下载完成请求是否玩咖商务包");
                } else {
                    StatManger.self().submitDataToService(data);
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                baiduStat(null, "app_download_success", "下载成功:" + game.getPackageName());
            }


            if (game.getReportData() != null) {
                // 下载完成玩咖上报Api调用
                WanKaManager.doRequest(WanKaUrl.DOWNLOAD_SUCCESS, game.getPackageName(), new SimpleResponseListener<JSONObject>() {
                    @Override
                    public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                        WanKaLog.e("玩咖应用下载成功：" + response.get().toString());
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                        WanKaLog.e("玩咖应用下载成功，但是玩咖下载上报接口调用fail：" + response.toString());
                    }
                });
            } else {
                WanKaLog.e("非玩咖应用下载成功：" + game.getPackageName());
            }

        }
    }

    /***
     * 统计下载失败/错误
     *
     * @param game
     * @param error
     */
    @SuppressWarnings("all")
    public static synchronized void downloadFialedEvent(AppEntity game, String download_mode, String error, String pageName, String error_detail, String event, String pageID) {
        try {
            if (null != game) {
                StatisticsEventData data = new StatisticsEventData();
                StatisticsEntity statisticsEntity = getEntityFromDB(game.getPackageName());
                data.setActionType(game.isAdData() ? ReportEvent.ACTION_AD_DOWNLOAD : ReportEvent.ACTION_DOWNLOAD);
                data.setId(game.getAppClientId());
                data.setAppType(game.getApps_type());
                data.setEvent(event);
                data.setSuccess(1);
                data.setPkgName(game.getPackageName());
                data.setDownload_mode(TextUtils.isEmpty(download_mode) ? (statisticsEntity == null ? "normal" : statisticsEntity.getMDownloadMode()) : download_mode);
//                data.setDownload_type(statisticsEntity == null ? "first" : statisticsEntity.getMDownloadType());
                data.setDownload_type(statisticsEntity == null ? (game.getStatus() == InstallState.upgrade ? "upgrade" : "first") : statisticsEntity.getMDownloadType());
//                data.setDownload_type(InstallState.upgrade == game.getStatus() ? "upgrade" : "first" );
                data.setAd_type(game.getAdMold());
                data.setKeyWord(statisticsEntity == null ? "" : statisticsEntity.getWord());

                int tempP1 = 0;
                int tempP2 = 0;
                if (statisticsEntity != null) {
                    if (statisticsEntity.getP1() != null) {
                        tempP1 = statisticsEntity.getP1();
                    }
                    if (statisticsEntity.getP2() != null) {
                        tempP2 = statisticsEntity.getP2();
                    }
                }

                data.setP1(tempP1);
                data.setP2(tempP2);
                data.setFrom_page(statisticsEntity == null ? "" : statisticsEntity.getFrom_page());
                data.setResource_type(statisticsEntity == null ? "" : statisticsEntity.getResource_type());
                data.setFrom_id(statisticsEntity == null ? "" : statisticsEntity.getFrom_id());
                if (game.getReportData() != null) {
                    if (!AdMold.WanKa.equals(game.getAdMold())) {
                        data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                    }
                }
                data.setError(error);
                data.setPage(TextUtils.isEmpty(pageName) ? statisticsEntity == null ? "" : statisticsEntity.getMPage() : pageName);
                data.setPageID(TextUtils.isEmpty(pageID) ? statisticsEntity == null ? "" : statisticsEntity.getMPageID() : pageID);
                data.setEvent_detail(error_detail);
                StatManger.self().submitDataToService(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baiduStat(null, "app_download_failed", "下载失败：" + game.getPackageName());
        }
    }

    /**
     * 安装成功统计
     *
     * @param pkgName
     */
    public static synchronized void installSuccessEvent(final String pkgName) {
        LogUtils.i("install", "installSuccessEvent++++" + pkgName);
        if (!TextUtils.isEmpty(pkgName)) {
            try {
                final StatisticsEventData data = new StatisticsEventData();

                Observable.just(pkgName).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                        .map(new Func1<String, StatisticsEntity>() { //转换类型
                            @Override
                            public StatisticsEntity call(String s) {
                                return getEntityFromDB(pkgName);
                            }
                        }).observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                        .subscribe(new Observer<StatisticsEntity>() {
                            @Override
                            public void onCompleted() {
                                deleteAppEntityByPkg(pkgName);
                                LogUtils.i("install", "统计信息删除完成");
                            }

                            @Override
                            public void onError(Throwable e) {
                                LogUtils.i("install", "installSuccessEvent+++Throwable" + pkgName);
                            }

                            @Override
                            public void onNext(StatisticsEntity statisticsEntity) {
                                if (null != statisticsEntity) {
                                    StatisticsEntity clone = null;
                                    try {
                                        clone = statisticsEntity.clone();
                                    } catch (CloneNotSupportedException e) {
                                        e.printStackTrace();
                                    }
                                    AppEntity appEntity = LTApplication.installStatAppList.remove(pkgName);
                                    reportNormalInstalledEvent(pkgName, data, clone == null ? statisticsEntity : clone, appEntity);
                                    reportWankaInstalledEvent(appEntity);
                                } else {
                                    reportOtherMarketInstalledEvent(pkgName, data);
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("install Success 上报出错了：" + e.toString());
            } finally {
                baiduStat(null, "app_install_success", "安装成功：" + pkgName);
            }
        }


    }

    /**
     * 第三方安装完成上报
     *
     * @param pkgName
     * @param data
     */
    private static void reportOtherMarketInstalledEvent(String pkgName, StatisticsEventData data) {
        data.setActionType(ReportEvent.ACTION_INSTALL);
        data.setEvent("installed");
        data.setInstall_way("other_market");
        data.setPkgName(pkgName);
        StatManger.self().submitDataToService(data);
    }

    /**
     * 玩咖安装完成上报
     *
     * @param appEntity
     */
    private static void reportWankaInstalledEvent(final AppEntity appEntity) {
        if (appEntity.getReportData() != null) {
            WanKaManager.doRequest(WanKaUrl.INSTALL, appEntity.getPackageName(), new SimpleResponseListener<JSONObject>() {
                @Override
                public void onSucceed(int what, Response<JSONObject> response) {
                    WanKaLog.e("玩咖应用安装成功：" + response.get().toString());
//                    replaceAppEntity(appEntity);
                }

                @Override
                public void onFailed(int what, Response<JSONObject> response) {
                    WanKaLog.e("玩咖应用安装成功，但是玩咖安装上报接口调用fail：" + response.toString());
//                    replaceAppEntity(appEntity);
                }

//                private void replaceAppEntity(AppEntity appEntity) {
//                    appEntity.setReportDataJsonObj(null);
//                    GlobalParams.getAppEntityDao().insertOrReplace(appEntity);
//                }
            });
        }
    }

    /**
     * 本平台安装完成上报
     *
     * @param pkgName
     * @param data
     * @param entity
     * @param appEntity
     */
    private static void reportNormalInstalledEvent(String pkgName, final StatisticsEventData data, StatisticsEntity entity, AppEntity appEntity) {
        if (appEntity != null) {
            LogUtils.i("install", "reportNormalInstalledEvent+++installed" + pkgName);
            data.setActionType(appEntity.isAdData() ? ReportEvent.ACTION_AD_INSTALL : ReportEvent.ACTION_INSTALL);
            data.setEvent("installed");
            try {
                data.setPage(entity.getMPage());
                data.setPageID(entity.getMPageID());
                data.setId(appEntity.getAppClientId());
                data.setDownload_type(entity.getMDownloadType());
                data.setDownload_mode(entity.getMDownloadMode());
                data.setInstall_mode(entity.getMInstallMode());
                data.setAppType(appEntity.getApps_type());
                data.setPkgName(pkgName);
                data.setInstall_way(entity.getMInstallWay());
                data.setAd_type(appEntity.getAdMold());      // 豌豆荚/玩咖API广告 或者没有
                data.setEvent_detail(entity.getMRemark());
                data.setFrom_page(entity.getFrom_page());
                data.setFrom_id(entity.getFrom_id());
                data.setResource_type(entity.getResource_type());
                data.setKeyWord(entity.getWord());
            } catch (Exception e) {
                e.printStackTrace();
            }
            int tempP1 = 0;
            int tempP2 = 0;
            if (entity != null) {
                if (entity.getP1() != null) {
                    tempP1 = entity.getP1();
                }
                if (entity.getP2() != null) {
                    tempP2 = entity.getP2();
                }
            }

            data.setP1(tempP1);
            data.setP2(tempP2);

            LogUtils.i("install", "reportNormalInstalledEvent++++" + pkgName);
            if (appEntity.getReportData() != null) {
                if (!AdMold.WanKa.equals(appEntity.getAdMold())) {
                    data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                }
                // 在上报之前先询问服务器是否是玩咖的商务包
                WanKaManager.whetherBusiness(pkgName, new SimpleResponseListener<JSONObject>() {
                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        data.setEffective(true);
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                        data.setEffective(false);
                    }

                    @Override
                    public void onFinish(int what) {
                        StatManger.self().submitDataToService(data);
                    }
                }, "安装完成请求是否玩咖商务包");
            } else {
                StatManger.self().submitDataToService(data);
            }
        }
    }

    /***
     * 安装/自动装统计
     *
     * @param entity
     */
    public static synchronized void installEvent(AppEntity entity, boolean installWay, String installMode, String page, String pageId, String error, String error_detail) {

        if (null != entity) {
            try {
                StatisticsEventData data = new StatisticsEventData();
                data.setActionType(entity.isAdData() ? ReportEvent.ACTION_AD_INSTALL : ReportEvent.ACTION_INSTALL);
                data.setEvent("install");
                if (page.equals(Constant.QUIT_DIALOG)) {
                    data.setInstall_way("onekey_exit");
                } else {
                    data.setInstall_way(installWay ? "onekey" : "single");
                }
                data.setInstall_mode(installMode);
                String realPakageName = CheckIsApkFile.getPackageNameByPackageManager(entity.getSavePath(), LTApplication.shareApplication());
                data.setPkgName(realPakageName != null ? realPakageName : entity.getPackageName());
                data.setError(error);
                StatisticsEntity sEntity = getEntityFromDB(entity.getPackageName());
                data.setEvent_detail(TextUtils.isEmpty(error_detail) ? (null == sEntity ? "" : sEntity.getMRemark()) : error_detail);
                data.setDownload_type(sEntity.getMDownloadType());
                data.setDownload_mode(sEntity.getMDownloadMode());
                if (TextUtils.isEmpty(page)) {
                    data.setPage(sEntity.getMPage());
                } else {
                    data.setPage(page);
                }
                if (TextUtils.isEmpty(pageId)) {
                    data.setPageID(sEntity.getMPageID());
                } else {
                    data.setPageID(pageId);
                }
                data.setAppType(entity.getApps_type());
                data.setId(entity.getAppClientId());
                data.setP1(entity.p1);
                data.setP2(entity.p2);
                data.setFrom_page(FromPageManager.getLastPage());
                data.setResource_type(TextUtils.isEmpty(entity.resource_type) ? "" : entity.resource_type);
                if (TextUtils.isEmpty(entity.word)) {
                    data.setKeyWord("");
                } else {
                    data.setKeyWord(entity.word);
                }
                if (FromPageManager.setFromIdByPage()) {
                    LogUtils.i("Erosion", "from_id=====" + LTApplication.instance.from_id);
                    data.setFrom_id(FromPageManager.getLastPageId());
                }

                data.setAd_type(entity.getAdMold());      // 豌豆荚/玩咖API广告 或者没有
                if (entity.getReportData() != null) {
                    if (!AdMold.WanKa.equals(entity.getAdMold())) {
                        data.setAd_type(AdMold.CHANG_WEI);             // 玩咖长尾
                    }
                }

                StatManger.self().submitDataToService(data);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (TextUtils.isEmpty(error)) {
                    if (installWay) {
                        baiduStat(null, "app_install", "应用一键安装:" + entity.getPackageName());
                    } else {
                        baiduStat(null, "app_install", "应用安装:" + entity.getPackageName());
                    }
                } else {
                    if ("memoryError".equals(error)) {
                        baiduStat(null, "app_install_failed", "应用安装失败(包名):" + entity.getPackageName() + "   失败原因：" + error_detail);
                    }
                }
            }
        }
    }

    private static StatisticsEntity getEntityFromDB(String packageName) {
        return DownloadTaskManager.getInstance().getStasticByPkg(packageName);
    }


    /***
     * 平台升级统计
     *
     * @param event
     * @param updateType
     * @param installMode
     * @param error       //错误类型
     * @param source      //来源:页面点击、服务触发、通知栏点击
     */
    public static synchronized void platUpdateEvent(String event, String source, String updateType, String installMode, String error, String event_detail) {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setActionType(ReportEvent.ACTION_PLATUPGRADE);
            data.setEvent(event);
            data.setSrcType(updateType);
            data.setError(error);
            data.setEvent_detail(event_detail);
            data.setSource(source);
            data.setInstall_mode(installMode);
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if ("downloaded".equals(event)) {
                baiduStat(null, "client_download_success", "客户端下载完成");
            } else if ("request".equals(event)) {
                baiduStat(null, "client_download_request", "客户端下载请求");
            } else if ("download_error".equals(event)) {
                baiduStat(null, "client_download_failed", "客户端下载出错，原因：" + event_detail);
            } else if ("retry".equals(event)) {
                baiduStat(null, "client_download_retry", "客户端下载重试");
            } else if ("install_error".equals(event)) {
                baiduStat(null, "client_install_failed", "客户端安装失败，原因：" + event_detail);
            } else if ("install".equals(event)) {
                baiduStat(null, "client_install", "客户端安装");
            } else if ("installed".equals(event)) {
                baiduStat(null, "client_install_success", "客户端安装成功");
            }
        }
    }


    /***
     * 用户安装应用列表
     *
     * @param pkgInfo
     */
    public static synchronized void appStart(Context context, String pkgInfo) {
        boolean isScreenOn = AppUtils.isScreenOn();
        try {
            if (!TextUtils.isEmpty(pkgInfo)) {
                StatisticsEventData data = new StatisticsEventData();
                data.setActionType(ReportEvent.ACTION_APP_START);
                data.setPackageInfo(pkgInfo);
                data.setScreenOn(isScreenOn);
                StatManger.self().submitDataToService(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baiduStat(context, "client_start", "客户端启动");
        }
    }

    /***
     * 用户安装应用列表
     */
    public static synchronized void quitAppCenter() {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setActionType(ReportEvent.ACTION_APP_END);
            data.setFrom_page("");
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EventBus.getDefault().post(new MainActivity.EventBean(Constant.CORNER_COUNT));
            baiduStat(null, "client_quit", "客户端退出");
        }
    }

    /***
     * 唤醒事件统计
     */
    public static synchronized void awake() {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setAction_type(ReportEvent.ACTION_AWAKE);
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baiduStat(null, "awake", "唤醒服务");
        }
    }

    /***
     * @param pushId
     * @param type
     * @param event
     * @param push_type push/app
     */
    public static synchronized void pushEvent(String pushId, String type, String event, String push_type, String appId) {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setAction_type(ReportEvent.ACTION_PUSH);
            data.setId(pushId);
            data.setPresentType(type);
            data.setDownload_type(push_type);//推送类型
            data.setEvent(event);
            data.setFrom_id(appId);// 用来装推送的应用id
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baiduStat(null, "push", "推送事件：" + "pushId:" + pushId + ";type:" + type + ";event:" + event + ";pushType:" + push_type);
        }
    }

    /***
     * 删除下载任务/安装包
     *
     * @param appEntity
     */
    public static synchronized void delete(Context context, AppEntity appEntity, String event) {
        if (null != appEntity) {
            try {
                StatisticsEventData data = new StatisticsEventData();
                data.setActionType(ReportEvent.ACTION_DELETE);
                data.setId(appEntity.getAppClientId());
                data.setPage(LTApplication.instance.current_page);   //暂时直接从内存去拿
                data.setEvent(event);
                data.setAppType(appEntity.getApps_type());
                StatManger.self().submitDataToService(data);
                deleteAppEntityByPkg(appEntity.getPackageName());//删除任务成功把对应的统计数据也删除，防止出现下载页面出现问题
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                baiduStat(context, "onclick", "安装包删除事件（包名）： " + appEntity.getPackageName());
            }
        }
    }


    /***
     * 广告点击事件
     *
     * @param pkgName
     * @param category
     */
    public static synchronized void adClickReport(String pkgName, String category, String adMold) {
        try {
            if (TextUtils.isEmpty(pkgName)) {
                StatisticsEventData data = new StatisticsEventData();
                data.setActionType(ReportEvent.ACTION_ADCLICK);
                data.setAppType(category);
                data.setId(pkgName);
                data.setPage(LTApplication.instance.current_page);   //暂时直接从内存去拿
                data.setAd_type(adMold);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baiduStat(null, "onclick", "广告位点击事件：" + pkgName);
        }
    }

    /**
     * 更新安装模式
     *
     * @param mode
     * @param appEntity
     */
    public static synchronized void updateInstallMode(String mode, String installWay, AppEntity appEntity) {
        try {
            StatisticsEntity app = getEntityFromDB(appEntity.getPackageName());
            if (null != app) {
                app.setMInstallMode(mode);
                app.setMInstallWay(installWay);
                GlobalParams.getStatisticsEntityDao().update(app);
                LogUtils.i("DCStat", "更新统计表状态");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 删除任务时根据包名删除对应的实体
     *
     * @param pkg
     */
    private static synchronized void deleteAppEntityByPkg(String pkg) {
        if (!TextUtils.isEmpty(pkg)) {
            try {
                StatisticsEntity app = getEntityFromDB(pkg);
                if (null != app) {
                    Log.i("DCStat", "删除统计表中的数据，pkgName=" + pkg);
                    GlobalParams.getStatisticsEntityDao().delete(app);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    /****
     * 百度统计的简单封装
     *
     * @param context
     * @param eventId
     * @param eventContent
     */
    public synchronized static void baiduStat(Context context, String eventId, String eventContent) {
        Context mContext = context;
        if (null == context) {
            mContext = LTApplication.shareApplication();
        }
        StatService.onEvent(mContext, eventId, eventContent, 1);//调用百度统计SDK
        LogUtils.i("ttt", "事件ID：" + eventId + " ||  上报名称：" + eventContent);

    }

    /***
     * 兼容百度统计自定义事件
     *
     * @param event
     * @return
     */
    private static String getEventName(String event) {
        switch (event) {
            case "request":
                return "app_download_request";
            case "pause":
                return "app_download_pause";
            case "continue":
                return "app_download_continue";
            case "retry":
                return "app_download_retry";
            case "upgrade":
                return "app_upgrade_request";
        }
        return "";
    }


    /***
     * 广告api展示统计事件
     */
    public static synchronized void adShow(String adListStr, String pageName, int curPage, String adMold) {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setAction_type(ReportEvent.ACTION_AD_SHOW);
            data.setAdListStr(adListStr);
            data.setPage(pageName);
            data.setCurPage(curPage);
            data.setAd_type(adMold);
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 开屏广告统计事件
     */
    public static void adReport(String event, String ad_source, String ad_type, String adId) {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setAction_type(ReportEvent.ACTION_AD_REPORT);
            data.setEvent(event);
            data.setSource(ad_source);
            data.setAd_type(ad_type);
            data.setId(adId);
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 拉活第三方应用事件
     */
    public static void activeEvent(String event, String task_id, SilentTask silentTask, WakeTaskEntity entity) {
        try {
            StatisticsEventData data = new StatisticsEventData();
            data.setAction_type(ReportEvent.ACTION_ACTIVE);
            data.setEvent(event);
            data.setTask_id(task_id);
            if(null!=silentTask) {
                data.setAction_name(silentTask.action_name);
                data.setType(silentTask.type);
                data.setPkgName(silentTask.package_name);
                data.setShow_type(silentTask.show_type);
                data.setClass_name(silentTask.class_name);
                data.setDeep_link(silentTask.deep_link);
            }
            if(null!=entity) {
                data.setAction_name(entity.getAction_name());
                data.setType(entity.getType());
                data.setPkgName(entity.getPackage_name());
                data.setShow_type(entity.getShow_type());
                data.setClass_name(entity.getClass_name());
                data.setDeep_link(entity.getDeep_link());
            }
            StatManger.self().submitDataToService(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 拼接拉活任务集合的task_id*/
    public static String jointTaskIdBySilentTask(List<SilentTask> list) {
        String appIds = "";
        if(list != null) {
            for (int i = 0; i < list.size(); i++) {
                SilentTask game = list.get(i);
                if(i != list.size() - 1) {
                    appIds += game.task_id + " | ";
                } else {
                    appIds += game.task_id;
                }
            }
        }

        return appIds;
    }

}
