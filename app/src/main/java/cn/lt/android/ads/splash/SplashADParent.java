package cn.lt.android.ads.splash;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Erosion on 2018/3/28.
 */

abstract class SplashADParent {

    protected TextView mSkipVew;
    protected Activity mActivity;
    protected ViewGroup mContainer;
    protected ImageView appLogo;
    protected boolean isLoad;

    protected abstract void showAdView();

    public void setmSkipVew(TextView mSkipVew) {
        this.mSkipVew = mSkipVew;
    }

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void setmContainer(ViewGroup mContainer) {
        this.mContainer = mContainer;
    }

    public void setAppLogo(ImageView appLogo) {
        this.appLogo = appLogo;
    }

    public void setLoad(boolean load) {
        isLoad = load;
    }
}
