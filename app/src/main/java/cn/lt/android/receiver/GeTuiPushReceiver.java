/*
package cn.lt.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.igexin.sdk.PushConsts;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LogTAG;
import cn.lt.android.main.personalcenter.AppInfoBackDoorActivity;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.notification.PushPayloadParser;
import cn.lt.android.service.CoreService;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.framework.util.PreferencesUtils;
import retrofit2.Call;
import retrofit2.Response;

import static cn.lt.android.util.AppUtils.getLocalParams;

*/
/**
 * Created by 林俊生 on 2016/1/20.
 *//*

public class GeTuiPushReceiver extends BroadcastReceiver {

    public final static String TAG = "LTGeTuiPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        LogUtils.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
        LogUtils.i("XiaomiTUI", "收到个推推送");
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用通常需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送。
                // 部分特殊情况下CID可能会发生变化，为确保应用服务端保存的最新的CID，应用程序在每次获取CID广播后，如果发现CID出现变化，需要重新进行一次关联绑定
                String cid = bundle.getString("clientid");
                LogUtils.d(LogTAG.PushTAG, "Got CID:" + cid);
                AppInfoBackDoorActivity.GeTuipushCID = cid;
                if (PopWidowManageUtil.needPostLoacalData(context)) {
                    postData(context, cid);
                }
                break;
            case PushConsts.GET_MSG_DATA:
                // 获取透传（payload）数据
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String payloadString = new String(payload);
                    String pushId = PushPayloadParser.parse(payloadString);
                    LogUtils.d(LogTAG.PushTAG, "Got Payload : " + pushId);
                    AppInfoBackDoorActivity.GeTui_Payload = pushId;
                    // TODO:接收处理透传（payload）数据
                    try {
                        if (!pushId.equals(PushPayloadParser.NEVER_PUSH)) {
                            context.startService(CoreService.getPushIntent(context, pushId));
                        } else {
                            parserOpenLog(payloadString);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void postData(final Context context, final String mCid) {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCls(List.class).setCallback(new retrofit2.Callback<List>() {
            @Override
            public void onResponse(Call<List> call, Response<List> response) {
                PreferencesUtils.putLong(context, Constant.POST_CID_PERIOD, System.currentTimeMillis());
                Log.i(TAG, "上报个推ＩＤ请求成功" + mCid);
            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                Log.i(TAG, "上报个推ＩＤ请求失败");
            }
        }).bulid().postLocalData(mCid, "getui", getLocalParams());

    }

    private void parserOpenLog(String payloadString) {
        String openLog = PushPayloadParser.parseOpenLog(payloadString);
        if (!TextUtils.isEmpty(openLog)) {
            if (openLog.equals(PushPayloadParser.ON)) {
                LogUtils.mDebuggable = LogUtils.LEVEL_ALL;
            }

            if (openLog.equals(PushPayloadParser.OFF)) {
                LogUtils.mDebuggable = LogUtils.LEVEL_OFF;
            }
        }
    }


}
*/
