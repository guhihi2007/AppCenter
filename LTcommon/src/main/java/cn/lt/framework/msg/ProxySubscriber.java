package cn.lt.framework.msg;

/**
 * Created by wenchao on 2016/1/19.
 */
public interface ProxySubscriber {
    Object getProxiedSubscriber();

    void proxyUnsubscribed();

    ReferenceStrength getReferenceStrength();
}
