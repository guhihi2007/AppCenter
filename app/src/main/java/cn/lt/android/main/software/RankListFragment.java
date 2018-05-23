package cn.lt.android.main.software;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wenchao on 2016/2/16.
 * 榜单
 */
@SuppressWarnings("ALL")
public class RankListFragment extends BaseFragment implements IrefreshAndLoadMoreListener {
    public static final String FRAGMENT_TAB_SOFT_HOT = "soft_hot";
    public static final String FRAGMENT_TAB_SOFT_MONTH = "soft_month";
    public static final String FRAGMENT_TAB_SOFT_TOTAL = "soft_total";
    public static final String FRAGMENT_TAB_GAME_OFFLINE = "game_offline";
    public static final String FRAGMENT_TAB_GAME_ONLINE = "game_online";
    public static final String FRAGMENT_TAB_GAME_TOTAL = "game_total";
    public static final String FRAGMENT_TAB_RANK_SOLF = "rank_soft";
    public static final String FRAGMENT_TAB_RANK_GAME = "rank_game";
    public static final String THEME_TYPE_MIX = "1";
    public static final String THEME_TYPE_SINGLE = "2";
    private String mFragmentTabName = FRAGMENT_TAB_RANK_SOLF; //首次跳到榜单
    private int mCurrentPage = GlobalConfig.FIRST_PAGE;
    private int mTotalPage;
    private View mRootView;
    public PullToLoadView mPullToLoadView;
    private RecyclerView.LayoutManager mLayoutManger;
    private EntranceAdapter mAdapter;
    private String mThemeType = THEME_TYPE_MIX;
    private NetDataInterfaceDao mDao;
    List<AppBriefBean> allData = new ArrayList<>();
    /**
     * 用于记录从服务器获取数据的最后一个的位置，初次请求时为0；
     */
    private int mlastPagePositionOfNetData = 0;
    private List<AppEntity> mInstallFailureTaskList;
    private boolean isRefresh;
    private int mLastpage;
    private boolean hasNextPage;
    private boolean isNotVisible;

    private boolean isRequested;

    public static RankListFragment newInstance(String tab) {
        RankListFragment fragment = new RankListFragment();
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
        LogUtils.i("RankListFragment", "onCreate走了");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentTabName = getArguments().getString(BaseFragment.FRAGMENT_TAB);
        getThemeType();
        if (mRootView == null) {
            initView();

            if (!isRequested && getUserVisibleHint()) {
                requestData(true, false);
                isRequested = true;
            }
        }
        return mRootView;
    }

