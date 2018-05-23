package cn.lt.android.notification;

import android.content.Context;

import cn.lt.android.db.AppEntity;
import cn.lt.android.service.NoticeIntentService;

/**
 * Created by LinJunSheng on 2016/1/21.
 *
 * 用于发送应用安装通知
 */
public class InstallNotification extends BaseNotification{

    private static final String INSTALL_COMPLETE = "安装完成";
    private static final String INSTALL_FAULT = "安装失败";
    private static final String ENTER = "点击进入";

    public InstallNotification(Context context) {
        super(context);
    }

    /**
     * 安装完成 通知
     */
    public void installComplete(AppEntity app) {
        setInstallCompleteIntent(app);
        notify(app.getName(), INSTALL_COMPLETE, ENTER, parseId(app), NoticBtnType.OPEN);
    }

    /** 设置 执行安装 意图*/
    private void setInstallCompleteIntent(AppEntity app) {
        setPendingIntent(
                getPendingIntentForService(
                        createIntent(NoticeIntentService.class).setAction(NoticeIntentService.ACTION).
                                putExtra("appEntity", app).
                                putExtra("appPkg", app.getPackageName()).
                                putExtra("appId", app.getAppClientId()).
                                putExtra(NoticeConsts.jumpBy, NoticeConsts.jumpByInstallComplete)));
    }

}
