package cn.lt.android.widget.dialog.holder;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.lt.android.Constant;
import cn.lt.android.main.requisite.manger.RequisiteManger;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.plateform.update.PlatUpdateAction;
import cn.lt.android.plateform.update.PlatUpdateService;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.util.PreferencesUtils;

/***
 * Created by dxx on 2016/3/9.
 */
public class UpdateHolder extends ADialogHolder {

    private TextView mVersion;
    private TextView mContent;
    private TextView mDate;
    private Button mConfirmBt;
    private Button mCancelBt;
    private VersionInfo info;
    private Context mContext;
    private TextView mFreeFlow;//免流量
    private View mEmptyView;
    private boolean isIndex;//只有在首页时才允许弹出升级必备

    public UpdateHolder(Context context, boolean isIndex) {
        this.mContext = context;
        this.isIndex = isIndex;
    }

    private void initView() {
        try {
            mView = mDialog.findViewById(R.id.updateDialog_root);
            mEmptyView = mDialog.findViewById(R.id.empty_view);
            mVersion = (TextView) mDialog.findViewById(R.id.tv_verison_update_dialog);
            mContent = (TextView) mDialog.findViewById(R.id.tv_content_update_dialog);
            mDate = (TextView) mDialog.findViewById(R.id.tv_date_update_dialog);
            mConfirmBt = (Button) mDialog.findViewById(R.id.bt_confirm_update_dialog);
            mCancelBt = (Button) mDialog.findViewById(R.id.bt_cancel_update_dialog);
            mFreeFlow = (TextView) mDialog.findViewById(R.id.tv_free_flow);
            if (UpdateUtil.isDowloaded(mDialog.getContext())) {
                mFreeFlow.setVisibility(View.VISIBLE);
                mCancelBt.setText("退出");
                mConfirmBt.setText("安装");
            }
            if (info.isForce()) {
                mCancelBt.setText("退出");
            } else {
                mCancelBt.setText("取消");
            }
            mConfirmBt.setOnClickListener(this);
            mCancelBt.setOnClickListener(this);
            mEmptyView.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fillData(DataInfo info) {
        try {
            if (info != null) {
                this.info = (VersionInfo) info.getmData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        mDialog.setContentView(R.layout.layout_dialog_updateinfo);
        initView();
        fillView();
        StatisticsEventData eventData = new StatisticsEventData();
        eventData.setActionType(ReportEvent.ACTION_PAGEVIEW);
        eventData.setPage(Constant.PAGE_PLATFORM_UPDATE);
        DCStat.pageJumpEvent(eventData);
        UpdateUtil.saveDialogShowThisTime(dialog.getContext(), System.currentTimeMillis());
        PreferencesUtils.putBoolean(dialog.getContext(), Constant.CLIENT_UPDATE_SHOWED, true);
    }

    private void fillView() {
        try {
            if (mVersion != null) {
                mVersion.setText(info.getmUpgradeVersion());
            }
            if (mDate != null) {
                mDate.setText(info.getmReleaseData());
            }
            if (mContent != null) {
                mContent.setText(info.getUpgradeIntroduce());
            }
            if (mContent.getLineCount() > 4) {
                mContent.setMaxLines(4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击更新按钮
            case R.id.bt_confirm_update_dialog:
                if (info.isForce()) {
                    UpdateUtil.savePlateUpdateType(v.getContext(), true);
                } else {
                    UpdateUtil.savePlateUpdateType(v.getContext(), false);
                    new RequisiteManger(mContext).requestData(isIndex);
                    super.closeDialog();
                }
                String btnContent = mConfirmBt.getText().toString();
                if ("确定".equals(btnContent)) {
                    if (!UpdateUtil.isDowloaded(mContext)) {
                        ToastUtils.showToast("正在下载，请稍后！");
                    }
                } else if ("安装".equals(btnContent)) {
                    if (!UpdateUtil.isDowloaded(v.getContext())) {
                        ToastUtils.showToast("客户端升级包不存在，正在重新下载。");
                    }
                }
                PlatUpdateService.startUpdateService(PlatUpdateAction.ACTION_DIALOG_CONFIRM, mContext);
                break;
            case R.id.bt_cancel_update_dialog:
                LTNotificationManager.getinstance().sendPlatformUpgrageNotice(info.getmUpgradeVersion());
                DCStat.baiduStat(mContext, "client_cancel_upgrade", "客户端取消升级事件"); //百度统计
                closeDialog();
                break;
            case R.id.empty_view:
                if (!info.isForce()) {
                    new RequisiteManger(mContext).requestData(isIndex);//非强制更新，如果取消升级框之后再弹推广框
                    super.closeDialog();
                } else {
                }
                break;
        }
    }

    /**
     * 关闭dialog
     */
    public void closeDialog() {
        if (info.isForce()) {
            DCStat.quitAppCenter();//上报退出应用市场
            ActivityManager.self().exitApp();
        } else {
            String r0 = "";
            new RequisiteManger(mContext).requestData(isIndex);//非强制更新，如果取消升级框之后再弹推广框
            super.closeDialog();
        }
    }
}
