package cn.lt.android.main.appdetail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yolanda.nohttp.rest.Response;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.Photo;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.main.UIController;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ScreenSizeUtils;
import cn.lt.android.util.StringNumberUtil;
import cn.lt.android.util.TimeUtils;
import cn.lt.android.widget.AppRecommendView;
import cn.lt.appstore.R;

/**
 * Created by atain on 2016/2/22.
 *
 * @des 游戏详情Fragment
 */
public class AppDetailFragment extends BaseFragment implements View.OnClickListener {
    List<AppRecommendView> appRecommendViewList = new ArrayList<>();
    private View mRootView;
    private Context mContext;
    private LinearLayout mImageContainer;//应用截图容器
    private LinearLayout mRecommend_Stub;//游戏推荐
    private LinearLayout mNewActivity;
    private int[] screenSize;// 屏幕尺寸
    private TextView mVersionCode;//应用版本号
    private TextView mUpdateTime;//应用更新时间
    private TextView mAppDesc;//应用描述
    private TextView mAppAction;
    private TextView mArrowAction;
    private ImageView mArrowActionIV;
    private ImageView mArrowIV;
    private TextView mArrow;//展开
    private boolean unwind = false;
    private boolean unArrow = false;
    private int imageViewHigth, imageViewWidth;//实际展示的截图尺寸
    private AppDetailBean appDetailBean;
    private TextView mRecommentType;//本周推荐的应用类型

