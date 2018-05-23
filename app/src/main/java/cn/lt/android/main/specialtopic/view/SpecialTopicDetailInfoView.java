package cn.lt.android.main.specialtopic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.GlobalConfig;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.item.view.ItemView;
import cn.lt.android.main.specialtopic.special_detail_bean.SpecialInfoBean;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;

/**
 * Created by LinJunSheng on 2016/3/2.
 */
public class SpecialTopicDetailInfoView extends ItemView {

    private ImageView iv_sTopicImage;
    private TextView tv_sTopicTitle;
    private TextView tv_sTopicIntro;
    private RelativeLayout rl_root;
    private int imageViewHeight;

    public SpecialTopicDetailInfoView(Context context, String pageName) {
        super(context, pageName);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_item_specail_topic_detail_first, this);
        initView();
    }


    public SpecialTopicDetailInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_item_specail_topic_detail_first, this);
        initView();
    }

    private void initView() {
        rl_root = (RelativeLayout) findViewById(R.id.rl_stdinfo);
        iv_sTopicImage = (ImageView) findViewById(R.id.iv_sTopicImage);
        tv_sTopicTitle = (TextView) findViewById(R.id.tv_sTopicTitle);
        tv_sTopicIntro = (TextView) findViewById(R.id.tv_sTopicIntro);
        getImageViewHeight();
        setImageViewHeight();
    }


    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        try {
            SpecialInfoBean info = (SpecialInfoBean) bean.getmData();
            ImageloaderUtil.loadBigImage(getContext(), info.getBanner(), iv_sTopicImage);
            tv_sTopicTitle.setText(info.getTitle());
            tv_sTopicIntro.setText(info.getLead_content());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 计算imageView的高度（通过网络图片大小来计算适配的高度）
    private void getImageViewHeight() {
        int paddingLeft = rl_root.getPaddingLeft();
        int paddingRight = rl_root.getPaddingRight();

        //请求下来图片的width
        int imagWidth = getResources().getDimensionPixelOffset(R.dimen.specialTopic_image_width);

        //请求下来图片的height
        int imageHeight = getResources().getDimensionPixelOffset(R.dimen.specialTopic_image_height);

        //获取屏幕的宽度
        int scrennwidth = ScreenUtils.getScreenWidth(getContext());

        // imageView的宽度
        int imageViewWidth = scrennwidth - paddingLeft - paddingRight;

        imageViewHeight = GlobalConfig.getImageViewHeight(imageViewWidth, imagWidth, imageHeight);
    }

    // 测量完之后设置imageView的高度
    private void setImageViewHeight() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv_sTopicImage.getLayoutParams();
        params.height = imageViewHeight;
    }

}
