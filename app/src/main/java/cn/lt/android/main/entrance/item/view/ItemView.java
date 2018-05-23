package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import cn.lt.android.LTApplication;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by dxx on 2016/2/25.
 */
public abstract class ItemView extends LinearLayout {

    protected String mPageName;
    protected String mID; //页面ID
    protected ItemData mItemData;

    public ItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ItemView(Context context, String pageName, String id) {
        super(context);
        this.mPageName = LTApplication.instance.current_page;
        this.mID = id;
    }

    public ItemView(Context context, String pageName) {
        super(context);
        this.mPageName = LTApplication.instance.current_page;
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public abstract void fillView(ItemData<? extends BaseBean> bean, int position);

    public  void fillManagerView( BaseBean bean, int position){

    }
}
