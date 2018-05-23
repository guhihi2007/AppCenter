package cn.lt.android.main.software;

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
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.entrance.AppsShell;
import cn.lt.android.main.entrance.BigPositionGeter;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.BaseBean;
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

import static cn.lt.android.ads.AdService.GAME_ESSENCE_AD;
import static cn.lt.android.ads.AdService.SOFTWARE_ESSENCE_AD;

/***
 * 精选界面
 */
public class HighlySelectiveFragment extends BaseFragment implements IrefreshAndLoadMoreListener {
    public static final String FRAGMENT_TAB_SOFT = "software_Handpick";
    public static final String FRAGMENT_TAB_GAME = "game_Handpick";
    private View mRootView;
    public PullToLoadView mPullToLoadView;
    private RecyclerView.LayoutManager mLayoutManger;
    private List<ItemData<BaseBean>> mDatas = new ArrayList<>();
    private EntranceAdapter mAdapter;
    private String mFragmentTabName = Constant.PAGE_SOFT_HIGHLYSELECT;
    private NetDataInterfaceDao mDao;
    /**
     * 用于记录从服务器获取数据的最后一个的位置，初次请求时为0；
     */
    private int mlastPagePositionOfNetData = 0;
    private boolean isRefresh;
    private List<BaseBean> allDatas = new ArrayList<>();
    private int mLastpage;
    private boolean isVisible;

    private boolean isSelf = true;

    /**
     * 用于计算p1的值
     */
    private BigPositionGeter bigPositionGeterBySoft;
    private BigPositionGeter bigPositionGeterByGame;
    private long mayDataRequestTime;

    /**
     * 页面类型（广告API用）
     */
    private int pageType;

    public static HighlySelectiveFragment newInstance(String tab) {
        HighlySelectiveFragment fragment = new HighlySelectiveFragment();
        Bundle args = new Bundle();
        args.putString(BaseFragment.FRAGMENT_TAB, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setPageAlias() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        if (bigPositionGeterBySoft == null) {
            bigPositionGeterBySoft = new BigPositionGeter();
        }

        if (bigPositionGeterByGame == null) {
            bigPositionGeterByGame = new BigPositionGeter();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentTabName = getArguments().getString(BaseFragment.FRAGMENT_TAB);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_highly_selective, container, false);
            mLayoutManger = new LinearLayoutManager(getContext());
            mAdapter = new EntranceAdapter(getContext(), getPageAlias(), "");  //这里获得的页面名字不准应该以baseFragment中的页面为准。
            initView();
            requestData(true);
        }

        if (mAdapter != null) {
            mAdapter.startBannerTimer();
        }
//        LogUtils.i("iii", "精选onCreateView走了"+getUserVisibleHint());
        if (getUserVisibleHint()) {
            setPageAndUploadPageEvent();
        }
        return mRootView;
    }

    /**
     * 这个方法才是fragment真正的可见
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            LogUtils.i("iii", "精选setUserVisibleHint走了" + isVisibleToUser);
        if (isVisibleToUser && mRootView != null && (getParentFragment() != null && getParentFragment().getUserVisibleHint())) {
            setPageAndUploadPageEvent();
            if (allDatas.size() == 0) {
                requestData(true);
            } else {
                refreshAdData();
            }

            LogUtils.i("iii", "精选setUserVisibleHint  请求网络了" + isVisibleToUser);
            ((MainActivity) mContext).mHeadView.setPageName(getPageAlias());
        }


    }

    /**
     * 刷新广告数据
     */
    private void refreshAdData() {

        // 以下是用于刷新广告数据（超过30秒就刷新）
        String adsTypeParams = getAdsTypeParams(pageType);
        if (TextUtils.isEmpty(adsTypeParams)) {
            return;
        }

        if (AdService.getInstance().isAdDirty(pageType)) {
            LogUtils.i(LogTAG.AdTAG, "《超过30秒》，" + mFragmentTabName + " 页开始重新请求广告");
            AdService.getInstance().setAdRefreshListener(pageType, new AdRefreshListener() {

                @Override
                public void refresh() {
                    if (allDatas != null && allDatas.size() != 0) {
                        AdService.getInstance().handleAd(pageType, allDatas, null);
                        // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                        new DownloadAdAppReplacer().replaceByBaseBeanList(allDatas);
                        mDatas = DataShell.wrapData(allDatas, mlastPagePositionOfNetData);
                        mAdapter.setList(mDatas);

                        // 上传已展示广告包名集合数据
//                                AdService.getInstance().posEexistAdList(SOFTWARE_ESSENCE_AD);

                        // 推荐页面数据调用玩咖曝光上报(这里是会重复刷新两次，目前毛办法吖~)
                        WanKaManager.exposureApps(allDatas, null, "精选页面曝光");

                        AdReporter.reportShow(pageType, mCurrentPage);
                    }
                }
            });
            clearList(pageType);
            AdService.getInstance().fetchAd(adsTypeParams, pageType, createAdRequestBean(adsTypeParams));
        } else {
            LogUtils.i(LogTAG.AdTAG, "《没有超过30秒》，" + mFragmentTabName + " 页不用重新请求广告");

            // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
            new DownloadAdAppReplacer().replaceByBaseBeanList(allDatas);
            mDatas = DataShell.wrapData(allDatas, 0);
            mAdapter.setList(mDatas);
        }
    }

