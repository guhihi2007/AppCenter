package cn.lt.framework.msg;

/**
 * Created by wenchao on 2016/1/19.
 */
public interface TopicSubscriber<T> {
    void onEvent(String var1,T t);
}
