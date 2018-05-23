package cn.lt.android.main.software;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.base.DefaultFragmentPagerAdapter;
import cn.lt.android.entity.TabTopicBean;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.android.widget.RankTabInnerViewPager;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/***
 * Created by dxx on 2016/3/3.
 */
public class RankRootFragment extends BaseFragment {
    public static final String FRAGMENT_TAB_SOLF = "tab_solf";
    public static final String FRAGMENT_TAB_GAME = "tab_game";
    private View mRootView;
    private PagerSlidingTabStrip mTabs;
    public RankTabInnerViewPager mViewPager;
    private String mFramentTab;
    public RankListFragment mRankListSoftHotFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_SOFT_HOT);
    public RankListFragment mRankListSoftMonthFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_SOFT_MONTH);
    public RankListFragment mRankListSoftTotalFragment;
    public RankListFragment mRankListGameOfflineFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_GAME_OFFLINE);
    public RankListFragment mRankListGameOnlineFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_GAME_ONLINE);
    public RankListFragment mRankListGameTotalFragment;

    private List<BaseFragment> fragments = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private LoadingLayout mRecyclerView;
    private boolean isVisible;

    public static RankRootFragment newInstance(String tab) {
        RankRootFragment fragment = new RankRootFragment();
        Bundle args = new Bundle();
        args.putString(BaseFragment.FRAGMENT_TAB, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogUtils.i("RankRootFragment", "onCreate走了");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        LogUtils.i("RankRootFragment", "onDetach走了");
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        LogUtils.i("iii", "跟榜单onCreateView走了"+getUserVisibleHint());
        mFramentTab = getArguments().getString(BaseFragment.FRAGMENT_TAB);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_rank_root, container, false);
            initView();
            requestData(mFramentTab);
            LogUtils.i("RankRootFragment", "正常");
        }
        if (getUserVisibleHint()) setPageAndUploadPageEvent(false);
        return mRootView;
    }

    /**
     * 优先于onCreateView执行，如果直接别的地方跳过来，会导致没有执行initView();
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) LogUtils.i("iii", "跟榜单setUserVisibleHint走了" + isVisibleToUser);
        if (isVisibleToUser && mRootView != null) {
            setPageAndUploadPageEvent(false);
            if (fragments.size() > 0) {
                // 异步请求成功之后fragments 才添加
                fragments.get(mViewPager.getCurrentItem()).setUserVisibleHint(true);
            }

        }

    }

    /**
     * 根据目前选中的哪个tab，设置子类要上报的pageName，上报统计，请求页面数据
     *
     * @param isCallback
     */
    private void setPageAndUploadPageEvent(boolean isCallback) {
        isVisible = true;  //用于重新获得焦点时。
        EventBus.getDefault().post("isNotVisible");
        if (TextUtils.isEmpty(mFramentTab) || FRAGMENT_TAB_GAME.equals(mFramentTab)) {
            if (isCallback) {
                setGameRankPage();
            } else {
                if (mViewPager == null) {
//                    setmPageAlias(Constant.PAGE_GAME_OFFLINE_RANK);   //第一次默认是第一个
//                    LTApplication.instance.current_child_rank_page = Constant.PAGE_GAME_OFFLINE_RANK;
//                    mRankListGameOfflineFragment.requestData(true,false);
                } else {
                    setGameRankPage();
                }
            }
        } else if (FRAGMENT_TAB_SOLF.equals(mFramentTab)) {    /******************以下是软件榜单*************************/
            if (isCallback) {
                LogUtils.i("gaotie", "软件榜回调监听");
                setSoftChildRankPage();
            } else {
                if (mViewPager == null) {  //这个也是公用的，就麻烦啦。
//                    setmPageAlias(Constant.PAGE_SOFT_HOT_RANK);   //第一次默认是第一个，若
//                    LTApplication.instance.current_child_rank_page = Constant.PAGE_SOFT_HOT_RANK;
//                    mRankListSoftHotFragment.requestData(true,false);
                } else {
                    setSoftChildRankPage();
                }
            }
        }
//        statEvent();
    }

    private void setGameRankPage() {
        switch (mViewPager.getCurrentItem()) {
            case 0:
//                setmPageAlias(Constant.PAGE_GAME_OFFLINE_RANK);
//                LTApplication.instance.current_child_rank_page=Constant.PAGE_GAME_OFFLINE_RANK;
//                mRankListGameOfflineFragment.requestData(true,false);
                break;
            case 1:
//                setmPageAlias(Constant.PAGE_GAME_LINE_RANK);
//                LTApplication.instance.current_child_rank_page=Constant.PAGE_GAME_LINE_RANK;
//                mRankListGameOnlineFragment.requestData(true,false);
                break;
        }
    }

    private void setSoftChildRankPage() {
        LogUtils.i("gaotie", "软件榜回调监听位置是：" + mViewPager.getCurrentItem());
        switch (mViewPager.getCurrentItem()) {
            case 0:
                setmPageAlias(Constant.PAGE_SOFT_HOT_RANK);
                break;
            case 1:
                setmPageAlias(Constant.PAGE_SOFT_MONTH_RANK);
                break;
        }
    }

    private void initView() {
        mTabs = (PagerSlidingTabStrip) mRootView.findViewById(R.id.tabs);
        mViewPager = (RankTabInnerViewPager) mRootView.findViewById(R.id.rank_root);
        mRecyclerView = (LoadingLayout) mRootView.findViewById(R.id.rcv_recommend);
        mRecyclerView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData(mFramentTab);
            }
        });
        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                setPageAndUploadPageEvent(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    @Override
    public void setPageAlias() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i("Jumper", "榜单根onDestroy走了");
        LTApplication.instance.jumpIsFromMainActivity = false;
    }

    /**
     * 根据不同的Tab请求不同的title===还是要显示无网络图标
     */
    void requestData(String mFramentTab) {
        mRecyclerView.showLoading();  //只在没网时show，避免出现两次转圈
        if (!NetWorkUtils.isConnected(getContext())) {
            ShowRefreshLoadingUtils.showLoadingForNoNet(mRecyclerView);    //此处还有点问题，前后状态不一直致。
            return;
        }
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<TabTopicBean>>() {
            @Override
            public void onResponse(Call<List<TabTopicBean>> call, Response<List<TabTopicBean>> response) {
                List<TabTopicBean> tabList = response.body();
                mRecyclerView.showContent();   //防止没有覆盖其它视图
                if (tabList != null) {
                    updateView(tabList);
                    if (getUserVisibleHint()) {
                        LogUtils.e("iii", "async back visible");
                        fragments.get(mViewPager.getCurrentItem()).setUserVisibleHint(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TabTopicBean>> call, Throwable t) {
                LogUtils.i("nnn", "失败了：");
                ShowRefreshLoadingUtils.showLoadingForNotGood(mRecyclerView);
            }
        }).bulid().requestRankName(getRequestPort(mFramentTab));
    }

    private String getRequestPort(String mFramentTab) {
        switch (mFramentTab) {
            case FRAGMENT_TAB_SOLF:
                return "software";
            case FRAGMENT_TAB_GAME:
                return "game";
            default:
                break;
        }
        return "";
    }

    /**
     * 确定标题及Fragment
     *
     * @param tabTopicBeanList
     */
    public void updateView(List<TabTopicBean> tabTopicBeanList) {
        if (tabTopicBeanList == null || tabTopicBeanList.size() == 0) return;
        if (tabTopicBeanList == null || tabTopicBeanList.size() <= 1) {
            mTabs.setVisibility(View.GONE);
        } else {
            titleList.clear();
            fragments.clear();
            //加fragment
            if (TextUtils.isEmpty(mFramentTab) || FRAGMENT_TAB_GAME.equals(mFramentTab)) {
                mViewPager.setId(R.id.rank_root_game);
//                mRankListGameOfflineFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_GAME_OFFLINE);
//                mRankListGameOnlineFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_GAME_ONLINE);
                fragments.add(mRankListGameOfflineFragment);
                fragments.add(mRankListGameOnlineFragment);
            } else if (FRAGMENT_TAB_SOLF.equals(mFramentTab)) {
                mViewPager.setId(R.id.rank_root_solf);
//                mRankListSoftHotFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_SOFT_HOT);
//                mRankListSoftMonthFragment = RankListFragment.newInstance(RankListFragment.FRAGMENT_TAB_SOFT_MONTH);
                fragments.add(mRankListSoftHotFragment);
                fragments.add(mRankListSoftMonthFragment);
            }
            //目前就是加标题
            for (int i = 0; i < tabTopicBeanList.size(); i++) {
                TabTopicBean tagBean = tabTopicBeanList.get(i);
                LogUtils.d("RankRoot", "标题：=>" + tagBean.getTitle());
                createFragment(tagBean.getTitle());
            }
        }
        mViewPager.setAdapter(new DefaultFragmentPagerAdapter(getChildFragmentManager(), fragments, titleList));
        mTabs.setViewPager(mViewPager);
    }


    public void createFragment(String title) {
        titleList.add(title);
//        fragments.add(RankListFragment.newInstance(title,id, position));   //接口改为公用的就用
    }


}
