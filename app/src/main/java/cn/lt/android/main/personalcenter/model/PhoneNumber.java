package cn.lt.android.main.personalcenter.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/4/29.
 */
public class PhoneNumber implements Serializable {
    private String phoneNum="";

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
