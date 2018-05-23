package cn.lt.android.widget.dialog.holder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.main.personalcenter.TempActivity;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.holder.supers.ADialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.util.PreferencesUtils;


/**
 * @author 王呈勇
 * @version $Rev$
 * @time 2016/5/16 18:34
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class AutoInstallDialogHolder extends ADialogHolder {
    private Button mConfirmBt;
    private Button mCancelBt;
    private Context context;
    private Activity activity;
    private View mEmptyView;

    public AutoInstallDialogHolder(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    private void initView() {
        try {
            mView = mDialog.findViewById(R.id.updateDialog_root);
            mConfirmBt = (Button) mDialog.findViewById(R.id.bt_confirm_update_dialog);
            mCancelBt = (Button) mDialog.findViewById(R.id.bt_cancel_update_dialog);
            mEmptyView = mDialog.findViewById(R.id.empty_view);
            mConfirmBt.setOnClickListener(this);
            mCancelBt.setOnClickListener(this);
            mEmptyView.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fillData(DataInfo info) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击更新按钮
            case R.id.bt_confirm_update_dialog:
                //先进入临时
                Intent intent = new Intent(activity, TempActivity.class);
                intent.putExtra("fourPage", true);
                context.startActivity(intent);
//                goAccessiblity(activity);
                break;
            case R.id.bt_cancel_update_dialog:
                break;
            case R.id.empty_view:
                break;
        }
        closeDialog();
    }

    @Override
    public void setContentView(Dialog dialog) {
        mDialog = dialog;
        mDialog.setContentView(R.layout.layout_dialog_autoinstall);
        initView();
        PreferencesUtils.putLong(dialog.getContext(), SharePreferencesKey.AUTO_INSTALL_TIME, System.currentTimeMillis());
        PreferencesUtils.putBoolean(dialog.getContext(), Constant.AUTO_INSTALL_SHOWED, true);
    }

    public static void goAccessiblity(final Activity context) {
        try {
            //跳转到设置页面
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //弹出引导
                    showSettingGuid(context);
                }
            }, 100);
        } catch (Exception e) {
            //No Activity found to handle Intent
            e.printStackTrace();
        }
    }

    /**
     * 显示引导界面
     *
     * @param context
     */
    private static void showSettingGuid(Activity context) {

        final WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        final View view = context.getLayoutInflater().inflate(R.layout.window_autoinstall_guid, null);
        wm.addView(view, params);

        view.findViewById(R.id.autoinstall_guid_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击我知道了消失
                wm.removeView(view);
            }
        });
    }
}