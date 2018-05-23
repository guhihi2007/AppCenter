package cn.lt.android.event;

/**
 * Created by wenchao on 2016/3/18.
 * app可以升级数目通知
 */
public class CanUpgradeEvent {
    //可以升级数目
    public int canUpgradeCount;

    public CanUpgradeEvent(int canUpgradeCount) {
        this.canUpgradeCount = canUpgradeCount;
    }
}
