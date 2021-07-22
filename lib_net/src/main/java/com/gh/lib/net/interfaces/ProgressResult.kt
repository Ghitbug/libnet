package com.gh.lib.net.interfaces

interface ProgressResult {
    //progress 当前进度 0-100 currentSize 当前已下载的字节大小 totalSize 要下载的总字节大小
    fun Progress(progress: Int, currentSize: Long, totalSize: Long)
}