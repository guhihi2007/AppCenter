package cn.lt.android.main.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.wanka.WanKaManager;
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
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.IrefreshAndLoadMoreListener;
import cn.lt.pullandloadmore.PullToLoadView;
import cn.lt.pullandloadmore.RefreshAndPullRecyclerView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wenchao on 2016/3/2.
 * 分类详情界面
 */
public class CategoryDetailActivity extends BaseAppCompatActivity implements IrefreshAndLoadMoreListener {

    public final static String TYPE_SOFTWARE = "software";
    public final static String TYPE_GAME = "game";

    /**
     * 当前所属分类类型
     */
    private String mCurrentType = TYPE_SOFTWARE;
    /**
     * 当前分类id
     */
    private String mCatId;
    /**
     * 当前分类的名称
     */
    private String mTitle;

    private int mCurrentPage = GlobalConfig.FIRST_PAGE;

    Handler mHandler = new Handler();
    private boolean isRefresh;

    private List<AppBriefBean> allDatas = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private int mLastpage;
    private boolean isFromOnCreate;
    private boolean hasNextPage;
    private boolean isFromRetry;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_CATEGORY_DETAIL, mCatId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulltoloadview);
        setStatusBar();
        mCurrentType = getIntent().getStringExtra(Constant.EXTRA_TYPE);
        LogUtils.i("Cate", "类型：" + mCurrentType);
        if (mCurrentType == null) {
            mCurrentType = TYPE_SOFTWARE;   //暂时这样改
        }
        mCatId = getIntent().getStringExtra(Constant.EXTRA_ID);
        mTitle = getIntent().getStringExtra(Constant.EXTRA_TITLE);
        initialize();

        requestList(mCurrentPage, true, false);
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
    private View mEmptyView;
    /**
     * 网络请求
     */
    private NetDataInterfaceDao mNetDataInterfaceDao;


    private void initialize() {
        mEmptyView = LayoutInflater.from(this).inflate(R.layout.view_empty_app_download, null);
        mEmptyView.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页去下载
                UIController.goHomePage(CategoryDetailActivity.this, MainActivity.PAGE_TAB_RECOMMEND, MainActivity.PAGE_TAB_GAME_SUB_INDEX);
            }
        });
        ((TextView) mEmptyView.findViewById(R.id.text)).setText(R.string.app_download_empty_tips);
        mPullToLoadView = (PullToLoadView) findViewById(R.id.pullToLoadView);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);

        mActionBar.setTitle(mTitle);
        layoutManager = new LinearLayoutManager(this);
        mPullToLoadView.setLayoutManager(layoutManager);
        mAdapter = new AppAdapter(this, getPageAlias(), mCatId, "分类列表");
        mPullToLoadView.setAdapter(mAdapter);
        mPullToLoadView.addCustomHeader(createHeaderView());
        mPullToLoadView.setOnRefreshAndLoadListener(this);
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = GlobalConfig.FIRST_PAGE;
                mPullToLoadView.showLoading();
                requestList(mCurrentPage, false, true);
            }
        });

        mNetDataInterfaceDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
            @Override
            public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                if (!isFromOnCreate && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(true);
                }
                mPullToLoadView.setLoadStopAndConfirmResult(true);  //此处暂时没有区别是否是第一次
                List<AppDetailBean> appDetailBeanList = response.body();
                if (appDetailBeanList == null || appDetailBeanList.size() == 0) {
                    mPullToLoadView.showEmpty2(mEmptyView);
                } else {
                    mPullToLoadView.showContent();
                }
                isRefresh = refresh(1, isRefresh);
                final List<AppBriefBean> briefList = AppBeanTransfer.transferAppDetailList(appDetailBeanList);

                // 玩咖数据曝光
