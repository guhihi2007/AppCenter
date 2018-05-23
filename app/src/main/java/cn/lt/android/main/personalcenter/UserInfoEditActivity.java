package cn.lt.android.main.personalcenter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.widget.ActionBar;
import cn.lt.appstore.R;

/***
 * 账户管理页面
 */
public class UserInfoEditActivity extends BaseAppCompatActivity {
    private ActionBar mActionBar;
    private Fragment mCurrentFragment;
    private ModifyNickNameFragment mModifyNickNameFragment;
    private ModifyMobileFragment mModifyMobileFragment;
    private ModifyPwdFragment mModifyPwdFragment;

    @Override
    public void setPageAlias() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_center);
        setStatusBar();
        initView();
        setFragment();
    }

    private void initView() {
        mActionBar = (ActionBar) findViewById(R.id.title_bar);
    }

    private void setFragment() {
        String type = getIntent().getStringExtra(Constant.TYPE);
        if (Constant.MODIFY_NICKNAME.equals(type)) {
            mActionBar.setTitle("修改昵称");
            mModifyNickNameFragment = new ModifyNickNameFragment();
            switchFragments(mModifyNickNameFragment);
        } else if (Constant.MODIFY_MOBILE.equals(type)) {
            mActionBar.setTitle("修改手机号码");
            mModifyMobileFragment = new ModifyMobileFragment();
            switchFragments(mModifyMobileFragment);
        } else if (Constant.MODIFY_PWD.equals(type)) {
            mActionBar.setTitle("修改密码");
            mModifyPwdFragment = new ModifyPwdFragment();
            switchFragments(mModifyPwdFragment);
        }
    }

    /***
     * 切换Fragment
     *
     * @param fragment
     */
    private void switchFragments(Fragment fragment) {
        FragmentManager mFragmentManger = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentManger.beginTransaction();
        if (mCurrentFragment == null) {
            ft.add(R.id.fl_content, fragment).commit();
        } else {
            if (fragment.isAdded()) {
                ft.hide(mCurrentFragment).show(fragment).commit();
            } else {
                ft.hide(mCurrentFragment).add(R.id.fl_content, fragment).commit();
            }
        }
        mCurrentFragment = fragment;
    }
}
