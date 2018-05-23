package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.entity.AppBriefBean;
import cn.lt.android.event.DownloadEvent;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.event.RemoveEvent;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.UIController;
import cn.lt.android.main.download.DownloadButton;
import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentData;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.ReportEvent;
import cn.lt.android.statistics.StatisticsDataProductorImpl;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.WeakView;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.download.util.FileDownloadUtils;


/***
 * Created by dxx on 2016/2/29.
 */
@SuppressWarnings("ALL")
public class ItemAbrahamianView extends ItemView implements View.OnClickListener {

    private List<ImageView> mLogos = new ArrayList<>();
    private List<TextView> mNames = new ArrayList<>();
    private List<TextView> mDownloadCounts = new ArrayList<>();
    private List<DownloadButton> mDownloadBTs = new ArrayList<>();
    public View mChangePadding;
    private List<ItemData<AppBriefBean>> lists;
    private AppEntity mEntitys;

    public ItemAbrahamianView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ItemAbrahamianView(Context context, String pageName, String id) {
        super(context, pageName, id);
        initWeakView();
        init();
    }

    private void initWeakView() {
        new WeakView<ItemAbrahamianView>(this) {
            @Override
            public void onEventMainThread(DownloadEvent downloadEvent) {
                if (lists == null || lists.size() <= 0) {
                    return;
                }
                for (int i = 0; i < lists.size(); i++) {
                    AppBriefBean app = lists.get(i).getmData();

                    if (app == null) {
                        continue;
                    }
                    mEntitys = app.getDownloadAppEntity();
                    if (mEntitys == null) {
                        try {
                            mEntitys = DownloadTaskManager.getInstance().transfer(app);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            // TODO:
                        }
                    }

                    if (FileDownloadUtils.generateId(mEntitys.getPackageName(), mEntitys.getSavePath()) == downloadEvent.downloadId) {
                        mEntitys.setTotal(downloadEvent.totalBytes);
                        mEntitys.setSoFar(downloadEvent.soFarBytes);
                        mEntitys.setStatus(downloadEvent.status);

                        mDownloadBTs.get(i).setData(mEntitys, mPageName);
                    }
                }
            }

            @Override
            public void onEventMainThread(InstallEvent installEvent) {
                if (lists == null || lists.size() <= 0) {
                    return;
                }
                for (int i = 0; i < lists.size(); i++) {
                    AppBriefBean app = lists.get(i).getmData();

                    if (app == null) {
                        continue;
                    }
                    mEntitys = app.getDownloadAppEntity();
                    if (mEntitys == null) {
                        try {
                            mEntitys = DownloadTaskManager.getInstance().transfer(app);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            // TODO:
                        }
                    }
                    if (mEntitys.getPackageName().equals(installEvent.packageName)) {
                        if (mEntitys.getStatus() == DownloadStatusDef.completed && installEvent.type == InstallEvent.INSTALL_FAILURE) {

                            //推荐页里面的bean 内存不足安装失败是改变bean里面的状态
                            mEntitys.setStatus(InstallState.install_failure);

                        } else {
                            mEntitys.setStatusByInstallEvent(installEvent.type);
                        }
                        mDownloadBTs.get(i).setData(mEntitys, mPageName);
                    }

                }
            }

            @Override
            public void onEventMainThread(RemoveEvent removeEvent) {
                if (lists == null || lists.size() <= 0) {
                    return;
                }
                for (int i = 0; i < lists.size(); i++) {
                    AppBriefBean app = lists.get(i).getmData();
                    if (app == null) {
                        continue;
                    }
                    mEntitys = app.getDownloadAppEntity();
                    if (mEntitys == null) {
                        try {
                            mEntitys = DownloadTaskManager.getInstance().transfer(app);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            // TODO:
                        }
                    }
                    if (mEntitys.getPackageName().equals(removeEvent.mAppEntity.getPackageName())) {
                        //更新界面i
                        mEntitys.setStatus(DownloadStatusDef.INVALID_STATUS);
                        mDownloadBTs.get(i).setData(mEntitys, mPageName);
                    }
                }
            }
        };
    }

