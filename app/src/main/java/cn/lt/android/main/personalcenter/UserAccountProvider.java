package cn.lt.android.main.personalcenter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.List;

import cn.lt.android.GlobalParams;
import cn.lt.android.LogTAG;
import cn.lt.android.db.UserEntity;
import cn.lt.android.util.LogUtils;

/**
 * @author chengyong
 * @time 2017/9/21 11:08
 * @des ${TODO}
 */
public class UserAccountProvider extends ContentProvider {

	static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final String TABLENAME = "USER_ENTITY";
	static{
		matcher.addURI("cn.lt.appstore.UserAccountProvider", TABLENAME, 8);
	}

    public static final String NUMBER = "number";
    public static final String TOKEN = "token";
    public static final String USERID = "userid";

	private SQLiteDatabase mDatabase;

    @Override
	public boolean onCreate() {
		initDataBase();
		return true;
	}

	private synchronized void initDataBase() {
        if(mDatabase==null){
            mDatabase = GlobalParams.getDb();
        }
	}
    @Override
    public String getType(Uri uri) {
        return null;
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		initDataBase();
		if(matcher.match(uri) == 8){
			LogUtils.d(LogTAG.USER, "appcenter:provider,delete");
            GlobalParams.getUserDao().deleteAll();
//			mDatabase.close();
			
			getContext().getContentResolver().notifyChange(uri, null);

		}else{
			throw new IllegalArgumentException("URI不匹配");
		}
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		initDataBase();
		if(matcher.match(uri) == 8){
			LogUtils.d(LogTAG.USER, "appcenter:provider,insert：token:"+(String)values.get(TOKEN));
            GlobalParams.getUserDao().insert(new UserEntity(null,(String)values.get(TOKEN)
                    ,(String)values.get(NUMBER),(String)values.get(USERID),null,null,null,null,null,null,null));

//			mDatabase.close();
			getContext().getContentResolver().notifyChange(uri, null);
			
			
		}else{
			throw new IllegalArgumentException("URI不匹配");
		}
		return null;
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		initDataBase();
		Cursor cursor = null;
		List<UserEntity> list;
		if(matcher.match(uri) == 8){
			LogUtils.d(LogTAG.USER, "appcenter:provider,query---");
			cursor = GlobalParams.getDbUser().query(TABLENAME, null, null, null, null, null, null);
			LogUtils.d(LogTAG.USER, "appcenter:provider,query得到的cursor是---"+cursor);
			/*cursor.close();
			db.close();*/
		}else{
			throw new IllegalArgumentException("URI不匹配");
		}
		return cursor ;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		initDataBase();
		if(matcher.match(uri) == 8){

			LogUtils.d(LogTAG.USER, "appcenter:provider,update---");

			mDatabase.update(TABLENAME, values, selection, selectionArgs);

			mDatabase.close();
			
			getContext().getContentResolver().notifyChange(uri, null);
		}else{
			throw new IllegalArgumentException("URI不匹配");
		}
		return 0;
	}

}
