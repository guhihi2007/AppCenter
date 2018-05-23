package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.Constant;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.UIController;
import cn.lt.android.main.download.AppDownloadAdapter;
import cn.lt.android.main.download.DownloadButton;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.StringUtils;
import cn.lt.framework.util.TimeUtils;

/**
 * 2017/2/18 chengyong
 * desc 管理页面专用view
 */
public class ItemManagerAppView extends ItemView implements View.OnClickListener {
    private  AppDownloadAdapter.OnDeleteListener onDeleteListener;
    private RelativeLayout rl_appRoot;
    private ImageView icon;
    private TextView name;
    private TextView appSize;
    private ImageView networkType;
    private TextView downloadSpeed;
    private TextView downloadSurplusTime;
    private View delete;
    private AppEntity mAppEntity;
    private DownloadButton mDownloadButton;
    private RelativeLayout rl_downBtn;
    private int mCurrentNetType;

    public ItemManagerAppView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {

    }

    public ItemManagerAppView(Context context, String pageName, String id,AppDownloadAdapter.OnDeleteListener onDeleteListener) {
        super(context, pageName, id);
        this.onDeleteListener = onDeleteListener;
//        initWeakView();
        init();
    }

    private void initWeakView() {
//        new WeakView<ItemManagerAppView>(this) {
//            @Override
//            public void onEventMainThread(DownloadEvent downloadEvent) {
//                LogUtils.d("yong", "下载中==>" + downloadEvent.soFarBytes);
//                if (mAppEntity == null) return;
//                if(downloadEvent.packageName.equals(mAppEntity.getPackageName())){
////                if (FileDownloadUtils.generateId(mAppEntity.getPackageName(), mAppEntity.getSavePath()) == downloadEvent.downloadId) {
//                    mAppEntity.setTotal(downloadEvent.totalBytes);
//                    mAppEntity.setSoFar(downloadEvent.soFarBytes);
//                    mAppEntity.setStatus(downloadEvent.status);
//                    mDownloadButton.setData(mAppEntity, mPageName);
////                    setSpeedText(networkType, downloadSpeed, downloadSurplusTime, mAppEntity); //更新文本 TODO 似乎速度值变化慢。。。
//                    initNetIcon();
//                }
//                setSpeedText(networkType, downloadSpeed, downloadSurplusTime, mAppEntity);
////                //下载完成、一键下载或安装更新
//                if ((byte)downloadEvent.status != DownloadStatusDef.progress ) {
//                    LogUtils.d("cheng", "不是Progress=>");
//                    onDeleteListener.onRefresh(mAppEntity);
//                }
//            }
//
//
//            @Override
//            public void onEventMainThread(InstallEvent installEvent) {
//                if (mAppEntity == null) return;
//                if (mAppEntity.getPackageName().equals(installEvent.packageName)) {
//                    if (mAppEntity.getStatus() == DownloadStatusDef.completed && installEvent.type == InstallEvent.INSTALL_FAILURE) {
//                        //推荐页里面的bean 内存不足安装失败是改变bean里面的状态
//                        mAppEntity.setStatus(InstallState.install_failure);
//                    } else {
//                        mAppEntity.setStatusByInstallEvent(installEvent.type);
//                    }
//                    mDownloadButton.setData(mAppEntity, mPageName);
//                    if (installEvent.type == InstallEvent.INSTALLED_ADD || installEvent.type==DownloadStatusDef.completed) {
//                        onDeleteListener.onRefresh(mAppEntity);     //安装完成才更新/下载完成让 installFragmnet更新
//                    }
//                }
//            }
//
//            @Override
//            public void onEventMainThread(RemoveEvent removeEvent) {
////                if (mAppEntity == null) return;
////                if (mAppEntity.getPackageName().equals(removeEvent.mAppEntity.getPackageName())) {
////                    //更新界面i
////                    mAppEntity.setStatus(DownloadStatusDef.INVALID_STATUS);
////                    mDownloadButton.setData(mAppEntity, mPageName);
////                }
//            }
//
//            @Override
//            public void onEventMainThread(DownloadSpeedEvent downloadSpeedEvent) {
//                if (mAppEntity == null) return;
//                LogUtils.d("yong", "下载速度  最外围更新了speed==>" + downloadSpeedEvent.speed);
//                if (downloadSpeedEvent.id.equals(mAppEntity.getAppClientId())) {
//                    if (mAppEntity.getStatus() == DownloadStatusDef.progress) {
//                        LogUtils.d("yong", "DownloadSpeedEvent 速度更新了speed==>" + downloadSpeedEvent.speed+"---剩余时间=>" + downloadSpeedEvent.surplus);
//                        mAppEntity.setSurplusTime(downloadSpeedEvent.surplus);
//                        mAppEntity.setSpeed(downloadSpeedEvent.speed);
//                        setSpeedText(networkType, downloadSpeed, downloadSurplusTime, mAppEntity);
//                    }
//                }
//                super.onEventMainThread(downloadSpeedEvent);
//            }
//
//            @Override
//            public void onEventMainThread(NetTypeEvent event) {
//                if (mAppEntity == null) return;
//                LogUtils.d("8887", "Download收到网络改变了" + event.getNetType());
//                if (mCurrentNetType == event.getNetType()) {
//                    return;
//                } else {
//                    mCurrentNetType = event.getNetType();
//                    LogUtils.d("8887", "Download真正改变网络图标" + event.getNetType());
//                }
//                if (event.getNetType() == ConnectivityManager.TYPE_MOBILE) {
//                    mAppEntity.netType = Constant.NET_MOBILE_PHONE;
//                } else if (event.getNetType() == ConnectivityManager.TYPE_WIFI) {
//                    mAppEntity.netType = Constant.NET_WIFI;
//                } else {
//                    mAppEntity.netType = Constant.NO_NET;
//                }
//                refreshNetIcon();
//                super.onEventMainThread(event);
//            }
//        };
    }

