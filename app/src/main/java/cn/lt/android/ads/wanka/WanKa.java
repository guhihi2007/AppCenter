package cn.lt.android.ads.wanka;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import cn.lt.android.LTApplication;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.util.AdMd5;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.framework.util.FileUtils;
import cn.lt.framework.util.PreferencesUtils;

/**
 * Created by chon on 2016/12/26.
 * What? How? Why?
 * <p>
 * 玩咖公共参数获取，用作请求参数
 */

public class WanKa {

    /**
     * 用户标识
     */
    static final String CUID = "cuid";
    /**
     * 系统版本 （5.1）
     */
    private static final String OVR = "ovr";
    /**
     * 系统 api 版本 （22）
     */
    private static final String OS_LEVEL = "os_level";
    /**
     * 手机品牌_手机型号 （Meizu_M7）
     */
    static final String DEVICE = "device";
    /**
     * 分配的渠道号
     */
    public static final String CHANNEL_ID = "channel_id";
    /**
     * 分配的渠道应用 id
     */
    public static final String APP_ID = "app_id";
    /**
     * App 版本号
     */
    private static final String SVR = "svr";
    /**
     * 网络运营,wifi,移动，联通，电信（wifi,cm,un,net）
     */
    private static final String NET_TYPE = "net_type";
    /**
     * 分辨率
     */
    private static final String RESOLUTION = "resolution";
    /**
     * Mac 地址
     */
    static final String INFO_MA = "info_ma";
    /**
     * IMSI 号
     */
    static final String INFO_MS = "info_ms";
    /**
     * IMEI 号
     */
    static final String CLIENT_ID = "client_id";
    /**
     * 像素密度
     */
    private static final String DPI = "dpi";
    /**
     * 客户端 ip
     */
    static final String CLIENT_IP = "client_ip";
    /**
     * 国家代码
     */
    private static final String MCC = "mcc";
    /**
     * 运营商 ID
     */
    private static final String MNO = "mno";
    /**
     * LAC  基站位置区域码
     */
    private static final String INFO_LA = "info_la";
    /**
     * CID，基站编号
     */
    private static final String INFO_CI = "info_ci";
    /**
     * 获取设备  android id
     */
    static final String OS_ID = "os_id";
    /**
     * WIFI 的 BBSID
     */
    private static final String BSSID = "bssid";
    /**
     * 请求时间戳（秒）
     */
    static final String NONCE = "nonce";
    /**
     * 宿主包名
     */
    private static final String PKG = "pkg";


    public static final String KEY_REPORT_DATA = "reportData";


    public static Map<String, String> getCommonParams(Application application) {
        Map<String, String> map = new TreeMap<>();

        map.put(OVR, Build.VERSION.RELEASE);
        map.put(OS_LEVEL, String.valueOf(Build.VERSION.SDK_INT));
        map.put(DEVICE, Build.BRAND + "_" + Build.MODEL);
        map.put(CHANNEL_ID, WanKaUrl.WANKA_CHANNEL_ID);
        map.put(APP_ID, WanKaUrl.WANKA_APP_ID);
        map.put(SVR, getAppVersion(application));
        map.put(NET_TYPE, getNetworkType(application));
        map.put(RESOLUTION, getResolution(application));

        map.put(INFO_MA, AppUtils.getLocalMacAddress(application));
        String IMSI = getIMSI(application);
        map.put(INFO_MS, IMSI);
        String IMEI = getIMEI(application);
        map.put(CLIENT_ID, IMEI);

        map.put(DPI, String.valueOf(getDPI(application)));
        map.put(CLIENT_IP, NetUtils.isWifiNet(application) ? AppUtils.getWIFILocalIpAdress(application) : AppUtils.getLocalIpAddress());

        map.put(MCC, TextUtils.isEmpty(IMSI) ? "0" : IMSI.substring(0, 3));
        map.put(MNO, getNetProviderCode(application));

        map.put(INFO_LA, String.valueOf(getLACOrCellID(application, true)));
        map.put(INFO_CI, String.valueOf(getLACOrCellID(application, false)));

        map.put(BSSID, getBSSID(application));
        map.put(NONCE, String.valueOf(System.currentTimeMillis() / 1000));

        String ANDROID_ID = Settings.System.getString(application.getContentResolver(), Settings.Secure.ANDROID_ID);
        map.put(OS_ID, ANDROID_ID);

        map.put(CUID, generateCUID(IMEI, OS_ID));
        map.put(PKG, application.getPackageName());

        return map;
    }

