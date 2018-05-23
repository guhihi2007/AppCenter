package cn.lt.android.deeplink

import android.content.Context
import android.net.Uri
import cn.lt.android.LTApplication
import cn.lt.android.LogTAG
import cn.lt.android.entity.ClickTypeBean
import cn.lt.android.main.UIController
import cn.lt.android.main.entrance.data.ClickType
import cn.lt.android.util.FromPageManager
import cn.lt.android.util.LogUtils
import com.google.gson.Gson

/**
 * Created by JohnsonLin on 2017/11/6.
 * deeplink管理类
 */

object DeeplinkMgr {

    fun handleDeeplinkAction(context: Context, deeplinkDataStr: String) {
        try {
            val uri = Uri.parse(deeplinkDataStr)

            val jsonStr = uri.getQueryParameter("dplink")
            val from = uri.getQueryParameter("from")
            LogUtils.i(LogTAG.deepLinkTAG, "jsonStr = $jsonStr, from = $from")

            val clickTypeBean = Gson().fromJson(jsonStr, ClickTypeBean::class.java)

            if (clickTypeBean != null) {
                LogUtils.i(LogTAG.deepLinkTAG, "Type = ${clickTypeBean.click_type}")

                // 为了数据统计的fromPage准确，这里延迟一下执行
                LTApplication.getMainThreadHandler().postDelayed({action(context, clickTypeBean, from)}, 500)

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun action(context: Context, clickTypeBean: ClickTypeBean, from: String) {
        when (clickTypeBean.click_type) {

            // 跳转到应用详情
            ClickType.app_info.toString() -> {
                LogUtils.i(LogTAG.deepLinkTAG, "跳转到应用详情")
                FromPageManager.setLastPage(from)
                UIController.goAppDetailByDeeplink(context, clickTypeBean.data.id, clickTypeBean.data.apps_type, from)
            }

            // 跳转到专题详情
            ClickType.topic_info.toString() -> {
                LogUtils.i(LogTAG.deepLinkTAG, "跳转到专题详情")
                FromPageManager.setLastPage(from)
                UIController.goSpecialDetail(context, clickTypeBean.data.id, clickTypeBean.data.title, from, false, true)
            }
        }
    }
}