package cn.lt.android.main.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.base.DefaultFragmentPagerAdapter;
import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.TabTopicBean;
import cn.lt.android.entity.TagBean;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.main.entrance.TouchManger;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.LazyPagerSlidingTabStrip;
import cn.lt.android.widget.LazyViewPager;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.android.widget.ScrollRelativeLayout;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wenchao on 2016/3/7.
 * 新品,必备-- tab--普通列表  公用
 */


public class NewAppActivity extends BaseAppCompatActivity {
//    private Handler mHandler = new Handler();
    private int mPageType;
    public static final int NEWAPPS = 1;
    public static final int NECESSARY = 2;
    public static final int NORMAL = 0;
    private String mNormalTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_necessary);
        mId = getIntent().getStringExtra(Constant.EXTRA_ID);
        mPageType = getIntent().getIntExtra(Constant.EXTRA_TYPE, 0);
        mNormalTitle = getIntent().getStringExtra(Constant.EXTRA_NORMAL_TITLE);
        setStatusBar();
        assignViews();
        requestData();
    }

    @Override
    public void setPageAlias() {
        if (NECESSARY == mPageType) {
            LogUtils.i("zzz", "上报装机必备");
//            setmPageAlias(Constant.PAGE_NECESSARY);
        } else if (NEWAPPS == mPageType) {
            LogUtils.i("zzz", "上报新品");
//            setmPageAlias(Constant.PAGE_NEW);
        }
    }



    private PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    public ActionBar mActionBar;
    private ScrollRelativeLayout mScrollView;
    private LoadingLayout mLoadingLayout;
    private TouchManger mAnimationManger;
    private List<BaseFragment> fragments = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private String mId;


    private void assignViews() {
        LogUtils.i(NewAppActivity.class.getSimpleName(), "新品页");
        mScrollView = (ScrollRelativeLayout) findViewById(R.id.root_srcoll_solf);
        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setPadding(0, 0, 0, -DensityUtil.dip2px(this, 1));  //解决白色线条bug
        mActionBar.setBackgroundColor(getResources().getColor(R.color.tool_bar_color));

        //重新请求数据
        mLoadingLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingLayout.showLoading();
                requestData();
            }
        });
        mLoadingLayout.showLoading();
    }

    void createFragment(String title, String id, int position,int count) {
        titleList.add(title);
        SingleNecessaryFragment necessaryFragment = SingleNecessaryFragment.newInstance(title, id, position,count);
        fragments.add(necessaryFragment);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return (mAnimationManger != null && mAnimationManger.onEvent(ev)) || super.dispatchTouchEvent(ev);
    }

    private void initAnimation() {
        if (mAnimationManger == null) {
            mAnimationManger = new TouchManger(this, mScrollView, mActionBar).init();
        }
    }



    void requestData() {
        if (!NetWorkUtils.isConnected(this)) {
            ShowRefreshLoadingUtils.showLoadingForNoNet(mLoadingLayout);
            return;
        }
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<TabTopicBean>() {
            @Override
            public void onResponse(Call<TabTopicBean> call, Response<TabTopicBean> response) {
                mLoadingLayout.showContent();
                TabTopicBean bean = response.body();
                //刷新和加载更多停止
                if (bean != null) {
                    LogUtils.i("NewAppActivity", "bean：" + bean.toString());
                    updateView(bean);
//                    statEventForSingle(bean.getTitle(), mId); //这里产品不要求统计
                } else {
                    mLoadingLayout.showErrorNotGoodNetwork();
                }
            }

            @Override
            public void onFailure(Call<TabTopicBean> call, Throwable t) {
                ShowRefreshLoadingUtils.showLoadingForNotGood(mLoadingLayout);
            }
        }).bulid().requestTabTopics(mId);
    }

    DefaultFragmentPagerAdapter pagerAdapter;

    void updateView(TabTopicBean tabTopicBean) {
        if (mPageType == NORMAL) {
            //兼容普通列表
            TagBean tagBean = new TagBean();
            tagBean.setId(mId);
            tagBean.setTitle(mNormalTitle);
            tabTopicBean.setApplists(new ArrayList<TagBean>());
            tabTopicBean.getApplists().add(tagBean);
        }

        if (tabTopicBean.getApplists() == null || tabTopicBean.getApplists().size() == 0) {
            return;
        }

        if (tabTopicBean.getApplists() == null || tabTopicBean.getApplists().size() <= 1) {
            mTabs.setVisibility(View.GONE);
            mScrollView.setPadding(mScrollView.getPaddingLeft(), mScrollView.getPaddingTop(), mScrollView.getPaddingRight(), 0);
        } else {
            FrameLayout.LayoutParams para = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            para.setMargins(0, 0, 0, -DensityUtil.dip2px(this, 50));
            mScrollView.setLayoutParams(para);
            initAnimation();
        }

        if (mPageType == NORMAL) {
            mActionBar.setTitle(tabTopicBean.getApplists().get(0).getTitle());
        } else {
            mActionBar.setTitle(tabTopicBean.getTitle());
        }

        titleList.clear();
        fragments.clear();
//        tabTopicBean.getApplists().remove(0);
//        tabTopicBean.getApplists().remove(0);
        for (int i = 0; i < tabTopicBean.getApplists().size(); i++) {
            TagBean tagBean = tabTopicBean.getApplists().get(i);
            createFragment(tagBean.getTitle(), tagBean.getId(), 0,tabTopicBean.getApplists().size());
        }

        pagerAdapter = new DefaultFragmentPagerAdapter(getSupportFragmentManager(), fragments, titleList);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mTabs.setViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewPager != null && pagerAdapter != null){
            pagerAdapter.setFragment(mViewPager.getCurrentItem());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "NewAppActivity中取消了==请求码" + requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }
}
