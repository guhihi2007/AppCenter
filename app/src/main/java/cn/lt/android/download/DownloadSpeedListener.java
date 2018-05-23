package cn.lt.android.download;

import java.util.Timer;
import java.util.TimerTask;

import cn.lt.android.db.AppEntity;
import cn.lt.android.event.DownloadSpeedEvent;
import cn.lt.download.DownloadAgent;
import cn.lt.framework.log.Logger;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/3/11.
 */
public class DownloadSpeedListener extends TimerTask {

    private Timer timer;
    private AppEntity appEntity;

    private long period;

    private long lastSofar;//保存上一个进度
    private long speed;//多少b每秒
    private long lastMs;//保存上个进度的时间进度

    public DownloadSpeedListener(AppEntity appEntity) {
        this(appEntity, 1000);
    }

    public DownloadSpeedListener(AppEntity appEntity, long period) {
        this.appEntity = appEntity;
        timer = new Timer(appEntity.getPackageName());
        this.period = period;
    }

    /**
     * 开始监听
     */
    public void start() {
        timer.schedule(this, 0, period);
    }

    public void stop() {
        timer.cancel();
        speed = 0;
        lastSofar = 0;
        lastMs = 0;
    }

    @Override
    public void run() {
        try {
            long currMs = System.currentTimeMillis();
            long currSofar = DownloadAgent.getImpl().getSofar(DownloadTaskManager.getInstance().getDownloadId(appEntity));
            if (lastMs != 0 && lastSofar != 0) {
                float sec = (currMs - lastMs) / 1000f;
                if (sec == 0) return;//防止Arithmeticexception 异常
                speed = (long) ((currSofar - lastSofar) / sec);
                if (speed == 0)return;
                long surplus = (DownloadTaskManager.getInstance().getTotal(appEntity) - currSofar) / speed;
                EventBus.getDefault().post(new DownloadSpeedEvent(appEntity.getAppClientId(), speed, surplus));
                Logger.i("speed=" + speed + " currSofar=" + currSofar + " lastSofar=" + lastSofar + " sec=" + sec);
            }
            lastSofar = currSofar;
            lastMs = currMs;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
