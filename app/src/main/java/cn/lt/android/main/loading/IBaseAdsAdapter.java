package cn.lt.android.main.loading;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.SplashAdListener;
import com.qq.e.ads.splash.SplashADListener;

/**
 * Created by ATian on 2017/10/12.
 */

public interface IBaseAdsAdapter {
    void loadGDT(Activity activity, ViewGroup viewGroup, View view, SplashADListener gdtListener);

    void loadBaiDu(Activity activity, ViewGroup viewGroup, SplashAdListener baiduListener);
}
