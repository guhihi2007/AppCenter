package cn.lt.download.services;


import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.lt.download.DownloadStatusDef;
import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.event.IEvent;
import cn.lt.download.event.IEventListener;
import cn.lt.download.model.DownloadInfo;
import cn.lt.download.model.DownloadModel;
import cn.lt.download.util.FileDownloadLog;
import cn.lt.download.util.FileDownloadUtils;

class FileDownloadMgr {
    private final IFileDownloadDBHelper mHelper;

    // TODO 对OkHttpClient，看如何可以有效利用OkHttpClient进行相关优化，进行有关封装
    private final OkHttpClient client;

    private final FileDownloadThreadPool mThreadPool = new FileDownloadThreadPool();

    public FileDownloadMgr() {
        mHelper = new FileDownloadDBHelper();
        // init client
        client = new OkHttpClient();
        // TODO 设置超时
        client.setConnectTimeout(3, TimeUnit.SECONDS);
    }


    // synchronize for safe: check downloading, check resume, update data, execute runnable
    public synchronized void start(String packageName,String url, String path, int callbackProgressTimes,
                                   int autoRetryTimes, boolean isOrderWifiDownload) {
        final int id = FileDownloadUtils.generateId(packageName, path);

        // check is already in download pool
        if (checkDownloading(packageName, path)) {
            FileDownloadLog.d(this, "has already started download %d", id);
            // warn
            final DownloadInfo warnModel = new DownloadInfo();
            warnModel.setDownloadId(id);
            warnModel.setPackageName(packageName);
            warnModel.setStatus(DownloadStatusDef.warn);

            DownloadServiceEventBusManager.getEventBus()
                    .publishByService(new DownloadInfoEvent(warnModel));
            return;
        }

        // real start

        // - create model
        DownloadModel model = findDownloadModel(id);
//        Log.d("sss","sofar："+model.getSoFar()+"total："+model.getTotal()+"pakage："+model.getPackageName()+"getStatus："+model.getStatus()+"ID："+model.getId());
//        boolean needUpdate2DB;
//                && model.getStatus() == DownloadStatusDef.paused
        if (model != null) {
            // TODO pending data is no use, if not resume by break point
        } else {
            model = new DownloadModel();
            model.setUrl(url);
            model.setPath(path);

            model.setId(id);
            model.setPackageName(packageName);
            model.setSoFar(0);
            model.setTotal(0);
            model.setStatus(DownloadStatusDef.pending);

            // insert model to db
            mHelper.update(model);
        }

        model.setCallbackProgressTimes(callbackProgressTimes);
        model.setIsCancel(false);

        // 属于预约wifi下载的，需要直接变暂停
        if(isOrderWifiDownload) {
            model.setIsCancel(true);
        }

        // - execute
        mThreadPool.execute(new FileDownloadRunnable(client, model, mHelper, autoRetryTimes));

    }

    private DownloadModel findDownloadModel(int downloadId) {
        DownloadModel model = mHelper.find(downloadId);
        if(model != null) {
            File file = new File(model.getPath());
            if(file.exists()) {
                Log.i("jasdofie", "file路径下安装文件存在，返回model,继续下载");
                return model;
            } else {
                Log.i("jasdofie", "file路径下安装文件不存在，返回null,重新下载");
            }
        }

        return null;
    }

    public synchronized boolean checkDownloading(String packageName, String path) {
        final int downloadId = FileDownloadUtils.generateId(packageName, path);
        final DownloadModel model = mHelper.find(downloadId);
        mThreadPool.checkNoExist();
        final boolean isInPool = mThreadPool.isInThreadPool(downloadId);

        boolean isDownloading = false;
        if (model == null) {

            isDownloading = false;

            // 数据库中没有，确在队列线程池，视为脏线程，清理掉
            if (isInPool) {
                mThreadPool.cancelDwonloadThread(downloadId);
            }

        } else {

            if (isInPool) {

                if (model.getStatus() != DownloadStatusDef.pending && model.getStatus() != DownloadStatusDef.progress) {
                    // status 不是pending/processing & 线程池有，只有可能是线程同步问题，status已经设置为complete/error/pause但是线程还没有执行完
                    // TODO 这里需要特殊处理，小概率事件，需要对同一DownloadId的Runnable与该方法同步
                    if (model.getStatus() == DownloadStatusDef.error
                            || model.getStatus() == DownloadStatusDef.retry
                            || model.getStatus() == DownloadStatusDef.paused
                            || model.getStatus() == DownloadStatusDef.completed) {
                        boolean isCencel = mThreadPool.cancelDwonloadThread(downloadId);
                        if (isCencel) {
                            isDownloading = false;
                        }
                    }
                } else {
                    // status 是pending/processing & 线程池有，直接返回正在下载中
                    isDownloading = true;
                }


            } else {
                // status 是pending/processing & 线程池没有，只有可能异常状态，打e级log，直接放回不在下载中
                FileDownloadLog.e(this, "status is[%s] & thread is not has %d", model.getStatus(), downloadId);
                isDownloading = false;
            }
        }

        return isDownloading;

    }

