package cn.lt.android.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.lt.android.GlobalConfig;
import cn.lt.android.download.DownloadRedPointManager;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UserRedPointManager;
import cn.lt.android.event.InstallEvent;
import cn.lt.android.install.InstallRedPointManager;
import cn.lt.android.main.UIController;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.umsharesdk.ShareBean;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ViewUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.ShareHolder;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * Created by atian on 15/4/1.
 *
 * @desc 导航栏公用
 */
public class ActionBar extends LinearLayout implements OnClickListener {
    private ImageView iv_share;
    private Context mContext;
    private TextView mTitle;
    private TextView mAutoBoard;
    private TextView mPoint;
    private TextView mUpdatePoint;
    private ShareBean shareBean;
    private OnClickListener iv_BackOnClickListener;

    private String page;
    private UserRedPointManager.Callback userRedPointCallback;
    private DownloadRedPointManager.Callback installRedPointCallback;
    private DownloadRedPointManager.Callback downloadRedPointCallback;
    private String searchAdsId;

    public String getPageName() {
        return page;
    }

    public void setPageName(String page) {
        this.page = page;
    }

    public ActionBar(Context context) {
        super(context);
        this.mContext = context;
    }

    public void setAutoBoard(String value, String searchAdsId) {
        if (mAutoBoard != null) mAutoBoard.setText(value);
        this.searchAdsId = searchAdsId;
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBarStyle);
        int i = a.getInt(R.styleable.ActionBarStyle_MyActionBar, 0);
        this.mContext = context;
        setActionBarStyle(context, i);
        a.recycle();
        EventBus.getDefault().register(this);
        if (mTitle != null) {
            mTitle.setMaxLines(1);
            mTitle.setEllipsize(TextUtils.TruncateAt.END);
        }
        /***
         * 初始化右侧小红点是否需要显示
         */
        if (DownloadRedPointManager.getInstance().isNeedShow() || InstallRedPointManager.getInstance().isNeedShow()) {
            initDownloadRedPoint();
        }
        downloadRedPointCallback = new DownloadRedPointManager.Callback() {
            @Override
            public void showRedPoint() {
                GlobalConfig.setIsOnClick(mContext, false);
                if (mPoint != null) {
                    mPoint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void hideRedPoint() {
                if (mPoint != null) {
                    mPoint.setVisibility(View.GONE);
                }
            }
        };
        DownloadRedPointManager.getInstance().register(downloadRedPointCallback);
        installRedPointCallback = new DownloadRedPointManager.Callback() {
            @Override
            public void showRedPoint() {
                GlobalConfig.setIsOnClick(mContext, false);
                if (mPoint != null) {
                    mPoint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void hideRedPoint() {
                if (mPoint != null) {
                    mPoint.setVisibility(View.GONE);
                }
            }
        };
        InstallRedPointManager.getInstance().register(installRedPointCallback);
        setUserRedPoint(UserRedPointManager.getInstance().isNeedShow() ? VISIBLE : GONE);
        userRedPointCallback = new UserRedPointManager.Callback() {
            @Override
            public void showRedPoint() {
                setUserRedPoint(VISIBLE);
            }

            @Override
            public void hideRedPoint() {
//                setUserRedPoint(GONE);
            }
        };
        UserRedPointManager.getInstance().register(userRedPointCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LogUtils.e("xxx", "ActionBar onDetachedFromWindow");

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        DownloadRedPointManager.getInstance().unregister(downloadRedPointCallback);
        InstallRedPointManager.getInstance().unregister(installRedPointCallback);
        UserRedPointManager.getInstance().unregister(userRedPointCallback);
    }

    /**
     * 设置ActionBar长啥样
     *
     * @param context
     * @param type
     */
    public void setActionBarStyle(Context context, int type) {
        switch (type) {
            /*搜索*/
            case 0:
                View.inflate(context, R.layout.search_titlebar_layout, this);
                findViewById(R.id.iv_back).setOnClickListener(this);
                break;
            /*详情*/
            case 1:
                View.inflate(context, R.layout.appdetail_titlebar_layout, this);
                findViewById(R.id.iv_back).setOnClickListener(this);
                findViewById(R.id.iv_download).setOnClickListener(this);
                findViewById(R.id.iv_share).setOnClickListener(this);
                findViewById(R.id.iv_search).setOnClickListener(this);
                mTitle = (TextView) findViewById(R.id.tv_title);
                mPoint = (TextView) findViewById(R.id.tv_point);
                iv_share = (ImageView) findViewById(R.id.iv_share);
                break;
            /*主页面*/
            case 2:
                View.inflate(context, R.layout.view_toolbar, this);
                findViewById(R.id.input_content_layout).setOnClickListener(this);
                findViewById(R.id.download_manager).setOnClickListener(this);
                mAutoBoard = (TextView) findViewById(R.id.tv_auto_board);
                mPoint = (TextView) findViewById(R.id.tv_point);//右侧小红点
                break;
            /*分类、专题页面*/
            case 3:
                View.inflate(context, R.layout.other_titlebar_layout, this);
                mTitle = (TextView) findViewById(R.id.tv_title);
                findViewById(R.id.iv_back).setOnClickListener(this);
                findViewById(R.id.iv_download).setOnClickListener(this);
                findViewById(R.id.iv_search).setOnClickListener(this);
                mPoint = (TextView) findViewById(R.id.tv_point);
                break;
            /*个人中心*/
            case 4:
                View.inflate(context, R.layout.percenter_titlebar_layout, this);
                mTitle = (TextView) findViewById(R.id.tv_title);
                findViewById(R.id.iv_download).setOnClickListener(this);
                findViewById(R.id.iv_back).setOnClickListener(this);
                mPoint = (TextView) findViewById(R.id.tv_point);
                break;
            /*其他页面*/
            case 5:
                View.inflate(context, R.layout.def_titlebar_layout, this);
                mTitle = (TextView) findViewById(R.id.tv_title);
                findViewById(R.id.iv_back).setOnClickListener(this);
                break;
        }
    }

    /***
     * 接收安装完成事件，用于右侧小红点消失
     *
     * @param event
     */
    public void onEventMainThread(InstallEvent event) {
        int downloadTaskCount = 0;
        if (mPoint != null) {
            downloadTaskCount = DownloadTaskManager.getInstance().getInstallTaskList().size();
            if (event.getType() == InstallEvent.INSTALLED_ADD) if (downloadTaskCount == 1) {
                mPoint.setVisibility(GONE);
            }
        }
    }

    /***
     * 初始化小红点是否可见
     */
    public void initDownloadRedPoint() {
        boolean isOnClick = GlobalConfig.getIsOnClick(mContext);
        if (mPoint != null) {
            if (!isOnClick) {
                mPoint.setVisibility(View.VISIBLE);
            } else {
                mPoint.setVisibility(View.GONE);
            }
        }
    }

    public void setUserRedPoint(int visiable) {
        if (mUpdatePoint != null) {
            mUpdatePoint.setVisibility(visiable);
        }
    }

    /***
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        if (mTitle == null) return;
        if (title == null) {
            return;
        }
        mTitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        if (ViewUtils.isFastClick()) return;
        switch (v.getId()) {
            case R.id.iv_back:
                if (iv_BackOnClickListener != null) {
                    iv_BackOnClickListener.onClick(v);
                } else {
                    ((Activity) getContext()).finish();
                }
                break;
            case R.id.iv_share:
                new PublicDialog(v.getContext(), new ShareHolder()).showDialog(new DataInfo(shareBean));
                break;
            case R.id.iv_download:
            case R.id.download_manager:
                UIController.goDownloadTask(mContext);
                GlobalConfig.setIsOnClick(mContext, true);
                mPoint.setVisibility(View.GONE);
                if (null != onJumpListener) {
                    onJumpListener.jumpByRight2Left();
                }
                break;

            case R.id.iv_search:
                UIController.goSearchActivity(mContext, "", false, getPageName(), "");
                if (null != onJumpListener) {
                    onJumpListener.jumpByRight2Left();
                }
                DCStat.baiduStat(mContext, "onclick", "搜索按钮点击事件(页面名称):" + getPageName());   //百度统计
                break;
            case R.id.input_content_layout:
                String adsStr = mAutoBoard.getText().toString().trim();
                UIController.goSearchActivity(mContext, adsStr, true, getPageName(), searchAdsId);
                if (null != onJumpListener) {
                    onJumpListener.jumpByRight2Left();
                }
                break;
        }
    }


    /**
     * 设置分享按钮是否可以点击
     */
    public void setShareViewEnable(boolean enable) {
        iv_share.setEnabled(enable);
    }

    public void setShareBean(ShareBean shareBean) {
        this.shareBean = shareBean;
    }

    private JumpListener onJumpListener;

    public interface JumpListener {
        void jumpByRight2Left();
    }

    public void setOnJumpListener(JumpListener onJumpListener) {
        this.onJumpListener = onJumpListener;
    }

    public void setIv_BackOnClickListener(OnClickListener onClickListener) {
        this.iv_BackOnClickListener = onClickListener;
    }


    public void refreshToolBarRedPoint(int visible) {
        if (mPoint != null) {
            mPoint.setVisibility(visible);
        }
    }
}
