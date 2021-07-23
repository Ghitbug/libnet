package com.gh.lib.net

import com.gh.lib.net.interfaces.ProgressResult


class HttpBuilder(url: String) {

    var domainUrl: String? = null//是否使用默认地址，如果不使用默认地址则使用第二默认地址
    var timeout: Long = 6 //动态设置连接超时时间 必须大于0 ，如果小于0则使用默认超时时间

    var url: String = url // 访问地址
    var tag: String? = null//设置标签
    var headers: HashMap<String, String>? = null//添加header
    var parameters: HashMap<String, Any>? = null//数据类型的参数
    var files: HashMap<String, String>? = null//文件类型的参数
    var isShowProgress: Boolean = false
    var msg: String? = null
    var isCancel: Boolean = false
    var retry: Int = HttpConfig.mRetry

    var httpType: HttpType = HttpType.POST //加载类型

    var parameter: String? = null
    var progressResult: ProgressResult? = null


    fun setHeaders(header: String): HttpBuilder {
        if (headers == null) {
            headers = hashMapOf("headers" to header)
        }
        return this
    }

    fun setFile(key: String, path: String) {
        if (files == null) {
            files = hashMapOf(key to path)
        } else {
            files!![key] = path
        }
    }

    companion object {
        fun getBuilder(url: String): HttpBuilder {
            return HttpBuilder(url)
        }
    }

}