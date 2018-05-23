/*
package cn.lt.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import cn.lt.android.Constant;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;

*/
/**
 * @author chengyong
 * @time 2016/5/2 1:51
 * @des 监听网络变化，任务列表里面网络图标的UI变化。上报之前统计失败的数据
 *//*

public class NetWorkBroadcastReceiver extends BroadcastReceiver {
    private int mNetType;
    public NetWorkBroadcastReceiver(Context context) {
        this.context = context;
    }

    public ChangeListener onJumpListener;
    public Context context;

    public interface ChangeListener {
        void changeNetType(int type);
    }

    public void setOnJumpListener(ChangeListener onJumpListener) {
        this.onJumpListener = onJumpListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            mNetType = NetUtils.getNetType(context);
            if (mNetType == ConnectivityManager.TYPE_WIFI) {
                LogUtils.i("hhhh", "WiFi网络");
                if (null != onJumpListener) {
                    onJumpListener.changeNetType(Constant.NET_WIFI);
                }
            } else if (mNetType == ConnectivityManager.TYPE_MOBILE) {
                LogUtils.i("hhhh", "4g网络");
                if (null != onJumpListener) {
                    onJumpListener.changeNetType(Constant.NET_MOBILE_PHONE);
                }
            } else {
                LogUtils.i("hhhh", "没有网络");
                if (null != onJumpListener) {
                    onJumpListener.changeNetType(Constant.NO_NET);
                }
            }
//            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            if (wifiInfo.getBSSID() != null) {
//                //wifi名称
//                String ssid = wifiInfo.getSSID();
//                //wifi信号强度
//                int signalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
//                //wifi速度
//                int speed = wifiInfo.getLinkSpeed();
//                //wifi速度单位
//                String units = WifiInfo.LINK_SPEED_UNITS;
//                LogUtils.i("receiver", "ssid="+ssid+",signalLevel="+signalLevel+",speed="+speed+",units="+units);
//            }
        }
    }

//    private void submitFailureData() {
//        List<StatisticsEntity> statUploadFailureListByDb = StatManger.self().getStatUploadFailureListByDb();
//        if(statUploadFailureListByDb.size()==0) return;
//        for (StatisticsEntity entity : statUploadFailureListByDb ) {
//            LogUtils.i("hhhh", "重新上报中，上报的数据是：==>"+entity.getMUploadFailureDataByJsonString()+"<==id是：==>"+entity.getId());
//            StatManger.self().submitDataToServer2BackUp(entity);
//        }
//    }
}
*/
