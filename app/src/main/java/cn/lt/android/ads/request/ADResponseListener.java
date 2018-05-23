package cn.lt.android.ads.request;

import com.yolanda.nohttp.rest.Response;

import java.util.List;

import cn.lt.android.entity.AppBriefBean;

/**
 * Created by LinJunSheng on 2016/12/27.
 */

public interface ADResponseListener {
    void onStart(int what);
    void onSucceed(int pageType, Response response, List<AppBriefBean> adList);
    void onFailed(int what, Response response);
    void onFinish(int what);
    void setReady(boolean ready, int pageType);
    void setRefreshCallToNull(int pageType);
}
