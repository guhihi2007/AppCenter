package cn.lt.android.main.software;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.entity.AppCatBean;
import cn.lt.android.main.Item;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.recommend.CategoryAdapter;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.PullToLoadView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wenchao on 2016/2/16.
 * 分类
 */
public class CategoryFragment extends BaseFragment {


    /**
     * 游戏分类
     */
    public static final int CAT_GAME = 0;
    /**
     * 软件分类
     */
    public static final int CAT_SOFT = 1;

    private View mRootView;
    public PullToLoadView mPullToLoadView;
    private CategoryAdapter mAdapter;

    /**
     * 当前是什么分类
     */
    private int mCatType;
    private boolean isRefresh;
    private boolean isVisible;
    private boolean isSelf = true;

    public static CategoryFragment newInstanceForGame() {
        CategoryFragment categoryFragment = new CategoryFragment();
        Bundle data = new Bundle();
        data.putInt(Constant.EXTRA_TYPE, CategoryFragment.CAT_GAME);
        categoryFragment.setArguments(data);
        return categoryFragment;
    }

    public static CategoryFragment newInstanceForSoft() {
        CategoryFragment categoryFragment = new CategoryFragment();
        Bundle data = new Bundle();
        data.putInt(Constant.EXTRA_TYPE, CategoryFragment.CAT_SOFT);
        categoryFragment.setArguments(data);
        return categoryFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) mCatType = getArguments().getInt(Constant.EXTRA_TYPE);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_category, container, false);
            init();
            requestData();
        }
        if (getUserVisibleHint()) {
            setPageAndUploadPageEvent();
        }
        return mRootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mRootView != null && (getParentFragment() != null && getParentFragment().getUserVisibleHint())) {
            LogUtils.i("iii", "分类setUserVisibleHint走了" + isVisibleToUser);
            setPageAndUploadPageEvent();
            ((MainActivity) mContext).mHeadView.setPageName(getPageAlias());
            requestData();
        }
    }

    /**
     * 设置页面并上报
     */
    private void setPageAndUploadPageEvent() {
        isVisible = true;  //用于重新获得焦点时。
        if (mCatType == CAT_SOFT) {
            setmPageAlias(Constant.PAGE_SOFT_CATEGORY);
        } else {
            setmPageAlias(Constant.PAGE_GAME_CATEGORY);
        }
        statEvent();
    }

    private void init() {
        mPullToLoadView = (PullToLoadView) mRootView.findViewById(R.id.pullToLoadView);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPullToLoadView.showLoading();
                requestData();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mAdapter = new CategoryAdapter(getActivity(), getPageAlias());
        mPullToLoadView.setLayoutManager(layoutManager);
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.showLoading();
        mPullToLoadView.setNoFooter();
        mPullToLoadView.addCustomHeader(createHeaderView());
        mPullToLoadView.setRefreshable(false);  //设置不可以下拉。
    }

    private void updateView(List<AppCatBean> catBeanList) {
        if (catBeanList == null) return;
        List<Item> itemList = new ArrayList<>();
        for (int i = 0; i < catBeanList.size(); i++) {
            List<AppCatBean> catItemBean = new ArrayList<>();
            catItemBean.add(catBeanList.get(i));
            i++;
            if (i < catBeanList.size()) {
                catItemBean.add(catBeanList.get(i));
            }
            itemList.add(getCategoryItem(catItemBean));
        }
        mAdapter.setList(itemList);
    }

    private Item getCategoryItem(List<AppCatBean> catBean) {
        Item categoryItem = new Item(CategoryAdapter.TYPE_CATEGORY_TWO, catBean);
        return categoryItem;
    }


    void requestData() {
        if (!NetWorkUtils.isConnected(getContext())) {
            isRefresh = refresh(0, isRefresh);
            ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);
            return;
        }

        if (mCatType == CAT_SOFT) {
            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppCatBean>>() {
                @Override
                public void onResponse(Call<List<AppCatBean>> call, Response<List<AppCatBean>> response) {
                    List<AppCatBean> catBeanList = response.body();
                    updateView(catBeanList);
                    mPullToLoadView.showContent();
                    isRefresh = refresh(1, isRefresh);
                    if (catBeanList == null || catBeanList.size() == 0) {
                        mPullToLoadView.showEmpty();
                    }
                }

                @Override
                public void onFailure(Call<List<AppCatBean>> call, Throwable t) {
                    isRefresh = refresh(0, isRefresh);
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                }
            }).bulid().requestSoftWareCatsList();
        } else {
            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppCatBean>>() {
                @Override
                public void onResponse(Call<List<AppCatBean>> call, Response<List<AppCatBean>> response) {
                    List<AppCatBean> catBeanList = response.body();
                    updateView(catBeanList);
                    mPullToLoadView.showContent();
                    isRefresh = refresh(1, isRefresh);
                }

                @Override
                public void onFailure(Call<List<AppCatBean>> call, Throwable t) {
                    isRefresh = refresh(0, isRefresh);
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                }
            }).bulid().requestGameCatsList();
        }
    }


    private View createHeaderView() {
        View view = new View(getContext());
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(getContext(), 8)));
        view.setBackgroundResource(R.color.grey_bg);
        return view;
    }

    @Override
    public void setPageAlias() {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}