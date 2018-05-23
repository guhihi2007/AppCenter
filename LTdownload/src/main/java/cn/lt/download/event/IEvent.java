package cn.lt.download.event;

public abstract class IEvent {
    public Runnable callback = null;
    protected final String id;

    public IEvent(final String id) {
        this.id = id;
    }

    public final String getId() {
        return this.id;
    }

}
