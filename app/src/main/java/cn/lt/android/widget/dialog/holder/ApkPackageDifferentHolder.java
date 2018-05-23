package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * Created by LinJunSheng on 2016/6/23.
 */
public class ApkPackageDifferentHolder extends ADialogHolder {

    private TextView mTitleTV, mLeftTV, mRightTV;
    private View mEmptyView;
    private AppEntity app;
    private Context context;

    @Override
    public void fillData(DataInfo info) {
        app = (AppEntity)info.getmData();
    }

    @Override
    public void setContentView(Dialog dialog) {
        context = dialog.getContext();
        mDialog = dialog;
        dialog.setContentView(R.layout.logout_dialog_layout);
        mView = dialog.findViewById(R.id.rl_logoutView);
        mEmptyView = dialog.findViewById(R.id.empty_view);

        mTitleTV = (TextView) dialog.findViewById(R.id.tv_title_tip);
        mLeftTV = (TextView) dialog.findViewById(R.id.tv_left);
        mRightTV = (TextView) dialog.findViewById(R.id.tv_right);

        mRightTV.setText("重新下载");
        mLeftTV.setText("设置网络");

        mLeftTV.setOnClickListener(this);
        mRightTV.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
        fillText();
    }

    private void fillText() {
        String title1 = "您当前网络不安全，下载的“";
        String appName = app.getName();
        String title2 = "” 应用已被挟持，请切换网络重新下载！";

        SpannableStringBuilder style = new SpannableStringBuilder(title1);
        style.append(appName).append(title2);

        // 设置字体颜色
        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.app_theme_color)),
                title1.length(), title1.length() + appName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTitleTV.setText(style);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                try {
                    DownloadTaskManager.getInstance().remove(app);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DownloadTaskManager.getInstance().startAfterCheck(context, app, "manual", "retry", "", "" , "下载请求的包名与安装的包名不一致", "");

                // 这里延迟是针对配置差的手机，不然页面更新会有异常
                LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new ApkNotExistEvent());
                    }
                }, 500);
                closeDialog();
                break;
            case R.id.tv_left:
                context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                closeDialog();
                break;
           case R.id.empty_view:
//                closeDialog();
                break;

        }
    }
}
