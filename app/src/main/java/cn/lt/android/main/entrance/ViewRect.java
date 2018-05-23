package cn.lt.android.main.entrance;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.view.View;

@SuppressLint("ParcelCreator")
public class ViewRect extends RectF {

	private ViewRect(float left, float top, float right, float bottom) {
		super(left, top, right, bottom);
//		Log.i("test", "left:"+left+",top:"+top+",right:"+right+",bootom:"+bottom);
	}

	public static ViewRect getRect(View view) {
		if (view != null && view.getVisibility() == View.VISIBLE) {
			int[] location = new int[2];
			view.getLocationInWindow(location);
			return new ViewRect(0, location[1], location[0]+view.getWidth(),
					location[1]+view.getHeight());
		}
		return null;
	}

	@Override
	public boolean contains(float left, float top, float right, float bottom) {
		return super.contains(left, top, right, bottom);
	}

	@Override
	public boolean contains(float x, float y) {
		return super.contains(x, y);
	}

	@Override
	public boolean contains(RectF r) {
		return super.contains(r);
	}

	public static boolean isHorizontalMove(float preX, float preY, float x,
			float y) {
		if (Math.abs(x - preX) > Math.abs(y - preY)) {
			return true;
		}
		return false;
	}
}
