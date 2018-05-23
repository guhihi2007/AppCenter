package cn.lt.android.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.main.entrance.item.view.ItemAbrahamianView;
import cn.lt.android.main.entrance.item.view.ItemAppVerticalRootView;
import cn.lt.android.main.entrance.item.view.ItemBannerView;
import cn.lt.android.main.entrance.item.view.ItemGarbView;
import cn.lt.android.main.entrance.item.view.ItemRecommendEntryView;
import cn.lt.android.main.entrance.item.view.ItemSingleAppView;
import cn.lt.android.main.entrance.item.view.ItemSubEntryGridView;
import cn.lt.android.main.entrance.item.view.ItemView;
import cn.lt.android.main.entrance.item.view.carousel.BannerView;
import cn.lt.android.main.entrance.item.view.factory.ItemViewHolderFactory;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.pullandloadmore.BaseLoadMoreRecyclerAdapter;

/***
 * 这是recycleview的适配器
 * Created by dxx on 2016/2/25.
 */
public class EntranceAdapter<T extends BaseBean> extends BaseLoadMoreRecyclerAdapter<ItemData<T>, EntranceAdapter.ViewHolder> {

    private String mPageName;
    private String id;
    private List<BannerView> mBanners = new ArrayList<>();
//    private SparseArray<ItemView> list = new SparseArray<>();

    public EntranceAdapter(Context context, String pageName, String id) {
        super(context);
        this.mPageName = pageName;
        this.id = id;
    }

    @Override
    public ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
        ViewHolder holder = null;
        try {
            PresentType presentType = PresentType.values()[i];
            holder = ItemViewHolderFactory.produceItemViewHolder(presentType, viewGroup.getContext(), mPageName,id);
            if (presentType == PresentType.carousel) {
                mBanners.add((BannerView) holder.getmView());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return holder;
    }

    public void startBannerTimer() {
        for (BannerView view : mBanners) {
            view.startBannerTimer();
        }
    }


    public void stopBannerTimer() {
        for (BannerView view : mBanners) {
            view.stopBannerTimer();
        }
    }

    @Override
    public void onBindItemViewHolder(ViewHolder viewHolder, int i) {
        LogUtils.i("EntranceAdapter", "位置是："+i+"当前类型：==>"+PresentType.values()[getViewType(i)]);

        try {
            viewHolder.getmView().fillView(getList().get(i), i);
       /*************************动态改变底部Item条纹********************/
            if (i == getItemCount() - 3) {
                setItemDecoration(viewHolder, PresentType.values()[getViewType(i)], 0);
            } else {
                setItemDecoration(viewHolder, PresentType.values()[getViewType(i)], DensityUtil.dip2px(mContext, 8));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制Item条纹的显示与隐藏
     * @param viewHolder
     * @param bottom
     */
    private void setItemDecoration(ViewHolder viewHolder, PresentType presentType, int bottom) {
        if (presentType == PresentType.pic_topic) {
            ((LinearLayout.LayoutParams) ((ItemBannerView) viewHolder.getmView()).mChangePadding.getLayoutParams()).bottomMargin = bottom;
        } else if (presentType == PresentType.apps) {
            ((ItemAppVerticalRootView) viewHolder.getmView()).mRoot.setPadding(0, 0, 0, bottom);
        } else if (presentType == PresentType.entry) {
                ((ItemRecommendEntryView) viewHolder.getmView()).mChangePadding.setPadding(0, 0, 0, bottom);
        } else if (presentType == PresentType.sub_entry) {
                ((ItemSubEntryGridView) viewHolder.getmView()).mChangePadding.setPadding(0, 0, 0, bottom);
        } else if (presentType == PresentType.abrahamian) {
            ((ItemAbrahamianView) viewHolder.getmView()).mChangePadding.setPadding(0, 0, 0, bottom);
        } else if (presentType == PresentType.app_topic) {
            if (bottom != DensityUtil.dip2px(mContext, 8)) {
                ((LinearLayout.LayoutParams) ((ItemGarbView) viewHolder.getmView()).mChangePadding.getLayoutParams()).bottomMargin = bottom;
            }
        } else if (presentType == PresentType.app) {
            if (bottom != DensityUtil.dip2px(mContext, 8)) {
                ((LinearLayout.LayoutParams) ((ItemSingleAppView) viewHolder.getmView()).rootLayout.getLayoutParams()).bottomMargin = bottom;
            }
        }
    }

    @Override
    public int getViewType(int position) {
        return getList().get(position).getmPresentType().viewType;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemView mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = (ItemView) itemView;
        }

        public ItemView getmView() {
            return mView;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}
