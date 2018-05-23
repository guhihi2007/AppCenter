package cn.lt.framework.crash;

import java.io.File;

/**
 * 崩溃监听。
 */
public interface CrashListener {
    /**
     * 发送日志文件。
     * @param file
     */
    public void sendFile(File file);

    /**
     * 退出APP。
     * @param thread
     * @param ex
     */
    public void closeApp(Thread thread, Throwable ex);
}