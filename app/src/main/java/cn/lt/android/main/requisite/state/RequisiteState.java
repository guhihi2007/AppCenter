package cn.lt.android.main.requisite.state;

import android.app.Dialog;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.db.AppEntity;
import cn.lt.android.download.DownloadChecker;
import cn.lt.android.download.DownloadTaskManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.NecessaryBean;
import cn.lt.android.main.requisite.RequisiteActivity;
import cn.lt.android.main.requisite.adapter.GridAdapter;
import cn.lt.android.util.ActivityManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.IntegratedDataUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.android.widget.CustomDialog;
import cn.lt.appstore.R;

/***
 * Created by dxx on 2016/3/10.
 *
 * @desc 装机必备视图
 */
@SuppressWarnings("ALL")
public class RequisiteState extends IState<NecessaryBean> {

    private GridView mGridView;

    private View mCancelView;

    private Button mDownloadBt;

    private List<RequisiteActivity.RequisiteItem> mDataList;

    private GridAdapter mAdapter;

    private ImageView mEmptyExit;

    private ImageView mNetTip;

    private TextView mNetTypeTip;

    private TextView mTitle;

    private boolean hasClicked = true;

    private String title;

    public RequisiteState(String title) {
        this.title = title;
    }

    private void setButtonState() {
        int i = 0;
        List<AppDetailBean> list = getDownloadList();
        if (getNeedToDownLoadCount() == 0) {
            mDownloadBt.setBackgroundResource(R.drawable.requesite_button_no_slect);
            mDownloadBt.setText("一键下载");
        } else {
            mDownloadBt.setBackgroundResource(R.drawable.appdetail_downloadbar_selector);
            long size = 0;
            for (AppDetailBean bean : list) {
                if (!AppUtils.isInstalled(bean.getPackage_name()) || hasInstalledButNeedToUpdate(bean) == true) {
                    LogUtils.d("asd","准备加的包大小。。==>"+bean.getPackage_name());
                    i++;
                    try {
                        size += Long.valueOf(bean.getPackage_size());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
            mDownloadBt.setText("一键下载(" + i + "个,共" + IntegratedDataUtil.calculateSizeMB(size) + ")");
        }
    }


    /**
     * 获取需要下载的数量
     *
     * @return boolean
     */
    private int getNeedToDownLoadCount() {
        List<AppDetailBean> list = getDownloadList();
        int i = 0;
        for (AppDetailBean bean : list) {
            if (!AppUtils.isInstalled(bean.getPackage_name()) || hasInstalledButNeedToUpdate(bean)) {
                i++;
            }
        }
        return i;
    }

    /**
     * 判斷已經安裝是否需要升級；
     *
     * @return boolean
     */
    private boolean hasInstalledButNeedToUpdate(AppDetailBean appBean) {
        List<AppDetailBean> upgradeList = UpgradeListManager.getInstance().getUpgradeAppList();
        if (AppUtils.isInstalled(appBean.getPackage_name())) {
            for (AppDetailBean bean : upgradeList) {
                if (bean.getPackage_name().equals(appBean.getPackage_name())) {
                    LogUtils.d("asd","安装但是需升级的包名==>"+bean.getPackage_name());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取需要下载的游戏；
     *
     * @return
     */
    private List<AppDetailBean> getDownloadList() {
        List downLoadList = null;
        if (mDataList != null) {
            downLoadList = new ArrayList<>();
            for (int i = 0; i < mDataList.size(); i++) {
                RequisiteActivity.RequisiteItem item = mDataList.get(i);
                if (item.isChecked()) {
                    downLoadList.add(item.getGameDetail());
                }
            }
        }
        return downLoadList;
    }

    /**
     * 初始话GridView；
     */
    private void initGridView() {
        if (mDataList == null || mDataList.size() == 0) {
            if (mDiaglogWeak.get() != null) {
                mDiaglogWeak.get().cancel();
            }
        }
        mAdapter = new GridAdapter(mDataList);
        mGridView.setAdapter(mAdapter);
        setButtonState();
        mGridView.setOnItemClickListener(this);
    }

    /**
     * 下载已经选中的应用
     */
    private boolean downloadForCheckedApp(boolean isOrderWifiDownload) {
        try {
            final List<AppDetailBean> downLoadList = getDownloadList();
            if (downLoadList == null || downLoadList.size() == 0) {
                return false;
            }
            List<AppEntity> appEntityList = new ArrayList<>();
            for (AppDetailBean bean : downLoadList) {
                if (!AppUtils.isInstalled(bean.getPackage_name()) || hasInstalledButNeedToUpdate(bean)) {
                    AppEntity appEntity = bean.getDownloadAppEntity();
                    // 是否预约wifi下载
                    appEntity.setIsOrderWifiDownload(isOrderWifiDownload);
                    appEntityList.add(appEntity);
                }
            }
            DownloadTaskManager.getInstance().startAfterCheckList(mGridView.getContext(), appEntityList, "onekey", "request", Constant.PAGE_BIBEI, "","","onekey_download");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public void fillData(NecessaryBean info) {
        coverntToRequisiteItem(info);
    }

    private void initView() {
        mGridView = (GridView) mDiaglogWeak.get().findViewById(R.id.gv_content_requisite);
        mCancelView = mDiaglogWeak.get().findViewById(R.id.bt_cancel_requisite);
        mDownloadBt = (Button) mDiaglogWeak.get().findViewById(R.id.bt_download_requisite);
        mRootView = mDiaglogWeak.get().findViewById(R.id.rl_root);
        mEmptyExit = (ImageView) mDiaglogWeak.get().findViewById(R.id.empty);
        mNetTypeTip = ((TextView) mDiaglogWeak.get().findViewById(R.id.tv_prompt));
        mTitle = ((TextView) mDiaglogWeak.get().findViewById(R.id.tv_title_requisite));
        mNetTip = ((ImageView) mDiaglogWeak.get().findViewById(R.id.iv_net));
        if (!NetUtils.isWifi(LTApplication.shareApplication())) {
            mNetTypeTip.setText("当前处于2G/3G/4G环境");
            mNetTip.setImageResource(R.mipmap.ic_traffic);
        }

        mCancelView.setOnClickListener(this);
        mDownloadBt.setOnClickListener(this);
        mEmptyExit.setOnClickListener(this);
    }

    @Override
    public void setContentView(Dialog dialog) {
        this.mDiaglogWeak = new WeakReference<>(dialog);
        dialog.setContentView(R.layout.activity_requisite);
        initView();
        initGridView();
        mTitle.setText(TextUtils.isEmpty(title) ? "装机必备" : title);
    }

    @Override
    public void onClick(View v) {
        boolean isColse = true;
        int id = v.getId();
        switch (id) {
            case R.id.bt_cancel_requisite:
                if (mDiaglogWeak.get() != null) {
                    mDiaglogWeak.get().cancel();
                }
                break;
            case R.id.empty:
                if (mDiaglogWeak.get() != null) {
                    mDiaglogWeak.get().cancel();
                }
                break;

            case R.id.bt_download_requisite:
                //1秒内再次点击一键下载无效
                if (hasClicked) {
                    performOnekeydownload(v);
                    hasClicked = false;
                    LTApplication.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hasClicked = true;
                        }
                    }, 1000);
                }
                break;

        }
    }

    private void performOnekeydownload(View v) {
        int downLoadCount = 0;
        downLoadCount = getNeedToDownLoadCount();
        //没有任务
        if (downLoadCount == 0) {
            ToastUtils.showToast("请选择应用");
            return;
        }
        //无网络弹框
        if (!NetUtils.isConnected(v.getContext())) {
            new DownloadChecker().noNetworkPromp(v.getContext(), new Runnable() {
                @Override
                public void run() {
                    downloadForCheckedApp(true);
                    if (mDiaglogWeak.get() != null) {
                        mDiaglogWeak.get().cancel();
                    }
                }
            });

            return;
        }
        if (NetUtils.isMobileNet(v.getContext())) {
            new CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？").setPositiveButton(R.string.continue_mobile_download).setPositiveListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadForCheckedApp(false);
                    if (mDiaglogWeak.get() != null) {
                        mDiaglogWeak.get().cancel();
                    }
                }
            }).setNegativeButton(R.string.order_wifi_download).setNegativeListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadForCheckedApp(true);
                    if (mDiaglogWeak.get() != null) {
                        mDiaglogWeak.get().cancel();
                    }
                }
            }).create().show();

        } else {
            ToastUtils.showToast("已加入任务管理进行下载！");
            downloadForCheckedApp(false);
            if (mDiaglogWeak.get() != null) {
                mDiaglogWeak.get().cancel();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GridAdapter.GameHolder holder = (GridAdapter.GameHolder) view.getTag();
        holder.view.switchCheckView();
        setButtonState();
    }

    /**
     * 转换数据；
     *
     * @param game s
     */
    private void coverntToRequisiteItem(NecessaryBean game) {
        if (game != null) {
            try {
                List<AppDetailBean> games = game.getApps();
                DownloadTaskManager.getInstance().transfer(games);
                List<AppEntity> baseGames = DownloadTaskManager.getInstance().getAll();
                if (mDataList == null) {
                    mDataList = new ArrayList<>();
                }
                mDataList.clear();
                for (int i = 0; i < games.size(); i++) {
                    boolean isInProgress = false;
                    AppDetailBean tempGame = games.get(i);
                    for (int j = 0; j < baseGames.size(); j++) {
                        if (tempGame.getPackage_name().equals(baseGames.get(j).getPackageName())) {
                            isInProgress = true;
                            break;
                        }
                    }
                    mDataList.add(new RequisiteActivity.RequisiteItem(tempGame, isInProgress));
                }
            } catch (RemoteException re) {
                re.printStackTrace();
                return;
            }
        }
    }
}