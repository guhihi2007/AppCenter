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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.bean.wk.WKAdDetailBean;
import cn.lt.android.ads.bean.wk.WKAdsBean;
import cn.lt.android.ads.beantransfer.WKAdTransfer;
import cn.lt.android.ads.wanka.WanKa;
import cn.lt.android.ads.wanka.WanKaUrl;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.util.AdMd5;
import cn.lt.android.util.CallServer;
import cn.lt.android.util.LogUtils;

/**
 * 玩咖广告数据请求器
 * Created by LinJunSheng on 2016/12/26.
 */

public class WKRequester extends AbstractRequester {

    private static WKRequester instance;
    private final Context context;
    private ADResponseListener responseListener;

    private int totalCount;
    private int hasMore = -1;
    private int NO = 0;
    private int Yes = 1;

    private final static String WK_LIST_Url = WanKaUrl.HOST + "/api/v2/apps/list";
    private final static String WK_DETAIL_Url = WanKaUrl.HOST + "/api/v2/apps/detail";
    private static final String KEY_REPORTDATA = "reportData";

    public static WKRequester getInstance(Context context) {
        if (instance == null) {
            synchronized (WKRequester.class) {
                if (instance == null) {
                    instance = new WKRequester(context);
                }
            }
        }

        return instance;
    }

    private WKRequester(Context context) {
        this.context = context;
    }

    private static class WKRequestBean {
        String reportData;
        String sign;

        WKRequestBean(String reportData, String sign) {
            this.reportData = reportData;
            this.sign = sign;
        }
    }

    @Override
    public void requestAdData(int pageType, AdRequestBean adRequestBean, ADResponseListener responseListener) {
        this.responseListener = responseListener;

        Map<String, String> commonParams = new TreeMap<>();
        commonParams.put(WanKa.APP_ID, WanKaUrl.WANKA_APP_ID);
        commonParams.put(WanKa.CHANNEL_ID, WanKaUrl.WANKA_CHANNEL_ID);
        commonParams.put("from_client", "server");
        commonParams.put("pn", String.valueOf(adRequestBean.wanKa.curPage));
        commonParams.put("rn", String.valueOf(adRequestBean.wanKa.adCount));
        commonParams.put("timestamp", String.valueOf(System.currentTimeMillis()));
        commonParams.put("from_client", "server");

        String sign = getWKSign(commonParams);
        commonParams.put("sign", sign);

        String requesAdListUrl = jointUrl(commonParams, WK_LIST_Url + "?");
        LogUtils.i(LogTAG.AdTAG, requesAdListUrl);
        Request<String> request = NoHttp.createStringRequest(requesAdListUrl, RequestMethod.GET);
        CallServer.getRequestInstance().add(pageType, request, adListResponseListner);
        requestTime = System.currentTimeMillis();
    }

