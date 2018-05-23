package cn.lt.framework.fs;

import java.util.Collection;

/**
 * Created by wenchao on 2016/1/19.
 */
public abstract class DirectoryContext {
    public static final int APP_ROOT_DIR = 1;
    protected Directory baseDirectory;

    public DirectoryContext(String root) {
        this.initContext(root);
    }

    protected DirectoryContext() {
    }

    public void initContext(String root) {
        Directory dir = new Directory(root, (Directory)null);
        this.baseDirectory = dir;
        dir.setType(1);
        Collection children = this.initDirectories();
        if(children != null && children.size() > 0) {
            dir.addChildren(children);
        }

    }

    public Directory getBaseDirectory() {
        return this.baseDirectory;
    }

    protected abstract Collection<Directory> initDirectories();
}
