package cn.lt.android.manager.fs;

import java.io.File;

import cn.lt.android.LTApplication;
import cn.lt.framework.fs.DirectoryManager;

/**
 * Created by wenchao on 2016/1/19.
 */
public class LTDirectoryManager {
    public static final String            ROOT_FOLDER       = "TT_AppCenter";
    private static      LTDirectoryManager sDirManager       = null;
    private             DirectoryManager  mDirectoryManager = null;

    private LTDirectoryManager() {
    }

    public static boolean initManager() {
        boolean flag = true;
        if (sDirManager == null) {
            synchronized (LTDirectoryManager.class) {
                if (sDirManager == null)
                {
                    sDirManager = new LTDirectoryManager();

                    flag = sDirManager.init();
                }
            }
        }
        return flag;
    }

    public boolean init()
    {
        DirectoryManager dm = new DirectoryManager(new LTDirectoryContext(LTApplication.instance, ROOT_FOLDER));
        boolean ret = dm.buildAndClean();
        if (!ret)
        {
            return false;
        }

        mDirectoryManager = dm;

        return ret;
    }

    private DirectoryManager getDirectoryManager()
    {
        if (sDirManager == null)
        {
            return null;
        }

        return sDirManager.mDirectoryManager;
    }

    private File getDirectory(LTDirType type)
    {
        DirectoryManager manager = getDirectoryManager();
        if (manager == null)
        {
            return null;
        }

        return manager.getDir(type.value());
    }

    public String getDirectoryPath(LTDirType type)
    {
        File file = getDirectory(type);
        if (file == null)
        {
            return null;
        }

        return file.getAbsolutePath();
    }

    public static LTDirectoryManager getInstance()
    {
        return sDirManager;
    }
}
