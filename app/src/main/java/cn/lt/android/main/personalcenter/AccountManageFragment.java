package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.LogTAG;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.LoginHistoryEntity;
import cn.lt.android.db.LoginHistoryEntityDao;
import cn.lt.android.db.UserEntity;
import cn.lt.android.db.UserEntityDao;
import cn.lt.android.main.UIController;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.manager.fs.LTDirType;
import cn.lt.android.manager.fs.LTDirectoryManager;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.android.widget.dialog.holder.LogoutDialogHolder;
import cn.lt.android.widget.dialog.holder.PhotoDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;
import cn.lt.framework.util.BitmapUtils;
import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/3/2.
 *
 * @desc 账户管理页面
 */
public class AccountManageFragment extends BaseFragment implements View.OnClickListener {
    private Bitmap mBitmap;
    private ImageView mCircleImageView;
    private TextView mNickName, mMobile;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_ACCOUNT_MANAGE);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_account_manage, container, false);
            initView();
            getData();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        statEvent();
        if (AppUtils.getExitAppFlag() == AppUtils.TAG3) {
            AppUtils.setExitAppFlag(AppUtils.TAG2);
            getActivity().finish();
        }
    }

    /***
     * 填充数据
     */
    private void getData() {
        UserBaseInfo userInfo = UserInfoManager.instance().getUserInfo();
        LogUtils.i("zzz", "用户账户页面=" + userInfo.getAvatar());
        ImageloaderUtil.loadUserHead(mContext, userInfo.getAvatar(), mCircleImageView);
        mNickName.setText(userInfo.getNickname());
        mMobile.setText(userInfo.getMobile());
    }

    /***
     * 初始化视图
     */
    private void initView() {
        mRootView.findViewById(R.id.rl_userhead).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_nickname).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_mobile).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_pwd).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_logout).setOnClickListener(this);
        mNickName = (TextView) mRootView.findViewById(R.id.tv_user_name);
        mMobile = (TextView) mRootView.findViewById(R.id.tv_tel_num);
        mCircleImageView = (ImageView) mRootView.findViewById(R.id.civ_user_head);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_userhead:
                new PublicDialog(this.getActivity(), new PhotoDialogHolder(this)).showDialog(null);
                break;
            case R.id.rl_nickname:
//                UIController.goUserInfoEditPage(getActivity(),Constant.MODIFY_NICKNAME);
                Intent intent = new Intent(mContext, UserInfoEditActivity.class);
                intent.putExtra("type", Constant.MODIFY_NICKNAME);
                startActivityForResult(intent, 3);
                break;
            case R.id.rl_mobile:
//                UIController.goUserInfoEditPage(getActivity(), Constant.MODIFY_MOBILE);
                Intent intent2 = new Intent(mContext, UserInfoEditActivity.class);
                intent2.putExtra("type", Constant.MODIFY_MOBILE);
                intent2.putExtra("mobile", mMobile.getText().toString());
                startActivityForResult(intent2, 4);
                break;
            case R.id.rl_pwd:
                UIController.goUserInfoEditPage(getActivity(), Constant.MODIFY_PWD);
                break;
            case R.id.tv_logout:
                LogUtils.d(LogTAG.USER,"应用市场退出："+UserInfoManager.instance().getUserInfo());
                new PublicDialog(getActivity(), new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.logout));
                break;
        }

    }

    private void goUserInfoEditPage(String type) {
        Intent intent = new Intent(mContext, UserInfoEditActivity.class);
        intent.putExtra("type", type);
        startActivityForResult(intent, 3);
    }

    private PublicDialog mDialog;

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
                        requestNet(mBitmap);
                    }
                    break;
                case PhotoDialogHolder.PHOTO:
                    LogUtils.i("zzz", "拍照返回");
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
                case 3:
                    mNickName.setText(data.getStringExtra("nickname"));
                    break;
                case 4:
                    mMobile.setText(data.getStringExtra("mobile"));
                    UserInfoManager.instance().saveUserMobile(data.getStringExtra("mobile"));
                    Logger.i(data.getStringExtra("mobile") + "======================");
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 上传图片
     *
     * @param bitmap
     */
    private void requestNet(Bitmap bitmap) {
        File file = BitmapUtils.compressBitmap(bitmap, LTDirectoryManager.getInstance().getDirectoryPath(LTDirType.image), "ltapp");
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                UserBaseInfo userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    userBaseInfo.setNickname(UserInfoManager.instance().getUserInfo().getNickname());
                    UserInfoManager.instance().updateUserAvatar(userBaseInfo);//保存当前用户头像
                    EventBus.getDefault().post(userBaseInfo);
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
                    mCircleImageView.setImageBitmap(toRoundBitmap(mBitmap));

                    //保存最新的图片等信息到数据库
                    updateData2Db(userBaseInfo);
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

    /**
     * 把bitmap转成圆形
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int r = 0;
        //取最短边做边长
        if (width < height) {
            r = width;
        } else {
            r = height;
        }
        //构建一个bitmap
        Bitmap backgroundBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //new一个Canvas，在backgroundBmp上画图
        Canvas canvas = new Canvas(backgroundBm);
        Paint p = new Paint();
        //设置边缘光滑，去掉锯齿
        p.setAntiAlias(true);
        RectF rect = new RectF(0, 0, r, r);
        //通过制定的rect画一个圆角矩形，当圆角X轴方向的半径等于Y轴方向的半径时，
        //且都等于r/2时，画出来的圆角矩形就是圆形
        canvas.drawRoundRect(rect, r / 2, r / 2, p);
        //设置当两个图形相交时的模式，SRC_IN为取SRC图形相交的部分，多余的将被去掉
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //canvas将bitmap画在backgroundBmp上
        canvas.drawBitmap(bitmap, null, rect, p);
        return backgroundBm;
    }

}
