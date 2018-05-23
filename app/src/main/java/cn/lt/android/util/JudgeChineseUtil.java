package cn.lt.android.util;

import java.util.regex.Pattern;

/**
 * @author wangchengyong
 * @version $Rev$
 * @time 2016/5/11 14:55
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class JudgeChineseUtil {
    public static boolean isChineseChar(String str){
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find();
    }

    public static boolean isContainsEnglish(String str) {
//        int num = 0;
        for(int i = 0; i<str.length (); i++){
            char x = str.charAt(i);
            if ((x > 'a' && x < 'z') || (x > 'A' && x < 'Z')) {
//                num++;
                return true;
            }
        }
//         if(num>0){
//             return true;
//         }
        return false;
    }
}
