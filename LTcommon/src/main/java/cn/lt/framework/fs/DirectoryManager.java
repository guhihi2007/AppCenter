package cn.lt.framework.fs;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import cn.lt.framework.util.CollectionUtils;

/**
 * Created by wenchao on 2016/1/19.
 */
public final class DirectoryManager {

    private DirectoryContext   context;
    private LinkedList<DirMap> dirs;

    public DirectoryManager(DirectoryContext context) {
        this.context = context;
        this.dirs = new LinkedList();
    }

    public boolean buildAndClean() {
        Directory directory = this.context.getBaseDirectory();
        return this.createDirectory(directory, true);
    }

    public File getDir(int type) {
        if(type <= 0) {
            return null;
        } else {
            Iterator var3 = this.dirs.iterator();

            while(var3.hasNext()) {
                DirectoryManager.DirMap map = (DirectoryManager.DirMap)var3.next();
                if(map.type == type) {
                    return map.dir;
                }
            }

            return null;
        }
    }

    public String getDirPath(int type) {
        File file = this.getDir(type);
        return file == null?"":file.getAbsolutePath();
    }

    private void cleanCache(File dir, final long expired) {
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile()?CacheChecker.expired(pathname, expired):false;
            }
        });
        if(!CollectionUtils.isEmpty(files)) {
            for(int i = 0; i < files.length; ++i) {
                files[i].delete();
            }

        }
    }

    private boolean createDirectory(Directory directory, boolean cleancache) {
        boolean ret = true;
        String path = null;
        Directory parent = directory.getParent();
        File file;
        if(parent == null) {
            path = directory.getPath();
        } else {
            file = this.getDir(parent.getType());
            path = file.getAbsolutePath() + File.separator + directory.getPath();
        }

        file = new File(path);
        if(!file.exists()) {
            ret = file.mkdirs();
        } else if(cleancache && directory.isForCache()) {
            this.cleanCache(file, directory.getExpiredTime());
        }

        if(!ret) {
            return false;
        } else {
            this.dirs.add(new DirectoryManager.DirMap(directory.getType(), file));
            Collection children = directory.getChildren();
            if(children != null) {
                Iterator var9 = children.iterator();

                while(var9.hasNext()) {
                    Directory dir = (Directory)var9.next();
                    if(!this.createDirectory(dir, true)) {
                        return false;
                    }
                }
            }

            return ret;
        }
    }

    private static class DirMap {
        int type;
        File dir;

        public DirMap(int t, File d) {
            this.type = t;
            this.dir = d;
        }
    }
}
