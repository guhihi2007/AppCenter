package cn.lt.android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import cn.lt.android.util.StringNumberUtil;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;

/**
 * 改装后适用appCenter
 */
public class LazyPagerSlidingTabStrip extends HorizontalScrollView {


    // @formatter:off
    private static final int[] ATTRS = new int[]{
            android.R.attr.textSize,
            android.R.attr.textColor,
            R.attr.theme_type
    };
    private final PageListener pageListener = new PageListener();
    // @formatter:on
    public OnPageChangeListener delegatePageListener;
    private int themeType = 0;
    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;
    private LinearLayout tabsContainer;
    private LazyViewPager pager;


    private int tabCount;

    private int currentPosition = 0;
    private float currentPositionOffset = 0f;
    private int scrollOffset = 52;
//    private int scrollOffset = 12;

    private Paint rectPaint;


    private boolean shouldExpand = false;
    private boolean textAllCaps = true;

    private int tabPadding = 24;

    private int tabTextSize = 12;
    private ColorStateList tabTextColor;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.BOLD;

    private int lastScrollX = 0;

    private int tabBackgroundResId = R.drawable.sc_btn_default;

    private Locale locale;
    private TextView mText;

    public LazyPagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public LazyPagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ResourceType")
    public LazyPagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);
        //获得窗体宽


        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams
                .MATCH_PARENT));
        addView(tabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        tabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tabPadding, dm);
        tabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tabTextSize, dm);


        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        tabTextSize = a.getDimensionPixelSize(0, tabTextSize);
        tabTextColor = a.getColorStateList(1);
        themeType = a.getInt(2, 0);
        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

        tabPadding = a.getDimensionPixelSize(R.styleable
                .PagerSlidingTabStrip_pstsTabPaddingLeftRight, tabPadding);
        tabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground,
                tabBackgroundResId);
        shouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand,
                shouldExpand);
        textAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, textAllCaps);

        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);


        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (locale == null) {
            locale = getResources().getConfiguration().locale;
        }
    }

    public void setViewPager(LazyViewPager pager) {
        this.pager = pager;

        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        pager.setOnPageChangeListener(pageListener);

        notifyDataSetChanged();
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {

        tabsContainer.removeAllViews();

        tabCount = pager.getAdapter().getCount();

        for (int i = 0; i < tabCount; i++) {
            addTextTab(i, pager.getAdapter().getPageTitle(i).toString());

        }


        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                currentPosition = pager.getCurrentItem();
                scrollToChild(currentPosition, -20);
            }
        });

        setupTabsSelected(0);

    }

    public void  notifyDataSetChangedRestorePosition(int position){
        notifyDataSetChanged();
        setupTabsSelected(position);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getTabcount() {
        return tabCount;
    }

    private void addTextTab(final int position, String title) {
        View tabView = null;
        if (themeType == 1) {
            tabView = LayoutInflater.from(getContext()).inflate(R.layout.view_tab_strip_rank, null);
        } else {
            tabView = LayoutInflater.from(getContext()).inflate(R.layout.view_tab_strip, null);
            //mText =(TextView) tabView.findViewById(R.id.tab_text);
        }
        TextView tab = (TextView) tabView.findViewById(R.id.tab_text);
        tab.setText(title);
        addTab(position, tabView);
    }

    private void addTab(final int position, View tab) {
        //不管选中与否，都得显示数量
        tab.setFocusable(true);
        tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(position);
            }
        });

        tab.setPadding(tabPadding, 0, tabPadding, 0);
        expandedTabLayoutParams.gravity = Gravity.CENTER;
        defaultTabLayoutParams.gravity = Gravity.CENTER;
        tabsContainer.addView(tab, position, shouldExpand ? expandedTabLayoutParams :
                defaultTabLayoutParams);
//        showAppsCount();
    }

