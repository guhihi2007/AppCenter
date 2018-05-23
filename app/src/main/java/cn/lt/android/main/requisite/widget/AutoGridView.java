package cn.lt.android.main.requisite.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import cn.lt.appstore.R;

/***
 * 此类主要用作实现在listview的item中添加gridview时可以完全显示grideview的每个item；
 * 以及在gridview的每行之间的添加分割线效果；
 * 
 * @author daxingxiang
 * 
 */
public class AutoGridView extends GridView {

	private Context mContext;

	public AutoGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.makeMeasureSpec(mContext.getResources()
				.getDimensionPixelOffset(R.dimen.index_requisite_max_hieght),
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, height);
	}

}
