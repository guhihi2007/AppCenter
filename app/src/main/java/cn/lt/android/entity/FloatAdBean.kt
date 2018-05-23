package cn.lt.android.entity

import cn.lt.android.network.netdata.bean.BaseBean

/**
 * Created by JohnsonLin on 2017/9/21.
 */
data class FloatAdBean(var id: String, // 广告id
                  var title: String, // 广告title
                  var click_type: Int, // 类型（1.仅下载 2.仅跳转 3.跳转+下载）
                  var ads_icon: String, // 广告图
                  var resource_id: String, // 跳转类型对应的id
                  var jump_type: String, // 跳转类型
                  var link: String, // 跳转H5链接
                  var app: AppDetailBean? // 要下载的app
) : BaseBean()
