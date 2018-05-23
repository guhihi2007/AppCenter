package cn.lt.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.reflect.Field;

import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.util.BadgeUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.framework.util.PreferencesUtils;

/**
 * Created by Erosion on 2018/3/26.
 */

public class LTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private boolean isDebug;
    private LTApplication instance;
    private BuildTypeUtils.RefWatcherUtil refWatcher;

    public LTActivityLifecycleCallbacks(LTApplication instance,boolean isDebug,BuildTypeUtils.RefWatcherUtil refWatcher) {
        this.instance = instance;
        this.isDebug = isDebug;
        this.refWatcher = refWatcher;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        if (isDebug) {
            Bundle parameter = activity.getIntent() == null ? null : activity.getIntent().getExtras();
            String parameterMap = null;
            Field field = null;
            if (parameter != null) {
                try {
                    field = parameter.getClass().getDeclaredField("mParcelledData");
                    if (field != null) {
                        field.setAccessible(true);
                        if (field.get(parameter) != null) {
                            field = parameter.getClass().getDeclaredField("mMap");
                            if (field != null) {
                                field.setAccessible(true);
                                Object pMap = field.get(parameter);
                                if(pMap!=null) {
                                    parameterMap = pMap.toString();
                                }
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            LogUtils.d("LT_Activity", String.format("onActivityCreated:\t%s\t%s", activity.getComponentName().getClassName(), parameter == null ? "" :  String.format("parameter:%s %s", parameterMap==null?"":parameterMap,parameter)));
        }
    }


    @Override
    public void onActivityStarted(Activity activity) {
        if (isDebug) {
            LogUtils.d("LT_Activity", String.format("onActivityStarted:\t%s", activity.getComponentName().getClassName()));
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isDebug) {
            LogUtils.d("LT_Activity", String.format("onActivityResumed:\t%s", activity.getComponentName().getClassName()));
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (isDebug) {
            LogUtils.d("LT_Activity", String.format("onActivityPaused:\t%s", activity.getComponentName().getClassName()));
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (isDebug) {
            LogUtils.d("LT_Activity", String.format("onActivityStopped:\t%s", activity.getComponentName().getClassName()));
        }
        if (!UpdateUtil.isForeground(instance)) {
            LogUtils.i("Erosion", "isForeground");
            LTApplication.isBackGroud = true;
            PreferencesUtils.putLong(instance, Constant.BACKGROUND_TIME, System.currentTimeMillis());
        }
        try {
            BadgeUtil.setBadgeCount(activity, getAllTaskCount());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (isDebug) {
            LogUtils.d("LT_Activity", String.format("onActivityDestroyed:\t%s", activity.getComponentName().getClassName()));
        }
        if (refWatcher != null) {
            refWatcher.watch(activity);
        }
    }

    private int getAllTaskCount() {
        int downloadCount = DownloadTaskManager.getInstance().getAll().size();
        int upgradeCount = UpgradeListManager.getInstance().getUpgradeAppList().size();
        return downloadCount + upgradeCount;
    }
}
