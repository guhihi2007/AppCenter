package cn.lt.android.network.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/3/12.
 */
public class HostConfigBean {
    private String rank_style;
    private List<String> popup_priority;
    private String convert_to_jpg;

    public String getRank_style() {
        return rank_style;
    }

    public void setRank_style(String rank_style) {
        this.rank_style = rank_style;
    }

    public List<String> getPopup_priority() {
        return popup_priority;
    }

    public void setPopup_priority(List<String> popup_priority) {
        this.popup_priority = popup_priority;
    }

    public String getConvert_to_jpg() {
        return convert_to_jpg;
    }

    public void setConvert_to_jpg(String convert_to_jpg) {
        this.convert_to_jpg = convert_to_jpg;
    }
}
