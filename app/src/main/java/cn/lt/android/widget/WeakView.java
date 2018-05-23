package cn.lt.android.widget;

import android.view.View;

import java.lang.ref.WeakReference;

import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.DownloadSpeedEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.NetTypeEvent;
import cn.lt.android.event.RemoveEvent;
import de.greenrobot.event.EventBus;

/**
 * Created by Administrator on 2016/3/18.
 */
public abstract class WeakView<T extends View> extends WeakReference<T> {

    public WeakView(T r) {
        super(r);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public abstract void onEventMainThread(DownloadEvent updateEvent);

    public abstract void onEventMainThread(InstallEvent installEvent);

    public abstract void onEventMainThread(RemoveEvent removeEvent);

    public  void onEventMainThread(DownloadSpeedEvent downloadSpeedEvent){

    }
    public  void onEventMainThread(NetTypeEvent netTypeEvent){

    }
}