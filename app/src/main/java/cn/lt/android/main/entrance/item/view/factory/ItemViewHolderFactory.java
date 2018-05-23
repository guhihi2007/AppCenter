package cn.lt.android.main.entrance.item.view.factory;

import android.content.Context;

import cn.lt.android.main.EntranceAdapter;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.main.entrance.item.view.ItemAbrahamianView;
import cn.lt.android.main.entrance.item.view.ItemAppVerticalRootView;
import cn.lt.android.main.entrance.item.view.ItemAutoSearchView;
import cn.lt.android.main.entrance.item.view.ItemBannerView;
import cn.lt.android.main.entrance.item.view.ItemGarbView;
import cn.lt.android.main.entrance.item.view.ItemRecommendEntryView;
import cn.lt.android.main.entrance.item.view.ItemSearchAdvView;
import cn.lt.android.main.entrance.item.view.ItemSearchHistoryView;
import cn.lt.android.main.entrance.item.view.ItemSingleAppView;
import cn.lt.android.main.entrance.item.view.ItemSubEntryGridView;
import cn.lt.android.main.entrance.item.view.ItemViewNull;
import cn.lt.android.main.entrance.item.view.carousel.BannerView;
import cn.lt.android.main.specialtopic.view.SpecialTopicDetailInfoView;


/***
 * Created by dxx on 2016/03/08.
 * 这是创建viewholder的工厂，
 */
public class ItemViewHolderFactory {

    public static EntranceAdapter.ViewHolder produceItemViewHolder(PresentType presentType, Context context, String pageName, String id) {
        EntranceAdapter.ViewHolder holder = null;
        switch (presentType) {

            case entry:
                holder = new EntranceAdapter.ViewHolder(new ItemRecommendEntryView(context, pageName));
                break;
            case sub_entry:
                holder = new EntranceAdapter.ViewHolder(new ItemSubEntryGridView(context, pageName));
                break;
            // 轮播图
            case carousel:
                holder = new EntranceAdapter.ViewHolder(new BannerView(context, BannerView.DEFAULT_HEIGHT, false, pageName));
                break;
            case pic_topic:
                holder = new EntranceAdapter.ViewHolder(new ItemBannerView(context, pageName));
                break;
            case app_topic:
                holder = new EntranceAdapter.ViewHolder(new ItemGarbView(context, pageName));
                break;

            case app:
            case hotword_app:
                holder = new EntranceAdapter.ViewHolder(new ItemSingleAppView(context, pageName, id));
                break;
            case automatch_title:
                holder = new EntranceAdapter.ViewHolder(new ItemAutoSearchView(context, pageName));
                break;
            case topic_info:
                holder = new EntranceAdapter.ViewHolder(new SpecialTopicDetailInfoView(context, pageName));
                break;
            case abrahamian:
                holder = new EntranceAdapter.ViewHolder(new ItemAbrahamianView(context, pageName, id));
                break;
            case apps:
                holder = new EntranceAdapter.ViewHolder(new ItemAppVerticalRootView(context, pageName, id));
                break;
            case history_app:
                holder = new EntranceAdapter.ViewHolder(new ItemSearchHistoryView(context, pageName, id));
                break;
            case s_hotword:
                holder = new EntranceAdapter.ViewHolder(new ItemSearchAdvView(context, pageName, "HOT"));
                break;
            case s_software_recommend:
                holder = new EntranceAdapter.ViewHolder(new ItemSearchAdvView(context, pageName, "软件"));
                break;
            case s_game_recommend:
                holder = new EntranceAdapter.ViewHolder(new ItemSearchAdvView(context, pageName, "游戏"));
                break;
            case game_info:
                break;
            case software_info:
                break;
            case app_upgrade:
                break;
            case client_upgrade:
                break;
            case category_group_name:
                break;
            case category:
                break;
            case text_feedback:
                break;
            case image_feedback:
                break;
            case tab_topic_info:
                break;
            case page:
                break;
            case push_app:
                break;
            case push_topic:
                break;
            case push_client_upgrade:
                break;
            case search_ads:
                break;
            case necessary_apps:
                break;
            case start_image:
                break;
            case hot_search:
                break;
            case ads_image_popup:
                break;
            case topic:
                break;
        }
        if (holder == null) {
            return new EntranceAdapter.ViewHolder(new ItemViewNull(context, pageName));
        }
        return holder;
    }
}
