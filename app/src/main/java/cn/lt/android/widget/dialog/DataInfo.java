package cn.lt.android.widget.dialog;

/***
 * Created by dxx on 2016/3/9.
 */
public class DataInfo<T> {
    T mData;

    public DataInfo(T mData) {
        this.mData = mData;
    }

    public T getmData() {
        return mData;
    }

    public void setmData(T mData) {
        this.mData = mData;
    }
}
