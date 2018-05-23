package cn.lt.android.main.loading;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.SplashAdListener;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import cn.lt.android.Constant;

/**
 * Created by ATian on 2017/10/11.
 */

public class IGDTListener implements IBaseAdsAdapter {


    @Override
    public void loadGDT(Activity activity, ViewGroup container, View skipView, SplashADListener gdtListener) {
        new SplashAD(activity, container, skipView, Constant.APPID, Constant.SplashPosID, gdtListener, 0);
    }

    @Override
    public void loadBaiDu(Activity activity, ViewGroup viewGroup, SplashAdListener baiduListener) {

    }
}
