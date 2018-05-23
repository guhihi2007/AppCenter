package cn.lt.android.entity;

import java.util.List;

/**
 * Created by Erosion on 2018/3/26.
 */

public class SplashConfigBean {
    private List<SplashShowTimeBean> baidu_ads;
    private List<SplashShowTimeBean> guangdiantong_ads;
    private ProportionBean proportion;

    public List<SplashShowTimeBean> getBaidu_ads() {
        return baidu_ads;
    }

    public void setBaidu_ads(List<SplashShowTimeBean> baidu_ads) {
        this.baidu_ads = baidu_ads;
    }

    public List<SplashShowTimeBean> getGuangdiantong_ads() {
        return guangdiantong_ads;
    }

    public void setGuangdiantong_ads(List<SplashShowTimeBean> guangdiantong_ads) {
        this.guangdiantong_ads = guangdiantong_ads;
    }

    public ProportionBean getProportion() {
        return proportion;
    }

    public void setProportion(ProportionBean proportion) {
        this.proportion = proportion;
    }
}
