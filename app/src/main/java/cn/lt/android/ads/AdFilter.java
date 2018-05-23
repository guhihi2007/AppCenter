package cn.lt.android.ads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.lt.android.LogTAG;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;

/**
 * Created by LinJunSheng on 2016/6/30.
 * 过滤广告工具
 */

public class AdFilter {

    /** 去除在白名单中的应用*/
    public synchronized void removeWhitelist(List<AppBriefBean> whitelist, List<AppBriefBean> adAppList) {
        if(adAppList.size() == 0
                || whitelist == null
                || whitelist.size() == 0) {
            if(whitelist.size() == 0) {
                LogUtils.i(LogTAG.AdTAG, "白名单列表size为0");
            }
            return;
        }
        LogUtils.i(LogTAG.AdTAG, "白名单列表 = " + whitelist.toString());

        List<AppBriefBean> tempAdList = new ArrayList<>();
        tempAdList.addAll(adAppList);


        for(AppBriefBean whiteApp : whitelist) {

            for(AppBriefBean adApp : tempAdList) {
                if(whiteApp.getPackage_name().equals(adApp.getPackage_name())) {
                    LogUtils.i(LogTAG.AdTAG , "因为白名单被移除的广告应用名称 = " + adApp.getName());
                    adAppList.remove(adApp);
                }
            }
        }

        LogUtils.i(LogTAG.AdTAG , "广告过滤白名单后 总数是 = " + adAppList.size());

    }

    /**
     * 去除已安装的应用
     * @param appList 需过滤的列表（一般app列表、广告列表）
     */
    public synchronized void removeInstalled(List<AppBriefBean> appList) {
        if(appList.size() == 0) {
            return;
        }

        List<AppBriefBean> tempAdList = new ArrayList<>();
        tempAdList.addAll(appList);

            for(AppBriefBean adApp : tempAdList) {
                if(AppUtils.isInstalled(adApp.getPackage_name())) {
                    appList.remove(adApp);
                    LogUtils.i(LogTAG.AdTAG , "被去掉在已安装的广告应用名称 = " + adApp.getName());
                }
            }
    }

    /** 去除在任务列表 下载中、待安装的应用*/
    public synchronized void removeDownloadTask(List<AppBriefBean> adAppList) {
        List<AppEntity> appList = null;
        appList = DownloadTaskManager.getInstance().getAll();

        if(appList == null || appList.size() == 0) {
            return;
        }

        List<AppBriefBean> tempAdList = new ArrayList<>();
        tempAdList.addAll(adAppList);

        for (AppEntity taskApp : appList) {
            for(AppBriefBean adApp : tempAdList) {
                if(taskApp.getPackageName().equals(adApp.getPackage_name())) {
                    adAppList.remove(adApp);
                    LogUtils.i(LogTAG.AdTAG , "被去掉在任务列表中的广告应用名称 = " + adApp.getName());
                }

            }
        }

    }

    /** 去除已经在列表中展示过的广告应用*/
    public synchronized void removeExistAd(List<AppBriefBean> adAppList, int pageType) {
        Map<String, AppBriefBean> existAdList = AdService.getInstance().getExistAdList(pageType);

        if(existAdList.size() == 0) {
            return;
        }

        List<AppBriefBean> tempAdList = new ArrayList<>();
        tempAdList.addAll(adAppList);

            for(AppBriefBean adApp : tempAdList) {
                if(existAdList.containsKey(adApp.getPackage_name())){
                    LogUtils.i(LogTAG.AdTAG , "由于已经展示过的广告，要去掉 = " + adApp.getName());
                    adAppList.remove(adApp);
                }
            }

        LogUtils.i(LogTAG.AdTAG , "去重后的广告 总数是 = " + adAppList.size());

    }

    /** 去除在已删除广告列表中的广告应用*/
    public void removeRemovedAd(List<AppBriefBean> adList, int pageType) {
        Map<String, AppBriefBean> removeAdList = AdService.getInstance().getRemoveAdList(pageType);

        List<AppBriefBean> tempAdList = new ArrayList<>(adList);

            for(AppBriefBean adApp : tempAdList) {
                if(removeAdList.containsKey(adApp.getPackage_name())){
                    LogUtils.i(LogTAG.AdTAG , "由于已经在 “已删除广告列表” 中，所以此处再次要去掉 = " + adApp.getName());
                    adList.remove(adApp);
                }
            }
    }
}
