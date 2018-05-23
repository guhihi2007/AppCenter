package cn.lt.android.main.specialtopic;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.DownloadAdAppReplacer;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.StorageSpaceDetection;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.SpecialTopicDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.entrance.DataShell;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.specialtopic.special_detail_bean.SpecialInfoBean;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by LinJunSheng on 2016/3/1.
 */
public class SpecialTopicDetailActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private LoadingLayout faultLayout; // 错误提示布局
    private Button btn_oneKeyAction;// 一键下载按钮
    private RelativeLayout fl_oneKey;// 一键下载布局块
    private RecyclerView rv_appList;
    private EntranceAdapter adapter;
    private String topicId;
    private List<BaseBean> STDList = new ArrayList<>();// 用于存放专题信息实体和游戏列表实体
    private List<AppBriefBean> appList = new ArrayList<>();// app列表
    private String pageTitle;// 页面标题
    private boolean hasClicked = true;
    private ActionBar mActionBar;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_SPECIAL_DETAIL, topicId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_topic_detail);
        setStatusBar();
        getIntentData();
        initView();
        requestData();
        EventBus.getDefault().register(this);
    }

    private void initView() {
        LogUtils.i(SpecialTopicDetailActivity.class.getSimpleName(), "专题详情页");
        faultLayout = (LoadingLayout) findViewById(R.id.faultLayout);
        rv_appList = (RecyclerView) findViewById(R.id.rv_appList);
        btn_oneKeyAction = (Button) findViewById(R.id.btn_oneKeyAction);
        fl_oneKey = (RelativeLayout) findViewById(R.id.fl_oneKey);
        btn_oneKeyAction.setOnClickListener(this);

        adapter = new EntranceAdapter(this, getPageAlias(), topicId);
        rv_appList.setLayoutManager(new LinearLayoutManager(this));
        rv_appList.setAdapter(adapter);
        rv_appList.setItemAnimator(null);
        mActionBar = (ActionBar) findViewById(R.id.ab);

        faultLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestData();
                faultLayout.showLoading();
            }
        });
    }

    private void getIntentData() {
        if (getIntent() != null) {
            topicId = getIntent().getStringExtra("topicId");
            pageTitle = getIntent().getStringExtra("pageTitle");
//            fromPage = getIntent().getStringExtra(Constant.EXTRA_PAGE);

            if (null != pageTitle && !"".equals(pageTitle)) {
                ((ActionBar) findViewById(R.id.ab)).setTitle(pageTitle);
            }
        }
    }

    private void requestData() {
        if (!NetWorkUtils.isConnected(this)) {
            //faultLayout.showErrorNoNetwork();
            ShowRefreshLoadingUtils.showLoadingForNoNet(faultLayout);
            return;
        }
        faultLayout.showLoading();
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<SpecialTopicDetailBean>() {
            @Override
            public void onResponse(Call<SpecialTopicDetailBean> call, Response<SpecialTopicDetailBean> response) {
                if (response != null && response.body() != null) {
                    SpecialTopicDetailBean bean = response.body();

                    // 转换成列表能显示的数据
                    transformData(bean);
                    setData();
                    initOneKey();
                } else {
                    faultLayout.showEmpty();
                }
            }

            @Override
            public void onFailure(Call<SpecialTopicDetailBean> call, Throwable t) {
                ShowRefreshLoadingUtils.showLoadingForNotGood(faultLayout);
                //faultLayout.showErrorNotGoodNetwork();
                ((ActionBar) findViewById(R.id.ab)).setTitle("专题详情");
            }
        }).bulid().requestSpecialTopicsDetail(topicId);
    }


    private void transformData(SpecialTopicDetailBean bean) {
        // 专题信息
        SpecialInfoBean specialInfoBean = new SpecialInfoBean(bean.getLead_content(), bean.getBanner(), bean.getCreated_at(), bean.getId(), bean.getType(), bean.getTitle(), bean.getLtType());

        // 如果没传进专题标题，则使用请求接口回来的标题数据
        if (!hasIntentPagetitle()) {
            ((ActionBar) findViewById(R.id.ab)).setTitle(bean.getTitle());
        }

        STDList.add(specialInfoBean);

        // app列表数据
        List<AppDetailBean> appDetailBeanList = bean.getApps();
        for (AppDetailBean app : appDetailBeanList) {
            app.setLtType("app");
            AppBriefBean briefBean = AppBeanTransfer.transferAppDetailBean(app);
            STDList.add(briefBean);
            appList.add(briefBean);
        }

        // 玩咖曝光
        WanKaManager.exposureApps(STDList, new SimpleResponseListener<JSONObject>() {
            @Override
            public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                setData();
            }
        },"专题页面曝光");

        try {
            DownloadTaskManager.getInstance().transferBriefBeanList(appList);
        } catch (RemoteException e) {
            e.printStackTrace();
            // TODO: 16/6/2
        }

        // 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app
        new DownloadAdAppReplacer().replaceByTopicDetail(STDList);// 本页所有列表
        new DownloadAdAppReplacer().replaceByAppBriefList(appList);// 本页app列表（下载用）

    }

    private void setData() {

        adapter.setList(DataShell.wrapData(STDList, 0));

        faultLayout.showContent();
    }


    private void initOneKey() {
        int downCnt = 0;
        int installCnt = 0;
        for (int i = 0; i < adapter.getList().size(); i++) {
            ItemData<BaseBean> item = (ItemData) adapter.getList().get(i);
            BaseBean bean = item.getmData();
            if (bean.getLtType().equals("app")) {
                AppBriefBean appDetailBean = (AppBriefBean) bean;
                AppEntity app = appDetailBean.getDownloadAppEntity();
                LogUtils.i("zhuanti", "status = " + app.getStatus());
                try {
                    int state = DownloadTaskManager.getInstance().getState(app);
                    app.setStatus(state);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                switch (app.getStatus()) {
                    case DownloadStatusDef.INVALID_STATUS:
                    case DownloadStatusDef.error:
                    case DownloadStatusDef.retry:
                    case DownloadStatusDef.paused:
                    case InstallState.upgrade: {
                        downCnt++;
                        break;
                    }
                    case InstallState.install_failure:
                    case DownloadStatusDef.completed: {
                        installCnt++;
                        break;
                    }
                    default:
                        break;
                }
            }

        }

        LogUtils.d("ddd", "initOnekey一键安装时的个数：==>"+installCnt+"集合个数=="+appList.size());
        if (downCnt >= 2) {
            btn_oneKeyAction.setVisibility(View.VISIBLE);
            btn_oneKeyAction.setText("一键下载");
            fl_oneKey.setVisibility(View.VISIBLE);
        } else if (installCnt != 0 && installCnt == appList.size()) {
            btn_oneKeyAction.setVisibility(View.VISIBLE);
            btn_oneKeyAction.setText("一键安装");
            fl_oneKey.setVisibility(View.VISIBLE);
        } else {
            btn_oneKeyAction.setVisibility(View.GONE);
            fl_oneKey.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int installCnt = 0;
        List<AppEntity> willDownloadList = new ArrayList<>();

        for (int i = 0; i < adapter.getList().size(); i++) {
            ItemData<BaseBean> item = (ItemData) adapter.getList().get(i);
            BaseBean bean = item.getmData();
            if (bean.getLtType().equals("app")) {
                AppBriefBean appDetailBean = (AppBriefBean) bean;
                AppEntity app = appDetailBean.getDownloadAppEntity();
                LogUtils.d("ddd", "点击一键安装时的状态：==>"+app.getStatus());
                switch (app.getStatus()) {
                    case DownloadStatusDef.INVALID_STATUS:
                    case DownloadStatusDef.error:
                    case DownloadStatusDef.retry:
                    case DownloadStatusDef.paused:
                    case InstallState.upgrade: {
                        willDownloadList.add(app);
                        break;
                    }
                    case InstallState.install_failure:
                    case DownloadStatusDef.completed:
                    case InstallState.uninstalled: {
                        LogUtils.d("ddd", "点击一键安装时的安装数目增加");
                        installCnt++;
                        break;
                    }
                    default:
                        break;
                }
            }

        }
        //一秒内点击无效，并不会上报
        if (hasClicked) {
            if (willDownloadList.size() > 0) {
                downloadAllApp(willDownloadList);
                DCStat.baiduStat(this, "onekey_download", "专题页面，一键下载");
            }
            performClick(installCnt);
            hasClicked = false;
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hasClicked = true;
                }
            }, 1000);
        }

    }

    // 如果所有游戏都是未安装状态，全部启动安装
    private void performClick(int installCnt) {
//        DCStat.baiduStat(this, "onekey_install", "专题页面，一键安装");
        if (installCnt == appList.size()) {
            fl_oneKey.setVisibility(View.GONE);
            for (int i = 0; i < adapter.getList().size(); i++) {
                ItemData<BaseBean> item = (ItemData) adapter.getList().get(i);
                BaseBean bean = item.getmData();
                if (bean.getLtType().equals("app")) {
                    AppBriefBean appDetailBean = (AppBriefBean) bean;
                    AppEntity app = appDetailBean.getDownloadAppEntity();
                    if (StorageSpaceDetection.getAvailableSize() <= Long.valueOf(app.getPackageSize())) {
                        fl_oneKey.setVisibility(View.VISIBLE);
                    }
                    InstallManager.getInstance().start(app, getPageAlias(), topicId, true);
                }
            }
        }
    }

    private void downloadAllApp(final List<AppEntity> willDownloadList) {
        if (!DownloadChecker.getInstance().noNetworkPromp(ActivityManager.self().topActivity(), new Runnable() {
            @Override
            public void run() {
                // 预约wifi下载
                goDownload(willDownloadList, true);
            }
        })) {

            // wifi网络下直接下载
            if (NetUtils.isWifi(this)) {
//                for (AppEntity app : willDownloadList) {
                DownloadTaskManager.getInstance().startAfterCheckList(SpecialTopicDetailActivity.this, willDownloadList, "onekey", null, getPageAlias(), topicId,"","onekey_download");
//                }
                return;
            }
            // 3G/4G网络需要弹窗确认
            if (NetUtils.isMobileNet(this)) {
                new CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？").setPositiveButton(R.string.continue_mobile_download).setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 直接下载
                        goDownload(willDownloadList, false);
                    }
                }).setNegativeButton(R.string.order_wifi_download).setNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 所有设置为预约wifi下载
                        goDownload(willDownloadList, true);
                    }
                }).create().show();
            }
        }
    }

    private void goDownload(List<AppEntity> willDownloadList, boolean isOrderWifiDownload) {
        for (AppEntity appEntity : willDownloadList) {
            appEntity.setIsOrderWifiDownload(isOrderWifiDownload);
        }

        DownloadTaskManager.getInstance().startAfterCheckList(SpecialTopicDetailActivity.this, willDownloadList, "onekey", null, getPageAlias(), topicId,"","onekey_download");
        fl_oneKey.setVisibility(View.GONE);
    }

    /**
     * 监听下载进度更新
     *
     * @param downloadEvent
     */
    public void onEventMainThread(DownloadEvent downloadEvent) {
//        LogUtils.i("qwe", "（专题详情）~  status = " + downloadEvent.status);
//        for (int i = 0; i < adapter.getList().size(); i++) {
//            try {
//                ItemData item = (ItemData) adapter.getList().get(i);
//                PresentType type = item.getmPresentType();
//                switch (type) {
//                    case app:
//                        AppBriefBean detailBean = (AppBriefBean) item.getmData();
//                        AppEntity appEntity = detailBean.getDownloadAppEntity();
//                        updateItemUI(downloadEvent, i, appEntity);
//                        break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        initOneKey();
    }

