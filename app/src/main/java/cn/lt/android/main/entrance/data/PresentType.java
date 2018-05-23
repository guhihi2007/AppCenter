package cn.lt.android.main.entrance.data;

import java.util.concurrent.atomic.AtomicInteger;


/***
 * Created by dxx on 2016/03/08.
 */
public enum PresentType {


    /**
     * 首页入口（大长方图）
     */
    entry(),
    /**
     * 二级入口（圆形）
     */
    sub_entry(),

    /**
     * 轮播图；
     */
    carousel(),

    /**
     * 图片专题
     */
    pic_topic(),
    /**
     * 应用专题（三个一排）
     */
    app_topic(),
    /**
     * 单应用
     */
    app(),
    /**
     * 游戏详情
     */
    game_info(),
    /**
     * 软件详情
     */
    software_info(),
    /**
     * 单应用更新（与单应用有点像）
     */
    app_upgrade(),
    /**
     * 客户端更新
     */
    client_upgrade(),
    /**
     * 应用分组名称
     */
    category_group_name(),
    /**
     * 单分类标签
     */
    category(),
    /**
     * 文本反馈
     */
    text_feedback(),
    /**
     * 图片反馈
     */
    image_feedback(),
    /**
     * tab专题详情
     */
    tab_topic_info(),
    /**
     * tab专题自定义页面
     */
    page(),
    /**
     * 应用类型推送
     */
    push_app(),
    /**
     * 专题类型推送
     */
    push_topic(),
    /**
     * 客户端升级推送
     */
    push_client_upgrade(),
    /**
     * 搜索框广告（轮播）
     */
    search_ads(),
    /**
     * 装机必备
     */
    necessary_apps(),
    /**
     * 启动图
     */
    start_image(),
    /***
     * 白名单
     */
    white_list(),
    /***
     * 配置信息
     */
    popup(),
    /**
     * 大家都在搜
     */
    hot_search(),
    /**
     * 弹窗推广图
     */
    ads_image_popup(),
    /**
     * 单专题
     */
    topic(),
    /**
     * 专题详情
     */
    topic_info(),
    /***
     * 搜索自动匹配
     */
    auto_search(),
    /**
     * 排行前三组合方式；
     */
    abrahamian(),
    /**
     * 精选页app集合；
     */
    apps(),
    /***
     * 模糊搜索结果
     */
    automatch_title(),
    /***
     * 精确搜索匹配结果
     */
    hotword_app(),

    /***
     * 搜索历史记录
     */
    history_app(),

    /***
     * 搜索推荐页热词
     */
    s_hotword(),

    /***
     * 搜索 推荐页游戏推荐
     */
    s_game_recommend(),
    /***
     * 搜索推荐页软件推荐
     */
    s_software_recommend(),

    /***
     * 榜单名称
     */
    rank_name(),

    /**
     * 页面对应的广告所属类型
     */
    ads_type(),

    /**
     * 安装时传入的来源包名
     */
    app_market_source();

    public int viewType;
    public String presentType;

    private PresentType() {
        this.presentType = this.toString();
        this.viewType = Atomicln.ai.incrementAndGet() - 1;
    }

    static class Atomicln {
        public static AtomicInteger ai = new AtomicInteger();
    }

}
