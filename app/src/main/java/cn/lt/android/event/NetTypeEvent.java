package cn.lt.android.event;

/**
 * @author Administrator
 * @time 2016/6/4 20:17
 * @de
 */
public class NetTypeEvent {
    private int netType;
    public NetTypeEvent(int netType) {
        this.netType=netType;
    }

    public int getNetType() {
        return netType;
    }
}
