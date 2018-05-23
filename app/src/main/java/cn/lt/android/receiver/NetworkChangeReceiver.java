package cn.lt.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.RemoteException;

import java.util.List;

import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.NetTypeEvent;
import cn.lt.android.statistics.StatFailureManager;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/3/16.
 * 网络状态改变
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "ChangePhoneNetReceiver";

    public NetworkChangeReceiver.ChangeListener onJumpListener;
    public Context context;
    private static int mCurrType;
    public static boolean NetworkChangedIsHandle;

    public NetworkChangeReceiver(Context context) {
        this.context = context;
    }

    public interface ChangeListener {
        void changeNetType(int type);
    }

    public void setOnJumpListener(NetworkChangeReceiver.ChangeListener onJumpListener) {
        this.onJumpListener = onJumpListener;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            /*由于好多手机切换网络时会连续下发多种状态，此方法会被调用多次，但是只有最后一种才是目标状态，
            * 所以这里做了控制，只处理一次，而且要等待5秒后才进行处理，以达到最终想要的效果*/
            if (NetworkChangedIsHandle) {
                NetworkChangedIsHandle = false;
                return;
            }
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCurrType = NetUtils.getNetType(context);
                    LogUtils.i(TAG, "mCurrType = " + mCurrType);
                    handleNetworkChanged(context, mCurrType);
                    NetworkChangedIsHandle = false;
                }
            }, 5000);

            NetworkChangedIsHandle = true;
        }
    }

    /**
     * 处理网络改变
     *
     * @param context
     * @param currType
     */
    private synchronized void handleNetworkChanged(Context context, int currType) {
        switch (currType) {
            case ConnectivityManager.TYPE_WIFI:
                LogUtils.i(TAG, "网络变成WIFI啦~~~");
                LTApplication.noNetIsToast = false;
                LTApplication.isMobileHandl = false;
                LogUtils.i(TAG, "isWifiHandle = " + LTApplication.isWifiHandle);
                EventBus.getDefault().post(new NetTypeEvent(ConnectivityManager.TYPE_WIFI));
                //自动开始所有的下载
                if (!LTApplication.isWifiHandle) {
                    handlerWifi(context);
                }

                // 设置5秒后复原，解决在手机没有sim卡时，来回切换wifi会没反应的问题
                LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LTApplication.isWifiHandle = false;
                    }
                }, 5000);

                break;
            case ConnectivityManager.TYPE_MOBILE:
                LogUtils.i(TAG, "网络变成 手机数据 啦~~~");
                EventBus.getDefault().post(new NetTypeEvent(ConnectivityManager.TYPE_MOBILE));
                if (!LTApplication.isMobileHandl) {
                    //发送事件监听
                    handleMobile(context);
                }

                LTApplication.noNetIsToast = false;
                LTApplication.isWifiHandle = false;
                break;
            default:
                LogUtils.i(TAG, "网络变成 无网络 啦~~~");
                EventBus.getDefault().post(new NetTypeEvent(ConnectivityManager.TYPE_BLUETOOTH));
                handleDefault(context);//无网络的时候不需要做处理 会自动暂停，如果手动启动下载会有提示无网络  modify by atian
                break;
        }
    }

    private void handleMobile(Context context) {
        // 标记已经处理过
        LTApplication.isMobileHandl = true;
        /****************上报之前统计上报失败的数据----如果数据库里面有就上报*****************/
        StatFailureManager.submitFailureData();
        try {
            final DownloadTaskManager downloadTaskInstance = DownloadTaskManager.getInstance();
            List<AppEntity> downloadingList = downloadTaskInstance.getUnAutoUpgradeDownloadTaskList();
            LogUtils.i("GOOD", "移动网暂停了");
            downloadTaskInstance.pauseAll("auto", "", "", "net_change", "");
//            downloadTaskInstance.pauseAll();
            // 如果app已经退出，不再作下载的处理
            if (LTApplication.appIsExit) {
                return;
            }

            /**有下载的项*/
            if (downloadingList.size() > 0) {
                //前台运行弹出对话框处理
                DownloadChecker.getInstance().changeToMoileNetworkPromp(ActivityManager.self().topActivity(), new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    downloadTaskInstance.startAll("manual", "net_change", "", false);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },

                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    downloadTaskInstance.startAll("normal", "net_change", "book_wifi", true);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }


    }

    private void handleDefault(Context context) {
        LTApplication.isMobileHandl = false;
        LTApplication.isWifiHandle = false;
        LogUtils.i(TAG, "无网络");
        if (!LTApplication.noNetIsToast) {
            ToastUtils.showToast(context.getResources().getString(R.string.download_no_network));
            LTApplication.noNetIsToast = true;
        }
    }


    private void handlerWifi(Context context) {
        LTApplication.isWifiHandle = true;
        /****************上报之前统计上报失败的数据----如果数据库里面有就上报*****************/
        StatFailureManager.submitFailureData();
        try {
            // 加WIFI判断，为了防止广播不正确时，错误启动任务
            if (NetUtils.isWifi(context)) {
                if (DownloadTaskManager.getInstance().getDownloadTaskList().size() > 0) {
                    DownloadTaskManager.getInstance().startAll("auto", "net_change", "", false);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO
            return;
        }
    }


}
