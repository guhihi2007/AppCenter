package cn.lt.android.ads;

import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LogTAG;
import cn.lt.android.ads.beantransfer.AppBeanTransfer;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.util.LogUtils;

/**
 * 广告替换工具
 * Created by LinJunSheng on 2016/6/30.
 */

public class DownloadAdAppReplacer {

    /** 如果应用列表里的app与任务列表中相同，把其替换成任务列表中的app*/
    public synchronized void replaceByBaseBeanList(List<BaseBean> originalList) {
        // 提前声明变量，减少内存抖动
        BaseBean baseBean;
        AppTopicBean topicBean;
        BaseBeanList<AppBriefBean> appList;
        List<AppEntity> taskAppList;

        for(int i = 0; i < originalList.size(); i++) {
            baseBean = originalList.get(i);
            if (baseBean != null) {

                if (baseBean.getLtType().equals("app_topic")) {
                    topicBean = (AppTopicBean) baseBean;
                    List<AppBriefBean> briefBeanList = topicBean.getBriefApps();

                    List<AppBriefBean> tempList = new ArrayList<>();
                    tempList.addAll(briefBeanList);
                    AppBriefBean briefBean;
                    for (int j = 0; j < tempList.size(); j++) {
                        briefBean = tempList.get(j);
                        replaceDownloadApp(briefBeanList, j, briefBean);
                    }
                }

                if (baseBean.getLtType().equals("apps")) {
                    appList = (BaseBeanList<AppBriefBean>) baseBean;

                    List<AppBriefBean> tempList = new ArrayList<>();
                    tempList.addAll(appList);
                    AppBriefBean briefBean;
                    for (int j = 0; j < tempList.size(); j++) {
                        briefBean = tempList.get(j);
                        replaceDownloadApp(appList, j, briefBean);
                    }

                }

                if (baseBean.getLtType().equals("app")) {
                    AppBriefBean originalApp = (AppBriefBean) baseBean;

                    taskAppList = DownloadTaskManager.getInstance().getAll();

                    if(taskAppList == null || taskAppList.size() == 0) {
                        continue;
                    }


                    for (AppEntity taskApp : taskAppList) {
                        if(taskApp.getPackageName().equals(originalApp.getPackage_name())) {
                            AppBriefBean taskBriefApp = AppBeanTransfer.getAppBriefBean(taskApp);

                            AppBriefBean app = (AppBriefBean)originalList.remove(i);
                            taskBriefApp.setLtType(app.getLtType());
                            taskBriefApp.setAD(app.isAD());
                            taskBriefApp.p1 = app.p1;
                            taskBriefApp.p2 = app.p2;
                            taskBriefApp.isPositionLast = app.isPositionLast;
                            taskBriefApp.setLtType("app");
                            originalList.add(i, taskBriefApp);
                            LogUtils.i(LogTAG.AdTAG , taskBriefApp.getName() + " App在任务列表中，把其替换掉页面上的《" + originalApp.getName() + "》");
                        }
                    }
                }

            }
        }
    }

    /** 如果应用列表里的app与任务列表中的广告app相同，把其替换掉广告的app*/
    public synchronized void replaceByAppBriefList(List<AppBriefBean> originalList) {
        List<AppBriefBean> tempList = new ArrayList<>();
        tempList.addAll(originalList);

        AppBriefBean briefBean;

        for(int i = 0; i < tempList.size(); i++) {
            briefBean = tempList.get(i);
            replaceDownloadApp(originalList, i, briefBean);
        }
    }

    /** 如果热搜bean里的app与任务列表中的广告app相同，把其替换掉广告的app*/
    public synchronized void replaceByHotSearchList(List<HotSearchBean> originalList) {
        List<HotSearchBean> tempList = new ArrayList<>();
        tempList.addAll(originalList);

        HotSearchBean hotSearchBean;

        for(int i = 0; i < tempList.size(); i++) {
            hotSearchBean = tempList.get(i);
            replaceDownloadApp(originalList, i, hotSearchBean);
        }
    }

