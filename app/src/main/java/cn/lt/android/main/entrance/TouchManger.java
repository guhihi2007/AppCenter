package cn.lt.android.main.entrance;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import cn.lt.android.widget.ScrollRelativeLayout;

/**
 * Created by tiantain on 2015/11/12.
 * 管理个人主页滑动事件
 */
public class TouchManger {
    private Context context;
    /**
     * Y轴上一次停留的坐标值
     */
    private float mPreYCOD;
    /**
     * X轴上一次停留的坐标值
     */
    private float mPreXCOD;
    /**
     * Y轴方向动画移动结束的位置坐标值，
     * <p/>
     * 相对Activity的左上点的坐标；
     */
    private float mGapYCOD;

    private AnimatorSet mAniamtaions;
    private final String TAG = getClass().getSimpleName();

    /**
     * 动画的最大上偏移量；
     * <p/>
     * 达到此偏移量时，搜索条刚好达到可允许的最顶部；
     */
    private float mMaxTopOffset;


    private ScrollRelativeLayout mScrollView;
    /**
     * 需要隐藏移动的view
     */
    private View mLiftView;

    private Scroller mScroller;
    private float mTempMoveOffset;

    public float getTempMoveOffset() {
        return mTempMoveOffset;
    }


    public TouchManger(Context context, ScrollRelativeLayout scrollView, View liftView) {
        this.context = context;
        this.mScroller = new Scroller(context);
        this.mScrollView = scrollView;
        this.mLiftView = liftView;
        scrollView.setmScroller(mScroller);
    }

    public TouchManger init() {
        mMaxTopOffset = mLiftView.getHeight();
        return this;
    }

    private boolean canMovable = true;

    /***
     * 设置是否可以滑动
     *
     * @param canMove
     */
    public void setCanScroll(boolean canMove) {
        this.canMovable = canMove;
    }

    public boolean onEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setPreCOD(ev.getX(), ev.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (headViewPressConfirm(ev)) {
                    return false;
                }
                if (!canMovable) return false;
                float moveTo = calculateMoveSize(ev);
                if (moveTo != Integer.MAX_VALUE && moveTo != Integer.MIN_VALUE) {
                    mTempMoveOffset = moveTo;
                    startDance(moveTo, 0, true);

                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;
    }

    /**
     * 根据触摸移动的距离得到动画移动时需要依照缩放的比例；如果动画效果的移动距离==触摸移动距离，
     * 则会出现listview动画移动的距离刚好抵消掉触摸滚动的效果。所以应该控制好动画滚动的速度小于触摸滑动的速度；
     *
     * @param tempRemoveDistance
     * @return 返回缩放率；
     */
    private float getMoveScale(float tempRemoveDistance) {
        float scale = 1;
        // 根据上拉、下拉的判断对移动的处理做相应缩放；
        if (tempRemoveDistance > 0) {
            scale = 1.2f;
        } else {
            scale = 1.2f;
        }
        return scale;
    }


    public void startDance(float nextY, long duration, boolean all) {
        try {
            if (mAniamtaions != null && mAniamtaions.isRunning()) {
                mAniamtaions.end();
            }
            mAniamtaions = new AnimatorSet();
            mAniamtaions.setDuration(duration);
            mAniamtaions.setInterpolator(new LinearInterpolator());
            ObjectAnimator mLiftViewAnim = ObjectAnimator.ofFloat(mScrollView, "y", nextY);
            mAniamtaions.playTogether(mLiftViewAnim);
            mAniamtaions.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float calculateMoveSize(MotionEvent ev) {
        float tempRawY = ev.getRawY();
        float tempDet = tempRawY - mPreYCOD;
        float scale = getMoveScale(tempDet);
        tempDet = tempDet / scale;
        // 增加判断是往下滚还是往上滚，然后处理mGapYCOD的值；
        if (tempDet < 0) {// 往上滑动；
            if (mGapYCOD == -mMaxTopOffset) {
                mPreYCOD = tempRawY;
                return Integer.MAX_VALUE;
            }
            // 计算缩放后需要移动的距离；
            mGapYCOD = (mGapYCOD += tempDet) > 0 ? 0 : mGapYCOD;
            // 增加判断，如果mGapYCOD的值大于mMaxTopOffset；
            if (Math.abs(mGapYCOD) > mMaxTopOffset) {
                mGapYCOD = -mMaxTopOffset;
            }
        } else {// 往下滑动；
            if (mGapYCOD == 0) {
                mPreYCOD = tempRawY;
                return Integer.MIN_VALUE;
            }
            // 计算缩放后需要移动的距离；
            mGapYCOD = (mGapYCOD += tempDet) > 0 ? 0 : mGapYCOD;
        }
        setPreCOD(ev.getX(0), ev.getRawY());
        return mGapYCOD;
    }

    /**
     * 判断顶部是否被触碰
     *
     * @param ev
     * @return
     */
    private boolean headViewPressConfirm(MotionEvent ev) {
        if (ViewRect.isHorizontalMove(mPreXCOD, mPreYCOD, ev.getX(0), ev.getY(0))) {
            return true;
        }
        return false;
    }

    private void setPreCOD(float x, float y) {
        mPreYCOD = y;
        mPreXCOD = x;
    }


}
