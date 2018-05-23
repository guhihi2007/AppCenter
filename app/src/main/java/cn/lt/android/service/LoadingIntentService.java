package cn.lt.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.ads.bean.WhiteListBean;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.ads.splash.SplashManager;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.entity.APPUpGradeBlackListBean;
import cn.lt.android.entity.AdsTypeBean;
import cn.lt.android.entity.AdvertisingConfigBean;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.entity.MarketResourceBean;
import cn.lt.android.entity.ProportionBean;
import cn.lt.android.entity.SplashShowTimeBean;
import cn.lt.android.main.LoadingImgWorker;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.GsonUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Erosion on 2018/3/19.
 */

public class LoadingIntentService extends IntentService {
    public LoadingIntentService() {
        super("LoadingIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("Erosion","onCreate");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        LogUtils.i("Erosion","onHandleIntent");
        requestData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i("Erosion","onDestroy");
    }

    private void requestData() {

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<BaseBeanList<BaseBean>>() {

            @Override
            public void onResponse(Call call, Response response) {
                BaseBeanList<BaseBean> base = (BaseBeanList<BaseBean>) response.body();
                if (base != null && base.size() > 0) {
                    for (int i = 0; i < base.size(); i++) {
                        BaseBean baseBean = base.get(i);
                        switch (PresentType.valueOf(baseBean.getLtType())) {
                            case start_image:
//                                AdsImageBean mBean = (AdsImageBean) baseBean;
                                handleLaunchImageInfo((AdsImageBean) baseBean);
                                LogUtils.i("Erosion","start_image");
                                break;
                            case white_list:
                                BaseBeanList<WhiteListBean> packageInfoList = (BaseBeanList<WhiteListBean>) baseBean;
                                List<WhiteListBean> whiteListBeenList = new ArrayList<WhiteListBean>();
                                whiteListBeenList.addAll(packageInfoList);
                                LTApplication.appWhiteList = AppBeanTransfer.transferWhiteList(packageInfoList);
                                LogUtils.i("Erosion","white_list");
                                break;
                            case popup:
                                ConfigureBean config = (ConfigureBean) baseBean;
                                if (null != config) {
                                    try {
                                        PopWidowManageUtil.saveConfigInfo(LTApplication.instance, config);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                boolean baiduState = PreferencesUtils.getBoolean(LTApplication.instance,Constant.BAIDU_STATUS);
                                boolean gdtState = PreferencesUtils.getBoolean(LTApplication.instance,Constant.GDT_STATUS);

                                if (baiduState || gdtState) {
                                    advertisingConfig();
                                }
                                LogUtils.i("Erosion","popup");
                                break;
                            case ads_type:
                                AdsTypeBean adsTypeBean = (AdsTypeBean) baseBean;
                                if (null != adsTypeBean) {
                                    saveAdsType(adsTypeBean);
                                }
                                LogUtils.i("Erosion","ads_type");
                                break;
                            case app_market_source:
                                MarketResourceBean marketResourceBean = (MarketResourceBean) baseBean;
                                if (null != marketResourceBean) {
                                    saveMarketResourceBean(marketResourceBean);
                                }
                                LogUtils.i("Erosion","app_market_source");
                                break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                PreferencesUtils.putBoolean(LTApplication.instance, Constant.GDT_STATUS, false);
                PreferencesUtils.putBoolean(LTApplication.instance, Constant.BAIDU_STATUS, false);
            }
        }).bulid().requestPreloading();
//        startRequestTime = System.currentTimeMillis();

        requestAutoUpgradeAppBlacklist();
    }

    private void requestAutoUpgradeAppBlacklist() {
        // 从接口获取是否黑名单
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<APPUpGradeBlackListBean>() {
            @Override
            public void onResponse(Call<APPUpGradeBlackListBean> call, Response<APPUpGradeBlackListBean> response) {
                APPUpGradeBlackListBean bean = response.body();
                if (bean != null) {
                    boolean isBalcklist = bean.is_black_list();
                    if (!isBalcklist) {
                        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "no");
                    } else {
                        SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "yes");
                    }
                    SharePreferenceUtil.putFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.GET_IS_BLACKLIST_TIME, System.currentTimeMillis());
                }
            }

            @Override
            public void onFailure(Call<APPUpGradeBlackListBean> call, Throwable t) {

            }
        }).bulid().requestBlacklist();
    }

    private void handleLaunchImageInfo(final AdsImageBean ads) {
        if (ads != null) {
            try {
                Set<String> exposureApps = WanKaManager.exposureSingleApp(ads, new SimpleResponseListener<JSONObject>() {
                    @Override
                    public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                        LoadingImgWorker.getInstance().setmBean(ads);
                    }

                    @Override
                    public void onFailed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {

                    }
                }, "启动页曝光");

                LoadingImgWorker.getInstance().setmBean(ads);
                LoadingImgWorker.getInstance().downloadImg();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveAdsType(AdsTypeBean adsTypeBean) {
        adsTypeBean.setPageAdType();
    }

    private void saveMarketResourceBean(MarketResourceBean marketResourceBean) {
        String netInstallPkg = marketResourceBean.getMarket_source();
        if (!"default".equals(netInstallPkg)) {
            PreferencesUtils.putString(this, Constant.INSTALL_PKG, netInstallPkg);
//            GlobalConfig.NET_INSTALL_PACKAGE =  Constant.DEF_INSTALL_PKG;
        } else {
//            GlobalConfig.NET_INSTALL_PACKAGE = netInstallPkg;
            PreferencesUtils.putString(this, Constant.INSTALL_PKG, Constant.DEF_INSTALL_PKG);
        }
    }

    /**
     * 获取开屏广告配置
     */
    private void advertisingConfig() {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<AdvertisingConfigBean>() {

            @Override
            public void onFailure(Call<AdvertisingConfigBean> call, Throwable t) {
                LogUtils.i("Erosion","ttt:" + t.getMessage().toString());
                PreferencesUtils.putString(LTApplication.instance, SplashManager.BAI_DU_SPLASH_SHOW_TIME, "");
                PreferencesUtils.putString(LTApplication.instance,SplashManager.GDT_SPLASH_SHOW_TIME, "");
                PreferencesUtils.putInt(LTApplication.instance,SplashManager.BAIDU_SPLASH_PROPORTION,0);
                PreferencesUtils.putInt(LTApplication.instance,SplashManager.GDT_SPLASH_PROPORTION,0);
            }

            @Override
            public void onResponse(Call<AdvertisingConfigBean> call, Response<AdvertisingConfigBean> response) {
                AdvertisingConfigBean bean = response.body();
                if (bean != null) {
                    List<SplashShowTimeBean> baiDuShowTimeBeans = bean.getSplash().getBaidu_ads();
                    List<SplashShowTimeBean> gdtShowTimeBeans = bean.getSplash().getGuangdiantong_ads();
                    ProportionBean proportionBean = bean.getSplash().getProportion();

                    if (null != baiDuShowTimeBeans && baiDuShowTimeBeans.size() > 0) {
                        PreferencesUtils.putString(LTApplication.instance,SplashManager.BAI_DU_SPLASH_SHOW_TIME, GsonUtils.GsonString(baiDuShowTimeBeans));
                    } else {
                        PreferencesUtils.putString(LTApplication.instance,SplashManager.BAI_DU_SPLASH_SHOW_TIME, "");
                    }
                    if (null != gdtShowTimeBeans && gdtShowTimeBeans.size() > 0) {
                        PreferencesUtils.putString(LTApplication.instance,SplashManager.GDT_SPLASH_SHOW_TIME, GsonUtils.GsonString(gdtShowTimeBeans));
                    } else {
                        PreferencesUtils.putString(LTApplication.instance,SplashManager.GDT_SPLASH_SHOW_TIME, "");
                    }
                    LogUtils.i("Erosion","onResponse====" + bean.getSplash().getBaidu_ads().size());

                    if (null != proportionBean) {
                        PreferencesUtils.putInt(LTApplication.instance,SplashManager.BAIDU_SPLASH_PROPORTION,proportionBean.getBaidu_ads());
                        PreferencesUtils.putInt(LTApplication.instance,SplashManager.GDT_SPLASH_PROPORTION,proportionBean.getGuangdiantong_ads());
                    }
                } else {
                    PreferencesUtils.putString(LTApplication.instance,SplashManager.BAI_DU_SPLASH_SHOW_TIME, "");
                    PreferencesUtils.putString(LTApplication.instance,SplashManager.GDT_SPLASH_SHOW_TIME, "");
                    PreferencesUtils.putInt(LTApplication.instance,SplashManager.BAIDU_SPLASH_PROPORTION,0);
                    PreferencesUtils.putInt(LTApplication.instance,SplashManager.GDT_SPLASH_PROPORTION,0);
                }
            }
        }).bulid().advertisingConfig();
    }
}
