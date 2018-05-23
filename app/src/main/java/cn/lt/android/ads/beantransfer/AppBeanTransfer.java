package cn.lt.android.ads.beantransfer;


import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LogTAG;
import cn.lt.android.ads.bean.WhiteListBean;
import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.util.LogUtils;

/**
 * Created by LinJunSheng on 2016/6/24.
 */
public class AppBeanTransfer {


    public static List<AppBriefBean> transferAppDetailList(List<AppDetailBean> appDetailList) {
        List<AppBriefBean> appBriefBeanList = new ArrayList<>();

        for (AppDetailBean app : appDetailList) {
            appBriefBeanList.add(transferAppDetailBean(app));
        }

        return appBriefBeanList;
    }


    /**
     * 转换白名单
     */
    public static List<AppBriefBean> transferWhiteList(BaseBeanList<WhiteListBean> packageInfoList) {

        List<AppBriefBean> appWhiteList = new ArrayList<>();

        for (WhiteListBean whiteBean : packageInfoList) {
            AppBriefBean briefBean = new AppBriefBean();
            briefBean.setPackage_name(whiteBean.getPackage_name());
            appWhiteList.add(briefBean);
        }

        return appWhiteList;
    }

    /**
     * 把AppDetailBean转换成BriefBean
     */
    public static AppBriefBean transferAppDetailBean(AppDetailBean appDetailBean) {
        AppBriefBean appBriefBean = new AppBriefBean();

        appBriefBean.setId(appDetailBean.getId());
        appBriefBean.setName(appDetailBean.getName());
        appBriefBean.setApps_type(appDetailBean.getApps_type());
        appBriefBean.setAlias(appDetailBean.getAlias());
        appBriefBean.setPackage_md5(appDetailBean.getPackage_md5());
        appBriefBean.setPackage_name(appDetailBean.getPackage_name());
        appBriefBean.setPackage_size(appDetailBean.getPackage_size());
        appBriefBean.setVersion_code(appDetailBean.getVersion_code());
        appBriefBean.setVersion_name(appDetailBean.getVersion_name());
        appBriefBean.setCorner_url(appDetailBean.getCorner_url());
        appBriefBean.setReviews(appDetailBean.getReviews());
//        appBriefBean.setCreated_at(adsBean.getPublishDate());
        appBriefBean.setDownload_url(appDetailBean.getDownload_url());
        appBriefBean.setIcon_url(appDetailBean.getIcon_url());
        appBriefBean.setDownload_count(appDetailBean.getDownload_count());
        appBriefBean.setDescription(appDetailBean.getDescription());
        appBriefBean.setCategory(appDetailBean.getCategory());
        appBriefBean.setLtType(appDetailBean.getLtType());
        appBriefBean.setAD(appDetailBean.isAd());

        appBriefBean.isPositionLast = appDetailBean.isPositionLast;

        appBriefBean.p1 = appDetailBean.p1;
        appBriefBean.p2 = appDetailBean.p2;

        appBriefBean.setCanReplace(appDetailBean.canReplace());
        appBriefBean.setAdMold(appDetailBean.getAdMold());
        appBriefBean.setReportData(appDetailBean.getReportData());

        return appBriefBean;
    }

    /**
     * appEntity转换成AppBriefBean（用于资源已经在任务列表中）
     */
    public static AppBriefBean getAppBriefBean(AppEntity appEntity) {

        AppBriefBean appBriefBean = new AppBriefBean();

        appBriefBean.setId(appEntity.getId() + "");
        appBriefBean.setPackage_name(appEntity.getPackageName());
        appBriefBean.setName(appEntity.getName());
        appBriefBean.setAlias(appEntity.getAlias());
        appBriefBean.setDownload_url(appEntity.getDownloadUrl());
        appBriefBean.setIcon_url(appEntity.getIconUrl());
        appBriefBean.setPackage_size(appEntity.getPackageSize());
        appBriefBean.setApps_type(appEntity.getApps_type());

        appBriefBean.setDescription(appEntity.getDescription());
        appBriefBean.setPackage_md5(appEntity.getPackage_md5());
        appBriefBean.setVersion_code(appEntity.getVersion_code());
        appBriefBean.setVersion_name(appEntity.getVersion_name());
        appBriefBean.setCorner_url(appEntity.getCorner_url());
        appBriefBean.setReviews(appEntity.getReviews());
        appBriefBean.setCreated_at(appEntity.getCreated_at());
        appBriefBean.setDownload_count(appEntity.getDownload_count());
        appBriefBean.setCategory(appEntity.getCategory());
        appBriefBean.setDownloadAppEntity(appEntity);
        appBriefBean.setAD(appEntity.getIsAD());

        appBriefBean.setCanReplace(appEntity.canReplace());
        appBriefBean.setAdMold(appEntity.getAdMold());
        appBriefBean.setReportData(appEntity.getReportDataJsonObj());
        return appBriefBean;
    }

    /**
     * appEntity转换成AppBriefBean（用于资源已经在任务列表中）
     */
    public static HotSearchBean getHotSearchBean(AppEntity appEntity) {

        HotSearchBean hotSearchBean = new HotSearchBean();
        hotSearchBean.setTitle(hotSearchBean.getTitle());
        hotSearchBean.setId(appEntity.getId());
        hotSearchBean.setPakageName(appEntity.getPackageName());
        hotSearchBean.setApps_type(appEntity.getApps_type());
        hotSearchBean.setAdv(appEntity.isAdData());
        LogUtils.d("transfor", "热搜被设置：" + appEntity.isAdData());
        if (appEntity.isAdData()) {
            hotSearchBean.setCategory(appEntity.getCategory());     //分类信息没有的话，会导致  下架。
            hotSearchBean.setDownloadUrl(appEntity.getDownloadUrl());
        }
        return hotSearchBean;
    }


    /**
     * 转换基础数据
     */
    public static void transferBaseData(List<BaseBean> datas) {
        for (int i = 0; i < datas.size(); i++) {

            BaseBean bean = datas.get(i);

            if (bean.getLtType().equals("app_topic")) {
                AppTopicBean appTopicBean = (AppTopicBean) bean;
                List<AppBriefBean> briefList = transferAppDetailList(appTopicBean.getApps());
                appTopicBean.setBriefApps(briefList);
            }

            if (bean.getLtType().equals("apps")) {
                BaseBeanList<AppDetailBean> appList = (BaseBeanList<AppDetailBean>) bean;
                List<AppBriefBean> list = transferAppDetailList(appList);
                BaseBeanList<AppBriefBean> briefList = new BaseBeanList<>();
                briefList.addAll(list);
                briefList.setLtType(appList.getLtType());
                datas.remove(i);
                datas.add(i, briefList);
            }

            if (bean.getLtType().equals("app") || bean.getLtType().equals("hotword_app")) {
                AppDetailBean app = (AppDetailBean) bean;
                AppBriefBean briefBean = transferAppDetailBean(app);
                briefBean.setLtType(app.getLtType());
                datas.remove(i);
                datas.add(i, briefBean);
            }
        }
    }

    /**
     * 过滤掉下载地址是以.apk结尾的广告APP
     */
    private static void filterDownloadUrlEndsWithApk(List<AppBriefBean> adList) {
        List<AppBriefBean> tempAdList = new ArrayList<>();
        tempAdList.addAll(adList);

        for (AppBriefBean app : tempAdList) {
            if (app.getDownload_url().endsWith(".apk")) {
                adList.remove(app);
                LogUtils.i(LogTAG.AdTAG, "下载地址是以.apk结尾的，要删除掉 = " + app.getName());
            }
        }


    }

}
