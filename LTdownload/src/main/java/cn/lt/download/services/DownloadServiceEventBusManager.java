package cn.lt.download.services;


import cn.lt.download.event.BaseEventBus;
import cn.lt.download.event.IEvent;

/**
 * Event pool for :filedownloader process
 */
class DownloadServiceEventBusManager extends BaseEventBus {

    private static BaseEventBus bus = new BaseEventBus();

    public static BaseEventBus getEventBus(){
        return bus;
    }
}
