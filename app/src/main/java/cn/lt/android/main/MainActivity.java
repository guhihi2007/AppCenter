package cn.lt.android.main;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import com.igexin.sdk.PushManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.wanka.WanKa;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.UserEntity;
import cn.lt.android.db.WakeTaskEntity;
import cn.lt.android.db.WakeTaskEntityDao;
import cn.lt.android.deeplink.DeeplinkMgr;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.ClickTypeDataBean;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.event.RootIsCheckedEvent;
import cn.lt.android.event.ShowFloatAdEvent;
import cn.lt.android.event.TabClickEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.install.root.ShellUtils;
import cn.lt.android.main.entrance.TouchManger;
import cn.lt.android.main.game.GamePortalFragment;
import cn.lt.android.main.personalcenter.MinePortalFragment;
import cn.lt.android.main.personalcenter.NotifyUserInfoToGameCenterMgr;
import cn.lt.android.main.personalcenter.SettingActivity;
import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.main.rank.RankPortalFragment;
import cn.lt.android.main.recommend.RecommendPortalFragment;
import cn.lt.android.main.requisite.floatads.FloatAdsMgr;
import cn.lt.android.main.requisite.manger.RequisiteManger;
import cn.lt.android.main.software.SoftwarePortalFragment;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.push.getui.GeTuiIntentService;
import cn.lt.android.push.getui.GeTuiService;
import cn.lt.android.receiver.NetworkChangeReceiver;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppSignConflictUtil;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.BadgeUtil;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.util.ViewUtils;
import cn.lt.android.wake.SilentManager;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.LTFragmentTabHost;
import cn.lt.android.widget.ScrollRelativeLayout;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.ExitInstallDialog;
import cn.lt.android.widget.dialog.ExitWarnDialog;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LogoutDialogHolder;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.NetWorkUtils;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cn.lt.android.main.MainActivity.EventBean.tag;


public class MainActivity extends BaseAppCompatActivity implements TabHost.OnTabChangeListener, VersionCheckManger.VersionCheckCallback {
    public static final String KEY_PAGE_PORTAL_RECOMMEND = "portal_recommend";
    public static final String KEY_PAGE_PORTAL_SOLF = "portal_solf";
    public static final String KEY_PAGE_PORTAL_GAME = "portal_game";
    public static final String KEY_PAGE_PORTAL_RANK = "portal_rank";
    public static final String KEY_PAGE_PORTAL_MINE = "portal_mine";

    public static final String INTENT_JUMP_KEY_MAIN_TAB = "mianTab";
    public static final String INTENT_JUMP_KEY_SUB_TAB = "subTab";
    public static final int PAGE_TAB_RECOMMEND = 0;
    public static final int PAGE_TAB_SOFT = 1;
    public static final int PAGE_TAB_GAME = 2;
    public static final int PAGE_TAB_RANK = 3;
    public static final int PAGE_TAB_MINE = 4;
    public static final int PAGE_TAB_SOFT_SUB_INDEX = 0;
    public static final int PAGE_TAB_SOFT_SUB_RANK = 1;
    public static final int PAGE_TAB_SOFT_SUB_CAT = 2;
    public static final int PAGE_TAB_GAME_SUB_INDEX = 0;
    public static final int PAGE_TAB_GAME_SUB_RANK = 1;
    public static final int PAGE_TAB_GAME_SUB_CAT = 2;
    public static final int PAGE_TAB_RANK_SUB_GAME = 1;
    public static final int PAGE_TAB_RANK_SUB_SOFT = 0;
    private LTFragmentTabHost mFragmentTabHost;
    private TouchManger mAnimationManger;
    private ScrollRelativeLayout mScrollView;
    public ActionBar mHeadView;
    //我的tab小红点标志
    private TextView mRedPoint;
    //    private int mAppUpdateCount = 0;
    //是否有新的版本
    private boolean mHavePlat = false;
    public boolean imageJumpFormLoading;
    private NetworkChangeReceiver myNetReceiver;

    //应用升级数量(4.3.3添加,底部tab需要时时跟随应用升级和任务管理数量加起来同步)
    private int taskCount;
    private int appUpgradeCount;
    private UpgradeListManager.CountCallback countCallback;

