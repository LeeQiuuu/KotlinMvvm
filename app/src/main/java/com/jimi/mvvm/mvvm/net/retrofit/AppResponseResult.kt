package com.jimi.app.mvvm.net.retrofit

class AppResponseResult<T> {
    var code = 0

    var msg: String? = null

    var data: T? = null

    companion object {
        const val SUCCESS = 0
    }
}