package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.search.SearchActivity;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.appstore.R;

/**
 * Created by ltbl on 2016/3/12.
 */
public class ItemAutoSearchView extends ItemView {

    private TextView mValueTV;

    public ItemAutoSearchView(Context context, String pageName) {
        super(context, pageName);
        initView();
    }

    public ItemAutoSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.autosearch_item, this);
        mValueTV = (TextView) findViewById(R.id.tv_value);
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        try {
            final HotSearchBean app = (HotSearchBean) bean.getmData();
            mValueTV.setText(app.getTitle());
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SearchActivity) (getContext())).gotoSearchResultFragment(app.getTitle(),"_mh");
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
