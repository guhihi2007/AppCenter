package cn.lt.android.ads;

import cn.lt.android.LTApplication;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.util.AppUtils;

import static cn.lt.android.ads.AdService.GAME_ESSENCE_AD;
import static cn.lt.android.ads.AdService.ONE_OFF_AD;
import static cn.lt.android.ads.AdService.RANK_AD;
import static cn.lt.android.ads.AdService.RECOMMEND_AD;
import static cn.lt.android.ads.AdService.SMART_LIST_AD;
import static cn.lt.android.ads.AdService.SOFTWARE_ESSENCE_AD;

/**
 * Created by LinJunSheng on 2016/7/12.
 */

public class WDJAdDownloadUrlJointer {

    private static final String LT_ID = "litianbaoli";

    public static void joint(AppBriefBean appBriefBean, int pageType) {
        String jointUrl = appBriefBean.getDownload_url();

        // 连接符
        String connectMark = getConnectMark(jointUrl);

        String pos = getPos(pageType);
        LTApplication context = LTApplication.shareApplication();
        jointUrl = jointUrl + connectMark +
                    "pos=" + pos + "&" +
                    "download_type=" + "download" + "&" +
                    "tokenId=" + LT_ID + "&" +
                    "phone_imei=" + AppUtils.getIMEI(context) + "&" +
                    "mac_address=" + AppUtils.getLocalMacAddress(context) + "&" +
                    "phone_model=" + AppUtils.getDeviceName() + "&" +
                    "api_level=" +  AppUtils.getAndroidAPILevel();

        appBriefBean.setDownload_url(jointUrl);

    }

    private static String getConnectMark(String downloadUrl) {
        if(downloadUrl.contains("?")) {
//            Log.i(LogTAG.AdTAG, "包含问号， 拼接符号返回 “&”");
            return "&";
        }
//        Log.i(LogTAG.AdTAG, "没有包含问号， 拼接符号返回 “？”");
        return "?";
    }

    private static String getPos(int pageType) {
        String pos = "open/other_";
        switch (pageType) {
            case RECOMMEND_AD :
                pos = pos + "recommend";
                break;
            case SOFTWARE_ESSENCE_AD :
                pos = pos + "softwareEssence";
                break;
            case GAME_ESSENCE_AD :
                pos = pos + "gameEssence";
                break;
            case RANK_AD :
                break;
            case ONE_OFF_AD:
                break;
            case SMART_LIST_AD :
                break;
        }
        return pos;
    }
}
