
package cn.lt.download.services;

import android.os.RemoteException;

import cn.lt.download.event.IEvent;
import cn.lt.download.event.IEventListener;
import cn.lt.download.event.DownloadInfoEvent;
import cn.lt.download.i.IFileDownloadIPCCallback;
import cn.lt.download.i.IFileDownloadIPCService;
import cn.lt.download.model.DownloadInfo;
import cn.lt.download.util.FileDownloadLog;
import cn.lt.download.util.FileDownloadUtils;


public class DownloadService extends BaseDownloadService<IFileDownloadIPCCallback, DownloadService.FileDownloadServiceBinder> {

    private IEventListener mListener;

    @Override
    public void onCreate() {
        super.onCreate();
        FileDownloadLog.i(this,"File Download Service is started !!!!");
        DownloadServiceEventBusManager.getEventBus().addListener(DownloadInfoEvent.ID, new IEventListener() {
            @Override
            public boolean onEvent(IEvent event) {
                FileDownloadLog.i(DownloadService.this,"service process event now and deliver to agent " + event);
                callback(0,event);
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected FileDownloadServiceBinder createBinder() {
        return new FileDownloadServiceBinder();
    }

    @Override
    protected boolean handleCallback(int cmd, IFileDownloadIPCCallback IFileDownloadIPCCallback, Object... objects) throws RemoteException {
        if (objects[0] instanceof DownloadInfoEvent) {
            FileDownloadLog.i(this, "callback to agent " + ((DownloadInfoEvent) objects[0]).getDownloadInfo().toString());
        }
        IFileDownloadIPCCallback.callback(((DownloadInfoEvent) objects[0]).getDownloadInfo());
        return false;
    }

    protected class FileDownloadServiceBinder extends IFileDownloadIPCService.Stub {
        private final FileDownloadMgr downloadManager;

        private FileDownloadServiceBinder() {
            downloadManager = new FileDownloadMgr();  //桥梁类对象调用此进程里面的其它类的方法
        }

        @Override
        public void registerCallback(IFileDownloadIPCCallback callback) throws RemoteException {
            register(callback);
        }

        @Override
        public void unregisterCallback(IFileDownloadIPCCallback callback) throws RemoteException {
            unregister(callback);
        }

        @Override
        public DownloadInfo checkReuse(String url, String path) throws RemoteException {
            return downloadManager.checkReuse(FileDownloadUtils.generateId(url, path));
        }

        @Override
        public DownloadInfo checkReuse2(int id) throws RemoteException {
            return downloadManager.checkReuse(id);
        }

        @Override
        public boolean checkDownloading(String url, String path) throws RemoteException {
            return downloadManager.checkDownloading(url, path);
        }

        @Override
        public void start(String packageName,String url, String path, int callbackProgressTimes, int autoRetryTimes, boolean isOrderWifiDownload) throws RemoteException {
            downloadManager.start(packageName,url, path, callbackProgressTimes, autoRetryTimes, isOrderWifiDownload);
        }

        @Override
        public boolean pause(int downloadId) throws RemoteException {
            return downloadManager.pause(downloadId);
        }

        @Override
        public void pauseAllTasks() throws RemoteException {
            downloadManager.pauseAll();
        }

        @Override
        public DownloadInfo getCommonDownloadInfo(int downloadId) throws RemoteException {
            return downloadManager.getCommonDownloadInfo(downloadId);
        }

        @Override
        public long getSofar(int downloadId) throws RemoteException {
            return downloadManager.getSoFar(downloadId);
        }

        @Override
        public long getTotal(int downloadId) throws RemoteException {
            return downloadManager.getTotal(downloadId);
        }

        @Override
        public int getStatus(int downloadId) throws RemoteException {
            return downloadManager.getStatus(downloadId);
        }

        @Override
        public boolean isIdle() throws RemoteException {
            return downloadManager.isIdle();
        }

        @Override
        public boolean remove(int downloadId) throws RemoteException{
            return downloadManager.remove(downloadId);
        }

        @Override
        public void updateToPauseStatus(int downloadId) throws RemoteException {
            downloadManager.updateToPauseStatus(downloadId);
        }
    }
}
