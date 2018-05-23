package cn.lt.framework.db;

/**
 * Created by wenchao on 2016/1/19.
 */
public enum  DemoTable implements IColumnType {
    id(integer),name(text),title(text)

    ;

    @Override
    public String getType() {
        return type;
    }
    String type;
    DemoTable(String type){
        this.type = type;
    }
}
