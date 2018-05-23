package cn.lt.android.ads.splash;

import android.app.Activity;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;

import cn.lt.android.Constant;
import cn.lt.android.entity.SplashShowTimeBean;
import cn.lt.android.util.GsonUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.TimeUtils;
import cn.lt.framework.util.PreferencesUtils;

/**
 * 开屏规则
 * Created by Erosion on 2018/3/26.
 */

public class SplashManager {

    public static final String BAI_DU_SPLASH_SHOW_TIME = "baidu_show_time"; // 百度开屏时间
    public static final String GDT_SPLASH_SHOW_TIME = "gdt_show_time"; //广点通开屏时间
    public static final String BAIDU_SPLASH_PROPORTION = "baidu_proportion"; // 百度开屏比例
    public static final String GDT_SPLASH_PROPORTION = "gdt_proportion"; // 广点通开屏比例

    private Activity context;
    private SplashADParent adView;
    private ViewGroup container;
    private TextView skipVew;
    private ImageView appLogo;
    private boolean isLoad;

    public SplashManager(Activity context, ViewGroup container, TextView skipVew,ImageView appLogo,boolean isLoad) {
        this.context = context;
        this.container = container;
        this.skipVew = skipVew;
        this.appLogo = appLogo;
        this.isLoad = isLoad;
    }

    /**
     * 开屏广告控制展示哪种广告
     *
     * @return
     */
    public void needShowSplash() {
        boolean baiduState = PreferencesUtils.getBoolean(context, Constant.BAIDU_STATUS);
        boolean gdtState = PreferencesUtils.getBoolean(context, Constant.GDT_STATUS);

        if (baiduState && !gdtState) {
            //只展示百度广告
            loadBaiDu();
            LogUtils.i("Erosion", "只展示百度开屏");
            return;
        }

        if (!baiduState && gdtState) {
            //只展示广点通广告
            LogUtils.i("Erosion", "只展示广点通开屏");
            loadGdt();
            return;
        }

        if (baiduState && gdtState) {
            //展示两种广告
            showTime();
            LogUtils.i("Erosion", "展示两种开屏");
        }
    }

    /**
     * 加载广点通开屏
     */
    private void loadGdt() {
        adView = new GDTSplashInitialize();
        adView.setmActivity(context);
        adView.setmContainer(container);
        adView.setmSkipVew(skipVew);
        if (null != appLogo) {
            adView.setAppLogo(appLogo);
        }
        adView.setLoad(isLoad);
        adView.showAdView();
    }

    /**
     * 加载百度开屏
     */
    private void loadBaiDu() {
        adView = new BaiDUSplashInitialize();
        adView.setmActivity(context);
        adView.setmContainer(container);
        if (null != appLogo) {
            adView.setAppLogo(appLogo);
        }
        adView.setLoad(isLoad);
        adView.showAdView();
    }

