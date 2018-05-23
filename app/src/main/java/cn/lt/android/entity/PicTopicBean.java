package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/3/12.
 */
public class PicTopicBean extends BaseBean {
    public String image;
    public String topic_name;
    public String click_type;
    public String topic_title;
    public String title_color;
    public ClickTypeDataBean data;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public String getClick_type() {
        return click_type;
    }

    public void setClick_type(String click_type) {
        this.click_type = click_type;
    }

    public String getTopic_title() {
        return topic_title;
    }

    public void setTopic_title(String topic_title) {
        this.topic_title = topic_title;
    }

    public String getTitle_color() {
        return title_color;
    }

    public void setTitle_color(String title_color) {
        this.title_color = title_color;
    }

    public ClickTypeDataBean getData() {
        return data;
    }

    public void setData(ClickTypeDataBean data) {
        this.data = data;
    }

    @Override
    public String getLtType() {
        return super.getLtType()==null?click_type:super.getLtType();
    }
}
