package cn.lt.android.install;

import cn.lt.android.db.AppEntity;

/**
 * @author chengyong
 * @time 2017/2/12 11:50
 * @des ${安装完成轮询的动态代理类}   二大好处：   方法增强；  调用接口中的方法时可以增加一些公共的逻辑
 */

public class InstalledLooperProxy {

    private static class SingleHolder {
        private static final InstalledLooperProxy INSTANCE=new InstalledLooperProxy();
    }

    public static InstalledLooperProxy getInstance() {
        return SingleHolder.INSTANCE;
    }
    /**
     * 移除集合中的Entity
     */
    public void removeLooperEntity() {
//        InstalledEventLooperDao installedEventLooperDao = new InstalledEventLooperDao();
//        InstallLooperHandler installLooperHandler = new InstallLooperHandler(installedEventLooperDao);
//        IntallDao proxyInstance = (IntallDao)Proxy.newProxyInstance(installedEventLooperDao.getClass().getClassLoader(), installedEventLooperDao.getClass().getInterfaces(), installLooperHandler);
//        InstalledEventLooperDao.getInstance().removeLooperEntity("com.noblemetal");
    }
    public void startInstall(final AppEntity entity) {
//        InstalledEventLooperDao installedEventLooperDao = new InstalledEventLooperDao();
//        InstallLooperHandler installLooperHandler = new InstallLooperHandler(installedEventLooperDao);
//        IntallDao proxyInstance = (IntallDao)Proxy.newProxyInstance(installedEventLooperDao.getClass().getClassLoader(), installedEventLooperDao.getClass().getInterfaces(), installLooperHandler);
        InstalledEventLooperDao.getInstance().startInstall(entity);
    }
}
