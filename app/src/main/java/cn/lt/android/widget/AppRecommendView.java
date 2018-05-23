package cn.lt.android.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.lt.android.db.AppEntity;
import cn.lt.android.entity.RecommendBean;
import cn.lt.android.main.UIController;
import cn.lt.android.main.download.DownloadButton;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/2/24.
 *
 * @desc 本周推荐游戏/猜你喜欢
 */
public class AppRecommendView extends RelativeLayout {
    private Context context;
    private ImageView mAppLogo;
    private TextView mAppName;
    private TextView mAppSize;
    private DownloadButton mDownload;
    private String mPageName;
    private int pos;
    private boolean isApp;

    public AppRecommendView(Context context, String pageName,int pos,boolean isApp) {
        super(context);
        this.mPageName = pageName;
        initView(context);
        this.context = context;
        this.pos = pos;
        this.isApp = isApp;
    }

    public AppRecommendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AppRecommendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.app_recommend_view, this);
        mAppLogo = (ImageView) findViewById(R.id.iv_app_icon);
        mAppName = (TextView) findViewById(R.id.tv_app_name);
        mAppSize = (TextView) findViewById(R.id.tv_app_size);
        mDownload = (DownloadButton) findViewById(R.id.recommend_downloadBtn);
    }

    public void setData(final RecommendBean bean) {
        try {
            ImageloaderUtil.loadRoundImage(getContext(), bean.getIcon_url(), mAppLogo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAppName.setText(TextUtils.isEmpty(bean.getAlias()) ? bean.getName() : bean.getAlias());
        mAppSize.setText(IntegratedDataUtil.calculateSizeMB(Long.parseLong(bean.getPackage_size())));

        AppEntity appEntity = bean.getAppEntity();
        appEntity.p1 = 0;
        appEntity.p2 = pos + 1;
        if (isApp) {
            appEntity.resource_type = "app_recommend";
        } else {
            appEntity.resource_type = "game_recommend";
        }
        mDownload.setData(appEntity, mPageName);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UIController.goAppDetail(v.getContext(), bean.isAdData(), "", bean.getAppClientId(), bean.getPackage_name(), bean.getApps_type(), mPageName, "", bean.getDownload_url(),bean.getReportData());
            }
        });
    }
}
