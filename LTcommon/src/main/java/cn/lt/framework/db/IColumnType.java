package cn.lt.framework.db;

/**
 * Created by wenchao on 2016/1/19.
 */
public interface IColumnType {
    String text = "text";
    String integer = "integer";

    String getType();
}
