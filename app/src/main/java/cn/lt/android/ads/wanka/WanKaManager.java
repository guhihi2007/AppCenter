package cn.lt.android.ads.wanka;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.entity.PicTopicBean;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.network.bean.HeaderParams;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CallServer;
import cn.lt.framework.util.PreferencesUtils;

/**
 * Created by chon on 2016/12/27.
 * What? How? Why?
 * <p>
 * 玩咖网络请求
 */

public class WanKaManager {
    private WanKaManager() {

    }

    public static void whetherBusiness(String pkgName, final OnResponseListener<JSONObject> listener, final String... logStr) {
        String url = GlobalParams.getHostBean().getAcenter_host() + WanKaUrl.WHETHER_WANKA;
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(url, RequestMethod.POST);
        request.add("package", pkgName);
        request.addHeader("X-Client-Info", new Gson().toJson(new HeaderParams()));
        CallServer.getRequestInstance().add(0, request, new SimpleResponseListener<JSONObject>() {
            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                JSONObject jsonObject = response.get();
                if (jsonObject != null) {
                    int status = jsonObject.optInt("status");
                    if (status == 1) {
                        // status=1就是玩咖商务包 status=0就是没有
                        if (logStr != null && logStr.length > 0) {
                            WanKaLog.w(logStr[0] + " success: " + jsonObject.toString());
                        }
                        listener.onSucceed(what, response);
                    } else {
                        onFailed(what, response);
                    }
                } else {
                    onFailed(what, response);
                }
            }

            @Override
            public void onFailed(int what, Response<JSONObject> response) {
                if (logStr != null && logStr.length > 0) {
                    WanKaLog.e(logStr[0] + " onFailed: " + response.toString());
                }
                listener.onFailed(what, response);
            }

            @Override
            public void onFinish(int what) {
                listener.onFinish(what);
            }
        });
    }

    /**
     * 玩咖相关API的调用
     *
     * @param originUrl  url
     * @param reportData 应玩咖文档上报的reportData
     * @param listener   请求回调
     * @param extraData  额外的信息，暂时作log辅助信息
     * @param <T>        reportData 泛型信息
     */
    public static <T> void doRequest(String originUrl, T reportData, OnResponseListener<JSONObject> listener, final String... extraData) {
        WanKaRequestBean requestBean = WanKaRequestBean.generateBean(originUrl, reportData, extraData);
        Request<JSONObject> request = NoHttp.createJsonObjectRequest(requestBean.url, RequestMethod.POST);
        request.setDefineRequestBodyForJson(requestBean.requestBodyJson);

        CallServer.getRequestInstance().add(0, request, listener);
    }


    public static <T extends BaseBean> Set<String> exposureSingleApp(final T bean, final OnResponseListener<JSONObject> listener, final String... extraData) {
        List<BaseBean> list = new ArrayList<>();
        list.add(bean);
        return exposureApps(list, listener, extraData);
    }

    /**
     * 调用玩咖曝光接口
     *
     * @param originalList baseBean的一个集合，根据LtType来填充其中数据
     * @param listener     曝光回调
     */
    @SuppressWarnings("all")
    public static Set<String> exposureApps(final List<? extends BaseBean> originalList, final OnResponseListener<JSONObject> listener, final String... extraData) {
        // 玩咖接口开关状态
//        final boolean wanKaSwitchOn = !TextUtils.isEmpty(Constant.WK_SWITCH) && !Constant.STATUS_CLOSE.equals(Constant.WK_SWITCH);
        final boolean wanKaSwitchOn = PreferencesUtils.getBoolean(LTApplication.instance,Constant.WK_SWITCH);
        WanKaLog.e("玩咖接口开关状态：" + wanKaSwitchOn);
        final Set<String> exposurePkgs = new CopyOnWriteArraySet<>();

        // 不需要过滤的包名
        List<String> whiteList = new ArrayList<>();

        for (int i = 0; i < originalList.size(); i++) {
            BaseBean baseBean = originalList.get(i);
            if (baseBean != null) {
                if (baseBean instanceof AppEntity) {
                    // 下载之前的曝光
                    AppEntity entity = (AppEntity) baseBean;
                    exposurePkgs.add(entity.getPackageName());
                    whiteList.add(entity.getPackageName());
                    continue;
                }

                if ("app_topic".equals(baseBean.getLtType())) {
                    AppTopicBean bean = (AppTopicBean) baseBean;
                    List<AppBriefBean> briefBeanList = bean.getBriefApps();

                    for (int j = 0; j < briefBeanList.size(); j++) {
                        AppBriefBean briefBean = briefBeanList.get(j);
                        boolean isWankaCommercial = AdMold.WanKa.equals(briefBean.getAdMold());

                        // 玩咖商业列表的曝光
                        // 否则需要是后台玩咖接口开启，并且是可替换的非广告位
                        if (isWankaCommercial || (wanKaSwitchOn && briefBean.canReplace() && !AdMold.WanDouJia.equals(briefBean.getAdMold()))) {
                            // 如果是玩咖的广告是一定要曝光
                            exposurePkgs.add(briefBean.getPackage_name());

                            String exposurePkg = briefBean.getPackage_name();
                            filterInstalledPkg(exposurePkgs, exposurePkg, briefBean.getVersion_code());
                        }
                    }
                }

                if ("apps".equals(baseBean.getLtType())) {
                    @SuppressWarnings("unchecked") BaseBeanList<AppBriefBean> appList = (BaseBeanList<AppBriefBean>) baseBean;

                    for (int j = 0; j < appList.size(); j++) {
                        AppBriefBean briefBean = appList.get(j);
                        boolean isWankaCommercial = AdMold.WanKa.equals(briefBean.getAdMold());

                        // 玩咖商业列表的曝光
                        // 否则需要是后台玩咖接口开启，并且是可替换的非广告位
                        if (isWankaCommercial || (wanKaSwitchOn && briefBean.canReplace() && !AdMold.WanDouJia.equals(briefBean.getAdMold()))) {
                            exposurePkgs.add(briefBean.getPackage_name());

                            String exposurePkg = briefBean.getPackage_name();
                            filterInstalledPkg(exposurePkgs, exposurePkg, briefBean.getVersion_code());
                        }

                    }
                }

                if ("app".equals(baseBean.getLtType())) {
                    AppBriefBean briefBean = (AppBriefBean) baseBean;
                    boolean isWankaCommercial = AdMold.WanKa.equals(briefBean.getAdMold());

                    String exposurePkg = briefBean.getPackage_name();
                    // 玩咖商业列表的曝光
                    // 否则需要是后台玩咖接口开启，并且是可替换的非广告位
                    if (isWankaCommercial || (wanKaSwitchOn && briefBean.canReplace() && !AdMold.WanDouJia.equals(briefBean.getAdMold()))) {
                        exposurePkgs.add(exposurePkg);
                    }

                    filterInstalledPkg(exposurePkgs, exposurePkg, briefBean.getVersion_code());
                }

                // 升级页面数据 推送应用详情 弹窗推广  自动升级
                if (baseBean instanceof AppDetailBean) {
                    AppDetailBean detailBean = (AppDetailBean) baseBean;

                    boolean isWankaCommercial = AdMold.WanKa.equals(detailBean.getAdMold());

                    String exposurePkg = detailBean.getPackage_name();
                    // 玩咖商业列表的曝光
                    // 否则需要是后台玩咖接口开启，并且是可替换的非广告位
                    if (isWankaCommercial || (wanKaSwitchOn && detailBean.canReplace() && !AdMold.WanDouJia.equals(detailBean.getAdMold()))) {
                        exposurePkgs.add(exposurePkg);
                    }

                    // ------------------------------ 过滤已安装且不可升级 ------------------------------
                    filterInstalledPkg(exposurePkgs, exposurePkg, detailBean.getVersion_code());
                }

                if (wanKaSwitchOn) {

                    // 搜索推荐
                    if (baseBean instanceof BaseBeanList) {
                        String ltType = baseBean.getLtType();

                        if ("s_software_recommend".equals(ltType) || "s_game_recommend".equals(ltType)) {
                            BaseBeanList<BaseBean> list = (BaseBeanList) baseBean;

                            for (BaseBean bean : list) {
                                HotSearchBean searchBean = (HotSearchBean) bean;
                                if (searchBean.canReplace()) {
                                    String exposurePkg = searchBean.getPackage_name();
                                    exposurePkgs.add(exposurePkg);

                                    // 过滤已安装
                                    filterInstalledPkg(exposurePkgs, exposurePkg);
                                }
                            }
                        }
                    }

                    // 启动页应用入口 弹窗推广单个应用
                    if (baseBean instanceof AdsImageBean) {
                        AdsImageBean imgBean = (AdsImageBean) baseBean;
                        if (imgBean.getData().is_replace()) {
                            // 过滤已安装
                            String exposurePkg = imgBean.getData().getPackage_name();
                            exposurePkgs.add(exposurePkg);
                            filterInstalledPkg(exposurePkgs, exposurePkg);
                        }
                    }

                    // 精选页配置的单个应用详情入口 图片专题 轮播图
                    if ("sub_entry".equals(baseBean.getLtType()) || "entry".equals(baseBean.getLtType()) || "carousel".equals(baseBean.getLtType())) {
                        BaseBeanList<ClickTypeBean> entries = (BaseBeanList<ClickTypeBean>) baseBean;
                        for (ClickTypeBean entry : entries) {
                            if ("app_info".equals(entry.getClick_type()) && entry.getData().is_replace()) {
                                // 过滤已安装
                                String exposurePkg = entry.getData().getPackage_name();
                                exposurePkgs.add(exposurePkg);
                                filterInstalledPkg(exposurePkgs, exposurePkg);
                            }
                        }
                    }

                    // 图片专题
                    if ("pic_topic".equals(baseBean.getLtType())) {
                        PicTopicBean picTopicBean = (PicTopicBean) baseBean;
                        if ("app_info".equals(picTopicBean.getClick_type()) && picTopicBean.getData().is_replace()) {
                            // 过滤已安装
                            String exposurePkg = picTopicBean.getData().getPackage_name();
                            exposurePkgs.add(exposurePkg);
                            filterInstalledPkg(exposurePkgs, exposurePkg);
                        }
                    }

                    // 应用详情里边推荐的游戏
                    if ("recommend".equals(baseBean.getLtType())) {
                        RecommendBean recommendBean = (RecommendBean) baseBean;
                        if (recommendBean.is_replace()) {
                            // 过滤已安装
                            String exposurePkg = recommendBean.getPackage_name();
                            exposurePkgs.add(exposurePkg);
                            filterInstalledPkg(exposurePkgs, exposurePkg);
                        }
                    }

                }

            }
        }

        // 过滤
        for (String exposurePkg : exposurePkgs) {
            if (TextUtils.isEmpty(exposurePkg)) {
                // 包名没有获取到
                if (extraData != null && extraData.length > 0) {
                    WanKaLog.e(extraData[0] + " 有部分包名没有获取到");
                } else {
                    WanKaLog.e("当前列表有包名没有获取到");
                }
                exposurePkgs.remove(exposurePkg);
                continue;
            }

            boolean inWhite = false;
            for (String white : whiteList) {
                if (exposurePkg.equals(white)) {
                    inWhite = true;
                    break;
                }
            }

            if (inWhite) {
                // 不需要删除
                continue;
            }


            // ------------------------------ 过滤暂停，下载中 ------------------------------
            for (AppEntity appEntity : DownloadTaskManager.getInstance().getDownloadTaskList()) {
                // 预约wifi的不予过滤
                if (exposurePkg.equals(appEntity.getPackageName())) {
                    // 在任务列表中的(包括重试)
                    exposurePkgs.remove(exposurePkg);
                }
            }

            // ------------------------------ 过滤待安装 ------------------------------
            for (AppEntity appEntity : DownloadTaskManager.getInstance().getInstallTaskList()) {
                if (exposurePkg.equals(appEntity.getPackageName())) {
                    // 在任务列表中的(且不是重试)
                    exposurePkgs.remove(exposurePkg);
                }
            }

        }

        if (exposurePkgs.size() == 0) {
            // 没有需要曝光的包，如果传入的是ExposureResponseListener 类型的回调，回调onEmpty 方法
            if (extraData != null && extraData.length > 0) {
                WanKaLog.e(extraData[0] + " 的应用数为0!");
            } else {
                WanKaLog.e("当前列表的应用不需要曝光!");
            }
            return exposurePkgs;
        }

        doRequest(WanKaUrl.EXPOSURE, exposurePkgs, new OnResponseListener<JSONObject>() {
            @Override
            public void onStart(int what) {
                if (listener != null) {
                    listener.onStart(what);
                }
            }

            @Override
            public void onSucceed(int what, Response<JSONObject> response) {
                JSONObject jsonObject = response.get();
                if (extraData != null && extraData.length > 0) {
                    WanKaLog.e(extraData[0] + " onSucceed: " + jsonObject.toString());
                } else {
                    WanKaLog.e("onSucceed: " + jsonObject.toString());
                }

                if (listener != null) {
                    listener.onSucceed(what, response);
                }
            }


            @Override
            public void onFailed(int what, Response<JSONObject> response) {
                if (extraData != null && extraData.length > 0) {
                    WanKaLog.e(extraData[0] + " onFailed: " + response.toString());
                } else {
                    WanKaLog.e("onFailed: " + response.toString());
                }
                if (listener != null) {
                    listener.onFailed(what, response);
                }
            }

            @Override
            public void onFinish(int what) {
                if (listener != null) {
                    listener.onFinish(what);
                }
            }


        }, extraData);


        return exposurePkgs;

    }

    /**
     * 过滤已安装的包名(无直接远程版本时，从升级列表比对)
     *
     * @param exposurePkgs 包名集合
     * @param exposurePkg  待过滤的包名
     */
    private static void filterInstalledPkg(Set<String> exposurePkgs, String exposurePkg) {
        boolean upgrade = false;
        for (AppDetailBean detailBean : UpgradeListManager.getInstance().getAllUpgradeAppList()) {
            if (null != detailBean && !TextUtils.isEmpty(exposurePkg)) {
                if (exposurePkg.equals(detailBean.getPackage_name())) {
                    upgrade = true;
                    break;
                }
            }
        }

        if (!upgrade && AppUtils.isInstalled(exposurePkg)) {
            exposurePkgs.remove(exposurePkg);
        }
    }

    /**
     * 过滤已安装的包名
     *
     * @param exposurePkgs  包名集合
     * @param exposurePkg   待过滤的包名
     * @param remoteVersion 远程包名版本
     */
    private static void filterInstalledPkg(Set<String> exposurePkgs, String exposurePkg, String remoteVersion) {
        PackageInfo packageInfo = AppUtils.getPackageInfo(exposurePkg);
        if (packageInfo != null) {
            int localVersionCode = packageInfo.versionCode;
            int remoteVersionCode = Integer.valueOf(remoteVersion);

            if (remoteVersionCode <= localVersionCode) {
                exposurePkgs.remove(exposurePkg);
            }
        }
    }

}
