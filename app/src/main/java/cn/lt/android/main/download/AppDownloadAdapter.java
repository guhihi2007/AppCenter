package cn.lt.android.main.download;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.UIController;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.StringUtils;
import cn.lt.framework.util.TimeUtils;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;

/**
 * Created by wenchao on 2016/3/9.
 * 应用下载适配器
 */
public class AppDownloadAdapter extends BaseLoadMoreRecyclerAdapter<AppEntity, RecyclerView.ViewHolder> {

    private Context mContext;
    private OnDeleteListener onDeleteListener;
    private String mPageName;
//    private ConnectivityManager mConnectivityManager;
//    private NetworkInfo netInfo;
    private AppDownloadViewHolder h;

    public AppDownloadAdapter(Context context, OnDeleteListener onDeleteListener, String pageName) {
        super(context);
        this.onDeleteListener = onDeleteListener;
        mContext = context;
        this.mPageName = pageName;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_app_download, parent, false);
        AppDownloadViewHolder appDownloadViewHolder = new AppDownloadViewHolder(itemView);
        return appDownloadViewHolder;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        h = (AppDownloadViewHolder) holder;

        final AppEntity appEntity = getList().get(position);
        ImageloaderUtil.loadImage(mContext, appEntity.getIconUrl(), h.icon);
        if (appEntity.getStatus() == DownloadStatusDef.error || appEntity.getStatus() == InstallState.install_failure) {
            h.networkType.setVisibility(View.VISIBLE);
            h.networkType.setImageResource(R.mipmap.icon_failure);
        } else {
            if (appEntity.netType == Constant.NET_MOBILE_PHONE) {
                h.networkType.setVisibility(View.VISIBLE);
                h.networkType.setImageResource(R.mipmap.ic_traffic);
            } else if (appEntity.netType == Constant.NET_WIFI) {
                h.networkType.setVisibility(View.VISIBLE);
                h.networkType.setImageResource(R.mipmap.ic_wifi);
            } else if (appEntity.netType == Constant.NO_NET) {
                h.networkType.setVisibility(View.GONE);
            }
        }

        h.name.setText(TextUtils.isEmpty(appEntity.getAlias()) ? appEntity.getName() : appEntity.getAlias());
        try {
            long packageSize = Long.parseLong(appEntity.getPackageSize());
            h.appSize.setText(IntegratedDataUtil.calculateSizeMB(packageSize));
        } catch (NumberFormatException e) {
            h.appSize.setText(IntegratedDataUtil.calculateSizeMB(appEntity.getTotal()));
        }