    private void initView() {
        mRootView = LayoutInflater.from(ActivityManager.self().topActivity()).inflate(R.layout.fragment_rank_single, null);
        mLayoutManger = new LinearLayoutManager(getContext());
        mAdapter = new EntranceAdapter(getContext(), getPageAlias(), "rank");
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.pullToLoadView);
        mPullToLoadView.setLayoutManager(mLayoutManger);
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.setOnRefreshAndLoadListener(this);
//        mPullToLoadView.setRefreshable(false);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = GlobalConfig.FIRST_PAGE;
                mPullToLoadView.showLoading();
                requestData(false, true);
            }
        });
    }

    private void getThemeType() {
        try {
            mThemeType = GlobalParams.getHostBean().getSettings().getRank_style();
        } catch (Exception e) {
            e.printStackTrace();
            mThemeType = THEME_TYPE_MIX;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mContext != null) {
            if (getParentFragment() != null && !getParentFragment().getUserVisibleHint()) {
                // 可能出现自己可见 但是父Fragment不可见
                setUserVisibleHint(false);
                return;
            }

            if (!isRequested) {
                requestData(true, false);
                isRequested = true;
            }

            String alias = getArguments().getString((BaseFragment.FRAGMENT_TAB));

            if (!TextUtils.isEmpty(alias)) {
                switch (alias) {
                    case FRAGMENT_TAB_SOFT_HOT:
                        setmPageAlias(Constant.PAGE_SOFT_HOT_RANK);
                        LogUtils.i("iii", "RankList已经是可见后的获得焦点了1:" + Constant.PAGE_SOFT_HOT_RANK);
                        break;
                    case FRAGMENT_TAB_SOFT_MONTH:
                        setmPageAlias(Constant.PAGE_SOFT_MONTH_RANK);
                        LogUtils.i("iii", "RankList已经是可见后的获得焦点了2:" + Constant.PAGE_SOFT_MONTH_RANK);
                        break;
                    case FRAGMENT_TAB_GAME_OFFLINE:
                        setmPageAlias(Constant.PAGE_GAME_OFFLINE_RANK);
                        LogUtils.i("iii", "RankList已经是可见后的获得焦点了3:" + Constant.PAGE_GAME_OFFLINE_RANK);
                        break;
                    case FRAGMENT_TAB_GAME_ONLINE:
                        setmPageAlias(Constant.PAGE_GAME_LINE_RANK);
                        LogUtils.i("iii", "RankList已经是可见后的获得焦点了4:" + Constant.PAGE_GAME_LINE_RANK);
                        break;
                    case FRAGMENT_TAB_RANK_SOLF:
                        setmPageAlias(Constant.PAGE_SOFT_RANK);
                        LogUtils.i("iii", "RankList已经是可见后的获得焦点了5:" + Constant.PAGE_SOFT_RANK);
                        break;
                    case FRAGMENT_TAB_RANK_GAME:
                        setmPageAlias(Constant.PAGE_GAME_RANK);
                        LogUtils.i("iii", "RankList已经是可见后的获得焦点了6:" + Constant.PAGE_GAME_RANK);
                        break;
                }
            }
            statEvent();
//            }

            ((MainActivity) mContext).mHeadView.setPageName(getPageAlias());

            if (allData.size() > 0) {
                new DownloadAdAppReplacer().replaceByAppBriefList(allData);
                List<ItemData<AppBriefBean>> temps = DataShell.wrapData(allData, 0);
                mAdapter.setList(resolver(temps));
            }
        }
    }

    public void onEventMainThread(String event) {
        if (event.equals("soft_jingxuan") || event.equals("soft_fenlei") || event.equals("game_jingxuan") || event.equals("game_fenlei")) {
            isNotVisible = false;
        } else if (event.equals("soft_bangdan") || event.equals("game_bangdan")) {
            isNotVisible = true;
        }
    }


    /**
     * 请求网络
     *
     * @param isFromOnCreate f非刷新上拉加载的，不显示头布局
     * @param isFromRetry    TODO 在RankRootFragment里面调用，实现可见时才去请求网络
     */
    public void requestData(final boolean isFromOnCreate, final boolean isFromRetry) {
        if (mRootView == null) {
            initView();
        }

        if (!NetWorkUtils.isConnected(ActivityManager.self().topActivity())) {
            setPageWhileFailure();
            mPullToLoadView.setLoadStopAndConfirmResult(false);
            if (!isFromOnCreate && !isFromRetry) {
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
            } else {
//                isRefresh = refresh(0, isRefresh);
                ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);  //只有第一次才显示无网络图
            }
            return;
        }

        mDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
            @Override
            public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {

                if (!isFromOnCreate && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(true);
                }
                mPullToLoadView.setLoadStopAndConfirmResult(true);
                mPullToLoadView.showContent();

                List<AppDetailBean> datas = response.body();

                if (datas != null && datas.size() > 0) {
                    final List<AppBriefBean> briefList = AppBeanTransfer.transferAppDetailList(datas);
                    WanKaManager.exposureApps(briefList, new SimpleResponseListener<JSONObject>() {
                        @Override
                        public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> exposureResponse) {
                            new DownloadAdAppReplacer().replaceByAppBriefList(briefList);

                            // 成功回调之后会修改briefList 集合里边的数据
                            mAdapter.setList(resolver(DataShell.wrapData(allData, 0)));
                        }
                    }, "榜单页面曝光");
                    // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                    new DownloadAdAppReplacer().replaceByAppBriefList(briefList);
                    if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                        allData.clear();
                    }
                    allData.addAll(briefList);
                    mAdapter.setList(resolver(DataShell.wrapData(allData, 0)));
                    if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                        mLayoutManger.scrollToPosition(0);   //只要是第一頁都回頂部
                    }
                    hasNextPage = hasNextPage(response);
                    //  第一次请求，最后一页就是固定的
                    mLastpage = Constant.getLastPage(response.headers());
                    if (!hasNextPage) {
                        mPullToLoadView.setHasNextPage2ShowFooter();    //防止第一次请求没有设置底部
                    }

                } else if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                    mPullToLoadView.showEmpty();
                }
                isRefresh = refresh(1, isRefresh);
            }


            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                setPageWhileFailure();
                if (mPullToLoadView != null) {
                    if (!isFromOnCreate && !isFromRetry) {
                        mPullToLoadView.setRefreshStopAndConfirmResult(false);
                    } else {
                        ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                    }
                    mPullToLoadView.setLoadStopAndConfirmResult(false);
//                    isRefresh = refresh(0, isRefresh);
                }
            }

        }).bulid();
        excucteRequest();
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

    private List<ItemData<? extends BaseBean>> resolver(List<ItemData<AppBriefBean>> temps) {
        List<ItemData<? extends BaseBean>> lists = new ArrayList<>();
        if (!THEME_TYPE_SINGLE.equals(mThemeType)) {
            List<ItemData<AppBriefBean>> list = new BaseBeanList<>();
            for (int i = 0; i < 3; i++) {
                try {
                    list.add(temps.remove(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            ItemData<BaseBean> item = new ItemData<>((BaseBean) list);
            item.setmType(PresentType.abrahamian);
            lists.add(item);
            lists.addAll(temps);
        } else {
            lists.addAll(temps);
        }
        return lists;
    }

    /**
     * 每次请求网络数据成功之后需要记录当前网络返回的最后一条数据中的最后一个元素在整个页面中所在的位置；
     *
     * @param temp
     */
    private void setMlastPagePositionOfNetData(List<ItemData<AppBriefBean>> temp) {
        try {
            if (temp != null && temp.size() > 0) {
                ItemData item = temp.get(temp.size() - 1);
                this.mlastPagePositionOfNetData = item.getPos();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * @param
     */
    private void excucteRequest() {
        switch (mFragmentTabName) {
            case FRAGMENT_TAB_SOFT_HOT:
                mDao.requestSoftwareHotRank(mCurrentPage);
                break;
            case FRAGMENT_TAB_SOFT_MONTH:
                mDao.requestSoftwareMonthRank(mCurrentPage);
                break;
            case FRAGMENT_TAB_SOFT_TOTAL:
                mDao.requestSoftwareTotalRank(mCurrentPage);
                break;
            case FRAGMENT_TAB_GAME_OFFLINE:
                mDao.requestGameOffLineRank(mCurrentPage);
                break;
            case FRAGMENT_TAB_GAME_ONLINE:
                mDao.requestGameRankOnline(mCurrentPage);
                break;
            case FRAGMENT_TAB_GAME_TOTAL:
                mDao.requestGameTotalRank(mCurrentPage);
                break;
            case FRAGMENT_TAB_RANK_SOLF:
                mDao.requestSoftwareTotalRank(mCurrentPage);
                break;
            case FRAGMENT_TAB_RANK_GAME:
                mDao.requestGameTotalRank(mCurrentPage);
                break;
        }

    }


    private void setDownloadInfo(DownloadEvent downloadEvent, int i, AppEntity appInfo) {
        if (FileDownloadUtils.generateId(appInfo.getPackageName(), appInfo.getSavePath()) == downloadEvent.downloadId) {
            //更新界面i
            appInfo.setTotal(downloadEvent.totalBytes);
            appInfo.setSoFar(downloadEvent.soFarBytes);
            appInfo.setStatus(downloadEvent.status);
            mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
            //下载完成则移除
            if (downloadEvent.status == DownloadStatusDef.completed) {
                mAdapter.getList().remove(appInfo);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private View createHeaderView() {
        View view = new View(ActivityManager.self().topActivity());
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(getContext(), 8)));
        view.setBackgroundResource(R.color.grey_bg);
        return view;
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        mCurrentPage = GlobalConfig.FIRST_PAGE;
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
            mPullToLoadView.setHasNextPage2ShowFooter();
        }
    }

}
