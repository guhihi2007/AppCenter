package cn.lt.android.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.db.LoginHistoryEntity;
import cn.lt.android.db.LoginHistoryEntityDao;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.LogUtils;

/**
 * Created by wenchao on 2016/1/20.
 * fragment基类
 */
public abstract class BaseFragment extends Fragment {
    public static final String FRAGMENT_TAB = "fragment_tab";
    public View mRootView;
    public boolean isAdv;
    public Context mContext;
    protected String mEventID = "";

    protected String adMold = "";// 广告所属商家
    /**
     * 页面的别名，用于数据统计
     */
    private String mPageAlias = "";

    /** 是否来自推送通知*/
    protected boolean isByPush = false;

    public String getPageAlias() {
        return mPageAlias;
    }

    public void setmPageAlias(String pageAlias) {
        this.mPageAlias = pageAlias;
    }

    public void setmPageAlias(String pageAlias, String id) {
        this.mPageAlias = pageAlias;
        this.mEventID = id;
    }

    public void setmPageAlias(String pageAlias, String id, boolean isAdv) {
        this.mPageAlias = pageAlias;
        this.mEventID = id;
        this.isAdv = isAdv;
    }

    /**
     * 设置页面的别名，用于统计。。
     * 复写该该方法时直接调用{@link #setmPageAlias(String s)}赋值；
     * 获取页面名称时直接调用{@link #getPageAlias()}
     */
    public abstract void setPageAlias();

    /**
     * 设置刷新结果
     * @param status 刷新结果
     * @param isRefresh
     * @return
     */
    public boolean refresh(int status, boolean isRefresh) {
        if (status == 0) {
            if (isRefresh) {
                cn.lt.android.util.ToastUtils.showToast("刷新失败");
            }
        } else {
            if (isRefresh) {
                cn.lt.android.util.ToastUtils.showToast("刷新完成");
            }
        }
        return false;
    }


    /**
     * 保存数据到数据库
     * @param userBaseInfo
     */
    public void saveUserInfoHistory(UserBaseInfo userBaseInfo) {
        LoginHistoryEntity loginHistoryEntity = new LoginHistoryEntity();
        loginHistoryEntity.setId(null);
        loginHistoryEntity.setUserId((long) userBaseInfo.getId());
        loginHistoryEntity.setAvatar(userBaseInfo.getAvatar());
        loginHistoryEntity.setMobile(userBaseInfo.getMobile());
        loginHistoryEntity.setToken(userBaseInfo.getToken());
        loginHistoryEntity.setEmail(userBaseInfo.getEmail());
        loginHistoryEntity.setNickName(userBaseInfo.getNickname());
        List<LoginHistoryEntity> list = GlobalParams.getLoginHistoryEntityDao().queryBuilder().where(LoginHistoryEntityDao.Properties.UserId.eq(userBaseInfo.getId())).list();
        if (list.size() == 0) {
            GlobalParams.getLoginHistoryEntityDao().insert(loginHistoryEntity);
        }else{
            deleteHistoryDataByUserId((long)userBaseInfo.getId());
            GlobalParams.getLoginHistoryEntityDao().insert(loginHistoryEntity);
        }

    }

    /**
     * 更新修改后的数据  到数据库
     * @param userBaseInfo
     */
    public void updateData2Db(UserBaseInfo userBaseInfo) {
        List<LoginHistoryEntity> list = GlobalParams.getLoginHistoryEntityDao().queryBuilder().where(LoginHistoryEntityDao.Properties.UserId.eq(userBaseInfo.getId())).list();
        if (list.size() != 0) {
            for (LoginHistoryEntity loginHistoryEntity : list) {
                loginHistoryEntity.setAvatar(userBaseInfo.getAvatar());
                loginHistoryEntity.setMobile(userBaseInfo.getMobile());
                loginHistoryEntity.setToken(userBaseInfo.getToken());
                loginHistoryEntity.setEmail(userBaseInfo.getEmail());
                loginHistoryEntity.setNickName(userBaseInfo.getNickname());
                GlobalParams.getLoginHistoryEntityDao().update(loginHistoryEntity);
            }
        }
    }

    /***
     * 根据UserId删除记录
     */
    public  void deleteHistoryDataByUserId(Long UserId) {
        if (UserId==0) return;
        LoginHistoryEntity entity = findEntityByUserId(UserId);
        if (entity != null) {
            GlobalParams.getLoginHistoryEntityDao().delete(entity);
        }
    }

    private  LoginHistoryEntity findEntityByUserId(Long UserId) {
        return GlobalParams.getLoginHistoryEntityDao().queryBuilder().where(LoginHistoryEntityDao.Properties.UserId.eq(UserId)).unique();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setPageAlias();
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    /**
     * 统计lll,这个全部放到子类去调用
     */
    public void statEvent() {
        try {

            // 通过点击推送的推荐页，不需要上报页面浏览数据
            if (isByPush && getPageAlias().equals(Constant.PAGE_RECOMMEND)) {
                return;
            }


            StatisticsEventData event = new StatisticsEventData();
            event.setActionType(ReportEvent.ACTION_PAGEVIEW);
            event.setPage(this.getPageAlias());
            event.setId(mEventID);
            if (isAdv) {
                event.setActionType(ReportEvent.ACTION_ADS_PAGEVIEW);
                event.setAd_type(adMold);
            } else {
                event.setActionType(ReportEvent.ACTION_PAGEVIEW);
            }

            event.setEvent_detail(isByPush ? "from_push_GETUI" : "");
            LogUtils.e("juice","baseFragment 上报页面浏览==>" );
            DCStat.pageJumpEvent(event);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onResume() {
        LogUtils.d("ppp","基类onResume走了");
        super.onResume();
        getByPush();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        getByPush();
    }

    private void getByPush() {
        try {
            isByPush = ((BaseAppCompatActivity)getActivity()).isByPush;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
