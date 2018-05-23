package cn.lt.android.main.personalcenter;

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
import cn.lt.android.main.UIController;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckUtil;
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
 * @desc 修改密码
 */
public class ModifyPwdFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private EditText mPwd01, mPwd02, mPwd03;
    private ImageView mDel01, mDel02, mDel03;
    private CheckBox mEye01, mEye02, mEye03;
    private PublicDialog mDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_modify_pwd, container, false);
            initView();
        }
        return mRootView;
    }

    private void initView() {
        mPwd01 = (EditText) mRootView.findViewById(R.id.et_current_pwd);
        mPwd02 = (EditText) mRootView.findViewById(R.id.et_new_password);
        mPwd03 = (EditText) mRootView.findViewById(R.id.et_confirm_pwd);
        mDel01 = (ImageView) mRootView.findViewById(R.id.iv_del);
        mDel02 = (ImageView) mRootView.findViewById(R.id.iv_del2);
        mDel03 = (ImageView) mRootView.findViewById(R.id.iv_del3);
        mEye01 = (CheckBox) mRootView.findViewById(R.id.iv_eye1);
        mEye02 = (CheckBox) mRootView.findViewById(R.id.iv_eye2);
        mEye03 = (CheckBox) mRootView.findViewById(R.id.iv_eye3);
        mRootView.findViewById(R.id.tv_submit).setOnClickListener(this);
        mDel01.setOnClickListener(this);
        mDel02.setOnClickListener(this);
        mDel03.setOnClickListener(this);
        mEye01.setOnCheckedChangeListener(this);
        mEye02.setOnCheckedChangeListener(this);
        mEye03.setOnCheckedChangeListener(this);
        setTextWatherListener();
        ScreenUtils.showKeyboard(getActivity());
    }

    private void setTextWatherListener() {
        mPwd01.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel01.setVisibility(View.VISIBLE);
                    mEye01.setVisibility(View.VISIBLE);
                    mEye01.setEnabled(true);
                } else {
                    mDel01.setVisibility(View.INVISIBLE);
                    mEye01.setVisibility(View.VISIBLE);
                    mEye01.setChecked(false);
                    mEye01.setEnabled(false);
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
                    mEye02.setVisibility(View.VISIBLE);
                    mEye02.setEnabled(true);
                } else {
                    mDel02.setVisibility(View.INVISIBLE);
                    mEye02.setVisibility(View.VISIBLE);
                    mEye02.setChecked(false);
                    mEye02.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mPwd03.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && s.toString().length() > 0) {
                    mDel03.setVisibility(View.VISIBLE);
                    mEye03.setVisibility(View.VISIBLE);
                    mEye03.setEnabled(true);
                } else {
                    mDel03.setVisibility(View.INVISIBLE);
                    mEye03.setVisibility(View.VISIBLE);
                    mEye03.setChecked(false);
                    mEye03.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        String pwd01 = mPwd01.getText().toString().trim();
        String pwd02 = mPwd02.getText().toString().trim();
        String pwd03 = mPwd03.getText().toString().trim();
        switch (v.getId()) {
            case R.id.tv_submit:
                if (!CheckUtil.checkModifyPassWordInfo(mEye01, mEye02, mEye03, mPwd01, mPwd02, mPwd03, pwd01, pwd02, pwd03)) {
                    ScreenUtils.showKeyboard(getActivity());
                    return;
                } else {
                    mDialog = new PublicDialog(getActivity(), new LoadingDialogHolder());
                    mDialog.showDialog(new DataInfo("正在提交"));
                    requestNet(pwd01, pwd03);
                }
                break;
            case R.id.iv_del:
                mPwd01.setText("");
                mEye01.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_del2:
                mPwd02.setText("");
                mEye02.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_del3:
                mPwd03.setText("");
                mEye03.setVisibility(View.VISIBLE);
                break;
        }

    }

    /***
     * 密码修改网络请求
     * 注意：修改密码完成以后要清空本地缓存
     *
     * @param oldPwd
     * @param newPwd
     */
    private void requestNet(final String oldPwd, final String newPwd) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(List.class).setCallback(new Callback<List>() {
            @Override
            public void onResponse(Call<List> call, Response<List> response) {
                List userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    mDialog.dismiss();
                    ToastUtils.showToast("密码修改成功");
//                    EventBus.getDefault().post(userBaseInfo);
                    UserInfoManager.instance().userLogout(false);
                    UIController.goAccountCenter(getActivity(),Constant.USER_LOGIN);
                    AppUtils.setExitAppFlag(AppUtils.TAG3);
                    getActivity().finish();
//                    ((Activity) mContext).finish();
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                if (t.getMessage().equals("旧密码输入不正确")){
                    mPwd01.setFocusable(true);
                    mPwd01.setFocusableInTouchMode(true);
                    mPwd01.requestFocus();
                    mEye01.setChecked(true);
                    mPwd01.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                mDialog.dismiss();
            }
        }).bulid().requestModifyPwd(oldPwd, newPwd);
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_MODIFY_PWD);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.iv_eye1:
                if (isChecked) {
                    mPwd01.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPwd01.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mPwd01.setSelection(mPwd01.getText().length());
                break;
            case R.id.iv_eye2:
                if (isChecked) {
                    mPwd02.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPwd02.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mPwd02.setSelection(mPwd02.getText().length());
                break;
            case R.id.iv_eye3:
                if (isChecked) {
                    mPwd03.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPwd03.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mPwd03.setSelection(mPwd03.getText().length());
                break;
        }

    }
}
