package cn.lt.android.main.requisite.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.main.requisite.RequisiteActivity;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.appstore.R;

public class RequisiteGameView extends FrameLayout {

    @SuppressWarnings("unused")
    private Context mContext;

    /**
     * 游戏logo小图
     */
    private ImageView mLogoIv;

    private TextView mNameTv;// 游戏名

    private TextView mTagSizeTv;// 游戏标签和大小

    private RequisiteActivity.RequisiteItem mItem;

    private ImageView mCheckView;

    private RelativeLayout mRootView;

    public RequisiteGameView(Context context) {
        super(context);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.requisite_game_item, this);
        init();
    }

    public RequisiteGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RequisiteGameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        mRootView = (RelativeLayout) findViewById(R.id.root);
        mLogoIv = (ImageView) findViewById(R.id.logoIv);
        mNameTv = (TextView) findViewById(R.id.nameTv);
        mTagSizeTv = (TextView) findViewById(R.id.tagSizeTv);
        mCheckView = (ImageView) findViewById(R.id.cb_requisite_game_item);
    }

    public void fillView(RequisiteActivity.RequisiteItem item) {
        this.mItem = item;
        if (mItem != null) {
            AppDetailBean mGame = (AppDetailBean) mItem.getmGame();
            hasInstalledAndNeedToUpdate(mGame);
            switchCheckView(mItem.isChecked());
            if (mGame != null) {
                mNameTv.setText(mGame.getName());
                String size = mGame.getPackage_size();
                long iSize = 0;
                try {
                    iSize = Long.valueOf(size);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                mTagSizeTv.setText(IntegratedDataUtil.calculateSizeMB(iSize));
                String imgUrl = mGame.getIcon_url();
                ImageloaderUtil.loadImage(getContext(), imgUrl, mLogoIv);
            }
        }
    }


    private void hasInstalledAndNeedToUpdate(AppDetailBean appBean) {

        List<AppDetailBean> upgradeList = UpgradeListManager.getInstance().getUpgradeAppList();
        if (AppUtils.isInstalled(appBean.getPackage_name()) || AppUtils.isDownloadTask(appBean.getPackage_name())) {
            mItem.setChecked(false);
            mCheckView.setBackgroundResource(R.mipmap.requisite_no_check);
            mRootView.setClickable(true);
            mRootView.setEnabled(true);

            for (AppDetailBean bean : upgradeList) {
                if (bean.getPackage_name().equals(appBean.getPackage_name())) {
                    mItem.setChecked(true);
                    mCheckView.setBackgroundResource(R.mipmap.requisite_checked);
                    mRootView.setClickable(false);
                    mRootView.setEnabled(false);
                    break;
                } else {
                    mItem.setChecked(false);
                    mCheckView.setBackgroundResource(R.mipmap.requisite_no_check);
                    mRootView.setClickable(true);
                    mRootView.setEnabled(true);
                }
            }
        } else {
            mItem.setChecked(true);
            mCheckView.setBackgroundResource(R.mipmap.requisite_checked);
            mRootView.setClickable(false);
            mRootView.setEnabled(false);
        }
    }


    private void switchCheckView(boolean check) {
        if (check) {
            mCheckView.setBackgroundResource(R.mipmap.requisite_checked);
        } else {
            mCheckView.setBackgroundResource(R.mipmap.requisite_no_check);
        }
    }

    public void switchCheckView() {
        if (mItem.isChecked()) {
            mCheckView.setBackgroundResource(R.mipmap.requisite_no_check);
            mItem.setChecked(false);
        } else {
            mCheckView.setBackgroundResource(R.mipmap.requisite_checked);
            mItem.setChecked(true);
        }
    }

}