package cn.lt.android.util;

import android.content.Context;

import java.util.List;

import cn.lt.android.LogTAG;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.ApkSignatureErrorHolder;

/**
 * Created by honaf on 2017/9/26.
 */

public class AppSignConflictUtil {
    public static void showSignConflictDialogs (Context context) {
        if(context == null) {
            LogUtils.w(LogTAG.HONAF,"請注意showSignConflictDialogs=>context為null");
            return;
        }
        List<AppEntity> installSignConflictList = DownloadTaskManager.getInstance().getInstallSignConflictList();
        for (AppEntity appEntity : installSignConflictList) {
            new PublicDialog(context, new ApkSignatureErrorHolder()).showDialog(new DataInfo(appEntity));
        }
    }
}
