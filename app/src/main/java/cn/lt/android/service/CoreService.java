package cn.lt.android.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.igexin.sdk.PushManager;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.entity.Configure;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.notification.bean.PushBaseBean;
import cn.lt.android.plateform.update.PlatUpdateService;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.push.getui.GeTuiIntentService;
import cn.lt.android.push.getui.GeTuiService;
import cn.lt.android.receiver.ScreenBroadcastReceiver;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PersistUtil;
import cn.lt.android.wake.SilentManager;
import cn.lt.android.wake.WaKeLog;
import cn.lt.download.DownloadAgent;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import update.IPlatUpdateCallback;
import update.IPlatUpdateService;

/**
 * Created by wenchao on 2016/1/19.
 * 系统常驻service
 */
public class CoreService extends Service {

    private ScreenBroadcastReceiver mScreenBroadcastReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ServiceConnection mPlatUpdateServiceConntection;
    private IPlatUpdateService mPlatServiceStub;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(HandleService.TAG, "onCreate() ");
        mPlatUpdateServiceConntection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPlatServiceStub = IPlatUpdateService.Stub.asInterface(service);
                try {
                    mPlatServiceStub.registerCallback(updateCallback);
                    mPlatServiceStub.requestNetWork();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtils.i("zzz", "服务断开。。。");
                mPlatServiceStub = null;
            }
        };
        //下载服务绑定
        DownloadAgent.getImpl().bindService(this.getApplicationContext());
        initGeTuiPush();
        // 注册监听屏幕亮屏熄屏广播
        registerScreenReceiver(this);
        ScheduledExecutorService pool = ThreadPoolProxyFactory.getScheduledThreadPool();
        pool.scheduleWithFixedDelay(platUpdateTask, 5000, Constant.CHECKVERSIONPERIOD, TimeUnit.MILLISECONDS);

        SilentManager.getInstance().onCreate();
    }

    /***
     * 创建一个子线程做任务
     */
    TimerTask platUpdateTask = new TimerTask() {
        @Override
        public void run() {
            if (UpdateUtil.needCheckUpdate(CoreService.this)) {
                LogUtils.i(TAG, "满足检测版本更新条件!");
                bindUpdateService();
                requestFYData();//检查飞扬开关状态
            } else {
                LogUtils.i(TAG, "不满足检测版本更新条件!");
            }
        }
    };
    private IPlatUpdateCallback updateCallback = new IPlatUpdateCallback.Stub() {
        @Override
        public void callback(boolean hasUpdate) throws RemoteException {
            if (hasUpdate) {
                LogUtils.i(TAG, "callback有版本更新，启动升级");
                mPlatServiceStub.checkVersion();
            } else {
                unbindService(mPlatUpdateServiceConntection);
                LogUtils.i(TAG, "callback无版本更新，不启动升级");
            }
        }
    };

    /**
     * 初始化 个推推送
     */
    private void initGeTuiPush() {
        PushManager.getInstance().initialize(this.getApplicationContext(), GeTuiService.class);
        // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), GeTuiIntentService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(LogTAG.PushTAG, "CoreService被启动了~~");
        if (intent != null) {
            handlePush(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 处理推送消息
    private void handlePush(Intent intent) {
        boolean isPush = intent.getBooleanExtra(NoticeConsts.isPush, false);

        // 是否推送启动service
        if (isPush) {
            String pushId = intent.getStringExtra(Constant.EXTRA_ID);
            requestPushData(pushId);
        }
    }

    // 请求推送数据
    private void requestPushData(final String pushId) {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<PushBaseBean>() {
            @Override
            public void onResponse(Call<PushBaseBean> call, Response<PushBaseBean> response) {
                PushBaseBean bean = response.body();
                if (bean != null) {
                    bean.pushId = pushId;
                    LTNotificationManager.getinstance().sendPushNotic(bean);
                } else {
                    LogUtils.i(LogTAG.PushTAG, "个推请求回来的bean 是空的！");
                }
            }

            @Override
            public void onFailure(Call<PushBaseBean> call, Throwable t) {
                LogUtils.i(LogTAG.PushTAG, "请求数据失败~ ： " + t.getMessage());
            }
        }).bulid().requestPush(pushId);
    }

    /***
     * 请求飞扬弹窗配置信息
     */
    private void requestFYData() {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<ConfigureBean>() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ConfigureBean base = (ConfigureBean) response.body();
                    if (base != null) {
                        if (null != base.getApp_pulllive()) {
                            WaKeLog.i("服务获取后台拉活配置开关状态:" + base.getApp_pulllive().getStatus());
                            Configure configure = new Configure(base.getApp_pulllive().getStatus());
                            PersistUtil.persistData(configure, Constant.PULLLIVE_STATUS);
                        }
                    } else {
                        Log.i("ConfigUtil", "FY return null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Log.i("ConfigUtil", "FY request failed");
            }
        }).bulid().requestPopup();
    }

    public static final String TAG = "UpdateService";

    private void bindUpdateService() {
        Intent serverIntent = new Intent(this, PlatUpdateService.class);
        serverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (mPlatServiceStub == null) {
            LogUtils.i(TAG, "bindService()");
            this.bindService(serverIntent, mPlatUpdateServiceConntection, Context.BIND_AUTO_CREATE);
        } else {
            try {
                LogUtils.i(TAG, "requestNetWork()");
                mPlatServiceStub.requestNetWork();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(HandleService.TAG, "onDestroy() ");
        if (mPlatServiceStub != null && mPlatServiceStub.asBinder().isBinderAlive()) {
            try {
                mPlatServiceStub.removeCallback(updateCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        SilentManager.getInstance().onDestroy();
    }


    public static Intent getPushIntent(Context mContext, String pushId) {
        Intent intent = new Intent(mContext, CoreService.class);
        intent.putExtra(Constant.EXTRA_ID, pushId);
        intent.putExtra(NoticeConsts.isPush, true);
        return intent;
    }

    private void registerScreenReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
        context.registerReceiver(mScreenBroadcastReceiver, filter);
    }
}
