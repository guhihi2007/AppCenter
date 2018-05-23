package cn.lt.download.event;


public class ServiceStatusEvent extends IEvent {
    public final static String ID = "event.service.connect.changed";

    public ServiceStatusEvent(final ConnectStatus status, final Class<?> serviceClass) {
        super(ID);

        this.status = status;
        this.serviceClass = serviceClass;
    }

    private final ConnectStatus status;

    public enum ConnectStatus {
        connected, disconnected
    }

    public ConnectStatus getStatus() {
        return status;
    }


    private final Class<?> serviceClass;

    public boolean isSuchService(final Class<?> serviceClass) {
        return this.serviceClass != null &&
                this.serviceClass.getName().equals(serviceClass.getName());

    }
}
