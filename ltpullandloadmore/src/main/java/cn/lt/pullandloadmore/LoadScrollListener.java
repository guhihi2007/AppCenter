package cn.lt.pullandloadmore;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import cn.lt.pullandloadmore.util.LogUtils;
import de.greenrobot.event.EventBus;

/**
 * @author chengyong
 * @time 2016/8/11 16:17
 * @des 滑动监听,触发加载更多
 */
public class LoadScrollListener extends RecyclerView.OnScrollListener{
        private LinearLayoutManager mLinearLayoutManager;
    private IrefreshAndLoadMoreListener mListener;
    public boolean isLoading;
    private boolean isRefreshing;
    private boolean loadable=true;
    public LoadScrollListener(LinearLayoutManager linearLayoutManager, BaseLoadMoreRecyclerAdapter adapter) {
            this.mLinearLayoutManager = linearLayoutManager;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            LogUtils.i("kkk", "到最后可见的position=="+mLinearLayoutManager.findLastVisibleItemPosition());
            LogUtils.i("kkk", "到最后recyclerView的position=="+recyclerView.getAdapter().getItemCount());
            LogUtils.i("kkk", "isLoading=="+isLoading);
            LogUtils.i("kkk", "loadable=="+loadable);
            LogUtils.i("kkk", "isRefreshing=="+isRefreshing);
            if(!loadable)return;
            if(isRefreshing)return;
            //触发加载数据,视图的显示在adapter里控制
            if (mLinearLayoutManager.findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
                if (newState == 0 || newState==2) {
                    if (mListener != null) {
                        if (!isLoading) {
                            isLoading = true;
                            mListener.onLoadMore(mListener);
                            EventBus.getDefault().post("isLoading");
                        }
                    }
                }
            }
            super.onScrollStateChanged(recyclerView, newState);
        }

    public void setLoadListener(IrefreshAndLoadMoreListener mListener) {
        this.mListener = mListener;
    }

    public void setRefreshing(boolean refreshing) {
        isRefreshing = refreshing;
    }

    public void setLoadable(boolean loadable) {
        this.loadable = loadable;
    }

    public void setLoadingStutas(boolean loading) {
        isLoading = loading;
    }
}
