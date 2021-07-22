package com.gh.lib.net

enum class HttpType(var ecode: String) {
    POST("POST"),
    GET("GET"),
    DEL("DEL"),
    PUT("PUT"),
    PATCH("PATCH"),
    BODY("BODY"),
}