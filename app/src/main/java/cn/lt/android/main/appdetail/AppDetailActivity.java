package cn.lt.android.main.appdetail;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.umeng.socialize.UMShareAPI;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.AdService;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.request.AdAppDetailResponseListener;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.base.DefaultFragmentPagerAdapter;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.main.download.BigDownloadButton;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.umsharesdk.OneKeyShareUtil;
import cn.lt.android.umsharesdk.ShareBean;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.FromPageManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/1/21.
 *
 * @desc 游戏详情主页面
 */
public class AppDetailActivity extends BaseAppCompatActivity implements View.OnClickListener, BigDownloadButton.ScrollCallBack {
    public final static String TYPE_SOFTWARE = "software";
    public final static String TYPE_GAME = "game";
    private PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    private AppDetailFragment mAppdetailFragment;
    private AppBaseInfoView mBaseInfoView;
    private ActionBar mActionBar;
    private AppDetailBean appDeailBean;
    public String mId = "";
    private NetDataInterfaceDao mAppDetailNetDataInterfaceDao, mRecommendNetDataInterfaceDao;
    private LoadingLayout mLoadingLayout;
    private BigDownloadButton mDownloadBar;
    private ShareBean shareBean;
    private ImageView mBack;
    private ScrollView mScrollView;
    private boolean isRequestComplete = false;
    private View notFoundView;
    public String fromPage = "";
    public boolean isAd = false;
    public String pkgName = "";
    private String mCurrentType = TYPE_SOFTWARE;//当前所属分类类型
    private boolean autoDownloadIsExecute;// 判断通过点击推送进来，自动下载是否已经执行
    private boolean hasClicked = true;
    private String adCategory;
    private static final String KEYPARCELABLE = "Parcelable";
    private String adDownUrl;