//    private void updateItemUI(DownloadEvent downloadEvent, int i, AppEntity appInfo) {
//        if (FileDownloadUtils.generateId(appInfo.getDownloadUrl(), appInfo.getSavePath()) == downloadEvent.downloadId) {
//
//            if (downloadEvent.status == DownloadStatusDef.blockComplete && appInfo.getStatus() == DownloadStatusDef.completed) {
//                return;
//            }
//
//
//            //更新界面i
//            appInfo.setTotal(downloadEvent.totalBytes);
//            appInfo.setSoFar(downloadEvent.soFarBytes);
//            appInfo.setStatus(downloadEvent.status);
//            adapter.notifyItemChanged(i);
//
//        }
//    }

    /**
     * 通知安装事件更新
     *
     * @param installEvent
     */
    public void onEventMainThread(InstallEvent installEvent) {
//        for (int i = 0; i < adapter.getList().size(); i++) {
//            try {
//                ItemData item = (ItemData) adapter.getList().get(i);
//                PresentType type = item.getmPresentType();
//                switch (type) {
//                    case app:
//                        AppBriefBean detailBean = (AppBriefBean) item.getmData();
//                        AppEntity appEntity = detailBean.getDownloadAppEntity();
//                        if (appEntity.getPackageName().equals(installEvent.packageName)) {
//                            //更新界面i
//                            appEntity.setStatusByInstallEvent(installEvent.type);
//                            adapter.notifyItemChanged(i);
//                        }
//                        break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        if(installEvent.type!=InstallEvent.INSTALL_FAILURE){
//        }
        initOneKey();


    }

//    public void onEventMainThread(RemoveEvent event) {
//        for (int i = 0; i < adapter.getList().size(); i++) {
//            try {
//                ItemData item = (ItemData) adapter.getList().get(i);
//                PresentType type = item.getmPresentType();
//                switch (type) {
//                    case app:
//                        AppBriefBean detailBean = (AppBriefBean) item.getmData();
//                        AppEntity appEntity = detailBean.getDownloadAppEntity();
//                        if (appEntity.getPackageName().equals(event.mAppEntity.getPackageName())) {
//                            //更新界面i
//                            appEntity.setStatus(DownloadStatusDef.INVALID_STATUS);
//                            adapter.notifyItemChanged(i);
//                        }
//                        break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }


    // 判断是否有传进专题标题
    private boolean hasIntentPagetitle() {
        if (getIntent() != null || getIntent().getStringExtra("pageTitle") == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initOneKey();
        mActionBar.setPageName(getPageAlias());

        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "SpecialTopicDetailActivity中取消了==请求码"+requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }
}
