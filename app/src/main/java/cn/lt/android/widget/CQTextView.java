package cn.lt.android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by JohnsonLin on 2017/4/28.
 */

public class CQTextView extends View {

    private Layout mLayout = null;

    private int mWidth;
    private int mHeight;

    public CQTextView(Context context) {
        super(context);
    }

    public CQTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CQTextView(Context context, AttributeSet attrs,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLayout(Layout layout) {
        if (layout == null) {
            return;
        }

        mLayout = layout;
        getLayoutParams().height = mLayout.getHeight();
        if (mLayout.getWidth() != mWidth || mLayout.getHeight() != mHeight) {
            mWidth = mLayout.getWidth();
            mHeight = mLayout.getHeight();
            requestLayout();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLayout != null) {
            mLayout.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLayout != null) {
            setMeasuredDimension(mLayout.getWidth(), mLayout.getHeight());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
