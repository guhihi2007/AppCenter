package cn.lt.android.autoinstall;


import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.main.personalcenter.AutoInstallLeadActivity;
import cn.lt.android.util.LogUtils;
import cn.lt.framework.log.Logger;

/**系统Accessibility服务，实现类似豌豆荚自动装功能
 * Created by wenchao on 2015/6/24.
 */
public class AccessibilityService extends android.accessibilityservice.AccessibilityService{


    /**
     * 安装服务监听回调
     */
    public interface IInstallMonitor{
        void onServiceAlive(boolean alive);
    }

    public AccessibilityService(){
        mIAccessibilityServices.add(new DefaultInstallerService());
    }


    private static List<IInstallMonitor>       mInstallMonitors;
    private static List<IAccessibilityService> mIAccessibilityServices;

    static{
        mInstallMonitors = new ArrayList<IInstallMonitor>();
        mIAccessibilityServices = new ArrayList<IAccessibilityService>();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        for(IAccessibilityService accessibilityService:mIAccessibilityServices){
            accessibilityService.onAccessibilityEvent(event,this);
        }
        Logger.i("AccessibilityService onAccessibilityEvent()");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        onServiceAlive(false);
        Logger.i("AccessibilityService onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        onServiceAlive(true);
        goBackToSettingsFromService();
        Logger.i("AccessibilityService onServiceConnected()");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.i("AccessibilityService onDestory()");
        goBackToSettingsFromService();
    }


    private void goBackToSettingsFromService() {
        Intent intent = new Intent(AccessibilityService.this , AutoInstallLeadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("service",true);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        for(IAccessibilityService accessibilityService:mIAccessibilityServices){
            accessibilityService.onInterrupt();
        }
        Logger.i("AccessibilityService onInterrupt()");
    }

    public static void addMonitor(IInstallMonitor monitor){
        if(!mInstallMonitors.contains(monitor)){
            mInstallMonitors.add(monitor);
        }

    }

    public static void removeMonitor(IInstallMonitor monitor){
        if(mInstallMonitors.contains(monitor)){
            mInstallMonitors.remove(monitor);
        }
    }

    private void onServiceAlive(boolean isAlive){
        for(IInstallMonitor monitor : mInstallMonitors){
            monitor.onServiceAlive(isAlive);
            LogUtils.i("Accessibility", "走了");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("Accessibility", "走了");
    }




}
