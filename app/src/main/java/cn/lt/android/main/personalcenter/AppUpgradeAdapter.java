package cn.lt.android.main.personalcenter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.Item;
import cn.lt.android.main.UIController;
import cn.lt.android.main.download.DownloadButton;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ViewUtils;
import cn.lt.android.widget.MarqueueTextView;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.framework.util.StringUtils;
import cn.lt.framework.util.TimeUtils;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/3/15.
 * 应用更新 适配器
 */

public class AppUpgradeAdapter extends BaseLoadMoreRecyclerAdapter<Item, RecyclerView.ViewHolder> {
    public static final int TYPE_LABEL = 0;
    public static final int TYPE_APP_UPGRADE = 1;
    public static final int TYPE_APP_IGNORE = 2;
    public static final int TYPE_DIVIDER = 3;
    public static final int TYPE_FIND_IGNORE = 4;
    public static final String CLICK_CHECK_IGNORE = "clickCheckIgnore";
    public static final String CLICK_IGNORE = "clickIgnore";
    private boolean unwind = false;

    public AppUpgradeActivity mAppUpgradeActivity;
    private String mPageName;

    public AppUpgradeAdapter(Context context, AppUpgradeActivity activity, String pageName) {
        super(context);
        this.mPageName = pageName;
        mAppUpgradeActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_LABEL:
                View view1 = LayoutInflater.from(mContext).inflate(R.layout.item_app_upgrade_label, parent, false);
                viewHolder = new LabelViewHolder(view1);
                break;
            case TYPE_APP_UPGRADE:
                View view2 = LayoutInflater.from(mContext).inflate(R.layout.item_app_upgrade_app, parent, false);
                viewHolder = new AppUpgradeViewHolder(view2);
                break;
            case TYPE_APP_IGNORE:
                View view3 = LayoutInflater.from(mContext).inflate(R.layout.item_app_upgrade_app, parent, false);
                viewHolder = new AppUpgradeViewHolder(view3);
                break;
            case TYPE_DIVIDER:
                View view4 = new View(mContext);
                view4.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(mContext, 8)));
                view4.setBackgroundColor(Color.parseColor("#EDF2F4"));
                viewHolder = new DividerViewHolder(view4);
                break;
            case TYPE_FIND_IGNORE:
                View view5 = LayoutInflater.from(mContext).inflate(R.layout.item_app_ignore_app, parent, false);
                viewHolder = new FindViewHolder(view5);
                break;
        }
        return viewHolder;
    }

    @Override
    public int getViewType(int position) {
        return getList().get(position).viewType;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getViewType(position)) {
            case TYPE_LABEL:
                bindLabelView((LabelViewHolder) holder, position);
                break;
            case TYPE_APP_UPGRADE:
                bindAppView((AppUpgradeViewHolder) holder, position, true);
                break;
            case TYPE_APP_IGNORE:
                bindAppView((AppUpgradeViewHolder) holder, position, false);
                break;
            case TYPE_FIND_IGNORE:
                bindFindView((FindViewHolder) holder);
                break;
        }
    }

    private void bindFindView(final FindViewHolder h) {
        h.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prompt = h.tvPrompt.getText().toString();
                String viewIgnore = mContext.getResources().getString(R.string.view_ignored_apps);
                String hideIgnore = mContext.getResources().getString(R.string.hide_ignored_apps);

                if (viewIgnore.equals(prompt)) {
                    h.tvPrompt.setText(R.string.hide_ignored_apps);
                    h.ivArrow.setRotation(180);
                } else if (hideIgnore.equals(prompt)) {
                    h.tvPrompt.setText(R.string.view_ignored_apps);
                    h.ivArrow.setRotation(0);
                }

                EventBus.getDefault().post(CLICK_CHECK_IGNORE);
            }
        });
    }

    private void bindLabelView(LabelViewHolder h, int position) {
        String label = (String) getList().get(position).data;
        h.label.setText(label);
    }


    private void bindAppView(final AppUpgradeViewHolder h, final int position, final boolean ignore) {

        final AppDetailBean appDetailBean = (AppDetailBean) getList().get(position).data;
        int pos = getList().get(position).pos + 1;

        // 如果已经在任务列表里的，应该使用数据库里面的AppEntity，这样数据才能保持一致
        AppEntity appEntity = DownloadTaskManager.getInstance().getAppEntityByPkg(appDetailBean.getPackage_name());
        appEntity = appEntity == null ? appDetailBean.getDownloadAppEntity() : appEntity;

        ImageloaderUtil.loadImage(mContext, appDetailBean.getIcon_url(), h.icon);
        if (TextUtils.isEmpty(appDetailBean.getAlias())) {
            h.name.setText(appDetailBean.getName());
        } else {
            h.name.setText(appDetailBean.getAlias());
        }

        initVersion(appDetailBean, h);

        h.updateTime.setText(cn.lt.android.util.TimeUtils.getDateToString(appDetailBean.getCreated_at()) + "更新");
        long size = 0;
        try {
            size = Long.parseLong(appDetailBean.getPackage_size());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        initAppDesc(appDetailBean, h);


        h.tv_size.setText(IntegratedDataUtil.calculateSizeMB(size));
        if (ignore) {
            h.tv_ignore.setText(R.string.ignore);
        } else {
            h.tv_ignore.setText(R.string.cancel);
        }
        h.tv_ignore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                if (ignore) {
                    try {
                        DownloadTaskManager.getInstance().remove(appDetailBean.getDownloadAppEntity());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return; // TODO
                    }

//                    if (UpgradeListManager.getInstance().getIgnoreAppList().size() == 0) {
//                        EventBus.getDefault().post(CLICK_IGNORE);
//                    }

                    //忽略点击事件
                    ignore(appDetailBean);
                    h.mArrow.setText("展开");
                    h.mArrowIV.setImageResource(R.drawable.ic_arrow_down);
                } else {
                    //取消忽略点击事件
                    cancelIgnore(appDetailBean);
                    h.mArrow.setText("展开");
                    h.mArrowIV.setImageResource(R.drawable.ic_arrow_down);
                }
            }
        });

        if (ignore) {
            appEntity.p1 = 1;
            appEntity.p2 = pos;
        } else {
            appEntity.p1 = 2;
            appEntity.p2 = pos;
        }
        h.downloadButton.setData(appEntity, mPageName);

        LogUtils.i("appEntity======>" + appEntity.getStatus());
        if (appEntity.getStatus() == InstallState.upgrade) {
            h.tv_ignore.setVisibility(View.VISIBLE);
        } else {
            h.tv_ignore.setVisibility(View.GONE);
        }

        if (appEntity.getStatus() == DownloadStatusDef.progress) {
            DownloadTaskManager.getInstance().listenSpeed(appEntity);
        } else {
            DownloadTaskManager.getInstance().cancelListenSpeed(appEntity);
        }
        setSpeedText(h.networkType, h.downloadSpeed, h.downloadSurplusTime, appEntity);

        h.rl_appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            UIController.goAppDetail(mContext, appDetailBean.isAdData(), appDetailBean.getAdMold(), appDetailBean.getAppClientId(), appDetailBean.getPackage_name(), appDetailBean.getApps_type(), mPageName, "", appDetailBean.getDownload_url(),appDetailBean.getReportData());
            }
        });

    }

    private synchronized void initVersion(AppDetailBean appDetailBean, final AppUpgradeViewHolder h) {

        PackageManager pm = mContext.getPackageManager();

        try {
            PackageInfo packageInfo = pm.getPackageInfo(appDetailBean.getPackage_name(),
                    PackageManager.GET_ACTIVITIES);
            String str = packageInfo.versionName + "->" + appDetailBean.getVersion_name();
            if (!TextUtils.isEmpty(h.versionName.getText().toString()) && str.equals(h.versionName.getText().toString())) {
                return;
            }
            Spannable spannable = new SpannableString(str);
            spannable.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.app_theme_color)), packageInfo.versionName.length() + 2, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            h.versionName.setText(spannable);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initAppDesc(final AppDetailBean appDetailBean, final AppUpgradeViewHolder h) {
        String finalString = "";
        String textViewString = "";
        String desc = appDetailBean.getChangelog().trim();
        final String des = desc.replace("<br/>", "").replace("<p>&nbsp;</p>", "").replace("<p>", "").replace("</p>", "<br>").replace("<br>", "").replace("<br />","");
        LogUtils.i("kkk","desdesdesdes = " + des.toString());
        if (!TextUtils.isEmpty(h.mAppDesc.getText().toString())) {
            textViewString = h.mAppDesc.getText().toString();
            int lineCount = h.mAppDesc.getLineCount();
            LogUtils.i("kkk","lineCount = " + lineCount);
            if (lineCount > 2) {
                finalString = Html.fromHtml(desc).toString();
                LogUtils.i("kkk","finalString ============= " + finalString);
            } else {
                finalString = Html.fromHtml(des).toString();
                LogUtils.i("kkk","finalString = " + finalString);
            }

            if (finalString.equals(textViewString)) {
                LogUtils.i("kkk","finalString = " + finalString + "====textViewString" + textViewString);
                return;

            }
        }
        LTApplication.getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                h.mAppDesc.setText(Html.fromHtml(des));
                LogUtils.i("kkk","Html.fromHtml(des) = " + Html.fromHtml(des).toString());
                int lineCount = h.mAppDesc.getLineCount();
                h.mArrow.setVisibility(lineCount > 2 ? View.VISIBLE : View.GONE);
                h.mArrowIV.setVisibility(lineCount > 2 ? View.VISIBLE : View.GONE);
                h.mCharater.setVisibility(TextUtils.isEmpty(appDetailBean.getChangelog().trim()) ? View.GONE : View.VISIBLE);
                h.mView.setVisibility(TextUtils.isEmpty(appDetailBean.getChangelog().trim()) ? View.GONE : View.VISIBLE);
                if (lineCount > 2) {
                    h.mAppDesc.setLines(2);
                    h.mArrow.setText("展开");
                    h.mArrowIV.setImageResource(R.drawable.ic_arrow_down);
                    h.mAppDesc.setClickable(true);
                }else {
                    h.mAppDesc.setClickable(false);
                }
            }
        });

        h.mArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fixArrow(appDetailBean, h);
            }
        });

        h.mArrowIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fixArrow(appDetailBean, h);
            }
        });

        h.mAppDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fixArrow(appDetailBean, h);
            }
        });
    }


    private void fixArrow(AppDetailBean appDetailBean, final AppUpgradeViewHolder h) {
        String desc = appDetailBean.getChangelog().trim();

        String des = desc.replace("<br/>", "").replace("<p>&nbsp;</p>", "").replace("<p>", "").replace("</p>", "<br>").replace("<br>", "").replace("<br />","");
        if (!unwind) {
            h.mArrow.setText("收起");
            h.mAppDesc.setMaxLines(200);
            h.mArrowIV.setImageResource(R.drawable.ic_arrow_up);
            h.mAppDesc.setText(Html.fromHtml(desc));
            unwind = true;
        } else if (unwind) {
            h.mArrow.setText("展开");
            h.mAppDesc.setMaxLines(2);
            h.mArrowIV.setImageResource(R.drawable.ic_arrow_down);
            h.mAppDesc.setText(Html.fromHtml(des));
            unwind = false;
        }
    }

    private void setSpeedText(ImageView networkType, TextView speedTextView, TextView surplusTextView, AppEntity appEntity) {
        networkType.setVisibility(View.VISIBLE);
        speedTextView.setVisibility(View.VISIBLE);
        if (NetUtils.isWifi(mContext)) {
            networkType.setImageResource(R.mipmap.ic_wifi);
        } else if (NetUtils.isMobileNet(mContext)) {
            networkType.setImageResource(R.mipmap.ic_traffic);
        }
        surplusTextView.setVisibility(View.GONE);
        int status = appEntity.getStatus();
        if (status == DownloadStatusDef.pending) {
            speedTextView.setText(R.string.waiting);
        } else if (status == DownloadStatusDef.paused) {
            LogUtils.i("juice_tips","是自动升级吗？"+appEntity.getIsAppAutoUpgrade()+"是预约wifi吗？"+appEntity.getIsOrderWifiDownload());
            if (appEntity.getIsAppAutoUpgrade()) {
                networkType.setVisibility(View.GONE);
                speedTextView.setText(R.string.auto_upgrade_is_pause);
            } else if (appEntity.getIsOrderWifiDownload()) {
                networkType.setVisibility(View.GONE);
                speedTextView.setText(R.string.wait_wifi_download);
            } else {
                speedTextView.setText(R.string.already_pause);
            }

        } else if (status == DownloadStatusDef.progress) {
            speedTextView.setText(StringUtils.byteToString(appEntity.getSpeed()) + "/s");
            surplusTextView.setText(TimeUtils.getSurplusTimeString(appEntity.getSurplusTime() * 1000));
            surplusTextView.setVisibility(View.VISIBLE);
        } else if (status == DownloadStatusDef.completed) {
            if(appEntity.getErrorType() == DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                speedTextView.setText(R.string.install_fail_sign);
                networkType.setVisibility(View.VISIBLE);
                networkType.setImageResource(R.mipmap.icon_failure);
            } else {
                speedTextView.setText(R.string.download_complete_wait_install);
            }
        } else if (status == DownloadStatusDef.error) {
            speedTextView.setText(R.string.download_error);
        } else if (status == InstallState.install_failure) {
            if(appEntity.getErrorType() == DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                speedTextView.setText(R.string.install_fail_sign);
                networkType.setVisibility(View.VISIBLE);
                networkType.setImageResource(R.mipmap.icon_failure);
            } else {
                speedTextView.setText(R.string.install_fail);
                networkType.setVisibility(View.VISIBLE);
                networkType.setImageResource(R.mipmap.icon_failure);
            }
        } else {
            networkType.setVisibility(View.GONE);
            speedTextView.setVisibility(View.GONE);
        }
    }

    private void ignore(AppDetailBean appDetailBean) {
        try {
            UpgradeListManager.getInstance().ignore(appDetailBean);
            mAppUpgradeActivity.setOrRefreshUpdateCount(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelIgnore(AppDetailBean appDetailBean) {
        try {
            UpgradeListManager.getInstance().getUpgradeAppList().add(appDetailBean);
            UpgradeListManager.getInstance().cancelIgnore(appDetailBean);
            mAppUpgradeActivity.setOrRefreshUpdateCount(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AppUpgradeViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rl_appInfo;
        ImageView icon;
        TextView name;
        MarqueueTextView versionName;
        TextView tv_size;
        DownloadButton downloadButton;
        TextView tv_ignore;
        ImageView networkType;
        TextView downloadSpeed;
        TextView downloadSurplusTime;
        RelativeLayout appUpdateDetail;
        RelativeLayout mCharater;
        TextView mArrow;
        ImageView mArrowIV;
        TextView mAppDesc;
        TextView updateTime;
        View mView;


        public AppUpgradeViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            versionName = (MarqueueTextView) itemView.findViewById(R.id.version_name);
            tv_size = (TextView) itemView.findViewById(R.id.size);
            downloadButton = (DownloadButton) itemView.findViewById(R.id.download_button);
            tv_ignore = (TextView) itemView.findViewById(R.id.ignore);
            networkType = (ImageView) itemView.findViewById(R.id.network_type);
            downloadSpeed = (TextView) itemView.findViewById(R.id.download_speed);
            downloadSurplusTime = (TextView) itemView.findViewById(R.id.download_surplus_time);
            appUpdateDetail = (RelativeLayout) itemView.findViewById(R.id.update_app);
            mCharater = (RelativeLayout) itemView.findViewById(R.id.rl_content);
            rl_appInfo = (RelativeLayout) itemView.findViewById(R.id.rl_appInfo);

            mArrow = (TextView) itemView.findViewById(R.id.tv_arrow);
            mArrowIV = (ImageView) itemView.findViewById(R.id.iv_arrow);
            mAppDesc = (TextView) itemView.findViewById(R.id.tv_app_desc);

            updateTime = (TextView) itemView.findViewById(R.id.update_time);
            mView = (View) itemView.findViewById(R.id.view);

        }

    }


    class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView label;

        public LabelViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.label);
        }
    }

    class DividerViewHolder extends RecyclerView.ViewHolder {

        public DividerViewHolder(View itemView) {
            super(itemView);
        }
    }

    class FindViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        TextView tvPrompt;
        ImageView ivArrow;

        public FindViewHolder(View itemView) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.rl_ignore);
            tvPrompt = (TextView) itemView.findViewById(R.id.find_ignore);
            ivArrow = (ImageView) itemView.findViewById(R.id.iv_arrow);
        }
    }

}
