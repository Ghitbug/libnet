package com.gh.lib.net.exception

class ApiException : Exception {
    /*错误码*/
    private var code = 0

    /*显示的信息*/
    var displayMessage: String? = null
    var isExecute = true
    var method: String? = null
    var data: String? = null
    var businessType: String? = null

    constructor(cause: Throwable?) : super(cause) {
    }

    constructor(cause: Throwable?, code: Int, showMsg: String?) : super(showMsg, cause) {
       this.code=code
        displayMessage = showMsg
    }

    constructor(cause: Throwable?, code: Int, showMsg: String?, method: String) : super(showMsg, cause) {
        this.code=code
        this.method = method
        this.displayMessage = showMsg
    }

    fun getCode(): Int {
        return code
    }

}