package cn.lt.android.wake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.lt.android.LTApplication;
import cn.lt.android.db.WakeTaskEntity;
import cn.lt.android.entity.SilentTask;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;

/**
 * @author chengyong
 * @time 2017/12/1 11:52
 * @des ${拉活：执行点击通知栏后的逻辑}
 */

public class NotificationClickReceiver extends BroadcastReceiver {

    public static final String WAKE_INTENT = "wake_intent";

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeTaskEntity entity = (WakeTaskEntity) intent.getSerializableExtra(SilentTask.class.getName());
        DCStat.activeEvent(ReportEvent.event_clicked, String.valueOf(entity.getTask_id()), null,entity);

        Intent wakeIntent = intent.getParcelableExtra(WAKE_INTENT);
        if (wakeIntent != null) {
            try {
                wakeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LTApplication.instance.startActivity(wakeIntent);
                DCStat.activeEvent(ReportEvent.event_open, String.valueOf(entity.getTask_id()), null, entity);
            } catch (Exception e) {
                WaKeLog.e("启动<通知栏过来的广播>出了点问题" + e.toString());
            }
        }
    }
}