    @Override
    public void setPageAlias() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.i("UpdateService", GlobalConfig.versionName + "的代码");
        checkRootPermission();
        setStatusBar();
        VersionCheckManger.getInstance().checkVerison(this, true);
        resolveStartIntent();
        initialize();
        getIntentData();
        requestSearchAdsData();//请求搜索广告词
        try {
            UpgradeListManager.getInstance().requestUpgradeListFromServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().register(this);
        // 如果不是小米系统，初始化个推推送
        initGeTuiPush();
        judgeNoticeStartType();
        LogUtils.i("asuiu312", "MainActivity：judgeNoticeStartType执行了");
        LTApplication.appIsExit = false;


        if (NetWorkUtils.isWifi(this)) {
            try {
                DownloadTaskManager.getInstance().startAll("auto", "app_start", "onekey", false);
                NetworkChangeReceiver.NetworkChangedIsHandle = true;//设置true后,NetworkChangeReceiver_OnReceive不会再次startAll;
            } catch (RemoteException e) {
                e.printStackTrace();
                return;
            }
        }
        wakeUpOtherApps();
        registerNetWorkReciver();
        //获取底部菜单小红点数量
        refreshTaskManagerRedCount();
        initAppManagerRedPoint();
        initUserAccount();

//        WakeTaskEntity entity = new WakeTaskEntity();
//        entity.setId(55L);//55L
//        entity.setExecuteTimes(900);
//        GlobalParams.getWakeTaskDao().insert(entity);
//        WakeTaskEntity entity2 = new WakeTaskEntity();
//        entity2.setId(66L);//55L
//        entity2.setExecuteTimes(900);
//        GlobalParams.getWakeTaskDao().insert(entity2);

//        WakeTaskEntity entity2 = new WakeTaskEntity();
//entity2.setId(55L);
//        try {
//            GlobalParams.getWakeTaskDao().delete(entity2);
//        } catch (Exception e) {
//            Log.d(LogTAG.USER, "delete"+e.getMessage());
//            e.printStackTrace();
//        }

    }

    private void initUserAccount() {
        try {
            List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
            if (list == null || list.size() == 0) { //
                Log.d(LogTAG.USER, "应用市场 MainActivity：查到自己无数据:");
                List<UserBaseInfo> userBaseInfosGameLists = NotifyUserInfoToGameCenterMgr.quaryFromGameCenter(this);
                if (userBaseInfosGameLists != null && userBaseInfosGameLists.size() == 1) {
                    Log.d(LogTAG.USER, "应用市场 MainActivity：查到游戏中心有 Token:" + userBaseInfosGameLists.get(userBaseInfosGameLists.size() - 1).getToken());
//                    UserInfoManager.instance().loginSuccess(userBaseInfosGameLists.get(userBaseInfosGameLists.size() - 1), true);
                    NetWorkClient.setToken(UserInfoManager.instance().getUserInfo().getToken());
                    getUserInfoByToken();
                }
            } else {
                getUserInfoByToken();
            }

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(LogTAG.USER, "应用市场 MainActivity：游戏中心查数据 initUserAccount 抛异常:" + e.getMessage());
        }
    }

    private byte errorCount = 0;

