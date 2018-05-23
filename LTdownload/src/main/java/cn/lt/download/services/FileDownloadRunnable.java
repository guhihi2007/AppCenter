package cn.lt.download.services;

import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import cn.lt.download.DownloadStatusDef;
import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.model.DownloadInfo;
import cn.lt.download.model.DownloadModel;
import cn.lt.download.util.FileDownloadLog;
import cn.lt.download.util.FileDownloadUtils;

/**
 * 真正执行下载的地方
 */
class FileDownloadRunnable implements Runnable {

    private static final int BUFFER_SIZE = 1024 * 4;
    private final DownloadInfo downloadTransfer;

    private final String url;
    private final String path;

    private final IFileDownloadDBHelper helper;

    private long maxNotifyBytes;


    private int maxNotifyCounts = 0;

    //tmp
    private boolean isContinueDownloadAvailable;

    // etag
    private String etag;

    private DownloadModel downloadModel;
    private long preProcessTime;
    private boolean isFirst = true;

    public int getId() {
        return downloadModel.getId();
    }

    private volatile boolean isRunning = false;
    private volatile boolean isPending = false;

    private final OkHttpClient client;
    private final int autoRetryTimes;

    public FileDownloadRunnable(final OkHttpClient client, final DownloadModel model, final IFileDownloadDBHelper helper, final int autoRetryTimes) {
        isPending = true;
        isRunning = false;

        this.client = client;
        this.helper = helper;

        this.url = model.getUrl();
        this.path = model.getPath();

        downloadTransfer = new DownloadInfo();

        downloadTransfer.setDownloadId(model.getId());
        downloadTransfer.setPackageName(model.getPackageName());
        downloadTransfer.setStatus(model.getStatus());
        downloadTransfer.setSoFarBytes(model.getSoFar());
        downloadTransfer.setTotalBytes(model.getTotal());

        maxNotifyCounts = model.getCallbackProgressTimes();
        maxNotifyCounts = maxNotifyCounts <= 0 ? 0 : maxNotifyCounts;

        this.isContinueDownloadAvailable = false;

        this.etag = model.getETag();
        this.downloadModel = model;

        this.autoRetryTimes = autoRetryTimes;
    }

    public boolean isExist() {
        return isPending || isRunning;
    }


    private Response requestApkData(String apkUrl) throws IOException {
        FileDownloadLog.d(FileDownloadRunnable.class, "start download %s %s", getId(), apkUrl);
        checkIsContinueAvailable();
        Request.Builder headerBuilder = new Request.Builder().url(apkUrl);
        addHeader(headerBuilder);
        headerBuilder.tag(this.getId());
        headerBuilder.cacheControl(CacheControl.FORCE_NETWORK);
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(3, TimeUnit.SECONDS);
        Call call = client.newCall(headerBuilder.get().build());
        client.setFollowRedirects(false);
        Response response = call.execute();
        String realUrl = response.header("location");
        FileDownloadLog.i(FileDownloadRunnable.this, "response code " + response.code() + " realLocation" + realUrl);
        return response;
    }

