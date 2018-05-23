package cn.lt.android.main.personalcenter;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.GlobalParams;
import cn.lt.android.LTApplication;
import cn.lt.android.base.BaseAppCompatActivity;
import cn.lt.android.main.personalcenter.model.AppInfoBean;
import cn.lt.android.service.CoreService;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.ToastUtils;
import cn.lt.appstore.BuildConfig;
import cn.lt.appstore.R;

/**
 * Created by LinJunSheng
 * 应用市场后门信息
 */
public class AppInfoBackDoorActivity extends BaseAppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static String GeTui_Payload = "";
    private static final String TAG = "infoBackDoor";
    private List<AppInfoBean> infoList = new ArrayList<>();
    private ListView lv_appInfo;
    ViewGroup bannerContainer;
    BannerView bv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info_back_door);
        initView();
        getInfo();
        setData();
        this.initBanner();
//        this.bv.loadAD();
    }
    private void initBanner() {
        this.bv = new BannerView(this, ADSize.BANNER, Constant.APPID, Constant.BANNERID);
        // 注意：如果开发者的banner不是始终展示在屏幕中的话，请关闭自动刷新，否则将导致曝光率过低。
        // 并且应该自行处理：当banner广告区域出现在屏幕后，再手动loadAD。
        bv.setRefresh(30);
        bv.setADListener(new AbstractBannerADListener() {
            @Override
            public void onNoAD(int i) {
                LogUtils.i("GDTBanner","noAD");
            }

            @Override
            public void onADReceiv() {
                LogUtils.i("GDTBanner","onADReceiv");
            }
        });
        bannerContainer.addView(bv);
    }
    private void initView() {
        lv_appInfo = (ListView) findViewById(R.id.lv_appInfo);
        bannerContainer = (ViewGroup) this.findViewById(R.id.bannerContainer);
    }


    private void setData() {
        AppInfoAdapter adapter = new AppInfoAdapter(this, infoList);
        lv_appInfo.setAdapter(adapter);
        lv_appInfo.setOnItemClickListener(this);
        lv_appInfo.setOnItemLongClickListener(this);
    }

    private void getInfo() {
        getPushInfo();
        LogUtils.i(TAG, infoList.toString());
    }

    private void getPushInfo() {
        infoList.add(new AppInfoBean("versionName", BuildConfig.VERSION_NAME));
        infoList.add(new AppInfoBean("versionCode", String.valueOf(BuildConfig.VERSION_CODE)));
        infoList.add(new AppInfoBean("channel", GlobalConfig.CHANNEL));
        infoList.add(new AppInfoBean("Acenter_Host", GlobalParams.getHostBean().getAcenter_host()));
        infoList.add(new AppInfoBean("App_Host", GlobalParams.getHostBean().getApp_host()));
        infoList.add(new AppInfoBean("Image_Host", GlobalParams.getHostBean().getImage_host()));
        infoList.add(new AppInfoBean("buildTime", getString(R.string.build_time)));
        infoList.add(new AppInfoBean("GETUI_PUSH_CID", Constant.GeTuipushCID));
        infoList.add(new AppInfoBean("IMEI:", AppUtils.getIMEI(LTApplication.shareApplication())));
        infoList.add(new AppInfoBean("ANDROIDID:", AppUtils.getAndroidID(LTApplication.shareApplication())));
        infoList.add(new AppInfoBean("FY_CHANEL:", Constant.FY_CHANEL));
        infoList.add(new AppInfoBean("GDT_ID", Constant.APPID));
        infoList.add(new AppInfoBean("GDT_SPLASH_ID", Constant.SplashPosID));
        infoList.add(new AppInfoBean("BaiDu_SPLASH_ID", Constant.baiduSplashId));
        infoList.add(new AppInfoBean("OPEN_LOG", ""));
        infoList.add(new AppInfoBean("CLOSE_LOG", ""));
        infoList.add(new AppInfoBean("requestPush", ""));
    }


    @Override
    public void setPageAlias() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfoBean bean = infoList.get(position);
        if (bean == null) {
            return;
        }

        if (bean.getName().equals("CLOSE_LOG")) {
            LogUtils.mDebuggable = LogUtils.LEVEL_OFF;
            ToastUtils.showToast("日志已关闭");
        } else if (bean.getName().equals("OPEN_LOG")) {
            LogUtils.mDebuggable = LogUtils.LEVEL_ALL;
            ToastUtils.showToast("日志已开启");
        }else if (bean.getName().equals("requestPush")) {
            showRequestPushDialog();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfoBean bean = infoList.get(position);
        if (bean != null) {
            String value = bean.getValue();
            if (null != value && !"".equals(value)) {
                // 得到剪贴板管理器
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(bean.getValue().trim());// 复制到剪贴板
                if (cmb.getText().equals(bean.getValue().trim())) {
                    ToastUtils.showToast("已复制到剪贴板");
                } else {
                    ToastUtils.showToast("复制失败，请重试");
                }
            }
        }

        return false;
    }

    private void showRequestPushDialog() {
        try {

            final EditText editText = new EditText(this);
            AlertDialog.Builder inputDialog =
                    new AlertDialog.Builder(this);
            inputDialog.setTitle("push~ID").setView(editText);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String pushId = editText.getText().toString();
                            if (TextUtils.isEmpty(pushId) || !BuildConfig.DEBUG) {
                                ToastUtils.showToast("！！");
                                return;
                            }

                            startService(CoreService.getPushIntent(AppInfoBackDoorActivity.this, pushId));

                        }
                    }).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AppInfoAdapter extends BaseAdapter {

        private Context context;
        private LayoutInflater inflater;
        private List<AppInfoBean> list;

        public AppInfoAdapter(Context context, List<AppInfoBean> list) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            if (list == null) {
                list = new ArrayList<AppInfoBean>();
            } else {
                this.list = list;
            }
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;

            if (convertView == null) {
                vh = new ViewHolder();
                convertView = inflater.inflate(R.layout.app_info_item, null);
                vh.tv_appInfoName = (TextView) convertView.findViewById(R.id.tv_appInfoName);
                vh.tv_appInfoValue = (TextView) convertView.findViewById(R.id.tv_appInfoValue);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            AppInfoBean bean = list.get(position);
            vh.tv_appInfoName.setText(bean.getName());
            if (null == bean.getValue() || "".equals(bean.getValue())) {
                vh.tv_appInfoValue.setText("");
            } else {
                vh.tv_appInfoValue.setText(bean.getValue());
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tv_appInfoName;
            TextView tv_appInfoValue;
        }

    }
}
