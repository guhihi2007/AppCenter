package cn.lt.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.util.LogUtils;

/**
 * 系统关闭广播接收器
 * Created by LinJunSheng on 2016/6/17.
 */

public class ShutdownReceiver extends BroadcastReceiver {
    private final String TAG = "ShutdownTAG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            LogUtils.i(TAG, "手机要关闭或重启啦~~");
            try {
                DownloadTaskManager.getInstance().pauseAll("auto","","","power off","");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }
}
