package cn.lt.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by Administrator on 2016/3/1.
 */
public class StreamUtil {
    public static String readerToString(Reader reader) {
        try {
            BufferedReader r = new BufferedReader(reader);
            StringBuilder b = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                b.append(line);
            }
            return b.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
