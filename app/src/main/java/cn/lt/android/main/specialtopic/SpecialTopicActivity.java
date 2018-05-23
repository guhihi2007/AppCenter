package cn.lt.android.main.specialtopic;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.entity.SpecialTopicBean;
import cn.lt.android.main.UIController;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ToastUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by LinJunSheng on 2016/3/1.
 * 专题列表页
 */
public class SpecialTopicActivity extends BaseAppCompatActivity implements IrefreshAndLoadMoreListener {

    private PullToLoadView pullToLoadView;
    private SpecailTopicAdapter adapter;
    private String activityType;
    private int curPage = 1;

    private List<SpecialTopicBean> STList;

    public static final String GAME = "game";
    public static final String SOFTWARE = "software";
    public static final String DEFAULT = "";
    private boolean isRefresh;
    private ActionBar mActionBar;
    private LinearLayoutManager layoutManager;
    private int mLastpage;


    @Override
    public void setPageAlias() {
        if (Constant.SPECIALTOP_TYPE_SOFT.equals(activityType)) {
            setmPageAlias(Constant.PAGE_SOFT_SPECIAL);
        } else if (Constant.SPECIALTOP_TYPE_GAME.equals(activityType)) {
            setmPageAlias(Constant.PAGE_GAMES_SPECIAL);
        } else {
            setmPageAlias(Constant.PAGE_SPECIAL);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_topic);
        getActivityType();
        setStatusBar();
        initView();
        addListener();
        requestData(true);
    }

    private void initView() {
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        if (activityType.equals(Constant.SPECIALTOP_TYPE_SOFT)) {
            mActionBar.setTitle("软件专题");
        } else if (activityType.equals(Constant.SPECIALTOP_TYPE_GAME)) {
            mActionBar.setTitle("游戏专题");
        } else {
            mActionBar.setTitle("专题");
        }
        mHandler = new Handler();
        pullToLoadView = (PullToLoadView) findViewById(R.id.pullToLoadView);
        layoutManager = new LinearLayoutManager(this);
        pullToLoadView.setLayoutManager(layoutManager);

        adapter = new SpecailTopicAdapter(this);
        pullToLoadView.setAdapter(adapter);
        pullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData(false);
                pullToLoadView.showLoading();
            }
        });
    }

    private void getActivityType() {
        if (getIntent() != null) {
            activityType = getIntent().getStringExtra("activityType");
        }
    }

    private void addListener() {
        pullToLoadView.setOnRefreshAndLoadListener(this);
        adapter.setOnSpecailTopicItemClickListener(new SpecailTopicAdapter.OnSpecailTopicItemClickListener() {
            @Override
            public void onItemClick(String topicId, String title) {
                LogUtils.i("dianId", "topic = " + topicId);
                UIController.goSpecialDetail(SpecialTopicActivity.this, topicId, title, SpecialTopicActivity.this.getPageAlias(), false, false);
            }


        });
    }

    Handler mHandler = new Handler();

    /**
     *请求网络
     * @param isFromOnCreate  true:第一次请求数据，不显示头布局。  false：其余情况下请求网络
     * */
    private void requestData(final boolean isFromOnCreate) {
        if (!NetWorkUtils.isConnected(this)) {
            if(!isFromOnCreate){
                pullToLoadView.setRefreshStopAndConfirmResult(false);
                pullToLoadView.setLoadStopAndConfirmResult(false);
            }else{
                isRefresh = refresh(0, isRefresh);
                ShowRefreshLoadingUtils.showLoadingForNoNet(pullToLoadView);  //只有第一次才显示无网络图
            }
            return;
        }
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<SpecialTopicBean>>() {
            @Override
            public void onResponse(Call<List<SpecialTopicBean>> call, Response<List<SpecialTopicBean>> response) {
                isRefresh = refresh(1, isRefresh);
                if(!isFromOnCreate) {
                    pullToLoadView.setRefreshStopAndConfirmResult(true);
                    pullToLoadView.setLoadStopAndConfirmResult(true);
                }
                if (response != null && response.body() != null) {
                    STList = response.body();
                    if (STList.size() != 0) {
                        setData(response);
//                        adapter.setHasMoreData(hasNextPage(response));
                    } else {
                        showEmpty();
                    }

                } else {
                    showEmpty();
                }
            }

            @Override
            public void onFailure(Call<List<SpecialTopicBean>> call, Throwable t) {
                isRefresh = refresh(0, isRefresh);
                if(!isFromOnCreate){
                    pullToLoadView.setRefreshStopAndConfirmResult(false);
                    pullToLoadView.setLoadStopAndConfirmResult(false);
                }
                if (curPage == 1) {
                    ShowRefreshLoadingUtils.showLoadingForNotGood(pullToLoadView);
                } else {
                    ToastUtils.show(getApplicationContext(), R.string.get_data_failure);
                }
            }
        }).bulid().requestSpecialTopicsList(curPage, activityType);
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
            if (curPage < lastPage) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setData(Response<List<SpecialTopicBean>> response) {
        if (curPage == 1) {
            adapter.setList(STList);
        } else {
            adapter.appendToList(STList);
        }
        pullToLoadView.showContent();
        //  第一次请求，最后一页就是固定的
        mLastpage = Constant.getLastPage(response.headers());
    }

    private void showNetworkFault() {
        pullToLoadView.showErrorNoNetwork();
    }

    private void showNetworkNotGood() {
        pullToLoadView.showErrorNotGoodNetwork();
    }


    private void showEmpty() {
        if (curPage == 1) {
            pullToLoadView.setRefreshStopAndConfirmResult(false);
            pullToLoadView.showEmpty();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar.setPageName(getPageAlias());
    }

    /**
     * 判断是否还有下一页
     * @param lastPage
     * @return
     */
    private boolean hasNextPage(int lastPage) {
        if (curPage <=lastPage) {
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        curPage = 1;
        requestData(false);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        curPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        LogUtils.i("aaa", "mLastpage==>"+mLastpage+"====mCurrentPage==>"+curPage+"====hasMoreData==>"+hasMoreData);
        if(hasMoreData){
            requestData(false);
        }else{
            pullToLoadView.setHasNextPage2ShowFooter();
        }
    }
}
