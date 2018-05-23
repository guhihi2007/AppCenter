package cn.lt.android.main;

/**
 * Created by wenchao on 2016/3/1.
 * 公用的item数据，包括item的类型和填充数据
 */
public class Item<T> {
    //视图类型
    public int viewType;

    //视图填充的数据
    public T data;

    public int pos;

    public Item(int viewType, T data) {
        this.viewType = viewType;
        this.data = data;
    }

    public Item() {
    }

    public Item(int viewType, T data,int pos) {
        this.viewType = viewType;
        this.data = data;
        this.pos = pos;
    }
}
