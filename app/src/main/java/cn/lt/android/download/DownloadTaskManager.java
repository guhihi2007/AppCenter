package cn.lt.android.download;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.wanka.WanKaLog;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.ads.wanka.WanKaUrl;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.AppEntityDao;
import cn.lt.android.db.StatisticsEntity;
import cn.lt.android.db.StatisticsEntityDao;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.event.NewDownloadTask;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.FileUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;
import cn.lt.download.DownloadAgent;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.model.DownloadInfo;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.FileUtils;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;

import static cn.lt.android.util.AppUtils.isInstalled;


/**
 * Created by wenchao on 2016/1/22.
 * 下载任务管理器
 */
public class DownloadTaskManager {

    private AppEntityDao mAppEntityDao;
    private DownloadAgent mDownloader;
    private Map<String, DownloadSpeedListener> mDownloadSpeedListenerMap;
    private DownloadEventTransmitter transmitter = DownloadEventTransmitter.getInstance();
    private DownloadReportListener listener;

    private DownloadTaskManager() {
        mAppEntityDao = GlobalParams.getAppEntityDao();

        mDownloader = DownloadAgent.getImpl();  //获得代理类对象，可以导包（有引用库）。

        mDownloadSpeedListenerMap = new HashMap<>();

        listener = new DownloadReportListener();
    }

    public void init(final Application context) {
        mDownloader.bindService(context);

    }

    /***
     * 检查下载条件后下载并上报数据
     *
     * @param context
     * @param appEntity 要上报的下载数据
     * @param mode      下载模式
     * @param event     下载事件
     * @param pageName  页面名称
     */
    public void startAfterCheck(Context context, final AppEntity appEntity, final String mode, final String event, final String pageName, final String pageID, final String event_reason, final String source) {
        DownloadChecker.getInstance().check(context, appEntity, pageName, pageID, mode, new Runnable() {
            @Override
            public void run() {
                if (null != appEntity) {
                    reporeEventData(appEntity, event, mode, pageName, pageID, event_reason, source);

//                    if((DownloadBar.TYPE_REQUEST.equals(event) || DownloadBar.TYPE_UPGRADE.equals(event)) && TextUtils.isEmpty(appEntity.getDownloadUrl())) {
                    if (!"auto".equals(mode) && TextUtils.isEmpty(appEntity.getDownloadUrl())) {
                        ToastUtils.showToast("下载地址不存在");
                    }
//                        appEntity.setStatus(DownloadStatusDef.error);
//                        EventBus.getDefault().post(new DownloadEvent(FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath()),DownloadStatusDef.error,appEntity.getPackageName()));
//                        GlobalParams.getAppEntityDao().insertOrReplace(appEntity);
//                        DCStat.downloadFialedEvent(appEntity, mode, "downloadError", pageName, DownloadBar.URL_EMPTY,event,pageID);
//                        return;
//                    }
                    start(appEntity);
                }
            }
        });
    }

    /***
     * 上报数据
     *
     * @param appEntity    下载实体
     * @param event        事件类型：请求、暂停、继续、重试
     * @param mode         下载模式：normal/auto/onekey/app_auto_upgrade
     * @param pageName
     * @param pageID
     * @param event_reason
     * @param source
     */
    private void reporeEventData(AppEntity appEntity, String event, String mode, String pageName, String pageID, String event_reason, String source) {
        if (AppUtils.isScreenOn()) {   //只有在亮屏的情况下才上报
            if (appEntity.isOrderWifiContinue() && ((byte) appEntity.getStatus()) == DownloadStatusDef.paused && appEntity.getSoFar() == 0) {
                DCStat.downloadRequestReport(appEntity, mode, "request", pageName, pageID, event_reason, source); //上报下载请求
            } else {
                DCStat.downloadRequestReport(appEntity, mode, event, pageName, pageID, event_reason, source); //上报下载请求
            }
            appEntity.setOrderWifiContinue(false);
        } else {
            LogUtils.i("DCStat", "熄屏不上报");
        }
    }


    public void startAfterCheckList(Context context, final List<AppEntity> appEntitieList, final String mode, final String event, final String pageName, final String pageID, final String event_reason, final String source) {

        switch (StorageSpaceDetection.check()) {
            case StorageSpaceDetection.STORAGE_IS_0:
                for (AppEntity app : appEntitieList) {
                    LogUtils.i("zzz", "内存空间为0++++++");
                    DCStat.downloadFialedEvent(app, mode, "memoryError", pageName, pageID, "内存空间为0", "download");
                }
                StorageSpaceDetection.showEmptyTips(context, context.getString(R.string.memory_download_error));
                break;
            case StorageSpaceDetection.STORAGE_SMALL_200:
                for (AppEntity app : appEntitieList) {
                    DCStat.downloadFialedEvent(app, mode, "memoryError", pageName, pageID, "内存空间不足200Ｍ", "download");
                }
                StorageSpaceDetection.showPathSettingDialog(context, 200);
                break;
            default:
                break;
        }

        if (null != appEntitieList && appEntitieList.size() >= 1) {
            for (final AppEntity app : appEntitieList) {
                try {
                    String eventString;
//                    if (event == null) {
                    if (app.getIsOrderWifiDownload() && app.getStatus() == DownloadStatusDef.paused) {
                        eventString = "request";
                    } else if (app.getStatus() == DownloadStatusDef.paused) {
                        eventString = "continue";
                    } else if (app.getStatus() == DownloadStatusDef.retry || app.getStatus() == DownloadStatusDef.error) {
                        eventString = "retry";
                    } else {
                        eventString = "request";
                    }
//                    } else {
//                        eventString = event;
//                    }

                    start(app);
                    DCStat.downloadRequestReport(app, mode, eventString, pageName, pageID, event_reason, source); //上报下载请求
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO
                }
            }
        }

    }


