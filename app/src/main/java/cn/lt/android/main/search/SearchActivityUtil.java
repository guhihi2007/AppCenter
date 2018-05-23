package cn.lt.android.main.search;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.db.SearchHistoryEntity;
import cn.lt.android.db.SearchHistoryEntityDao;
import cn.lt.android.entity.SearchHistoryBean;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;

/**
 * Created by atian on 2016/1/21.
 * SearchActivity的辅助类(用于简化搜索主页面)
 */
public class SearchActivityUtil implements View.OnClickListener {
    private FragmentActivity mActivity;
    private SearchAdvFragment mAdvFragement;
    private SearchResultFragment mResultFragment;
    private SearchAutoMatchFragment mSearchAutoMatchFragment;
    private View mView;
    private MyWatcher watcher;
    private String keyword = "";
    private EditText mEditText;
    private ImageView mDelete;
    private ImageView mSearch;
    private View mDivider;
    private ImageView mBack;
    public static int returnType = 0;
    public static final int SEARCHADV = 0;
    public static final int SEARCHNODATA = 1;
    public static final int SEARCHRESULT = 2;
    public static final int SEARCHAUTOMATCH = 3;
    public static final int SEARCHADS = 4;
    private boolean isAds;


    public SearchActivityUtil(FragmentActivity mActivity) {
        this.mActivity = mActivity;
    }

