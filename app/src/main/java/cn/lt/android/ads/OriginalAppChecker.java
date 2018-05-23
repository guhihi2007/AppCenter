package cn.lt.android.ads;

import java.util.List;
import java.util.Map;

import cn.lt.android.LogTAG;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.util.LogUtils;

/**
 * Created by LinJunSheng on 2017/2/19.
 * 自有资源检测工具（检测是否与已展示过的广告相同）
 */

public class OriginalAppChecker {

    public static void checkByBaseBeanList(List<BaseBean> originalList, int pageType) {
        for (int i = 0; i < originalList.size(); i++) {
            BaseBean baseBean = originalList.get(i);
            if (baseBean != null) {

                if (baseBean.getLtType().equals("app_topic")) {
                    AppTopicBean bean = (AppTopicBean) baseBean;
                    List<AppBriefBean> briefBeanList = bean.getBriefApps();

                    for (int j = 0; j < briefBeanList.size(); j++) {
                        AppBriefBean briefBean = briefBeanList.get(j);
                        checkApp(briefBeanList, briefBean, j, pageType);
                    }
                }

                if (baseBean.getLtType().equals("apps")) {
                    BaseBeanList<AppBriefBean> appList = (BaseBeanList<AppBriefBean>) baseBean;

                    for (int j = 0; j < appList.size(); j++) {
                        AppBriefBean briefBean = appList.get(j);
                        checkApp(appList, briefBean, j, pageType);
                    }

                }

                if (baseBean.getLtType().equals("app")) {
                    AppBriefBean app = (AppBriefBean) baseBean;

                    // 当自有资源与已展示过广告相同时，先替换成备用资源
                    Map<String, AppBriefBean> existAdList = AdService.getInstance().getExistAdList(pageType);
                    if(existAdList.containsKey(app.getPackage_name())) {

                        Map<String, AppBriefBean> backupList = AdService.getInstance().getBackupList(pageType);

                        AppBriefBean backupApp = null;
                        for (Map.Entry<String, AppBriefBean> entry : backupList.entrySet()) {
                            if(!existAdList.containsKey(entry.getKey())) {
                                backupApp = backupList.remove(entry.getKey());
                                break;
                            }
                        }

                        // 判断是替换成备用资源还是广告资源
                        if(backupApp != null) {

                            // 替换
                            backupApp.isPositionLast = app.isPositionLast;
                            backupApp.p1 = app.p1;
                            backupApp.p2 = app.p2;
                            backupApp.setLtType("app");
                            originalList.remove(i);
                            originalList.add(i, backupApp);

                            LogUtils.i(LogTAG.AdTAG, app.getName() + " 与已展示过广告相同,使用备用资源 <"+ backupApp.getName() + "> 来替换");
                        }


                    }
                }

            }
        }
    }

    private static void checkApp(List<AppBriefBean> originalList, AppBriefBean originalApp, int position, int pageType) {
        // 当自有资源与已展示过广告相同时，先替换成备用资源
        Map<String, AppBriefBean> existAdList = AdService.getInstance().getExistAdList(pageType);
        if (existAdList.containsKey(originalApp.getPackage_name())) {

            Map<String, AppBriefBean> backupList = AdService.getInstance().getBackupList(pageType);

            AppBriefBean backupApp = null;
            for (Map.Entry<String, AppBriefBean> entry : backupList.entrySet()) {
                if (!existAdList.containsKey(entry.getKey())) {
                    backupApp = backupList.remove(entry.getKey());
                    break;
                }
            }

            if (backupApp != null) {

                // 替换
                backupApp.p1 = originalApp.p1;
                backupApp.p2 = originalApp.p2;
                originalList.remove(position);
                originalList.add(position, backupApp);

                LogUtils.i(LogTAG.AdTAG, originalApp.getName() + " 与已展示过广告相同,使用备用资源 <" + backupApp.getName() + "> 来替换");
            }


        }

    }
}
