package cn.lt.android.ads.bean;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by ltbl on 2016/7/8.
 */
public class WhiteListBean extends BaseBean {
    private String package_name;

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }
}
