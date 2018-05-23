package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by dxx on 2016/3/9.
 */
public class PlatVersionBean extends BaseBean {

    private String download_link;
    private String package_md5;
    private long package_size;
    private String changelog;
    private String version_name;
    private String version_code;
    private String created_at;
    private boolean force;

    public String getDownload_link() {
        return download_link;
    }

    public void setDownload_link(String download_link) {
        this.download_link = download_link;
    }

    public String getPackage_md5() {
        return package_md5;
    }

    public void setPackage_md5(String package_md5) {
        this.package_md5 = package_md5;
    }

    public long getPackage_size() {
        return package_size;
    }

    public void setPackage_size(long package_size) {
        this.package_size = package_size;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

}