    /** 如果应用列表里的app与任务列表中的广告app相同，把其替换掉广告的app*/
    /** 替换专题详情*/
    public synchronized void replaceByTopicDetail(List<BaseBean> originalList) {
        List<BaseBean> tempList = new ArrayList<>();
        tempList.addAll(originalList);

        List<AppEntity> appList = DownloadTaskManager.getInstance().getAll();

        AppBriefBean briefBean;
        for(int i = 0; i < tempList.size(); i++) {
            if (tempList.get(i).getLtType().equals("app")) {
                briefBean = (AppBriefBean) tempList.get(i);

                if(appList == null || appList.size() == 0) {
                    return;
                }

                for (AppEntity taskApp : appList) {
                    if(taskApp.getPackageName().equals(briefBean.getPackage_name())) {
                        AppBriefBean taskBriefApp = AppBeanTransfer.getAppBriefBean(taskApp);
                        originalList.remove(originalList.get(i));
                        taskBriefApp.setLtType(briefBean.getLtType());
                        originalList.add(i, taskBriefApp);
                        LogUtils.i(LogTAG.AdTAG , taskBriefApp.getName() + " App在任务列表中，把其替换掉页面上的《" + briefBean.getName() + "》");

                    }
                }

            }
        }
    }

    /** 替换应用详情的推荐app列表*/
    public synchronized void replaceByRecommendApps(List<RecommendBean> recommendList) {
        List<AppEntity> appList = DownloadTaskManager.getInstance().getAll();


        if(appList == null || appList.size() == 0) {
            return;
        }

        RecommendBean recommendBean;

        for (int i = 0; i < recommendList.size(); i++) {
            recommendBean = recommendList.get(i);

            for (AppEntity taskApp : appList) {
                if (taskApp.getPackageName().equals(recommendBean.getPackage_name())) {

                    try {
                        recommendBean.setAppEntity(DownloadTaskManager.getInstance().appEntityCopy(taskApp));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    recommendBean.setId(taskApp.getId() + "");
                    recommendBean.setPackage_size(taskApp.getPackageSize());
                    recommendBean.setName(taskApp.getName());
                    recommendBean.setAlias(taskApp.getAlias());
                    recommendBean.setIcon_url(taskApp.getIconUrl());
                    recommendBean.setDownload_url(taskApp.getDownloadUrl());

                    LogUtils.i(LogTAG.AdTAG, taskApp.getName() + " App在任务列表中，把其替换掉页面上的《" + recommendBean.getName() + "》");

                }
            }


        }
    }

    /** 替换*/
    private void replaceDownloadApp(List<AppBriefBean> originalList, int originalPosition, AppBriefBean originalApp) {
        List<AppEntity> appList = DownloadTaskManager.getInstance().getAll();

        if(appList == null || appList.size() == 0) {
            return;
        }


        for (AppEntity taskApp : appList) {
            if(taskApp.getPackageName().equals(originalApp.getPackage_name())) {
                AppBriefBean taskBriefApp = AppBeanTransfer.getAppBriefBean(taskApp);
                AppBriefBean app = originalList.remove(originalPosition);
                taskBriefApp.setLtType(app.getLtType());
                taskBriefApp.setAD(app.isAD());
                taskBriefApp.p1 = app.p1;
                taskBriefApp.p2 = app.p2;
                originalList.add(originalPosition, taskBriefApp);
                LogUtils.i(LogTAG.AdTAG , taskBriefApp.getName() + " App在任务列表中，把其替换掉页面上的《" + originalApp.getName() + "》");
            }
        }
    }



    /** 替换HotBean*/
    private void replaceDownloadApp(List<HotSearchBean> originalList, int originalPosition, HotSearchBean originalApp) {
        List<AppEntity> appList = DownloadTaskManager.getInstance().getAll();

        if(appList == null || appList.size() == 0) {
            return;
        }


        for (AppEntity taskApp : appList) {
            if(taskApp.getPackageName().equals(originalApp.getPackage_name())) {
                HotSearchBean hotSearchBean = AppBeanTransfer.getHotSearchBean(taskApp);
                HotSearchBean app = originalList.remove(originalPosition);

                hotSearchBean.setId(app.getId());
                hotSearchBean.setTitle(app.getTitle());
                hotSearchBean.setLtType(app.getLtType());
                originalList.add(originalPosition, hotSearchBean);
                LogUtils.i(LogTAG.AdTAG , hotSearchBean.getPackage_name() + " App在任务列表中，把其替换掉页面上的《" + originalApp.getPackage_name() + "》");
            }
        }
    }



}
