package cn.lt.android.main.personalcenter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.SharePreferencesKey;
import cn.lt.android.autoinstall.AutoInstallerContext;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.event.RootIsCheckedEvent;
import cn.lt.android.install.root.PackageUtils;
import cn.lt.android.main.UIController;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.SharePreferenceUtil;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.ActionBar;
import cn.lt.android.widget.CustomDialog;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.RootCheckingDialogHolder;
import cn.lt.appstore.R;
import de.greenrobot.event.EventBus;

/**
 * Created by wenchao on 2016/3/16.
 * 设置页面
 */
public class SettingActivity extends BaseAppCompatActivity {

    public static final String TAG = "SettingActivity";

    private final String showExplain = "∨  展开说明";
    private final String closeExplain = "∧  收起说明";
    public static boolean rootInstallIsChecked = false;
    private boolean userWantRootInstall;

    @Override
    public void setPageAlias() {
        setmPageAlias(Constant.PAGE_APP_SETTING);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settting);
        EventBus.getDefault().register(this);
        setStatusBar();
        initView();
    }

    private ActionBar mActionBar;
    private TextView mLocation;
    private TextView tv_autoUpgradeExplain;
    private TextView tv_autoUpgradeExplainBtn;
    private ToggleButton btnAutoUpgradeApp;
    private ToggleButton mRootInstall;
    private ToggleButton mAutoDelete;
    private ToggleButton mAutoInstallByAccessibility;
    private View mDivider,mDividerMore;

    private void initView() {
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setTitle(getString(R.string.setting));

        mLocation = (TextView) findViewById(R.id.location);
        btnAutoUpgradeApp = (ToggleButton) findViewById(R.id.btn_auto_upgrade_app);
        mRootInstall = (ToggleButton) findViewById(R.id.root_install);
        mAutoDelete = (ToggleButton) findViewById(R.id.auto_delete);
        mAutoInstallByAccessibility = (ToggleButton) findViewById(R.id.auto_install_no_root);

        mDividerMore = findViewById(R.id.divider_more);
        mDivider = findViewById(R.id.divider);

        setRootInstallBtn();
        mAutoDelete.setChecked(GlobalConfig.getAutoDeleteApk(this));

        tv_autoUpgradeExplain = (TextView) findViewById(R.id.tv_autoUpgradeExplain);
        tv_autoUpgradeExplainBtn = (TextView) findViewById(R.id.tv_autoUpgradeExplainBtn);

        String downloadLocation = DownloadTaskManager.getInstance().getSaveDirPath();
        mLocation.setText(downloadLocation);

        setAutoUpgradeAppBtn();

        mAutoDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GlobalConfig.setAutoDeleteApk(SettingActivity.this, isChecked);
            }
        });


        mAutoInstallByAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先进入临时
                Intent intent=new Intent(SettingActivity.this, TempActivity.class);
                intent.putExtra("settings",mAutoInstallByAccessibility.isChecked());
                startActivity(intent);
//                AutoInstallerContext.goAccessiblity(SettingActivity.this, mAutoInstallByAccessibility.isChecked());
            }
        });

        tv_autoUpgradeExplainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tv_autoUpgradeExplainBtn.getText().toString().equals(closeExplain)) {
                    displayExplain(View.GONE, showExplain);
                } else {
                    displayExplain(View.VISIBLE, closeExplain);
                }
            }
        });
