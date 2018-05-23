package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by JohnsonLin on 2017/5/7.
 *
 * @desc ROOT检测中弹窗
 */
public class RootCheckingDialogHolder extends ADialogHolder {


    private TextView tv_iKnow;

    @Override
    public void fillData(DataInfo info) {

    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        dialog.setContentView(R.layout.root_checking_layout);
        tv_iKnow = (TextView) dialog.findViewById(R.id.tv_iKnow);
        tv_iKnow.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_iKnow:
                closeDialog();
                break;
        }
    }
}
