package cn.lt.android.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import cn.lt.android.network.bean.NetWorkConfig;
import cn.lt.android.network.callback.LTInitCallback;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.network.netdata.bean.NetResponse;
import cn.lt.android.util.LogUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Created by Administrator on 2016/3/3.
 */
class NetWorkProxy implements InvocationHandler {
    private NetWorkConfig config;
    private NetWorkCore netWorkCore;

    public NetWorkProxy(NetWorkConfig config) {
        this.config = config;
        netWorkCore = NetWorkCore.getInstance();
    }

    @Override
    public Object invoke(Object o, final Method method, final Object[] objects) throws Throwable {
//        LogUtils.i("NetWorkClient", "method:" + method.getName());
        if (!netWorkCore.checkBaseHost()) {
            netWorkCore.baseHost(new LTInitCallback() {
                @Override
                public void onSuccess() {
//                    config.getCallback().onFailure(null, new Exception("baseHost没有被初始化,此次请求初始化host"));
                    try {
                        postRequest(method, objects);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void error(Throwable t) {
                    config.getCallback().onFailure(null, t);
                }
            });
            return null;
        }
        Call<BaseBean> call = postRequest(method, objects);
        return call;
    }

    @NonNull
    private Call<BaseBean> postRequest(Method method, Object[] objects) throws Exception {
        Retrofit       retrofit = getRetrofit(config.getHostType(),method);
        Call<BaseBean> call     = (Call<BaseBean>) method.invoke(retrofit.create(NetDataInterfaceDao.class), objects);//第一个参数：创建接口的实现类对象
//        LogUtils.i("NetWorkClient", "url:" + call.request().url());

        LogUtils.i("NetWorkClient", "method:" + method.getName() + "/t url: " + call.request().url());

        call.enqueue(new Callback<BaseBean>() {
            @Override
            public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                if (response.code() >= 200 && response.code() < 300) {
                    // 为了兼容用户中心错误数据的那种格式
                    if (HostType.UCENETER_HOST == config.getHostType()) {
                        userCenterCallBack(call, config.getCallback(), response);
                    }else{
                        config.getCallback().onResponse(call, response);
                    }
                } else {
                    config.getCallback().onFailure(call, new Exception("error code:" + response.code()));
                }
            }

            @Override
            public void onFailure(Call<BaseBean> call, Throwable t) {
                config.getCallback().onFailure(call, t);
            }
        });
        return call;
    }

    private Retrofit getRetrofit(HostType hostType,Method method) throws Exception {
        Retrofit retrofit = null;
        if (HostType.GCENTER_HOST == hostType) {
            retrofit = netWorkCore.getgCenterHostRetrofit();
        } else if (HostType.DCENTER_HOST == hostType) {
            retrofit = netWorkCore.getdCenterHostRetrofit();
        } else if (HostType.UCENETER_HOST == hostType) {
            //retrofit = netWorkCore.getuCenterHostRetrofit();
            Annotation[] annotations = method.getAnnotations();
            final String uri = getUriFromArr(annotations);
            retrofit = netWorkCore.getuCenterHostRetrofit(uri);
        } else if (HostType.NORMAL_CENTER == hostType) {
            retrofit = netWorkCore.getgNormalCenterHostRetrofit();
        } else {
            throw new Exception("没有对应的host");
        }
        return retrofit;
    }

    private String getUriFromArr(Annotation[] annotationArr) {
        Annotation annotation;
        String uri = "";
        for (int i = 0; i < annotationArr.length; i++) {
            annotation = annotationArr[i];
            if(TextUtils.isEmpty(uri)){
                uri = getUri(annotation);
            }else{
                break;
            }
        }
        return uri;
    }

    private String getUri(Annotation annotation) {
        String uri = "";
        if (annotation instanceof GET) {
            uri = ((GET) annotation).value();
        } else if (annotation instanceof POST) {
            uri = ((POST) annotation).value();
        } else if (annotation instanceof PUT) {
            uri = ((PUT) annotation).value();
        } else if (annotation instanceof DELETE) {
            uri = ((DELETE) annotation).value();
        }
        return uri;
    }

    private void userCenterCallBack(Call<BaseBean> call,Callback callback,Response response){
        NetResponse netResponse = (NetResponse)response.body();
        if(netResponse.getStatus()==NetFlag.userSuccess){
            Gson gson = new Gson();
            Response responseCallBack;
            if (config.getCls()!=null) {
                responseCallBack = Response.success(gson.fromJson(netResponse.getData().toString(), (Type) config.getCls()), response.raw());
            }else{

                responseCallBack = Response.success(netResponse.getMessage(), response.raw());
            }
//            Response responseCallBack = Response.success(((NetResponse) response.body()).getData(), response.raw());
            callback.onResponse(call, responseCallBack);
        }else{
            callback.onFailure(call,new Exception(netResponse.getMessage()));
        }
    }
}