        h.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUninstallDialog(appEntity);

            }
        });

        setSpeedText(h.networkType, h.downloadSpeed, h.downloadSurplusTime, appEntity);
        appEntity.p1 = 0;
        appEntity.p2 = position + 1;
        h.downloadButton.setData(appEntity, mPageName);

        if (appEntity.getStatus() == DownloadStatusDef.progress) {
            DownloadTaskManager.getInstance().listenSpeed(appEntity);
        } else {
            DownloadTaskManager.getInstance().cancelListenSpeed(appEntity);
        }

        h.rl_appRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIController.goAppDetail(mContext, appEntity.isAdData(), appEntity.getAdMold(), String.valueOf(appEntity.getAppClientId()), appEntity.getPackageName(), appEntity.getApps_type(), mPageName, appEntity.getCategory(), appEntity.getDownloadUrl());
            }
        });

        // 用于扩大下载按钮的区域
        h.rl_downloadbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                h.downloadButton.dealClick(v);
            }
        });
    }

    private void setSpeedText(ImageView networkView, TextView speedTextView, TextView surplusTextView, AppEntity appEntity) {
        surplusTextView.setVisibility(View.GONE);
        int status = appEntity.getStatus();
        if (status == DownloadStatusDef.pending) {
            speedTextView.setText(R.string.waiting);
        } else if (status == DownloadStatusDef.connected) {
            speedTextView.setText("0B/s");
        } else if (status == DownloadStatusDef.paused) {

            if (StringUtils.isEmpty(appEntity.getErrMsg())) {
                if (appEntity.getIsAppAutoUpgrade()) {
                    h.networkType.setVisibility(View.GONE);
                    speedTextView.setText(R.string.auto_upgrade_is_pause); //零流量已升级。
                } else if (appEntity.getIsOrderWifiDownload()) {
                    h.networkType.setVisibility(View.GONE);
                    speedTextView.setText(R.string.wait_wifi_download);
                } else {
                    speedTextView.setText(R.string.already_pause);
                }
            } else {
                speedTextView.setText(appEntity.getErrMsg());
            }

        } else if (status == DownloadStatusDef.progress) {
            speedTextView.setText(StringUtils.byteToString(appEntity.getSpeed()) + "/s");
            surplusTextView.setText(TimeUtils.getSurplusTimeString(appEntity.getSurplusTime() * 1000));
            surplusTextView.setVisibility(View.VISIBLE);
        } else if (status == DownloadStatusDef.blockComplete) {
        } else if (status == DownloadStatusDef.completed) {
            if (DownloadTaskManager.getInstance().isFailureByInstall(appEntity.getAppClientId())) {
                speedTextView.setText(R.string.install_memory_error);
                networkView.setVisibility(View.VISIBLE);
                networkView.setImageResource(R.mipmap.icon_failure);
            } else if (appEntity.getErrorType() == (long) DownloadStatusDef.COMPLETE_SIGN_FAIL) {
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
            if (DownloadTaskManager.getInstance().isFailureByInstall(appEntity.getAppClientId())) {
                speedTextView.setText(R.string.install_memory_error);
            } else if (appEntity.getErrorType() == (long) DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                speedTextView.setText(R.string.install_fail_sign);
                networkView.setVisibility(View.VISIBLE);
                networkView.setImageResource(R.mipmap.icon_failure);
            } else {
                speedTextView.setText(R.string.install_fail);
            }

            networkView.setVisibility(View.VISIBLE);
            networkView.setImageResource(R.mipmap.icon_failure);
        } else if (status == InstallState.installing) {
            speedTextView.setText("安装中");
        }

    }

    void showUninstallDialog(final AppEntity appEntity) {
        new CustomDialog.Builder(mContext).setMessage("是否删除" + (TextUtils.isEmpty(appEntity.getAlias()) ? appEntity.getName() : appEntity.getAlias()) + "?").setPositiveButton("确定").setPositiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DownloadTaskManager.getInstance().remove(appEntity);
                    DCStat.delete(mContext, appEntity, Constant.PAGE_DOWNLOAD.equals(LTApplication.instance.current_page) ? "del_download" : "del_install");//上报删除操作
                } catch (Exception e) {
                    e.printStackTrace();
                    return;// TODO:
                }
                getList().remove(appEntity);
                AppDownloadAdapter.this.notifyDataSetChanged();
                ToastUtils.showToast("删除成功");
                if (onDeleteListener != null) {
                    onDeleteListener.onDelete();
                }
            }
        }).setNegativeButton(R.string.cancel).create().show();
    }

    public class AppDownloadViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_appRoot;
        RelativeLayout rl_downloadbar;
        ImageView icon;
        TextView name;
        TextView appSize;
        ImageView networkType;
        TextView downloadSpeed;
        TextView downloadSurplusTime;
        DownloadButton downloadButton;
        View delete;


        public AppDownloadViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            appSize = (TextView) itemView.findViewById(R.id.app_size);
            networkType = (ImageView) itemView.findViewById(R.id.network_type);
            downloadSpeed = (TextView) itemView.findViewById(R.id.download_speed);
            downloadSurplusTime = (TextView) itemView.findViewById(R.id.download_surplus_time);
            downloadButton = (DownloadButton) itemView.findViewById(R.id.download_button);
            delete = itemView.findViewById(R.id.delete);

            // 用于扩大下载按钮的区域
            rl_downloadbar = (RelativeLayout) itemView.findViewById(R.id.rl_downloadbar);
            rl_appRoot = (RelativeLayout) itemView.findViewById(R.id.rl_appRoot);
        }
    }

    public interface OnDeleteListener {
        void onDelete();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }


}




