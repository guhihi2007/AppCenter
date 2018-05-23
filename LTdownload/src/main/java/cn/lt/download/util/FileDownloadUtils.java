package cn.lt.download.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileDownloadUtils {

    private static final String TAG = "FileDownloadUtils";

    /**
     * Checks whether the filename looks legitimate
     */
    public static boolean isFilenameValid(String filename) {
//        filename = filename.replaceFirst("/+", "/"); // normalize leading
        // slashes
//        return filename.startsWith(Environment.getDownloadCacheDirectory()
//                .toString())
//                || filename.startsWith(Environment
//                .getExternalStorageDirectory().toString());
        return true;
    }

    public static String getDefaultSaveRootPath() {
        if (ContextHolder.getAppContext().getExternalCacheDir() == null) {
            return Environment.getDownloadCacheDirectory().getAbsolutePath();
        } else {
            return ContextHolder.getAppContext().getExternalCacheDir().getAbsolutePath();
        }
    }

    public static String getDefaultSaveFilePath(final String url) {
        return String.format("%s%s%s", getDefaultSaveRootPath(), File.separator, md5(url));
    }


    public static int generateId(final String url, final String path) {
        if(url == null || TextUtils.isEmpty(url)){
            Log.e(TAG,"getDownloadId failed,because downloadUrl == null");
            return 0;
        }
        if(path == null || TextUtils.isEmpty(path)){
            Log.e(TAG,"getDownloadId failed,because downloadPath == null");
            return 0;
        }
        return md5(String.format("%sp%s", url, path)).hashCode();
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


    // stack

    public static String getStack() {
        return getStack(true);
    }

    public static String getStack(final boolean printLine) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        return getStack(stackTrace, printLine);
    }

    public static String getStack(final StackTraceElement[] stackTrace, final boolean printLine) {
        if ((stackTrace == null) || (stackTrace.length < 4)) {
            return "";
        }

        StringBuilder t = new StringBuilder();

        for (int i = 3; i < stackTrace.length; i++) {
            if (!stackTrace[i].getClassName().contains("com.liulishuo.filedownloader")) {
                continue;
            }
            t.append("[");
            t.append(stackTrace[i].getClassName().substring("com.liulishuo.filedownloader".length()));
            t.append(":");
            t.append(stackTrace[i].getMethodName());
            if (printLine) {
                t.append("(").append(stackTrace[i].getLineNumber()).append(")]");
            } else {
                t.append("]");
            }
        }
        return t.toString();
    }

    public static boolean isDownloaderProcess(final Context context) {
        int pid = android.os.Process.myPid();
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == pid) {
                return runningAppProcessInfo.processName.endsWith(":lt_downloader");
            }
        }

        return false;
    }

    public static int encodeLong2Int(final long size) {
        if (size < 0) {
            return 0;
        }

        // (0, Integer.MAX_VALUE]
        if (size <= Integer.MAX_VALUE) {
            return (int) size;
        }

        // (Integer.MAX_VALUE, Integer.MAX_VALUE - Integer.MIN_VALUE]
        if (size > Integer.MAX_VALUE && size <= (Integer.MAX_VALUE + (long) (-Integer.MIN_VALUE))) {
            return (int) (size + Integer.MIN_VALUE);
        }

        return Integer.MAX_VALUE;
    }

    public static long decodeInt2Long(final int size, final boolean useNegative) {
        if (useNegative) {
            return size + (long)(-Integer.MIN_VALUE);
        }

        return size;
    }
}
