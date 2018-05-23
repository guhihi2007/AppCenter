package cn.lt.android.main.personalcenter;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalParams;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.LoginHistoryEntity;
import cn.lt.android.db.LoginHistoryEntityDao;
import cn.lt.android.main.UIController;
import cn.lt.android.main.personalcenter.model.PhoneNumber;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.statistics.DCStat;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.CheckUtil;
import cn.lt.android.util.DensityUtil;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.DeleteAccountDialogHolder;
import cn.lt.android.widget.dialog.holder.LoadingDialogHolder;
import cn.lt.appstore.R;
import cn.lt.framework.util.NetWorkUtils;
import cn.lt.framework.util.ScreenUtils;
import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by atian on 2016/2/29.
 *
 * @desc 个人中心登录页面
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private EditText mPhoneNum, mPwd;
    private ImageView mDel01, mDel02, mPhoneNumToggle;
    private CheckBox mEye;
    private PopupWindow mPopupWindow;
    private List<LoginHistoryEntity> mHistoryListnew = new ArrayList();
    MyAdapter mMyAdapter = null;
    ListView contentView;
    private View mStone_center;
    //    private List<LoginHistoryEntity> mDataList= new ArrayList();

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_LOGIN);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_login, container, false);
            initView();
        }
        return mRootView;
    }

    private void initView() {
        mRootView.findViewById(R.id.tv_register_new).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_getbackpwd).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_login).setOnClickListener(this);
        mPhoneNum = (EditText) mRootView.findViewById(R.id.et_account);
        mPwd = (EditText) mRootView.findViewById(R.id.et_password);
        mDel01 = (ImageView) mRootView.findViewById(R.id.iv_mobile_del);
        mDel02 = (ImageView) mRootView.findViewById(R.id.iv_del2);
        mPhoneNumToggle = (ImageView) mRootView.findViewById(R.id.iv_mobile_toggle);
        mStone_center = mRootView.findViewById(R.id.stone_center);
        mEye = (CheckBox) mRootView.findViewById(R.id.iv_eye);
        mEye.setOnCheckedChangeListener(this);
        mDel01.setOnClickListener(this);
        mDel02.setOnClickListener(this);
        mPhoneNumToggle.setOnClickListener(this);
        setOnTextWaterLisener();
        mDialog = new PublicDialog(this.getActivity(), new LoadingDialogHolder());
        //从拿到数据集
        mHistoryListnew = getLoginHistoryListByDb();
        LogUtils.i("ttt", mHistoryListnew.size() + "");
        ScreenUtils.showKeyboard(getActivity());
        //默认
        if (mHistoryListnew.size() > 0) {
            mPhoneNumToggle.setVisibility(View.VISIBLE);
            mStone_center.setVisibility(View.VISIBLE);
            LoginHistoryEntity loginHistoryEntity = mHistoryListnew.get(0);
            mPhoneNum.setText(loginHistoryEntity.getMobile());
            mPhoneNum.setSelection(loginHistoryEntity.getMobile().length());
        } else {
            mPhoneNumToggle.setVisibility(View.GONE);
            mStone_center.setVisibility(View.GONE);
        }
    }


    /***
     * 获取 用户登录历史记录列表
     */
    public List<LoginHistoryEntity> getLoginHistoryListByDb() {
        QueryBuilder query = getLoginHistoryDao().queryBuilder().orderDesc(LoginHistoryEntityDao.Properties.Id);
        List<LoginHistoryEntity> list = query.list();
        for (LoginHistoryEntity e : list) {
            mHistoryListnew.add(new LoginHistoryEntity(e.getId(), e.getAvatar(), e.getMobile(), e.getEmail(), e.getToken(), e.getUserId(), e.getNickName()));
        }
        return mHistoryListnew;
    }

    private AbstractDao getLoginHistoryDao() {
        return GlobalParams.getLoginHistoryEntityDao();
    }

    private void setOnTextWaterLisener() {
        mPhoneNum.addTextChangedListener(new TextWatcher() {
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
        mPwd.addTextChangedListener(new TextWatcher() {
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
        String phoneNum = mPhoneNum.getText().toString().trim();
        String pwd = mPwd.getText().toString().trim();

        switch (v.getId()) {
            case R.id.tv_register_new:
                UIController.goAccountCenter(getActivity(), Constant.USER_REGISTER);
//                ((Activity) mContext).finish();
                break;
            case R.id.tv_getbackpwd:
                UIController.goAccountCenter(getActivity(), Constant.GET_BACK_PWD);
//                ((Activity) mContext).finish();
                break;
            case R.id.iv_mobile_del:
                mPhoneNum.setText("");
                break;
            case R.id.iv_del2:
                mPwd.setText("");
                mEye.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_login:
                if (!CheckUtil.checkLoginInfo(mPhoneNum, mPwd, phoneNum, pwd, mEye)) {
                    ScreenUtils.showKeyboard(getActivity());
                    return;
                }
                if (!NetWorkUtils.isConnected(getContext())) {
                    ToastUtils.showToast("当前网络不可用,请检查网络设置");
                    return;
                } else {
                    mDialog.showDialog(new DataInfo("正在登录"));
                    requestNet(phoneNum, pwd);
                }
                break;
            //查看历史记录
            case R.id.iv_mobile_toggle:
                ScreenUtils.hideKeyboard(getActivity());
                showHistoryLoginNumber(mHistoryListnew.size());
                break;

        }
    }


    /**
     * 弹出下拉选择框
     *
     * @param
     * @param size
     */
    private void showHistoryLoginNumber(int size) {
        if (mPopupWindow == null) {
            int width = mPhoneNum.getWidth();
            int height = DensityUtil.dip2px(getContext(), size >= 5 ? 220 : 44 * size);
            mPopupWindow = new PopupWindow(width, height);
            contentView = new ListView(getContext());
            //如果超过5个历史账号就一直显示滑动条
            if (Build.VERSION.SDK_INT > 15 && size > 5) {
                contentView.setScrollbarFadingEnabled(false);
                contentView.setScrollBarFadeDuration(0);
            }
            contentView.setBackgroundResource(R.drawable.listview_background);
            mMyAdapter = new MyAdapter();
            contentView.setAdapter(mMyAdapter);
            contentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    LoginHistoryEntity loginHistoryEntity = mHistoryListnew.get(position);
                    mPhoneNum.setText(loginHistoryEntity.getMobile());
                    mPhoneNum.setSelection(loginHistoryEntity.getMobile().length());
                    mPopupWindow.dismiss();
                }
            });
            mPopupWindow.setContentView(contentView);
            mPopupWindow.setOutsideTouchable(true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
            mPopupWindow.setFocusable(true);
        }
        mPopupWindow.showAsDropDown(mPhoneNum);
        changeArrowToggleByAnimation(0, 180);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                changeArrowToggleByAnimation(180, 0);
            }
        });
    }

    /**
     * 改变状态让箭头旋转
     *
     * @param start
     * @param end
     */
    //方式一：
    private void changeArrowToggleByAnimation(int start, int end) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(mPhoneNumToggle, "rotation", start, end);
        animator.setDuration(500);
        animator.start();
    }

    /**
     * 改变状态让箭头旋转
     *
     * @param animator_resource 顺时针：R.animator.clockwise  逆时针：R.animator.contrarotate
     */
    //方式二：---在xml中定义动画  R.animator.clockwise
    private void changeArrowToggleByAnimation(int animator_resource) {
        Animator animator = AnimatorInflater.loadAnimator(getContext(), animator_resource);
        animator.setTarget(mPhoneNumToggle);
        animator.start();
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mHistoryListnew.size();
        }

        @Override
        public Object getItem(int position) {
            return mHistoryListnew.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_login_history, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final LoginHistoryEntity loginHistoryEntity = mHistoryListnew.get(position);
            LogUtils.i("Loginfragment", "原地址==" + loginHistoryEntity.toString() + loginHistoryEntity.getMobile());
            viewHolder.mUserId.setText(loginHistoryEntity.getEmail().equals("") ? loginHistoryEntity.getMobile() : loginHistoryEntity.getEmail());
            //显示头像
            ImageloaderUtil.loadUserHead(getContext(), loginHistoryEntity.getAvatar(), viewHolder.mUserHead);
            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PublicDialog(getActivity(), new DeleteAccountDialogHolder()).showDialog(new DataInfo(loginHistoryEntity));
                }
            });
            return convertView;
        }
    }


    private class ViewHolder {
        public ViewHolder(View v) {
            mUserId = (TextView) v.findViewById(R.id.userId);
            mDelete = (ImageView) v.findViewById(R.id.delete);
            mUserHead = (ImageView) v.findViewById(R.id.item_user_head);
        }

        TextView mUserId;
        ImageView mDelete, mUserHead;
    }

    /***************************************************************/

    /**
     * 登录请求
     *
     * @param mobile
     * @param pwd
     */
    private void requestNet(final String mobile, String pwd) {
        NetWorkClient.getHttpClient().setHostType(HostType.UCENETER_HOST).setCls(UserBaseInfo.class).setCallback(new Callback<UserBaseInfo>() {
            @Override
            public void onResponse(Call<UserBaseInfo> call, Response<UserBaseInfo> response) {
                mDialog.dismiss();
                UserBaseInfo userBaseInfo = response.body();
                if (null != userBaseInfo) {
                    UserInfoManager.instance().loginSuccess(userBaseInfo, false);
                    //将三个信息保存到数据库
                    saveUserInfoHistory(userBaseInfo);
                    DCStat.baiduStat(mContext, "login_success", "登录成功：" + userBaseInfo.getId()); //统计登录成功
                    ((Activity) mContext).setResult(Activity.RESULT_OK);
                    ((Activity) mContext).finish();
                } else {
                    ToastUtils.showToast("信息异常！");
                }

            }

            @Override
            public void onFailure(Call<UserBaseInfo> call, Throwable t) {
                ToastUtils.showToast(t.getMessage());
                mDialog.dismiss();
                mEye.setChecked(true);
                mPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                mPwd.requestFocus();
                ScreenUtils.showKeyboard(getActivity());
            }
        }).bulid().requestLogin(mobile, pwd);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        mPwd.setSelection(mPwd.getText().length());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 删除账号通知
     *
     * @param
     */
    public void onEventMainThread(LoginHistoryEntity loginHistoryEntity) {
        //还要移除数据库
        deleteHistoryDataByUserId(loginHistoryEntity.getUserId());
        mHistoryListnew.remove(loginHistoryEntity);
        mMyAdapter.notifyDataSetChanged();
        if (mHistoryListnew.size() == 0) {
            mPopupWindow.dismiss();
            mPhoneNumToggle.setVisibility(View.GONE);
            mStone_center.setVisibility(View.GONE);
        }
        String phoneNum = mPhoneNum.getText().toString().trim();
        //删除历史账号后，如果账号框中是该历史账号，也同时清除
        if (loginHistoryEntity.getMobile().equals(phoneNum)) {
            mPhoneNum.setText("");
        }

    }
}
