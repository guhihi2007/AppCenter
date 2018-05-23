package cn.lt.android.main.entrance;

import cn.lt.android.main.entrance.data.PresentType;

/**
 * Created by LinJunSheng on 2016/7/21.
 */

public class BigPositionGeter {
    private int entryCount, sub_entryCount, carouselCount, pic_topicCount,
            app_topicCount, appCount, hotword_appCount, automatch_titleCount, topic_infoCount, abrahamianCount, appsCount;

    public int getBigPosition(PresentType type) {
        switch (type) {
            case entry:
                return ++entryCount;
            case sub_entry:
                return ++sub_entryCount;
            // 轮播图
            case carousel:
                return ++carouselCount;
            case pic_topic:
                return ++pic_topicCount;
            case app_topic:
                return ++app_topicCount;
            case app:
                return ++appCount;
            case hotword_app:
                return ++hotword_appCount;
            case automatch_title:
                return ++automatch_titleCount;
            case topic_info:
                return ++topic_infoCount;
            case abrahamian:
                return ++abrahamianCount;
            case apps:
                return ++appsCount;


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

        return -1;
    }
}
