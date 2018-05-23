package cn.lt.android.notification.sender;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.notification.PushNotification;
import cn.lt.android.notification.bean.PushAwakeBean;
import cn.lt.android.notification.bean.PushBaseBean;
import cn.lt.android.notification.bean.PushGameBean;
import cn.lt.android.notification.bean.PushH5Bean;
import cn.lt.android.notification.bean.PushPlatUpgradeBean;
import cn.lt.android.notification.bean.PushSoftwareBean;
import cn.lt.android.notification.bean.PushTopicBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.BitmapUtils;
import cn.lt.framework.util.PreferencesUtils;

/**
 * Created by LinJunSheng on 2016/3/7.
 */
public class PushNoticeSender {

    private final Context context;
    private PushNotification pushNotification;
    private ExecutorService mThreadPool;
    private Bitmap icon;// 推送图标

    public PushNoticeSender(Context context, ExecutorService mThreadPool) {
        this.mThreadPool = mThreadPool;
        this.context = context;
        pushNotification = new PushNotification(context);
    }

    /**
     * 发送推送通知
     */
    public void senPushNotice(final PushBaseBean bean) {
        // 加载推送的图标
        String iconUrl = bean.getIcon();
        final String noticeStyle = bean.getNotice_style();
        if (null != iconUrl && !"".equals(iconUrl)) {// 如果有传图标，网络加载图标
            LogUtils.i(LogTAG.PushTAG, "传了icon");
            ImageloaderUtil.loadImageCallBack(context, iconUrl, new SimpleTarget<GlideDrawable>() {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    icon = BitmapUtils.drawable2Bitmap(resource.getCurrent());
                    if (null == noticeStyle || "".equals(noticeStyle)) {// 版本升级是没有noticeStyle数据
                        pushNotice(bean, null);
                    } else {
                        getBigPhoto(bean);
                    }
                }
            });
        } else {// 没传图标，使用默认图标
            LogUtils.i(LogTAG.PushTAG, "没传icon");
            icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            if (null == noticeStyle || "".equals(noticeStyle)) {// 版本升级是没有noticeStyle数据
                pushNotice(bean, null);
            } else {
                getBigPhoto(bean);
            }
        }


    }

    // 加载大图（如果有的话）
    private void getBigPhoto(final PushBaseBean bean) {
        String noticeStyle = bean.getNotice_style();

        // 默认无图
        if (null != noticeStyle && noticeStyle.equals(PushNotification.defaule)) {
            LogUtils.i(LogTAG.PushTAG, "没传图片来哦~");
            pushNotice(bean, null);
        }

        // 小图或大图
        if (null != noticeStyle && noticeStyle.equals(PushNotification.smallPhoto) || noticeStyle.equals(PushNotification.bigPhoto)) {
            LogUtils.i(LogTAG.PushTAG, "有图片，，要加载了！");
            // 加载图片
            String imageUrl = GlobalConfig.combineImageUrl(bean.getImage());
            ImageloaderUtil.loadImageCallBack(context, imageUrl, new SimpleTarget<GlideDrawable>() {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    Bitmap pushImage = BitmapUtils.drawable2Bitmap(resource.getCurrent());
                    pushNotice(bean, pushImage);
                }
            });
        }

    }

    // 根据数据类型发送通知
    private void pushNotice(final PushBaseBean bean, final Bitmap pushImage) {

        LogUtils.i("zzz", "pushID--" + bean.pushId);
        if (bean instanceof PushSoftwareBean || bean instanceof PushGameBean) {// APP推送
            LogUtils.i(LogTAG.PushTAG, "是详情推送");
            final AppDetailBean appDetailBean = bean.getApp();

            // 推送过来的必曝光
//            String tempStatus = Constant.WK_SWITCH;
//            Constant.WK_SWITCH = Constant.STATUS_OPEN;
            boolean tempStatus = PreferencesUtils.getBoolean(LTApplication.instance,Constant.WK_SWITCH);
            PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,true);
            Set<String> exposureApps = WanKaManager.exposureSingleApp(appDetailBean, new SimpleResponseListener<JSONObject>() {
                @Override
                public void onSucceed(int what, Response<JSONObject> response) {
                    bean.setApp(appDetailBean);
                    LogUtils.i(LogTAG.PushTAG, "详情曝光成功");
                    pushNotification.sendPushAppNotification(bean, icon, pushImage);
                    DCStat.pushEvent(bean.getId(), "App", "received", "GETUI", "");//统计推送到达
                }

                @Override
                public void onFailed(int what, Response<JSONObject> response) {
                    pushNotification.sendPushAppNotification(bean, icon, pushImage);
                    LogUtils.i(LogTAG.PushTAG, "详情曝光失败");
                    DCStat.pushEvent(bean.getId(), "App", "received", "GETUI", "");//统计推送到达
                }

            }, "推送应用曝光：");
//            Constant.WK_SWITCH = tempStatus;
            PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,tempStatus);

            if (exposureApps.size() == 0) {
                pushNotification.sendPushAppNotification(bean, icon, pushImage);
                DCStat.pushEvent(bean.getId(), "App", "received", "GETUI", "");//统计推送到达
            }

        }

        if (bean instanceof PushTopicBean) {// 专题推送
            LogUtils.i(LogTAG.PushTAG, "是专题推送");
            PushTopicBean topic = (PushTopicBean) bean;
            pushNotification.sendPushTopicNotification(topic, icon, pushImage);
            DCStat.pushEvent(bean.pushId, "Topic", "received", "GETUI", "");//统计推送到达
        }

        if (bean instanceof PushPlatUpgradeBean) {// 平台升级推送
            LogUtils.i(LogTAG.PushTAG, "是平台升级推送");
            PushPlatUpgradeBean upgradeBean = (PushPlatUpgradeBean) bean;
            pushNotification.sendPlatformUpgradeNotification(upgradeBean, icon);
        }

        if (bean instanceof PushH5Bean) {// H5推送
            LogUtils.i(LogTAG.PushTAG, "是 H5 推送");
            PushH5Bean H5Bean = (PushH5Bean) bean;
            pushNotification.sendPushH5Notification(H5Bean, icon, pushImage);
            DCStat.pushEvent(bean.pushId, "H5", "received", "GETUI", "");//统计推送到达
        }
        if (bean instanceof PushAwakeBean) {
            LogUtils.i(LogTAG.PushTAG, "是 拉活 推送");
            if (null != bean) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(((PushAwakeBean) bean).getPulladdress()));
                if (AppUtils.checkUrlScheme(context, intent)) {
                    LogUtils.i(LogTAG.PushTAG, "拉活地址有效，发通知");
                    PushAwakeBean awakeBean = (PushAwakeBean) bean;
                    pushNotification.sendPushAwakeNotification(awakeBean, icon, pushImage);
                    DCStat.pushEvent(bean.pushId, "Awake", "received", "GETUI", "");//统计推送到达
                } else {
                    LogUtils.i(LogTAG.PushTAG, "拉活地址无效，不发通知");
                }
            }
        }
    }


}
