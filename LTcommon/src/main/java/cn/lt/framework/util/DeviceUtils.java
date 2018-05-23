package cn.lt.framework.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.math.BigDecimal;

/**
 * Created by wenchao on 2016/3/14.
 */
public class DeviceUtils {
    /**
     * 获取
     *
     * @param context
     * @return 得到IMEI 例如：*#06#
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                .TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();

    }

    /**
     * @param
     * @return 得到设备型号 例如 Nexus 6、MOTO X
     */
    public static String getDeviceName() {
        String deviceName = Build.MODEL;
        if(isNumber(deviceName)){
            deviceName = Build.MANUFACTURER+" "+Build.DEVICE.replace(deviceName,"");
        }
        return deviceName;

    }

    /**
     * 获取版本name
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取版本code
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(),0);
            int versionCode= packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 得到手机sdk版本号 例如4.4.4
     *
     * @return
     */
    public static String getAndroidSDKVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /* 将byte大小转换至G或M为单位的字符串 */
    public static String converByteToGOrM(long num) {
        double m = num / 1048576;
        double g = 0;
        if (m > 800) {
            g = m / 1024;
            BigDecimal b    = new BigDecimal(g);
            float      size = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            return size + "G";
        } else {
            BigDecimal b = new BigDecimal(m);
            float size = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
            return size + "M";
        }

    }
    /**
     * 判断一个String是否是纯数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        char[] ch = str.toCharArray();
        for (int i = 0;i<ch.length;i++){
            if(ch[i]<'0'||ch[i]>'9'){
                return false;
            }
        }
        return true;
    }


}