    /**
     * 设置页面并上报
     */
    private void setPageAndUploadPageEvent() {
        isVisible = true;  //用于重新获得焦点时。
        switch (mFragmentTabName) {
            case FRAGMENT_TAB_GAME:
                setmPageAlias(Constant.PAGE_GAME_HIGHLYSELECT);
                break;
            case FRAGMENT_TAB_SOFT:
                setmPageAlias(Constant.PAGE_SOFT_HIGHLYSELECT);
                break;
        }
        statEvent();
    }

    public void onEventMainThread(String event) {
        if ("refreshPage".equals(event)) {
            if (allDatas.size() > 0) {
                LogUtils.i("Loading", "刷新推荐页面");
                new DownloadAdAppReplacer().replaceByBaseBeanList(allDatas);
                mDatas = DataShell.wrapData(allDatas, 0);
                mAdapter.setList(mDatas);
            }
        } else if (event.equals("soft_bangdan") || event.equals("soft_fenlei") ||
                event.equals("game_bangdan") || event.equals("game_fenlei")) {
            isSelf = false;
        } else if (event.equals("soft_jingxuan") || event.equals("game_jingxuan")) {
            isSelf = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private int mCurrentPage = GlobalConfig.FIRST_PAGE;

    /**
     * 请求网络
     *
     * @param isFromOnCreate f非刷新上拉加载的，不显示头布局
     */
    private void requestData(final boolean isFromOnCreate) {
        LogUtils.i("fasg34tfq24",  "精选精选requestData");

        if (!NetWorkUtils.isConnected(getContext())) {
            setPageWhileFailure();
            LogUtils.i("aaa", isFromOnCreate + " --isFromOnCreate!");
            if (!isFromOnCreate) {
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
                mPullToLoadView.setLoadStopAndConfirmResult(false);
                if (mCurrentPage == GlobalConfig.FIRST_PAGE && allDatas.size() == 0) {
                    ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);  //只有第一次才显示无网络图
                }
            } else {
                ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);  //只有第一次才显示无网络图
            }

        } else {

            if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                clearList(pageType);
                createBigPositionGeter(pageType);
            }

            // 发送广告请求
            String adsParams = getAdsTypeParams(pageType);
            AdService.getInstance().fetchAd(adsParams, pageType, createAdRequestBean(adsParams));

            mayDataRequestTime = System.currentTimeMillis();
            mDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<BaseBean>>() {
                @Override
                public void onResponse(Call<List<BaseBean>> call, final Response<List<BaseBean>> response) {
                    try {
                        if (!isFromOnCreate) {
                            mPullToLoadView.setRefreshStopAndConfirmResult(true);
                            mPullToLoadView.setLoadStopAndConfirmResult(true);
                        }
                        final List<BaseBean> requestDataList = response.body();
                        if (requestDataList != null && requestDataList.size() > 0) {//有数据；
                            mDatas.clear();
                            LogUtils.i(LogTAG.AdTAG, mFragmentTabName + " 数据请求时间 = " + (System.currentTimeMillis() - mayDataRequestTime));

                            AppsShell.RefactorDataStructure(requestDataList, getBigPositionGeter(pageType)); //转换数据结构，提高页面流畅度

                            AppBeanTransfer.transferBaseData(requestDataList);

                            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    AdService.getInstance().handleAd(pageType, requestDataList, allDatas);
                                    setDatas(response, requestDataList, isFromOnCreate);

                                }
                            }, getWaitTime());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<List<BaseBean>> call, Throwable t) {
                    LogUtils.i("GOOD", mFragmentTabName + "--failed!");
                    setPageWhileFailure();
                    if (!isFromOnCreate) {
                        mPullToLoadView.setRefreshStopAndConfirmResult(false);
                        mPullToLoadView.setLoadStopAndConfirmResult(false);
                    }
                    isRefresh = refresh(0, isRefresh);
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                }

            }).bulid();

            excucteRequest(mDao);

        }
    }

    private void setDatas(Response<List<BaseBean>> response, final List<BaseBean> requestDataList, boolean isFromOnCreate) {

        // 精选页面数据调用玩咖曝光上报
        WanKaManager.exposureApps(requestDataList, null, "精选页面曝光");

        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
            allDatas.clear();
            mLayoutManger.scrollToPosition(0);
        }

        new DownloadAdAppReplacer().replaceByBaseBeanList(requestDataList);
        allDatas.addAll(requestDataList);
        mDatas = DataShell.wrapData(allDatas, mlastPagePositionOfNetData);
        setMlastPagePositionOfNetData(mDatas);
        isRefresh = refresh(1, isRefresh);
        //刷新和加载更多停止
        mPullToLoadView.showContent();
        mAdapter.setList(mDatas);

        //  第一次请求，最后一页就是固定的
        mLastpage = Constant.getLastPage(response.headers());

        // 上传已展示广告包名集合数据
        AdReporter.reportShow(pageType, mCurrentPage);
    }

    private void createBigPositionGeter(int pageType) {
        switch (pageType) {
            case SOFTWARE_ESSENCE_AD:
                bigPositionGeterBySoft = new BigPositionGeter();
                break;
            case GAME_ESSENCE_AD:
                bigPositionGeterByGame = new BigPositionGeter();
                break;
        }
    }

    private BigPositionGeter getBigPositionGeter(int pageType) {
        switch (pageType) {
            case SOFTWARE_ESSENCE_AD:
                return bigPositionGeterBySoft;
            case GAME_ESSENCE_AD:
                return bigPositionGeterByGame;
        }

        return null;
    }

    private String getAdsTypeParams(int pageType) {
        switch (pageType) {
            case SOFTWARE_ESSENCE_AD:
                return AdsTypeParams.softwareIndexAdType;
            case GAME_ESSENCE_AD:
                return AdsTypeParams.GameIndexAdType;
            default:
                return "";
        }
    }

    private int getAdStartPage() {
        int page = 0;
        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
            page = mCurrentPage - 1;
        } else {
            page = (mCurrentPage + 4 * (mCurrentPage - 1)) - 1;
        }
        return page;
    }

    private void setPageWhileFailure() {
        if (mCurrentPage > GlobalConfig.FIRST_PAGE) {
            mCurrentPage--;
        }
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

    /**
     * 不同tab需要请求不同数据；
     *
     * @param dao
     */
    private void excucteRequest(NetDataInterfaceDao dao) {
        switch (mFragmentTabName) {
            case FRAGMENT_TAB_GAME:
                dao.requestGameIndex(mCurrentPage, 10);
                break;
            case FRAGMENT_TAB_SOFT:
                dao.requestSoftIndex(mCurrentPage, 10);
                break;
        }
    }


    /**
     * 每次请求网络数据成功之后需要记录当前网络返回的最后一条数据中的最后一个元素在整个页面中所在的位置；
     *
     * @param temp
     */
    private void setMlastPagePositionOfNetData(List<ItemData<BaseBean>> temp) {
        try {
            if (temp != null && temp.size() > 0) {
                ItemData item = temp.get(temp.size() - 1);
                this.mlastPagePositionOfNetData = item.getPos();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.pullToLoadView);
        mPullToLoadView.setLayoutManager(mLayoutManger);
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.setOnRefreshAndLoadListener(this);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = GlobalConfig.FIRST_PAGE;
                mPullToLoadView.showLoading();
                requestData(false);
            }
        });

        switch (mFragmentTabName) {
            case FRAGMENT_TAB_SOFT:
                pageType = SOFTWARE_ESSENCE_AD;
                break;
            case FRAGMENT_TAB_GAME:
                pageType = GAME_ESSENCE_AD;
                break;
        }
    }


    private int getAdCount() {
        return mCurrentPage > 5 ? 50 : mCurrentPage * 15;
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        mCurrentPage = GlobalConfig.FIRST_PAGE;
        requestData(false);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        mCurrentPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        LogUtils.i("aaa", "mLastpage==>" + mLastpage + "====mCurrentPage==>" + mCurrentPage + "====hasMoreData==>" + hasMoreData);
        if (hasMoreData) {
            requestData(false);
        } else {
            mPullToLoadView.setHasNextPage2ShowFooter();
        }
    }

    /**
     * 创建广告请求所需分页参数的实体
     */
    private AdRequestBean createAdRequestBean(String adMold) {
        AdRequestBean bean = new AdRequestBean();
        switch (adMold) {
            case AdMold.WanDouJia:
                bean.wanDouJia.startNum = getAdStartPage();
                bean.wanDouJia.adCount = 30;
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

        switch (mFragmentTabName) {
            case FRAGMENT_TAB_SOFT:
                // 没设置广告
                if (TextUtils.isEmpty(AdsTypeParams.softwareIndexAdType)) {
                    return 0;
                }
                break;
            case FRAGMENT_TAB_GAME:
                // 没设置广告
                if (TextUtils.isEmpty(AdsTypeParams.GameIndexAdType)) {
                    return 0;
                }
                break;
        }

        // 设置页面了广告
        return 200;
    }

    /**
     * 清空相关list
     */
    private void clearList(int pageType) {
        // 请求第一页时，要清空已展示的广告名单
        AdService.getInstance().getExistAdList(pageType).clear();

        // 清空由于与自有资源重复而被删除的广告列表
        AdService.getInstance().getRemoveAdList(pageType).clear();

        // 清空备用资源
        AdService.getInstance().getBackupList(pageType).clear();
    }

}
