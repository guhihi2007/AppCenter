package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.view.View;

import cn.lt.android.umsharesdk.ShareBean;
import cn.lt.android.widget.ShareView;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/3/11.
 * @desc 分享Dialog
 */
public class ShareHolder extends ADialogHolder {

    private ShareView shareView;
    private ShareBean shareBean;
    private View mEmptyView;

    @Override
    public void fillData(DataInfo info) {
        try {
            shareBean = (ShareBean) info.getmData();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        dialog.setContentView(R.layout.share_dialog_layout);
        initView();
        setData();
    }

    private void initView() {
        mView = mDialog.findViewById(R.id.rl_root);
        mDialog.findViewById(R.id.iv_close_share).setOnClickListener(this);
        mEmptyView  = mDialog.findViewById(R.id.empty_view);
        shareView = (ShareView) mDialog.findViewById(R.id.shareDialog_share_view);
        mEmptyView.setOnClickListener(this);
        shareView.setOnclick(new ShareView.shareViewOnclick() {
            @Override
            public void shareOnClick(View view) {
                closeDialog();
            }
        });
    }

    private void setData() {
        shareView.setActivity(shareBean.getActivity()).
                setShareBean(shareBean);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close_share:
            case R.id.empty_view:
                closeDialog();
                break;

        }
    }

}