//                WanKaManager.exposureApps(briefList, new SimpleResponseListener<JSONObject>() {
//                    @Override
//                    public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
//                        // 成功回调之后会修改briefList 集合里边的数据,直接notifyDataSetChanged
//
//                        // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
//                        new DownloadAdAppReplacer().replaceByAppBriefList(briefList);
//                        mAdapter.notifyDataSetChanged();
//                    }
//                },"分类曝光");

                WanKaManager.exposureApps(briefList, null,"分类曝光");

                //下载数据初始化
                try {
                    DownloadTaskManager.getInstance().transferBriefBeanList(briefList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
                new DownloadAdAppReplacer().replaceByAppBriefList(briefList);

                //根据页码添加数据
                if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                    allDatas.clear();
                    layoutManager.scrollToPosition(0);
                }
                allDatas.addAll(briefList);
                mAdapter.setList(allDatas);

                hasNextPage = hasNextPage(response);
                //  第一次请求，最后一页就是固定的
                mLastpage = Constant.getLastPage(response.headers());
                if (!hasNextPage) {
                    mPullToLoadView.setHasNextPage2ShowFooter();    //防止第一次请求没有设置底部
                }


            }

            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                isRefresh = refresh(0, isRefresh);
                setPageWhileFailure();
                mPullToLoadView.setLoadStopAndConfirmResult(false);
                if (!isFromOnCreate && !isFromRetry) {
                    mPullToLoadView.setRefreshStopAndConfirmResult(false);
                } else {
                    ShowRefreshLoadingUtils.showLoadingForNotGood(mPullToLoadView);
                }
            }
        }).bulid();
    }

    /**
     * 请求网络
     *
     * @param isFromOnCreate true:第一次请求数据，不显示头布局。  false：其余情况下请求网络
     * @param isFromRetry
     */
    private void requestList(int page, boolean isFromOnCreate, boolean isFromRetry) {
        this.isFromOnCreate = isFromOnCreate;
        this.isFromRetry = isFromRetry;
        if (!NetWorkUtils.isConnected(this)) {
            setPageWhileFailure();              //要不要区分是加载还是刷新？
            mPullToLoadView.setLoadStopAndConfirmResult(false);
            if (!isFromOnCreate && !isFromRetry) {
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
            } else {
                isRefresh = refresh(0, isRefresh);
                ShowRefreshLoadingUtils.showLoadingForNoNet(mPullToLoadView);  //只有第一次才显示无网络图
            }
            return;
        }

        if (TYPE_SOFTWARE.equals(mCurrentType)) {
            requestSoftwareList(page, isFromOnCreate);
        } else if (TYPE_GAME.equals(mCurrentType)) {
            requestGameList(page, isFromOnCreate);
        }
    }

    private void requestSoftwareList(final int page, boolean isFromOnCreate) {
        mNetDataInterfaceDao.requestSoftWareCatDetail(mCatId, page);
    }


    private void requestGameList(final int page, boolean isFromOnCreate) {
        mNetDataInterfaceDao.requestGameCatDetail(mCatId, page);
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

    /**
     * 判断是否还有下一页
     *
     * @param lastPage
     * @return
     */
    private boolean hasNextPage(int lastPage) {
        if (mCurrentPage <= lastPage) {
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar.setPageName(getPageAlias());

        new DownloadAdAppReplacer().replaceByAppBriefList(allDatas);
        mAdapter.setList(allDatas);
    }

    @Override
    public void onRefresh(RefreshAndPullRecyclerView refreshRecyclerView) {
        mCurrentPage = GlobalConfig.FIRST_PAGE;
        requestList(mCurrentPage, false, false);
    }

    @Override
    public void onLoadMore(IrefreshAndLoadMoreListener mListener) {
        mCurrentPage++;
        boolean hasMoreData = hasNextPage(mLastpage);
        LogUtils.i("aaa", "mLastpage==>" + mLastpage + "====mCurrentPage==>" + mCurrentPage + "====hasMoreData==>" + hasMoreData);
        if (hasMoreData) {
            requestList(mCurrentPage, false, false);
        } else {
            mPullToLoadView.setHasNextPage2ShowFooter();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "CategoryDetailActivity中取消了==请求码" + requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }
}
