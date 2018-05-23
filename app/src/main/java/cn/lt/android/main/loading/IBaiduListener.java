package cn.lt.android.main.loading;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;
import com.qq.e.ads.splash.SplashADListener;

import cn.lt.android.Constant;

/**
 * Created by ATian on 2017/10/11.
 */

public class IBaiduListener implements IBaseAdsAdapter {


    @Override
    public void loadGDT(Activity activity, ViewGroup viewGroup, View view, SplashADListener gdtListener) {

    }

    @Override
    public void loadBaiDu(Activity activity, ViewGroup viewGroup, SplashAdListener baiduListener) {
        new SplashAd(activity, viewGroup, baiduListener, Constant.baiduSplashId, true);
    }
}
