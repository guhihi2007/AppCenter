package cn.lt.android.main.download;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.base.DefaultFragmentPagerAdapter;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadRedPointManager;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.ApkNotExistEvent;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.DownloadUrlIsReturnEvent;
import cn.lt.android.event.NewDownloadTask;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.notification.event.NoticeTaskEvent;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.PagerSlidingTabStrip;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.log.Logger;
import de.greenrobot.event.EventBus;


/**
 * Created by wenchao on 2016/3/8.
 * 任务管理
 */
public class TaskManagerActivity extends BaseAppCompatActivity implements AppInstallCallBack {


    private AppDownloadFragment appDownloadFragment;
    private boolean exist;

    @Override
    public void setPageAlias() {
//        setmPageAlias(Constant.PAGE_TASK_MANAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DownloadRedPointManager.getInstance().setInTaskManager();
        //InstallRedPointManager.getInstance().setInTaskManager();
        setContentView(R.layout.activity_task_manager);
        setStatusBar();
        assignViews();
        getIntentData();
        EventBus.getDefault().register(this);
    }

    public PagerSlidingTabStrip mTabs;
    private ViewPager mViewPager;
    private ActionBar mActionBar;
    private DefaultFragmentPagerAdapter pagerAdapter;


    private List<BaseFragment> fragments = new ArrayList<>();
    public List<String> titleList = new ArrayList<>();

    private void assignViews() {
        exist = true;
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setTitle(getString(R.string.task_manager));
        mTabs.setShouldExpand(true);
        appDownloadFragment = new AppDownloadFragment();
        fragments.add(appDownloadFragment);
        AppInstallFragment appInstallFragment = new AppInstallFragment();
        appInstallFragment.setAppInstallCallBack(this);

        fragments.add(appInstallFragment);
        fillAppCount();
        pagerAdapter = new DefaultFragmentPagerAdapter(getSupportFragmentManager(), fragments, titleList);
        mViewPager.setAdapter(pagerAdapter);
        mTabs.setViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updataDownCount(mViewPager.getCurrentItem());
        LogUtils.d("iii", "TaskActivity onResume=>exist"+(exist?"之前不存在":"此activity已经存在" ));
        if(!exist){
            fragments.get(mViewPager.getCurrentItem()).setUserVisibleHint(true);
        }
        exist=false;
    }

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
        if (downloadEvent.status == DownloadStatusDef.completed || downloadEvent.status == DownloadStatusDef.INVALID_STATUS) {
            LogUtils.d("ActivityCom", "Activity onEventMainThread=>x下载完成" );
            titleList.clear();
            fillAppCount();
            int position = mTabs.getCurrentPosition();
            mTabs.notifyDataSetChangedRestorePosition(position);
        }
    }

    /**
     * 下载列表加入一条下载数据
     * 通知栏点击玩咖替换url地址异步
     */
    public void onEventMainThread(NewDownloadTask newDownloadTask) {
        updataDownCount(0);
    }

    public void onEventMainThread(RemoveEvent event) {
        titleList.clear();
        int loadCount = 0;
        int count = 0;
        List<AppEntity> downloadTaskList = DownloadTaskManager.getInstance().getDownloadTaskList();
        loadCount = downloadTaskList.size();
        if (downloadTaskList.contains(event.mAppEntity)) {
            loadCount--;
        }

        List<AppEntity> installTaskList = DownloadTaskManager.getInstance().getInstallTaskList();
        count = installTaskList.size();

        if (event.isInstalled && installTaskList.contains(event.mAppEntity)) {
            count--;
        }

        if (loadCount > 0) {
            titleList.add(getString(R.string.app_download) + loadCount);
        } else {
            titleList.add(getString(R.string.app_download));
        }
        if (count > 0) {
            titleList.add(getString(R.string.app_install) + count);
        } else {
            titleList.add(getString(R.string.app_install));
        }
        int position = mTabs.getCurrentPosition();
        mTabs.notifyDataSetChangedRestorePosition(position);


    }

    int loadCount;

    @Override
    public void onAppCountChanged(int count) {
        int loadCount = getLoadingCount();
        if (loadCount > 0) {
            titleList.add(getString(R.string.app_download) + loadCount);
        } else {
            titleList.add(getString(R.string.app_download));
        }
        if (count > 0) {
            titleList.add(getString(R.string.app_install) + count);
        } else {
            titleList.add(getString(R.string.app_install));
        }
        int position = mTabs.getCurrentPosition();
        mTabs.notifyDataSetChangedRestorePosition(position);
    }

    private void fillAppCount() {
        int loadCount = DownloadTaskManager.getInstance().getDownloadTaskList().size();

        if (loadCount > 0) {
            titleList.add(getString(R.string.app_download) + loadCount);
        } else {
            titleList.add(getString(R.string.app_download));
        }


        int completeCount = DownloadTaskManager.getInstance().getInstallTaskList().size();
        if (completeCount > 0) {
            titleList.add(getString(R.string.app_install) + completeCount);
        } else {
            titleList.add(getString(R.string.app_install));
        }
    }

    private int getLoadingCount() {
        int count = 0;
        List<AppEntity> apps = DownloadTaskManager.getInstance().getAll();
        if (apps.size() == 0) {
            return count;
        }
        for (AppEntity entity : apps) {
            if (DownloadStatusDef.isIng(entity.getStatus()) || entity.getStatus() == DownloadStatusDef.paused || entity.getStatus() == DownloadStatusDef.error) {
                count++;
            }
        }
        return count;
    }

    private int getCompleteCount() {
        int count = 0;
        List<AppEntity> apps = DownloadTaskManager.getInstance().getAll();
        if (apps.size() == 0) {
            return count;
        }
        for (AppEntity entity : apps) {
            try {
                if (entity.getStatus() == DownloadStatusDef.completed && DownloadTaskManager.getInstance().getState(entity) != InstallState.installed) {
                    count++;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                return 0;// TODO:
            }
        }
        Logger.i("countjfeifi" + count);
        return count;
    }

    private void getIntentData() {
        if (getIntent() != null) {
            boolean goInstallTab = getIntent().getBooleanExtra(Constant.GO_INSTALL_TAB, false);
            if (goInstallTab) {
                mViewPager.setCurrentItem(1);
            }
        }
    }

    /**
     * 安装时，apk不存，被执行了重新下载需要页面配合更新
     */
    public void onEventMainThread(ApkNotExistEvent event) {
        updataDownCount(0);

    }

    /**
     * 下载url被改变了，被执行了重新下载需要页面配合更新
     */
    public void onEventMainThread(DownloadUrlIsReturnEvent event) {
        updataDownCount(0);
        if (appDownloadFragment != null) {
            appDownloadFragment.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadRedPointManager.getInstance().setNotInTaskManager();
        //InstallRedPointManager.getInstance().setNotInTaskManager();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        EventBus.getDefault().post(new NoticeTaskEvent());
    }


    public void gengxinCOunt() {

    }

    private void updataDownCount(final int page) {
        LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int loadCount = DownloadTaskManager.getInstance().getDownloadTaskList().size();
                titleList.remove(0);
                titleList.add(0, getString(R.string.app_download) + (loadCount > 0 ? loadCount : ""));
                mTabs.notifyDataSetChangedRestorePosition(0);
                mViewPager.setCurrentItem(page);
                mTabs.notifyDataSetChangedRestorePosition(page);// 再调一次这方法，是为了让tab回到指定的位置
            }

        }, 200);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "TaskManagerActivity中取消了==请求码" + requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Activity被意外终止（回收）的话，直接关闭该页面
        this.finish();
    }

}
