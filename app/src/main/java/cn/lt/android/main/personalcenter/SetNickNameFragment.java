package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.android.widget.dialog.holder.PhotoDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.BitmapUtils;
import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ltbl on 2016/4/15.
 *
 * @des 注册成功之后的设置昵称和头像页面
 */
public class SetNickNameFragment extends BaseFragment implements View.OnClickListener {

    private ImageView mDel;
    private EditText mNickName;
    private CircleImageView mHeadView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_set_nickname, container, false);
            initView();
        }
        return mRootView;
    }

    private void initView() {
        mDel = (ImageView) mRootView.findViewById(R.id.iv_del);
        mNickName = (EditText) mRootView.findViewById(R.id.et_username);
        mHeadView = (CircleImageView) mRootView.findViewById(R.id.iv_user_head);
        String userNickName = UserInfoManager.instance().getUserInfo().getNickname();
        if (userNickName != null) {
            mNickName.setText(userNickName);
            mNickName.setSelection(userNickName.length());
        }
        mRootView.findViewById(R.id.tv_submit).setOnClickListener(this);
        mDel.setOnClickListener(this);
        mHeadView.setOnClickListener(this);
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

        ((AccountCenterActivity) getActivity()).mActionBar.setIv_BackOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIController.goAccountCenter1(getActivity(), Constant.USER_INFO);
                getActivity().finish();
            }
        });

        mRootView.setFocusable(true);
        mRootView.setFocusableInTouchMode(true);
        mRootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    if (keyCode == KeyEvent.KEYCODE_BACK){
                        AppUtils.setExitAppFlag(AppUtils.TAG1);
                        UIController.goAccountCenter1(getActivity(), Constant.USER_INFO);
                        getActivity().finish();
                        Logger.i("点击了返回键");
                    }
                }
                return false;
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
            case R.id.iv_user_head:
                new PublicDialog(this.getActivity(), new PhotoDialogHolder(this)).showDialog(null);
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

//                    AppUtils.setExitAppFlag(AppUtils.TAG1);

                    UIController.goHomePage(getContext(), MainActivity.PAGE_TAB_MINE, MainActivity.PAGE_TAB_GAME_SUB_INDEX);
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

    private PublicDialog mDialog;
    private Bitmap mBitmap;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PhotoDialogHolder.ALBUM:
                    //相册返回
                    mBitmap = data.getParcelableExtra("data");

                    if (mBitmap == null && data.getData() != null) {
                        Intent intent = new Intent("com.android.camera.action.CROP");
                        intent.setType("image/*");
                        intent.setDataAndType(data.getData(), "image/jpeg");
                        intent.putExtra("crop", "true");
                        intent.putExtra("aspectX", 1);
                        intent.putExtra("aspectY", 1);
                        // outputX outputY 是裁剪图片宽高
                        intent.putExtra("outputX", 100);
                        intent.putExtra("outputY", 100);
                        intent.putExtra("return-data", true);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        intent.putExtra("noFaceDetection", true);
                        this.startActivityForResult(intent, PhotoDialogHolder.ALBUM);
                    } else {
                        mDialog = new PublicDialog(mContext, new LoadingDialogHolder());
                        mDialog.showDialog(new DataInfo("图片上传中"));
                        upLoadAvatar(mBitmap);
//                        File bb = new File(LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.image) + File.separator + "ltapp.jpg");
//                        if (bb.exists()) {
//                            bb.delete();
//                        }
                    }
                    break;
                case PhotoDialogHolder.PHOTO:
                    Log.i("zzz", "拍照返回");
                    // 照相返回
                    File bb = new File(LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.image) + File.separator + "ltapp.jpg");
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setType("image/*");
                    intent.setDataAndType(Uri.fromFile(bb), "image/jpeg");
                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    // outputX outputY 是裁剪图片宽高
                    intent.putExtra("outputX", 100);
                    intent.putExtra("outputY", 100);
                    intent.putExtra("return-data", true);
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    intent.putExtra("noFaceDetection", true);
                    this.startActivityForResult(intent, PhotoDialogHolder.ALBUM);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void upLoadAvatar(Bitmap bitmap) {
        File file = BitmapUtils.compressBitmap(bitmap, LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.image), "ltapp");
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                UserBaseInfo userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    userBaseInfo.setNickname(UserInfoManager.instance().getUserInfo().getNickname());
                    UserInfoManager.instance().updateUserAvatar(userBaseInfo);//保存当前用户头像
                    EventBus.getDefault().post(userBaseInfo);
                    Log.i("zzz", "新头像地址===" + userBaseInfo.getAvatar());
                    updateUserInfo(userBaseInfo.getAvatar());
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                mDialog.dismiss();
            }
        }).bulid().uploadAvatar(RequestBody.create(MediaType.parse("application/octet-stream"), file));
    }

    /***
     * 更新用户头像信息，保证用户退出后重新登录获取到最新头像
     *
     * @param avatarUri
     */
    private void updateUserInfo(final String avatarUri) {
        UserBaseInfo localUserInfo = UserInfoManager.instance().getUserInfo();
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                UserBaseInfo userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    mDialog.dismiss();
                    ToastUtils.showToast("上传成功");
                    mHeadView.setImageBitmap(mBitmap);
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
            }
        }).bulid().updateUserInfo(avatarUri, localUserInfo.getNickname(), localUserInfo.getSex(), localUserInfo.getBirthday(), localUserInfo.getAddress());
    }

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_REGISTER_SUCCESS);
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
    }
}
