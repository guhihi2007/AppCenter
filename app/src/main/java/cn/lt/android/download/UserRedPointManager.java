package cn.lt.android.download;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.entity.AppDetailBean;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/4/20.
 * 用户图标小红点管理器， 管理首页左上角用户图标上的小红点
 * 1.应用升级显示小红点
 * 2.检测平台升级显示小红点
 */
public class UserRedPointManager {

    public interface Callback {
        void showRedPoint();

        void hideRedPoint();
    }

    /**
     * 是否需要显示小红点
     */
    private boolean isNeedShow = false;

    /**
     * 当前界面在用户中心
     */
    private boolean isInUserCenter = false;


    private List<Callback> callbackList;

    public boolean isNeedShow() {
        return isNeedShow;
    }

    private void setNeedShow(boolean needShow) {
        isNeedShow = needShow;
    }

    public void setInUserCenter() {
        isInUserCenter = true;
        setNeedShow(false);
        for (Callback callback : callbackList) {
            callback.hideRedPoint();
        }
    }

    public void setNotInUserCenter() {
        isInUserCenter = false;
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

    /**
     * 注销管理器
     */
    public void destory() {
        EventBus.getDefault().unregister(this);
    }

    private UserRedPointManager() {
        callbackList = new ArrayList<>();
        if (UpgradeListManager.getInstance().isInit()) {
            getUpgradeListSize();
        } else {
            UpgradeListManager.getInstance().registerCallback(new UpgradeListManager.Callback() {
                @Override
                public void onResponse(List<AppDetailBean> upgradeList) {
                    getUpgradeListSize();
                }
            });
        }
    }

    private void getUpgradeListSize() {
        int count = UpgradeListManager.getInstance().getUpgradeAppList().size();
        if (count > 0) {
            setNeedShow(true);
//            if(isInUserCenter) {
            for (Callback callback : callbackList) {
                callback.showRedPoint();
            }
//            }
        } else {
            setNeedShow(false);
//            if(isInUserCenter) {
            for (Callback callback : callbackList) {
                callback.hideRedPoint();
            }
//            }
        }
    }

    private final static class HolderClass {
        private final static UserRedPointManager INSTANCE = new UserRedPointManager();
    }

    public static UserRedPointManager getInstance() {
        return HolderClass.INSTANCE;
    }
}
