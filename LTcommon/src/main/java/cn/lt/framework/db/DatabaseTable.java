package cn.lt.framework.db;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.Map;

import cn.lt.framework.exception.LTRunntimeException;

/**
 * Created by wenchao on 2016/1/6.
 */
public abstract class DatabaseTable implements BaseColumns {


    public String getTableCreateor() {
        return getTableCreator(getTableName(), getTableMap());
    }


    /**
     * Create a sentence to create a table by using a hash-map.
     *
     * @param tableName The table's name to create.
     * @param map       A map to store table columns info.
     * @return
     */
    private static final String getTableCreator(String tableName,
                                                Map<String, String> map) {
        String[]      keys    = map.keySet().toArray(new String[0]);
        String        value   = null;
        StringBuilder creator = new StringBuilder();
        creator.append("CREATE TABLE ").append(tableName).append("( ");
        int length = keys.length;
        for (int i = 0; i < length; i++) {
            value = map.get(keys[i]);
            creator.append(keys[i]).append(" ");
            creator.append(value);
            if (i < length - 1) {
                creator.append(",");
            }
        }
        creator.append(")");
        return creator.toString();
    }

    abstract public String getTableName();

    abstract public Class<IColumnType> getTableClass();

    abstract public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    private Map<String, String> getTableMap() {
        Class      tableClass = getTableClass();
        HashMap<String, String> columns    = new HashMap<>();
        for (Object obj : tableClass.getEnumConstants()) {
            if(obj instanceof IColumnType){
                columns.put(obj.toString(),((IColumnType) obj).getType());
            }else{
                throw new LTRunntimeException("TABLE CLASS"+tableClass+" can not cast to IColumnType,please implements IColumnType!");
            }
        }
        return columns;
    }


}