    /**检测是否可以断点续传
     * @return can resume by break point
     */
    public static boolean checkBreakpointAvailable(final int downloadId, final DownloadModel model) {
        boolean result = false;

        do {
            if (model == null) {
                FileDownloadLog.d(FileDownloadMgr.class, "can't continue %d model == null", downloadId);
                Log.i("jiulai", "checkBreakpointAvailable~model == null");
                break;
            }

            if (model.getStatus() != DownloadStatusDef.paused
                    && model.getStatus() != DownloadStatusDef.retry
                    && model.getStatus() != DownloadStatusDef.error
                    && model.getStatus() != DownloadStatusDef.pending // may pending in case of enqueue
                    ) {
                FileDownloadLog.d(FileDownloadMgr.class, "can't continue %d status[%d] isn't paused",
                        downloadId, model.getStatus());
                Log.i("jiulai", "checkBreakpointAvailable~一大堆状态");
                break;
            }
            /*
            if (TextUtils.isEmpty(model.getETag())) {
                FileDownloadLog.d(FileDownloadMgr.class, "can't continue %d etag is empty", downloadId);
                break;
            }
            */

            File          file        = new File(model.getPath());
            final boolean isExists    = file.exists();
            final boolean isDirectory = file.isDirectory();

            if (model.getStatus() != DownloadStatusDef.pending
                    && model.getStatus() != DownloadStatusDef.paused
                    && ((!isExists && model.getSoFar() != 0) || isDirectory ) ) {
                FileDownloadLog.d(FileDownloadMgr.class, "can't continue %d file not suit, exists[%B], directory[%B]",
                        downloadId, isExists, isDirectory);
                if(!isExists) {

                    Log.i("jiulai", "checkBreakpointAvailable~!isExists");
                }

                if(!isDirectory) {

                    Log.i("jiulai", "checkBreakpointAvailable~isDirectory");
                }
                break;
            }

            final long fileLength = file.length();

            if (model.getStatus() != DownloadStatusDef.pending
                    && model.getStatus() != DownloadStatusDef.paused
                    && (fileLength < model.getSoFar()
                    || (model.getTotal() != -1  // not chunk transfer encoding data
                    && fileLength >= model.getTotal() && model.getStatus() != DownloadStatusDef.error))) {
                // 脏数据
                FileDownloadLog.d(FileDownloadMgr.class, "can't continue %d dirty data fileLength[%d] sofar[%d] total[%d]",
                        downloadId, fileLength, model.getSoFar(), model.getTotal());
                if(fileLength < model.getSoFar()) {
                    Log.i("jiulai", "checkBreakpointAvailable~fileLength < model.getSoFar()");
                }

                if((model.getTotal() != -1  // not chunk transfer encoding data
                        && fileLength >= model.getTotal())) {
                    Log.i("jiulai", "checkBreakpointAvailable~(model.getTotal() != -1  // not chunk transfer encoding data\n" +
                            "                    && fileLength >= model.getTotal()");
                }

                break;

            }

            result = true;
        } while (false);


        return result;
    }

    public synchronized DownloadInfo checkReuse(final int downloadId) {
        DownloadInfo transferModel = null;

        final DownloadModel model    = mHelper.find(downloadId);

        final boolean           canReuse = checkReuse(downloadId, model);
        Log.d("dfjkd","fjkdj"+canReuse+"dizhi=="+model.toString());
        if (canReuse) {
            transferModel = new DownloadInfo(model);
            transferModel.setDownloadId(model.getId());
            transferModel.setPackageName(model.getPackageName());
            transferModel.setUseOldFile(true);
        }

        return transferModel;
    }

    /**
     * 供app进程调用 ，拿到通讯 DownloadInfo
     * @param downloadId
     * @return
     */
    public synchronized DownloadInfo getCommonDownloadInfo(final int downloadId) {
        DownloadInfo transferModel = new DownloadInfo();
        final DownloadModel model = mHelper.find(downloadId);  //先测试一下效果
        if (model == null) {
            transferModel.setSoFarBytes(0);
            transferModel.setTotalBytes(0);
            transferModel.setStatus(DownloadStatusDef.INVALID_STATUS);
            Log.d("FileDownMgr","model为空");
        } else {
            transferModel.setSoFarBytes(model.getSoFar());
            transferModel.setTotalBytes(model.getTotal());
            transferModel.setStatus(model.getStatus());
            Log.d("FileDownMgr", "model不为空，正式赋值了");
        }
        return transferModel;
    }

//    public synchronized long getSoFar(final int id) {
//        final DownloadModel model = mHelper.find(id);
//        if (model == null) {
//            return 0;
//        }
//        return model.getSoFar();
//    }

