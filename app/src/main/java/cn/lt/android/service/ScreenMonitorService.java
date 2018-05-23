package cn.lt.android.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.PackageInfo;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.APPUpGradeBlackListBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.entity.LimitBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.NetWorkCore;
import cn.lt.android.network.bean.HttpResult;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.FileSizeUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.util.TimeUtils;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static cn.lt.download.DownloadStatusDef.completed;
import static cn.lt.download.DownloadStatusDef.error;
import static cn.lt.download.DownloadStatusDef.paused;
import static cn.lt.download.DownloadStatusDef.progress;

/**
 * Created by chon on 2017/5/3.
 * What? How? Why?
 * 开亮屏自动升级逻辑处理
 */

public class ScreenMonitorService extends Service {
    private static final String TAG = LogTAG.appAutoUpgrade;
    private int startId;
    private Handler mHandler;


    // request fail times count
    private byte canBeDownloadedFailTimes, heartBeatsFailTimes;
    private final byte RETRY_TIMES = 3;

    private List<AppDetailBean> businessUpgradeList = new ArrayList<>();
    private List<AppDetailBean> otherUpgradeList = new ArrayList<>();

    // own resource to execute download
    private boolean ownResource;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            LogUtils.e(TAG, "action = " + action);
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    LogUtils.i(TAG, "屏幕熄屏啦~");
                    this.startId = startId;
                    mHandler = new Handler();
                    // may stop self immediately,execute this at the end.
                    executeAutoUpgrade();
                    break;
                default:
                    LogUtils.i(TAG, "亮屏啦~~");
                    executePause();
                    stopSelf(startId);
            }
        } else {
            stopSelf(startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 暂停所有自动升级任务
     */
    private void executePause() {
        try {
            LogUtils.i(TAG, "暂停所有升级任务~~~");
            DownloadTaskManager.getInstance().autoUpgradeAppPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停三方包、商务包的自动升级任务
     */
    private void executePause(List<AppDetailBean> upgradeList) {
        try {
            List<AppEntity> downloadList = DownloadTaskManager.getInstance().getDownloadingList();
            for (AppEntity app : downloadList) {
                for (AppDetailBean detailBean : upgradeList) {
                    AppEntity downloadAppEntity = detailBean.getDownloadAppEntity();
                    if (app.getId() == downloadAppEntity.getId()) {
                        DownloadTaskManager.getInstance().pause(app, "auto", "", "", "", "app_auto_upgrade");
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void executeAutoUpgrade() {
        // 不符合条件的停止进行自动升级
        if (!isConditionsMet()) {
            stopSelf(startId);
            return;
        }

        // 判断是否黑名单
        long getBlacklistTime = (long) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.GET_IS_BLACKLIST_TIME, 0L);
        String isBlacklist = (String) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "");

        if (getBlacklistTime == 0 || TimeUtils.isExceedHour(getBlacklistTime, 8) || TextUtils.isEmpty(isBlacklist)) {
            if (TimeUtils.isExceedHour(getBlacklistTime, 8)) {
                LogUtils.i(TAG, "超过8小时，重新请求黑名单");
            }

            // 从接口获取是否黑名单
            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<APPUpGradeBlackListBean>() {
                @Override
                public void onResponse(Call<APPUpGradeBlackListBean> call, Response<APPUpGradeBlackListBean> response) {
                    APPUpGradeBlackListBean bean = response.body();
                    if (bean != null) {
                        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.GET_IS_BLACKLIST_TIME, System.currentTimeMillis());
                        LogUtils.i(TAG, bean.getMessage() + bean.is_black_list());
                        if (!bean.is_black_list()) {
                            LogUtils.i(TAG, "当前不在黑名单中，启动升级");
                            SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "no");
                            fetchUpgradeList();
                        } else {
                            LogUtils.e(TAG, "当前在黑名单上，不予升级");
                            SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "yes");
//                            GlobalConfig.setIsOpenAutoUpgradeApp(getApplicationContext(), false);
                            stopSelf(startId);
                        }

                    } else {
                        LogUtils.e(TAG, "请求黑名单返回空，不予升级");
                        stopSelf(startId);
                    }

                }

                @Override
                public void onFailure(Call<APPUpGradeBlackListBean> call, Throwable t) {
                    LogUtils.i(TAG, "请求是否黑名单失败，不予升级");
                    stopSelf(startId);
                }
            }).bulid().requestBlacklist();

        } else {
            if ("yes".equals(isBlacklist)) {
                LogUtils.i(TAG, "是黑名单（文件保存过），不予升级");
                stopSelf(startId);
            } else {
                LogUtils.i(TAG, "不是黑名单（文件保存过），启动升级");
                fetchUpgradeList();
            }
        }
    }


    private void fetchUpgradeList() {
        long getUpgradeListTime = (long) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.GET_UPGRADE_LIST_TIME, 0L);
        boolean isExceedDay = TimeUtils.isExceedDay(getUpgradeListTime);

        // 超过一天，重新请求升级列表
        if (isExceedDay) {
            LogUtils.i(TAG, "超过一天，准备开始重新请求升级列表");

            List<android.content.pm.PackageInfo> apps = AppUtils.getUserAppList(LTApplication.shareApplication());
            List<PackageInfo> uploadApps = new ArrayList<>();
            for (android.content.pm.PackageInfo packageInfo : apps) {
                uploadApps.add(new PackageInfo(packageInfo.packageName, String.valueOf(packageInfo.versionCode)));
            }
            String params = new Gson().toJson(uploadApps);

            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {

                @Override
                public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                    List<AppDetailBean> upgradeList = response.body();
                    // 判断是否有可升级的应用
                    if (upgradeList.size() > 0) {
                        UpgradeListManager.getInstance().filter(upgradeList);
                        UpgradeListManager.getInstance().getAllUpgradeAppList().clear();
                        UpgradeListManager.getInstance().getAllUpgradeAppList().addAll(upgradeList);
                        // 升级列表以json格式存到文件中,并保存获取时间
                        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.GET_UPGRADE_LIST_TIME, System.currentTimeMillis());
                        LogUtils.i(TAG, "请求成功，有可升级的应用");

                        // 启动升级
                        goUpgrade(UpgradeListManager.getInstance().getUpgradeAppList());

                    } else {
                        LogUtils.i(TAG, "请求成功，但没有可升级的应用，so应用自动升级不启动");
                        stopSelf(startId);
                    }

                }

                @Override
                public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                    LogUtils.i(TAG, "请求升级列表失败，自动升级无法启动");
                    stopSelf(startId);
                }
            }).bulid().requestUpgrade(params);
        } else {
            LogUtils.i(TAG, "没超过一天，开始执行自动升级");
            // 启动升级
            goUpgrade(UpgradeListManager.getInstance().getUpgradeAppList());
        }

    }

    /**
     * 启动自动升级
     */
    private void goUpgrade(final List<AppDetailBean> upgradeList) {
        if (upgradeList.size() == 0) {
            LogUtils.i(TAG, "升级列表个数是 0 ，不需启动自动升级");
            stopSelf(startId);
            return;
        }

        long totalSize = 0;
        for (AppDetailBean app : upgradeList) {
            totalSize += Long.parseLong(app.getPackage_size());
        }

        // 手机内存是否小于所有可升级app总大小
        if (FileSizeUtil.getAvailableExternalMemorySize() < totalSize) {
            LogUtils.i(TAG, "手机内存小于所有可升级app总大小，so应用自动升级不启动");
            stopSelf(startId);
            return;
        }

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<ConfigureBean>() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ConfigureBean base = (ConfigureBean) response.body();
                    if (base != null && null != base.getThird_party_wk_app()) {
                        if ("close".equals(base.getThird_party_wk_app().getStatus())) {
//                            Constant.WK_SWITCH = "close";
                            PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,false);
                        } else {
//                            Constant.WK_SWITCH = "open";
                            PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,true);
                        }
                    }
                } catch (Exception ignore) {

                } finally {
                    exposureApps(upgradeList);
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                exposureApps(upgradeList);
            }

        }).bulid().requestPopup();
    }

    private void exposureApps(final List<AppDetailBean> upgradeList) {
        Set<String> exposureApps = WanKaManager.exposureApps(upgradeList, new SimpleResponseListener<JSONObject>() {
            @Override
            public void onFinish(int what) {
                executeUpgrade(upgradeList);
            }
        }, "应用自动升级曝光");

        if (exposureApps.size() == 0) {
            executeUpgrade(upgradeList);
        }
    }

    private void executeUpgrade(List<AppDetailBean> upgradeList) {
        try {
            // 筛选出走我们自己和CDN的商务应用
            for (AppDetailBean detailBean : upgradeList) {
                if (detailBean.is_business_package) {
                    businessUpgradeList.add(detailBean);
                } else {
                    otherUpgradeList.add(detailBean);
                }
            }

            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }

            if (businessUpgradeList.size() > 0) {
                askIfCanBeDownloaded();
            } else {
                LogUtils.i(TAG, "成功启动自动升级（无商务包升级，不走自家CDN，升级个数是 " + otherUpgradeList.size() + " 个），结束限流流程");
                DownloadTaskManager.getInstance().autoUpgradeApp(otherUpgradeList);
                stopSelf(startId);
            }
        } catch (RemoteException e) {
            LogUtils.i(TAG, "下载出了点问题：" + e.toString());
            stopSelf(startId);
        }
    }

    private long printTime = 0;
    public void onEventMainThread(DownloadEvent downloadEvent) {
        if (downloadEvent != null) {
//            LogUtils.e(TAG, "任务状况， " + downloadEvent.status + "\t" + downloadEvent.downloadId);

            if (downloadEvent.status == progress) {
                // 1. 有商务包，且有下载通道。
                // 2. 有商务包，无下载通道，且有三方包

                // -------------------------- 纯粹为了log start --------------------------
                if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    for (AppDetailBean bean : businessUpgradeList) {
                        AppEntity appEntity = bean.getDownloadAppEntity();
                        if (appEntity != null && FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath()) == downloadEvent.downloadId) {
                            if (System.currentTimeMillis() - printTime > 2000) {
                                printTime = System.currentTimeMillis();
                                LogUtils.i(TAG, "process:" + downloadEvent.packageName + " - " + downloadEvent.soFarBytes + "/" + downloadEvent.totalBytes);
                            }
                            break;
                        }
                    }

                    for (AppDetailBean bean : otherUpgradeList) {
                        AppEntity appEntity = bean.getDownloadAppEntity();
                        if (appEntity != null && FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath()) == downloadEvent.downloadId) {
                            if (System.currentTimeMillis() - printTime > 2000) {
                                printTime = System.currentTimeMillis();
                                LogUtils.i(TAG, "process:" + downloadEvent.packageName + " - " + downloadEvent.soFarBytes + "/" + downloadEvent.totalBytes);
                            }
                            break;
                        }
                    }
                }
                // -------------------------- 纯粹为了log end --------------------------

            } else if (downloadEvent.status == completed || downloadEvent.status == error || downloadEvent.status == paused) {

                boolean result = false;

                for (AppDetailBean bean : businessUpgradeList) {
                    AppEntity appEntity = bean.getDownloadAppEntity();
                    if (appEntity != null && FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath()) == downloadEvent.downloadId) {
                        if (downloadEvent.status == error) {
                            LogUtils.e(TAG, "任务失败， " + appEntity.getName());
                        } else if (downloadEvent.status == completed){
                            LogUtils.i(TAG, "任务成功， " + appEntity.getName());
                        } else if (downloadEvent.status == paused){
                            LogUtils.e(TAG, "任务暂停(下载地址出错或者网络中断)， " + appEntity.getName());
                        }
                        businessUpgradeList.remove(bean);
                        if (businessUpgradeList.size() == 0) {
                            if (otherUpgradeList.size() > 0) {
                                try {

                                    // 商务包都下完了，开始下载三方包，结束限流流程（只允许在息屏下执行）
                                    if (!AppUtils.isScreenOn()) {
                                        DownloadTaskManager.getInstance().autoUpgradeApp(otherUpgradeList);
                                        LogUtils.i(TAG, "商务包下载完毕，结束限流流程,开始下载三方包");
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                LogUtils.i(TAG, "商务包下载完毕，且无三方升级包，结束流程");
                            }

                            stopSelf(startId);
                            return;
                        }
                        result = true;
                        break;
                    }
                }

                if (!result) {
                    // 有商务包，无下载通道，且有三方包
                    for (AppDetailBean bean : otherUpgradeList) {
                        AppEntity appEntity = bean.getDownloadAppEntity();
                        if (appEntity != null && FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath()) == downloadEvent.downloadId) {
                            otherUpgradeList.remove(bean);
                            if (downloadEvent.status == error) {
                                LogUtils.e(TAG, "任务失败， " + appEntity.getName());
                            } else {
                                LogUtils.i(TAG, "任务成功， " + appEntity.getName());
                            }
                            break;
                        }
                    }
                }

            }
        }


    }


    private void askIfCanBeDownloaded() {
        // y,down business packages,execute heartBeats every n min,then execute normal tasks while business tasks finish and stop self.
        // n,execute normal task directly and ask if can be downloaded every m min.

        Retrofit cdnLimitHostRetrofit = NetWorkCore.getInstance().getCdnLimitHostRetrofit();
        NetDataInterfaceDao dataInterfaceDao = cdnLimitHostRetrofit.create(NetDataInterfaceDao.class);

        dataInterfaceDao.getTicket().enqueue(new Callback<HttpResult<LimitBean>>() {
            @Override
            public void onResponse(Call<HttpResult<LimitBean>> call, Response<HttpResult<LimitBean>> response) {
                canBeDownloadedFailTimes = 0;

                HttpResult<LimitBean> result = response.body();

                if (result.status == 0) {
                    LogUtils.e(TAG, "服务器当前有空闲资源");
                    ownResource = true;

                    // 当前有三方的自动升级任务，暂停(可能三方任务并没有开启)
                    executePause(otherUpgradeList);
                    try {
                        // 启动自己CDN的升级任务
                        DownloadTaskManager.getInstance().autoUpgradeApp(businessUpgradeList);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    // 开启心跳上报
                    executeHeartBeats(0);
                } else if (result.status == 1) {
                    ownResource = false;
                    // do ask after delay min
                    LogUtils.e(TAG, "服务器当前没有空闲资源，间隔" + result.data.delay + "s询问服务器是否有空闲资源");

                    if (otherUpgradeList.size() > 0) {
                        try {
                            LogUtils.e(TAG, "自动升级三方的应用（" + otherUpgradeList.size() + ")");
                            DownloadTaskManager.getInstance().autoUpgradeApp(otherUpgradeList);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    if (mHandler != null) {
                        // Network asynchronous request,mHandler may be null when handle invoke.
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                askIfCanBeDownloaded();
                            }
                        }, result.data.delay * 1000);
                    }
                } else {
                    // may hold the resource.
                    LogUtils.e(TAG, "状态不对：" + result.message);
                    ownResource = true;
                    stopSelf(startId);
                }
            }

            @Override
            public void onFailure(Call<HttpResult<LimitBean>> call, Throwable t) {
                canBeDownloadedFailTimes++;
                LogUtils.e(TAG, "请求服务器是否有空闲资源接口失败" + canBeDownloadedFailTimes + "次: " + t.toString());
                if (canBeDownloadedFailTimes >= RETRY_TIMES) {
                    LogUtils.e(TAG, "请求服务器是否有空闲资源接口失败" + RETRY_TIMES + "次，开始走结束流程");
                    stopSelf(startId);
                } else {
                    askIfCanBeDownloaded();
                }
            }
        });
    }

    private void executeHeartBeats(final int interval) {
        Retrofit cdnLimitHostRetrofit = NetWorkCore.getInstance().getCdnLimitHostRetrofit();
        NetDataInterfaceDao dataInterfaceDao = cdnLimitHostRetrofit.create(NetDataInterfaceDao.class);

        dataInterfaceDao.checkTicket().enqueue(new Callback<HttpResult<LimitBean>>() {
            @Override
            public void onResponse(Call<HttpResult<LimitBean>> call, Response<HttpResult<LimitBean>> response) {
                heartBeatsFailTimes = 0;
                final HttpResult<LimitBean> result = response.body();

                if (result.status == 0) {
                    // execute download,tell serve downloading every heartbeat min
                    LogUtils.e(TAG, "服务器当前有空闲资源，执行下载，并间隔" + result.data.heartbeat + "s上报心跳 - " + interval);
                    if (mHandler != null) {
                        // Network asynchronous request,mHandler may be null when handle invoke.
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                executeHeartBeats(result.data.heartbeat * 1000);
                            }
                        }, interval);
                    }
                } else if (result.status == 1) {
                    LogUtils.e(TAG, "发送心跳过程中服务器说没资源了，暂停商务包任务：" + result.message);
                    // pause business tasks
                    executePause(businessUpgradeList);
                    askIfCanBeDownloaded();
                }
            }

            @Override
            public void onFailure(Call<HttpResult<LimitBean>> call, Throwable t) {
                heartBeatsFailTimes++;
                // After a few minutes of running in the background, the network may be disconnected.
                LogUtils.e(TAG, "请求服务器心跳接口失败" + heartBeatsFailTimes + "次:" + t.toString());
                if (heartBeatsFailTimes >= RETRY_TIMES) {
                    LogUtils.e(TAG, "请求服务器心跳接口失败" + RETRY_TIMES + "次，开始走结束流程");
                    stopSelf(startId);
                } else {
                    executeHeartBeats(0);
                }
            }
        });
    }

    /**
     * notify server finished all tasks,release resources
     */
    private void notifyServerReleased() {
        businessUpgradeList.clear();
        otherUpgradeList.clear();
        ownResource = false;

        Retrofit cdnLimitHostRetrofit = NetWorkCore.getInstance().getCdnLimitHostRetrofit();
        NetDataInterfaceDao dataInterfaceDao = cdnLimitHostRetrofit.create(NetDataInterfaceDao.class);
        dataInterfaceDao.dropTicket().enqueue(new Callback<HttpResult<Object>>() {
            @Override
            public void onResponse(Call<HttpResult<Object>> call, Response<HttpResult<Object>> response) {
                LogUtils.i(TAG, "通知服务器释放资源成功");
            }

            @Override
            public void onFailure(Call<HttpResult<Object>> call, Throwable t) {
                LogUtils.e(TAG, "通知服务器释放资源失败:" + t.toString());
            }
        });
    }


    private boolean isConditionsMet() {
        // 判断是否已经打开了自动升级开关
        boolean isOpenAutoUpgrade = GlobalConfig.getIsOpenAutoUpgradeApp(this);
        if (!isOpenAutoUpgrade) {
            LogUtils.e(TAG, "自动升级开关没打开，so应用自动升级不启动");
            return false;
        }
//        LogUtils.i(TAG, "满足 打开了自动升级开关");

        // 判断是否系统权限
        boolean isSystemApp = PackageUtils.isSystemApplication(LTApplication.shareApplication());
        if (!isSystemApp) {
            LogUtils.e(TAG, "不具备静默安装的权限，so应用自动升级不启动");
            return false;
        }
//        LogUtils.i(TAG, "满足 具备静默安装的权限");

        // 判断网络环境，再进一步操作
        if (!NetUtils.isWifi(this)) {
            LogUtils.e(TAG, "非wifi环境，so应用自动升级不启动");
            return false;
        }
//        LogUtils.i(TAG, "满足 是wifi环境");

        // 判断是否大于500M
        if (FileSizeUtil.getAvailableExternalMemorySize() / (1024 * 1024) <= 500) {
            LogUtils.e(TAG, "手机内存小于500M，so应用自动升级不启动");
            return false;
        }
//        LogUtils.i(TAG, "满足 手机内存大于500M");

        // 是否电量充足
        if (!isHighLevel()) {
            LogUtils.e(TAG, "电量不足，so应用自动升级不启动");
            return false;
        }
//        LogUtils.i(TAG, "满足 电量充足");

        return true;
    }

    /**
     * 判断电量是否充足
     */
    private boolean isHighLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        //你可以读到充电状态,如果在充电，可以读到是usb还是交流电

        if (batteryStatus == null) {
            LogUtils.e(TAG, "手机电量状态获取失败.");
            return false;
        }

        // 是否在充电
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
//        LogUtils.i(TAG, "手机充电中或者满电~ = " + isCharging);

        //当前剩余电量
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        LogUtils.i(TAG, "当前剩余电量 = " + level);

        //电量最大值
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        LogUtils.i(TAG, "电量最大值 = " + scale);

        //电量百分比
        float batteryPct = level / (float) scale;
//        LogUtils.i(TAG, "电量百分比 = " + batteryPct);

        // 是否正在充电或者电量大于30%
        return isCharging || batteryPct >= 0.3;
    }

    @Override
    public void onDestroy() {
        LogUtils.e(TAG, TAG + " onDestroy");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        if (ownResource) {
            notifyServerReleased();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
