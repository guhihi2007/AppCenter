package cn.lt.android.network;

import java.lang.reflect.Proxy;

import cn.lt.android.LogTAG;
import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.network.bean.NetWorkConfig;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import retrofit2.Callback;

/**
 * Created by Administrator on 2016/3/1.
 */
public class NetWorkClient {

    private NetWorkConfig config;


    public static NetWorkClient getHttpClient() {
        return new NetWorkClient();
    }

    private NetWorkClient() {
        config = new NetWorkConfig();
    }

    public NetDataInterfaceDao bulid() {
        NetWorkClient.setToken(UserInfoManager.instance().getUserInfo().getToken());
        return (NetDataInterfaceDao) Proxy.newProxyInstance(NetDataInterfaceDao.class.getClassLoader(), new Class<?>[]{NetDataInterfaceDao.class}, new NetWorkProxy(config));
    }


    public boolean isNeedCache() {
        return config.isNeedCache();
    }

    public NetWorkClient setNeedCache(boolean needCache) {
        this.config.setNeedCache(needCache);
        return this;
    }

    public Callback getCallback() {
        return config.getCallback();
    }

    public NetWorkClient setCallback(Callback<?> callback) {
        config.setCallback(callback);
        return this;
    }

    public HostType getHostType() {
        return config.getHostType();
    }

    public NetWorkClient setHostType(HostType hostType) {
        config.setHostType(hostType);
        return this;
    }

    public static void setToken(String token) {
        NetWorkCore.getInstance().setToken(token);
    }

    /**
     * 用户中心相关接口使用，由于泛型会被自动擦除，故使用class做转换
     */
    public NetWorkClient setCls(Class cls) {
        this.config.setCls(cls);
        return this;
    }

    public NetWorkConfig getConfig() {
        return config;
    }
}
