package cn.lt.android.main.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.AdFilter;
import cn.lt.android.ads.AdService;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.main.AppAdapter;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.framework.util.ToastUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by linjunsheng on 2016/7/11.
 * 智能列表界面
 */
public class SmartListActivity extends BaseAppCompatActivity implements IrefreshAndLoadMoreListener {
    /**
     * 智能列表id
     */
    private String mId;

    private int mCurrentPage = GlobalConfig.FIRST_PAGE;

    Handler mHandler = LTApplication.getMainThreadHandler();
    private boolean isRefresh;
    private LinearLayoutManager layoutManager;
    private int mLastpage;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_SAMRT_LIST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulltoloadview);
        setStatusBar();
        mId = getIntent().getStringExtra(Constant.EXTRA_ID);
        initialize();

        requestList(mCurrentPage);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private AppAdapter mAdapter;
    private PullToLoadView mPullToLoadView;
    private ActionBar mActionBar;


    private void initialize() {
        mPullToLoadView = (PullToLoadView) findViewById(R.id.pullToLoadView);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setTitle("智能列表");
        layoutManager = new LinearLayoutManager(this);
        mPullToLoadView.setLayoutManager(layoutManager);
        mAdapter = new AppAdapter(this, getPageAlias(), mId, "智能列表");
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.addCustomHeader(createHeaderView());
       mPullToLoadView.setOnRefreshAndLoadListener(this);
    }


    private void requestList(int page) {
        if (!NetWorkUtils.isConnected(this)) {
            mPullToLoadView.setRefreshStopAndConfirmResult(false);
            isRefresh = refresh(0, isRefresh);
            ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);
            return;
        }
        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
            // 请求第一页时，要清空已展示的广告名单
            AdService.getInstance().getExistAdList(AdService.SMART_LIST_AD).clear();
        }

//        AdRequestBean bean = new AdRequestBean();
//        bean.startNum = mCurrentPage - 1;
//        bean.adCount = 10;
//        AdService.getInstance().fetchAd(AdService.SMART_LIST_AD, AdService.SMART_LIST_AD, bean);

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
            @Override
            public void onResponse(Call<List<AppDetailBean>> call, final Response<List<AppDetailBean>> response) {
                List<AppDetailBean> appDetailBeanList = response.body();
                if (appDetailBeanList == null || appDetailBeanList.size() == 0) {
                    mPullToLoadView.showEmpty();
                    return;
                } else {
                    final List<AppBriefBean> briefList = AppBeanTransfer.transferAppDetailList(appDetailBeanList);

                    // 去掉已安装的应用
                    new AdFilter().removeInstalled(briefList);

//                    LTApplication.instance.getMainThreadHandler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            AdService.getInstance().handleAd(AdService.SMART_LIST_AD, briefList, null);
//                            setDatas(response, briefList);
//                        }
//                    }, 100);


                }

            }

            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                //刷新和加载更多停止
                isRefresh = refresh(0, isRefresh);
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
                mPullToLoadView.setLoadStopAndConfirmResult(false);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                            mPullToLoadView.showErrorNotGoodNetwork();
                        } else {

                            ToastUtils.show(getApplicationContext(), R.string.get_data_failure);
                        }
                        //setPageWhileFailure();
                    }
                }, 500);
                //setPageWhileFailure();
            }
        }).bulid().requestSmartList(mId, page);

    }

    private void setDatas(Response<List<AppDetailBean>> response, List<AppBriefBean> briefList) {
        isRefresh = refresh(1, isRefresh);
        //下载数据初始化
        try {
            DownloadTaskManager.getInstance().transferBriefBeanList(briefList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //根据页码添加数据
        if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
            mAdapter.setList(briefList);
            layoutManager.scrollToPosition(0);
        } else {
            mAdapter.appendToList(briefList);
        }
        //刷新和加载更多停止
        mPullToLoadView.setRefreshStopAndConfirmResult(false);
        mPullToLoadView.setLoadStopAndConfirmResult(false);
        mPullToLoadView.showContent();

        //  第一次请求，最后一页就是固定的
        mLastpage = Constant.getLastPage(response.headers());
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


    private View createHeaderView() {
        View view = new View(this);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(this, 8)));
        return view;
    }

    /**
     * 判断是否还有下一页
     * @param lastPage
     * @return
     */
    private boolean hasNextPage(int lastPage) {
        if (mCurrentPage <=lastPage) {
            return true;
        }
        return false;
    }


    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
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

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar.setPageName(getPageAlias());
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        mCurrentPage = GlobalConfig.FIRST_PAGE;
                    requestList(mCurrentPage);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        mCurrentPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        LogUtils.i("aaa", "mLastpage==>"+mLastpage+"====mCurrentPage==>"+mCurrentPage+"====hasMoreData==>"+hasMoreData);
        if(hasMoreData){
            requestList(mCurrentPage);
        }else{
            mPullToLoadView.setHasNextPage2ShowFooter();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "SmartListActivity中取消了==请求码"+requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }
}
