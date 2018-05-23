package cn.lt.framework.threadpool;

/**
 * Created by wenchao on 2015/09/21.
 */
public enum ThreadPriority {
    LOW(19), MEDIUM(10), HIGH(0), HIGHEST(-1);

    private final int code;

    private ThreadPriority(int code) {
        this.code = code;
    }

    public int toInt() {
        return code;
    }
}
