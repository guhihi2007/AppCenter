package cn.lt.android.main.recommend;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.AdReporter;
import cn.lt.android.ads.AdService;
import cn.lt.android.ads.AdsTypeParams;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.listener.refresh.AdRefreshListener;
import cn.lt.android.ads.request.AdRequestBean;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.event.TabClickEvent;
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.entrance.AppsShell;
import cn.lt.android.main.entrance.BigPositionGeter;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cn.lt.android.ads.AdService.RECOMMEND_AD;


/***
 * 推荐
 */
@SuppressWarnings("ALL")
public class RecommendPortalFragment extends BaseFragment implements IrefreshAndLoadMoreListener {

    private View mRootView;
    private PullToLoadView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManger;
    private List<ItemData<BaseBean>> mDatas = new ArrayList<>();
    private EntranceAdapter mAdapter;
    private List<AppEntity> mInstallFailureTaskList;
    private boolean isRefresh;
    private int mCurrentPage = GlobalConfig.FIRST_PAGE;
    private View mContentView;
    private BaseBeanList allDatas = new BaseBeanList();
    private int mLastpage;

    /**
     * 用于计算p1的值
     */
    private BigPositionGeter bigPositionGeter;
    private long mayDataRequestTime;

    public static RecommendPortalFragment newInstance(String tab) {
        RecommendPortalFragment fragment = new RecommendPortalFragment();
        Bundle args = new Bundle();
        args.putString(BaseFragment.FRAGMENT_TAB, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_RECOMMEND);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (bigPositionGeter == null) {
            bigPositionGeter = new BigPositionGeter();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopBannerTimer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mAdapter != null) {
            mAdapter.stopBannerTimer();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (!((MainActivity) getActivity()).imageJumpFormLoading) {
                statEvent();
            }
            LogUtils.i("asuiu312", "精选页数据上报执行了");

            ((MainActivity) getActivity()).imageJumpFormLoading = false;//为了过滤多上报推荐页的数据。
            if (mAdapter != null) {
                mAdapter.startBannerTimer();
            }
            LogUtils.i("iii", "底部推荐onHiddenChanged走了");
            ((MainActivity) mContext).mHeadView.setPageName(getPageAlias());

            // 刷新广告数据
            refreshAdData();
        }
    }

    /**
     * 刷新广告数据
     */
    private void refreshAdData() {
        if (TextUtils.isEmpty(AdsTypeParams.RecommendAdType)) {
            return;
        }

        // 以下是用于刷新广告数据（超过30秒就刷新）
        if (AdService.getInstance().isAdDirty(RECOMMEND_AD)) {
            if (allDatas.size() == 0) return;
            LogUtils.i(LogTAG.AdTAG, "《超过30秒》，推荐页开始重新请求广告");
            AdService.getInstance().setAdRefreshListener(RECOMMEND_AD, new AdRefreshListener() {

                @Override
                public void refresh() {
                    if (allDatas != null && allDatas.size() != 0) {
                        AdService.getInstance().handleAd(RECOMMEND_AD, allDatas, null);
                        // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                        new DownloadAdAppReplacer().replaceByBaseBeanList(allDatas);

                        mDatas = DataShell.wrapData(allDatas, 0);
                        mAdapter.setList(mDatas);

                        // 上传已展示广告包名集合数据
//                        AdService.getInstance().posEexistAdList(RECOMMEND_AD);

                        AdReporter.reportShow(RECOMMEND_AD, mCurrentPage);


                        // 推荐页面数据调用玩咖曝光上报(这里是会重复刷新两次，目前毛办法吖~)
                        WanKaManager.exposureApps(allDatas, null, "推荐页曝光");

                    }
                }
            });

            // 相关的list要清除
            clearList();

            AdService.getInstance().fetchAd(AdsTypeParams.RecommendAdType, RECOMMEND_AD, createAdRequestBean(AdsTypeParams.RecommendAdType));
        } else {
            LogUtils.i(LogTAG.AdTAG, "《没有超过30秒》，推荐页不用重新请求广告");

            // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
            new DownloadAdAppReplacer().replaceByBaseBeanList(allDatas);
            mDatas = DataShell.wrapData(allDatas, 0);
            mAdapter.setList(mDatas);
        }
    }

