package cn.lt.download;

import cn.lt.download.event.BaseEventBus;

/**
 * Created by liangxiaokai on 16/6/2.
 */
public class DownloadEventBusManager {

    private static BaseEventBus bus = new BaseEventBus();

    public static BaseEventBus getEventBus(){
        return bus;
    }

}
