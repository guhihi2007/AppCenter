package cn.lt.android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.lt.android.LTApplication;
import cn.lt.android.main.LoadingActivity;


public class CrashExceptionHandler implements UncaughtExceptionHandler {
    private Map<String, String> infos = new HashMap<String, String>();
    // 格式化日期时间
    private DateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    private Context mContext;

    private CrashExceptionHandler() {

    }

    private static final String TAG = "CrashExceptionHandler";

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        logErr(ex);
//        reportError(mContext, ex);
//        handleErr(mContext, ex);
    }

    private void logErr(Throwable e) {
        Log.d(TAG, "crash error begin-*----*----*----*----*----*----*----*----*----*-");
        e.printStackTrace();
        Log.d(TAG, "crash error end-*----*----*----*----*----*----*----*----*----*-");
    }

    /**
     * 发生异常时可以完成相关异常收集统计操作；
     *
     * @param context
     */
    private void reportError(Context context, Throwable e) {
        collectDeviceInfo(mContext);
        saveInfo(e);
    }

    /**
     * 处理异常；
     *
     * @param context
     * @param e
     */
    private void handleErr(Context context, Throwable e) {
        try {
            Thread.sleep(500);
        } catch (Exception e1) {
        }
        if (ActivityManager.self().isUP() && needReboot()) {
            reboot();
        }
        ActivityManager.self().exitApp();
    }

    private boolean needReboot() {
        SharedPreferences pre = mContext.getSharedPreferences("reboot_count", Context.MODE_PRIVATE);
        int count = pre.getInt("reboot", 0);
        Log.d(TAG, "重启次数达到：" + count);
        if (count < 4) {
            Editor e = pre.edit();
            e.putInt("reboot", count + 1);
            e.commit();
            return true;
        } else {
            Editor e = pre.edit();
            e.putInt("reboot", 0);
            e.commit();
            return false;
        }
    }

    private void reboot() {
        Intent intent = new Intent(mContext, LoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);

    }


    /**
     * @throws
     * @Title: collectDeviceInfo
     * @Description: TODO(收集设备信息)
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * @throws
     * @Title: saveCrashInfo2File
     * @Description: TODO(保存错误信息到本地文件中)
     * @author: yyx
     */
    @SuppressLint("SdCardPath")
    private String saveInfo(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = LTApplication.shareApplication().getFilesDir().getPath();
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }

    private static class CrashExceptionHandlerHolder {
        static final CrashExceptionHandler sInstance = new CrashExceptionHandler();
    }

    public static CrashExceptionHandler self() {
        return CrashExceptionHandlerHolder.sInstance;
    }

    public void init(Context context) {
        this.mContext = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


}
