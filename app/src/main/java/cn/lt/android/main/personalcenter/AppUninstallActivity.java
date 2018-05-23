package cn.lt.android.main.personalcenter;

import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.entity.UninstallAppInfo;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;

/**
 * Created by yuanlei on 2016/8/23.
 */
public class AppUninstallActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();
        setContentView(R.layout.app_uninstall);
        EventBus.getDefault().register(this);
        init();
        getData();
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_APP_UNINSTALL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActionBar.setPageName(getPageAlias());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private LoadingLayout mLoadingLayout;
    private RecyclerView mRecyclerView;
    private ActionBar mActionBar;

    private AppUninstallAdapter mAdapter;

    private void init() {
        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);


        RecyclerView.LayoutManager mLayoutManger = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManger);
        mAdapter = new AppUninstallAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.addHeaderView(createHeaderView());

        mLoadingLayout.showLoading();
        mActionBar.setTitle("应用卸载");

        View mEmptyView = LayoutInflater.from(this).inflate(R.layout.view_empty_app_download, null);
        Button toDown = (Button) mEmptyView.findViewById(R.id.button);
        toDown.setText(R.string.now_download);
        toDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页去下载
                UIController.goHomePage(AppUninstallActivity.this, MainActivity.PAGE_TAB_RECOMMEND, MainActivity.PAGE_TAB_GAME_SUB_INDEX);
            }
        });
        ((TextView) mEmptyView.findViewById(R.id.text)).setText(R.string.app_download_empty_tips);
        mLoadingLayout.setEmptyView(mEmptyView);

    }

    private View createHeaderView() {
        View view = new View(this);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(this, 8)));
        return view;
    }

    private void getData() {
        new AsyncTask<Void, Void, List<UninstallAppInfo>>() {
            @Override
            protected List<UninstallAppInfo> doInBackground(Void... params) {
                return getInstalledList();
            }

            @Override
            protected void onPostExecute(List<UninstallAppInfo> uninstallAppInfos) {
                if (uninstallAppInfos.size() > 0) {
                    mAdapter.setList(uninstallAppInfos);
                    mLoadingLayout.showContent();
                } else {
                    mLoadingLayout.showEmpty();
                }
            }
        }.execute();
    }

    private List<UninstallAppInfo> getInstalledList() {
        List<PackageInfo> packageInfoList = AppUtils.getUserAppList(this);
        List<UninstallAppInfo> uninstallAppInfoList = new ArrayList<>();

        for (PackageInfo packageInfo : packageInfoList) {
            //过滤本应用程序
            if (packageInfo.packageName.equals(this.getPackageName())) {
                continue;
            }
            UninstallAppInfo uninstallAppInfo = new UninstallAppInfo();
            uninstallAppInfo.icon = packageInfo.applicationInfo.loadIcon(this.getPackageManager());
            uninstallAppInfo.name = String.valueOf(packageInfo.applicationInfo.loadLabel(this.getPackageManager()));
            uninstallAppInfo.firstInstallTime = packageInfo.firstInstallTime;
            uninstallAppInfo.packageName = packageInfo.packageName;
            uninstallAppInfoList.add(uninstallAppInfo);
        }

        Collections.sort(uninstallAppInfoList, new Comparator<UninstallAppInfo>() {
            @Override
            public int compare(UninstallAppInfo lhs, UninstallAppInfo rhs) {
                return lhs.firstInstallTime == rhs.firstInstallTime ? 0 : lhs.firstInstallTime > rhs.firstInstallTime ? -1 : 1;

            }
        });
        return uninstallAppInfoList;
    }

    public void onEventMainThread(InstallEvent event) {
        if (event.type == InstallEvent.UNINSTALL) {
            for (int i = 0; i < mAdapter.getList().size(); i++) {
                UninstallAppInfo uninstallAppInfo = mAdapter.getList().get(i);
                if (event.packageName.equals(uninstallAppInfo.packageName)) {
                    mAdapter.getList().remove(uninstallAppInfo);
                    if (mAdapter.getList().size() > 0) {
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mLoadingLayout.showEmpty();
                    }
                    return;
                }
            }
        }

//        LogUtils.i("installState" + event.type);
//        if (event.type == InstallEvent.INSTALLED_UPGRADE) {
//            for (int i = 0; i < mAdapter.getList().size(); i++) {
//                UninstallAppInfo uninstallAppInfo = mAdapter.getList().get(i);
//                if (!event.packageName.equals(uninstallAppInfo.packageName)) {
//                    mAdapter.setList(getInstalledList());
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        }
    }
}
