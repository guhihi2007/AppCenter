package cn.lt.android.ads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.lt.android.LogTAG;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.util.LogUtils;

/**
 * 广告app填充工具
 * Created by LinJunSheng on 2016/6/30.
 */

public class AdFillMachine {

    private Random random = new Random();

    /**
     * 把广告app填充如列表中
     */
    public synchronized void fillAdAppByBaseBeanList(List<BaseBean> originalList, List<AppBriefBean> adAppList, int pageType) {
        removeAdApp(originalList, adAppList, pageType);

        LogUtils.i(LogTAG.AdTAG, "开始填充广告，现在广告数量。。 = " + adAppList.size());
        List<AppBriefBean> tempList = null;

        // 提前声明变量，减少内存抖动
        BaseBean baseBean;
        AppBriefBean app;
        Map<String, AppBriefBean> existAdList;
        Map<String, AppBriefBean> backupList;

        for (int i = 0; i < originalList.size(); i++) {

            baseBean = originalList.get(i);
            if (baseBean != null) {
                if (baseBean.getLtType().equals("app_topic")) {
                    AppTopicBean topicBean = (AppTopicBean) baseBean;
                    List<AppBriefBean> briefBeanList = topicBean.getBriefApps();

                    tempList = new ArrayList<>(briefBeanList);
                    AppBriefBean briefBean;
                    for (int j = 0; j < tempList.size(); j++) {
                        briefBean = tempList.get(j);
                        fillAdApp(briefBeanList, j, briefBean, adAppList, pageType);
                    }
                }

                if (baseBean.getLtType().equals("apps")) {
                    BaseBeanList<AppBriefBean> appList = (BaseBeanList<AppBriefBean>) baseBean;

                    tempList = new ArrayList<>(appList);
                    AppBriefBean briefBean;
                    for (int j = 0; j < tempList.size(); j++) {
                        briefBean = tempList.get(j);
                        fillAdApp(appList, j, briefBean, adAppList, pageType);
                    }

                }

                if (baseBean.getLtType().equals("app")) {

                        app = (AppBriefBean) baseBean;

                        // 是广告位，填充
                        if (app.isAD() && app.canReplace()) {

                            if(adAppList.size() > 0) {
                                AppBriefBean adApp = adAppList.remove(adAppList.size() == 1 ? 0 : random.nextInt(adAppList.size()));
                                if (adApp == null) {
                                    continue;
                                }

                                adApp.setLtType("app");
                                originalList.remove(i);
                                adApp.isPositionLast = app.isPositionLast;
                                adApp.p1 = baseBean.p1;
                                adApp.p2 = baseBean.p2;
                                adApp.setCanReplace(true);

                                originalList.add(i, adApp);

                                // 记录到已展示广告的名单中
                                markToExistAdList(pageType, adApp);

                                // 记录到备用资源的名单中
                                markToBackupList(pageType, app);

                                // 记录到数据上报
                                AdReporter.add(pageType, adApp);

                                LogUtils.i(LogTAG.AdTAG, "被填充的广告是 = " + adApp.getName());
                            }


                        } else {

                            // 当自有资源与已展示过广告相同时，先替换成备用资源，如果备用资源用完了，就替换成相同的广告资源
                            existAdList = AdService.getInstance().getExistAdList(pageType);
                            if(existAdList.containsKey(app.getPackage_name())) {

                                backupList = AdService.getInstance().getBackupList(pageType);

                                AppBriefBean backupApp = null;
                                for (Map.Entry<String, AppBriefBean> entry : backupList.entrySet()) {
                                    if(!existAdList.containsKey(entry.getKey())) {
                                        backupApp = backupList.remove(entry.getKey());
                                        break;
                                    }
                                }

                                // 判断是替换成备用资源还是广告资源
                                AppBriefBean replaceApp = null;
                                if(backupApp != null) {
                                    replaceApp = backupApp;
                                    LogUtils.i(LogTAG.AdTAG, app.getName() + " 与已展示过广告相同,使用备用资源 <"+ backupApp.getName() + "> 来替换");
                                } else {
                                    if (adAppList.size() > 0) {
                                        AppBriefBean adApp = adAppList.remove(adAppList.size() == 1 ? 0 : random.nextInt(adAppList.size()));
                                        replaceApp = adApp;

                                        // 记录到数据上报
                                        AdReporter.add(pageType, adApp);
                                        LogUtils.i(LogTAG.AdTAG, app.getName() + " 与已展示过广告相同,使用其他广告资源来替换");
                                    }

                                }

                                if(replaceApp != null) {
                                    // 替换
                                    originalList.remove(i);
                                    replaceApp.isPositionLast = app.isPositionLast;
                                    replaceApp.p1 = app.p1;
                                    replaceApp.p2 = app.p2;
                                    replaceApp.setLtType("app");
                                    originalList.add(i, replaceApp);
                                }
                            }
                        }




                }

            }
        }

        LogUtils.i(LogTAG.AdTAG, "广告填充完毕，剩余数量 = " + adAppList.size());


    }

