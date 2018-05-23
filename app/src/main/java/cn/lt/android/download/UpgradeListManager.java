package cn.lt.android.download;

import android.content.Context;
import android.os.RemoteException;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.IgnoreUpgradeAppEntity;
import cn.lt.android.db.IgnoreUpgradeAppEntityDao;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.CanUpgradeEvent;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Response;

import static cn.lt.android.util.AppUtils.getUploadParams;

/**
 * Created by wenchao on 2016/3/10.
 * 升级管理
 */
public class UpgradeListManager {
    public interface Callback {
        void onResponse(List<AppDetailBean> upgradeList);
    }

    /**
     * 更新完成后数量要减少的监听器
     */
    public interface CountCallback {
        void onCountChange(int count);
    }

    // 原始所有可升级列表
    private List<AppDetailBean> allUpgradeAppList = new ArrayList<>();

    //可升级列表，由服务器获取.过滤忽略升级列表后所得
    private List<AppDetailBean> mUpgradeAppList;
    //忽略升级列表
    private List<AppDetailBean> mIgnoreAppList;

    private boolean isInitComplete = false;

    private List<Callback> mCallbackList;

    private List<CountCallback> mCountCallbackList;

    private IgnoreUpgradeAppEntityDao mIgnoreUpgradeAppEntityDao;

    /**
     * 是否已经初始化完成了
     *
     * @return
     */
    public boolean isInit() {
        return isInitComplete;
    }

    /**
     * 从服务器拉取升级列表
     */
    public void requestUpgradeListFromServer() {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new retrofit2.Callback<List<AppDetailBean>>() {

            @Override
            public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                isInitComplete = true;
                final List<AppDetailBean> upgradeList = response.body();

//                String tempStatus = Constant.WK_SWITCH;
                boolean tempStatus = PreferencesUtils.getBoolean(LTApplication.instance,Constant.WK_SWITCH);
                PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,true);
//                Constant.WK_SWITCH = Constant.STATUS_OPEN;
                Set<String> exposureApps = WanKaManager.exposureApps(upgradeList, new SimpleResponseListener<JSONObject>() {
                    @Override
                    public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                        handleUpgradeList(upgradeList);
                    }

                    @Override
                    public void onFailed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                        handleUpgradeList(upgradeList);
                    }
                }, "升级列表(通知栏)曝光 ");
//                Constant.WK_SWITCH = tempStatus;
                PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,tempStatus);

                if (exposureApps.size() == 0) {
                    handleUpgradeList(upgradeList);
                }

            }

            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                isInitComplete = true;
                t.printStackTrace();
                Logger.i("upgradeList失败" + t.getMessage());
                mCallbackList.clear();
                for (Callback callback : mCallbackList) {
                    callback.onResponse(new ArrayList<AppDetailBean>());
                }
            }
        }).bulid().requestUpgrade(geLoacalAppParams());
    }

    private void handleUpgradeList(List<AppDetailBean> upgradeList) {
        // 先添加入所有列表集合（应用自动升级用）
        allUpgradeAppList.clear();
        allUpgradeAppList.addAll(upgradeList);

        // 升级列表以json格式存到文件中,并保存获取时间
        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.GET_UPGRADE_LIST_TIME, System.currentTimeMillis());

