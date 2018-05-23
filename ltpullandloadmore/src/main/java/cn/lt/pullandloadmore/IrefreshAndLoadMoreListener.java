package cn.lt.pullandloadmore;

/**
 * @author chengyong
 * @time 2016/8/11 14:20
 * @des 刷新加载机制的 刷新、上拉接口
 */
public interface IrefreshAndLoadMoreListener {

    void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView);

    void onLoadMore(IrefreshAndLoadMoreListener mListener);
}
