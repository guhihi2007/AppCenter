package cn.lt.android.main.download;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.DownloadSpeedEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.NetTypeEvent;
import cn.lt.android.event.NewDownloadTask;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.notification.event.NoticeTaskEvent;
import cn.lt.android.plateform.update.PlatUpdateAction;
import cn.lt.android.plateform.update.PlatUpdateService;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;
import cn.lt.download.DownloadAgent;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/2/16.
 * 应用下载fragment
 */
public class AppDownloadFragment extends BaseFragment {

    public int mAppDownCount;
    private boolean hasClicked = true;
    private double mCurrentNetType = 100;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_app_download, container, false);
            init(mRootView);
        }
        getIntentData();

        // 初始化页面数据已经放在onResume方法里面了（有不明白来问俊生）

        EventBus.getDefault().register(this);
        initNetIcon();
        LogUtils.i("iii", "AppDownloadFragment onCreateView getUserVisibleHint?" + getUserVisibleHint());
        if (getUserVisibleHint()) {
            setPageAndUploadPageEvent();
        }
        return mRootView;
    }

    /**
     * 这个方法才是fragment真正的可见
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogUtils.i("iii", "AppDownloadFragment setUserVisibleHint走了" + isVisibleToUser);
        if (isVisibleToUser && mRootView != null) setPageAndUploadPageEvent();
    }

    /**
     * 设置页面并上报
     */
    private void setPageAndUploadPageEvent() {
        LogUtils.i("iii", "AppDownloadFragment statEvent");
        setmPageAlias(Constant.PAGE_DOWNLOAD);
        statEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (isVisible && ((TaskManagerActivity) getActivity()).mTabs.getCurrentPosition() == 0) {      //如何过滤不可见时，不能上报
//            setmPageAlias(Constant.PAGE_DOWNLOAD);
//            statEvent();
//        }
        showData();
    }

    private void showData() {
        requestData();
        //更新空页面
        updateEmptyIf();
        //更新升级view
        initUpgradeView();
    }

    private void initNetIcon() {
        if (NetUtils.isMobileNet(getActivity())) {
            //全部bean全部改为4G
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                AppEntity appInfo = mAdapter.getList().get(i);
                appInfo.netType = Constant.NET_MOBILE_PHONE;

            }
        } else if (NetUtils.isWifiNet(getActivity())) {
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                AppEntity appInfo = mAdapter.getList().get(i);
                appInfo.netType = Constant.NET_WIFI;
            }
        } else {
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                AppEntity appInfo = mAdapter.getList().get(i);
                appInfo.netType = Constant.NO_NET;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public AppDownloadAdapter mAdapter;
    private View mEmptyView;
    private LoadingLayout mLoadingLayout;
    private RecyclerView mRecyclerView;
    private RelativeLayout mUpgradeLayout;
    private Button mUpgrade;
    private Button mClose;
    private TextView mUpgradeDesc;
    private Button mOneKeyDownload;


    private void init(View v) {
        mEmptyView = LayoutInflater.from(mContext).inflate(R.layout.view_empty_app_download, null);
        mEmptyView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页去下载
                UIController.goHomePage(mContext, MainActivity.PAGE_TAB_RECOMMEND, MainActivity.PAGE_TAB_GAME_SUB_INDEX);
            }
        });
        ((TextView) mEmptyView.findViewById(R.id.text)).setText(R.string.app_download_empty_tips);

        mLoadingLayout = (LoadingLayout) v.findViewById(R.id.loadingLayout);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mUpgradeLayout = (RelativeLayout) v.findViewById(R.id.upgrade_layout);
        mUpgrade = (Button) v.findViewById(R.id.upgrade);
        mClose = (Button) v.findViewById(R.id.close);
        mUpgradeDesc = (TextView) v.findViewById(R.id.upgrade_desc);
        mOneKeyDownload = (Button) v.findViewById(R.id.one_key_download);


        mLoadingLayout.setEmptyView(mEmptyView);
        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManger);
        mRecyclerView.setItemAnimator(null);
        mAdapter = new AppDownloadAdapter(mContext, new AppDownloadAdapter.OnDeleteListener() {
            @Override
            public void onDelete() {
                updateEmptyIf();
            }

        }, getPageAlias());
        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.addHeaderView(createHeaderView());
        mAdapter.notifyDataSetChanged();
        mAppDownCount = mAdapter.getItemCount();
        mLoadingLayout.showLoading();
    }

    private void requestData() {
        try {
            List<AppEntity> downloadList = DownloadTaskManager.getInstance().getDownloadTaskList();
            if (downloadList.size() == 0) {
                updateEmptyIf();
            } else {
                // 列表初始化时，如果状态是invalid变成暂停
                for (AppEntity appEntity : downloadList) {
                    if (appEntity.getStatus() == DownloadStatusDef.INVALID_STATUS) {
                        int downloadId = DownloadTaskManager.getInstance().getDownloadId(appEntity);
                        DownloadAgent.getImpl().updatePauseStatus(downloadId);
                        appEntity.setStatus(DownloadStatusDef.paused);
                    }
                }

//                WanKaManager.exposureApps(downloadList, new SimpleResponseListener<JSONObject>() {
//                    @Override
//                    public void onSucceed(int what, Response<JSONObject> response) {
//                        mAdapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onFailed(int what, Response<JSONObject> response) {
//
//                    }
//                },"下载列表(重试)曝光: ");

                mAdapter.setList(downloadList);
                mLoadingLayout.showContent();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void onEventMainThread(RemoveEvent event) {
        if (mAdapter.getList().contains(event.mAppEntity)) {
            mAdapter.getList().remove(event.mAppEntity);
            mAdapter.notifyDataSetChanged();
            updateOneKeyButton();
        }
    }

    /**
     * 下载列表加入一条下载数据
     * 通知栏点击玩咖替换url地址异步
     */
    public void onEventMainThread(NewDownloadTask newDownloadTask) {
        AppEntity entity = newDownloadTask.entity;
        if (entity == null) {
            return;
        }

        for (AppEntity appEntity : mAdapter.getList()) {
            if (appEntity.getPackageName().equals(entity.getPackageName())) {
                // 列表中存在
                return;
            }
        }

        mAdapter.getList().add(entity);
        mAdapter.notifyItemInserted(mAdapter.getList().size() - 1);
        updateOneKeyButton();
    }

    /**
     * 改变网络标志图标
     *
     * @param event
     */
    public void onEventMainThread(NetTypeEvent event) {
        LogUtils.d("8887", "Download收到网络改变了" + event.getNetType());
        if (mCurrentNetType == event.getNetType()) {
            return;
        } else {
            mCurrentNetType = event.getNetType();
            LogUtils.d("8887", "Download真正改变网络图标" + event.getNetType());
        }
        if (event.getNetType() == ConnectivityManager.TYPE_MOBILE) {
            //全部bean全部改为4G
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                AppEntity appInfo = mAdapter.getList().get(i);
                appInfo.netType = Constant.NET_MOBILE_PHONE;

            }
        } else if (event.getNetType() == ConnectivityManager.TYPE_WIFI) {
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                AppEntity appInfo = mAdapter.getList().get(i);
                appInfo.netType = Constant.NET_WIFI;
            }
        } else {
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                AppEntity appInfo = mAdapter.getList().get(i);
                appInfo.netType = Constant.NO_NET;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    /***
     * 初始化版本升级提示框，该提示框优先于一件升级按钮显示
     */
    private void initUpgradeView() {
        VersionCheckManger.getInstance().checkVerison(new VersionCheckManger.VersionCheckCallback() {
            @Override
            public void callback(Result result, final VersionInfo info) {
                boolean isUpgrading = VersionCheckManger.getInstance().isUpgrading();
                int size = mAdapter.getList().size();
                if (Result.have == result && !isUpgrading && size > 0) {
                    LogUtils.i("PlatUpdateView", "有版本升级，需弹框提示11");
                    if (UpdateUtil.needShowUpdateView(mContext)) {
                        LogUtils.i("PlatUpdateView", "有版本升级，需弹框提示22");
                        mOneKeyDownload.setVisibility(View.GONE);
                        mUpgradeLayout.setVisibility(View.VISIBLE);
                    } else {
                        updateOneKeyButton();
                    }
                    mUpgradeDesc.setText(mContext.getString(R.string.upgrade_tips) + "V" + info.getmUpgradeVersion());
                    mUpgrade.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //升级
                            mUpgradeLayout.setVisibility(View.GONE);
                            if (info.isForce()) {
                                UpdateUtil.savePlateUpdateType(mContext, true);
                            } else {
                                UpdateUtil.savePlateUpdateType(mContext, false);
                            }
                            PlatUpdateService.startUpdateService(PlatUpdateAction.ACTION_DIALOG_CONFIRM, mContext);

                        }
                    });
                    mClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //关闭升级
                            UpdateUtil.saveUpdateViewThisTime(mContext, System.currentTimeMillis());//记录第一次关闭的时间
                            mUpgradeLayout.setVisibility(View.GONE);
                            updateOneKeyButton(); //关闭按钮后如果有两个以上下载任务显示一键下载按钮
                        }
                    });
                } else if (Result.none == result) {
                    LogUtils.i("PlatUpdateView", "没有版本升级");
                    mUpgradeLayout.setVisibility(View.GONE);
                    updateOneKeyButton();
                } else {
                    LogUtils.i("PlatUpdateView", "版本升级请求失败");
                    mUpgradeLayout.setVisibility(View.GONE);
                    updateOneKeyButton();
                }
            }
        }, true);
    }

    private int getUndownloadCount(List<AppEntity> downloadList) {
        int count = 0;
        for (AppEntity appEntity : downloadList) {
            int state = appEntity.getStatus();
            if (!DownloadStatusDef.isIng(state)) {
                count++;
            }
            //下载任务删掉了
            if (DownloadStatusDef.isInvalid(state)) {
                count--;
            }
        }

        Log.i("download", "DownloadFragment:任务总个数：---------" + downloadList.size());
        return count;
    }


    private void updateEmptyIf() {
        if (mAdapter.getList().size() == 0) {
            mLoadingLayout.showEmpty();
        }
    }


    private void updateOneKeyButton() {
        int undownloadCount = getUndownloadCount(mAdapter.getList());
        if (undownloadCount >= 2) {
            showOneKeyDownload(undownloadCount);
            if (mUpgradeLayout.getVisibility() == View.VISIBLE) {
                mUpgradeLayout.setVisibility(View.GONE);
            }
        } else {
            hideOneKeyDownload();
        }
    }

    /**
     * 通知下载进度更新
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppEntity appInfo = mAdapter.getList().get(i);

            if (FileDownloadUtils.generateId(appInfo.getPackageName(), appInfo.getSavePath()) == downloadEvent.downloadId) {
                //更新界面i
                Log.i("download", "fragment中eventbus传来的状态：" + appInfo.getName() + "-----------是：" + downloadEvent.status);
                appInfo.setStatus(downloadEvent.status);
                if (appInfo.getStatus() == DownloadStatusDef.progress) {
                    appInfo.setTotal(downloadEvent.totalBytes);
                    appInfo.setSoFar(downloadEvent.soFarBytes);
                }
                if (appInfo.getStatus() == DownloadStatusDef.error) {
                    appInfo.setErrMsg(downloadEvent.errorMessage);
                }
                mAdapter.notifyItemChanged(i);

                //下载完成则移除
                if (downloadEvent.status == DownloadStatusDef.completed) {
                    LogUtils.i("yyy", "AppDownload=====下载完成了" + appInfo.getName());
                    mAdapter.getList().remove(appInfo);
                    mAdapter.notifyDataSetChanged();
                    updateEmptyIf();
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
            AppEntity appInfo = mAdapter.getList().get(i);
            if (downloadSpeedEvent.id.equals(appInfo.getAppClientId())) {
                //更新界面i
                if (appInfo.getStatus() == DownloadStatusDef.progress) {
                    appInfo.setSurplusTime(downloadSpeedEvent.surplus);
                    appInfo.setSpeed(downloadSpeedEvent.speed);
                }
                mAdapter.notifyItemChanged(i);
            }

        }
        updateEmptyIf();
    }

    /**
     * 通知安装事件更新
     *
     * @param installEvent
     */
    public void onEventMainThread(InstallEvent installEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppEntity appInfo = mAdapter.getList().get(i);
            if (appInfo.getPackageName().equals(installEvent.packageName)) {
                //更新界面i
                appInfo.setStatusByInstallEvent(installEvent.type);
                mAdapter.notifyItemChanged(i);
            }

        }
    }


    private View createHeaderView() {
        View view = new View(mContext);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(mContext, 8)));
        return view;
    }

    private void getIntentData() {
        if (null != getActivity().getIntent()) {

            // 是否通过点击升级通知启动本页面的
            String jumpBy = getActivity().getIntent().getStringExtra(NoticeConsts.jumpBy);
            if (null != jumpBy && jumpBy.equals(NoticeConsts.jumpByUpgrade)) {

                // 如果是WIFI网络下，可以升级的应用全部执行升级
                if (NetUtils.isWifi(getActivity())) {

                    // 执行升级
                    UpgradeListManager.getInstance().startAllAfterCheck(getActivity(), "notification", false);

                    // 这里延迟后刷新页面是为了解决部分机型进任务管理页面后看不到新的任务
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showData();
                        }
                    }, 800);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void showOneKeyDownload(final int undownloadCount) {
        mOneKeyDownload.setVisibility(View.VISIBLE);
        mOneKeyDownload.setText(mContext.getString(R.string.one_key_download) + "(" + undownloadCount + ")");
        mOneKeyDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1秒内再次点击一键下载无效
                DCStat.baiduStat(mContext, "onekey_download", "任务管理页面，一键下载");
                if (hasClicked) {
                    performOneKeyDownload();
                    hasClicked = false;
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hasClicked = true;
                        }
                    }, 1000);
                }
            }
        });
    }

    private void performOneKeyDownload() {
        if (!NetUtils.isConnected(mContext)) {
            new DownloadChecker().noNetworkPromp(mContext, new Runnable() {
                @Override
                public void run() {
                    startOneKeyDownload(true);
                }
            });

            return;
        }
        if (NetUtils.isMobileNet(mContext)) {
            new CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？").setPositiveButton(R.string.continue_mobile_download).setPositiveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startOneKeyDownload(false);
                }
            }).setNegativeButton(R.string.order_wifi_download).setNegativeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startOneKeyDownload(true);
                }
            }).create().show();
        } else {
            startOneKeyDownload(false);
        }
    }

    private void startOneKeyDownload(boolean isOrderWifiDownload) {
        List<AppEntity> list = mAdapter.getList();
        for (AppEntity appEntity : list) {
            int state = appEntity.getStatus();
            String event = "request";
            switch (state) {
                case DownloadStatusDef.error:
                case DownloadStatusDef.retry:
                    event = "retry";
                    break;
                case DownloadStatusDef.paused:
                    event = "continue";
                    break;
            }

            // 是否预约wifi下载
            appEntity.setIsOrderWifiDownload(isOrderWifiDownload);

            // 需要标志为非应用自动升级
            appEntity.setIsAppAutoUpgrade(false);
            DownloadTaskManager.getInstance().startAfterCheck(mContext, appEntity, "onekey", event, getPageAlias(), "", "", "onekey_download");
        }
    }

    /**
     * 安装时，apk不存，被执行了重新下载需要页面配合更新
     */
    public void onEventMainThread(ApkNotExistEvent event) {
        requestData();
        updateOneKeyButton();
    }

    private void hideOneKeyDownload() {
        mOneKeyDownload.setVisibility(View.GONE);
    }

    @Override
    public void setPageAlias() {
    }

    public ChangeListener onJumpListener;

    public interface ChangeListener {
        void changeNetType(int itemDownLoadNum);
    }

    public void setOnJumpListener(ChangeListener onJumpListener) {
        this.onJumpListener = onJumpListener;
    }

    public void onEventMainThread(NoticeTaskEvent event) {
        getIntentData();
    }
}
