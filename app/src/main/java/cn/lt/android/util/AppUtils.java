package cn.lt.android.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Process;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.ClientInstallInfo;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.statistics.DCStat;
import cn.lt.framework.util.ToastUtils;

import static android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES;

/**
 * Created by wenchao on 2016/1/22.
 */
public class AppUtils {

    private static Map<String, PackageInfo> systemApps = new HashMap<>();
//    private static PackageInfo tempPackageInfo;

    /***
     * 是否有系统权限
     *
     * @param context
     * @return
     */
    public static boolean isSystemApp(Context context) {
        if (((context.getApplicationInfo().flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) || ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) != 0)) {
            return true;
        }
        return false;
    }

    /**
     * 检查安装状态
     *
     * @param packagename
     * @return
     */
    public static boolean isInstalled(String packagename) {
        return getPackageInfo(packagename) != null;
    }

    /**
     * 获取包信息
     *
     * @param packagename
     * @return
     */
    public static PackageInfo getPackageInfo(String packagename) {
        PackageInfo tempPackageInfo = null;
        try {
            tempPackageInfo = LTApplication.shareApplication().getPackageManager().getPackageInfo(packagename, 0);
        } catch (Exception e) {
        }

        if (tempPackageInfo == null) {
            if (systemApps.size() <= 0) {
                getSystemApps();
            }

            tempPackageInfo = systemApps.get(packagename);
        }

        return tempPackageInfo;
    }


    /***
     * 根据包名判断应用是否已安装   by ATian
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstalled(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) return false;
        try {
            context.getPackageManager().getApplicationInfo(packageName, GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
            return false;
        }
    }

    /***
     * 判断当前应用是否在下载任务列表中
     *
     * @param pkgName
     * @return
     */
    public static boolean isDownloadTask(String pkgName) {
        try {
            List<AppEntity> taskList = DownloadTaskManager.getInstance().getAll();
            for (AppEntity taskApp : taskList) {
                if (pkgName.equals(taskApp.getPackageName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 通过包名打开app
     *
     * @param context
     * @param packageName
     */
    public static void openApp(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            context.startActivity(intent);
            DCStat.baiduStat(context, "app_open", "应用打开：（包名）" + packageName);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(context, "打开失败");
        }
    }

    /* 传入activity的context，获取屏幕宽 */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;

    }

    /* 传入activity的context，获取屏幕高 */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;

    }

    /***
     * 判断是否熄屏
     *
     * @return
     */
    public static boolean isScreenOn() {
        PowerManager pm = (PowerManager) LTApplication.shareApplication().getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    /**
     * 获取
     *
     * @param context
     * @return 得到IMEI 例如：*#06#
     */

    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数

    public static String getIMEI(Context context) {
        String imei = "";
        try {
            imei = ((TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE)).getDeviceId();
            if (imei == null) {
                imei = "";
            } else {
                if ("000000000000000".equals(imei)) {
                    imei = "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    public static synchronized String getIMEIOrAndroid(Context context) {
        String id = "";
        //android.telephony.TelephonyManager
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean phonePermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED;
        if (!phonePermission && mTelephony.getDeviceId() != null) {
            id = mTelephony.getDeviceId();
            if ("000000000000000".equals(id)){
                id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                if (!TextUtils.isEmpty(id)) {
                    id = id.toUpperCase();
                }
            }
        } else {
            //android.provider.Settings;
            id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            if (!TextUtils.isEmpty(id)) {
                id = id.toUpperCase();
            }
        }
        return id;
    }

    /***
     * 获取IMEI
     * @param context
     * @return
     */
    public static synchronized String getAndroidID(Context context) {
        String id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(id)) {
            id = id.toUpperCase();
        }
        return id;
    }

    /***
     * 获取本机MAC地址
     *
     * @return
     */
    public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /***
     * 获取本机IP地址
     *
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            LogUtils.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }

    public static String getWIFILocalIpAdress(Context mContext) {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    //这里需要注意：这里增加了一个限定条件( inetAddress instanceof Inet4Address ),主要是在Android4.0高版本中可能优先得到的是IPv6的地址。参考：http://blog.csdn.net/stormwy/article/details/8832164
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("here", ex.toString());
        }
        return null;
    }


    // 请求权限兼容低版本
    private static void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(ActivityManager.self().topActivity(), permissions, PERMISSION_REQUEST_CODE);
    }

    /**
     * @param
     * @return 得到设备型号 例如 Nexus 6、MOTO X
     */
    public static String getDeviceName() {
        String deviceName = Build.MODEL;
        if (isNumber(deviceName)) {
            deviceName = Build.MANUFACTURER + " " + Build.DEVICE.replace(deviceName, "");
        }

        if (JudgeChineseUtil.isChineseChar(deviceName)) {
            return "shanzai";
        }
        return deviceName;

    }

    /***
     * 获取当前APP版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /***
     * 获取当前APP版本号
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    /**
     * 判断一个String是否是纯数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        char[] ch = str.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] < '0' || ch[i] > '9') {
                return false;
            }
        }
        return true;
    }


    /**
     * 得到手机sdk版本号 例如4.4.4
     *
     * @return
     */
    public static String getAndroidSDKVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /***
     * 得到API
     *
     * @return
     */
    public static int getAndroidAPILevel() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取所有应用程序
     *
     * @param context
     * @return
     */
//    public static List<PackageInfo> getAllApps(Context context) {
//        List<PackageInfo> allApps = new ArrayList<>();
//
//        // 获取系统应用列表
//        if (systemApps.size() <= 0) {
//            getSystemApps(context);
//        }
//
//        allApps.addAll(systemApps);
//
//        // 获取用户自己装的应用列表
//        allApps.addAll(getUserAppList(context));
//
//        // 需要返回所有已安装列表
//        return allApps;
//    }

    /**
     * 获取用户自己装的应用列表
     */
    public static List<PackageInfo> getUserAppList(Context context) {
        List<PackageInfo> appList = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);

        // 获取用户自己装的应用列表
        for (PackageInfo packageInfo : packageInfoList) {

            // 值如果 <=0 则为自己装的程序
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                appList.add(packageInfo);
            }
        }

        return appList;
    }

    /***
     * 获取本地应用列表包名和任务列表的包名
     *
     * @return
     */
    public static String getUploadParams() {
        List<android.content.pm.PackageInfo> apps = getUserAppList(LTApplication.shareApplication());
        List<cn.lt.android.download.PackageInfo> uploadApps = new ArrayList<>();
        if (apps != null) {
            for (android.content.pm.PackageInfo packageInfo : apps) {
                uploadApps.add(new cn.lt.android.download.PackageInfo(packageInfo.packageName, String.valueOf(packageInfo.versionCode)));
            }
        }
        String str = new Gson().toJson(uploadApps);
        return str;
    }

    public static String getLocalParams() {
        PackageManager packageManager = LTApplication.shareApplication().getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        List<cn.lt.android.download.PackageInfo> uploadApps = new ArrayList<>();
        if (packageInfoList != null && packageInfoList.size() > 0) {
            for (android.content.pm.PackageInfo packageInfo : packageInfoList) {
                boolean isSystem = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;//为系统应用
                boolean isNeedReport = needReporetApps().contains(packageInfo.packageName);
                if (!isSystem || isNeedReport) {
                    uploadApps.add(new cn.lt.android.download.PackageInfo(packageInfo.packageName, String.valueOf(packageInfo.versionCode), TextUtils.isEmpty(packageInfo.versionName) ? "" : packageInfo.versionName, isSystem ? true : false));
//                    LogUtils.e("gpp", "packageName:" + packageInfo.packageName + "-----isNeedReport:" + isNeedReport);
                }
            }
        }
        String str = new Gson().toJson(uploadApps);
        return str;
    }


    /**
     * 获取系统自带应用列表
     */
    private static void getSystemApps() {
        PackageManager packageManager = LTApplication.shareApplication().getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);

        LogUtils.i("huoquxitonglieb", "还没获取过系统列表，马上获取。。");
        for (PackageInfo packageInfo : packageInfoList) {

            // 值如果 >0 则为系统的程序
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                systemApps.put(packageInfo.packageName, packageInfo);
            }
        }

    }

    public static void queryPackageSize(Context context, String packageName, PkgSizeObserver.OnPackageSizeListener onPackageSizeListener) {
        try {
            if (packageName != null) {
                PackageManager packageManager = context.getPackageManager();
                if (Build.VERSION.SDK_INT >= 24) {
                    Method getPackageSizeInfo = packageManager.getClass().getDeclaredMethod("getPackageSizeInfoAsUser", String.class, int.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(packageManager, packageName, Process.myUid() / 100000, new PkgSizeObserver(packageName, onPackageSizeListener));
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Method getPackageSizeInfo = packageManager.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, int.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(packageManager, packageName, Process.myUid() / 100000, new PkgSizeObserver(packageName, onPackageSizeListener));
                } else {
                    Method getPackageSizeInfo = packageManager.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                    getPackageSizeInfo.invoke(packageManager, packageName, new PkgSizeObserver(packageName, onPackageSizeListener));
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static final String BUILD_PROP_FILE = "/system/build.prop";
    private static final String PROP_NAME_MIUI_VERSION_CODE = "ro.miui.ui.version.code";

    /**
     * 判断是否  小米系统
     *
     * @return
     */
    public static boolean isMIUI() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(new File(BUILD_PROP_FILE)));
            String readLine;
            do {
                readLine = bufferedReader.readLine();
                if (readLine == null) {
                    return false;
                }
            } while (!readLine.startsWith(PROP_NAME_MIUI_VERSION_CODE));
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 判断 是否 魅族手机
     *
     * @return
     */
    public static boolean isFlymeOs() {
        try {
            return android.os.Build.FINGERPRINT.toLowerCase().contains("flyme");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取配置文件meta-data信息
     *
     * @param key
     * @return
     */
    public static String getMetaData(String key) {
        try {
            PackageManager pm = LTApplication.instance.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(LTApplication.instance.getPackageName(), PackageManager.GET_META_DATA);
            return ai.metaData.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /***
     * 当用户已在网络注册时有效, CDMA 可能会无效（中国移动：46000 46002, 中国联通：46001,中国电信：46003）
     *
     * @param context
     * @return
     */
    public static String getNetworkOperator(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telephonyManager.getSimOperator();
        String operatorName = "未知";
        if (operator != null) {
            if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
                operatorName = "中国移动";
            } else if (operator.equals("46001") || operator.equals("46006")) {
                operatorName = "中国联通";
            } else if (operator.equals("46003") || operator.equals("46005") || operator.equals("46011")) {
                operatorName = "中国电信";
            }
        }
        return operatorName;
    }

    /***
     * 返回移动终端类型
     * <p/>
     * PHONE_TYPE_NONE :0 手机制式未知
     * PHONE_TYPE_GSM :1 手机制式为GSM，移动和联通
     * PHONE_TYPE_CDMA :2 手机制式为CDMA，电信
     * PHONE_TYPE_SIP:3
     *
     * @param context
     * @return
     */
    public static int getPhoneType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getPhoneType();
    }

    public static int getNetWorkClass(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return Constant.NETWORK_CLASS_2_G;

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return Constant.NETWORK_CLASS_3_G;

            case TelephonyManager.NETWORK_TYPE_LTE:
                return Constant.NETWORK_CLASS_4_G;

            default:
                return Constant.NETWORK_CLASS_UNKNOWN;
        }
    }


    private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
    private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

    /***
     * 获取可用存储空间
     * 返回单位：MB
     *
     * @return
     */
    public static long getAvailablMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize / (1048 * 1024);
        } else {
            return getAvailableInternalMemorySize();
        }
    }

    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize / (1048 * 1024);
    }

    /**
     * 获取客户端安装/升级时间
     */
    public static ClientInstallInfo getClientInstallInfo() {
        ClientInstallInfo clientInstallInfo = null;
        PackageInfo packageInfo = null;
        try {
            packageInfo = LTApplication.shareApplication().getPackageManager().getPackageInfo(LTApplication.shareApplication().getPackageName(), 0);
        } catch (Exception e) {
        }
        if (packageInfo != null) {
            clientInstallInfo = new ClientInstallInfo();
            clientInstallInfo.setInstall_time(packageInfo.firstInstallTime);
            clientInstallInfo.setLast_upgrade_time(packageInfo.lastUpdateTime);
        }
        return clientInstallInfo;
    }


    /**
     * SDCARD是否存
     */
    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }


    /**
     * @param
     * @return 得到设备品牌
     */
    public static String getBrand() {
        String deviceName = "unknown";
        try {
            deviceName = Build.BRAND;
            if (JudgeChineseUtil.isChineseChar(deviceName)) {
                return "shanzai_brand";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceName;

    }

    /***
     * 判断是否有SIM卡
     *
     * @return
     */
    public static boolean hasSIMCard(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        int state = mTelephonyManager.getSimState();
        if (TelephonyManager.SIM_STATE_READY == state) {
            return true;
        }
        return false;
    }

    public static final int TAG1 = 1;
    public static final int TAG2 = 2;
    public static final int TAG3 = 3;

    private static int ExitAppFlag;

    public static int getExitAppFlag() {
        return ExitAppFlag;
    }

    public static void setExitAppFlag(int exitAppFlag) {
        ExitAppFlag = exitAppFlag;
    }

    /**
     * 判断apk是否不存在
     */
    public static boolean apkIsNotExist(String apkPath) {
        UpdateUtil.modifyPermission(apkPath);
        File file = new File(apkPath);
        if (file.exists()) {
            LogUtils.i("apkIsNotExist", "apk文件还在，执行安装");
            return false;
        }
        LogUtils.i("apkIsNotExist", "apk文件不存在，执行重新下载");
        return true;
    }

    public static String getLoaclJsonData(String fileName) {
        String result = "";

        try {
            FileInputStream f = new FileInputStream(Environment.getExternalStorageDirectory() + File.separator + fileName);
            BufferedReader bis = new BufferedReader(new InputStreamReader(f));
            String line = "";
            while ((line = bis.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    public static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    /***
     * 检查Scheme地址是否有效
     * @param context
     * @param intent
     * @return
     */
    public static boolean checkUrlScheme(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return !activities.isEmpty();
    }

    private static List<String> needReporetApps() {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(apps));
        return list;
    }

    private static String apps[] = {
            "tv.xiaoka.live",
            "com.dc.geek",
            "com.iflytek.inputmethod",
            "com.qihoo360.mobilesafe",
            "com.qihoo.browser",
            "com.qihoo.appstore",
            "com.sogou.activity.src",
            "com.cleanmaster.mguard_cn",
            "com.yixia.videoeditor",
            "com.tencent.qqpim",
            "com.sogou.androidtool",
            "sogou.mobile.explorer",
            "com.market.chenxiang",
            "cn.lt.game",
            "cn.lt.appstore",
            "com.sina.weibo",
            "com.tencent.reading",
            "com.tencent.qqlive",
            "com.jingdong.app.mall",
            "com.sohu.inputmethod.sogou",
            "com.baidu.searchbox",
            "com.tencent.news",
            "com.tencent.qqpimsecure",
            "com.andreader.prein",
            "com.sankuai.meituan",
            "com.ss.android.article.news",
            "com.storm.smart",
            "com.UCMobile",
            "com.tencent.mtt",
            "com.browser_llqhz",
            "cn.kuwo.player",
            "cn.opda.a.phonoalbumshoushou",
            "com.autonavi.minimap",
            "com.luo.CashPocket",
            "com.sogou.toptennews",
            "com.iyd.reader.ReadingJoy",
            "com.iflytek.cpacmcc",
            "com.tuan800.tao800",
            "com.pp.assistant",
            "com.tmall.wireless",
            "com.tencent.android.qqdownloader",
            "com.baidu.input",
            "com.dianping.v1",
            "com.baidu.BaiduMap",
            "com.zengame.ttddzzrb.lt",
            "cn.jj",
            "com.shky.cyjshxhjdt.egame.uc",
            "co.yazhai.dtbzgf",
            "com.zengame.ttddzzrb.p365you"
    };

}
