package cn.lt.android.main.entrance.item.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.ads.wanka.WanKaLog;
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
import cn.lt.android.util.FromPageManager;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.JudgeChineseUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.WeakView;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;

import static cn.lt.appstore.R.id.rl_DownBtn;


public class ItemSingleAppView extends ItemView implements View.OnClickListener {
    String themeType = "1";
    private ImageView mLogoIv;
    private ImageView mMarkIv;
    private TextView name_count_size_describeTV;
    private TextView mRankNumberView;
    private DownloadButton mDownloadButton;
    private AppBriefBean mApp;
    private FrameLayout mfl_number_single_item;
    public RelativeLayout rootLayout;
    private AppEntity entitys;
    private int position;
    private ViewStub viewStub_number;
    private ViewStub viewStub_iv_mark;

    public ItemSingleAppView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ItemSingleAppView(Context context, String pageName, String id) {
        super(context, pageName, id);
        init();
        initWeakView();
    }

    private void initWeakView() {
        new WeakView<ItemSingleAppView>(this) {
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
                if (entitys.getPackageName().equals(removeEvent.mAppEntity.getPackageName())) {
                    //更新界面i
                    entitys.setStatus(DownloadStatusDef.INVALID_STATUS);
                    mDownloadButton.setData(entitys, mPageName);
                }
            }
        };
    }

    @Override
    public void fillView(ItemData<? extends BaseBean> bean, int position) {

        this.position = position;
        mItemData = bean;
        this.mApp = (AppBriefBean) bean.getmData();

        if (mApp.isPositionLast) {
            ((LayoutParams) rootLayout.getLayoutParams()).bottomMargin = getResources().getDimensionPixelOffset(R.dimen.padding_eight_dp);
        } else {
            ((LayoutParams) rootLayout.getLayoutParams()).bottomMargin = 0;
        }

        if (!TextUtils.isEmpty(mApp.getCorner_url())) {
            showMarkIv();
        } else {
            if(mMarkIv != null) {
                mMarkIv.setVisibility(View.GONE);
            }
        }

        try {
            ImageloaderUtil.loadImage(getContext(), mApp.getIcon_url(), mLogoIv);
        } catch (Exception e) {
            mLogoIv.setImageResource(R.mipmap.app_default);
        }

        setName_count_size_describe();

        try {
            /************这个方法也调用了远程，耗时**************/
            if (this.mApp != null) {
                entitys = this.mApp.getDownloadAppEntity();
                if (entitys == null) {
                    entitys = DownloadTaskManager.getInstance().transfer(this.mApp);
                }

                LogUtils.i("Erosion","p1====="  +mApp.p1 + ".p2======" + mApp.p2 + ",themeType=="  + themeType);
                if (mApp.p1 == 0 && mApp.p2 == 0) {
                    if (Constant.PAGE_SEARCH_RESULT.equals(mPageName) || Constant.PAGE_SEARCH_ADS.equals(mPageName)
                            || Constant.PAGE_SEARCH_AUTOMATCH.equals(mPageName)) {
                        entitys.p1 = 0;
                        entitys.p2 = position + 1;
                    } else if (Constant.PAGE_SPECIAL_DETAIL.equals(mPageName)) {
                        entitys.p1 = 0;
                        entitys.p2 = position;
                    } else {
                        entitys.p1 = 0;
                        entitys.p2 = position + 3;
                    }
                } else {
                    entitys.p1 = mApp.p1;
                    entitys.p2 = mApp.p2;
                }
                entitys.resource_type = "app";
                mDownloadButton.setData(entitys, mPageName, mID);
            } else {
                WanKaLog.e("mApp 为空");
            }

        } catch (Exception e) {
            LogUtils.i("MemoryLow", "异常信息：" + e.getMessage());
            e.printStackTrace();
        }

        rootLayout.setTag(R.id.click_type, position);
        if (!"2".equals(themeType)) {
            createNumberLayout();
            mRankNumberView.setText("" + (position + 3));
            rootLayout.setTag(R.id.click_date, position + 3);
        } else {
            if(mfl_number_single_item != null) {
                mfl_number_single_item.setVisibility(GONE);
            }
        }
    }

    private void showMarkIv() {
        if(mMarkIv == null) {
            mMarkIv = (ImageView) viewStub_number.inflate();
        }
        mMarkIv.setVisibility(View.VISIBLE);
        ImageloaderUtil.loadImage(getContext(), mApp.getCorner_url(), mMarkIv);
    }

    /** 设置 应用名、下载数、大小、小编点评*/
    private void setName_count_size_describe() {
        // 应用名
        String appName = getAppName();

        // 下载数、大小、小编点评
        String count_size_describe = IntegratedDataUtil.calculateCounts(TextUtils.isEmpty(mApp.getDownload_count()) ? 0 : Integer.valueOf(mApp.getDownload_count()))
                                    + "     " + IntegratedDataUtil.calculateSizeMB(Long.valueOf(mApp.getPackage_size()))
                                    + "\n" + (TextUtils.isEmpty(mApp.getReviews()) ? "" : mApp.getReviews());

        // 设置下载数、大小、小编点评 的字体和颜色（与应用名不一样）
        SpannableStringBuilder style = new SpannableStringBuilder(appName);
        style.append("\n");
        style.append(count_size_describe);
        style.setSpan(new AbsoluteSizeSpan(DensityUtil.dip2px(getContext(), 12f)), appName.length(), appName.length() + count_size_describe.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.app_size_color)), appName.length(), appName.length() + count_size_describe.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        name_count_size_describeTV.setText(style);
    }

    private String getAppName() {
        String finalAppName = "";
        try {
            String tempName = TextUtils.isEmpty(mApp.getAlias()) ? mApp.getName() : mApp.getAlias();

            if (JudgeChineseUtil.isChineseChar(tempName)) {

                if (JudgeChineseUtil.isContainsEnglish(tempName.trim())) {
                    finalAppName = tempName.length() <= 14 ? tempName :tempName.substring(0, 14).concat("...");
                } else {
                    finalAppName = tempName.length() <= 10 ? tempName : tempName.substring(0, 10).concat("...");
                }
            } else {
                finalAppName = tempName.length() <= 20 ? tempName : tempName.substring(0, 20).concat("...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return finalAppName;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_item_app_single2, this);
        rootLayout = (RelativeLayout) findViewById(R.id.body);
        rootLayout.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        mLogoIv = (ImageView) findViewById(R.id.iv_logo_app_item);
        name_count_size_describeTV = (TextView) findViewById(R.id.tv_name_count_size_describe);
        mDownloadButton = (DownloadButton) findViewById(R.id.download_button);
        // 下载按钮区域设置监听
        findViewById(rl_DownBtn).setOnClickListener(this);

        viewStub_number = (ViewStub) findViewById(R.id.viewStub_number);
        viewStub_iv_mark = (ViewStub) findViewById(R.id.viewStub_iv_mark);
        rootLayout.setOnClickListener(this);
        if ("rank".equals(mID)) {
            try {
                themeType = GlobalParams.getHostBean().getSettings().getRank_style();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!"2".equals(themeType)) {
                createNumberLayout();
                mfl_number_single_item.setVisibility(VISIBLE);
                //设置icorginleft
                RelativeLayout.LayoutParams layoutParam = new RelativeLayout.LayoutParams(mLogoIv.getLayoutParams());
                layoutParam.setMargins((DensityUtil.dip2px(getContext(), 39)), 0, 0, 0);
                mLogoIv.setLayoutParams(layoutParam);
            }
        }


    }

    private void createNumberLayout() {
        if(mfl_number_single_item == null) {
            mfl_number_single_item = (FrameLayout) viewStub_number.inflate();
            mRankNumberView = (TextView) mfl_number_single_item.findViewById(R.id.tv_number_single_item);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_DownBtn:
                mDownloadButton.dealClick(v);
                break;
            case R.id.body:
                int pos = (int) v.getTag(R.id.click_date);
                UIController.goAppDetail(getContext(), mApp.isAdData(), mApp.getAdMold(), mApp.getAppClientId(), mApp.getPackage_name(), mApp.getApps_type(), mPageName, mApp.getCategory(), mApp.getDownload_url(), mApp.getReportData());
                if (mfl_number_single_item != null && mfl_number_single_item.getVisibility() == View.VISIBLE) {
                    StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(mItemData.getmPresentData(), mApp.isAdData(), pos, mApp.getAppClientId(), mPageName, null);
                    eventData.setAd_type(mApp.getAdMold());
                    DCStat.clickEvent(eventData);
                } else {
                    int p2;
                    if (Constant.PAGE_SEARCH_RESULT.equals(mPageName) || Constant.PAGE_SEARCH_ADS.equals(mPageName) || Constant.PAGE_SEARCH_AUTOMATCH.equals(mPageName)) {
                        p2 =pos -2;
                    } else if (Constant.PAGE_SPECIAL_DETAIL.equals(mPageName)) {
                        p2 =pos -3;
                    } else {
                        p2 = mApp.p2;
                    }
                    PresentData presentData = new PresentData();
                    presentData.setmType(PresentType.app);
                    presentData.setPos(mApp.p1);
                    StatisticsEventData eventData = StatisticsDataProductorImpl.produceStatisticsData(presentData, mApp.isAdData(), p2, mApp.getAppClientId(), mPageName, null);
                    eventData.setAd_type(mApp.getAdMold());
                    DCStat.clickEvent(eventData);
                }
                break;
        }
    }

}
