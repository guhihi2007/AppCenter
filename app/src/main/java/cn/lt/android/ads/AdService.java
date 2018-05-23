package cn.lt.android.ads;

import android.content.Context;

import com.yolanda.nohttp.rest.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lt.android.LogTAG;
import cn.lt.android.ads.listener.refresh.AdRefreshListener;
import cn.lt.android.ads.request.ADResponseListener;
import cn.lt.android.ads.request.AbstractRequester;
import cn.lt.android.ads.request.AdAppDetailResponseListener;
import cn.lt.android.ads.request.AdRequestBean;
import cn.lt.android.ads.request.AdRequesterFactory;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.util.LogUtils;

/**
 * Created by ltbl on 2016/6/25.
 *
 * @des 用于对豌豆荚等广告过滤加工处理变为自己的数据结果
 */
public class AdService {

    public static class AdServiceHolder {
        private static AdService instance = new AdService();
    }

    public static AdService getInstance() {
        return AdServiceHolder.instance;
    }

    /**
     * 推荐页广告类型
     */
    public static final int RECOMMEND_AD = 1;

    /**
     * 软件精选页广告类型
     */
    public static final int SOFTWARE_ESSENCE_AD = 2;

    /**
     * 游戏精选页广告类型
     */
    public static final int GAME_ESSENCE_AD = 3;

    /**
     * 榜单广告类型
     */
    public static final int RANK_AD = 4;

    /**
     * 分类详情页广告类型
     */
    public static final int ONE_OFF_AD = 5;

    /**
     * 智能列表广告类型
     */
    public static final int SMART_LIST_AD = 6;

    // 刷新的回调
    private AdRefreshListener recommendAdRefresh;
    private AdRefreshListener softwareEssenceAdRefresh;
    private AdRefreshListener gameEssenceAdRefresh;
    private AdRefreshListener rankAdRefresh;
    private AdRefreshListener oneOffAdRefresh;


    private AdFilter adFilter;
    private AdFillMachine adFillMachine;
    private DownloadAdAppReplacer downloadAdAppReplacer;
    private AdTimeRecorder adTimeRecorder;

    /**
     * 推荐广告应用列表
     */
    private List<AppBriefBean> recommendAdAppList = new ArrayList<>();

    /**
     * 软件精选广告应用列表
     */
    private List<AppBriefBean> softwareEssenceAdAppList = new ArrayList<>();

    /**
     * 游戏精选广告应用列表
     */
    private List<AppBriefBean> gameEssenceAdAppList = new ArrayList<>();

    /**
     * 榜单广告应用列表
     */
    private List<AppBriefBean> rankAdAppList = new ArrayList<>();

    /**
     * 一次性应用列表(分类详情、智能列表)
     */
    private List<AppBriefBean> oneOffAdAppList = new ArrayList<>();


    /**
     * （推荐页）已展示过的广告名单列表
     */
    private Map<String, AppBriefBean> existRecommendAdList = new HashMap<>();

    /**
     * （软件精选页）已展示过的广告名单列表
     */
    private Map<String, AppBriefBean> existSoftwareEssenceAdList = new HashMap<>();

    /**
     * （游戏精选页）已展示过的广告名单列表
     */
    private Map<String, AppBriefBean> existGameEssenceAdList = new HashMap<>();

    /**
     * （推荐页）被删除掉的广告名单列表（与自有资源重复）
     */
    private Map<String, AppBriefBean> removeRecommendAdList = new HashMap<>();

    /**
     * （软件精选页）被删除掉的广告名单列表（与自有资源重复）
     */
    private Map<String, AppBriefBean> removeSoftwareEssenceAdList = new HashMap<>();

    /**
     * （游戏精选页）被删除掉的广告名单列表（与自有资源重复）
     */
    private Map<String, AppBriefBean> removeGameEssenceAdList = new HashMap<>();

    /**
     * （共用页）已展示过的广告名单列表
     */
    private Map<String, AppBriefBean> existOneOffAdList = new HashMap<>();

    /**
     * （推荐页）备用资源列表
     */
    private Map<String, AppBriefBean> backupRecommendList = new HashMap<>();

    /**
     * （软件精选页）备用资源列表
     */
    private Map<String, AppBriefBean> backupSoftwareEssenceList = new HashMap<>();

    /**
     * （游戏精选页）备用资源列表
     */
    private Map<String, AppBriefBean> backupGameEssenceList = new HashMap<>();

