package cn.lt.android.plateform.update;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;

import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.install.AdMd5;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.install.root.ShellUtils;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.plateform.update.manger.UpdatePathManger;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.FileUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.framework.util.PreferencesUtils;

/***
 * Created by ATian on 2016/3/9.
 */
@SuppressWarnings("ALL")
public class UpdateServiceController {

    public final static String TAG = "UpdateService";
    private VersionInfo mUpdateInfo;
    private Context mContext;
    private Handler mHandler;
    public boolean isFromNotification;
    private boolean isLoading;
//    private boolean isPush;
    /**
     * 是否已经调用通知栏已经下载完成了。。。
     */
    private boolean isNotified;

    public UpdateServiceController(Context context, Handler mHandler) {
        this.mContext = context;
        this.mHandler = mHandler;
    }


    public boolean isFromNotification() {
        return isFromNotification;
    }

    public void setIsFromNotification(boolean isFromNotification) {
        this.isFromNotification = isFromNotification;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

//    public boolean isPush() {
//        return isPush;
//    }
//
//    public void setIsPush(boolean isPush) {
//        this.isPush = isPush;
//    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setIsNotified(boolean isNotified) {
        this.isNotified = isNotified;
    }

    /***
     * 是否为用户手动触发
     *
     * @param isUserBehavior
     */
    public synchronized void checkVersion(final boolean isUserBehavior) {
        checkUpdate(isUserBehavior);
    }

    public void dealWithIntentData(Intent intent) {
        final String action = intent.getStringExtra(PlatUpdateAction.ACTION);
        if (PlatUpdateAction.ACTION_NOTIFICATION.equals(action)) {
            isFromNotification = true;
        } else {
            isFromNotification = false;
        }
//        isPush = intent.getBooleanExtra("isPush", false);
//        if (isPush) {//通知触发的下载请求在此处统计，其他统计在dialog提示框点击等处理；
//            intent.removeExtra("isPush");
//        }
    }

