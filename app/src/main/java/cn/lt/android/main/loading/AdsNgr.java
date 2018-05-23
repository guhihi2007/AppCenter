package cn.lt.android.main.loading;

import android.app.Activity;

import com.qq.e.ads.splash.SplashAD;

/**
 * Created by ATian on 2017/10/11.
 */

public class AdsNgr {
    private Activity mActivity;
    private SplashAD gdtAd;
    private IAdsSuper mListener;

    public AdsNgr(Activity activity, IAdsSuper listener) {
        this.mActivity = activity;
        this.mListener = listener;
    }


}
