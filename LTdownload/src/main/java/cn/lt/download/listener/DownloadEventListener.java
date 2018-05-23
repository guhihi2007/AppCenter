package cn.lt.download.listener;


import cn.lt.download.DownloadStatusDef;
import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.event.IEvent;
import cn.lt.download.event.IEventListener;
import cn.lt.download.model.DownloadInfo;
import cn.lt.download.util.FileDownloadLog;


public abstract class DownloadEventListener extends IEventListener {

    public DownloadEventListener() {

    }

    @Override
    public boolean onEvent(IEvent event) {
        if (!(event instanceof DownloadInfoEvent)) {
            return false;
        }


        final DownloadInfoEvent downloadEvent = ((DownloadInfoEvent) event);
        final DownloadInfo info = downloadEvent.getDownloadInfo();

        FileDownloadLog.i(this, " DownloadInfoEvent =>" + info.getPackageName() + ":" + info.getStatus() + ":" + info.getSoFarBytes() + ":" + info.getTotalBytes());

        int status = downloadEvent.getDownloadInfo().getStatus();

        switch (status) {
            case DownloadStatusDef.trigger:
                trigger(downloadEvent);
                break;
            case DownloadStatusDef.pending:
                pending(downloadEvent);
                break;
            case DownloadStatusDef.connected:
                connected(downloadEvent);
                break;
            case DownloadStatusDef.progress:
                progress(downloadEvent);
                break;

            case DownloadStatusDef.blockComplete:
                blockComplete(downloadEvent);
                break;
            case DownloadStatusDef.retry:
                retry(downloadEvent);
                break;

            case DownloadStatusDef.completed:
                completed(downloadEvent);
                break;
            case DownloadStatusDef.error:
                error(downloadEvent, downloadEvent.getDownloadInfo().getThrowable());
                break;
            case DownloadStatusDef.paused:
                paused(downloadEvent);
                break;
            case DownloadStatusDef.warn:
                // already same url & path in pending/running list
                warn(downloadEvent);
                break;
        }

        return false;
    }


    protected abstract void pending(DownloadInfoEvent event);


    protected void connected(DownloadInfoEvent event) {

    }


    protected abstract void progress(DownloadInfoEvent event);

    /**
     * Block completed in new thread
     *
     * @param task Current task
     */
    protected abstract void blockComplete(DownloadInfoEvent event);

    /**
     * Start Retry
     *
     * @param task          Current task
     * @param ex            why retry
     * @param retryingTimes How many times will retry
     * @param soFarBytes    Number of bytes download so far
     */
    protected void retry(DownloadInfoEvent event) {

    }

    // final width below methods

    /**
     * Succeed download
     *
     * @param task Current task
     */
    protected abstract void completed(DownloadInfoEvent event);

    /**
     * Download paused
     *
     * @param task       Current task
     * @param soFarBytes Number of bytes download so far
     * @param totalBytes Total size of the download in bytes
     */
    protected abstract void paused(DownloadInfoEvent event);

    /**
     * Download error
     *
     * @param task Current task
     * @param e    Any throwable on download pipeline
     */
    protected abstract void error(DownloadInfoEvent event, final Throwable e);

    /**
     * There is already an identical task being downloaded
     *
     * @param task Current task
     */
    protected abstract void warn(DownloadInfoEvent event);

    protected abstract void trigger(DownloadInfoEvent event);
}
