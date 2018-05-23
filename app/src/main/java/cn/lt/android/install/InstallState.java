package cn.lt.android.install;

/**
 * Created by wenchao on 2016/1/21.
 */
public class InstallState {

    /**未安装*/
    public final static int uninstalled = 101;

    /**安装中*/
    public final static byte installing = 102;

    /**已经安装*/
    public final static int installed = 103;

    /**可升级状态*/
    public final static int upgrade = 104;
    /**安装失败*/
    public final static int install_failure = 105;

}
