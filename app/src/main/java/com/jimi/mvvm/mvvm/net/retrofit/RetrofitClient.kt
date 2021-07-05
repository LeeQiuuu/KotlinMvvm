package com.jimi.app.mvvm.net.retrofit

import com.jimi.app.mvvm.net.ApiService
import com.jimi.app.mvvm.net.interceptor.AddParsInterceptor
import com.jimi.app.mvvm.net.interceptor.LoggingInterceptor
import com.jimi.app.mvvm.net.interceptor.SessionInterceptor
import com.jimi.app.utils.Constant
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 *Created by LeeQiuuu on 2021/5/28.
 */

object RetrofitClient {
    private var retrofit: Retrofit? = null
    val api: ApiService by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        create()
    }

    fun create(): ApiService = Retrofit.Builder()
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(NetConverterFactory.create())
            .baseUrl(Constant.API_HOST)
            .build().create(
                    ApiService::class.java
            )

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(SessionInterceptor())
                .addInterceptor(AddParsInterceptor())
                .addInterceptor(LoggingInterceptor())
                // .sslSocketFactory(SSLContextSecurity.createIgnoreVerifySSL("TLS"))
                .build()
    }
}