    /**
     * 开始或者继续下载
     * 通过返回的BaseDownloadTask可以设置监听器
     *
     * @param baseAppInfo
     */
    public void start(final AppEntity baseAppInfo) {
        if (TextUtils.isEmpty(baseAppInfo.getSavePath())) {
            return;
        }
        // 这里是为了防止下载目录被删除了，所以要重新判断创建
        if (LTDirectoryManager.getInstance() == null) {
            LTDirectoryManager.initManager();
        } else {
            LTDirectoryManager.getInstance().init();
        }
        int downloadId = getDownloadId(baseAppInfo);
        int downloadState = 0;
        try {
            downloadState = mDownloader.getStatus(downloadId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //正在下载.5种情况。   有时bean的实际状态是 等待中，但是界面没有变成 等待中，而是 继续。
        if (DownloadStatusDef.isIng(downloadState)) {
            return;
        }
        if (DownloadStatusDef.completed == downloadState && FileUtil.fileExists(baseAppInfo.getSavePath())) {
            //下载完成 并且 安装包也存在的不再继续
            return;
        }
        List<AppEntity> list = mAppEntityDao.queryBuilder().list();
        if (list == null) {
            baseAppInfo.setApps_startDownloadTime(System.currentTimeMillis());
        } else {
            if (!list.contains(baseAppInfo)) {
                baseAppInfo.setApps_startDownloadTime(System.currentTimeMillis());
            }
        }

//        boolean wanKaSwitchOn = !TextUtils.isEmpty(Constant.WK_SWITCH) && !Constant.STATUS_CLOSE.equals(Constant.WK_SWITCH);
        boolean wanKaSwitchOn = PreferencesUtils.getBoolean(LTApplication.instance,Constant.WK_SWITCH);
        boolean isWankaCommercial = AdMold.WanKa.equals(baseAppInfo.getAdMold());
        boolean isWanKa = isWankaCommercial || (wanKaSwitchOn && baseAppInfo.canReplace() && !AdMold.WanDouJia.equals(baseAppInfo.getAdMold()));
        if (isWanKa) {
            DownloadInfo downloadInfo = DownloadTaskManager.getInstance().getDownloadInfo(baseAppInfo);
            int state = DownloadTaskManager.getInstance().getRealState(baseAppInfo, downloadInfo.getStatus());
//            WanKaLog.e("state = " + state);
            long sofar = downloadInfo.getSoFarBytes();
            long total = downloadInfo.getTotalBytes();

            boolean unDownload = DownloadStatusDef.isInvalid(state) && (sofar <= 0 || sofar == total);  // 未下载
            boolean retry = DownloadStatusDef.error == state || baseAppInfo.getStatus() == DownloadStatusDef.retry; // 重试
            boolean upgrade = InstallState.upgrade == state;                                            // 升级

            // 预约wifi 下载
            boolean orderWifi = baseAppInfo.getIsOrderWifiDownload();
//            WanKaLog.e("orderWifi = " + orderWifi);

            int downLoadApps = 0;
            try {
                downLoadApps = DownloadTaskManager.getInstance().getDownloadingList().size();
            } catch (RemoteException e) {
                //
            }

            if (unDownload || retry || upgrade || orderWifi || sofar <= 0) {
                // 未下载 升级 预约wifi
                if (orderWifi || retry) {
                    // 预约wifi 玩咖重试
                    executeDownload(baseAppInfo);
                } else {
                    // upgrade 字段给玩咖区分是否是升级，返回应用策略
                    // fromUpdate:  是否是更新（1 是），如果值为 1，会返回最大版本号的应用内容。
                    if (DownloadStatusDef.paused == downloadState && downLoadApps >= 2) {
                        // 等待中的任务(sofar为0)暂停之后 继续下载
                        executeDownload(baseAppInfo);
                    } else {
                        replaceAppInfoAndDownload(baseAppInfo, upgrade ? "1" : "");
                    }
                }

            } else {
                if (DownloadStatusDef.paused == state && downLoadApps <= 2) {
                    // 是否是升级
                    boolean inUpgradeList = false;
                    for (AppDetailBean detailBean : UpgradeListManager.getInstance().getAllUpgradeAppList()) {
                        if (detailBean.getPackage_name().equals(baseAppInfo.getPackageName())) {
                            inUpgradeList = true;
                            break;
                        }
                    }

                    // 断点续传
                    WanKaManager.doRequest(WanKaUrl.DOWNLOAD_START, baseAppInfo.getPackageName(), new SimpleResponseListener<JSONObject>() {
                        @Override
                        public void onSucceed(int what, Response<JSONObject> response) {
                            JSONObject jsonObject = response.get();
                            WanKaLog.e("onSucceed: " + jsonObject.toString());
                        }

                        @Override
                        public void onFailed(int what, Response<JSONObject> response) {
                            WanKaLog.e("玩咖断点续传上报网络请求失败，不影响逻辑");
                        }
                    }, inUpgradeList ? "1" : "", "1");
                }
                // 继续和其他状态下直接下载
                executeDownload(baseAppInfo);
            }

        } else {
            // 非玩咖资源直接执行下载
            executeDownload(baseAppInfo);
        }

    }

    /**
     * 替换下载地址(如果满足条件可替换)执行下载
     *
     * @param baseAppInfo 应用实体
     */
    private void replaceAppInfoAndDownload(final AppEntity baseAppInfo, final String whetherUpgrade) {
        WanKaManager.exposureSingleApp(baseAppInfo, new SimpleResponseListener<JSONObject>() {

            @Override
            public void onFinish(int what) {

                WanKaManager.doRequest(WanKaUrl.DOWNLOAD_START, baseAppInfo.getPackageName(), new SimpleResponseListener<JSONObject>() {

                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        String msg = null;
                        try {
                            JSONObject jsonObject = response.get();
                            WanKaLog.e("onSucceed: " + jsonObject.toString());
                            //                    baseAppInfo.setReportDataJsonObj(jsonObject);

                            msg = jsonObject.optString("msg");
                            JSONObject content = jsonObject.optJSONObject("content");

                            JSONObject extraDataJSONObject = content.optJSONObject("info");

                            String versionCode = extraDataJSONObject.optString("versionCode");
                            String apkMD5 = extraDataJSONObject.optString("apkMd5");
                            String apkUrl = extraDataJSONObject.optString("apkUrl");
                            // String pkgName = extraDataJSONObject.optString("package");

                            // 版本号转成数字比较
                            if (versionCode != null && !"null".equals(versionCode)) {
                                String ourVersionCode = baseAppInfo.getVersion_code();
                                WanKaLog.e("服务器的versionCode: " + ourVersionCode + "\t玩咖versionCode：" + versionCode);
                                Integer ourVersionCodeInt = Integer.valueOf(ourVersionCode);
                                Integer versionCodeInt = Integer.valueOf(versionCode);

                                if (ourVersionCodeInt <= versionCodeInt) {
                                    // 玩咖版本号大于等于我们版本号 可替换
                                    if (!TextUtils.isEmpty(apkUrl) && !"null".equals(apkUrl) && !TextUtils.isEmpty(apkMD5)) {
                                        WanKaLog.e("替换玩咖Url: " + apkUrl);
                                        WanKaLog.e("替换玩咖md5: " + apkMD5);
                                        // 替换下载地址和MD5值
                                        baseAppInfo.setPackage_md5(apkMD5);
                                        baseAppInfo.setDownloadUrl(apkUrl);
                                        // 标记当前App是玩咖的商业应用
                                        baseAppInfo.setReportDataJsonObj(jsonObject);
                                    } else {
                                        WanKaLog.e("玩咖没有返回md5或者url，没有替换Url: " + msg + "\t" + baseAppInfo.getDownloadUrl());
                                        baseAppInfo.setAdMold("");
                                    }
                                } else {
                                    if (AdMold.WanKa.equals(baseAppInfo.getAdMold())) {
                                        // 玩咖的广告资源 就算版本比我们的低，还是要上报下载完成 和安装完成
                                        WanKaLog.e("玩咖广告资源,下载上报时返回了低版本，不替换Url(继续下载完成上报和安装成功上报):" + baseAppInfo.getDownloadUrl());
                                    } else {
                                        WanKaLog.e("玩咖资源版本比我们的低，没有替换Url: " + baseAppInfo.getDownloadUrl());
                                        baseAppInfo.setAdMold("");
                                    }
                                }

                            } else {
                                WanKaLog.e("玩咖没有返回版本号，没有替换Url: " + msg + "\t" + baseAppInfo.getDownloadUrl());
                                baseAppInfo.setAdMold("");
                            }
                        } catch (Exception e) {
                            if (AdMold.WanKa.equals(baseAppInfo.getAdMold())) {
                                // 玩咖的广告资源 就算版本比我们的低，还是要上报下载完成 和安装完成
                                WanKaLog.e("出了点问题，没有替换Url:(继续下载完成上报和安装成功上报):" + msg + "\t" + baseAppInfo.getDownloadUrl());
                            } else {
                                WanKaLog.e("出了点问题，没有替换Url:  " + msg + "\t" + baseAppInfo.getDownloadUrl());
                                baseAppInfo.setAdMold("");
                            }
                        } finally {
                            executeDownload(baseAppInfo);
                            EventBus.getDefault().post(new NewDownloadTask(baseAppInfo));
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                        if (AdMold.WanKa.equals(baseAppInfo.getAdMold())) {
                            // 玩咖的广告资源 就算版本比我们的低，还是要上报下载完成 和安装完成
                            WanKaLog.e("玩咖广告资源,网络请求失败，没有替换Url(继续下载完成上报和安装成功上报):" + baseAppInfo.getDownloadUrl());
                        } else {
                            if (baseAppInfo.getReportDataJsonObj() == null) {
                                WanKaLog.e("网络请求失败，没有替换Url: " + baseAppInfo.getDownloadUrl());
                                baseAppInfo.setAdMold("");
                            } else {
                                WanKaLog.e("网络请求失败，已经替换过Url: " + baseAppInfo.getDownloadUrl());
                            }
                        }
                        executeDownload(baseAppInfo);
                        EventBus.getDefault().post(new NewDownloadTask(baseAppInfo));
                    }
                }, whetherUpgrade);
            }
        }, "下载开始之前的曝光");


    }

    /**
     * 存入数据库，执行下载
     *
     * @param baseAppInfo 应用实体
     */
    private void executeDownload(AppEntity baseAppInfo) {
        try {
            mAppEntityDao.insertOrReplace(baseAppInfo);
            mDownloader.startDownloader(baseAppInfo.getPackageName(), baseAppInfo.getDownloadUrl(), baseAppInfo.getSavePath(), 1000, 1, baseAppInfo.getIsOrderWifiDownload());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始下载所有未下载完成的应用
     * 前提条件：如果内存空间为零，终止一切自动下载操作
     * isOrderWifiDownload为标记是否预约wifi下载，默认为false即可
     */
    public void startAll(String mode, String event_reason, String source, boolean isOrderWifiDownload) throws RemoteException {
        List<AppEntity> downloadList = getDownloadTaskList();
        Long size = 0L;
        for (AppEntity appEntity : downloadList) {
            size = size + Long.parseLong(appEntity.getPackageSize());
        }
        LogUtils.i(size / (1048 * 1024) + "下载总大小,内存大小 =====》" + AppUtils.getAvailablMemorySize());
        if (AppUtils.getAvailablMemorySize() <= 0 || AppUtils.getAvailablMemorySize() < size / (1048 * 1024))
            return;
        String collectStatus = "";
        for (AppEntity appEntity : downloadList) {

            // 任务已经在下载队列中\自动升级\已经下载完成，无需启动下载，也不要上报数据
            if (DownloadStatusDef.isIng(appEntity.getStatus()) || appEntity.getIsAppAutoUpgrade() || appEntity.getStatus() == DownloadStatusDef.completed) {

                continue;
            }

            if (appEntity.getIsOrderWifiDownload() && appEntity.getStatus() == DownloadStatusDef.paused) {
                if (appEntity.getSoFar() == 0) {
                    collectStatus = "request";
                } else {
                    collectStatus = "continue";
                }
                appEntity.setOrderWifiContinue(true);
            } else if (appEntity.getStatus() == DownloadStatusDef.paused) {
                collectStatus = "continue";
            } else if (appEntity.getStatus() == DownloadStatusDef.retry || appEntity.getStatus() == DownloadStatusDef.error) {
                collectStatus = "retry";
            } else {
                collectStatus = "request";
            }

            // 是否属于wifi预约下载
            appEntity.setIsOrderWifiDownload(isOrderWifiDownload);
            if (AppUtils.apkIsNotExist(appEntity.getSavePath()) && appEntity.getSoFar() > 0) {
                DCStat.downloadRequestReport(appEntity, "auto", "request", "", "", "apk_deleted", "");//产品需求：自动继续时安装包不存在要上报apk_deleted added by ATian
            }
            startAfterCheck(LTApplication.shareApplication(), appEntity, mode, collectStatus, "", "", event_reason, source);
        }
    }

    /**
     * 启动应用自动升级
     */
    public void autoUpgradeApp(List<AppDetailBean> downloadList) throws RemoteException {
        transfer(downloadList);
        for (AppDetailBean appDetailBean : downloadList) {

            // 如果是忽略列表中的，不进行自动升级
            if (UpgradeListManager.getInstance().isIgnore(appDetailBean.getPackage_name())) {
                continue;
            }

            AppEntity appEntity = getAppEntityByPkg(appDetailBean.getPackage_name());
            if (appEntity == null) {
                appEntity = appDetailBean.getDownloadAppEntity();
            }
            appEntity.setIsAppAutoUpgrade(true);
            /*如果是下载完成或者安装失败，不执行自动升级*/
            if ((DownloadStatusDef.completed == appEntity.getStatus()) && FileUtil.fileExists(appEntity.getSavePath())) {
                return;
            }
            start(appEntity);

            String event_detail = "";
            if (AppUtils.apkIsNotExist(appEntity.getSavePath()) && appEntity.getSoFar() > 0) {
                event_detail = "apk_deleted";
                //这里只保存，以request保存，才能让之后字段延续
                DCStat.downloadRequestReport(appEntity, "app_auto_upgrade", "request", Constant.PAGE_APP_UDPATE, "", event_detail, "app_auto_upgrade");
            }

            if (DownloadStatusDef.paused == appEntity.getStatus()) {
                DCStat.downloadRequestReport(appEntity, "app_auto_upgrade", "continue", Constant.PAGE_APP_UDPATE, "", event_detail, "app_auto_upgrade"); //应用自动升级上报下载请求
            } else {
                DCStat.downloadRequestReport(appEntity, "app_auto_upgrade", "upgrade", Constant.PAGE_APP_UDPATE, "", "", "app_auto_upgrade"); //应用自动升级上报下载请求
            }
        }
    }

    /**
     * 暂停应用自动升级
     */
    public void autoUpgradeAppPause() throws RemoteException {
        List<AppEntity> downloadList = getDownloadingList();

        for (AppEntity app : downloadList) {
            if (app.getIsAppAutoUpgrade()) {
                pause(app, "app_auto_upgrade", "", "", "", "app_auto_upgrade");
            }
        }
    }

    /**
     * 暂停
     *
     * @param appInfo
     */
    public void pause(AppEntity appInfo, String pause_mode, String pageName, String pageId, String event_reason, String source) throws RemoteException {
        mDownloader.pauseDownloader(getDownloadId(appInfo));
        DCStat.downloadRequestReport(appInfo, pause_mode, "pause", pageName, pageId, event_reason, source);//上报暂停
    }

    /**
     * 暂停所有
     */
    public void pauseAll(String pause_mode, String pageName, String pageId, String event_reason, String source) throws RemoteException {
//        mDownloader.pauseAllTasks();
        List<AppEntity> downlloadingList = getDownloadTaskList();
        for (AppEntity app : downlloadingList) {
            if (!DownloadStatusDef.isIng(app.getStatus())) {
                continue;
            }
            pause(app, pause_mode, pageName, pageId, event_reason, source);
        }

    }

    /**
     * 暂停所有(不包含上报)
     * 网络监听接受无网时调用
     */
    public void pauseAll() throws RemoteException {
//        mDownloader.pauseAllTasks();
        List<AppEntity> downlloadingList = getDownloadTaskList();
        for (AppEntity app : downlloadingList) {
            if (!DownloadStatusDef.isIng(app.getStatus())) {
                continue;
            }
            mDownloader.pauseDownloader(getDownloadId(app));
        }

    }

    /**
     * 获取app状态
     * 1/已安装状态： 可安装/可升级
     * 2/包括各种下载状态
     *
     * @param baseAppInfo
     * @return
     */
    public int getState(AppEntity baseAppInfo) throws RemoteException {
        int downloadState = mDownloader.getStatus(getDownloadId(baseAppInfo));
        if (DownloadStatusDef.isInvalid(downloadState)) {
            if (isInstalled(baseAppInfo.getPackageName())) {
                AppDetailBean upgradeApp = UpgradeListManager.getInstance().findByAppId(baseAppInfo.getAppClientId());
                if (upgradeApp == null) return InstallState.installed;
                return InstallState.upgrade;
            }
        }

        return downloadState;
    }


    public int getDownloadId(AppEntity appEntity) {
        return FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath());
    }

    /**
     * 获取当前进度
     *
     * @param appEntity
     * @return
     */
    public long getSofar(AppEntity appEntity) throws RemoteException {
        return mDownloader.getSofar(getDownloadId(appEntity));
    }


    /**
     * 获取总进度
     *
     * @param appEntity
     * @return
     */
    public long getTotal(AppEntity appEntity) throws RemoteException {
        return mDownloader.getTotal(getDownloadId(appEntity));
    }


    /**
     * 安装完成后移除
     *
     * @param packageName
     */
    public void remove(String packageName, boolean removeFromDatabase) throws RemoteException {
        //查找下载库
        List<AppEntity> list = mAppEntityDao.queryBuilder().where(AppEntityDao.Properties.PackageName.eq(packageName)).list();


        if (list.size() == 0) return;
        AppEntity appEntity = list.get(0);

        //生成downloadId
        int downloadId = getDownloadId(appEntity);
        //移除下载，从下载数据库中删除
        mDownloader.remove(downloadId);
        if (removeFromDatabase) {
            //删除本地库
            mAppEntityDao.delete(appEntity);
        }
        if (GlobalConfig.getAutoDeleteApk(LTApplication.shareApplication())) {
            //删除文件
            FileUtils.deleteFile(appEntity.getSavePath());
        }
        LogUtils.d("ccc", "发送移除事件了，将要更新为打开");  //会更新变打开
        EventBus.getDefault().post(new RemoveEvent(appEntity, true));
    }

    /**
     * 移除
     *
     * @param appEntity
     */
    public void remove(final AppEntity appEntity) throws Exception {
        int downloadId = getDownloadId(appEntity);
        //删除本地库
        mAppEntityDao.delete(appEntity);
        //移除下载，从下载数据库中删除
        mDownloader.remove(downloadId);

        // 删除文件(延迟处理是为了防止删包后任务还未暂停导致抛异常变重试)
        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FileUtils.deleteFile(appEntity.getSavePath());
            }
        }, 600);

        EventBus.getDefault().post(new RemoveEvent(appEntity));
    }

