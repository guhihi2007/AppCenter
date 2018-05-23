package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.widget.SearchAdvItemView;
import cn.lt.appstore.R;

/***
 * 搜索推荐自定义控件
 */
public class ItemSearchAdvView extends ItemView implements View.OnClickListener {
    private TextView mAdvTitle;
    private LinearLayout mContainer;
    private Context mContext;
    private String type;
    private int mPosition;

    public ItemSearchAdvView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ItemSearchAdvView(Context context, String pageName, String type) {
        super(context, pageName, type);
        init(context);
        this.type = type;

    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        this.mPosition = position;
        if (null == bean) return;
        List<ItemData<HotSearchBean>> datas = (List<ItemData<HotSearchBean>>) bean.getmData();
        if (datas != null && datas.size() > 0) {
            List<HotSearchBean> mDatas = new ArrayList<>();
            for (int i = 0; i < datas.size(); i++) {
                mDatas.add(datas.get(i).getmData());
            }
            mAdvTitle.setText(type);
            produceChild(mDatas);
        }

    }

    private void produceChild(List<HotSearchBean> searchAdvList) {
        int lineCount = searchAdvList.size() / 3;   //总行数
        mContainer.removeAllViews();
        for (int j = 0; j < lineCount; j++) {
            SearchAdvItemView advItemView = new SearchAdvItemView(mContext, type,j);
            advItemView.setData(searchAdvList.subList(j * 3, (j + 1) * 3));
            if (j == 0 && 0 == mPosition) {
                advItemView.setColor();
                mAdvTitle.setTextColor(mContext.getResources().getColor(R.color.orange));
            }
            if (j >= 1) {
                mContainer.addView(dividerView());
            }
            mContainer.addView(advItemView);
        }

    }

    /***
     * 华丽的分隔线
     *
     * @return
     */
    private View dividerView() {
        View divider = new View(mContext);
        divider.setBackgroundColor(mContext.getResources().getColor(R.color.light_grey));
        RelativeLayout.LayoutParams para = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        divider.setLayoutParams(para);
        return divider;
    }

    private void init(final Context context) {
        this.mContext = context;
        LayoutInflater.from(getContext()).inflate(R.layout.item_search_adv_view, this);
        mAdvTitle = (TextView) findViewById(R.id.tv_title);
        mContainer = (LinearLayout) findViewById(R.id.ll_content);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_history_del:
                break;
        }
    }
}
