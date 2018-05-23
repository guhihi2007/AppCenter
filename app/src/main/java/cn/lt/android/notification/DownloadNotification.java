package cn.lt.android.notification;

import android.content.Context;
import android.text.TextUtils;

import cn.lt.android.db.AppEntity;
import cn.lt.android.service.NoticeIntentService;

/**
 * Created by 林俊生 on 2016/1/19.
 * <p>
 * 用于发送应用下载通知
 */
public class DownloadNotification extends BaseNotification {

    private static final String SINGLE_DOWNLOADING = "正在下载";
    private static final String DOWNLOAD_FAULT = "下载失败";
    private static final String DOWNLOAD_COMPLETE = "下载完成";
    private static final String CHECK_DOWNLOAD = "查看下载";
    private static final String GO_INSTALL = "马上安装";
    private static final String CLICK_TO_VIEW = "点击查看";

    public DownloadNotification(Context context) {
        super(context);
    }


    /**
     * 单个应用下载 通知
     */
    public void singleDownload(AppEntity app) {
        setGoDownloadPagePendingIntent(NoticeConsts.goDownloadTab, app.getAppClientId());
        notify(TextUtils.isEmpty(app.getAlias()) ? app.getName() : app.getAlias(), SINGLE_DOWNLOADING, CHECK_DOWNLOAD, parseId(app), NoticBtnType.GONE);
    }

    /**
     * 多个应用下载 通知
     */
    public void multiDownload(int downloadCount, String statId) {
        setGoDownloadPagePendingIntent(NoticeConsts.goDownloadTab, statId);
        notify(null, budileMultiString(downloadCount) + SINGLE_DOWNLOADING, CHECK_DOWNLOAD, NoticeConsts.MULTI_DOWNLOADING_NOTIFY_ID, NoticBtnType.GONE);
    }

    /**
     * 应用下载完成 通知
     */
    public void downloadComplete(AppEntity app) {
        setInstallPendingIntent(app);
        notify(TextUtils.isEmpty(app.getAlias()) ? app.getName() : app.getAlias(), DOWNLOAD_COMPLETE, GO_INSTALL, parseId(app), NoticBtnType.INSTALL);
    }

    /**
     * 单个应用下载失败 通知
     */
    public void singleDownloadFault(AppEntity app) {
//        setReDownloadPendingIntent(app);
        setGoDownloadPageAndRedownPendingIntent(app);
        notify(TextUtils.isEmpty(app.getAlias()) ? app.getName() : app.getAlias(), DOWNLOAD_FAULT, AT_RETRY, parseId(app), NoticBtnType.RETRY);
    }

    /**
     * 多个 应用下载失败 通知
     */
    public void multiDownloadFault(int downloadCount, String statId) {
//        setReMultiDownloadPendingIntent();
        setGoDownloadPagePendingIntent(NoticeConsts.goDownloadTabByMultiDownloadFault, statId);
        notify(null, budileMultiString(downloadCount) + DOWNLOAD_FAULT, AT_RETRY, NoticeConsts.MUTLI_DOWLOAD_FAIL_NOTIFY_ID, NoticBtnType.RETRY);
    }

    /**
     * 应用下载暂停 通知
     */
    public void appDownloadPause(int pauseCount, String statId) {
        setGoDownloadPagePendingIntent(NoticeConsts.goDownloadTab, statId);
        notify(null, pauseCount + "个下载已暂停", CLICK_TO_VIEW, NoticeConsts.APP_DOWNLOAD_PAUSE_NOTIFY_ID, NoticBtnType.GONE);
    }

    /**
     * 撤销 多个应用正在下载 通知
     */
    public void cancelMultiDownloadingNotice() {
        cancelNotice(NoticeConsts.MULTI_DOWNLOADING_NOTIFY_ID);
    }

    /**
     * 撤销 多个应用下载失败 通知
     */
    public void cancelMultiDownloadFailNotice() {
        cancelNotice(NoticeConsts.MUTLI_DOWLOAD_FAIL_NOTIFY_ID);
    }

    /**
     * 撤销应用下载暂停 通知
     */
    public void cancelDownloadPauseNotice() {
        cancelNotice(NoticeConsts.APP_DOWNLOAD_PAUSE_NOTIFY_ID);
    }

    /**
     * 设置 执行安装 意图
     */
    private void setInstallPendingIntent(AppEntity app) {
        setPendingIntent(
                getPendingIntentForService(
                        createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                                putExtra("appEntity", app).
                                putExtra("appPkg", app.getPackageName()).
                                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByDownloadComplete)));
    }

    /**
     * 设置 重新下载 意图
     */
    private void setReDownloadPendingIntent(AppEntity app) {
        setPendingIntent(
                getPendingIntentForService(
                        createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                                putExtra("appEntity", app).
                                putExtra("appPkg", app.getPackageName()).
                                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByDownloadFault)));
    }

    /**
     * 设置 多个重新下载 意图
     */
    private void setReMultiDownloadPendingIntent() {
        setPendingIntent(
                getPendingIntentForService(
                        createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByMultiDownloadFault)));
    }


}
