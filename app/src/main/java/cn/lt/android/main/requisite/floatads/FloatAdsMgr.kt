package cn.lt.android.main.requisite.floatads

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import cn.lt.android.Constant
import cn.lt.android.LTApplication
import cn.lt.android.LogTAG
import cn.lt.android.SharePreferencesKey
import cn.lt.android.autoinstall.AutoInstallerContext
import cn.lt.android.download.DownloadChecker
import cn.lt.android.download.DownloadTaskManager
import cn.lt.android.entity.ClickTypeDataBean
import cn.lt.android.entity.FloatAdBean
import cn.lt.android.install.InstallManager
import cn.lt.android.main.MainActivity
import cn.lt.android.main.UIController
import cn.lt.android.main.entrance.Jumper
import cn.lt.android.main.entrance.data.ClickType
import cn.lt.android.network.NetWorkClient
import cn.lt.android.network.netdata.bean.HostType
import cn.lt.android.statistics.DCStat
import cn.lt.android.util.*
import cn.lt.android.widget.CustomDialog
import cn.lt.android.widget.OrderWifiDownloadClickListener
import cn.lt.appstore.R
import cn.lt.download.DownloadStatusDef
import cn.lt.framework.util.PreferencesUtils
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import kotlinx.android.synthetic.main.float_ad_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

/**
 * Created by JohnsonLin on 2017/9/22.
 * 浮层广告管理文件
 */

private val JUMP_ONLY = 2
private val DOWNLOAD_ONLY = 1
private val DOWNLOAD_AND_JUMP = 3

object FloatAdsMgr {

    fun requestFloatAdsData(context: Context) {

        NetWorkClient.getHttpClient().setHostType(HostType.GCENTER_HOST).setCallback(object : Callback<FloatAdBean> {

            override fun onResponse(call: Call<FloatAdBean>, response: Response<FloatAdBean>) {
                if (response.body() != null) {
                    LogUtils.i(LogTAG.floatAdTAG, "浮层广告接口请求成功~")

                    FloatAdsDialog(context, response.body()).apply {
                        LogUtils.i(LogTAG.floatAdTAG, floatAdBean.toString())

                        if (canShow(floatAdBean)) {
                            LogUtils.i(LogTAG.floatAdTAG, "满足显示浮层广告条件~")
                            showFloatAd()
                        } else {
                            LogUtils.i(LogTAG.floatAdTAG, "条件不满足，忽略浮层广告显示~")
                            AppSignConflictUtil.showSignConflictDialogs(context)
                        }
                    }

                } else {
                    LogUtils.i(LogTAG.floatAdTAG, "浮层广告数据实体为空~")
                    AppSignConflictUtil.showSignConflictDialogs(context)
                }


            }

            override fun onFailure(call: Call<FloatAdBean>?, t: Throwable?) {
                LogUtils.i(LogTAG.floatAdTAG, "浮层广告接口请求失败~")
                AppSignConflictUtil.showSignConflictDialogs(context)
            }

        }).bulid().requestFloatAds()

    }

    private fun canShow(floatAdBean: FloatAdBean): Boolean {
        when (floatAdBean.click_type) {
            DOWNLOAD_ONLY -> {
                val floatApp = floatAdBean.app
                floatApp?: return false

                val installedApp = AppUtils.getPackageInfo(floatApp.package_name)
                installedApp?: return true

                return installedApp.versionCode < floatApp.version_code.toInt()

            }
        }

        return true
    }

}


/**
 * 浮层广告Dialog
 */
