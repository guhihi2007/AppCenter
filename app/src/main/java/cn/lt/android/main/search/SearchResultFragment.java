package cn.lt.android.main.search;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.appstore.R;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/1/21.
 * 搜索结果页面
 */
public class SearchResultFragment extends BaseFragment implements IrefreshAndLoadMoreListener, View.OnClickListener {
    private EntranceAdapter mAdapter;
    private PullToLoadView mPullToLoadView;
    private int mCurrentPage = GlobalConfig.FIRST_PAGE;
    private String keyword;
    private NetDataInterfaceDao mNetDataInterfaceDao;
    private boolean isAds;
    private LinearLayoutManager layoutManager;
    private boolean hasNextPage;
    private int mLastpage;
    private boolean isFromRefresh;
    private boolean isFromRetry;
    private String searchAdsId;

    List<AppBriefBean> allData = new ArrayList<>();

    @Override
    public void setPageAlias() {
        setmPageAlias(isAds ? Constant.PAGE_SEARCH_ADS : Constant.PAGE_SEARCH_RESULT, isAds ? searchAdsId : "");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this.getActivity();
        getIntentData();
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        checkNetWork(false, false, isAds, mCurrentPage, keyword);
    }

    private void getIntentData() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isAds = bundle.getBoolean("isAds");
            keyword = bundle.getString("keyWord", "");
            searchAdsId = bundle.getString("searchAdsId","");

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.search_result_fragment, container, false);
            initView();
        }
        return mRootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initView() {
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.resultPullToloadView);
        layoutManager = new LinearLayoutManager(mContext);
        mPullToLoadView.setLayoutManager(layoutManager);
        mAdapter = new EntranceAdapter(mContext, getPageAlias(), searchAdsId);
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.showLoading();
        mPullToLoadView.setOnRefreshAndLoadListener(this);
        mPullToLoadView.setOnRetryClickListener(this);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPullToLoadView.showLoading();
                checkNetWork(true, false, isAds, GlobalConfig.FIRST_PAGE, keyword);
            }
        });
        mNetDataInterfaceDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
            @Override
            public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                List<AppDetailBean> list = response.body();
                //刷新和加载更多停止
                if (isFromRefresh && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(true);
                }
                mPullToLoadView.setLoadStopAndConfirmResult(true);
                if (null != list && list.size() > 0) {
                    final List<AppBriefBean> briefList = AppBeanTransfer.transferAppDetailList(list);

                    WanKaManager.exposureApps(briefList, new SimpleResponseListener<JSONObject>() {
                        @Override
                        public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> exposureResponse) {
                            new DownloadAdAppReplacer().replaceByAppBriefList(briefList);

                            // 成功回调之后会修改briefList 集合里边的数据
                            mAdapter.setList(DataShell.wrapData(allData, 0));
                        }
                    }, "搜索结果曝光");


                    //初始化下载状态
                    try {
                        DownloadTaskManager.getInstance().transferBriefBeanList(briefList);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return;
                    }

                    // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                    new DownloadAdAppReplacer().replaceByAppBriefList(briefList);

                    //根据页码添加数据
                    if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                        allData.clear();
                    }

                    allData.addAll(briefList);
                    mAdapter.setList(DataShell.wrapData(allData, 0));

                    mPullToLoadView.showContent();

                    if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                        layoutManager.scrollToPosition(0);
                    }
                } else {
//                    if (mSearchCallBack != null) {
//                        Log.i("SearchResultFragment", "搜索无结果，跳转到无结果页面");
//                        mSearchCallBack.gotoAdverFragment();
//                    }
                    if (mCurrentPage == 1) {
                        LogUtils.i("SearchResultFragment", "第一页无结果。。。");
                        mAdapter.clear();
                        if (mSearchCallBack != null) {
                            LogUtils.i("SearchResultFragment", "搜索无结果，跳转到无结果页面");
                            mSearchCallBack.gotoAdverFragment();
                        }
                    } else {
                        LogUtils.i("SearchResultFragment", "非第一页无结果。。。显示底部信息");
                        mPullToLoadView.setHasNextPage2ShowFooter();
                    }
                }
                isRefresh = refresh(1, isRefresh);

                //  第一次请求，最后一页就是固定的
                hasNextPage = hasNextPage(response);
                mLastpage = Constant.getLastPage(response.headers());
                if (!hasNextPage) {
                    mPullToLoadView.setHasNextPage2ShowFooter();    //防止第一次请求没有设置底部
                }

            }

            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                setPageWhileFailure();
                mPullToLoadView.setLoadStopAndConfirmResult(false);
                isRefresh = refresh(0, isRefresh);
                if (isFromRefresh && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(false);
                } else {
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                }
            }


        }).bulid();


    }

    /**
     * 检测网络，并请求
     *
     * @param isFromRetry
     * @param isFromRefresh 是否来自刷新动作
     * @param isAds
     * @param page
     * @param keyword
     */
    public void checkNetWork(boolean isFromRetry, boolean isFromRefresh, boolean isAds, int page, String keyword) {
        this.isFromRefresh = isFromRefresh;
        this.mCurrentPage = page;
        this.keyword = keyword;
        this.isFromRetry = isFromRetry;
        if (NetUtils.isConnected(LTApplication.instance)) {
            if (isAds) {
                requestAdsData(isFromRefresh, keyword);
            } else {
                requestData(isFromRefresh, page, keyword);
            }
        } else {
            isRefresh = refresh(0, isRefresh);
            mPullToLoadView.setLoadStopAndConfirmResult(false);
            setPageWhileFailure();
            if (this.isFromRefresh && !isFromRetry) {
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
            } else {
                ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);
            }
        }

    }

    private void setPageWhileFailure() {
        if (mCurrentPage > GlobalConfig.FIRST_PAGE) {
            mCurrentPage--;
        }
    }

    /***
     * @param isFromRefresh
     * @param keyword
     */
    private void requestAdsData(boolean isFromRefresh, String keyword) {
        this.isFromRefresh = isFromRefresh;
        mNetDataInterfaceDao.requestSearchAdsList(keyword);
    }

    /***
     * 搜索结果请求
     *
     * @param isFromRefresh
     * @param page
     * @param keyword
     */
    private void requestData(boolean isFromRefresh, int page, String keyword) {
        this.isFromRefresh = isFromRefresh;
        mNetDataInterfaceDao.requestSearch(keyword, page);
    }

    /**
     * 判断是否还有下一页
     *
     * @param response
     * @return
     */
    private boolean hasNextPage(Response response) {
        try {
            int lastPage = Constant.getLastPage(response.headers());
            if (mCurrentPage < lastPage) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private SearchActivityUtil.SearchCallBack mSearchCallBack;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SearchActivityUtil.SearchCallBack)) {
            throw new IllegalStateException("TitlesListFragment所在的Activity必须实现TitlesListFragmentCallBack接口");
        }
        mSearchCallBack = (SearchActivityUtil.SearchCallBack) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSearchCallBack = null;
        EventBus.getDefault().unregister(this);
    }

    private boolean isRefresh;

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
//    public void onEventMainThread(DownloadEvent downloadEvent) {
//        for (int i = 0; i < mAdapter.getList().size(); i++) {
//            AppBriefBean app = (AppBriefBean) ((ItemData) mAdapter.getList().get(i)).getmData();
//            AppEntity appInfo = app.getDownloadAppEntity();
//            if (FileDownloadUtils.generateId(appInfo.getDownloadUrl(), appInfo.getSavePath()) == downloadEvent.downloadId) {
//                //更新界面i
//                appInfo.setTotal(downloadEvent.totalBytes);
//                appInfo.setSoFar(downloadEvent.soFarBytes);
//                appInfo.setStatus(downloadEvent.status);
//                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
//            }
//
//        }
//    }

    /**
     * 通知安装事件更新
     * <p>
     * //     * @param installEvent
     */
