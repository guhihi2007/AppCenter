package cn.lt.android.event;

/**
 * @author chengyong
 * @time 2016/8/16 10:23
 * @des 用于传递双击tab事件
 */
public class TabClickEvent {
    public String tabName;

    public TabClickEvent(String tabName) {
        this.tabName = tabName;
    }
}
