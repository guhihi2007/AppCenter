package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/3/11.
 *
 * @desc 加载Dialog
 */
public class LoadingDialogHolder extends ADialogHolder {
    private TextView mLoadingView;
    private DataInfo mDataInfo;

    public LoadingDialogHolder() {

    }

    @Override
    public void fillData(DataInfo info) {
        try {
            if (info != null) {
                this.mDataInfo = info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        dialog.setContentView(R.layout.loading_dialog_layout);
        initView();
        fillView(mDataInfo);

    }

    private void fillView(DataInfo mDataInfo) {
        try {
            String loadText = (String) mDataInfo.getmData();
            mLoadingView.setText(loadText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        ImageView loadingView = (ImageView) mDialog.findViewById(R.id.iv_loading);
        mLoadingView = (TextView) mDialog.findViewById(R.id.tv_submit);
        RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(200);// 小球的旋转速率
        ra.setRepeatCount(Animation.INFINITE);
        ra.setRepeatMode(Animation.RESTART);
        LinearInterpolator lir = new LinearInterpolator();
        ra.setInterpolator(lir);
        loadingView.setAnimation(ra);
    }

    @Override
    public void onClick(View v) {

    }
}
