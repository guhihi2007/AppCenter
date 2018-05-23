package cn.lt.android.push.getui;

import android.content.Context;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.message.FeedbackCmdMessage;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.igexin.sdk.message.SetTagCmdMessage;

import java.util.List;

import cn.lt.android.Constant;
import cn.lt.android.LogTAG;
import cn.lt.android.entity.ClientInstallInfo;
import cn.lt.android.network.NetWorkClient;
import cn.lt.android.network.netdata.bean.HostType;
import cn.lt.android.push.CQPushManager;
import cn.lt.android.util.AppUtils;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.NetUtils;
import cn.lt.android.util.PopWidowManageUtil;
import cn.lt.framework.util.PreferencesUtils;
import retrofit2.Call;
import retrofit2.Response;

/**
 * 继承 GTIntentService 接收来自个推的消息, 所有消息在线程中回调, 如果注册了该服务, 则务必要在 AndroidManifest中声明, 否则无法接受消息<br>
 * onReceiveMessageData 处理透传消息<br>
 * onReceiveClientId 接收 cid <br>
 * onReceiveOnlineState cid 离线上线通知 <br>
 * onReceiveCommandResult 各种事件处理回执 <br>
 */
public class GeTuiIntentService extends GTIntentService {
    public GeTuiIntentService() {

    }

    /**
     * 为了观察透传数据变化.
     */
    private static int cnt;

    @Override
    public void onReceiveServicePid(Context context, int pid) {
        LogUtils.d(LogTAG.PushTAG, "onReceiveServicePid -> " + pid);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage msg) {
//        String appid = msg.getAppid();
//        String taskid = msg.getTaskId();
//        String messageid = msg.getMessageId();
        byte[] payload = msg.getPayload();
//        String pkg = msg.getPkgName();
//        String cid = msg.getClientId();

        // 第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
//        boolean result = PushManager.getInstance().sendFeedbackMessage(context, taskid, messageid, 90001);
//        LogUtils.d(LogTAG.PushTAG, "call sendFeedbackMessage = " + (result ? "success" : "failed"));
//
//        LogUtils.d(LogTAG.PushTAG, "onReceiveMessageData -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nmessageid = " + messageid + "\npkg = " + pkg
//                + "\ncid = " + cid);

        if (payload == null) {
            LogUtils.i(LogTAG.PushTAG, "receiver payload = null");
        } else {
            String data = new String(payload);
            CQPushManager.sendPushByPayload(context, data);
        }
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        LogUtils.d(LogTAG.PushTAG, "onReceiveClientId -> " + "clientid = " + clientid);
        Constant.GeTuipushCID = clientid;
        if (PopWidowManageUtil.needPostLoacalData(context)) {
            postData(context, clientid);
        }
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean online) {
        LogUtils.d(LogTAG.PushTAG, "onReceiveOnlineState -> " + (online ? "online" : "offline"));
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage cmdMessage) {
        LogUtils.d(LogTAG.PushTAG, "onReceiveCommandResult -> " + cmdMessage);

        int action = cmdMessage.getAction();

        if (action == PushConsts.SET_TAG_RESULT) {
            setTagResult((SetTagCmdMessage) cmdMessage);
        } else if ((action == PushConsts.THIRDPART_FEEDBACK)) {
            feedbackResult((FeedbackCmdMessage) cmdMessage);
        }
    }

    private void setTagResult(SetTagCmdMessage setTagCmdMsg) {
        String sn = setTagCmdMsg.getSn();
        String code = setTagCmdMsg.getCode();

        String text = "设置标签失败, 未知异常";
        switch (Integer.valueOf(code)) {
            case PushConsts.SETTAG_SUCCESS:
                text = "设置标签成功";
                break;

            case PushConsts.SETTAG_ERROR_COUNT:
                text = "设置标签失败, tag数量过大, 最大不能超过200个";
                break;

            case PushConsts.SETTAG_ERROR_FREQUENCY:
                text = "设置标签失败, 频率过快, 两次间隔应大于1s且一天只能成功调用一次";
                break;

            case PushConsts.SETTAG_ERROR_REPEAT:
                text = "设置标签失败, 标签重复";
                break;

            case PushConsts.SETTAG_ERROR_UNBIND:
                text = "设置标签失败, 服务未初始化成功";
                break;

            case PushConsts.SETTAG_ERROR_EXCEPTION:
                text = "设置标签失败, 未知异常";
                break;

            case PushConsts.SETTAG_ERROR_NULL:
                text = "设置标签失败, tag 为空";
                break;

            case PushConsts.SETTAG_NOTONLINE:
                text = "还未登陆成功";
                break;

            case PushConsts.SETTAG_IN_BLACKLIST:
                text = "该应用已经在黑名单中,请联系售后支持!";
                break;

            case PushConsts.SETTAG_NUM_EXCEED:
                text = "已存 tag 超过限制";
                break;

            default:
                break;
        }

        LogUtils.d(LogTAG.PushTAG, "settag result sn = " + sn + ", code = " + code + ", text = " + text);
    }

    private void feedbackResult(FeedbackCmdMessage feedbackCmdMsg) {
        String appid = feedbackCmdMsg.getAppid();
        String taskid = feedbackCmdMsg.getTaskId();
        String actionid = feedbackCmdMsg.getActionId();
        String result = feedbackCmdMsg.getResult();
        long timestamp = feedbackCmdMsg.getTimeStamp();
        String cid = feedbackCmdMsg.getClientId();

        LogUtils.d(LogTAG.PushTAG, "onReceiveCommandResult -> " + "appid = " + appid + "\ntaskid = " + taskid + "\nactionid = " + actionid + "\nresult = " + result + "\ncid = " + cid + "\ntimestamp = " + timestamp);
    }


    private void postData(final Context context, final String mCid) {
        final String localAppList = AppUtils.getLocalParams();
        final String netType = NetUtils.getNetworkType(context);
        final String netOperator = AppUtils.getNetworkOperator(context);
        final ClientInstallInfo clientInstallInfo = AppUtils.getClientInstallInfo();
        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCls(List.class).setCallback(new retrofit2.Callback<List>() {
            @Override
            public void onResponse(Call<List> call, Response<List> response) {
                PreferencesUtils.putLong(context, Constant.POST_CID_PERIOD, System.currentTimeMillis());
                LogUtils.i(TAG, "上报个推ＩＤ请求成功" + mCid);
            }

            @Override
            public void onFailure(Call<List> call, Throwable t) {
                LogUtils.i(TAG, "上报个推ＩＤ请求失败");
            }
        }).bulid().postLocalData(mCid, "getui", localAppList, netType, netOperator, clientInstallInfo.getLast_upgrade_time(), clientInstallInfo.getInstall_time());

    }

}
