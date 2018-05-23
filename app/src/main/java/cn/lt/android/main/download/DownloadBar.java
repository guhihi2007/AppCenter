package cn.lt.android.main.download;

import android.app.Activity;
import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.entity.PkgInfoBean;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.event.DownloadUrlIsReturnEvent;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.AppAutoUpgradeHolder;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.model.DownloadInfo;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by wenchao on 2016/3/11.
 * 下载进度展示，以及更新ui
 * 继承此类可实现下载进度条相关功能
 */
public abstract class DownloadBar extends RelativeLayout {
    public static final String TYPE_REQUEST = "request";
    private static final String TYPE_CONTINUE = "continue";
    private static final String TYPE_RETRY = "retry";
    public static final String TYPE_UPGRADE = "upgrade";
    private static final String TYPE_REDOWNLOAD = "request [file is not exist, reDownload(byInstall)]";
    public static final String URL_EMPTY = "url_empty";

    public AppEntity mAppEntity;
    public String mPageName;
    public String mID;
    // 判断app是不是推送过来的
    private boolean appIsByPush = false;
    public Context mContext;

    public DownloadBar(Context context) {
        super(context);
        this.mContext = context;
        initialize();
    }

    public DownloadBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initialize();
    }

    public DownloadBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initialize();
    }

    private void initialize() {
        assignViews();
    }

    public void setData(AppEntity appEntity, String pageName) {
        this.mPageName = pageName;
        if (appEntity != null) {
            this.mAppEntity = appEntity;
            updateState(appEntity);
        }
    }

    /***
     * 要求一下页面上报下载所在页面ID  如：分类详情/游戏详情/专题详情/选项卡专题等页面，目的：计算下载转化率
     *
     * @param appEntity
     * @param pageName
     * @param id
     */
    public void setData(AppEntity appEntity, String pageName, String id) {
        this.mPageName = pageName;
        this.mID = id;
        LTApplication.instance.from_id = mID;
        if (appEntity != null) {
            this.mAppEntity = appEntity;
            updateState(appEntity);
        }
    }

    private void updateState(final AppEntity appEntity) {
        DownloadInfo downloadInfo = DownloadTaskManager.getInstance().getDownloadInfo(appEntity);
        int state = DownloadTaskManager.getInstance().getRealState(appEntity, downloadInfo.getStatus());
        long sofar = downloadInfo.getSoFarBytes();
        long total = downloadInfo.getTotalBytes();

        /******************************************/
        //为了解决断网重启进程移动网进入状态异常
        if (state == DownloadStatusDef.INVALID_STATUS && appEntity.getStatus() == DownloadStatusDef.paused) {
            state = DownloadStatusDef.paused;
        }
        if (InstallState.upgrade == state) {
            //升级
            showUpgrade();
        } else if (DownloadStatusDef.isInvalid(state)) {

            if (sofar <= 0 || sofar == total) {// 还没下载 或者 下载完成安装后又卸载掉了
                //未下载，进度为0
                showDownload();
            } else if (sofar < 100) {
                //未下载 ，进度不为0
                showContinue(sofar, total);
            } else {
                // 已下载 ，进度为100（应用更新时，新包安装前，会卸载老包，此时发出的卸载状态会走这里）
                showOpenApp();
                LogUtils.e("ccc", "downloadbar变打开");
            }

        } else if (DownloadStatusDef.isIng(state)) {

            int downLoadApps = 0;
            try {
                downLoadApps = DownloadTaskManager.getInstance().getDownloadingList().size();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (DownloadStatusDef.pending == state && downLoadApps > 2) {
                //队列中的
                showWaiting(sofar, total);
            } else {
                // 正在下载中的
                showProgress(sofar, total);
            }

        } else if (DownloadStatusDef.error == state) {
            //下载错误
            showRetry();

        } else if (DownloadStatusDef.paused == state) {
            //暂停
            showContinue(sofar, total);
        } else if (DownloadStatusDef.completed == state) {

            if (DownloadTaskManager.getInstance().isFailureByInstall(appEntity.getAppClientId())) {
                //showRetry();  因内存不足，不显示重试
                showInstall();

            } else if (InstallManager.getInstance().isAppInstalling(appEntity.getPackageName())) {
                showInstalling();

            } else {
                // check install state
                switch (appEntity.getStatus()) {
                    case InstallState.installing:
                        showInstalling();
                        break;
                    case InstallState.install_failure:
                        if (!DownloadTaskManager.getInstance().isFailureByInstall(appEntity.getAppClientId())) {
                            showRetry();   //  因内存不足，不显示重试
                        }
                        break;
                    case InstallState.installed:
                        LogUtils.e("ccc", "下载完成的大前提downloadbar变打开");
                        showOpenApp();
                        break;
                    default:
                        showInstall();
                        break;
                }
            }


        } else if (InstallState.installed == state) {
            //已安装
            showOpenApp();
            LogUtils.e("ccc", "最外层打开");
        }
    }

    private boolean isoncl = true;

    /**
     * 按钮点击事件，供给按钮触发调用
     */
    public void doClick() {
        mPageName = appIsByPush ? "notification" : mPageName;

        if (mAppEntity == null) return;
        updateNetIcon(mAppEntity);
        int realState;
        long realSofar, realTotal;
        //点击时校准进度及状态
        DownloadInfo downloadInfo = DownloadTaskManager.getInstance().getDownloadInfo(mAppEntity);
        realState = DownloadTaskManager.getInstance().getRealState(mAppEntity, downloadInfo.getStatus());
        realSofar = downloadInfo.getSoFarBytes();
        realTotal = downloadInfo.getTotalBytes();
        long soFar = mAppEntity.getSoFar();
        long total = mAppEntity.getTotal();
        int state = mAppEntity.getStatus();
        Log.i("DownloadBar", mAppEntity.getName() + "当前按钮状态---》" + state);
        if (state == InstallState.installing) return;
        if (realSofar != soFar) {
            soFar = realSofar;
            mAppEntity.setSoFar(soFar);
        }

        if (realTotal != total) {
            total = realTotal;
            mAppEntity.setTotal(total);
        }

        if (realState != state) {
            state = realState;
            mAppEntity.setStatus(state);
        }
        if (InstallState.upgrade == state) {
            //可更新,执行下载
            goUpgrade();
        } else if (InstallState.installed == state) {
            //已安装，打开
            AppUtils.openApp(mContext, mAppEntity.getPackageName());
        } else if (DownloadStatusDef.isInvalid(state)) {
            if (PopWidowManageUtil.needAutoInstallDialog(mContext)) {
                AutoInstallerContext.getInstance().promptUserOpen((Activity) mContext);
            }
            startDownload(TYPE_REQUEST, "");
        } else if (DownloadStatusDef.isIng(state)) {
            //点击暂停,每次点击后，1s内再点击无效。
            try {
                if (isoncl) {
                    mAppEntity.setIsAppAutoUpgrade(false); //复位提示语
                    DownloadTaskManager.getInstance().pause(mAppEntity, "manual", mPageName, mID, "", "");
                    isoncl = false;
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isoncl = true;
                        }
                    }, 1000);
                }

            } catch (RemoteException re) {
                re.printStackTrace();
                // TODO:
                return;
            }

        } else if (DownloadStatusDef.error == state) {
            //重试下载
            //点击重试,每次点击后，1s内再点击无效。
            if (isoncl) {
                boolean apkNotExist = AppUtils.apkIsNotExist(mAppEntity.getSavePath());
                if (apkNotExist) {
                    DCStat.downloadRequestReport(mAppEntity, "manual", "request", mPageName, mID, "apk_deleted" , "");// 这里只为存库，为重试上报apk_deleted做准备 ATian
                }
                if (mAppEntity.getReportData() != null) {
                    // 玩咖的应用直接开始下载去
                    DownloadTaskManager.getInstance().startAfterCheck(mContext, mAppEntity, "manual", "retry", mPageName, mID, apkNotExist ? "apk_deleted" : "", "");
                } else {
                    requestData(mAppEntity, apkNotExist);
                }

                isoncl = false;
                LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isoncl = true;
                    }
                }, 1000);
            }

        } else if (DownloadStatusDef.paused == state) {
            //点击继续
            if (AppUtils.apkIsNotExist(mAppEntity.getSavePath())) {
                DCStat.downloadRequestReport(mAppEntity, "", TYPE_CONTINUE, mPageName, mID, "apk_deleted", "");   //产品需求：手动继续如果安装包不存在要上报apk_apk_deleted added by ATian
                startDownload("request", "apk_deleted");
            } else {
                startDownload(TYPE_CONTINUE, "");
            }
        } else if (DownloadStatusDef.completed == state) {
            if (InstallManager.getInstance().isAppInstalling(mAppEntity.getPackageName())) {
                return;
            }
            if (AppUtils.apkIsNotExist(mAppEntity.getSavePath())) {
                try {
//                    DownloadAgent.getImpl().testExcption();//可以用来模拟页面下载按钮发生异常的情况
                    DCStat.installEvent(mAppEntity, false, "", LTApplication.instance.current_page, "", "packageError", "apk_deleted");//手动上报一条安装请求
                    DownloadTaskManager.getInstance().remove(mAppEntity);
                    startDownload("request", "apk_deleted");
                    // 这里延迟是针对配置差的手机，不然页面更新会有异常
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new ApkNotExistEvent());
                            ToastUtils.showToast(mAppEntity.getName() + " 的安装包不存在，正在为您重新下载");
                        }
                    }, 500);

                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showToast("安装包不存在,请删除任务后重新下载");
                }
                return;

            }
            //点击安装,每次点击后，1s内再点击无效。
            if (isoncl) {
                mAppEntity.setIsAppAutoUpgrade(false);
                InstallManager.getInstance().start(mAppEntity, mPageName, mID, false);
                isoncl = false;
                LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isoncl = true;
                    }
                }, 1000);
            }
        } else if (InstallState.install_failure == state) {
            if (InstallManager.getInstance().isAppInstalling(mAppEntity.getPackageName())) {
                return;
            }
            //重新 因内存不足，需要重新安装。
            if (StorageSpaceDetection.getAvailableSize() <= Long.valueOf(mAppEntity.getPackageSize())) {
                mAppEntity.setIsAppAutoUpgrade(false);
                InstallManager.getInstance().start(mAppEntity, mPageName, mID, false);
            } else {
                //移除数据
                try {
                    DownloadTaskManager.getInstance().remove(mAppEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                //重新下载
                startDownload(TYPE_RETRY, "");
            }
        }
    }


    /**
     * 下载或安装失败的情况下点击更新网络图标
     */
    private void updateNetIcon(AppEntity appEntity) {
        if (NetUtils.isMobileNet(mContext)) {
            appEntity.netType = Constant.NET_MOBILE_PHONE;
        } else if (NetUtils.isWifiNet(mContext)) {
            appEntity.netType = Constant.NET_WIFI;
        } else {
            appEntity.netType = Constant.NO_NET;
        }
    }

    private void goUpgrade() {
        startDownload(TYPE_UPGRADE, "");
        /* 以下代码用于判断是否满足所有应用自动升级弹窗的条件*/
        boolean hasAppAutoUpgradeDialog = PopWidowManageUtil.needPromptAutoUpgrade(mContext);
        if (hasAppAutoUpgradeDialog) {
            try {
                new PublicDialog(ActivityManager.self().topActivity(), new AppAutoUpgradeHolder()).showDialog(null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    private void startDownload(final String event, String event_reason) {
//        final String pageName = appIsByPush ? "notification" : mPageName;
        mAppEntity.setIsAppAutoUpgrade(false);
        mAppEntity.setIsOrderWifiDownload(false);
        DownloadTaskManager.getInstance().startAfterCheck(mContext, mAppEntity, appIsByPush ? "auto" : "manual", event, mPageName, mID, event_reason, "");
    }

    /***
     * 根据已有的下载数据从服务器重新请求最新数据
     *
     * @param appEntity
     */
    private void requestData(AppEntity appEntity, boolean apkNotExist) {
        if (null != appEntity) {
            if (appEntity.isAdData()) {
                startDownload(TYPE_RETRY, apkNotExist ? "apk_deleted" : "");
            } else {
                String id = String.valueOf(appEntity.getAppClientId());
                String type = appEntity.getApps_type();
                if (TextUtils.isEmpty(type)) return;
                if ("game".equals(type)) {
                    type = "games";
                } else if ("software".equals(type)) {
                    type = "softwares";
                }
                requestData(id, type, apkNotExist);
            }
        }
    }

    /***
     * 请求最新的下载地址
     *
     * @param mId
     * @param type
     */
    private void requestData(final String mId, final String type, final boolean apkNotExist) {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<PkgInfoBean>() {
            @Override
            public void onResponse(Call<PkgInfoBean> call, Response<PkgInfoBean> response) {
                PkgInfoBean appDetail = response.body();
                if (appDetail != null) {
                    try {
                        String newDwnUrl = GlobalConfig.combineDownloadUrl(appDetail.getDownload_url());
                        if (!mAppEntity.getDownloadUrl().equals(newDwnUrl)) {
                            /*删除旧文件，防止文件不一致导致安装失败*/
                            DownloadTaskManager.getInstance().removeFile(mAppEntity);
                            mAppEntity.setDownloadUrl(newDwnUrl);
                            mAppEntity.setPackage_md5(appDetail.getPackage_md5());//同时更新MD5值
                            LogUtils.i("重试下载地址/MD5值 = " + newDwnUrl + "/" + appDetail.getPackage_md5());
//                            DCStat.downloadRequestReport(mAppEntity, "manual", "request", mPageName, mID, apkNotExist ? "apk_deleted" : "download_url does not match", "");// 这里只为存库，为重试上报apk_deleted做准备 ATian
                            DownloadTaskManager.getInstance().startAfterCheck(mContext, mAppEntity, "manual", "retry", mPageName, mID, apkNotExist ? "apk_deleted" : "download_url does not match", "");
                            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // 发通知，让下载任务页面得到更新
                                    EventBus.getDefault().post(new DownloadUrlIsReturnEvent());
                                    ToastUtils.showToast("原下载地址失效，正在为您重新下载");
                                }
                            }, 200);

                        } else {
//                            DCStat.downloadRequestReport(mAppEntity, "manual", "request", mPageName, mID, apkNotExist ? "apk_deleted" : "", "");// 这里只为存库，为重试上报apk_deleted做准备 ATian
                            DownloadTaskManager.getInstance().startAfterCheck(mContext, mAppEntity, "manual", "retry", mPageName, mID, apkNotExist ? "apk_deleted" : "", "");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<PkgInfoBean> call, Throwable t) {
                LogUtils.i("zzz", "最新下载地址请求失败");

            }
        }).bulid().requestPkgInfo(type, mId);
    }


    /**
     * 设置app是推送过来的
     */
    public void setAppIsByPush(boolean isPush) {
        this.appIsByPush = isPush;
    }


    /**
     * must override method
     * 加载布局和初面相关元素
     */
    public abstract void assignViews();
    //===========================以下==界面显示部分===========================

    /**
     * must override method
     * 展示更新
     */
    public abstract void showUpgrade();

    /**
     * must override method
     * 展示 打开app
     */
    public abstract void showOpenApp();

    /**
     * must override method
     * 展示 下载
     */
    public abstract void showDownload();

    /**
     * must override method
     * 展示 继续
     */
    public abstract void showContinue(long progress, long total);

    /**
     * must override method
     * 展示 等待中
     */
    public abstract void showWaiting(long progress, long total);

    /**
     * must override method
     * 展示 下载中，进度条
     */
    public abstract void showProgress(long progress, long total);

    /**
     * must override method
     * 展示 重试
     */
    public abstract void showRetry();

    /**
     * must override method
     * 展示 安装
     */
    public abstract void showInstall();

    /**
     * must override method
     * 展示 安装中
     */
    public abstract void showInstalling();

}