    /**
     * 移除旧文件,用于重试时
     *
     * @param appEntity
     */
    public void removeFile(AppEntity appEntity) throws Exception {
        //删除文件
        FileUtils.deleteFile(appEntity.getSavePath());
    }

    public List<AppEntity> getAll() {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().list();
        List<AppEntity> downloadList = new ArrayList<>();
        try {
            for (AppEntity appEntity : dbList) {
                int state = getState(appEntity);
                long sofar = getSofar(appEntity);
                long total = getTotal(appEntity);
                appEntity.setStatus(state);
                appEntity.setSoFar(sofar);
                appEntity.setTotal(total);
                downloadList.add(appEntity);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
        return downloadList;
    }

    /**
     * 获取下载任务列表
     *
     * @return
     */
    public List<AppEntity> getDownloadTaskList() {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().list();
        List<AppEntity> downloadList = new ArrayList<>();
        try {
            for (AppEntity appEntity : dbList) {
                int state = getState(appEntity);
                //未安装 且 未下载完成的 则在下载任务列表中
                long sofar = getSofar(appEntity);
                long total = getTotal(appEntity);
                if (state != InstallState.installed && state != DownloadStatusDef.completed && state != InstallState.upgrade) {
                    appEntity.setStatus(state);
                    appEntity.setSoFar(sofar);
                    appEntity.setTotal(total);
                    downloadList.add(appEntity);
                }
            }
            Collections.sort(downloadList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return downloadList;
    }

    /**
     * 获取非应用自动升级下载任务列表
     *
     * @return
     */
    public List<AppEntity> getUnAutoUpgradeDownloadTaskList() {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().list();
        List<AppEntity> downloadList = new ArrayList<>();
        try {
            for (AppEntity appEntity : dbList) {
                int state = getState(appEntity);
                //未安装 且 未下载完成的 则在下载任务列表中
                long sofar = getSofar(appEntity);
                long total = getTotal(appEntity);
                if (appEntity.getIsAppAutoUpgrade()) {
                    continue;
                } else if (state != InstallState.installed && state != DownloadStatusDef.completed && state != InstallState.upgrade) {
                    appEntity.setStatus(state);
                    appEntity.setSoFar(sofar);
                    appEntity.setTotal(total);
                    downloadList.add(appEntity);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return downloadList;
    }

    /**
     * 获取正在下载的任务列表
     *
     * @return
     */
    public List<AppEntity> getDownloadingList() throws RemoteException {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().list();
        List<AppEntity> downloadList = new ArrayList<>();
        for (AppEntity appEntity : dbList) {
            int state = getState(appEntity);
            //未安装 且 未下载完成的 则在下载任务列表中
            if (DownloadStatusDef.isIng(state)) {
                long sofar = getSofar(appEntity);
                long total = getTotal(appEntity);
                appEntity.setStatus(state);
                appEntity.setSoFar(sofar);
                appEntity.setTotal(total);
                downloadList.add(appEntity);
            }
        }
        return downloadList;
    }


    /**
     * 获取安装任务列表，
     * 包括已下载未安装 和 已安装app
     *
     * @return
     */
    public List<AppEntity> getInstallTaskList() {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().orderDesc(AppEntityDao.Properties.Apps_endDownloadTime).list();
        List<AppEntity> installList = new ArrayList<>();
        try {
            for (AppEntity appEntity : dbList) {
                int state = 0;
                state = getState(appEntity);
                if (state == DownloadStatusDef.completed) {
                    long sofar = getSofar(appEntity);
                    long total = getTotal(appEntity);
                    if (appEntity.getLackofmemory() != null && appEntity.getLackofmemory()) {
                        appEntity.setStatus(InstallState.install_failure);
                    } else {
                        appEntity.setStatus(state);
                    }
                    appEntity.setSoFar(sofar);
                    appEntity.setTotal(total);
                    installList.add(appEntity);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return installList;
    }


    /**
     * 获取内存不足 安装任务失败列表，
     *
     * @return
     */

    public List<AppEntity> getInstallFailureTaskList() {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().orderDesc(AppEntityDao.Properties.Apps_endDownloadTime).list();
        List<AppEntity> installList = new ArrayList<>();
        for (AppEntity appEntity : dbList) {
            int state = 0;
            try {
                state = getState(appEntity);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (state == DownloadStatusDef.completed) {
                if (appEntity.getLackofmemory() != null && appEntity.getLackofmemory()) {
                    appEntity.setStatus(InstallState.install_failure);
                    installList.add(appEntity);
                }
            }
        }
        return installList;  //一上来，有可能是空
    }

    //因内存不足而失败
    public boolean isFailureByInstall(String id) {
        List<AppEntity> installFailureTaskList = getInstallFailureTaskList();
        for (AppEntity appEntity : installFailureTaskList) {
            if (appEntity.getAppClientId().equals(id)) {
                return appEntity.getLackofmemory();
            }
        }
        return false;
    }

    /**
     * 把下载数据载入 应用详情实体对象中
     *
     * @param appDetailBeanList
     */
    public void transfer(List<AppDetailBean> appDetailBeanList) throws RemoteException {
        if (appDetailBeanList == null || appDetailBeanList.size() == 0) return;
        for (AppDetailBean appDetailBean : appDetailBeanList) {
            transfer(appDetailBean);
        }
    }

    /**
     * 把下载数据载入 应用详情实体对象中
     */
    public void transferBriefBeanList(List<AppBriefBean> briefList) throws RemoteException {
        if (briefList == null || briefList.size() == 0) return;
        for (AppBriefBean appDetailBean : briefList) {
            transfer(appDetailBean);
        }
    }

    /**
     * 获取下载实体对象 转换服务器下发的实体
     *
     * @param appDetailBean
     * @return
     */
    public AppEntity transfer(AppDetailBean appDetailBean) throws RemoteException {
        AppEntity appEntity = new AppEntity();
        if (appDetailBean.isAdData()) {
            long hashId = appDetailBean.getPackage_name().hashCode();
            appEntity.setId(hashId < 0 ? hashId : hashId * -1);
        } else {
            appEntity.setId(Long.valueOf(appDetailBean.getAppClientId()));
        }

        appEntity.setPackageName(appDetailBean.getPackage_name());
        appEntity.setName(appDetailBean.getName());
        appEntity.setAlias(appDetailBean.getAlias());

        if (appDetailBean.isAdData()) {
            appEntity.setDownloadUrl(appDetailBean.getDownload_url());
            appEntity.setIconUrl(appDetailBean.getIcon_url());
        } else {
            appEntity.setDownloadUrl(GlobalConfig.combineDownloadUrl(appDetailBean.getDownload_url()));
            appEntity.setIconUrl(GlobalConfig.combineImageUrl(appDetailBean.getIcon_url()));
        }

        appEntity.setSavePath(getSaveDirPath() + File.separator + appDetailBean.getPackage_name() + "_" + appDetailBean.getAppClientId() + ".apklll");
        LogUtils.d("path", "AppDetailBean转后==>" + getSaveDirPath() + File.separator + appDetailBean.getPackage_name() + "_" + appDetailBean.getAppClientId() + ".apklll");
/******************以下是一次性拿bean的方式*************************/
        DownloadInfo downloadInfo = getDownloadInfo(appEntity);
        if (downloadInfo != null) {
            appEntity.setStatus(getRealState(appEntity, downloadInfo.getStatus()));
            appEntity.setSoFar(downloadInfo.getSoFarBytes());
            appEntity.setTotal(downloadInfo.getTotalBytes());
        }
        appEntity.setPackageSize(appDetailBean.getPackage_size());
        appEntity.setApps_type(appDetailBean.getApps_type());
        appEntity.setDescription(appDetailBean.getDescription());
        appEntity.setPackage_md5(appDetailBean.getPackage_md5());
        appEntity.setVersion_code(appDetailBean.getVersion_code());
        appEntity.setVersion_name(appDetailBean.getVersion_name());
        appEntity.setCorner_url(appDetailBean.getCorner_url());
        appEntity.setReviews(appDetailBean.getReviews());
        appEntity.setCreated_at(appDetailBean.getCreated_at());
        appEntity.setDownload_count(appDetailBean.getDownload_count());
        appEntity.setCategory(appDetailBean.getCategory());
        appEntity.setIsAD(appDetailBean.isAd());

        appEntity.setCanReplace(appDetailBean.canReplace());
        appEntity.setAdMold(appDetailBean.getAdMold());
        appEntity.setReportDataJsonObj(appDetailBean.getReportData());

        appDetailBean.setDownloadAppEntity(appEntity);
        return appEntity;
    }

    public AppEntity transfer(AppBriefBean appBriefBean) throws RemoteException {
        AppEntity appEntity = new AppEntity();
        if (appBriefBean.isAdData()) {
            try {
                long hashId = appBriefBean.getPackage_name().hashCode();
                appEntity.setId(hashId < 0 ? hashId : hashId * -1);
                LogUtils.d("DLM", "baoming：" + appBriefBean.getPackage_name());
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.d("DLM", "hashCode抛异常：" + e.toString());
            }
        } else {
            appEntity.setId(Long.valueOf(appBriefBean.getAppClientId()));
        }
        appEntity.setPackageName(appBriefBean.getPackage_name());
        appEntity.setName(appBriefBean.getName());
        appEntity.setAlias(appBriefBean.getAlias());

        if (appBriefBean.isAdData()) {
            appEntity.setDownloadUrl(appBriefBean.getDownload_url());
            appEntity.setIconUrl(appBriefBean.getIcon_url());
        } else {
            appEntity.setDownloadUrl(GlobalConfig.combineDownloadUrl(appBriefBean.getDownload_url()));
            appEntity.setIconUrl(GlobalConfig.combineImageUrl(appBriefBean.getIcon_url()));
        }
        String downloadPath = getSaveDirPath() + File.separator + appBriefBean.getPackage_name() + "_" + appBriefBean.getAppClientId() + ".apklll";
        LogUtils.d("path", "AppBriefBean转后==>" + downloadPath);
        appEntity.setSavePath(downloadPath);
        DownloadInfo downloadInfo = getDownloadInfo(appEntity);
        appEntity.setStatus(getRealState(appEntity, downloadInfo.getStatus()));
        appEntity.setSoFar(downloadInfo.getSoFarBytes());
        appEntity.setTotal(downloadInfo.getTotalBytes());
        appEntity.setPackageSize(appBriefBean.getPackage_size());
        appEntity.setApps_type(appBriefBean.getApps_type());
        appEntity.setDescription(appBriefBean.getDescription());
        appEntity.setPackage_md5(appBriefBean.getPackage_md5());
        appEntity.setVersion_code(appBriefBean.getVersion_code());
        appEntity.setVersion_name(appBriefBean.getVersion_name());
        appEntity.setCorner_url(appBriefBean.getCorner_url());
        appEntity.setReviews(appBriefBean.getReviews());
        appEntity.setCreated_at(appBriefBean.getCreated_at());
        appEntity.setDownload_count(appBriefBean.getDownload_count());
        appEntity.setCategory(appBriefBean.getCategory());
        appEntity.setIsAD(appBriefBean.isAD());

        appEntity.setCanReplace(appBriefBean.canReplace());
        appEntity.setAdMold(appBriefBean.getAdMold());
        appEntity.setReportDataJsonObj(appBriefBean.getReportData());
//        appEntity.p1 = appBriefBean.p1;
//        appEntity.p2 = appBriefBean.p2;
        appBriefBean.setDownloadAppEntity(appEntity);

        return appEntity;
    }

    /**
     * 针对推荐位app初始化
     *
     * @param recommendBean
     * @return
     */
    public AppEntity transfer(RecommendBean recommendBean) throws RemoteException {
        AppEntity appEntity = new AppEntity();
        long id = Long.parseLong(recommendBean.getId());
        appEntity.setId(id);
        appEntity.setPackageName(recommendBean.getPackage_name());
        appEntity.setName(recommendBean.getName());
        appEntity.setAlias(recommendBean.getAlias());
        appEntity.setDownloadUrl(GlobalConfig.combineDownloadUrl(recommendBean.getDownload_url()));
        appEntity.setSavePath(getSaveDirPath() + File.separator + recommendBean.getPackage_name() + "_" + recommendBean.getId() + ".apklll");
        LogUtils.d("path", "RecommendBean转后==>" + getSaveDirPath() + File.separator + recommendBean.getPackage_name() + "_" + recommendBean.getId() + ".apklll");
        appEntity.setIconUrl(GlobalConfig.combineImageUrl(recommendBean.getIcon_url()));
        DownloadInfo downloadInfo = getDownloadInfo(appEntity);
        appEntity.setStatus(getRealState(appEntity, downloadInfo.getStatus()));
        appEntity.setSoFar(downloadInfo.getSoFarBytes());
        appEntity.setTotal(downloadInfo.getTotalBytes());
        appEntity.setPackageSize(recommendBean.getPackage_size());
        appEntity.setApps_type(recommendBean.getApps_type());
        appEntity.setReviews(recommendBean.getReviews());
        appEntity.setDownload_count(recommendBean.getDownload_count());
        appEntity.setCorner_url(GlobalConfig.combineImageUrl(recommendBean.getCorner_url()));
        appEntity.setIsAD(false);
        appEntity.setVersion_name(recommendBean.getVersion_name());
        appEntity.setVersion_code(recommendBean.getVersion_code());

        appEntity.setCanReplace(recommendBean.is_replace());
        appEntity.setReportDataJsonObj(recommendBean.getReportData());

        recommendBean.setAppEntity(appEntity);
        return appEntity;
    }

    /**
     * AppEntity复制
     */
    public AppEntity appEntityCopy(AppEntity entity) throws RemoteException {
        AppEntity appEntity = new AppEntity();
        appEntity.setId(Long.valueOf(entity.getAppClientId()));
        appEntity.setPackageName(entity.getPackageName());
        appEntity.setName(entity.getName());
        appEntity.setAlias(entity.getAlias());
        appEntity.setDownloadUrl(entity.getDownloadUrl());
        appEntity.setSavePath(entity.getSavePath());
        DownloadInfo downloadInfo = getDownloadInfo(appEntity);
        appEntity.setStatus(getRealState(appEntity, downloadInfo.getStatus()));
        appEntity.setSoFar(downloadInfo.getSoFarBytes());
        appEntity.setTotal(downloadInfo.getTotalBytes());
        appEntity.setPackageSize(entity.getPackageSize());
        appEntity.setApps_type(entity.getApps_type());
        appEntity.setDescription(entity.getDescription());
        appEntity.setPackage_md5(entity.getPackage_md5());
        appEntity.setVersion_code(entity.getVersion_code());
        appEntity.setVersion_name(entity.getVersion_name());
        appEntity.setCorner_url(entity.getCorner_url());
        appEntity.setReviews(entity.getReviews());
        appEntity.setCreated_at(entity.getCreated_at());
        appEntity.setDownload_count(entity.getDownload_count());
        appEntity.setCategory(entity.getCategory());
        appEntity.setIsAD(entity.getIsAD());
        appEntity.setCanReplace(entity.canReplace());
        appEntity.setAdMold(entity.getAdMold());
        appEntity.setReportData(entity.getReportData());
        return appEntity;
    }

    /**
     * 是否有正在下载的应用
     *
     * @param appInfos
     * @return
     */
    public boolean hasDownloading(List<AppEntity> appInfos) throws RemoteException {
        for (AppEntity appInfo : appInfos) {
            int downloadState = mDownloader.getStatus(getDownloadId(appInfo));
            if (DownloadStatusDef.isIng(downloadState)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取保存路劲
     *
     * @return
     */
    public String getSaveDirPath() {
        return LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.root);
    }


    /**
     * 开始监听进度
     */
    public void listenSpeed(final AppEntity appEntity) {
        if (mDownloadSpeedListenerMap.containsKey(appEntity.getAppClientId())) {
            return;
        }
        DownloadSpeedListener listener = new DownloadSpeedListener(appEntity);
        mDownloadSpeedListenerMap.put(appEntity.getAppClientId(), listener);
        listener.start();
    }

    /**
     * 取消监听进度
     */
    public void cancelListenSpeed(final AppEntity appEntity) {
        if (mDownloadSpeedListenerMap.containsKey(appEntity.getAppClientId())) {
            DownloadSpeedListener listener = mDownloadSpeedListenerMap.get(appEntity.getAppClientId());
            listener.stop();
            mDownloadSpeedListenerMap.remove(appEntity.getAppClientId());
        }
    }


    private final static class HolderClass {
        private final static DownloadTaskManager INSTANCE = new DownloadTaskManager();
    }

    public static DownloadTaskManager getInstance() {
        return HolderClass.INSTANCE;
    }

    /**
     * 根据包名获取AppEntity
     *
     * @param pkg 应用包名
     * @return AppEntity
     */
    public synchronized AppEntity getAppEntityByPkg(String pkg) {
        AppEntity appEntity = null;
        if (!TextUtils.isEmpty(pkg)) {
            try {
                appEntity = GlobalParams.getAppEntityDao().queryBuilder().where(AppEntityDao.Properties.PackageName.eq(pkg)).unique();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return appEntity;
    }

    /***
     * 根据包名查询统计表里的一个对象
     *
     * @param pkg
     * @return
     */
    public synchronized StatisticsEntity getStasticByPkg(String pkg) {
        StatisticsEntity appEntity = null;
        if (!TextUtils.isEmpty(pkg)) {
            try {
                appEntity = GlobalParams.getStatisticsEntityDao().queryBuilder().where(StatisticsEntityDao.Properties.MPkgName.eq(pkg)).unique();
            } catch (Exception e) {
                LogUtils.i("DCStat", "查询上报数据异常信息" + e.getMessage());
                e.printStackTrace();
            }
        }
        return appEntity;
    }

    /***
     * 获取bean 获取
     *
     * @param appEntity
     * @return
     */
    public DownloadInfo getDownloadInfo(AppEntity appEntity) {
        try {
            downloadInfo = mDownloader.getCommonDownloadInfo(getDownloadId(appEntity));
            LogUtils.d("FileDownMgr", "获取bean" + downloadInfo.toString());
        } catch (RemoteException e) {
            LogUtils.d("FileDownMgr", "RemoteException" + e.getMessage());
            e.printStackTrace();
        }
        return downloadInfo;
    }

    private DownloadInfo downloadInfo;

    /**
     * 过滤装态，但此种过滤会耗时
     *
     * @param baseAppInfo
     * @param downloadState
     * @return
     */
    public int getRealState(AppEntity baseAppInfo, int downloadState) {
        if (DownloadStatusDef.isInvalid(downloadState)) {
            if (isInstalled(baseAppInfo.getPackageName())) {
                AppDetailBean upgradeApp = UpgradeListManager.getInstance().findByAppId(baseAppInfo.getAppClientId());
                if (upgradeApp == null) {
                    return InstallState.installed;
                }
                return InstallState.upgrade;
            }
        }
        return downloadState;
    }


    /**
     * 根据包名查询是否在任务列表里面
     *
     * @param pkg 应用包名
     * @return AppEntity
     */
    public synchronized boolean isInTask(String pkg) {
        AppEntity appEntity = null;
        if (!TextUtils.isEmpty(pkg)) {
            try {
                appEntity = GlobalParams.getAppEntityDao().queryBuilder().where(AppEntityDao.Properties.PackageName.eq(pkg)).unique();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (appEntity == null) {
            return false;
        }

        return true;
    }


    public List<AppEntity> getInstallSignConflictList() {
        List<AppEntity> dbList = mAppEntityDao.queryBuilder().orderDesc(AppEntityDao.Properties.Apps_endDownloadTime).list();
        List<AppEntity> installSignConflictList = new ArrayList<>();
        try {
            for (AppEntity appEntity : dbList) {
                if (appEntity.getErrorType() == (long) DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                    installSignConflictList.add(appEntity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installSignConflictList;
    }
}
