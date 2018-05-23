package cn.lt.android.ads.request;

import android.content.Context;

import com.google.gson.Gson;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.bean.wdj.WDJAdsBean;
import cn.lt.android.ads.beantransfer.WDJAdTransfer;
import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.util.AdMd5;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CallServer;
import cn.lt.android.util.LogUtils;

import static cn.lt.android.ads.AdService.GAME_ESSENCE_AD;
import static cn.lt.android.ads.AdService.RECOMMEND_AD;
import static cn.lt.android.ads.AdService.SMART_LIST_AD;
import static cn.lt.android.ads.AdService.SOFTWARE_ESSENCE_AD;


/**
 * 豌豆荚广告数据请求器
 * Created by LinJunSheng on 2016/12/26.
 */

public class WDJRequester extends AbstractRequester{

    private static WDJRequester instance;
    private Context context;
    private ADResponseListener responseListener;

    public static WDJRequester getInstance(Context context) {
        if (instance == null) {
            synchronized (WDJRequester.class) {
                if (instance == null) {
                    instance =  new WDJRequester(context);
                }
            }
        }

        return instance;
    }

    private WDJRequester(Context context) {
        this.context = context;
    }

    private static final String WDJ_HOST = "http://api.wandoujia.com/v1/";

    private static final String WDJ_INSTALLED_REPORT = "http://click.wandoujia.com/installreport";

    /***
     * 豌豆荚key_id
     */
    public static final String key_id = "litianbaoli";

    /***
     * 豌豆荚key_value
     */
    public static final String key_value = "e27f5ae7acf44c7a829b74e5772a9397";

    /***
     * 豌豆荚广告列表(这个接口会返回豌豆荚精心挑选的广告。)
     * 请求方式：GET
     */
    public static final String getAdsList() {
        return WDJ_HOST + "adslist";
    }

    /**
     * 豌豆荚单个应用
     * 请求方式：GET
     */
    public static final String getSingleApp(String pkgName) {
        return WDJ_HOST + "apps/" + pkgName;
    }

    /***
     * 豌豆荚应用升级列表
     * 请求方式：POST
     */
    public static final String getUpdataAppList() {
        return WDJ_HOST + "update";
    }


    /***
     * 获取豌豆荚分类列表
     * 请求方式：GET
     */
    public static final String getCategoryList() {
        return WDJ_HOST + "categories";
    }

    /***
     * 获取分类榜单
     * 请求方式：GET
     */
    public static final String getCategoryApps() {
        return WDJ_HOST + "apps";
    }

    /***
     * 豌豆荚搜索接口
     *
     * @param keyWord
     * @return
     */
    public static final String searchApp(String keyWord) {
        return WDJ_HOST + "apps/" + keyWord;
    }

    private final String ASD_TYPE_ALL = "all";
    private final String ASD_TYPE_APP = "app";
    private final String ASD_TYPE_GAME = "game";

    /***
     * 获取广告数据
     *
     * @param pageType
     */
    @Override
    public void requestAdData(int pageType, AdRequestBean adRequestBean, ADResponseListener responseListener) {
        this.responseListener = responseListener;

        Request<String> request = NoHttp.createStringRequest(WDJRequester.getAdsList(), RequestMethod.GET);
        request.add("id", WDJRequester.key_id);
        request.add("timestamp", System.currentTimeMillis());
        request.add("token", AdMd5.MD5(WDJRequester.key_id + WDJRequester.key_value + System.currentTimeMillis()));
        request.add("ip", AppUtils.getWIFILocalIpAdress(context));
        request.add("phone_imei", AppUtils.getIMEI(context));
        request.add("phone_model", AppUtils.getDeviceName());
        request.add("mac_address ", AppUtils.getLocalMacAddress(context));
        request.add("api_level  ", AppUtils.getAndroidAPILevel() + "");//系统的API level
        request.add("startNum", adRequestBean.wanDouJia.startNum);//从第0个开始
        request.add("count", adRequestBean.wanDouJia.adCount);//最多取多少个，不能超过50

        // 获取广告类型
        switch (pageType) {
            case RECOMMEND_AD:
            case SMART_LIST_AD:
                request.add("adstype", ASD_TYPE_ALL);
                break;
            case SOFTWARE_ESSENCE_AD:
                request.add("adstype", ASD_TYPE_APP);
                break;
            case GAME_ESSENCE_AD:
                request.add("adstype", ASD_TYPE_GAME);
                break;
            default:
                request.add("adstype", ASD_TYPE_ALL);
                break;
        }
        CallServer.getRequestInstance().add(pageType, request, adListResponseListner);

    }

    private void setParams(int pageType, AdRequestBean adRequestBean) {



    }

    private OnResponseListener adListResponseListner = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            requestTime = System.currentTimeMillis();
            LogUtils.i(LogTAG.AdTAG, "豌豆荚广告列表请求开始");
            if(responseListener == null) {
                return;
            }

