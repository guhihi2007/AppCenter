package cn.lt.framework.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by wenchao on 2016/1/19.
 */
public final class Directory {
    private String path;
    private Directory parent = null;
    private Collection<Directory> children;
    private int     type        = -1;
    private boolean forCache    = false;
    private long    expiredTime = -1L;

    public Directory(String path, Directory parent) {
        this.path = path;
        this.parent = parent;
    }

    public String getPath() {
        return this.path;
    }

    public Directory getParent() {
        return this.parent;
    }

    public Collection<Directory> getChildren() {
        return this.children;
    }

    public void addChild(Directory directory) {
        if(this.children == null) {
            this.children = new ArrayList();
        }

        directory.parent = this;
        this.children.add(directory);
    }

    public void addChildren(Collection<Directory> dirs) {
        if(dirs != null && dirs.size() != 0) {
            Iterator var3 = dirs.iterator();

            while(var3.hasNext()) {
                Directory d = (Directory)var3.next();
                this.addChild(d);
            }

        }
    }

    public void addChild(int type, String path, long expired) {
        Directory child = new Directory(path, this);
        child.type = type;
        if(expired > 0L) {
            child.expiredTime = expired;
            child.forCache = true;
        }

        this.addChild(child);
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isForCache() {
        return this.forCache;
    }

    public void setForCache(boolean forCache) {
        this.forCache = forCache;
    }

    public long getExpiredTime() {
        return this.expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }
}
