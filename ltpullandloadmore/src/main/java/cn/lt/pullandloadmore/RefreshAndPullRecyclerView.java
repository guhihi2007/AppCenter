package cn.lt.pullandloadmore;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.lt.pullandloadmore.util.LogUtils;


/**
 * @author chengyong
 * @time 2016/8/11 11:20
 * @des 刷新加载机制的 自定义控件
 */

public class RefreshAndPullRecyclerView extends RecyclerView {
    public static final String TAG = RefreshAndPullRecyclerView.class.getSimpleName();
    // 下拉刷新
    public static final int STATE_PULL_REFRESH = 0;
    // 松开刷新
    public static final int STATE_RELEASE_REFRESH = 1;
    // 刷新中
    public static final int STATE_REFRESHING = 2;
    // 刷新成功
    public static final int STATE_REFRESH_SUCCESS = 3;
    // 刷新失败
    public static final int STATE_REFRESH_FAILURE = 4;
    //默认是下拉刷新状态
    public int mCurState = STATE_PULL_REFRESH;
    private BaseLoadMoreRecyclerAdapter mAdapter;
    public ImageView mRefreshRecyclerHeaderViewArrowToggle;
    public ProgressBar mRefreshListViewHeaderViewArrowLoading;
    public ImageView mRefreshListViewHeaderViewFailure;
    public TextView mRefreshListViewHeaderViewState;
    public RelativeLayout mRefreshHeaderView;   //下拉刷新的容器mImvContainer
    public RelativeLayout mImvContainer;
    public View mHeaderView; //刷新头布局xml对象
    public View mFooterView; //加载底布局xml对象
    private int mInitPaddingTop;
    private float mDownX;
    private float mDownY;
    public int mHeaderViewMeasuredHeight;
    //默认是隐藏
    private boolean isRefreshHeaderViewHide = true;
    private IrefreshAndLoadMoreListener mListener;
    private TextView mLoadFooterTextState;
    private FrameLayout mCustomHeader;
    private LoadScrollListener mScrollLoadListener;
    private ProgressBar mLoadFooterIvLoading;
    private ImageView mLoadFooterIvFailure;
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isLoading;
    private boolean canMovable = true;

    public RefreshAndPullRecyclerView(Context context) {
        super(context);
    }

