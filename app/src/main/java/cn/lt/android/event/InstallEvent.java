package cn.lt.android.event;

import cn.lt.android.db.AppEntity;

/**
 * Created by wenchao on 2016/1/26.
 * 安装或者卸载事件
 */
public class InstallEvent {
    //正常安装包成功
    public static final int INSTALLED_ADD = 0;
    //安装中
    public static final int INSTALLING = 1;
    //安装失败
    public static final int INSTALL_FAILURE = 3;
    //升级包成功
    public static final int INSTALLED_UPGRADE = 4;
    //卸载
    public static final int UNINSTALL = 5;

    public String packageName;

    public int type = INSTALLED_ADD;

    public String id ;

    public AppEntity app;

    public boolean isOutOfMemoryByInstall;

    public InstallEvent(String packageName, int type) {
        this.packageName = packageName;
        this.type = type;
    }

    public InstallEvent(AppEntity app, String packageName, int type) {
        this.app = app;
        this.packageName = packageName;
        this.type = type;
    }

    public InstallEvent(AppEntity app,int type) {
        this.app = app;
        this.type = type;
    }

    public InstallEvent(AppEntity app, int type, String id) {
        this.app = app;
        this.type = type;
        this.id = id;
    }

    public InstallEvent(String  packageName, int type, String id,boolean isOutOfMemoryByInstall) {
        this.packageName = packageName;
        this.type = type;
        this.id = id;
        this.isOutOfMemoryByInstall = isOutOfMemoryByInstall;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}