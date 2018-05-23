package cn.lt.android.ads;


import cn.lt.android.LogTAG;
import cn.lt.android.util.LogUtils;

import static cn.lt.android.ads.AdService.GAME_ESSENCE_AD;
import static cn.lt.android.ads.AdService.ONE_OFF_AD;
import static cn.lt.android.ads.AdService.RANK_AD;
import static cn.lt.android.ads.AdService.RECOMMEND_AD;
import static cn.lt.android.ads.AdService.SOFTWARE_ESSENCE_AD;

/**
 * Created by LinJunSheng on 2016/7/4.
 */

public class AdTimeRecorder {

    private long recommendAdRequestTime = 0;
    private long softwareEssenceAdRequestTime = 0;
    private long gameEssenceAdRequestTime = 0;
    private long rankAdRequestTime = 0;
    private long catDetailAdRequestTime = 0;

    private long THIRTY_SECONDS = 30000;

    public boolean isDirty (int adType) {
        long curTime = System.currentTimeMillis();
        switch (adType) {
            case RECOMMEND_AD :
                if(recommendAdRequestTime == 0) return false;
                return (curTime - recommendAdRequestTime) >= THIRTY_SECONDS;
            case SOFTWARE_ESSENCE_AD :
                if(softwareEssenceAdRequestTime == 0) return false;
                return (curTime - softwareEssenceAdRequestTime) >= THIRTY_SECONDS;
            case GAME_ESSENCE_AD:
                if(gameEssenceAdRequestTime == 0) return false;
                return (curTime - gameEssenceAdRequestTime) >= THIRTY_SECONDS;
            case RANK_AD :
                if(rankAdRequestTime == 0) return false;
                return (curTime - rankAdRequestTime) >= THIRTY_SECONDS;
            case ONE_OFF_AD:
                if(catDetailAdRequestTime == 0) return false;
                return (curTime - catDetailAdRequestTime) >= THIRTY_SECONDS;
            default:
                return false;
        }
    }

    public void setRequestTime(int adType) {
        LogUtils.i(LogTAG.AdTAG, "请求时间类型 = " + adType);
        switch (adType) {
            case RECOMMEND_AD :
                recommendAdRequestTime = System.currentTimeMillis();
                break;
            case SOFTWARE_ESSENCE_AD :
                softwareEssenceAdRequestTime = System.currentTimeMillis();
                break;
            case GAME_ESSENCE_AD:
                gameEssenceAdRequestTime = System.currentTimeMillis();
                break;
            case RANK_AD :
                rankAdRequestTime = System.currentTimeMillis();
                break;
            case ONE_OFF_AD:
                catDetailAdRequestTime = System.currentTimeMillis();
                break;
        }
    }
}
