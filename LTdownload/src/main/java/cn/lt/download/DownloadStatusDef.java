package cn.lt.download;

public class DownloadStatusDef {
    // [-2^7, 2^7 -1]
    public final static byte pending = 1;//等待下载的
    public final static byte connected = 2;
    public final static byte progress = 3;
    public final static byte blockComplete = 4;
    public final static byte retry = 5;
    public final static byte error = -1;
    public final static byte paused = -2;
    public final static byte completed = -3;
    public final static byte warn = -4;
    public final static byte trigger = -5;

    public final static byte MAX_INT = 5;
    public final static byte MIN_INT = -4;
    public final static byte INVALID_STATUS = 0;// 未下载

    public final static int COMPLETE_SIGN_FAIL = 11;// 签名问题

    public static boolean isOver(final int status) {
        return status < 0;
    }

    public static boolean isIng(final int status) {
        return status >= pending && status <= retry;
    }

    /** 此方法供给webview用的，其他慎用！*/
    public static boolean isRealIng(final int status) {
        return status >= connected && status <= retry;
    }

    public static boolean isInvalid(final int status){
        return status == INVALID_STATUS;
    }
}
