package cn.lt.android.main.rank;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.base.DefaultFragmentPagerAdapter;
import cn.lt.android.entity.TabTopicBean;
import cn.lt.android.event.TabClickEvent;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.software.RankListFragment;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/***
 * 排行
 */
public class RankPortalFragment extends BaseFragment {
    private View mRootView;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    private RankListFragment mRankSoft = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_RANK_SOLF);
    private RankListFragment mRankGame = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_RANK_GAME);
    private LoadingLayout mLoadingLayout;
    private List<String> titleList = new ArrayList<>();
    private List<BaseFragment> fragments = new ArrayList<>();

    public static RankPortalFragment newInstance(String tab) {
        RankPortalFragment fragment = new RankPortalFragment();
        Bundle args = new Bundle();
        args.putString(BaseFragment.FRAGMENT_TAB, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_rank_entry, container, false);
            init();
            requestData("summary");
        }
        return mRootView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && fragments.size() > 0) {
            fragments.get(mViewPager.getCurrentItem()).setUserVisibleHint(true);
        }
    }

    private void init() {
        mTabs = (PagerSlidingTabStrip) mRootView.findViewById(R.id.tabs);
        mViewPager = (ViewPager) mRootView.findViewById(R.id.vp_fragment_rank_entry);
        mLoadingLayout = (LoadingLayout) mRootView.findViewById(R.id.rank_total_loadinglayout);
        mLoadingLayout.showLoading();
        mLoadingLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingLayout.showLoading();
                requestData("summary");
            }
        });
    }


    @Override
    public void setPageAlias() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.i("Jumper", "onDestroyView");
        LTApplication.instance.jumpIsFromMainActivity = false;
    }

    /**
     * 接收tab双击事件
     *
     * @param event
     */
    public void onEventMainThread(TabClickEvent event) {
        if (MainActivity.KEY_PAGE_PORTAL_RANK.equals(event.tabName)) {
            LogUtils.i("jkl", "收到了：");
            int currentItem = mViewPager.getCurrentItem();
            switch (currentItem) {
                case 0:
                    mRankSoft.mPullToLoadView.goBackToTopAndRefresh();
                    break;
                case 1:
                    mRankGame.mPullToLoadView.goBackToTopAndRefresh();
                    break;
            }
        }
    }

    /**
     * 请求title
     *
     * @param mFramentTab
     */
    void requestData(String mFramentTab) {
        if (!NetWorkUtils.isConnected(getContext())) {
            mLoadingLayout.showLoading();  //只在没网时show，避免出现两次转圈
            ShowRefreshLoadingUtils.showLoadingForNoNet(mLoadingLayout);    //此处还有点问题，前后状态不一直致。
            return;
        }
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<TabTopicBean>>() {
            @Override
            public void onResponse(Call<List<TabTopicBean>> call, Response<List<TabTopicBean>> response) {
                List<TabTopicBean> tabList = response.body();
                mLoadingLayout.showContent();   //防止没有覆盖其它视图
                if (tabList != null && tabList.size() > 0) {
                    updateView(tabList);
                }
            }

            @Override
            public void onFailure(Call<List<TabTopicBean>> call, Throwable t) {
                ShowRefreshLoadingUtils.showLoadingForNotGood(mLoadingLayout);
            }
        }).bulid().requestRankName(mFramentTab);
    }

    /**
     * 确定标题及Fragment
     *
     * @param tabTopicBeanList
     */
    public void updateView(List<TabTopicBean> tabTopicBeanList) {
        if (tabTopicBeanList == null || tabTopicBeanList.size() == 0) return;
        if (tabTopicBeanList.size() <= 1) {
            mTabs.setVisibility(View.GONE);
        } else {
            titleList.clear();
            fragments.clear();
            fragments.add(mRankSoft);
            fragments.add(mRankGame);
            for (int i = 0; i < tabTopicBeanList.size(); i++) {
                TabTopicBean tagBean = tabTopicBeanList.get(i);
                titleList.add(tagBean.getTitle());
            }
        }
        mViewPager.setAdapter(new DefaultFragmentPagerAdapter(getChildFragmentManager(), fragments, titleList));
        mTabs.setViewPager(mViewPager);
    }
}
