package cn.lt.android.download;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import cn.lt.android.db.AppEntity;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;

/**
 * Created by wenchao on 2016/3/16.
 * 下载环境 检测器
 */
public class DownloadChecker {

    private boolean noNetworkPrompIsShow;

    private final static class HolderClass {
        private final static DownloadChecker INSTANCE = new DownloadChecker();
    }

    public static DownloadChecker getInstance() {
        return HolderClass.INSTANCE;
    }

    /**
     * 是否记住了3G直接下载
     */
    private boolean needToast = true;


    /**
     * 处理各种网络状态下的弹框提示
     *
     * @param context  顶部activity contex对象
     * @param executor 继续下载执行器
     */
    public void check(final Context context, final AppEntity appEntity, final String mPageName, final String pageID, final String mode, final Runnable executor) {
        catchNetworkStatusIfNeed(context, new Runnable() {
            @Override
            public void run() {
                if (null != appEntity) {
                    StorageSpaceDetection.check(context, appEntity, mPageName,pageID,mode, executor);
                } else {
                    StorageSpaceDetection.check(context, null, mPageName,pageID,mode, executor);
                }
            }
        });
    }

    /**
     * 处理各种网络状态下的弹框提示
     *
     * @param context  顶部activity contex对象
     * @param executor 继续下载执行器
     */
    private void catchNetworkStatusIfNeed(final Context context, final Runnable executor) {
        do {
//            if (noNetworkPromp(context)) break;
//            if (mobileNetworkPromp(context, executor)) break;
            wifiNetwork(executor);
        } while (false);
    }

    /**
     * 无网络提示
     *
     * @param context
     * @param orderWifiRunnable 点击预约wifi下载时的操作
     * @return true无网络， false有网络
     */
    public boolean noNetworkPromp(final Context context, final Runnable orderWifiRunnable) {
        if (noNetworkPrompIsShow) {
            return true;
        }

        //无网络情况,弹出对话框提醒
        if (!NetUtils.isConnected(context)) {
            new CustomDialog.Builder(context).setMessage(R.string.download_no_network).setPositiveButton(R.string.go_setting).setPositiveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    noNetworkPrompIsShow = false;
                }
            }).setNegativeButton(R.string.order_wifi_download).setNegativeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    noNetworkPrompIsShow = false;
                    orderWifiRunnable.run();
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    noNetworkPrompIsShow = false;
                }
            }).create().show();
            noNetworkPrompIsShow = true;
            return true;
        }
        return false;
    }

    /**
     * 手机网络状态时，点击下载弹出提醒框
     *
     * @param context         顶层Activity的context
     */
//    public boolean mobileNetworkPromp(Context context) {
//        if (NetUtils.isMobileNet(context)) {
////            downloadExcutor.run();
//            if (isNeedToast()) {
////                Toast.makeText(context, R.string.download_mobile_network_tips, Toast.LENGTH_LONG).show();
//                new CustomDialog.Builder(context).setMessage(R.string.download_mobile_network_tips).setPositiveButton(R.string.download_continue).setPositiveListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        return true;
//                    }
//                }).setNegativeButton(R.string.download_later).create().show();
//                return true;
//            }
//        }
//        return false;
//    }

    /**
     * 切换到手机网络提示框
     *
     * @param context 上下文
     * @param PositiveExecutor 点击确定键时执行的操作Runnable
     * @param NegativeExecutor 点击取消键时执行的操作Runnable
     */
    public void changeToMoileNetworkPromp(Context context, final Runnable PositiveExecutor, final Runnable NegativeExecutor) {
        if (NetUtils.isMobileNet(context)) {
            try {
                String dialogMessage = "";

                int downloadingCount = DownloadTaskManager.getInstance().getDownloadTaskList().size();

                if (downloadingCount != 0) {
                    dialogMessage = String.format(context.getResources().getString(R.string.download_mobile_network_tips), downloadingCount);
                }else{
                    dialogMessage = "当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？";
                }

                new CustomDialog.Builder(context).setMessage(dialogMessage)
                        .setPositiveButton(R.string.continue_mobile_download)
                        .setPositiveListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PositiveExecutor.run();
                            }
                        })
                        .setNegativeButton(R.string.order_wifi_download)
                        .setNegativeListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                NegativeExecutor.run();
                            }
                        })
                        .create().show();

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.i("showDialogException", "showDialogException");
            }
        }
    }


    private void wifiNetwork(Runnable executor) {
        executor.run();
    }


    /**
     * 3G是否直接下载
     *
     * @return
     */
    public boolean isNeedToast() {
        return needToast;
    }


}
