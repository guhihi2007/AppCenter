package cn.lt.android.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;

import java.util.List;

/**
 * @author chengyong
 * @des 判断服务是否在跑
 */
public class ServiceUtil {

	public static boolean isServiceRunning(Context context , Class<? extends Service> clazz){

		try {
			//1. 得到任务管理器对象
			ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

			//2. 获取所有正在运行的服务
			List<RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);

			//3. 遍历集合
			for (RunningServiceInfo runningServiceInfo : runningServices) {

                //4. 取出每个服务的名字
                String serviceName = runningServiceInfo.service.getClassName();
                if(serviceName.equals(clazz.getName())){
                    return true;
                }
            }
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
}
