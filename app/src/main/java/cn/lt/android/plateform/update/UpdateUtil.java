package cn.lt.android.plateform.update;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.entity.PersisData;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.plateform.update.manger.UpdatePathManger;
import cn.lt.android.plateform.update.manger.VersionCheckManger;

/***
 * 管理平台升级的相关数据；
 *
 * @author dxx
 */
public class UpdateUtil {

    public static final String UPDATE_VERSION_KEY_TARGET_VERSION = "targetVersion";
    public static final String UPDATE_VERSION_KEY_DIALOG_SHOW_lASTTIME = "lastShowTime";
    public static final String SPREAD_KEY_DIALOG_SHOW_lASTTIME = "spreadShowTime";
    public static final String AUTO_INSTALL_KEY_DIALOG_SHOW_lASTTIME = "lastAutoInstallPopTime";
    public static final String UPDATE_VERSION_KEY_INSTALL_STATUS = "canInstall";
    public static final String UPDATE_VERSION_KEY_UPDATE_TYPE = "updateType";
    public static final String UPDATE_RED_POINT_SHOW = "showRedPoint";
    public static final String PLAT_FIRST_INSTALL = "platFirstInstall";
    public static String PRE_FILE_NAME = "versionUpdate";
    public static final String ONEDAY_UPDATE_SHOW = "oneday_update_show";
    public static final String UPDATE_CHECK_PERIOD = "update_check_period";//版本升级检测周期

    public static long getTargetVersionCode(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getLong(UPDATE_VERSION_KEY_TARGET_VERSION, 0);
    }

