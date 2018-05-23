package cn.lt.android.plateform.update.manger;

import android.content.Context;
import android.content.DialogInterface;

import java.io.File;

import cn.lt.android.entity.PlatVersionBean;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.dao.NetDataInterfaceDao;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.plateform.update.UpdateUtil;
import cn.lt.android.plateform.update.entiy.VersionInfo;
import cn.lt.android.util.FileUtil;
import cn.lt.android.util.LogUtils;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.android.widget.dialog.PublicDialog;
import cn.lt.android.widget.dialog.holder.UpdateHolder;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/***
 * Created by Administrator on 2015/12/18.
 */
@SuppressWarnings("ALL")
public class VersionCheckManger {

    private Context mContext;
    private boolean needShowRedPointAtSearchBar;

    /**
     * 是否在下载任务管理器中显示升级
     */
    private boolean isShowUpgradeInAppDownload = true;
    /**
     * 是否正在升级
     */
    private boolean isUpgrading = false;
    /**
     * 待升级版本信息对象；
     */
    private VersionInfo mVersionInfo;

    /**
     * 判断升级弹窗是否已经弹出来了
     */
//    private boolean updateDialogIsShow = false;
    private VersionCheckManger() {
    }

    public static VersionCheckManger getInstance() {
        return VersionCheckMangerHolder.mInstance;
    }

    public VersionInfo getmVersionInfo() {
        return mVersionInfo;
    }

    public void setmVersionInfo(VersionInfo mVersionInfo) {
        this.mVersionInfo = mVersionInfo;
    }

    public boolean isNeedShowRedPointAtSearchBar() {
        return needShowRedPointAtSearchBar;
    }

    public void setNeedShowRedPointAtSearchBar(boolean needShowRedPointAtSearchBar) {
        this.needShowRedPointAtSearchBar = needShowRedPointAtSearchBar;
        EventBus.getDefault().post(new PlatUpdatePrompteType(this.needShowRedPointAtSearchBar));
    }

    public VersionCheckManger init(Context context) {
        mContext = context;
        return this;
    }

    private NetDataInterfaceDao updateDao;

    public void checkVerison(final VersionCheckCallback callback, final boolean isUserBehavior) {
        updateDao = NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<PlatVersionBean>() {
            @Override
            public void onResponse(Call<PlatVersionBean> call, Response<PlatVersionBean> response) {
                VersionCheckCallback.Result result = null;
                try {
                    PlatVersionBean bean = response.body();
                    if (bean != null) {
                        checkVersion(bean);
                        result = VersionCheckCallback.Result.have;
                    } else {
                        result = VersionCheckCallback.Result.none;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = VersionCheckCallback.Result.fail;
                }
                if (callback != null) {
                    callback.callback(result, mVersionInfo);
                }
            }

            @Override
            public void onFailure(Call<PlatVersionBean> call, Throwable t) {
                LogUtils.i("UpdateService", "检查平台版本更新网络请求失败");
                //                        ToastUtils.showToast("网络请求失败，请稍后再试！");
                if (callback != null) {
                    callback.callback(VersionCheckCallback.Result.fail, null);
                }
            }

        }).bulid();
        if (isUserBehavior) {
            LogUtils.i("UpdateService", "用户行为检查版本升级");
            updateDao.checkPlatformUpdate();
        } else {
            LogUtils.i("UpdateService", "服务检查版本升级");
            updateDao.requestPlatformUpdate();
        }
    }


    /* 检查是否有版本更新 */
    private void checkVersion(PlatVersionBean bean) {
        if (bean == null) {
            // 没有新版本
            FileUtil.delFile(new File(UpdatePathManger.getDownloadFilePath(mContext)));
            UpdateUtil.saveDialogShowThisTime(mContext, 0);
        } else {
            mVersionInfo = new VersionInfo(bean);
            PreferencesUtils.putString(mContext, "MD5", bean.getPackage_md5());//保存MD5值
            PreferencesUtils.putString(mContext, "newVersionCode", bean.getVersion_code());
            setNeedShowRedPointAtSearchBar(true);
            UpdateUtil.setShowRedPoint(mContext, true);
        }
    }

    public void showUpdateDialog(Context context, boolean isIndex) {
        new PublicDialog(context, new UpdateHolder(context, isIndex)).setOnTheDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        }).showDialog(new DataInfo(mVersionInfo));
    }

    public boolean isShowUpgradeInAppDownload() {
        return isShowUpgradeInAppDownload;
    }

    public void setShowUpgradeInAppDownload(boolean showUpgradeInAppDownload) {
        isShowUpgradeInAppDownload = showUpgradeInAppDownload;
    }

    public boolean isUpgrading() {
        return isUpgrading;
    }

    public void setUpgrading(boolean upgrading) {
        isUpgrading = upgrading;
    }

    private VersionCheckCallback.Result setNoneVersion() {
        VersionCheckCallback.Result result;
        UpdateUtil.setShowRedPoint(mContext, false);
        setNeedShowRedPointAtSearchBar(false);
        result = VersionCheckCallback.Result.none;
        return result;
    }

    public interface VersionCheckCallback {
        /**
         * 此方法为版本请求完成后用来通知调用方的回调；
         * 收到回调之后请移除此回调；
         *
         * @param result
         */
        public void callback(Result result, VersionInfo info);

        public enum Result {
            /**
             * 有新版本；
             */
            have, /**
             * 无更新；
             */
            none, /**
             * 请求失败；
             */
            fail;
        }
    }

    private static class VersionCheckMangerHolder {
        private static VersionCheckManger mInstance = new VersionCheckManger();
    }

    public static class PlatUpdatePrompteType {
        /**
         * 是否需要显示小红点；
         */
        public boolean show;

        public PlatUpdatePrompteType(boolean flag) {
            this.show = flag;
        }
    }

}
