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
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.ScreenUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ltbl on 2016/3/2.
 *
 * @desc 修改手机号码页面
 */
public class ModifyMobileFragment extends BaseFragment implements View.OnClickListener {
    private TextView mCurrentNum;
    private EditText mPhoneNum, mCodeNum;
    private TextView mCodeView;
    private ImageView mDel;
    private long codeTime = 0;
    private int count = 0;
    private PublicDialog mDialog;
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            checkCode();
            return false;
        }
    });

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_modify_mobile, container, false);
            initView();
        }
        return mRootView;
    }

    private void initView() {
        mPhoneNum = (EditText) mRootView.findViewById(R.id.et_account);
        mCodeNum = (EditText) mRootView.findViewById(R.id.et_vertifyCode);
        mCurrentNum = (TextView) mRootView.findViewById(R.id.tv_current_mobile);
        mCodeView = (TextView) mRootView.findViewById(R.id.bt_getCode);
        mDel = (ImageView) mRootView.findViewById(R.id.iv_del);
        mRootView.findViewById(R.id.tv_submit).setOnClickListener(this);
        mDel.setOnClickListener(this);
        mCodeView.setOnClickListener(this);
        mCurrentNum.setText(UserInfoManager.instance().getUserMobile().getMobile());

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
    public void onClick(View v) {
        String phoneNum = mPhoneNum.getText().toString().trim();
        String codeNum = mCodeNum.getText().toString().trim();
        switch (v.getId()) {
            case R.id.iv_del:
                mPhoneNum.setText("");
                break;
            case R.id.bt_getCode:
                if (TextUtils.isEmpty(phoneNum)) {
                    ToastUtils.showToast("手机号码不能为空");
                } else if (!CheckUtil.isMobileNO(phoneNum)) {
                    ToastUtils.showToast("手机号码格式有误");
                } else {
                    requestCode(phoneNum);
                }
                break;
            case R.id.tv_submit:
                if (!CheckUtil.checkBindPhone(mPhoneNum, mCodeNum, phoneNum, codeNum)) {
                    ScreenUtils.showKeyboard(getActivity());
                    return;
                } else {
                    mDialog = new PublicDialog(getActivity(), new LoadingDialogHolder());
                    mDialog.showDialog(new DataInfo("正在提交"));
                    requestModify(Integer.parseInt(codeNum), phoneNum);

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
        }).bulid().requestSendCode(mobile, 1, 0);
    }

    private void requestModify(int vertifyCode, final String mobile) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCallback(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (null != response.body()) {
//                    codeTime = System.currentTimeMillis();
                    LogUtils.i("zzz", "修改手机号码成功");
//                    //更新数据库
//                    UserBaseInfo userBaseInfo = new UserBaseInfo();
//                    userBaseInfo.setMobile(mobile);
//                    updateData2Db(userBaseInfo);
                    checkCode();
                    mDialog.dismiss();
                    ToastUtils.showToast("修改成功");
                    Intent intent = new Intent();
                    intent.putExtra("mobile", mobile);
                    Logger.i(mobile + "++++++++++++++++++++++++++");
                    ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                    ((Activity) mContext).finish();
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                if (t.getMessage().equals("验证码不正确")){
                    mCodeNum.setFocusable(true);
                    mCodeNum.requestFocus();
                    mCodeNum.setFocusableInTouchMode(true);
                }
                mDialog.dismiss();
            }
        }).bulid().requestModifyMobile(vertifyCode, mobile);
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
        setmPageAlias(Constant.PAGE_MODIFY_MOBILE);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }
}