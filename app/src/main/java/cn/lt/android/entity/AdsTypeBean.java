package cn.lt.android.entity;

import cn.lt.android.ads.AdMold;
import cn.lt.android.ads.AdsTypeParams;
import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by LinJunSheng on 2017/1/3.
 */

public class AdsTypeBean extends BaseBean {
    private String index = "";
    private String software_index = "";
    private String game_index = "";

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getGame_index() {
        return game_index;
    }

    public void setGame_index(String game_index) {
        this.game_index = game_index;
    }

    public String getSoftware_index() {
        return software_index;
    }

    public void setSoftware_index(String software_index) {
        this.software_index = software_index;
    }

    public void setPageAdType() {
        AdsTypeParams.RecommendAdType = index;
        AdsTypeParams.softwareIndexAdType = software_index;
        AdsTypeParams.GameIndexAdType = game_index.equals(AdMold.WanKa) ? AdMold.NO_AD : game_index;
    }

    @Override
    public String toString() {
        return "AdsTypeBean{" +
                "index='" + index + '\'' +
                ", game_index='" + game_index + '\'' +
                ", software_index='" + software_index + '\'' +
                '}';
    }
}
