package cn.lt.android.autoinstall.category;


import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.autoinstall.InstallerUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;
/**
 * @author chengyong
 * @time 2017/2/6 11:06
 * @des 华为安装器
 */
public class HUAWEIInstaller implements IInstaller {
    private final String app_auto_install_done;  //完成
    private final String app_auto_install_install;
    private final String app_auto_install_next; //下一步
    private final String continue_install;  //继续安装
    private final String app_auto_install_confirm_risk; //我已了解风险
    private final String app_auto_install_delete_pakage; //删除安装包

    public HUAWEIInstaller() {
        this.continue_install = AutoInstallerContext.getInstance().getContext().getResources().getString(R.string.app_auto_install_continue_install);
        this.app_auto_install_done = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_done);
        this.app_auto_install_confirm_risk = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_confirm_risk);
        this.app_auto_install_install = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_install);
        this.app_auto_install_delete_pakage = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_delete_pakage);
        this.app_auto_install_next = AutoInstallerContext.getInstance().getContext().getResources()
                .getString(R.string.app_auto_install_next);
    }

    @Override
    public String getPackageInstallerName() {
        return "com.android.packageinstaller";
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onInstall(AccessibilityNodeInfo accessibilityNodeInfo,
                          AccessibilityNodeInfo accessibilityNodeInfo2, AccessibilityEvent accessibilityEvent) {
        LogUtils.d("wuyu","华为安装器开点了=：");
//        //我已了解风险
            for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(accessibilityNodeInfo,  //TODO 集合非空判断
                    this.app_auto_install_confirm_risk)) {
                InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.app_auto_install_confirm_risk);
                showAnim();
            }
//
//            //继续安装
            for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(accessibilityNodeInfo,  //TODO 集合非空判断
                    this.continue_install)) {
                InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.continue_install);
                showAnim();
            }

            //下一步
        for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(accessibilityNodeInfo,  //TODO 集合非空判断
                this.app_auto_install_next)) {
            InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.app_auto_install_next);
            showAnim();
        }

            //安装
            for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(accessibilityNodeInfo,  //TODO 集合非空判断
                    this.app_auto_install_install)) {
                InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.app_auto_install_install);
                showAnim();
            }

//            //删除  （安装包）
//            for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(accessibilityNodeInfo,  //TODO 集合非空判断
//                    this.app_auto_install_delete_pakage)) {
//                InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.app_auto_install_delete_pakage);
//                showAnim();
//            }

        //完成
        for (AccessibilityNodeInfo mAccessibilityNodeInfo : InstallerUtils.contains(accessibilityNodeInfo,
                this.app_auto_install_done)) {
            InstallerUtils.performOnclick(mAccessibilityNodeInfo, this.app_auto_install_done);
        }
    }

    @Override
    public void onInstallEnd() {
    }

    protected void showAnim() {
        // TODO show anim
    }
}
