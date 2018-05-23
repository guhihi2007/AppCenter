package cn.lt.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.install.root.ShellUtils;
import cn.lt.android.util.LogUtils;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author chengyong
 * @time 2016/10/20 18:21
 * @des 用于检测是否可以静默卸载
 */
public class CheckSilentService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkSilent(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 检查是否能静默卸载
     *
     * @param context
     */
    public void checkSilent(final Context context) {
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (PackageUtils.isSystemApplication(context) || ShellUtils.checkRootPermission()) {
                    subscriber.onNext(0);
                } else {
                    subscriber.onNext(1);
                }
                subscriber.onCompleted();
            }
        })
// .filter(new Func1<Integer, Boolean>() {
//            @Override
//            public Boolean call(Integer integer) {
//                return integer==1;
//            }
//        })
                .subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer i) {
                        if (i == 0) {
//                            LTApplication.instance.canSilentAction = true;
                        } else {
//                            LTApplication.instance.canSilentAction = false;
                        }
                    }

                    @Override
                    public void onCompleted() {
                        LogUtils.d("静默卸载检查完毕 ");
                        stopSelf();
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.e("静默卸载检查错误 " + e.toString());
                    }
                });
    }
}
