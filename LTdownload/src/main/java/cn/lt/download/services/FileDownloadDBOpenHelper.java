package cn.lt.download.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.lt.download.model.DownloadModel;

class FileDownloadDBOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "filedownloader.db";
    private static final int DATABASE_VERSION = 1;

    public FileDownloadDBOpenHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                FileDownloadDBHelper.TABLE_NAME +
                String.format(
                        "(" +
                                "%s INTEGER PRIMARY KEY, " + // id
                                "%s VARCHAR, " + //url
                                "%s VARCHAR, " + // path
                                "%s INTEGER, " + // callbackProgressTimes // no need store, but SQLite not support remove a column
                                "%s TINYINT, " + // status ,ps SQLite will auto change to integer.
                                "%s INTEGER, " + // so far
                                "%s INTEGER, " + // total
                                "%s VARCHAR, " + // err msg
                                "%s VARCHAR," + // e tag
                                "%s VARCHAR" +
                                ")",
                        DownloadModel.ID,
                        DownloadModel.URL,
                        DownloadModel.PATH,
                        DownloadModel.CALLBACK_PROGRESS_TIMES,
                        DownloadModel.STATUS,
                        DownloadModel.SOFAR,
                        DownloadModel.TOTAL,
                        DownloadModel.ERR_MSG,
                        DownloadModel.ETAG,
                        DownloadModel.PACKAGE_NAME));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