    public static void saveTargetVersionCode(Context context, long versionCode) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(UPDATE_VERSION_KEY_TARGET_VERSION, versionCode);
        editor.apply();
    }

    public static long getUpdateViewLastTime(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getLong(ONEDAY_UPDATE_SHOW, 0);
    }

    public static void saveDialogShowThisTime(Context context, long time) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(UPDATE_VERSION_KEY_DIALOG_SHOW_lASTTIME, time);
        editor.apply();
    }

    public static long getDialogShowLastTime(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getLong(UPDATE_VERSION_KEY_DIALOG_SHOW_lASTTIME, 0);
    }

    /***
     * 保存弹窗推广第一次弹出时间
     *
     * @param context
     * @param time
     */
    public static void saveSpreadDialogThisTime(Context context, long time) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(SPREAD_KEY_DIALOG_SHOW_lASTTIME, time);
        editor.apply();
    }

    /***
     * 获取最后一次弹窗推广时间
     *
     * @param context
     * @return
     */
    public static long getSpreadDialogLastTime(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getLong(SPREAD_KEY_DIALOG_SHOW_lASTTIME, 0);
    }

    /***
     * 记录下载管理-正在下载底部的更新提示框显示时间
     *
     * @param context
     * @param time
     */
    public static void saveUpdateViewThisTime(Context context, long time) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(ONEDAY_UPDATE_SHOW, time);
        editor.apply();
    }

    public static boolean getAutoInstallStatus(Context context) {
        return context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).getBoolean(UPDATE_VERSION_KEY_INSTALL_STATUS, true);
    }

    public static void setShowRedPoint(Context context, boolean canInstalled) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(UPDATE_RED_POINT_SHOW, canInstalled);
        editor.apply();
    }

    /***
     * 获取升级类型，是强制升级还是提示升级
     *
     * @return
     */
    public static boolean getUpgradeType() {
        if (null != VersionCheckManger.getInstance().getmVersionInfo()) {
            return VersionCheckManger.getInstance().getmVersionInfo().isForce();
        }
        return false;
    }

    public static String getPlatUpgradeType() {
        String upgradeType = "";
        if (getUpgradeType()) {
            upgradeType = "force";
        } else {
            upgradeType = "suggest";
        }
        return upgradeType;
    }

    public static void savePlateUpdateType(Context context, boolean upgradeType) {
        Editor editor = context.getSharedPreferences(PRE_FILE_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(UPDATE_VERSION_KEY_UPDATE_TYPE, upgradeType);
        editor.apply();
    }

    /**
     * 判断是否已经下载完成；
     *
     * @return a
     */
    public static boolean isDowloaded(Context context) {
        File file = new File(UpdatePathManger.getDownloadFilePath(context));
        return file.exists() && getTargetVersionCode(context) == VersionCheckManger.getInstance().getmVersionInfo().getmUpgradeVersionCode();
    }

    /***
     * 一天检查一次更新
     *
     * @param context
     * @return
     */
    public static boolean needShowUpdateView(Context context) {
        long nowTime = System.currentTimeMillis();
        long lastTime = UpdateUtil.getUpdateViewLastTime(context);
        long gapTime = (nowTime - lastTime) / (1000 * 60 * 60 * 24 * 1);
        return gapTime > 0;
    }

    /***
     * 8小时检查一次版本升级
     *
     * @param context
     * @return
     */
    public static boolean needCheckUpdate(Context context) {
        long nowTime = System.currentTimeMillis();
//        long lastTime = PreferencesUtils.getLong(context, UPDATE_CHECK_PERIOD, 0);
        long lastTime = readData();
        long gapTime = (nowTime - lastTime) / Constant.CHECKVERSIONPERIOD;//测试用：五分钟临界值
        return gapTime > 0;
    }

    /***
     * 记录第一次检查版本更新的时间
     *
     * @param context
     */
    public static void saveLastCheckTime(Context context) {
        long lastTime = System.currentTimeMillis();
//        PreferencesUtils.putLong(context, UPDATE_CHECK_PERIOD, lastTime);
        persistData(new PersisData(lastTime));
    }

    /**
     * @param context d
     * @return boolean
     */
    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningAppProcessInfo> pis = manager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo topAppProcess = pis.get(0);
            if (topAppProcess != null && topAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : topAppProcess.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        return true;
                    }
                }
            }
        } else {
            List localList = manager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo taskInfo = (ActivityManager.RunningTaskInfo) localList.get(0);
            if (taskInfo != null) {
                ComponentName componentName = taskInfo.topActivity;
                String packageName = componentName.getPackageName();
                if (!TextUtils.isEmpty(packageName) && packageName.equals(context.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                Log.i("UpdateService", "后台2");
                return true;
            } else {
                Log.i("UpdateService", "前台2");
            }
        }
        return false;

    }

    /***
     * 判断手机是否熄屏
     *
     * @param context
     * @return
     */
    public static boolean isBackGroundAndGameDowning(Context context) {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return (isForeground(context) || mKeyguardManager.inKeyguardRestrictedInputMode());
    }

    /**
     * 修改文件的权限；
     */
    public static void modifyPermission(String filePath) {
        try {
            String cmd = "chmod 777 " + filePath;
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 读数据
     */
    public static long readData() {
        PersisData data;
        String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "TTData" + File.separator + "data.txt";
        File file = new File(filePath);
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(filePath));
                data = (PersisData) ois.readObject();
                if (data != null) {
                    long time = data.getTimestamp();
                    Log.i("UpdateService", "read lastTime=" + time);
                    return time;
                }
                ois.close();
            } catch (IOException e) {
                Log.i("UpdateService", "read lastTime exception");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Log.i("UpdateService", "read lastTime exception");
                e.printStackTrace();
            } finally {

            }
        } else {
            Log.i("UpdateService", "file is not exist,return 0");
            return 0;
        }
        return 0;

    }

    /***
     * 持久化数据
     */
    public static void persistData(final PersisData data) {
        if (null == data) return;
        ThreadPoolProxyFactory.getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                File files = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "TTData");
                if (!files.exists()) {
                    Log.i("UpdateService", "create file success");
                    files.mkdirs();
                }
                File file = new File(files + File.separator + "data.txt");
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                    oos.writeObject(data);
                    Log.i("UpdateService", "save last time=" + data.getTimestamp());
                    oos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
        });


    }
}
