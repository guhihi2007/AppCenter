package cn.lt.android.main.download;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.NetTypeEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;


/**
 * Created by wenchao on 2016/2/16.
 * 应用安装fragment
 */
public class AppInstallFragment extends BaseFragment {

    public int mItemCount;
    private Button mBtnOneKeyInstall;
    private AppInstallCallBack appInstallCallBack;
    private int onResumeTimes;
    private boolean hasClicked = true;
    private double mCurrentNetType = 100;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.public_recyclerview_with_loadinglayout, container, false);
            init(mRootView);
        }
        EventBus.getDefault().register(this);
        requestData();
        //一键安装的按钮逻辑
        updateOneKeyButton();
        initNetIcon();
        if (getUserVisibleHint()) {
            setPageAndUploadPageEvent();
        }
        return mRootView;
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

    /**
     * 这个方法才是fragment真正的可见
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogUtils.i("iii", "AppInstallFragment  setUserVisibleHint走了" + isVisibleToUser);
        if (isVisibleToUser && mRootView != null) setPageAndUploadPageEvent();
    }

    /**
     * 设置页面并上报
     */
    private void setPageAndUploadPageEvent() {
        setmPageAlias(Constant.PAGE_INSTALL);
        statEvent();
    }

    //清理内存后，重试状态变成待安装状态
    @Override
    public void onResume() {
        super.onResume();
        onResumeTimes++;
        if (onResumeTimes > 1) {
            List<AppEntity> downloadList = mAdapter.getList();
            if (downloadList == null) return;
            for (AppEntity appEntity : downloadList) {
                //重新检测内存，遍历列表集合，重新赋值状态
                if (StorageSpaceDetection.getAvailableSize() >= Long.valueOf(appEntity.getPackageSize())) {
                    //存数据库-
                    appEntity.setLackofmemory(false);
                    GlobalParams.getAppEntityDao().insertOrReplace(appEntity);
                    appEntity.setStatus(DownloadStatusDef.completed);
                    EventBus.getDefault().post(new InstallEvent(appEntity, DownloadStatusDef.completed, appEntity.getAppClientId()));
                }
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    public void onEventMainThread(RemoveEvent event) {
        if (mAdapter.getList().contains(event.mAppEntity)) {
            mAdapter.getList().remove(event.mAppEntity);
            mAdapter.notifyDataSetChanged();

            updateOneKeyButton();
            updateEmptyIf();
        }
    }

    private LoadingLayout mLoadingLayout;
    private RecyclerView mRecyclerView;

    public AppDownloadAdapter mAdapter;


    private View mEmptyView;

    private void init(View v) {
        mEmptyView = LayoutInflater.from(getContext()).inflate(R.layout.view_empty_app_download, null);
        mEmptyView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页去下载
                UIController.goHomePage(getContext(), MainActivity.PAGE_TAB_RECOMMEND, MainActivity.PAGE_TAB_GAME_SUB_INDEX);
            }
        });
        ((TextView) mEmptyView.findViewById(R.id.text)).setText(R.string.app_install_empty_tips);

        mLoadingLayout = (LoadingLayout) v.findViewById(R.id.loadingLayout);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mBtnOneKeyInstall = (Button) v.findViewById(R.id.one_key_install);

        mLoadingLayout.setEmptyView(mEmptyView);
        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManger);
        mAdapter = new AppDownloadAdapter(getContext(), new AppDownloadAdapter.OnDeleteListener() {
            @Override
            public void onDelete() {
                updateEmptyIf();
            }

        }, getPageAlias());
        mRecyclerView.setAdapter(mAdapter);
