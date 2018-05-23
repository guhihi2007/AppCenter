package cn.lt.android.main.entrance.data;

/***
 * Created by dxx on 2016/03/08.
 */
public class ItemData<T> {
    private PresentData mPresentData = new PresentData();
    private T mData;
    private boolean isFirst;
    private boolean isLast;

    public ItemData() {
    }

    public ItemData(T mData) {
        setmData(mData);
    }

    public ItemData(T mData, PresentType type) {
        setmData(mData);
        setmType(type);
    }

    public ItemData(T mData, PresentType type, int pos, int subPos) {
        setmData(mData);
        setmType(type);
        setPos(pos);
        setSubPos(subPos);
    }

    public PresentData getmPresentData() {
        return mPresentData;
    }

    public void setmPresentData(PresentData mPresentData) {
        this.mPresentData = mPresentData;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setIsFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setIsLast(boolean isLast) {
        this.isLast = isLast;
    }

    public int getPos() {
        return mPresentData.getPos();
    }

    public void setPos(int pos) {
        this.mPresentData.setPos(pos);
    }

    public int getSubPos() {
        return mPresentData.getSubPos();
    }

    public void setSubPos(int subPos) {
        this.mPresentData.setSubPos(subPos);
    }

    public T getmData() {
        return mData;
    }

    public void setmData(T mData) {
        this.mData = mData;
    }

    public PresentType getmPresentType() {
        return mPresentData.getmType();
    }

    public void setmType(PresentType mType) {
        this.mPresentData.setmType(mType);
    }

}