    private void markToBackupList(int pageType, AppBriefBean app) {
        AdService.getInstance().getBackupList(pageType).put(app.getPackage_name(), app);
    }

    /**
     * 把广告app填充如列表中
     */
    public synchronized void fillAdAppByAppBriefList(List<AppBriefBean> originalList, List<AppBriefBean> adAppList, int pageType) {
        List<AppBriefBean> tempList = new ArrayList<>(originalList);

        AppBriefBean briefBean;

        for (int i = 0; i < tempList.size(); i++) {
            briefBean = tempList.get(i);

            // 非广告位，判断此位置上的app是否跟广告的有相同，是的话去除广告
            remove(originalList, adAppList, pageType);

            fillAdApp(originalList, i, briefBean, adAppList, pageType);
        }
    }


    /**
     * 标志为不可替换的资源，与自有的资源重复的广告去掉
     */
    public void removeAdApp(List<BaseBean> originalList, List<AppBriefBean> adAppList, int pageType) {
        if (adAppList.size() == 0) {
            return;
        }

        BaseBean baseBean;
        AppBriefBean app;

        for (int i = 0; i < originalList.size(); i++) {
            baseBean = originalList.get(i);
            if (baseBean != null) {
                if (baseBean.getLtType().equals("app_topic")) {
                    AppTopicBean bean = (AppTopicBean) baseBean;
                    List<AppBriefBean> briefBeanList = bean.getBriefApps();

                    // 非广告位，判断此位置上的app是否跟广告的有相同，是的话去除广告
                    remove(briefBeanList, adAppList, pageType);
                }

                if (baseBean.getLtType().equals("apps")) {
                    BaseBeanList<AppBriefBean> appList = (BaseBeanList<AppBriefBean>) baseBean;

                    // 标志为不可替换的资源，判断此位置上的app是否跟广告的有相同，是的话去除广告
                    remove(appList, adAppList, pageType);

                }

                if (baseBean.getLtType().equals("app")) {

                    app = (AppBriefBean) baseBean;
                    List<AppBriefBean> tempAdList = new ArrayList<>(adAppList);
                    if (!app.canReplace()) {
                        for (AppBriefBean adApp : tempAdList) {
                            if (app.getPackage_name().equals(adApp.getPackage_name())) {
                                adAppList.remove(adApp);

                                // 记录到已删除广告的名单中
                                markToRemoveAdList(pageType, adApp);
                                LogUtils.i(LogTAG.AdTAG, "与自有资源相同的广告是 ： " + adApp.getName() + ", 把它删除掉");
                            }
                        }
                    }

                }

            }
        }


    }

