package cn.lt.android.entity;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 * Created by Administrator on 2016/9/1.
 */
public class APPUpGradeBlackListBean extends BaseBean {
    private boolean is_black_list = false;
    private String message;

    public boolean is_black_list() {
        return is_black_list;
    }

    public void setIs_black_list(boolean is_black_list) {
        this.is_black_list = is_black_list;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
