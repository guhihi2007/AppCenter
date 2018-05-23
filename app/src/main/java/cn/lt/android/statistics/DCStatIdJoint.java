package cn.lt.android.statistics;

import java.util.List;

import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.entity.AppDetailBean;



/**
 * Created by LinJunSheng on 2016/7/27.
 */

public class DCStatIdJoint {

    public static String jointIdByAppDetailBean(List<AppDetailBean> apps) {
        String appIds = "";
        if(apps != null) {
            for (int i = 0; i < apps.size(); i++) {
                AppDetailBean app = apps.get(i);
                if(i != apps.size() - 1) {
                    appIds += app.getAppClientId() + " | ";
                } else {
                    appIds += app.getAppClientId();
                }
            }
        }

        return appIds;
    }

    public static String jointIdByAppBriefBean(List<AppBriefBean> apps) {
        String appIds = "";
        if(apps != null) {
            for (int i = 0; i < apps.size(); i++) {
                AppBriefBean app = apps.get(i);
                if(i != apps.size() - 1) {
                    appIds += app.getAppClientId() + " | ";
                } else {
                    appIds += app.getAppClientId();
                }
            }
        }

        return appIds;
    }

    public static String jointIdByAppEntity(List<AppEntity> apps) {
        String appIds = "";
        if(apps != null) {
            for (int i = 0; i < apps.size(); i++) {
                AppEntity app = apps.get(i);
                if(i != apps.size() - 1) {
                    appIds += app.getAppClientId() + " | ";
                } else {
                    appIds += app.getAppClientId();
                }
            }
        }

        return appIds;
    }
}
