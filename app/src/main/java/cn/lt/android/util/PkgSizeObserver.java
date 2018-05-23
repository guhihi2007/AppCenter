package cn.lt.android.util;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

/**
 * Created by wenchao on 2016/3/15.
 */
public class PkgSizeObserver extends IPackageStatsObserver.Stub{
    private String packageName;
    private OnPackageSizeListener onPackageSizeListener;
    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public PkgSizeObserver(String packageName,OnPackageSizeListener onPackageSizeListener){
        this.packageName = packageName;
        this.onPackageSizeListener = onPackageSizeListener;
    }

    /*** 回调函数，
     * @param pStats ,返回数据封装在PackageStats对象中
     * @param succeeded  代表回调成功
     */
    @Override
    public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
            throws RemoteException {
        // TODO Auto-generated method stub
        final long cachesize = pStats.cacheSize  ; //缓存大小
        final long datasize  = pStats.dataSize  ;  //数据大小
        final long codesize  =	pStats.codeSize  ;  //应用程序大小
        long       totalsize = cachesize + datasize + codesize ;
        LogUtils.i("PkgSizeObserver", "packageName-->"+packageName+" cachesize--->"+cachesize+" datasize---->"+datasize+ " codeSize---->"+codesize)  ;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPackageSizeListener.computeComplete(packageName,cachesize,datasize,codesize);
            }
        });

    }


    public interface OnPackageSizeListener{
        void computeComplete(String packageName,long cacheSize,long dataSize,long codeSize);
    }
}
