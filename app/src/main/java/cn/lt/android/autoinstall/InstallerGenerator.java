package cn.lt.android.autoinstall;


import android.view.accessibility.AccessibilityNodeInfo;

import cn.lt.android.autoinstall.category.DefaultInstaller;
import cn.lt.android.autoinstall.category.FlymeInstaller;
import cn.lt.android.autoinstall.category.HUAWEIInstaller;
import cn.lt.android.autoinstall.category.IInstaller;
import cn.lt.android.autoinstall.category.LenovoInstaller;
import cn.lt.android.autoinstall.category.MIUIInstaller;

/**
 * Created by wenchao on 2015/6/24.
 * 安装生成器
 */
public enum InstallerGenerator {
    /**默认安装*/
    DefaultGenerator(new DefaultInstaller()),
    /**小米安装*/
    MIUIGenerator(new MIUIInstaller()),
    /**联想安装*/
    LenovoGenerator(new LenovoInstaller()),
    /**魅族安装*/
    FlymeGenerator(new FlymeInstaller()),

    HUIWEIGenerator(new HUAWEIInstaller());

    public final IInstaller installer;

    private InstallerGenerator(IInstaller installer){
        this.installer = installer;
    }

    public IInstaller getInstaller(){
        return installer;
    }

    /**
     * 根据NodeInfo 自动选择安装器
     * @param accessibilityNodeInfo
     * @return
     */
    public static InstallerGenerator getGenerator(AccessibilityNodeInfo accessibilityNodeInfo){
        if(accessibilityNodeInfo.getPackageName() == null){
            throw new NullPointerException();
        }else if(InstallerUtils.isMIUI()){
            return MIUIGenerator;
        }else {
            if(InstallerUtils.isFlymeOs()){
                return FlymeGenerator;
            }
            if(accessibilityNodeInfo.getPackageName().equals(LenovoGenerator.getInstaller().getPackageInstallerName())){
                return LenovoGenerator;
            }
            if(InstallerUtils.isHUIWEI()){
                return HUIWEIGenerator;
            }
            return DefaultGenerator;
        }
    }
}
