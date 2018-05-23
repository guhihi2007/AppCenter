package cn.lt.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.baidu.mobstat.StatService;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.yolanda.nohttp.NoHttp;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.lt.android.ads.AdService;
import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.Configure;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.NetWorkCore;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.service.CoreService;
import cn.lt.android.statistics.StatManger;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.BadgeUtil;
import cn.lt.android.util.CrashExceptionHandler;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.MetaDataUtil;
import cn.lt.android.util.PersistUtil;
import cn.lt.android.util.TimerExecutor;
import cn.lt.android.wake.WaKeLog;
import cn.lt.appstore.BuildConfig;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.PreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by wenchao on 2016/1/14.
 */
public class LTApplication extends Application {
    public static LTApplication instance;
    private String mProcessName;

    /**
     * 搜索关键词
     */
    public String word;

    public String from_id;

    private List<String> mLastPageIdList = new ArrayList<>();

    public List<String> getmLastPageIdList() {
        return mLastPageIdList;
    }

    public List<String> getmLastPageList() {
        return mLastPageList;
    }

    public String is_bibei = "";
    /**
     * 用户当前浏览的页面
     */
    public String current_page = "";
    /**
     * 打开系统安装界面时的请求码
     */
    public int requstCode_uuid;
    /**
     * 安装完成的轮询任务集
     */
    public SparseArray<AppEntity> normalInstallTaskLooper = new SparseArray<>();

    /**
     * 标记fragment销毁时的跳转记录
     */
    public Map<String, Integer> mJumpDestroyIndexMap = new HashMap<>();

    /**
     * 记录上次页面浏览
     */
    public List<String> mLastPageList = new ArrayList<>();
    /**
     * 标记是否来自MainActivity内的跳转
     */
    public boolean jumpIsFromMainActivity;

    private static Handler mMainThreadHandler;

    /**
     * 标记app是否已经启动起来（通知栏用）
     */
    public static boolean appIsStart;

    /**
     * 是否已经对 切换到移动网络 进行处理
     */
    public static boolean isMobileHandl = false;

    /**
     * 是否已经对 切换到wifi网络 进行处理
     */
    public static boolean isWifiHandle = false;

    /**
     * 无网情况下是否已经弹出过提示(用于防止重复弹出toast提示)
     */
    public static boolean noNetIsToast = false;

    /**
     * 记录是否已经退出app(非结束进程)
     */
    public static boolean appIsExit = false;

    /**
     * app白名单(用于广告过滤)
     */
    public static List<AppBriefBean> appWhiteList = new ArrayList<>();


    public static boolean isBackGroud = false;    //是否是用户Home键打开游戏中心


    /**
     * 用于安装时存放AppEntity, 防止数据上报前数据库里的AppEntity被InstalledEventLooper删除了，导致拿不到
     */
    public static Map<String, AppEntity> installStatAppList = new ConcurrentHashMap<>();


    //轮询检查已经安装完成的app,保存为了在广播那里判断
    public static ArrayList<String> loopInstalledList = new ArrayList<>();

    /**
     * 初始化友盟分享
     * 添加各平台参数
     */ {
        //微信    wx12342956d1cab4f9,a5ae111de7d9ea137e88a5e02c07c94d
        PlatformConfig.setWeixin("wxe8ba8ffb70a6d888", "e47ae75eff8cc6a4aa188923acfc4975");
        //新浪微博 appkey appsecret
        PlatformConfig.setSinaWeibo("1604697301", "1efe47a9dbd6e9c652b4cb011df95f0d", "http://sns.whalecloud.com");
        // QQ和Qzone appid appkey
        PlatformConfig.setQQZone("1105130473", "FlmdjSyrXb6SZBGv");
    }


    //用于debug模式
    private BuildTypeUtils.RefWatcherUtil refWatcher;
//    private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallback;//Support25.10后加入


    public static LTApplication shareApplication() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mProcessName = getProcessName(this, android.os.Process.myPid());
        instance = this;

