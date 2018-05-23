package cn.lt.android.network.bean;

import cn.lt.android.network.netdata.bean.HostType;
import retrofit2.Callback;

/**
 * Created by Administrator on 2016/3/3.
 */
public class NetWorkConfig {
    private Callback callback;
    private boolean needCache;
    private HostType HostType;
    private Class cls;

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean isNeedCache() {
        return needCache;
    }

    public void setNeedCache(boolean needCache) {
        this.needCache = needCache;
    }

    public HostType getHostType() {
        return HostType;
    }

    public void setHostType(HostType hostType) {
        HostType = hostType;
    }

    public Class getCls() {
        return cls;
    }

    public void setCls(Class cls) {
        this.cls = cls;
    }
}
