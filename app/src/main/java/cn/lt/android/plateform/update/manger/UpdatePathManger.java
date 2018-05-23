package cn.lt.android.plateform.update.manger;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.util.LogUtils;


public class UpdatePathManger {
    /***
     * 获取升级包下载文件夹路径
     *
     * @return
     */
    public static String getDownloadPath(Context context) {
        String downPath = null;
        try {
            String path = LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.root);
            Runtime.getRuntime().exec("chmod 777 " + path);
            path = path + File.separator + "upgrade";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdir();
            }
            downPath = path + File.separator;
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.i("DownloadManager===", downPath);
        return downPath;
    }

    /***
     * 获取升级包绝对路径
     *
     * @return
     */
    public static String getDownloadFilePath(Context context) {
        return getDownloadPath(context) + "update.apklll";
    }
}
