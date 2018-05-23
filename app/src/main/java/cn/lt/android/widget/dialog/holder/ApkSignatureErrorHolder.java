package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import cn.lt.android.db.AppEntity;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/6/23.
 */
public class ApkSignatureErrorHolder extends ADialogHolder {

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

        mRightTV.setText("卸载");
        mLeftTV.setText("取消");

        mLeftTV.setOnClickListener(this);
        mRightTV.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
        fillText();
    }

    private void fillText() {
        String title1 = "您的手机存在签名冲突的同名安装包，请先卸载 ";
        String appName = app.getName();
        String title2 = " ，卸载后我们将为你重新安装。";

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
                PackageUtils.uninstallNormal(context, app.getPackageName());
                InstallManager.getInstance().addSignErrorList(app);
            case R.id.tv_left:
                closeDialog();
                break;
            case R.id.empty_view:
                closeDialog();
                break;

        }
    }
}
