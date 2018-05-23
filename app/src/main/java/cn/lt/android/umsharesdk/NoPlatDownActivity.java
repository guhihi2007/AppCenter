package cn.lt.android.umsharesdk;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.GlobalParams;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.db.AppEntityDao;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.main.UIController;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 未下载分享平台页面
 */
public class NoPlatDownActivity extends BaseAppCompatActivity {
    public static final String WECHAT = "Wechat";
    public static final String QQ = "qq";
    public static final String WEIBO = "SinaWeibo";

    private Button btn_goDownload;
    private AppEntity target_APP;

    private String targetPlatform = "";
    private int targetPlatIconId;
    private String targetPkgName = "";
    private String targetChiName = "";
    private TextView ic_noInstall;
    private TextView noInstallInfo;
    private String resourceType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_install_qq);
        getIntentData();
        EventBus.getDefault().register(this);
        setStatusBar();
        initView();
        addListener();
        judgeIsDownloaing();
        judgePlatIsDownloaed();


    }

    private void getIntentData() {
        if (null != getIntent()) {
            targetPlatform = getIntent().getStringExtra("platName");
            resourceType = getIntent().getStringExtra("resourceType");

            switch (targetPlatform) {
                case WECHAT:
                    targetPkgName = "com.tencent.mm";
                    targetChiName = "微信";
                    targetPlatIconId = R.mipmap.ic_no_install_wechat_logo;
                    break;
                case QQ:
                    targetPkgName = "com.tencent.mobileqq";
                    targetChiName = "QQ";
                    targetPlatIconId = R.mipmap.ic_no_install_qq_logo;
                    break;
                case WEIBO:
                    targetPkgName = "com.sina.weibo";
                    targetChiName = "新浪微博";
                    targetPlatIconId = R.mipmap.ic_no_install_weibo_logo;
                    break;
            }

        }
    }


    private void initView() {
        ic_noInstall = (TextView) findViewById(R.id.ic_noQQinstall);
        noInstallInfo = (TextView) findViewById(R.id.noQQinstallInfo);
        btn_goDownload = (Button) findViewById(R.id.btn_goDownloadQQ);

        ic_noInstall.setText(targetChiName);
        ic_noInstall.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(targetPlatIconId), null, null);

        noInstallInfo.setText("你还没安装 " + targetChiName + "，请先下载并安装，社交互动体验更流畅，更安全");
        btn_goDownload.setText("下载 " + targetChiName);
    }

    /**
     * 获取平台是否已经下载过了
     */
    private void judgePlatIsDownloaed() {
        List<AppEntity> installList = null;
        installList = DownloadTaskManager.getInstance().getInstallTaskList();
        for (AppEntity app : installList) {
            if (app.getPackageName().equals(targetPkgName)) {
                target_APP = app;
            }
        }

        if (target_APP != null) {
            btn_goDownload.setText(targetChiName + " 已下载，前往安装");
            btn_goDownload.setOnClickListener(goInstallListener);
        }
    }

    private void addListener() {
        if (target_APP != null) {

            // 平台已经下载了的情况下，点击进行安装
            btn_goDownload.setOnClickListener(goInstallListener);
        } else {

            // 平台载QQ，点击进行下载
            btn_goDownload.setOnClickListener(goDownloadListener);
        }

    }


    // 监听是否已经进入下载状态
    public void onEventMainThread(DownloadEvent downloadEvent) {
        AppEntityDao dao = GlobalParams.getAppEntityDao();
        List<AppEntity> list = dao.queryBuilder().where(AppEntityDao.Properties.PackageName.eq(downloadEvent.packageName)).list();

        if (list.size() == 0) {
            return;
        }

        AppEntity app = list.get(0);

        if (app.getPackageName().equals(targetPkgName)) {

            // 已经成功启动下载
            if (downloadEvent.status == DownloadStatusDef.pending) {
                UIController.goDownloadTask(NoPlatDownActivity.this);
                finish();
            }

            // 正在下载中了
            if (downloadEvent.status == DownloadStatusDef.progress) {
                btn_goDownload.setText(targetChiName + " 正在下载中，点击查看");
                btn_goDownload.setOnClickListener(goDownloadTaskListener);
            }

            // 下载出错
            if (downloadEvent.status == DownloadStatusDef.error) {
                showFail();
            }

            // 重试
            if (downloadEvent.status == DownloadStatusDef.retry) {
                showFail();
            }

            // 下载完成
            if (downloadEvent.status == DownloadStatusDef.completed) {
                judgePlatIsDownloaed();
                btn_goDownload.setText(targetChiName + " 已下载，前往安装");
                btn_goDownload.setOnClickListener(goInstallListener);
            }
        }

    }

    private void judgeIsDownloaing() {
        AppEntityDao dao = GlobalParams.getAppEntityDao();
        List<AppEntity> list = dao.queryBuilder().where(AppEntityDao.Properties.PackageName.eq(targetPkgName)).list();
        if (list.size() == 0) return;
        AppEntity app = list.get(0);

        // 正在下载中了
        if (app != null && app.getPackageName().equals(targetPkgName)) {
            btn_goDownload.setText(targetChiName + " 正在下载中，点击查看");
            btn_goDownload.setOnClickListener(goDownloadTaskListener);
        }
    }


    private void showFail() {
        ToastUtils.showToast("下载出错，请重试");
        btn_goDownload.setText("下载 " + targetChiName);
        btn_goDownload.setEnabled(true);
    }

    @Override
    public void setPageAlias() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private View.OnClickListener goDownloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_goDownload.setText("请稍后..");
            btn_goDownload.setEnabled(false);

            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<List<AppDetailBean>>() {
                @Override
                public void onResponse(Call<List<AppDetailBean>> call, Response<List<AppDetailBean>> response) {
                    List<AppDetailBean> list = response.body();
                    if (list != null && list.size() != 0) {
                        AppDetailBean bean = list.get(0);
                        if (bean != null) {
                            AppEntity app = null;
                            try {
                                app = DownloadTaskManager.getInstance().transfer(bean);
                                app.resource_type = resourceType;
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                // TBD: 服务bu存在
                            }
                            // 启动下载
                            DownloadTaskManager.getInstance().startAfterCheck(NoPlatDownActivity.this, app, "manual", "request", "SharePage", "", targetPlatform + " is not exist when share by " + targetPlatform, "share");
                        }
                    } else {
                        showFail();
                    }
                }

                @Override
                public void onFailure(Call<List<AppDetailBean>> call, Throwable t) {
                    showFail();
                }

            }).bulid().requestAppByPackageName(targetPkgName);
        }
    };

    private View.OnClickListener goInstallListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UIController.goInstallTask(NoPlatDownActivity.this);
            finish();
        }
    };

    private View.OnClickListener goDownloadTaskListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btn_goDownload.setEnabled(false);
            UIController.goDownloadTask(NoPlatDownActivity.this);
            finish();
        }
    };
}