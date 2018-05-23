package cn.lt.android.main.search;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/1/20.
 * 搜索推荐页面/搜索无结果页面
 */
public class SearchAdvFragment extends BaseFragment implements View.OnClickListener, IrefreshAndLoadMoreListener {
    private View mRootView;
    public PullToLoadView mPullToLoadView;
    private EntranceAdapter mAdapter;
    private int fragmentType;//区分是搜索推荐页还是搜索无结果页

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.search_adv_layout, container, false);
            initView();
            requestData();
        }
        return mRootView;
    }

    private List<ItemData<BaseBean>> mDatas = new ArrayList<>();

    private void requestData() {
        if (!NetWorkUtils.isConnected(getContext())) {
            ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);
        } else {
            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<BaseBean>>() {
                @Override
                public void onResponse(Call<List<BaseBean>> call, Response<List<BaseBean>> response) {
                    LogUtils.i("SearchAdv", "SearchAdv 搜索推荐成功");
                    final BaseBeanList<BaseBean> netBeanList = (BaseBeanList<BaseBean>) response.body();
                    if (netBeanList != null && netBeanList.size() > 0) {
                        try {
                            WanKaManager.exposureApps(netBeanList, new SimpleResponseListener<JSONObject>() {
                                @Override
                                public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                                    mDatas = DataShell.wrapData(netBeanList, 0);
                                    mAdapter.setList(mDatas);
                                }

                                @Override
                                public void onFailed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {

                                }
                            }, "搜索推荐曝光:");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mDatas = DataShell.wrapData(netBeanList, 0);
                        if (SearchActivityUtil.SEARCHNODATA == fragmentType) {
                            LogUtils.i("SearchAdv", "显示无结果页面");
                            View top = LayoutInflater.from(ActivityManager.self().topActivity()).inflate(R.layout.no_searchout_stub, null);
                            mPullToLoadView.addCustomHeader(top);
                        }
                        mAdapter.setList(mDatas);
                        mPullToLoadView.setLoadStopAndConfirmResult(true);
                        mPullToLoadView.setRefreshStopAndConfirmResult(true);
                        mPullToLoadView.showContent();
                    } else {
                        mPullToLoadView.showEmpty();
                    }

                }

                @Override
                public void onFailure(Call<List<BaseBean>> call, Throwable t) {
                    LogUtils.i("SearchAdv", "SearchAdv请求失败");
                    mPullToLoadView.setLoadStopAndConfirmResult(false);
                    mPullToLoadView.setRefreshStopAndConfirmResult(false);
                    mPullToLoadView.showErrorNoNetwork();
                }

            }).bulid().requestMutilSearch();
        }
    }

    /***
     * 初始化UI
     */
    private void initView() {
        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(mContext);
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.ptv_search);
        mPullToLoadView.setLayoutManager(mLayoutManger);
        mPullToLoadView.showLoading();
        mAdapter = new EntranceAdapter(mContext, getPageAlias(), "");
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.setNoFooter();
        mPullToLoadView.setOnRefreshAndLoadListener(this);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPullToLoadView.showLoading();
                requestData();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (SearchActivityUtil.SEARCHADV == fragmentType) {
            LTApplication.instance.word = "";
        }
        statEvent();
    }

    @Override
    public void setPageAlias() {
        if (SearchActivityUtil.SEARCHNODATA == fragmentType) {
            setmPageAlias(Constant.PAGE_SEARCH_NODATA, "");
        } else {
            setmPageAlias(Constant.PAGE_SEARCH_ADV, "");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();

    }

    private void getIntentData() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fragmentType = bundle.getInt("fragmentType", SearchActivityUtil.SEARCHADV);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        requestData();
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {

    }
}
