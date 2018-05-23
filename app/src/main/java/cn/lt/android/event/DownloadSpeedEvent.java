package cn.lt.android.event;

/**
 * Created by wenchao on 2016/3/11.
 * 下载速度事件
 */
public class DownloadSpeedEvent {
    public long speed;//单位b/s
    public String id;
    public long surplus;//单位s

    public DownloadSpeedEvent(String id,long speed,long surplus) {
        this.speed = speed;
        this.id = id;
        this.surplus = surplus;
    }
}