    private void initNetIcon() {
        if (NetUtils.isMobileNet(getContext())) {
            mAppEntity.netType = Constant.NET_MOBILE_PHONE;
        } else if (NetUtils.isWifiNet(getContext())) {
            mAppEntity.netType = Constant.NET_WIFI;
        } else {
            mAppEntity.netType = Constant.NO_NET;
        }
    }

    @Override
    public void fillManagerView(BaseBean bean, int position) {
        try {
            LogUtils.i("yong", " manager view 填充视图了");
            mAppEntity = (AppEntity) bean;
            ImageloaderUtil.loadImage(getContext(), mAppEntity.getIconUrl(), icon);
            initNetIcon();
            refreshNetIcon();
            name.setText(TextUtils.isEmpty(mAppEntity.getAlias()) ? mAppEntity.getName() : mAppEntity.getAlias());
            try {
                long packageSize = Long.parseLong(mAppEntity.getPackageSize());
                appSize.setText(IntegratedDataUtil.calculateSizeMB(packageSize));
            } catch (NumberFormatException e) {
                appSize.setText(IntegratedDataUtil.calculateSizeMB(mAppEntity.getTotal()));
            }

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUninstallDialog(mAppEntity);
                }
            });

            setSpeedText(networkType, downloadSpeed, downloadSurplusTime, mAppEntity);
            LogUtils.i("yong", "  ItemManagerview  setData时的状态" + mAppEntity.getStatus());
            mDownloadButton.setData(mAppEntity, mPageName);

            if (mAppEntity.getStatus() == DownloadStatusDef.progress) {
                DownloadTaskManager.getInstance().listenSpeed(mAppEntity);
            } else {
                DownloadTaskManager.getInstance().cancelListenSpeed(mAppEntity);
            }

            rl_appRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.i("yong", "view中进入详情");
                    UIController.goAppDetail(getContext(), mAppEntity.isAdData(), mAppEntity.getAdMold(), String.valueOf(mAppEntity.getAppClientId()), mAppEntity.getPackageName(), mAppEntity.getApps_type(), mPageName, mAppEntity.getCategory(), mAppEntity.getDownloadUrl());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshNetIcon() {
        if (mAppEntity.getStatus() == DownloadStatusDef.error || mAppEntity.getStatus() == InstallState.install_failure) {
            networkType.setVisibility(View.VISIBLE);
            networkType.setImageResource(R.mipmap.icon_failure);
        } else {
            if (mAppEntity.netType == Constant.NET_MOBILE_PHONE) {
                networkType.setVisibility(View.VISIBLE);
                networkType.setImageResource(R.mipmap.ic_traffic);
            } else if (mAppEntity.netType == Constant.NET_WIFI) {
                networkType.setVisibility(View.VISIBLE);
                networkType.setImageResource(R.mipmap.ic_wifi);
            } else if (mAppEntity.netType == Constant.NO_NET) {
                networkType.setVisibility(View.GONE);
            }
        }
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_app_download, this);
        icon = (ImageView) findViewById(R.id.icon);
        name = (TextView) findViewById(R.id.name);
        appSize = (TextView) findViewById(R.id.app_size);
        networkType = (ImageView) findViewById(R.id.network_type);
        downloadSpeed = (TextView) findViewById(R.id.download_speed);
        downloadSurplusTime = (TextView) findViewById(R.id.download_surplus_time);
        delete = findViewById(R.id.delete);
        createDownloadBtn();
        rl_appRoot = (RelativeLayout) findViewById(R.id.rl_appRoot);
    }


    private void setSpeedText(ImageView networkView, TextView speedTextView, TextView surplusTextView, AppEntity mAppEntity) {
        surplusTextView.setVisibility(View.GONE);
        int status = mAppEntity.getStatus();
        if (status == DownloadStatusDef.pending) {
            speedTextView.setText(R.string.waiting);
        } else if (status == DownloadStatusDef.connected) {
            speedTextView.setText("0B/s");
        } else if (status == DownloadStatusDef.paused) {
            if (StringUtils.isEmpty(mAppEntity.getErrMsg())) {
                if (mAppEntity.getIsAppAutoUpgrade()) {
                    networkType.setVisibility(View.GONE);
                    speedTextView.setText(R.string.auto_upgrade_is_pause); //零流量已升级。
                } else if (mAppEntity.getIsOrderWifiDownload()) {
                    networkType.setVisibility(View.GONE);
                    speedTextView.setText(R.string.wait_wifi_download);
                } else {
                    speedTextView.setText(R.string.already_pause);
                }
            } else {
                speedTextView.setText(mAppEntity.getErrMsg());
            }

        } else if (status == DownloadStatusDef.progress) {
            speedTextView.setText(StringUtils.byteToString(mAppEntity.getSpeed()) + "/s");
            surplusTextView.setText(TimeUtils.getSurplusTimeString(mAppEntity.getSurplusTime() * 1000));
            surplusTextView.setVisibility(View.VISIBLE);
        } else if (status == DownloadStatusDef.blockComplete) {
        } else if (status == DownloadStatusDef.completed) {
            if (DownloadTaskManager.getInstance().isFailureByInstall(mAppEntity.getAppClientId())) {
                speedTextView.setText(R.string.install_memory_error);
                networkView.setVisibility(View.VISIBLE);
                networkView.setImageResource(R.mipmap.icon_failure);
            } else if (mAppEntity.getErrorType().intValue() == DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                speedTextView.setText(R.string.install_fail_sign);
                networkView.setVisibility(View.VISIBLE);
                networkView.setImageResource(R.mipmap.icon_failure);
            } else {
                speedTextView.setText(R.string.download_complete_wait_install);
            }
        } else if (status == DownloadStatusDef.error) {
            if (StorageSpaceDetection.getAvailableSize() / (1048 * 1024) < 1) {
                speedTextView.setText(R.string.download_memory_error);
//                StorageSpaceDetection.showEmptyTips(ActivityManager.self().topActivity(), "手机空间不足，无法完成下载，请清理内存释放空间！");
            } else {
                speedTextView.setText(R.string.download_error);
            }
        } else if (status == InstallState.install_failure) {
            if (mAppEntity.getErrorType() == (long) DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                speedTextView.setText(R.string.install_fail_sign);
                networkView.setVisibility(View.VISIBLE);
                networkView.setImageResource(R.mipmap.icon_failure);
            } else {
                speedTextView.setText(R.string.install_memory_error);
            }
        } else if (status == InstallState.installing) {
            speedTextView.setText("安装中");
        }

    }

    void showUninstallDialog(final AppEntity appEntity) {
        new CustomDialog.Builder(getContext()).setMessage("是否删除" + (TextUtils.isEmpty(appEntity.getAlias()) ? appEntity.getName() : appEntity.getAlias()) + "?").setPositiveButton("确定").setPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DownloadTaskManager.getInstance().remove(appEntity);
                    DCStat.delete(getContext(), appEntity, Constant.PAGE_DOWNLOAD.equals(mPageName) ? "del_download" : "del_install");//上报删除操作
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (onDeleteListener != null) {
//                    onDeleteListener.onDelete(appEntity);
                }
            }
        }).setNegativeButton(R.string.cancel).create().show();
    }

    /**
     * 动态添加下载按钮
     */
    private void createDownloadBtn() {
        /* 下载按钮的包裹布局*/
        rl_downBtn = (RelativeLayout) findViewById(R.id.rl_downloadbar);
        /* 下载按钮及参数*/
        mDownloadButton = new DownloadButton(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(DensityUtil.dip2px(getContext(), 60), DensityUtil.dip2px(getContext(), 23));
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mDownloadButton.setLayoutParams(layoutParams);
        rl_downBtn.addView(mDownloadButton);
        // 设置监听
        rl_downBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_downloadbar:
//                LogUtils.d("yong", "点击下载按钮了");
                mDownloadButton.dealClick(v);
                break;
        }
//            case R.id.rl_appRoot:
//                LogUtils.d("yong","点击进入详情了");
////                int pos = (int) v.getTag(R.id.click_date);
////                UIController.goAppDetail(getContext(), mAppDeailBean.isAdData(), mAppDeailBean.getAdMold(), mAppDeailBean.getAppClientId(), mAppDeailBean.getPackage_name(), mAppDeailBean.getApps_type(), mPageName, mAppDeailBean.getCategory(), mAppDeailBean.getDownload_url(), mAppDeailBean.getReportType(), mAppDeailBean.getReportData());
////                if (mfl_number_single_item.getVisibility() == View.VISIBLE) {
////                    StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(mItemData.getmPresentData(), mAppDeailBean.isAdData(), pos, mAppDeailBean.getAppClientId(), mPageName, null);
////                    eventData.setAd_type(mAppDeailBean.getAdMold());
////                    DCStat.clickEvent(eventData);
////                } else {
////                    PresentData presentData = new PresentData();
////                    presentData.setmType(PresentType.app);
////                    presentData.setPos(mAppDeailBean.p1);
////                    StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(presentData, mAppDeailBean.isAdData(), mAppDeailBean.p2, mAppDeailBean.getAppClientId(), mPageName, null);
////                    eventData.setAd_type(mAppDeailBean.getAdMold());
////                    DCStat.clickEvent(eventData);
////                }
//
//                break;
//        }
    }
}
