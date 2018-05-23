package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/3/2.
 *
 * @desc 修改昵称页面
 */
public class ModifyNickNameFragment extends BaseFragment implements View.OnClickListener {
    private ImageView mDel;
    private EditText mNickName;
    private PublicDialog mDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.modify_nickname_fragment, container, false);
            initView();
            String nickName = UserInfoManager.instance().getUserInfo().getNickname();
            mNickName.setText(nickName);
            mNickName.setSelection(nickName.length());

        }
        return mRootView;
    }

    private void initView() {
        mDel = (ImageView) mRootView.findViewById(R.id.iv_del);
        mNickName = (EditText) mRootView.findViewById(R.id.et_username);
        mRootView.findViewById(R.id.tv_submit).setOnClickListener(this);
        mDel.setOnClickListener(this);
        mNickName.addTextChangedListener(new TextWatcher() {
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
        String nickName = mNickName.getText().toString().trim();
        switch (v.getId()) {
            case R.id.iv_del:
                mNickName.setText("");
                break;
            case R.id.tv_submit:
                if (!CheckUtil.checkNickName(mNickName, nickName)) {
                    CheckUtil.showKeyboard(mNickName);
                    return;
                } else {
                    mDialog = new PublicDialog(getActivity(), new LoadingDialogHolder());
                    mDialog.showDialog(new DataInfo("正在提交"));
                    requestNet(nickName);
                }

        }
    }

    private void requestNet(final String nickname) {
        UserBaseInfo localUserInfo = UserInfoManager.instance().getUserInfo();
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                UserBaseInfo userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    mDialog.dismiss();
                    ToastUtils.showToast("昵称修改成功");
                    UserInfoManager.instance().updateUserNickName(userBaseInfo);
                    Intent intent = new Intent();
                    intent.putExtra("nickname", nickname);
                    EventBus.getDefault().post(userBaseInfo);
                    ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                    ((Activity) mContext).finish();
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                mDialog.dismiss();
                CheckUtil.showKeyboard(mNickName);
            }
        }).bulid().updateUserInfo(localUserInfo.getAvatar(), nickname, localUserInfo.getSex(), localUserInfo.getBirthday(), localUserInfo.getAddress());
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_MODIFY_NICKNAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }
}
