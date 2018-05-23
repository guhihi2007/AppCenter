package cn.lt.android.manager.fs;

/**
 * Created by wenchao on 2016/1/19.
 */
public enum LTDirType {
    root,log,image,app,cache,crash;

    public int value(){
        return ordinal()+1;
    }
}
