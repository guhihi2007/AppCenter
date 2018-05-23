package cn.lt.download.listener;


import cn.lt.download.event.ServiceStatusEvent;
import cn.lt.download.event.IEvent;
import cn.lt.download.event.IEventListener;
import cn.lt.download.services.DownloadService;

/**
 */
public abstract class ServiceStatusEventListener extends IEventListener {

    public ServiceStatusEventListener(){
    }

    @Override
    public boolean onEvent(IEvent event) {
        if (event instanceof ServiceStatusEvent) {
            final ServiceStatusEvent connectChangedEvent
                    = (ServiceStatusEvent) event;
            if (connectChangedEvent.isSuchService(DownloadService.class)
                    && connectChangedEvent.getStatus()
                    == ServiceStatusEvent.ConnectStatus.connected) {
                connected();
            } else {
                disconnected();
            }
        }
        return false;
    }

    /**
     * connected file download service
     */
    public abstract void connected();

    /**
     * disconnected file download service
     */
    public abstract void disconnected();

}
