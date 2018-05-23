package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.entity.ResetCodeBean;
import cn.lt.android.main.UIController;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.util.ScreenUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/3/3.
 *
 * @desc 忘记密码/找回密码
 */
public class GetBackPwdFragment extends BaseFragment implements View.OnClickListener {
    private EditText mPhoneNum, mCode;
    private TextView mGetCodeBtn;
    private ImageView mDel;
    private long codeTime = 0;
    private int count = 0;
    private PublicDialog mDialog;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_FORGET_PWD);
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_getbackpwd, container, false);
            initView();
        }
        return mRootView;
    }

    private void initView() {
        mRootView.findViewById(R.id.tv_next).setOnClickListener(this);
        mPhoneNum = (EditText) mRootView.findViewById(R.id.et_account);
        mCode = (EditText) mRootView.findViewById(R.id.et_vertifyCode);
        mGetCodeBtn = (TextView) mRootView.findViewById(R.id.bt_getCode);
        mDel = (ImageView) mRootView.findViewById(R.id.iv_del);
        mGetCodeBtn.setOnClickListener(this);
        mDel.setOnClickListener(this);
        mPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel.setVisibility(View.VISIBLE);
                } else {
                    mDel.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
        if (AppUtils.getExitAppFlag() == AppUtils.TAG1) {
            AppUtils.setExitAppFlag(AppUtils.TAG2);
            getActivity().finish();
        }
    }

    @Override
    public void onClick(View v) {
        String phoneNum = mPhoneNum.getText().toString().trim();
        String codeNum = mCode.getText().toString().trim();
        switch (v.getId()) {
            case R.id.tv_next:
                if (!CheckUtil.checkBindPhone(mPhoneNum, mCode, phoneNum, codeNum)) {
                    ScreenUtils.showKeyboard(getActivity());
                    return;
                } else {
                    mDialog = new PublicDialog(getActivity(), new LoadingDialogHolder());
                    mDialog.showDialog(new DataInfo("正在提交"));
                    requstCheck(phoneNum, codeNum);
                }
                break;
            case R.id.iv_del:
                mPhoneNum.setText("");
                break;
            case R.id.bt_getCode:
                if (TextUtils.isEmpty(phoneNum)) {
                    ToastUtils.showToast("请输入手机号！");
                } else if (!CheckUtil.isMobileNO(phoneNum)) {
                    ToastUtils.showToast("手机号码格式错误！");
                } else {
                    requestCode(phoneNum);
                }
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
                    mCode.setFocusable(true);
                    mCode.setFocusableInTouchMode(true);
                    mCode.requestFocus();
                    codeTime = System.currentTimeMillis();
                    checkCode();
                    ToastUtils.showToast("验证码已发送至你的手机");
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
            }
        }).bulid().requestSendCode(mobile, 0, 1);
    }

    private void requstCheck(String mobile, String code) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(ResetCodeBean.class).setCallback(new Callback<ResetCodeBean>() {
            @Override
            public void onResponse(Call<ResetCodeBean> call, Response<ResetCodeBean> response) {
                ResetCodeBean resetCodeBean = response.body();
                if (null != resetCodeBean) {
                    LogUtils.i("zzz", "进入下一步" + resetCodeBean.getResetCode());
                    mDialog.dismiss();
//                    getActivity().finish();
                    UIController.goAccountCenter(getActivity(), Constant.RESET_PWD, resetCodeBean.getResetCode());
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<ResetCodeBean> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                if (t.getMessage().equals("验证码错误")) {
                    mCode.setFocusable(true);
                    mCode.setFocusableInTouchMode(true);
                    mCode.requestFocus();
                }
                mDialog.dismiss();
                LogUtils.i("zzz", "忘记密码异常");
            }
        }).bulid().requestSmsCheck(mobile, Integer.parseInt(code));
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            checkCode();
            return false;
        }
    });

    /**
     * 检查验证码是否可以继续发送
     */
    private void checkCode() {
        if (!CheckUtil.checkCode(codeTime, mGetCodeBtn, count)) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
