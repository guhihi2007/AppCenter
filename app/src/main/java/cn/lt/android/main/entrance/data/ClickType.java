package cn.lt.android.main.entrance.data;

import java.io.Serializable;

/***
 * Created by dxx on 2016/3/11.
 */
public enum ClickType implements Serializable{
    /** h5 url*/
    h5,

    /** 应用详情*/
    app_info,

    /** 专题详情*/
    topic_info,

    /** tab专题详情*/
    tab_topic_info,

    /** 客户端页面*/
    page,

    /** 普通列表*/
    list,

    /** 智能列表*/
    intellective_ads_lists,

    /** 专题列表*/
    topic_list


}
