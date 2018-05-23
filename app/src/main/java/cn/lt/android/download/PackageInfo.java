package cn.lt.android.download;

import java.io.Serializable;

/**
 * Created by wenchao on 2016/3/15.
 */
public class PackageInfo implements Serializable {
    private String app_name;
    private String package_name;
    private String version_code;
    private String version_name;
    private String uesd_time;
    private boolean is_system_app;

    public PackageInfo() {
    }

    public PackageInfo(String package_name, String version_code) {
        this.package_name = package_name;
        this.version_code = version_code;
    }

    public PackageInfo(String package_name, String version_code, String version_name, boolean isSystemApp) {
        this.package_name = package_name;
        this.version_code = version_code;
        this.version_name = version_name;
        this.is_system_app = isSystemApp;
    }

    public PackageInfo(String app_name, String package_name, String version_code, String version_name, String uesd_time) {
        this.app_name = app_name;
        this.package_name = package_name;
        this.version_code = version_code;
        this.version_name = version_name;
        this.uesd_time = uesd_time;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getVersion_code() {
        return version_code;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }
}
