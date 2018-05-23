package cn.lt.framework.msg;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.lt.framework.log.Logger;


/**
 * Created by wenchao on 2016/1/19.
 */
public class MessageCenter {
    private static final String TAG = "MessageCenter";
    private       Map     subscribersByTopic;
    private       Map     subscribersByClass;
    private final Object  listenerLock;
    private       Handler mHandler;

    private MessageCenter() {
        this.subscribersByTopic = new HashMap();
        this.subscribersByClass = new HashMap();
        this.listenerLock = new Object();
        Looper looper = Looper.getMainLooper();
        this.mHandler = new Handler(looper);
    }

    public static MessageCenter defaultCenter() {
        return MessageCenter.CenterInstance._instanceCenter;
    }

    public void setLooper(Looper looper) {
        if (looper == null) {
            looper = Looper.getMainLooper();
        }

        if (this.mHandler.getLooper() != looper) {
            this.mHandler = new Handler(looper);
        }
    }

    public void clearAllSubscribers() {
        Object var1 = this.listenerLock;
        synchronized (this.listenerLock) {
            this.unsubscribeAllInMap(this.subscribersByTopic);
            this.unsubscribeAllInMap(this.subscribersByClass);
        }
    }

    private void unsubscribeAllInMap(Map subscriberMap) {
        Object var2 = this.listenerLock;
        synchronized (this.listenerLock) {
            Set subscriptionKeys = subscriberMap.keySet();
            Iterator var5 = subscriptionKeys.iterator();

            while (var5.hasNext()) {
                Object key = var5.next();
                List subscribers = (List) subscriberMap.get(key);

                while (!subscribers.isEmpty()) {
                    this.unsubscribe(key, subscriberMap, subscribers.get(0));
                }
            }

        }
    }

    protected Object getRealSubscriberAndCleanStaleSubscriberIfNecessary(Iterator iterator, Object existingSubscriber) {
        ProxySubscriber existingProxySubscriber = null;
        if (existingSubscriber instanceof WeakReference) {
            existingSubscriber = ((WeakReference) existingSubscriber).get();
            if (existingSubscriber == null) {
                iterator.remove();
            }
        } else if (existingSubscriber instanceof ProxySubscriber) {
            existingProxySubscriber = (ProxySubscriber) existingSubscriber;
            existingSubscriber = existingProxySubscriber.getProxiedSubscriber();
            if (existingSubscriber != null) {
                this.removeProxySubscriber(existingProxySubscriber, iterator);
            }
        }

        return existingSubscriber;
    }

    protected void removeProxySubscriber(ProxySubscriber proxy, Iterator iter) {
        iter.remove();
        proxy.proxyUnsubscribed();
    }

    protected boolean subscribe(Object classTopicOrPatternWrapper, Map<Object, Object> subscriberMap, Object subscriber) {
        if (classTopicOrPatternWrapper == null) {
            throw new IllegalArgumentException("Can\'t subscribe to null.");
        } else if (subscriber == null) {
            throw new IllegalArgumentException("Can\'t subscribe null subscriber to " + classTopicOrPatternWrapper);
        } else {
            boolean alreadyExists = false;
            Object realSubscriber = subscriber;
            boolean isWeakRef = subscriber instanceof WeakReference;
            if (isWeakRef) {
                realSubscriber = ((WeakReference) subscriber).get();
            }

            boolean isWeakProxySubscriber = false;
            if (subscriber instanceof ProxySubscriber) {
                ProxySubscriber proxySubscriber = (ProxySubscriber) subscriber;
                isWeakProxySubscriber = proxySubscriber.getReferenceStrength() == ReferenceStrength.WEAK;
                if (isWeakProxySubscriber) {
                    realSubscriber = ((ProxySubscriber) subscriber).getProxiedSubscriber();
                }
            }

            if (isWeakRef && isWeakProxySubscriber) {
                throw new IllegalArgumentException("ProxySubscribers should always be subscribed strongly.");
            } else if (realSubscriber == null) {
                return false;
            } else {
                Object proxySubscriber1 = this.listenerLock;
                synchronized (this.listenerLock) {
                    Object currentSubscribers = (List) subscriberMap.get(classTopicOrPatternWrapper);
                    if (currentSubscribers == null) {
                        Logger.d("MessageCenter", "Creating new subscriber map for:" + classTopicOrPatternWrapper, new Object[0]);
                        currentSubscribers = new ArrayList();
                        subscriberMap.put(classTopicOrPatternWrapper, currentSubscribers);
                    } else {
                        Iterator iterator = ((List) currentSubscribers).iterator();

                        while (iterator.hasNext()) {
                            Object currentSubscriber = iterator.next();
                            Object realCurrentSubscriber = this.getRealSubscriberAndCleanStaleSubscriberIfNecessary(iterator, currentSubscriber);
                            if (realSubscriber.equals(realCurrentSubscriber)) {
                                iterator.remove();
                                alreadyExists = true;
                            }
                        }
                    }

                    ((List) currentSubscribers).add(realSubscriber);
                    return !alreadyExists;
                }
            }
        }
    }

