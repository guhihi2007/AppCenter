package cn.lt.android.main.loading;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mobads.SplashAdListener;
import com.qq.e.ads.splash.SplashADListener;

import cn.lt.android.Constant;

import static cn.lt.android.main.loading.AdsAdapter.AdsType.BaiDu;
import static cn.lt.android.main.loading.AdsAdapter.AdsType.GDT;

/**
 * Created by ATian on 2017/10/12.
 */

public class AdsAdapter implements IAdsSuper {
    AdsType type;
    Activity activity;
    IBaseAdsAdapter mBaseAdapter;
    ViewGroup viewGroup;
    View skipView;
    SplashADListener gdtListener;
    SplashAdListener baiduListener;

    public enum AdsType {
        GDT("gdt"), BaiDu("baidu");
        private String type;
         AdsType(String type) {
            this.type = type;
        }
    }

    public AdsAdapter(AdsType type, Activity activity, ViewGroup viewGroup, View view, SplashADListener gdtListener, SplashAdListener baiduListener) {
        this.type = type;
        this.activity = activity;
        if (GDT == type) {
            mBaseAdapter = new IGDTListener();
            this.viewGroup = viewGroup;
            this.skipView = view;
            this.gdtListener = gdtListener;
        } else if (BaiDu == type) {
            mBaseAdapter = new IBaiduListener();
            this.viewGroup = viewGroup;
            this.baiduListener = baiduListener;
        }

    }


    @Override
    public void loadSdk() {
        if (GDT == type) {
            mBaseAdapter.loadGDT(activity, viewGroup, skipView, gdtListener);
        } else if (BaiDu == type) {
            mBaseAdapter.loadBaiDu(activity, viewGroup, baiduListener);
        }

    }
}
