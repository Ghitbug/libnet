package com.gh.lib.net.interfaces

interface DownLoadResult<T> {
    /**
     * @param result
     */
    fun onNext(result: T)

    /**
     * @param e
     */
    fun onError(e: Throwable)

    //progress 当前进度 0-100 currentSize 当前已下载的字节大小 totalSize 要下载的总字节大小
    fun Progress(progress: Int, currentSize: Long, totalSize: Long)
}