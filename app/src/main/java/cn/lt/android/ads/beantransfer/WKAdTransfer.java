package cn.lt.android.ads.beantransfer;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.bean.wk.WKAdDetailBean;
import cn.lt.android.ads.bean.wk.WKAdsBean;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;

/**
 * 玩咖广告数据转换器
 * Created by LinJunSheng on 2016/12/28.
 */

public class WKAdTransfer {

    public static List<AppBriefBean> transferAdsList(List<WKAdsBean> adsList, int pageType) {
        List<AppBriefBean> appBriefBeanList = new ArrayList<>();

        for (WKAdsBean adsBean : adsList) {
            AppBriefBean adApp = transferAdsBean(adsBean);
            if(null != adApp) {
                appBriefBeanList.add(adApp);
            }
        }

        return appBriefBeanList;
    }

    /**
     * 把AdsBean转换成BriefBean
     */
    private static AppBriefBean transferAdsBean(WKAdsBean adsBean) {
        try {
            AppBriefBean appBriefBean = new AppBriefBean();

            appBriefBean.setAdMold(AdMold.WanKa);

            appBriefBean.setName(adsBean.getName());
            appBriefBean.setApps_type(adsBean.getType().equals("soft") ? "software" : "game");
            appBriefBean.setAlias(adsBean.getName());
            appBriefBean.setPackage_md5(adsBean.getApk_md5());
            appBriefBean.setPackage_name(adsBean.getPackageName());
            appBriefBean.setPackage_size(String.valueOf(adsBean.getApk_size()));
            appBriefBean.setVersion_code(adsBean.getVersion_code());
            appBriefBean.setVersion_name(adsBean.getVersion_name());

            // 由于玩咖有没提供小编点评这种数据，所以使用细分分类作为小编点评
            appBriefBean.setReviews(adsBean.getCategory());
            appBriefBean.setDownload_url(adsBean.getApk_url());
            appBriefBean.setIcon_url(adsBean.getIcon_url());
            appBriefBean.setDownload_count(adsBean.getDownload_cnt());
            appBriefBean.setDescription("");
            appBriefBean.setCategory(adsBean.getCategory());
            appBriefBean.setAD(true);


            return appBriefBean;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /** 转换成AppDetailBean（应用详情页面用）*/
    public static AppDetailBean convertToAppDetailBean(WKAdDetailBean bean) {
        try {
            AppDetailBean appDetailBean = new AppDetailBean();
            appDetailBean.setPackage_name(bean.getPackageName());
            appDetailBean.setDownload_url(bean.getApk_url());
            appDetailBean.setName(bean.getName());
            appDetailBean.setDownload_count(bean.getDownload_cnt());
            appDetailBean.setCategory(bean.getCategory());
            appDetailBean.setCategoryName(bean.getCategory());
            appDetailBean.setPackage_size(String.valueOf(bean.getApk_size()));
            appDetailBean.setPackage_md5(bean.getApk_md5());
//        appDetailBean.setCreated_at(TimeUtils.getStringToDateHaveHour(timeMil));
            appDetailBean.setCreated_at(bean.getUpdatetime());
            appDetailBean.setDescription(bean.getDesc());
            appDetailBean.setIcon_url(bean.getIcon_url());
            appDetailBean.setScreenshoot_urls(bean.getScreen_url());
            appDetailBean.setVersion_name(bean.getVersion_name());
            appDetailBean.setApps_type(bean.getType().equals("soft") ? "software" : "game");
            appDetailBean.setAlias(bean.getName());
            appDetailBean.setVersion_code(bean.getVersion_code());
            appDetailBean.setReviews("");
            appDetailBean.setAd(true);

            appDetailBean.setAdMold(AdMold.WanKa);
            return appDetailBean;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
