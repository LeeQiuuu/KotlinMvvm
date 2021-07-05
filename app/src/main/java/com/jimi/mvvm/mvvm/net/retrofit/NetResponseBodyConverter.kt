package com.jimi.app.mvvm.net.retrofit

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.jimi.app.mvvm.net.error.ApiException
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type

class NetResponseBodyConverter<T>(var gson: Gson, var adapter: TypeAdapter<T>) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        return value.use {
            val result = it.string()
            handleNetResult(result)
        }
    }


    @Throws(IOException::class)
    private fun handleNetResult(result: String): T {
        val netResult: AppResponseResult<T>
        try {
            val type: Type = object : TypeReference<AppResponseResult<T>>() {}.type
            netResult = JSON.parseObject(result, type)
        } catch (e: JsonSyntaxException) {
            if (!TextUtils.isEmpty(e.message) && e.message!!.contains("Expected BEGIN_OBJECT but was STRING")) {
                return result as T
            } else {
                e.printStackTrace()
                throw e
            }
        }
        if (netResult.code == AppResponseResult.SUCCESS) {
            val inputStream: InputStream = ByteArrayInputStream(result.toByteArray())
            val jsonReader = gson.newJsonReader(InputStreamReader(inputStream))
            return adapter.read(jsonReader)
        } else {
            throw ApiException(netResult.msg, netResult.code)
        }
    }
}