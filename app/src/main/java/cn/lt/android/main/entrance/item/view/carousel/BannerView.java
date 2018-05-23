package cn.lt.android.main.entrance.item.view.carousel;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.GlobalConfig;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.item.view.ItemView;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.ScreenUtils;


@SuppressWarnings("ALL")
public class BannerView extends ItemView implements ViewPager.OnPageChangeListener {

    /**
     * 多久切换轮播图
     */
    public static final int sleepTime = 4000;
    public static final int DEFAULT_HEIGHT = -1;
    /**
     * 页签
     */
    private ArrayList<View> pointList;
    private List<ItemData> mBannerItemDatas;
    private MyGallery bannerViewPager;
    private LinearLayout pointLayout;
    private ImgAdapter mBannerAdapter;
    private int preSelImgIndex = 0;
    private float mBannerHeight;
    private boolean showPoint = true;
    private boolean needShowWithoutData;
    private int mCurrentIndex = -1;
    private boolean isAdd;

    /**
     * @param context 上下文
     * @param height  指定轮播图的高度，使用默认值{@link #DEFAULT_HEIGHT}
     */
    public BannerView(Context context, int height, boolean showWithoutData, String pageName) {
        super(context, pageName);
        this.needShowWithoutData = showWithoutData;

        int scrennwidth = ScreenUtils.getScreenWidth(getContext());
        int imageWidth = context.getResources().getDimensionPixelOffset(R.dimen.bannerView_image_width);
        int imageHeight = context.getResources().getDimensionPixelOffset(R.dimen.bannerView_image_height);

        //轮播图默认高度；
        if (height < 0) {
//            mBannerHeight = context.getResources().getDimensionPixelOffset(R.dimen.banner_height);
            mBannerHeight = GlobalConfig.getImageViewHeight(scrennwidth, imageWidth, imageHeight);
            Logger.i("mBannerHeight = " + mBannerHeight + "scrennwidth = " + scrennwidth);
        } else {
            mBannerHeight = height;
        }
        LayoutInflater.from(context).inflate(R.layout.layout_carousel, this);
        initInfiniteLoopView();
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        mBannerHeight = mTypedArray.getDimension(R.styleable.BannerView_banner_height, context.getResources().getDimensionPixelOffset(R.dimen.banner_height));
        showPoint = mTypedArray.getBoolean(R.styleable.BannerView_show_point, true);
        needShowWithoutData = mTypedArray.getBoolean(R.styleable.BannerView_show_without_data, false);
        mTypedArray.recycle();
        LayoutInflater.from(context).inflate(R.layout.layout_carousel, this);
        initInfiniteLoopView();
    }

    public boolean isNeedShowWithoutData() {
        return needShowWithoutData;
    }

    public void setNeedShowWithoutData(boolean needShowWithoutData) {
        this.needShowWithoutData = needShowWithoutData;
    }


    public void initInfiniteLoopView() {
        bannerViewPager = (MyGallery) findViewById(R.id.vp_banner_view);
        pointLayout = (LinearLayout) findViewById(R.id.llt_point_root_banner_view);
        if (showPoint) {
            pointLayout.setVisibility(View.VISIBLE);
        } else {
            pointLayout.setVisibility(View.GONE);
        }
        pointLayout.removeAllViews();
        bannerViewPager.getLayoutParams().height = (int) mBannerHeight;
        mBannerAdapter = new ImgAdapter(getContext(), null, mPageName, 1);
        bannerViewPager.setAdapter(mBannerAdapter);
        bannerViewPager.setFocusable(true);
        bannerViewPager.setCurrentItem(Integer.MAX_VALUE >> 2);
        bannerViewPager.addOnPageChangeListener(this);
    }

    public void stopBannerTimer() {
        if (bannerViewPager != null) {
            bannerViewPager.pasue();
        }
    }

    public void startBannerTimer() {
        if (bannerViewPager != null) {
            bannerViewPager.start();
        }
    }

    private void setViewPagePoint(LinearLayout pointLayout, int imgCnt, int curr) {
        pointLayout.removeAllViews();
        pointList = new ArrayList<>();
        if (pointList.size() != 0) {
            pointList.clear();
        }
        int pointHeight = (int) getContext().getResources().getDimension(R.dimen.carousel_pointView_height);
        int pointWidth = (int) getContext().getResources().getDimension(R.dimen.carousel_pointView_height);
        int marginRight = (int) getContext().getResources().getDimension(R.dimen.carousel_pointView_margin);
        LayoutParams params = new LayoutParams(pointWidth, pointHeight);
        params.setMargins(0, 0, marginRight, 0);
        for (int i = 0; i < imgCnt; i++) {
            pointLayout.addView(producePointView(params, i, imgCnt, curr));
        }
    }

    private TextView producePointView(LayoutParams params, int i, int imgCnt, int curr) {
        TextView pointView = new TextView(getContext());
        if (i == curr) {
            pointView.setBackgroundResource(R.drawable.banner_point_slected);
        } else {
            pointView.setBackgroundResource(R.drawable.banner_point_no_slected);
        }
        pointView.setLayoutParams(params);
        pointList.add(pointView);
        return pointView;
    }

    private void fillView() {
        try {
            if (mBannerItemDatas == null || mBannerItemDatas.size() == 0) {
                if (!needShowWithoutData) {
                    bannerViewPager.setVisibility(View.GONE);
                }
                return;
            }
//            stopBannerTimer();
            bannerViewPager.setVisibility(View.VISIBLE);
            int size = mBannerItemDatas.size();
            List<ClickTypeBean> bannerIvs = new ArrayList<>();
            bannerIvs.clear();
            for (int i = 0; i < size; i++) {
                ClickTypeBean banner = (ClickTypeBean) mBannerItemDatas.get(i).getmData();
                bannerIvs.add(banner);
            }
            //为了解决轮播图少于3张时，向左滑动出现空白页的bug
            while (bannerIvs.size() < 4 && bannerIvs.size() != 1) {
                bannerIvs.addAll(bannerIvs);
                isAdd = true;
            }
            // 确保每次刷新和第一次初始化时banner图的指示器在第一个位置；
            setViewPagePoint(pointLayout, size, 0);// 设置轮播图页签
            mBannerAdapter.setList(bannerIvs,isAdd);
            mBannerAdapter.setItemDataList(mBannerItemDatas);
            //左右无限轮播
            //初始化viewPager首次选中
            int diff = Integer.MAX_VALUE / 2 % bannerIvs.size();
            int index = Integer.MAX_VALUE / 2 - diff;
            bannerViewPager.setCurrentItem(index);
            startBannerTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        try {
            if (bean != null) {
                BaseBeanList mg = (BaseBeanList) bean.getmData();
                //noinspection unchecked
                mBannerItemDatas = mg;
                mBannerAdapter.setmPosition(position);
                fillView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        try {
            mCurrentIndex = position;
            position = position % mBannerItemDatas.size();
            pointLayout.getChildAt(preSelImgIndex).setBackgroundResource(R.drawable.banner_point_no_slected);
            pointLayout.getChildAt(position).setBackgroundResource(R.drawable.banner_point_slected);
            preSelImgIndex = position;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("Banner", "轮播图设置错误");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