    /**
     * token登录请求
     */
    private void getUserInfoByToken() {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                errorCount = 0;
                UserBaseInfo userBaseInfo = response.body();
                LogUtils.d(LogTAG.USER, "应用市场 MainActivity:token登录成功：" + userBaseInfo);
                if (null != userBaseInfo) {
                    userBaseInfo.setToken(UserInfoManager.instance().getUserInfo().getToken());//token请求没有返回token
                    UserInfoManager.instance().loginSuccess(userBaseInfo, true);
                    DCStat.baiduStat(LTApplication.instance, "login_success", "登录成功：" + userBaseInfo.getId()); //统计登录成功
                } else {
                    ToastUtils.showToast("信息异常！");
                }
            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                LogUtils.d(LogTAG.USER, "应用市场 MainActivity：：token登路失败：" + t.getMessage());
                if (errorCount++ > 3) {
                    UserInfoManager.instance().userLogout(true);
                    return;
                }
                // Token 失效重新去游戏中心获取
                List<UserBaseInfo> userBaseInfosGameLists = NotifyUserInfoToGameCenterMgr.quaryFromGameCenter(LTApplication.instance);
                if (userBaseInfosGameLists != null && userBaseInfosGameLists.size() == 1) {
                    Log.d(LogTAG.USER, "应用市场 MainActivity：查到游戏中心有 Token:" + userBaseInfosGameLists.get(userBaseInfosGameLists.size() - 1).getToken());
                    UserInfoManager.instance().loginSuccess(userBaseInfosGameLists.get(userBaseInfosGameLists.size() - 1), true);
                    getUserInfoByToken();
                }
            }
        }).bulid().requestUserInfoForToken();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        UpgradeListManager.getInstance().unregisterCountCallback(countCallback);
        if (myNetReceiver != null) {
            Log.d("8887", "网络广播销毁了~~");
            unregisterReceiver(myNetReceiver);
        }
    }

    //为了 MainActivity 还没创建时的跳转方式-------针对于首页跳转到4个tab页的fragment；
    private void getIntentData() {
        if (getIntent() != null) {
            ClickTypeDataBean clickTypeDataBean = (ClickTypeDataBean) getIntent().getSerializableExtra("ClickTypeDataBean");
            if (clickTypeDataBean != null) {
                String mType = clickTypeDataBean.getPage();
                if (mType != null) {
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    switch (mType) {
                /* *********************软件*********************/
                        case Constant.PAGE_APP_CHOICE:
                        case Constant.PAGE_APP_LIST:
                        case Constant.PAGE_APP_CLASSIFY:
                            SoftwarePortalFragment.jumpTAG = mType;
                            mFragmentTabHost.setCurrentTab(PAGE_TAB_SOFT);
                            getIntent().removeExtra("ClickTypeDataBean");
                            break;

                /* *********************游戏*********************/
                        case Constant.PAGE_GAME_CHOICE:
                        case Constant.PAGE_GAME_LIST:
                        case Constant.PAGE_GAME_CLASSIFY:
                            GamePortalFragment.JumpTAG = mType;
                            mFragmentTabHost.setCurrentTab(PAGE_TAB_GAME);
                            getIntent().removeExtra("ClickTypeDataBean");
                            break;

                 /* *********************榜单*********************/
                        case Constant.PAGE_LIST:
                            mFragmentTabHost.setCurrentTab(PAGE_TAB_RANK);
                            getIntent().removeExtra("ClickTypeDataBean");
                            break;

                    }
                }
            }
        }
    }

    private void resolveStartIntent() { //从loading页跳转过来；应用正常启动时走；
        Intent intent = getIntent();

        // deeplink优先级最高
        boolean isFromDeeplink = intent.getBooleanExtra(Constant.EXTRA_IS_FROM_DEEPLINK, false);
        if (isFromDeeplink) {
            String deeplinkDataStr = intent.getStringExtra(Constant.EXTRA_DEEPLINK_DATA_STR);
            LogUtils.i(LogTAG.deepLinkTAG, "执行Deeplink操作， dataStr = " + deeplinkDataStr);
            DeeplinkMgr.INSTANCE.handleDeeplinkAction(this, deeplinkDataStr);
            return;
        }

        imageJumpFormLoading = intent.getBooleanExtra(LoadingActivity.INTENT_JUMP_KEY, false);
        if (imageJumpFormLoading) {
            LoadingImgWorker.getInstance().toJump(this);
            LogUtils.i("zzz", "从loading页点击跳转过来");
        }
    }

    private void resolveInternalIntent() { //应用内跳转；
        Intent intent = getIntent();
        int mainTab = intent.getIntExtra(INTENT_JUMP_KEY_MAIN_TAB, PAGE_TAB_RECOMMEND);
        intent.removeExtra(INTENT_JUMP_KEY_MAIN_TAB);
        mFragmentTabHost.setCurrentTab(mainTab);
    }

    /**
     * 初始化 个推推送（启动应用只会调用一次）
     */
    private void initGeTuiPush() {
        PushManager.getInstance().initialize(this.getApplicationContext(), GeTuiService.class);

        // com.getui.demo.DemoIntentService 为第三方自定义的推送服务事件接收类
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), GeTuiIntentService.class);
    }

    /**
     * 4.3.3更改
     *
     * @param
     */
    private void refreshTabMineRedPoint(int appUpdateAndTaskCount) {
        if (appUpdateAndTaskCount > 0) {
            mRedPoint.setText(String.valueOf(appUpdateAndTaskCount));
            mRedPoint.setVisibility(View.VISIBLE);
            mRedPoint.setBackgroundResource(appUpdateAndTaskCount > 9 ? R.drawable.shape_red_rectangle : R.drawable.shape_red_circle);
        } else if (mHavePlat) {
            mRedPoint.setText("");
            mRedPoint.setVisibility(View.VISIBLE);
            mRedPoint.setBackgroundResource(R.mipmap.tab_me_new);
        } else {
            mRedPoint.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        int currentTab = mFragmentTabHost.getCurrentTab();
        String tag = "";
        switch (currentTab) {
            case PAGE_TAB_RECOMMEND:
                tag = KEY_PAGE_PORTAL_RECOMMEND;
                break;
            case PAGE_TAB_SOFT:
                tag = KEY_PAGE_PORTAL_SOLF;
                break;
            case PAGE_TAB_GAME:
                tag = KEY_PAGE_PORTAL_GAME;
                break;
            case PAGE_TAB_RANK:
                tag = KEY_PAGE_PORTAL_RANK;
                break;
            case PAGE_TAB_MINE:
                tag = KEY_PAGE_PORTAL_MINE;
                break;

        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null && fragment.isAdded()) {
            fragment.onHiddenChanged(false);
            LogUtils.e("mmm", "find Fragment: " + fragment.getClass().getCanonicalName());
        }
    }

    public MinePortalFragment getMineFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(KEY_PAGE_PORTAL_MINE);
        if (fragment != null) {
            return (MinePortalFragment) fragment;
        }
        return null;
    }

    private void initialize() {
        mHeadView = (ActionBar) findViewById(R.id.root_action_bar);
        mScrollView = (ScrollRelativeLayout) findViewById(R.id.root_srcollView);
        mFragmentTabHost = (LTFragmentTabHost) findViewById(android.R.id.tabhost);
        mHeadView.setPadding(0, 0, 0, -DensityUtil.dip2px(this, 1));  //解决白色线条bug。
        mHeadView.setBackgroundColor(getResources().getColor(R.color.tool_bar_color));
        mFragmentTabHost.setup(this, getSupportFragmentManager(), R.id.main_fragment_content);

        View recommendTab = LayoutInflater.from(this).inflate(R.layout.tab_main_recommend, null);
        View softwareTab = LayoutInflater.from(this).inflate(R.layout.tab_main_software, null);
        View gameTab = LayoutInflater.from(this).inflate(R.layout.tab_main_game, null);
        View rankTab = LayoutInflater.from(this).inflate(R.layout.tab_main_rank, null);
        View mineTab = LayoutInflater.from(this).inflate(R.layout.tab_main_mine, null);

        mRedPoint = (TextView) mineTab.findViewById(R.id.iv_point);

        TabHost.TabSpec tabSpec1 = mFragmentTabHost.newTabSpec(KEY_PAGE_PORTAL_RECOMMEND).setIndicator(recommendTab);
        tabSpec1.getTag();
        mFragmentTabHost.addTab(tabSpec1, RecommendPortalFragment.class, null);
        TabHost.TabSpec tabSpec2 = mFragmentTabHost.newTabSpec(KEY_PAGE_PORTAL_SOLF).setIndicator(softwareTab);
        mFragmentTabHost.addTab(tabSpec2, SoftwarePortalFragment.class, null);
        TabHost.TabSpec tabSpec3 = mFragmentTabHost.newTabSpec(KEY_PAGE_PORTAL_GAME).setIndicator(gameTab);
        mFragmentTabHost.addTab(tabSpec3, GamePortalFragment.class, null);
        TabHost.TabSpec tabSpec4 = mFragmentTabHost.newTabSpec(KEY_PAGE_PORTAL_RANK).setIndicator(rankTab);
        mFragmentTabHost.addTab(tabSpec4, RankPortalFragment.class, null);
        TabHost.TabSpec tabSpec5 = mFragmentTabHost.newTabSpec(KEY_PAGE_PORTAL_MINE).setIndicator(mineTab);
        mFragmentTabHost.addTab(tabSpec5, MinePortalFragment.class, null);

        mFragmentTabHost.setOnTabChangedListener(this);
        mFragmentTabHost.getTabWidget().setDividerDrawable(null);

        setTabClickToTopAndRefresh(recommendTab, KEY_PAGE_PORTAL_RECOMMEND, PAGE_TAB_RECOMMEND);
        setTabClickToTopAndRefresh(softwareTab, KEY_PAGE_PORTAL_SOLF, PAGE_TAB_SOFT);
        setTabClickToTopAndRefresh(gameTab, KEY_PAGE_PORTAL_GAME, PAGE_TAB_GAME);
        setTabClickToTopAndRefresh(rankTab, KEY_PAGE_PORTAL_RANK, PAGE_TAB_RANK);
        setTabClickToTopAndRefresh(mineTab, KEY_PAGE_PORTAL_MINE, PAGE_TAB_MINE);
    }

    /**
     * 双击回顶部、刷新
     *
     * @param tabItem
     * @param tabName
     */
    private void setTabClickToTopAndRefresh(View tabItem, final String tabName, final int tabIndex) {
        tabItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i("jkl", "点击了：" + tabName + " index:" + mFragmentTabHost.getCurrentTab());
                if (mFragmentTabHost.getCurrentTab() == tabIndex) {
                    goTop(tabName);
                } else {
                    mFragmentTabHost.setCurrentTab(tabIndex);
                }
            }
        });
    }

    /**
     * 执行双击的逻辑
     *
     * @param tabName
     * @des 必须在500毫秒内才有效果
     */
    private void goTop(String tabName) {
        if (ViewUtils.isFastClick()) {
            EventBus.getDefault().post(new TabClickEvent(tabName));
        }
    }


    /**
     * 用于判断通过点击通知栏后跳转到对应界面
     */
    private void judgeNoticeStartType() {
        Intent intent = getIntent();
        if (intent != null) {
            judgeGoDownloadTab(intent);
            judgePush(intent);
        }
    }

    /**
     * 判断跳转到应用下载tab的操作
     */
    private void judgeGoDownloadTab(Intent intent) {
        int type = intent.getIntExtra(NoticeConsts.noticeStartType, 0);
        switch (type) {
            case NoticeConsts.goDownloadTab:// 应用下载Tab
                UIController.goDownloadTask(MainActivity.this);
                break;
            case NoticeConsts.goDownloadTabByUpgrade:// 点击升级通知跳到 应用下载Tab
                if (NetWorkUtils.isWifi(MainActivity.this)) {
                    UIController.goDownloadTask(MainActivity.this, NoticeConsts.jumpByUpgrade);
                } else {
                    UIController.goUpdateActivity(MainActivity.this);
                }
                break;
            case NoticeConsts.goDownloadTabByMultiDownloadFault:// 点击升级通知跳到 应用下载Tab
                UIController.goDownloadTask(MainActivity.this, NoticeConsts.jumpByMultiDownloadFault);
                break;
            default:
                break;
        }
    }


    /**
     * 判断点击 推送通知 跳转页面的操作
     */
    private void judgePush(Intent intent) {
        Bundle pushBundle = intent.getBundleExtra(NoticeConsts.pushBundle);
        if (pushBundle != null) {
            String pushId = pushBundle.getString(Constant.EXTRA_PUSH_ID);
            boolean isPush = pushBundle.getBoolean(NoticeConsts.isPush, false);
            if (isPush) {// 是点击推送通知
                boolean isPushAPP = pushBundle.getBoolean(NoticeConsts.isPushAPP, false);// 是否APP推送
                boolean isPushTopic = pushBundle.getBoolean(NoticeConsts.isPushTopic, false);// 是否专题推送
                boolean isPushH5 = pushBundle.getBoolean(NoticeConsts.isPushH5, false);// 是否H5推送
                String reportData = pushBundle.getString(WanKa.KEY_REPORT_DATA);    // 推送的玩咖长尾有这个数据

                // 跳转到应用详情
                if (isPushAPP) {
                    String appId = pushBundle.getString(Constant.EXTRA_ID);
                    String type = pushBundle.getString(Constant.EXTRA_TYPE);
                    UIController.goAppDetailByPush(this, appId, type, pushId, reportData);
                    DCStat.pushEvent(pushId, "appDetail", "clicked", "GETUI", "");
                }

                // 跳转到专题详情
                if (isPushTopic) {
                    String topicId = pushBundle.getString(Constant.EXTRA_ID);
                    String title = pushBundle.getString(Constant.EXTRA_TITLE);
                    UIController.goSpecialDetail(this, topicId, title, "notification", true, false);
                    DCStat.pushEvent(pushId, "TopicDetail", "clicked", "GETUI", "");
                }

                // 跳转到h5
                if (isPushH5) {
                    String title = pushBundle.getString(Constant.EXTRA_TITLE);
                    String h5Url = pushBundle.getString(Constant.EXTRA_URL);
                    UIController.goWebView(this, title, h5Url, true);
                    DCStat.pushEvent(pushId, "H5", "clicked", "GETUI", "");
                }
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveStartIntent();
        judgeNoticeStartType();
        resolveInternalIntent();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mFragmentTabHost.getCurrentTab() == PAGE_TAB_RECOMMEND) {
            return super.dispatchTouchEvent(ev);
        }
        if (mAnimationManger != null && mAnimationManger.onEvent(ev)) {

            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }


    /***
     * 初始化动画引擎
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (mAnimationManger == null) {
                mAnimationManger = new TouchManger(this, mScrollView, mHeadView).init();
            }
        }
    }

    protected HotSearchBean mCurrAds;
    protected int i = 1;

    @Override
    public void onTabChanged(String tabId) {
        //选中为推荐页的话，就动态恢复滚动的位置
        if (mAnimationManger != null) {
            if (mFragmentTabHost.getCurrentTab() == PAGE_TAB_RECOMMEND) {
                mAnimationManger.setCanScroll(true);
                mAnimationManger.startDance(0, 5, true);

            } else if (mFragmentTabHost.getCurrentTab() == PAGE_TAB_MINE) {
                mAnimationManger.startDance(-mHeadView.getMeasuredHeight(), 5, true);//复位
                mAnimationManger.setCanScroll(false);//设置动画不执行
            } else {
                mAnimationManger.setCanScroll(true);
                mAnimationManger.startDance(mAnimationManger.getTempMoveOffset(), 5, true);
            }
        }
        setAutoBoard();

    }

    /***
     * 设置搜索广告语切换
     */
    private void setAutoBoard() {
        if (adsList.size() > 0) {
            if (i >= adsList.size()) {
                i = 0;
            }
            mCurrAds = adsList.get(i++);
            mHeadView.setAutoBoard(mCurrAds.getTitle(), mCurrAds.getId() + "");
        } else {
            mHeadView.setAutoBoard("请输入关键字  ", "");
        }
    }

    /**
     * 4.3.3注释掉(honaf)
     *
     * @param result
     */
    /*private void setmAppUpdateCount(final Result result) {
        //显示我tab中的小红点逻辑(如果有版本升级或者应用升级就显示)
        if (UpgradeListManager.getInstance().isInit()) {
            mAppUpdateCount = UpgradeListManager.getInstance().getUpgradeAppList().size();
            refreshRedPoint(result);
        } else {
            UpgradeListManager.getInstance().registerCallback(new UpgradeListManager.Callback() {
                @Override
                public void onResponse(List<AppDetailBean> upgradeList) {
                    UpgradeListManager.getInstance().unregisterCallback(this);
                    mAppUpdateCount = upgradeList.size();
                    refreshRedPoint(result);
                }
            });
        }
    }*/
    @Override
    public void callback(final Result result, VersionInfo info) {


        switch (result) {
            case have:
                mHavePlat = true;
//                refreshRedPoint(Result.have);
                // 判断是否需要立即弹框；
                if (info.isForce()) {
                    VersionCheckManger.getInstance().showUpdateDialog(this, true);
                } else {
                    if (PopWidowManageUtil.needShowClientUpdateDialog(this)) {
                        VersionCheckManger.getInstance().showUpdateDialog(this, true);
                    } else {
                        new RequisiteManger(this).requestData(true);
                    }
                }

                refreshTabMineRedNum();
                break;
            case none:
            case fail:
                new RequisiteManger(this).requestData(true);
                break;
            default:

                break;
        }
    }

    public void onEventMainThread(ShowFloatAdEvent event) {
        if (PopWidowManageUtil.needShowFloatAdDialog(this) // 浮层广告弹窗条件
                && mFragmentTabHost.getCurrentTab() == PAGE_TAB_RECOMMEND // 当前页必须是推荐页
                && AppUtils.hasSIMCard(this)) {// 有sim卡

            FloatAdsMgr.INSTANCE.requestFloatAdsData(this);
        } else {
            AppSignConflictUtil.showSignConflictDialogs(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExitDialog() {
        int downloadingCount;
        try {
            downloadingCount = DownloadTaskManager.getInstance().getDownloadingList().size();
        } catch (RemoteException e) {
            e.printStackTrace();
            return;// TODO:
        }
        if (downloadingCount > 0) {
            // 如果有任务正在下载，弹出是否继续下载的提示弹窗
            new ExitWarnDialog(this, downloadingCount).show();
        } else {
            if (AppUtils.getAvailablMemorySize() <= 0) {
                new PublicDialog(this, new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.quit));
            } else {
                List<AppEntity> appEntities = DownloadTaskManager.getInstance().getInstallTaskList();
                List<AppEntity> installList = new ArrayList<>();
                LogUtils.i("Erosion", "安装列表===" + appEntities.size());
                if (appEntities.size() > 0) {
                    PackageManager pm = this.getPackageManager();
                    for (AppEntity appEntity : appEntities) {
                        LogUtils.i("Erosion","state===" + appEntity.getStatus() + ",name===" + appEntity.getName());
                        if (appEntity.getStatus() == InstallState.install_failure || appEntity.getErrorType() == DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                            continue;
                        }

                        // 包不存在
                        if (!new File(appEntity.getSavePath()).exists()) {
                            continue;
                        }

                        // TODO 解析软件包时出现问题
                        try {
                            PackageInfo info = pm.getPackageArchiveInfo(appEntity.getSavePath(),0);
                            if (info == null) {
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }

                        installList.add(appEntity);
                    }
                    if (installList.size() == 0) {
                        new PublicDialog(this, new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.quit));
                    } else {
                        Resources resources = MainActivity.this.getResources();
                        String message;
                        String appName = TextUtils.isEmpty(installList.get(0).getAlias()) ? installList.get(0).getName() : installList.get(0).getAlias();
                        if (installList.size() == 1) {
                            message = String.format(resources.getString(R.string.exit_with_uninstalled), appName);
                        } else {
                            message = String.format(resources.getString(R.string.exit_with_multi_uninstalled), appName, installList.size());
                        }

                        ExitInstallDialog exitInstallDialog = new ExitInstallDialog(MainActivity.this, message, installList);
                        exitInstallDialog.show();
                    }
                } else {
                    new PublicDialog(this, new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.quit));
                }
            }

        }

    }

    /***
     * 获取任务列表和可升级列表总长度
     * 备注：放在MainActivity是为了保证能拿到可升级列表
     *
     * @return
     */
    private int getAllTaskCount() {
        int downloadCount = DownloadTaskManager.getInstance().getAll().size();
        int upgradeCount = UpgradeListManager.getInstance().getUpgradeAppList().size();
        return downloadCount + upgradeCount;
    }

    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    private void addGameShortcut() {
        Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
        addShortcutIntent.putExtra("duplicate", false);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "游戏中心");
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_game_launcher));
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(MainActivity.this, MainActivity.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launcherIntent.putExtra(MainActivity.INTENT_JUMP_KEY_MAIN_TAB, KEY_PAGE_PORTAL_GAME);
        launcherIntent.putExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB, PAGE_TAB_GAME_SUB_INDEX);
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        sendBroadcast(addShortcutIntent);
    }


    /**
     * 底部外部跳过来tab页面跳转
     *
     * @param event
     */
    public void onEventMainThread(EventBean event) {

        if (!this.isFinishing()) {
            switch (tag) {
               /* *********************游戏*********************/
                case Constant.PAGE_GAME_CHOICE:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    GamePortalFragment.JumpTAG = Constant.PAGE_GAME_CHOICE;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_GAME);
                    break;
                case Constant.PAGE_GAME_LIST:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    GamePortalFragment.JumpTAG = Constant.PAGE_GAME_LIST;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_GAME);
                    break;
                case Constant.PAGE_GAME_CLASSIFY:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    GamePortalFragment.JumpTAG = Constant.PAGE_GAME_CLASSIFY;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_GAME);
                    break;

               /* *********************軟件*********************/
                case Constant.PAGE_APP_CHOICE:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    SoftwarePortalFragment.jumpTAG = Constant.PAGE_APP_CHOICE;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_SOFT);

                    break;
                case Constant.PAGE_APP_LIST:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    SoftwarePortalFragment.jumpTAG = Constant.PAGE_APP_LIST;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_SOFT);
                    break;
                case Constant.PAGE_APP_CLASSIFY:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    SoftwarePortalFragment.jumpTAG = Constant.PAGE_APP_CLASSIFY;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_SOFT);
                    break;

               /* *********************榜單*********************/
                case Constant.PAGE_LIST:
                    LTApplication.instance.jumpIsFromMainActivity = true;
                    SoftwarePortalFragment.jumpTAG = Constant.PAGE_LIST;
                    mFragmentTabHost.setCurrentTab(PAGE_TAB_RANK);
                    break;
                case Constant.CORNER_COUNT:
