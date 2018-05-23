package cn.lt.android.main.recommend;

import android.os.Bundle;
import android.os.RemoteException;
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
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.main.AppAdapter;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
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
 * 必备单个fragment
 */
public class SingleNecessaryFragment extends BaseFragment implements IrefreshAndLoadMoreListener {

    private boolean hasNextPage;
    private boolean isRefresh;
    private String mTitle;
    private List<AppBriefBean> allDatas = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManger;
    private int mLastpage;
    private boolean isFromOnCreate;
    private boolean isFromRetry;
    private int pageCount;
    //标记数据重复
    private boolean isRepeat;

    public static SingleNecessaryFragment newInstance(String title, String id, int position, int pageCount) {
        SingleNecessaryFragment fragment = new SingleNecessaryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.EXTRA_ID, id);
        bundle.putInt("pagePosition", position);
        bundle.putInt("pageCount", pageCount);
        bundle.putString(Constant.EXTRA_PAGE, title);
        fragment.setArguments(bundle);
        return fragment;
    }

    private PullToLoadView mPullToLoadView;
    private AppAdapter mAdapter;
    public View mRootView;
    private String mId;

    private int mCurrentPage = GlobalConfig.FIRST_PAGE;

    @Override
    public void setPageAlias() {
        LogUtils.i("Erosion", "setPageAlias==");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mId = getArguments().getString(Constant.EXTRA_ID);
        mTitle = getArguments().getString(Constant.EXTRA_PAGE, "");
        pageCount = getArguments().getInt("pageCount");
        if (pageCount == 1) {
            mTitle = Constant.PAGE_NORMAL_PT_TITLE + getArguments().getString(Constant.EXTRA_PAGE, "");
        } else {
            mTitle = Constant.PAGE_NORMAL_LIST_TITLE + getArguments().getString(Constant.EXTRA_PAGE, "");
        }
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_rank_single, container, false);
            init();
            if (getUserVisibleHint() && !isRepeat) {
                requestData(mCurrentPage, true, false);
                isRepeat = true;
            }
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
        if (isVisibleToUser && mRootView != null) {
            if (!isRepeat) {
                requestData(mCurrentPage, true, false);
                isRepeat = true;
            }
        }
        if (isVisibleToUser) {
            setPageAndUploadPageEvent();
        }
    }

    /**
     * 设置页面并上报
     */
    private void setPageAndUploadPageEvent() {
        setmPageAlias(TextUtils.isEmpty(mTitle) ? Constant.PAGE_NORMAL_LIST : mTitle, mId);
        statEvent();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    private void init() {
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.pullToLoadView);
        mLayoutManger = new LinearLayoutManager(getContext());
        mPullToLoadView.setLayoutManager(mLayoutManger);
        mAdapter = new AppAdapter(getContext(), mTitle, mId, "普通列表");
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.addCustomHeader(createHeaderView());
        mPullToLoadView.setOnRefreshAndLoadListener(this);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = GlobalConfig.FIRST_PAGE;
                mPullToLoadView.showLoading();
                requestData(mCurrentPage, false, true);
            }
        });
    }

    /**
     * 请求网络
     *
     * @param isFromOnCreate true:第一次请求数据，不显示头布局。  false：其余情况下请求网络
     * @param isFromRetry
     */
    public void requestData(int page, final boolean isFromOnCreate, final boolean isFromRetry) {
        isRepeat = true;
        this.isFromOnCreate = isFromOnCreate;
        this.isFromRetry = isFromRetry;
        if (!NetWorkUtils.isConnected(getContext())) {
            if (!isFromOnCreate && !isFromRetry) {
                mPullToLoadView.setRefreshStopAndConfirmResult(false);      //第一次请求刷新不能设置结果
            } else {
                isRefresh = refresh(0, isRefresh);
                ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);  //只有第一次才显示无网络图
            }
            mPullToLoadView.setLoadStopAndConfirmResult(false);
            setPageWhileFailure();  //加载失败-1
            return;
        }

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
            @Override
            public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                //刷新和加载更多停止
                if (!isFromOnCreate && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(true);
                }
                mPullToLoadView.setLoadStopAndConfirmResult(true);
                isRefresh = refresh(1, isRefresh);
                List<AppDetailBean> appDetailBeanList = response.body();
                if (appDetailBeanList == null || appDetailBeanList.size() == 0) {
                    mPullToLoadView.showEmpty();
                    return;
                } else {
                    mPullToLoadView.showContent();
                }

                final List<AppBriefBean> briefList = AppBeanTransfer.transferAppDetailList(appDetailBeanList);

                // 玩咖数据曝光
                WanKaManager.exposureApps(briefList, new SimpleResponseListener<JSONObject>() {
                    @Override
                    public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                        // 成功回调之后会修改briefList 集合里边的数据,直接notifyDataSetChanged

                        // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                        new DownloadAdAppReplacer().replaceByAppBriefList(briefList);
                        mAdapter.notifyDataSetChanged();
                    }
                }, "必备单个页面曝光");

                //下载数据初始化
                try {
                    DownloadTaskManager.getInstance().transferBriefBeanList(briefList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                new DownloadAdAppReplacer().replaceByAppBriefList(briefList);

                //根据页码添加数据
                if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                    allDatas.clear();
                    mLayoutManger.scrollToPosition(0);
                }
                allDatas.addAll(briefList);
                mAdapter.setList(allDatas);

                hasNextPage = hasNextPage(response);
                //  第一次请求，最后一页就是固定的
                mLastpage = Constant.getLastPage(response.headers());
                if (!hasNextPage) {
                    mPullToLoadView.setHasNextPage2ShowFooter();    //防止第一次请求没有设置底部
                }
            }

            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                //刷新和加载更多停止
                setPageWhileFailure();
                mPullToLoadView.setLoadStopAndConfirmResult(false);
                if (!isFromOnCreate && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(false);
                } else {
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                }
                isRefresh = refresh(0, isRefresh);
            }
        }).bulid().requestNormalList(mId, page);
