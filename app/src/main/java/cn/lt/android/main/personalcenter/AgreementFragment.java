package cn.lt.android.main.personalcenter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.appstore.R;

/**
 * Created by Administrator on 2016/5/11.
 */
public class AgreementFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_agreement, null);
        WebView webView = (WebView) rootView.findViewById(R.id.agreement);
        webView.loadUrl("file:///android_asset/agreement-8.html");
        return rootView;
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_USER_AGREENMENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }
}
