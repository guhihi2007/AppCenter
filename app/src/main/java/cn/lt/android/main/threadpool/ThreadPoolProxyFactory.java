package cn.lt.android.main.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chengyong
 * @time 2016/10/10 21:06
 * @des 线程池工厂
 */
public class ThreadPoolProxyFactory {
    /*
      1.使用方便
      2.共用了一个线程池
      3.对线程池代理创建封装
     */
    static ThreadPoolProxy mInstalledEventThreadPoolProxy;
    static ExecutorService mReportThreadPoolProxy = null;
    static ThreadPoolProxy mNormalThreadPoolProxy;
    static ScheduledExecutorService mScheduledThreadPool;
    private static ExecutorService mCachedThreadPool;

    /**
     * @return
     * @des 处理安装完成的线程池代理
     */
    public static ThreadPoolProxy getDealInstalledEventThreadPoolProxy() {
        if (mInstalledEventThreadPoolProxy == null) {
            synchronized (ThreadPoolProxyFactory.class) {
                if (mInstalledEventThreadPoolProxy == null) {
                    mInstalledEventThreadPoolProxy = new ThreadPoolProxy(30, 30);
                }
            }
        }
        return mInstalledEventThreadPoolProxy;
    }

    /**
     * @return
     * @des 处理数据上报的单线程池
     * @des 串行执行
     */
    public static ExecutorService getReportDataThreadPoolProxy() {
        if (mReportThreadPoolProxy == null) {
            synchronized (ThreadPoolProxyFactory.class) {
                if (mReportThreadPoolProxy == null) {
                    mReportThreadPoolProxy = Executors.newSingleThreadExecutor();
                }
            }
        }
        return mReportThreadPoolProxy;
    }


    /**
     * @return
     * @des 一般的子线程调用
     */
    public static ThreadPoolProxy getNormalThreadPoolProxy() {
        if (mNormalThreadPoolProxy == null) {
            synchronized (ThreadPoolProxyFactory.class) {
                if (mNormalThreadPoolProxy == null) {
                    mNormalThreadPoolProxy = new ThreadPoolProxy(10, 10);
                }
            }
        }
        return mNormalThreadPoolProxy;
    }


    /**
     * @des 定时器
     * @des 延时执行
     */
    public static ScheduledExecutorService getScheduledThreadPool() {
        if (mScheduledThreadPool == null) {
            synchronized (ThreadPoolProxyFactory.class) {
                if (mScheduledThreadPool == null) {
                    mScheduledThreadPool = Executors.newScheduledThreadPool(1);
                }
            }
        }
        return mScheduledThreadPool;
    }

    /**
     * @des 无上限线程池
     * @des 可复用已经销毁的线程
     */
    public static ExecutorService getCachedThreadPool() {
        if (mCachedThreadPool == null) {
            synchronized (ThreadPoolProxyFactory.class) {
                if (mCachedThreadPool == null) {
                    mCachedThreadPool = Executors.newCachedThreadPool();
                }
            }
        }
        return mCachedThreadPool;
    }

    public static void test() {
        mScheduledThreadPool.schedule(new Runnable() {

            @Override
            public void run() {
                System.out.println("delay 3 seconds");
            }
        }, 3, TimeUnit.SECONDS);
//        mScheduledThreadPool.scheduleAtFixedRate()
    }

}
