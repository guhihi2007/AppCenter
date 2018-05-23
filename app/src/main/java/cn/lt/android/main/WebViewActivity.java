package cn.lt.android.main;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.DeviceInfo;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.install.InstallManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.umsharesdk.OneKeyShareUtil;
import cn.lt.android.umsharesdk.ShareBean;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ShowRefreshLoadingUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.ShareHolder;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.model.DownloadInfo;
import cn.lt.framework.log.Logger;
import cn.lt.pullandloadmore.LoadingLayout;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wenchao on 2015/8/19.
 * web视图activity
 */
public class WebViewActivity extends BaseAppCompatActivity {
    private ActionBar mActionbar;
    private LoadingLayout mLoadingLayout;
    private WebView mWebview;
    private String gotoUrl;
    private String extraTitle;

    private static final int NOT_DOWNLOAD = 0;
    private static final int WAITING = 1;
    private static final int DOWNLOADING = 2;
    private static final int STOP = 3;
    private static final int DOWNLOAD_FINISH = 4;
    private static final int INSTALL_FINISH = 5;
    private static final int RETRY = 6;
    private static final int UPGRADE = 7;
    private static final int INSTALLING = 8;
    private String packageNameData;
    private ExecutorService mThreadPool;
    private int installStatus;


    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_H5, gotoUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        EventBus.getDefault().register(this);
        mThreadPool = Executors.newSingleThreadExecutor();
        setStatusBar();
        getIntentData();
        initialize();
    }

    public synchronized void onEventMainThread(final DownloadEvent downloadEvent) {
        LogUtils.i("auoiowah2", "下载原始status = " + downloadEvent.status);

        if (TextUtils.isEmpty(packageNameData) || packageNameData.contains(downloadEvent.packageName)) {
            final int status;
            if (DownloadStatusDef.isRealIng(downloadEvent.status)) {
                status = DOWNLOADING;
            } else if((downloadEvent.status == DownloadStatusDef.completed)
                    && InstallManager.getInstance().isAppInstalling(downloadEvent.packageName)) {
                return;
            } else {
                status = getAppStatus(downloadEvent.packageName);
            }



            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    WebViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LogUtils.i("auoiowah2", "status = " + status + ", sofar = " + downloadEvent.soFarBytes + ", totla = " + downloadEvent.totalBytes);
                            mWebview.loadUrl("javascript:percent(" + downloadEvent.soFarBytes + "," + downloadEvent.totalBytes + "," + status + ",\"" + downloadEvent.packageName + "\")");
                        }
                    });

                }
            });


        }
    }


    public void onEventMainThread(final InstallEvent installEvent) {
        if (TextUtils.isEmpty(packageNameData) || packageNameData.contains(installEvent.packageName)) {
            if (installEvent.getType() == InstallEvent.INSTALLED_ADD) {
                installStatus = INSTALL_FINISH;
            } else if(installEvent.getType() == InstallEvent.UNINSTALL) {
                if(installStatus == INSTALLING) {
                    return;
                }
                installStatus = NOT_DOWNLOAD;
            } else {
                installStatus = getAppStatus(installEvent.packageName);
            }

            if (installStatus != -100) {
                LogUtils.i("auoiowah2", "安装status = " + installStatus);

                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        WebViewActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mWebview.loadUrl("javascript:percent(" + 0 + "," + 0 + "," + installStatus + ",\"" + installEvent.packageName + "\")");

                            }
                        });

                    }
                });

            }
        }
    }

    private int getAppStatus(String packageName) {
        AppEntity app = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);

//        if(DownloadStatusDef.isInvalid(status)) {
//            return NOT_DOWNLOAD;
//        } else if(status == DownloadStatusDef.pending) {
//            return WAITING;
//        } else if(DownloadStatusDef.isIng(status)) {
//            return DOWNLOADING;
//        } else if(status == DownloadStatusDef.paused) {
//            return STOP;
//        } else if(status == DownloadStatusDef.completed) {
//            return DOWNLOAD_FINISH;
//        } else if(status == DownloadStatusDef.error || status == InstallEvent.INSTALL_FAILURE) {
//            return RETRY;
//        } else if(status == InstallEvent.INSTALLED_ADD) {
//            return INSTALL_FINISH;
//        }

        int state = 0;
        long sofar = 0;
        long total = 0;
