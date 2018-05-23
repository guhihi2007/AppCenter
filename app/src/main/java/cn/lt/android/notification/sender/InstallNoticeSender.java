package cn.lt.android.notification.sender;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;

import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.AppEntityDao;
import cn.lt.android.notification.InstallNotification;
import cn.lt.android.util.LogUtils;

/**
 * Created by LinJunSheng on 2016/3/7.
 */
public class InstallNoticeSender {
    private final AppEntityDao mAppEntityDao;
    private InstallNotification installNotification;
    private ExecutorService mThreadPool;

    public InstallNoticeSender(Context context, ExecutorService mThreadPool) {
        this.mThreadPool = mThreadPool;
        installNotification = new InstallNotification(context);
        mAppEntityDao = GlobalParams.getAppEntityDao();
    }

    public void sendInstallComplete(String packageName) {
        //查找下载库
        List<AppEntity> list = mAppEntityDao.queryBuilder().where(AppEntityDao.Properties.PackageName.eq(packageName)).list();
        if (list.size() == 0){
            LogUtils.i("wqqwqwf", "app是空的");
            return;
        }

        final AppEntity app = list.get(0);

        // 属于应用自动下载的，不发通知
        if(app.getIsAppAutoUpgrade()) {
            return;
        }

        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                completeNotice(app);
                LogUtils.i("wqqwqwf", app.getName()+"安装完成了");
            }
        }, 1000);

    }

    private void completeNotice(final AppEntity app) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                installNotification.cancelAppNotice(app);
                installNotification.installComplete(app);
            }
        });
    }

}

