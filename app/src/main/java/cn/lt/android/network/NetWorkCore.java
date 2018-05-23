package cn.lt.android.network;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.network.bean.HeaderParams;
import cn.lt.android.network.bean.HostBean;
import cn.lt.android.network.callback.LTInitCallback;
import cn.lt.android.network.dao.InitHostInterfaceDao;
import cn.lt.android.network.retrofit2.factory.gson.GsonConverterFactory;
import cn.lt.android.network.retrofit2.factory.ltdata.LTDataConverterFactory;
import cn.lt.android.network.retrofit2.factory.ltdata.LTUserCenterDataConverterFactory;
import cn.lt.android.util.AdMd5;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Administrator on 2016/2/26.
 */
public class NetWorkCore {
    private Retrofit baseHostRetrofit, dCenterHostRetrofit, gCenterHostRetrofit, uCenterHostRetrofit, cdnLimitHostRetrofit;
    private boolean getBaseHosted;
    private String headerParams;
    private String token = "";
    private HeaderParams head;
    //正在做base请求
    private boolean initBaseRequestIng = false;
    //base请求是否已经完成
    private boolean initBaseRequestCompleted = false;

    private List<LTInitCallback> initCallbackList = new ArrayList<>();
    private Retrofit gNormalCenterHostRetrofit;

    private NetWorkCore() {
        baseHostRetrofit = new Retrofit.Builder().baseUrl(GlobalConfig.BASE_HOST).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public void initNetWorkConfig(Context context) {
        head = new HeaderParams(context);
        Gson gson = new Gson();
        headerParams = gson.toJson(head);
    }

    private static NetWorkCore mInstance;

    public static NetWorkCore getInstance() {
        if (mInstance == null) {
            synchronized (NetWorkCore.class) {
                mInstance = new NetWorkCore();
            }
        }
        return mInstance;

    }

    /***
     * 释放回调列表
     */
    public void release() {
        initCallbackList.clear();
    }

    /***
     * 异步获取Base请求
     *
     * @param callback
     */
    public synchronized void baseHost(LTInitCallback callback) {
        //若已经请求完成,则直接回掉
        if (initBaseRequestCompleted) {
            if (checkBaseHost()) {
                callback.onSuccess();
            } else {
                callback.error(new Exception("初始化失败"));
            }
            return;
        }
        //回掉加入
        initCallbackList.add(callback);
        //若正在请求中，则不再请求
        if (initBaseRequestIng) {
            return;
        }
        initBaseRequestIng = true;
        InitHostInterfaceDao initHostInterfaceDao = baseHostRetrofit.create(InitHostInterfaceDao.class);
        Call<HostBean> call = initHostInterfaceDao.baseHost();
        call.enqueue(new Callback<HostBean>() {
            @Override
            public void onResponse(Call<HostBean> call, Response<HostBean> response) {
                synchronized (NetWorkCore.class) {
                    if (response.code() >= 200 && response.code() < 300) {
                        getBaseHosted = true;
                        LogUtils.i("NetWorkCore", "code=" + response.code());
                        LogUtils.i("NetWorkCore", "bean.dcenter_host=" + response.body().getDcenter_host());
                        LogUtils.i("NetWorkCore", "bean.ucenter_host=" + response.body().getUcenter_host());
                        LogUtils.i("NetWorkCore", "bean.gcenter_host=" + response.body().getAcenter_host());
                        LogUtils.i("NetWorkCore", "bean.getWeixin_host=" + response.body().getWeixin_host());
                        GlobalParams.setHostBean(response.body());
                        Interceptor interceptor = new Interceptor() {
                            @Override
                            public okhttp3.Response intercept(Chain chain) throws IOException {
                                Gson gson = new Gson();
                                head.setMemory_size(AppUtils.getAvailablMemorySize());          //这里重组头部信息，避免缓存引起的内存上报不准确 By ATian 2016/9/21
                                headerParams = gson.toJson(head);
                                LogUtils.i("NetWorkCore", "current available memory size==" + head.getMemory_size());
                                Request newRequest = chain.request().newBuilder().addHeader("X-Client-Info", headerParams).build();
                                return chain.proceed(newRequest);
                            }
                        };
                        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(interceptor).build();
                        dCenterHostRetrofit = new Retrofit.Builder().baseUrl(response.body().getDcenter_host() + File.separator).addConverterFactory(LTDataConverterFactory.create()).client(client).build();
                        gCenterHostRetrofit = new Retrofit.Builder().baseUrl(response.body().getAcenter_host() + File.separator).addConverterFactory(LTDataConverterFactory.create()).client(client).build();
                        uCenterHostRetrofit = new Retrofit.Builder().baseUrl(response.body().getUcenter_host() + File.separator + "api/").client(client).build();
                        cdnLimitHostRetrofit = new Retrofit.Builder().baseUrl(response.body().getCdnlimit_host() + File.separator).addConverterFactory(GsonConverterFactory.create()).client(client).build();
                        gNormalCenterHostRetrofit = new Retrofit.Builder().baseUrl(response.body().getAcenter_host() + File.separator).addConverterFactory(GsonConverterFactory.create()).client(client).build();
                        for (LTInitCallback initCallback : initCallbackList) {
                            initCallback.onSuccess();
                        }
                    } else {
                        for (LTInitCallback initCallback : initCallbackList) {
                            initCallback.error(new Exception("error code:" + response.code()));
                        }

                    }
                }
                initBaseRequestCompleted = true;
                initBaseRequestIng = false;
            }

            @Override
            public void onFailure(Call<HostBean> call, Throwable t) {
                LogUtils.i("NetWorkCore", "onFailure:" + t.getMessage());
                GlobalParams.setHostBean(new HostBean());
                initCallbackList.clear();
                synchronized (NetWorkCore.class) {
                    for (LTInitCallback initCallback : initCallbackList) {
                        initCallback.error(t);
                    }
                }
                initBaseRequestCompleted = false;
                initBaseRequestIng = false;
            }
        });

    }

    public boolean checkBaseHost() {
        return getBaseHosted;
    }

    public Retrofit getdCenterHostRetrofit() {
        return dCenterHostRetrofit;
    }

    public Retrofit getgCenterHostRetrofit() {
        return gCenterHostRetrofit;
    }

    public Retrofit getgNormalCenterHostRetrofit() {
        return gNormalCenterHostRetrofit;
    }

    public Retrofit getCdnLimitHostRetrofit() {
        return cdnLimitHostRetrofit;
    }


    public Retrofit getuCenterHostRetrofit(final String uri) {
        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                LogUtils.i("NetWorkCore", "uri:" + uri);
                Request newRequest = chain.request().newBuilder().addHeader("SIGN", AdMd5.MD5("QnAyFtAJf4" + File.separator + uri + head.uuid + "QnAyFtAJf4")).addHeader("X-Client-Info", headerParams).addHeader("PARAMS", headerParams).addHeader("SALT", "QnAyFtAJf4").addHeader("TOKEN", token).build();
                return chain.proceed(newRequest);
            }
        };
        OkHttpClient client = new OkHttpClient().newBuilder().addInterceptor(interceptor).build();
        uCenterHostRetrofit = new Retrofit.Builder().baseUrl(uCenterHostRetrofit.baseUrl().url().toString()).addConverterFactory(LTUserCenterDataConverterFactory.create()).client(client).build();
        return uCenterHostRetrofit;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
        try {
            JSONObject json = new JSONObject(headerParams);
            if (getToken() != null) {
                json.put("access_token", getToken());
            } else {
                json.put("access_token", "");
            }
            headerParams = json.toString();
        } catch (JSONException e) {
        }
    }


}
