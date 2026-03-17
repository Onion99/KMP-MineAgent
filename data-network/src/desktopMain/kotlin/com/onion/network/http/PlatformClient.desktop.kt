package com.onion.network.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.config
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.ConnectionSpec
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

actual fun getPlatformHttpEngine():HttpClientEngine  = OkHttp.create{
    config {

        // ------------------------------------------------------------------------
        // 创建一个信任所有证书的 TrustManager:
        // 为了解决客户端不信任服务器数字证书的问题，
        // 网络上大部分的解决方案都是让客户端不对证书做任何检查，
        // 这是一种有很大安全漏洞的办法
        // ------------------------------------------------------------------------
        val unsafeTrustManager: X509TrustManager = @Suppress("CustomX509TrustManager") object : X509TrustManager {
            @Suppress("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                //do nothing，接受任意客户端证书
            }

            @Suppress("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                //do nothing，接受任意客户端证书
            }

            fun checkServerTrusted(chain: Array<X509Certificate>, authType: String, host: String): List<X509Certificate> {
                return chain.toList()
            }
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
        // ---- 用我们的 TrustManager 初始化 SSLContext ------
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(unsafeTrustManager), SecureRandom())
        sslSocketFactory(sslContext.socketFactory,unsafeTrustManager)
        // ------------------------------------------------------------------------
        // 此类是用于主机名验证的基接口。 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，
        // 则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。策略可以是基于证书的或依赖于其他验证方案。
        // 当验证 URL 主机名使用的默认规则失败时使用这些回调。如果主机名是可接受的，则返回 true
        // ------------------------------------------------------------------------
        hostnameVerifier(HostnameVerifier { _, _ -> true })
        // OkHttp: .connectionSpecs(specs)
        connectionSpecs(arrayListOf(
            ConnectionSpec.MODERN_TLS,
            ConnectionSpec.COMPATIBLE_TLS,
            ConnectionSpec.CLEARTEXT
        ))
        followRedirects(true)
        followSslRedirects(true)
        // 通过创建一个完全自定义的 SSLContext 并将其强制应用到 OkHttp 引擎，我们可以更彻底地绕过 JVM 的默认安全检查
    }
}