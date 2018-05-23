package cn.lt.android.main.recommend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.entity.AppCatBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.CategoryNameBean;
import cn.lt.android.main.Item;
import cn.lt.android.main.UIController;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatisticsDataProductorImpl;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ViewUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.StringUtils;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;

/**
 * Created by wenchao on 2016/3/1.
 */
public class CategoryAdapter extends BaseLoadMoreRecyclerAdapter<Item, RecyclerView.ViewHolder> {
    /**
     * 分类标签
     */
    public static final int TYPE_LABEL = 0;
    /**
     * 2个小分类
     */
    public static final int TYPE_CATEGORY_TWO = 1;


    private String pageName;

    public CategoryAdapter(Context context, String pageName) {
        super(context);
        this.pageName = pageName;
    }

    @Override
    public int getViewType(int position) {
        return getList().get(position).viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder h = null;
        switch (viewType) {
            case TYPE_LABEL:
                View labelView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_label, parent, false);
                h = new LabelViewHolder(labelView);
                break;
            case TYPE_CATEGORY_TWO:
                View categoryTwoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_two, parent, false);
                h = new CategoryTwoViewHolder(categoryTwoView);
                break;
        }
        return h;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LabelViewHolder) {
            bindLabel((LabelViewHolder) holder, position);
        } else if (holder instanceof CategoryTwoViewHolder) {
            CategoryTwoViewHolder h = (CategoryTwoViewHolder) holder;
            bindCategoryTwo(h, position);
        }
    }

    void bindCategoryTwo(CategoryTwoViewHolder h, final int position) {
        List<AppCatBean> bean = (List<AppCatBean>) getList().get(position).data;
        if (bean.size() > 0) {
            final AppCatBean cat01 = bean.get(0);
            bindOneCategory(cat01, h.icon01, h.name01, h.labelLeft01, h.labelRight01);

            h.categoryClick01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewUtils.isFastClick()) return;
                    UIController.goCategoryDetail(mContext, cat01.getType(), cat01.getId(), cat01.getTitle());
                    StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData("分类列表", cat01.getId(), pageName, position * 2 + 1, "");
                    DCStat.clickEvent(eventData);
                }
            });
        }
        if (bean.size() > 1) {
            final AppCatBean cat02 = bean.get(1);
            bindOneCategory(cat02, h.icon02, h.name02, h.labelLeft02, h.labelRight02);
            h.layout02.setVisibility(View.VISIBLE);

            h.categoryClick02.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ViewUtils.isFastClick()) return;
                    UIController.goCategoryDetail(mContext, cat02.getType(), cat02.getId(), cat02.getTitle());
                    StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData("分类列表", cat02.getId(), pageName, position * 2 + 2, "");
                    DCStat.clickEvent(eventData);
                }
            });
        } else {
            h.layout02.setVisibility(View.GONE);
        }


    }

    void bindLabel(LabelViewHolder h, int position) {
        CategoryNameBean bean = (CategoryNameBean) getList().get(position).data;
        h.name.setText(StringUtils.nullStrToEmpty(bean.getTitle()));
    }

    void bindOneCategory(final AppCatBean bean, ImageView icon, TextView name, TextView label01, TextView label02) {
        ImageloaderUtil.loadImage(mContext, bean.getImage(), icon);
        name.setText((bean.getTitle().length() <= 4 ? bean.getTitle() : bean.getTitle().substring(0, 4).concat("...")));
        final AppDetailBean[] apps = bean.getApps();
        label01.setVisibility(View.GONE);
        label02.setVisibility(View.GONE);
        if (apps != null && apps.length > 0) {
            label01.setText(apps[0].getName());
//            label01.setVisibility(View.VISIBLE);//暂时屏蔽
            label01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtils.i("zzz", "id = " + apps[0].getAppClientId());
                    UIController.goAppDetail(mContext, false, "", apps[0].getAppClientId(), "", apps[0].getApps_type(), pageName, "", apps[0].getDownload_url());
                }
            });
            if (apps.length > 1) {
                label02.setText(apps[1].getName());
//                label02.setVisibility(View.VISIBLE);//暂时屏蔽
                label02.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtils.i("zzz", "id222 = " + apps[1].getAppClientId());
                        UIController.goAppDetail(mContext, false, "", apps[1].getAppClientId(), "", apps[1].getApps_type(), pageName, "", apps[0].getDownload_url());
                    }
                });
            }
        }
    }


    class CategoryTwoViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout categoryClick01;
        ImageView icon01;
        TextView name01;
        TextView labelLeft01;
        TextView labelRight01;
        RelativeLayout categoryClick02;
        ImageView icon02;
        TextView name02;
        TextView labelLeft02;
        TextView labelRight02;
        View layout02;


        public CategoryTwoViewHolder(View itemView) {
            super(itemView);
            categoryClick01 = (RelativeLayout) itemView.findViewById(R.id.category_click_01);
            icon01 = (ImageView) itemView.findViewById(R.id.icon_01);
            name01 = (TextView) itemView.findViewById(R.id.name_01);
            labelLeft01 = (TextView) itemView.findViewById(R.id.label_left_01);
            labelRight01 = (TextView) itemView.findViewById(R.id.label_right_01);
            categoryClick02 = (RelativeLayout) itemView.findViewById(R.id.category_click_02);
            icon02 = (ImageView) itemView.findViewById(R.id.icon_02);
            name02 = (TextView) itemView.findViewById(R.id.name_02);
            labelLeft02 = (TextView) itemView.findViewById(R.id.label_left_02);
            labelRight02 = (TextView) itemView.findViewById(R.id.label_right_02);
            layout02 = itemView.findViewById(R.id.layout_02);
        }
    }

    class LabelViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public LabelViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }

}
