package cn.lt.android.ads.bean.wk;

/**
 * 玩咖广告数据实体类
 * Created by LinJunSheng on 2016/12/28.
 */

public class WKAdsBean {
    private String packageName;

    private String apk_url;
    private String icon_url;
    private String update_info;
    private String version_code;
    private String mtime;
    private String type;
    private String download_cnt;
    private String apk_md5;
    private String version_name;
    private int apk_size;
    private String name;
    private String category;

    public void setApk_url(String apk_url) {
        this.apk_url = apk_url;
    }

    public String getApk_url() {
        return this.apk_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getIcon_url() {
        return this.icon_url;
    }

    public void setUpdate_info(String update_info) {
        this.update_info = update_info;
    }

    public String getUpdate_info() {
        return this.update_info;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public String getVersion_code() {
        return this.version_code;
    }

    public void setMtime(String mtime) {
        this.mtime = mtime;
    }

    public String getMtime() {
        return this.mtime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setDownload_cnt(String download_cnt) {
        this.download_cnt = download_cnt;
    }

    public String getDownload_cnt() {
        return this.download_cnt;
    }

    public void setApk_md5(String apk_md5) {
        this.apk_md5 = apk_md5;
    }

    public String getApk_md5() {
        return this.apk_md5;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getVersion_name() {
        return this.version_name;
    }

    public void setApk_size(int apk_size) {
        this.apk_size = apk_size;
    }

    public int getApk_size() {
        return this.apk_size;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return this.category;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
