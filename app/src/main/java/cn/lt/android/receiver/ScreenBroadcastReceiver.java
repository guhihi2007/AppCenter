package cn.lt.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;

import cn.lt.android.service.ScreenMonitorService;
import cn.lt.android.statistics.StatFailureManager;
import cn.lt.android.util.NetUtils;
import cn.lt.android.entity.SilentTask;
import cn.lt.android.wake.WaKeLog;
import cn.lt.framework.util.FileUtils;

public class ScreenBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        WaKeLog.w("ScreenBroadcastReceiver,亮屏了么：" + action);
        if(NetUtils.isWifi(context) && Intent.ACTION_SCREEN_OFF.equals(action)) {
            StatFailureManager.submitFailureData();
        }

        Intent service = new Intent(context, ScreenMonitorService.class);
        service.setAction(action);
        context.startService(service);

        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            StringBuilder builder = FileUtils.readFile(SilentTask.TASK_PATH, "UTF-8");

            if (builder != null) {
                String jsonStr = builder.toString();
                WaKeLog.w("解锁存储内容：" + jsonStr);
                if (TextUtils.isEmpty(jsonStr)) {
                    FileUtils.deleteFile(SilentTask.TASK_PATH);
                    return;
                }

                Gson gson = new Gson();
                SilentTask silentTask = gson.fromJson(jsonStr, SilentTask.class);
                if (silentTask != null) {
                    silentTask.startActivity();
                    FileUtils.deleteFile(SilentTask.TASK_PATH);
                }
            }
        }
    }


}
