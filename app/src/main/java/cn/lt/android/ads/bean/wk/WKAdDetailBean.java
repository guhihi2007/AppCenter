package cn.lt.android.ads.bean.wk;

import java.util.List;

/**
 * Created by LinJunSheng on 2017/1/4.
 */

public class WKAdDetailBean {
    private String apk_url;
    private String icon_url;
    private String packageName;
    private String version_code;
    private String type;
    private String download_cnt;
    private String apk_md5;
    private String version_name;
    private int apk_size;
    private String name;
    private String category;
    private String desc;
    private String updatetime;
    private List<String> screen_url;

    public String getApk_url() {
        return apk_url;
    }

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDownload_cnt() {
        return download_cnt;
    }

    public void setDownload_cnt(String download_cnt) {
        this.download_cnt = download_cnt;
    }

    public String getApk_md5() {
        return apk_md5;
    }

    public void setApk_md5(String apk_md5) {
        this.apk_md5 = apk_md5;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public int getApk_size() {
        return apk_size;
    }

    public void setApk_size(int apk_size) {
        this.apk_size = apk_size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getScreen_url() {
        return screen_url;
    }

    public void setScreen_url(List<String> screen_url) {
        this.screen_url = screen_url;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }
}
