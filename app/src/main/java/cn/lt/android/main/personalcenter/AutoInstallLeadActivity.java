package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;

import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;

/**
 * Created by ltbl on 2016/6/3.
 */
public class AutoInstallLeadActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.window_autoinstall_guid);
        TextView title = (TextView) findViewById(R.id.tv_title);
        TextView right = (TextView) findViewById(R.id.tv_your_right);
        boolean state = getIntent().getBooleanExtra("state", false);
        LogUtils.i("zzz", "自动装状态==" + state);
        title.setText(state == true ? this.getResources().getString(R.string.app_auto_install_open) : this.getResources().getString(R.string.app_auto_install_close));
        right.setText(state == true ? String.format(getString(R.string.app_auto_install_setting_desc), "开启") : String.format(getString(R.string.app_auto_install_setting_desc), "关闭"));
        findViewById(R.id.autoinstall_guid_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoInstallLeadActivity.this.finish();
            }
        });

        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoInstallLeadActivity.this.finish();
            }
        });

        boolean isFromAccessibilityService = getIntent().getBooleanExtra("service", false);
        if (isFromAccessibilityService) {
            Intent intent = new Intent(this, TempActivity.class);
//            intent.putExtra("Auto","tuijian");
            intent.putExtra("autoInstall",true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
           finish();

//            likeBack();
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
    }
