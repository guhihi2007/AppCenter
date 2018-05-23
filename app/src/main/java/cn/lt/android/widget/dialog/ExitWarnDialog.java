package cn.lt.android.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import cn.lt.android.LTApplication;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatFailureManager;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.appstore.R;


/**
 * Created by LinJunSheng on 2016/5/16. 退出提醒弹框，提醒下载任务
 */
public class ExitWarnDialog extends Dialog {

    private TextView messageTv;
    private CheckBox downloadCb;
    private int downloadTaskCount;

    public ExitWarnDialog(Context context, int downloadTaskCount) {
        super(context, R.style.ShareDialogStyle);
        this.downloadTaskCount = downloadTaskCount;
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

        setContentView(R.layout.dialog_exit_warn2);
        this.setCanceledOnTouchOutside(true);

        messageTv = (TextView) findViewById(R.id.message);

        downloadCb = (CheckBox) findViewById(R.id.exit_warm_cb);
        boolean isCheck = (Boolean) SharePreferenceUtil.getFromSpName(SharePreferenceUtil.DIALOG_WARN_NAME, SharePreferenceUtil.EXIT_DIALOG_REMENBER_DOWNLOAD, true);
        downloadCb.setChecked(isCheck);

        messageTv.setText(String.format(getContext().getResources().getString(R.string.download_exit_tips), downloadTaskCount));

        findViewById(R.id.negative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 取消
                dismiss();
            }
        });

        findViewById(R.id.positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确定
                dismiss();
                boolean isCheck = downloadCb.isChecked();

                // 标记app是否已经退出（用于判断是否再弹发通知栏）
                if (isCheck) {
                    LTApplication.appIsExit = false;
                } else {
                    LTApplication.appIsExit = true;
                    LTNotificationManager.getinstance().cancelAllNotice();
                }

                saveCheckedInSP(isCheck);

                if (!isCheck) {
                    try {
                        DownloadTaskManager.getInstance().pauseAll("auto", "", "", "exit appstore", "");
                    } catch (RemoteException e) {
                        e.printStackTrace();// TODO
                    }
                }
                StatFailureManager.submitFailureData(); //重新上报之前上报失败的数据。
                DCStat.quitAppCenter();//上报退出应用市场
                ActivityManager.self().exitApp();
                LTApplication.appIsStart = false;
            }
        });


        downloadCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveCheckedInSP(isChecked);
            }
        });

        findViewById(R.id.empty_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    private void saveCheckedInSP(boolean isCheck) {
        SharePreferenceUtil.putFromSpName(SharePreferenceUtil.DIALOG_WARN_NAME, SharePreferenceUtil.EXIT_DIALOG_REMENBER_DOWNLOAD, isCheck);
    }

}
