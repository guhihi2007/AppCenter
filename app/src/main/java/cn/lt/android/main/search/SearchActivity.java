package cn.lt.android.main.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.db.AppEntity;
import cn.lt.android.install.InstalledLooperProxy;
import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;


/**
 * Created by atian on 2016/1/21.
 * 搜索主页面
 */
public class SearchActivity extends BaseAppCompatActivity implements SearchActivityUtil.SearchCallBack {
    private SearchActivityUtil mSearchUtil;
    private String searchAds;
    private String pageName;
    private boolean isAds;
    private String searchAdsId;

    public String getPageName() {
        return pageName;
    }

    @Override
    public void setPageAlias() {

    }

    @Override
    protected void onResume() {
        if (TextUtils.isEmpty(searchAds) && !"请输入关键字".equals(searchAds) && TextUtils.isEmpty(mSearchUtil.getSeachValue())) {
            SearchActivityUtil.returnType = SearchActivityUtil.SEARCHADV;
//            mSearchUtil.switchFragments(SearchActivityUtil.SEARCHADV, false, "");
        }

        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_search, null);
        setStatusBar();
        getIntentData();
        setContentView(view);
        if (mSearchUtil == null) {
            mSearchUtil = new SearchActivityUtil(this);
            mSearchUtil.onCreate(view);
        }
        if (!TextUtils.isEmpty(searchAds) && !"请输入关键字".equals(searchAds)) {
            SearchActivityUtil.returnType = SearchActivityUtil.SEARCHADS;
            mSearchUtil.switchFragments(SearchActivityUtil.SEARCHRESULT, isAds, searchAds,"",searchAdsId);
        } else {
            SearchActivityUtil.returnType = SearchActivityUtil.SEARCHRESULT;
            mSearchUtil.switchFragments(SearchActivityUtil.SEARCHADV, false, "","","");
        }
    }

    private void getIntentData() {
        searchAds = getIntent().getStringExtra("searchAds");
        pageName = getIntent().getStringExtra("pageName");
        isAds = getIntent().getBooleanExtra("isAds", false);
        searchAdsId = getIntent().getStringExtra("searchAdsId");
    }

    @Override
    public void gotoNoDataFragment() {
        SearchActivityUtil.returnType = SearchActivityUtil.SEARCHNODATA;
        mSearchUtil.switchFragments(SearchActivityUtil.SEARCHNODATA, false, "","","");
        Log.i("SearchResultFragment", "跳转到无结果页面");

    }

    @Override
    public void gotoSearchResultFragment(String keyword, String page) {
        SearchActivityUtil.returnType = SearchActivityUtil.SEARCHRESULT;
        mSearchUtil.switchFragments(SearchActivityUtil.SEARCHRESULT, false, keyword,page,"");
        mSearchUtil.setmEditText(keyword);
        mSearchUtil.saveSearchValue(keyword);
    }

    @Override
    public void gotoAdverFragment() {
        SearchActivityUtil.returnType = SearchActivityUtil.SEARCHNODATA;
        mSearchUtil.switchFragments(SearchActivityUtil.SEARCHNODATA, false, "","","");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) {
            //需要重新检查该packageName是否安装了
            LogUtils.d("ccc", "SearchActivity中取消了==请求码"+requestCode);
            AppEntity appEntity = LTApplication.instance.normalInstallTaskLooper.get(requestCode);
            //移除轮询器中的监控任务
            InstalledLooperProxy.getInstance().removeLooperEntity();
        }
    }
}
