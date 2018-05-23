package cn.lt.android.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.SimpleTarget;

import cn.lt.android.GlobalConfig;
import cn.lt.android.widget.GlideCircleTransform;
import cn.lt.android.widget.GlideRoundTransform;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/10/17.
 */

public class ImageloaderUtil {
    private static RequestManager reqManager;

    public void init(Context context) {
        reqManager = Glide.with(context);
    }

    public static class ImageLoaderHolder {
        private final static ImageloaderUtil instance = new ImageloaderUtil();
    }

    public static ImageloaderUtil getInstance() {
        return ImageLoaderHolder.instance;
    }

    /***
     * 暂停加载图片
     *
     * @param context
     */
    public static void pauseImageLoader(Context context) {
        reqManager.pauseRequests();
    }

    /***
     * 回复图片加载
     *
     * @param context
     */
    public static void resumeImageLoader(Context context) {
        reqManager.resumeRequests();
    }

    /***
     * 加载普通图片
     *
     * @param context
     * @param url
     * @param view
     */
    public static void loadImage(Context context, String url, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).animate(R.anim.item_alpha_in).placeholder(R.mipmap.app_default).into(view);
    }

    /***
     * 需要回调的加载图片
     *
     * @param context
     * @param url
     * @param mTarget
     */
    public static void loadImageCallBack(Context context, String url, SimpleTarget<GlideDrawable> mTarget) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).placeholder(R.mipmap.special_topic_default).fitCenter().into(mTarget);
    }

    /***
     * 加载圆形图片
     *
     * @param context
     * @param url
     * @param view
     */
    public static void loadRoundImage(Context context, String url, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).placeholder(R.mipmap.app_default).transform(new GlideRoundTransform(context)).crossFade().into(view);
    }

    /***
     * @param context
     * @param url
     * @param view
     */
    public static void loadBigImage(Context context, String url, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).thumbnail(0.1f).placeholder(R.mipmap.special_topic_default).into(view);
    }

    /***
     * 加载长图
     * @param context
     * @param url
     * @param view
     */
    public static void loadRectImage(Context context, String url, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).thumbnail(0.1f).placeholder(R.mipmap.bannerview_defult_imageview).into(view);
    }
    /***
     * 加载用户头像
     *
     * @param context
     * @param url
     * @param view
     */
    public static void loadUserHead(Context context, String url, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).placeholder(R.mipmap.def_user_head).transform(new GlideCircleTransform(context)).into(view);
    }

    /***
     * 加载应用市场
     *
     * @param context
     * @param url
     * @param view
     */
    public static void loadLTLogo(Context context, String url, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).crossFade().placeholder(R.mipmap.ic_launcher).into(view);
    }

    /***
     * 加载普通图片
     *
     * @param context
     * @param url
     * @param view
     */
    public static void loadImage(Context context, String url, int defaultImgId, ImageView view) {
        Glide.with(context).load(GlobalConfig.combineImageUrl(url)).animate(R.anim.item_alpha_in).placeholder(defaultImgId).into(view);
    }
}
