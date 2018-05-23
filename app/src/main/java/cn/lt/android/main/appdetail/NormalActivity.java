package cn.lt.android.main.appdetail;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.main.AppAdapter;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ToastUtils;
import cn.lt.pullandloadmore.PullToLoadView;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * created at 5/5
 */
public class NormalActivity extends BaseAppCompatActivity {

    private PullToLoadView mPullToLoadView;
    private AppAdapter mAdapter;
    private int mCurrentPage = GlobalConfig.FIRST_PAGE;
    private boolean isRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        setStatusBar();
        EventBus.getDefault().register(this);
        mId = getIntent().getStringExtra(Constant.EXTRA_ID);
        mTitle = getIntent().getStringExtra(Constant.NORMAL_TITLE);
        assignViews();
        requestData(mCurrentPage);
    }

    private String mId, mTitle;
    private ActionBar mActionBar;


    private void assignViews() {
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        if (mTitle != null) {
            mActionBar.setTitle(mTitle);
        }
        mPullToLoadView = (PullToLoadView) findViewById(R.id.pullToLoadView);

        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(this);
        mPullToLoadView.setLayoutManager(mLayoutManger);
        mAdapter = new AppAdapter(this, getPageAlias(), mId, "普通列表");
        mPullToLoadView.setAdapter(mAdapter);

//        mPullToLoadView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mCurrentPage = GlobalConfig.FIRST_PAGE;
//                requestData(mCurrentPage);
//                isRefresh = true;
//            }
//        });
//        mPullToLoadView.setOnLoadMoreListener(new OnLoadMoreListener() {
//            @Override
//            public void onLoadMore() {
//                requestData(++mCurrentPage);
//            }
//        });
        mPullToLoadView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = GlobalConfig.FIRST_PAGE;
                requestData(mCurrentPage);
            }
        });
    }


    void requestData(int page) {
        if (!NetWorkUtils.isConnected(this)) {
            mPullToLoadView.setRefreshStopAndConfirmResult(false);
            if (isRefresh) {
                cn.lt.android.util.ToastUtils.showToast("刷新失败");
                isRefresh = false;
            }
            mPullToLoadView.showErrorNoNetwork();
            return;
        }
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
            @Override
            public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                List<AppDetailBean> appDetailBeanList = response.body();
                if (appDetailBeanList == null || appDetailBeanList.size() == 0) {
                    mPullToLoadView.showEmpty();
                    return;
                } else {
                    mPullToLoadView.showContent();
                }

                if (isRefresh) {
                    cn.lt.android.util.ToastUtils.showToast("刷新成功");
                    isRefresh = false;
                }
                //刷新和加载更多停止
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
                mPullToLoadView.setLoadStopAndConfirmResult(false);

                List<AppBriefBean> briefList = AppBeanTransfer.transferAppDetailList(appDetailBeanList);

                //下载数据初始化
                try {
                    DownloadTaskManager.getInstance().transferBriefBeanList(briefList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    return;// TODO:
                }

                //根据页码添加数据
                if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                    mAdapter.setList(briefList);
                } else {
                    mAdapter.appendToList(briefList);
                }

                //设置是否还有下一页
                mPullToLoadView.setHasNextPage2ShowFooter();

            }

            @Override
            public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                //刷新和加载更多停止
                if (isRefresh) {
                    cn.lt.android.util.ToastUtils.showToast("刷新失败");
                    isRefresh = false;
                }
                mPullToLoadView.setRefreshStopAndConfirmResult(false);
//                mPullToLoadView.setLoadStopAndConfirmResult(mContext, false);
                setPageWhileFailure();
                if (mCurrentPage == GlobalConfig.FIRST_PAGE) {
                    mPullToLoadView.showErrorNotGoodNetwork();
                } else {
                    ToastUtils.show(NormalActivity.this, R.string.get_data_failure);
                }
            }
        }).bulid().requestNormalList(mId, mCurrentPage);
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
    public void setPageAlias() {
        setmPageAlias(TextUtils.isEmpty(mTitle) ? Constant.PAGE_NORMAL_LIST : Constant.PAGE_NORMAL_PT_TITLE + mTitle, mId);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