    public String adMold = "";// 广告所属商家
    @Override
    public void setPageAlias() {
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_app_detail);
        setStatusBar();
        EventBus.getDefault().register(this);
        recoverData(bundle);
        initView();
        getIntentData();
        requestNetData();
    }

    private void requestNetData() {
        if (isAd) {
            //请求第三方接口
            requestAdsData(pkgName);
        } else {
            requestData();//请求自己的接口
        }
    }

    private void recoverData(Bundle bundle) {
        if (bundle != null) {
            if ((bundle.getParcelable(KEYPARCELABLE) != null) && appDeailBean == null) {
                appDeailBean = bundle.getParcelable(KEYPARCELABLE);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable(KEYPARCELABLE, appDeailBean);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar.setPageName("应用详情");
//        if (getAppDeailBean() != null) {
//            AppEntity appInfo = getAppDeailBean().getDownloadAppEntity();
//            if (appInfo != null) {
//                if(AppUtils.isInstalled(appInfo.getPackageName())) {
//
//                    mDownloadBar.showOpenApp();
//                }
//            }
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    private void getIntentData() {
        isAd = getIntent().getBooleanExtra(Constant.EXTRA_AD, false);
        pkgName = getIntent().getStringExtra(Constant.EXTRA_PKGNAME);

        if (isAd) {
            adCategory = getIntent().getStringExtra(Constant.EXTRA_CATEGORY);
            adMold = getIntent().getStringExtra(Constant.EXTRA_AD_MOLD);
        } else {
            mId = getIntent().getStringExtra(Constant.EXTRA_ID);
        }
        mCurrentType = getIntent().getStringExtra(Constant.EXTRA_TYPE);
        fromPage = getIntent().getStringExtra(Constant.EXTRA_PAGE);
    }

    private void requestData() {
        mLoadingLayout.showLoading();
        if (!NetWorkUtils.isConnected(this)) {
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLoadingLayout.showErrorNoNetwork();
                }
            }, 500);
            return;
        }
        if (TYPE_SOFTWARE.equals(mCurrentType)) {
            requestAppData();
            shareBean.setShareType(OneKeyShareUtil.ShareType.software);
        } else {
            requestGameData();
            shareBean.setShareType(OneKeyShareUtil.ShareType.game);
        }
    }

    /***
     * APP网络请求
     */
    private void requestAppData() {
        mAppDetailNetDataInterfaceDao.requestSoftWareDetail(mId);
    }

    /***
     * 游戏网络请求
     */
    private void requestGameData() {
        mAppDetailNetDataInterfaceDao.requestGameDetail(mId);
    }

    /***
     * 请求推荐
     *
     * @param type
     * @param pkg
     */
    private void requestRecommentData(String type, String pkg) {
        mRecommendNetDataInterfaceDao.requestRecommendApp(type, pkg);
    }

    /***
     * 构建广告推荐请求
     */
    private void buidRecommendRequest() {
        mRecommendNetDataInterfaceDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<RecommendBean>>() {
            @Override
            public void onResponse(Call<List<RecommendBean>> call, Response<List<RecommendBean>> response) {
                List<RecommendBean> datas = response.body();
                if (datas != null) {
                    try {
                        new DownloadAdAppReplacer().replaceByRecommendApps(datas);
                        appDeailBean.setRecommend_apps(datas);
                        mAppdetailFragment.checkNetwork();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RecommendBean>> call, Throwable t) {

            }
        }).bulid();
    }

    /***
     * 请求广告详情
     */
    private void requestAdsData(String packageName) {
        AdService.getInstance().fetchAdDetail(adMold, packageName, new AdAppDetailResponseListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSucceed(AppDetailBean appDetailBean) {
                appDeailBean = appDetailBean;

                if (adMold.equals(AdMold.WanDouJia)) {
                    // 豌豆荚广告详情下载地址需用外部传进来
                    appDeailBean.setDownload_url(getIntent().getStringExtra(Constant.EXTRA_AD_DOWNLOAD_URL));
                }

                if (null != appDeailBean) {
                    setAppDeailBean(appDeailBean);
                    mBaseInfoView.setData(appDeailBean);
                    setHeadTitle(appDeailBean.getName());
                    requestRecommentData(adCategory, pkgName);
                    try {
                        AppEntity appEntity = DownloadTaskManager.getInstance().transfer(appDeailBean);
                        if (appEntity.getApps_type().equals("game")) {
                            appEntity.resource_type = "game_info";
                        } else {
                            appEntity.resource_type = "app_info";
                        }
                        mDownloadBar.setData(appEntity, isAd ? Constant.PAGE_AD_DETAIL : Constant.PAGE_DETAIL,appDetailBean.getPackage_name());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        // TODO:
                        return;
                    }
                    mLoadingLayout.showContent();
                }
            }

            @Override
            public void onFailed(com.yolanda.nohttp.rest.Response<String> response) {
                mLoadingLayout.showErrorNotGoodNetwork();
                ToastUtils.showToast("请求数据失败");
            }

            @Override
            public void onFinish() {

            }
        });
    }

    private void initView() {
        mActionBar = (ActionBar) findViewById(R.id.detail_actionbar);
        mBack = (ImageView) findViewById(R.id.iv_back);
        mScrollView = (ScrollView) findViewById(R.id.my_scrollView);
        mBack.setOnClickListener(this);
        mActionBar.setShareViewEnable(false);// 还没加载数据，分享按钮设置不可点击
        shareBean = new ShareBean(this);
        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mLoadingLayout.showLoading();
        mDownloadBar = (BigDownloadButton) findViewById(R.id.download_progress_bar);
        mDownloadBar.setmCallBack(this);
        mBaseInfoView = (AppBaseInfoView) findViewById(R.id.app_base_info);
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.detail_tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        List<BaseFragment> fragmentList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        mAppdetailFragment = new AppDetailFragment();

        titleList.add("详情");
        fragmentList.add(mAppdetailFragment);

        DefaultFragmentPagerAdapter pagerAdapter = new DefaultFragmentPagerAdapter(this.getSupportFragmentManager(), fragmentList, titleList);
        mViewPager.setAdapter(pagerAdapter);
        mTabs.setViewPager(mViewPager);
        buildAppDetailRequest();
        buidRecommendRequest();
        mLoadingLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNetData();
//                exposureDetail();
            }
        });
    }

    /***
     * 构建非广告的详情请求
     */
    private void buildAppDetailRequest() {
        mAppDetailNetDataInterfaceDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<AppDetailBean>() {
            @Override
            public void onResponse(Call<AppDetailBean> call, Response<AppDetailBean> response) {
                appDeailBean = response.body();
                isRequestComplete = true;
                if (null != appDeailBean) {

                    setAppDeailBean(appDeailBean);
                    mBaseInfoView.setData(appDeailBean);
                    setHeadTitle(appDeailBean.getName());
                    mAppdetailFragment.checkNetwork();
                    try {
                        LogUtils.d("ccc", "详情页费广告t收到要更新了");
                        AppEntity appEntity = DownloadTaskManager.getInstance().transfer(appDeailBean);
                        if (appEntity.getApps_type().equals("game")) {
                            appEntity.resource_type = "game_info";
                        } else {
                            appEntity.resource_type = "app_info";
                        }
                        shareBean.setResource_type(appEntity.resource_type);
                        mDownloadBar.setData(appEntity, Constant.PAGE_DETAIL,appEntity.getId() + "");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        // TODO:
                        return;
                    }
                    goAutoDownloadByPush();
                    goAutoDownloadByDeeplink();
                    setShareViewData(appDeailBean);
                    mLoadingLayout.showContent();
                } else {
                    LogUtils.i("zzz", "游戏已下架");
                    notFoundView = LayoutInflater.from(AppDetailActivity.this).inflate(R.layout.view_no_data, null);
                    mLoadingLayout.setEmptyView(notFoundView);
                    mLoadingLayout.showEmpty();
                }
            }

            @Override
            public void onFailure(Call<AppDetailBean> call, Throwable t) {
                isRequestComplete = true;
                if (isRequestComplete) {
                    mLoadingLayout.showErrorNotGoodNetwork();

                }
            }
        }).bulid();
    }

    /**
     * 设置分享shareView所需要的数据
     *
     * @param appDetail
     */
    private void setShareViewData(AppDetailBean appDetail) {
        shareBean.setApp(appDetail);
        mActionBar.setShareBean(shareBean);
        mActionBar.setShareViewEnable(true);// 数据加载完，分享按钮设置可以点击
    }

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
        if (getAppDeailBean() == null) return;
        AppEntity appInfo = getAppDeailBean().getDownloadAppEntity();
        if (FileDownloadUtils.generateId(appInfo.getPackageName(), appInfo.getSavePath()) == downloadEvent.downloadId) {
            //更新界面i
            appInfo.setTotal(downloadEvent.totalBytes);
            appInfo.setSoFar(downloadEvent.soFarBytes);
            appInfo.setStatus(downloadEvent.status);
            mDownloadBar.setData(appInfo, isAd ? Constant.PAGE_AD_DETAIL : Constant.PAGE_DETAIL);
        } else {
            for (int i = 0; i < getAppDeailBean().getRecommend_apps().size(); i++) {
                AppEntity appEntity = getAppDeailBean().getRecommend_apps().get(i).getAppEntity();
                if (FileDownloadUtils.generateId(appEntity.getPackageName(), appEntity.getSavePath()) == downloadEvent.downloadId) {
                    //更新界面i
                    appEntity.setTotal(downloadEvent.totalBytes);
                    appEntity.setSoFar(downloadEvent.soFarBytes);
                    appEntity.setStatus(downloadEvent.status);
                    mAppdetailFragment.updateRecommendView(i);
                    break;
                }
            }

        }

    }


    /**
     * 通知安装事件更新
     *
     * @param installEvent
     */
    public void onEventMainThread(InstallEvent installEvent) {
        AppEntity appInfo = getAppDeailBean().getDownloadAppEntity();
        if (appInfo.getPackageName().equals(installEvent.packageName)) {
            //更新界面i
            appInfo.setStatusByInstallEvent(installEvent.type);
            mDownloadBar.setData(appInfo, isAd ? Constant.PAGE_AD_DETAIL : Constant.PAGE_DETAIL);
        } else {
            for (int i = 0; i < getAppDeailBean().getRecommend_apps().size(); i++) {
                AppEntity appEntity = getAppDeailBean().getRecommend_apps().get(i).getAppEntity();
                if (installEvent.packageName.equals(appEntity.getPackageName())) {
                    //更新界面i
                    appEntity.setStatusByInstallEvent(installEvent.type);
                    mAppdetailFragment.updateRecommendView(i);
                    break;
                }
            }
        }

    }


    public void onEventMainThread(RemoveEvent event) {
        AppEntity appInfo = getAppDeailBean().getDownloadAppEntity();
        if (appInfo.getPackageName().equals(event.mAppEntity.getPackageName())) {
            //更新界面i
            appInfo.setStatus(DownloadStatusDef.INVALID_STATUS);
            LogUtils.d("ccc", "详情页移除收到要更新了");  //会更新变打开
            mDownloadBar.setData(appInfo, isAd ? Constant.PAGE_AD_DETAIL : Constant.PAGE_DETAIL);
        } else {
            for (int i = 0; i < getAppDeailBean().getRecommend_apps().size(); i++) {
                AppEntity appEntity = getAppDeailBean().getRecommend_apps().get(i).getAppEntity();
                if (event.mAppEntity.getPackageName().equals(appEntity.getPackageName())) {
                    //更新界面i
                    appEntity.setStatus(DownloadStatusDef.INVALID_STATUS);
                    mAppdetailFragment.updateRecommendView(i);
                    break;
                }
            }
        }
        refreshActionBarRed();

    }

    /**
     * 如果是点击推送进来的，WIFI状态自动启动下载
     */
    private void goAutoDownloadByPush() {
        if (isByPush) {
            Intent intent = getIntent();
            String pushId = intent.getStringExtra(Constant.EXTRA_PUSH_ID);

            mDownloadBar.setAppIsByPush(true);
            if(canUpgrade()) {
                if (NetWorkUtils.isWifi(this)) {
                    upgradeApp("notification", "notification is onclick", "push");
                }
            } else {
                if (!autoDownloadIsExecute) {// 判断是否已经执行过下载(强制只能执行一次)
                    mDownloadBar.autoPerformClick();
                    autoDownloadIsExecute = true;

                    // 已安装自动打开的，需要数据上报
                    if (AppUtils.isInstalled(appDeailBean.getPackage_name())) {
                        DCStat.pushEvent(pushId, "appDetail", "clicked_open", "GETUI", appDeailBean.getAppClientId());
                    }
                }
            }

            mDownloadBar.setAppIsByPush(false);
        }
    }

    /**
     * 如果是通过deeplink进来的，WIFI状态自动启动下载
     */
    private void goAutoDownloadByDeeplink() {
        boolean isByDeeplink = getIntent().getBooleanExtra(Constant.EXTRA_IS_FROM_DEEPLINK, false);
        if (isByDeeplink) {

            if(!AppUtils.isInstalled(appDeailBean.getPackage_name()) || canUpgrade()) {

                if (!autoDownloadIsExecute) {// 判断是否已经执行过下载(强制只能执行一次)
                    String from = getIntent().getStringExtra(Constant.EXTRA_DEEPLINK_FROM);
                    FromPageManager.setLastPage(from);

                    mDownloadBar.autoPerformClickByDeeplink();
                    autoDownloadIsExecute = true;
                    LogUtils.i(LogTAG.deepLinkTAG, "启动下载}");
                }

            }

        }
    }

    private void upgradeApp(String pageName, String event_reason, String source) {
        appDeailBean.getDownloadAppEntity().setStatus(InstallState.upgrade);
        DownloadTaskManager.getInstance().startAfterCheck(this, appDeailBean.getDownloadAppEntity(), "auto", "request", pageName, "", event_reason, source);

    }

    private boolean canUpgrade() {
        PackageInfo packageInfo = AppUtils.getPackageInfo(appDeailBean.getPackage_name());
        if (packageInfo != null && packageInfo.versionCode < Integer.parseInt(appDeailBean.getVersion_code())) {
            LogUtils.i(LogTAG.tempLog, appDeailBean.getName() + "收到推送来的版本号比本地的高，现在进行升级");
            return true;
        }
        LogUtils.i(LogTAG.tempLog, appDeailBean.getName() + "收到推送来的版本号没有本地的高，执行模拟点击操作");

        return false;
    }

    private void setHeadTitle(String name) {
        mActionBar.setTitle(name);
    }

    public AppDetailBean getAppDeailBean() {
        return this.appDeailBean;
    }

    public void setAppDeailBean(AppDetailBean appDeailBean) {
        this.appDeailBean = appDeailBean;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 友盟分享所需要使用的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "详情页中取消了==请求码" + requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

    @Override
    public void gotoBottom() {
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    /**
     * 更新头部右上角角标
     */
    private void refreshActionBarRed() {
        int taskCount = DownloadTaskManager.getInstance().getDownloadTaskList().size() + DownloadTaskManager.getInstance().getInstallTaskList().size();
        if(taskCount == 0) {
            mActionBar.refreshToolBarRedPoint(View.INVISIBLE);
        }

    }
}
