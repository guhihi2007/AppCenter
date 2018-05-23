package cn.lt.android.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobads.SplashAdListener;
import com.google.gson.Gson;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.ads.bean.WhiteListBean;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.splash.SplashManager;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.entity.APPUpGradeBlackListBean;
import cn.lt.android.entity.AdsTypeBean;
import cn.lt.android.entity.AdvertisingConfigBean;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.entity.ClickTypeDataBean;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.entity.MarketResourceBean;
import cn.lt.android.entity.ProportionBean;
import cn.lt.android.entity.SplashShowTimeBean;
import cn.lt.android.main.entrance.data.ClickType;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.main.loading.AdsAdapter;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.service.LoadingIntentService;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatFailureManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.appstore.R;
import cn.lt.framework.util.PreferencesUtils;
import cn.lt.framework.util.ScreenUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/***
 * Created by dxx on 2016/3/7.
 */
@SuppressWarnings("ALL")
public class LoadingActivity extends BaseAppCompatActivity implements View.OnClickListener {

    public static final String INTENT_JUMP_KEY = "imgJump";
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String STORAGE_MESSAGE = "存储空间";
    public static final String PHONE_MESSAGE = "设备信息";
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int FORRESULT_CODE = 400;
    private ImageView mBackGroudView;
    private boolean imgaeJump;
    private boolean isForResult;
    /**
     * 是否启动于点击通知栏
     */
    private boolean clickFromNotice;
    private AdsImageBean mBean;
    private List<ItemData<ClickTypeBean>> mDatas;
    private boolean isClick;
    private int clickTimes;
    private int i;
    private ClickType mClickType;
    private ClickTypeDataBean mData;
    private long startRequestTime;
    private ViewGroup container;
    private TextView skipView;
    private SplashAD splashAD;
    private ImageView mAppLogo;
    private static final String SKIP_TEXT = "点击跳过 %d";
    public boolean canJump = false;

