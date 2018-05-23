package cn.lt.android.main.appdetail;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.GlobalConfig;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/2/23.
 *
 * @desc 游戏详情头部信息
 */
public class AppBaseInfoView extends RelativeLayout {

    private ImageView mLogo;
    private TextView mTitle;
    private TextView mCategory;
    private TextView mSize;
    private TextView mDownloadCount;
    private TextView mComment;
    private View mView;

    public AppBaseInfoView(Context context) {
        super(context);
        initView(context);
    }

    public AppBaseInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AppBaseInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.app_detail_baseinfo, this);
        mLogo = (ImageView) findViewById(R.id.iv_app_logo);
        mTitle = (TextView) findViewById(R.id.tv_app_name);
        mCategory = (TextView) findViewById(R.id.tv_app_category);
        mSize = (TextView) findViewById(R.id.tv_app_size);
        mDownloadCount = (TextView) findViewById(R.id.tv_download_count);
        mComment = (TextView) findViewById(R.id.app_comment);
        mView = (View) findViewById(R.id.view);
    }

    public void setData(AppDetailBean bean) {
        mTitle.setText(TextUtils.isEmpty(bean.getAlias()) ? bean.getName() : bean.getAlias());
        mCategory.setText(TextUtils.isEmpty(bean.getCategoryName()) ? bean.getCategory() : bean.getCategoryName());
        mSize.setText(IntegratedDataUtil.calculateSizeMB(Long.parseLong(bean.getPackage_size())));
        mDownloadCount.setText(IntegratedDataUtil.calculateCounts(Integer.valueOf(bean.getDownload_count())));
        String imgUrl = GlobalConfig.combineImageUrl(bean.getIcon_url());
        try {
            ImageloaderUtil.loadRoundImage(getContext(), imgUrl, mLogo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(bean.getReviews())) {
            mComment.setVisibility(View.GONE);
            mView.setVisibility(View.GONE);
        } else {
            mComment.setText("小编点评：" + bean.getReviews());
        }
    }
}
