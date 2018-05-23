package cn.lt.android.network.netdata.analyze;


import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import cn.lt.android.ads.bean.WhiteListBean;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.entity.APPUpGradeBlackListBean;
import cn.lt.android.entity.AdsTypeBean;
import cn.lt.android.entity.AdvertisingConfigBean;
import cn.lt.android.entity.AppCatBean;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.entity.CategoryNameBean;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.entity.ConfigureBean;
import cn.lt.android.entity.FloatAdBean;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.entity.MarketResourceBean;
import cn.lt.android.entity.NecessaryBean;
import cn.lt.android.entity.PicTopicBean;
import cn.lt.android.entity.PkgInfoBean;
import cn.lt.android.entity.PlatVersionBean;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.entity.SpecialTopicBean;
import cn.lt.android.entity.SpecialTopicDetailBean;
import cn.lt.android.entity.TabTopicBean;
import cn.lt.android.main.personalcenter.model.FeedBackBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.notification.bean.PushAwakeBean;
import cn.lt.android.notification.bean.PushGameBean;
import cn.lt.android.notification.bean.PushH5Bean;
import cn.lt.android.notification.bean.PushPlatUpgradeBean;
import cn.lt.android.notification.bean.PushSoftwareBean;
import cn.lt.android.notification.bean.PushTopicBean;
import cn.lt.android.util.LogUtils;

/**
 * Created by Administrator on 2015/11/14.
 */
public class AnalyzeConfig {
    public static Map<String, AnalyzeJsonBean> typeMap;

    public static Map<String, AnalyzeJsonBean> getMap() {
        if (typeMap == null) {
            typeMap = new HashMap<>();
            typeMap.put("category", new AnalyzeJsonBean(true, new TypeToken<AppCatBean>() {
            }));
            typeMap.put("app", new AnalyzeJsonBean(true, new TypeToken<AppDetailBean>() {
            }));
            typeMap.put("software_info", new AnalyzeJsonBean(true, new TypeToken<AppDetailBean>() {
            }));
            typeMap.put("game_info", new AnalyzeJsonBean(true, new TypeToken<AppDetailBean>() {
            }));
            typeMap.put("search_ads", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<HotSearchBean>>() {
            }));
            typeMap.put("category_group_name", new AnalyzeJsonBean(true, new TypeToken<CategoryNameBean>() {
            }));
            typeMap.put("topic", new AnalyzeJsonBean(true, new TypeToken<SpecialTopicBean>() {
            }));
            typeMap.put("client_upgrade", new AnalyzeJsonBean(true, new TypeToken<PlatVersionBean>() {
            }));
            typeMap.put("topic_info", new AnalyzeJsonBean(true, new TypeToken<SpecialTopicDetailBean>() {
            }));
            typeMap.put("necessary_apps", new AnalyzeJsonBean(true, new TypeToken<NecessaryBean>() {
            }));
            typeMap.put("ads_image_popup", new AnalyzeJsonBean(true, new TypeToken<AdsImageBean>() {
            }));
            typeMap.put("start_image", new AnalyzeJsonBean(true, new TypeToken<AdsImageBean>() {
            }));
            typeMap.put("white_list", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<WhiteListBean>>() {
            }));
            typeMap.put("popup", new AnalyzeJsonBean(true, new TypeToken<ConfigureBean>() {
            }));
            typeMap.put("hot_search", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<HotSearchBean>>() {
            }));
            typeMap.put("carousel", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<ClickTypeBean>>() {
            }));
            typeMap.put("app_topic", new AnalyzeJsonBean(true, new TypeToken<AppTopicBean>() {
            }));
            typeMap.put("sub_entry", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<ClickTypeBean>>() {
            }));
            typeMap.put("pic_topic", new AnalyzeJsonBean(true, new TypeToken<PicTopicBean>() {
            }));
            typeMap.put("tab_topic_info", new AnalyzeJsonBean(true, new TypeToken<TabTopicBean>() {
            }));
            typeMap.put("entry", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<ClickTypeBean>>() {
            }));
            typeMap.put("app_upgrade", new AnalyzeJsonBean(true, new TypeToken<AppDetailBean>() {
            }));
            typeMap.put("apps", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<AppDetailBean>>() {
            }));
            typeMap.put("push_game", new AnalyzeJsonBean(true, new TypeToken<PushGameBean>() {
            }));
            typeMap.put("push_software", new AnalyzeJsonBean(true, new TypeToken<PushSoftwareBean>() {
            }));
            typeMap.put("push_topic", new AnalyzeJsonBean(true, new TypeToken<PushTopicBean>() {
            }));
            typeMap.put("push_client_upgrade", new AnalyzeJsonBean(true, new TypeToken<PushPlatUpgradeBean>() {
            }));
            typeMap.put("text_feedback", new AnalyzeJsonBean(true, new TypeToken<FeedBackBean>() {
            }));
            typeMap.put("image_feedback", new AnalyzeJsonBean(true, new TypeToken<FeedBackBean>() {
            }));
            typeMap.put("hotword_app", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<AppDetailBean>>() {
            }));
            typeMap.put("automatch_title", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<HotSearchBean>>() {
            }));
            typeMap.put("push_h5", new AnalyzeJsonBean(true, new TypeToken<PushH5Bean>() {
            }));
            typeMap.put("recommend_apps", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<RecommendBean>>() {
            }));
            typeMap.put("s_hotword", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<HotSearchBean>>() {
            }));
            typeMap.put("s_game_recommend", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<HotSearchBean>>() {
            }));
            typeMap.put("s_software_recommend", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<HotSearchBean>>() {
            }));
            typeMap.put("rank_name", new AnalyzeJsonBean(true, new TypeToken<BaseBeanList<TabTopicBean>>() {
            }));
            typeMap.put("packages", new AnalyzeJsonBean(true, new TypeToken<PkgInfoBean>() {
            }));
            typeMap.put("app_auto_upgrade_black_list", new AnalyzeJsonBean(true, new TypeToken<APPUpGradeBlackListBean>() {
            }));
            typeMap.put("ads_type", new AnalyzeJsonBean(true, new TypeToken<AdsTypeBean>() {
            }));
            typeMap.put("app_market_source", new AnalyzeJsonBean(true, new TypeToken<MarketResourceBean>() {
            }));
            typeMap.put("all_popup", new AnalyzeJsonBean(true, new TypeToken<ConfigureBean>() {
            }));
            typeMap.put("push_pullapp", new AnalyzeJsonBean(true, new TypeToken<PushAwakeBean>() {
            }));
            typeMap.put("floating_ads", new AnalyzeJsonBean(true, new TypeToken<FloatAdBean>() {
            }));
            typeMap.put("advertising_config", new AnalyzeJsonBean(true, new TypeToken<AdvertisingConfigBean>() {
            }));
            LogUtils.e("AnalyzeConfig", "typeMap构建完成");
        }
        return typeMap;
    }
}
