package com.jimi.app.mvvm.net

class URLConstant {
    companion object {

        private const val BASE_URL_DEBUG: String = "https://www.wanandroid.com/"
        private const val BASE_URL_RELEASE: String = "https://www.wanandroid.com/"

        val BASE_URL: String = if (com.jimi.app.mvvm.App.DEBUG) BASE_URL_DEBUG else BASE_URL_RELEASE

    }
}