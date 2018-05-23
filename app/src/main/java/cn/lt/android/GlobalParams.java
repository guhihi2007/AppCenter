package cn.lt.android;

import android.database.sqlite.SQLiteDatabase;

import cn.lt.android.db.AppEntityDao;
import cn.lt.android.db.DaoMaster;
import cn.lt.android.db.DaoSession;
import cn.lt.android.db.IgnoreUpgradeAppEntityDao;
import cn.lt.android.db.LoginHistoryEntityDao;
import cn.lt.android.db.RetryStatisticsEntityDao;
import cn.lt.android.db.SearchHistoryEntityDao;
import cn.lt.android.db.StatisticsEntityDao;
import cn.lt.android.db.UserEntityDao;
import cn.lt.android.db.WakeTaskEntityDao;
import cn.lt.android.download.DownloadRedPointManager;
import cn.lt.android.download.UserRedPointManager;
import cn.lt.android.network.bean.HostBean;

/**
 * Created by wenchao on 2016/1/14.
 * 应用内保存的数据
 */
public class GlobalParams {

    private static DaoSession mDaoSession;

    private static HostBean mHostBean = new HostBean();
    public static SQLiteDatabase db;
    public static DaoMaster.DevOpenHelper helper;


    public static void init() {
        setupDatabase();
        DownloadRedPointManager.getInstance();
        UserRedPointManager.getInstance();
    }

    private static synchronized void setupDatabase() {
        //初始化数据ku
        helper = new DaoMaster.DevOpenHelper(LTApplication.shareApplication(), "lt_appstore_db", null);
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
    }

    public static SQLiteDatabase getDbUser() {
        return mDaoSession.getDatabase();
    }

    public static SQLiteDatabase getDb() {
        return db;
    }

    public static AppEntityDao getAppEntityDao() {
        return mDaoSession.getAppEntityDao();
    }

    public static SearchHistoryEntityDao getSearchHisoryEntityDao() {
        return mDaoSession.getSearchHistoryEntityDao();
    }

    public static StatisticsEntityDao getStatisticsEntityDao() {
        return mDaoSession.getStatisticsEntityDao();
    }

    public static RetryStatisticsEntityDao getRetryStatisticsEntityDao() {
        return mDaoSession.getRetryStatisticsEntityDao();
    }

    public static LoginHistoryEntityDao getLoginHistoryEntityDao() {
        return mDaoSession.getLoginHistoryEntityDao();
    }
    public static UserEntityDao getUserDao() {
        return mDaoSession.getUserEntityDao();
    }
    public static WakeTaskEntityDao getWakeTaskDao() {
        return mDaoSession.getWakeTaskEntityDao();
    }

    /**
     * 忽略升级数据库dao
     *
     * @return
     */
    public static IgnoreUpgradeAppEntityDao getIgnoreUpgradeAppEntityDao() {
        return mDaoSession.getIgnoreUpgradeAppEntityDao();
    }
    public static HostBean getHostBean() {
        return mHostBean;
    }

    public static void setHostBean(HostBean hostBean) {
        mHostBean = hostBean;
    }

}