//                Logger.i("upgradeList" + upgradeList.size());
        // 区分升级列表  和忽略列表
        filter(upgradeList);

        List<Callback> tempCallbackList = new ArrayList<>();
        tempCallbackList.addAll(mCallbackList);

        for (Callback callback : tempCallbackList) {
            callback.onResponse(mUpgradeAppList);
        }

        for (CountCallback countCallback : mCountCallbackList) {
            countCallback.onCountChange(mUpgradeAppList.size());
        }

        // 发送可升级列表个数通知
        EventBus.getDefault().post(new CanUpgradeEvent(mUpgradeAppList.size()));

        // 发送应用可升级的通知（通知栏的）
        LTNotificationManager.getinstance().sendUpgradeNotice();
    }

    /**
     * 过滤列表， 分为 升级列表和已忽略列表
     *
     * @param serverList
     */
    public void filter(List<AppDetailBean> serverList) {
        if (serverList == null) return;
        mUpgradeAppList.clear();
        mUpgradeAppList.addAll(serverList);
        Logger.i(serverList.size() + "更新");
        mIgnoreAppList.clear();
        List<IgnoreUpgradeAppEntity> ignoreList = getIgnoreListFromDB();
        Iterator<AppDetailBean> it = mUpgradeAppList.iterator();
        while (it.hasNext()) {
            AppDetailBean appDetailBean = it.next();
            Iterator<IgnoreUpgradeAppEntity> ignoreIt = ignoreList.iterator();
            while (ignoreIt.hasNext()) {
                IgnoreUpgradeAppEntity ignoreBean = ignoreIt.next();
                if (appDetailBean.getAppClientId().equals(String.valueOf(ignoreBean.getId()))) {
                    //满足是同一个游戏
                    if (!appDetailBean.getVersion_name().equals(ignoreBean.getVersionName())) {
                        //版本不一致的忽略列表  从忽略列表中删除
                        cancelIgnore(appDetailBean);
                        ignoreIt.remove();
                    } else {
                        mIgnoreAppList.add(appDetailBean);
                        it.remove();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 获取可升级列表（去除已忽略列表）
     *
     * @return 升级列表
     */
    public List<AppDetailBean> getUpgradeAppList() {
        return mUpgradeAppList;
    }

    public List<AppDetailBean> getIgnoreAppList() {
        return mIgnoreAppList;
    }

    /**
     * 获取原始所有可以升级列表集合
     */
    public List<AppDetailBean> getAllUpgradeAppList() {
        return allUpgradeAppList;
    }

    /**
     * 根据id查找可升级列表,若返回null为表示没有
     *
     * @return
     */
    public AppDetailBean findByAppId(String appId) {
        for (AppDetailBean appDetailBean : allUpgradeAppList) {
            if (appDetailBean.getAppClientId().equals(appId)) {
                return appDetailBean;
            }
        }

        return null;
    }

    /**
     * 根据包名查找可升级列表,若返回null为表示没有
     *
     * @return
     */
    public AppDetailBean findByPackageName(String packageName) {
        for (AppDetailBean appDetailBean : allUpgradeAppList) {
            if (appDetailBean.getPackage_name().equals(packageName)) {
                return appDetailBean;
            }
        }
        return null;
    }

    /**
     * 注册回调
     *
     * @param callback
     */
    public void registerCallback(Callback callback) {
        if (!mCallbackList.contains(callback)) {
            mCallbackList.add(callback);
        }
    }

    /**
     * 注销回调
     *
     * @param callback
     */
    public void unregisterCallback(Callback callback) {
        if (mCallbackList.contains(callback)) {
            mCallbackList.remove(callback);
        }
    }

    /**
     * 注册数量回掉
     *
     * @param countCallback
     */
    public void registerCountCallback(CountCallback countCallback) {
        if (!mCountCallbackList.contains(countCallback)) {
            mCountCallbackList.add(countCallback);
        }
    }

    /**
     * 注销数量回掉
     *
     * @param countCallback
     */
    public void unregisterCountCallback(CountCallback countCallback) {
        if (mCountCallbackList.contains(countCallback)) {
            mCountCallbackList.remove(countCallback);
        }
    }

    /**
     * 所有忽略列表
     *
     * @return
     */
    private List<IgnoreUpgradeAppEntity> getIgnoreListFromDB() {
        return mIgnoreUpgradeAppEntityDao.queryBuilder().list();
    }

    /**
     * 加入忽略列表
     *
     * @return
     */
    public long ignore(AppDetailBean appDetailBean) {
        DCStat.baiduStat(null, "ignore_upgrade", "忽略升级（包名）：" + appDetailBean.getPackage_name());
        long id = Long.parseLong(appDetailBean.getAppClientId());
        IgnoreUpgradeAppEntity ignoreUpgradeAppEntity = new IgnoreUpgradeAppEntity(id, appDetailBean.getName(), appDetailBean.getPackage_name(), appDetailBean.getVersion_name());
        mIgnoreAppList.add(appDetailBean);
        //移除
        Iterator<AppDetailBean> it = mUpgradeAppList.iterator();
        while (it.hasNext()) {
            AppDetailBean ignoreApp = it.next();
            if (ignoreApp.getId().equals(appDetailBean.getAppClientId())) {
                it.remove();
                break;
            }
        }
        for (CountCallback countCallback : mCountCallbackList) {
            countCallback.onCountChange(mUpgradeAppList.size());
        }
        return mIgnoreUpgradeAppEntityDao.insertOrReplace(ignoreUpgradeAppEntity);
    }

    /**
     * 取消忽略
     */
    public void cancelIgnore(AppDetailBean appDetailBean) {
        DCStat.baiduStat(null, "cancel_ingore_upgrade", "取消忽略升级（包名）：" + appDetailBean.getPackage_name());
        long id = Long.parseLong(appDetailBean.getAppClientId());
        mIgnoreUpgradeAppEntityDao.deleteByKey(id);
        //移除
        Iterator<AppDetailBean> it = mIgnoreAppList.iterator();
        while (it.hasNext()) {
            AppDetailBean ignoreApp = it.next();
            if (ignoreApp.getId().equals(appDetailBean.getAppClientId())) {
                it.remove();
                break;
            }
        }
        for (CountCallback countCallback : mCountCallbackList) {
            countCallback.onCountChange(mUpgradeAppList.size());
        }
    }

    /** 判断是否属于忽略列表*/
    public boolean isIgnore(String pkgName) {
        for (int i = 0; i < mIgnoreAppList.size(); i++) {
            if (mIgnoreAppList.get(i).getPackage_name().equals(pkgName)) {
                return true;
            }

        }
        return false;
    }

    /**
     * 更新完成后从列表中移除
     *
     * @param packageName
     */
    public void remove(String packageName) {
        //从升级列表中移除
        Iterator<AppDetailBean> iterator = mUpgradeAppList.iterator();
        while (iterator.hasNext()) {
            AppDetailBean app = iterator.next();
            if (app.getPackage_name().equals(packageName)) {
                iterator.remove();
                allUpgradeAppList.remove(app);
                for (CountCallback countCallback : mCountCallbackList) {
                    countCallback.onCountChange(mUpgradeAppList.size());
                }
                return;
            }
        }

        //从忽略列表中移除
        Iterator<AppDetailBean> iterator2 = mIgnoreAppList.iterator();
        while (iterator2.hasNext()) {
            AppDetailBean app = iterator2.next();
            if (app.getPackage_name().equals(packageName)) {
                iterator2.remove();
                allUpgradeAppList.remove(app);
                return;
            }
        }

    }

    /**
     * 开始所有更新
     */
    private void startUpgradeAll(Context context, String mode, String pageName, boolean isOrderWifiDownload) throws RemoteException {
        List<AppEntity> appEntityList = new ArrayList<>();
        for (AppDetailBean appDetailBean : mUpgradeAppList) {
            if (appDetailBean.getDownloadAppEntity() == null) {
                DownloadTaskManager.getInstance().transfer(appDetailBean);
            }
            AppEntity entity = appDetailBean.getDownloadAppEntity();

            // 是否预约wifi下载
            entity.setIsOrderWifiDownload(isOrderWifiDownload);

            appEntityList.add(entity);
        }
        DownloadTaskManager.getInstance().startAfterCheckList(context, appEntityList, mode, "upgrade", pageName, "", "", "onekey_upgrade");
    }

    /**
     * 一键更新所有 可更新的应用
     *
     * @param context
     * @param isOrderWifiDownload 标记是否wifi下载，默认为false即可
     */
    public void startAllAfterCheck(final Context context, final String pageName, boolean isOrderWifiDownload) {
        try {
            startUpgradeAll(context, "onekey", pageName, isOrderWifiDownload);//上报一键升级
            DCStat.baiduStat(context, "onekey_upgrade", "全部更新更新事件");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * wifi自动升级
     */
//    private void autoUpgrade(Context context, String pageName) throws RemoteException {
//        if (NetWorkUtils.isWifi(LTApplication.shareApplication()) && GlobalConfig.getIsOpenAutoUpgradeApp(LTApplication.shareApplication())) {
//            startUpgradeAll(context, "auto", pageName, false);//上报自动升级
//        }
//    }

    /***
     * 获取本地应用列表包名和任务列表的包名
     *
     * @return
     */
    private String geLoacalAppParams() {
        String str = "";
        try {
            str = getUploadParams();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    private UpgradeListManager() {
        mUpgradeAppList = new ArrayList<>();
        mCallbackList = new ArrayList<>();
        mIgnoreUpgradeAppEntityDao = GlobalParams.getIgnoreUpgradeAppEntityDao();
        mIgnoreAppList = new ArrayList<>();
        mCountCallbackList = new ArrayList<>();
    }

    private final static class HolderClass {
        private final static UpgradeListManager INSTANCE = new UpgradeListManager();
    }

    public static UpgradeListManager getInstance() {
        return HolderClass.INSTANCE;
    }
}

