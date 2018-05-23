package cn.lt.android.ads.splash;

import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import cn.lt.android.main.LoadingActivity;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.SplashADActivity;
import cn.lt.android.main.loading.AdsAdapter;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;

/**
 * 初始化广点通开屏
 * Created by Erosion on 2018/3/28.
 */

public class GDTSplashInitialize extends SplashADParent{
    private SplashAD splashAD;
    private static final String SKIP_TEXT = "点击跳过 %d";

    @Override
    protected void showAdView() {
        loadGDT();
    }

    /***
     * 加载广点通开屏广告
     */
    private void loadGDT() {
        new AdsAdapter(AdsAdapter.AdsType.GDT, mActivity, mContainer, mSkipVew, new SplashADListener() {
            @Override
            public void onADDismissed() {
                LogUtils.i("Erosion", "onADDismissed");

                if (isLoad) {
                    ((LoadingActivity)mActivity).next();
                } else {
                    ((SplashADActivity)mActivity).next();
                }
            }

            @Override
            public void onNoAD(int i) {
                if (isLoad) {
                    LogUtils.i("Erosion", "onNoAD");
                    Intent localIntent = new Intent(mActivity, MainActivity.class);
                    ((LoadingActivity)mActivity).putDeeplinkIntentData(localIntent);
                    ((LoadingActivity)mActivity).goToNextActivity(localIntent);
                } else {
                    mActivity.finish();
                }
                DCStat.adReport("noAD", "GDT", "splash", "");
            }

            @Override
            public void onADPresent() {
                LogUtils.i("Erosion", "onADPresent");
                mSkipVew.setVisibility(View.VISIBLE);
                AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(1000);
                if (mContainer != null) {
                    mContainer.startAnimation(animation);
                }

                if (null != appLogo) {
                    appLogo.setVisibility(View.VISIBLE);
                }
                DCStat.adReport("adPresent", "GDT", "splash", "");
            }

            @Override
            public void onADClicked() {
                LogUtils.i("Erosion", "onADClicked");
                DCStat.adReport("adClicked", "GDT", "splash", "");
            }

            @Override
            public void onADTick(long l) {
                LogUtils.i("Erosion", "onADTick==" + String.format(SKIP_TEXT, Math.round(l / 1000f)));
                mSkipVew.setText(String.format(SKIP_TEXT, Math.round(l / 1000f)));
            }
        }, null).loadSdk();
    }
}
