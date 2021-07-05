package com.jimi.app.mvvm.utils

import android.util.Log
import com.jimi.app.BuildConfig

object LogUtil {
    private const val TAG = "mvvm_log"
    private const val TAG_NET = "mvvm_net"

    @JvmStatic
    fun i(message: String?) {
        if (BuildConfig.DEBUG) Log.i(TAG, message ?: "")
    }

    @JvmStatic
    fun d(message: String?) {
        if (BuildConfig.DEBUG) Log.d(TAG, message ?: "")
    }

    @JvmStatic
    fun e(message: String?) {
        if (BuildConfig.DEBUG) Log.e(TAG, message ?: "")
    }

    @JvmStatic
    fun i(tag: String, message: String?) {
        val customerTAG = StringBuilder(TAG).append("_").append(tag).toString()
        if (BuildConfig.DEBUG) Log.i(customerTAG, message ?: "")
    }

    @JvmStatic
    fun d(tag: String, message: String?) {
        val customerTAG = StringBuilder(TAG).append("_").append(tag).toString()
        if (BuildConfig.DEBUG) Log.d(customerTAG, message ?: "")
    }

    @JvmStatic
    fun e(tag: String, message: String?) {
        val customerTAG = StringBuilder(TAG).append("_").append(tag).toString()
        if (BuildConfig.DEBUG) Log.e(customerTAG, message ?: "")
    }

    @JvmStatic
    fun showHttpHeaderLog(message: String?) {
        if (BuildConfig.DEBUG) Log.d(TAG_NET, message ?: "")
    }

    @JvmStatic
    fun showHttpApiLog(message: String?) {
        if (BuildConfig.DEBUG) Log.w(TAG_NET, message ?: "")
    }

    @JvmStatic
    fun showHttpLog(message: String?) {
        if (BuildConfig.DEBUG) Log.i(TAG_NET, message ?: "")
    }
}