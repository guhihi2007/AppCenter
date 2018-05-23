package cn.lt.android.ads.request;

import cn.lt.android.entity.AppDetailBean;

/**
 * Created by LinJunSheng on 2017/1/3.
 */

public interface AdAppDetailResponseListener {
    void onStart();
    void onSucceed(AppDetailBean appDetailBean);
    void onFailed(com.yolanda.nohttp.rest.Response<String> response);
    void onFinish();
}