    private static String generateCUID(String imei, String androidID) {
        // imei + androdiid + uuid
        String cuid = PreferencesUtils.getString(LTApplication.instance, CUID);
        if (TextUtils.isEmpty(cuid)) {
            // 去文件读取
            String path = DownloadTaskManager.getInstance().getSaveDirPath() + File.separator + "cuid.chon";

            StringBuilder stringBuilder = FileUtils.readFile(path, "UTF-8");
            if (stringBuilder != null) {
                cuid = stringBuilder.toString();
                PreferencesUtils.putString(LTApplication.instance, CUID, cuid);
            }
        }

        if (TextUtils.isEmpty(cuid)) {
            cuid = AdMd5.MD5(imei + androidID + UUID.randomUUID().toString());
            PreferencesUtils.putString(LTApplication.instance, CUID, cuid);
            String path = DownloadTaskManager.getInstance().getSaveDirPath() + File.separator + "cuid.chon";
            FileUtils.writeFile(path, cuid);
        }
        return TextUtils.isEmpty(cuid) ? null : cuid.toUpperCase();
    }

    private static String getAppVersion(Application application) {
        try {
            PackageManager manager = application.getPackageManager();
            PackageInfo info = manager.getPackageInfo(application.getPackageName(), 0);
            return String.valueOf(info.versionCode);
        } catch (Exception e) {
            // can not reach
            return "";
        }
    }

    private static String getNetProviderCode(Application application) {
        String providersCode = "0";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
            String IMSI = telephonyManager.getSubscriberId();
            // FIXME　IMSI号前面3位460是国家，紧接着后面2位00 02 04 07是中国移动，01 06 09是中国联通，03 05是中国电信。
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46004") || IMSI.startsWith("46007")) {
                providersCode = IMSI.substring(3, 5);
            } else if (IMSI.startsWith("46001") || IMSI.startsWith("46006") || IMSI.startsWith("46009")) {
                providersCode = IMSI.substring(3, 5);
            } else if (IMSI.startsWith("46003") || IMSI.startsWith("46005")) {
                providersCode = IMSI.substring(3, 5);
            }
        } catch (Exception e) {
            WanKaLog.e("fetch NetProviderName failed");
        }
        return providersCode;
    }

    private static String getResolution(Application application) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;
        int widthPixels = displayMetrics.widthPixels;

        return widthPixels + "_" + heightPixels;
    }


    private static int getDPI(Application application) {
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        return displayMetrics.densityDpi;
    }

    private static String getIMSI(Application application) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getSubscriberId() == null ? "" : telephonyManager.getSubscriberId();
        } catch (Exception e) {
            return "";
        }
    }

    static String getIMEI(Application application) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getDeviceId() == null ? "" : telephonyManager.getDeviceId();
        } catch (Exception e) {
            return "";
        }
    }


    private static int getLACOrCellID(Application application, boolean fetchLAC) {
        int lac = 0;       // Location Area Code，位置区域码；
        int cellId = 0;    // Cell Identity，基站编号；
        TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            // 中国移动和中国联通获取LAC、CID的方式
            GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
            lac = location.getLac();
            cellId = location.getCid();
        } catch (Exception e) {
            // 中国电信获取LAC、CID的方式
            try {
                CdmaCellLocation location = (CdmaCellLocation) telephonyManager.getCellLocation();
                if (null != location) {
                    lac = location.getNetworkId();
                    cellId = location.getBaseStationId();
                    cellId /= 16;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return fetchLAC ? lac : cellId;
    }

    private static String getBSSID(Application application) {
        WifiManager wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo == null || wifiInfo.getBSSID() == null) {
            return "";
        } else {
            return wifiInfo.getBSSID();
        }
    }

    private static String getNetworkType(Application application) {
        StringBuilder strNetworkType = new StringBuilder("");
        NetworkInfo networkInfo = ((ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType.append("1");
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType.append("2");
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType.append("3");
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType.append("4");
                        break;
                    default:
                        // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType.append("3");
                        } else {
//                            strNetworkType.append(_strSubTypeName);
                            strNetworkType.append("4");
                        }
                        break;
                }
            }
        }
        return strNetworkType.toString();
    }
}
