package com.gh.lib.net.param

import android.text.TextUtils
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import rxhttp.wrapper.param.JsonParam
import rxhttp.wrapper.param.Method

class PostText(url: String) : JsonParam(url, Method.POST) {
    private val mediaType: MediaType = "application/json; charset=UTF-8".toMediaType()

    private var text: String? = null
    fun setText(text: String?): PostText {
        this.text = text
        return this
    }

    override fun getRequestBody(): RequestBody {
        return if (TextUtils.isEmpty(text)) RequestBody.create(null, byteArrayOf(0))
        else RequestBody.create(mediaType, text!!)
    }
}