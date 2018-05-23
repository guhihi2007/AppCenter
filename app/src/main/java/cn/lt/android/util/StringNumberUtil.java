package cn.lt.android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/5/10.
 */
public class StringNumberUtil {

    //截取数字
    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    // 截取非数字
    public static String splitNotNumber(String content) {
        Pattern pattern = Pattern.compile("\\D+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return "";
    }

    // 判断一个字符串是否含有数字
    public static boolean hasNumber(String content) {
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        return m.matches();
    }

    // 判断最后一个字母是否为数字
    public static boolean isLastIndexNumber(String content) {
        String last = content.substring(content.length()-1, content.length());
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(last);
        return m.matches();
    }

    /**
     * 去除字符串中的空格、回车、换行符等
     * @param str
     * @return
     */
    public static String replaceBlank(String str){
        String dest = "";
        if (str != null){
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
