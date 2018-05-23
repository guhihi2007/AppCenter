package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.lt.android.entity.SearchHistoryBean;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.search.SearchActivity;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/***
 * 搜索历史控件
 */
public class ItemSearchHistoryView extends ItemView implements View.OnClickListener {
    private TextView mHistoryTitle;
    private ImageView mDel;
    private int mPosition;
    private SearchHistoryBean searchHistoryBean;
    private Context mContext;

    public ItemSearchHistoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ItemSearchHistoryView(Context context, String pageName, String id) {
        super(context, pageName, id);
        init(context);
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        mItemData = bean;
        this.mPosition = position;
        searchHistoryBean = (SearchHistoryBean) bean.getmData();
        if (null != searchHistoryBean) {
            mHistoryTitle.setText(searchHistoryBean.getTitle());
        }

    }

    private void init(final Context context) {
        this.mContext = context;
        LayoutInflater.from(getContext()).inflate(R.layout.item_search_history, this);
        mHistoryTitle = (TextView) findViewById(R.id.tv_history_title);
        mDel = (ImageView) findViewById(R.id.iv_history_del);
        mDel.setOnClickListener(this);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SearchActivity) context).gotoSearchResultFragment(searchHistoryBean.getTitle(),"_ls");
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_history_del:
                searchHistoryBean.setPos(mPosition);
                EventBus.getDefault().post(searchHistoryBean);
                break;
        }
    }
}
