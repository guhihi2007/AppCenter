package cn.lt.android.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import cn.lt.android.util.LogUtils;

/**
 * @创建者	 chengyong
 * @创建时间 	 2016-7-10 下午2:14:52
 * @描述	     用于解决RankRoot的子Fragment与父ViewPager的滑动冲突
 */
public class RankTabInnerViewPager extends ViewPager {
	public static final String	TAG	= RankTabInnerViewPager.class.getSimpleName();
	private float				mDownX;
	private float				mDownY;

	//在xml中使用的时候
	public RankTabInnerViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//直接在代码中new的时候
	public RankTabInnerViewPager(Context context) {
		super(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				LogUtils.i(TAG, "InnerViewPager-->ACTION_DOWN");
				mDownX = ev.getRawX();
				mDownY = ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				LogUtils.i(TAG, "InnerViewPager-->ACTION_MOVE");
				float moveX = ev.getRawX();
				float moveY = ev.getRawY();

				int diffX = (int) (moveX - mDownX + .5f);
				int diffY = (int) (moveY - mDownY + .5f);

				if (Math.abs(diffX) > Math.abs(diffY)) {//水平滚动
					int position = this.getCurrentItem();
					if (position == 0) {//第一个点
						if (diffX < 0) {//往左滑动
							LogUtils.i(TAG, "第一个点-往左滑动-自己处理");
							getParent().requestDisallowInterceptTouchEvent(true);//自己处理
						} else {
							getParent().requestDisallowInterceptTouchEvent(false);//父容器处理
							LogUtils.i(TAG, "第一个点-往右滑动-父容器处理");
						}
					} else if (position == getAdapter().getCount() - 1) {//最后一个点
						if (diffX < 0) {//往左滑动
							getParent().requestDisallowInterceptTouchEvent(false);//父容器处理
							LogUtils.i(TAG, "最后一个点-往左滑动-父容器处理");
						} else {
							getParent().requestDisallowInterceptTouchEvent(true);//自己处理
							LogUtils.i(TAG, "最后一个点-往右滑动-自己处理");
						}
					} else {//中间点
						getParent().requestDisallowInterceptTouchEvent(true);//自己处理
						LogUtils.i(TAG, "中间的点-自己处理");
					}
				} else {//垂直滚动
					getParent().requestDisallowInterceptTouchEvent(false);
					LogUtils.i(TAG, "垂直滚动-父容器处理");
				}

				break;
			case MotionEvent.ACTION_UP:

				break;

			default:
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		return super.onTouchEvent(ev);
	}
}