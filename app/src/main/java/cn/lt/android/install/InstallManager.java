package cn.lt.android.install;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.autoinstall.AccessibilityService;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.install.system.OnInstalledListener;
import cn.lt.android.install.system.SystemInstallerManager;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckIsApkFile;
import cn.lt.android.util.FileUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ServiceUtil;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.ApkPackageDifferentHolder;
import cn.lt.android.widget.dialog.holder.ApkSignatureErrorHolder;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.exception.LTException;
import cn.lt.framework.util.ToastUtils;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by wenchao on 2016/1/22.
 * 安装管理器
 */
public class InstallManager {

    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 签名不一致安装失败的应用集合
     */
    private Map<String, AppEntity> signErrorList = new ConcurrentHashMap<>();

    /**
     * 用于在系统权限或root权限安装时，临时保存AppEntity
     */
    private Map<String, AppEntity> appMap = new HashMap<>();
    private boolean isOutOfMem;
    private boolean isOnekeyInstall = false;
    private String pageName;
    private String pageId;
    private boolean autoInstall, isSystemApp, isRoot;
    private String installMode = "manual";


    public void start(final AppEntity appEntity, final String pageName, final String pageId, boolean isOneketInstall) {
        this.isOnekeyInstall = isOneketInstall;
        this.pageName = pageName;
        this.pageId = pageId;

        autoInstall = GlobalConfig.isAutoInstall();
        isSystemApp = PackageUtils.isSystemApplication(LTApplication.shareApplication());
        isRoot = GlobalConfig.canRootInstall(LTApplication.shareApplication());

        // 判断installMode
        confirmInstallMode();

        // 检测apk各种异常情况并上报数据
        checkApkAndReport(appEntity, pageName, pageId);

        // 必须先检测是否符合执行安装条件
        if (canInstall(appEntity)) {

            ThreadPoolProxyFactory.getCachedThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    doInstall(LTApplication.shareApplication(), appEntity, appEntity.getSavePath());
                    LogUtils.i("juice", "doInstall执行完毕");
                }
            });

        }
    }

    /**
     * 检测apk各种异常情况并上报数据
     */
    private boolean canInstall(AppEntity appEntity) {
        /* 严格按照优先级顺序执行*/

        // 检测apk是否存在
        if (needStopInstall(appEntity.getSavePath())) {
//            postError(appEntity);
            DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "packageError", "安装包不存在");
            resetStatusToDownloaded(appEntity);
            return false;
        }

        // 检测下载完成的apk包名是否一致，否则不执行安装
        String localPkgName = CheckIsApkFile.getPackageNameByPackageManager(appEntity.getSavePath(), LTApplication.shareApplication());
        if (!TextUtils.isEmpty(localPkgName) && !appEntity.getPackageName().equals(localPkgName)) {
            resetStatusToInstallfail(appEntity);
            DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "packageError", "下载请求的包名与安装的包名不一致");
            return false;
        }

        // 检测内存空间是否足够
        if (StorageSpaceDetection.getAvailableSize() <= Long.valueOf(appEntity.getPackageSize())) {
            StorageSpaceDetection.showEmptyTips(ActivityManager.self().topActivity(), LTApplication.shareApplication().getString(R.string.memory_install_error));
            saveInstallFailureAndPostEvent(appEntity);
            resetStatusToDownloaded(appEntity);

            DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "memoryError", "手机剩余空间不足，无法安装");
            return false;
        }

        return true;
    }


    private void checkApkAndReport(final AppEntity appEntity, final String pageName, final String pageId) {
        ThreadPoolProxyFactory.getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Observable.just(appEntity).filter(new Func1<AppEntity, Boolean>() {
                        @Override
                        public Boolean call(AppEntity entity) {
                            return !needStopInstall(appEntity.getSavePath());  //file存在
                        }
                    }).filter(new Func1<AppEntity, Boolean>() {
                        @Override
                        public Boolean call(AppEntity entity) {
                            return !CheckIsApkFile.checkIsApkisComplete(appEntity.getSavePath(), LTApplication.shareApplication()); //不是完整apk
                        }
                    }).subscribe(new Action1<AppEntity>() {
                        @Override
                        public void call(AppEntity entity) {
                            boolean isApkFile = CheckIsApkFile.isApkFile(appEntity.getSavePath());
                            DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "packageError", "安装包解析出错" + (isApkFile ? "包不完整" : "不是apk文件"));
                        }
                    });

                    checkFileMD5(appEntity);//检测文件MD5值并上报
                    LogUtils.i("juice", "检测安装包即将完毕");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });  //检测和安装需要异步执行
    }

    private void confirmInstallMode() {
        if (isSystemApp) {
            installMode = "system";
        } else if (autoInstall) {
            installMode = "auto";
        } else if (isRoot) {
            installMode = "root";
        } else {
            installMode = "manual";
        }
    }
    /**
     * 保存安装失败状态并发出通知
     */
    private void saveInstallFailureAndPostEvent(AppEntity appEntity) {
        //存数据库-
        appEntity.setLackofmemory(true);
        GlobalParams.getAppEntityDao().insertOrReplace(appEntity);

        EventBus.getDefault().post(new InstallEvent(appEntity, InstallEvent.INSTALL_FAILURE, appEntity.getAppClientId()));
    }

    private void doInstall(final Context context, AppEntity appEntity, String apkPath) {
        String installWay = isOnekeyInstall ? "onekey" : "single";
        LTApplication.installStatAppList.put(appEntity.getPackageName(), appEntity);
        // 修改文件权限
        try {
            String cmd = "chmod 777 " + apkPath;
            Runtime.getRuntime().exec(cmd);
            //消息提示
            LogUtils.i("ccc", "开始安装了");

            InstalledLooperProxy.getInstance().startInstall(appEntity); //轮询监控
            if (isSystemApp) {
                //系统权限安装
                try {
                    appMap.put(appEntity.getSavePath(), appEntity);
                    DCStat.updateInstallMode("system", installWay, appEntity);
                    DCStat.installEvent(appEntity, isOnekeyInstall, "system", pageName, pageId, "", "");//系统装数据上报
                    addInstallingApp(appEntity.getPackageName());
                    EventBus.getDefault().post(new InstallEvent(appEntity.getPackageName(), InstallEvent.INSTALLING));
                    systemInstall(apkPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    //安装權限異常
                    if (e instanceof InvocationTargetException) {
                        LogUtils.e("InstallManager", "系统装异常:" + ((InvocationTargetException) e).getTargetException());
                        DCStat.installEvent(appEntity, isOnekeyInstall, "system", pageName, pageId, "InstallError", "系统装异常：" + ((InvocationTargetException) e).getTargetException());//系统装异常数据上报
                    }
                    //采用root pm命令安装

                    rootInstall(context, apkPath);
                    //移除轮询器中的监控任务
                    InstalledLooperProxy.getInstance().removeLooperEntity();
                }
            } else if (autoInstall) {
                DCStat.updateInstallMode("auto", installWay, appEntity);
                removeInstallingApp(appEntity.getPackageName());
                LogUtils.i("InstallManager", "自动装服务安装");
                // 如果开启自动装
                normalInstall(context, apkPath, appEntity);

                boolean isServiceExist = ServiceUtil.isServiceRunning(context, AccessibilityService.class);
                LogUtils.i("Erosion", "isServiceExist====" + isServiceExist);
                DCStat.installEvent(appEntity, isOnekeyInstall, isServiceExist ? "auto" : "manual", pageName, pageId, "", "");//自动装数据上报
            } else if (isRoot) {

                DCStat.updateInstallMode("root", installWay, appEntity);
                appMap.put(appEntity.getSavePath(), appEntity);

                DCStat.installEvent(appEntity, isOnekeyInstall, "root", pageName, pageId, "", "");//Root装数据上报

                addInstallingApp(appEntity.getPackageName());
                EventBus.getDefault().post(new InstallEvent(appEntity.getPackageName(), InstallEvent.INSTALLING));
                //root权限安装
                LogUtils.i("InstallManager", "Root装开始执行+path" + apkPath);
                rootInstall(context, apkPath);
            } else {

                removeInstallingApp(appEntity.getPackageName());

                LogUtils.i("InstallManager", "正常安装装开始执行+path" + apkPath);
                if (!isAppInstalling(appEntity.getPackageName())) {
                    //这里干嘛用？会影响安装的数据上报
                }
                DCStat.installEvent(appEntity, isOnekeyInstall, "manual", pageName, pageId, "", "");//正常装数据上报(要区分是单个安装还是一键安装)
                // 如果没有设置自动装，采用正常安装方式
                normalInstall(context, apkPath, appEntity);   //保存一个APPEntity，requestCode\
                if (pageName.equals(Constant.QUIT_DIALOG)) {
                    installWay = "onekey_exit";
                }
                DCStat.updateInstallMode("manual", installWay, appEntity);  //同步更新模式。

            }

        } catch (Exception e) {
//            doFailure(context, e, apkPath);
            /*产品要求不管安装有没有异常，都上报，误删*/
            DCStat.installEvent(appEntity, isOnekeyInstall, "system/root/auto/manual", pageName, pageId, "InstallingError", "安装异常：" + e.getMessage());
            //发送安装失败状态
            EventBus.getDefault().post(new InstallEvent(appEntity, appEntity.getPackageName(), InstallEvent.INSTALL_FAILURE));
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();

            LTApplication.installStatAppList.remove(appEntity.getPackageName());
        }
        LogUtils.i("InstallManager", "install 的最后+path");

    }

    private boolean needStopInstall(String apkPath) {
        File file = new File(apkPath);
        if (!file.exists() || !file.isFile() || file.length() <= 0) {
            LogUtils.i("ccc", "文件不存在，或不是文件，不执行安装");
            return true;
        }
        return false;
    }

    /**
     * 验证应用签名(普通权限安装 或者 root权限没开启静默装的)
     */
    private void verifySignature(final AppEntity appEntity, final boolean isWeizhicuoWU) {

        // 系统权限 或者 root权限并开启静默装的不执行
        if (!isWeizhicuoWU && (isSystemApp || (isRoot))) {
            LogUtils.i("Erosion","root权限并开启静默装的不执行");
            return;
        }

        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return ApkSignatureCompare.isSignatureSame(LTApplication.instance, appEntity.getSavePath(), appEntity.getPackageName());
            }

            @Override
            protected void onPostExecute(Boolean isSame) {
                super.onPostExecute(isSame);
                if (!isSame) {
                    showApkSignatureErrorDialog(appEntity, isWeizhicuoWU);
                    postInstallFailureSign(appEntity);
                }
            }
        }.execute();

    }

    /**
     * 安装时签名错误通知
     * @param appEntity
     */
    private void postInstallFailureSign(AppEntity appEntity) {
        appEntity.setErrorType((long)DownloadStatusDef.COMPLETE_SIGN_FAIL);
        EventBus.getDefault().post(new InstallEvent(appEntity, appEntity.getPackageName(), InstallEvent.INSTALL_FAILURE));
        GlobalParams.getAppEntityDao().insertOrReplace(appEntity);
    }

    /**
     * 签名不对的包弹出弹窗提示
     */
    private void showApkSignatureErrorDialog(AppEntity appEntity, boolean isWeizhicuoWU) {
        DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "packageError", isWeizhicuoWU ? "签名不一致_静默装失败未知原因重检测" : "签名不一致");//上报签名不一致
