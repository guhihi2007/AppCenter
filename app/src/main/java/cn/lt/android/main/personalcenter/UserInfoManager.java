package cn.lt.android.main.personalcenter;

import android.text.TextUtils;

import java.util.List;

import cn.lt.android.GlobalParams;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.db.UserEntity;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.util.ToastUtils;
import de.greenrobot.event.EventBus;

/***
 * 用户账户行为相关操作
 */
public class UserInfoManager {

    private volatile static UserInfoManager instance;

    public static UserInfoManager instance() {
        if (instance == null) {
            synchronized (UserInfoManager.class) {
                if (instance == null) {
                    instance = new UserInfoManager();
                }
            }
        }
        return instance;
    }

    /**
     * 把用户信息存在本地
     *
     * @param userBaseInfo
     * @param isFromToken
     */
    public void savaUserInfo(UserBaseInfo userBaseInfo, boolean isFromToken) {
        try {
            GlobalParams.getUserDao().deleteAll();
            GlobalParams.getUserDao().insert(new UserEntity(null,userBaseInfo.getToken(),userBaseInfo.getMobile(),userBaseInfo.getId()+""
            ,userBaseInfo.getAvatar(),userBaseInfo.getEmail(),userBaseInfo.getNickname(),
                    userBaseInfo.getSex(),userBaseInfo.getBirthday()+"",userBaseInfo.getAddress(),userBaseInfo.getUserName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(!isFromToken) {
//            NotifyUserInfoToGameCenterMgr.insertIntoGameCenter(LTApplication.instance, userBaseInfo);
//        }
    }
    public void updateUserNickName(UserBaseInfo userBaseInfo){
//        SharePreferenceUtil.put(SharePreferencesKey.NICKNAME, userBaseInfo.getNickname());
        try {
            List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
            if(list!=null && list.size()==1){
                UserEntity userEntity = list.get(0);
                if(userBaseInfo!=null){
                    userEntity.setNickname(userBaseInfo.getNickname());
                    GlobalParams.getUserDao().update(userEntity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveUserMobile(String mobile){
//        SharePreferenceUtil.put(SharePreferencesKey.USERMOBILE, mobile);
        List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
        if(list!=null && list.size()==1){
            UserEntity userEntity = list.get(0);
            if(!TextUtils.isEmpty(mobile)){
                userEntity.setNumber(mobile);
                GlobalParams.getUserDao().update(userEntity);
            }
        }
    }

    public UserBaseInfo getUserMobile(){
        UserBaseInfo userBaseInfo = new UserBaseInfo();
        userBaseInfo.setMobile((String) SharePreferenceUtil.get(SharePreferencesKey.USERMOBILE, ""));
        List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
        if(list!=null && list.size()==1){
            UserEntity userEntity = list.get(0);
            userBaseInfo.setMobile(userEntity.getNumber());
        }
        return userBaseInfo;
    }

    public void updateUserAvatar(UserBaseInfo userBaseInfo){
//        SharePreferenceUtil.put(SharePreferencesKey.USERAVATAR, userBaseInfo.getAvatar());
        List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
        if(list!=null && list.size()==1){
            UserEntity userEntity = list.get(0);
            if(userBaseInfo!=null){
                userEntity.setAvatar(userBaseInfo.getAvatar());
                GlobalParams.getUserDao().update(userEntity);
            }
        }
    }
    /**
     * 用户退出登录
     * @param isFromToken
     */
    public void userLogout(boolean isFromToken) {
//        clearUserInfo();
        GlobalParams.getUserDao().deleteAll();
//        if(!isFromToken){
//            NotifyUserInfoToGameCenterMgr.deleteIntoGameCenter(LTApplication.instance);
//        }
    }
//    /***
//     *
//     */
//    public void clearUserInfo(){
//        LogUtils.i("zzz","清除用户信息");
//        SharePreferenceUtil.clear();
//    }
    /**
     * 获取用户信息
     *
     * @return
     */
    public UserBaseInfo getUserInfo() {
        UserBaseInfo userBaseInfo = new UserBaseInfo();
        List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
        if(list!=null && list.size()==1){
            userBaseInfo.setMobile(list.get(0).getNumber());
            userBaseInfo.setUserName(list.get(0).getUserName());
            userBaseInfo.setNickname(list.get(0).getNickname());
            userBaseInfo.setAvatar(list.get(0).getAvatar());
            userBaseInfo.setId(Integer.parseInt(list.get(0).getUserid()));
            userBaseInfo.setToken(list.get(0).getToken());
        }else{
            userBaseInfo.setToken("");
        }
        return userBaseInfo;
    }

    /**
     * 是否登录
     *
     * @return false 未登录
     */
    public boolean isLogin() {
//        String token = (String) SharePreferenceUtil.get(SharePreferencesKey.TOKENKEY, "");
        List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
        if(list==null || list.size()==0){
            return false;
        }
        return true;
    }

    /***
     * 用户登录成功之后调用
     *
     * @param baseInfo
     * @param isFromToken
     */
    public void loginSuccess(UserBaseInfo baseInfo, boolean isFromToken) {
        if(!isFromToken){
            ToastUtils.showToast("登录成功");
        }
        savaUserInfo(baseInfo,isFromToken);
        EventBus.getDefault().post(baseInfo);
    }


}
