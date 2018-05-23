package cn.lt.android.main.download;

import android.content.Context;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.android.widget.OrderWifiDownloadClickListener;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import rx.functions.Action1;


/**
 * Created by wenchao on 2016/3/12.
 * 针对app详情页面的下载按钮
 */
public class BigDownloadButton extends DownloadBar {

    private int mState;
    private RelativeLayout rootLayout;

    public BigDownloadButton(Context context) {
        super(context);
    }

    public BigDownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BigDownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ProgressBar mProgress;
    private TextView mText;


    public interface ScrollCallBack {
        void gotoBottom();
    }

    public void setmCallBack(ScrollCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    private ScrollCallBack mCallBack;

    @Override
    public void assignViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_install_button_big, this);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mText = (TextView) findViewById(R.id.text);
        rootLayout = (RelativeLayout)findViewById(R.id.root);
//        rootLayout.setOnClickListener(this);
        dealClick();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_UP == event.getAction()) {
            if (mCallBack != null) {
                mCallBack.gotoBottom();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void showUpgrade() {
        mProgress.setProgress(100);
        mText.setText(R.string.upgrade);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showOpenApp() {
        mProgress.setProgress(100);
        mText.setText(R.string.open);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showDownload() {
        mProgress.setProgress(100);
        mText.setText(R.string.download);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showContinue(long progress, long total) {
        int percent = (int) (progress * 100f / total);
        mProgress.setProgress(percent);
        mText.setText(R.string.continue_);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showWaiting(long progress, long total) {
        int percent = (int) (progress * 100f / total);
        mProgress.setProgress(percent);
        mText.setText(R.string.waiting);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showProgress(long progress, long total) {
        int percent = (int) (progress * 100f / total);
        float percentF = progress * 100f / total;
        mProgress.setMax(100);
        mProgress.setProgress(percent);
        String pro = String.format("%.2f", percentF);
        mText.setText(pro.equals("NaN") ? "0.00%" : pro + "%");
        mText.setTextColor(getResources().getColor(R.color.light_black));
    }

    @Override
    public void showRetry() {
        mProgress.setProgress(100);
        mText.setText(R.string.retry);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showInstall() {
        mProgress.setProgress(100);
        mText.setText(R.string.install);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public void showInstalling() {
        mProgress.setProgress(100);
        mText.setText(R.string.installing);
        mText.setTextColor(getResources().getColor(R.color.white));
    }

    /**
     * 防抖处理
     */
    private void dealClick() {
        RxView.clicks( rootLayout )
                .throttleFirst(500 , TimeUnit.MILLISECONDS )   //0.5秒钟之内只取一个点击事件，防抖操作
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
//                        LogUtils.i("juice", "点击下载按钮了"+System.currentTimeMillis());
                        realClick();
                    }
                }) ;
    }


    private void realClick() {
        int state = 0;
        try {
            state = DownloadTaskManager.getInstance().getState(mAppEntity);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO:
            return;
        }
        switch (state) {
            case DownloadStatusDef.INVALID_STATUS:
            case DownloadStatusDef.error:
            case DownloadStatusDef.paused:
            case InstallState.upgrade: {
                if (!DownloadChecker.getInstance().noNetworkPromp(ActivityManager.self().topActivity(),
                        new Runnable() {
                            @Override
                            public void run() {
                                mAppEntity.setIsOrderWifiDownload(true);
                                DownloadTaskManager.getInstance().start(mAppEntity);
                            }
                        }))
                {

                    // wifi网络下直接下载
                    if (NetUtils.isWifi(getContext())) {
                        // 设置不是预约wifi下载的状态
                        mAppEntity.setIsOrderWifiDownload(false);
                        doClick();
                        return;
                    }

                    // 3G/4G网络需要弹窗确认
                    if (NetUtils.isMobileNet(getContext())) {
                        new CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？")
                                .setPositiveButton(R.string.continue_mobile_download)
                                .setPositiveListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mAppEntity.setIsOrderWifiDownload(false);
                                        doClick();
                                    }
                                })
                                .setNegativeButton(R.string.order_wifi_download)
                                .setNegativeListener(new OrderWifiDownloadClickListener(mAppEntity))
                                .create().show();
                    }
                }
                return;
            }
        }

        doClick();
    }

    /**
     * 自动点击下载
     */
    public void autoPerformClick() {
        if(mAppEntity.getStatus() == DownloadStatusDef.progress) {
            return;
        }

        // 已安装游戏无需判断网络状态可以直接点击打开
        if (AppUtils.isInstalled(mAppEntity.getPackageName())) {
            rootLayout.performClick();
        } else {
            // 非已安装游戏需要判断wifi状态才继续点击
            if(NetUtils.isWifi(mContext)) {
                rootLayout.performClick();
            }
        }



    }

    /**
     * 自动点击下载
     */
    public void autoPerformClickByDeeplink() {
        if(mAppEntity.getStatus() == DownloadStatusDef.progress) {
            return;
        }

        rootLayout.performClick();
    }

}
