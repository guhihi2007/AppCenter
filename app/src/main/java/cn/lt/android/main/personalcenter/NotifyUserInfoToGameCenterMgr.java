package cn.lt.android.main.personalcenter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LogTAG;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.util.LogUtils;

/**
 * @author chengyong
 * @time 2017/9/21 15:21
 * @des ${TODO}
 */

public class NotifyUserInfoToGameCenterMgr {

    public static final String DATABASE_NAME = "user.db";

    public static final String TABLE_NAME = "account";
    public static final  Uri uri = Uri.parse("content://cn.lt.game.UserAccountProvider/account");

    public static final String NUMBER = "number";
    public static final String TOKEN = "token";
    public static final String USERID = "userid";

    public static final String AVATAR = "avatar";
    public static final String EMAIL = "email";
    public static final String NICKNAME = "nickname";
    public static final String SEX = "sex";
    public static final String BIRTHDAY = "birthday";
    public static final String ADDRESS = "address";
    public static final String USERNAME = "userName";
    public static void insertIntoGameCenter(Context context,UserBaseInfo userBaseInfo){
        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(NUMBER, userBaseInfo.getMobile());
            values.put(TOKEN, userBaseInfo.getToken());
            values.put(USERID, userBaseInfo.getId()+"");
            resolver.insert(uri, values);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(LogTAG.USER, "应用市场 NotifyUserInfoToGameCenterMgr：insertIntoGameCenter-抛异常" + e.getMessage());
        }
    }

    public static void deleteIntoGameCenter(Context context){
        try {
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(uri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(LogTAG.USER, "应用市场 NotifyUserInfoToGameCenterMgr：deleteIntoGameCenter-抛异常" + e.getMessage());
        }
    }

    public static List<UserBaseInfo> quaryFromGameCenter(Context context){
        Cursor cursor=null;
        try {
            List<UserBaseInfo> list=new ArrayList<>();
            ContentResolver resolver = context.getContentResolver();
            cursor = resolver.query(uri, null, null, null, null);
            if(cursor!=null){
                LogUtils.d(LogTAG.USER, "应用市场 NotifyUserInfoToGameCenterMgr：quaryFromGameCenter cursor 不为空-");
            }
            while (cursor.moveToNext()) {
                LogUtils.d(LogTAG.USER, "应用市场 NotifyUserInfoToGameCenterMgr：quaryFromGameCenter cursor遍历了");
                UserBaseInfo info=new UserBaseInfo();
//                info.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(USERID))));
                info.setToken(cursor.getString(cursor.getColumnIndex(TOKEN)));
//                info.setMobile(cursor.getString(cursor.getColumnIndex(NUMBER)));
//                info.setAvatar(cursor.getString(cursor.getColumnIndex(AVATAR)));
//                info.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
//                info.setNickname(cursor.getString(cursor.getColumnIndex(NICKNAME)));
//                info.setSex(cursor.getString(cursor.getColumnIndex(SEX)));
//                info.setBirthday((cursor.getString(cursor.getColumnIndex(BIRTHDAY))));
//                info.setAddress(cursor.getString(cursor.getColumnIndex(  ADDRESS)));
//                info.setUserName(cursor.getString(cursor.getColumnIndex(  USERNAME)));
                list.add(info);
            }
//            cursor.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(LogTAG.USER, "应用市场 NotifyUserInfoToGameCenterMgr：quaryFromAppCenter-" + e.getMessage());
        }finally {
            cursor.close();
        }
        return null;
    }
}
