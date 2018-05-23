package cn.lt.android.main.search;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.entity.SearchHistoryBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.appstore.R;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cn.lt.android.main.search.SearchActivityUtil.getHistoryList;

/**
 * Created by atian on 2016/1/21.
 * 搜索自动匹配页面
 */
public class SearchAutoMatchFragment extends BaseFragment implements IrefreshAndLoadMoreListener {
    private EntranceAdapter mAdapter;
    private PullToLoadView mPullToLoadView;
    private String mKeyword;
    private boolean isFromRetry;

    // 是否删除是删除历史记录

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_SEARCH_AUTOMATCH, "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.search_automatch_fragment, container, false);
            initView();
        }
        return mRootView;
    }

    private void getIntentData() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mKeyword = bundle.getString("keyWord", "");
            LogUtils.i("zzz", "自动匹配词==" + mKeyword);
        }
    }

    private void initView() {
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.autoMatchPullToLoadView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mPullToLoadView.setLayoutManager(layoutManager);
        mAdapter = new EntranceAdapter(mContext, getPageAlias(), mKeyword);
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.setOnRefreshAndLoadListener(this);
        mPullToLoadView.showLoading();
        mPullToLoadView.setNoFooter();

        // 无网络时，点击刷新按钮
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNetWork(mKeyword, false, true);
            }
        });

    }

    /**
     * 请求网络
     *
     * @param keyword
     * @param isFromRefresh
     * @param isFromRetry   是否来自重试
     */
    public void checkNetWork(String keyword, boolean isFromRefresh, boolean isFromRetry) {
        this.isFromRetry = isFromRetry;
        if (NetUtils.isConnected(LTApplication.instance)) {
            this.mKeyword = keyword;
            requestData(keyword, isFromRefresh);
        } else {
            mPullToLoadView.showErrorNoNetwork();
            mPullToLoadView.setLoadStopAndConfirmResult(false);
            if (isFromRefresh) mPullToLoadView.setRefreshStopAndConfirmResult(false);
        }
    }


    /***
     * 请求网络数据
     */
    List<AppBriefBean> apps = new ArrayList<>();   // 精确匹配数据(AppDetailBean)
    List<ItemData<SearchHistoryBean>> localList = new BaseBeanList<>();//本地历史记录数据
    List<ItemData<HotSearchBean>> fuzzyList = new BaseBeanList<>();//模糊匹配数据

    public void requestData(final String keyword, final boolean isFromRefresh) {

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<BaseBean>>() {
            @Override
            public void onResponse(Call<List<BaseBean>> call, Response<List<BaseBean>> response) {
                if (isFromRefresh) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(true);    //这个可以控制只在刷新时，才出现下拉头
                }

                setData(keyword, response);
                mPullToLoadView.showContent();
            }

            @Override
            public void onFailure(Call<List<BaseBean>> call, Throwable t) {
                if (isFromRefresh) mPullToLoadView.setRefreshStopAndConfirmResult(false);
                mPullToLoadView.setVisibility(View.GONE);
            }
        }).bulid().requestAutoMatch(keyword);

    }

    private void setData(String keyword, Response<List<BaseBean>> response) {
        List<BaseBean> netData = response.body();
        BaseBeanList<SearchHistoryBean> baseBeanList = getHistoryBaseBean(keyword);
        if (netData != null && baseBeanList.size() > 0) {
            netData.add(baseBeanList);
        }

        List<ItemData<BaseBean>> mDatas = DataShell.wrapData(netData, 0);
        if (mDatas == null || mDatas.size() == 0) {
            return;
        }

        for (int i = 0; i < mDatas.size(); i++) {
            ItemData item = (ItemData) mDatas.get(i);
            if (item != null) {
                PresentType type = item.getmPresentType();
                switch (type) {
                    //精确搜索类型
                    case hotword_app:
                        apps.clear();
                        BaseBeanList<ItemData<AppDetailBean>> accurateDatas = (BaseBeanList<ItemData<AppDetailBean>>) item.getmData();
                        for (ItemData<AppDetailBean> itemData : accurateDatas) {
                            itemData.getmData().setLtType("app");
                            apps.add(AppBeanTransfer.transferAppDetailBean(itemData.getmData()));
                        }
                        break;
                    case history_app:
                        localList.clear();
                        List<ItemData<SearchHistoryBean>> historyDatas = (List<ItemData<SearchHistoryBean>>) item.getmData();
                        for (ItemData<SearchHistoryBean> itemData : historyDatas) {
                            localList.add(itemData);
                        }
                        break;
                    //模糊搜索类型
                    case automatch_title:
                        fuzzyList.clear();
                        List<ItemData<HotSearchBean>> fuzzyDatas = (List<ItemData<HotSearchBean>>) item.getmData();
                        for (ItemData<HotSearchBean> itemData : fuzzyDatas) {
                            fuzzyList.add(itemData);
                        }
                        break;
                }
            } else {
                LogUtils.i("zzz", "itemData为空");
            }
        }
        setAccurateAdapter();
    }

    private void setAccurateAdapter() {
        WanKaManager.exposureApps(apps, new SimpleResponseListener<JSONObject>() {
            @Override
            public void onFailed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {

            }

            @Override
            public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                populate();
            }
        }, "搜索自动匹配曝光");

        populate();
    }

    private void populate() {
        new DownloadAdAppReplacer().replaceByAppBriefList(apps);

        List itemDatas = DataShell.wrapData(apps, 0);
        itemDatas.addAll(localList);
        itemDatas.addAll(fuzzyList);

        mAdapter.setList(itemDatas);
    }

    @Override
    public void onResume() {
        statEvent();
        checkNetWork(mKeyword, false, false);
        super.onResume();
    }

    private BaseBeanList<SearchHistoryBean> getHistoryBaseBean(String keyword) {
        List<SearchHistoryBean> historyList = new ArrayList<>();
        historyList.addAll(getHistoryList(keyword));
        BaseBeanList<SearchHistoryBean> baseBean = new BaseBeanList<>();
        baseBean.setLtType("history_app");
        for (int i = 0; i < historyList.size(); i++) {
            SearchHistoryBean searchBean = new SearchHistoryBean();
            searchBean.setTitle(historyList.get(i).getTitle());
            baseBean.add(searchBean);
        }
        return baseBean;
    }

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
        for (int i = 0; i < mAdapter.getList().size(); i++) {
            AppDetailBean app = (AppDetailBean) ((ItemData) mAdapter.getList().get(i)).getmData();
            AppEntity appInfo = app.getDownloadAppEntity();
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
            AppDetailBean app = (AppDetailBean) ((ItemData) mAdapter.getList().get(i)).getmData();
            AppEntity appInfo = app.getDownloadAppEntity();
            if (appInfo.getPackageName().equals(installEvent.packageName)) {
                //更新界面i
                appInfo.setStatusByInstallEvent(installEvent.type);
                mAdapter.notifyItemChanged(mAdapter.hasHeader() ? i + 1 : i);
            }

        }
    }

    /***
     * 删除历史记录
     *
     * @param historyBean
     */
    public void onEventMainThread(SearchHistoryBean historyBean) {
        if (historyBean != null) {
            SearchActivityUtil.deleteHistoryDataByTitle(historyBean.getTitle());
            mAdapter.remove(historyBean.getPos());
            Log.i("SearchAutoMatch", "历史记录已删除");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        checkNetWork(mKeyword, true, false);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {

    }


}