//                    int cornerCount = getAllTaskCount();
//                    BadgeUtil.setBadgeCount(this, cornerCount);
//                    Log.i("zzz", "更新角标数量===" + cornerCount);
                    break;

            }
        }

    }

    public void onEventMainThread(String str) {
        if (str.equals(Constant.CORNER_COUNT)) {
            int cornerCount = getAllTaskCount();
            BadgeUtil.setBadgeCount(this, cornerCount);
            Log.i("Erosion", "更新角标数量===" + cornerCount);
        }
    }

    public static class EventBean {
        public EventBean(String s) {
            this.tag = s;
        }

        public static String tag = "";
    }

    /***
     * 请求搜索广告词
     */
    private List<HotSearchBean> adsList = new ArrayList<>();

    private void requestSearchAdsData() {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<BaseBeanList<HotSearchBean>>() {

            @Override
            public void onResponse(Call call, Response response) {
                BaseBeanList<HotSearchBean> base = (BaseBeanList<HotSearchBean>) response.body();
                if (base != null && base.size() > 0) {
                    List<HotSearchBean> data = base;
                    adsList.addAll(data);
                    if (adsList.size() > 0) {
                        mHeadView.setAutoBoard(adsList.get(0).getTitle(), adsList.get(0).getId() + "");
                    }
                } else {
                    mHeadView.setAutoBoard("请输入关键字  ", "");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                mHeadView.setAutoBoard("请输入关键字  ", "");
            }
        }).bulid().requestSearchAds();
    }

    /**
     * 唤醒其他app
     */
    private void wakeUpOtherApps() {
        wakeUpBaiduZhuShou();
//        wakeUpBaiduZhuShou2();
    }

    /**
     * 唤醒百度助手
     */
    private void wakeUpBaiduZhuShou() {
        try {
            startService(new Intent().setComponent(new ComponentName("com.baidu.appsearch", "com.baidu.appsearch.silent.EmptyService")));
//            wakeUpBaiduZhuShou2();
        } catch (Throwable localThrowable) {
        }

    }

    /**
     * 进入华为手机管家  （设置我们的应用为白名单：不被清理）
     */
    private void wakeUpBaiduZhuShou2() {
        try {
//            startActivity(new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")));

            //乐视安全管家 ActivityRecord{55e825c u0 com.letv.android.letvsafe/.BackgroundAppManageActivity, isShadow:false t243}
//            startActivityForResult(new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity")),1000);
        } catch (Throwable localThrowable) {
//            LogUtils.d()
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "MainActivity中取消了==请求码" + requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }

    private void registerNetWorkReciver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.setPriority(2147483647);
        myNetReceiver = new NetworkChangeReceiver(this);
        registerReceiver(myNetReceiver, mFilter);
        Log.d("8887", "网络广播注册了~~");
    }

    /**
     * 初始化 应用管理 小红点
     */
    private void initAppManagerRedPoint() {
        if (UpgradeListManager.getInstance().isInit()) {
            refreshAppUpdateCount(UpgradeListManager.getInstance().getUpgradeAppList().size());
        } else {
            UpgradeListManager.getInstance().registerCallback(new UpgradeListManager.Callback() {
                @Override
                public void onResponse(List<AppDetailBean> upgradeList) {
                    UpgradeListManager.getInstance().unregisterCallback(this);
                    refreshAppUpdateCount(upgradeList.size());
                }
            });
        }
        countCallback = new UpgradeListManager.CountCallback() {
            @Override
            public void onCountChange(int count) {
                refreshAppUpdateCount(count);
            }
        };
        UpgradeListManager.getInstance().registerCountCallback(countCallback);

    }

    public void onEventMainThread(DownloadEvent downloadEvent) {
        if (downloadEvent.status == DownloadStatusDef.completed || downloadEvent.status == DownloadStatusDef.pending) {
            refreshTaskManagerRedCount();
            refreshAppUpdateCount(UpgradeListManager.getInstance().getUpgradeAppList().size());
        }
    }


    public void onEventMainThread(InstallEvent installEvent) {
        if (installEvent.getType() == InstallEvent.INSTALLED_ADD || installEvent.getType() == InstallEvent.UNINSTALL) {
            refreshTaskManagerRedCount();
            refreshAppUpdateCount(UpgradeListManager.getInstance().getUpgradeAppList().size());
        }
    }


    public void onEventMainThread(RemoveEvent removeEvent) {
        refreshTaskManagerRedCount();
        refreshAppUpdateCount(UpgradeListManager.getInstance().getUpgradeAppList().size());
        refreshActionBarRed();
    }

    /**
     * 刷新任务管理小红点
     */
    public void refreshTaskManagerRedCount() {
        setTaskCount(DownloadTaskManager.getInstance().getDownloadTaskList().size() + DownloadTaskManager.getInstance().getInstallTaskList().size());
        MinePortalFragment fragment = getMineFragment();
        if (fragment != null) {
            fragment.updateTaskCount(getTaskCount());
        }
        refreshTabMineRedNum();
    }

    /**
     * 更新首页头部右上角角标
     */
    private void refreshActionBarRed() {
        if (getTaskCount() == 0) {
            mHeadView.refreshToolBarRedPoint(View.INVISIBLE);
        }

    }


    /**
     * 刷新应用升级(忽略已过滤)
     *
     * @param count
     */
    private void refreshAppUpdateCount(int count) {
        setAppUpgradeCount(count);
        MinePortalFragment fragment = getMineFragment();
        if (fragment != null) {
            fragment.updateAppManagerRedPoint(getAppUpgradeCount());
        }
        refreshTabMineRedNum();
    }

    /**
     * 刷新首页底部我的小红点
     */
    private void refreshTabMineRedNum() {
        refreshTabMineRedPoint(getTaskCount() + getAppUpgradeCount());
    }

    //方便以后扩展(提供get,set方法)
    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getAppUpgradeCount() {
        return appUpgradeCount;
    }

    public void setAppUpgradeCount(int appUpgradeCount) {
        this.appUpgradeCount = appUpgradeCount;
    }

    /**
     * 检测root权限
     */
    private void checkRootPermission() {
        ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                if (ShellUtils.checkRootPermission()) {
                    GlobalConfig.deviceIsRoot = true;
                    GlobalConfig.setRootInstall(MainActivity.this, true);
                } else {
                    GlobalConfig.deviceIsRoot = false;
                    GlobalConfig.setRootInstall(MainActivity.this, false);
                }
                SettingActivity.rootInstallIsChecked = true;
                EventBus.getDefault().post(new RootIsCheckedEvent());
            }
        });
    }

    public void refreshTabMeNewForMinePortalFragment(boolean isNewVersion) {
        setmHavePlat(isNewVersion);
        refreshTabMineRedNum();
    }

    public boolean ismHavePlat() {
        return mHavePlat;
    }

    public void setmHavePlat(boolean mHavePlat) {
        this.mHavePlat = mHavePlat;
    }

    public int getCurPage() {
        return mFragmentTabHost.getCurrentTab();
    }
}