    /**
     * （共用页）备用资源列表
     */
    private Map<String, AppBriefBean> backupOneOffList = new HashMap<>();

    private Context mContext;

    private boolean recommendAdisReady = false;
    private boolean softwareEssenceAdisReady = false;
    private boolean gameEssenceAdisReady = false;
    private boolean rankAdisReady = false;
    private boolean oneOffAdisReady = false;


    public void init(Context context) {
        this.mContext = context;

        adFilter = new AdFilter();
        adFillMachine = new AdFillMachine();
        downloadAdAppReplacer = new DownloadAdAppReplacer();
        adTimeRecorder = new AdTimeRecorder();

    }

    /***
     * 获取广告详情页数据
     */
    public void fetchAdDetail(String adMold, String pacagekName, AdAppDetailResponseListener responseListener) {
        AbstractRequester requester = AdRequesterFactory.produceAdRequester(mContext, adMold);

        if(requester != null) {
            requester.requestAdDetailData(pacagekName, responseListener);
        } else {
            if(null != responseListener) {
                responseListener.onFailed(null);
            }
        }

    }

    /***
     * 获取广告数据
     */
    public void fetchAd(String adMold, int pageType, AdRequestBean adRequestBean) {
        LogUtils.i(LogTAG.AdTAG, "fetchAd_adMold = " + adMold);
        AbstractRequester requester = AdRequesterFactory.produceAdRequester(mContext, adMold);

        setReady(false, pageType);

        if(requester != null) {
            LogUtils.i(LogTAG.AdTAG, "开始请求广告数据列表");
            requester.requestAdData(pageType, adRequestBean, adResponseListener);
        } else {
            LogUtils.i(LogTAG.AdTAG, pageType + " --> requester获取失败");
        }
    }

    private ADResponseListener adResponseListener = new ADResponseListener() {

        @Override
        public void onStart(int what) {
        }

        @Override
        public void onSucceed(int pageType, Response response, List<AppBriefBean> adList) {

            adTimeRecorder.setRequestTime(pageType);
            setReady(true, pageType);

            List<AppBriefBean> adAppList = getAdAppList(pageType);
            AdRefreshListener refreshListener = getAdRefresh(pageType);

            adAppList.clear();
            adAppList.addAll(adList);

            if(refreshListener != null) {
                LogUtils.i(LogTAG.AdTAG, "广告请求成功，刷新页面数据");
                refreshListener.refresh();
                setRefreshCallToNull(pageType);
            }

        }

        @Override
        public void onFailed(int what, Response response) {
            setReady(false, what);
            setRefreshCallToNull(what);
            LogUtils.i("zzz", "豌豆荚接口请求或解析json数据失败");
        }

        @Override
        public void onFinish(int what) {

        }

        @Override
        public void setReady(boolean ready, int pageType) {
            AdService.this.setReady(ready, pageType);
        }

        @Override
        public void setRefreshCallToNull(int pageType) {
            switch (pageType) {
                case RECOMMEND_AD:
                    recommendAdRefresh = null;
                case SOFTWARE_ESSENCE_AD:
                    softwareEssenceAdRefresh = null;
                case GAME_ESSENCE_AD:
                    gameEssenceAdRefresh = null;
                case ONE_OFF_AD:
                    oneOffAdRefresh = null;
            }
        }

    };

