package cn.lt.android.ads.request;

/**
 * Created by LinJunSheng on 2016/12/27.
 */

public abstract class AbstractRequester {
    protected long requestTime = 0;
    public abstract void requestAdData(int pageType, AdRequestBean adRequestBean, ADResponseListener responseListener);
    public abstract void requestAdDetailData(String packageName, AdAppDetailResponseListener responseListener);
}
