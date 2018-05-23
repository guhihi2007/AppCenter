package cn.lt.download;


import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.i.IFileDownloadIPCCallback;
import cn.lt.download.i.IFileDownloadIPCService;
import cn.lt.download.model.DownloadInfo;
import cn.lt.download.services.DownloadService;
import cn.lt.download.util.ContextHolder;
import cn.lt.download.util.FileDownloadLog;

/**
 */
public class DownloadAgent extends BaseServiceAgent<DownloadAgent.FileDownloadServiceCallback, IFileDownloadIPCService> {

    private final static class HolderClass {
        private final static DownloadAgent INSTANCE = new DownloadAgent();
    }

    public static DownloadAgent getImpl() {
        return HolderClass.INSTANCE;
    }

    protected DownloadAgent() {
        super(DownloadService.class);
    }

    public void bindService(Context context) {
        ContextHolder.initAppContext(context);
        super.bindStartByContext(ContextHolder.getAppContext());
    }

    @Override
    protected FileDownloadServiceCallback createCallback() {
        return new FileDownloadServiceCallback();
    }

    //通过代理类拿到 接口实现类对象
    @Override
    protected IFileDownloadIPCService asInterface(IBinder service) {
        return IFileDownloadIPCService.Stub.asInterface(service);
    }

    @Override
    protected void registerCallback(IFileDownloadIPCService service, FileDownloadServiceCallback fileDownloadServiceCallback) throws RemoteException {
        service.registerCallback(fileDownloadServiceCallback);
    }

    @Override
    protected void unregisterCallback(IFileDownloadIPCService service, FileDownloadServiceCallback fileDownloadServiceCallback) throws RemoteException {
        service.unregisterCallback(fileDownloadServiceCallback);
    }

    public static class FileDownloadServiceCallback extends IFileDownloadIPCCallback.Stub {

        @Override
        public synchronized void callback(DownloadInfo transfer) throws RemoteException {
            FileDownloadLog.e(this, "=====> agent receive a event! " + transfer);

            DownloadInfoEvent event = new DownloadInfoEvent(transfer);

            if (event.getDownloadInfo() != null && event.getDownloadInfo().getStatus() == DownloadStatusDef.completed) {
                DownloadEventBusManager.getEventBus().publishByAgent(event);
            } else {
                DownloadEventBusManager.getEventBus().publishByAgent(event);
            }
        }
    }

    /**
     * @param url                   for download
     * @param path                  for save download file
     * @param callbackProgressTimes for onEvent progress times
     * @param autoRetryTimes        for auto retry times when error
     */
    public void startDownloader(final String packageName, final String url, final String path, final int callbackProgressTimes, final int autoRetryTimes, boolean isOrderWifiDownload) throws RemoteException {
        checkService();
        getService().start(packageName, url, path, callbackProgressTimes, autoRetryTimes, isOrderWifiDownload);
    }

    public void pauseDownloader(final int downloadId) throws RemoteException {
        checkService();
        getService().pause(downloadId);
    }

    public DownloadInfo checkReuse(final String url, final String path) throws RemoteException {
        checkService();
        return getService().checkReuse(url, path);
    }

    public DownloadInfo checkReuse(final int id) throws RemoteException {
        checkService();
        return getService().checkReuse2(id);

    }

    public DownloadInfo getDownloadInfo(final int id) throws RemoteException {
        checkService();
        return getService().checkReuse2(id);

    }

    public boolean checkIsDownloading(final String url, final String path) throws RemoteException {
        checkService();
        return getService().checkDownloading(url, path);
    }

    public long getSofar(final int downloadId) throws RemoteException {
        checkService();
        return getService().getSofar(downloadId);   //getService拿到可以调用服务方法的对象
    }

    public long getTotal(final int downloadId) throws RemoteException {

        checkService();
        return getService().getTotal(downloadId);
    }

    public void remove(final int downloadId) throws RemoteException {
        checkService();
        getService().remove(downloadId);
    }

    public int getStatus(final int downloadId) throws RemoteException {
        checkService();
        return getService().getStatus(downloadId);
    }

    public void pauseAllTasks() throws RemoteException {
        checkService();
        getService().pauseAllTasks();
    }

    /**
     * 代理类中获取远程服务的 DownloadInfo
     *
     * @param downloadId
     * @return
     * @throws RemoteException
     */
    public DownloadInfo getCommonDownloadInfo(int downloadId) throws RemoteException {
        checkService();
        return getService().getCommonDownloadInfo(downloadId);
    }

    /**
     * @return any error, will return true
     */
    public boolean isIdle() throws RemoteException {
        checkService();
        return getService().isIdle();
    }

    public void updatePauseStatus(int id) throws RemoteException {
        checkService();
        getService().updateToPauseStatus(id);
    }

    protected void checkService() throws RemoteException {
        if (getService() == null || !isConnected()) {
            //抛出异常后重新绑定服务前要先解绑用来创建一个新的远程Binder，解决远程下载服务异常后下载按钮状态异常的问题// modify by ATian at 2017/7/6
            unbindService(ContextHolder.getAppContext());
            bindService(ContextHolder.getAppContext());
            throw new RemoteException("service is gone!");
        }
    }

    private void unbindService(Context appContext) {
        unbindByContext(appContext);
    }

    public void testExcption() {
        testServiceException();
    }
}
