package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by LinJunSheng on 2017/1/13.
 */

public class MarketResourceBean extends BaseBean {
    private String market_source;

    public String getMarket_source() {
        return market_source;
    }

    public void setMarket_source(String market_source) {
        this.market_source = market_source;
    }
}
