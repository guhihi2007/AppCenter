package cn.lt.android.statistics;

import android.text.TextUtils;
import android.util.Log;

import cn.lt.android.main.entrance.data.PresentData;

/***
 * Created by Administrator on 2015/12/12.
 */
public class StatisticsDataProductorImpl {

    /**
     * 获取统计相关数据，需要在子类实现。。
     */
    public static StatisticsEventData produceStatisticsData(String presentType, String id, String pageName, int position, String srcType) {
        StatisticsEventData mStatisticsData = null;
        try {
            mStatisticsData = new StatisticsEventData();
            mStatisticsData.setActionType(ReportEvent.ACTION_CLICK);
            mStatisticsData.setPresentType(presentType);
            mStatisticsData.setP2(position);
            mStatisticsData.setId(id);//资源位ID
            mStatisticsData.setPage(pageName);
            mStatisticsData.setSrcType(srcType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mStatisticsData;
    }


    /**
     * 获取统计相关数据，需要在子类实现。。id有得页面也是拿不到的。
     */
    public static StatisticsEventData produceStatisticsData(PresentData data, boolean isAdData, int position, String id, String pageName, String srcType) {
        StatisticsEventData mStatisticsData = null;
        try {
            mStatisticsData = new StatisticsEventData();
            mStatisticsData.setActionType(isAdData ? ReportEvent.ACTION_ADCLICK : ReportEvent.ACTION_CLICK);
            mStatisticsData.setPresentType(data.getmType().presentType);
            mStatisticsData.setP2(position);
            mStatisticsData.setId(TextUtils.isEmpty(id) ? "" : id);//资源位ＩＤ
            Log.i("zzz", "sameTypePosition==" + data.getPos());
            mStatisticsData.setP1(data.getPos());
            mStatisticsData.setPage(pageName);
            mStatisticsData.setSrcType(srcType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mStatisticsData;
    }
}
