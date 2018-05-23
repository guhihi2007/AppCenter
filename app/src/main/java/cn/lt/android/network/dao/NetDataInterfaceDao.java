package cn.lt.android.network.dao;


import java.util.List;

import cn.lt.android.entity.AdvertisingConfigBean;
import cn.lt.android.entity.LimitBean;
import cn.lt.android.network.bean.HttpResult;
import cn.lt.android.network.bean.WakeTaskResult;
import cn.lt.android.entity.SilentTask;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2016/2/29.
 */
public interface NetDataInterfaceDao {
    /**
     * 推荐页-分类列表
     */
    @GET("cats")
    Call<String> requestCatsList();

    /**
     * 游戏分类详情
     */
    @GET("cats/{id}/games")
    Call<String> requestGameCatDetail(@Path("id") String id, @Query("page") int page);

    /**
     * 软件详情
     */
    @GET("softwares/{id}")
    Call<String> requestSoftWareDetail(@Path("id") String id);

    /**
     * 搜索匹配
     */
    @GET("automatch")
    Call<String> requestAutoMatch(@Query("q") String q);

    /**
     * 搜索应用
     */
    @GET("api/search")
    Call<String> requestSearch(@Query("q") String q, @Query("page") int page);

    /**
     * 游戏页-分类列表
     */
    @GET("cats/game")
    Call<String> requestGameCatsList();

    /**
     * 软件分类详情
     */
    @GET("cats/{id}/softwares")
    Call<String> requestSoftWareCatDetail(@Path("id") String id, @Query("page") int page);

    /**
     * 软件页-分类列表
     */
    @GET("cats/software")
    Call<String> requestSoftWareCatsList();

    /**
     * 游戏详情
     */
    @GET("api/games/{id}")
    Call<String> requestGameDetail(@Path("id") String id);

    /**
     * 游戏-榜单-单机榜单
     */
    @GET("game-rank-offline")
    Call<String> requestGameOffLineRank(@Query("page") int page);

    /**
     * 游戏-榜单-总榜单
     */
    @GET("game-rank-total")
    Call<String> requestGameTotalRank(@Query("page") int page);

    /**
     * 游戏-榜单-网游榜单
     */
    @GET("game-rank-online")
    Call<String> requestGameRankOnline(@Query("page") int page);

    /**
     * 软件-榜单-总榜单
     */
    @GET("software-rank-total")
    Call<String> requestSoftwareTotalRank(@Query("page") int page);

    /**
     * 软件-榜单-月榜单
     */
    @GET("software-rank-month")
    Call<String> requestSoftwareMonthRank(@Query("page") int page);

    /**
     * 软件-榜单-热门榜单
     */
    @GET("software-rank-hot")
    Call<String> requestSoftwareHotRank(@Query("page") int page);

    /**
     * 启动页
     */
    @GET("search-ads")
    Call<String> requestSearchAds();

    /**
     * 搜索框广告
     */
    @GET("search-hot")
    Call<String> requestHot();

    /***
     * 搜索框广告列表
     */
    @GET("search-ads")
    Call<String> requestSearchAdsList(@Query("title") String title);

    /**
     * 预加载（系统配置+启动页）
     */
    @GET("start")
    Call<String> requestPreloading();

    /**
     * 专题列表
     */
    @GET("topics")
    Call<String> requestSpecialTopicsList(@Query("page") int page, @Query("type") String type);

    /**
     * 专题详情
     */
    @GET("topics/{id}")
    Call<String> requestSpecialTopicsDetail(@Path("id") String id);

    /**
     * 用户登录
     */
    @PUT("user/signin")
    @FormUrlEncoded
    Call<String> requestLogin(@Field("username") String userName, @Field("password") String passWord);

    /**
     * 用户登录by token
     * http://ucenter.dvp.ttigame.com/api/user/info
     */
    @GET("user/info")
    Call<String> requestUserInfoForToken();

    /***
     * 修改昵称/头像 and so on...
     */
    @PUT("user/update")
    @FormUrlEncoded     //get请求不需要加，否则会抛异常。
    Call<String> updateUserInfo(@Field("avatar") String avatar, @Field("nickname") String nickName, @Field("sex") String sex, @Field("birthday") long birthday, @Field("address") String address);

    /***
     * 上传头像
     */
    @Multipart
    @POST("user/avatar")
    Call<String> uploadAvatar(@Part("avatar\"; filename=\"xx.jpg") RequestBody imgs);

    /***
     * 找回密码时检查验证码
     *
     * @param mobile
     * @return
     */
    @GET("sms/check")
    Call<String> requestSmsCheck(@Query("mobile") String mobile, @Query("code") int code);

    /**
     * 发送短信验证（注册时的短信验证和忘记密码时的短信验证）
     *
     * @mobile 手机号
     * @check 若需要检测手机是否被注册时请加此参数=1
     * @exist 若通过手机找回密码需要验证手机号是否注册时请加此参数=1
     */
    @GET("sms/send")
    Call<String> requestSendCode(@Query("mobile") String mobile, @Query("check") int check, @Query("exist") int exist);

    /***
     * 用户注册
     */
    @POST("user/create")
    @FormUrlEncoded
    Call<String> requestRegister(@Field("mobile") String mobile, @Field("password") String passWord, @Field("code") int code);

    /***
     * 修改密码
     */
    @PUT("user/pwd")
    @FormUrlEncoded
    Call<String> requestModifyPwd(@Field("old_password") String oldPassword, @Field("new_password") String newPassword);