    public ItemAbrahamianView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        if (bean != null) {
            mItemData = bean;
            lists = (List) bean.getmData();
            if (lists != null && lists.size() > 0) {
                for (int i = 0; i < lists.size(); i++) {
                    AppBriefBean app = lists.get(i).getmData();
                    ImageloaderUtil.loadImage(getContext(), app.getIcon_url(), mLogos.get(i));
                    if (!TextUtils.isEmpty(app.getAlias())) {
                        mNames.get(i).setText(app.getAlias());
                    } else {
                        String appName = app.getName().length() <= 6 ? app.getName() : app.getName().substring(0, 6).concat("...");
                        mNames.get(i).setText(appName);
                    }
                    mDownloadCounts.get(i).setText(IntegratedDataUtil.calculateCounts(Integer.valueOf(app.getDownload_count())));

                    PresentData presentData = mItemData.getmPresentData();
                    AppEntity entitys = app.getDownloadAppEntity();
                    if (entitys == null) {
                        try {
                            entitys = DownloadTaskManager.getInstance().transfer(app);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return; // TODO
                        }
//                        entitys = app.getDownloadAppEntity();
                    }

                    entitys.p1 = 0;
                    entitys.p2 = i + 1;
                    entitys.resource_type = "abrahamian";

                    mDownloadBTs.get(i).setData(entitys, mPageName);
                    mLogos.get(i).setTag(R.id.data_tag, app);
                    mNames.get(i).setTag(R.id.data_tag, app);
                    mDownloadCounts.get(i).setTag(R.id.data_tag, app);
                    mLogos.get(i).setTag(R.id.click_date, i);
                    mNames.get(i).setTag(R.id.click_date, i);
                    mDownloadCounts.get(i).setTag(R.id.click_date, i);
                }
            }

        }
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_item_abrahamian, this);
        mChangePadding = findViewById(R.id.padding_change);
        mLogos.add((ImageView) findViewById(R.id.iv_logo_gold_item_rank));
        mLogos.add((ImageView) findViewById(R.id.iv_logo_silver_item_rank));
        mLogos.add((ImageView) findViewById(R.id.iv_logo_bronze_item_rank));

        mNames.add((TextView) findViewById(R.id.tv_name_gold_rank_item));
        mNames.add((TextView) findViewById(R.id.tv_name_silver_rank_item));
        mNames.add((TextView) findViewById(R.id.tv_name_bronze_rank_item));

        mDownloadCounts.add((TextView) findViewById(R.id.tv_download_gold_rank_item));
        mDownloadCounts.add((TextView) findViewById(R.id.tv_download_silver_rank_item));
        mDownloadCounts.add((TextView) findViewById(R.id.tv_download_bronze_rank_item));

        mDownloadBTs.add((DownloadButton) findViewById(R.id.db_gold_rank_item));
        mDownloadBTs.add((DownloadButton) findViewById(R.id.db_silver_rank_item));
        mDownloadBTs.add((DownloadButton) findViewById(R.id.db_bronze_rank_item));

        for (int i = 0; i < 3; i++) {
            mLogos.get(i).setOnClickListener(this);
            mNames.get(i).setOnClickListener(this);
            mDownloadCounts.get(i).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        AppBriefBean app = (AppBriefBean) v.getTag(R.id.data_tag);
        UIController.goAppDetail(getContext(), app.isAdData(), app.getAdMold(), app.getAppClientId(), app.getPackage_name(), app.getApps_type(), mPageName, app.getCategory(), app.getDownload_url(), app.getReportData());

        int pos = (int) v.getTag(R.id.click_date);
        PresentData presentData = mItemData.getmPresentData();
        presentData.setPos(presentData.getPos());
        StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(mItemData.getmPresentData(), app.isAdData(), pos + 1, app.getAppClientId(), mPageName, ReportEvent.ACTION_CLICK);
        eventData.setAd_type(app.getAdMold());
        DCStat.clickEvent(eventData);

    }

    /**
     * 通知下载进度更新
     *
     * @param downloadEvent
     */
//    public void onEventMainThread(DownloadEvent downloadEvent) {
//        if (lists == null || lists.size() <= 0) {
//            return;
//        }
//        for (int i = 0; i < lists.size(); i++) {
//            AppBriefBean app = lists.get(i).getmData();
//
//            if (app == null) {
//                continue;
//            }
//            mEntitys = app.getDownloadAppEntity();
//            if (mEntitys == null) {
//                try {
//                    mEntitys = DownloadTaskManager.getInstance().transfer(app);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                    // TODO:
//                }
//            }
//
//            if (FileDownloadUtils.generateId(mEntitys.getPackageName(), mEntitys.getSavePath()) == downloadEvent.downloadId) {
//                mEntitys.setTotal(downloadEvent.totalBytes);
//                mEntitys.setSoFar(downloadEvent.soFarBytes);
//                mEntitys.setStatus(downloadEvent.status);
//
//                mDownloadBTs.get(i).setData(mEntitys, mPageName);
//            }
//        }
//
//    }

    /**
     * 通知安装事件状态更新
     */
//    public void onEventMainThread(InstallEvent installEvent) {
//        if (lists == null || lists.size() <= 0) {
//            return;
//        }
//        for (int i = 0; i < lists.size(); i++) {
//            AppBriefBean app = lists.get(i).getmData();
//
//            if (app == null) {
//                continue;
//            }
//            mEntitys = app.getDownloadAppEntity();
//            if (mEntitys == null) {
//                try {
//                    mEntitys = DownloadTaskManager.getInstance().transfer(app);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                    // TODO:
//                }
//            }
//            if (mEntitys.getPackageName().equals(installEvent.packageName)) {
//                if (mEntitys.getStatus() == DownloadStatusDef.completed && installEvent.type == InstallEvent.INSTALL_FAILURE) {
//
//                    //推荐页里面的bean 内存不足安装失败是改变bean里面的状态
//                    mEntitys.setStatus(InstallState.install_failure);
//
//                } else {
//                    mEntitys.setStatusByInstallEvent(installEvent.type);
//                }
//                mDownloadBTs.get(i).setData(mEntitys, mPageName);
//            }
//
//        }
//
//    }

    /**
     * 通知任务移除时的操作
     */
//    public void onEventMainThread(RemoveEvent event) {
//        if (lists == null || lists.size() <= 0) {
//            return;
//        }
//
//        for (int i = 0; i < lists.size(); i++) {
//            AppBriefBean app = lists.get(i).getmData();
//
//            if (app == null) {
//                continue;
//            }
//            mEntitys = app.getDownloadAppEntity();
//
//            if (mEntitys == null) {
//                try {
//                    mEntitys = DownloadTaskManager.getInstance().transfer(app);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                    // TODO:
//                }
//            }
//            if (mEntitys.getPackageName().equals(event.mAppEntity.getPackageName())) {
//                //更新界面i
//                mEntitys.setStatus(DownloadStatusDef.INVALID_STATUS);
//                mDownloadBTs.get(i).setData(mEntitys, mPageName);
//            }
//        }
//
//    }


}