    public RefreshAndPullRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHeaderView = View.inflate(context, R.layout.inflate_refresh_recyclerview_headerview, null);//头
        mFooterView = View.inflate(context, R.layout.inflate_refresh_recyclerview_footerview, null);//尾
    }

    public RefreshAndPullRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMyAdapter(BaseLoadMoreRecyclerAdapter adapter, LoadScrollListener mScrollLoadListener) {
        this.mScrollLoadListener = mScrollLoadListener;
        mAdapter = adapter;
        addHeaderView();
        addFooterView();
    }

    /**
     * 提供外界调用，创建普通头视图
     * 在调用之前必须先调用 setAdapter方法
     *
     * @param view
     */
    public void addCustomHeader(View view) {
        if (mCustomHeader == null) {
            Toast.makeText(getContext(), "请先调用setAdapter方法", Toast.LENGTH_SHORT);
        }
        mCustomHeader.addView(view);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 添加刷新 头布局
     */
    public void addHeaderView() {
        mAdapter.addHeaderView(mHeaderView);
        mRefreshHeaderView = (RelativeLayout) mHeaderView.findViewById(R.id.refreshlishview_headerview);
        mImvContainer = (RelativeLayout) mHeaderView.findViewById(R.id.icon_container_ivs);
        mRefreshRecyclerHeaderViewArrowToggle = (ImageView) mHeaderView.findViewById(R.id.recyclerview_headerview_arrow_down);
        mRefreshListViewHeaderViewArrowLoading = (ProgressBar) mHeaderView.findViewById(R.id.recyclerview_headerview_loading);
        mRefreshListViewHeaderViewFailure = (ImageView) mHeaderView.findViewById(R.id.recyclerview_headerview_load_failure);
        mRefreshListViewHeaderViewState = (TextView) mHeaderView.findViewById(R.id.header_tv_content_state);
        mCustomHeader = (FrameLayout) mHeaderView.findViewById(R.id.recyclerview_headerview_custom);

        //预先隐藏 下拉刷新的头布局 -->mRefreshHeaderView
        mRefreshHeaderView.measure(0, 0);
        mHeaderViewMeasuredHeight = mRefreshHeaderView.getMeasuredHeight();
        mInitPaddingTop = -mHeaderViewMeasuredHeight;
        LogUtils.i("aaa", "顶部高度：" + mInitPaddingTop);
        mHeaderView.setPadding(0, mInitPaddingTop, 0, 0);
    }

    /**
     * 添加脚布局
     */
    public void addFooterView() {
        mAdapter.addFooterView(mFooterView);
        mLoadFooterTextState = (TextView) mFooterView.findViewById(R.id.tv_content);
        mLoadFooterIvLoading = (ProgressBar) mFooterView.findViewById(R.id.recyclerview_footerview_loading);
        mLoadFooterIvFailure = (ImageView) mFooterView.findViewById(R.id.recyclerview_footerview_load_failure);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                LogUtils.i("abc", "down走了：");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurState == STATE_REFRESHING) return true;
                if (!canMovable) return super.onTouchEvent(ev);
                float moveX = ev.getRawX();
                float moveY = ev.getRawY();
                if (mDownX == 0 && mDownY == 0) {  // down的分支被抢，会导致不能来到action_down
                    mDownX = moveX;
                    mDownY = moveY;
                }
                int[] refreshHeaderViewLocationArr = new int[2];
                mRefreshHeaderView.getLocationOnScreen(refreshHeaderViewLocationArr);
                int refreshHeaderViewTopY = refreshHeaderViewLocationArr[1];
                int refreshHeaderViewBottomY = refreshHeaderViewTopY + mHeaderViewMeasuredHeight;   //值1
                int[] listViewLocationArr = new int[2];
                this.getLocationOnScreen(listViewLocationArr);
                int refreshListViewTopY = listViewLocationArr[1];     //值2

                /****************************************基本条件************************************/
                int diffY = (int) (moveY - mDownY + .5f);
                if (refreshHeaderViewBottomY >= refreshListViewTopY && diffY > 0) {
                    if (isRefreshHeaderViewHide) {
                        mDownY = moveY;
                        isRefreshHeaderViewHide = false;  //防止跳
                    }
                    int diffY2 = (int) (moveY - mDownY + .5f);
                    int paddingTop = mInitPaddingTop + (diffY2 >> 2);     //让你拉的艰难点，为何就会先跳一下。
                    mHeaderView.setPadding(0, paddingTop, 0, 0);
                    LogUtils.i("aaa", "顶部高度：" + mInitPaddingTop);
                    LogUtils.i("aaa", "移动的距离：" + diffY2);
                    LogUtils.i("aaa", "paddingTop值：" + paddingTop);
                    if (paddingTop < 0 && mCurState != STATE_PULL_REFRESH) {
                        mCurState = STATE_PULL_REFRESH;
                        //状态改变,刷新ui
                        LogUtils.i("aaa", "切换为-->下拉刷新");
                        refreshHeaderUiByState();
                    } else if (paddingTop >= 0 && mCurState != STATE_RELEASE_REFRESH) {
                        mCurState = STATE_RELEASE_REFRESH;
                        //状态改变,刷新ui
                        LogUtils.i("aaa", "切换为-->松开刷新");
                        refreshHeaderUiByState();
                    }
                    return true;
                } else {
                    LogUtils.i("aaa", "recyclerView");
                    return super.onTouchEvent(ev);
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDownX = 0;
                mDownY = 0;
                isRefreshHeaderViewHide = true;//又默认隐藏
                if (mCurState == STATE_RELEASE_REFRESH) {
                    mCurState = STATE_REFRESHING;//切换为正在刷新
                    LogUtils.i("aaa", "切换为-->正在刷新");
                    //状态发生改变,更新ui
                    refreshHeaderUiByState();
                    mHeaderView.setPadding(0, 0, 0, 0);
//                      changeRefreshHeaderViewPaddingTopAnimation(mHeaderView.getPaddingTop(), 0);
                    if (mListener != null && !isLoading) {   //刷新、加载只能执行一个
                        mListener.onRefresh(this);
                        LogUtils.i("aaa", "大哥，触发刷新啦");    //？停止刷新在 触发刷新的前面 ？ --异步
                        mScrollLoadListener.setRefreshing(true);
                    }
                } else {
                    mCurState = STATE_PULL_REFRESH;//切换为下拉刷新
                    LogUtils.i("aaa", "切换为-->下拉刷新");
                    //状态发生改变,更新ui
                    refreshHeaderUiByState();
                    mHeaderView.setPadding(0, mInitPaddingTop, 0, 0);
//                    changeRefreshHeaderViewPaddingTopAnimation(mHeaderView.getPaddingTop(), mInitPaddingTop);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 根据最新的状态,刷新 下拉刷新头布局的ui
     */
    private void refreshHeaderUiByState() {
        switch (mCurState) {
            case STATE_PULL_REFRESH://切换为-->下拉刷新
                mRefreshListViewHeaderViewState.setText("下拉刷新");
                mRefreshListViewHeaderViewArrowLoading.setVisibility(View.GONE);
                mRefreshListViewHeaderViewFailure.setVisibility(View.GONE);
                mRefreshRecyclerHeaderViewArrowToggle.setVisibility(View.VISIBLE);
                changeArrowToggleByAnimation(-180, 0);
                break;
            case STATE_RELEASE_REFRESH://切换为-->松开刷新
                mRefreshListViewHeaderViewState.setText("松开刷新");
                mRefreshListViewHeaderViewArrowLoading.setVisibility(View.GONE);
                mRefreshListViewHeaderViewFailure.setVisibility(View.GONE);
                mRefreshRecyclerHeaderViewArrowToggle.setVisibility(View.VISIBLE);
                changeArrowToggleByAnimation(0, 180);
                break;
            case STATE_REFRESHING://切换为-->正在刷新
                mRefreshListViewHeaderViewState.setText("正在刷新");
                //			清除箭头的动画
                mRefreshRecyclerHeaderViewArrowToggle.clearAnimation();
                mRefreshRecyclerHeaderViewArrowToggle.setVisibility(View.INVISIBLE);
                mRefreshListViewHeaderViewArrowLoading.setVisibility(View.VISIBLE);
                mRefreshListViewHeaderViewFailure.setVisibility(View.GONE);
                break;
            case STATE_REFRESH_SUCCESS://切换为-->刷新成功
                mRefreshListViewHeaderViewState.setText("刷新完成");
                mRefreshRecyclerHeaderViewArrowToggle.setVisibility(View.INVISIBLE);
                mRefreshListViewHeaderViewArrowLoading.setVisibility(View.VISIBLE);
                mRefreshListViewHeaderViewFailure.setVisibility(View.GONE);
                break;
            case STATE_REFRESH_FAILURE://切换为-->刷新失败
                mRefreshListViewHeaderViewState.setText("刷新失败");
                mRefreshRecyclerHeaderViewArrowToggle.setVisibility(View.INVISIBLE);
                mRefreshListViewHeaderViewArrowLoading.setVisibility(View.GONE);
                mRefreshListViewHeaderViewFailure.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * 改变状态让箭头旋转
     *
     * @param start
     * @param end
     */
    private void changeArrowToggleByAnimation(int start, int end) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mRefreshRecyclerHeaderViewArrowToggle, "rotation", start, end);
        animator.setDuration(400);
        animator.start();
    }

    /**
     * @param start
     * @param end
     * @des 以动画的方式修改 --- 刷新头布局的高度 ----属性动画渐变改变
     */
    public void changeRefreshHeaderViewPaddingTopAnimation(int start, int end) {
        LogUtils.i(TAG, "start:" + start + " end:" + end);
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(400);
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                int paddingTop = (Integer) value.getAnimatedValue();
                mHeaderView.setPadding(0, paddingTop, 0, 0);
            }
        });
    }

    /**
     * 从外界设置刷新、加载的接口
     *
     * @param mListener
     */
    public void setOnRefreshAndLoadListener(IrefreshAndLoadMoreListener mListener) {
        this.mListener = mListener;
        mScrollLoadListener.setLoadListener(mListener);
    }

    /**
     * 双击tab时，回顶部 自动刷新
     * 开启下拉刷新的效果
     */
    public void goBackToTopAndRefresh() {
        mCurState = STATE_PULL_REFRESH;  //下
        refreshHeaderUiByState();
        changeRefreshHeaderViewPaddingTopAnimation(mHeaderView.getPaddingTop(), 0);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCurState = STATE_REFRESHING;  //重置状态
                refreshHeaderUiByState();
                if (mListener != null) {
                    mHeaderView.clearAnimation();   //先清除，否则会异步。
                    mListener.onRefresh(RefreshAndPullRecyclerView.this);
                }
            }
        }, 400);
    }

    /**
     * 结束下拉刷新的效果
     */
    public void stopRefresh(boolean isSuccessful) {
        mHeaderView.clearAnimation();   //先清除，否则会异步。
        LogUtils.i("aaa", "停止刷新啦");
        if (isSuccessful) {
            mScrollLoadListener.setLoadable(true);   //可加载
            mCurState = STATE_REFRESH_SUCCESS;
            refreshHeaderUiByState();
            mHeaderView.setPadding(0, 0, 0, 0);
//            changeRefreshHeaderViewPaddingTopAnimation(mHeaderView.getPaddingTop(), 0);
        } else {
            mCurState = STATE_REFRESH_FAILURE;
            LogUtils.i("aaa", "刷新失败啦");
            refreshHeaderUiByState();
            mHeaderView.setPadding(0, 0, 0, 0);
//            changeRefreshHeaderViewPaddingTopAnimation(mHeaderView.getPaddingTop(), 0);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LogUtils.i("aaa", "延迟500ms啦，藏起来");
                mHeaderView.setPadding(0, mInitPaddingTop, 0, 0);
                LogUtils.i("aaa", "juli:==>" + mInitPaddingTop);
                mScrollLoadListener.setRefreshing(false);  //防止异步，与 触发时。
                mCurState = STATE_PULL_REFRESH;
                refreshHeaderUiByState();
            }
        }, 500);


    }

    /**
     * 加载更多完成
     *
     * @param isLoadMoreSuccess
     */
    public void stopLoadMore(boolean isLoadMoreSuccess) {
        if (isLoadMoreSuccess) {
            mLoadFooterIvFailure.setVisibility(View.GONE);
            mLoadFooterIvLoading.setVisibility(View.VISIBLE);
            mLoadFooterTextState.setText("加载中");
        } else {
            mLoadFooterIvLoading.setVisibility(View.GONE);
            mLoadFooterTextState.setText("加载失败,再试一次");           //在外界每加载失败都要 page-1，否则就会加载错误页面的数据
            mLoadFooterIvFailure.setVisibility(View.VISIBLE);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isLoading = false;
                mScrollLoadListener.setLoadingStutas(false);
            }
        }, 200);  //防止异步
    }

    /**
     * 是否有加载更多==显示底部，并不允许再次触发加载
     */
    public void setHasNextPage2ShowFooter() {
        mAdapter.setFooterHint();
        mScrollLoadListener.setLoadable(false);
        mLoadFooterIvLoading.setVisibility(View.GONE);
        mLoadFooterTextState.setText("呀！到底了，好指力！");
    }

    /**
     * 不显示头部UI
     */
    public void setNoFooterAndHeader() {
        mAdapter.setNoFooterAndHeader();
    }

    /**
     * 不显示头部UI
     */
    public void setNoHeader() {
        mAdapter.setNoHeader();
    }

    /**
     * 不显示底部UI
     */
    public void setNoFooter() {
        mAdapter.setNoFooter();
    }

    /***
     * 设置是否可以下拉
     *
     * @param canMovable
     */
    public void setCanMovable(Boolean canMovable) {
        this.canMovable = canMovable;
    }

    /**
     * 是否在加载中
     *
     * @param event
     */
    public void onEventMainThread(String event) {
        if ("isLoading".equals(event) && !isLoading) {
            isLoading = true;
            LogUtils.d("RefreshAndLoad", "在加载中吗? 变成：==>" + isLoading);
        }
    }


}