    private String getWKSign(Map<String, String> commonParams) {

        // 需要签名的字符串
        String signStr = "";
        for (Map.Entry<String, String> entry : commonParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                signStr += (key + "=" + URLEncoder.encode(value, "UTF-8") + "&");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        signStr = signStr.substring(0, signStr.lastIndexOf("&"));
        signStr += WanKaUrl.WANKA_APP_SECRET;

        LogUtils.i(LogTAG.AdTAG, "玩咖sign = " + signStr);

        String signMD5 = AdMd5.MD5(signStr);
        LogUtils.i(LogTAG.AdTAG, "玩咖signMD5 = " + signMD5);

        return signMD5;
    }

    private String jointUrl(Map<String, String> commonParams, String url) {

        for (Map.Entry<String, String> entry : commonParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            try {
                if (value == null) {
                    value = "";
                }
                url += (key + "=" + URLEncoder.encode(value, "UTF-8") + "&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        return url.substring(0, url.lastIndexOf("&"));
    }



    private OnResponseListener adListResponseListner = new OnResponseListener() {
        @Override
        public void onStart(int what) {
            if (responseListener == null) {
                return;
            }

            responseListener.onStart(what);
        }

        @Override
        public void onSucceed(int pageType, Response response) {
            if (responseListener == null) {
                return;
            }
//
            int requestCode = response.getHeaders().getResponseCode();
            if (requestCode == 200) {

                try {
                    JSONObject jsonObject = new JSONObject((String) response.get());
//                    JSONObject jsonObject = new JSONObject(AppUtils.getLoaclJsonData("wanka_api_test.txt"));
                    int result = jsonObject.getInt("result");

                    if (result != 0) {
                        onFailed(pageType, response);
                        LogUtils.i(LogTAG.AdTAG, "玩咖广告API接口请求有误, 有返回数据：result = " + result + ", msg = " + jsonObject.getString("msg"));
                        return;
                    }

                    JSONObject jsonContent = jsonObject.getJSONObject("content");

                    JSONArray jsonList = jsonContent.getJSONArray("list");

                    Gson gson = new Gson();
                    List<WKAdsBean> adsList = new ArrayList<>();
                    JSONObject adApp;

                    for (int i = 0; i < jsonList.length(); i++) {
                        adApp = jsonList.getJSONObject(i);
                        WKAdsBean adsBean = gson.fromJson(adApp.toString(), WKAdsBean.class);
                        adsBean.setPackageName(adApp.getString("package"));
                        adsList.add(adsBean);
                    }
                    LogUtils.i(LogTAG.AdTAG, "玩咖广告API接口请求成功，实体转换成功");

                    responseListener.onSucceed(pageType, response, WKAdTransfer.transferAdsList(adsList, pageType));
                    LogUtils.i(LogTAG.AdTAG, "玩咖广告列表准备完成时间 = " + (System.currentTimeMillis() - requestTime));

                    totalCount = jsonContent.getInt("total_cnt");
                    hasMore = jsonContent.getInt("has_more");
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
            if (responseListener == null) {
                return;
            }

            responseListener.onFailed(what, response);
            LogUtils.i(LogTAG.AdTAG, "玩咖广告API接口请求或解析json数据失败");
        }

        @Override
        public void onFinish(int what) {
            if (responseListener == null) {
                return;
            }

            responseListener.onFinish(what);
        }
    };

    @Override
    public void requestAdDetailData(String packageName, final AdAppDetailResponseListener responseListener) {
        Map<String, String> commonParams = WanKa.getCommonParams(LTApplication.instance);
        commonParams.put(KEY_REPORTDATA, "");

        String requesAdDetailUrl = jointUrl(commonParams, WK_DETAIL_Url + "?");
        // 拼接请求页码和个数到url
        requesAdDetailUrl += requesAdDetailUrl + "package=" + packageName;

        Request<String> request = NoHttp.createStringRequest(requesAdDetailUrl, RequestMethod.POST);

        String sign = getWKSign(commonParams);
        WKRequestBean requestBean = new WKRequestBean("", sign);

        request.setDefineRequestBodyForJson(new Gson().toJson(requestBean));
        CallServer.getRequestInstance().add(0, request, new OnResponseListener<String>() {
            @Override
            public void onStart(int what) {
                if (responseListener == null) {
                    return;
                }

                responseListener.onStart();
            }

            @Override
            public void onSucceed(int what, Response<String> response) {
                if (responseListener == null) {
                    return;
                }

                int requestCode = response.getHeaders().getResponseCode();
                if (requestCode == 200) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.get());
                        int result = jsonObject.getInt("result");

                        if (result != 0) {
                            onFailed(0, response);
                            LogUtils.i(LogTAG.AdTAG, "玩咖广告详情API接口请求有误, 有返回数据：result = " + result + ", msg = " + jsonObject.getString("msg"));
                            return;
                        }

                        JSONObject jsonContent = jsonObject.getJSONObject("content");
                        Gson gson = new Gson();

                        WKAdDetailBean detailBean = gson.fromJson(jsonContent.toString(), WKAdDetailBean.class);
                        detailBean.setPackageName(jsonContent.getString("package"));

                        AppDetailBean appDetailBean = WKAdTransfer.convertToAppDetailBean(detailBean);
                        if (null != appDetailBean) {
                            responseListener.onSucceed(appDetailBean);
                        } else {
                            onFailed(what, response);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        onFailed(what, response);
                    }

                }

            }

            @Override
            public void onFailed(int what, Response<String> response) {
                if (responseListener == null) {
                    return;
                }

                responseListener.onFailed(response);
            }

            @Override
            public void onFinish(int what) {
                if (responseListener == null) {
                    return;
                }

                responseListener.onFinish();
            }
        });
    }

}
