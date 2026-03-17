package com.onion.model.js

import com.onion.model.js.JsExtensions

interface BookSourceJS : JsExtensions {
/*
    */
/**
     * 并发率
     *//*

    var concurrentRate: String?
*/

    /**
     * 登录地址
     */
    var loginUrl: String?

    /**
     * 登录UI
     */
    var loginUi: String?

    /**
     * 请求头
     */
    var header: String?

    /**
     * 启用cookieJar
     */
    var enabledCookieJar: Boolean?
    /**
     * js库
     */
    var jsLib: String?

    override fun getSource(): BookSourceJS? {
        return this
    }
}