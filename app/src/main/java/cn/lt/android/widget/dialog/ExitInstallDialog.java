package cn.lt.android.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.install.InstallManager;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatFailureManager;
import cn.lt.appstore.R;

/**
 * Created by Administrator on 2017/9/18.
 */

public class ExitInstallDialog extends Dialog {
    private String msg;
    private List<AppEntity> appEntities;
    public ExitInstallDialog(@NonNull Context context,String message,List<AppEntity> appEntities) {
//        super(context, R.style.BottomDialogStyle);
        super(context, R.style.ShareDialogStyle);
        this.msg = message;
        this.appEntities = appEntities;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        win.setWindowAnimations(R.style.BottomSheetAnimationStyle);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);

        setContentView(R.layout.exit_install_dialog);
        assignViews();
        initView();
    }

    private View emptyView;
    private LinearLayout llPrompt;
    private TextView message;
    private Button exit;
    private Button installExit;

    private void assignViews() {
        emptyView = findViewById(R.id.empty_view);
        llPrompt = (LinearLayout) findViewById(R.id.ll_prompt);
        message = (TextView) findViewById(R.id.message);
        exit = (Button) findViewById(R.id.exit);
        installExit = (Button) findViewById(R.id.install_exit);
    }

    private void initView() {
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StatFailureManager.submitFailureData();
                dismiss();
                DCStat.quitAppCenter();//上报退出应用市场
                cn.lt.android.util.ActivityManager.self().exitApp();
                LTApplication.appIsStart = false;
            }
        });

        installExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                for (AppEntity appEntity : appEntities) {
                    InstallManager.getInstance().start(appEntity, Constant.QUIT_DIALOG, "", false);
                }
            }
        });
        message.setText(msg);
    }

}
