package cn.lt.android.install;

import cn.lt.android.db.AppEntity;

/**
 * @author chengyong
 * @time 2017/2/12 11:42
 * @des ${安装完成轮询的接口}
 */

public interface IntallDao {
     void startInstall(final AppEntity entity);
     void removeLooperEntity();
}
