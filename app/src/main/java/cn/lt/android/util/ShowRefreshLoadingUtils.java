package cn.lt.android.util;


import cn.lt.android.LTApplication;
import cn.lt.pullandloadmore.LoadingLayout;
import cn.lt.pullandloadmore.PullToLoadView;

/**
 * @author chengyong
 * @version $Rev$
 * @time 2016/6/28 17:46
 * @des ${TODO}
 */
public class ShowRefreshLoadingUtils {
    //无网络刷新时转圈--网络不好
    public static void showLoadingForNotGood(final PullToLoadView mLoadingLayout) {
        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.showErrorNotGoodNetwork();
            }
        }, 500);
    }
    //无网络刷新时转圈--无网络
    public static void showLoadingForNoNet(final PullToLoadView mLoadingLayout) {
        Runnable noNetTask = new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.showErrorNoNetwork();
            }
        };
        LTApplication.getMainThreadHandler().postDelayed(noNetTask, 500);

//        ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().executeDelay(r); //线程池无法延迟执行任务
    }


    //无网络刷新时转圈--网络不好
    public static void showLoadingForNotGood(final LoadingLayout mLoadingLayout) {
        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.showErrorNotGoodNetwork();
            }
        }, 500);
    }
    //无网络刷新时转圈--无网络
    public static void showLoadingForNoNet(final LoadingLayout mLoadingLayout) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                mLoadingLayout.showErrorNoNetwork();
            }
        };
        LTApplication.getMainThreadHandler().postDelayed(task, 500);
//        ThreadPoolProxyFactory.getDealInstalledEventThreadPoolProxy().executeDelay(r);

    }
}
