package cn.lt.download.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.lt.download.DownloadStatusDef;
import cn.lt.download.model.DownloadModel;
import cn.lt.download.util.ContextHolder;
import cn.lt.download.util.FileDownloadLog;
import cn.lt.download.util.FileDownloadUtils;

class FileDownloadDBHelper implements IFileDownloadDBHelper {

    private final SQLiteDatabase db;

    public final static String TABLE_NAME = "filedownloader";

    private final Map<Integer, DownloadModel> downloaderModelMap = new HashMap<>();

    private static final ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    public FileDownloadDBHelper() {
        FileDownloadDBOpenHelper openHelper = new FileDownloadDBOpenHelper(ContextHolder.getAppContext());

        db = openHelper.getWritableDatabase();

        refreshDataFromDB();
    }


    @Override
    public synchronized Set<DownloadModel> getAllUnComplete() {
        return null;
    }

    @Override
    public synchronized Set<DownloadModel> getAllCompleted() {
        return null;
    }

    @Override
    public synchronized void refreshDataFromDB() {
        // TODO 优化，分段加载，数据多了以后
        // TODO 自动清理一个月前的数据
        long start = System.currentTimeMillis();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if(c == null) {
            Log.i("akonoas909089", "Cursor == null");
        } else {
            Log.i("akonoas909089", "Cursor_size = " + c.getCount());
        }

        List<Integer> dirtyList = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                DownloadModel model = new DownloadModel();
                model.setId(c.getInt(c.getColumnIndex(DownloadModel.ID)));
                model.setUrl(c.getString(c.getColumnIndex(DownloadModel.URL)));
                model.setPath(c.getString(c.getColumnIndex(DownloadModel.PATH)));
                model.setCallbackProgressTimes(c.getInt(c.getColumnIndex(DownloadModel.CALLBACK_PROGRESS_TIMES)));
                model.setStatus((byte) c.getShort(c.getColumnIndex(DownloadModel.STATUS)));
                model.setSoFar(c.getInt(c.getColumnIndex(DownloadModel.SOFAR)));
                model.setTotal(c.getInt(c.getColumnIndex(DownloadModel.TOTAL)));
                model.setErrMsg(c.getString(c.getColumnIndex(DownloadModel.ERR_MSG)));
                model.setETag(c.getString(c.getColumnIndex(DownloadModel.ETAG)));
                model.setPackageName(c.getString(c.getColumnIndex(DownloadModel.PACKAGE_NAME)));
                Log.i("jiulai", "id = " + model.getId() +
                                ", packageName = " + model.getPackageName() +
                                ", status = " + model.getStatus() +
                                ", sofar = " + model.getSoFar() +
                                ", total = " + model.getTotal()
                );
                if (model.getStatus() == DownloadStatusDef.progress
                        || model.getStatus() == DownloadStatusDef.connected
                        || model.getStatus() == DownloadStatusDef.pending) {

                    if(model.getStatus() == DownloadStatusDef.progress) {
                        File file = new File(model.getPath());
                        if(file.exists() && !file.isDirectory()) {
                            long fileLength = file.length();
                            if(fileLength < model.getSoFar()) {
                                model.setSoFar(fileLength);
                            }
                        }
                    }

                    // 保证断点续传可以覆盖到
                    model.setStatus(DownloadStatusDef.paused);
                }

                int newId = FileDownloadUtils.generateId(model.getPackageName(), model.getPath());
                Log.i("akonoas909089", "yuanId = " + model.getId() + ", newId = " + newId +"status:"+ model.getStatus());
                if(model.getId() != newId) {
                    Log.i("akonoas909089", model.getPackageName() + "的downId（"+model.getId() +"）与新的id("+newId+")不一样，现在替换");
                    model.setId(newId);
                    update(model);
                }

                // consider check in new thread, but SQLite lock | file lock aways effect, so sync
                if (!FileDownloadMgr.checkReuse(model.getId(), model)
                        && !FileDownloadMgr.checkBreakpointAvailable(model.getId(), model)) {
                    // can't use to reuse old file & can't use to resume form break point
                    // = dirty
                    dirtyList.add(model.getId());
                    Log.i("jiulai", "addDirtyList");
                }
                downloaderModelMap.put(model.getId(), model);
            }
        } finally {
            c.close();

            for (Integer integer : dirtyList) {
                downloaderModelMap.remove(integer);
                Log.i("jiulai", "remove:" + integer);
            }

            // db
            if (dirtyList.size() > 0) {
                String args = TextUtils.join(", ", dirtyList);
                FileDownloadLog.d(this, "delete %s", args);
                db.execSQL(String.format("DELETE FROM %s WHERE %s IN (%s);",
                        TABLE_NAME, DownloadModel.ID, args));
                Log.i("jiulai", "deleteToDB=" + args +"\n\n");
            }

            // 566 data consumes about 140ms
            FileDownloadLog.d(this, "refresh data %d , will delete: %d consume %d",
                    downloaderModelMap.size(), dirtyList.size(), System.currentTimeMillis() - start);
        }

    }

    @Override
    public synchronized DownloadModel find(final int id) {
        return downloaderModelMap.get(id);
    }

    @Override
    public synchronized void insert(DownloadModel downloadModel) {
        downloaderModelMap.put(downloadModel.getId(), downloadModel);

        // db
        db.insert(TABLE_NAME, null, downloadModel.toContentValues());
    }

    @Override
    public synchronized void update(DownloadModel downloadModel) {
        if (downloadModel == null) {
            FileDownloadLog.e(this, "update but model == null!");
            return;
        }

        if (find(downloadModel.getId()) != null) {
            // 替换
            downloaderModelMap.remove(downloadModel.getId());
            downloaderModelMap.put(downloadModel.getId(), downloadModel);

            // db
            ContentValues cv = downloadModel.toContentValues();
            db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(downloadModel.getId())});
        } else {
            insert(downloadModel);
        }
    }

    @Override
    public synchronized void remove(int id) {
        downloaderModelMap.remove(id);

        // db
        db.delete(TABLE_NAME, DownloadModel.ID + " = ?", new String[]{String.valueOf(id)});
    }

    private long lastRefreshUpdate = 0;

    @Override
    public synchronized void update(final int id, byte status, long soFar, long total) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setStatus(status);
            downloadModel.setSoFar(soFar);
            downloadModel.setTotal(total);

            boolean needRefresh2DB = false;
            final int MIN_REFRESH_DURATION_2_DB = 10;
            if (System.currentTimeMillis() - lastRefreshUpdate > MIN_REFRESH_DURATION_2_DB) {
                needRefresh2DB = true;
                lastRefreshUpdate = System.currentTimeMillis();
            }

            if (!needRefresh2DB) {
                return;
            }

            // db
            final ContentValues cv = new ContentValues();
            cv.put(DownloadModel.STATUS, status);
            cv.put(DownloadModel.SOFAR, soFar);
            cv.put(DownloadModel.TOTAL, total);

            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});

                }
            });
        }

    }

    @Override
    public synchronized void updateHeader(int id, String etag) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setETag(etag);

            //db
            ContentValues cv = new ContentValues();
            cv.put(DownloadModel.ETAG, etag);
            db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public synchronized void updateError(int id, String errMsg) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setStatus(DownloadStatusDef.error);
            downloadModel.setErrMsg(errMsg);

            // db
            ContentValues cv = new ContentValues();
            cv.put(DownloadModel.ERR_MSG, errMsg);
            cv.put(DownloadModel.STATUS, DownloadStatusDef.error);
            db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public synchronized void updateRetry(int id, String errMsg, int retryingTimes) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setStatus(DownloadStatusDef.retry);
            downloadModel.setErrMsg(errMsg);

            // db
            ContentValues cv = new ContentValues();
            cv.put(DownloadModel.ERR_MSG, errMsg);
            cv.put(DownloadModel.STATUS, DownloadStatusDef.retry);
            db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public synchronized void updateComplete(int id, final long total) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setStatus(DownloadStatusDef.completed);
            downloadModel.setSoFar(total);
            downloadModel.setTotal(total);
        }

        //db
        ContentValues cv = new ContentValues();
        cv.put(DownloadModel.STATUS, DownloadStatusDef.completed);
        cv.put(DownloadModel.TOTAL, total);
        cv.put(DownloadModel.SOFAR, total);
        db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});
    }

    @Override
    public synchronized void updatePause(int id) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setStatus(DownloadStatusDef.paused);

            // db
            ContentValues cv = new ContentValues();
            cv.put(DownloadModel.STATUS, DownloadStatusDef.paused);
            db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});
        }
    }

    @Override
    public synchronized void updatePending(int id) {
        final DownloadModel downloadModel = find(id);
        if (downloadModel != null) {
            downloadModel.setStatus(DownloadStatusDef.pending);

            // db
            ContentValues cv = new ContentValues();
            cv.put(DownloadModel.STATUS, DownloadStatusDef.pending);
            db.update(TABLE_NAME, cv, DownloadModel.ID + " = ? ", new String[]{String.valueOf(id)});
        }
    }
}
