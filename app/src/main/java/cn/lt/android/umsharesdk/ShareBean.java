package cn.lt.android.umsharesdk;

import android.app.Activity;

import cn.lt.android.entity.AppDetailBean;

/**
 * Created by LinJunSheng on 2016/3/29.
 */
public class ShareBean {
    private Activity activity;
    private OneKeyShareUtil.ShareType shareType;
    private AppDetailBean app;
    private String shareIcon;
    private String shareLink;
    private String title;
    private String shareContent;
    private String resource_type = "";

    public ShareBean() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShareIcon() {
        return shareIcon;
    }

    public void setShareIcon(String shareIcon) {
        this.shareIcon = shareIcon;
    }

    public String getShareLink() {
        return shareLink;
    }

    public void setShareLink(String shareLink) {
        this.shareLink = shareLink;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public ShareBean(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public OneKeyShareUtil.ShareType getShareType() {
        return shareType;
    }

    public void setShareType(OneKeyShareUtil.ShareType shareType) {
        this.shareType = shareType;
    }

    public AppDetailBean getApp() {
        return app;
    }

    public void setApp(AppDetailBean app) {
        this.app = app;
    }

    public String getResource_type() {
        return resource_type;
    }

    public void setResource_type(String resource_type) {
        this.resource_type = resource_type;
    }
}
