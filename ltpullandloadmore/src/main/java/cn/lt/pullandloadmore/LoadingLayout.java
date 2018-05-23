package cn.lt.pullandloadmore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.lt.pullandloadmore.util.BitmapUtils;


/**
 * Created by wenchao on 22/12/15.
 */
public class LoadingLayout extends FrameLayout {
    private OnClickListener onRetryClickListener;
    private RelativeLayout emptyView;
    public RelativeLayout loadingView;
    private ViewStub errorStub;
    private ViewStub notGoodStub;
    private ImageView iv_errorNoNetwork;
    private ImageView iv_errorNotGoodNetwork;
    private View errorNotGoodNetworkView;
    private View errorNetworkView;


    public LoadingLayout(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.network_state, this);
        initView();
    }

    public LoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.network_state, this);
        initView();
    }

    public LoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.network_state, this);
        initView();
    }

    private void initView() {
        emptyView = (RelativeLayout) findViewById(R.id.view_empty);
        loadingView = (RelativeLayout) findViewById(R.id.view_loading); //加载中视图
        errorStub = (ViewStub) findViewById(R.id.view_error_no_network);
        notGoodStub = (ViewStub) findViewById(R.id.view_error_not_good_network);
    }


    /**
     * 加载错误提示小猫的图片
     */
    private void loadCatImage() {
        Bitmap bitmap1 = BitmapUtils.readBitMap(getContext(), R.drawable.error);
        iv_errorNoNetwork.setImageBitmap(bitmap1);
        Bitmap bitmap2 = BitmapUtils.readBitMap(getContext(), R.drawable.error_02);
        iv_errorNotGoodNetwork.setImageBitmap(bitmap2);
        Log.i("smallCat", "errorNoNetwork 宽 = " + bitmap1.getWidth() + ", 高 = " + bitmap1.getHeight());
        Log.i("smallCat", "errorNotGoodNetwork 宽 = " + bitmap2.getWidth() + ", 高 = " + bitmap2.getHeight());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        emptyView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);

    }

    public void setOnRetryClickListener(OnClickListener onRetryClickListener) {
        this.onRetryClickListener = onRetryClickListener;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView.removeAllViews();
        this.emptyView.addView(emptyView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }


    public void showEmpty() {
        hideContent();
        emptyView.setVisibility(View.VISIBLE);
        hideErrorView();
        loadingView.setVisibility(View.GONE);
        hideNotGoodNetworkErrorView();
    }



    public void hideLoading() {
        loadingView.setVisibility(View.GONE);
    }


    public void showErrorNoNetwork() {
        hideContent();
        emptyView.setVisibility(View.GONE);
        showErrorView();
        loadingView.setVisibility(View.GONE);
        hideNotGoodNetworkErrorView();
    }

    public void showLoading() {
        hideContent();
        emptyView.setVisibility(View.GONE);
        hideErrorView();
        loadingView.setVisibility(View.VISIBLE);
        hideNotGoodNetworkErrorView();
    }

    public void showErrorNotGoodNetwork() {
        hideContent();
        emptyView.setVisibility(View.GONE);
        hideErrorView();
        loadingView.setVisibility(View.GONE);
        showNotGoodNetworkErrorView();
    }

    public void showContent() {
        emptyView.setVisibility(View.GONE);
        hideErrorView();
        loadingView.setVisibility(View.GONE);
        hideNotGoodNetworkErrorView();
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (i == 1) {
                child.setVisibility(VISIBLE);
            }
        }
    }

    private void hideContent() {
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (i == 1) {
                child.setVisibility(GONE);
            }
        }
    }

    private void showErrorView() {
        if(errorNetworkView == null) {
            errorNetworkView = errorStub.inflate();
            errorViewAddListener(errorNetworkView);
            loadErrorCat(errorNetworkView);
        } else {
            errorNetworkView.setVisibility(View.VISIBLE);
        }
    }



    private void showNotGoodNetworkErrorView() {
        if(errorNotGoodNetworkView == null) {
            errorNotGoodNetworkView = notGoodStub.inflate();
            notGoodViewAddListener(errorNotGoodNetworkView);
            loadNotGoodCat(errorNotGoodNetworkView);
        } else {
            errorNotGoodNetworkView.setVisibility(View.VISIBLE);
        }
    }

    private void errorViewAddListener(View view) {
        view.findViewById(R.id.btn_error_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onRetryClickListener) {
                    onRetryClickListener.onClick(v);
                }
            }
        });


        view.findViewById(R.id.btn_network_setting).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
    }

    private void notGoodViewAddListener(View view) {
        view.findViewById(R.id.btn_error_retry_02).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != onRetryClickListener) {
                    onRetryClickListener.onClick(v);
                }
            }
        });
    }

    private void loadErrorCat(View view) {
        iv_errorNoNetwork = (ImageView) view.findViewById(R.id.iv_errorNoNetwork);
        iv_errorNoNetwork.setImageBitmap(BitmapUtils.readBitMap(getContext(), R.drawable.error));
    }

    private void loadNotGoodCat(View view) {
        iv_errorNotGoodNetwork = (ImageView) view.findViewById(R.id.iv_errorNotGoodNetwork);
        iv_errorNotGoodNetwork.setImageBitmap(BitmapUtils.readBitMap(getContext(), R.drawable.error_02));
    }

    private void hideNotGoodNetworkErrorView() {
        if (errorNotGoodNetworkView != null) {
            errorNotGoodNetworkView.setVisibility(View.GONE);
        }
    }

    private void hideErrorView() {
        if (errorNetworkView != null) {
            errorNetworkView.setVisibility(View.GONE);
        }
    }

}
