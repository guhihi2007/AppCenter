package cn.lt.android.push;

import android.content.Context;

import cn.lt.android.LogTAG;
import cn.lt.android.main.personalcenter.AppInfoBackDoorActivity;
import cn.lt.android.notification.PushPayloadParser;
import cn.lt.android.service.CoreService;
import cn.lt.android.util.LogUtils;

/**
 * Created by JohnsonLin on 2017/4/20.
 * 推送管理类
 */

public class CQPushManager {

    /** 处理推送过来的透传数据*/
    public static void sendPushByPayload(Context context, String payloadString) {
        String pushId = PushPayloadParser.parse(payloadString);
        LogUtils.d(LogTAG.PushTAG, "Got Payload : " + pushId);
        AppInfoBackDoorActivity.GeTui_Payload = pushId;

        try {
            if (!pushId.equals(PushPayloadParser.NEVER_PUSH)) {
                context.startService(CoreService.getPushIntent(context, pushId));
            } else {
                PushPayloadParser.parserOpenLog(payloadString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
