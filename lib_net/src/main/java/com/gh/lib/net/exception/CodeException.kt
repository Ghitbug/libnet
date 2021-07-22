package com.gh.lib.net.exception

class CodeException {
    companion object{
        /*网络错误*/
        val NETWORD_ERROR = 0x1

        /*http_错误*/
        val HTTP_ERROR = 0x2

        /*fastjson错误*/
        val JSON_ERROR = 0x3

        /*未知错误*/
        var UNKNOWN_ERROR: Int = 0x4

        /*运行时异常-包含自定义异常*/
        val RUNTIME_ERROR = 0x5

        /*无法解析该域名*/
        val UNKOWNHOST_ERROR = 0x6

        /*登录过期*/
        val LOGIN_OUTTIME = 0x7

        /*被迫下线*/
        val TAPEOUT = 0x8
        val ERROR = 0x9
        val NOT_NETWORD = 0x10
        annotation class CodeEp
    }

}