//    private void initViewForAppCount(View view, String count) {
//        ImageView imageView = (ImageView) view.findViewById(R.id.app_count);
//        TextView textView = (TextView) view.findViewById(R.id.app_count_tv);
//        imageView.setVisibility(View.VISIBLE);
//        textView.setVisibility(View.VISIBLE);
//        if (!TextUtils.isEmpty(count)) {
//            textView.setText(count);
//        }
//    }

    private void scrollToChild(int position, int offset) {

        if (tabCount == 0 ||tabCount<=4) {
            return;
        }

        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }

    }

    private void setupTabsSelected(int position) {
        for (int i = 0; i < tabsContainer.getChildCount(); i++) {
            tabsContainer.getChildAt(i).setSelected(false);
            View lineView = tabsContainer.getChildAt(i).findViewById(R.id.tab_line);
            TextView textView = (TextView) tabsContainer.getChildAt(i).findViewById(R.id.tab_text);
            if (themeType == 1) {
                lineView.setBackgroundColor(Color.TRANSPARENT);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            } else {
                lineView.setBackgroundResource(R.drawable.rectangle_grey_corner);
                lineView.getLayoutParams().width = ScreenUtils.dpToPxInt(getContext(), 8);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            }
        }
        View currentTab = tabsContainer.getChildAt(position);
        View lineView = currentTab.findViewById(R.id.tab_line);
        if (themeType == 1) {
            lineView.setBackgroundResource(R.mipmap.icon_cursor);
            lineView.getLayoutParams().width = ScreenUtils.dpToPxInt(getContext(), 16);
            lineView.getLayoutParams().height = ScreenUtils.dpToPxInt(getContext(), 8);
        } else {
            lineView.setBackgroundResource(R.drawable.rectangle_white_corner);
            lineView.getLayoutParams().width = ScreenUtils.dpToPxInt(getContext(), 30);
        }
        TextView textView = (TextView) currentTab.findViewById(R.id.tab_text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        currentTab.setSelected(true);
        if(themeType == 0){
            showAppsCount();
        }
    }

    private void showAppsCount(){
        for (int i = 0; i < tabsContainer.getChildCount(); i++) {
            TextView textView = (TextView) tabsContainer.getChildAt(i).findViewById(R.id.tab_text);
            ImageView imageView = (ImageView) tabsContainer.getChildAt(i).findViewById(R.id.app_count);
            TextView countTv = (TextView) tabsContainer.getChildAt(i).findViewById(R.id.app_count_tv);
            String currentTabStr =  pager.getAdapter().getPageTitle(i).toString();
            textView.setText(StringNumberUtil.splitNotNumber(currentTabStr));

            if (StringNumberUtil.isLastIndexNumber(currentTabStr)) {
                countTv.setText(StringNumberUtil.getNumbers(currentTabStr));
                imageView.setVisibility(View.VISIBLE);
                countTv.setVisibility(View.VISIBLE);
            }/* else {
                imageView.setVisibility(View.GONE);
                countTv.setVisibility(View.GONE);
            }*/
        }
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public boolean getShouldExpand() {
        return shouldExpand;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
        requestLayout();
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
    }

    public int getTextSize() {
        return tabTextSize;
    }

    public int getTabBackground() {
        return tabBackgroundResId;
    }

    public void setTabBackground(int resId) {
        this.tabBackgroundResId = resId;
    }

    public int getTabPaddingLeftRight() {
        return tabPadding;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }
    }

    private class PageListener implements OnPageChangeListener, LazyViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            currentPosition = position;
            currentPositionOffset = positionOffset;

            scrollToChild(position, (int) (positionOffset * tabsContainer.getChildAt(position)
                    .getWidth()));

            invalidate();
           /* if (position == 1) {
                //通知software变
                if (delegatePageListener != null) {
                    delegatePageListener.onPageSelected(position);
                }
            }*/

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                scrollToChild(pager.getCurrentItem(), -5);
            }

            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            setupTabsSelected(position);
            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
        }

    }

}
