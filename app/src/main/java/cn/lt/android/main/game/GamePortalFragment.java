package cn.lt.android.main.game;

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
import cn.lt.android.main.software.CategoryFragment;
import cn.lt.android.main.software.HighlySelectiveFragment;
import cn.lt.android.main.software.RankRootFragment;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.android.widget.RankTabInnerViewPager;
import cn.lt.android.widget.ScrollRelativeLayout;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * 游戏
 */
public class GamePortalFragment extends BaseFragment {

    private View mRootView;
    private PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    private ScrollRelativeLayout mScrollView;
    private int mCurrTab = 0;
    private int mTemp;
    private HighlySelectiveFragment mGameHighly;
    private RankRootFragment mGameRoot;
    private CategoryFragment mGameCaregory;

    List<BaseFragment> fragments;

    public static GamePortalFragment newInstance(String tab) {
        GamePortalFragment fragment = new GamePortalFragment();
        Bundle args = new Bundle();
        args.putString(BaseFragment.FRAGMENT_TAB, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCurrTab = getActivity().getIntent().getIntExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB, 0);
        getActivity().getIntent().removeExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_game_entry, container, false);
            initView();
            initViewPage();
        }

        Integer temp = LTApplication.instance.mJumpDestroyIndexMap.get("GamePortalFragment");
        if(temp != null) {
            mTemp = temp;
        }

        if (!LTApplication.instance.jumpIsFromMainActivity) {
            mViewPager.setCurrentItem(mTemp);
        }
        return mRootView;
    }

    private void initView() {
        mTabs = (PagerSlidingTabStrip) mRootView.findViewById(R.id.tabs);
        mViewPager = (ViewPager) mRootView.findViewById(R.id.game_fragment_game);
        mScrollView = (ScrollRelativeLayout) mRootView.findViewById(R.id.root_srcoll_solf);

        //第一次创建fragment默认设置
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(mScrollView.getLayoutParams());
        layoutParam.setMargins(0, 0, 0, (DensityUtil.dip2px(mContext, 54)));
        mScrollView.setLayoutParams(layoutParam);
    }

    private void initViewPage() {
        fragments = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        titleList.add("精选");
        titleList.add("榜单");
        titleList.add("分类");
        mGameHighly = HighlySelectiveFragment.newInstance(HighlySelectiveFragment.FRAGMENT_TAB_GAME);
        mGameRoot = RankRootFragment.newInstance(RankRootFragment.FRAGMENT_TAB_GAME);
        mGameCaregory = CategoryFragment.newInstanceForGame();
        fragments.add(mGameHighly);
        fragments.add(mGameRoot);
        fragments.add(mGameCaregory);
        mViewPager.setAdapter(new DefaultFragmentPagerAdapter(getChildFragmentManager(), fragments, titleList));
        mTabs.setViewPager(mViewPager);

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
                }
                sendPortalPage(position);
                mTemp = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void setPageAlias() {}

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            switch (JumpTAG) {
                case Constant.PAGE_GAME_CLASSIFY:
                    mTemp = 2;
                    break;
                case Constant.PAGE_GAME_LIST:
                    mTemp = 1;
                    break;
                case Constant.PAGE_GAME_CHOICE:
                    mTemp = 0;
                    break;
            }
            JumpTAG = "";

            LogUtils.i("iii", "底部游戏onHiddenChanged走了");
            mViewPager.setCurrentItem(mTemp);
            fragments.get(mTemp).setUserVisibleHint(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (JumpTAG) {
            case Constant.PAGE_GAME_CLASSIFY:
                mTemp = 2;
                break;
            case Constant.PAGE_GAME_LIST:
                mTemp = 1;
                break;
            case Constant.PAGE_GAME_CHOICE:
                mTemp = 0;
                break;
        }
        JumpTAG = "";

        LogUtils.i("iii", "底部游戏onHiddenChanged走了");
        mViewPager.setCurrentItem(mTemp);
    }

    /**
     * 目前处于哪个二级页面
     * @param position
     */
    private void sendPortalPage(int position) {
        if(position==0){
            EventBus.getDefault().post("game_jingxuan");
        }else if(position==1){
            EventBus.getDefault().post("game_bangdan");
        }else {
            EventBus.getDefault().post("game_fenlei");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        LogUtils.i("Jumper", "onDestroy：" + getClass().getCanonicalName());
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

//    @Override
//    public void jumpToInternal(int type, int index) {
//        if (type == 2000) {
//            mViewPager.setCurrentItem(index);
//        }
//    }
    public static String JumpTAG = "";

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtils.i("Jumper", "onDestroyView：" + getClass().getCanonicalName());
        LTApplication.instance.jumpIsFromMainActivity=false;
    }

    /**
     * 接收tab双击事件
     * @param event
     */
    public void onEventMainThread(TabClickEvent event) {
        if (MainActivity.KEY_PAGE_PORTAL_GAME.equals(event.tabName)) {
            LogUtils.i("jkl", "收到了：" );
            int currentItem = mViewPager.getCurrentItem();
            switch (currentItem) {
                case 0:
                    mGameHighly.mPullToLoadView.goBackToTopAndRefresh();
                    break;
                case 1:
                    performRankListToTop(mGameRoot.mViewPager);
                    break;
                case 2:
//                    mGameCaregory.mPullToLoadView.goBackToTopAndRefresh();
                    break;
            }
        }
    }

    private void performRankListToTop(RankTabInnerViewPager viewPager) {
        int  currentItem=viewPager.getCurrentItem();
        switch (currentItem) {
            case 0:
                mGameRoot.mRankListGameOfflineFragment.mPullToLoadView.goBackToTopAndRefresh();
                break;
            case 1:
                mGameRoot.mRankListGameOnlineFragment.mPullToLoadView.goBackToTopAndRefresh();
                break;
            case 2:
                mGameRoot.mRankListGameTotalFragment.mPullToLoadView.goBackToTopAndRefresh();
                break;
        }
    }
}