package cn.lt.framework.fs;

import java.io.File;

/**
 * Created by wenchao on 2016/1/19.
 */
public class CacheChecker {
    public CacheChecker() {
    }

    public static boolean expired(File file, long expiredTimeMs) {
        long current = System.currentTimeMillis() / 1000L * 1000L - file.lastModified();
        return current < 0L || current >= expiredTimeMs;
    }

    public static boolean checkCache(String path, long expiredtime) {
        return checkCache(path, expiredtime, true);
    }

    public static boolean checkCache(String path, long expiredtime, boolean del) {
        File file = new File(path);
        if(!file.exists()) {
            return false;
        } else if(!expired(file, expiredtime)) {
            return true;
        } else {
            if(del) {
                file.delete();
            }

            return false;
        }
    }

    public static boolean checkCache(File file, long expiredtime, boolean del) {
        if(!file.exists()) {
            return false;
        } else if(!expired(file, expiredtime)) {
            return true;
        } else {
            if(del) {
                file.delete();
            }

            return false;
        }
    }
}