        //初始化自动装
        AutoInstallerContext.getInstance().init(this);
        //初始化目录
        LTDirectoryManager.initManager();
        //初始化网络配置
        NetWorkCore.getInstance().initNetWorkConfig(this);
        //初始化数据库等等
        GlobalParams.init();
        //初始化下载
        DownloadTaskManager.getInstance().init(this);
        //初始化后台服务
        startService(new Intent(this, CoreService.class));
        //初始化日志
        Logger.init().methodCount(1).hideThreadInfo();
        initStatManger();//初始化统计
        // 初始化通知
        LTNotificationManager.getinstance().init(this);
        // 初始化友盟分享SDK
        UMShareAPI.get(this);
        initCrashExcepiton();
        NoHttp.initialize(this);//初始化NoHttp引擎 by atian
        AdService.getInstance().init(this);//初始化豌豆荚数据刷新服务 by atian
        VersionCheckManger.getInstance().init(this); //初始化版本更新检测 by atian
        ImageloaderUtil.getInstance().init(this); //初始化图片加载 by atian
        initBugly();//初始化腾讯Bugly
        //主线程的Handler
        mMainThreadHandler = new Handler();
        if (!TextUtils.isEmpty(mProcessName) && mProcessName.equals(this.getPackageName())) {
            getGDTID();//动态获取广点通广告位ID（打包用）
            // 设置是否要打开log日志
            StatService.setDebugOn(false);
            StatService.autoTrace(this, true); //开启百度无埋点统计
//            checkAdConfig();
        }
        // 初始化调试神器
        final boolean isDebug = BuildConfig.DEBUG;
        if (isDebug) {
            refWatcher = BuildTypeUtils.INSTANCE.installRefWatcher(this);
            BuildTypeUtils.INSTANCE.installStetho(this);
        }
        ScheduledExecutorService pool = ThreadPoolProxyFactory.getScheduledThreadPool();
        pool.scheduleWithFixedDelay(platUpdateTask, 5000, Constant.CHECK_RUIWEI_PREIOD, TimeUnit.MILLISECONDS);

        this.registerActivityLifecycleCallbacks(new LTActivityLifecycleCallbacks(this, isDebug, refWatcher));
    }

    /***
     * 开启定时检测广告配置
     */
//    private void checkAdConfig() {
//        TimerExecutor.interval(Constant.CHECK_RUIWEI_PREIOD, new TimerExecutor.IRxNext() {
//            @Override
//            public void doNext(long number) {
//                LogUtils.i("checkAdConfig", "doNext Num:" + number);
//                requestFYData();
//            }
//        });
//    }

    TimerTask platUpdateTask = new TimerTask() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(mProcessName) && mProcessName.equals(LTApplication.this.getPackageName())) {
                LogUtils.i("Erosion", "满足检测版本更新条件!");
                requestFYData();//检查飞扬开关状态
            }
        }
    };

    private void requestFYData() {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<ConfigureBean>() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ConfigureBean base = (ConfigureBean) response.body();
                    if (base != null) {
                        if (null != base.getRuiwei_ads()) {
                            if ("open".equals(base.getRuiwei_ads().getStatus())) {
                                com.locate.utils.Wgr.setSwitchOn(LTApplication.this, true);
                                com.locate.utils.Wgr.init(getApplicationContext(), Constant.RW_CHANEL, Constant.RW_APPID, Constant.RW_TOKEN);
                                LogUtils.i("Erosion", "芮薇初始化了");
                            } else {
                                com.locate.utils.Wgr.setSwitchOn(getApplicationContext(), false);
                                LogUtils.i("Erosion", "芮薇停止了");
                            }
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


    /***
     * 动态获取开屏广告ID
     */
    private void getGDTID() {
        String splash_id = MetaDataUtil.getMetaData("SplashPosID").replace("_", "").trim();
        Constant.SplashPosID = splash_id;
    }

    private void initBugly() {
        Context context = getApplicationContext();
        String packageName = context.getPackageName();
        String processName = AppUtils.getProcessName(android.os.Process.myPid());
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        CrashReport.initCrashReport(getApplicationContext(), "5e25e125c1", BuildConfig.DEBUG, strategy);
    }


    /***
     * 初始化内存泄露
     */
//    private void initLeakCanary() {
//        if (GlobalConfig.DEBUG) {
//            if (LeakCanary.isInAnalyzerProcess(this)) {
//                return;
//            }
//            LeakCanary.install(this); //内存泄露分析
//        }
//    }

    /**
     * 得到主线程的handler
     */
    public static Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }

    private void initCrashExcepiton() {
        CrashExceptionHandler.self().init(this);
    }

    private void initStatManger() {
        StatManger.self().init(this);
    }


    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    public String getProcessName() {
        return mProcessName;
    }
}