//        postInstallFailureSign(appEntity);
        // 亮屏时才弹
        if (AppUtils.isScreenOn()) {
            // 弹出卸载提示窗口
            try {
                new PublicDialog(ActivityManager.self().topActivity(), new ApkSignatureErrorHolder()).showDialog(new DataInfo(appEntity));
            } catch (Exception e) {
                e.printStackTrace();
                cn.lt.android.util.ToastUtils.showToast("您的手机存在签名冲突的同名安装包，请先卸载 " + appEntity.getName() + " , 才能进行安装。");
            }
        }

    }


    /**
     * 校验游戏是否能安装
     *
     * @return
     */

    private void checkFileMD5(AppEntity appEntity) throws Exception {
        String netMd5 = appEntity.getPackage_md5();
        if (TextUtils.isEmpty(netMd5)) return;
        if (appEntity.getSavePath() == null) {
            throw new LTException("down path is null");
        }
        File saveFile = new File(appEntity.getSavePath());
        if (!saveFile.exists()) {
            throw new LTException("apk is not exists : " + appEntity.getSavePath());
        }
        // MD5校验
        String localMd5 = AdMd5.md5sum(appEntity.getSavePath());
        LogUtils.i("MD5", "netMd5/localMd5" + netMd5 + "/" + localMd5);
        if (!netMd5.equalsIgnoreCase(localMd5)) {
            DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "packageError", "MD5值不匹配");
        }
        LogUtils.i("InstallPkgSizeCp", "checkFileMD5 安装包存在吗？" + saveFile.exists());
    }


    public void doFailure(final Context context, Exception e, String apkPath) {
        e.printStackTrace();
        if (apkPath != null) {
            FileUtil.deleteFile(apkPath);
        }
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                ToastUtils.show(context, "安装失败");
            }
        });
    }

    /**
     * root权限安装
     *
     * @param context
     * @param apkFile
     */
    private void rootInstall(final Context context, String apkFile) {
        int returnCode = PackageUtils.installSilent(context, apkFile);
        installResultHandle(context, "root", returnCode, apkFile);
    }

    /**
     * 普通安装方式
     *
     * @param context
     * @param apkPath
     * @param appEntity
     */
    private void normalInstall(Context context, String apkPath, AppEntity appEntity) {
        PackageUtils.installNormal(context, apkPath, appEntity);

        // 升级的应用，需要验证签名
        if (AppUtils.isInstalled(context, appEntity.getPackageName())) {
            // 验证签名
            verifySignature(appEntity, false);
        }
    }

    /**
     * 系统app权限安装
     *
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void systemInstall(final String apkFilePath) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        SystemInstallerManager installerManager = new SystemInstallerManager(LTApplication.shareApplication());
        //设置监听
        installerManager.setOnInstalledPackaged(new OnInstalledListener() {
            @Override
            public void packageInstalled(String packageName, int returnCode) {
                installResultHandle(LTApplication.shareApplication(), "system", returnCode, apkFilePath);
            }
        });
        installerManager.installPackage(apkFilePath);
    }

    private void  installResultHandle(final Context context, final String installType, int returnCode, String apkFilePath) {

        final AppEntity appEntity = appMap.get(apkFilePath);
        String resultInfo = "";

        switch (returnCode) {
            case PackageUtils.INSTALL_FAILED_ALREADY_EXISTS:
                resultInfo = "安装失败，包已安装_系统";
                showToastOnUIThread(context, "安装失败，包已安装");
                break;
            case PackageUtils.INSTALL_FAILED_INVALID_APK:
                resultInfo = "解析包错误_系统";
                showToastOnUIThread(context, "安装失败，无效的包");
//                DCStat.installEvent(appEntity, isOnekeyInstall, installType, pageName, pageId, "packageError", "解析包错误");//放到最后统一上报
                break;
            case PackageUtils.INSTALL_FAILED_INVALID_URI:
                resultInfo = "安装失败，无效的包路径_系统";
                showToastOnUIThread(context, "安装失败，无效的包路径");
                break;
            case PackageUtils.INSTALL_FAILED_INSUFFICIENT_STORAGE:
                isOutOfMem = true;
                resultInfo = "安装失败，存储空间不足_系统";
                //                showToastOnUIThread(context, "安装失败，存储空间不足");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        StorageSpaceDetection.showEmptyTips(ActivityManager.self().topActivity(), LTApplication.shareApplication().getString(R.string.memory_install_error));
                        DCStat.installEvent(appEntity, isOnekeyInstall, installType, pageName, pageId, "memoryError", "手机空间不足");
                    }
                });
                //发送安装失败状态
                if (appEntity != null) {
                    EventBus.getDefault().post(new InstallEvent(appEntity, appEntity.getPackageName(), InstallEvent.INSTALL_FAILURE));
                }
                saveInstallFailureAndPostEvent(appEntity);
                break;
            case PackageUtils.INSTALL_FAILED_DUPLICATE_PACKAGE:
                resultInfo = "安装失败，重复的包_系统";
                showToastOnUIThread(context, "安装失败，重复的包");
                break;
            case PackageUtils.INSTALL_FAILED_NO_SHARED_USER:
                resultInfo = "安装失败，No shared user_系统";
                showToastOnUIThread(context, "安装失败，No shared user");
                break;
            case PackageUtils.INSTALL_FAILED_UPDATE_INCOMPATIBLE:
                resultInfo = "签名不一致_系统";
                showToastOnUIThread(context, "安装失败，更新不兼容");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showApkSignatureErrorDialog(appEntity, false);
                    }
                });
                break;
            case PackageUtils.INSTALL_FAILED_SHARED_USER_INCOMPATIBLE:
                resultInfo = "安装失败，shared user不兼容_系统";
                showToastOnUIThread(context, "安装失败，shared user不兼容");
                break;
            case PackageUtils.INSTALL_FAILED_MISSING_SHARED_LIBRARY:
                resultInfo = "安装失败，missing shared library_系统";
                showToastOnUIThread(context, "安装失败，missing shared library!");
                break;
            case PackageUtils.INSTALL_FAILED_REPLACE_COULDNT_DELETE:
                resultInfo = "安装失败，replace couldnt delete_系统";
                showToastOnUIThread(context, "安装失败，replace couldnt delete!");
                break;
            case PackageUtils.INSTALL_FAILED_DEXOPT:
                resultInfo = "安装失败，dexopt_系统";
                showToastOnUIThread(context, "安装失败，dexopt!");
                break;
            case PackageUtils.INSTALL_FAILED_OLDER_SDK:
                resultInfo = "安装失败，older sdk_系统";
                showToastOnUIThread(context, "安装失败，older sdk!");
                break;
            case PackageUtils.INSTALL_FAILED_CONFLICTING_PROVIDER:
                resultInfo = "安装失败，conflicting provider_系统";
                showToastOnUIThread(context, "安装失败，conflicting provider");
                break;
            case PackageUtils.INSTALL_FAILED_NEWER_SDK:
                resultInfo = "安装失败，newer sdk_系统";
                showToastOnUIThread(context, "安装失败，newer sdk!");
                break;
            case PackageUtils.INSTALL_FAILED_TEST_ONLY:
                resultInfo = "安装失败，whetherBusiness only_系统";
                showToastOnUIThread(context, "安装失败，whetherBusiness only!");
                break;
            case PackageUtils.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE:
                resultInfo = "安装失败，cpu abi incompatible_系统";
                showToastOnUIThread(context, "安装失败，cpu abi incompatible!");
                break;
            case PackageUtils.INSTALL_FAILED_MISSING_FEATURE:
                resultInfo = "安装失败，missing feature_系统";
                showToastOnUIThread(context, "安装失败，missing feature!");
                break;
            case PackageUtils.INSTALL_FAILED_CONTAINER_ERROR:
                resultInfo = "安装失败，container error_系统";
                showToastOnUIThread(context, "安装失败，container error!");
                break;
            case PackageUtils.INSTALL_FAILED_INVALID_INSTALL_LOCATION:
                resultInfo = "安装失败，无效的安装路径_系统";
                showToastOnUIThread(context, "安装失败，无效的安装路径!");
                break;
            case PackageUtils.INSTALL_FAILED_MEDIA_UNAVAILABLE:
                resultInfo = "安装失败，media unavailable_系统";
                showToastOnUIThread(context, "安装失败，media unavailable!");
                break;
            case PackageUtils.INSTALL_FAILED_VERIFICATION_TIMEOUT:
                resultInfo = "安装失败，verification timeout_系统";
                showToastOnUIThread(context, "安装失败，verification timeout!");
                break;
            case PackageUtils.INSTALL_FAILED_VERIFICATION_FAILURE:
                resultInfo = "安装失败，verification failure_系统";
                showToastOnUIThread(context, "安装失败，verification failure!");
                break;
            case PackageUtils.INSTALL_FAILED_PACKAGE_CHANGED:
                resultInfo = "安装失败，package changed_系统";
                showToastOnUIThread(context, "安装失败，package changed!");
                break;
            case PackageUtils.INSTALL_FAILED_UID_CHANGED:
                resultInfo = "安装失败，uid changed_系统";
                showToastOnUIThread(context, "安装失败，uid changed!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_NOT_APK:
                resultInfo = "安装失败，解析失败not apk_系统";
                showToastOnUIThread(context, "安装失败，解析失败not apk!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_BAD_MANIFEST:
                resultInfo = "安装失败，bad manifest_系统";
                showToastOnUIThread(context, "安装失败，bad manifest");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION:
                resultInfo = "安装失败，unexpected exception_系统";
                showToastOnUIThread(context, "安装失败，unexpected exception!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_NO_CERTIFICATES:
                resultInfo = "安装失败，no certificates_系统";
                showToastOnUIThread(context, "安装失败，no certificates!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES:
            case PackageUtils.INSTALL_FAILED_DUPLICATE_PERMISSION:
                resultInfo = "安装失败，inconsistent certificates_系统";
                showToastOnUIThread(context, "安装失败，inconsistent certificates!");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showApkSignatureErrorDialog(appEntity, false);
                    }
                });
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING:
                resultInfo = "安装失败，certificate encoding_系统";
                showToastOnUIThread(context, "安装失败，certificate encoding!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME:
                resultInfo = "安装失败，bad package name_系统";
                showToastOnUIThread(context, "安装失败，bad package name!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID:
                resultInfo = "安装失败，bad shared user id_系统";
                showToastOnUIThread(context, "安装失败，bad shared user id!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED:
                resultInfo = "安装失败，manifest malformed_系统";
                showToastOnUIThread(context, "安装失败，manifest malformed!");
                break;
            case PackageUtils.INSTALL_PARSE_FAILED_MANIFEST_EMPTY:
                resultInfo = "安装失败，manifest empty_系统";
                showToastOnUIThread(context, "安装失败，manifest empty!");
                break;
            case PackageUtils.INSTALL_FAILED_INTERNAL_ERROR:
                resultInfo = "安装失败，internal error_系统";
                showToastOnUIThread(context, "安装失败，internal error!");
                break;
            case PackageUtils.INSTALL_FAILED_OTHER:
                resultInfo = "安装失败，未知原因_系统";
                showToastOnUIThread(context, "安装失败，未知原因!");
                verifySignature(appEntity, true);
                break;
        }

        if (PackageUtils.INSTALL_SUCCEEDED == returnCode) {
            //此处不做处理，在接收到广播后统一处理】
        } else {
            DCStat.installEvent(appEntity, isOnekeyInstall, installMode, pageName, pageId, "installError", resultInfo);
            boolean isSingError = (returnCode == PackageUtils.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES) || (returnCode == PackageUtils.INSTALL_FAILED_UPDATE_INCOMPATIBLE);
            if (!isOutOfMem && !appEntity.getIsAppAutoUpgrade() && !isSingError) {
                String installWay = isOnekeyInstall ? "onekey" : "single";
                //修复系统权限自动装上报install_mode错误bug
                if (autoInstall && returnCode != PackageUtils.INSTALL_FAILED_INVALID_APK) {
                    DCStat.updateInstallMode("auto", installWay, appEntity);
                    removeInstallingApp(appEntity.getPackageName());
                    LogUtils.i("InstallManager", "自动装服务安装");
                    // 如果开启自动装
                    normalInstall(context, apkFilePath, appEntity);

                    boolean isServiceExist = ServiceUtil.isServiceRunning(context, AccessibilityService.class);
                    LogUtils.i("Erosion", "isServiceExist====" + isServiceExist);
                    DCStat.installEvent(appEntity, isOnekeyInstall, isServiceExist ? "auto" : "manual", pageName, pageId, "", "");//自动装数据上报
                } else {
                    normalInstall(context, apkFilePath, appEntity);

                    //系统装失败后install_mode应为manual
                    DCStat.updateInstallMode("manual", installWay, appEntity);
                }
            } else {
                LTApplication.installStatAppList.remove(appEntity.getPackageName());
            }
            isOutOfMem = false;
            //            cn.lt.game.model.State.updateState(game, InstallState.install);

            // 发送下载完成的消息（为了让按钮状态变回“安装”）
            postDownloaded(appEntity);
        }

        appMap.remove(apkFilePath);
        if (appEntity != null) {
            removeInstallingApp(appEntity.getPackageName());
        }

        if(PackageUtils.INSTALL_FAILED_DUPLICATE_PERMISSION == returnCode
                || PackageUtils.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES == returnCode
                || PackageUtils.INSTALL_FAILED_UPDATE_INCOMPATIBLE == returnCode) {
            postInstallFailureSign(appEntity);
        }


    }

    /**
     * 重置状态为安装失败
     */
    private void resetStatusToInstallfail(final AppEntity appEntity) {
        // 更新按钮状态
        removeInstallingApp(appEntity.getPackageName());
        postDownloaded(appEntity);

        // 自动升级的不要弹窗
        if (appEntity.getIsAppAutoUpgrade()) {
            LogUtils.i("casc1121212", appEntity.getName() + "是自动升级，不弹出被挟持的窗口");
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // 弹出卸载提示窗口
                try {
                    new PublicDialog(ActivityManager.self().topActivity(), new ApkPackageDifferentHolder()).setKeybackNotClose().showDialog(new DataInfo(appEntity));
                } catch (Exception e) {
                    e.printStackTrace();
                    cn.lt.android.util.ToastUtils.showToast("您当前网络不安全，下载的“" + appEntity.getName() + "” 应用已被挟持，请切换网络重新下载！");
                }
            }
        });

    }

    /**
     * 使按钮状态变回安装
     */
    private void resetStatusToDownloaded(AppEntity appEntity) {
        if (isSystemApp || isRoot) {
            removeInstallingApp(appEntity.getPackageName());

            // 发送下载完成的消息（为了让按钮状态变回“安装”）
            postDownloaded(appEntity);
        }
    }

    private void postDownloaded(AppEntity appEntity) {
        int downloadId = DownloadTaskManager.getInstance().getDownloadId(appEntity);
        DownloadEvent downloadEvent = new DownloadEvent(downloadId, DownloadStatusDef.completed, appEntity.getPackageName(), appEntity.getSoFar(), appEntity.getTotal());
        EventBus.getDefault().post(downloadEvent);
    }

    private void postError(AppEntity appEntity) {
        InstallEvent installEvent = new InstallEvent(appEntity.getPackageName(), InstallEvent.INSTALL_FAILURE);
        EventBus.getDefault().post(installEvent);
    }

    private void showToastOnUIThread(final Context context, final String message) {
    }


    private InstallManager() {
    }

    private final static class HolderClass {
        private final static InstallManager INSTANCE = new InstallManager();
    }

    public static InstallManager getInstance() {
        return HolderClass.INSTANCE;
    }


    private List<String> installingApps = new ArrayList<String>();

    public void addInstallingApp(String pkg) {
        synchronized (installingApps) {
            if (!installingApps.contains(pkg)) {
                installingApps.add(pkg);
            }
        }
    }

    public boolean isAppInstalling(String pkg) {
        synchronized (installingApps) {
            return installingApps.contains(pkg);
        }
    }

    public void removeInstallingApp(String pkg) {
        synchronized (installingApps) {
            if (installingApps.contains(pkg)) {
                installingApps.remove(pkg);
            }
        }
    }

    public void addSignErrorList(AppEntity app) {
        signErrorList.put(app.getPackageName(), app);
    }

    public void removeSignErrorList(String packageName) {
        signErrorList.remove(packageName);
    }

    public boolean isSignError(String packageName) {
        return signErrorList.containsKey(packageName);
    }
}