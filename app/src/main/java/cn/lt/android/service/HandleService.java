package cn.lt.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ServiceUtil;

/**
 * 释放内存APP，可调活应用市场后台服务
 * 根据此需求实现
 */
public class HandleService extends Service {


    public static final String TAG = "HandleService";

    private MyBinder mBinder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean isAlive = ServiceUtil.isServiceRunning(this, CoreService.class);
        LogUtils.d(TAG, "is alive =" + isAlive);
        if (!isAlive) {
            LogUtils.d(TAG, "need to wake coreservice");
            DCStat.awake();
//            getApplicationContext().startService(new Intent(getApplicationContext(), CoreService.class));
//            startForeground(0, null);
        } else {
            LogUtils.d(TAG, "need not wake coreservice");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy() executed");
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder {

        public void startDownload() {
            LogUtils.d(TAG, "startDownload() executed");
            // 执行具体的下载任务
        }

    }
}
