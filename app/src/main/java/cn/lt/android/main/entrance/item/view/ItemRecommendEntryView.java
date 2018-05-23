package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.GlobalConfig;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.main.entrance.Jumper;
import cn.lt.android.main.entrance.data.ClickType;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentData;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatisticsDataProductorImpl;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;

/***
 * Created by Administrator on 2016/2/24.
 * 推荐页顶入口，2的倍数
 */
@SuppressWarnings("ALL")
public class ItemRecommendEntryView extends ItemView implements View.OnClickListener {

    List<ImageView> mIvs = new ArrayList<>();
    private int imageViewWidth;
    private int imagWidth;
    private int imageHeight;
    public View mChangePadding;

    public ItemRecommendEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ItemRecommendEntryView(Context context, String pageName) {
        super(context, pageName);
        init(context);
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        try {
            if (bean != null) {
                mItemData = bean;
                List<ItemData<ClickTypeBean>> lists = (List<ItemData<ClickTypeBean>>) bean.getmData();
                if (lists != null && lists.size() > 0) {
                    for (int j = lists.size(); j < mIvs.size(); j++) {
                        mIvs.get(j).setVisibility(View.GONE);
                    }
                    for (int i = 0; i < lists.size(); i++) {
                        mIvs.get(i).setVisibility(View.VISIBLE);
                        ClickTypeBean clickType = lists.get(i).getmData();
                        //设置ImageView的height
                        LinearLayout.LayoutParams rp = (LayoutParams) mIvs.get(i).getLayoutParams();
                        rp.height = GlobalConfig.getImageViewHeight(imageViewWidth, imagWidth, imageHeight);
                        ImageloaderUtil.loadBigImage(getContext(), clickType.getImage(), mIvs.get(i));
                        mIvs.get(i).setTag(R.id.data_tag, clickType);
                        mIvs.get(i).setTag(R.id.click_date, i);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_item_recommend_entry, this);
        mChangePadding = findViewById(R.id.padding_changing);
        mIvs.add((ImageView) findViewById(R.id.iv_entry_top_left));
        mIvs.add((ImageView) findViewById(R.id.iv_entry_top_right));
        mIvs.add((ImageView) findViewById(R.id.iv_entry_middle_left));
        mIvs.add((ImageView) findViewById(R.id.iv_entry_middle_right));
        mIvs.add((ImageView) findViewById(R.id.iv_bottom_left));
        mIvs.add((ImageView) findViewById(R.id.iv_bottom_right));

        getImageViewWidth();

        for (ImageView v : mIvs) {
            v.setOnClickListener(this);
        }
    }

    /**
     * 获取布局中ImageVIew的宽度
     */
    private void getImageViewWidth() {
        //RelativeLayout布局与左右的间距
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.rl_re);
        int rlPaddingLeft = relativeLayout.getPaddingLeft();
        int rlPaddingRight = relativeLayout.getPaddingRight();

        //leftIv的右边间距
        ImageView leftIv = (ImageView) findViewById(R.id.iv_entry_top_left);
        LinearLayout.LayoutParams lp = (LayoutParams) leftIv.getLayoutParams();
        int lpMR = lp.rightMargin;

        //rightIv的左边间距
        ImageView rightIv = (ImageView) findViewById(R.id.iv_entry_top_right);
        LinearLayout.LayoutParams rp = (LayoutParams) rightIv.getLayoutParams();
        int rpMR = lp.leftMargin;

        //请求下来图片的width
        imagWidth = getContext().getResources().getDimensionPixelOffset(R.dimen.recommended_entrance_image_width);
        //请求下来图片的height
        imageHeight = getContext().getResources().getDimensionPixelOffset(R.dimen.recommended_entrance_image_height);

        //获取屏幕的宽度
        int scrennwidth = ScreenUtils.getScreenWidth(getContext());

        //ImageView的width
        imageViewWidth = (scrennwidth - rlPaddingLeft - rlPaddingRight - lpMR - rpMR) / 2;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        ClickTypeBean bean = (ClickTypeBean) v.getTag(R.id.data_tag);
        new Jumper().jumper(getContext(), ClickType.valueOf(bean.getClick_type()), bean.getData(), mPageName, false);
        PresentData presentData = new PresentData();
        presentData.setmType(mItemData.getmPresentType());
        presentData.setPos(mItemData.getmPresentData().getPos());
        presentData.setPos(mItemData.getmPresentData().getPos());
        int subPos = (int) v.getTag(R.id.click_date);
        presentData.setSubPos(subPos + 1);
        StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(presentData, false, presentData.getSubPos(), bean.getData().getId(), mPageName, bean.getClick_type());
        DCStat.clickEvent(eventData);
    }

}
