package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.statistics.StatisticsDataProductorImpl;
import cn.lt.android.statistics.StatisticsEventData;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.JudgeChineseUtil;
import cn.lt.android.widget.WeakView;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;

/***
 * 专题栏 （带标题和图片的View）
 */
public class SquareAppView extends ItemView implements View.OnClickListener {

    private ImageView mLogoIv;

    private TextView mNameTv;// 游戏名

    private TextView mSizeTv;// 游戏标签和大小
    private DownloadButton mDownloadButton;
    private AppBriefBean mApp;
    private AppEntity entitys;

    public SquareAppView(Context context, String pageName) {
        super(context, pageName);
        initWeakView();
        init();
    }

    private void initWeakView() {
        new WeakView<SquareAppView>(this) {
            @Override
            public void onEventMainThread(DownloadEvent downloadEvent) {
                if (entitys == null) return;
                if (entitys.getPackageName().equals(downloadEvent.packageName)) {
                    entitys.setTotal(downloadEvent.totalBytes);
                    entitys.setSoFar(downloadEvent.soFarBytes);
                    entitys.setStatus(downloadEvent.status);
                    mDownloadButton.setData(entitys, mPageName);
                }
            }

            @Override
            public void onEventMainThread(InstallEvent installEvent) {
                if (entitys == null) return;
                if (entitys.getPackageName().equals(installEvent.packageName)) {
                    if (entitys.getStatus() == DownloadStatusDef.completed && installEvent.type == InstallEvent.INSTALL_FAILURE) {
                        //推荐页里面的bean 内存不足安装失败是改变bean里面的状态
                        entitys.setStatus(InstallState.install_failure);
                    } else {
                        entitys.setStatusByInstallEvent(installEvent.type);
                    }
                    mDownloadButton.setData(entitys, mPageName);
                }
            }

            @Override
            public void onEventMainThread(RemoveEvent removeEvent) {
                if(entitys == null) {
                    return;
                }
                if (entitys.getPackageName().equals(removeEvent.mAppEntity.getPackageName())) {
                    //更新界面i
                    entitys.setStatus(DownloadStatusDef.INVALID_STATUS);
                    mDownloadButton.setData(entitys, mPageName);
                }
            }
        };
    }

    public SquareAppView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {
        if (bean != null) {
            mItemData = bean;
            mApp = (AppBriefBean) bean.getmData();
            try {
                ImageloaderUtil.loadImage(getContext(), mApp.getIcon_url(), mLogoIv);
            } catch (Exception e) {
                mLogoIv.setImageResource(R.mipmap.app_default);
            }

            mNameTv.setText(TextUtils.isEmpty(mApp.getAlias()) ? mApp.getName() : mApp.getAlias());

            mSizeTv.setText(IntegratedDataUtil.calculateSizeMB(Long.valueOf(mApp.getPackage_size())));
            entitys = mApp.getDownloadAppEntity();
            /************这个方法也调用了远程，耗时**************/
            if (entitys == null) {
                try {
                    entitys = DownloadTaskManager.getInstance().transfer(mApp);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            entitys.p1 = mApp.p1;
            entitys.p2 = mApp.p2;
            entitys.resource_type = "app_topic";

            mDownloadButton.setData(entitys, mPageName);   //里面也调用了远程，耗时
            this.setOnClickListener(this);
        }

    }

//    private String getAppName() {
//        String finalAppName = "";
//
//        try {
//            String tempName = TextUtils.isEmpty(mApp.getAlias()) ? mApp.getName() : mApp.getAlias();
//
//            if (JudgeChineseUtil.isChineseChar(tempName)) {
//                // have chinese
//                if (JudgeChineseUtil.isContainsEnglish(tempName)) {
//                    finalAppName = tempName.length() <= 10 ? tempName : tempName.substring(0, 10).concat("...");
//                } else {
//                    // full chinese
//                    finalAppName = tempName.length() <= 6 ? tempName : tempName.substring(0, 6).concat("...");
//                }
//            } else {
//                // full english
//                finalAppName = tempName.length() <= 12 ? tempName : tempName.substring(0, 12).concat("...");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//
//        return finalAppName;
//    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_smallimage_down, this);
        mLogoIv = (ImageView) findViewById(R.id.iv_square_game_view);
        mNameTv = (TextView) findViewById(R.id.nameTv);
        mSizeTv = (TextView) findViewById(R.id.tagSizeTv);
        mDownloadButton = (DownloadButton) findViewById(R.id.download_button);

    }

    public void setStyle(boolean large) {
        ViewGroup.LayoutParams params = mLogoIv.getLayoutParams();
//        RelativeLayout.LayoutParams nameParams = (RelativeLayout.LayoutParams) mNameTv.getLayoutParams();
//        RelativeLayout.LayoutParams sizeParams = (RelativeLayout.LayoutParams) mSizeTv.getLayoutParams();
//        RelativeLayout.LayoutParams downParams = (RelativeLayout.LayoutParams) mDownloadButton.getLayoutParams();
        if (large) {
            int iconSize = DensityUtil.dip2px(getContext(), 70);
            if (params.width == iconSize) {
                return;
            }
            params.width = params.height = iconSize;

//            nameParams.setMargins(0,DensityUtil.dip2px(getContext(),6),0,0);
//            mNameTv.setLines(1);
            mNameTv.setSingleLine();
            mNameTv.setMaxEms(6);

//            sizeParams.setMargins(0,DensityUtil.dip2px(getContext(),2),0,0);
//            downParams.setMargins(0,DensityUtil.dip2px(getContext(),5),0,0);
        } else {
            int iconSize = DensityUtil.dip2px(getContext(), 60);
            if (params.width == iconSize) {
                return;
            }
            params.width = params.height = iconSize;

//            nameParams.setMargins(0,DensityUtil.dip2px(getContext(),10),0,0);
            mNameTv.setSingleLine(false);
            mNameTv.setLines(2);
            mNameTv.setMaxEms(4);

//            sizeParams.setMargins(0,DensityUtil.dip2px(getContext(),4),0,0);
//            downParams.setMargins(0,DensityUtil.dip2px(getContext(),8),0,0);
        }
    }


    @Override
    public void onClick(View v) {
        UIController.goAppDetail(getContext(), mApp.isAdData(), mApp.getAdMold(), mApp.getAppClientId(), mApp.getPackage_name(), mApp.getApps_type(), mPageName, mApp.getCategory(), mApp.getDownload_url(), mApp.getReportData());
        PresentData presentData = new PresentData();
        presentData.setmType(PresentType.app_topic);
        presentData.setPos(mApp.p1);
        StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(presentData, mApp.isAdData(), mApp.p2, mApp.getAppClientId(), mPageName, null);
        eventData.setAd_type(mApp.getAdMold());
        DCStat.clickEvent(eventData);
    }


}
