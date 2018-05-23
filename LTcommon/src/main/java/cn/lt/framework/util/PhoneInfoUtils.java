package cn.lt.framework.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.PrintWriter;

/**
 * Created by Administrator on 2016/4/25.
 */
public class PhoneInfoUtils {


    public static void getPhoneInfo(Context mContext,PrintWriter printWriter) throws PackageManager.NameNotFoundException{
        //应用版本名称和版本号
        PackageManager packageManager=mContext.getPackageManager();
        PackageInfo packageInfo=packageManager.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        printWriter.print("App Version: ");
        printWriter.print(packageInfo.versionName);
        printWriter.print("_");
        printWriter.println(packageInfo.versionCode);

        //Android版本号
        printWriter.print("OS Version: ");
        printWriter.print(Build.VERSION.RELEASE);
        printWriter.print("_");
        printWriter.println(Build.VERSION.SDK_INT);

        //手机制造商
        printWriter.print("Vendor: ");
        printWriter.println(Build.MANUFACTURER);

        //手机型号
        printWriter.print("Model: ");
        printWriter.println(Build.MODEL);

        //cpu架构
        printWriter.print("CPU ABI: ");
        printWriter.println(Build.CPU_ABI);
    }
}
