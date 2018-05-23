package cn.lt.download.services;


import java.util.Set;

import cn.lt.download.model.DownloadModel;

/**
 * Created by Jacksgong on 9/24/15.
 */
interface IFileDownloadDBHelper {

    Set<DownloadModel> getAllUnComplete();

    Set<DownloadModel> getAllCompleted();

    void refreshDataFromDB();

    /**
     * @param id download id
     */
    DownloadModel find(final int id);


    void insert(final DownloadModel downloadModel);

    void update(final DownloadModel downloadModel);

    void remove(final int id);

    void update(final int id, final byte status, final long soFar, final long total);

    void updateHeader(final int id, final String etag);

    void updateError(final int id, final String errMsg);

    void updateRetry(final int id, final String errMsg, final int retryingTimes);

    void updateComplete(final int id, final long total);

    void updatePause(final int id);

    void updatePending(final int id);

}