    private boolean isFromDeeplink = false;
    private String deeplinkDataStr = "";

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_LOADING);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading);
        Intent intent = new Intent(this,LoadingIntentService.class);
        startService(new Intent(this, LoadingIntentService.class));
        mBackGroudView = (ImageView) findViewById(R.id.iv_loading_activity);
        mAppLogo = (ImageView) findViewById(R.id.app_logo);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAppLogo.getLayoutParams();
        int srceenHeight = ScreenUtils.getScreenHeight(this);
        params.height = (int) (srceenHeight * 0.184375);

        container = (ViewGroup) this.findViewById(R.id.splash_container);
        skipView = (TextView) findViewById(R.id.skip_view);
        getDeepLinkData();
        checkin();
    }

    private void checkin() {
        if (LTDirectoryManager.getInstance() == null) {
            LTDirectoryManager.initManager();
        } else {
            LTDirectoryManager.getInstance().init();
        }

        setBackGround();
        judgeStartActivity();
        GlobalConfig.setIsOnClick(this, false);
//        DCStat.appStart(this, getPackageInfo());//上报启动数据
        //上报未上报成功的数据
        StatFailureManager.submitFailureData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //勾选了永久拒绝，从设置返回才会进入
        if (requestCode == FORRESULT_CODE && isForResult) {
            //设置里开启存储权限
            checkin();
        }
    }

    private void setBackGround() {
        SharedPreferences loadingImgSp = this.getSharedPreferences("loadingImg", Context.MODE_PRIVATE);
        String md5 = loadingImgSp.getString(LoadingImgWorker.PRE_KEY_IMG_MD5, null);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TT_AppCenter/image" + File.separator + md5;
        File file = new File(path);
//        if (mBackGroudView != null) {
        if (md5 != null && file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                Bitmap bm = BitmapFactory.decodeStream(fis);
                if (bm != null) {
                    mBackGroudView.setImageBitmap(bm);
                    mBackGroudView.setOnClickListener(this);
                }
                fis.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 保存个页面对应广告类型参数
     */
    private void saveAdsType(AdsTypeBean adsTypeBean) {
        adsTypeBean.setPageAdType();
    }

    /**
     * 启动下一个activity前做数据处理，并设置启动新页面的方式
     */
    private void judgeStartActivity() {
        final Intent localIntent = new Intent(LoadingActivity.this, MainActivity.class);
        getPushIntentData(localIntent);
        localIntent.putExtra(NoticeConsts.isPush, isByPush);
        putDeeplinkIntentData(localIntent);

        if (clickFromNotice) {// 点击通知栏进来的，不许显示启动大图，直接进入下一个页面
            // 这行代码的作用是保持HomeActivity唯一。防止推送启动Activity时启动多个HomeActivity
            localIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            goToNextActivity(localIntent);
        } else {
            // 属于正常启动的，显示启动大图
            LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (PreferencesUtils.getBoolean(LoadingActivity.this,Constant.RUIWEI_STATUS)) {
                        com.locate.utils.Wgr.setSwitchOn(LoadingActivity.this,true);
                    } else {
                        com.locate.utils.Wgr.setSwitchOn(LoadingActivity.this,false);
                    }

                    boolean gdtStatus = PreferencesUtils.getBoolean(LoadingActivity.this, Constant.GDT_STATUS, false);
                    boolean baiduStatus = PreferencesUtils.getBoolean(LoadingActivity.this, Constant.BAIDU_STATUS, false);
                    LogUtils.i("Erosion", "广点通：" + gdtStatus + ",百度：" + baiduStatus);

                    if (!baiduStatus && !gdtStatus) {
                        new Handler() {
                            public void handleMessage(Message msg) {
                                setImageJumpIntentData(localIntent);
                                // 应先判断是否安装完第一次启动，需跳转到应到页面（目前暂无），所以目前直接跳转
                                goToNextActivity(localIntent);
                            }
                        }.sendEmptyMessageDelayed(0, 1500);
                    } else {
                        if (imgaeJump) {
                            setImageJumpIntentData(localIntent);
                            // 应先判断是否安装完第一次启动，需跳转到应到页面（目前暂无），所以目前直接跳转
                            goToNextActivity(localIntent);
                            LogUtils.i("Erosion", "点击进入");
                        } else {
                            SplashManager splashManager = new SplashManager(LoadingActivity.this, container, skipView,null,true);
                            splashManager.needShowSplash();
                        }
                    }
                }
            },1000);
        }
        LTApplication.appIsStart = true;
    }

    /**
     * 设置首页图片点击跳转的数据
     */
    private void setImageJumpIntentData(Intent localIntent) {
        localIntent.putExtra(INTENT_JUMP_KEY, imgaeJump);
        localIntent.putExtra(MainActivity.INTENT_JUMP_KEY_MAIN_TAB, MainActivity.KEY_PAGE_PORTAL_RECOMMEND);
        localIntent.putExtra(MainActivity.INTENT_JUMP_KEY_MAIN_TAB, MainActivity.PAGE_TAB_RECOMMEND);
        localIntent.putExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB, MainActivity.PAGE_TAB_GAME_SUB_INDEX);

        localIntent.putExtra("ClickTypeDataBean", (Parcelable) mData);
        localIntent.putExtra("ClickType", mClickType);
    }

    /**
     * 获取通知栏点击事件传来的相关数据
     */
    private void getPushIntentData(Intent localIntent) {
        Intent intent = getIntent();
        if (intent != null) {
            clickFromNotice = intent.getBooleanExtra(Constant.EXTRA_CLICK_FROM_NOTICE, false);

            // 封装数据的bundle
            Bundle pushBundle = intent.getBundleExtra(NoticeConsts.pushBundle);
            if (pushBundle != null) {
                isByPush = true;
                // 传递到下个activity
                localIntent.putExtra(NoticeConsts.pushBundle, pushBundle);
            }

            // 点击启动页面类型
            int noticeStartType = getIntent().getIntExtra(NoticeConsts.noticeStartType, 0);
            localIntent.putExtra(NoticeConsts.noticeStartType, noticeStartType);
        }
    }

    /**
     * 启动下一个页面
     */
    public void goToNextActivity(Intent localIntent) {
        startActivity(localIntent);
        overridePendingTransition(R.anim.loading_image_push_left_in, R.anim.loading_image_push_left_out);
        LoadingActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        imgaeJump = true;
        if (mBean != null) {
            mClickType = ClickType.valueOf(mBean.getClick_type());
            mData = mBean.getData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJump) {
            next();
        }
        canJump = true;

        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
        canJumpImmediately = false;
    }

    /***
     * 获取已安装列表
     *
     * @return
     */
    private String getPackageInfo() {
        List<android.content.pm.PackageInfo> apps = AppUtils.getUserAppList(LTApplication.shareApplication());
        List<String> uploadApps = new ArrayList<>();
        if (apps != null) {
            for (android.content.pm.PackageInfo packageInfo : apps) {
                uploadApps.add(packageInfo.packageName);
            }
        }
        String str = new Gson().toJson(uploadApps);
        return str;
    }

    public void next() {
        if (canJump) {
            Intent localIntent = new Intent(LoadingActivity.this, MainActivity.class);
            getPushIntentData(localIntent);
            putDeeplinkIntentData(localIntent);
            goToNextActivity(localIntent);
        } else {
            canJump = true;
        }
    }

    public boolean canJumpImmediately = false;

    public void jumpWhenCanClick() {
        Log.d("test", "this.hasWindowFocus():" + this.hasWindowFocus());
        if (canJumpImmediately) {
            jump();
        } else {
            canJumpImmediately = true;
        }
    }

    public void jump() {
        Intent localIntent = new Intent(LoadingActivity.this, MainActivity.class);
        getPushIntentData(localIntent);
        putDeeplinkIntentData(localIntent);
        goToNextActivity(localIntent);
    }

    /**
     * 从deep link中获取数据
     */
    private void getDeepLinkData() {
        Uri data = getIntent().getData();
        try {
            if (data != null) {
                String scheme = data.getScheme(); // "will"

                if (!TextUtils.isEmpty(scheme) && "appcenter_chaoqian".equals(scheme)) {
                    isFromDeeplink = true;
                    deeplinkDataStr = data.toString().trim();
                    LogUtils.i(LogTAG.deepLinkTAG, "uriStr = " + deeplinkDataStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i(LogTAG.deepLinkTAG, "getDeepLinkData()抛异常了");
        }
    }

    public void putDeeplinkIntentData(Intent localIntent) {
        // deeplink相关数据
        localIntent.putExtra(Constant.EXTRA_IS_FROM_DEEPLINK, isFromDeeplink);
        localIntent.putExtra(Constant.EXTRA_DEEPLINK_DATA_STR, deeplinkDataStr);
    }
}
