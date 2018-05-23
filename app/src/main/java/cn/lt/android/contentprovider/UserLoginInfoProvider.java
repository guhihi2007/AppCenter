package cn.lt.android.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import cn.lt.android.db.DaoMaster;
import cn.lt.android.db.LoginHistoryEntityDao;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;

/**
 * Created by Administrator on 2016/9/1.
 */
public class UserLoginInfoProvider extends ContentProvider {
    SQLiteDatabase db;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(UserBaseInfo.AUTHORITY, "item", UserBaseInfo.ITEM);
        uriMatcher.addURI(UserBaseInfo.AUTHORITY, "item/#", UserBaseInfo.ITEM_ID);
        uriMatcher.addURI(UserBaseInfo.AUTHORITY, "pos/#", UserBaseInfo.ITEM_POS);
    }

    @Override
    public boolean onCreate() {
        initDataBase();
        return true;
    }

    private synchronized void initDataBase() {
        DaoMaster.DevOpenHelper  helper    = new DaoMaster.DevOpenHelper(this.getContext(), "lt_appstore_db", null);
        db = helper.getWritableDatabase();
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c;
        switch (uriMatcher.match(uri)) {
            case UserBaseInfo.ITEM:
                c = db.query(LoginHistoryEntityDao.TABLENAME, projection, selection, selectionArgs, null, null, null);

                break;
            case UserBaseInfo.ITEM_ID:
                String id = uri.getPathSegments().get(1);
                c = db.query(LoginHistoryEntityDao.TABLENAME, projection, LoginHistoryEntityDao.Properties.Id+"="+id+(!TextUtils.isEmpty(selection)?"AND("+selection+')':""),selectionArgs, null, null, sortOrder);
                break;
            case UserBaseInfo.ITEM_POS:
                String pos = uri.getPathSegments().get(1);
                c = db.query(LoginHistoryEntityDao.TABLENAME,projection,LoginHistoryEntityDao.Properties.Id+"="+pos+(!TextUtils.isEmpty(selection)?"AND("+selection+')':""),selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case UserBaseInfo.ITEM:
                return UserBaseInfo.CONTENT_TYPE;
            case UserBaseInfo.ITEM_ID:
            case UserBaseInfo.ITEM_POS:
                return UserBaseInfo.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Error Uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != UserBaseInfo.ITEM) {
            throw new IllegalArgumentException("Error Uri: " + uri);
        }
        long id = db.insert(LoginHistoryEntityDao.TABLENAME, String.valueOf(LoginHistoryEntityDao.Properties.Id),values);
        if (id < 0) {
            throw new SQLiteException("Unable to insert " + values + " for " + uri);
        }

        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
