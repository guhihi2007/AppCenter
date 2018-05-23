package cn.lt.android.autoinstall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import cn.lt.android.LTApplication;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.main.personalcenter.AutoInstallLeadActivity;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.AutoInstallDialogHolder;
import cn.lt.framework.util.PreferencesUtils;

/**
 * Created by wenchao on 2015/6/24.
 * 自动安装对外api
 */
public class AutoInstallerContext {

    private long WEEK = 7 * 24 * 60 * 60 * 1000;//单位毫秒

    public static final int STATUS_DISABLE = 0;

    /**
     * 已经开启自动装
     */
    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSE = 2;

    private static Context mContext;

    private boolean needPropUser = true;


    /**
     * 是否开启自动安装功能
     *
     * @return
     */
    public int getAccessibilityStatus() {
        int i = 0;
        try {
            i = Settings.Secure.getInt(mContext.getContentResolver(), "accessibility_enabled");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
//            return STATUS_DISABLE;
        }
        if (i == 0) {
            //乐视手机返回0
            //华为手机返回1
//            return STATUS_CLOSE;
        }

        String string = Settings.Secure.getString(mContext.getContentResolver(), "enabled_accessibility_services");
        if (string == null) {
            return STATUS_CLOSE;
        }

        TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
        simpleStringSplitter.setString(string);
        while (simpleStringSplitter.hasNext()) {
            if (simpleStringSplitter.next().equalsIgnoreCase(mContext.getPackageName() + "/" + AccessibilityService.class.getName())) {
                return STATUS_OPEN;
            }
        }
        return STATUS_CLOSE;
    }

    private static final String TAG = "AutoInstallerContext";

    /**
     * 用户自动装功能开启逻辑
     */
    public void promptUserOpen(Activity context) {
        //山寨机渠道不使用辅助功能自动安装功能
//        if(MyApplication.application.getChannel().contains("szj")){
//            return;
//        }
        if (context == null) return;
        boolean isSystemApp = PackageUtils.isSystemApplication(context);
        //只有在没有系统权限的时候才允许弹自动装框
        if (!isSystemApp) {
            //自动装没有开启就提示用户开启
            int status = getAccessibilityStatus();
            switch (status) {
                case STATUS_DISABLE:
                    LogUtils.d(TAG, "the phone do not support Accessibility Service!");
                    break;
                case STATUS_CLOSE:
//                    if (PopWidowManageUtil.needAutoInstallDialog(context)) {
                        showConfirmDialog(context);
//                    }
                    LogUtils.d(TAG, "Accessibility Service is close,but once prop one");
                    break;
                case STATUS_OPEN:
                    LogUtils.d(TAG, "Accessibility Service is aleardy enabled.");
                    break;
            }
        }

    }

    /**
     * 弹出确认框
     */
    private static void showConfirmDialog(final Activity context) {
        new PublicDialog(context, new AutoInstallDialogHolder(context, context)).showDialog(null);
    }

    public static void goAccessiblity(final Activity context, final boolean state) {
        try {
            //跳转到设置页面
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //弹出引导
                    showSettingGuid(context, state);
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
    private static void showSettingGuid(Activity context, boolean state) {
        Intent intent = new Intent(context, AutoInstallLeadActivity.class);
        intent.putExtra("state", state);
        context.startActivity(intent);
    }

    public void markNeedPropUser() {
        needPropUser = true;
    }

//    private void checkTime() {
//        long lastTime = PreferencesUtils.getLong(mContext, SharePreferencesKey.AUTO_INSTALL_TIME, 0);
//        long currentTime = System.currentTimeMillis();
//        long periodTime = (long) SharePreferenceUtil.get(Constant.AUTO_INSTALL_PERIOD, 1000 * 60 * 60 * 24 * 99999999L);
//        WEEK = periodTime;
//        if (lastTime == 0 || currentTime - lastTime >= WEEK) {
//            markNeedPropUser();
//        } else {
//            needPropUser = false;
//        }
//    }

    private void setLastWarmTime() {
        PreferencesUtils.putLong(mContext, SharePreferencesKey.AUTO_INSTALL_TIME, System.currentTimeMillis());
    }


    private AutoInstallerContext() {
    }

    private static AutoInstallerContext instance;

    public static AutoInstallerContext getInstance() {
        if (instance == null) {
            synchronized (AutoInstallerContext.class) {
                if (instance == null) {
                    instance = new AutoInstallerContext();
                }
            }
        }
        return instance;

    }

    public void init(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

}