    /**
     * 删除与自有资源重复的广告
     */
    private void remove(List<AppBriefBean> originalList, List<AppBriefBean> adAppList, int pageType) {
        List<AppBriefBean> tempAdList = new ArrayList<>(adAppList);

        for (AppBriefBean app : originalList) {

            if (!app.canReplace()) {
                for (AppBriefBean adApp : tempAdList) {
                    if (app.getPackage_name().equals(adApp.getPackage_name())) {
                        adAppList.remove(adApp);

                        // 记录到已删除广告的名单中
                        markToRemoveAdList(pageType, adApp);
                        LogUtils.i(LogTAG.AdTAG, "与自有资源相同的广告是 ： " + adApp.getName() + ", 把它删除掉");
                    }
                }
            }
        }
    }

    /**
     * 填充广告应用
     */
    private void fillAdApp(List<AppBriefBean> originalList, int originalPosition, AppBriefBean originalApp, List<AppBriefBean> adAppList, int pageType) {

        // 是广告位，填充
        if (originalApp.isAD() && originalApp.canReplace()) {
            if (adAppList.size() == 0) {
                return;
            }

            AppBriefBean adApp = adAppList.remove(adAppList.size() == 1 ? 0 : random.nextInt(adAppList.size()));

            if (adApp == null) {
                return;
            }

            AppBriefBean app = originalList.remove(originalPosition);
            adApp.p1 = app.p1;
            adApp.p2 = app.p2;
            adApp.setCanReplace(true);

            originalList.add(originalPosition, adApp);

            // 记录到已展示广告的名单中
            markToExistAdList(pageType, adApp);

            // 记录到备用资源的名单中
            markToBackupList(pageType, app);

            // 记录到数据上报
            AdReporter.add(pageType, adApp);

            LogUtils.i(LogTAG.AdTAG, "被填充的广告是 = " + adApp.getName());
        } else {

            // 当自有资源与已展示过广告相同时，先替换成备用资源，如果备用资源用完了，就替换成相同的广告资源
            Map<String, AppBriefBean> existAdList = AdService.getInstance().getExistAdList(pageType);
            if(existAdList.containsKey(originalApp.getPackage_name())) {

                Map<String, AppBriefBean> backupList = AdService.getInstance().getBackupList(pageType);

                AppBriefBean backupApp = null;
                for (Map.Entry<String, AppBriefBean> entry : backupList.entrySet()) {
                    if(!existAdList.containsKey(entry.getKey())) {
                        backupApp = backupList.remove(entry.getKey());
                        break;
                    }
                }

                // 判断是替换成备用资源还是广告资源
                AppBriefBean replaceApp = null;
                if(backupApp != null) {
                    replaceApp = backupApp;
                    LogUtils.i(LogTAG.AdTAG, originalApp.getName() + " 与已展示过广告相同,使用备用资源 <" + backupApp.getName() + "> 来替换");
                } else {
                    if (adAppList.size() > 0) {
                        replaceApp = adAppList.remove(adAppList.size() == 1 ? 0 : random.nextInt(adAppList.size()));

                        // 记录到数据上报
                        AdReporter.add(pageType, replaceApp);
                        LogUtils.i(LogTAG.AdTAG, originalApp.getName() + " 与已展示过广告相同,使用其他广告资源来替换");
                    }


                }

                if(replaceApp != null) {
                    // 替换
                    AppBriefBean app = originalList.remove(originalPosition);
                    replaceApp.p1 = app.p1;
                    replaceApp.p2 = app.p2;
                    originalList.add(originalPosition, replaceApp);
                }


            }
        }

    }

    /**
     * 记录到已展示广告的名单中
     */
    private void markToExistAdList(int pageType, AppBriefBean adApp) {
        AdService.getInstance().getExistAdList(pageType).put(adApp.getPackage_name(), adApp);
    }

    /**
     * 记录到已删除广告的名单中
     */
    private void markToRemoveAdList(int pageType, AppBriefBean adApp) {
        AdService.getInstance().getRemoveAdList(pageType).put(adApp.getPackage_name(), adApp);
    }

}
