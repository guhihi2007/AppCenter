package cn.lt.android;

import android.content.Context;
import android.text.TextUtils;

import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.util.MetaDataUtil;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.appstore.BuildConfig;
import cn.lt.framework.util.PreferencesUtils;

import static cn.lt.android.GlobalParams.getHostBean;

/**
 * Created by wenchao on 2016/1/14.
 */
public class GlobalConfig {
    /**
     * 是否debug模式
     */
    public static final boolean DEBUG = BuildConfig.IS_DEBUGABLE;

    public static final String BASE_HOST = MetaDataUtil.getMetaData("BASE_HOST");

    /**
     * 渠道名
     */
    public static String CHANNEL = BuildConfig.FLAVOR;

    static {
        String chanel = BuildConfig.FLAVOR;
        if (chanel.contains("_")) {
            CHANNEL = chanel.replace("_", "");
        }
    }

    /**
     * 版本代号
     */
    public static final String versionCode = String.valueOf(BuildConfig.VERSION_CODE);

    /**
     * 版本名称
     */
    public static final String versionName = BuildConfig.VERSION_NAME;

    /**
     * 安装器包名
     */
    public static String NET_INSTALL_PACKAGE = Constant.DEF_INSTALL_PKG;

    /**
     * 第一页的页码
     */
    public static final int FIRST_PAGE = 1;
    /**
     * 带有小红点的按钮是否被点击过
     */
    public static boolean isOnclick = false;

    /**
     * 设备是否具备root权限
     */
    public static boolean deviceIsRoot = false;

    /**
     * 自动装
     */
    public static boolean isAutoInstall() {
        boolean noRootInstall = AutoInstallerContext.getInstance().getAccessibilityStatus() == AutoInstallerContext.STATUS_OPEN;
        return noRootInstall;
    }

    //wifi下自动升级  默认值
    private static final boolean AUTO_UPGRADE_ONLY_IN_WIFI_DEFAULT = false;
    //root自动安装默认   默认值
    private static final boolean AUTO_INSTALL_BY_ROOT_DEFAULT = false;
    //自动删除apk默认   默认值
    private static final boolean AUTO_DELETE_APK_DEFAULT = true;

    public static void setIsOpenAutoUpgradeApp(Context context, boolean b) {
//        PreferencesUtils.putBoolean(context,SharePreferencesKey.AUTO_UPGRADE_ONLY_IN_WIFI,b);
        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_OPEN_APP_AUTO_UPGRADE, b);
    }

    public static boolean getIsOpenAutoUpgradeApp(Context context) {
        return (boolean) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_OPEN_APP_AUTO_UPGRADE, true);
    }

    public static void setRootInstall(Context context, boolean b) {
        PreferencesUtils.putBoolean(context, SharePreferencesKey.AUTO_INSTALL_BY_ROOT, b);
    }

    /**
     * 获取用户自己选择的是否要root装
     */
    public static boolean getRootInstall(Context context) {
        return PreferencesUtils.getBoolean(context, SharePreferencesKey.AUTO_INSTALL_BY_ROOT, AUTO_INSTALL_BY_ROOT_DEFAULT);
    }

    public static void setRootInstallUserIsChange(Context context) {
        PreferencesUtils.putBoolean(context, SharePreferencesKey.INSTALL_BY_ROOT_USER_IS_CHANGE, true);
    }

    /**
     * 判断用户是否自己点击过root装设置
     */
    public static boolean getRootInstallUserIsChange(Context context) {
        return PreferencesUtils.getBoolean(context, SharePreferencesKey.INSTALL_BY_ROOT_USER_IS_CHANGE, false);
    }

    /**
     * 判断能否进行root装（两个条件必须同时满足）
     */
    public static boolean canRootInstall(Context context) {
        return deviceIsRoot && getRootInstall(context);
    }

    public static void setAutoDeleteApk(Context context, boolean b) {
        PreferencesUtils.putBoolean(context, SharePreferencesKey.AUTO_DELETE_APK, b);
    }

    public static boolean getAutoDeleteApk(Context context) {
        return PreferencesUtils.getBoolean(context, SharePreferencesKey.AUTO_DELETE_APK, AUTO_DELETE_APK_DEFAULT);
    }

    public static boolean getIsOnClick(Context context) {
        return PreferencesUtils.getBoolean(context, SharePreferencesKey.IS_ONCLICK, isOnclick);
    }

    public static boolean setIsOnClick(Context context, boolean b) {
        return PreferencesUtils.putBoolean(context, SharePreferencesKey.IS_ONCLICK, b);
    }

    /**
     * 组合图片 url
     *
     * @param imageUrl
     * @return
     */
    public static String combineImageUrl(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return "";
        }

        String realUrl = "";
        if (imageUrl.contains("http://") || imageUrl.contains("https://")) {
            realUrl = imageUrl;
        } else {
            String image_host = GlobalParams.getHostBean().getImage_host();
            String convert_to_jpg = GlobalParams.getHostBean().getSettings().getConvert_to_jpg();

            //正式
            realUrl = image_host + imageUrl + convert_to_jpg;
            //测试
//           realUrl = GlobalParams.getHostBean().getImage_host() + imageUrl;
        }
        return realUrl;
    }

    /**
     * 组合 下载链接url
     *
     * @param downloadUrl
     * @return
     */
    public static String combineDownloadUrl(String downloadUrl) {
        if (TextUtils.isEmpty(downloadUrl)) {
            return "";
        }
        String realUrl = "";
        if (downloadUrl.contains("http://") || downloadUrl.contains("https://")) {
            realUrl = downloadUrl;
        } else {
            realUrl = getHostBean().getApp_host() + downloadUrl;
        }
        return realUrl;
    }

    /**
     * 图片适配
     * @param imageView
     * @return public static BitmapImageViewTarget getBitmapImageViewTarget(final ImageView imageView){
    return new BitmapImageViewTarget(imageView){
    @Override public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
    super.onResourceReady(resource, glideAnimation);
    int width = resource.getWidth();
    int height =resource.getHeight();
    float widthIMG = imageView.getWidth();


    float scale = 0;
    //图片宽度和imageView宽度的比例
    scale= widthIMG/(float) width;

    Matrix matrix = new Matrix();
    matrix.postScale(scale,scale);
    Bitmap resizeBmp = Bitmap.createBitmap(resource, 0, 0, width, height, matrix, true);
    imageView.setImageBitmap(resizeBmp);
    }
    };
    }
     */

    /**
     * 图片适配
     *
     * @param imageViewWidth
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    public static int getImageViewHeight(int imageViewWidth, int imageWidth, int imageHeight) {
        float scale = 0;
        //图片宽度和imageView宽度的比例
        scale = (float) imageViewWidth / (float) imageWidth;
        int heightIMG = (int) (scale * (float) imageHeight);

        return heightIMG;
    }


}
