package cn.lt.android.main.entrance.item.view.carousel;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.GlobalConfig;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.main.entrance.Jumper;
import cn.lt.android.main.entrance.data.ClickType;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentData;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatisticsDataProductorImpl;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;

@SuppressWarnings("deprecation")
public class ImgAdapter extends PagerAdapter implements View.OnClickListener {

    private Context mContext;
    private List<ClickTypeBean> mImgList;
    private List<ImageView> mViews = new ArrayList<>();
    private String mPageName;
    private boolean isAdd;

    public int getmPosition() {
        return mPosition;
    }

    public void setmPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    private int mPosition;
    private List<ItemData> mItemData;


    public ImgAdapter(Context context, List<ClickTypeBean> imgList, String pageName, int position) {
        mContext = context;
        this.mPageName = pageName;
        this.mPosition = position;
        setList(imgList, isAdd);
//       EventBus.getDefault().register(this);  // 注意：没有接收，就一定不要注册，
/****否则会报java.lang.NullPointerException: Attempt to write to field 'int android.support.v7.widget.RecyclerView$ViewHolder.mItemViewType' on a null object reference*/
    }

    /***
     * 用于上报当前资源位的位置 by atian
     * @param mItemData
     */
    public void setItemDataList(List<ItemData> mItemData) {
        this.mItemData = mItemData;
    }

    public void setList(List<ClickTypeBean> imgList, boolean isAdd) {
        this.isAdd=isAdd;
        if (imgList == null) {
            this.mImgList = new ArrayList<>();
        } else {
            if (this.mImgList.size() != 0) {
                mImgList.removeAll(mImgList);
                this.notifyDataSetChanged();
            }
            this.mImgList = imgList;
        }
        setImagVeiw(isAdd);
        this.notifyDataSetChanged();
    }


    private void setImagVeiw(boolean isAdd) {
        if (mImgList != null) {
            mViews.removeAll(mViews);
            for (int i = 0; i < mImgList.size(); i++) {
                int temp=i;
                ImageView imageView = new ImageView(mContext);
                imageView.setScaleType(ScaleType.FIT_XY);
                imageView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                if(isAdd && mImgList.size()==4){   //解决由于图片集翻倍，导致的position错误的bug。
                    temp=i%2;
                }else if(isAdd && mImgList.size()==6){
                    temp=i%3;
                }
                LogUtils.d(ImgAdapter.class.getSimpleName(),"i的位置："+i);
                imageView.setTag(R.id.data_tag, temp);
                imageView.setOnClickListener(this);
                String url = GlobalConfig.combineImageUrl(mImgList.get(i).getImage());
                Logger.i(url);
                ImageloaderUtil.loadBigImage(mContext,url,imageView);
                mViews.add(imageView);
            }
        }
    }

    public int getCount() {
        if (mImgList == null || mImgList.size() <= 0) {
            return 0;
        } else if (mImgList == null || mImgList.size() == 1) {
            return 1;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int getItemPosition(Object object) {

        return mImgList != null && mImgList.size() == 0 ? POSITION_NONE : super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int index = position % mImgList.size();
        ImageView imageView = mViews.get(index);
        if (imageView.getParent() != null) {
            ((ViewGroup) imageView.getParent()).removeView(imageView);
        }
        container.addView(imageView);
        return imageView;
    }

    //轮播图点击图片的跳转
    @Override
    public void onClick(View v) {
        try {
            int i = (int) v.getTag(R.id.data_tag);
            ClickTypeBean bean = mImgList.get(i);
            if (bean != null) {
                ClickType type = ClickType.valueOf(bean.getClick_type());
                new Jumper().jumper(mContext, type, bean.getData(), mPageName, false);
                LogUtils.d("img","要报的模块中的位置==>"+(i + 1));
                if (null != mItemData && mItemData.size() > 0) {
                    PresentData presentData = mItemData.get(0).getmPresentData();
                    presentData.setPos(mItemData.get(0).getmPresentData().getPos());
                    StatisticsEventData data = StatisticsDataProductorImpl.produceStatisticsData(presentData, false, i + 1, bean.getData().getId(), mPageName, type.toString());
                    DCStat.clickEvent(data);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
