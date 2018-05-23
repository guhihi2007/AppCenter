package cn.lt.android.ads.beantransfer;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.WDJAdDownloadUrlJointer;
import cn.lt.android.ads.bean.wdj.WDJAdsBean;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.util.TimeUtils;

/**
 * 豌豆荚广告数据转换器
 * Created by LinJunSheng on 2016/12/28.
 */

public class WDJAdTransfer {

    public static List<AppBriefBean> transferAdsList(List<WDJAdsBean> adsList, int pageType) {
        List<AppBriefBean> appBriefBeanList = new ArrayList<>();

        for (WDJAdsBean adsBean : adsList) {
            AppBriefBean adApp = transferAdsBean(adsBean);
            if(null != adApp) {
                appBriefBeanList.add(adApp);
            }
        }

        // 过滤掉下载地址是以.apk结尾的广告APP
//        filterDownloadUrlEndsWithApk(appBriefBeanList);

        // 拼接广告下载地址
        for (AppBriefBean briefBean : appBriefBeanList) {
            WDJAdDownloadUrlJointer.joint(briefBean, pageType);
        }

        return appBriefBeanList;
    }

    /**
     * 把AdsBean转换成BriefBean
     */
    private static AppBriefBean transferAdsBean(WDJAdsBean adsBean) {
        try {
            AppBriefBean appBriefBean = new AppBriefBean();

            appBriefBean.setAdMold(AdMold.WanDouJia);

            appBriefBean.setName(adsBean.getTitle());
            appBriefBean.setApps_type(adsBean.getAppsType());
            appBriefBean.setAlias(adsBean.getTitle());
            appBriefBean.setPackage_md5(adsBean.getPackageMd5());
            appBriefBean.setPackage_name(adsBean.getPackageName());
            appBriefBean.setPackage_size(adsBean.getPackageSize());
            appBriefBean.setVersion_code(adsBean.getVersionCode());
            appBriefBean.setVersion_name(adsBean.getVersionName());
            appBriefBean.setReviews(adsBean.getTagline());
            appBriefBean.setDownload_url(adsBean.getDownloadUrl());
            appBriefBean.setIcon_url(adsBean.getIcons().getPx256());
            appBriefBean.setDownload_count(String.valueOf(adsBean.getDownloadCount()));
            appBriefBean.setDescription(adsBean.getDescription());
            appBriefBean.setCategory(adsBean.getCategoryAlias());
            appBriefBean.setAD(true);

            return appBriefBean;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /** 转换成AppDetailBean（应用详情页面用）*/
    public static AppDetailBean convertToAppDetailBean(WDJAdsBean bean) {
        try {
            AppDetailBean appDetailBean = new AppDetailBean();
            appDetailBean.setPackage_name(bean.getPackageName());
            appDetailBean.setDownload_url(bean.getDownloadUrl());
            appDetailBean.setName(bean.getTitle());
            appDetailBean.setDownload_count(String.valueOf(bean.getDownloadCount()));
            appDetailBean.setCategory(bean.getCategoryAlias());
            appDetailBean.setCategoryName(bean.getCategoryName());
            appDetailBean.setPackage_size(bean.getPackageSize());
            appDetailBean.setPackage_md5(bean.getPackageMd5());
            long timeMil = bean.getPublishDate();
            appDetailBean.setCreated_at(TimeUtils.getStringToDateHaveHour(timeMil));
            appDetailBean.setDescription(bean.getDescription());
            appDetailBean.setIcon_url(bean.getIcons().getPx100());
            appDetailBean.setScreenshoot_urls(bean.getScreenshots().getSmall());
            appDetailBean.setVersion_name(bean.getVersionName());
            appDetailBean.setApps_type(bean.getAppsType());
            appDetailBean.setAlias(bean.getTitle());
            appDetailBean.setVersion_code(bean.getVersionCode());
            appDetailBean.setReviews(bean.getTagline());
            appDetailBean.setAd(true);

            appDetailBean.setAdMold(AdMold.WanDouJia);

            return appDetailBean;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
