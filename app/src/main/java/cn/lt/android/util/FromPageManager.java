package cn.lt.android.util;

import android.text.TextUtils;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;

/**
 * @author chengyong
 * @time 2016/7/23 11:03
 * @des 页面及页面id的管理类
 */
public class FromPageManager {
    /**
     * 直接获取上一级页面
     *
     * @return
     */
    public static String getLastPage() {
        List<String> mLastPageList = LTApplication.instance.mLastPageList;
        String lastPage = "";
        if (mLastPageList.size() == 1) {
            lastPage = mLastPageList.get(0);
        } else if (mLastPageList.size() > 1) {
            lastPage = mLastPageList.get(1);
        }
        return lastPage;
    }

    /**
     * 传入当前页面并设置确定上一级页面
     *
     * @param getCurrentPage
     * @return
     */
    public static void setLastPage(String getCurrentPage) {
        try {
            List<String> mLastPageList = LTApplication.instance.mLastPageList;
            LogUtils.e("juice", "设置上级页面时传入的当前的页面是==>" + getCurrentPage);
            if (!TextUtils.isEmpty(getCurrentPage)) {
                //            if(mLastPageList.size()>=1 && mLastPageList.get(0).equals(getCurrentPage)){
                //                return;
                //            }
                LogUtils.e("juice", "真正赋值：页面时传入的当前的页面是==>" + getCurrentPage);
                mLastPageList.add(0, getCurrentPage);
            }
            if (mLastPageList.size() > 1) {
                if (mLastPageList.size() > 2) {
                    String currentPage = mLastPageList.get(0);
                    String secondPage = mLastPageList.get(1);
                    mLastPageList.removeAll(mLastPageList);
                    mLastPageList.add(0, currentPage);
                    mLastPageList.add(1, secondPage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 传入当前页面id 并设置确定上一级页面id
     *
     * @param currentPageId
     * @return
     */
    public static void setLastPageId(String currentPageId) {
        try {
            List<String> mLastPageIdList = LTApplication.instance.getmLastPageIdList();
            LogUtils.e("juice", "设置上级页面id时传入的当前的id是==>" + currentPageId);
            //            if(mLastPageIdList.size()>=1 && mLastPageIdList.get(0).equals(currentPageId)){  //连续两次一样不存 TODO 详情页除外
            //                return;
            //            }
            LogUtils.e("juice", "真正传入赋值：id时传入的当前的id是==>" + currentPageId);
            mLastPageIdList.add(0, currentPageId);
            if (mLastPageIdList.size() > 2) {
                String currentPage = mLastPageIdList.get(0);
                String secondPage = mLastPageIdList.get(1);
                mLastPageIdList.removeAll(mLastPageIdList);
                mLastPageIdList.add(0, currentPage);
                mLastPageIdList.add(1, secondPage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取上一级页面id
     *
     * @return
     */
    public static String getLastPageId() {
        List<String> mLastPageIdList = LTApplication.instance.getmLastPageIdList();
        String lastPageId = "";
        if (mLastPageIdList.size() == 1) {
            lastPageId = mLastPageIdList.get(0);
        } else if (mLastPageIdList.size() > 1) {
            lastPageId = mLastPageIdList.get(1);
        }
        return lastPageId;
    }


    /**
     * 设置from_page并上报
     */
//    public static void setFromPageAndReport(StatisticsEventData event,String page) {
//        try {
//            DCStat.pageJumpEvent(event);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 当前页是搜索时确定word
     *
     * @param currPage
     * @return
     */
    public static boolean isWordByPage(String currPage) {
        if (Constant.PAGE_SEARCH_ADS.equals(currPage) || Constant.PAGE_SEARCH_RESULT.equals(currPage)
                || Constant.PAGE_SEARCH_AUTOMATCH.equals(currPage) || Constant.PAGE_SEARCH_NODATA.equals(currPage)
                || Constant.PAGE_SEARCH_ADV.equals(currPage)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 上一页是搜索时确定word
     *
     * @param lastpage
     * @return
     */
    public static boolean isWordByLastPage(String lastpage) {
        if ((Constant.PAGE_SEARCH_AUTOMATCH.equals(lastpage) || Constant.PAGE_SEARCH_RESULT.equals(lastpage) || Constant.PAGE_SEARCH_ADS.equals(lastpage)
                || Constant.PAGE_SEARCH_ADV.equals(lastpage) || Constant.PAGE_SEARCH_NODATA.equals(lastpage)) && Constant.PAGE_DETAIL.equals(LTApplication.instance.current_page)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 需要上报from_id的页面
     *
     * @return
     */
    public static boolean setFromIdByPage() {
        if (Constant.PAGE_DETAIL.equals(getLastPage()) || Constant.PAGE_AD_DETAIL.equals(getLastPage())
                || Constant.PAGE_SEARCH_ADS.equals(getLastPage()) || Constant.PAGE_SPECIAL_DETAIL.equals(getLastPage())
                || Constant.PAGE_SEARCH_AUTOMATCH.equals(getLastPage()) || Constant.PAGE_CATEGORY_DETAIL.equals(getLastPage())
                || Constant.PAGE_NOTIFICATION.equals(getLastPage()) || Constant.PAGE_NORMAL_LIST.equals(getLastPage())
                || (getLastPage()).contains(Constant.PAGE_NORMAL_LIST_TITLE) || Constant.PAGE_FLOAT.equals(getLastPage())
                || getLastPage().contains(Constant.PAGE_NORMAL_PT_TITLE)) {
            return true;
        } else {
            return false;
        }
    }
}
