package cn.lt.android.main.personalcenter;


import cn.lt.android.main.personalcenter.model.UserBaseInfo;

public interface UserInfoUpdateListening {
    /**
     * 登陆成功返回
     */
    void userLogin(UserBaseInfo userBaseInfo);

    void updateUserInfo(UserBaseInfo userBaseInfo);

    /**
     * 注销
     */
    void userLogout();
}
