package cn.lt.android.main.download;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.fbui.textlayoutbuilder.TextLayoutBuilder;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.widget.CQTextView;
import cn.lt.android.widget.CustomDialog;
import cn.lt.android.widget.OrderWifiDownloadClickListener;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import rx.functions.Action1;

/**
 * Created by wenchao on 2016/1/22.
 * 普通的下载按钮
 */
public class DownloadButton extends DownloadBar {
    private FrameLayout downloadButton;
    private ProgressBar downloadProgressBar;
    private TextView downloadLabel;
    private TextView progressText;
    private FrameLayout progressBarView;
    private ViewStub progressBarStub;
    private static Layout textLayout;
    private FrameLayout fl_cqTextView;


    private DownloadEvent downloadInfo;

    public DownloadButton(Context context) {
        super(context);
    }

    public DownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void assignViews() {
        inflate(getContext(), R.layout.view_install_button, this);
        downloadButton = (FrameLayout) findViewById(R.id.download_click_layout);
        fl_cqTextView = (FrameLayout) findViewById(R.id.fl_cqTextView);
        progressBarStub = (ViewStub) findViewById(R.id.progressBarView);
        dealClick(downloadButton);
        createDownloadTextView();
    }

    /**
     * 创建未下载状态的按钮样式（大部分是用这个）
     */
    private void createDownloadTextView() {
        if (textLayout == null) {
            textLayout = new TextLayoutBuilder().setText("下载").setTextColor(mContext.getResources().getColor(R.color.tool_bar_color)).setTextSize(DensityUtil.dip2px(getContext(), 12f)).build();
        }

        ((CQTextView) findViewById(R.id.cqTextView)).setLayout(textLayout);
    }

    /**
     * 创建非未下载状态的按钮样式
     */
    private void createDownloadLabel() {
        if (downloadLabel == null) {
            downloadLabel = new TextView(mContext);
            downloadLabel.setWidth(DensityUtil.dip2px(getContext(), 60f));
            downloadLabel.setHeight(DensityUtil.dip2px(getContext(), 23f));
            downloadLabel.setGravity(Gravity.CENTER);
            downloadLabel.setTextSize(12);
            downloadButton.addView(downloadLabel);
        }
    }


    @Override
    public void showUpgrade() {
        //升级
        createDownloadLabel();
        downloadLabel.setBackgroundResource(R.drawable.rectangle_blue_corner);
        downloadLabel.setText(R.string.upgrade);
        downloadLabel.setTextColor(getResources().getColor(R.color.tool_bar_color));
        downloadLabel.setVisibility(View.VISIBLE);
        fl_cqTextView.setVisibility(View.GONE);
        hideProgressView();
    }

    @Override
    public void showOpenApp() {
        createDownloadLabel();
        downloadLabel.setBackgroundResource(R.drawable.rectangle_windred_corner);
        downloadLabel.setText(R.string.open);
        downloadLabel.setTextColor(getResources().getColor(R.color.orange));
        downloadLabel.setVisibility(View.VISIBLE);
        fl_cqTextView.setVisibility(View.GONE);
        hideProgressView();
    }

    @Override
    public void showDownload() {
        //未下载，进度为0
        if (downloadLabel != null) {
            downloadLabel.setVisibility(View.GONE);
        }
        fl_cqTextView.setVisibility(View.VISIBLE);
        hideProgressView();
    }

    @Override
    public void showContinue(long progress, long total) {
        //未下载 ，进度不为0
        if (downloadLabel != null) {
            downloadLabel.setVisibility(View.GONE);
        }
        updateProgress(progress, total);
        progressText.setText(R.string.continue_);
        fl_cqTextView.setVisibility(View.GONE);
    }

    @Override
    public void showWaiting(long progress, long total) {
        //队列中的
        createDownloadLabel();
        downloadLabel.setBackgroundResource(R.drawable.rectangle_blue_grey_corner);
        downloadLabel.setText(R.string.waiting);
        downloadLabel.setTextColor(getResources().getColor(R.color.white));
        downloadLabel.setVisibility(View.VISIBLE);
        fl_cqTextView.setVisibility(View.GONE);
        hideProgressView();

    }

    @Override
    public void showProgress(long progress, long total) {
        //正在下载中的
        if (downloadLabel != null) {
            downloadLabel.setVisibility(View.GONE);
        }
        updateProgress(progress, total);
        fl_cqTextView.setVisibility(View.GONE);

    }

