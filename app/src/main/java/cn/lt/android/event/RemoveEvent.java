package cn.lt.android.event;

import cn.lt.android.db.AppEntity;

/**
 * @author Administrator
 * @time 2016/6/4 20:17
 * @des ${任务管理列表数字的更新}
 */
public class RemoveEvent {
    public AppEntity mAppEntity;
    public boolean isInstalled;
    public RemoveEvent(AppEntity mAppEntity) {
        this.mAppEntity=mAppEntity;
    }
    public RemoveEvent(AppEntity mAppEntity,boolean isInstalled) {
        this.mAppEntity=mAppEntity;
        this.isInstalled=isInstalled;
    }
}