//        try {
//            state = DownloadTaskManager.getInstance().getState(app);
//            sofar = DownloadTaskManager.getInstance().getSofar(app);
//            total = DownloadTaskManager.getInstance().getTotal(app);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        /******************以下是一次性拿bean的方式*************************/
//        RemoteFilter remoteFilter = new RemoteFilter(app).invoke();
//        state = remoteFilter.getState();
//        sofar = remoteFilter.getSofar();
//        total = remoteFilter.getTotal();

        DownloadInfo downloadInfo = DownloadTaskManager.getInstance().getDownloadInfo(app);
        state = DownloadTaskManager.getInstance().getRealState(app, downloadInfo.getStatus());
        sofar = downloadInfo.getSoFarBytes();
        total = downloadInfo.getTotalBytes();

        LogUtils.e("xxx",state + "");

        /******************************************/

        //为了解决断网重启进程移动网进入状态异常
        if (state == DownloadStatusDef.INVALID_STATUS && app.getStatus() == DownloadStatusDef.paused) {
            state = DownloadStatusDef.paused;
        }
        if (InstallState.upgrade == state) {

            //升级
//            showUpgrade();
            return UPGRADE;

        } else if (DownloadStatusDef.isInvalid(state)) {

            if (sofar <= 0 || sofar == total) {// 还没下载 或者 下载完成安装后又卸载掉了
                //未下载，进度为0
//                showDownload();
                return NOT_DOWNLOAD;
            } else if (sofar < 100) {
                //未下载 ，进度不为0
//                showContinue(sofar, total);
                return STOP;
            } else {
                // 已下载 ，进度为100（应用更新时，新包安装前，会卸载老包，此时发出的卸载状态会走这里）
//                showOpenApp();
                return INSTALL_FINISH;
            }

        } else if (DownloadStatusDef.isIng(state)) {

            int downLoadApps = 0;
            try {
                downLoadApps = DownloadTaskManager.getInstance().getDownloadingList().size();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (DownloadStatusDef.pending == state && downLoadApps > 2) {
                //队列中的
//                showWaiting(sofar, total);
                return WAITING;
            } else {
                // 正在下载中的
//                showProgress(sofar, total);
                return DOWNLOADING;
            }

        } else if (DownloadStatusDef.error == state) {
            //下载错误
//            showRetry();
            return RETRY;

        } else if (DownloadStatusDef.paused == state) {
            //暂停
//            showContinue(sofar, total);
            return STOP;
        } else if (DownloadStatusDef.completed == state) {

            if (DownloadTaskManager.getInstance().isFailureByInstall(app.getAppClientId())) {
                //showRetry();  因内存不足，不显示重试
//                showInstall();
                return DOWNLOAD_FINISH;

            } else if (InstallManager.getInstance().isAppInstalling(app.getPackageName())) {
//                showInstalling();
                return INSTALLING;

            } else {
                // check install state
                switch (app.getStatus()) {
                    case InstallState.installing:
//                        showInstalling();
                        return INSTALLING;
                    case InstallState.install_failure:
                        if (!DownloadTaskManager.getInstance().isFailureByInstall(app.getAppClientId())) {
                            //  因内存不足，不显示重试
                            // showRetry();
                            return RETRY;
                        }
                        break;
                    case InstallState.installed:
//                        showOpenApp();
                        return INSTALL_FINISH;
                    default:
//                        showInstall();
                        return DOWNLOAD_FINISH;
                }
            }


        } else if (InstallState.installed == state) {
            //已安装
//            showOpenApp();
            return INSTALL_FINISH;
        }

        return -100;
    }


    private void getIntentData() {
        extraTitle = getIntent().getStringExtra(Constant.EXTRA_TITLE);
        gotoUrl = getIntent().getStringExtra(Constant.EXTRA_URL);
    }


    private void initialize() {
        //初始化
        mActionbar = (ActionBar) findViewById(R.id.actionbar);
        mLoadingLayout = (LoadingLayout) findViewById(R.id.loadingLayout);
        mWebview = (WebView) findViewById(R.id.webview);
//        mWebview.loadUrl("https://www.baidu.com/");
        mLoadingLayout.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadingLayout.showLoading();
                loadPage();
            }
        });
        //设置标题
        if (!TextUtils.isEmpty(extraTitle)) {
            mActionbar.setTitle(extraTitle);
            Logger.i("extraTitle = " + extraTitle);
        }
        mWebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setBuiltInZoomControls(true);
        mWebview.getSettings().setUseWideViewPort(true);
        mWebview.getSettings().setDefaultTextEncodingName("utf-8");
        mWebview.getSettings().setSavePassword(true);
        mWebview.getSettings().setLoadWithOverviewMode(true);

        mWebview.getSettings().setAllowFileAccess(true);
        mWebview.getSettings().setAppCacheEnabled(true);
        mWebview.getSettings().setSaveFormData(false);
        mWebview.getSettings().setLoadsImagesAutomatically(true);

        loadPage();


    }

    private void loadPage() {
        if (NetUtils.isConnected(this)) {
            mWebview.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(filterAddClientInfoParamForV4(url));
                    Logger.i("url:" + url);
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    mLoadingLayout.showLoading();
                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    super.onLoadResource(view, url);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    mLoadingLayout.showContent();
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    mLoadingLayout.showErrorNoNetwork();
                    super.onReceivedError(view, request, error);
                }
            });
            mWebview.addJavascriptInterface(new JavascriptInterface(), "ttappstore");
            mWebview.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    if (TextUtils.isEmpty(extraTitle)) {
                        mActionbar.setTitle(title);
                    }
                }
            });

            //web安全证书
            mWebview.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                    super.onReceivedSslError(view, handler, error);
                    handler.proceed();
                }
            });

            //跳转到URL
            if (!TextUtils.isEmpty(gotoUrl)) {
                gotoUrl = filterAddClientInfoParamForV4(gotoUrl);
                mWebview.loadUrl(gotoUrl);
                Logger.i("webviewUrl:" + gotoUrl);
            }
        } else {
            ShowRefreshLoadingUtils.showLoadingForNoNet(mLoadingLayout);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebview.canGoBack()) {
                mWebview.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebview.destroy();
        EventBus.getDefault().unregister(this);
    }


    /**
     * 4.0版本
     *
     * @param url
     * @return
     */
    private String filterAddClientInfoParamForV4(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getAuthority();
            String scheme = uri.getScheme();
            List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
            String replaceParam = null;
            for (NameValuePair param : params) {
                if (param.getName().equals("clientinfo") && param.getValue().equals("1")) {
                    replaceParam = param.getName() + "=" + param.getValue();
                    DeviceInfo phoneParams = new DeviceInfo(this);
                    String value = new Gson().toJson(phoneParams);
                    String newParam = "clientinfo=" + value;
                    url = url.replace(replaceParam, newParam);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    private AppDetailBean appDeailBean;

    /**
     * 请求详情信息
     * 这里还要区分是游戏详情还是软件详情，因为接口不一样
     */
    private void requestAppData(final String id, String type) {
        NetDataInterfaceDao dao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<AppDetailBean>() {
            @Override
            public void onResponse(Call<AppDetailBean> call, Response<AppDetailBean> response) {
                appDeailBean = response.body();
                if (null != appDeailBean) {
                    try {
                        AppEntity entity = DownloadTaskManager.getInstance().transfer(appDeailBean);
                        DownloadTaskManager.getInstance().startAfterCheck(WebViewActivity.this, entity, "manual", "request", "H5", gotoUrl,"","H5");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    LogUtils.i("zzz", "游戏已下架");
                    mLoadingLayout.showEmpty();
                }
            }

            @Override
            public void onFailure(Call<AppDetailBean> call, Throwable t) {
                mLoadingLayout.showErrorNotGoodNetwork();

            }
        }).bulid();

        if (type.equals("software")) {
            dao.requestSoftWareDetail(id);
        } else {
            dao.requestGameDetail(id);
        }

    }


    private void showShareDialog(String logoUrl, String id, String type, String link, String content, String title) {
        try {
            ShareBean shareBean = new ShareBean();
            shareBean.setShareIcon(logoUrl);
            shareBean.setTitle(title);
            shareBean.setShareType(OneKeyShareUtil.ShareType.activities);
            shareBean.setShareContent(content);
            shareBean.setShareLink(link);
            shareBean.setActivity(WebViewActivity.this);
            new PublicDialog(this, new ShareHolder()).showDialog(new DataInfo(shareBean));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示登陆框，登陆成功后跳转url，此url在原url基础之上添加了参数
     *
     * @param url
     */
    private String mLoginUrl;

    private void showLoginDialog(final String url) {
        mLoginUrl = url;
        UIController.goAccountCenter(this, Constant.USER_LOGIN);
    }

    private void showRegisterDialog() {
        UIController.goAccountCenter(this, Constant.USER_REGISTER, "h5");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String token = UserInfoManager.instance().getUserInfo().getToken();
        mWebview.loadUrl("javascript:checkLogin(\"" + (token == null ? "" : token) + "\")");
    }


    /**
     * url中添加参数
     *
     * @param url
     * @return
     */
    private void loadAddUserParams(String url) {
        if (url == null || url.contains("clientinfo={")) {
            //如果已经有此参数，则不用添加了,也不需要重新加载
            return;
        }

        URI uri = URI.create(url);
        DeviceInfo phoneParams = new DeviceInfo(this);
        String value = new Gson().toJson(phoneParams);

        List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
        if (params != null && params.size() > 0) {
            url = url + "&clientinfo=" + value;
        } else {
            url = url + "?clientinfo=" + value;
        }
        mWebview.loadUrl(url);

    }

    /**
     * 执行js脚本
     *
     * @param js
     */

    private void execJs(String js) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebview.evaluateJavascript(js, null);
        } else {
            mWebview.loadUrl(js);
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public class JavascriptInterface {
        /**
         * js接口
         * 点击触发按钮，会有不同状态
         */
        @android.webkit.JavascriptInterface
        public void app_download(String id, String type, final String packageName, int status) {
            LogUtils.i("jsBtn", "id = " + id + ", packageName = " + packageName + ", type = " + type + ", status = " + status);
            switch (status) {
                case UPGRADE:
                case NOT_DOWNLOAD:// 未下载

                    WebViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWebview.loadUrl("javascript:percent(" + 0 + "," + 0 + "," + WAITING + ",\"" + packageName + "\")");

                        }
                    });

                    requestAppData(id, type);
                    break;
                case WAITING:// 等待中
                case DOWNLOADING:// 下载中
                    AppEntity app = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);
                    if (app != null) {
                        try {
                            DownloadTaskManager.getInstance().pause(app, "normal", "H5", gotoUrl,"","H5");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case STOP:// 暂停
                    AppEntity app2 = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);
                    if (app2 != null) {
                        DownloadTaskManager.getInstance().startAfterCheck(WebViewActivity.this, app2, "maunal", "continue", "H5", gotoUrl,"","H5");
                    }
                    break;
                case RETRY:// 重试
                    AppEntity app3 = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);
                    if (app3 != null) {
                        DownloadTaskManager.getInstance().startAfterCheck(WebViewActivity.this, app3, "manual", "retry", "H5", gotoUrl,"","H5");
                    }
                    break;
                case DOWNLOAD_FINISH:// 下载完成
                    AppEntity app4 = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);
                    if (app4 != null) {
                        InstallManager.getInstance().start(app4, "H5", "", false);
                    }
                    break;
                case INSTALL_FINISH:// 安装完成
                    AppUtils.openApp(WebViewActivity.this, packageName);
                    break;
                default:
                    break;
            }
        }


        /**
         * 初始化所有app的接口
         *
         * @param data 包名数组
         */
        @android.webkit.JavascriptInterface
        public void app_init_app_status(String data) {
            WebViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String token = UserInfoManager.instance().getUserInfo().getToken();
                    mWebview.loadUrl("javascript:checkLogin(\"" + (token == null ? "" : token) + "\")");
                }
            });

            packageNameData = data;

            String[] packageNameArr = data.split(",");

            long sofar = 0;
            long total = 0;
            int status = 0;

            List<String> urls = new ArrayList<>();

            for (int i = 0; i < packageNameArr.length; i++) {

                String packageName = packageNameArr[i];
                AppEntity app = DownloadTaskManager.getInstance().getAppEntityByPkg(packageName);
                LogUtils.e("xxx",packageNameArr.length + " -- " + (app == null));
                if (app == null) {
                    sofar = 0;
                    total = 0;
                    status = NOT_DOWNLOAD;

                    if (AppUtils.isInstalled(packageName)) {
                        AppDetailBean upgradeApp = UpgradeListManager.getInstance().findByPackageName(packageName);
                        if (upgradeApp == null) {
                            status = INSTALL_FINISH;
                        } else {
                            status = UPGRADE;
                        }
                    }

                } else {
                    sofar = app.getSoFar();
                    total = app.getTotal();
                    status = getAppStatus(packageName);
                }
                LogUtils.i("auoiowah2", "init--->("+ packageName +") sofar = " + sofar + ", total = " + total + ", status = " + status);

                urls.add("javascript:percent(" + sofar + "," + total + "," + status + ",\"" + packageName + "\")");
            }

            for (final String url : urls) {
                WebViewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebview.loadUrl(url);
                    }
                });
            }

        }


        /**
         * @param id          应用id
         * @param packageName 应用包名
         * @param type        应用分类
         */
        @android.webkit.JavascriptInterface
        public void app_goAppDetail(final int id, final String packageName, final String type) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    UIController.goAppDetail(WebViewActivity.this, false, "", id + "", packageName, type, "H5", "", "");
                }
            });
        }

        /**
         * js接口,
         *
         * @param logoUrl 分享logo
         * @param id      分享标题
         * @param type    分享内容
         * @param link    分享跳转链接
         */
        @android.webkit.JavascriptInterface
        public void app_share(final String logoUrl, final String id, final String type, final String link, final String content, final String title) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    showShareDialog(logoUrl, id, type, link, content, title);
                }
            });
        }

        @android.webkit.JavascriptInterface
        public void user_login() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    goLogin();
                }
            });

        }

        @android.webkit.JavascriptInterface
        public void user_register() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    goLogin();
                    showRegisterDialog();
                }
            });

        }

        private String percent = "";

//        @android.webkit.JavascriptInterface
//        public String get_percent(final String pkgName) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    AppEntity entity = DownloadTaskManager.getInstance().getAppEntityByPkg(pkgName);
//                    try {
//                        long sofar = DownloadTaskManager.getInstance().getSofar(entity);
//                        long total = DownloadTaskManager.getInstance().getTotal(entity);
//                        float percentF = sofar * 100f / total;
//                        String pro = String.format("%.2f", percentF);
//                        percent = pro.equals("NaN") ? "0.00" : pro + "%";
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//            return percent;
//
//        }
//        /***
//         * 暂停
//         */
//        /***
//         * 继续
//         */
//        /**
//         * 安装
//         */
//        /***
//         * 打开
//         */

    }

    private void goLogin() {
        String url = mWebview.getUrl();
        //调用登陆框
        if (UserInfoManager.instance().isLogin()) {
//                已经登陆
            loadAddUserParams(url);
        } else {
            //未登陆
            showLoginDialog(url);

        }
    }

    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    private boolean isAppInstalled(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}
