package cn.lt.android.ads;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.statistics.DCStat;

import static cn.lt.android.ads.AdService.GAME_ESSENCE_AD;
import static cn.lt.android.ads.AdService.RECOMMEND_AD;
import static cn.lt.android.ads.AdService.SOFTWARE_ESSENCE_AD;

/**
 * Created by LinJunSheng on 2017/1/9.
 */

public class AdReporter {
    private static List<AppBriefBean> recommend_adList = new ArrayList<>();
    private static List<AppBriefBean> softEssence_adList = new ArrayList<>();
    private static List<AppBriefBean> gameEssence_adList = new ArrayList<>();

    public synchronized static void add(int pageType, AppBriefBean adApp) {
        switch (pageType) {
            case RECOMMEND_AD:
                recommend_adList.add(adApp);
                break;
            case SOFTWARE_ESSENCE_AD:
                softEssence_adList.add(adApp);
                break;
            case GAME_ESSENCE_AD:
                gameEssence_adList.add(adApp);
                break;
        }
    }


    public synchronized static void reportShow(int pageType, int curPage) {
        String pageName = "";
        String adMold = "";
        List<AppBriefBean> adList = new ArrayList<>();
        switch (pageType) {
            case RECOMMEND_AD:
                adList.addAll(recommend_adList);
                pageName = Constant.PAGE_RECOMMEND;
                adMold = AdsTypeParams.RecommendAdType;
                break;
            case SOFTWARE_ESSENCE_AD:
                adList.addAll(softEssence_adList);
                pageName = Constant.PAGE_SOFT_HIGHLYSELECT;
                adMold = AdsTypeParams.softwareIndexAdType;
                break;
            case GAME_ESSENCE_AD:
                adList.addAll(gameEssence_adList);
                pageName = Constant.PAGE_GAME_HIGHLYSELECT;
                adMold = AdsTypeParams.GameIndexAdType;
                break;
        }

        if(adList.size() == 0) {
            return;
        }

        String adListStr = jointListStr(adList);
        DCStat.adShow(adListStr, pageName, curPage, adMold);

        clearAdList(pageType);
    }


    private static String jointListStr(List<AppBriefBean> adList) {
        String adListStr = "";
        for (AppBriefBean adApp : adList) {
            adListStr += adApp.getPackage_name() + "/" + adApp.getVersion_name() + "/" + adApp.getVersion_code() + " | ";
        }

        adListStr = adListStr.substring(0, adListStr.lastIndexOf(" | "));

        return adListStr;
    }

    private static void clearAdList(int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                recommend_adList.clear();
                break;
            case SOFTWARE_ESSENCE_AD:
                softEssence_adList.clear();
                break;
            case GAME_ESSENCE_AD:
                gameEssence_adList.clear();
                break;
        }
    }
}