            responseListener.onStart(what);
        }

        @Override
        public void onSucceed(int pageType, Response response) {
            if(responseListener == null) {
                return;
            }

            int requestCode = response.getHeaders().getResponseCode();
            if (requestCode == 200) {
                LogUtils.i(LogTAG.AdTAG, "豌豆荚广告列表请求时间 = " + (System.currentTimeMillis() - requestTime));
                requestTime = System.currentTimeMillis();

                String result = (String) response.get();
//                String result = AppUtils.getLoaclJsonData("wdj_api_test.txt");
                LogUtils.i(LogTAG.AdTAG, "豌豆荚接口请求成功");

                JSONArray jsonArry = null;
                Gson gson = new Gson();
                try {
                    jsonArry = new JSONArray(result);
                    JSONObject jsonObj;

                    List<WDJAdsBean> adsList = new ArrayList<>();

                    for (int i = 0; i < jsonArry.length(); i++) {
                        jsonObj = jsonArry.getJSONObject(i);
                        WDJAdsBean adsBean = gson.fromJson(jsonObj.toString(), WDJAdsBean.class);
                        adsList.add(adsBean);
                    }

                    LogUtils.i(LogTAG.AdTAG, "豌豆荚广告数据解析成功");
                    responseListener.onSucceed(pageType, response, WDJAdTransfer.transferAdsList(adsList, pageType));
                    LogUtils.i(LogTAG.AdTAG, "豌豆荚广告数据准备完成时间 = " + (System.currentTimeMillis() - requestTime));
                } catch (Exception e) {
                    e.printStackTrace();

                    // json解析出现异常，走请求失败的流程
                    onFailed(pageType, response);
                }
            } else {
                onFailed(pageType, response);
            }


        }

        @Override
        public void onFailed(int what, Response response) {
            if(responseListener == null) {
                return;
            }

            responseListener.onFailed(what, response);
            LogUtils.i("zzz", "豌豆荚接口请求或解析json数据失败");
        }

        @Override
        public void onFinish(int what) {
            if(responseListener == null) {
                return;
            }

            responseListener.onFinish(what);
        }
    };

    @Override
    public void requestAdDetailData(String packageName, final AdAppDetailResponseListener responseListener) {
        Request<String> request = NoHttp.createStringRequest(WDJRequester.getSingleApp(packageName), RequestMethod.GET);
        request.add("id", WDJRequester.key_id);
        request.add("timestamp", System.currentTimeMillis());
        request.add("token", AdMd5.MD5(WDJRequester.key_id + WDJRequester.key_value + System.currentTimeMillis()));
        request.add("packageName", packageName);
        request.add("phone_imei ", AppUtils.getIMEI(context));
        request.add("mac_address", AppUtils.getLocalMacAddress(context));
        request.add("phone_model ", AppUtils.getDeviceName());
        CallServer.getRequestInstance().add(0, request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {
                responseListener.onStart();
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                String result = response.get();
                Gson gson = new Gson();
                try {

                    WDJAdsBean bean = gson.fromJson(result, WDJAdsBean.class);

                    AppDetailBean appDetailBean = WDJAdTransfer.convertToAppDetailBean(bean);
                    if(null != appDetailBean) {
                        responseListener.onSucceed(appDetailBean);
                    } else {
                        onFailed(what, response);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    onFailed(what, response);
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                responseListener.onFailed(response);
            }

            @Override
            public void onFinish(int what) {
                responseListener.onFinish();
            }
        });
    }

    /** 豌豆荚广告安装完成回传数据*/
    public static void wdjInstalledReport(final AppEntity app) {
        Request<String> request = NoHttp.createStringRequest(WDJ_INSTALLED_REPORT, RequestMethod.GET);
        request.add("phone_imei", AppUtils.getIMEI(LTApplication.instance));
        request.add("mac_address ", AppUtils.getLocalMacAddress(LTApplication.instance));
        request.add("ip", AppUtils.getWIFILocalIpAdress(LTApplication.instance));
        request.add("package_name", app.getPackageName());
        request.add("md5", app.getPackage_md5());
        request.add("api_level  ", AppUtils.getAndroidAPILevel() + "");//系统的API level
        request.add("id", WDJRequester.key_id);
        request.add("timestamp", System.currentTimeMillis());
        request.add("api_token", AdMd5.MD5(WDJRequester.key_id + WDJRequester.key_value + System.currentTimeMillis()));

        CallServer.getRequestInstance().add(0, request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {
                LogUtils.i(LogTAG.AdTAG, app.getName() + "  开始调用豌豆荚回传接口。。。");
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                int requestCode = response.getHeaders().getResponseCode();
                if (requestCode == 200) {
                    LogUtils.i(LogTAG.AdTAG, app.getName() + "  调用豌豆荚回传接口成功");
                } else {
                    onFailed(what, response);
                }
            }

            @Override
            public void onFailed(int what, Response<String> response) {
                LogUtils.i(LogTAG.AdTAG, app.getName() + "  调用豌豆荚回传接口失败");
            }

            @Override
            public void onFinish(int what) {

            }
        });
    }

}