    private void setReady(boolean ready, int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                recommendAdisReady = ready;
                break;
            case SOFTWARE_ESSENCE_AD:
                softwareEssenceAdisReady = ready;
                break;
            case GAME_ESSENCE_AD:
                gameEssenceAdisReady = ready;
                break;
            case RANK_AD:
                rankAdisReady = ready;
                break;
            case ONE_OFF_AD:
                oneOffAdisReady = ready;
                break;
        }
    }

    /**
     * 处理广告
     */
    public synchronized void handleAd(int pageType, List<BaseBean> requestDatas, List<BaseBean> allDatas) {

        if (requestDatas == null || requestDatas.size() == 0) {
            LogUtils.i(LogTAG.AdTAG, "requestDatas是0，不需要处理广告了");
            return;
        }

        List<AppBriefBean> adList = null;

        switch (pageType) {
            case RECOMMEND_AD:
                if (!recommendAdisReady) {
                    OriginalAppChecker.checkByBaseBeanList(requestDatas, pageType);
                    LogUtils.i(LogTAG.AdTAG, "recommendAdisReady == false");
                    return;
                }
                adList = recommendAdAppList;
                break;
            case SOFTWARE_ESSENCE_AD:
                if (!softwareEssenceAdisReady) {
                    OriginalAppChecker.checkByBaseBeanList(requestDatas, pageType);
                    LogUtils.i(LogTAG.AdTAG, "softwareEssenceAdisReady == false");
                    return;
                }
                adList = softwareEssenceAdAppList;
                break;
            case GAME_ESSENCE_AD:
                if (!gameEssenceAdisReady) {
                    OriginalAppChecker.checkByBaseBeanList(requestDatas, pageType);
                    LogUtils.i(LogTAG.AdTAG, "gameEssenceAdisReady == false");
                    return;
                }
                adList = gameEssenceAdAppList;
                break;
        }

        // 过滤广告
        filter(adList, pageType);

        // 去除跟已展示过的自有资源相同的广告
        if (allDatas != null && allDatas.size() > 0) {
            adFillMachine.removeAdApp(allDatas, adList, pageType);
        }

        // 填充广告
        adFillMachine.fillAdAppByBaseBeanList(requestDatas, adList, pageType);

    }

    /***
     * 过滤广告列表
     *
     */
    private void filter(List<AppBriefBean> adList, int pageType) {
        LogUtils.i(LogTAG.AdTAG , "广告过滤前 总数是 = " + adList.size());
//        appFilter.removeWhitelist(LTApplication.appWhiteList, adList);
        adFilter.removeInstalled(adList);
        adFilter.removeDownloadTask(adList);
        adFilter.removeExistAd(adList, pageType);
        adFilter.removeRemovedAd(adList, pageType);
        LogUtils.i(LogTAG.AdTAG , "广告过滤后 总数是 = " + adList.size());
    }

    /***
     * 是否需要刷新数据
     *
     * @return
     */
    public boolean isAdDirty(int adType) {
        return adTimeRecorder.isDirty(adType);
    }

    public List<AppBriefBean> getAdAppList(int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                return this.recommendAdAppList;
            case SOFTWARE_ESSENCE_AD:
                return this.softwareEssenceAdAppList;
            case GAME_ESSENCE_AD:
                return this.gameEssenceAdAppList;
            case ONE_OFF_AD:
                return this.oneOffAdAppList;
            default:
                return new ArrayList<>();
        }
    }

    public AdRefreshListener getAdRefresh(int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                return this.recommendAdRefresh;
            case SOFTWARE_ESSENCE_AD:
                return this.softwareEssenceAdRefresh;
            case GAME_ESSENCE_AD:
                return this.gameEssenceAdRefresh;
            case ONE_OFF_AD:
                return this.oneOffAdRefresh;
            default:
                return null;
        }
    }

    public Map<String, AppBriefBean> getExistAdList(int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                return this.existRecommendAdList;
            case SOFTWARE_ESSENCE_AD:
                return this.existSoftwareEssenceAdList;
            case GAME_ESSENCE_AD:
                return this.existGameEssenceAdList;
            case ONE_OFF_AD:
                return this.existOneOffAdList;
            default:
                return new HashMap<>();
        }
    }


    public Map<String, AppBriefBean> getRemoveAdList(int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                return this.removeRecommendAdList;
            case SOFTWARE_ESSENCE_AD:
                return this.removeSoftwareEssenceAdList;
            case GAME_ESSENCE_AD:
                return this.removeGameEssenceAdList;
            default:
                return new HashMap<>();
        }
    }

    public Map<String, AppBriefBean> getBackupList(int pageType) {
        switch (pageType) {
            case RECOMMEND_AD:
                return this.backupRecommendList;
            case SOFTWARE_ESSENCE_AD:
                return this.backupSoftwareEssenceList;
            case GAME_ESSENCE_AD:
                return this.backupGameEssenceList;
            default:
                return new HashMap<>();
        }
    }

    public void setAdRefreshListener(int pageType, AdRefreshListener adRefreshListener) {
        switch (pageType) {
            case RECOMMEND_AD:
                this.recommendAdRefresh = adRefreshListener;
                break;
            case SOFTWARE_ESSENCE_AD:
                this.softwareEssenceAdRefresh = adRefreshListener;
                break;
            case GAME_ESSENCE_AD:
                this.gameEssenceAdRefresh = adRefreshListener;
                break;
            case RANK_AD:
                this.rankAdRefresh = adRefreshListener;
                break;
            case ONE_OFF_AD:
                this.oneOffAdRefresh = adRefreshListener;
                break;
        }
    }
}
