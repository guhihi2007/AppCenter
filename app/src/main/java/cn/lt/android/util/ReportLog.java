package cn.lt.android.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;

/**
 * Created by atian on 2016/7/13.
 *
 * @des 给测试用来记录数据上报日志
 */
public class ReportLog {
    private static String rootPath = LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.root) + File.separator + "report";


    private final static String enter = System.getProperty("line.separator");

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static synchronized void log(final String logMessage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec("chmod 777 " + rootPath);
                    File folder = new File(rootPath);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    File file = new File(rootPath + File.separator + "数据上报日志(上线删除).txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    BufferedReader in = new BufferedReader(new FileReader(file));
                    String str = "";
                    String strToal = "";

                    while ((str = in.readLine()) != null) {
                        strToal += (str + enter);
                    }
                    strToal = strToal + (sdf.format(new Date()) + " " + logMessage + enter);
                    in.close();
                    BufferedWriter out = new BufferedWriter(new FileWriter(file));
                    out.write(strToal);
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
