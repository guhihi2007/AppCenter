package cn.lt.android.ads;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.LogTAG;
import cn.lt.android.ads.bean.wdj.AdPackageBean;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.util.LogUtils;

/**
 * Created by LinJunSheng on 2016/9/22.
 */

public class AdListPackagesJoint {
    public static String jointPackages(List<AppBriefBean> adList) {
        List<AdPackageBean> list = new ArrayList<>();
        for(AppBriefBean app : adList){
            list.add(new AdPackageBean(app.getPackage_name()));
        }
        String packageNames = new Gson().toJson(list);
        LogUtils.i(LogTAG.AdTAG, "要上传的广告json = " + packageNames);
        return packageNames;
    }

}
