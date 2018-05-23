package cn.lt.download.i;

import cn.lt.download.model.DownloadInfo;

interface IFileDownloadIPCCallback {
    oneway void callback(in DownloadInfo downloadInfo);
}
