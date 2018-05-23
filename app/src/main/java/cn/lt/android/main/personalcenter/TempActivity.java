package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import java.io.IOException;

import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;

/**
 * @author chengyong
 * @time 2016/7/4 16:08
 * @des 用于临时跳转--自动装设置自动跳回
 */
public class TempActivity extends Activity {
    private int onResumeTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent);
        boolean state = getIntent().getBooleanExtra("settings", false);
        boolean isFromAccessibilityService = getIntent().getBooleanExtra("autoInstall", false);
        if (!isFromAccessibilityService) {
            AutoInstallerContext.goAccessiblity(this, state);
        } else {
            finish();
        }
    }

    private void likeBack() {
        LogUtils.i("Accessibility", "点返回了");
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            LogUtils.i("Accessibility", "yichangle");
            e.printStackTrace();
        }
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            }
        }).start();*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeTimes++;
        if(onResumeTimes>1){
            finish();
        }
    }
}
