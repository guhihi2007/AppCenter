package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Erosion on 2018/3/26.
 */

public class AdvertisingConfigBean extends BaseBean{
    private SplashConfigBean splash;

    public SplashConfigBean getSplash() {
        return splash;
    }

    public void setSplash(SplashConfigBean splash) {
        this.splash = splash;
    }
}