    public boolean subscriber(Class eventClass, Subscriber subscriber) {
        if (eventClass == null) {
            throw new IllegalArgumentException("Event class must not be null");
        } else if (subscriber == null) {
            throw new IllegalArgumentException("Event subscriber must not be null");
        } else {
            Logger.d("MessageCenter", "Subscribing by class, class:" + eventClass + ", subscriber:" + subscriber, new Object[0]);

            return this.subscribe(eventClass, this.subscribersByClass, new WeakReference(subscriber));
        }
    }

    public boolean subscribeStrongly(Class cl, Subscriber eh) {
        if (eh == null) {
            throw new IllegalArgumentException("Subscriber cannot be null.");
        } else {
            return this.subscribe(cl, this.subscribersByClass, eh);
        }
    }

    public boolean subscriber(String topic, TopicSubscriber subscriber) {
        if (TextUtils.isEmpty(topic)) {
            throw new IllegalArgumentException("Topic must not be null or empty");
        } else if (subscriber == null) {
            throw new IllegalArgumentException("Event subscriber must not be null");
        } else {
            Logger.d("MessageCenter", "Subscribing by topic, topic:" + topic + ", subscriber:" + subscriber, new Object[0]);

            return this.subscribe(topic, this.subscribersByTopic, new WeakReference(subscriber));
        }
    }

    protected boolean unsubscribe(Object o, Map subscriberMap, Object subscriber) {
        Logger.v("MessageCenter", "unsubscribe(" + o + "," + subscriber + ")", new Object[0]);

        if (o == null) {
            throw new IllegalArgumentException("Can\'t unsubscribe to null.");
        } else if (subscriber == null) {
            throw new IllegalArgumentException("Can\'t unsubscribe null subscriber to " + o);
        } else {
            Object var4 = this.listenerLock;
            synchronized (this.listenerLock) {
                return this.removeFromSetResolveWeakReferences(subscriberMap, o, subscriber);
            }
        }
    }

