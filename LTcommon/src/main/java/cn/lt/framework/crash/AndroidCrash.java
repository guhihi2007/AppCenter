package cn.lt.framework.crash;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class AndroidCrash {
    private static final AndroidCrash instance = new AndroidCrash();

    private AbstractCrashHandler mReporter;

    private String mLogName;

    private AndroidCrash(){}

    public static AndroidCrash getInstance() {
        return instance;
    }

    /**
     * 设置报告处理。
     * @param reporter
     * @return
     */
    public AndroidCrash setCrashReporter(AbstractCrashHandler reporter) {
        mReporter = reporter;
        return this;
    }

    /**
     * 设置日志文件名。
     * @param name
     * @return
     */
    public AndroidCrash setLogFileName(String name) {
        mLogName = name;
        return this;
    }

    public void init(Context mContext) {
        if (mLogName == null) {
            mLogName = "AndroidCrash.log";
        }
        File logFile = getLogFile(mContext, mLogName);
        CrashCatcher.getInstance().init(logFile, mReporter);
        Thread.setDefaultUncaughtExceptionHandler(CrashCatcher.getInstance());
        Log.d("AndroidCrash", "init success: " + Thread.getDefaultUncaughtExceptionHandler().getClass());
    }

    protected static final File getLogFile(Context context, String name) {
        return new File(context.getFilesDir(), name);
    }

}