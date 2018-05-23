package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import cn.lt.android.db.LoginHistoryEntity;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * Created by honaf on 2016/3/11.
 *
 * @desc 删除账号Dialog
 */
public class DeleteAccountDialogHolder extends ADialogHolder {
    private String mTitle, mLeftTitle, mRightTitle;
    private TextView mTitleTV, mLeftTV, mRightTV;
    private LoginHistoryEntity mLoginHistoryEntity;
    private View mEmptyView;

    public DeleteAccountDialogHolder() {

    }

    @Override
    public void fillData(DataInfo info) {
        try {
            mLoginHistoryEntity = (LoginHistoryEntity) info.getmData();
            this.mTitle = "确定删除应用市场账号"+mLoginHistoryEntity.getMobile()+"？";
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
                closeDialog();
                //LoginFragment刷新popwindow的listview
                EventBus.getDefault().post(mLoginHistoryEntity);
                break;
            case R.id.tv_left:
                closeDialog();
                break;
            case R.id.empty_view:
                closeDialog();
                break;

        }
    }


    public interface LeftBtnClickListener {
        void OnClick(View view);
    }

    public interface RightBtnClickListener {
        void OnClick(View view);
    }

}
