package cn.lt.android.download;


import java.util.ArrayList;
import java.util.List;

import cn.lt.android.event.DownloadEvent;
import cn.lt.download.DownloadStatusDef;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/3/19.
 * 下载红点管理器
 */
public class DownloadRedPointManager {

    public interface Callback {
        void showRedPoint();

        void hideRedPoint();
    }

    /**
     * 是否需要显示小红点
     */
    private boolean isNeedShow = false;

    /**
     * 当前界面在任务管理
     */
    private boolean isInTaskManager = false;

    private List<Callback> callbackList;

    public boolean isNeedShow() {
        setNeedShow(DownloadTaskManager.getInstance().getDownloadTaskList().size() > 0);
        return isNeedShow;
    }

    private void setNeedShow(boolean needShow) {
        isNeedShow = needShow;
    }

    public void onEventMainThread(DownloadEvent downloadEvent) {
        if (downloadEvent.status == DownloadStatusDef.pending) {
            if (!isInTaskManager) {
                //来了一个下载
                setNeedShow(true);
                for (Callback callback : callbackList) {
                    callback.showRedPoint();
                }
            }
        }
//        //honaf
//        //只要完成了下载或者安装 都应该通知MinePortalFragment中的任务管理小红点刷新
//        EventBus.getDefault().post(Constant.EVENTBUS_TASK_MANAGER);
    }

    public void setInTaskManager() {
        isInTaskManager = true;
        setNeedShow(false);
        for (Callback callback : callbackList) {
            callback.hideRedPoint();
        }
    }

    public void setNotInTaskManager() {
        isInTaskManager = false;
    }

    /**
     * 注册监听
     *
     * @param callback
     */
    public void register(Callback callback) {
        if (!callbackList.contains(callback)) {
            callbackList.add(callback);
        }
    }

    /**
     * 注销监听
     *
     * @param callback
     */
    public void unregister(Callback callback) {
        if (callbackList.contains(callback)) {
            callbackList.remove(callback);
        }
    }


    private DownloadRedPointManager() {
        EventBus.getDefault().register(this);
        callbackList = new ArrayList<>();
    }

    /**
     * 注销管理器
     */
    public void destory() {
        EventBus.getDefault().unregister(this);
    }


    private final static class HolderClass {
        private final static DownloadRedPointManager INSTANCE = new DownloadRedPointManager();
    }

    public static DownloadRedPointManager getInstance() {
        return HolderClass.INSTANCE;
    }
}
