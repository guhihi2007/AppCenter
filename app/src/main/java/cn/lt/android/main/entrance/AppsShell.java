package cn.lt.android.main.entrance;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.AppTopicBean;
import cn.lt.android.entity.ClickTypeBean;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;

import static cn.lt.android.main.entrance.data.PresentType.app_topic;
import static cn.lt.android.main.entrance.data.PresentType.apps;
import static cn.lt.android.main.entrance.data.PresentType.carousel;
import static cn.lt.android.main.entrance.data.PresentType.entry;
import static cn.lt.android.main.entrance.data.PresentType.sub_entry;


/**
 * 重构数据结构
 * Created by LinJunSheng on 2016/8/9.
 */

public class AppsShell {
    /** 重构数据结构*/
    public synchronized static void RefactorDataStructure(final List<BaseBean> requestDataList, final BigPositionGeter bigPositionGeter) {
        setP1P2(requestDataList, bigPositionGeter);

        BaseBean bean;
        for (int i = 0; i < requestDataList.size(); i++) {
            bean = requestDataList.get(i);
            if (bean.getLtType().equals("apps")) {
                requestDataList.remove(i);
                BaseBeanList<AppDetailBean> appList = (BaseBeanList<AppDetailBean>) bean;

                for (int j = 0; j < appList.size(); j++) {
                    AppDetailBean app = appList.get(j);
                    app.setLtType("app");
                    requestDataList.add(i + j, app);

                    if (j == appList.size() - 1) {

                        app.isPositionLast = true;

                    }
                }

            }

            if (bean.getLtType().equals("app_topic")) {

                AppTopicBean appTopicBean = (AppTopicBean) bean;
                List<AppDetailBean> apps = appTopicBean.getApps();

                int columnNum = appTopicBean.getShow_type();
                columnNum = columnNum > 4 ? 4 :columnNum;
                columnNum = columnNum < 3 ? 3 :columnNum;

                if(apps.size() <= columnNum && appTopicBean.getPositionType() == 0) {
                    appTopicBean.setPositionType(AppTopicBean.IS_ONLY);
                }

                if (apps.size() > columnNum) {
                    requestDataList.remove(i);

                    for (int j = 0; j < apps.size() / columnNum; j++) {
                        int startPosition = j  * columnNum;

                        AppTopicBean topicBean = new AppTopicBean();
                        topicBean.p1 = appTopicBean.p1;

                        // 设置是首位还是末位
                        if(j == 0) {
                            topicBean.setPositionType(AppTopicBean.IS_FIRST);
                        } else if (j == apps.size() / columnNum - 1) {
                            topicBean.setPositionType(AppTopicBean.IS_LAST);
                        } else {
                            topicBean.setPositionType(AppTopicBean.DEFAULT);
                        }

                        topicBean.setLtType(appTopicBean.getLtType());
                        topicBean.setTopic_name(appTopicBean.getTopic_name());
                        topicBean.setTitle_color(appTopicBean.getTitle_color());
                        topicBean.setTopic_title(appTopicBean.getTopic_title());

                        List<AppDetailBean> list = new ArrayList<>();

                        int p = 0;
                        while (p < columnNum) {
                            list.add(apps.get(startPosition + p));
                            p++;
                        }

                        topicBean.setApps(list);
                        requestDataList.add(i + j, topicBean);
                    }
                }

            }


        }


    }

    private static void setP1P2(List<BaseBean> requestDataList, BigPositionGeter bigPositionGeter) {
        BaseBean bean;
        PresentType type;
        for (int i = 0; i < requestDataList.size(); i++) {
            bean = requestDataList.get(i);
            type = PresentType.valueOf(bean.getLtType());

            bean.p1 = bigPositionGeter.getBigPosition(type);

            if (type == apps) {
                BaseBeanList<AppDetailBean> appList = (BaseBeanList<AppDetailBean>) bean;

                for (int j = 0; j < appList.size(); j++) {
                    AppDetailBean app = appList.get(j);
                    app.p1 = bean.p1;
                    app.p2 = j + 1;
                }
            }


            if (type == app_topic) {
                AppTopicBean appTopicBean = (AppTopicBean) bean;
                appTopicBean.p1 = bean.p1;
                List<AppDetailBean> apps = appTopicBean.getApps();
                for (int j = 0; j < apps.size(); j++) {
                    AppDetailBean app = apps.get(j);
                    app.p1 = bean.p1;
                    app.p2 = j + 1;
                }
            }

            // 多组入口,长方形的或圆形的
            if (type == entry || type == sub_entry || type == carousel) {
                BaseBeanList<ClickTypeBean> clickTypeList = (BaseBeanList<ClickTypeBean>) bean;
                for (int j = 0; j < clickTypeList.size(); j++) {
                    ClickTypeBean clickTypeBean = clickTypeList.get(j);
                    clickTypeBean.p1 = bean.p1;
                    clickTypeBean.p2 = j + 1;
                }
            }



        }
    }


}