    @Override
    public void showRetry() {
        //下载错误
        createDownloadLabel();
        downloadLabel.setBackgroundResource(R.drawable.rectangle_blue_corner);
        downloadLabel.setText(R.string.retry);
        downloadLabel.setTextColor(getResources().getColor(R.color.tool_bar_color));
        downloadLabel.setVisibility(View.VISIBLE);
        hideProgressView();
        fl_cqTextView.setVisibility(View.GONE);

    }

    @Override
    public void showInstall() {
        if (downloadLabel != null) {
            downloadLabel.setVisibility(View.GONE);
        }
        updateProgress(100, 100);
        progressText.setText(R.string.install);
        fl_cqTextView.setVisibility(View.GONE);

    }

    @Override
    public void showInstalling() {
        createDownloadLabel();
        downloadLabel.setBackgroundResource(R.drawable.rectangle_blue_corner);
        downloadLabel.setText(R.string.installing);
        downloadLabel.setTextColor(getResources().getColor(R.color.tool_bar_color));
        downloadLabel.setVisibility(View.VISIBLE);
        hideProgressView();
        fl_cqTextView.setVisibility(View.GONE);

    }


    /**
     * 跟新进度
     */
    private void updateProgress(long progress, long total) {
        showProgressView();
        int percent = (int) (progress * 100f / total);

        // 防止超低概率情况下会显示超过100%
        percent = percent > 100 ? 100 : percent;
        float percentF = progress * 100f / total;
        downloadProgressBar.setMax(100);
        downloadProgressBar.setProgress(percent);
        String pro = String.format("%.2f", percentF);
        progressText.setText(pro.equals("NaN") ? "0.00%" : pro + "%");

    }


    private void showProgressView() {
        if (progressBarView == null) {
            progressBarView = (FrameLayout) progressBarStub.inflate();
            downloadProgressBar = (ProgressBar) progressBarView.findViewById(R.id.download_progress_bar);
            progressText = (TextView) progressBarView.findViewById(R.id.progress_text);
        }
        downloadProgressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

    }

    private void hideProgressView() {
        if (progressBarView != null) {
            downloadProgressBar.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
        }
    }

    /**
     * RxBinding
     *
     * @param v
     */
    public void dealClick(View v) {
        RxView.clicks(v).throttleFirst(500, TimeUnit.MILLISECONDS)   //0.5秒钟之内只取一个点击事件，防抖操作
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
//                        LogUtils.i("juice", "点击下载按钮了"+System.currentTimeMillis());
                        dealOnClick();
                    }
                });
    }


    private void dealOnClick() {
        if (mAppEntity == null) return;
        switch (mAppEntity.getStatus()) {
            case DownloadStatusDef.INVALID_STATUS:
            case DownloadStatusDef.error:
            case DownloadStatusDef.paused:
            case InstallState.upgrade: {
                if (!DownloadChecker.getInstance().noNetworkPromp(ActivityManager.self().topActivity(), new Runnable() {
                    @Override
                    public void run() {
                        mAppEntity.setIsOrderWifiDownload(true);
                        mAppEntity.setIsOrderWifiUpgrade(InstallState.upgrade);
                        DownloadTaskManager.getInstance().start(mAppEntity);
                        LogUtils.i("DCStat", "预约Wifi下载存库");

                        DCStat.downloadRequestReport(mAppEntity, "manual", mAppEntity.getStatus() == DownloadStatusDef.INVALID_STATUS ? "request" : "upgrade", mPageName, mID, "network_change", "book_wifi"); //解决预约Wifi下载没有存库导致的下载请求缺少部分字段(download_type)
                    }
                })) {

                    // wifi网络下直接下载
                    if (NetUtils.isWifi(getContext())) {
                        // 设置不是预约wifi下载的状态
                        mAppEntity.setIsOrderWifiDownload(false);
                        doClick();
                        return;
                    }
                    // 3G/4G网络需要弹窗确认
                    if (NetUtils.isMobileNet(getContext())) {
                        new CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？").setPositiveButton(R.string.continue_mobile_download).setPositiveListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LogUtils.i("kkk", "是否是预约wifi===" + mAppEntity.getIsOrderWifiDownload());
                                if (mAppEntity.getIsOrderWifiDownload()) {
                                    mAppEntity.setOrderWifiContinue(true);
                                } else {
                                    mAppEntity.setOrderWifiContinue(false);
                                }
                                mAppEntity.setIsOrderWifiDownload(false);
                                doClick();
                            }
                        }).setNegativeButton(R.string.order_wifi_download).setNegativeListener(new OrderWifiDownloadClickListener(mAppEntity)).create().show();
                    }
                }
                return;
            }
        }

        doClick();
    }


}
