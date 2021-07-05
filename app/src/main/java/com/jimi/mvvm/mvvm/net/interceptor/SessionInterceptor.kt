package com.jimi.app.mvvm.net.interceptor

import android.os.Build
import android.util.Log
import com.jimi.app.protocol.CookieUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


/**
 *Created by LeeQiuuu on 2021/5/26.
 *Describe:
 */
class SessionInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder: Request.Builder = request.newBuilder()
        if (CookieUtils.cookie != null) {
            builder.addHeader("Cookie", "SHAREJSESSIONID=" + CookieUtils.cookie.value())
            builder.addHeader("Connection", "close")
        } else {
            Log.e("Cookie", "Cookie not found")
        }
        return chain.proceed(builder.build())
    }
}