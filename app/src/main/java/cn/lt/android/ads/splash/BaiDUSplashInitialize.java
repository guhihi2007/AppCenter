package cn.lt.android.ads.splash;

import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.baidu.mobads.SplashAdListener;

import cn.lt.android.main.LoadingActivity;
import cn.lt.android.main.SplashADActivity;
import cn.lt.android.main.loading.AdsAdapter;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;

/**
 * 初始化百度开屏
 * Created by Erosion on 2018/3/28.
 */

public class BaiDUSplashInitialize extends SplashADParent {
    @Override
    protected void showAdView() {
        loadBaiDu();
    }

    /***
     * 加载百度开屏广告
     */
    private void loadBaiDu() {
        LogUtils.i("Erosion", "百度广告点击进入");
        new AdsAdapter(AdsAdapter.AdsType.BaiDu, mActivity, mContainer, null, null, new SplashAdListener() {
            @Override
            public void onAdDismissed() {
                Log.i("RSplashActivity", "onAdDismissed");
                if (isLoad) {
                    ((LoadingActivity)mActivity).jumpWhenCanClick(); // 跳转至您的应用主界面
                } else {
                    ((SplashADActivity)mActivity).next();
                }
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i("RSplashActivity", "onAdFailed");
                DCStat.adReport("noAD", "BaiDu", "splash", "");
                if (isLoad) {
                    ((LoadingActivity)mActivity).jump();
                } else {
                    mActivity.finish();
                }
            }

            @Override
            public void onAdPresent() {
                Log.i("RSplashActivity", "onAdPresent");
                AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(1000);
                if (mContainer != null) {
                    mContainer.startAnimation(animation);
                }

                if (null != appLogo) {
                    appLogo.setVisibility(View.VISIBLE);
                }
                DCStat.adReport("adPresent", "BaiDu", "splash", "");
            }

            @Override
            public void onAdClick() {
                Log.i("RSplashActivity", "onAdClick");
                DCStat.adReport("adClicked", "BaiDu", "splash", "");
                // 设置开屏可接受点击时，该回调可用
            }
        }).loadSdk();
    }
}
