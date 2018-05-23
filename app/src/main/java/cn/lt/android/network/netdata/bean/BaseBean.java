package cn.lt.android.network.netdata.bean;

import java.io.Serializable;

/***
 * Created by Administrator on 2015/11/10.
 */
public class BaseBean implements Serializable {
    public String ltType;

    // 视图中的位置标示
    public int p1;
    public int p2;

    public String getLtType() {
        return ltType;
    }

    public void setLtType(String type) {
        this.ltType = type;
    }
}
