package cn.lt.android.ads.bean.wdj;

import cn.lt.android.entity.ClickTypeDataBean;
import cn.lt.android.network.netdata.bean.BaseBean;

/***
 * Created by dxx on 2016/3/10.
 */
public class AdsImageBean extends BaseBean {
    /**
     * 模板id;
     */
    private int id;
    /**
     * 展示图片；
     */
    private String image;
    /**
     * 点击图片的跳转类型；
     * 'app_info'=>'专题详情','topic_info'=>'专题详情','tab_topic_info'＝>'tab专题详情','page'=>'客户端页面','list'=>'普通列表']
     */
    private String click_type;
    private ClickTypeDataBean data;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getClick_type() {
        return click_type;
    }

    public void setClick_type(String click_type) {
        this.click_type = click_type;
    }

    public ClickTypeDataBean getData() {
        return data;
    }

    public void setData(ClickTypeDataBean data) {
        this.data = data;
    }

}
