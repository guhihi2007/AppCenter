package cn.lt.android.event;

import cn.lt.android.db.AppEntity;

/**
 * Created by chon on 2017/2/19.
 * What? How? Why?
 */

public class NewDownloadTask {
    public AppEntity entity;

    public NewDownloadTask(AppEntity entity) {
        this.entity = entity;
    }
}
