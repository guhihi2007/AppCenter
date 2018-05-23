package cn.lt.android.main.personalcenter.feedback;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.entity.Photo;
import cn.lt.android.main.Item;
import cn.lt.android.main.UIController;
import cn.lt.android.main.appdetail.ImageViewPagerActivity;
import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.main.personalcenter.model.FeedBackBean;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.TimeUtils;
import cn.lt.android.util.UrlUtil;
import cn.lt.android.widget.CircleImageView;
import cn.lt.appstore.R;

/**
 * Created by wenchao on 2016/3/8.
 */
public class FeedbackAdapter extends BaseAdapter {

    private final Context mContext;
    private List<Item<FeedBackBean>> list;
    private FeedBackCallBack feedBackCallBack;

    public FeedbackAdapter(Context context) {
        this.mContext = context;
        this.list = new ArrayList<>();
    }


    public void setList(List<Item<FeedBackBean>> list) {
        this.list = list;
        handleTimeTag();
        this.notifyDataSetChanged();
    }

    public void addItem(Item<FeedBackBean> item) {
        if (item != null) {
            this.list.add(item);
            handleTimeTag();
        }
    }

    /**
     * 按天间隔时间（第一项一定要显示时间）
     */
    public void handleTimeTag() {
        String lastTime = null;
        for (int i = 0; i < list.size(); i++) {
            Item<FeedBackBean> item = list.get(i);
            if (i == 0) {
                item.data.setNeedShowTime(true);
                lastTime = TimeUtils.getStringToString(item.data.getCreated_at());
            } else {
                String time = TimeUtils.getStringToString(item.data.getCreated_at());

                // 如果跟上一个时间不是同一天，则要显示
                if (!lastTime.equals(time)) {
                    item.data.setNeedShowTime(true);
                    lastTime = time;
                } else {
                    item.data.setNeedShowTime(false);
                }
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        return list.get(position).viewType;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Item<FeedBackBean> getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case FeedBackActivity.MSG_LEFT_TEXT:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_msg_left_text, parent, false);
                    holder = new LeftTextViewHolder(convertView);
                    break;
                case FeedBackActivity.MSG_LEFT_IMAGE:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_msg_left_image, parent, false);
                    holder = new LeftImageViewHolder(convertView);
                    break;
                case FeedBackActivity.MSG_RIGHT_TEXT:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_msg_right_text, parent, false);
                    holder = new RightTextViewHolder(convertView);
                    break;
                case FeedBackActivity.MSG_RIGHT_IMAGE:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_msg_right_image, parent, false);
                    holder = new RightImageViewHolder(convertView);
                    break;
                case FeedBackActivity.MSG_DEFAULT:
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_msg_default, parent, false);
                    holder = new DefaultViewHolder(convertView);
                    break;
            }

            convertView.setTag(holder);
        } else {
            holder = (MyViewHolder) convertView.getTag();
        }

        switch (type) {
            case FeedBackActivity.MSG_LEFT_TEXT:
                leftTextSetData((LeftTextViewHolder) holder, position);
                break;
            case FeedBackActivity.MSG_LEFT_IMAGE:
                leftImgSetData((LeftImageViewHolder) holder, position);
                break;
            case FeedBackActivity.MSG_RIGHT_TEXT:
                rightTextSetData((RightTextViewHolder) holder, position);
                break;
            case FeedBackActivity.MSG_RIGHT_IMAGE:
                rightImgSetData((RightImageViewHolder) holder, position);
                break;
            case FeedBackActivity.MSG_DEFAULT:
                defaultSetData((DefaultViewHolder) holder, position);
                break;
        }

        // 判断是否要显示时间（已经按天间隔）
        boolean needShowTime = list.get(position).data.isNeedShowTime();
        holder.showTime(needShowTime);

        return convertView;
    }

    /**
     * 设置左边文字item数据
     */
    private void leftTextSetData(LeftTextViewHolder holder, int position) {
        FeedBackBean bean = list.get(position).data;
        holder.msg.setText(bean.getContent());
        holder.time.setText(TimeUtils.getStringToString(bean.getCreated_at()));
    }

    /**
     * 设置左边图片item数据
     */
    private void leftImgSetData(LeftImageViewHolder holder, int position) {
        FeedBackBean bean = list.get(position).data;
        ImageloaderUtil.loadLTLogo(mContext, bean.getThumb_url(), holder.image);
        holder.time.setText(TimeUtils.getStringToString(bean.getCreated_at()));
    }

    /**
     * 设置右边文字item数据
     */
    private void rightTextSetData(RightTextViewHolder holder, final int position) {
        setUserAvatar(holder.head);
        final Item<FeedBackBean> item = list.get(position);
        FeedBackBean bean = item.data;
        holder.msg.setText(bean.getContent());
        holder.time.setText(TimeUtils.getStringToString(bean.getCreated_at()));
        checkMsgState(bean.getSendState(), holder.failureView, holder.progressBar);

        holder.failureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedBackCallBack != null) {
                    feedBackCallBack.onTextFailureClick(item);
                }
            }
        });
    }

    /**
     * 设置右边图片item数据
     */
    private void rightImgSetData(final RightImageViewHolder holder, int position) {
        setUserAvatar(holder.head);
        final Item<FeedBackBean> item = list.get(position);
        FeedBackBean bean = item.data;
        String imagePath = getSmallImagePath(bean);
//        String imagePath = "http://upload.news.cecb2b.com/2016/0327/1459067610166.jpg";

        if (!imagePath.contains("file")) {
            imagePath = UrlUtil.getImageUrl(imagePath);
        }
//        BitmapImageViewTarget target = getBitmapImageViewTarget(holder.image);
//        Glide.with((Activity) mContext).load(imagePath).asBitmap().placeholder(R.mipmap.app_default).error(R.mipmap.app_default).into(target);
        ImageloaderUtil.loadImage(mContext,imagePath,holder.image);

        holder.time.setText(TimeUtils.getStringToString(bean.getCreated_at()));
        checkMsgState(bean.getSendState(), holder.failureView, holder.progressBar);

        setImageListener(bean, holder.image);
        holder.showUploadImageProgress(bean.isShowImageProgress());
//        holder.feedbackListItemProgressText.setText(bean.getImageProgress() + "%");
        holder.failureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feedBackCallBack != null) {
                    feedBackCallBack.onImageFailureClick(item);
                }
            }
        });
    }

    private BitmapImageViewTarget getBitmapImageViewTarget(ImageView imageView) {
        return new BitmapImageViewTarget(imageView) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                super.onResourceReady(resource, glideAnimation);

                int width = resource.getWidth();
                int height = resource.getHeight();
                int screenWidth = DensityUtil.getScreenSize(mContext)[0];

                float scale = 0;

                // 横屏图片
                if (width > height) {
                    scale = (float) (screenWidth * 0.35) / (float) width;
                }

                // 竖屏图片
                if (height > width) {
                    scale = (float) (screenWidth * 0.2) / (float) width;
                }

                Matrix matrix = new Matrix();

                matrix.postScale(scale, scale); //长和宽放大缩小的比例
                Bitmap resizeBmp = Bitmap.createBitmap(resource, 0, 0, width, height, matrix, true);
                view.setImageBitmap(resizeBmp);
            }
        };
    }

    public void updateProgress(int position, ListView listView) {
        int firstPosi = listView.getFirstVisiblePosition();
        int lastPosi = listView.getLastVisiblePosition();
        if (position >= firstPosi && position <= lastPosi) {
            View view = listView.getChildAt(position - firstPosi);
            if (view.getTag() instanceof RightImageViewHolder) {
                RightImageViewHolder holder = (RightImageViewHolder) view.getTag();
                FeedBackBean bean = list.get(position).data;
                holder.showUploadImageProgress(bean.isShowImageProgress());
                holder.feedbackListItemProgressText.setText(bean.getImageProgress() + "%");
            }
        }


    }

    /**
     * 设置用户头像
     */
    private void setUserAvatar(ImageView imageView) {
        boolean isLogin = UserInfoManager.instance().isLogin();
        // 如果用户登录了，设置用户头像，否则设置默认头像
        if (isLogin) {
            String avatarUrl = UserInfoManager.instance().getUserInfo().getAvatar();
            ImageloaderUtil.loadLTLogo(mContext,avatarUrl,imageView);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * 根据状态显示错误标志、转圈圈标志
     */
    private void checkMsgState(int state, ImageView failureView, ProgressBar progressBar) {
        switch (state) {
            // 发送成功
            case FeedBackActivity.SEND_SUCCESS:
                failureView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;

            // 发送失败
            case FeedBackActivity.SEND_FAILED:
                failureView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;

            // 发送中
            case FeedBackActivity.SEND_ING:
                failureView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String getSmallImagePath(FeedBackBean bean) {
        final String imagePath;
        if (!TextUtils.isEmpty(bean.getImagePath())) {
            imagePath = "file://" + bean.getImagePath();
        } else if (!TextUtils.isEmpty(bean.getThumb_url())) {
            imagePath = bean.getThumb_url();
        } else {
            imagePath = "";
        }
        return imagePath;
    }

    /**
     * 设置用户发的图片的点击事件
     */
    private void setImageListener(final FeedBackBean bean, ImageView image) {
        switch (bean.getSendState()) {
            // 发送成功
            case FeedBackActivity.SEND_SUCCESS:
                // 发送失败/发送中
            case FeedBackActivity.SEND_FAILED: {
                image.setClickable(true);
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtils.i("huikui", "图片被点击了");
                        Photo photo = new Photo();
                        photo.original = getBigImageUrl(bean);
                        photo.thumbnail = getSmallImagePath(bean);
                        List<Photo> pList = new ArrayList<>();
                        pList.add(photo);
                        UIController.jumpToImageBrowster((Activity) mContext, new ImageViewPagerActivity.ImageUrl(pList), 0);
                    }
                });
            }
            break;
            case FeedBackActivity.SEND_ING:
                image.setClickable(false);
                break;
        }
    }

    private String getBigImageUrl(FeedBackBean bean) {
        final String imagePath;
        if (!TextUtils.isEmpty(bean.getImagePath())) {
            imagePath = "file://" + bean.getImagePath();
        } else if (!TextUtils.isEmpty(bean.getImage_url())) {
            imagePath = bean.getImage_url();
        } else {
            imagePath = "";
        }
        return imagePath;
    }

    private void defaultSetData(DefaultViewHolder holder, int position) {
        String time = TimeUtils.getStringToString(list.get(position).data.getCreated_at());
        holder.time.setText(time);
    }

    class LeftTextViewHolder extends MyViewHolder {
        TextView msg;

        public LeftTextViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.msg);
        }
    }

    class LeftImageViewHolder extends MyViewHolder {
        ImageView image;

        public LeftImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    class RightTextViewHolder extends MyViewHolder {
        TextView msg;
        ProgressBar progressBar;
        ImageView failureView;

        public RightTextViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.msg);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            failureView = (ImageView) itemView.findViewById(R.id.failure_view);

        }
    }

    class RightImageViewHolder extends MyViewHolder {
        ImageView image;
        ImageView failureView;
        RelativeLayout feedbackListitemProgressLayout;
        ProgressBar progressBar;
        TextView feedbackListItemProgressText;

        public RightImageViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            failureView = (ImageView) itemView.findViewById(R.id.failure_view);
            feedbackListitemProgressLayout = (RelativeLayout) itemView.findViewById(R.id.feedback_listitem_progressLayout);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            feedbackListItemProgressText = (TextView) itemView.findViewById(R.id.feedback_listItem_progressText);
        }

        void showUploadImageProgress(boolean show) {
            if (show) {
                feedbackListitemProgressLayout.setVisibility(View.VISIBLE);
            } else {
                feedbackListitemProgressLayout.setVisibility(View.GONE);
            }
        }
    }

    class DefaultViewHolder extends MyViewHolder {
        TextView msg;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.msg);

            setData();
        }

        private void setData() {
//            time.setText(cn.lt.framework.util.TimeUtils.getCurrentTimeInString());

            String hint = "若在使用应用中心时出现问题，请在此处留言向我们反馈。\n";
            String contacts = "请留下您的QQ、邮箱或电话号码";
            SpannableStringBuilder style = new SpannableStringBuilder(hint);
            style.append(contacts);
            int size14sp = mContext.getResources().getDimensionPixelSize(R.dimen.font_size_14sp);

            // 设置字体大小
            style.setSpan(new AbsoluteSizeSpan(size14sp), hint.length(), style.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 设置字体颜色
            style.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.orange)), hint.length(), style.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            msg.setText(style);
        }
    }

    class MyViewHolder {
        TextView time;
        CircleImageView head;

        MyViewHolder(View itemView) {
            time = (TextView) itemView.findViewById(R.id.time);
            head = (CircleImageView) itemView.findViewById(R.id.head);
        }

        // 是否需要显示时间
        void showTime(boolean needShow) {

            if (needShow) {
                time.setVisibility(View.VISIBLE);
            } else {
                time.setVisibility(View.GONE);
            }
        }
    }

    public void setFeedBackCallBack(FeedBackCallBack feedBackCallBack) {
        this.feedBackCallBack = feedBackCallBack;
    }

    interface FeedBackCallBack {
        void onTextFailureClick(Item<FeedBackBean> item);

        void onImageFailureClick(Item<FeedBackBean> item);
    }
}
