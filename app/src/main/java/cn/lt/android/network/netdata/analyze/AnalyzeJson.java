package cn.lt.android.network.netdata.analyze;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

import cn.lt.android.main.personalcenter.UserInfoManager;
import cn.lt.android.network.NetFlag;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;
import cn.lt.android.network.netdata.bean.NetResponse;
import cn.lt.android.util.LogUtils;

/**
 * Created by Administrator on 2015/11/12.
 */
public class AnalyzeJson {

    public synchronized static BaseBean analyzeJson(String str) {

        //LogUtils.e("base64",NetDataEncoding.decode(NetDataEncoding.encode(str)));

        str = str.trim();
        if (str.length() == 0) {
            return null;
        }
        if (str.toCharArray()[0] == '[') {
            return analyzeArray(str);
        } else {
            return analyzeBean(str);
        }
    }

    private synchronized static BaseBean analyzeArray(String str) {
        JSONArray jsonArray;
        BaseBeanList reList = new BaseBeanList();
        Gson gson = new Gson();
        try {
            jsonArray = new JSONArray(str);
            JSONObject jsonObj;
            BaseBean bean;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = jsonArray.getJSONObject(i);
                bean = AutoCallAnalyzeFun(jsonObj, gson);
                if (bean != null) {
                    reList.add(bean);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reList;
    }

    public synchronized static BaseBeanList analyzeArrayList(String str) {
//        if(TextUtils.isEmpty(str)) return null;
        JSONArray jsonArray;
        BaseBeanList reList = new BaseBeanList();
        Gson gson = new Gson();
        try {
            jsonArray = new JSONArray(str);
            JSONObject jsonObj;
            BaseBean bean;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = jsonArray.getJSONObject(i);
                bean = AutoCallAnalyzeFun(jsonObj, gson);
                if (bean != null) {
                    reList.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reList;
    }

    private synchronized static BaseBean analyzeBean(String str) {
        Gson gson = new Gson();
        BaseBean bean = null;
        try {
            JSONObject jsonObj = new JSONObject(str);
            bean = AutoCallAnalyzeFun(jsonObj, gson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return bean;
    }

    private synchronized static BaseBean AutoCallAnalyzeFun(JSONObject jsonObj, Gson gson) {

        BaseBean bean = null;
        String typeString = "";
        try {
            AnalyzeJsonBean analyzeJsonBean;
            typeString = getJsonType(jsonObj);
            if (!TextUtils.isEmpty(typeString)) {
                analyzeJsonBean = AnalyzeConfig.getMap().get(typeString);
                if (analyzeJsonBean != null) {
                    if (analyzeJsonBean.isAutoAnalyze()) {
                        bean = gson.fromJson(jsonObj.opt("data").toString(), ((TypeToken) analyzeJsonBean.getValue()).getType());
                    } else {
                        // 手动解析的代码，使用反射调用method，method为静态方法
                        Method method = (Method) analyzeJsonBean.getValue();
                        bean = (BaseBean) method.invoke(null, jsonObj.opt("data").toString());
                    }
                    bean.setLtType(typeString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return bean;
        }
    }

    private static String getJsonType(JSONObject jsonObj) {
        String str = jsonObj.optString("theme_type", "");
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        str = jsonObj.optString("click_type", "");
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        return "";
    }

    //    public static BaseBean analyzeBeanTest(String str) {
    //        Gson gson = new Gson();
    //        BaseBean bean = null;
    //        TypeToken typeToken = new TypeToken<BaseBeanList<GiftBean>>() {
    //        };
    //        if (typeToken != null) {
    //            bean = gson.fromJson(str, typeToken.getType());
    //        }
    //        return bean;
    //    }
    //
    //    public static <T> T analyzeBeanTest(String str,int y) {
    //        Gson gson = new Gson();
    //        BaseBean bean = null;
    //        TypeToken typeToken = new TypeToken<BaseBeanList<GiftBean>>() {
    //        };
    //        if (typeToken != null) {
    //            bean = gson.fromJson(str, typeToken.getType());
    //        }
    //        return (T)bean;
    //}


    public static NetResponse parseUserCenterData(String result) {
        NetResponse<String> netResponse = new NetResponse<String>();
        try {
            JSONObject obj = new JSONObject(result);
            if (obj.optInt("status", 1) == 1) {
                String data = obj.optString("data", null);
                try {
                    if (TextUtils.isEmpty(data)) {
                        LogUtils.e("net", "data数据为空");
                        netResponse.setMessage(obj.optString("message", "data数据为空"));
                        netResponse.setStatus(NetFlag.userSuccess);
                    } else {
                        netResponse.setData(data);
                        netResponse.setStatus(NetFlag.userSuccess);
                    }
                } catch (Exception e) {
                    netResponse.setMessage(e.getMessage());
                    netResponse.setStatus(NetFlag.dataError);
                }
            } else {
                if (obj.optInt("status", 1) == NetFlag.userLogout) {
                    UserInfoManager.instance().userLogout(false);
                }
                netResponse.setMessage(obj.optString("message", "网络异常,无数据返回"));
                netResponse.setStatus(obj.optInt("status", 1));
            }
        } catch (JSONException e) {
            netResponse.setMessage(e.getMessage());
            netResponse.setStatus(NetFlag.dataError);
        }
        return netResponse;
    }

}
