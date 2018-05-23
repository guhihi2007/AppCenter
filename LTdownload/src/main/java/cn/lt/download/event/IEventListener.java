package cn.lt.download.event;

public abstract class IEventListener {
    public IEventListener() {

    }

    public abstract boolean onEvent(IEvent event);

}
