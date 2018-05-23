package cn.lt.android.main.requisite.state;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import cn.lt.android.Constant;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.main.entrance.Jumper;
import cn.lt.android.main.entrance.data.ClickType;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.appstore.R;

/***
 * Created by dxx on 2016/3/11.
 * 推广大图
 */
public class PopularizeState extends IState<AdsImageBean> {

    private View mCancelView;
    private ImageView mImageView;
    private AdsImageBean mBean;
    private Context context;

    public PopularizeState(Context context) {
        this.context = context;
    }

    @Override
    public void fillData(AdsImageBean info) {
        this.mBean = info;

    }

    private void initView() {
        mCancelView = mDiaglogWeak.get().findViewById(R.id.bt_cancel_requisite);
        mImageView = (ImageView) mDiaglogWeak.get().findViewById(R.id.iv_requiste_popularize);
        mRootView = mDiaglogWeak.get().findViewById(R.id.rl_root);
        mCancelView.setOnClickListener(this);
        mImageView.setOnClickListener(this);
        if (mBean != null) {
            ImageloaderUtil.loadBigImage(mImageView.getContext(),mBean.getImage(),mImageView);
        }
    }

    @Override
    public void setContentView(Dialog dialog) {
        this.mDiaglogWeak = new WeakReference<>(dialog);
        dialog.setContentView(R.layout.requisite_popularize);
        initView();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.bt_cancel_requisite:
                    break;
                case R.id.iv_requiste_popularize:
                    ClickType typeClic = ClickType.valueOf(mBean.getClick_type());
                    new Jumper().jumper(context, typeClic, mBean.getData(), Constant.PAGE_RECOMMEND, false);
                    break;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        mDiaglogWeak.get().cancel();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