    private boolean removeFromSetResolveWeakReferences(Map map, Object key, Object toRemove) {
        List subscribers = (List) map.get(key);
        if (subscribers == null) {
            return false;
        } else if (subscribers.remove(toRemove)) {
            boolean var10000 = toRemove instanceof WeakReference;
            if (toRemove instanceof ProxySubscriber) {
                ((ProxySubscriber) toRemove).proxyUnsubscribed();
            }

            return true;
        } else {
            Iterator iter = subscribers.iterator();

            while (iter.hasNext()) {
                Object existingSubscriber = iter.next();
                if (existingSubscriber instanceof ProxySubscriber) {
                    ProxySubscriber wr = (ProxySubscriber) existingSubscriber;
                    existingSubscriber = wr.getProxiedSubscriber();
                    if (existingSubscriber == toRemove) {
                        this.removeProxySubscriber(wr, iter);
                        return true;
                    }
                }

                if (existingSubscriber instanceof WeakReference) {
                    WeakReference wr1 = (WeakReference) existingSubscriber;
                    Object realRef = wr1.get();
                    if (realRef == null) {
                        iter.remove();
                        return true;
                    }

                    if (realRef == toRemove) {
                        iter.remove();
                        return true;
                    }

                    if (realRef instanceof ProxySubscriber) {
                        ProxySubscriber proxy = (ProxySubscriber) realRef;
                        existingSubscriber = proxy.getProxiedSubscriber();
                        if (existingSubscriber == toRemove) {
                            this.removeProxySubscriber(proxy, iter);
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public boolean unsubscribe(Class cl, Subscriber subscriber) {
        return this.unsubscribe(cl, this.subscribersByClass, subscriber);
    }

    public boolean unsubscribe(String topic, TopicSubscriber subscriber) {
        return this.unsubscribe(topic, this.subscribersByTopic, subscriber);
    }

    private List createCopyOfContentsRemoveWeakRefs(Collection subscribersOrVetoListeners) {
        if (subscribersOrVetoListeners == null) {
            return null;
        } else {
            ArrayList copyOfSubscribersOrVetolisteners = new ArrayList(subscribersOrVetoListeners.size());
            Iterator iter = subscribersOrVetoListeners.iterator();

            while (iter.hasNext()) {
                Object elem = iter.next();
                if (elem instanceof ProxySubscriber) {
                    ProxySubscriber hardRef = (ProxySubscriber) elem;
                    elem = hardRef.getProxiedSubscriber();
                    if (elem == null) {
                        this.removeProxySubscriber(hardRef, iter);
                    } else {
                        copyOfSubscribersOrVetolisteners.add(hardRef);
                    }
                } else if (elem instanceof WeakReference) {
                    Object hardRef1 = ((WeakReference) elem).get();
                    if (hardRef1 == null) {
                        iter.remove();
                    } else {
                        copyOfSubscribersOrVetolisteners.add(hardRef1);
                    }
                } else {
                    copyOfSubscribersOrVetolisteners.add(elem);
                }
            }

            return copyOfSubscribersOrVetolisteners;
        }
    }

    public <T> List<T> getSubscribersToClass(Class<T> eventClass) {
        ArrayList result   = null;
        Map       classMap = this.subscribersByClass;
        Set       keys     = classMap.keySet();
        Iterator  iterator = keys.iterator();

        while (iterator.hasNext()) {
            Class cl = (Class) iterator.next();
            if (cl.isAssignableFrom(eventClass)) {
                Collection subscribers = (Collection) classMap.get(cl);
                if (result == null) {
                    result = new ArrayList();
                }

                result.addAll(this.createCopyOfContentsRemoveWeakRefs(subscribers));
            }
        }

        return result;
    }

    public <T> List<T> getSubscribers(Class<T> eventClass) {
        Object var2 = this.listenerLock;
        synchronized (this.listenerLock) {
            return this.getSubscribersToClass(eventClass);
        }
    }

    public void publish(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Cannot publish null event.");
        } else {
            this.mHandler.post(new MessageCenter.PublishRunnable(event, (String) null, (Object) null, this.getSubscribers(event.getClass())));
        }
    }

    private List getSubscribers(Object classOrTopic, Map subscriberMap) {
        List subscribers = (List) subscriberMap.get(classOrTopic);
        List result      = this.createCopyOfContentsRemoveWeakRefs(subscribers);
        return result;
    }

    public <T> List<T> getSubscribersToTopic(String topic) {
        Object var2 = this.listenerLock;
        synchronized (this.listenerLock) {
            return this.getSubscribers(topic, this.subscribersByTopic);
        }
    }

    public void publish(String topicName, Object eventObj) {
        this.mHandler.post(new MessageCenter.PublishRunnable((Object) null, topicName, eventObj, this.getSubscribersToTopic(topicName)));
    }

    protected void publish(Object event, String topic, Object eventObj, List subscribers) {
        if (event == null && topic == null) {
            throw new IllegalArgumentException("Can\'t publish to null topic/event.");
        } else if (subscribers != null && !subscribers.isEmpty()) {
            for (int i = 0; i < subscribers.size(); ++i) {
                Object eh = subscribers.get(i);
                if (event != null) {
                    Subscriber eventTopicSubscriber = (Subscriber) eh;

                    try {
                        eventTopicSubscriber.onEvent(event);
                    } catch (Throwable var9) {
                        ;
                    }
                } else {
                    TopicSubscriber var11 = (TopicSubscriber) eh;

                    try {
                        var11.onEvent(topic, eventObj);
                    } catch (Throwable var10) {
                        ;
                    }
                }
            }

        } else {
            Logger.d("MessageCenter", "No subscribers for event or topic. Event:" + event + ", Topic:" + topic, new Object[0]);

        }
    }

    private static class CenterInstance {
        private static MessageCenter _instanceCenter = new MessageCenter();

        private CenterInstance() {
        }
    }

    class PublishRunnable implements Runnable {
        Object theEvent;
        String theTopic;
        Object theEventObject;
        List   theSubscribers;

        public PublishRunnable(Object event, String topic, Object eventObj, List subscribers) {
            this.theEvent = event;
            this.theTopic = topic;
            this.theEventObject = eventObj;
            this.theSubscribers = subscribers;
        }

        public void run() {
            MessageCenter.this.publish(this.theEvent, this.theTopic, this.theEventObject, this.theSubscribers);
        }
    }
}
