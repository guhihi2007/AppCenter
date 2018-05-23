package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by LinJunSheng on 2017/1/9.
 */

public class AdShowBean extends BaseEventBean {
    private String adListStr;
    private int curPage;
    private String pageName;
    private String adMold;

    public String getAdListStr() {
        return adListStr;
    }

    public void setAdListStr(String adListStr) {
        this.adListStr = adListStr;
    }

    public int getCurPage() {
        return curPage;
    }

    public void setCurPage(int curPage) {
        this.curPage = curPage;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getAdMold() {
        return adMold;
    }

    public void setAdMold(String adMold) {
        this.adMold = adMold;
    }
}
