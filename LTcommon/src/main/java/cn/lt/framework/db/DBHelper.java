package cn.lt.framework.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


/**
 * Created by wenchao on 2016/1/6.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "LT_APPSTORE";

    private SQLiteDatabase db;

    private static DBHelper mdbHelper;

    private static Class<DatabaseTable>[] mColumnsClasses;

    private static int mDBVersion;

    public static DBHelper getInstance(Context context)
    {
        if(mdbHelper==null)
        {
            mdbHelper=new DBHelper(context);
        }
        return mdbHelper;
    }

    public static DBHelper init(int dbVersion,Class<DatabaseTable>[] columnsClasses){
        mColumnsClasses = columnsClasses;
        mDBVersion = dbVersion;
        return mdbHelper;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null,mDBVersion);
    }

    private DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                     int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        operateTable(db, "");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        if (oldVersion == newVersion) {
            return;
        }
//        operateTable(db, "DROP TABLE IF EXISTS ");
//        onCreate(db);
        onUpgrade(db,oldVersion,newVersion);
    }



    public void operateTable(SQLiteDatabase db, String actionString) {
        DatabaseTable          columns = null;

        for (int i = 0; i < mColumnsClasses.length; i++) {
            try {
                columns = mColumnsClasses[i].newInstance();
                if ("".equals(actionString) || actionString == null) {
                    db.execSQL(columns.getTableCreateor());
                } else {
                    db.execSQL(actionString + columns.getTableName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void upgradeTable(SQLiteDatabase db, int oldVersion, int newVersion){
        DatabaseTable          columns = null;

        for (int i = 0; i < mColumnsClasses.length; i++) {
            try {
                columns = mColumnsClasses[i].newInstance();
                columns.onUpgrade(db,oldVersion,newVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public long insert(String tableName, ContentValues values) {
        if (db == null)
            db = getWritableDatabase();
        return db.insert(tableName, null, values);
    }

    /**
     *
     * @param tableName
     * @param id
     * @return 影响行数
     */
    public int delete(String tableName, int id) {
        if (db == null)
            db = getWritableDatabase();
        return db.delete(tableName, BaseColumns._ID + "=?",
                new String[] { String.valueOf(id) });
    }

    /**
     * 根据条件删除
     * @param tableName
     * @param whereClause
     * @param whereArgs
     * @return
     */
    public int delete(String tableName,String whereClause,String[] whereArgs){
        if(db == null){
            db = getWritableDatabase();
        }
        return db.delete(tableName,whereClause,whereArgs);
    }

    /**
     * @param tableName
     * @param values
     * @param WhereClause
     * @param whereArgs
     * @return 影响行数
     */
    public int update(String tableName, ContentValues values,
                      String WhereClause, String[] whereArgs) {
        if (db == null) {
            db = getWritableDatabase();
        }
        return db.update(tableName, values, WhereClause, whereArgs);
    }

    public Cursor query(String tableName, String[] columns, String whereStr,
                        String[] whereArgs) {
        if (db == null) {
            db = getReadableDatabase();
        }
        return db.query(tableName, columns, whereStr, whereArgs, null, null,
                null);
    }

    public Cursor rawQuery(String sql, String[] args) {
        if (db == null) {
            db = getReadableDatabase();
        }
        return db.rawQuery(sql, args);
    }

    public void execSQL(String sql) {
        if (db == null) {
            db = getWritableDatabase();
        }
        db.execSQL(sql);
    }

    public void closeDb() {
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
