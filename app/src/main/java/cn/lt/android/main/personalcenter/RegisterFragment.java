package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.main.UIController;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/3/2.
 *
 * @desc 注册页面
 */
public class RegisterFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private long codeTime = 0;
    private int count = 0;
    private TextView mCodeView;
    private EditText mPhonenumber;
    private EditText mPassword;
    private EditText mCode;
    private ImageView mDel01, mDel02;
    private CheckBox mEye;
    private String from;
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            checkCode();
            return false;
        }
    });

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.register_fragment, container, false);
            initView();
        }
        return mRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        from = getActivity().getIntent().getStringExtra("resetCode");
    }

    private void initView() {
        mCodeView = (TextView) mRootView.findViewById(R.id.tv_getCode);
        mRootView.findViewById(R.id.tv_old_login).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_onekey_register).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_term).setOnClickListener(this);
        mPhonenumber = (EditText) mRootView.findViewById(R.id.et_account);
        mPassword = (EditText) mRootView.findViewById(R.id.et_password);
        mCode = (EditText) mRootView.findViewById(R.id.et_vertifyCode);
        mDel01 = (ImageView) mRootView.findViewById(R.id.iv_del);
        mDel02 = (ImageView) mRootView.findViewById(R.id.iv_del2);
        mEye = (CheckBox) mRootView.findViewById(R.id.iv_eye);
        mCodeView.setOnClickListener(this);
        mDel01.setOnClickListener(this);
        mDel02.setOnClickListener(this);
        mEye.setOnCheckedChangeListener(this);
        setOnTextChangeListener();
        ScreenUtils.showKeyboard(getActivity());
    }

    private void setOnTextChangeListener() {
        mPhonenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel01.setVisibility(View.VISIBLE);
                } else {
                    mDel01.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel02.setVisibility(View.VISIBLE);
                    mEye.setVisibility(View.VISIBLE);
                    mEye.setEnabled(true);
                } else {
                    mDel02.setVisibility(View.INVISIBLE);
                    mEye.setVisibility(View.VISIBLE);
                    mEye.setChecked(false);
                    mEye.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private PublicDialog mDialog;

    @Override
    public void onClick(View v) {
        String phoneNum = mPhonenumber.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String vertifyCode = mCode.getText().toString().trim();
        switch (v.getId()) {
            case R.id.tv_old_login:
                UIController.goAccountCenter(getActivity(), Constant.USER_LOGIN);
                break;
            case R.id.tv_getCode:
                if (!NetWorkUtils.isConnected(getContext())) {
                    ToastUtils.showToast("当前网络不可用,请检查网络设置");
                    return;
                }
                if (!CheckUtil.obtainCode(mPhonenumber, mPassword, phoneNum, password,mEye)) {
                    return;
                } else {
                    count++;
                    requestCode(phoneNum);
                }
                break;
            case R.id.tv_onekey_register:
                if (!NetWorkUtils.isConnected(getContext())) {
                    ToastUtils.showToast("当前网络不可用,请检查网络设置");
                    return;
                }
                if (!CheckUtil.checkPhoneRegisterInfo(mPhonenumber, mPassword, phoneNum, password, vertifyCode,mEye)) {
                    ScreenUtils.showKeyboard(getActivity());
                    return;
                } else {
                    mDialog = new PublicDialog(getActivity(), new LoadingDialogHolder());
                    mDialog.showDialog(new DataInfo("正在提交"));
                    requestRegister(phoneNum, password, Integer.parseInt(vertifyCode));
                }
                break;
            case R.id.iv_eye:
                mPassword.setInputType(0x90);
                mPassword.setSelection(mPassword.getText().length());
                break;
            case R.id.iv_del:
                mPhonenumber.setText("");
                break;
            case R.id.iv_del2:
                mPassword.setText("");
                mEye.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_term:
                UIController.goAccountCenter(getActivity(), Constant.USER_AGREEMENT);
                break;
        }
    }

    /***
     * 获取验证码网络请求
     *
     * @param mobile
     */
    private void requestCode(String mobile) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(List.class).setCallback(new Callback<List>() {
            @Override
            public void onResponse(Call<List> call, Response<List> response) {
                List userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    codeTime = System.currentTimeMillis();
                    checkCode();
                    mCode.setFocusable(true);
                    mCode.setFocusableInTouchMode(true);
                    mCode.requestFocus();
                    ToastUtils.showToast("验证码已发送至你的手机");
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
            }
        }).bulid().requestSendCode(mobile, 1, 0);
    }

    /***
     * 注册网络请求
     *
     * @param mobile
     * @param pwd
     * @param code
     */
    private void requestRegister(final String mobile, String pwd, int code) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                UserBaseInfo userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    UserInfoManager.instance().loginSuccess(userBaseInfo, false);
                    mDialog.dismiss();
                    ToastUtils.showToast("注册成功");
                    //保存信息到数据库
                    saveUserInfoHistory(userBaseInfo);
                    DCStat.baiduStat(mContext, "register_success", "注册成功：" + userBaseInfo.getId());//z
                    LogUtils.i("zzz", "注册成功后的用户昵称==" + userBaseInfo.getNickname());
                    if (TextUtils.isEmpty(from)){
                        UIController.goAccountCenter(getActivity(), Constant.SETNICKNAME);
                        getActivity().setResult(Activity.RESULT_OK);
                        ((Activity) mContext).finish();
                    }else{
                        ((Activity) mContext).finish();
                    }

                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                if (t.getMessage().equals("验证码错误")){
                    mCode.setFocusable(true);
                    mCode.setFocusableInTouchMode(true);
                    mCode.requestFocus();
                }
                mDialog.dismiss();
                ScreenUtils.showKeyboard(getActivity());
            }
        }).bulid().requestRegister(mobile, pwd, code);
    }

    /**
     * 检查验证码是否可以继续发送
     */
    private void checkCode() {
        if (!CheckUtil.checkCode(codeTime, mCodeView, count)) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_REGISTER);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            mPassword.setSelection(mPassword.getText().length());
        } else {
            mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            mPassword.setSelection(mPassword.getText().length());
        }
    }
}
