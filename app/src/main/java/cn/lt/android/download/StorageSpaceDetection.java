package cn.lt.android.download;


import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import java.util.Date;
import java.util.List;

import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.main.UIController;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.FileSizeUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.util.TimeUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;

/**
 * Created by wenchao on 2016/1/6.
 * 内存空间检测
 */
public class StorageSpaceDetection {

    private static boolean emptyTipsIsShow;

    private static final long GET_AVAILABLE_SIZE_ERROR = -100;

    public static final int STORAGE_IS_0 = 1;
    public static final int STORAGE_SMALL_200 = 2;
    public static final int NO_PROBLEM = 3;
    private static List<AppEntity> list;

    public static void check(Context context, AppEntity appEntity, String mPageName,String pageID, String mode, Runnable runnable) {
        long surplus = getAvailableSize();
        long limit = getMinSpaceConfig();
        long surplusM = surplus / (1048 * 1024);
        //最低值
        if (surplus != GET_AVAILABLE_SIZE_ERROR && surplusM <= 0) {
            LogUtils.i("zzz", "上报内存空间为零");
            DCStat.downloadFialedEvent(appEntity, mode, "memoryError", mPageName, "内存空间为0", "download",pageID);
        } else if (surplus != GET_AVAILABLE_SIZE_ERROR && surplusM <= limit) {
            showPathSettingDialog(context, limit);
            DCStat.downloadFialedEvent(appEntity, mode, "memoryError", mPageName, "内存空间不足200Ｍ", "download",pageID);//上报一键升级内存空间不足200M
            LogUtils.i("zzz", "内存空间不足200Ｍ");
        }
        runnable.run();//上报一键下载内存空间为零

        LogUtils.i("lujinkongjian", "有savePath的getAvailableSize = " + (surplus / (1048 * 1024)) + "M");
        LogUtils.i("lujinkongjian", "木有savePath的getAvailableSize = " + (getAvailableSize() / (1048 * 1024)) + "M");
        LogUtils.i("lujinkongjian", "获取手机内部剩余存储空间 = " + FileSizeUtil.getAvailableInternalMemorySize() / (1048 * 1024) + "M");
        LogUtils.i("lujinkongjian", "获取SDCARD剩余存储空间 = " + FileSizeUtil.getAvailableExternalMemorySize() / (1048 * 1024) + "M");
        LogUtils.i("lujinkongjian", "获取当前可用运行内存 = " + FileSizeUtil.getAvailableMemory(context) / (1048 * 1024) + "M");
    }

    /**
     * 检查内存大小，然后数据上报，给wifi自动下载提供
     */
    public static int check() {
        long surplus = getAvailableSize();
        long limit = getMinSpaceConfig();
        long surplusM = surplus / (1048 * 1024);
        //最低值
        if (surplusM <= 0) {
            LogUtils.i("zzz", "内存空间为0，数据上报");
            return STORAGE_IS_0;
        } else if (surplusM <= limit) {
            // TODO: 2016/3/31 空间小于限定值，结果上报
            LogUtils.i("zzz", "内存小于200M数据上报，可用内存:" + surplusM + "(内存不足200M)");
            return STORAGE_SMALL_200;
        }
        return NO_PROBLEM;
    }

    public static boolean outOfMemory2() {
        long surplus = getAvailableSize();
        long surplusM = surplus / (1048 * 1024);
        if (surplusM <= 0) {
            return false;
        }
        return true;
    }

    public static void showPathSettingDialog(final Context context, final long limit) {
        long lastTime = (Long) SharePreferenceUtil.getFromSpName(SharePreferenceUtil.DIALOG_WARN_NAME, SharePreferenceUtil.NOT_ENOUGH_200M_DOWN_TIPS_DATE, 0L);
        long curTime = System.currentTimeMillis();
        // 如果跟上一次弹出窗时间不是同一天，就再次弹出（每天第一次检测才弹出）
        if (lastTime == 0 || !TimeUtils.isSameDate(new Date(lastTime), new Date(curTime))) {
            try {
                new CustomDialog.Builder(context).setTitle("内存不足").setMessage(String.format(context.getString(R.string.storage_space_not_enough_change_path), limit)).setPositiveButton(R.string.clean).setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIController.goAppManager_uninstallPage(context);
                    }
                }).setNegativeButton(R.string.cancel).create().show();

                // 保存本次弹出日期到文件
                SharePreferenceUtil.putFromSpName(SharePreferenceUtil.DIALOG_WARN_NAME, SharePreferenceUtil.NOT_ENOUGH_200M_DOWN_TIPS_DATE, curTime);
            } catch (Exception e) {
                e.printStackTrace();
                LTApplication.getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        cn.lt.android.util.ToastUtils.showToast("手机空间小于" + limit + "MB，可能导致下载安装失败，请清理手机空间");
                    }
                });
            }
        }
    }

    public static void showEmptyTips(final Context context, String message) {
        if (emptyTipsIsShow) {
            return;
        }
        performDialog(context, message);

    }

    public static void performDialog(final Context context, String message) {
        try {
            new CustomDialog.Builder(context).setTitle("内存不足").setMessage(message).setPositiveButton(R.string.clean).setPositiveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIController.goAppManager_uninstallPage(context);
                    emptyTipsIsShow = false;
                }
            })

                    .setNegativeButton(R.string.cancel).setNegativeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emptyTipsIsShow = false;
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    emptyTipsIsShow = false;
                }
            }).create().show();
            emptyTipsIsShow = true;
        } catch (Exception e) {
            LogUtils.i("LackOfMemException", "异常信息：" + e.toString());
            e.printStackTrace();
            LTApplication.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    cn.lt.android.util.ToastUtils.showToast(LTApplication.shareApplication().getString(R.string.memory_install_error));
                }
            });

        }
    }


    /**
     * 单位是M
     *
     * @return
     */
    private static long getMinSpaceConfig() {
        String minSpace = "200";
        try {
            return Long.parseLong(minSpace);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 200;
    }



    /**
     * 计算剩余空间
     *
     * @return
     */
    public static long getAvailableSize() {
        try {
            return FileSizeUtil.getAvailableInternalMemorySize();
        } catch (Exception e) {
            e.printStackTrace();
            return GET_AVAILABLE_SIZE_ERROR;
        }
    }
}