    /**
     * 展示两种广告
     */
    private void showTime() {
        String baiduJson = PreferencesUtils.getString(context, BAI_DU_SPLASH_SHOW_TIME);
        String gdtJson = PreferencesUtils.getString(context, GDT_SPLASH_SHOW_TIME);

        if (TextUtils.isEmpty(baiduJson) && TextUtils.isEmpty(gdtJson)) {
            //按比例展示广告
            LogUtils.i("Erosion","baiduJson && gdtJson 为空");
            proportionShowSplash();
            return;
        }

        long currTime = System.currentTimeMillis();
        String str = TimeUtils.getLongtoString(currTime);
        try {
            int count = 0;
            int timeSize = 0;

            if (!TextUtils.isEmpty(baiduJson)) {
                List<SplashShowTimeBean> baiduTimeList = GsonUtils.parseArray(baiduJson, SplashShowTimeBean[].class);
                timeSize = baiduTimeList.size();
                for (SplashShowTimeBean baiduBean : baiduTimeList) {

                    if (TextUtils.isEmpty(baiduBean.getStart_at()) || TextUtils.isEmpty(baiduBean.getEnd_at())) {
                        count++;
                    } else {
                        String baiduStartTime = str + " " + baiduBean.getStart_at() + ":00";
                        String baiduEndTime = str + " " + baiduBean.getEnd_at() + ":00";
                        long baiduStartAt = TimeUtils.string2Long(baiduStartTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                        long baiduEndAt = TimeUtils.string2Long(baiduEndTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

                        if (currTime > baiduStartAt && currTime < baiduEndAt) {
                            //展示百度开屏
                            LogUtils.i("Erosion", "按时间展示百度开屏");
                            loadBaiDu();
                            break;
                        } else {
                            count++;
                        }
                    }

                }
            }

            if (!TextUtils.isEmpty(gdtJson)) {
                List<SplashShowTimeBean> gdtTimeList = GsonUtils.parseArray(gdtJson, SplashShowTimeBean[].class);
                timeSize = timeSize + gdtTimeList.size();
                for (SplashShowTimeBean gdtBean : gdtTimeList) {

                    if (TextUtils.isEmpty(gdtBean.getStart_at()) || TextUtils.isEmpty(gdtBean.getEnd_at())) {
                        count++;
                    } else {
                        String gdtStartTime = str + " " + gdtBean.getStart_at() + ":00";
                        String gdtEndTime = str + " " + gdtBean.getEnd_at() + ":00";
                        long gdtStartAt = TimeUtils.string2Long(gdtStartTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                        long gdtEndAt = TimeUtils.string2Long(gdtEndTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

                        if (currTime > gdtStartAt && currTime < gdtEndAt) {
                            //展示广点通开屏
                            LogUtils.i("Erosion", "按时间展示广点通开屏");
                            loadGdt();
                            break;
                        } else {
                            count++;
                        }
                    }
                }
            }

            //按比例展示广告
            LogUtils.i("Erosion", "count===" + count);
            if (count == timeSize) {
                proportionShowSplash();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 比例展示广告
     */
    private void proportionShowSplash() {
        int baiduProportion = PreferencesUtils.getInt(context, BAIDU_SPLASH_PROPORTION);
        int gdtProportion = PreferencesUtils.getInt(context, GDT_SPLASH_PROPORTION);

        boolean baiduState = PreferencesUtils.getBoolean(context, Constant.BAIDU_STATUS);

        if (baiduProportion == 0 && gdtProportion == 0) {
            //按原有逻辑展示 优先百度
            if (baiduState) {
                //展示百度
                LogUtils.i("Erosion", "按原有逻辑展示 优先百度");
                loadBaiDu();
            } else {
                //展示广点通
                loadGdt();
                LogUtils.i("Erosion", "按原有逻辑展示 优先广点通");
            }
            return;
        }

        if (baiduProportion == 100) {
            //展示百度
            LogUtils.i("Erosion", "按比例展示只展示百度");
            loadBaiDu();
            return;
        }

        if (gdtProportion == 100) {
            //展示广点通
            loadGdt();
            LogUtils.i("Erosion", "按比例展示只展示广点通");
            return;
        }

        Random random = new Random();
        int next = random.nextInt(101);
        LogUtils.i("Erosion", "next===" + next);
        if (baiduProportion < gdtProportion) {
            if (next <= baiduProportion) {
                //展示百度
                loadBaiDu();
                LogUtils.i("Erosion", "按比例展示百度");
            } else {
                //展示广点通
                loadGdt();
                LogUtils.i("Erosion", "按比例展示广点通loadGdtloadGdt");
            }
        } else {
            if (next <= gdtProportion) {
                //展示广点通
                LogUtils.i("Erosion", "按比例展示广点通");
                loadGdt();
            } else {
                //展示百度
                loadBaiDu();
                LogUtils.i("Erosion", "按比例展示百度");
            }
        }
    }
}
