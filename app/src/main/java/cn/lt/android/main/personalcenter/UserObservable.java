package cn.lt.android.main.personalcenter;

import android.database.ContentObserver;
import android.os.Handler;

import java.util.List;

import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.db.UserEntity;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * @author chengyong
 * @time 2017/9/21 11:08
 * @des ${观察游戏中心的登录变化}
 */

public class UserObservable extends ContentObserver {
    public UserObservable(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
//        LogUtils.d(LogTAG.USER,"应用市场UserObservable：游戏中心来改我的数据了，selfChange-"+selfChange);
//        List<UserEntity> list = GlobalParams.getUserDao().queryBuilder().list();
//        if(list!=null && list.size()==1){
//            requestUserInfo();
//        }else{
//            LogUtils.d(LogTAG.USER,"应用市场UserObservable：检查自己的数据库无数据 登出");
//            UserInfoManager.instance().userLogout(true);
//        }
    }

    /**
     * token登录请求
     *
     */
    private void requestUserInfo() {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                UserBaseInfo userBaseInfo = response.body();
                LogUtils.d(LogTAG.USER, "应用市场UserObservable：：MineFragment:token登录成功：" + userBaseInfo);
                if (null != userBaseInfo) {
                    userBaseInfo.setToken(UserInfoManager.instance().getUserInfo().getToken());//token请求没有返回token
                    UserInfoManager.instance().loginSuccess(userBaseInfo,true);
                    DCStat.baiduStat(LTApplication.instance, "login_success", "登录成功：" + userBaseInfo.getId()); //统计登录成功
                } else {
                    ToastUtils.showToast("信息异常！");
                }
            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                LogUtils.d(LogTAG.USER, "应用市场UserObservable：：token登路失败：" + t.getMessage());
            }
        }).bulid().requestUserInfoForToken();
    }
}
