package cn.lt.android.main.personalcenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.socialize.UMShareAPI;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.umsharesdk.OneKeyShareUtil;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;

public class AboutUsActivity extends BaseAppCompatActivity {

    private ImageView iv_weiXin;// 微信分享
    private ImageView iv_friendQuan;// 微信朋友圈分享
    private ImageView iv_qq;// QQ分享
    private ImageView iv_sinaWeibo;// 新浪微博分享
    private ImageView iv_appIcon;
    private int clickAboutLogoCount = 0;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_ABOUT_US);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        setStatusBar();
        initView();

    }

    private void initView() {
        ((ActionBar) findViewById(R.id.ab_title)).setTitle("关于我们");
        TextView tv_verInfo = (TextView) findViewById(R.id.tv_verInfo);
        iv_appIcon = (ImageView) findViewById(R.id.iv_appIcon);
        iv_weiXin = (ImageView) findViewById(R.id.iv_weiXin);
        iv_friendQuan = (ImageView) findViewById(R.id.iv_friendQuan);
        iv_qq = (ImageView) findViewById(R.id.iv_qq);
        iv_sinaWeibo = (ImageView) findViewById(R.id.iv_sinaWeibo);

        tv_verInfo.setText("版本信息：" + GlobalConfig.versionName);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_weiXin:
                OneKeyShareUtil.getInstance(this, OneKeyShareUtil.ShareType.appCenter).shareWeiXin(null);
                DCStat.baiduStat(view.getContext(), "share", "微信分享");
                break;
            case R.id.iv_friendQuan:
                OneKeyShareUtil.getInstance(this, OneKeyShareUtil.ShareType.appCenter).shareWeiXin_Circle(null);
                DCStat.baiduStat(view.getContext(), "share", "微信朋友圈分享");
                break;
            case R.id.iv_qq:
                OneKeyShareUtil.getInstance(this, OneKeyShareUtil.ShareType.appCenter).shareQQ(null);
                DCStat.baiduStat(view.getContext(), "share", "QQ分享");
                break;
            case R.id.iv_sinaWeibo:
                OneKeyShareUtil.getInstance(this, OneKeyShareUtil.ShareType.appCenter).shareSinaWeiBo(null);
                DCStat.baiduStat(view.getContext(), "share", "新浪微博分享");
                break;
            case R.id.iv_appIcon:
                goBackDoor();
                break;
        }
    }

    private void goBackDoor() {
        clickAboutLogoCount++;
        if (clickAboutLogoCount == 6) {
            startActivity(new Intent(this, AppInfoBackDoorActivity.class));
            clickAboutLogoCount = 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** attention to this below ,must add this**/
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

    }

}
