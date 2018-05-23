package cn.lt.android.util;

import android.content.Context;
import android.os.Environment;
import android.os.Process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.PhoneInfoUtils;

/**
 * Created by Administrator on 2016/4/22.
 */
public class CrashHandlerUtil implements Thread.UncaughtExceptionHandler {

    private static CrashHandlerUtil sInstance=new CrashHandlerUtil();
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;
    private Context mContext;

    private static final boolean DEBUG = true;
    private static final String PATH="crash";
    private static final String FILE_NAME_SUFFIX = ".txt";

    private CrashHandlerUtil(){
    }

    public static CrashHandlerUtil getInstance(){
        return sInstance;
    }

    public void init(Context context){
        mUncaughtExceptionHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext=context.getApplicationContext();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            importExceptionToSDCard(ex);
            Logger.i("加入异常日志");
        } catch (IOException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
        ex.printStackTrace();
        if (mUncaughtExceptionHandler!=null){
            mUncaughtExceptionHandler.uncaughtException(thread,ex);
        }else{
            Process.killProcess(Process.myPid());
        }
    }

    private void importExceptionToSDCard(Throwable ex) throws IOException{
        //如果SD卡不存在或无法使用，则无法把异常信息写入SD卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if (DEBUG){
                return;
            }
        }
        File dir=new File(PATH);
        if (!dir.exists()){
            dir.mkdir();
        }
        long current=System.currentTimeMillis();
        String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        File file=new File(getSaveDirPath()+"/" +PATH   +time+ FILE_NAME_SUFFIX);
        Logger.i(getSaveDirPath() +PATH   +time+ FILE_NAME_SUFFIX);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            PhoneInfoUtils.getPhoneInfo(mContext,pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.close();
        }catch (Exception e) {
        }
    }

    private String getSaveDirPath() {
        return LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.crash);
    }

}