//    public void onEventMainThread(InstallEvent installEvent) {
//        for (int i = 0; i < mAdapter.getList().size(); i++) {
//            AppBriefBean app = (AppBriefBean) ((ItemData) mAdapter.getList().get(i)).getmData();
//            AppEntity appInfo = app.getDownloadAppEntity();
//            if (appInfo.getPackageName().equals(installEvent.packageName)) {
//                //更新界面i
//                appInfo.setStatusByInstallEvent(installEvent.type);
//                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
//            }
//
//        }
//    }
//变打开
//    public void onEventMainThread(RemoveEvent event) {
//        for (int i = 0; i < mAdapter.getList().size(); i++) {
//            AppBriefBean app = (AppBriefBean) ((ItemData) mAdapter.getList().get(i)).getmData();
//            AppEntity appInfo = app.getDownloadAppEntity();
//            if (appInfo.getPackageName().equals(event.mAppEntity.getPackageName())) {
//                //更新界面i
//                appInfo.setStatusByInstallEvent(DownloadStatusDef.INVALID_STATUS);
//                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
//            }
//        }
//    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

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

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        mCurrentPage = GlobalConfig.FIRST_PAGE;
        if (isAds) {
            checkNetWork(false, true, true, mCurrentPage, keyword);
        } else {
            checkNetWork(false, true, false, mCurrentPage, keyword);
        }
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        mCurrentPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        if (hasMoreData) {
            if (isAds) {
                checkNetWork(false, false, true, mCurrentPage, keyword);
            } else {
                checkNetWork(false, false, false, mCurrentPage, keyword);
            }
        } else {
            mPullToLoadView.setHasNextPage2ShowFooter();
        }
    }
}
