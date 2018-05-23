package cn.lt.android.manager.fs;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.lt.framework.fs.Directory;
import cn.lt.framework.fs.DirectoryContext;
import cn.lt.framework.util.TimeConstants;

/**
 * Created by wenchao on 2016/1/19.
 */
public class LTDirectoryContext  extends DirectoryContext{
    private Context mContext;

    public LTDirectoryContext(Context context,String rootFolder){
        mContext = context;
        initContext(rootFolder);
    }

    @Override
    public void initContext(String root) {
        String rootPath = null;
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED))
        {
            File fileDir = mContext.getFilesDir();
            rootPath = fileDir.getAbsolutePath() + File.separator + root;
        }
        else
        {
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + root;
        }
        super.initContext(rootPath);
    }

    @Override
    protected Collection<Directory> initDirectories() {
        List<Directory> children = new ArrayList<Directory>();

        Directory dir = newDirectory(LTDirType.log);
        children.add(dir);
        dir = newDirectory(LTDirType.image);
        children.add(dir);
        dir = newDirectory(LTDirType.crash);
        children.add(dir);
        dir = newDirectory(LTDirType.app);
        children.add(dir);
        dir = newDirectory(LTDirType.cache);
        children.add(dir);
        return children;
    }

    private Directory newDirectory(LTDirType type)
    {
        Directory child = new Directory(type.toString(), null);
        child.setType(type.value());
        if (type.equals(LTDirType.cache))
        {
            child.setForCache(true);
            child.setExpiredTime(TimeConstants.ONE_DAY_MS);
        }

        return child;
    }
}
