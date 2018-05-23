package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/3/3.
 *
 * @desc 设置新密码
 */
public class SetNewPwdFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ImageView mDel, mDel02;
    private CheckBox mEye, mEye2;
    private EditText mPwd, mPwd02;
    private String resetCode;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_resetpwd, container, false);
            initView();
        }
        return mRootView;
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_SET_NEWPWD);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetCode = getActivity().getIntent().getStringExtra("resetCode");
        LogUtils.i("zzz", "ResetCode==" + resetCode);
    }

    private void initView() {
        mPwd = (EditText) mRootView.findViewById(R.id.et_new_pwd);
        mPwd02 = (EditText) mRootView.findViewById(R.id.et_password);
        mDel = (ImageView) mRootView.findViewById(R.id.iv_del);
        mDel02 = (ImageView) mRootView.findViewById(R.id.iv_del2);
        mEye = (CheckBox) mRootView.findViewById(R.id.iv_eye);
        mEye2 = (CheckBox) mRootView.findViewById(R.id.iv_eye2);
        mRootView.findViewById(R.id.tv_submit).setOnClickListener(this);
        mDel.setOnClickListener(this);
        mDel02.setOnClickListener(this);
        mEye.setOnCheckedChangeListener(this);
        mEye2.setOnCheckedChangeListener(this);
        setTextWatherListner();
        ScreenUtils.showKeyboard(getActivity());
    }

    private void setTextWatherListner() {
        mPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel.setVisibility(View.VISIBLE);
                    mEye.setVisibility(View.VISIBLE);
                    mEye.setEnabled(true);
                } else {
                    mDel.setVisibility(View.INVISIBLE);
                    mEye.setVisibility(View.VISIBLE);
                    mEye.setChecked(false);
                    mEye.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mPwd02.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel02.setVisibility(View.VISIBLE);
                    mEye2.setVisibility(View.VISIBLE);
                    mEye2.setEnabled(true);
                } else {
                    mDel02.setVisibility(View.INVISIBLE);
                    mEye2.setVisibility(View.VISIBLE);
                    mEye2.setEnabled(false);
                    mEye2.setChecked(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_del:
                mPwd.setText("");
                mEye.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_del2:
                mPwd02.setText("");
                mEye2.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_submit:
                String pwd01 = mPwd.getText().toString().trim();
                String pwd02 = mPwd02.getText().toString().trim();
                if (!CheckUtil.checkFindPassWord(mEye,mEye2,mPwd,mPwd02,pwd01, pwd02)) {
                    ScreenUtils.showKeyboard(getActivity());
                    return;
                } else {
                    LogUtils.i("zzz", "请求设置新密码网络");
                    requstSetNewPwd(resetCode, pwd01);
                }
                break;


        }

    }

    /***
     * 设置新密码请求
     *
     * @param code
     * @param newPwd
     */
    private void requstSetNewPwd(String code, String newPwd) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(List.class).setCallback(new Callback<List>() {
            @Override
            public void onResponse(Call<List> call, Response<List> response) {
                if (null != response.body()) {
                    LogUtils.i("zzz", "新密码设置成功");
                    ToastUtils.showToast("新密码设置成功！");
//                    UIController.goPersonalCenter(mContext);
//                    UIController.goAccountCenter(getActivity(), Constant.USER_LOGIN);
                    getActivity().setResult(Activity.RESULT_OK);
                    AppUtils.setExitAppFlag(AppUtils.TAG1);
                    ((Activity) mContext).finish();
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                mEye.setChecked(true);
                mPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ToastUtils.showToast(t.getMessage());
            }
        }).bulid().requestSetNewPwd(code, newPwd);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.iv_eye:
                if (isChecked) {
                    mPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mPwd.setSelection(mPwd.getText().length());
                break;
            case R.id.iv_eye2:
                if (isChecked) {
                    mPwd02.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPwd02.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mPwd02.setSelection(mPwd02.getText().length());
                break;
        }
    }
}