    /**
     * 检查是否需要升级；包含两种情况：
     * <p/>
     * 1、后台无升级提示；无升级信息提示则检查文件是否存在并删除；
     * <p/>
     * 2、有升级信息 ：
     * <p/>
     * a,已经下载完成，需要判断是否为系统应用或者有无root权限，1）、如果有权限则静默安装{安装失败则检查是否要弹框}；2）、无权限
     * 检查是否需要弹框提示； 无权限则检查是否需要弹框提示；
     * <p/>
     * b,下载中，
     * <p/>
     * c,待下载；
     */
    private void checkUpdate(boolean isUserBehaior) {
        try {
            mUpdateInfo = VersionCheckManger.getInstance().getmVersionInfo();
            if (TextUtils.isEmpty(mUpdateInfo.getmUpgradeVersion()) && !isUpdateing()) {// 不需要升级；//
                // 判断是否有在下载，无则删除文件；
                LogUtils.i(TAG, "do not need update。。。");
                FileUtil.delFile(new File(UpdatePathManger.getDownloadFilePath(mContext)));
                mHandler.sendMessage(mHandler.obtainMessage(PlatUpdateService.HANDLER_WAHT_STOP_SERVICE, 0));
            } else { // 可以升级；
                checkRootPermission(mContext);
                if (UpdateUtil.isDowloaded(mContext)) { // 已经下载完成；
                    if (isFromNotification && !isNotified) {
                        isNotified = true;
                    }
                    LogUtils.i(TAG, "already downloaded,install the game now");
                    installApk(isUserBehaior);
                } else { // 下载中或者待下载；
                    if (isUpdateing()) { // 正在下载中；
                        LogUtils.i(TAG, "downloading please wait a moment!");
                    } else { // 未下载，待下载；
                        LogUtils.i(TAG, "start download..");
                        FileUtil.delFile(new File(UpdatePathManger.getDownloadFilePath(mContext)));
                        startDownload(isUserBehaior, isFromNotification ? true : false);
                    }
                    if (PopWidowManageUtil.needShowClientUpdateDialog(mContext)) {
                        LogUtils.i(TAG, "popup the window to prompt user start upgrade!");
                        showDialog(mUpdateInfo.isForce(), false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkRootPermission(final Context context) {
        if (!GlobalConfig.deviceIsRoot) {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    if (PackageUtils.isSystemApplication(context) || ShellUtils.checkRootPermission()) {
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    GlobalConfig.deviceIsRoot = result;
                    super.onPostExecute(result);

                }
            }.execute();
        } else {
            LogUtils.i("zzz", "已获取Root权限，不再检查Root");
        }
    }

    /**
     * 启动下载
     *
     * @param isUserBehavori 用户行为:来自页面还是服务
     * @param isPush         是否为个推推送的升级
     */
    private void startDownload(boolean isUserBehavori, boolean isFromNotification) {
        String source = isUserBehavori ? (isFromNotification ? "push" : "page") : "service";
        if (NetUtils.netWorkConnection(mContext)) {
            String url = mUpdateInfo.getDownloadUrl();
            if (isUserBehavori || (!isUserBehavori && NetUtils.isWifi(mContext))) {
                LogUtils.i(TAG, "down load");
                isLoading = true;
                mHandler.sendMessage(mHandler.obtainMessage(PlatUpdateService.HANDLER_WAHT_START_DOWN, GlobalConfig.combineDownloadUrl(url)));
                DCStat.platUpdateEvent("request", source, UpdateUtil.getPlatUpgradeType(), "", "", "");
            }
        } else {
            mHandler.sendEmptyMessage(PlatUpdateService.HANDLER_WAHT_NETWORK_ERROR);
            DCStat.platUpdateEvent("download_error", source, UpdateUtil.getPlatUpgradeType(), "", "netError", "there is no network");
        }

    }

    /**
     * 按照要求显示dialog
     *
     * @param isForce 是否为强制升级样式；
     */
    private void showDialog(final boolean isForce, boolean isDownload) {
        mHandler.sendMessage(mHandler.obtainMessage(PlatUpdateService.HANDLER_WAHT_SHOW_DIALOG, isForce));
    }


    /**
     * 判断是否在升级下载进行中；
     *
     * @return
     */
    private boolean isUpdateing() {
        return isLoading;
    }

    /***
     * 给后台检测平台升级服务调用
     * 1.先检测内存是否充足
     * 2.检验ＭＤ５值是否匹配
     * 3.然后判断是用户手动执行的安装还是服务执行的安装
     */
    public void installApk(boolean isUserBehavior) {
        UpdateUtil.modifyPermission(UpdatePathManger.getDownloadFilePath(mContext));
        if (!StorageSpaceDetection.outOfMemory2()) {
            showToastOnMainThread("内存不足，无法完成安装");
            DCStat.platUpdateEvent("install_error", isUserBehavior ? "page" : "service", UpdateUtil.getPlatUpgradeType(), "", "memoryError", "内存空间不足");
        } else {
            if (UpdateUtil.isDowloaded(mContext)) {
                String fileMd5 = AdMd5.md5sum(UpdatePathManger.getDownloadFilePath(mContext));//下载的文件的MD5值
                String localMD5 = PreferencesUtils.getString(mContext, "MD5", "");//服务器上请求的下来的MD5值
                LogUtils.i(TAG, "fileMd5/localMD5====" + fileMd5 + "/" + localMD5.toUpperCase());
                if (fileMd5.equals(localMD5.toUpperCase())) {
                    LogUtils.i(TAG, "MD5值匹配，允许安装");
                    if (isUserBehavior) {
                        LogUtils.i(TAG, "用户检测到的安装");
                        // 安装；
                        PackageUtils.installNormal(mContext, UpdatePathManger.getDownloadFilePath(mContext), null); //TODO
                        int autoInstall = AutoInstallerContext.getInstance().getAccessibilityStatus();
                        DCStat.platUpdateEvent("install", "page", UpdateUtil.getPlatUpgradeType(), autoInstall == 1 ? "auto" : "manual", "", "");//上报安装
                        PreferencesUtils.putString(mContext, "installMode", "manual");
                    } else {
                        if (!UpdateUtil.isForeground(mContext)) {
                            LogUtils.i(TAG, "在后台运行，允许静默装");
                            if (PackageUtils.isSystemApplication(mContext)) {
                                LogUtils.i(TAG, "系统权限静默安装");
                                try {
                                    InstallManager.getInstance().systemInstall(UpdatePathManger.getDownloadFilePath(mContext));
                                    DCStat.platUpdateEvent("install", "service", UpdateUtil.getPlatUpgradeType(), "system", "", "");
                                    PreferencesUtils.putString(mContext, "installMode", "system");
                                } catch (Exception e) {
                                    LogUtils.i(TAG, "系统权限静默安装失败");
                                    e.printStackTrace();
                                    DCStat.platUpdateEvent("install_error", "service", UpdateUtil.getPlatUpgradeType(), "system", "installError", "系统装异常：" + e.getMessage());
                                }
                                return;
                            }
                            //如果有Root权限并且是解锁状态，执行Root安装
                            else if (ShellUtils.checkRootPermission()) {
                                LogUtils.i(TAG, "有Root权限，准备静默安装");
                                int returnCode = 0;
                                try {
                                    DCStat.platUpdateEvent("install", "service", UpdateUtil.getPlatUpgradeType(), "root", "", "");
                                    PreferencesUtils.putString(mContext, "installMode", "root");
                                    returnCode = PackageUtils.installSilent(mContext, UpdatePathManger.getDownloadFilePath(mContext));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LogUtils.i(TAG, "Root权限安装");
                                    DCStat.platUpdateEvent("install_error", "service", UpdateUtil.getPlatUpgradeType(), "root", "installError", "Root装异常：" + e.getMessage());
                                }
                            }
                        } else {
                            LogUtils.i(TAG, "非后台运行，不允许静默装");
                        }
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(PlatUpdateService.HANDLER_WAHT_STOP_SERVICE, 5 * 1000));
                } else {
                    LogUtils.i(TAG, "MD5值不匹配，重新下载");
                    FileUtil.delFile(new File(UpdatePathManger.getDownloadFilePath(mContext)));
                    if (isUserBehavior) {
                        showToastOnMainThread("MD5值不匹配，请重新下载");//静默安装不弹吐司
                    }
                    DCStat.platUpdateEvent("install_error", isUserBehavior ? "page" : "service", UpdateUtil.getPlatUpgradeType(), "", "packageError", "MD5 no match");
                }
            } else {
                DCStat.platUpdateEvent("install_error", isUserBehavior ? "page" : "service", "", "packageError", UpdateUtil.getPlatUpgradeType(), "package is not exist");
            }
        }

    }

    private void showToastOnMainThread(final String msg) {
        LTApplication.getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
