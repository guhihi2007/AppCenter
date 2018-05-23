package cn.lt.android.main.entrance.item.view;

import android.content.Context;

import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by Administrator on 2015/12/29.
 */
public class ItemViewNull extends ItemView{


    public ItemViewNull(Context context,String pageName) {
        super(context,pageName);
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {

    }


}
