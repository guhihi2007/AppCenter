package cn.lt.android.widget.dialog.holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.TextView;

import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatFailureManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * Created by atian on 2016/3/11.
 *
 * @desc 退出登录Dialog
 */
public class LogoutDialogHolder extends ADialogHolder {
    private String mTitle, mLeftTitle, mRightTitle;
    private TextView mTitleTV, mLeftTV, mRightTV;
    private DialogType type;
    private View mEmptyView;

    public LogoutDialogHolder() {

    }

    @Override
    public void fillData(DataInfo info) {
        try {
            type = (DialogType) info.getmData();
            switch (type) {
                case quit:
                    this.mTitle = "客官要走了吗？记得常来哦。";
                    //                    this.mLeftTitle = "取消";
                    //                    this.mRightTitle = "确定";
                    break;
                case logout:
                    this.mTitle = "确定退出当前账号吗？";
                    //                    this.mLeftTitle = "取消";
                    //                    this.mRightTitle = "确定";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        dialog.setContentView(R.layout.logout_dialog_layout);
        mView = dialog.findViewById(R.id.rl_logoutView);
        mTitleTV = (TextView) dialog.findViewById(R.id.tv_title_tip);
        mLeftTV = (TextView) dialog.findViewById(R.id.tv_left);
        mRightTV = (TextView) dialog.findViewById(R.id.tv_right);
        mEmptyView = dialog.findViewById(R.id.empty_view);
        mLeftTV.setOnClickListener(this);
        mRightTV.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
        fillText();
    }

    private void fillText() {
        mTitleTV.setText(mTitle);
        //        mLeftTV.setText(mLeftTitle);
        //        mRightTV.setText(mRightTitle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                switch (type) {
                    case logout:
                        try {
                            UserInfoManager.instance().userLogout(false);
                            EventBus.getDefault().post(new UserBaseInfo());
                            DCStat.baiduStat(mDialog.getContext(), "user_login_out", "退出登录");//百度上报退出登录事件
                            Activity a = (Activity) ((ContextWrapper) mDialog.getContext()).getBaseContext();
                            closeDialog();
                            a.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtils.d(LogTAG.USER, "应用市场 onClick：logout 异常-" + e.getMessage());
                        }
                        break;
                    case quit:
                        LogUtils.i("zzz", "退出应用");
                        //上报未上报成功的数据
                        StatFailureManager.submitFailureData();
                        closeDialog();
                        DCStat.quitAppCenter();//上报退出应用市场
                        cn.lt.android.util.ActivityManager.self().exitApp();
                        LTApplication.appIsStart = false;
                        break;
                }
                break;
            case R.id.tv_left:
                closeDialog();
                break;
            case R.id.empty_view:
                closeDialog();
                break;

        }
    }

    public enum DialogType {
        quit, logout
    }


    public interface LeftBtnClickListener {
        void OnClick(View view);
    }

    public interface RightBtnClickListener {
        void OnClick(View view);
    }

}
