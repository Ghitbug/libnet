package com.gh.lib.net.interfaces

open interface NetWorkResult {
    /**
     * @param result
     */
    fun onNext(result: String)

    /**
     * @param e
     */
    fun onError(e: Throwable)
}