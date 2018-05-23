package cn.lt.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by tiantian on 2015/11/12.
 */
public class ScrollRelativeLayout extends RelativeLayout {
    private Scroller mScroller;

    public ScrollRelativeLayout(Context context) {
        super(context);
    }

    public ScrollRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Scroller getmScroller() {
        return mScroller;
    }

    public void setmScroller(Scroller mScroller) {
        this.mScroller = mScroller;
    }

    @Override
    public void computeScroll() {
        if (mScroller != null && mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    /**
     * Start scrolling by providing a starting point, the distance to travel,
     * and the duration of the scroll.
     *
     * @param nextX    Ending horizontal scroll offset in pixels. Positive
     *                 numbers will scroll the content to the left.
     * @param nextY    Ending vertical scroll offset in pixels. Positive numbers
     *                 will scroll the content up.
     * @param duration Duration of the scroll in milliseconds.
     */
    public void startScroll(int nextX, int nextY, int duration) {
        if (mScroller != null) {
            mScroller.forceFinished(true);
            mScroller.startScroll(0, getScrollY(),0, -nextY -
                    getScrollY(), duration);
            this.postInvalidate();
        }
    }

}
