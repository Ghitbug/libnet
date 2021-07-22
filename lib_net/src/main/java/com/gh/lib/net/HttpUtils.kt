package com.gh.lib.net

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.rxLifeScope
import com.gh.lib.net.HttpBuilder
import com.gh.lib.net.HttpConfig
import com.rxlife.coroutine.RxLifeScope
import com.gh.lib.net.exception.ApiException
import com.gh.lib.net.interfaces.DownLoadResult
import com.gh.lib.net.interfaces.NetWorkResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import rxhttp.*
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.param.RxHttp
import rxhttp.wrapper.param.RxHttpFormParam
import java.io.File

open class HttpUtils {
    companion object {
        var map = mutableMapOf<String, Job>()
        var TAG = "HttpSend"

        /**
         * 初始化http请求
         *
         * @param url     默认地址
         * @param timeout 连接超时时间（秒）
         * @param isdebug 是否dubug模式 dubug模式下会输入日志
         */
        fun init(url: String, timeout: Long, retry: Int, isdebug: Boolean) {
            HttpConfig.init(url, timeout, retry, isdebug)
        }

        fun init(url: String, isdebug: Boolean) {
            HttpConfig.init(url, isdebug)
        }

        fun postJson(builder: HttpBuilder, model: ViewModel?, listener: NetWorkResult): Job { //生成一个根据地址和tab组合的key,来判断是否同一请求发起了多次请求，则取消之前的请求，使用最后一次
            return postJson(builder, true, model, listener)
        }

        fun postJson(builder: HttpBuilder, isDisposable: Boolean, model: ViewModel?, listener: NetWorkResult): Job { //生成一个根据地址和tab组合的key,来判断是否同一请求发起了多次请求，则取消之前的请求，使用最后一次
            return post(model, builder, isDisposable, listener, HttpConfig.getRxHttpJson(builder))
        }

        fun postForm(builder: HttpBuilder, model: ViewModel?, listener: NetWorkResult): Job {
            return postForm(builder, true, model, listener)
        }

        fun postForm(builder: HttpBuilder, isDisposable: Boolean, model: ViewModel?, listener: NetWorkResult): Job { //生成一个根据地址和tab组合的key,来判断是否同一请求发起了多次请求，则取消之前的请求，使用最后一次
            //生成一个根据地址和tab组合的key,来判断是否同一请求发起了多次请求，则取消之前的请求，使用最后一次
            return post(model, builder, isDisposable, listener, HttpConfig.getRxHttpFrom(builder))
        }


        private fun post(model: ViewModel?, builder: HttpBuilder, isDisposable: Boolean, listener: NetWorkResult, rxHttp: RxHttp<*, *>): Job {
            var rxLifeScope = model?.rxLifeScope ?: RxLifeScope()
            var isViewModel = model != null
            return rxlifeScope(isViewModel, rxLifeScope, builder, isDisposable, listener, rxHttp)
        }

        /**
         * 处理请求
         */
        private fun rxlifeScope(isViewModel: Boolean, rxLifeScope: RxLifeScope, builder: HttpBuilder, isDisposable: Boolean, listener: NetWorkResult, rxHttp: RxHttp<*, *>): Job {
            val key = if (builder.tag == null) builder.url else builder.tag + builder.url
            var job = rxLifeScope.launch({
                //生成一个根据地址和tab组合的key,来判断是否同一请求发起了多次请求，则取消之前的请求，使用最后一次
                if (isDisposable && map.containsKey(key)) {
                    val job = map[key]
                    job!!.cancel()
                    map.remove(key)
                }
                if (builder.files != null) {
                    if (builder.progressResult != null && rxHttp is RxHttpFormParam) {
                        rxHttp.upload(AndroidSchedulers.mainThread()) { progress: Progress ->
                            builder.progressResult?.Progress(
                                    progress.progress,
                                    progress.currentSize,
                                    progress.totalSize
                            )
                        }
                    }
                }
                var iwat = rxHttp.toStr()
                //动态设置超时时间
                if (builder.timeout > 0) iwat.timeout(builder.timeout)
                if (builder.retry > 0) {
                    iwat.retry(builder.retry) {
                        it is TimeoutCancellationException
                    }
                }
                var result = iwat.await()
                listener.onNext(result)
            }, {
                if ((isViewModel && it.localizedMessage != "Job was cancelled") || !isViewModel) {
                    listener?.onError(ApiException(it))
                }
                //异常回调，这里可以拿到Throwable对象
                Log.d(TAG, "异常")
            }, {
                //开始回调，可以开启等待弹窗
                Log.d(TAG, "开始回调")
            }, {
                if (map.containsKey(key)) map.remove(key)
                //结束回调，可以销毁等待弹窗
                Log.d(TAG, "结束回调")
            })
            if (isDisposable && !isViewModel) map[key] = job
            return job
        }


        /**
         * 下载File文件
         */
        fun downloadFile(model: ViewModel?, path: String, savePath: String, listener: DownLoadResult<String>) {
            var rxLifeScope = model?.rxLifeScope ?: RxLifeScope()
            rxLifeScope.launch({
                RxHttp.get(path).toDownload(savePath, Dispatchers.Main) { listener?.Progress(it.progress, it.currentSize, it.totalSize) }.await()
                listener?.onNext(savePath)
            }, {
                if ((it.localizedMessage != "Job was cancelled")) {
                    listener?.onError(ApiException(it))
                }
                //异常回调，这里可以拿到Throwable对象
                Log.d(TAG, "异常")
            }, {
                //开始回调，可以开启等待弹窗
                Log.d(TAG, "开始回调")
            }, {
                //结束回调，可以销毁等待弹窗
                Log.d(TAG, "结束回调")
            })
        }

        /**
         * 断点下载File文件
         */
        fun downloadBreakpointFile(model: ViewModel?, path: String, savePath: String, listener: DownLoadResult<String>) {
            var rxLifeScope = model?.rxLifeScope ?: RxLifeScope()
            rxLifeScope.launch({
                val length = File(savePath).length()
                RxHttp.get(path).setRangeHeader(length, true).toDownload(savePath, Dispatchers.Main) {
                    listener?.Progress(it.progress, it.currentSize, it.totalSize)
                }
                listener?.onNext(savePath)
            }, {
                if (model != null || (!it.localizedMessage.endsWith("was cancelled"))) {
                    listener?.onError(ApiException(it))
                }
                //异常回调，这里可以拿到Throwable对象
                Log.d(TAG, "异常")
            }, {
                //开始回调，可以开启等待弹窗
                Log.d(TAG, "开始回调")
            }, {
                //结束回调，可以销毁等待弹窗
                Log.d(TAG, "结束回调")
            })
        }

        fun downloadBitmap(path: String, listener: DownLoadResult<Bitmap>) {
            var rxLifeScope = RxLifeScope()
            rxLifeScope.launch({
                //成功回调
                val bitmap = RxHttp.get(path).toBitmap().await()
                listener.onNext(bitmap)
            }, {
                if (!it.localizedMessage.endsWith("was cancelled")) {
                    listener?.onError(ApiException(it))
                }
                //异常回调，这里可以拿到Throwable对象
                Log.d(TAG, "异常")
            }, {
                //开始回调，可以开启等待弹窗
                Log.d(TAG, "开始回调")
            }, {
                //结束回调，可以销毁等待弹窗
                Log.d(TAG, "结束回调")
            })
        }
    }

    /**
     * 下载bitmap
     */
    fun downloadBitmap(model: ViewModel?, path: String, listener: DownLoadResult<Bitmap>) {
        var rxLifeScope = model?.rxLifeScope ?: RxLifeScope()
        rxLifeScope.launch({
            //成功回调
            val bitmap = RxHttp.get(path).toBitmap().await()
            listener.onNext(bitmap)
        }, {
            if (!it.localizedMessage.endsWith("was cancelled")) {
                listener?.onError(ApiException(it))
            }
            //异常回调，这里可以拿到Throwable对象
            Log.d(TAG, "异常")
        }, {
            //开始回调，可以开启等待弹窗
            Log.d(TAG, "开始回调")
        }, {
            //结束回调，可以销毁等待弹窗
            Log.d(TAG, "结束回调")
        })
    }
}