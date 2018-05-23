package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by ltbl on 2016/7/1.
 */
public class PkgInfoBean extends BaseBean {
    private String package_md5;
    private String download_url;

    public String getPackage_md5() {
        return package_md5;
    }

    public void setPackage_md5(String package_md5) {
        this.package_md5 = package_md5;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }
}