class FloatAdsDialog(val mContext: Context, val floatAdBean: FloatAdBean) : Dialog(mContext, android.R.style.Theme) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//设置状态栏透明色
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        window.setBackgroundDrawableResource(R.color.transparent)
        setContentView(R.layout.float_ad_layout)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        ImageloaderUtil.loadImage(mContext, floatAdBean.ads_icon, R.mipmap.float_default_img, iv_ads)

        init()
        LogUtils.i(LogTAG.floatAdTAG, "FloatAdsDialog初始化了~~")

    }

    private fun init() {

        iv_close.setOnClickListener { dismiss() }

        iv_ads.setOnClickListener {
            setFromPage()
            try {
                executeJump()
                DCStat.adReport("adClicked", Constant.PAGE_FLOAT, "", floatAdBean.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            dismiss()
        }
    }

    fun showFloatAd() {

        ImageloaderUtil.loadImageCallBack(mContext, floatAdBean.ads_icon, object : SimpleTarget<GlideDrawable>() {

            override fun onResourceReady(resource: GlideDrawable?, glideAnimation: GlideAnimation<in GlideDrawable>?) {
                LogUtils.i(LogTAG.floatAdTAG, "图片加载成功")
                try {
                    if (mContext is MainActivity && mContext.curPage == MainActivity.PAGE_TAB_RECOMMEND) {
                        LogUtils.i(LogTAG.floatAdTAG, "当前页面是推荐页， 展示！")
                        show()
                        DCStat.adReport("adPresent", Constant.PAGE_FLOAT, "", floatAdBean.id)
                        PreferencesUtils.putLong(mContext, SharePreferencesKey.FLOAT_AD_LAST_SHOW_TIME, System.currentTimeMillis())
                    } else {
                        LogUtils.i(LogTAG.floatAdTAG, "mContext.curPage = " + (mContext as MainActivity).curPage)
                        LogUtils.i(LogTAG.floatAdTAG, "当前页面不是推荐页， 不展示！")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtils.i(LogTAG.floatAdTAG, "抛异常了~！")
                }

            }

            override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                super.onLoadFailed(e, errorDrawable)
                LogUtils.i(LogTAG.floatAdTAG, "图片加载失败， 浮层广告不显示！")
            }

        })

    }

    private fun executeJump() {
        when (floatAdBean.click_type) {

            JUMP_ONLY -> {
                LogUtils.i(LogTAG.floatAdTAG, "点击类型：JUMP_ONLY, 跳转类型 = ${floatAdBean.jump_type}")
                var pkgName: String = ""
                if (floatAdBean.app != null) {
                    pkgName = floatAdBean.app?.package_name.toString()
                }

                val clickData = ClickTypeDataBean(floatAdBean.link, floatAdBean.resource_id, floatAdBean.app?.apps_type, floatAdBean.title, Constant.PAGE_FLOAT, false, false, pkgName, null)

                Jumper().jumper(mContext, ClickType.valueOf(floatAdBean.jump_type), clickData, Constant.PAGE_FLOAT, false)
            }

            DOWNLOAD_ONLY -> {
                LogUtils.i(LogTAG.floatAdTAG, "点击类型：DOWNLOAD_ONLY")
                judgeAction()
            }

            DOWNLOAD_AND_JUMP -> {
                LogUtils.i(LogTAG.floatAdTAG, "点击类型：DOWNLOAD_AND_JUMP")
                UIController.goAppDetail(mContext, false, "", floatAdBean.app?.id.toString(), "", floatAdBean.app?.apps_type, Constant.PAGE_FLOAT, "", "", null)

                val installedApp = AppUtils.getPackageInfo(floatAdBean.app?.package_name)

                if (installedApp == null || installedApp.versionCode < floatAdBean.app?.version_code?.toInt()?: 0) {
                    LTApplication.getMainThreadHandler().postDelayed({ judgeAction() }, 1000)
                }

                LogUtils.i(LogTAG.floatAdTAG, "跳转到应用详情页")

            }
        }
    }

    private fun setFromPage() {
        FromPageManager.setLastPage(Constant.PAGE_FLOAT)
        FromPageManager.setLastPageId(floatAdBean.id)
    }

    private fun judgeAction() {
        val floatApp = floatAdBean.app
        val downloadApp = DownloadTaskManager.getInstance().getAppEntityByPkg(floatApp!!.package_name)

        if (downloadApp == null) {
            startDownload()
            LogUtils.i(LogTAG.floatAdTAG, "应用未安装，直接启动下载")
            ToastUtils.showToast("开始下载 " + floatAdBean.app?.name)
            return
        }


        val downloadComplete = DownloadTaskManager.getInstance().getState(downloadApp) == DownloadStatusDef.completed.toInt()

        if (downloadComplete && (downloadApp.version_code.toInt() >= floatApp.version_code.toInt())) {
            LogUtils.i(LogTAG.floatAdTAG, "应用已下载完成，版本是最新的，启动安装")
            InstallManager.getInstance().start(downloadApp, downloadApp.packageName, Constant.PAGE_FLOAT, false)
        } else {
            LogUtils.i(LogTAG.floatAdTAG, "应用未安装也未下载，开始下载应用")
            startDownload()
            ToastUtils.showToast("开始下载 " + floatAdBean.app?.name)
        }

    }

    private fun startDownload() {
        var app = DownloadTaskManager.getInstance().getAppEntityByPkg(floatAdBean.app?.package_name)
        var downloadEvent = "request"

        if (app != null) {

            // 任务已经在下载队列中\已经下载完成，无需启动下载，也不要上报数据
            if (DownloadStatusDef.isIng(app.status)) {
                return
            }

            if (app.status == DownloadStatusDef.paused.toInt()) {
                downloadEvent = "continue"
            } else if (app.status == DownloadStatusDef.retry.toInt() || app.status == DownloadStatusDef.error.toInt()) {
                downloadEvent = "retry"
            }

        } else {
            app = DownloadTaskManager.getInstance().transfer(floatAdBean.app)
        }

        if (!DownloadChecker.getInstance().noNetworkPromp(ActivityManager.self().topActivity()) {
            app.isOrderWifiDownload = true
            if (app.status == DownloadStatusDef.paused.toInt()) {
                if (app.soFar != 0L) {
                    downloadEvent = "continue"
                }
                app.isOrderWifiContinue = true
            }
            DownloadTaskManager.getInstance().start(app)
            DCStat.downloadRequestReport(app, "manual", downloadEvent, Constant.PAGE_FLOAT, floatAdBean.id, "network_change", "book_wifi")//解决预约Wifi下载没有存库导致的下载请求缺少部分字段(download_type)
        }) {

            // wifi网络下直接下载
            if (NetUtils.isWifi(mContext)) {
                // 设置不是预约wifi下载的状态
                DownloadTaskManager.getInstance().startAfterCheck(ActivityManager.self().topActivity(), app, "manual", downloadEvent, Constant.PAGE_FLOAT, floatAdBean.id, "", "")
            }

            // 3G/4G网络需要弹窗确认
            if (NetUtils.isMobileNet(mContext)) {
                CustomDialog.Builder(ActivityManager.self().topActivity()).setMessage("当前处于2G/3G/4G环境，下载应用将消耗流量，是否继续下载？").setPositiveButton(R.string.continue_mobile_download).setPositiveListener {
                    app.isOrderWifiContinue = app.isOrderWifiDownload
                    app.isOrderWifiDownload = false
                    DownloadTaskManager.getInstance().startAfterCheck(ActivityManager.self().topActivity(), app, "manual", downloadEvent, Constant.PAGE_FLOAT, floatAdBean.id, "", "")

                }.setNegativeButton(R.string.order_wifi_download).setNegativeListener(OrderWifiDownloadClickListener(app)).create().show()
            }
        }

        // 自动装弹窗
        if (PopWidowManageUtil.needAutoInstallDialog(mContext)) {
            AutoInstallerContext.getInstance().promptUserOpen(ActivityManager.self().topActivity())
        }
    }


}