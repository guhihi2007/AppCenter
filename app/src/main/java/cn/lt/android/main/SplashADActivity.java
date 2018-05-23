package cn.lt.android.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobads.SplashAdListener;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

import java.util.Locale;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.splash.SplashManager;
import cn.lt.android.main.loading.AdsAdapter;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.PreferencesUtils;
import cn.lt.framework.util.ScreenUtils;

/**
 * Created by yuan on 2017/3/14.
 * * 用于跳转广告的临时页面
 */

public class SplashADActivity extends Activity{

    private ViewGroup container;
    private TextView skipView;
    private static final String SKIP_TEXT = "点击跳过 %d";
    public boolean canJump = false;
    private boolean isFromGameCenterActivity;
    private ImageView rootIv;
    private long time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_demo);
        container = (ViewGroup) this.findViewById(R.id.splash_container);
        skipView = (TextView) findViewById(R.id.skip_view);
        rootIv = (ImageView) findViewById(R.id.app_logo);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rootIv.getLayoutParams();
        int srceenHeight = ScreenUtils.getScreenHeight(this);
        params.height = (int) (srceenHeight * 0.184375);
        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                time = System.currentTimeMillis();
                SplashManager splashManager = new SplashManager(SplashADActivity.this,container,skipView,rootIv,false);
                splashManager.needShowSplash();
                LogUtils.i("AD_DEMO", "onCreate");
            }
        }, 1000);
        LTApplication.isBackGroud = false;

        isFromGameCenterActivity = getIntent().getBooleanExtra("fromGameCenterActivity", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            next();
        }
        canJump = true;
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            LogUtils.i("oooo", "按下了");
            finish();
        }
        return super.onTouchEvent(event);
    }

    public void next() {
        if (canJump) {
            if (isFromGameCenterActivity) {
                finish();
            }
        } else {
            canJump = true;
        }
    }

    /**
     * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