    @Override
    public void run() {
        isPending = false;
        isRunning = true;
        int retryingTimes = 0;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        DownloadModel model = this.downloadModel;
        if (model == null) {
            FileDownloadLog.e(this, "start runnable but model == null?? %s", getId());

            this.downloadModel = helper.find(getId());

            if (this.downloadModel == null) {
                FileDownloadLog.e(this, "start runnable but downloadMode == null?? %s", getId());
                return;
            }

            model = this.downloadModel;
        }

        if (model.getStatus() != DownloadStatusDef.pending) {
            if (model.getStatus() == DownloadStatusDef.paused) {
                return;
            }
            FileDownloadLog.e(this, "start runnable but status err %s", model.getStatus());
            // 极低概率事件，相同url与path的任务被放到了线程池中(目前在入池之前是有检测的，但是还是存在极低概率的同步问题) 执行的时候有可能会遇到
            onError(new RuntimeException(String.format("start runnable but status err %s", model.getStatus())));

            return;
        }

        if (TextUtils.isEmpty(url)) {
            onError(new RuntimeException("url_empty"));
            return;
        }

        // 进入下载
        do {

            long soFar = 0;
            try {
                if (isCancelled()) {
                    FileDownloadLog.d(this, "already canceled %d %d", model.getId(), model.getStatus());
                    onPause();
                    break;
                }

                Response response = null;

                String apkUrl = url;

                int countOfRedirect = 0;

                while (response == null && countOfRedirect < 3) {
                    response = requestApkData(apkUrl);
                    if (response.code() == 302) {
                        countOfRedirect++;
                        apkUrl = response.header("location");
                        response = null;
                    }
                }

                final boolean isSucceedStart = response.code() == 200;
                final boolean isSucceedContinue = response.code() == 206 && isContinueDownloadAvailable;


                if (isSucceedStart || isSucceedContinue) {
                    long total = downloadTransfer.getTotalBytes();
                    final String transferEncoding = response.header("Transfer-Encoding");


                    if (isSucceedStart || total <= 0) {
                        if (transferEncoding == null) {
                            total = response.body().contentLength();
                        } else {
                            // if transfer not nil, ignore content-length
                            total = -1;
                        }
                    }

                    // TODO consider if not is chunked & http 1.0/(>=http1.1 & connect not be keep live) may not give content-length

                    if (total < 0) {
                        // invalid total length
                        final boolean isEncodingChunked = transferEncoding != null && transferEncoding.equals("chunked");
                        if (!isEncodingChunked) {
                            // not chunked transfer encoding data
                            throw new GiveUpRetryException("can't know size, and not chunk transfer encoding");
                        }
                    }

                    if (isSucceedContinue) {
                        soFar = downloadTransfer.getSoFarBytes();
                        FileDownloadLog.d(this, "add range %d %d", downloadTransfer.getSoFarBytes(), downloadTransfer.getTotalBytes());
                    }

                    InputStream inputStream = null;
                    RandomAccessFile accessFile = getRandomAccessFile(isSucceedContinue);
                    try {
                        inputStream = response.body().byteStream();
                        byte[] buff = new byte[BUFFER_SIZE];
                        maxNotifyBytes = maxNotifyCounts <= 0 ? -1 : total / maxNotifyCounts;

                        updateHeader(response);
                        onConnected(isSucceedContinue, soFar, total);
                        long downloadedLength = 0;
                        do {
                            int byteCount = inputStream.read(buff);
                            if (byteCount == -1) {
                                break;
                            }

                            accessFile.write(buff, 0, byteCount);

                            //write buff
                            soFar += byteCount;
                            if (accessFile.length() < soFar) {
                                // 文件大小必须会等于正在写入的大小
                                throw new RuntimeException(String.format("file be changed by others when downloading %d %d", accessFile.length(), soFar));
                            } else {

                                // 每当下载了5M时判断下文件是否存在
                                downloadedLength += soFar;
                                if (downloadedLength > 5242880) {
                                    if (!new File(downloadModel.getPath()).exists()) {
                                        onError(new RuntimeException("Dir or File not exist"));

                                        if (inputStream != null) {
                                            inputStream.close();
                                        }

                                        if (accessFile != null) {
                                            accessFile.close();
                                        }

                                        return;
                                    }
                                    downloadedLength = 0;
                                }

                                if (soFar == byteCount && isFirst) {
                                    reportDownloadRequest();
                                    isFirst = false;
                                }

                                onProcess(soFar, total);

                            }

                            if (isCancelled()) {
                                onPause();
                                return;
                            }

                        } while (true);

                        // total等于205时（由于soFar是205），代表没网
                        if (total == -1) {
                            total = soFar;
                        }
                        if (soFar == total && total != 205) {
                            if (!new File(downloadModel.getPath()).exists()) {
                                onError(new RuntimeException("Dir or File not exist"));
                            } else {
                                onComplete(total);
                            }
                            // 成功
                            break;
                        } else {
                            throw new RuntimeException(String.format("sofar[%d] not equal total[%d]", soFar, total));
                        }
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (accessFile != null) {
                            accessFile.close();
                        }
                    }


                } else {
//                    throw new RuntimeException(String.format("response code error: %d", response.code()));
                    onError(new RuntimeException(String.format("response code error: %d", response.code())));
                    break;
                }


            } catch (Throwable ex) {
//                if ((new File(model.getPath())).exists()) {
                    // TODO 决策是否需要重试，是否是用户决定，或者根据错误码处理
                    if (autoRetryTimes > retryingTimes++ && !(ex instanceof GiveUpRetryException)) {
                        // retry
                        onRetry(ex, retryingTimes, soFar);
                    } else {
                        // error
                        onError(ex);
                        break;
                    }
//                } else {
//                    onError(new Throwable("akp_delete"));
//                    break;
//                }
            } finally {
                isRunning = false;
            }

        } while (true);


    }

