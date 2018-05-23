package cn.lt.android.ads.request;

/**
 * Created by LinJunSheng on 2016/12/27.
 */

public class AdRequestBean {

    public final WanDouJia wanDouJia;
    public final WanKa wanKa;

    public AdRequestBean() {
        this.wanDouJia = new WanDouJia();
        this.wanKa = new WanKa();
    }

    /** 豌豆荚广告用*/
    public static class WanDouJia{
        /** 每次请求广告数目的开始位置*/
        public int startNum;

        /** 每次请求广告的个数*/
        public int adCount;
    }

    /** 玩咖广告用*/
    public static class WanKa{
        /** 每次请求广告数目的开始位置*/
        public int curPage;

        /** 每次请求广告的个数*/
        public int adCount;
    }
}
