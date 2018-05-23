package cn.lt.android.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/***
 * 处理数据此方法主要用于1、将传入的list中的连续count个元素组合在一起直到所有元素被组合完； 2、计算两个数的百分比；
 * <p/>
 * 3、按照一种格式将一个数字转换成对应的字符串；
 *
 * @author dxx
 */
public class IntegratedDataUtil {
    /**
     * 将传入的list中的连续count个元素组合在一起直到所有元素被组合完
     *
     * @param list
     * @param count 将多少个元素组合在一起
     */
    public static <T> List<List<T>> integratedData(List<T> list, int count) {
        List<List<T>> finalList = new ArrayList<List<T>>();
        if (list == null || list.size() == 0) {
            return null;
        }
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            List<T> temp = new ArrayList<T>();
            for (int i = 0; i < count; i++) {
                try {
                    temp.add(iterator.next());
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    break;
                }

            }
            finalList.add(temp);
        }

        return finalList;
    }

    /**
     * 计算remain占 total的百分比并返回百分值字符串；
     *
     * @param remain 做分子
     * @param total  做分母；
     * @return
     */
    public static String calculatePrecent(int remain, int total) {

        if (total <= 0) {
            return null;
        }

        int result = (int) (100f * remain / total);

        return result + "%";
    }

    /**
     * 计算一个long型数据为多少MB大小并返回字符串值；
     *
     * @param size
     * @return
     */
    public static String calculateSizeMB(long size) {

        if (size == 0) {
            return 0 + "MB";
        }
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(1d * size / (1024 * 1024)) + "MB";

    }

    /**
     * 计算一个数据为多少"万"次并返回字符串值；
     *
     * @param downloadCnt
     * @return
     */
    public static String calculateCounts(int downloadCnt) {

        // 大于等于1万，小于100万
        if (downloadCnt >= 10000 && downloadCnt < 1000000) {
            double count = ArithUtil.div(downloadCnt, 10000, 1);

            if(count == 100) {
                return "100 万人下载";
            }

            // 不是整数时才需要显示小数位，只显示整数位
            if(Integer.parseInt(String.valueOf(count).split("\\.")[1]) >= 1) {
                return count + " 万人下载";
            } else {
                return (int)count + " 万人下载";
            }


        }


        // 大于等于100万，小于1亿的
        if(downloadCnt >= 1000000 && downloadCnt < 100000000) {
            double count = ArithUtil.div(downloadCnt, 10000, 0);

            if((int)count == 10000) {
                return "1 亿人下载";
            }

            return (int)count + " 万人下载";
        }


        // 大于等于1亿的
        if(downloadCnt >= 100000000) {
            double count = ArithUtil.div(downloadCnt, 100000000, 1);
            return (int)count + " 亿人下载";
        }

        return downloadCnt + " 人下载";

    }


}
