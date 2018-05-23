package cn.lt.android.notification.bean;

/**
 * 拉活
 * Created by ltbl on 2017/8/9.
 */

public class PushAwakeBean extends PushBaseBean {
    private String pulladdress;

    public String getPulladdress() {
        return pulladdress;
    }

    public void setPulladdress(String pulladdress) {
        this.pulladdress = pulladdress;
    }
}
