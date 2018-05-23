package cn.lt.android.statistics;

import java.util.List;

import cn.lt.android.db.RetryStatisticsEntity;
import cn.lt.android.util.LogUtils;

/**
 * @author chengyong
 * @time 2016/9/19 10:34
 * @des 处理上报失败的数据
 */
public class StatFailureManager {
    public static void submitFailureData() {
        LogUtils.i("hhh", "重新上报方法调用了");
        List<RetryStatisticsEntity> statUploadFailureListByDb = StatManger.self().getStatUploadFailureListByDb();
        if(statUploadFailureListByDb.size()==0) return;
        for (RetryStatisticsEntity entity : statUploadFailureListByDb ) {
            if(entity.getMUploadFailureDataByJsonString()!=null){
                LogUtils.i("hhh", "重新上报中，上报的数据是：==>"+entity.getMUploadFailureDataByJsonString()+"<==id是：==>"+entity.getId());
                StatManger.self().submitDataToServer2BackUp(entity);
            }
        }
    }
}
