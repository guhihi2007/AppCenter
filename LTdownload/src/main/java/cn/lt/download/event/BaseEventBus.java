package cn.lt.download.event;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.lt.download.util.FileDownloadLog;

public class BaseEventBus {

    private final ExecutorService threadPool = new ThreadPoolExecutor(3, 30,
            10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private final HashMap<String, LinkedList<IEventListener>> listenersMap = new HashMap<>();

    private final Handler handler;

    public BaseEventBus() {
        handler = new Handler(Looper.getMainLooper());
    }

    public synchronized boolean addListener(final String eventId, final IEventListener listener) {
        FileDownloadLog.v(this, "setListener %s", eventId);
        LinkedList<IEventListener> container = listenersMap.get(eventId);
        if (container == null) {
            container = new LinkedList<IEventListener>();
            listenersMap.put(eventId, container);
        }
        return container.add(listener);
    }


    public synchronized boolean removeListener(final String eventId, final IEventListener listener) {
        FileDownloadLog.v(this, "removeListener %s", eventId);
        final LinkedList<IEventListener> container = listenersMap.get(eventId);
        if (container == null || listener == null) {
            return false;
        }
        return container.remove(listener);
    }

    protected synchronized boolean publish(final IEvent event) {
        FileDownloadLog.v(this, "publish %s", event.toString());
        String eventId = event.getId();
        LinkedList<IEventListener> listeners = listenersMap.get(eventId);
        if (listeners == null) {
            FileDownloadLog.d(this, "No listener for this event %s", eventId);
            return false;
        }
        trigger(listeners, event);
        return true;
    }

    /*
    public synchronized void asyncPublish(final IEvent event, final Looper looper) {
        FileDownloadLog.v(this, "asyncPublish %s", event.getId());
        Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override
            public void run() {
                BaseEventBus.this.publish(event);
            }
        });
    }
    */


    public synchronized void publishByService(final IEvent event) {
        FileDownloadLog.v(this, "publishByService %s", event.getId());



        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                BaseEventBus.this.publish(event);
            }
        });
    }


    public synchronized void publishByAgent(final IEvent event) {
        FileDownloadLog.v(this, "publishByAgent %s", event.getId());



        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                BaseEventBus.this.publish(event);
            }
        });
    }

    /*
    public synchronized void asyncPublishInMain(final IEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                BaseEventBus.this.publish(event);
            }
        });
    }

    */

    private synchronized void trigger(final LinkedList<IEventListener> listeners, final IEvent event) {
        for (IEventListener listener : listeners) {
            if (listener.onEvent(event)) {
                break;
            }
        }

        if (event.callback != null) {
            event.callback.run();
        }
    }

    public synchronized boolean hasListener(final IEvent event) {
        FileDownloadLog.v(this, "hasListener %s", event.getId());
        String eventId = event.getId();
        LinkedList<IEventListener> listeners = listenersMap.get(eventId);
        return listeners != null && listeners.size() > 0;
    }
}
