package cn.lt.android.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.baidu.mobstat.StatService;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.main.SplashADActivity;
import cn.lt.android.notification.NoticeConsts;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.appstore.R;
import cn.lt.framework.util.PreferencesUtils;

/**
 * Created by wenchao on 2016/1/14.
 * ActionBar相关界面基类
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    /**
     * 页面的别名，用于数据统计
     */
    private String mPageAlias = "";
    protected String mEventID = "";

    /** 标记是否点击推送后进入页面的*/
    protected boolean isByPush = false;

    public String getPageAlias() {
        return mPageAlias;
    }

    public void setmPageAlias(String pageAlias, String eventID) {
        this.mPageAlias = pageAlias;
        this.mEventID = eventID;
    }

    public void setmPageAlias(String pageAlias) {
        this.mPageAlias = pageAlias;
    }

    /**
     * 设置页面的别名，用于统计。。
     *
     * 获取页面名称时直接调用{@link #getPageAlias()}
     */
    public abstract void setPageAlias();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageAlias();
        ActivityManager.self().add(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.self().remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPageAlias();
        statEvent();
        StatService.onResume(this);
        if (LTApplication.isBackGroud) {
            LTApplication.isBackGroud = false;
            PreferencesUtils.putLong(this, Constant.FRONT_DESK_TIME,System.currentTimeMillis());
            if (PopWidowManageUtil.needShowGDT(this)) {
                LogUtils.i("Erosion","已经符合条件，可以执行");
                Intent intent = new Intent(this, SplashADActivity.class);
                intent.putExtra("fromGameCenterActivity", true);
                startActivity(intent);
            }
        }

        if (getIntent() != null) {
            isByPush = getIntent().getBooleanExtra(NoticeConsts.isPush, false);
            getIntent().removeExtra(NoticeConsts.isPush);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    public boolean refresh(int status, boolean isRefresh) {
        if (status == 0) {
            if (isRefresh) {
                cn.lt.android.util.ToastUtils.showToast("刷新失败");
                isRefresh = false;
            }
        } else {
            if (isRefresh) {
                cn.lt.android.util.ToastUtils.showToast("刷新完成");
                isRefresh = false;
            }
        }
        return isRefresh;
    }

    protected void statEvent() {
//        String lastPage = FromPageUtil.getLastPage(this.getPageAlias());

        // 点击推送通知栏的启动和精选页不需要上报浏览数据

        StatisticsEventData event = new StatisticsEventData();

        if (Constant.PAGE_AD_DETAIL.equals(getPageAlias())) {
            event.setActionType(ReportEvent.ACTION_ADS_PAGEVIEW);
        } else {
            event.setActionType(ReportEvent.ACTION_PAGEVIEW);
        }
        event.setPage(getPageAlias());
        event.setId(mEventID);
        event.setEvent_detail(isByPush ? "from_push_GETUI" : "");
//        event.setFrom_page( lastPage );
        LogUtils.e("juice","baseActivity上报页面浏览==>" );
        DCStat.pageJumpEvent(event);
    }


    /***
     * 给有网络请求成功之后才能拿到页面名称的页面用
     */
    public void statEventForSingle(String curPage, String id) {
//        String lastPage = FromPageUtil.getLastPage(this.getPageAlias());
//        StatisticsEventData event = new StatisticsEventData();
//        event.setActionType(ReportEvent.ACTION_PAGEVIEW);
//        event.setPage(curPage);
//        event.setId(id);
//        event.setFrom_page(lastPage );
//        DCStat.pageJumpEvent(event);
    }

    private SystemBarTintManager tintManager;

    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.app_theme_color));
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(false);
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityManager.self().remove(this);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

}
