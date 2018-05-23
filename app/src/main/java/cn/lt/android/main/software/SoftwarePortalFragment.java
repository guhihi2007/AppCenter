package cn.lt.android.main.software;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.base.DefaultFragmentPagerAdapter;
import cn.lt.android.event.TabClickEvent;
import cn.lt.android.main.MainActivity;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.android.widget.RankTabInnerViewPager;
import cn.lt.android.widget.ScrollRelativeLayout;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * 软件
 */
public class SoftwarePortalFragment extends BaseFragment {

    private View mRootView;
    private ScrollRelativeLayout mScrollView;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    private int mCurrTab;
    private int mTemp;
    private HighlySelectiveFragment softHighlyFragment;
    private RankRootFragment rankRootFragment;
    private CategoryFragment categoryFragment;

    List<BaseFragment> fragments;

    public static SoftwarePortalFragment newInstance(String tab) {
        SoftwarePortalFragment fragment = new SoftwarePortalFragment();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCurrTab = getActivity().getIntent().getIntExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB, 0);
        getActivity().getIntent().removeExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB);
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_software_entry, container, false);
            initView();
            initViewPage();
        }

       /* Integer temp = LTApplication.instance.mJumpDestroyIndexMap.get("SoftwarePortalFragment");
        if( temp !=null){
            mTemp  = temp;
        }*/

        // 從MainActvity跳过来则不让执行
        if (!LTApplication.instance.jumpIsFromMainActivity) {
            mViewPager.setCurrentItem(mTemp);
        }

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.i("Jumper", "onDestroyView：" + getClass().getCanonicalName());
        LTApplication.instance.jumpIsFromMainActivity = false;
    }

    @Override
    public void onDestroy() {
        LogUtils.i("Jumper", "onDestroy：" + getClass().getCanonicalName());
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView() {
        mTabs = (PagerSlidingTabStrip) mRootView.findViewById(R.id.tabs);
        mViewPager = (ViewPager) mRootView.findViewById(R.id.softViewPager);
        mScrollView = (ScrollRelativeLayout) mRootView.findViewById(R.id.root_srcoll_solf);

        //第一次创建fragment默认设置
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(mScrollView.getLayoutParams());
        layoutParam.setMargins(0, 0, 0, (DensityUtil.dip2px(mContext, 54)));
        mScrollView.setLayoutParams(layoutParam);

        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(mScrollView.getLayoutParams());
                    layoutParam.setMargins(0, 0, 0, (DensityUtil.dip2px(mContext, 4)));
                    mScrollView.setLayoutParams(layoutParam);
                } else {
                    LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(mScrollView.getLayoutParams());
                    layoutParam.setMargins(0, 0, 0, (DensityUtil.dip2px(mContext, 54)));
                    mScrollView.setLayoutParams(layoutParam);
                    LogUtils.i("Jumper", "刚上来会切换选中" + position);
                }
                LogUtils.i("Jumper", "切换选中" + position);
                sendPortalPage(position);
                mTemp = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
//            onResume();
            switch (jumpTAG) {
                case Constant.PAGE_APP_CLASSIFY:
                    mTemp = 2;
                    break;
                case Constant.PAGE_APP_LIST:
                    mTemp = 1;
                    break;
                case Constant.PAGE_APP_CHOICE:
                    mTemp = 0;
                    break;
            }
            jumpTAG = "";

            LogUtils.i("iii", "底部软件onHiddenChanged走了");
            mViewPager.setCurrentItem(mTemp);

            // 手动调用子Fragment的显示方法
            fragments.get(mTemp).setUserVisibleHint(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (jumpTAG) {
            case Constant.PAGE_APP_CLASSIFY:
                mTemp = 2;
                break;
            case Constant.PAGE_APP_LIST:
                mTemp = 1;
                break;
            case Constant.PAGE_APP_CHOICE:
                mTemp = 0;
                break;
        }
        jumpTAG = "";

        LogUtils.i("iii", "底部软件onResume走了");
        mViewPager.setCurrentItem(mTemp);
    }


    /**
     * 目前处于哪个二级页面
     *
     * @param position
     */
    private void sendPortalPage(int position) {
        if (position == 0) {
            EventBus.getDefault().post("soft_jingxuan");
        } else if (position == 1) {
            EventBus.getDefault().post("soft_bangdan");
        } else {
            EventBus.getDefault().post("soft_fenlei");
        }
    }

    private void initViewPage() {
        List<String> titleList = new ArrayList<>();
        titleList.add("精选");
        titleList.add("榜单");
        titleList.add("分类");
        fragments = new ArrayList<>();
        softHighlyFragment = HighlySelectiveFragment.newInstance(HighlySelectiveFragment.FRAGMENT_TAB_SOFT);
        rankRootFragment = RankRootFragment.newInstance(RankRootFragment.FRAGMENT_TAB_SOLF);
        categoryFragment = CategoryFragment.newInstanceForSoft();
        fragments.add(softHighlyFragment);
        fragments.add(rankRootFragment);
        fragments.add(categoryFragment);
        mViewPager.setAdapter(new DefaultFragmentPagerAdapter(getChildFragmentManager(), fragments, titleList));
        mTabs.setViewPager(mViewPager);

//        onHiddenChanged(false);
    }

    @Override
    public void setPageAlias() {
    }



    public static String jumpTAG = "";

    /**
     * 接收tab双击事件
     *
     * @param event
     */
    public void onEventMainThread(TabClickEvent event) {
        if (MainActivity.KEY_PAGE_PORTAL_SOLF.equals(event.tabName)) {
            LogUtils.i("jkl", "收到了：");
            int currentItem = mViewPager.getCurrentItem();
            switch (currentItem) {
                case 0:
                    softHighlyFragment.mPullToLoadView.goBackToTopAndRefresh();
                    break;
                case 1:
                    performRankListToTop(rankRootFragment.mViewPager);
                    break;
                case 2:
//                    categoryFragment.mPullToLoadView.goBackToTopAndRefresh();
                    break;
            }
        }
    }

    private void performRankListToTop(RankTabInnerViewPager viewPager) {
        int currentItem = viewPager.getCurrentItem();
        switch (currentItem) {
            case 0:
                rankRootFragment.mRankListSoftHotFragment.mPullToLoadView.goBackToTopAndRefresh();
                break;
            case 1:
                rankRootFragment.mRankListSoftMonthFragment.mPullToLoadView.goBackToTopAndRefresh();
                break;
            case 2:
                rankRootFragment.mRankListSoftTotalFragment.mPullToLoadView.goBackToTopAndRefresh();
                break;
        }
    }


}
