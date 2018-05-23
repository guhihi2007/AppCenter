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
 * @desc 用户登录/注册/修改密码页面
 */
public class AccountCenterActivity extends BaseAppCompatActivity {

    private  FragmentManager mFragmentManger;
    public ActionBar mActionBar;
    private Fragment mCurrentFragment;
    private LoginFragment mLoginFragment;
    private RegisterFragment mRegisterFragment;
    private AccountManageFragment mAccountFragment;
    private GetBackPwdFragment mGetBackPwdFragment;
    private SetNewPwdFragment mSetNewPwdFragment;
    private SetNickNameFragment mSetNickNameFragment;
    private AgreementFragment agreementFragment;

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
    private void setFragment(){
        String type = getIntent().getStringExtra(Constant.TYPE);
        if (Constant.USER_LOGIN.equals(type)){
            mActionBar.setTitle("登录");
            mLoginFragment  = new LoginFragment();
            switchFragments(mLoginFragment);
        }else if(Constant.USER_REGISTER.equals(type)){
            mActionBar.setTitle("注册");
            mRegisterFragment = new RegisterFragment();
            switchFragments(mRegisterFragment);
        }else if(Constant.USER_INFO.equals(type)){
            mActionBar.setTitle("账户管理");
            mAccountFragment =  new AccountManageFragment();
            switchFragments(mAccountFragment);
        }else if(Constant.GET_BACK_PWD.equals(type)){
            mActionBar.setTitle("忘记密码");
            mGetBackPwdFragment =  new GetBackPwdFragment();
            switchFragments(mGetBackPwdFragment);
        }else if(Constant.RESET_PWD.equals(type)){
            mActionBar.setTitle("设置新密码");
            mSetNewPwdFragment =  new SetNewPwdFragment();
            switchFragments(mSetNewPwdFragment);
        }else if (Constant.SETNICKNAME.equals(type)){
            mActionBar.setTitle("设置昵称");
            mSetNickNameFragment = new SetNickNameFragment();
            switchFragments(mSetNickNameFragment);
        }else if (Constant.USER_AGREEMENT.equals(type)) {
            mActionBar.setTitle("用户协议");
            agreementFragment = new AgreementFragment();
            switchFragments(agreementFragment);
        }
    }
    /***
     * 切换Fragment
     *
     * @param fragment
     */
    private void switchFragments(Fragment fragment) {
        mFragmentManger = getSupportFragmentManager();
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
