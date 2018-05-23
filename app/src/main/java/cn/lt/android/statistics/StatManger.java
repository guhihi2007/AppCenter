package cn.lt.android.statistics;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.GlobalParams;
import cn.lt.android.db.RetryStatisticsEntity;
import cn.lt.android.db.RetryStatisticsEntityDao;
import cn.lt.android.db.StatisticsEntity;
import cn.lt.android.db.StatisticsEntityDao;
import cn.lt.android.main.threadpool.ThreadPoolProxyFactory;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.BuildConfig;
import de.greenrobot.dao.query.QueryBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatManger {

    private Context mContext;
    private StatisticsEntityDao statisticsEntityDao;
    private RetryStatisticsEntityDao retryStatisticsEntityDao;

    private StatManger() {
    }

    public static StatManger self() {
        return DataCollectMangerHolder.sInstance;
    }

    public void init(Context context) {
        this.mContext = context;
        statisticsEntityDao = getStatisticsEntityDao();
        retryStatisticsEntityDao = getRetryStatisticsEntityDao();
    }

    /***
     * 上报数据存库
     *
     * @param data
     */
    public void saveDownloadTempData(final StatisticsEventData data) {
        ThreadPoolProxyFactory.getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    try {
                        StatisticsEntity entity = new StatisticsEntity();
                        entity.setMPkgName(data.getPkgName());
                        entity.setMPage(data.getPage());
                        entity.setMActionType(ReportEvent.ACTION_DOWNLOAD);
                        entity.setMGameID(data.getId()); //ID可能为包名
                        entity.setMDownloadType(data.getDownload_type());//是第一次下载还是升级
                        entity.setMPageID(data.getPageID());
                        entity.setMDownloadMode(data.getDownload_mode());
                        entity.setMInstallMode(data.getInstall_mode());
                        entity.setMAdType(data.getAd_type());
                        entity.setMRemark(data.getEvent_detail());//用Remark字段保存时间详细原因
                        entity.setP1(data.getP1());
                        entity.setP2(data.getP2());
                        entity.setFrom_page(data.getFrom_page());
                        entity.setFrom_id(data.getFrom_id());
                        entity.setResource_type(data.getResource_type());
                        entity.setWord(data.getKeyWord());
                        LogUtils.i("DCStat", "保存下载上报数据:" + data.getEvent_detail() + "download_mode:" + data.getDownload_mode());
                        insertOrUpdateData(entity);
                        LogUtils.i("DCStat", "保存下载上报数据成功");
                    } catch (Exception e) {
                        LogUtils.i("DCStat", "保存下载上报数据失败" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /***
     * 更新或插入下载统计数据
     *
     * @param entity
     */
    private void insertOrUpdateData(final StatisticsEntity entity) {
        if (null == entity) return;
        try {
            StatisticsEntity data = statisticsEntityDao.queryBuilder().where(StatisticsEntityDao.Properties.MPkgName.eq(entity.getMPkgName())).unique();
            if (null != data) {
                LogUtils.i("DCStat", "替换已存在统计信息");
                statisticsEntityDao.delete(data);
                statisticsEntityDao.insert(entity);
            } else {
                LogUtils.i("DCStat", "插入统计信息");
                statisticsEntityDao.insert(entity);
            }
        } catch (Exception e) {
            LogUtils.i("DCStat", "保存/更新下载上报数据异常" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 提交统计数据到服务器；
     *
     * @param data //
     */
    public synchronized void submitDataToService(final StatisticsEventData data) {
        if (data == null) {
            return;
        }
        ThreadPoolProxyFactory.getReportDataThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(500);
                LogUtils.i("777777", "统计数据上报任务加进来了--->" + data.getActionType());
                final String dataStr = data.getString(data.getActionType());
                LogUtils.i("www", "上报的类型:" + data.getActionType());
                NetWorkClient.getHttpClient().setHostType(HostType.DCENTER_HOST).setCallback(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        LogUtils.i("GOOD", "上报成功--->" + dataStr);
                        if (BuildConfig.IS_DEBUGABLE) {
                            saveLog(dataStr);
                        }

                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        LogUtils.i("GOOD", "上报失败--->" + dataStr);
                        saveStatisticUploadFailureData(dataStr);
                    }
                }).bulid().requestDataCenter(dataStr, "app_center");
            }
        });
    }

    /*******************************************************以下是处理上报失败的数据*******************************************************************/

    /***
     * 上报失败的 数据存库
     *
     * @param jsonString 要上报的数据（失败）的jsonString
     */
    public void saveStatisticUploadFailureData(final String jsonString) {
        ThreadPoolProxyFactory.getCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RetryStatisticsEntity entity = new RetryStatisticsEntity();
                    entity.setMUploadFailureDataByJsonString(jsonString);    //jsonString
                    retryStatisticsEntityDao.insert(entity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 提交统计数据到服务器；(上报之前提交失败的数据)
     *
     * @param entity
     * @des 监听到网络良好时，调用此方法。
     */
    public synchronized void submitDataToServer2BackUp(final RetryStatisticsEntity entity) {
        ThreadPoolProxyFactory.getReportDataThreadPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(500);
                LogUtils.i("777777", "上报之前提交失败的数据=" + entity.getMUploadFailureDataByJsonString());
                NetWorkClient.getHttpClient().setHostType(HostType.DCENTER_HOST).setCallback(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        String data = entity.getMUploadFailureDataByJsonString();
                        LogUtils.i("GOOD", "上报之前提交失败的数据---成功！" + "==数据为：" + data);
                        if (BuildConfig.IS_DEBUGABLE) {
                            saveLog(data);
                        }
                        //移除数据库   --这里移除数据库
                        retryStatisticsEntityDao.deleteByKey(entity.getId());
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        LogUtils.i("GOOD", "上报之前提交失败的数据---失败！" + "==数据为：" + entity.getMUploadFailureDataByJsonString());
                        //不做任何操作
                    }
                }).bulid().requestDataCenter(entity.getMUploadFailureDataByJsonString(), "app_center");
            }
        });
    }

    private StatisticsEntityDao getStatisticsEntityDao() {
        return GlobalParams.getStatisticsEntityDao();
    }

    private RetryStatisticsEntityDao getRetryStatisticsEntityDao() {
        return GlobalParams.getRetryStatisticsEntityDao();
    }


    /***
     * @des 从数据库获取上报失败的集合。
     * @des 要上报数据事调用。
     */
    public List<RetryStatisticsEntity> getStatUploadFailureListByDb() {
        QueryBuilder query = retryStatisticsEntityDao.queryBuilder().where(RetryStatisticsEntityDao.Properties.MUploadFailureDataByJsonString.isNotNull());
        List<RetryStatisticsEntity> list = query.list();
        List<RetryStatisticsEntity> mDataList = new ArrayList();
        for (RetryStatisticsEntity e : list) {
            mDataList.add(new RetryStatisticsEntity(e.getId(), e.getMUploadFailureDataByJsonString()));
        }
        return mDataList;
    }

    public static class DataCollectMangerHolder {
        public static StatManger sInstance = new StatManger();
    }

    private void saveLog(String dataStr) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LTDirectoryManager.ROOT_FOLDER + File.separator + "log");
        File log = new File(file.getAbsolutePath() + File.separator + "AppCenterLog.txt");
        if (file.exists()) {
            if (!log.exists()) {
                try {
                    log.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileWriter fw = null;
            try {
                fw = new FileWriter(log.getAbsolutePath(), true);
                fw.write(dataStr + "\r\n");
                fw.flush();
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fw != null)
                        fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
