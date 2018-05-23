package cn.lt.pullandloadmore;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by wenchao on 2016/2/16.
 * @des:封装的控件：直接与Activity、Fragment对接。
 */
public class PullToLoadView extends FrameLayout {
    private final static  String TAG = "PullToLoadView";
    private LinearLayoutManager mLayoutManager;
    public LoadingLayout      mLoadingLayout;
    private RefreshAndPullRecyclerView       mRecyclerView;
    private LoadScrollListener mEndlessRecyclerOnScrollListener;
    public PullToLoadView(Context context) {
        this(context, null);
    }
    public PullToLoadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public PullToLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void assignViews() {
        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mRecyclerView = (RefreshAndPullRecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(null);
    }

    private void initialize() {
        View.inflate(getContext(), R.layout.view_loadview, this);
        assignViews();
        mLoadingLayout.showLoading();
    }

    /**
     * 设置布局管理
     * @param layoutManager
     */
    public void setLayoutManager(RecyclerView.LayoutManager layoutManager){
        mRecyclerView.setLayoutManager(layoutManager);
    }

    /**
     * 设置adapter
     * @param adapter
     */
    public void setAdapter(BaseLoadMoreRecyclerAdapter adapter){
        mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if(mLayoutManager == null){
            Log.e(TAG,"please set layoutManager first!");
            return;
        }
        mEndlessRecyclerOnScrollListener = new LoadScrollListener(mLayoutManager,adapter);
        mRecyclerView.setMyAdapter(adapter,mEndlessRecyclerOnScrollListener);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    public void showLoading(){
        mLoadingLayout.showLoading();
    }

    public void showEmpty2(View emptyView){
        mLoadingLayout.setEmptyView(emptyView);
        mLoadingLayout.showEmpty();
    }

    public void showEmpty(){
        mLoadingLayout.showEmpty();
    }


    public void showErrorNoNetwork(){
        mLoadingLayout.showErrorNoNetwork();
    }

    public void showErrorNotGoodNetwork(){
        mLoadingLayout.showErrorNotGoodNetwork();
    }

    public void showContent(){
        mLoadingLayout.showContent();
    }

    public void setOnRetryClickListener(OnClickListener onRetryClickListener){
        mLoadingLayout.setOnRetryClickListener(onRetryClickListener);
    }

    /***
     * 设置是否可以刷新，来控制刷新头布局是否可以显示
     * @param canMovable
     */
    public void setRefreshable(Boolean canMovable) {
        mRecyclerView.setCanMovable(canMovable);
    }

    /**
     * 双击tab时，回顶部 自动刷新
     * 开启下拉刷新的效果
     */
    public void goBackToTopAndRefresh() {
        mLayoutManager.scrollToPosition(0);
        mRecyclerView.goBackToTopAndRefresh();
    }

    /**
     * 提供外界调用，创建普通头视图
     * 在调用之前必须先调用 setAdapter方法
     * @param view
     */
    public void addCustomHeader(View view) {
        mRecyclerView.addCustomHeader(view);
    }

    /**
     * @des 结束下拉刷新,并确定结果
     * @des 外界刷新完必须要调用，否则会导致上拉不能加载
     * @param isSuccessful
     */
    public void setRefreshStopAndConfirmResult(boolean isSuccessful) {
        mRecyclerView.stopRefresh(isSuccessful);
    }
    /***********************************下面是加载相关API*********************************************/

    /**
     * @des 设置是否还有下一页，用来显示是否见底啦
     */
    public void setHasNextPage2ShowFooter() {
        mRecyclerView.setHasNextPage2ShowFooter();
    }

    /**
     * 结束加载,并确定结果
     * @param isLoadMoreSuccess
     */
    public void setLoadStopAndConfirmResult(boolean isLoadMoreSuccess) {
        mRecyclerView.stopLoadMore(isLoadMoreSuccess);
    }

    /**
     * 不显示头尾UI
     */
    public void setNoFooterAndHeader() {
        mRecyclerView.setNoFooterAndHeader();
    }

    /**
     * 不显示头部UI
     */
    public void setNoHeader() {
        mRecyclerView.setNoHeader();
    }

    /**
     * 不显示底部UI
     */
    public void setNoFooter() {
        mRecyclerView.setNoFooter();
    }

/***********************************下面是接口API*********************************************/
    /**
     * 从外界设置 刷新、加载 的接口
     * @param mListener
     */
    public void setOnRefreshAndLoadListener(IrefreshAndLoadMoreListener mListener) {
        mRecyclerView.setOnRefreshAndLoadListener(mListener);
    }
}