    private void addHeader(Request.Builder builder) {
        FileDownloadLog.i(this, "Add header :" + isContinueDownloadAvailable);
        if (isContinueDownloadAvailable) {
            if (this.etag != null) {
                builder.addHeader("If-Match", this.etag);
            }
            builder.addHeader("Range", String.format("bytes=%d-", downloadTransfer.getSoFarBytes()));
        }
    }

    private void updateHeader(Response response) {
        if (response == null) {
            throw new RuntimeException("response is null when updateHeader");
        }

        boolean needRefresh = false;
        final String oldEtag = this.etag;
        final String newEtag = response.header("Etag");

        FileDownloadLog.w(this, "etag find by header %d %s", getId(), newEtag);

        if (oldEtag == null && newEtag != null) {
            needRefresh = true;
        } else if (oldEtag != null && newEtag != null && !oldEtag.equals(newEtag)) {
            needRefresh = true;
        }

        if (needRefresh) {
            this.etag = newEtag;
            helper.updateHeader(downloadTransfer.getDownloadId(), newEtag);
        }

    }

    private void onConnected(final boolean isContinue, final long soFar, final long total) {

        downloadTransfer.setSoFarBytes(soFar);
        downloadTransfer.setTotalBytes(total);
        downloadTransfer.setEtag(this.etag);
        downloadTransfer.setIsContinue(isContinue);
        downloadTransfer.setStatus(DownloadStatusDef.connected);

        helper.update(downloadTransfer.getDownloadId(), DownloadStatusDef.connected, soFar, total);
        FileDownloadLog.d(this, "On Connected %d %d %d", downloadTransfer.getDownloadId(), downloadTransfer.getSoFarBytes(), downloadTransfer.getTotalBytes());

        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);
    }

    private long lastNotifiedSoFar = 0;

    private void onProcess(final long soFar, final long total) {
        downloadTransfer.setStatus(DownloadStatusDef.progress);

        if (soFar != total) {
            downloadTransfer.setSoFarBytes(soFar);
            downloadTransfer.setTotalBytes(total);
            helper.update(downloadTransfer.getDownloadId(), DownloadStatusDef.progress, soFar, total);
        }

        if ((System.currentTimeMillis() - preProcessTime) < 1000) {
            return;
        }
        if (maxNotifyBytes < 0 || soFar - lastNotifiedSoFar < maxNotifyBytes) {
            return;
        }

        lastNotifiedSoFar = soFar;
        FileDownloadLog.d(this, "On progress %d %d %d", downloadTransfer.getDownloadId(), soFar, total);

        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);
        preProcessTime = System.currentTimeMillis();
    }

    /***
     * 分发下载触发事件
     */
    private void reportDownloadRequest() {
        downloadTransfer.setStatus(DownloadStatusDef.trigger);
        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);

    }

    private void onRetry(Throwable ex, final int retryTimes, final long soFarBytes) {
        FileDownloadLog.e(this, ex, "On retry %d %s %d %d", downloadTransfer.getDownloadId(), ex.getMessage(), retryTimes, autoRetryTimes);

        ex = exFiltrate(ex);
        downloadTransfer.setStatus(DownloadStatusDef.retry);
        downloadTransfer.setThrowable(ex);
        downloadTransfer.setRetryingTimes(retryTimes);
        downloadTransfer.setSoFarBytes(soFarBytes);
        // TODO 目前是做断点续传，实际还需要看情况而定

        helper.updateRetry(downloadTransfer.getDownloadId(), ex.getMessage(), retryTimes);
        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);// because we must make sure retry status no change by downloadTransfer reference
    }

    private void onError(Throwable ex) {
        FileDownloadLog.e(this, ex, "On error %d %s", downloadTransfer.getDownloadId(), ex.getMessage());

        ex = exFiltrate(ex);
        downloadTransfer.setStatus(DownloadStatusDef.error);
        downloadTransfer.setThrowable(ex);

        final DownloadModel downloadModel = helper.find(downloadTransfer.getDownloadId());
        if (downloadModel != null) {
            downloadTransfer.setSoFarBytes(downloadModel.getSoFar());
            downloadTransfer.setTotalBytes(downloadModel.getTotal());
        }

        helper.updateError(downloadTransfer.getDownloadId(), ex.getMessage());
        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);
    }

    private void onComplete(final long total) {
        FileDownloadLog.d(this, "On completed %d %d", downloadTransfer.getDownloadId(), total);
        downloadTransfer.setStatus(DownloadStatusDef.completed);

        helper.updateComplete(downloadTransfer.getDownloadId(), total);
        downloadTransfer.setUseOldFile(false);
        downloadTransfer.setSoFarBytes(total);
        downloadTransfer.setTotalBytes(total);
        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);
    }

    public void onPause() {
        this.isRunning = false;
        FileDownloadLog.d(this, "On paused %d %d %d", downloadTransfer.getDownloadId(), downloadTransfer.getSoFarBytes(), downloadTransfer.getTotalBytes());
        downloadTransfer.setStatus(DownloadStatusDef.paused);

        helper.updatePause(downloadTransfer.getDownloadId());
        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);
    }

    public void onResume() {
        FileDownloadLog.d(this, "On resume %d", downloadTransfer.getDownloadId());
        downloadTransfer.setStatus(DownloadStatusDef.pending);

        this.isPending = true;

        helper.updatePending(downloadTransfer.getDownloadId());
        DownloadInfoEvent event = new DownloadInfoEvent(downloadTransfer);
        DownloadServiceEventBusManager.getEventBus().publishByService(event);
    }

    private boolean isCancelled() {
        return this.downloadModel.isCanceled();
    }

    // ----------------------------------
    private RandomAccessFile getRandomAccessFile(final boolean append) throws Throwable {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("found invalid internal destination path, empty");
        }

        if (!FileDownloadUtils.isFilenameValid(path)) {
            throw new RuntimeException(String.format("found invalid internal destination filename %s", path));
        }

        File file = new File(path);

        if (file.exists() && file.isDirectory()) {
            throw new RuntimeException(String.format("found invalid internal destination path[%s], & path is directory[%B]", path, file.isDirectory()));
        }
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException(String.format("create new file error  %s", file.getAbsolutePath()));
            }
        }

        RandomAccessFile outFd = new RandomAccessFile(file, "rw");
        if (append) {
            outFd.seek(downloadTransfer.getSoFarBytes());
        }
        return outFd;
//        return new FileOutputStream(file, append);
    }

    private void checkIsContinueAvailable() {
        if (FileDownloadMgr.checkBreakpointAvailable(getId(), this.downloadModel)) {
            this.isContinueDownloadAvailable = true;
        } else {
            this.isContinueDownloadAvailable = false;
            // delete dirty file
            File file = new File(path);
            if (file.exists()) {
                Log.i("FileDownloadRunnable", "file exist.");
            }
            file.delete();
        }
    }

    private Throwable exFiltrate(Throwable ex) {
        if (TextUtils.isEmpty(ex.getMessage())) {
            if (ex instanceof SocketTimeoutException) {
                ex = new RuntimeException(ex.getClass().getSimpleName(), ex);
            }
        }

        return ex;
    }

    public static class GiveUpRetryException extends RuntimeException {
        public GiveUpRetryException(final String detailMessage) {
            super(detailMessage);
        }
    }
}