//        mNetDataInterfaceDao.requestNormalList(mId, page);
    }

    private View createHeaderView() {
        View view = new View(getContext());
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(getContext(), 8)));
        return view;
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

    private void setPageWhileFailure() {
        if (mCurrentPage > GlobalConfig.FIRST_PAGE) {
            mCurrentPage--;
        }
    }


    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public synchronized void onEventMainThread(final DownloadEvent downloadEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppEntity appInfo = mAdapter.getList().get(i).getDownloadAppEntity();
            if (FileDownloadUtils.generateId(appInfo.getPackageName(), appInfo.getSavePath()) == downloadEvent.downloadId) {
                //更新界面i
                appInfo.setTotal(downloadEvent.totalBytes);
                appInfo.setSoFar(downloadEvent.soFarBytes);
                appInfo.setStatus(downloadEvent.status);
                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
            }

        }
    }


    /**
     * 通知安装事件更新
     *
     * @param installEvent
     */
    public void onEventMainThread(InstallEvent installEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppEntity appInfo = mAdapter.getList().get(i).getDownloadAppEntity();
            if (appInfo.getPackageName().equals(installEvent.packageName)) {
                //更新界面i
                appInfo.setStatusByInstallEvent(installEvent.type);
                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
            }

        }
    }


    public void onEventMainThread(RemoveEvent event) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppEntity appInfo = mAdapter.getList().get(i).getDownloadAppEntity();
            if (appInfo.getPackageName().equals(event.mAppEntity.getPackageName())) {
                //更新界面i
                appInfo.setStatus(DownloadStatusDef.INVALID_STATUS);
                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
            }

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
        requestData(mCurrentPage, false, false);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        mCurrentPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        if (hasMoreData) {
            requestData(mCurrentPage, false, false);
        } else {
            mPullToLoadView.setHasNextPage2ShowFooter();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((NewAppActivity) mContext).mActionBar.setPageName(getPageAlias());

        new DownloadAdAppReplacer().replaceByAppBriefList(allDatas);
        mAdapter.setList(allDatas);
    }
}