    private int getAdCount() {
        return mCurrentPage > 5 ? 50 : mCurrentPage * 15;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_recommend_entry, container, false);
            mLayoutManger = new LinearLayoutManager(mContext);
            mAdapter = new EntranceAdapter(getContext(), getPageAlias(), "");
            init();
            requestData(true, false);
        }
        return mRootView;
    }

    private void init() {
        mRecyclerView = (PullToLoadView) mRootView.findViewById(R.id.rcv_recommend);
        mRecyclerView.setLayoutManager(mLayoutManger);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnRefreshAndLoadListener(this);
        mRecyclerView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = 1;
                mRecyclerView.showLoading();
                requestData(false, true);
            }
        });
        onHiddenChanged(false);
    }

    /**
     * 请求网络
     *
     * @param isFromOnCreate true:第一次请求数据，不显示头布局。  false：其余情况下请求网络
     * @param isFromRetry    是否是重试请求
     */
    private void requestData(final boolean isFromOnCreate, final boolean isFromRetry) {
        if (!NetWorkUtils.isConnected(getContext())) {
            setPageWhileFailure();
            if (!isFromOnCreate && !isFromRetry) {
                mRecyclerView.setRefreshStopAndConfirmResult(false);
                mRecyclerView.setLoadStopAndConfirmResult(false);
            } else {
                isRefresh = refresh(0, isRefresh);
                ShowRefreshLoadingUtils.showLoadingForNoNet(mRecyclerView);  //只有第一次或者重试时才显示无网络图
            }
            return;
        }

        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {

            // 相关的List要清除
            clearList();
            bigPositionGeter = new BigPositionGeter();
        }

        // 发送广告请求
        AdService.getInstance().fetchAd(AdsTypeParams.RecommendAdType, RECOMMEND_AD, createAdRequestBean(AdsTypeParams.RecommendAdType));

        mayDataRequestTime = System.currentTimeMillis();
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<BaseBean>>() {
            @Override
            public void onResponse(Call<List<BaseBean>> call, final Response<List<BaseBean>> response) {
                try {
                    if (!isFromOnCreate && !isFromRetry) {
                        mRecyclerView.setRefreshStopAndConfirmResult(true);
                        mRecyclerView.setLoadStopAndConfirmResult(true);
                    }
                    final List<BaseBean> datas = response.body();
                    if (datas != null && datas.size() > 0) {//有数据；
                        LogUtils.i(LogTAG.AdTAG, "推荐页数据请求时间 = " + (System.currentTimeMillis() - mayDataRequestTime));
                        mDatas.clear();
                        AppsShell.RefactorDataStructure(datas, bigPositionGeter);

                        AppBeanTransfer.transferBaseData(datas);
                        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AdService.getInstance().handleAd(RECOMMEND_AD, datas, allDatas);
                                setDatas(response, datas, isFromOnCreate);
                            }
                        }, getWaitTime());

                    } else {
                        showRequestDataError(isFromOnCreate, isFromRetry);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showRequestDataError(isFromOnCreate, isFromRetry);
                }
            }

            @Override
            public void onFailure(Call<List<BaseBean>> call, Throwable t) {
                setPageWhileFailure();
                if (!isFromOnCreate && !isFromRetry) {
                    mRecyclerView.setRefreshStopAndConfirmResult(false);
                    mRecyclerView.setLoadStopAndConfirmResult(false);
                } else {
                    isRefresh = refresh(0, isRefresh);
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mRecyclerView);
                }
            }
        }).bulid().requestRecommend(mCurrentPage, 10);
    }

    /**
     * 加载失败时调用，否则上拉加载的数据页码会错位
     */
    private void setPageWhileFailure() {
        if (mCurrentPage > GlobalConfig.FIRST_PAGE) {
            mCurrentPage--;
        }
    }

    private void setDatas(final Response<List<BaseBean>> response, final List<BaseBean> requestDataList, boolean isFromOnCreate) {
        mRecyclerView.showContent();

        // 推荐页面数据调用玩咖曝光上报
        WanKaManager.exposureApps(requestDataList, null, "推荐页曝光");


        // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
        new DownloadAdAppReplacer().replaceByBaseBeanList(requestDataList);
        isRefresh = refresh(1, isRefresh);
        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
            allDatas.clear();
//            mLayoutManger.scrollToPosition(0);   //只要是第一頁都回頂部
        }
        allDatas.addAll(requestDataList);
