package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.content.Context;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.lt.android.SharePreferencesKey;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/8/23.
 */
public class AppAutoUpgradeHolder extends ADialogHolder {

    private TextView mTitleTV;
    private Button mLeftTV, mRightTV;
    private View mEmptyView;
    private Context context;

    @Override
    public void fillData(DataInfo info) {
    }

    @Override
    public void setContentView(Dialog dialog) {
        context = dialog.getContext();
        mDialog = dialog;
        dialog.setContentView(R.layout.layout_dialog_autoinstall);
        mEmptyView = dialog.findViewById(R.id.empty_view);

        mTitleTV = (TextView) dialog.findViewById(R.id.tv_content_update_dialog);
        mLeftTV = (Button) dialog.findViewById(R.id.bt_cancel_update_dialog);
        mRightTV = (Button) dialog.findViewById(R.id.bt_confirm_update_dialog);

        mTitleTV.setText("开启零流量升级，Wi-Fi环境下将自动升级应用");
        mLeftTV.setText("取消");
        mRightTV.setText("确定");

        mLeftTV.setOnClickListener(this);
        mRightTV.setOnClickListener(this);
        mEmptyView.setOnClickListener(this);
        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_DIALOG_TIME, System.currentTimeMillis());
        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.AUTO_UPGRADE_IS_DIALOGED, true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_confirm_update_dialog:
                // 保存已经打开自动升级的开关
                SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_OPEN_APP_AUTO_UPGRADE, true);

                // 启动自动升级
                try {
                    DownloadTaskManager.getInstance().autoUpgradeApp(UpgradeListManager.getInstance().getAllUpgradeAppList());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                closeDialog();
                break;
            case R.id.bt_cancel_update_dialog:
                closeDialog();
                break;
            case R.id.empty_view:
                closeDialog();
                break;

        }
    }
}
