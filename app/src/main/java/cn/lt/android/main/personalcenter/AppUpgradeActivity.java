package cn.lt.android.main.personalcenter;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.DownloadSpeedEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.Item;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ViewUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.log.Logger;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/8/23.
 */
public class AppUpgradeActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private boolean hasClicked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_upgrade);
        setStatusBar();
        EventBus.getDefault().register(this);
        init();
        setOrRefreshUpdateCount(true);
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_APP_UDPATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private LoadingLayout mLoadingLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mOneKeyUpgradeLayout;
    private Button mOneKeyUpgrade;
    private ActionBar mActionBar;

    private AppUpgradeAdapter mAdapter;

    private View mEmptyView;

    private void init() {
        mEmptyView = LayoutInflater.from(this).inflate(R.layout.view_empty_app_download, null);
        mEmptyView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页去下载
                UIController.goHomePage(AppUpgradeActivity.this, MainActivity.PAGE_TAB_RECOMMEND, MainActivity.PAGE_TAB_GAME_SUB_INDEX);
            }
        });
        ((TextView) mEmptyView.findViewById(R.id.text)).setText(R.string.app_upgrade_empty_tips);

        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mOneKeyUpgradeLayout = (LinearLayout) findViewById(R.id.one_key_upgrade_layout);
        mOneKeyUpgrade = (Button) findViewById(R.id.one_key_upgrade);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);

        mLoadingLayout.setEmptyView(mEmptyView);
        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManger);
        mAdapter = new AppUpgradeAdapter(this, this, getPageAlias());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);

        mLoadingLayout.showLoading();
        mActionBar.setTitle("应用升级");


    }

    /**
     * @param needExposure 是否需要调用玩咖的曝光API
     */
    public void setOrRefreshUpdateCount(boolean needExposure) {
        final List<AppDetailBean> upgradeList = UpgradeListManager.getInstance().getUpgradeAppList();
        Logger.i(upgradeList.size() + "===================");
        upDateCount = upgradeList.size();
        List<Item> transfer = transfer(upgradeList, false);

        final List<AppDetailBean> ignoreList = UpgradeListManager.getInstance().getIgnoreAppList();
        transfer.addAll(transfer(ignoreList, true));
        mAdapter.setList(transfer);
        mLoadingLayout.showContent();
        updateEmptyIf();
        updateOneKeyButton();

        if (needExposure) {
            final List<AppDetailBean> totalList = new ArrayList<>();
            totalList.addAll(upgradeList);
            totalList.addAll(ignoreList);
            WanKaManager.exposureApps(totalList, new SimpleResponseListener<JSONObject>() {
                @Override
                public void onSucceed(int what, Response<JSONObject> response) {
                    for (AppDetailBean appDetailBean : totalList) {

                        for (AppDetailBean upgradeDetailBean : upgradeList) {
                            if (appDetailBean.getPackage_name().equals(upgradeDetailBean.getPackage_name())) {
                                upgradeDetailBean = appDetailBean;
                                break;
                            }
                        }

                        for (AppDetailBean ignoreDetailBean : ignoreList) {
                            if (appDetailBean.getPackage_name().equals(ignoreDetailBean.getPackage_name())) {
                                ignoreDetailBean = appDetailBean;
                                break;
                            }
                        }
                    }
                    List<Item> transfer = transfer(upgradeList, false);
                    transfer.addAll(transfer(ignoreList, true));
                    mAdapter.setList(transfer);
                    mLoadingLayout.showContent();
                    updateEmptyIf();
                    updateOneKeyButton();
                }
            }, "升级列表曝光");
        }
    }

    private int upDateCount;

    private List<Item> transfer(List<AppDetailBean> upgradeList, boolean isIgnore) {
        List<Item> list = new ArrayList<>();
        if (upgradeList.size() == 0) return list;
        try {
            if (!isIgnore) {

                list.add(new Item(AppUpgradeAdapter.TYPE_LABEL, this.getString(R.string.all_upgrade) + "(" + upgradeList.size() + ")"));
                for (int i = 0;i < upgradeList.size();i++) {
                    DownloadTaskManager.getInstance().transfer(upgradeList.get(i));
                    list.add(new Item(AppUpgradeAdapter.TYPE_APP_UPGRADE, upgradeList.get(i),i));
                    list.add(new Item(AppUpgradeAdapter.TYPE_DIVIDER, null));
                }
                list.remove(list.size() - 1);
            } else {
                list.add(new Item(AppUpgradeAdapter.TYPE_LABEL, this.getString(R.string.ignore_upgrade) + "(" + upgradeList.size() + ")"));
                if (isClick) {
                    for (int i = 0;i < upgradeList.size();i++) {
                        DownloadTaskManager.getInstance().transfer(upgradeList.get(i));
                        list.add(new Item(AppUpgradeAdapter.TYPE_APP_IGNORE, upgradeList.get(i),i));
                        list.add(new Item(AppUpgradeAdapter.TYPE_DIVIDER, null));
                    }
//                    list.remove(list.size() - 1);
                    list.add(new Item(AppUpgradeAdapter.TYPE_FIND_IGNORE, null));
                } else {
                    list.add(new Item(AppUpgradeAdapter.TYPE_FIND_IGNORE, null));
                }

            }
        } catch (RemoteException re) {
            re.printStackTrace();
        }
        return list;
    }

    private void updateEmptyIf() {
        if (mAdapter.getList().size() == 0) {
            mLoadingLayout.showEmpty();
        }
    }

    private int waitingUpDateCount;

    public void updateOneKeyButton() {
        waitingUpDateCount = 0;
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            Item item = mAdapter.getList().get(i);
            int viewType = item.viewType;
            if (viewType == AppUpgradeAdapter.TYPE_APP_UPGRADE) {
                AppDetailBean appDetailBean = (AppDetailBean) item.data;
                int status = appDetailBean.getDownloadAppEntity().getStatus();
                switch (status) {
                    case InstallState.upgrade:
                    case DownloadStatusDef.retry:
                    case DownloadStatusDef.paused:
                        waitingUpDateCount++;
                        break;
                    case InstallState.install_failure:
                    case DownloadStatusDef.error:
                        break;
                }
            }
        }
        Logger.i("up =" + waitingUpDateCount);
        if (upDateCount >= 2 && waitingUpDateCount >= 2) {
            mOneKeyUpgradeLayout.setVisibility(View.VISIBLE);
            mOneKeyUpgrade.setOnClickListener(this);
        } else {
            mOneKeyUpgradeLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            Item item = mAdapter.getList().get(i);
            int viewType = item.viewType;
            if (viewType == AppUpgradeAdapter.TYPE_APP_UPGRADE || viewType == AppUpgradeAdapter.TYPE_APP_IGNORE) {
                AppDetailBean appDetailBean = (AppDetailBean) item.data;
                AppEntity appInfo = appDetailBean.getDownloadAppEntity();
                if (FileDownloadUtils.generateId(appInfo.getPackageName(), appInfo.getSavePath()) == downloadEvent.downloadId) {

                    Logger.i("状态 = " + downloadEvent.status);
                    appInfo.setStatus(downloadEvent.status);
                    appInfo.setTotal(downloadEvent.totalBytes);
                    appInfo.setSoFar(downloadEvent.soFarBytes);
                    mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
                }
            }
        }
        updateOneKeyButton();
    }

    /**
     * 更新下载速度
     *
     * @param downloadSpeedEvent
     */
    public void onEventMainThread(DownloadSpeedEvent downloadSpeedEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            Item item = mAdapter.getList().get(i);
            int viewType = item.viewType;
            if (viewType == AppUpgradeAdapter.TYPE_APP_UPGRADE || viewType == AppUpgradeAdapter.TYPE_APP_IGNORE) {
                AppDetailBean appDetailBean = (AppDetailBean) item.data;
                AppEntity appInfo = appDetailBean.getDownloadAppEntity();
                if (downloadSpeedEvent.id.equals(appInfo.getAppClientId())) {
                    //更新界面i
                    appInfo.setSurplusTime(downloadSpeedEvent.surplus);
                    appInfo.setSpeed(downloadSpeedEvent.speed);
                    mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
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
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            Item item = mAdapter.getList().get(i);
            int viewType = item.viewType;
            if (viewType == AppUpgradeAdapter.TYPE_APP_UPGRADE || viewType == AppUpgradeAdapter.TYPE_APP_IGNORE) {
                AppDetailBean appDetailBean = (AppDetailBean) item.data;
                AppEntity appInfo = appDetailBean.getDownloadAppEntity();
                if (appInfo.getPackageName().equals(installEvent.packageName)) {
                    Logger.i("下载安装状态 = " + appInfo.getStatus());
                    if (installEvent.type == InstallEvent.UNINSTALL) {
                        return;
                    }
                    //更新界面i
                    appInfo.setStatusByInstallEvent(installEvent.type);
                    mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
                }
            }
        }
        updateOneKeyButton();
    }


    private boolean isClick = false;

    public void onEventMainThread(String event) {
        if (event.equals(AppUpgradeAdapter.CLICK_CHECK_IGNORE)) {
            isClick = !isClick;
            setOrRefreshUpdateCount(false);
        }

//        if (event.equals(AppUpgradeAdapter.CLICK_IGNORE)) {
//            // 忽略单个应用升级
//            isClick = false;
//            setOrRefreshUpdateCount(false);
//        }

    }

    public void onEventMainThread(RemoveEvent event) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            Item item = mAdapter.getList().get(i);
            int viewType = item.viewType;
            if (viewType == AppUpgradeAdapter.TYPE_APP_UPGRADE || viewType == AppUpgradeAdapter.TYPE_APP_IGNORE) {
                AppDetailBean appDetailBean = (AppDetailBean) item.data;
                AppEntity appInfo = appDetailBean.getDownloadAppEntity();
                if (appInfo.getPackageName().equals(event.mAppEntity.getPackageName())) {
                    LogUtils.i("ccc", "升级移除了 = ");
                    //更新界面i
                    appInfo.setStatusByInstallEvent(DownloadStatusDef.INVALID_STATUS);
                    mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
                }
            }
        }
        updateOneKeyButton();

    }

    @Override
    public void onClick(View v) {
        if (ViewUtils.isFastClick()) return;
        if (!NetUtils.isConnected(this)) {
            new DownloadChecker().noNetworkPromp(this, new Runnable() {
                @Override
                public void run() {
                    UpgradeListManager.getInstance().startAllAfterCheck(AppUpgradeActivity.this, getPageAlias(), true);
                }
            });
            return;
        }
        switch (v.getId()) {
            case R.id.one_key_upgrade:
                //一秒内点击无效，并不会上报
                if (hasClicked) {
                    peformOneKeyUpgrade();
                    hasClicked = false;
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hasClicked = true;
                        }
                    }, 1000);
                }
                break;
        }
    }

    private void peformOneKeyUpgrade() {
        //一键 全部更新
        if (NetUtils.isMobileNet(this)) {
            new CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？").setPositiveButton(R.string.continue_mobile_download).setPositiveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UpgradeListManager.getInstance().startAllAfterCheck(AppUpgradeActivity.this, getPageAlias(), false);
                }
            }).setNegativeButton(R.string.order_wifi_download).setNegativeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UpgradeListManager.getInstance().startAllAfterCheck(AppUpgradeActivity.this, getPageAlias(), true);
                }
            }).create().show();
        } else {
            UpgradeListManager.getInstance().startAllAfterCheck(this, getPageAlias(), false);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Activity被意外终止（回收）的话，直接关闭该页面
        this.finish();
    }
}