    LinearLayout mRecommendContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.app_detail_fragment, container, false);
            initView();
        }
        return mRootView;
    }

    private String pkgName = "";
    private String mId = "";

    @Override
    public void setPageAlias() {
        isAdv = getActivity().getIntent().getBooleanExtra(Constant.EXTRA_AD, false);
        if (isAdv) {
            pkgName = getActivity().getIntent().getStringExtra(Constant.EXTRA_PKGNAME);
            adMold = ((AppDetailActivity) getActivity()).adMold;
        } else {
            mId = getActivity().getIntent().getStringExtra(Constant.EXTRA_ID);
        }
        setmPageAlias(isAdv ? Constant.PAGE_AD_DETAIL : Constant.PAGE_DETAIL, isAdv ? pkgName : mId, isAdv);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        screenSize = ScreenSizeUtils.getScreenSize(mContext);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!((AppDetailActivity) getActivity()).isAd) {
            checkNetwork();
        }
    }

    public void checkNetwork() {
        try {
            requestData();
            LogUtils.i("appDetail", "checkNetwork调用了");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mImageContainer = (LinearLayout) mRootView.findViewById(R.id.ll_image_shot);
        mRecommend_Stub = (LinearLayout) mRootView.findViewById(R.id.recommend_stub);
        mRecommentType = (TextView) mRootView.findViewById(R.id.tv_recomment_type);
        mVersionCode = (TextView) mRootView.findViewById(R.id.tv_version_code);
        mUpdateTime = (TextView) mRootView.findViewById(R.id.tv_update_time);
        mAppDesc = (TextView) mRootView.findViewById(R.id.tv_app_desc);
        mArrowIV = (ImageView) mRootView.findViewById(R.id.iv_arrow);
        mArrow = (TextView) mRootView.findViewById(R.id.tv_arrow);

        mAppAction = (TextView) mRootView.findViewById(R.id.tv_app_action);
        mArrowAction = (TextView) mRootView.findViewById(R.id.tv_arrow_action);
        mArrowActionIV = (ImageView) mRootView.findViewById(R.id.iv_arrow_action);
        mNewActivity = (LinearLayout) mRootView.findViewById(R.id.new_activity);
        mRecommendContainer = (LinearLayout) mRootView.findViewById(R.id.detail_game_recommend);

        mArrowAction.setOnClickListener(this);
        mArrowActionIV.setOnClickListener(this);
        mAppAction.setOnClickListener(this);

        mArrow.setOnClickListener(this);
        mArrowIV.setOnClickListener(this);
        mAppDesc.setOnClickListener(this);
    }

    /***
     * 获取应用数据
     */
    public void requestData() {
        appDetailBean = ((AppDetailActivity) getActivity()).getAppDeailBean();
        initScreenShots(appDetailBean.getScreenshoot_urls());
        LogUtils.i("appDetail", "描述调用了");

        if (appDetailBean != null) {
//            LogUtils.i("appDetail", "getRecommend_apps().size()==" + appDetailBean.getRecommend_apps().size());
            initAppDescription(appDetailBean);
            initAppAction(appDetailBean);

            final List<RecommendBean> recommendApps = appDetailBean.getRecommend_apps();
            for (int i = 0; i < recommendApps.size(); i++) {
                try {
                    RecommendBean recommendApp = recommendApps.get(i);
                    recommendApp.setLtType("recommend");
                    DownloadTaskManager.getInstance().transfer(recommendApp);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            if (recommendApps.size() > 0) {
                Set<String> exposureApps = WanKaManager.exposureApps(recommendApps, new SimpleResponseListener<JSONObject>() {
                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        new DownloadAdAppReplacer().replaceByRecommendApps(recommendApps);
                        initRecommend(appDetailBean, recommendApps);
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                        new DownloadAdAppReplacer().replaceByRecommendApps(recommendApps);
                        initRecommend(appDetailBean, recommendApps);
                    }
                }, "游戏详情推荐应用曝光");

                if (exposureApps.size() == 0) {
                    new DownloadAdAppReplacer().replaceByRecommendApps(recommendApps);
                    initRecommend(appDetailBean, recommendApps);
                }
            } else {
                mRecommend_Stub.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 加载游戏截图
     */
    private void initScreenShots(final List<String> mScreenshowList) {
        int size = mScreenshowList.size();
        final List<Photo> pList = new ArrayList<>();
        int paddingLeft = (int) mContext.getResources().getDimension(R.dimen.appbar_padding_top);
        mImageContainer.removeAllViews();
        for (int i = 0; i < size; i++) {
            final ImageView imageView = new ImageView(mContext);
            final int position = i;
            if (i != (size - 1)) {
                imageView.setPadding(0, 0, paddingLeft, 0);
            }
            String imgUrl = GlobalConfig.combineImageUrl(mScreenshowList.get(i));
            ImageloaderUtil.loadImageCallBack(mContext, imgUrl, new SimpleTarget<GlideDrawable>() {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    if (resource.getIntrinsicWidth() > resource.getIntrinsicHeight()) {
                        /*横图*/
                        imageViewHigth = (int) (screenSize[0] * 0.5);
                        imageViewWidth = (int) ((((double) imageViewHigth) / resource.getIntrinsicHeight()) * resource.getIntrinsicWidth());
                    } else {
                        /*竖图*/
                        imageViewHigth = (int) (screenSize[0] * 0.75);
                        imageViewWidth = (int) ((((double) imageViewHigth) / resource.getIntrinsicHeight()) * resource.getIntrinsicWidth());
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(imageViewWidth, imageViewHigth);
                    imageView.setLayoutParams(lp);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageView.setImageDrawable(resource);
                }
            });
            mImageContainer.addView(imageView);

            Photo phone = new Photo();
            phone.original = mScreenshowList.get(i);
            pList.add(phone);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIController.jumpToImageBrowster((Activity) mContext, new ImageViewPagerActivity.ImageUrl(pList), position);
                }
            });
        }
    }

    /***
     * 应用描述信息
     */
    private String des;
    private String desc;
    private String desWANKA;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initAppDescription(AppDetailBean bean) {
        mVersionCode.setText(bean.getVersion_name());
        String upDataTime = TimeUtils.getDateToString(bean.getCreated_at());
        String str = upDataTime.substring(0, 11);
        mUpdateTime.setText(str);

        desc = bean.getDescription().trim();//这个值是用来展示原文的，不能直接在描述文本内设置
        /*如果是玩咖的应用则不需要过滤文本内容*/
        if (!bean.getAdMold().equals(AdMold.WanKa)) {
            try {
                /*这个值是用来展示缩写描述的，不能直接在描述文本内设置*/
                des = StringNumberUtil.replaceBlank(desc).replace("<br/>", "").replace("<p>&nbsp;</p>", "").replace("<p>", "").replace("</p>", "<br>").replace("<br>", "").replace("&nbsp;", "").replace("<div>", "").replace("</div><div>", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            desWANKA = StringNumberUtil.replaceBlank(desc);
        }

        setDesc();
//        mAppDesc.setText(StringNumberUtil.replaceBlank(des));
        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int lineCount = mAppDesc.getLineCount();
                if (lineCount < 3) {//描述信息两行以上显示
                    mArrow.setVisibility(View.GONE);
                    mArrowIV.setVisibility(View.GONE);
                } else {
                    mArrow.setVisibility(View.VISIBLE);
                    mArrowIV.setVisibility(View.VISIBLE);
                    mAppDesc.setMaxLines(2);
                }
                mAppDesc.setVisibility(View.VISIBLE);//加载完成后再显示应用描述，防止出现展开再收缩的现象
            }
        }, 500);

        mArrow.setOnClickListener(this);
    }

    private void setDesc() {
        if (appDetailBean.getAdMold().equals(AdMold.WanKa)) {
            mAppDesc.setText(desWANKA);
        } else {
            mAppDesc.setText(Html.fromHtml(des));
        }
    }

    /**
     * 最新活动
     */
    String actionDesc, actionDes;

    private void initAppAction(AppDetailBean bean) {
        if (TextUtils.isEmpty(bean.getActivity())) {
            mNewActivity.setVisibility(View.GONE);
        } else {
            mNewActivity.setVisibility(View.VISIBLE);
            actionDesc = bean.getActivity().trim();
            try {
                actionDes = StringNumberUtil.replaceBlank(actionDesc).replace("<br/>", "").replace("<p>&nbsp;</p>", "").replace("<p>", "").replace("</p>", "<br>").replace("<br>", "").replace("&nbsp;", "").replace("<div>", "").replace("</div><div>", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mAppAction.setText(Html.fromHtml(actionDes));

            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int lineCount = mAppAction.getLineCount();
                    if (lineCount < 3) {
                        mArrowAction.setVisibility(View.GONE);
                        mArrowActionIV.setVisibility(View.GONE);
                        mAppAction.setClickable(false);
                    } else {
                        mArrowAction.setVisibility(View.VISIBLE);
                        mArrowActionIV.setVisibility(View.VISIBLE);
                        mAppAction.setMaxLines(2);
                        mAppAction.setClickable(true);
                    }
                    mAppAction.setVisibility(View.VISIBLE);
                }
            }, 500);
        }
    }

    /**
     * 本周推荐游戏
     */
    private void initRecommend(AppDetailBean bean, List<RecommendBean> recommendGameList) {
        appRecommendViewList.clear();
        if (recommendGameList.size() > 0) {
            mRecommend_Stub.setVisibility(View.VISIBLE);
            String appType = bean.getApps_type();
            mRecommentType.setText("game".equals(appType) || "GAME".equals(appType) ? "大家都在玩" : "大家都在用");
            boolean isApp = !("game".equals(appType) || "GAME".equals(appType));

            // 过滤已安装，不能在下边的for循环中删除(会导致并发修改异常)
            Iterator<RecommendBean> iterator = recommendGameList.iterator();
            while (iterator.hasNext()) {
                RecommendBean recommendBean = iterator.next();

                if (AppUtils.isInstalled(recommendBean.getPackage_name())) {
                    iterator.remove();
                }
            }

            if (recommendGameList.size() > 0) {
                for (int i = 0; i < recommendGameList.size(); i++) {
                    RecommendBean recommendBean = recommendGameList.get(i);

                    AppRecommendView recommendView = new AppRecommendView(mContext, getPageAlias(), i, isApp);
                    recommendView.setData(recommendBean);
                    mRecommendContainer.addView(recommendView);
                    appRecommendViewList.add(recommendView);
                    if (i == recommendGameList.size() - 1) {
                        recommendView.setPadding(0, 0, 40, 0);
                    }
                }
                mRecommendContainer.requestLayout();
            } else {
                // 隐藏模块
                mRecommend_Stub.setVisibility(View.GONE);
            }
        } else {
            mRecommend_Stub.setVisibility(View.GONE);
        }
    }

    /***
     * 动态调整应用描述展开收起
     */
    private void fixArrow() {
        if (!unwind) {
            mArrow.setText("收起");
            mAppDesc.setMaxLines(200);
            mArrowIV.setImageResource(R.drawable.ic_arrow_up);
//            setDesc();
            mAppDesc.setText(Html.fromHtml(desc));
            unwind = true;
        } else {
            mArrow.setText("展开");
            mAppDesc.setMaxLines(2);
            mArrowIV.setImageResource(R.drawable.ic_arrow_down);
            setDesc();
            unwind = false;
        }
    }

    /***
     * 动态调整最新活动展开收起
     */
    private void fixActivityArrow2() {
        if (!unArrow) {
            mArrowAction.setText("收起");
            mAppAction.setMaxLines(200);
            mArrowActionIV.setImageResource(R.drawable.ic_arrow_up);
            mAppAction.setText(Html.fromHtml(actionDesc));
            unArrow = true;
        } else if (unArrow) {
            mArrowAction.setText("展开");
            mAppAction.setMaxLines(2);
            mArrowActionIV.setImageResource(R.drawable.ic_arrow_down);
            mAppAction.setText(Html.fromHtml(StringNumberUtil.replaceBlank(actionDes)));
            unArrow = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_arrow:
            case R.id.iv_arrow:
            case R.id.tv_app_desc:
                if (TextUtils.isEmpty(desc)) return;
                fixArrow();
                break;
            case R.id.tv_arrow_action:
            case R.id.iv_arrow_action:
            case R.id.tv_app_action:
                if (TextUtils.isEmpty(actionDesc)) return;
                fixActivityArrow2();
                break;
        }
    }

    /***
     * 更新本周推荐游戏
     *
     * @param position
     */
    public void updateRecommendView(int position) {
        if (appRecommendViewList.size() > position) {
            appRecommendViewList.get(position).setData(appDetailBean.getRecommend_apps().get(position));
        }
    }
}