    /**
     * @return Already succeed & exists
     */
    public static boolean checkReuse(final int downloadId, final DownloadModel model) {
        boolean result = false;
        // 这个方法判断应该在checkDownloading之后，如果在下载中，那么这些判断都将产生错误。
        // 存在小概率事件，有可能，此方法判断过程中，刚好下载完成, 这里需要对同一DownloadId的Runnable与该方法同步
        do {
            if (model == null) {
                // 数据不存在
                FileDownloadLog.w(FileDownloadMgr.class, "can't reuse %d model not exist", downloadId);
                Log.i("jiulai", "checkReuse~model == null   数据不存在");
                break;
            }

            if (model.getStatus() != DownloadStatusDef.completed ) {
                // 数据状态没完成
                FileDownloadLog.w(FileDownloadMgr.class, "can't reuse %d status not completed %s",
                        downloadId, model.getStatus());
                Log.i("jiulai", "checkReuse~model.getStatus() != DownloadStatusDef.completed   数据状态没完成");
                break;
            }

            final File file = new File(model.getPath());
            if ((!file.exists() || !file.isFile()) && model.getSoFar() != 0) {
                if(!file.exists()) {
                    Log.i("jiulai", "checkReuse~!file.exists()");
                }

                if(!file.isFile()) {
                    Log.i("jiulai", "checkReuse~!file.isFile()");
                }
                // 文件不存在
                FileDownloadLog.w(FileDownloadMgr.class, "can't reuse %d file not exists", downloadId);
                break;
            }

            if (model.getSoFar() != model.getTotal()) {
                // 脏数据
                FileDownloadLog.w(FileDownloadMgr.class, "can't reuse %d soFar[%d] not equal total[%d] %d",
                        downloadId, model.getSoFar(), model.getTotal());
                Log.i("jiulai", "checkReuse~model.getSoFar() != model.getTotal()   脏数据");
                break;
            }

            if (file.length() != model.getTotal()) {
                // 无效文件
                FileDownloadLog.w(FileDownloadMgr.class, "can't reuse %d file length[%d] not equal total[%d]",
                        downloadId, file.length(), model.getTotal());
                Log.i("jiulai", "checkReuse~file.length() != model.getTotal()   无效文件");
                break;
            }

            result = true;
        } while (false);

        return result;
    }

    public synchronized boolean pause(final int id) {
        final DownloadModel model = mHelper.find(id);
        if (model == null) {
            return false;
        }
        if(model.getStatus() == DownloadStatusDef.pending) {
            FileDownloadRunnable runnable = mThreadPool.getRunnableByDownloadId(id);
            if(runnable != null) {
                runnable.onPause();
                mThreadPool.cancelDwonloadThread(id);
            }
        }

        FileDownloadLog.d(this, "paused %d", id);
        model.setIsCancel(true);

        /**
         * 耦合 by {@link FileDownloadRunnable#run()} 中的 {@link com.squareup.okhttp.Request.Builder#tag(Object)}
         * 目前在okHttp里还是每个单独任务
         */
        // 之所以注释掉，不想这里回调error，okHttp中会根据okHttp所在被cancel的情况抛error
//        client.cancel(id);
        return true;
    }

    /**
     * Pause all running task
     */
    public synchronized void pauseAll() {
        List<Integer> list = mThreadPool.getAllExactRunningDownloadIds();

        FileDownloadLog.d(this, "pause all tasks %d", list.size());

        for (Integer id : list) {
            pause(id);
        }
    }

    public synchronized long getSoFar(final int id) {
        final DownloadModel model = mHelper.find(id);
        if (model == null) {
            return 0;
        }
        Log.d("FileDownMgr","model不为空，单独sofar:==>"+model.getSoFar());
        return model.getSoFar();
    }

    public synchronized long getTotal(final int id) {
        final DownloadModel model = mHelper.find(id);
        if (model == null) {
            return 0;
        }
        return model.getTotal();
    }

    public synchronized int getStatus(final int id) {
        final DownloadModel model = mHelper.find(id);
        if (model == null) {
            return DownloadStatusDef.INVALID_STATUS;
        }

        return model.getStatus();
    }

    public synchronized boolean isIdle() {
        return mThreadPool.exactSize() <= 0;
    }

    public synchronized boolean remove(final int id) {
        final DownloadModel model = mHelper.find(id);
        if (model != null) {

            if(model.getStatus() == DownloadStatusDef.pending
                    || model.getStatus() == DownloadStatusDef.progress) {

                DownloadServiceEventBusManager.getEventBus().addListener(DownloadInfoEvent.ID,new IEventListener() {
                    @Override
                    public boolean onEvent(IEvent event) {
                        DownloadInfoEvent e = (DownloadInfoEvent)event;
                        if (e.getDownloadInfo().getDownloadId() != id) return false;

                        if (e.getDownloadInfo().getStatus() == DownloadStatusDef.paused) {
                            mHelper.remove(id);
                            DownloadServiceEventBusManager.getEventBus().removeListener(DownloadInfoEvent.ID,this);
                        }

                        return false;
                    }
                });

                this.pause(id);
            } else {

                mHelper.remove(id);

            }

            return true;
        }
        return false;
    }

    public synchronized void updateToPauseStatus(int downloadId) {
        mHelper.updatePause(downloadId);
    }
}