//        mAdapter.addHeaderView(createHeaderView());
        mItemCount = mAdapter.getItemCount();
        mLoadingLayout.showLoading();
    }


    private void requestData() {
        List<AppEntity> installList = null;
        installList = DownloadTaskManager.getInstance().getInstallTaskList();
        //Collections.reverse(installList);
        mAdapter.setList(installList);
        mLoadingLayout.showContent();
        updateEmptyIf();
    }

    private void updateEmptyIf() {
        //更新空列表视图,如果为空
        if (mAdapter.getList().size() == 0) {
            mLoadingLayout.showEmpty();
        }
    }

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
        //下载完成刷新安装界面,会新增数据
        if (downloadEvent.status == DownloadStatusDef.completed || downloadEvent.status == DownloadStatusDef.INVALID_STATUS) {
            requestData();
            updateOneKeyButton();
            if (appInstallCallBack != null) {
                appInstallCallBack.onAppCountChanged(mAdapter.getList().size());
                Logger.i("安装个数事实上 " + mAdapter.getList().size());
            }
        }
    }

    /**
     * 通知安装事件更新
     *
     * @param installEvent
     */
    public void onEventMainThread(InstallEvent installEvent) {
        Logger.i("installFragment " + "zoule");
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppEntity appInfo = mAdapter.getList().get(i);

            //改变具体每一个的状态
            if (installEvent.type == InstallEvent.INSTALL_FAILURE && appInfo.getAppClientId().equals(installEvent.id)) {
                appInfo.setStatus(InstallState.install_failure);
                mAdapter.notifyItemChanged(i);
            }

            if (appInfo.getPackageName().equals(installEvent.packageName)) {
                //更新界面i
                appInfo.setStatusByInstallEvent(installEvent.type);
                mAdapter.notifyItemChanged(i);

                //若安装成功则移除出列表
                if (appInfo.getStatus() == InstallState.installed) {
                    mAdapter.getList().remove(appInfo);
                    mAdapter.notifyDataSetChanged();
                    updateOneKeyButton();
//                    if (appInstallCallBack != null) {
//                        appInstallCallBack.onAppCountChanged(mAdapter.getList().size());
//                        Logger.i("安装个数 " +mAdapter.getList().size() );
//                    }
                }
            }
        }
        updateEmptyIf();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private View createHeaderView() {
        View view = new View(getContext());
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(getContext(), 8)));
        return view;
    }

    @Override
    public void setPageAlias() {
    }


    private void updateOneKeyButton() {
        mAdapter.notifyDataSetChanged();
        int undownloadCount = getUndownloadCount(mAdapter.getList());
        if (undownloadCount >= 2) {
            showOneKeyDownload(undownloadCount);
        } else {
            hideOneKeyDownload();
        }
    }

    private int getUndownloadCount(List<AppEntity> downloadList) {
        int count = 0;
        for (AppEntity appEntity : downloadList) {
            //没有下载ing
            if (DownloadStatusDef.completed == appEntity.getStatus()
                    || appEntity.getStatus() == InstallState.install_failure) {
                count++;
            }
        }
        return count;
    }

    private void showOneKeyDownload(int undownloadCount) {
        mBtnOneKeyInstall.setText(String.format(getContext().getString(R.string.one_key_install), undownloadCount));
        oneKeyClickEvent();
    }

    private void oneKeyClickEvent() {
        mBtnOneKeyInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击安装,每次点击后，1s内再点击无效。
                DCStat.baiduStat(mContext, "onekey_install", "任务管理页面，一键安装");
                if (hasClicked) {
                    clickEvent();
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
        mBtnOneKeyInstall.setVisibility(View.VISIBLE);
    }

    private void clickEvent() {
        //这里处理一键安装的逻辑 TODO
        if (StorageSpaceDetection.getAvailableSize() < getAppsTotalSize()) {
            StorageSpaceDetection.showEmptyTips(getActivity(), getString(R.string.memory_install_error));

            boolean autoInstall = GlobalConfig.isAutoInstall();
            boolean isSystemApp = PackageUtils.isSystemApplication(LTApplication.shareApplication());
            boolean isRoot = GlobalConfig.deviceIsRoot;

            //让  安装列表 里面的状态全部变成  重试
            for (AppEntity appEntity : mAdapter.getList()) {
                //内存不足，上报数据

                if (autoInstall) {
                    DCStat.installEvent(appEntity, true, "auto", Constant.PAGE_INSTALL, "", "memoryError", "手机空间不足");
                } else if (isSystemApp) {
                    DCStat.installEvent(appEntity, true, "system", Constant.PAGE_INSTALL, "", "memoryError", "手机空间不足");
                } else if (isRoot) {
                    DCStat.installEvent(appEntity, true, "root", Constant.PAGE_INSTALL, "", "memoryError", "手机空间不足");
                } else {
                    DCStat.installEvent(appEntity, true, "normal", Constant.PAGE_INSTALL, "", "memoryError", "手机空间不足");
                }
                if (appEntity.getStatus() == DownloadStatusDef.completed) {
                    appEntity.setLackofmemory(true);
                    GlobalParams.getAppEntityDao().insertOrReplace(appEntity);
                    appEntity.setStatus(InstallState.install_failure);
                    //让外面页面也变成失败状态
                    EventBus.getDefault().post(new InstallEvent(appEntity, InstallEvent.INSTALL_FAILURE, appEntity.getAppClientId()));
                    mAdapter.notifyDataSetChanged();
                }
            }
            return;
        } else {
            List<AppEntity> tempList = new ArrayList<>();
            tempList.addAll(mAdapter.getList());
            for (AppEntity appEntity : tempList) {

                if (AppUtils.apkIsNotExist(appEntity.getSavePath())) {
                    try {
                        DCStat.installEvent(appEntity, true, "", LTApplication.instance.current_page, "", "packageError", "apk_deleted");//手动上报一条安装请求
                        DownloadTaskManager.getInstance().remove(appEntity);
                        DownloadTaskManager.getInstance().startAfterCheck(getContext(), appEntity, "onekey", "request", getPageAlias(), "", "apk_deleted", "onekey_download");
                        EventBus.getDefault().post(new ApkNotExistEvent());
                        ToastUtils.showToast(appEntity.getName() + " 的安装包不存在，正在为您重新下载");
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.showToast(appEntity.getName() + " 安装包不存在,请删除任务后重新下载");
                    }

                } else {
                    InstallManager.getInstance().start(appEntity, getPageAlias(), "", true);
                }
            }
        }
    }


    /**
     * 改变网络标志图标
     *
     * @param event
     */
    public void onEventMainThread(NetTypeEvent event) {
        LogUtils.d("8887", "install收到网络改变了" + event.getNetType());
        if (mCurrentNetType == event.getNetType()) {
            return;
        } else {
            mCurrentNetType = event.getNetType();
            LogUtils.d("8887", "install真正改变网络图标" + event.getNetType());
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


    private void hideOneKeyDownload() {
        mBtnOneKeyInstall.setVisibility(View.GONE);
    }

    public void setAppInstallCallBack(AppInstallCallBack appInstallCallBack) {
        this.appInstallCallBack = appInstallCallBack;
    }

    private long getAppsTotalSize() {
        long size = 0;
        for (AppEntity appEntity : mAdapter.getList()) {
            size += Long.valueOf(appEntity.getPackageSize());
        }
        return size;
    }

}