    /***
     * 设置新密码
     */
    @PUT("user/pwd")
    @FormUrlEncoded
    Call<String> requestSetNewPwd(@Field("code") String code, @Field("new_password") String newPassword);

    /***
     * 修改手机号码
     */
    @PUT("user/bind")
    @FormUrlEncoded
    Call<String> requestModifyMobile(@Field("code") int code, @Field("mobile") String mobile);

    /**
     * 平台升级检测自动；
     */
    @GET("client/upgrade")
    Call<String> requestPlatformUpdate();

    /**
     * 平台升级检测手动；
     */
    @GET("client/upgrade/manual")
    Call<String> checkPlatformUpdate();

    /**
     * 装机必玩；
     */
    @GET("index-popup")
    Call<String> requestNecessary(@Query("id") int id);

    /**
     * 精选；；
     */
    @GET("index")
    Call<String> requestRecommend(@Query("page") int page, @Query("pagesize") int pagesize);

    /**
     * 游戏精选；；
     */
    @GET("game-index")
    Call<String> requestGameIndex(@Query("page") int page, @Query("pagesize") int pagesize);

    /**
     * 软件精选；；
     */
    @GET("software-index")
    Call<String> requestSoftIndex(@Query("page") int page, @Query("pagesize") int pagesize);

    /**
     * 弹框推广图；；
     */
    @GET("tab-topics/{id}")
    Call<String> requestTabTopics(@Path("id") String id);

    /**
     * 普通列表
     */
    @GET("applist/{id}")
    Call<String> requestNormalList(@Path("id") String id, @Query("page") int page);

    /**
     * 推送
     */
    @GET("push/{id}")
    Call<String> requestPush(@Path("id") String id);

    /**
     * 应用更新检测
     */
    @POST("apps/upgrade")
    @FormUrlEncoded
    Call<String> requestUpgrade(@Field("packages") String packages);

    /**
     * 根据包名搜索应用
     */
    @GET("search-package-name")
    Call<String> requestAppByPackageName(@Query("package_name") String package_name);

    /***
     * 数据统计
     */
    @POST(" ")
    @FormUrlEncoded
    Call<String> requestDataCenter(@Field("data") String data, @Field("source") String source);

    /**
     * 获取反馈列表
     */
    @GET("feedbacks")
    Call<String> requestFeedBacks(@Query("page") int page);

    /**
     * 发送文本反馈消息
     */
    @POST("feedbacks")
    @FormUrlEncoded
    Call<String> requestFeedBacks(@Field("content") String content);

    /**
     * 发送图片反馈消息
     */
    @Multipart
    @POST("feedbacks")
    Call<String> requestFeedBacks(@Part("image\"; filename=\"xx.jpg") RequestBody imgs);

    /***
     * 根据广告类型和包名获取推荐应用列表
     *
     * @param type
     * @param package_name
     * @return
     */
    @GET("apps/recommend")
    Call<String> requestRecommendApp(@Query("cat") String type, @Query("package_name") String package_name);

    /***
     * 根据ID获取应用ＭＤ５和下载地址
     *
     * @param type
     * @param id
     * @return
     */
    @GET("packages/{type}/{id}")
    Call<String> requestPkgInfo(@Path("type") String type, @Path("id") String id);

    /***
     * 根据智能列表数据
     *
     * @param id 只能列表id
     * @return
     */
    @GET("intelligentlist/{id}")
    Call<String> requestSmartList(@Path("id") String id, @Query("page") int page);

    /***
     * 4.2版本搜索推荐页
     *
     * @return
     */
    @GET("mutil-search")
    Call<String> requestMutilSearch();

    /***
     * 榜单标签
     * 榜单类型:game|software|summary
     */
    @GET("rank-name")
    Call<String> requestRankName(@Query("rank_type") String type);

    /**
     * 应用升级黑名单
     *
     * @return
     */
    @GET("apps/isblacklist")
    Call<String> requestBlacklist();

    /**
     * 上传页面已展示的广告包名集合
     */
    @POST("clients/wdjPackages")
    @FormUrlEncoded
    Call<String> posEexistAdList(@Field("packages") String packages, @Field("pageName") String pageName);


    /**
     * 回传CID 、推送类型和本地应用列表
     */
    @POST("client/report/info")
    @FormUrlEncoded
    Call<String> postLocalData(@Field("cid") String cid, @Field("type") String type, @Field("already_install_apps") String already_install_apps, @Field("network_type") String net_type, @Field("network_operator") String operator, @Field("last_update_time") long last_update_time, @Field("first_install_time") long first_install_time);


    @GET("lua/ticket_appcenter/get")
    Call<HttpResult<LimitBean>> getTicket();

    @GET("lua/ticket_appcenter/check")
    Call<HttpResult<LimitBean>> checkTicket();

    @GET("lua/ticket_appcenter/drop")
    Call<HttpResult<Object>> dropTicket();

    /**
     * 飞扬配置信息
     */
    @GET("popup")
    Call<String> requestPopup();

    /**
     * 浮层广告
     */
    @GET("floating/layer/adverting")
    Call<String> requestFloatAds();

    /**
     * 拉取拉活任务
     */
    @GET("active/task")
    Call<WakeTaskResult<List<SilentTask>>> requestWakeTaskList();

    /**
     * check任务是否有效
     */
    @GET("active/validate/{task_id}")
    Call<WakeTaskResult> checkTaskIsAvailable(@Path("task_id") String task_id);

    /**
     *开屏广告配置
     * @return
     */
    @GET("advertisingConfig")
    Call<String> advertisingConfig();

}
