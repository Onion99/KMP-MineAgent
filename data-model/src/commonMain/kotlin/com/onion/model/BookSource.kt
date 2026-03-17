package com.onion.model

import com.onion.model.js.BookSourceJS
import com.onion.model.rule.BookListRule
import kotlinx.serialization.Serializable

@Serializable
data class BookSource(
    // 地址，包括 http/https
    var bookSourceUrl: String = "",
    // 名称
    var bookSourceName: String = "",
    // 分组
    var bookSourceGroup: String? = null,
    // 类型，0 文本，1 音频, 2 图片, 3 文件（指的是类似知轩藏书只提供下载的网站）
    var bookSourceType: Int = 0,
    // 详情页url正则
    var bookUrlPattern: String? = null,
    // 手动排序编号
    var customOrder: Int = 0,
    // 是否启用
    var enabled: Boolean = true,
    // 启用发现
    var enabledExplore: Boolean = true,
    // 登录检测js
    var loginCheckJs: String? = null,
    // 封面解密js
    var coverDecodeJs: String? = null,
    // 注释
    var bookSourceComment: String? = null,
    // 自定义变量说明
    var variableComment: String? = null,
    // 最后更新时间，用于排序
    var lastUpdateTime: Long = 0,
    // 响应时间，用于排序
    var respondTime: Long = 180000L,
    // 智能排序的权重
    var weight: Int = 0,
    // 发现url
    var exploreUrl: String? = null,
    // 发现筛选规则
    var exploreScreen: String? = null,
    // 发现规则
    var ruleExplore: BookListRule? = null,
    override var loginUrl: String? = null,
    override var loginUi: String? = null,
    override var header: String? = null,
    override var enabledCookieJar: Boolean? = null,
    override var jsLib: String? = null,
): BookSourceJS