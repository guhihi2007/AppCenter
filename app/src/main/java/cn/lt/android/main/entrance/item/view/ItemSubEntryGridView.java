package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;

/**
 * Created by erosion on 2016/5/25.
 * 软件/游戏页二级入口 4或4的倍数
 */
public class ItemSubEntryGridView extends ItemView {
    List<ItemData<ClickTypeBean>> mEntrys = new ArrayList<>();
    private ImageAdapter mAdapter;
    public View mChangePadding;

    public ItemSubEntryGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ItemSubEntryGridView(Context context, String pageName) {
        super(context, pageName);
        init();
    }

    public ItemSubEntryGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        if (bean != null) {
            mEntrys = (List<ItemData<ClickTypeBean>>) bean.getmData();
            Logger.i("mEntrys = " + mEntrys.size());
            if (mEntrys != null && mEntrys.size() > 0) {
                mAdapter = new ImageAdapter(getContext(), mEntrys);
                view.setAdapter(mAdapter);
                for (int i = 0; i < mEntrys.size(); i++) {
                    ItemView itemView = (ItemView) mAdapter.getItem(i);
                    itemView.fillView(mEntrys.get(i), i);
                }
            }
        }
    }

    private MyGridView view;

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_gridview, this);
        view = (MyGridView) findViewById(R.id.gridview);
        mChangePadding = findViewById(R.id.padding_changing);
    }


    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private List<ItemData<ClickTypeBean>> mData;

        public ImageAdapter(Context context, List<ItemData<ClickTypeBean>> bean) {
            this.mContext = context;
            this.mData = bean;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_gridview_item, null);
                holder = new Holder();

                holder.mIconIv = (ImageView) convertView.findViewById(R.id.iv_icon_entry_elem);
                holder.mNameTv = (TextView) convertView.findViewById(R.id.tv_name_entry_elem);
                holder.layout = (LinearLayout) convertView.findViewById(R.id.layout_entry);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            if (mData != null) {
                String title = mData.get(position).getmData().getAlias();
                title = TextUtils.isEmpty(title) ? "常用" : title;
                LogUtils.i("title = " + title + mEntrys.size());
                String url = GlobalConfig.combineImageUrl(mData.get(position).getmData().getImage());
                holder.mNameTv.setText(title);
                ImageloaderUtil.loadImage(getContext(), url, holder.mIconIv);
                holder.layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClickType type = ClickType.valueOf(mData.get(position).getmData().getClick_type());
                        new Jumper().jumper(getContext(), type, mData.get(position).getmData().getData(), mPageName, false);
                        if (mData.size() > 0) {
                            PresentData presentData = mData.get(position).getmPresentData();
                            presentData.setPos(mData.get(position).getPos());
                            StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(presentData, false, position + 1, mData.get(position).getmData().getData().getId(), mPageName, type.toString());
                            DCStat.clickEvent(eventData);
                        }
                    }
                });
            }
            return convertView;

        }


        private class Holder {
            private ImageView mIconIv;
            private TextView mNameTv;
            private LinearLayout layout;
        }

    }
}
