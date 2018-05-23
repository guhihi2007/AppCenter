package cn.lt.android.umsharesdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.main.UIController;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng on 2016/3/3.
 */
public class OneKeyShareUtil {
    private static OneKeyShareUtil instance;
    private static ShareType shareType;
    private static Activity mActivity;
    private final Bitmap defaultBitmap;
    private UMImage image;
    private String title;
    private String content;
    private String targetUrl;

    public static final String TAG = "UMshareLog";

    public static final String APK_DownloadUrl = "http://appcenter.ttigame.com/api/weixin ";// 末尾加空格是为了防止新浪微博@人时，链接会失效

    /**
     * 应用市场下载页面
     */
    public static final String AppCenterPageUrl = "http://appcenter.ttigame.com/api/weixin";

    public enum ShareType {
        software, game, appCenter, activities
    }

    private OneKeyShareUtil() {
        defaultBitmap = BitmapFactory.decodeResource(mActivity.getResources(), R.mipmap.ic_launcher_jpg);

        // 自定义一个透明弹出框（用于覆盖友盟的进度弹框，因为友盟分享弹框无法取消）
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.CustomDialog);
//        image = new UMImage(activity, "http://www.umeng.com/images/pic/social/integrated_3.png");
    }

    public static OneKeyShareUtil getInstance(Activity activity, ShareType type) {
        shareType = type;
        mActivity = activity;
        if (instance == null) {
            synchronized (OneKeyShareUtil.class) {
                if (instance == null) {
                    instance = new OneKeyShareUtil();
                }
            }
        }

        return instance;
    }

    public void shareWeiXin(ShareBean shareBean) {

        if (UMShareAPI.get(mActivity).isInstall(mActivity, SHARE_MEDIA.WEIXIN)) {

            createShareStrings(shareBean, ShareStrings.WeiXin);
            targetUrl = createWeiXinDownloadUrl(shareBean);
            LogUtils.i(TAG, "activityName = " + mActivity.getLocalClassName());

            /*标题：支持
            * 支持超长字符和特殊字符*/
            UMWeb web = new UMWeb(targetUrl);// 链接
            web.setTitle(title);//标题
            web.setThumb(image);  //缩略图
            web.setDescription(content);//描述

            new ShareAction(mActivity)
                    .setPlatform(SHARE_MEDIA.WEIXIN)
                    .setCallback(umShareListener)
                    .withText(content)
                    .withMedia(web)
                    .share();

        } else {

            // 如果没有安装对应的分享平台，启动我们自己的下载页面
            UIController.goNoInstallSharePlat(mActivity, NoPlatDownActivity.WECHAT, null == shareBean ? "" : shareBean.getResource_type());
        }

    }

    public void shareWeiXin_Circle(ShareBean shareBean) {

        if (UMShareAPI.get(mActivity).isInstall(mActivity, SHARE_MEDIA.WEIXIN_CIRCLE)) {

            createShareStrings(shareBean, ShareStrings.WeiXin_Circle);
            targetUrl = createWeiXinDownloadUrl(shareBean);
            LogUtils.i(TAG, "activityName = " + mActivity.getLocalClassName());

            /*标题：支持
            * 支持超长字符和特殊字符*/
            UMWeb web = new UMWeb(targetUrl);// 链接
            web.setTitle(title);//标题
            web.setThumb(image);  //缩略图
            web.setDescription(content);//描述

            new ShareAction(mActivity)
                    .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                    .setCallback(umShareListener)
                    .withText(content)
                    .withMedia(web)
                    .share();

        } else {

            // 如果没有安装对应的分享平台，启动我们自己的下载页面
            UIController.goNoInstallSharePlat(mActivity, NoPlatDownActivity.WECHAT, null == shareBean ? "" : shareBean.getResource_type());
        }

    }

    public void shareSinaWeiBo(ShareBean shareBean) {

        if (UMShareAPI.get(mActivity).isInstall(mActivity, SHARE_MEDIA.SINA)) {

            createShareStrings(shareBean, ShareStrings.sinaWeiBo);
            LogUtils.i(TAG, "activityName = " + mActivity.getLocalClassName());

                /*标题：不支持
            * 新浪编辑页面，限制最多140个字符（1个数字或者英文算半个字符，中文算一个字符）*/
            UMWeb web = new UMWeb(targetUrl);// 链接
            web.setTitle(title);//标题
            web.setThumb(image);  //缩略图

            new ShareAction(mActivity)
                    .setPlatform(SHARE_MEDIA.SINA)
                    .setCallback(umShareListener)
                    .withText(content)// 微博不支持设置targetUrl，只能加在内容后面,末尾加空格是为了防止新浪微博@人时，链接会失效
                    .withMedia(web)
                    .share();

        } else {

            // 如果没有安装对应的分享平台，启动我们自己的下载页面
            UIController.goNoInstallSharePlat(mActivity, NoPlatDownActivity.WEIBO, null == shareBean ? "" : shareBean.getResource_type());
        }

    }

    public void shareQQ(ShareBean shareBean) {

        if (UMShareAPI.get(mActivity).isInstall(mActivity, SHARE_MEDIA.QQ)) {

            createShareStrings(shareBean, ShareStrings.QQ);
            LogUtils.i(TAG, "activityName = " + mActivity.getLocalClassName());
            UMWeb web = new UMWeb(targetUrl);// 链接
            web.setTitle(title);//标题
            web.setThumb(image);  //缩略图
            web.setDescription(content);//描述

            new ShareAction(mActivity)
                    .setPlatform(SHARE_MEDIA.QQ)
                    .setCallback(umShareListener)
                    .withText(content)
                    .withMedia(web)
                    .share();

        } else {

            // 如果没有安装对应的分享平台，启动我们自己的下载页面
            UIController.goNoInstallSharePlat(mActivity, NoPlatDownActivity.QQ, null == shareBean ? "" : shareBean.getResource_type());
        }

    }

    /**
     * 生成分享所需要的文案
     *
     * @param shareBean  分享数据
     * @param plat 所分享的目标平台
     */
    private void createShareStrings(ShareBean shareBean, int plat) {
        if (shareType == ShareType.software) {
            title = ShareStrings.getSoftwareTitle(shareBean.getApp().getName(), plat);
            content = ShareStrings.getSoftwareContent(shareBean.getApp().getName(), plat);
            targetUrl = GlobalConfig.combineDownloadUrl(shareBean.getApp().getDownload_url());
            image = new UMImage(mActivity, GlobalConfig.combineImageUrl(shareBean.getApp().getIcon_url()));

        }

        if (shareType == ShareType.game) {
            title = ShareStrings.getGameTitle(shareBean.getApp().getName(), plat);
            content = ShareStrings.getGameContent(shareBean.getApp().getName(), plat);
            targetUrl = GlobalConfig.combineDownloadUrl(shareBean.getApp().getDownload_url());
            image = new UMImage(mActivity, GlobalConfig.combineImageUrl(shareBean.getApp().getIcon_url()));
        }

        if (shareType == ShareType.appCenter) {
            title = ShareStrings.geTitle_AppCenter();
            content = ShareStrings.getContent_AppCenter();
            targetUrl = APK_DownloadUrl;
            image = new UMImage(mActivity, defaultBitmap);
        }
        if (shareType == ShareType.activities) {
            title = shareBean.getTitle();
            content = shareBean.getShareContent();
            targetUrl = shareBean.getShareLink();
            image = new UMImage(mActivity, shareBean.getShareIcon());
        }
    }

    /**
     * 生成微信分享用的链接
     */
    private String createWeiXinDownloadUrl(ShareBean shareBean) {
        // 关于我们
        if(shareBean == null || shareBean.getApp() == null) {
            return AppCenterPageUrl;
        }

        // 活动
        if (shareBean.getShareType() == ShareType.activities) {
            return shareBean.getShareLink();
        }

        AppDetailBean app = shareBean.getApp();

        String type = "";
        if (app.getApps_type().equals("game")) {
            type = "game";
        }
        if (app.getApps_type().equals("software")) {
            type = "software";
        }
        LogUtils.i(TAG, "type = " + type);
        LogUtils.i(TAG, "微信分享的链接 = " + GlobalParams.getHostBean().getWeixin_host() + "/api/weixin?id=" + app.getAppClientId() + "&type=" + type);
        return GlobalParams.getHostBean().getWeixin_host() + "/api/weixin?id=" + app.getAppClientId() + "&type=" + type;
    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA platform) {
            LogUtils.i(TAG, platform + " 开始分享....");
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            LogUtils.i(TAG, platform + " 分享成功啦");

            // QQ平台分享不提示
            if (platform == SHARE_MEDIA.QQ) {
                return;
            }
            ToastUtils.showToast("分享成功");
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            LogUtils.i(TAG, platform + " 分享失败啦");

            // QQ平台分享不提示
            if (platform == SHARE_MEDIA.QQ) {
                return;
            }
            ToastUtils.showToast(platform + " 分享失败");
            try {
                LogUtils.i(TAG, t.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            LogUtils.i(TAG, platform + " 分享取消了");

            // QQ平台分享不提示
            if (platform == SHARE_MEDIA.QQ) {
                return;
            }
            ToastUtils.showToast("分享取消了");
        }
    };
}
