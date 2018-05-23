package cn.lt.android.plateform.update;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.yolanda.nohttp.Headers;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.download.DownloadListener;
import com.yolanda.nohttp.download.DownloadRequest;
import com.yolanda.nohttp.error.StorageReadWriteError;
import com.yolanda.nohttp.error.StorageSpaceNotEnoughError;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.plateform.update.manger.UpdatePathManger;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.CallServer;
import cn.lt.android.util.FileUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import update.IPlatUpdateCallback;
import update.IPlatUpdateService;

/**
 * 升级服务
 *
 * @author dxx
 */
public class PlatUpdateService extends Service {

    public static final String TAG = "UpdateService";
    public static final int HANDLER_WAHT_NETWORK_ERROR = 0;
    public static final int HANDLER_WAHT_SHOW_DIALOG = 1;
    public static final int HANDLER_WAHT_INSTALL_PROCESS_COMPLETED = 2;
    public static final int HANDLER_WAHT_STOP_SERVICE = 3;
    public static final int HANDLER_WAHT_START_DOWN = 4;
    public static final int HANDLER_WAHT_RETRY = 5;
    private MyHandler mHandler;
    private UpdateServiceController mServiceControlller;
    private Context mContext;
    private boolean isUserBehavior;
    private RemoteCallbackList<IPlatUpdateCallback> mCallBackList = new RemoteCallbackList<>();
    private static boolean isRetryUpgrade;

    public static void startUpdateService(String action, Context context) {
        Intent serverIntent = new Intent(context, PlatUpdateService.class);
        serverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!TextUtils.isEmpty(action)) {
            serverIntent.putExtra(PlatUpdateAction.ACTION, action);
        }
        context.startService(serverIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new MyHandler(this);
        initDownloadData();
    }

    public void initDownloadData() {
        mServiceControlller = new UpdateServiceController(mContext, mHandler);
    }

    @SuppressWarnings("unused")
    private void setForeground() {
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        this.startForeground(100, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mServiceControlller.dealWithIntentData(intent);
            isRetryUpgrade = intent.getBooleanExtra("retryUpgrade", false);
            if (intent.hasExtra("retryUpgrade")) {
                intent.removeExtra("retryUpgrade");
            }

            judgeIsPushAndReport(intent);
        }
        LogUtils.i(TAG, "手动触发的版本更新");
        isUserBehavior = true;
        mServiceControlller.checkVersion(true);
        return super.onStartCommand(intent, flags, startId);
    }

    private void judgeIsPushAndReport(Intent intent) {
        if (intent.hasExtra(NoticeConsts.isPush)) {
            String isPush = intent.getStringExtra(NoticeConsts.isPush);

            if (isPush != null && isPush.equals("yes")) {
                String pushId = intent.getStringExtra(Constant.EXTRA_PUSH_ID);
                DCStat.pushEvent(pushId, "platUpgrade", "clicked", "GETUI", "");

                intent.removeExtra(Constant.EXTRA_PUSH_ID);
            } else {
                DCStat.pushEvent("", "platUpgrade", "clicked", "APP", "");
            }

            intent.removeExtra(NoticeConsts.isPush);
        }
    }


    private IPlatUpdateService mBinder = new IPlatUpdateService.Stub() {
        @Override
        public void checkVersion() throws RemoteException {
            LogUtils.i(TAG, "触发服务检查版本更新");
            if (!mServiceControlller.isLoading()) {//如果手动下载已执行，则不在启动服务检查更新
                isUserBehavior = false;
                mServiceControlller.checkVersion(false);
            } else {
                LogUtils.i(TAG, "手动已经在下载，服务下载将不在执行");
            }
        }

        @Override
        public void requestNetWork() throws RemoteException {
            if (!isUserBehavior) {
                LogUtils.i("UpdateService", "记录最后一次版本请求时间");
                UpdateUtil.saveLastCheckTime(mContext);//记录最后一次服务检测版本更新服务时间
            }
            VersionCheckManger.VersionCheckCallback mVersionCheckCallBack = new VersionCheckManger.VersionCheckCallback() {
                @Override
                public void callback(Result result, VersionInfo info) {
                    switch (result) {
                        case have:
                            final int N = mCallBackList.beginBroadcast();
                            for (int i = 0; i < N; i++) {
                                try {
                                    mCallBackList.getBroadcastItem(i).callback(true);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            mCallBackList.finishBroadcast();
                            break;
                        case none:
                            LogUtils.i(TAG, "没有新版本，服务停止");
                            stopMe();
                            break;
                        case fail:
                            LogUtils.i(TAG, "检查版本升级失败，服务停止");
                            stopMe();
                            break;
                    }
                }
            };
            VersionCheckManger.getInstance().checkVerison(mVersionCheckCallBack, false);
        }

        @Override
        public void registerCallback(IPlatUpdateCallback callback) throws RemoteException {
            if (callback != null) {
                mCallBackList.register(callback);
            }
        }

        @Override
        public void removeCallback(IPlatUpdateCallback callback) throws RemoteException {
            if (callback != null) {
                mCallBackList.unregister(callback);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i(TAG, "UpdateService onBind");
        return mBinder.asBinder();
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "UpdateService onDestroy");
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        LogUtils.i(TAG, "UpdateService onRebind()");
        super.onRebind(intent);
    }

    private void stopMe() {
        LogUtils.i(TAG, "the service is stopped...");
        unregisterDownloadRequest();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        stopSelf();
    }

    /**
     * 下载监听
     */

    private DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onStart(int what, boolean isResume, long beforeLength, Headers headers, long allCount) {
            LogUtils.i(TAG, "下载开始");
            if (isUserBehavior) {
                LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast("开始下载...");// 只有用户手动触发升级才弹吐司
                    }
                }, 2000);
            }
        }

