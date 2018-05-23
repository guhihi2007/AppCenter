package cn.lt.android.main.specialtopic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.GlobalConfig;
import cn.lt.android.entity.SpecialTopicBean;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;

/**
 * Created by LinJunSheng on 2016/3/1.
 */
public class SpecailTopicAdapter extends BaseLoadMoreRecyclerAdapter<SpecialTopicBean, SpecailTopicAdapter.ViewHolder> {


    private final LayoutInflater layoutInflater;
    private final Context context;
    private OnSpecailTopicItemClickListener clickListener;

    public SpecailTopicAdapter(Context context) {
        super(context);
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
        View v = layoutInflater.inflate(R.layout.layout_item_special_topic, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindItemViewHolder(final ViewHolder viewHolder, int i) {
        final SpecialTopicBean bean = getList().get(i);
        if (bean != null) {
            ImageloaderUtil.loadBigImage(mContext, bean.getBanner(), viewHolder.iv_specialImage);
            viewHolder.tv_specialTopicTitle.setText(bean.getTitle());
        }

        viewHolder.root_sp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick(bean.getId(), bean.getTitle());
                }
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout root_sp;
        public TextView tv_specialTopicTitle;
        public ImageView iv_specialImage;
        private int imageViewHeight;

        public ViewHolder(View itemView) {
            super(itemView);
            root_sp = (RelativeLayout) itemView.findViewById(R.id.root_sp);
            iv_specialImage = (ImageView) itemView.findViewById(R.id.iv_specialImage);
            tv_specialTopicTitle = (TextView) itemView.findViewById(R.id.tv_specialTopicTitle);

            getImageViewHeight();
            setImageViewHeight();
        }


        // 计算imageView的高度（通过网络图片大小来计算适配的高度）
        private void getImageViewHeight() {
            int paddingLeft = root_sp.getPaddingLeft();
            int paddingRight = root_sp.getPaddingRight();

            //请求下来图片的width
            int imagWidth = context.getResources().getDimensionPixelOffset(R.dimen.specialTopic_image_width);

            //请求下来图片的height
            int imageHeight = context.getResources().getDimensionPixelOffset(R.dimen.specialTopic_image_height);

            //获取屏幕的宽度
            int scrennwidth = ScreenUtils.getScreenWidth(context);

            // imageView的宽度
            int imageViewWidth = scrennwidth - paddingLeft - paddingRight;

            imageViewHeight = GlobalConfig.getImageViewHeight(imageViewWidth, imagWidth, imageHeight);
        }

        // 测量完之后设置imageView的高度
        private void setImageViewHeight() {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv_specialImage.getLayoutParams();
            params.height = imageViewHeight;
        }
    }

    interface OnSpecailTopicItemClickListener {
        void onItemClick(String topicId, String title);
    }

    void setOnSpecailTopicItemClickListener(OnSpecailTopicItemClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
