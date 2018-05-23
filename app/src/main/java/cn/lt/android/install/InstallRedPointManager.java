package cn.lt.android.install;


import java.util.ArrayList;
import java.util.List;

import cn.lt.android.download.DownloadRedPointManager;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.DownloadEvent;
import cn.lt.download.DownloadStatusDef;
import de.greenrobot.event.EventBus;


public class InstallRedPointManager {

    /**
     * 是否需要显示小红点
     */
    private boolean isNeedShow;

    /**
     * 当前界面在任务管理
     */
    private boolean isInTaskManager = false;

    private List<DownloadRedPointManager.Callback> callbackList;

    public boolean isNeedShow() {
        setNeedShow(DownloadTaskManager.getInstance().getInstallTaskList().size() > 0);
        return isNeedShow;
    }

    private void setNeedShow(boolean needShow) {
        isNeedShow = needShow;
    }

    public void onEventMainThread(DownloadEvent downloadEvent) {
        if (downloadEvent.status == DownloadStatusDef.completed) {
            //// TODO: 2017/9/24  我认为下载本来有红点，下载完转安装不应该再重复显示，
            //// TODO: 2017/9/24  如果用户点过消失此场景又会重复显示
            if (DownloadTaskManager.getInstance().getInstallTaskList().size() > 0 && !isInTaskManager) {
//                for (DownloadRedPointManager.Callback callback : callbackList) {
//                    callback.showRedPoint();
//                }
            } else {
                for (DownloadRedPointManager.Callback callback : callbackList) {
                    callback.hideRedPoint();
                }
            }
            //            //honaf
//            //只要完成了下载或者安装 都应该通知MinePortalFragment中的任务管理小红点刷新
//            EventBus.getDefault().post(Constant.EVENTBUS_TASK_MANAGER);
        }
    }

    /**
     * 通知安装事件更新
     *
//     * @param installEvent
     *
     *
     * 点击安装按钮的时候 不需要显示小红点，因为不知道进入到安装页面以后用户是取消还是安装
    */
/*    public void onEventMainThread(InstallEvent installEvent) {
        try {
            int size = DownloadTaskManager.getInstance().getInstallTaskList().size();
            if (installEvent.type == InstallEvent.INSTALLED_ADD) {
                size--;
            }
            Logger.i("安装状态" + installEvent.type);
            if (size > DownloadTaskManager.getInstance().getInstallFailureTaskList().size() && !isInTaskManager) {
                Logger.i("安装红点fefef" + DownloadTaskManager.getInstance().getInstallTaskList().size());
                for (DownloadRedPointManager.Callback callback : callbackList) {
                    Logger.i("安装红点");
                    callback.showRedPoint();
                }
            } else {
                for (DownloadRedPointManager.Callback callback : callbackList) {
                    callback.hideRedPoint();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            // TBD 异常处理
        }
    }*/

    public void setInTaskManager() {
        isInTaskManager = true;
        setNeedShow(false);
        for (DownloadRedPointManager.Callback callback : callbackList) {
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
    public void register(DownloadRedPointManager.Callback callback) {
        if (!callbackList.contains(callback)) {
            callbackList.add(callback);
        }
    }

    /**
     * 注销监听
     *
     * @param callback
     */
    public void unregister(DownloadRedPointManager.Callback callback) {
        if (callbackList.contains(callback)) {
            callbackList.remove(callback);
        }
    }


    private InstallRedPointManager() {
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
        private final static InstallRedPointManager INSTANCE = new InstallRedPointManager();
    }

    public static InstallRedPointManager getInstance() {
        return HolderClass.INSTANCE;
    }
}
