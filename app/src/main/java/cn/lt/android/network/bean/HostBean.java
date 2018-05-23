package cn.lt.android.network.bean;

/**
 * Created by Administrator on 2016/2/29.
 */
public class HostBean {
    private String acenter_host;
    private String ucenter_host;
    private String dcenter_host;
    private String cdnlimit_host;
    private String image_host;
    private String app_host;

    public String getCdnlimit_host() {
        return cdnlimit_host;
    }

    public void setCdnlimit_host(String cdnlimit_host) {
        this.cdnlimit_host = cdnlimit_host;
    }

    private String weixin_host;
    private HostConfigBean settings = new HostConfigBean();

    public String getWeixin_host() {
        return weixin_host;
    }

    public void setWeixin_host(String weixin_host) {
        this.weixin_host = weixin_host;
    }

    public String getAcenter_host() {
        return acenter_host;
    }

    public void setAcenter_host(String acenter_host) {
        this.acenter_host = acenter_host;
    }

    public String getUcenter_host() {
        return ucenter_host;
    }

    public void setUcenter_host(String ucenter_host) {
        this.ucenter_host = ucenter_host;
    }

    public String getDcenter_host() {
        return dcenter_host;
    }

    public void setDcenter_host(String dcenter_host) {
        this.dcenter_host = dcenter_host;
    }

    public String getImage_host() {
        return image_host;
    }

    public void setImage_host(String image_host) {
        this.image_host = image_host;
    }

    public String getApp_host() {
        return app_host;
    }

    public void setApp_host(String app_host) {
        this.app_host = app_host;
    }

    public HostConfigBean getSettings() {
        return settings;
    }

    public void setSettings(HostConfigBean settings) {
        this.settings = settings;
    }
}
