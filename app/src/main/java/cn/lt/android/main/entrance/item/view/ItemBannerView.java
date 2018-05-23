package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.GlobalConfig;
import cn.lt.android.entity.ClickTypeDataBean;
import cn.lt.android.entity.PicTopicBean;
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
 * dxx
 */

/***
 * 广告专题
 */
public class ItemBannerView extends ItemView implements View.OnClickListener {

    private ImageView mIv;
    private TextView mName;
    private TextView mTitle;
    private PicTopicBean mTopic;
    private LinearLayout mTextContainer;
    private int imageViewHeight;
    public View mChangePadding;

    public ItemBannerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ItemBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ItemBannerView(Context context, String pageName) {
        super(context, pageName);
        init();
    }


    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        if (bean != null) {
            mItemData = bean;
            mTopic = (PicTopicBean) bean.getmData();
            String textColor = mTopic.getTitle_color();

            if (mTopic.getTopic_title().toString().length() == 0 || textColor.length() == 0 || mTopic.getTopic_name().length() == 0) {
                mTextContainer.setVisibility(View.GONE);
            }

            if (mTopic.getTopic_title().toString().length() != 0 && textColor.length() != 0 && mTopic.getTopic_name().length() != 0) {
                mTextContainer.setVisibility(View.VISIBLE);
                mTitle.setText(mTopic.getTopic_title());
                try {
                    mTitle.setBackgroundColor(Color.parseColor(textColor));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mName.setText(mTopic.getTopic_name());
            }
            ClickTypeDataBean click = mTopic.getData();
            if (click != null) {
                ImageloaderUtil.loadRectImage(getContext(), mTopic.getImage(), mIv);
            }
        }
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_item_banner, this);
        mChangePadding = findViewById(R.id.change_padding);
        mIv = (ImageView) findViewById(R.id.bigImageIv);
        mTitle = (TextView) findViewById(R.id.tv_title);
        mName = (TextView) findViewById(R.id.tv_name);
        mTextContainer = (LinearLayout) findViewById(R.id.llt_title_root);
        mIv.setOnClickListener(this);

        getImageViewHeight();
        setImageViewHeight();
    }


    @Override
    public void onClick(View v) {
        ClickType type = ClickType.valueOf(mTopic.getClick_type());
        new Jumper().jumper(getContext(), type, mTopic.getData(), mPageName, false);
        PresentData presentData = mItemData.getmPresentData();
        presentData.setPos(presentData.getPos());
        StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(presentData, false, 1, mTopic.getData().getId(), mPageName, type.toString());
        DCStat.clickEvent(eventData);
    }

    // 计算imageView的高度（通过网络图片大小来计算适配的高度）
    private void getImageViewHeight() {
        int leftMargin = ((RelativeLayout.LayoutParams) mIv.getLayoutParams()).leftMargin;
        int rightMargin = ((RelativeLayout.LayoutParams) mIv.getLayoutParams()).rightMargin;

        //请求下来图片的width
        int imagWidth = getResources().getDimensionPixelOffset(R.dimen.specialTopic_image_width);

        //请求下来图片的height
        int imageHeight = getResources().getDimensionPixelOffset(R.dimen.specialTopic_image_height);

        //获取屏幕的宽度
        int scrennwidth = ScreenUtils.getScreenWidth(getContext());

        // imageView的宽度
        int imageViewWidth = scrennwidth - leftMargin - rightMargin;

        imageViewHeight = GlobalConfig.getImageViewHeight(imageViewWidth, imagWidth, imageHeight);
    }

    // 测量完之后设置imageView的高度
    private void setImageViewHeight() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIv.getLayoutParams();
        params.height = imageViewHeight;
    }
}
