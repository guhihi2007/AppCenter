package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/3/12.
 */
public class ClickTypeBean extends BaseBean {
    public String click_type;
    public String image;
    private String alias;
    private ClickTypeDataBean data;

    public String getImage() {
        return image;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    @Override
    public String getLtType() {
        return super.getLtType()==null?click_type:super.getLtType();
    }
}
