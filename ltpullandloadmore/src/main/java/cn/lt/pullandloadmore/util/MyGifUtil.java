package cn.lt.pullandloadmore.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.util.DebugUtils;
import android.view.View;
import android.widget.ImageView;

import cn.lt.pullandloadmore.R;

/**
 * @author chengyong
 * @time 2016/8/22 14:45
 * @des 加载gif图片
 */
public class MyGifUtil extends View {
    private Movie mMovie;
    private long mMovieStart;
    private int mWidth, mHeight;
    private int mViewWidht, mViewHeight;
//    private OnPlayListener onPlayListener;
    private int measuredHeight;
    private int measuredWidth;

    public MyGifUtil(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public MyGifUtil(Context context) {
        super(context);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        mMovie = Movie.decodeStream(getResources().openRawResource(
                R.raw.cat_loading_transparent));
    }

    public MyGifUtil(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isHardwareAccelerated()){
            this.setLayerType(View.LAYER_TYPE_SOFTWARE,null);   //启用硬件加速
        }
        mMovie = Movie.decodeStream(getResources().openRawResource( R.raw.cat_loading_transparent));
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Loading_cat);
        measuredWidth = typedArray.getInt(R.styleable.Loading_cat_loading_width, 0);
        measuredHeight = typedArray.getInt(R.styleable.Loading_cat_loading_height, 0);
        typedArray.recycle();
        LogUtils.d("MyGifUtil","测出来的控件的宽：=>"+measuredHeight);
        LogUtils.d("MyGifUtil","测出来的控件的高：=>"+measuredHeight);

        //gif图片宽度，高度
        mViewHeight = mMovie.height();
        mViewWidht = mMovie.width();
        LogUtils.d("MyGifUtil","gif图片宽度：=>"+mViewWidht);
        LogUtils.d("MyGifUtil","gif图片高度：=>"+mViewHeight);
    }

//    public OnPlayListener getOnPlayListener() {
//        return onPlayListener;
//    }
//
//    public void setOnPlayListener(OnPlayListener onPlayListener) {
//        this.onPlayListener = onPlayListener;
//    }

    boolean isDraw = true;

    public void onDraw(Canvas canvas) {
        long now = android.os.SystemClock.uptimeMillis();
        if (isDraw) {
            if (mMovieStart == 0) { // first time
                mMovieStart = now;
            }
            if (mMovie != null) {

                int dur = mMovie.duration();
                if (dur == 0) {
                    dur = 5000;
                }

//                //计算gif播放时间，gif播放完成，关闭界面---允许你转两圈
//                if (now - mMovieStart >= 2*dur) {
//                    isDraw = false;
////                    if (onPlayListener != null) {
////                        onPlayListener.onFinished();
////                    }
//                }
              //目前设置成无穷转
                int relTime = (int) ((now - mMovieStart) % dur);

                mMovie.setTime(relTime);
                //根据 控件大小 计算缩放比例
                float saclex = (float) DensityUtil.dip2px(getContext(),measuredHeight) / (float) mViewWidht;
                float sacley = (float) DensityUtil.dip2px(getContext(),measuredWidth)  / (float) mViewHeight;
                float sameRate = saclex > sacley ? saclex : sacley;   //按最大的缩放适配
                canvas.scale(sameRate, sameRate);
                mMovie.draw(canvas, 0, 0);
                invalidate();
            }
        }
    }
//    //gif关闭接口
//    public static interface OnPlayListener {
//        public void onFinished();
//    }
}