/*        mRootInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootInstall.setEnabled(false);
                if (mRootInstall.isChecked()) {
                    checkRoot();
                } else {
                    GlobalConfig.setRootInstall(SettingActivity.this, mRootInstall.isChecked());
                }
            }
        });*/
    }

    /** 设置root装按钮初始状态以及点击监听事件*/
    private void setRootInstallBtn() {

        // root已经检测完成 并且 设备有root权限
        if(rootInstallIsChecked && GlobalConfig.deviceIsRoot) {

            // 用户是否自己已经选择过
            if(GlobalConfig.getRootInstallUserIsChange(this)) {

                // 恢复用户自己的选择的设置
                mRootInstall.setChecked(GlobalConfig.getRootInstall(this));
            } else {

                // 默认开启root装
                mRootInstall.setChecked(true);
            }

        } else {

            // 不符合条件默认关闭root装
            mRootInstall.setChecked(false);
        }


        mRootInstall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 这里业务很复杂，慎改
                if(isChecked) {
                    if(rootInstallIsChecked && GlobalConfig.deviceIsRoot) {
                        openRootInstall();
                    } else {

                        mRootInstall.setChecked(false);

                        if(!rootInstallIsChecked) {

                            // root没检测完毕，弹窗提示正在检测
                            userWantRootInstall = true;
                            LTApplication.getMainThreadHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    new PublicDialog(SettingActivity.this, new RootCheckingDialogHolder()).showDialog(null);
                                }
                            });
                        } else {

                            // 已经检测完毕，但是设备没有root权限，弹窗提示下载root软件获取权限
                            LTApplication.getMainThreadHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    showNotRootDialog();
                                }
                            });
                        }
                    }
                } else {
                    mRootInstall.setChecked(false);
                    GlobalConfig.setRootInstall(SettingActivity.this, false);
                    GlobalConfig.setRootInstallUserIsChange(SettingActivity.this);
                }


            }
        });

    }

    private void openRootInstall() {
        mRootInstall.setChecked(true);
        GlobalConfig.setRootInstall(SettingActivity.this, true);
        GlobalConfig.setRootInstallUserIsChange(SettingActivity.this);
        ToastUtils.showToast("ROOT快速安装功能已开启");
    }

    private void showNotRootDialog() {
        new CustomDialog.Builder(SettingActivity.this)
                .setMessage("您的手机不支持快速安装功能，可下载ROOT功能，享受快速安装带来的极致体验")
                .setPositiveButton("去下载")
                .setPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIController.goSearchActivity(SettingActivity.this, "ROOT", false, "SettingPage","");
                    }
                })
                .setNegativeButton("取消").create().show();
    }

    private void setAutoUpgradeAppBtn() {
        RelativeLayout rl_autoUpgradeApp = (RelativeLayout) findViewById(R.id.rl_autoUpgradeApp);

        boolean isSystemApp = PackageUtils.isSystemApplication(LTApplication.shareApplication());
        String isBlacklist = (String) SharePreferenceUtil.getFromSpName(SharePreferencesKey.APP_AUTO_UPGRADE, SharePreferencesKey.IS_BLACKLIST, "no");
        LogUtils.i(LogTAG.appAutoUpgrade, "SettingActivity -- > isSystemApp = " + isSystemApp + ", isBlacklist = " + isBlacklist);

        // 有系统权限，并且不是黑名单才显示
        if(isSystemApp && (TextUtils.isEmpty(isBlacklist) || isBlacklist.equals("no"))) {
            LogUtils.i(LogTAG.appAutoUpgrade, "SettingActivity -- > 设置显示应用自动升级选项");
            rl_autoUpgradeApp.setVisibility(View.VISIBLE);
            tv_autoUpgradeExplain.setVisibility(View.VISIBLE);
            tv_autoUpgradeExplainBtn.setVisibility(View.VISIBLE);
            mDividerMore.setVisibility(View.VISIBLE);
            mDivider.setVisibility(View.VISIBLE);
            btnAutoUpgradeApp.setChecked(GlobalConfig.getIsOpenAutoUpgradeApp(this));
            btnAutoUpgradeApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    GlobalConfig.setIsOpenAutoUpgradeApp(SettingActivity.this, isChecked);
                    if(isChecked) {
                        displayExplain(View.GONE, showExplain);
                    } else {
                        displayExplain(View.VISIBLE, closeExplain);
                    }
                }
            });

            setAutoUpgradeAppExplain();


        } else {
            LogUtils.i(LogTAG.appAutoUpgrade, "SettingActivity -- > 隐藏应用自动升级选项");
            rl_autoUpgradeApp.setVisibility(View.GONE);
            tv_autoUpgradeExplain.setVisibility(View.GONE);
            tv_autoUpgradeExplainBtn.setVisibility(View.GONE);
            mDivider.setVisibility(View.GONE);
            mDividerMore.setVisibility(View.GONE);
        }

    }

    private void setAutoUpgradeAppExplain() {
        if(btnAutoUpgradeApp.isChecked()) {
            displayExplain(View.GONE, showExplain);
        } else {
            displayExplain(View.VISIBLE, closeExplain);
        }
    }

    private void displayExplain(int visibility, String text) {
        tv_autoUpgradeExplain.setVisibility(visibility);
        tv_autoUpgradeExplainBtn.setText(text);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (msg.arg1 == 1) {//获取root权限成功
                        ToastUtils.showToast("获取自动安装权限成功");
                        mRootInstall.setChecked(true);
                        GlobalConfig.setRootInstall(SettingActivity.this, true);
                    } else {//获取Root权限失败
                        mRootInstall.setChecked(false);
                        ToastUtils.showToast("获取自动安装权限失败");
                        GlobalConfig.setRootInstall(SettingActivity.this, false);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        boolean noRootInstall = AutoInstallerContext.getInstance().getAccessibilityStatus() == AutoInstallerContext.STATUS_OPEN;
//        boolean serviceRunning = ServiceUtil.isServiceRunning(this, AccessibilityService.class);
//        Log.d("accessbility","辅助服务是否在跑："+serviceRunning);
//        Log.d("accessbility","open的方式：辅助服务是否在跑："+noRootInstall);
        mAutoInstallByAccessibility.setChecked(noRootInstall);
    }

    /** 接收root权限检测完成事件通知*/
    public void onEventMainThread(RootIsCheckedEvent event) {
        if(userWantRootInstall) {
            if(GlobalConfig.deviceIsRoot) {
                openRootInstall();
            } else {
                showNotRootDialog();
            }
        }

    }



    private void checkRootResult(final boolean isSuccess) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRootInstall.setChecked(isSuccess);
                mRootInstall.setEnabled(true);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
