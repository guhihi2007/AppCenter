package cn.lt.android.install;

import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.SystemClock;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.notification.LTNotificationManager;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import de.greenrobot.event.EventBus;

import static cn.lt.android.LTApplication.loopInstalledList;

/**
 * @author chengyong
 * @time 2016/10/10 21:06
 * @des 检测 安装完成的轮询器
 */
public class InstalledEventLooperDao {
    private static InstalledEventLooperDao instance;

    public static InstalledEventLooperDao getInstance() {
        if (instance == null) {
            synchronized (InstalledEventLooperDao.class) {
                if (instance == null) {
                    instance = new InstalledEventLooperDao();
                }
            }
        }
        return instance;
    }

    /**
     * 移除集合中的Entity
     */
    public void removeLooperEntity(String packageName) {
        //// TODO: 2017/5/9  任务移除问题
        ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().remove(packageName);
    }

    /**
     * 加入安装完成监控的轮询任务
     *
     * @param entity
     */
    public void startInstall(final AppEntity entity) {
        InstalledLooperTask task = new InstalledLooperTask(entity);
        ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().submit(task, entity);
    }

    private class InstalledLooperTask implements Runnable {
        public final AppEntity entity;

        InstalledLooperTask(AppEntity entity) {
            this.entity = entity;
        }

        @Override
        public void run() {
            LogUtils.d("ccc", "加入任务=包名" + Thread.currentThread().getName() + "@" + entity.getPackageName());
            entity.mSetUpTime = System.currentTimeMillis();
            while (true) {
                SystemClock.sleep(1000);
                if (System.currentTimeMillis() - entity.mSetUpTime > 1000 * 60 * 3) {   //移除某个bean的任务
                    ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().remove(this, entity.getPackageName());
                    LogUtils.d("ccc", "安装时间超过3分钟，移除目标" + entity.getPackageName());
                    break;
                }    //保险
                LogUtils.d("ccc", "我在轮询哦" + Thread.currentThread().getName() + "@" + entity.getPackageName());
                //这里获取对象的好处就是,接下来版本对比不用重新去获取本地安装列表(honaf)
                PackageInfo packageInfo = AppUtils.getPackageInfo(entity.getPackageName());
                if (packageInfo != null) {
//                if (AppUtils.isInstalled(entity.getPackageName())) {
                    LogUtils.d("ccc", "已经安装了。哈哈" + entity.getPackageName());
                    //上面已经获取对象,下面就无需再从手机去读取安装列表
                    if (packageInfo.versionName.equals(entity.getVersion_name())) {
//                    if (compareVersionCode(entity)) {
                        LogUtils.d("ccc", "已经安装了。并且版本一样" + entity.getPackageName());
                        postStatuAndRemove(entity.getPackageName());
                        ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().remove(this, entity.getPackageName());
                        LogUtils.d("ccc", "已经安装了。移除任务了。" + entity.getPackageName());
                        break;
                    }
                }
            }
        }
    }

    /**
     * 发送安装完成的通知，并删除包
     *
     * @param packageName
     */
    private void postStatuAndRemove(final String packageName) {
        if (!loopInstalledList.contains(packageName)) {
            loopInstalledList.add(packageName);
        }
        InstallManager.getInstance().removeInstallingApp(packageName);
        // 发送安装完成通知
        DCStat.installSuccessEvent(packageName);
        // 发送应用安装完成的通知（通知栏的）
        LTNotificationManager.getinstance().sendInstallCompleteNotice(packageName);
        // 发送应用可升级的通知（通知栏的）
        LTNotificationManager.getinstance().sendUpgradeNotice();
        LTApplication.getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                UpgradeListManager.getInstance().remove(packageName); //这个要在主线程去操作
            }
        });
        deleteApk(packageName); //里面会发remove事件，保证在别的页面去更新按钮状态，变成打开。
        EventBus.getDefault().post(new InstallEvent(packageName, InstallEvent.INSTALLED_ADD));
        if (InstallManager.getInstance().isSignError(packageName)) {
            InstallManager.getInstance().removeSignErrorList(packageName);
        }
        EventBus.getDefault().post(Constant.CORNER_COUNT);
    }

    /**
     * 对比版本名字或 版本号（升级情况），判断是否发送安装完成的通知。
     * GameBaseDetail entity
     *
     * @return
     */
    private boolean compareVersionCode(AppEntity entity) {
        try {
            List<PackageInfo> packageInfoList = AppUtils.getUserAppList(LTApplication.instance.getApplicationContext());
            for (PackageInfo packageInfo : packageInfoList) {
                if (packageInfo.packageName.equals(entity.getPackageName())) {
                    LogUtils.d("ccc", "包名一致：==>" + entity.getPackageName());
                    LogUtils.d("ccc", "VersionName：==>" + packageInfo.versionName + "/" + entity.getVersion_name());
                    LogUtils.d("ccc", "VersionCode：==>" + packageInfo.versionCode + "/" + entity.getVersion_code());
                    LogUtils.d("ccc", "系统查出的这个包名的版本号==>" + packageInfo.versionName + "====要安装、传进的的这个包名的版本号==>" + entity.getVersion_name());
                    //这里为了防止服务端传过来的versionCode与本地获取的VersionCode一致，去掉versionCode对比
                    if (packageInfo.versionName.equals(entity.getVersion_name())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 安装完成删除apk
     */
    private void deleteApk(String packageName) {
        try {
            LogUtils.d("ccc", "安装完成删除apk");
            DownloadTaskManager.getInstance().remove(packageName, true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
