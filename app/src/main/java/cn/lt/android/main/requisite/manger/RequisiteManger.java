package cn.lt.android.main.requisite.manger;

import android.content.Context;

import com.yolanda.nohttp.rest.SimpleResponseListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.lt.android.Constant;
import cn.lt.android.LTApplication;
import cn.lt.android.LogTAG;
import cn.lt.android.ads.bean.wdj.AdsImageBean;
import cn.lt.android.ads.wanka.WanKaManager;
import cn.lt.android.download.UpgradeListManager;
import cn.lt.android.entity.AppDetailBean;
import cn.lt.android.entity.NecessaryBean;
import cn.lt.android.event.ShowFloatAdEvent;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.main.requisite.RequisiteActivity;
import cn.lt.android.main.requisite.state.PopularizeState;
import cn.lt.android.main.requisite.state.RequisiteState;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.android.widget.dialog.DataInfo;
import cn.lt.framework.util.PreferencesUtils;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 弹窗推广图管理
 */
public class RequisiteManger {

    private Context mContext;

    private RequisiteActivity mRequisiteView;


    public RequisiteManger(Context context) {
        this.mContext = context;
    }


    /**
     * 加载数据；
     * 只有在首页才允许弹装机必备框
     */
    public void requestData(boolean isIndex) {
        if (isIndex) {
            NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(new Callback<BaseBean>() {
                @Override
                public void onResponse(Call<BaseBean> call, Response<BaseBean> response) {
                    try {
                        final BaseBean bean = response.body();
                        if (bean != null) {
                            if (PopWidowManageUtil.needShowSpreadDialog(mContext)) {
                                // TODO 曝光玩咖长尾
                                final List<BaseBean> appList = new ArrayList<>();
                                if (bean instanceof NecessaryBean) {
                                    appList.addAll(((NecessaryBean) bean).getApps());
                                } else if (bean instanceof AdsImageBean) {
                                    appList.add(bean);
                                }

//                                String tempStatus = Constant.WK_SWITCH;
//                                Constant.WK_SWITCH = Constant.STATUS_OPEN;
                                boolean tempStatus = PreferencesUtils.getBoolean(LTApplication.instance,Constant.WK_SWITCH);
                                PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,true);
                                Set<String> exposureApps = WanKaManager.exposureApps(appList, new SimpleResponseListener<JSONObject>() {
                                    @Override
                                    public void onSucceed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                                        showView(bean);
                                    }

                                    @Override
                                    public void onFailed(int what, com.yolanda.nohttp.rest.Response<JSONObject> response) {
                                        showView(bean);
                                    }
                                },"精选必玩-装机必备曝光");
//                                Constant.WK_SWITCH = tempStatus;
                                PreferencesUtils.putBoolean(LTApplication.instance,Constant.WK_SWITCH,tempStatus);

                                if (exposureApps.size() == 0) {
                                    showView(bean);
                                }
                            } else {
                                LogUtils.i(LogTAG.floatAdTAG, "执行浮层广告请求");
                                // 执行浮层广告请求
//                                executeFloatAdCallback();
                                EventBus.getDefault().post(new ShowFloatAdEvent());
                            }
                        } else {
                            LogUtils.i(LogTAG.floatAdTAG, "执行浮层广告请求");
                            // 执行浮层广告请求
//                            executeFloatAdCallback();
                            EventBus.getDefault().post(new ShowFloatAdEvent());

                        }
                        LogUtils.i("GOOD", "装机必备网络请求成功！");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<BaseBean> call, Throwable t) {
                    LogUtils.i("GOOD", "装机必备网络请求失败");
                }

            }).bulid().requestNecessary(-1);
        }
    }

    private File getFile(String fileName) {
        File file = mContext.getCacheDir();
        String filePath = file.getAbsolutePath() + File.separator + fileName + ".txt";
        return new File(filePath);
    }

//    public boolean delData() {
        //        return FileUtil.delFile(getFile(Uri.GAME_CHOICE_URI.hashCode() + ""));
//        return false;
//    }

    public void showView(BaseBean baseBean) {
        if (mRequisiteView == null) {
            if (PresentType.necessary_apps.presentType.equals(baseBean.getLtType())) {
                String title = ((NecessaryBean) baseBean).getTitle();
                mRequisiteView = new RequisiteActivity(mContext, new RequisiteState(title));
                List<AppDetailBean> appList = ((NecessaryBean) baseBean).getApps();
                if (appList.size() != getUnClickableNumber(baseBean)) {
                    mRequisiteView.startActivity(new DataInfo<>(baseBean));
                }
            } else if (PresentType.ads_image_popup.presentType.equals(baseBean.getLtType())) {
                mRequisiteView = new RequisiteActivity(mContext, new PopularizeState(mContext));
                mRequisiteView.startActivity(new DataInfo<>(baseBean));
            }
        }
    }

    private int getUnClickableNumber(BaseBean apps) {
        int number = 0;
        List<AppDetailBean> appList = ((NecessaryBean) apps).getApps();
        List<AppDetailBean> upgradeList = UpgradeListManager.getInstance().getUpgradeAppList();
        for (AppDetailBean app : appList) {

            for (AppDetailBean upgradeBean : upgradeList) {
                if (upgradeBean.getPackage_name().equals(app.getPackage_name())) {
                    number++;
                    break;
                }
            }
        }
        return number;
    }

}