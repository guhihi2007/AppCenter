package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.util.DensityUtil;
import cn.lt.appstore.R;

/***
 * Created by dxx on 2016/2/26.
 * app集合的容器
 */
@SuppressWarnings("ALL")
public class ItemAppVerticalRootView extends ItemView {
    public LinearLayout mRoot;

    public ItemAppVerticalRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ItemAppVerticalRootView(Context context, String pageName, String id) {
        super(context, pageName, id);
        init();
    }

    public ItemAppVerticalRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        try {
            if (bean != null) {
                List<ItemData<AppBriefBean>> datas = (List<ItemData<AppBriefBean>>) bean.getmData();
                if (datas != null) {
                    int count = datas.size();
                    produceChild(count);
                    for (int i = 0; i < count; i++) {
                        ItemView view = (ItemView) mRoot.getChildAt(i);
                        view.setVisibility(View.VISIBLE);
                        view.fillView(datas.get(i), i);
                    }
                }
            }
            mRoot.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void produceChild(int count) {
        int children = mRoot.getChildCount();
        if (children > count) {
            for (int i = count; i < children; i++) {
                mRoot.getChildAt(i).setVisibility(View.GONE);
            }
        } else if (children < count) {
            for (int i = children; i < count; i++) {
                ItemSingleAppView v = new ItemSingleAppView(getContext(), mPageName, mID);
                mRoot.addView(v);
            }
        } else {
            for (int i = 0; i < mRoot.getChildCount(); i++) {
                mRoot.getChildAt(i).setVisibility(View.VISIBLE);
            }
        }
    }


    private void init() {
        mRoot = new LinearLayout(getContext());
        int padding = getResources().getDimensionPixelOffset(R.dimen.padding_eight_dp);
        mRoot.setPadding(0, 0, 0, padding);
        mRoot.setOrientation(VERTICAL);
        mRoot.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams lp = new LayoutParams(DensityUtil.getScreenSize(getContext())[0], ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mRoot, lp);
        mRoot.setVisibility(View.GONE);
    }
}
