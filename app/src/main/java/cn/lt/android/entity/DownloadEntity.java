package cn.lt.android.entity;

import cn.lt.android.event.InstallEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.download.DownloadStatusDef;

/**
 * Created by wenchao on 2016/2/26.
 */
public class DownloadEntity extends BaseBean{
    //下载状态
    private int status;
    //下载当前进度
    private long soFar;
    //下载总进度
    private long total;
    //下载错误消息
    private String errMsg;
    //下载速度
    private long speed;
    //下载剩余时间
    private long surplusTime;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getSoFar() {
        return soFar;
    }

    public void setSoFar(long soFar) {
        this.soFar = soFar;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public void setStatusByInstallEvent(int event){
        switch (event){
            case InstallEvent.INSTALLED_ADD:
                setStatus(InstallState.installed);
                break;
            case InstallEvent.UNINSTALL:
                setStatus(DownloadStatusDef.INVALID_STATUS);
                break;
            case InstallEvent.INSTALLED_UPGRADE:
                setStatus(InstallState.installed);
                break;
            case InstallEvent.INSTALL_FAILURE:
                setStatus(InstallState.install_failure);
                break;
            case InstallEvent.INSTALLING:
                setStatus(InstallState.installing);
                break;
        }
    }

    public long getSurplusTime() {
        return surplusTime;
    }

    public void setSurplusTime(long surplusTime) {
        this.surplusTime = surplusTime;
    }
}
