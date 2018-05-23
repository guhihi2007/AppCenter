package cn.lt.android.main.personalcenter;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.base.BaseFragment;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UserRedPointManager;
import cn.lt.android.install.InstallState;
import cn.lt.android.main.MainActivity;
import cn.lt.android.main.UIController;
import cn.lt.android.main.personalcenter.model.UserBaseInfo;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.plateform.update.manger.VersionCheckManger;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.ImageloaderUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.util.ViewUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.ExitInstallDialog;
import cn.lt.android.widget.dialog.ExitWarnDialog;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.LogoutDialogHolder;
import cn.lt.appstore.R;
import cn.lt.download.DownloadStatusDef;
import cn.lt.framework.util.NetWorkUtils;
import de.greenrobot.event.EventBus;

/**
 * @author chengyong
 * @time 2016/8/18 17:51
 * @des 我：个人中心底部tab
 */
public class MinePortalFragment extends BaseFragment implements View.OnClickListener {
    private int mCurrTab;
    private TextView mLoginTV, mUserNameTV;
    private TextView mRegisterTV;
    private RelativeLayout mUserInfo;
    private ImageView mUserHeadView;
    private TextView mAppManagerRedPoint;
    private boolean isLogin;
    private TextView mUpdateBar;
    private TextView mTaskCount;
    private Activity mContext;
//    private int taskCount;
//    //应用升级数量(4.3.3添加,底部tab需要时时跟随应用升级和任务管理数量加起来同步)
//    private int appUpgradeCount;

    @Override
    public void setPageAlias() {
        //TODO shezhi yemian  PAGE_MINE
        setmPageAlias(Constant.PAGE_MINE);
    }

