package cn.lt.android.ads.bean.wdj;

import java.util.List;


/**
 * Created by Administrator on 2016/3/5.
 */
public class WDJAdsBean {
    private String title;
    private String description;
    private long downloadCount;
    private String imprUrl;
    private String packageName;
    private long installedCount;
    private long publishDate;
    private String changelog;
    private List<AdsCategory> categories;
    private List<AdsApkBean> apks;
    private AdsScreenshotBean screenshots;
    private AdsIconsBean icons;
    private String tagline;

    public String getTagline() {
        return tagline;
    }

    public AdsScreenshotBean getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(AdsScreenshotBean screenshots) {
        this.screenshots = screenshots;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public AdsIconsBean getIcons() {
        return icons;
    }

    public void setIcons(AdsIconsBean icons) {
        this.icons = icons;
    }

    public List<AdsApkBean> getApks() {
        return apks;
    }

    public void setApks(List<AdsApkBean> apks) {
        this.apks = apks;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getImprUrl() {
        return imprUrl;
    }

    public void setImprUrl(String imprUrl) {
        this.imprUrl = imprUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getInstalledCount() {
        return installedCount;
    }

    public void setInstalledCount(long installedCount) {
        this.installedCount = installedCount;
    }

    public long getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(long publishDate) {
        this.publishDate = publishDate;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public List<AdsCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<AdsCategory> categories) {
        this.categories = categories;
    }

    public String getAppsType() {
        if(null != getCategories() && getCategories().size() > 0) {
            return getCategories().get(0).getType();
        }

        return "";
    }

    public String getPackageMd5() {
        if(null != getApks() && getApks().size() > 0) {
            return getApks().get(0).getMd5();
        }

        return "";
    }

    public String getPackageSize() {
        if(null != getApks() && getApks().size() > 0) {
            return String.valueOf(getApks().get(0).getBytes());
        }

        return "";
    }

    public String getVersionCode() {
        if(null != getApks() && getApks().size() > 0) {
            return getApks().get(0).getVersionCode();
        }

        return "";
    }

    public String getVersionName() {
        if(null != getApks() && getApks().size() > 0) {
            return getApks().get(0).getVersionName();
        }

        return "";
    }

    public String getDownloadUrl() throws Exception {
        if (null != getApks() && getApks().size() > 0) {
            if(null == getApks().get(0).getDownloadUrl()) {
                throw new NullPointerException("豌豆荚某广告downloadurl对象为空");
            } else {
                return getApks().get(0).getDownloadUrl().getUrl();
            }
        }

        return "";
    }

    public String getCategoryAlias() {
        if(null != getCategories() && getCategories().size() > 0) {
            return getCategories().get(0).getAlias();
        }

        return "";
    }

    public String getCategoryName() {
        if(null != getCategories() && getCategories().size() > 0) {
            return getCategories().get(0).getName();
        }

        return "";
    }
}
