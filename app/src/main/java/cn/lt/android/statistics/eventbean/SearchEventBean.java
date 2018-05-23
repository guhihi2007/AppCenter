package cn.lt.android.statistics.eventbean;

import cn.lt.android.statistics.eventbean.base.BaseEventBean;

/**
 * Created by ltbl on 2016/5/30.
 */
public class SearchEventBean extends BaseEventBean {
    private String page;
    private String resource_typ;
    private String word;
    private int p2;
    private int p1;

    public String getResource_typ() {
        return resource_typ;
    }

    public void setResource_typ(String resource_typ) {
        this.resource_typ = resource_typ;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getP2() {
        return p2;
    }

    public void setP2(int p2) {
        this.p2 = p2;
    }

    public int getP1() {
        return p1;
    }

    public void setP1(int p1) {
        this.p1 = p1;
    }
}
