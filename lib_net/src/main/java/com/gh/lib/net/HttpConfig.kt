package com.gh.lib.net

import android.text.TextUtils
import okhttp3.OkHttpClient
import rxhttp.RxHttpPlugins
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.RxHttpFormParam
import rxhttp.wrapper.ssl.HttpsUtils
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class HttpConfig {
    companion object {
        var baseUrl: String? = null
        var mDomainUrl: String? = null
        var mTimeout: Long = 6
        var mRetry = 0

        /**
         * 初始化http请求
         *
         * @param url     默认地址
         * @param timeout 连接超时时间（秒）
         * @param isdebug 是否dubug模式 dubug模式下会输入日志
         */
        fun init(url: String?, timeout: Long, retry: Int, isdebug: Boolean) {
            baseUrl = url
            mTimeout = timeout
            mRetry = retry
            if (timeout > 0) {
                try {
                    RxHttp.init(getDefaultOkHttpClient(timeout), isdebug)
                } catch (e: IllegalArgumentException) {
                }
            } else {
                RxHttp.setDebug(isdebug)
            }
        }

        fun init(url: String?, isdebug: Boolean) {
            init(url, mTimeout, 0, isdebug)
        }

        /**
         * 设置地址和参数
         *
         * @param builder
         * @return
         */
        fun getRxHttpFrom(builder: HttpBuilder): RxHttpFormParam {
            val rxHttp: RxHttpFormParam?
            val url = builder.url
            rxHttp = when (builder.httpType) {
                HttpType.DEL -> RxHttp.deleteForm(url)
                HttpType.PUT -> RxHttp.putForm(url)
                HttpType.PATCH -> RxHttp.patchForm(url)
                else -> RxHttp.postForm(url)
            }
            if (rxHttp != null) {
                //设置参数
                if (builder.parameters != null) {
                    rxHttp.addAll(builder.parameters)
                }
                //设置文件参数
                if (builder.files != null) {
                    for (file in builder.files!!.keys) {
                        rxHttp.addFile(file, File(builder.files!![file]))
                    }
                }
                setRxhttpSetting(rxHttp, builder)
            }
            return rxHttp
        }

        /**
         * 设置地址和参数
         *
         * @param builder
         * @return
         */
        fun getRxHttpJson(builder: HttpBuilder): RxHttp<*, *> {
            val rxHttp: RxHttp<*, *>
            val url = builder.url
            rxHttp = if (TextUtils.isEmpty(builder.parameter)) {
                //根据不同的请求方式生成不同的请求对象
                when (builder.httpType) {
                    HttpType.DEL -> RxHttp.deleteJson(url, builder.parameters)
                    HttpType.GET -> RxHttp.get(url, builder.parameters)
                    HttpType.PUT -> RxHttp.putJson(url, builder.parameters)
                    HttpType.PATCH -> RxHttp.patchJson(url, builder.parameters)
                    else -> RxHttp.postJson(url, builder.parameters)
                }
            } else {
                RxHttp.postText(url, builder.parameter)
            }
            if (rxHttp != null) {
                setRxhttpSetting(rxHttp, builder)
            }
            return rxHttp
        }

        /**
         * 设置请求公共参数
         *
         * @param rxHttp
         * @param builder
         */
        private fun setRxhttpSetting(rxHttp: RxHttp<*, *>, builder: HttpBuilder) {
            rxHttp.addHeader("Connection", "close")
            if (builder.headers != null) {
                for (keys in builder.headers!!.keys) {
                    rxHttp.addHeader(keys, builder.headers!![keys])
                }
            }
            //判断使用默认地址还是动态地址
            if (builder.domainUrl != null && "" != builder.domainUrl) {
                mDomainUrl = builder.domainUrl
                rxHttp.setDomainToUpdateIfAbsent()
            }
        }

        /**
         * 连接、读写超时均为10s、添加信任证书并忽略host验证
         *
         * @return 返回默认的OkHttpClient对象
         */
        private fun getDefaultOkHttpClient(timeout: Long): OkHttpClient {
            val sslParams = HttpsUtils.getSslSocketFactory()
            return OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager) //添加信任证书
                    .hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true }) //忽略host验证
                    .build()
        }

        /**
         * 取消所有请求
         */
        fun cancelAll() {
            RxHttpPlugins.cancelAll()
        }
    }


}