package cn.lt.android.ads.request;

import android.content.Context;

import static cn.lt.android.ads.AdMold.NO_AD;
import static cn.lt.android.ads.AdMold.WanDouJia;
import static cn.lt.android.ads.AdMold.WanKa;

/**
 * Created by LinJunSheng on 2017/1/4.
 */

public class AdRequesterFactory {

    public static AbstractRequester produceAdRequester (Context context, String adMold) {
        switch (adMold) {
            case WanDouJia:
                return WDJRequester.getInstance(context);
            case WanKa:
//                return WKRequester.getInstance(context);
            case NO_AD:
            default:
                return null;
        }
    }
}
