package cn.lt.download.services;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 */
class FileDownloadThreadPool {

    private SparseArray<FileDownloadRunnable> runnablePool = new SparseArray<>();

    // TODO 对用户开放线程池大小，全局并行下载数
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);

    public synchronized void execute(FileDownloadRunnable runnable) {
        runnable.onResume();
        threadPool.execute(runnable);
        runnablePool.put(runnable.getId(), runnable);

        final int CHECK_THRESHOLD_VALUE = 600;
        if (mIgnoreCheckTimes >= CHECK_THRESHOLD_VALUE) {
            checkNoExist();
            mIgnoreCheckTimes = 0;
        } else {
            mIgnoreCheckTimes++;
        }
    }


    private int mIgnoreCheckTimes = 0;

    public synchronized void checkNoExist() {
        SparseArray<FileDownloadRunnable> correctedRunnablePool = new SparseArray<>();
        for (int i = 0; i < runnablePool.size(); i++) {
            final int key = runnablePool.keyAt(i);
            final FileDownloadRunnable runnable = runnablePool.get(key);
            if (runnable.isExist()) {
                correctedRunnablePool.put(key, runnable);
            }
        }
        runnablePool = correctedRunnablePool;

    }

    public boolean isInThreadPool(final int downloadId) {
        final FileDownloadRunnable runnable = runnablePool.get(downloadId);
        return runnable != null && runnable.isExist();
    }

    public synchronized int exactSize(){
        checkNoExist();
        return runnablePool.size();
    }

    public synchronized List<Integer> getAllExactRunningDownloadIds() {
        checkNoExist();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < runnablePool.size(); i++) {
            list.add(runnablePool.get(runnablePool.keyAt(i)).getId());
        }

        return list;
    }

    /** 从线程池中终止，并且从runnablePool中移除*/
    public synchronized boolean cancelDwonloadThread(int downloadId) {
        FileDownloadRunnable runnable = runnablePool.get(downloadId);
        if(runnable != null){
            Future<FileDownloadRunnable> future = (Future<FileDownloadRunnable>) threadPool.submit(runnable);
            boolean isCancel = future.cancel(true);
            if(isCancel) {
                runnablePool.remove(downloadId);
            }
            return isCancel;
        }
        return false;
    }

    /** 根据downloadId获取线程池中的Runnable*/
    public FileDownloadRunnable getRunnableByDownloadId(int downloadId) {
        return runnablePool.get(downloadId);
    }

}
