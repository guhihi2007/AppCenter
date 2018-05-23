package cn.lt.android.ads.wanka;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.lt.android.LTApplication;
import cn.lt.android.util.AdMd5;

/**
 * Created by chon on 2016/12/27.
 * What? How? Why?
 * 构造玩咖请求的url 以及请求体
 */

class WanKaRequestBean {
    String url;
    String requestBodyJson;
    private WanKaRequestBodyBean bodyBean;

    private static final String KEY_SIGN = "sign";

    private static Gson gson;

    private WanKaRequestBean() {
        gson = new Gson();
    }

    private static class WanKaRequestBodyBean<T> {
        T reportData;
        String sign;

        WanKaRequestBodyBean(T reportData, String sign) {
            this.reportData = reportData;
            this.sign = sign;
        }
    }

    // 生成玩咖sign所需要的key
    private static List<String> KEYS = Arrays.asList(WanKa.CLIENT_IP, WanKa.NONCE, WanKa.OS_ID, WanKa.CLIENT_ID,
            WanKa.INFO_MS, WanKa.INFO_MA, WanKa.DEVICE, WanKa.CHANNEL_ID,
            WanKa.APP_ID, WanKa.CUID, WanKa.KEY_REPORT_DATA);

    static <T> WanKaRequestBean generateBean(String originUrl, T reportData, String... extraData) {
        WanKaRequestBean requestBean = new WanKaRequestBean();

        // url 中参数
        Map<String, String> commonParams = WanKa.getCommonParams(LTApplication.instance);

        if (WanKaUrl.DOWNLOAD_START.equals(originUrl)) {
            // 普通下载给0
            if (extraData != null) {
                if (extraData.length > 0 && !TextUtils.isEmpty(extraData[0])) {
                    // from_update:  是否是升级，如果是值为 1，否则为空。
                    commonParams.put("from_update", extraData[0]);
                }

                if (extraData.length > 1 && !TextUtils.isEmpty(extraData[1])) {
                    // is_resume:  是否是断点续传，如果是值为 1，否则为空。
                    commonParams.put("is_resume", extraData[1]);
                }
            }
        }

        // 后续获取到真实的sign 再设置进去
        String sign = "";
        // 构建bodyBean，为了生成的reportDataFromJson 符合玩咖的传参
        requestBean.bodyBean = new WanKaRequestBodyBean<>(reportData, sign);

        try {
            JSONObject jsonObject = new JSONObject(gson.toJson(requestBean.bodyBean));
            String reportDataFromJson = jsonObject.optString(WanKa.KEY_REPORT_DATA);

            // WanKaLog.e(TAG,"reportData: " + reportDataFromJson);
            // post 请求体的参数之一 (拿到给下边生成sign用)
            commonParams.put(WanKa.KEY_REPORT_DATA, reportDataFromJson);

            // 需要签名的字符串
            try {
                for (Map.Entry<String, String> entry : commonParams.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value != null) {
                        // 对value 进行urlEncode
                        value = URLEncoder.encode(value, "UTF-8");
                    }


                    if (!WanKa.KEY_REPORT_DATA.equals(key)) {
                        // post 请求的参数不用追加到url上
                        originUrl += (key + "=" + value + "&");
                    }

                    if (KEYS.contains(key)) {
                        sign += (key + "=" + value + "&");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // can not reach
                e.printStackTrace();
            }

            requestBean.url = originUrl.substring(0, originUrl.lastIndexOf("&"));
            if (extraData != null && extraData.length > 0 && WanKaUrl.EXPOSURE.equals(originUrl)) {
                WanKaLog.e(extraData[0] + " url = " + requestBean.url);
            } else {
                WanKaLog.e("url = " + requestBean.url);
            }

            sign = sign.substring(0, sign.lastIndexOf("&"));
            sign += (WanKa.getIMEI(LTApplication.instance) + LTApplication.instance.getPackageName() + WanKaUrl.WANKA_APP_SECRET);
//            WanKaLog.e("signStr: " + sign);

            sign = AdMd5.MD5(sign).toLowerCase();
//            WanKaLog.e("sign MD5: " + sign);
            requestBean.bodyBean.sign = sign;
            jsonObject.put(KEY_SIGN, sign);

            requestBean.requestBodyJson = jsonObject.toString();
            if (extraData != null && extraData.length > 0 && WanKaUrl.EXPOSURE.equals(originUrl)) {
                WanKaLog.e(extraData[0] + " post请求参数：" + requestBean.requestBodyJson);
            } else {
                WanKaLog.e("post请求参数：" + requestBean.requestBodyJson);
            }
        } catch (JSONException e) {
            // can not reach
            e.printStackTrace();
        }


        return requestBean;
    }
}
