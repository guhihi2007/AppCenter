package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/6/5.
 */
public class AwakeEventBean extends BaseEventBean {
    private boolean is_alive;//是否存活

    public boolean isAlive() {
        return is_alive;
    }

    public void setAlive(boolean alive) {
        is_alive = alive;
    }
}
