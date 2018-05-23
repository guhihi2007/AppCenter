package cn.lt.android.install;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author chengyong
 * @time 2017/2/12 11:45
 * @des ${不改变源码的情况下实现方法增强}
 */

class InstallLooperHandler implements InvocationHandler {
    private  Object obj;

    public InstallLooperHandler(Object obj){
        this.obj=obj;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d("ccc","加入轮询之前添加逻辑，成功了---");
        Object invoke = method.invoke(obj, args);
        Log.d("ccc","---轮询之后添加逻辑，成功了");
        return invoke;
    }
}