        @Override
        public void onDownloadError(int what, Exception exception) {
            LogUtils.i(TAG, "下载出错:" + exception.getMessage());
            mServiceControlller.setIsLoading(false);
            String source = isUserBehavior ? "page" : "service";
            if (exception instanceof StorageReadWriteError || exception instanceof StorageSpaceNotEnoughError) {
                ToastUtils.showToast("存储空间不足,升级失败！");
            }
            if (isUserBehavior) {
                LogUtils.i(TAG, "手动下载失败，发通知栏");
                LTNotificationManager.getinstance().sendPlatformUpgrageFailNotice();
            } else {
                LogUtils.i(TAG, "来自服务下载失败，不发通知栏");
            }
            FileUtil.delFile(new File(UpdatePathManger.getDownloadFilePath(getApplicationContext()))); //下载失败删除残留的文件
            if (isUserBehavior) {
                DCStat.platUpdateEvent("download_error", source, UpdateUtil.getPlatUpgradeType(), "", "downloadError", exception.getMessage());
            } else {
                DCStat.platUpdateEvent("download_error", source, UpdateUtil.getPlatUpgradeType(), "", "downloadError", exception.getMessage());
            }
            stopMe();
        }

        @Override
        public void onProgress(int what, int progress, long fileCount) {
            LogUtils.i(TAG, "当前下载进度==" + progress);
            VersionCheckManger.getInstance().setUpgrading(true); //标记正在升级任务正在进行中
            mServiceControlller.setIsLoading(true);
            if (isUserBehavior) {
                LTNotificationManager.getinstance().sendPlatformUpgrageProgressNotice(progress, 100);
                if (progress == 100) {
                    mServiceControlller.setIsNotified(true);
                }
            }
        }

        @Override
        public void onFinish(int what, String filePath) {
            LogUtils.i(TAG, "下载完成");
            UpdateUtil.saveTargetVersionCode(mContext, VersionCheckManger.getInstance().getmVersionInfo().getmUpgradeVersionCode());
            mServiceControlller.setIsLoading(false);
            VersionCheckManger.getInstance().setUpgrading(false); //标记正在升级任务不在进行
            if (isUserBehavior) {
                DCStat.platUpdateEvent("downloaded", "page", UpdateUtil.getPlatUpgradeType(), "", "", "");
            } else {
                DCStat.platUpdateEvent("downloaded", "service", UpdateUtil.getPlatUpgradeType(), "", "", "");
            }

            ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().execute(new Runnable() {
                @Override
                public void run() {
                    mServiceControlller.installApk(isUserBehavior);
                }
            });
        }

        @Override
        public void onCancel(int what) {
            LogUtils.i(TAG, "下载已取消");
        }

    };

    /***
     * 暂停下载
     */
    public void unregisterDownloadRequest() {
        // 暂停下载
        if (mDownloadRequest != null) {
            mDownloadRequest.cancel();
        }
    }


    /***
     * 如果使用断点续传的话，一定要指定文件名。
     *
     * @param url        下载地址。
     * @param fileName   文件名。
     * @param fileFolder 是否断点续传下载。
     */
    private DownloadRequest mDownloadRequest;

    public void addNewDownload(String url, String fileName, String fileFolder) {
        // 开始下载了，但是任务没有完成，代表正在下载，那么暂停下载。
        if (mDownloadRequest != null && mDownloadRequest.isStarted() && !mDownloadRequest.isFinished()) {
            // 暂停下载。
            mDownloadRequest.cancel();
        } else if (mDownloadRequest == null || mDownloadRequest.isFinished()) {// 没有开始或者下载完成了，就重新下载。
            mDownloadRequest = NoHttp.createDownloadRequest(url, fileFolder, fileName, false, true);
            CallServer.getDownloadInstance().add(0, mDownloadRequest, downloadListener);

        }
    }

    class MyHandler extends Handler {//连接UpdateServiceController

        WeakReference<PlatUpdateService> service;

        public MyHandler(PlatUpdateService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_WAHT_INSTALL_PROCESS_COMPLETED:
                    break;
                case HANDLER_WAHT_NETWORK_ERROR:
                    LTNotificationManager.getinstance().sendPlatformUpgrageFailNotice();
                    ToastUtils.showToast("网络异常，请检查网络设置！");
                    break;
                case HANDLER_WAHT_SHOW_DIALOG:
                    break;
                case HANDLER_WAHT_STOP_SERVICE:
                    service.get().stopMe();
                    break;
                case HANDLER_WAHT_START_DOWN:
                    if (isRetryUpgrade) {
                        DCStat.platUpdateEvent("retry", "push", UpdateUtil.getPlatUpgradeType(), "manual", "", "");
                        isRetryUpgrade = false;
                    }//真正去执行下载
                    addNewDownload((String) msg.obj, "update.apklll", UpdatePathManger.getDownloadPath(mContext));
                    break;
//                case HANDLER_WAHT_RETRY:
//                    ToastUtils.showToast("安装包损坏，正在为你重新下载！");
//                    addNewDownload((String) msg.obj, "update.apk", UpdatePathManger.getDownloadPath(mContext));
//                    break;
            }
        }
    }

}
