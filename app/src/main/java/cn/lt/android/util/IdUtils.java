package cn.lt.android.util;


/**
 * Created by LinJunSheng on 2016/6/22.
 */
public class IdUtils {

    public static String parseServerId(String clientId) {
        LogUtils.i("IdUtils","client Id =" + clientId);
        if(!clientId.contains("|")) {
            LogUtils.i("IdUtils","return not contains(\"|\") = " + clientId);
            return clientId;
        }

        String [] s = clientId.split("\\|");
        if (s.length != 2) {
            return clientId.replace("|", "");
        }
        return s[1];
    }
}
