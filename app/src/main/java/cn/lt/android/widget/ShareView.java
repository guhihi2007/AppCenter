package cn.lt.android.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.umsharesdk.OneKeyShareUtil;
import cn.lt.android.umsharesdk.ShareBean;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;


/**
 * Created by atian
 */
public class ShareView extends LinearLayout implements View.OnClickListener {
    private static final int SUCCESS = 0;
    private static final int FAILED = 1;
    private static final int CANCEL = 2;
    private TextView share_wechatIv;// 微信分享
    private TextView share_pyqIv;// 朋友圈分享
    private TextView share_sinaIv;// 新浪分享
    private TextView share_qqIv;// QQ分享
    private shareViewOnclick onclick;
    private AppDetailBean app;
    private OneKeyShareUtil.ShareType shareType;
    private Activity activity;
    private ShareBean shareBean;

    public ShareView(Context context) {
        super(context);
    }

    public ShareView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        initView();
    }

    public ShareView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initListener() {
        share_pyqIv.setOnClickListener(this);
        share_sinaIv.setOnClickListener(this);
        share_qqIv.setOnClickListener(this);
        share_wechatIv.setOnClickListener(this);

    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shareview, this);
        share_pyqIv = (TextView) findViewById(R.id.share_pyqIv);
        share_sinaIv = (TextView) findViewById(R.id.share_sinaIv);
        share_qqIv = (TextView) findViewById(R.id.share_qqIv);
        share_wechatIv = (TextView) findViewById(R.id.share_wechatIv);
        initListener();
    }

    public ShareView setShareBean(ShareBean shareBean) {
        this.shareBean = shareBean;
        this.app = shareBean.getApp();
        this.shareType = shareBean.getShareType();
        return this;
    }

    // 友盟分享必须使用activity
    public ShareView setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    @Override
    public void onClick(View view) {

        if (!NetUtils.isConnected(view.getContext())) {
            ToastUtils.showToast("请检查网络");
            return;
        }

        /* 拼接数据上报文字*/
        String reportString = "";
        if(app != null) {
            reportString = "(包名)：" + app.getPackage_name();
        } else {
            if(shareBean.getShareType() == OneKeyShareUtil.ShareType.activities) {
                reportString = "(活动分享链接)：" + shareBean.getShareLink();
            } else {
                reportString = "（应用市场分享）";
            }
        }

        switch (view.getId()) {
            case R.id.share_pyqIv:
                OneKeyShareUtil.getInstance(activity, shareType).shareWeiXin_Circle(shareBean);
                DCStat.baiduStat(view.getContext(), "share", "微信朋友圈分享" + reportString);
                break;
            case R.id.share_sinaIv:
                OneKeyShareUtil.getInstance(activity, shareType).shareSinaWeiBo(shareBean);
                DCStat.baiduStat(view.getContext(), "share", "新浪分享" + reportString);
                break;
            case R.id.share_qqIv:
                OneKeyShareUtil.getInstance(activity, shareType).shareQQ(shareBean);
                DCStat.baiduStat(view.getContext(), "share", "QQ分享" + reportString);
                break;
            case R.id.share_wechatIv:
                OneKeyShareUtil.getInstance(activity, shareType).shareWeiXin(shareBean);
                DCStat.baiduStat(view.getContext(), "share", "微信分享" + reportString);
                break;

            default:
                break;
        }
        onclick.shareOnClick(view);

    }

    public interface shareViewOnclick {
        void shareOnClick(View view);
    }


    public void setOnclick(shareViewOnclick onclick) {
        this.onclick = onclick;
    }
}
