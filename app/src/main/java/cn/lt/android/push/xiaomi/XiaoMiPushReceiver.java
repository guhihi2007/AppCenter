/*
package cn.lt.android.push.xiaomi;

import android.content.Context;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.GlobalConfig;
import cn.lt.android.LogTAG;
import cn.lt.android.main.personalcenter.AppInfoBackDoorActivity;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.push.CQPushManager;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.MetaDataUtil;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.framework.util.PreferencesUtils;
import retrofit2.Call;
import retrofit2.Response;

import static cn.lt.android.util.AppUtils.getLocalParams;

*/
/**
 * Created by 林俊生 on 2016/1/19.
 *//*

public class XiaoMiPushReceiver extends PushMessageReceiver {

    private String regId;

    */
/**
     * 接收服务器发来的通知栏消息（消息到达客户端时触发，并且可以接收应用在前台时不弹出通知的通知消息）
     *//*

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        super.onNotificationMessageArrived(context, message);
    }

    */
/**
     * 接收服务器发送的透传消息
     *//*

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        super.onReceivePassThroughMessage(context, message);
        LogUtils.i(LogTAG.PushTAG, "收到透传消息~~" + message.toString());
        LogUtils.i("XiaomiTUI", "收到小米推送");
        String payloadString = message.getContent();

        if (payloadString == null) {
            LogUtils.i(LogTAG.PushTAG, "receiver payload = null");
        } else {
            CQPushManager.sendPushByPayload(context, payloadString);
        }
    }

    private void postData(final Context context, final String mCid) {
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCls(List.class).setCallback(new retrofit2.Callback<List>() {
            @Override
            public void onResponse(Call<List> call, Response<List> response) {
                PreferencesUtils.putLong(context, Constant.POST_CID_PERIOD, System.currentTimeMillis());
                LogUtils.i(LogTAG.PushTAG, "小米推送ID请求成功=" + mCid);
            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                LogUtils.i(LogTAG.PushTAG, "小米推送ＩＤ请求失败");
            }
        }).bulid().postLocalData(mCid, "xiaomi", getLocalParams());

    }

    */
/**
     * 接收服务器发来的通知栏消息（用户点击通知栏时触发）
     *//*

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        super.onNotificationMessageClicked(context, message);
        LogUtils.i(LogTAG.PushTAG, "点击推送啦~~" + "title = " + message.getTitle() +
                ", content = " + message.getDescription());
    }


    */
/**
     * 接收客户端向服务器发送命令消息后返回的响应
     *//*

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        super.onCommandResult(context, message);
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                String mAlias = arguments.get(0);
                AppInfoBackDoorActivity.XiaoMi_alias = mAlias;
                LogUtils.i(LogTAG.PushTAG, "小米别名注册成功！， mAlias = " + mAlias);
            } else {
                LogUtils.i(LogTAG.PushTAG, "小米别名注册失败！");
            }
        }

        if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                String topic = arguments.get(0);
                AppInfoBackDoorActivity.XiaoMi_topic = topic;
                LogUtils.i(LogTAG.PushTAG, "小米标签注册成功！， topic = " + topic);
            } else {
                LogUtils.i(LogTAG.PushTAG, "小米标签注册失败！");
            }
        }

        if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                String topic = arguments.get(0);
                AppInfoBackDoorActivity.XiaoMi_topic = topic;
                LogUtils.i(LogTAG.PushTAG, "小米取消标签成功！， topic = " + topic);
            } else {
                LogUtils.i(LogTAG.PushTAG, "小米取消标签失败！");
            }
        }
    }

    */
/**
     * 接受客户端向服务器发送注册命令消息后返回的响应
     *//*

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        super.onReceiveRegisterResult(context, message);

        LogUtils.i(LogTAG.PushTAG, "onReceiveRegisterResult is called. " + message.toString());
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                regId = cmdArg1;
                log = "小米推送注册成功， " + "regId = " + regId;
                AppInfoBackDoorActivity.XiaoMiRegistId = regId;
                // 设置别名（必须在注册成功后方可调用），一个id可以设置多个别名
                setAlias(context);
//                setTopic(context);
                if (PopWidowManageUtil.needPostLoacalData(context)) {
                    postData(context, regId);
                }
            } else {
                log = "小米推送注册失败";
            }
        } else {
            log = message.getReason();
        }

        LogUtils.i(LogTAG.PushTAG, log);
    }

    // 设置标签
    private void setTopic(Context context) {
        String topic = MetaDataUtil.getMetaData("XiaoMi_Push_topic");
        LogUtils.i(LogTAG.PushTAG, "配置文件中得到的XiaoMi_Push_topic = " + topic);
        if (!"".equals(topic) && null != topic) {
            if (topic.equals("whetherBusiness")) {
                MiPushClient.subscribe(context, topic, null);
            }
            if (topic.equals("noTopic")) {
                MiPushClient.unsubscribe(context, "whetherBusiness", null);
            }
        } else {
            MiPushClient.unsubscribe(context, "whetherBusiness", null);
        }
    }

    // 设置别名
    private void setAlias(Context context) {
        String alias = "";

        // 去掉之前注册过的别名
        if (GlobalConfig.DEBUG) {
            MiPushClient.unsetAlias(context, "release", null);
            alias = "whetherBusiness";
        } else {
            MiPushClient.unsetAlias(context, "whetherBusiness", null);
            alias = "release";

        }

        LogUtils.i(LogTAG.PushTAG, "将要注册的别名是 = " + alias);

        MiPushClient.setAlias(context, alias, null);
    }
}
*/
