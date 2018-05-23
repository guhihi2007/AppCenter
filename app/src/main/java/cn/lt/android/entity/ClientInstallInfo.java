package cn.lt.android.entity;

/**
 * Created by ATian on 2017/6/9.
 */

public class ClientInstallInfo {
    private long install_time;//客户端首次安装时间
    private long last_upgrade_time;//客户端最后一次升级时间

    public long getInstall_time() {
        return install_time;
    }

    public void setInstall_time(long install_time) {
        this.install_time = install_time;
    }

    public long getLast_upgrade_time() {
        return last_upgrade_time;
    }

    public void setLast_upgrade_time(long last_upgrade_time) {
        this.last_upgrade_time = last_upgrade_time;
    }
}
