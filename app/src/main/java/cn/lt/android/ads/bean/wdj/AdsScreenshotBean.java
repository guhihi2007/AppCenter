package cn.lt.android.ads.bean.wdj;

import java.util.List;

/**
 * Created by ltbl on 2016/6/30.
 */
public class AdsScreenshotBean {
    private List<String> normal;
    private List<String>  small;

    public List<String> getSmall() {
        return small;
    }

    public void setSmall(List<String> small) {
        this.small = small;
    }

    public List<String> getNormal() {
        return normal;
    }

    public void setNormal(List<String> normal) {
        this.normal = normal;
    }
}
