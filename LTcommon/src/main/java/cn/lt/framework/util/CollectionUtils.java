package cn.lt.framework.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wenchao on 2016/1/19.
 */
public class CollectionUtils {
    public CollectionUtils() {
    }

    public static boolean isEmpty(Collection<? extends Object> c) {
        return c == null || c.size() == 0;
    }

    public static boolean isEmpty(Object[] objs) {
        return objs == null || objs.length == 0;
    }

    public static <T> T get(Collection<T> c, int index) {
        if(isEmpty(c)) {
            return null;
        } else if(index >= 0 && index < c.size()) {
            if(c instanceof List) {
                return (T) ((List)c).get(index);
            } else {
                ArrayList a = new ArrayList(c);
                return (T) a.get(index);
            }
        } else {
            return null;
        }
    }

    public static <T> T first(Collection<T> c) {
        if(isEmpty(c)) {
            return null;
        } else if(c instanceof List) {
            return (T) ((List)c).get(0);
        } else {
            Iterator iter = c.iterator();
            return iter.hasNext()? (T) iter.next() :null;
        }
    }

    public static <T> T last(Collection<T> c) {
        if(isEmpty(c)) {
            return null;
        } else if(c instanceof List) {
            return (T) ((List)c).get(c.size() - 1);
        } else {
            ArrayList a = new ArrayList(c);
            return (T) a.get(a.size() - 1);
        }
    }

    public static <E> Collection<E> diff(Collection<E> l, Collection<E> r) {
        if(!isEmpty(l) && !isEmpty(r)) {
            ArrayList s = new ArrayList(l);
            s.removeAll(r);
            return s;
        } else {
            return l;
        }
    }

    public static <E> Collection<E> diffLeft(Collection<E> l, Collection<E> r) {
        if(!isEmpty(l) && !isEmpty(r)) {
            ArrayList s = new ArrayList(l);
            s.removeAll(r);
            r.removeAll(l);
            return s;
        } else {
            return l;
        }
    }

    public static <E> Collection<E> same(Collection<E> l, Collection<E> r) {
        if(!isEmpty(l) && !isEmpty(r)) {
            ArrayList s = new ArrayList(l);
            s.removeAll(r);
            ArrayList k = new ArrayList(l);
            k.removeAll(s);
            return k;
        } else {
            return null;
        }
    }
}
