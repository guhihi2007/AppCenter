package cn.lt.android.autoinstall.category;


import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.autoinstall.InstallerUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;

/**
 * Created by wenchao on 2015/6/24.
 */
public class DefaultInstaller implements IInstaller {
    private final String app_auto_install_next;
    private final String app_auto_install_done;
    private final String app_auto_install_confirm;
    private final String app_auto_instal_continue;
    private final String app_auto_install_install;
//    private final String app_auto_install_confirm_risk; //我已了解风险

    public DefaultInstaller() {
        this.app_auto_install_install = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_install);
        this.app_auto_install_next = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_next);
        this.app_auto_install_done = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_done);
        this.app_auto_instal_continue = AutoInstallerContext.getInstance().getContext().getResources().getString(R.string.app_auto_install_continue_install);
        this.app_auto_install_confirm = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_confirm);
//        this.app_auto_install_confirm_risk = AutoInstallerContext.getInstance().getContext().getResources()
//                .getString(R.string.app_auto_install_confirm_risk);
    }

    @Override
    public String getPackageInstallerName() {
        return "com.android.packageinstaller";
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onInstall(AccessibilityNodeInfo parentNodeInfo, AccessibilityNodeInfo sourceNodeInfo, AccessibilityEvent accessibilityEventc) {
        LogUtils.d("wuyu","普通安装器开点了=：");
// 我已了解风险
//            for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(parentNodeInfo,  //TODO 集合非空判断
//                    this.app_auto_install_confirm_risk)) {
//                InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.app_auto_install_confirm_risk);
//                showAnim();
//            }

        //确认
        for (AccessibilityNodeInfo accessibilityNodeInfo : InstallerUtils.contains(parentNodeInfo, this.app_auto_install_confirm)) {
            InstallerUtils.performOnclick(accessibilityNodeInfo, this.app_auto_install_confirm);
            showAnim();
        }

        //继续安装
        for (AccessibilityNodeInfo accessibilityNodeInfo : InstallerUtils.contains(parentNodeInfo, this.app_auto_instal_continue)) {
            InstallerUtils.performOnclick(accessibilityNodeInfo, this.app_auto_instal_continue);
            showAnim();
        }

        //下一步
        for (AccessibilityNodeInfo accessibilityNodeInfo : InstallerUtils.contains(parentNodeInfo, this.app_auto_install_next)) {
            InstallerUtils.performOnclick(accessibilityNodeInfo, this.app_auto_install_next);
            showAnim();
        }

        //安装
        for (AccessibilityNodeInfo accessibilityNodeInfo : InstallerUtils.contains(parentNodeInfo, this.app_auto_install_install)) {
            InstallerUtils.performOnclick(accessibilityNodeInfo, this.app_auto_install_install);
            showAnim();
        }

        //完成
        for (AccessibilityNodeInfo accessibilityNodeInfo : InstallerUtils.contains(parentNodeInfo, this.app_auto_install_done)) {
            InstallerUtils.performOnclick(accessibilityNodeInfo, this.app_auto_install_done);
            onInstallEnd();
        }
    }

    @Override
    public void onInstallEnd() {
    }

    protected void showAnim() {
        // TODO show anim
    }


}
