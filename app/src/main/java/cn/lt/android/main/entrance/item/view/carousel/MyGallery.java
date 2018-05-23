package cn.lt.android.main.entrance.item.view.carousel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

import cn.lt.android.util.LogUtils;

@SuppressWarnings("deprecation")
public class MyGallery extends ViewPager {

    private static final int timerAnimation = 1;
    private static final String TAG = "MyGallery";
    private boolean isPressed;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case timerAnimation:
                    if (!isPressed) {
                        int position = getCurrentItem();
                        LogUtils.i("msg", "position:" + position);
                        if (position >= (getChildCount() - 1)) {
                            setCurrentItem(position+1);
                        } else {
                        }
                    }
                    break;

            }
        }

        ;
    };
    private Timer timer;

    public MyGallery(Context context) {
        super(context);
    }

    public MyGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void playSoundEffect(int soundConstant) {
        return;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            LogUtils.d(TAG, "MyGallery are pressed ----------");
            isPressed = true;
            timer.cancel();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            LogUtils.d(TAG, "MyGallery are up ----------");
            isPressed = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            },2000);
        }
        return super.dispatchTouchEvent(ev);
    }


    public void destroy() {
        if (timer != null)
            timer.cancel();
    }

    public void pasue() {
        if (timer != null)
            timer.cancel();
    }

    public void start() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            boolean flag = true;

            public void run() {
                if (flag) {
                    isPressed = false;
                    flag = false;
                }
                mHandler.sendEmptyMessage(timerAnimation);
            }
        }, 3000, 3500);
    }
}