    public void onCreate(View view) {
        this.mView = view;
        mEditText = (EditText) mView.findViewById(R.id.et_searchcontent);
        mDelete = (ImageView) mView.findViewById(R.id.iv_del);
        mDivider = mView.findViewById(R.id.v_divider);
        mSearch = (ImageView) mView.findViewById(R.id.iv_search);
        mBack = (ImageView) mView.findViewById(R.id.iv_back);
        watcher = new MyWatcher();
        mEditText.addTextChangedListener(watcher);
        mEditText.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    toSearch(isAds);
                    return true;
                } else {
                    return false;

                }
            }
        });


    }

    public void setHintText(String value) {
        mEditText.setHint(value);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_search:
                toSearch(isAds);
                break;
            case R.id.iv_del:
                mEditText.setText("");
                getHistoryList(keyword);
                showKeyboard();
                break;
            case R.id.iv_back:
                if (returnType == SEARCHRESULT) {
                    mEditText.setText("");
                } else {
                    mActivity.finish();
                }
            case R.id.et_searchcontent:

                break;
            default:
                break;
        }
    }

    private void toSearch(boolean isAds) {
        if (isAds) {
            String intputHintStr = mEditText.getHint().toString().trim();
            search(intputHintStr);
        } else {
            String inputStr = mEditText.getText().toString().trim();
            if (checkInputValue(inputStr)) {
                search(inputStr);
            }
        }
    }

    private void search(String value) {
//        DCStat.searchEvent(value, getPageName(returnType),"");
        returnType = SEARCHRESULT;
        switchFragments(SEARCHRESULT, false, value,"","");
        setmEditText(value);
        saveSearchValue(value);
        hideKeyboard();
    }

    private String getPageName(int returnType) {
        switch (returnType) {
            case SEARCHADS:
                return Constant.PAGE_SEARCH_ADS;
            case SEARCHADV:
                return Constant.PAGE_SEARCH_ADV;
            case SEARCHAUTOMATCH:
                return Constant.PAGE_SEARCH_AUTOMATCH;
            case SEARCHRESULT:
            case SEARCHNODATA:
                return Constant.PAGE_SEARCH_RESULT;
        }
        return "";
    }

    // 隐藏键盘
    public boolean hideKeyboard() {
        mSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean active = imm.isActive(mEditText);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        mEditText.clearFocus();
        return active;
    }

    /**
     * 显示键盘
     */
    public void showKeyboard() {
        mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mEditText, 0);
            }
        }, 100);
    }

    /***
     * 保存一个搜索记录
     *
     * @param value
     */
    public static void saveSearchValue(String value) {
        SearchHistoryEntityDao searcgDao = getHistoryDao();
        SearchHistoryEntity entity = new SearchHistoryEntity(null, value);
        List<SearchHistoryEntity> list = searcgDao.queryBuilder().where(SearchHistoryEntityDao.Properties.Title.eq(value)).list();
        if (list.size() == 0) {
            searcgDao.insert(entity);
        }
    }

    /***
     * 返回文本框的 内容
     *
     * @return
     */
    public String getSeachValue() {
        return keyword;
    }

    /***
     * 获取搜索历史记录列表
     */
    public static List<SearchHistoryBean> getHistoryList(String keyword) {
        List<SearchHistoryBean> list = new ArrayList<>();
        Cursor cursor = GlobalParams.getDb().rawQuery("SELECT * FROM SEARCH_HISTORY_ENTITY WHERE TITLE LIKE ? ORDER BY _ID DESC ", new String[]{"%" + keyword + "%"});
        while (cursor.moveToNext()) {
            String key = cursor.getString(1);
            list.add(new SearchHistoryBean(key));
        }
        return list;
    }

    /***
     * 根据条件删除记录
     */
    public static void deleteHistoryDataByTitle(String title) {
        if (TextUtils.isEmpty(title)) return;
        SearchHistoryEntity historyEntity = findByTitle(title);
        if (historyEntity != null) {
            getHistoryDao().delete(historyEntity);
        }
    }

    private static SearchHistoryEntityDao getHistoryDao() {
        return GlobalParams.getSearchHisoryEntityDao();
    }

    private static SearchHistoryEntity findByTitle(String title) {
        return getHistoryDao().queryBuilder().where(SearchHistoryEntityDao.Properties.Title.eq(title)).unique();
    }

    /***
     * 检查输入框内容是否为空
     *
     * @param val
     * @return
     */
    private boolean checkInputValue(String val) {
        if (!TextUtils.isEmpty(val)) {
            return true;
        } else {
            cn.lt.android.util.ToastUtils.showToast("请输入关键字");
        }
        return false;
    }

    /***
     * 设置输入框内容
     *
     * @param value
     */
    public void setmEditText(String value) {
        mEditText.setText(value);
        mEditText.setTextColor(mActivity.getResources().getColor(R.color.white));
        mEditText.setSelection(value.length());
    }

    /***
     * 监听搜索输入框
     */
    public class MyWatcher implements TextWatcher {
        private CharSequence cs;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            keyword = s.toString();
            LTApplication.instance.word = keyword;
            LogUtils.i("Erosion","instance.word===" + LTApplication.instance.word  + ",keywor===" + keyword);
            cs = s;
            if (!TextUtils.isEmpty(keyword) && keyword.length() > 0) {
                mDelete.setVisibility(View.VISIBLE);
                mDivider.setVisibility(View.VISIBLE);
                if (returnType != SEARCHRESULT) {
                    returnType = SEARCHAUTOMATCH;
                    switchFragments(SEARCHAUTOMATCH, false, keyword,"","");
                }
            } else {
                returnType = SEARCHADV;
                switchFragments(SEARCHADV, false, "","","");
                mDelete.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            int editStart = mEditText.getSelectionStart();
            int editEnd = mEditText.getSelectionEnd();
            if (cs.length() >= 50) {
                ToastUtils.showToast("您输入的内容已超过限制！");
                s.delete(editStart - 1, editEnd);
            }
        }
    }

    /***
     * 切换fragment
     *
     * @param flag
     * @param keyword
     */
    public void switchFragments(int flag, boolean isAds, String keyword,String pagePP,String searchAdsId) {
        this.isAds = isAds;
        switch (flag) {
            case SEARCHADV:
                returnType = SEARCHADV;
                mAdvFragement = new SearchAdvFragment();
                setHintText("请输入关键字");
                Bundle bundle1 = mAdvFragement.getArguments();
                if (bundle1 == null) {
                    bundle1 = new Bundle();
                    mAdvFragement.setArguments(bundle1);
                }
                showKeyboard();
                bundle1.putInt("fragmentType", SEARCHADV);
                attatchFragment(mAdvFragement);
                break;
            case SEARCHRESULT:
                if (isAds) {
                    returnType = SEARCHADS;
                    setHintText(keyword);
                } else {
                    returnType = SEARCHRESULT;
                }
                setmEditText(keyword);
                mResultFragment = new SearchResultFragment();
                hideKeyboard();
                Bundle bundle2 = mResultFragment.getArguments();
                if (bundle2 == null) {
                    bundle2 = new Bundle();
                    mResultFragment.setArguments(bundle2);
                }
                bundle2.putBoolean("isAds", isAds);
                bundle2.putString("keyWord", keyword);
                bundle2.putString("searchAdsId",searchAdsId);
                attatchFragment(mResultFragment);

                // 非广告词需单独上报搜索事件
                if(!isAds) {
                    DCStat.searchEvent(keyword, getPageName(returnType),pagePP);
                }
                break;
            case SEARCHNODATA:
                returnType = SEARCHNODATA;
                setHintText("请输入关键字");
                mAdvFragement = new SearchAdvFragment();
                Bundle bundle3 = mAdvFragement.getArguments();
                if (bundle3 == null) {
                    bundle3 = new Bundle();
                    mAdvFragement.setArguments(bundle3);
                }
                bundle3.putInt("fragmentType", SEARCHNODATA);
                attatchFragment(mAdvFragement);
                break;
            case SEARCHAUTOMATCH:
                returnType = SEARCHAUTOMATCH;
                mSearchAutoMatchFragment = new SearchAutoMatchFragment();
                Bundle bundle4 = mSearchAutoMatchFragment.getArguments();
                if (bundle4 == null) {
                    bundle4 = new Bundle();
                    mSearchAutoMatchFragment.setArguments(bundle4);
                }
                showKeyboard();
                bundle4.putString("keyWord", keyword);
                attatchFragment(mSearchAutoMatchFragment);
                break;
            default:
                break;

        }

    }

    private void attatchFragment(Fragment fragment) {
        FragmentManager mFragmentManger = mActivity.getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManger.beginTransaction();
//        setFragmentTransitionAnimation(ft);
        ft.replace(R.id.fl_content, fragment).commit();
    }

    /**
     * 设置fragment的转场动画
     * @param ft
     */
    private void setFragmentTransitionAnimation(FragmentTransaction ft) {
        // 标准动画
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);  //渐变效果
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

       // 自定义动画---左右滑
//        ft.setCustomAnimations(
//                R.anim.push_left_in,
//                R.anim.push_left_out,
//                R.anim.push_left_in,
//                R.anim.push_left_out);
    }

    /***
     * 在Activity需要实现该接口通过此接口与它所在的Activity交互
     */
    public interface SearchCallBack extends Serializable {

        void gotoNoDataFragment();// 跳转到无搜索结果页面回调

        void gotoSearchResultFragment(String keyword,String pageName);//跳转到搜索结果页面回调

        void gotoAdverFragment();//跳转到搜索推荐页面回调
    }
}