    /**
     * 获得自己的引用，并保存相关的参数
     *
     * @param tab
     * @return
     */
    public static MinePortalFragment newInstance(String tab) {
        MinePortalFragment fragment = new MinePortalFragment();
        Bundle args = new Bundle();
        args.putString(BaseFragment.FRAGMENT_TAB, tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mCurrTab = getActivity().getIntent().getIntExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB, 0);
        getActivity().getIntent().removeExtra(MainActivity.INTENT_JUMP_KEY_SUB_TAB);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_personal_center_bak, container, false);
            initView(mRootView);
            checkPlatUpdate();
            getTaskCount();
        }

        UserRedPointManager.getInstance().setInUserCenter();
        isLogin = UserInfoManager.instance().isLogin();

        if (!NetWorkUtils.isConnected(getContext())) {
            mAppManagerRedPoint.setVisibility(View.GONE);
            mUpdateBar.setVisibility(View.GONE);
        }
        getAppUpdateCount();
        return mRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mContext = this.getActivity();
    }

    /***
     * 刷新页面
     *
     * @param userInfo
     */
    public void onEventMainThread(UserBaseInfo userInfo) {
        refreshUserInfo(userInfo);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            statEvent();
            refreshUserInfo(UserInfoManager.instance().getUserInfo());
            getAppUpdateCount();
            LogUtils.i("iii", "底部我onHiddenChanged走了");
//            refreshUserInfo(UserInfoManager.instance().getUserInfo());
            getTaskCount();
        }

    }

    private void initView(View mRootView) {
        mLoginTV = (TextView) mRootView.findViewById(R.id.tv_login_tip);
        mRegisterTV = (TextView) mRootView.findViewById(R.id.tv_register);
        mUserInfo = (RelativeLayout) mRootView.findViewById(R.id.rl_userinfo);
        mUserNameTV = (TextView) mRootView.findViewById(R.id.tv_username);
        mUserHeadView = (ImageView) mRootView.findViewById(R.id.civ_user_head);
        mAppManagerRedPoint = (TextView) mRootView.findViewById(R.id.iv_app_update_count);
        mUpdateBar = (TextView) mRootView.findViewById(R.id.tv_version_update_new);
        mTaskCount = (TextView) mRootView.findViewById(R.id.iv_app_task_manager_count);

        mRootView.findViewById(R.id.rl_iv_app_update).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_iv_app_task_manager).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_iv_app_uninstall).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_iv_app_settings).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_iv_version_update).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_iv_app_about_us).setOnClickListener(this);
        mRootView.findViewById(R.id.rl_iv_app_exit).setOnClickListener(this);

        mLoginTV.setOnClickListener(this);
        mRegisterTV.setOnClickListener(this);
        mUserInfo.setOnClickListener(this);
        mUserHeadView.setOnClickListener(this);

        onHiddenChanged(false);
    }


    private void refreshUserInfo(UserBaseInfo userBaseInfo) {
        isLogin = UserInfoManager.instance().isLogin();
        ImageloaderUtil.loadUserHead(getContext(), userBaseInfo.getAvatar(), mUserHeadView);
        if (isLogin) {
            setUserInfo(userBaseInfo);
        } else {
            mLoginTV.setVisibility(View.VISIBLE);
            mRegisterTV.setVisibility(View.VISIBLE);
            mUserInfo.setVisibility(View.GONE);
        }
    }

    private void setUserInfo(UserBaseInfo userBaseInfo) {
        mLoginTV.setVisibility(View.GONE);
        mRegisterTV.setVisibility(View.GONE);
        mUserInfo.setVisibility(View.VISIBLE);
        mUserNameTV.setText(userBaseInfo.getNickname());
    }


    @Override
    public void onClick(View v) {
        if (ViewUtils.isFastClick()) return;
        switch (v.getId()) {
            case R.id.tv_login_tip:
                UIController.goAccountCenter(getActivity(), Constant.USER_LOGIN);
                break;
            case R.id.tv_register:
                UIController.goAccountCenter(getActivity(), Constant.USER_REGISTER);
                break;
            case R.id.rl_userinfo:
                UIController.goAccountCenter(getActivity(), Constant.USER_INFO);
                break;

//            case R.id.feedback:
//                UIController.goFeedback(getActivity());
//                break;
            case R.id.civ_user_head:
                if (!UserInfoManager.instance().isLogin()) {
                    UIController.goAccountCenter(getActivity(), Constant.USER_LOGIN);
                } else {
                    UIController.goAccountCenter(getActivity(), Constant.USER_INFO);
                }
                break;
            /**********************************************/
            case R.id.rl_iv_app_update:
                LogUtils.d("Mine", "去应用升级");
                UIController.goUpdateActivity(getActivity());
                break;
            case R.id.rl_iv_app_task_manager:
                LogUtils.d("Mine", "去任务管理");
                UIController.goDownloadTask(mContext);
//                GlobalConfig.setIsOnClick(mContext, true);
//                mTaskCount.setVisibility(View.GONE);
                break;
            case R.id.rl_iv_app_uninstall:
                LogUtils.d("Mine", "去应用卸载");
                UIController.goAppUninstallActivity(getActivity());
                break;
            case R.id.rl_iv_app_about_us:
                UIController.goAboutUs(getActivity());
                break;
            case R.id.rl_iv_app_settings:
                UIController.goSetting(getActivity());
                break;
            case R.id.rl_iv_version_update:
                VersionCheckManger.getInstance().checkVerison(new VersionCheckManger.VersionCheckCallback() {
                    @Override
                    public void callback(Result result, VersionInfo info) {
                        switch (result) {
                            case have:
                                try {
                                    VersionCheckManger.getInstance().showUpdateDialog(getActivity(), false);
                                    mUpdateBar.setVisibility(View.VISIBLE);
                                    ((MainActivity) mContext).refreshTabMeNewForMinePortalFragment(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case none:
                                mUpdateBar.setVisibility(View.INVISIBLE);
                                ((MainActivity) mContext).refreshTabMeNewForMinePortalFragment(false);
                                ToastUtils.showToast("已经是最新版本");
                                break;
                            case fail:
                                ToastUtils.showToast("请稍后重试");
                                break;
                        }
                    }
                }, true);
                break;
            case R.id.rl_iv_app_exit:
                int downloadingCount = 0;
                try {
                    downloadingCount = DownloadTaskManager.getInstance().getDownloadingList().size();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (downloadingCount > 0) {
                    new ExitWarnDialog(getActivity(), downloadingCount).show();
                } else {
                    if (AppUtils.getAvailablMemorySize() <= 0) {
                        new PublicDialog(getContext(), new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.quit));
                    } else {
                        exitInstall();
                    }
                }
                break;
        }
    }

    private void exitInstall() {
        List<AppEntity> appEntities = DownloadTaskManager.getInstance().getInstallTaskList();
        List<AppEntity> installList = new ArrayList<>();
        LogUtils.i("Erosion", "安装列表===" + appEntities.size());
        if (appEntities.size() > 0) {
            PackageManager pm = getActivity().getPackageManager();
            for (AppEntity appEntity : appEntities) {
                if (appEntity.getStatus() == InstallState.install_failure || appEntity.getErrorType() == DownloadStatusDef.COMPLETE_SIGN_FAIL) {
                    continue;
                }
                // 包不存在
                if (!new File(appEntity.getSavePath()).exists()) {
                    continue;
                }

                // TODO 解析软件包时出现问题
                try {
                    PackageInfo info = pm.getPackageArchiveInfo(appEntity.getSavePath(),0);
                    if (info == null) {
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                installList.add(appEntity);
            }
            if (installList.size() == 0) {
                new PublicDialog(getContext(), new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.quit));
            } else {
                Resources resources = getActivity().getResources();
                String message;
                String appName = TextUtils.isEmpty(installList.get(0).getAlias()) ? installList.get(0).getName() : installList.get(0).getAlias();
                if (installList.size() == 1) {
                    message = String.format(resources.getString(R.string.exit_with_uninstalled), appName);
                } else {
                    message = String.format(resources.getString(R.string.exit_with_multi_uninstalled), appName, installList.size());
                }

                ExitInstallDialog exitInstallDialog = new ExitInstallDialog(getContext(), message, installList);
                exitInstallDialog.show();
            }
        } else {
            new PublicDialog(getActivity(), new LogoutDialogHolder()).showDialog(new DataInfo(LogoutDialogHolder.DialogType.quit));
        }
    }

    private void checkPlatUpdate() {
        if (((MainActivity) getActivity()).ismHavePlat()) {
            mUpdateBar.setVisibility(View.VISIBLE);
        } else {
            mUpdateBar.setVisibility(View.INVISIBLE);
        }
    }

    private void getAppUpdateCount() {
        if (mContext != null) {
            updateAppManagerRedPoint(((MainActivity) mContext).getAppUpgradeCount());
        }
    }

    private void getTaskCount() {
        if (mContext != null) {
            updateTaskCount(((MainActivity) mContext).getTaskCount());
        }
    }


    public void updateAppManagerRedPoint(int count) {
        if (count > 0) {
            mAppManagerRedPoint.setVisibility(View.VISIBLE);
            mAppManagerRedPoint.setText(String.valueOf(count));
            mAppManagerRedPoint.setBackgroundResource(count > 9 ? R.drawable.shape_red_rectangle : R.drawable.shape_red_circle);
        } else {
            mAppManagerRedPoint.setVisibility(View.GONE);
        }
    }

    public void updateTaskCount(int count) {
        if (count > 0) {
            mTaskCount.setVisibility(View.VISIBLE);
            mTaskCount.setText(String.valueOf(count));
            mTaskCount.setBackgroundResource(count > 9 ? R.drawable.shape_red_rectangle : R.drawable.shape_red_circle);
        } else {
            mTaskCount.setVisibility(View.GONE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        UserRedPointManager.getInstance().setNotInUserCenter();
    }


}