//        AppsShell2.RefactorDataStructure(allDatas, bigPositionGeter);

        mDatas = DataShell.wrapData(allDatas, 0);

        //根据页码添加数据mCurrentPage
        mAdapter.setList(mDatas);

        //  第一次请求，最后一页就是固定的
        mLastpage = Constant.getLastPage(response.headers());

        AdReporter.reportShow(RECOMMEND_AD, mCurrentPage);
    }

    /**
     * 判断是否还有下一页
     *
     * @param lastPage
     * @return
     */
    private boolean hasNextPage(int lastPage) {
        if (mCurrentPage <= lastPage) {
            return true;
        }
        return false;
    }

    private void showRequestDataError(boolean isFromOnCreate, boolean isFromRetry) {
        setPageWhileFailure();
        //刷新和加载更多停止
        if (!isFromOnCreate && !isFromRetry) {
            mRecyclerView.setRefreshStopAndConfirmResult(false);
            mRecyclerView.setLoadStopAndConfirmResult(false);
        } else {
            isRefresh = refresh(0, isRefresh);  //吐司暂时没有使用
            ShowRefreshLoadingUtils.showLoadingForNotGood(mRecyclerView);
        }
    }


    public void onEventMainThread(String event) {
        if ("refreshPage".equals(event)) {
            if (allDatas.size() > 0) {
                new DownloadAdAppReplacer().replaceByBaseBeanList(allDatas);
                mDatas = DataShell.wrapData(allDatas, 0);
                mAdapter.setList(mDatas);
            }
        }
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        LogUtils.i("aaa", "刷新时页码是：" + mCurrentPage);
        mCurrentPage = 1;
        requestData(false, false);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        mCurrentPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        LogUtils.i("aaa", "mLastpage==>" + mLastpage + "====mCurrentPage==>" + mCurrentPage + "====hasMoreData==>" + hasMoreData);
        if (hasMoreData) {
            requestData(false, false);
        } else {
            mRecyclerView.setHasNextPage2ShowFooter();
        }
    }

    /**
     * 接收tab双击事件
     *
     * @param str
     */
    public void onEventMainThread(TabClickEvent event) {
        if (MainActivity.KEY_PAGE_PORTAL_RECOMMEND.equals(event.tabName)) {
            LogUtils.i("jkl", "收到了：");
            mRecyclerView.goBackToTopAndRefresh();
        }
    }

    /**
     * 创建广告请求所需分页参数的实体
     */
    private AdRequestBean createAdRequestBean(String adMold) {
        AdRequestBean bean = new AdRequestBean();
        switch (adMold) {
            case AdMold.WanDouJia:
                bean.wanDouJia.startNum = 0;
                bean.wanDouJia.adCount = getAdCount();
                break;
            case AdMold.WanKa:
                bean.wanKa.curPage = mCurrentPage;
                bean.wanKa.adCount = 30;
                break;
        }

        return bean;
    }

    /**
     * 判断加载页面前的等待时间
     */
    private long getWaitTime() {

        // 没设置广告
        if (TextUtils.isEmpty(AdsTypeParams.RecommendAdType)) {
            return 0;
        }

        // 设置页面了广告
        return 200;
    }

    /**
     * 清空相关list
     */
    private void clearList() {
        // 请求第一页时，要清空已展示的广告名单
        AdService.getInstance().getExistAdList(RECOMMEND_AD).clear();

        // 清空由于与自有资源重复而被删除的广告列表
        AdService.getInstance().getRemoveAdList(RECOMMEND_AD).clear();

        // 清空备用资源
        AdService.getInstance().getBackupList(RECOMMEND_AD).clear();
    }
}